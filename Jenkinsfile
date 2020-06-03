pipeline{
    agent any

    environment{
        MAVEN_CREDENTIAL = credentials('heartpattern-maven-repository')
    }

    stages{
        stage('publish'){
            steps{
                sh './gradlew publish -Pmaven.username=${MAVEN_CREDENTIAL_USR} -Pmaven.password=${MAVEN_CREDENTIAL_PSW}'
            }
        }
    }

    post{
        always{
            archiveArtifacts artifacts: '**/build/libs/*'
        }
    }
}