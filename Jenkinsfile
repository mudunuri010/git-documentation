// Define Docker image name prefix
def dockerImagePrefix = 'saimudunuri9'

properties([
    parameters([
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'qa', 'staging', 'prod'],
            description: 'Select deployment environment'
        ),
        string(
            name: 'SERVER',
            defaultValue: '',
            description: 'Target server (leave empty to auto-select first server for environment)'
        ),
        string(
            name: 'CONTAINER_NAME',
            defaultValue: '',
            description: 'Container name (leave empty to auto-generate)'
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

    environment {
        IMAGE_NAME_TAG = "${dockerImagePrefix}/git-documentation:${params.ENVIRONMENT}-b${BUILD_NUMBER}"
        TARGET_SERVER = ''
        CONTAINER_NAME = ''
        HOST_PORT = ''
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Setup Configuration') {
            steps {
                script {
                    echo "=== Automated Configuration Setup ==="
                    echo "Environment: ${params.ENVIRONMENT}"
                    
                    // 1. Get available servers for environment
                    def serversOutput = sh(
                        script: "/var/jenkins_home/scripts/get_servers.sh ${params.ENVIRONMENT}",
                        returnStdout: true
                    ).trim()
                    def servers = serversOutput.split(/\n/)
                    echo "Available servers: ${servers.join(', ')}"
                    echo "DEBUG: servers array size = ${servers.size()}"
                    echo "DEBUG: servers[0] = '${servers[0]}'"
                    
                    // 2. Select target server (use provided or auto-select first)
                    if (params.SERVER && !params.SERVER.isEmpty()) {
                        env.TARGET_SERVER = params.SERVER
                        echo "Using specified server: ${env.TARGET_SERVER}"
                    } else {
                        env.TARGET_SERVER = servers[0].trim()
                        echo "Auto-selected server: ${env.TARGET_SERVER}"
                    }
                    
                    // 3. Generate container name
                    if (params.CONTAINER_NAME && !params.CONTAINER_NAME.isEmpty()) {
                        env.CONTAINER_NAME = params.CONTAINER_NAME
                        echo "Using specified container name: ${env.CONTAINER_NAME}"
                    } else {
                        def containerName = sh(
                            script: "/var/jenkins_home/scripts/generate_container_name.sh '${env.TARGET_SERVER}'",
                            returnStdout: true
                        ).trim()
                        env.CONTAINER_NAME = containerName
                        echo "Auto-generated container name: ${env.CONTAINER_NAME}"
                    }
                    
                    // 4. Get port for environment
                    def port = sh(
                        script: "/var/jenkins_home/scripts/get_port.sh ${params.ENVIRONMENT}",
                        returnStdout: true
                    ).trim()
                    env.HOST_PORT = port
                    echo "Assigned port: ${env.HOST_PORT}"
                    
                    // 5. Summary
                    echo "=== Configuration Complete ==="
                    echo "Environment:   ${params.ENVIRONMENT}"
                    echo "Target Server: ${env.TARGET_SERVER}"
                    echo "Container:     ${env.CONTAINER_NAME}"
                    echo "Port:          ${env.HOST_PORT}"
                    echo "Image:         ${env.IMAGE_NAME_TAG}"
                    echo "=============================="
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
                    echo "=== Checking for existing container: ${env.CONTAINER_NAME} ==="
                    sh(script: "docker stop ${env.CONTAINER_NAME} || true", returnStatus: true)
                    sh(script: "docker rm ${env.CONTAINER_NAME} || true", returnStatus: true)
                    echo "Cleanup finished (errors ignored)."
                }
            }
        }

        stage('Deploy Container') {
            steps {
                script {
                    echo "=== Deploying Application Container: ${env.CONTAINER_NAME} ==="
                    sh """
                        docker run -d \\
                            --name ${env.CONTAINER_NAME} \\
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
                    sh "docker ps | grep ${env.CONTAINER_NAME}"
                    echo "✅ Container is running!"
                    echo "🌐 Application live at: http://localhost:${env.HOST_PORT}"
                }
            }
        }
    }

    post {
        success {
            script {
                echo "🎉 Pipeline successful!"
                echo "📦 Application: ${env.CONTAINER_NAME}"
                echo "🌍 Environment: ${params.ENVIRONMENT}"
                echo "🖥️  Server: ${env.TARGET_SERVER}"
                echo "🌐 Access: http://localhost:${env.HOST_PORT}"
            }
        }
        failure {
            echo "❌ Pipeline failed. Please review the console output for errors."
        }
    }
}