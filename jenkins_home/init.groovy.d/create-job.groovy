import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import hudson.model.BooleanParameterDefinition
import hudson.model.ChoiceParameterDefinition

def jenkins = Jenkins.instance
def jobName = 'git-documentation-pipeline'

println "🔍 Checking for existing job: ${jobName}"

// Delete existing job if it exists
def existingJob = jenkins.getItem(jobName)
if (existingJob != null) {
    println "🧹 Deleting existing job..."
    existingJob.delete()
}

println "⚙️ Creating new job: ${jobName}"

// Create the pipeline job
def job = jenkins.createProject(WorkflowJob.class, jobName)
job.setDescription('Automated CI/CD pipeline for git-documentation with dynamic parameters')

// Define parameters list
def parameters = []

try {
    // Try to use Active Choices plugin
    println "📦 Attempting to load Active Choices plugin classes..."
    
    def ChoiceParameter = Class.forName('org.biouno.unochoice.ChoiceParameter')
    def CascadeChoiceParameter = Class.forName('org.biouno.unochoice.CascadeChoiceParameter')
    def GroovyScript = Class.forName('org.biouno.unochoice.model.GroovyScript')
    
    println "✅ Active Choices plugin classes loaded successfully!"
    
    // 1. ENVIRONMENT - Active Choice Parameter
    def envScript = new GroovyScript(
        'return ["dev", "qa", "staging", "prod"]',
        'return ["dev"]'
    )
    
    def envParam = ChoiceParameter.newInstance(
        'ENVIRONMENT',
        'Select deployment environment',
        '',
        envScript,
        'SINGLE_SELECT',
        false,
        1
    )
    parameters.add(envParam)
    println "✅ Added ENVIRONMENT parameter (Active Choices)"
    
    // 2. SERVER - Active Choices Reactive Parameter
    def serverScript = new GroovyScript(
        '''
def env = ENVIRONMENT ?: "dev"
def command = ["sh", "-c", "/var/jenkins_home/scripts/get_servers.sh " + env]
def process = command.execute()
process.waitFor()
def output = process.in.text.trim()
return output ? output.split("\\n") as List : ["error-no-servers"]
        ''',
        'return ["dev-server-01"]'
    )
    
    def serverParam = CascadeChoiceParameter.newInstance(
        'SERVER',
        'Server (auto-populated based on environment)',
        '',
        serverScript,
        'SINGLE_SELECT',
        'ENVIRONMENT',
        false,
        1
    )
    parameters.add(serverParam)
    println "✅ Added SERVER parameter (Active Choices Reactive)"
    
    // 3. CONTAINER_NAME - Active Choices Reactive Parameter
    def containerScript = new GroovyScript(
        '''
if (SERVER && !SERVER.contains("error")) {
    def command = ["sh", "-c", "/var/jenkins_home/scripts/generate_container_name.sh " + SERVER]
    def process = command.execute()
    process.waitFor()
    def output = process.in.text.trim()
    return output ? [output] : ["error-generating-name"]
}
return ["select-server-first"]
        ''',
        'return ["default-container"]'
    )
    
    def containerParam = CascadeChoiceParameter.newInstance(
        'CONTAINER_NAME',
        'Container name (auto-generated from server)',
        '',
        containerScript,
        'SINGLE_SELECT',
        'SERVER',
        false,
        1
    )
    parameters.add(containerParam)
    println "✅ Added CONTAINER_NAME parameter (Active Choices Reactive)"
    
} catch (ClassNotFoundException e) {
    println "⚠️  Active Choices plugin not available. Using simple parameters."
    
    // Fallback to simple parameters
    parameters.add(new ChoiceParameterDefinition(
        'ENVIRONMENT',
        ['dev', 'qa', 'staging', 'prod'] as String[],
        'Select deployment environment'
    ))
    parameters.add(new StringParameterDefinition('SERVER', 'dev-server-01', 'Target server'))
    parameters.add(new StringParameterDefinition('CONTAINER_NAME', '', 'Container name (auto-generated)'))
} catch (Exception e) {
    println "⚠️  Error creating Active Choices parameters: ${e.message}"
    e.printStackTrace()
    
    // Fallback
    parameters.add(new ChoiceParameterDefinition(
        'ENVIRONMENT',
        ['dev', 'qa', 'staging', 'prod'] as String[],
        'Select deployment environment'
    ))
    parameters.add(new StringParameterDefinition('SERVER', 'dev-server-01', 'Target server'))
    parameters.add(new StringParameterDefinition('CONTAINER_NAME', '', 'Container name (auto-generated)'))
}

// Add remaining parameters
parameters.add(new StringParameterDefinition('GIT_BRANCH', 'master', 'Git branch to checkout'))
parameters.add(new StringParameterDefinition('GIT_URL', 'https://github.com/mudunuri010/git-documentation', 'Git repository URL'))
parameters.add(new BooleanParameterDefinition('FORCE_REMOVE', true, 'Force remove existing container before deploy?'))

println "📝 Adding all parameters to job..."
def paramProp = new ParametersDefinitionProperty(parameters)
job.addProperty(paramProp)

// Embedded pipeline script
def pipelineScript = '''
pipeline {
    agent any
    
    environment {
        IMAGE_NAME_TAG = "saimudunuri9/git-documentation:${params.ENVIRONMENT}-b${BUILD_NUMBER}"
        HOST_PORT = ""
    }
    
    stages {
        stage("Setup") {
            steps {
                script {
                    // Get port for environment
                    def portCmd = ["sh", "/var/jenkins_home/scripts/get_port.sh", params.ENVIRONMENT]
                    def portProc = portCmd.execute()
                    portProc.waitFor()
                    env.HOST_PORT = portProc.in.text.trim()
                    
                    echo "=== Build Configuration ==="
                    echo "Environment:   ${params.ENVIRONMENT}"
                    echo "Server:        ${params.SERVER}"
                    echo "Container:     ${params.CONTAINER_NAME}"
                    echo "Port:          ${env.HOST_PORT}"
                    echo "Image:         ${env.IMAGE_NAME_TAG}"
                    echo "=========================="
                }
            }
        }
        
        stage("Checkout") {
            steps {
                checkout([
                    $class: "GitSCM",
                    branches: [[name: "*/${params.GIT_BRANCH}"]],
                    userRemoteConfigs: [[url: params.GIT_URL, credentialsId: "git-credentials"]],
                    extensions: [[$class: "CleanBeforeCheckout"]]
                ])
            }
        }
        
        stage("Build Image") {
            steps {
                script {
                    echo "Building: ${env.IMAGE_NAME_TAG}"
                    docker.build(env.IMAGE_NAME_TAG, ".")
                }
            }
        }
        
        stage("Cleanup") {
            when { expression { params.FORCE_REMOVE } }
            steps {
                sh "docker stop ${params.CONTAINER_NAME} || true"
                sh "docker rm ${params.CONTAINER_NAME} || true"
            }
        }
        
        stage("Deploy") {
            steps {
                sh """
                    docker run -d \\
                        --name ${params.CONTAINER_NAME} \\
                        -p ${env.HOST_PORT}:3000 \\
                        ${env.IMAGE_NAME_TAG}
                """
            }
        }
        
        stage("Verify") {
            steps {
                sleep 5
                sh "docker ps | grep ${params.CONTAINER_NAME}"
                echo "✅ Live at: http://localhost:${env.HOST_PORT}"
            }
        }
    }
    
    post {
        success {
            echo "🎉 Deployment successful!"
            echo "Access: http://localhost:${env.HOST_PORT}"
        }
        failure {
            echo "❌ Deployment failed!"
        }
    }
}
'''

println "📄 Setting pipeline definition..."
def definition = new CpsFlowDefinition(pipelineScript, true)
job.setDefinition(definition)

// Save the job
println "💾 Saving job..."
job.save()

println "✅ Job '${jobName}' created successfully!"
println "✨ Job is ready with dynamic cascading parameters!"