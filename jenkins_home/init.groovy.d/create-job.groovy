import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import hudson.model.ChoiceParameterDefinition
import hudson.model.BooleanParameterDefinition

def jenkins = Jenkins.instance
def jobName = 'git-documentation-pipeline'

// Check if job already exists
def job = jenkins.getItem(jobName)

if (job == null) {
    println "Creating job: ${jobName}"
    
    // Create the pipeline job
    job = jenkins.createProject(WorkflowJob.class, jobName)
    job.setDescription('Automated pipeline for git-documentation - Created by init.groovy.d')
    
    // Define parameters
    def parameters = [
        new ChoiceParameterDefinition('ENVIRONMENT', ['dev', 'qa', 'staging', 'prod'] as String[], 'Select deployment environment'),
        new StringParameterDefinition('SERVER', '', 'Target server (will be populated based on environment)'),
        new StringParameterDefinition('CONTAINER_NAME', '', 'Container name (auto-generated)'),
        new StringParameterDefinition('IMAGE_NAME', 'saimudunuri9/git-documentation', 'Docker image name'),
        new StringParameterDefinition('IMAGE_TAG', '4', 'Docker image tag/version'),
        new StringParameterDefinition('GIT_BRANCH', 'master', 'Git branch to checkout'),
        new StringParameterDefinition('GIT_URL', 'https://github.com/mudunuri010/git-documentation', 'Git repository URL'),
        new BooleanParameterDefinition('FORCE_REMOVE', true, 'Force remove existing container before deploy?')
    ]
    
    def paramProp = new ParametersDefinitionProperty(parameters)
    job.addProperty(paramProp)
    
    // Define the pipeline script
    def pipelineScript = '''
pipeline {
    agent any
    
    stages {
        stage('Setup Parameters') {
            steps {
                script {
                    // Get servers for the environment
                    if (!params.SERVER || params.SERVER.isEmpty()) {
                        def serverCmd = "sh /var/jenkins_home/scripts/get_servers.sh ${params.ENVIRONMENT}"
                        def servers = serverCmd.execute().text.trim().split('\\n')
                        env.TARGET_SERVER = servers[0]
                    } else {
                        env.TARGET_SERVER = params.SERVER
                    }
                    
                    // Generate container name
                    if (!params.CONTAINER_NAME || params.CONTAINER_NAME.isEmpty()) {
                        def nameCmd = "sh /var/jenkins_home/scripts/generate_container_name.sh ${env.TARGET_SERVER}"
                        env.CONTAINER_NAME = nameCmd.execute().text.trim()
                    } else {
                        env.CONTAINER_NAME = params.CONTAINER_NAME
                    }
                    
                    // Get port for environment
                    def portCmd = "sh /var/jenkins_home/scripts/get_port.sh ${params.ENVIRONMENT}"
                    env.HOST_PORT = portCmd.execute().text.trim()
                }
            }
        }
        
        stage('Display Configuration') {
            steps {
                script {
                    echo "Deploying with the following configuration:"
                    echo "------------------------------------------"
                    echo "Environment:     ${params.ENVIRONMENT}"
                    echo "Target Server:   ${env.TARGET_SERVER}"
                    echo "Container Name:  ${env.CONTAINER_NAME}"
                    echo "Image:           ${params.IMAGE_NAME}:${params.IMAGE_TAG}"
                    echo "Host Port:       ${env.HOST_PORT}"
                    echo "Git Branch:      ${params.GIT_BRANCH}"
                    echo "Force Remove:    ${params.FORCE_REMOVE}"
                    echo "------------------------------------------"
                }
            }
        }
        
        stage('Cleanup') {
            when {
                expression { params.FORCE_REMOVE == true }
            }
            steps {
                script {
                    echo "=== Checking for existing container ==="
                    sh """
                        if docker ps -a --format '{{.Names}}' | grep -q '^${env.CONTAINER_NAME}\\$'; then
                            echo "Container ${env.CONTAINER_NAME} exists. Removing..."
                            docker stop ${env.CONTAINER_NAME} || true
                            docker rm ${env.CONTAINER_NAME} || true
                        else
                            echo "No existing container found."
                        fi
                    """
                }
            }
        }
        
        stage('Deploy Container') {
            steps {
                script {
                    echo "=== Deploying Application ==="
                    sh """
                        docker run -d \\
                            --name ${env.CONTAINER_NAME} \\
                            -p ${env.HOST_PORT}:3000 \\
                            ${params.IMAGE_NAME}:${params.IMAGE_TAG}
                    """
                    
                    echo "✅ Deployment successful!"
                    echo "Container: ${env.CONTAINER_NAME}"
                    echo "Access at: http://localhost:${env.HOST_PORT}"
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                script {
                    echo "=== Verifying Deployment ==="
                    sh "docker ps | grep ${env.CONTAINER_NAME}"
                    echo "✅ Container is running!"
                }
            }
        }
    }
    
    post {
        success {
            echo "🎉 Deployment completed successfully!"
        }
        failure {
            echo "❌ Deployment failed. Please review the console output for errors."
        }
    }
}
'''
    
    def definition = new CpsFlowDefinition(pipelineScript, false)
    job.setDefinition(definition)
    
    // Save the job
    job.save()
    
    println "✅ Job '${jobName}' created successfully!"
} else {
    println "ℹ️  Job '${jobName}' already exists. Skipping creation."
}