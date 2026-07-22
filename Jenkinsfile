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

        stage('Clean Previous Results') {
            steps {
                echo 'Clearing old Allure results...'
                bat 'if exist allure-results rmdir /s /q allure-results'
            }
        }

        stage('Create Zephyr Test Cycle') {
    steps {
        echo 'Creating a new Zephyr Test Cycle for this run...'
        withCredentials([string(credentialsId: 'zephyr-api-token', variable: 'ZEPHYR_TOKEN')]) {
            script {
                def cycleName = "Automated Run - Build #${env.BUILD_NUMBER}"
                bat """
                    C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe -Command ^
                    "\$headers = @{ Authorization = 'Bearer %ZEPHYR_TOKEN%'; 'Content-Type' = 'application/json' }; ^
                    \$body = @{ projectKey = 'QAF'; name = '${cycleName}' } | ConvertTo-Json; ^
                    \$result = Invoke-RestMethod -Uri 'https://api.zephyrscale.smartbear.com/v2/testcycles' -Method POST -Headers \$headers -Body \$body; ^
                    \$result.key | Out-File -FilePath cycle_key.txt -Encoding ascii"
                """
                env.ZEPHYR_CYCLE_KEY = readFile('cycle_key.txt').trim()
                echo "Created Zephyr Test Cycle: ${env.ZEPHYR_CYCLE_KEY}"
            }
        }
    }
}

        stage('Test') {
            steps {
                echo 'Running automated tests...'
                bat 'mvn test -Dmaven.test.failure.ignore=true'
            }
        }

        stage('Push Results to Zephyr') {
            steps {
                echo 'Uploading test results to Zephyr Scale...'
                withCredentials([string(credentialsId: 'zephyr-api-token', variable: 'ZEPHYR_TOKEN')]) {
                    bat """
                        C:\\Windows\\System32\\curl.exe -X POST "https://api.zephyrscale.smartbear.com/v2/automations/executions/junit?projectKey=QAF&testCycleKey=${env.ZEPHYR_CYCLE_KEY}" ^
                        -H "Authorization: Bearer %ZEPHYR_TOKEN%" ^
                        -F "file=@target/surefire-reports/testng-results.xml;type=application/xml"
                    """
                }
            }
        }

        stage('Report') {
            steps {
                echo 'Archiving JUnit test results...'
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
            echo 'Stopping Chrome container...'
            bat 'docker compose down'
        }
        success {
            emailext(
                subject: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>All tests passed.</p>
                         <p>Job: ${env.JOB_NAME} | Build: ${env.BUILD_NUMBER}</p>
                         <p>Zephyr Cycle: ${env.ZEPHYR_CYCLE_KEY}</p>
                         <p>Report: <a href="${env.BUILD_URL}allure">${env.BUILD_URL}allure</a></p>
                         <p>Console: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>""",
                to: 'mktheekshana2001@gmail.com',
                mimeType: 'text/html'
            )
        }
        unstable {
            emailext(
                subject: "UNSTABLE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' - Some tests failed",
                body: """<p>The pipeline completed, but one or more test cases failed.</p>
                         <p>Job: ${env.JOB_NAME} | Build: ${env.BUILD_NUMBER}</p>
                         <p>Zephyr Cycle: ${env.ZEPHYR_CYCLE_KEY}</p>
                         <p>Check the report for details: <a href="${env.BUILD_URL}allure">${env.BUILD_URL}allure</a></p>
                         <p>Console: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>""",
                to: 'mktheekshana2001@gmail.com',
                mimeType: 'text/html'
            )
        }
        failure {
            emailext(
                subject: "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' - Pipeline broke",
                body: """<p>The pipeline itself failed (build, checkout, or infrastructure issue) — check console output.</p>
                         <p>Job: ${env.JOB_NAME} | Build: ${env.BUILD_NUMBER}</p>
                         <p>Console: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>""",
                to: 'mktheekshana2001@gmail.com',
                mimeType: 'text/html'
            )
        }
    }
}