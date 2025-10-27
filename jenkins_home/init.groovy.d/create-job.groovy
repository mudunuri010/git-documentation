import jenkins.model.Jenkins
import javaposse.jobdsl.plugin.ExecuteDslScripts
import javaposse.jobdsl.plugin.RemovedJobAction
import javaposse.jobdsl.plugin.RemovedViewAction
import javaposse.jobdsl.plugin.LookupStrategy

def jenkins = Jenkins.instance
def seedJobName = 'seed-create-pipeline'

println "Checking for seed job: ${seedJobName}"

// Delete existing seed job if it exists
def existingSeedJob = jenkins.getItem(seedJobName)
if (existingSeedJob != null) {
    println "Deleting existing seed job..."
    existingSeedJob.delete()
}

// Delete existing pipeline if it exists
def existingPipeline = jenkins.getItem('git-documentation-pipeline')
if (existingPipeline != null) {
    println "Deleting existing pipeline..."
    existingPipeline.delete()
}

println "Creating new seed job..."

// Create freestyle seed job
def seedJob = jenkins.createProject(hudson.model.FreeStyleProject, seedJobName)
seedJob.setDescription('Seed job to automatically create git-documentation-pipeline with dynamic parameters')

// Job DSL script
def jobDslScript = '''
pipelineJob('git-documentation-pipeline') {
    description('Automated CI/CD pipeline for git-documentation with environment-based dynamic parameters')
    
    parameters {
        activeChoiceParam('ENVIRONMENT') {
            description('Select deployment environment')
            choiceType('SINGLE_SELECT')
            groovyScript {
                script('return ["dev", "qa", "staging", "prod"]')
                fallbackScript('return ["dev"]')
            }
        }
        
        activeChoiceReactiveParam('SERVER') {
            description('Server auto-populated based on environment')
            choiceType('SINGLE_SELECT')
            groovyScript {
                script(\'\'\'
                    def env = ENVIRONMENT ?: "dev"
                    def command = ["sh", "/var/jenkins_home/scripts/get_servers.sh", env]
                    def process = command.execute()
                    process.waitFor()
                    def output = process.in.text.trim()
                    return output ? output.split(/\\n/) as List : ["error-no-servers"]
                \'\'\')
                fallbackScript('return ["fallback-server"]')
            }
            referencedParameter('ENVIRONMENT')
        }
        
        activeChoiceReactiveParam('CONTAINER_NAME') {
            description('Container name auto-generated from server')
            choiceType('SINGLE_SELECT')
            groovyScript {
                script(\'\'\'
                    if (SERVER && !SERVER.contains("error") && !SERVER.contains("fallback")) {
                        def command = ["sh", "/var/jenkins_home/scripts/generate_container_name.sh", SERVER]
                        def process = command.execute()
                        process.waitFor()
                        def output = process.in.text.trim()
                        return output ? [output] : ["error-container"]
                    }
                    return ["error-no-server"]
                \'\'\')
                fallbackScript('return ["fallback-container"]')
            }
            referencedParameter('SERVER')
        }
        
        stringParam('GIT_BRANCH', 'master', 'Git branch to checkout')
        stringParam('GIT_URL', 'https://github.com/mudunuri010/git-documentation', 'Git repository URL')
        booleanParam('FORCE_REMOVE', true, 'Force remove existing container before deploy?')
    }
    
    definition {
        cps {
            sandbox(true)
            script(\'\'\'
pipeline {
    agent any
    
    environment {
        IMAGE_NAME_TAG = "saimudunuri9/git-documentation:${params.ENVIRONMENT}-b${BUILD_NUMBER}"
        HOST_PORT = ""
    }
    
    stages {
        stage("Initialize") {
            steps {
                script {
                    echo "=== Build Configuration ==="
                    echo "Build Number:    ${BUILD_NUMBER}"
                    echo "Environment:     ${params.ENVIRONMENT}"
                    echo "Server:          ${params.SERVER}"
                    echo "Container:       ${params.CONTAINER_NAME}"
                    echo "Git Branch:      ${params.GIT_BRANCH}"
                    echo "Image:           ${env.IMAGE_NAME_TAG}"
                    
                    def portCmd = ["sh", "/var/jenkins_home/scripts/get_port.sh", params.ENVIRONMENT]
                    def portProc = portCmd.execute()
                    portProc.waitFor()
                    env.HOST_PORT = portProc.in.text.trim()
                    echo "Port:            ${env.HOST_PORT}"
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
            \'\'\')
        }
    }
}
'''

// Add Job DSL build step
def jobDslBuilder = new ExecuteDslScripts(
    new ExecuteDslScripts.ScriptLocation(
        null,
        null,
        jobDslScript
    ),
    false,
    RemovedJobAction.DELETE,
    RemovedViewAction.DELETE,
    LookupStrategy.JENKINS_ROOT,
    null
)

seedJob.getBuildersList().add(jobDslBuilder)
seedJob.save()

println "✅ Seed job created successfully"
println "⏳ Triggering seed job to create pipeline..."

// Trigger the seed job immediately
seedJob.scheduleBuild2(0)

println "✅ Pipeline creation initiated. Check 'seed-create-pipeline' job for status."