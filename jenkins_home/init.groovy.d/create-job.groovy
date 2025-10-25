import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import hudson.model.BooleanParameterDefinition
import org.biouno.unochoice.ChoiceParameter
import org.biouno.unochoice.CascadeChoiceParameter
import org.biouno.unochoice.model.GroovyScript

def jenkins = Jenkins.instance
def jobName = 'git-documentation-pipeline'

// Delete existing job if it exists
def existingJob = jenkins.getItem(jobName)
if (existingJob != null) {
    println "Deleting existing job: ${jobName}"
    existingJob.delete()
}

println "Creating job: ${jobName}"

// Create the pipeline job
def job = jenkins.createProject(WorkflowJob.class, jobName)
job.setDescription('Automated pipeline for git-documentation with dynamic parameters')

// Define parameters using Active Choices plugin
def parameters = []

// 1. ENVIRONMENT - Choice Parameter
def environmentParam = new ChoiceParameter(
    'ENVIRONMENT',
    'Select deployment environment',
    new GroovyScript(
        new org.biouno.unochoice.model.ScriptlerScript('', []),
        'return ["dev", "qa", "staging", "prod"]'
    ),
    org.biouno.unochoice.ChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
    false,
    1
)
parameters.add(environmentParam)

// 2. SERVER - Cascade Choice Parameter (depends on ENVIRONMENT)
def serverScript = '''
try {
    def env = ENVIRONMENT ?: "dev"
    def command = ["sh", "/var/jenkins_home/scripts/get_servers.sh", env]
    def process = command.execute()
    process.waitFor()
    def output = process.in.text.trim()
    if (process.exitValue() != 0 || !output) { 
        return ["ERR-ScriptFailed"] 
    }
    def servers = output.split(/\\n/) as List
    return servers ?: ["ERR-NoServers"]
} catch (Exception e) { 
    return ["EXCEPTION-" + e.class.simpleName] 
}
'''

def serverParam = new CascadeChoiceParameter(
    'SERVER',
    'Select the server dynamically based on environment',
    new GroovyScript(
        new org.biouno.unochoice.model.ScriptlerScript('', []),
        serverScript
    ),
    org.biouno.unochoice.ChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
    'ENVIRONMENT',
    false,
    1
)
parameters.add(serverParam)

// 3. CONTAINER_NAME - Cascade Choice Parameter (depends on SERVER)
def containerScript = '''
try {
    if (!SERVER || SERVER.startsWith("ERR-") || SERVER.startsWith("EXCEPTION-")) { 
        return ["error-server-param"] 
    }
    def command = ["sh", "/var/jenkins_home/scripts/generate_container_name.sh", SERVER]
    def process = command.execute()
    process.waitFor()
    def output = process.in.text.trim()
    if (process.exitValue() == 0 && output) { 
        return [output] 
    } else { 
        return ["error-generating-name"] 
    }
} catch (Exception e) { 
    return ["exception-generating-name"] 
}
'''

def containerParam = new CascadeChoiceParameter(
    'CONTAINER_NAME',
    'Auto-generate container name from server selection',
    new GroovyScript(
        new org.biouno.unochoice.model.ScriptlerScript('', []),
        containerScript
    ),
    org.biouno.unochoice.ChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
    'SERVER',
    false,
    1
)
parameters.add(containerParam)

// 4. Regular string parameters
parameters.add(new StringParameterDefinition('GIT_BRANCH', 'master', 'Git branch to checkout'))
parameters.add(new StringParameterDefinition('GIT_URL', 'https://github.com/mudunuri010/git-documentation', 'Git repository URL'))
parameters.add(new BooleanParameterDefinition('FORCE_REMOVE', true, 'Force remove existing container before deploy?'))

def paramProp = new ParametersDefinitionProperty(parameters)
job.addProperty(paramProp)

// Embed the pipeline script directly
def pipelineScript = '''
pipeline {
    agent any

    environment {
        IMAGE_NAME_TAG = "saimudunuri9/git-documentation:${params.ENVIRONMENT}-b${BUILD_NUMBER}"
        HOST_PORT = ''
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Initialize & Get Port') {
            steps {
                script {
                    echo "Starting Build #${BUILD_NUMBER}"
                    echo "Image to Build/Deploy: ${env.IMAGE_NAME_TAG}"
                    echo "Environment:     ${params.ENVIRONMENT}"
                    echo "Target Server:   ${params.SERVER}"
                    echo "Container Name:  ${params.CONTAINER_NAME}"
                    echo "Git Branch:      ${params.GIT_BRANCH}"
                    echo "Git URL:         ${params.GIT_URL}"
                    echo "Force Remove:    ${params.FORCE_REMOVE}"

                    def portCommand = ["sh", "/var/jenkins_home/scripts/get_port.sh", params.ENVIRONMENT]
                    def portProcess = portCommand.execute()
                    portProcess.waitFor()
                    env.HOST_PORT = portProcess.in.text.trim()
                    if (portProcess.exitValue() != 0 || !env.HOST_PORT) {
                        error "Failed to get port for environment ${params.ENVIRONMENT}"
                    }
                    echo "Using port '${env.HOST_PORT}' for environment '${params.ENVIRONMENT}'"
                    echo "------------------------------------------"
                }
            }
        }

        stage('Checkout Code') {
            steps {
                echo "=== Checking out code from ${params.GIT_URL} branch ${params.GIT_BRANCH} ==="
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${params.GIT_BRANCH}"]],
                    userRemoteConfigs: [[url: params.GIT_URL, credentialsId: 'git-credentials']],
                    extensions: [[$class: 'CleanBeforeCheckout']]
                ])
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "=== Building Docker Image: ${env.IMAGE_NAME_TAG} ==="
                    def customImage = docker.build(env.IMAGE_NAME_TAG, ".")
                    echo "✅ Docker Image Built Successfully"
                }
            }
        }

        stage('Cleanup Existing Container') {
            when {
                expression { params.FORCE_REMOVE == true }
            }
            steps {
                script {
                    echo "=== Checking for existing container: ${params.CONTAINER_NAME} ==="
                    sh(script: "docker stop ${params.CONTAINER_NAME} || true", returnStatus: true)
                    sh(script: "docker rm ${params.CONTAINER_NAME} || true", returnStatus: true)
                    echo "Cleanup finished (errors ignored)."
                }
            }
        }

        stage('Deploy Container') {
            steps {
                script {
                    echo "=== Deploying Application Container: ${params.CONTAINER_NAME} ==="
                    sh """
                        docker run -d \\
                            --name ${params.CONTAINER_NAME} \\
                            -p ${env.HOST_PORT}:3000 \\
                            ${env.IMAGE_NAME_TAG}
                    """
                    echo "✅ Deployment successful!"
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    echo "=== Verifying Deployment ==="
                    sleep 5
                    sh "docker ps | grep ${params.CONTAINER_NAME}"
                    echo "✅ Container is running!"
                    echo "Application should be live at: http://localhost:${env.HOST_PORT}"
                }
            }
        }
    }

    post {
        success {
            script {
                echo "🎉 Pipeline successful!"
                echo "Application '${params.CONTAINER_NAME}' for environment '${params.ENVIRONMENT}' is live."
                echo "Access it at: http://localhost:${env.HOST_PORT}"
            }
        }
        failure {
            echo "❌ Pipeline failed. Please review the console output for errors."
        }
    }
}
'''

def definition = new CpsFlowDefinition(pipelineScript, true)
job.setDefinition(definition)

// Save the job
job.save()

println "✅ Job '${jobName}' created successfully with embedded pipeline and dynamic parameters!"