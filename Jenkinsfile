@Library('shared-library') _

properties([
    parameters([
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'staging', 'prod'],
            description: 'Select deployment environment'
        ),
        [$class: 'CascadeChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: 'Select the server dynamically from script',
            name: 'SERVER',
            referencedParameters: 'ENVIRONMENT',
            script: [
                $class: 'GroovyScript',
                fallbackScript: [
                    classpath: [],
                    sandbox: false,
                    script: 'return ["FALLBACK-CHECK-LOGS"]'
                ],
                script: [
                    classpath: [],
                    sandbox: false,
                    script: '''
                        try {
                            def env = ENVIRONMENT ?: "dev"
                            def command = ["/var/jenkins_home/scripts/get_servers.sh", env]
                            def process = command.execute()
                            process.waitFor()
                            
                            def output = process.in.text.trim()
                            def exitCode = process.exitValue()
                            
                            if (exitCode != 0) {
                                return ["ERR-ExitCode-" + exitCode]
                            }
                            
                            if (!output || output.isEmpty()) {
                                return ["ERR-NoOutput"]
                            }
                            
                            def servers = output.split(/\n/) as List
                            
                            if (!servers || servers.isEmpty()) {
                                return ["ERR-EmptyList"]
                            }
                            
                            return servers
                            
                        } catch (Exception e) {
                            return ["EXCEPTION-" + e.class.simpleName]
                        }
                    '''
                ]
            ]
        ],
        [$class: 'CascadeChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: 'Auto-generate container name from script',
            name: 'CONTAINER_NAME',
            referencedParameters: 'SERVER',
            script: [
                $class: 'GroovyScript',
                fallbackScript: [
                    classpath: [],
                    sandbox: false,
                    script: 'return ["default-container"]'
                ],
                script: [
                    classpath: [],
                    sandbox: false,
                    script: '''
                        try {
                            if (!SERVER || SERVER.startsWith("ERR-") || SERVER.startsWith("EXCEPTION-")) {
                                return ["error-container"]
                            }
                            
                            def command = ["/var/jenkins_home/scripts/generate_container_name.sh", SERVER]
                            def process = command.execute()
                            process.waitFor()

                            def output = process.in.text.trim()

                            if (process.exitValue() == 0 && output) {
                                return [output]
                            } else {
                                return ["error-container"]
                            }
                        } catch (Exception e) {
                            return ["exception-container"]
                        }
                    '''
                ]
            ]
        ],
        string(
            name: 'IMAGE_NAME',
            defaultValue: 'saimudunuri9/git-documentation',
            description: 'Docker image name'
        ),
        string(
            name: 'IMAGE_TAG',
            defaultValue: '4',
            description: 'Docker image tag/version'
        ),
        string(
            name: 'GIT_BRANCH',
            defaultValue: 'master',
            description: 'Git branch to checkout'
        ),
        string(
            name: 'GIT_URL',
            defaultValue: 'https://github.com/mudunuri010/git-documentation',
            description: 'Git repository URL'
        ),
        booleanParam(
            name: 'FORCE_REMOVE',
            defaultValue: true,
            description: 'Force remove existing container before deploy?'
        )
    ])
])

pipeline {
    agent any
    
    stages {
        stage('Display Configuration') {
            steps {
                script {
                    echo "Deploying with the following configuration:"
                    echo "------------------------------------------"
                    echo "Environment:     ${params.ENVIRONMENT}"
                    echo "Target Server:   ${params.SERVER}"
                    echo "Container Name:  ${params.CONTAINER_NAME}"
                    echo "Image:           ${params.IMAGE_NAME}:${params.IMAGE_TAG}"
                    echo "Git Branch:      ${params.GIT_BRANCH}"
                    echo "Force Remove:    ${params.FORCE_REMOVE}"
                    
                    // Get port for environment
                    def portCommand = ["/var/jenkins_home/scripts/get_port.sh", params.ENVIRONMENT]
                    def portProcess = portCommand.execute()
                    portProcess.waitFor()
                    
                    env.HOST_PORT = portProcess.in.text.trim()
                    echo "Using port '${env.HOST_PORT}' for environment '${params.ENVIRONMENT}'"
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
                        if docker ps -a --format '{{.Names}}' | grep -q '^${params.CONTAINER_NAME}\$'; then
                            echo "Container ${params.CONTAINER_NAME} exists. Removing..."
                            docker stop ${params.CONTAINER_NAME} || true
                            docker rm ${params.CONTAINER_NAME} || true
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
                            --name ${params.CONTAINER_NAME} \\
                            -p ${env.HOST_PORT}:3000 \\
                            ${params.IMAGE_NAME}:${params.IMAGE_TAG}
                    """
                    
                    echo "✅ Deployment successful!"
                    echo "Container: ${params.CONTAINER_NAME}"
                    echo "Access at: http://localhost:${env.HOST_PORT}"
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                script {
                    echo "=== Verifying Deployment ==="
                    sh "docker ps | grep ${params.CONTAINER_NAME}"
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
