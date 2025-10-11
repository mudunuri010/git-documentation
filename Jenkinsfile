pipeline{
    agent any
      environemnt {
         IMAGE_NAME="saimudunuri09/git-documentation "
       }
   stages{
      stage('Checkout'){
          steps{
            git branch 'master',url: "https://github.com/mudunuri010/git-documentation"
                }
            }
       stage('Build'){
        steps{
             bat "docker build -t %IMAGE_NAME%:latest ."
          }
       }
     stage('push to Dockerhub'){
        steps{
         withCredentials([usernamePassword(credentialsId:'docker',usernameVariable:'DOCKER_USER',passwordVariable:'DOCKER_PASS')])
              bat 'docker login -u %DOCKER_USER% -p %DOCKER_PASS%'
              bat 'docker push %IMAGE_NAME%:%IMAGE_TAG%'

        }
     }
   }
}
