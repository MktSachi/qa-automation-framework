pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Pulling latest code from Git...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building the project...'
                bat 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                echo 'Running automated tests...'
                bat 'mvn test'
            }
        }

        stage('Report') {
            steps {
                echo 'Archiving test results...'
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('Allure Report') {
            steps {
                echo 'Generating Allure report...'
                allure includeProperties: false,
                       jdk: '',
                       results: [[path: 'allure-results']]
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished. Cleaning up...'
        }
        success {
            emailext(
                subject: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>Good news — the build succeeded.</p>
                         <p>Job: ${env.JOB_NAME}</p>
                         <p>Build Number: ${env.BUILD_NUMBER}</p>
                         <p>View full report: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>""",
                to: 'mktheekshana2001@gmail.com',
                mimeType: 'text/html'
            )
        }
        failure {
            emailext(
                subject: "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>The build failed — check the console output.</p>
                         <p>Job: ${env.JOB_NAME}</p>
                         <p>Build Number: ${env.BUILD_NUMBER}</p>
                         <p>Console: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>""",
                to: 'mktheekshana2001@gmail.com',
                mimeType: 'text/html'
            )
        }
    }
}