// Define Docker image name prefix
def dockerImagePrefix = 'saimudunuri9'

// Define dynamic parameters using Active Choices Plugin
properties([
    parameters([
        // 1️⃣ Environment dropdown
        [
            $class: 'ChoiceParameter',
            name: 'ENVIRONMENT',
            description: 'Select deployment environment',
            choiceType: 'PT_SINGLE_SELECT',
            script: [
                $class: 'GroovyScript',
                script: [
                    script: "return ['dev', 'qa', 'staging', 'prod']",
                    sandbox: true
                ],
                fallbackScript: [
                    script: "return ['dev']",
                    sandbox: true
                ]
            ]
        ],
        
        // 2️⃣ Server dropdown dynamically populated based on environment
        [
            $class: 'CascadeChoiceParameter',
            name: 'SERVER',
            description: 'Select server based on environment',
            choiceType: 'PT_SINGLE_SELECT',
            referencedParameters: 'ENVIRONMENT',
            script: [
                $class: 'GroovyScript',
                script: [
                    script: '''
                        def env = ENVIRONMENT ?: "dev"
                        def cmd = ["bash", "/var/jenkins_home/scripts/get_servers.sh", env]
                        def process = cmd.execute()
                        process.waitFor()
                        def output = process.in.text.trim()
                        return output ? output.split("\\n").toList() : ["no-server-found"]
                    ''',
                    sandbox: false
                ],
                fallbackScript: [
                    script: "return ['fallback-server']",
                    sandbox: true
                ]
            ]
        ],
        
        // 3️⃣ Container name auto-generated based on server
        [
            $class: 'CascadeChoiceParameter',
            name: 'CONTAINER_NAME',
            description: 'Auto-generated container name based on server',
            choiceType: 'PT_SINGLE_SELECT',
            referencedParameters: 'SERVER',
            script: [
                $class: 'GroovyScript',
                script: [
                    script: '''
                        if (SERVER && !SERVER.contains("error") && !SERVER.contains("fallback")) {
                            def cmd = ["bash", "/var/jenkins_home/scripts/generate_container_name.sh", SERVER]
                            def process = cmd.execute()
                            process.waitFor()
                            def output = process.in.text.trim()
                            return output ? [output] : ["error-container"]
                        }
                        return ["no-container"]
                    ''',
                    sandbox: false
                ],
                fallbackScript: [
                    script: "return ['fallback-container']",
                    sandbox: true
                ]
            ]
        ],
        
        // 4️⃣ Port auto-generated based on environment
        [
            $class: 'CascadeChoiceParameter',
            name: 'PORT',
            description: 'Auto-selected port based on environment',
            choiceType: 'PT_SINGLE_SELECT',
            referencedParameters: 'ENVIRONMENT',
            script: [
                $class: 'GroovyScript',
                script: [
                    script: '''
                        def env = ENVIRONMENT ?: "dev"
                        def cmd = ["bash", "/var/jenkins_home/scripts/get_port.sh", env]
                        def process = cmd.execute()
                        process.waitFor()
                        def output = process.in.text.trim()
                        return output ? [output] : ["error-port"]
                    ''',
                    sandbox: false
                ],
                fallbackScript: [
                    script: "return ['3000']",
                    sandbox: true
                ]
            ]
        ],
        
        // Optional additional params
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
            description: 'Remove existing container before deploying?'
        )
    ])
])

pipeline {
    agent any

    environment {
        IMAGE_NAME_TAG = "${dockerImagePrefix}/git-documentation:${params.ENVIRONMENT}-b${BUILD_NUMBER}"
        FINAL_SERVER = "${params.SERVER}"
        FINAL_CONTAINER_NAME = "${params.CONTAINER_NAME}"
        FINAL_PORT = "${params.PORT}"
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Display Configuration') {
            steps {
                script {
                    echo "╔════════════════════════════════════════════════════════╗"
                    echo "║        DEPLOYMENT CONFIGURATION                        ║"
                    echo "╚════════════════════════════════════════════════════════╝"
                    echo ""
                    echo "📦 Environment:     ${params.ENVIRONMENT}"
                    echo "🖥️  Target Server:  ${env.FINAL_SERVER}"
                    echo "🐳 Container Name:  ${env.FINAL_CONTAINER_NAME}"
                    echo "🌐 Port:            ${env.FINAL_PORT}"
                    echo "🏷️  Image Tag:      ${env.IMAGE_NAME_TAG}"
                    echo "🌿 Git Branch:      ${params.GIT_BRANCH}"
                    echo "🔗 Git URL:         ${params.GIT_URL}"
                    echo "🗑️  Force Remove:   ${params.FORCE_REMOVE}"
                    echo ""
                    echo "════════════════════════════════════════════════════════"
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
                    echo "=== Checking for existing container: ${env.FINAL_CONTAINER_NAME} ==="
                    sh(script: "docker stop ${env.FINAL_CONTAINER_NAME} || true", returnStatus: true)
                    sh(script: "docker rm ${env.FINAL_CONTAINER_NAME} || true", returnStatus: true)
                    echo "✅ Cleanup finished (errors ignored)."
                }
            }
        }

        stage('Deploy Container') {
            steps {
                script {
                    echo "=== Deploying Application Container: ${env.FINAL_CONTAINER_NAME} ==="
                    sh """
                        docker run -d \\
                            --name ${env.FINAL_CONTAINER_NAME} \\
                            -p ${env.FINAL_PORT}:3000 \\
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
                    sh "docker ps | grep ${env.FINAL_CONTAINER_NAME}"
                    echo "✅ Container is running!"
                    echo "🌐 Application live at: http://localhost:${env.FINAL_PORT}"
                }
            }
        }
    }

    post {
        success {
            script {
                echo ""
                echo "╔════════════════════════════════════════════════════════╗"
                echo "║           🎉 PIPELINE SUCCESSFUL! 🎉                   ║"
                echo "╚════════════════════════════════════════════════════════╝"
                echo ""
                echo "📦 Application:  ${env.FINAL_CONTAINER_NAME}"
                echo "🌍 Environment:  ${params.ENVIRONMENT}"
                echo "🖥️  Server:      ${env.FINAL_SERVER}"
                echo "🌐 Access URL:   http://localhost:${env.FINAL_PORT}"
                echo ""
                echo "════════════════════════════════════════════════════════"
            }
        }
        failure {
            echo ""
            echo "╔════════════════════════════════════════════════════════╗"
            echo "║           ❌ PIPELINE FAILED ❌                        ║"
            echo "╚════════════════════════════════════════════════════════╝"
            echo ""
            echo "Please review the console output for errors."
            echo ""
        }
        always {
            echo "Pipeline execution completed at: ${new Date()}"
        }
    }
}
