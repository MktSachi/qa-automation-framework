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

        stage('Start Selenium Container') {
    steps {
        echo 'Starting Chrome container via Docker...'
        bat 'docker compose up -d'
        sleep(time: 10, unit: 'SECONDS')
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
                bat 'mvn test -Dmaven.test.failure.ignore=true'
            }
        }

        stage('Report') {
            steps {
                echo 'Archiving JUnit test results...'
                catchError(buildResult: 'SUCCESS', stageResult: 'SUCCESS') {
                    junit '**/target/surefire-reports/*.xml'
                }
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
            echo 'Stopping Chrome container...'
            bat 'docker compose down'
        }
        success {
            emailext(
                subject: "Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>All tests passed.</p>
                         <p>Job: ${env.JOB_NAME} | Build: ${env.BUILD_NUMBER}</p>
                         <p>Report: <a href="${env.BUILD_URL}allure">${env.BUILD_URL}allure</a></p>
                         <p>Console: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>""",
                to: 'mktheekshana2001@gmail.com',
                mimeType: 'text/html'
            )
        }
        failure {
            emailext(
                subject: "Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' - Pipeline broke",
                body: """<p>The pipeline itself failed (not just a test assertion) — check console output.</p>
                         <p>Job: ${env.JOB_NAME} | Build: ${env.BUILD_NUMBER}</p>
                         <p>Console: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>""",
                to: 'mktheekshana2001@gmail.com',
                mimeType: 'text/html'
            )
        }
    }
}