// Define Docker image name prefix (e.g., your Docker Hub username)
def dockerImagePrefix = 'saimudunuri9'

properties([
    parameters([
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'qa', 'staging', 'prod'],
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
                    script: 'return ["FALLBACK-CHECK-LOGS"]' 
                ],
                script: [
                    classpath: [],
                    script: '''
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
                    script: 'return ["default-container"]' 
                ],
                script: [
                    classpath: [],
                    script: '''
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
                ]
            ]
        ],
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

    environment {
        IMAGE_NAME_TAG = "${dockerImagePrefix}/git-documentation:${params.ENVIRONMENT}-b${BUILD_NUMBER}"
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

                    // FIXED: Use sh() step instead of .execute()
                    try {
                        env.HOST_PORT = sh(
                            script: "/var/jenkins_home/scripts/get_port.sh ${params.ENVIRONMENT}",
                            returnStdout: true
                        ).trim()
                        
                        if (!env.HOST_PORT || env.HOST_PORT.isEmpty()) {
                            error "Port script returned empty value"
                        }
                    } catch (Exception e) {
                        echo "⚠️ Warning: Failed to get port from script: ${e.message}"
                        // Fallback to hardcoded ports
                        def portMap = [dev: '3001', qa: '3002', staging: '3003', prod: '3004']
                        env.HOST_PORT = portMap[params.ENVIRONMENT] ?: '8080'
                        echo "Using fallback port: ${env.HOST_PORT}"
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
