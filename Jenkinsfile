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
                bat 'mvn test -Dmaven.test.failure.ignore=true'
            }
        }
    }

    post {
        always {
            echo 'Archiving test results and generating reports...'
            catchError(buildResult: 'SUCCESS', stageResult: 'SUCCESS') {
                junit '**/target/surefire-reports/*.xml'
            }
            allure includeProperties: false,
                   jdk: '',
                   results: [[path: 'allure-results']]
        }
        success {
            emailext(
                subject: "Pipeline Completed: '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>The pipeline completed successfully.</p>
                         <p>Job: ${env.JOB_NAME} | Build: ${env.BUILD_NUMBER}</p>
                         <p>View the full test report (pass/fail breakdown) here:</p>
                         <p><a href="${env.BUILD_URL}allure">${env.BUILD_URL}allure</a></p>""",
                to: 'mktheekshana2001@gmail.com',
                mimeType: 'text/html'
            )
        }
        failure {
            emailext(
                subject: "PIPELINE FAILED: '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>The pipeline itself failed — this means a build, checkout, or infrastructure problem, not a test assertion failure.</p>
                         <p>Job: ${env.JOB_NAME} | Build: ${env.BUILD_NUMBER}</p>
                         <p>Console output: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>""",
                to: 'mktheekshana2001@gmail.com',
                mimeType: 'text/html'
            )
        }
    }
}