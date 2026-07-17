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
            echo 'All tests passed!'
        }
        failure {
            echo 'Build or tests failed. Check console output above.'
        }
    }
}