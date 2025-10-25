import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import hudson.model.BooleanParameterDefinition

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

// Try to use Active Choices plugin - wrapped in try-catch
def parameters = []

try {
    // Load Active Choices classes
    def ChoiceParameter = Class.forName('org.biouno.unochoice.ChoiceParameter')
    def CascadeChoiceParameter = Class.forName('org.biouno.unochoice.CascadeChoiceParameter')
    def GroovyScript = Class.forName('org.biouno.unochoice.model.GroovyScript')
    def ScriptlerScript = Class.forName('org.biouno.unochoice.model.ScriptlerScript')
    
    println "Active Choices plugin classes loaded successfully"
    
    // 1. ENVIRONMENT - Choice Parameter
    def environmentScript = new GroovyScript(
        ScriptlerScript.newInstance('', []),
        'return ["dev", "qa", "staging", "prod"]'
    )
    def environmentParam = ChoiceParameter.newInstance(
        'ENVIRONMENT',
        'Select deployment environment',
        null,
        environmentScript,
        ChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
        false,
        1
    )
    parameters.add(environmentParam)
    println "Added ENVIRONMENT parameter"
    
    // 2. SERVER - Cascade Choice Parameter
    def serverScriptText = '''
def env = ENVIRONMENT ?: "dev"
def command = ["sh", "/var/jenkins_home/scripts/get_servers.sh", env]
def process = command.execute()
process.waitFor()
def output = process.in.text.trim()
if (output) {
    return output.split(/\\n/) as List
} else {
    return ["error"]
}
'''
    
    def serverScript = new GroovyScript(
        ScriptlerScript.newInstance('', []),
        serverScriptText
    )
    def serverParam = CascadeChoiceParameter.newInstance(
        'SERVER',
        'Select the server dynamically based on environment',
        null,
        serverScript,
        CascadeChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
        'ENVIRONMENT',
        false,
        1
    )
    parameters.add(serverParam)
    println "Added SERVER parameter"
    
    // 3. CONTAINER_NAME - Cascade Choice Parameter
    def containerScriptText = '''
if (SERVER && !SERVER.startsWith("error")) {
    def command = ["sh", "/var/jenkins_home/scripts/generate_container_name.sh", SERVER]
    def process = command.execute()
    process.waitFor()
    def output = process.in.text.trim()
    if (output) {
        return [output]
    }
}
return ["error"]
'''
    
    def containerScript = new GroovyScript(
        ScriptlerScript.newInstance('', []),
        containerScriptText
    )
    def containerParam = CascadeChoiceParameter.newInstance(
        'CONTAINER_NAME',
        'Auto-generate container name from server selection',
        null,
        containerScript,
        CascadeChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
        'SERVER',
        false,
        1
    )
    parameters.add(containerParam)
    println "Added CONTAINER_NAME parameter"
    
} catch (ClassNotFoundException e) {
    println "⚠️  Active Choices plugin not found or not loaded yet. Using fallback string parameters."
    println "Error: ${e.message}"
    
    // Fallback: Use simple string parameters if Active Choices isn't available
    parameters.add(new StringParameterDefinition('ENVIRONMENT', 'dev', 'Environment (dev/qa/staging/prod)'))
    parameters.add(new StringParameterDefinition('SERVER', '', 'Target server'))
    parameters.add(new StringParameterDefinition('CONTAINER_NAME', '', 'Container name'))
} catch (Exception e) {
    println "⚠️  Error creating Active Choices parameters: ${e.message}"
    e.printStackTrace()
    
    // Fallback
    parameters.add(new StringParameterDefinition('ENVIRONMENT', 'dev', 'Environment (dev/qa/staging/prod)'))
    parameters.add(new StringParameterDefinition('SERVER', '', 'Target server'))
    parameters.add(new StringParameterDefinition('CONTAINER_NAME', '', 'Container name'))
}

// Add regular string/boolean parameters
parameters.add(new StringParameterDefinition('GIT_BRANCH', 'master', 'Git branch to checkout'))
parameters.add(new StringParameterDefinition('GIT_URL', 'https://github.com/mudunuri010/git-documentation', 'Git repository URL'))
parameters.add(new BooleanParameterDefinition('FORCE_REMOVE', true, 'Force remove existing container before deploy?'))

println "Adding parameters to job..."
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

                    // Get port for environment
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

println "Setting pipeline definition..."
def definition = new CpsFlowDefinition(pipelineScript, true)
job.setDefinition(definition)

// Save the job
println "Saving job..."
job.save()

println "✅ Job '${jobName}' created successfully with dynamic parameters!"