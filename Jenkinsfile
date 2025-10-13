pipeline {
    agent any
    
    environment {
        AWS_REGION = 'us-east-1'
        AWS_ACCOUNT_ID = '025765678699'  // Your AWS Account ID
        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        ECR_REPO = 'test'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/mudunuri010/git-documentation'
            }
        }
        
        stage('Build') {
            steps {
                sh "docker build -t ${ECR_REPO}:${IMAGE_TAG} ."
                sh "docker tag ${ECR_REPO}:${IMAGE_TAG} ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"
            }
        }
        
        stage('Push to ECR') {
            steps {
                withAWS(credentials: 'aws_cred', region: "${AWS_REGION}") {
                    sh '''
                        aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin ${ECR_REGISTRY}
                        
                        aws ecr describe-repositories --repository-names ${test} || \
                        aws ecr create-repository --repository-name ${test}
                        
                        docker push ${ECR_REGISTRY}/${test}:${IMAGE_TAG}
                    '''
                }
            }
        }
    }
    
    post {
        success {
            echo "✅ Image pushed: ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"
        }
    }
}

