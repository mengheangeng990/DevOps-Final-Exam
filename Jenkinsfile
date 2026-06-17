pipeline {
    agent any
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }
    
    triggers {
        pollSCM('*/5 * * * *')  // Poll Git every 5 minutes
    }
    
    environment {
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk'
        MAVEN_HOME = '/usr/share/maven'
        GIT_COMMIT_AUTHOR = sh(script: "git log -1 --format='%an <%ae>'", returnStdout: true).trim()
        BUILD_STATUS = 'SUCCESS'
    }
    
    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "Checking out from Git repository..."
                    checkout scm
                    echo "Current branch: ${env.GIT_BRANCH}"
                    echo "Latest commit: ${env.GIT_COMMIT}"
                }
            }
        }
        
        stage('Clean') {
            steps {
                script {
                    echo "Cleaning previous builds..."
                    sh 'mvn clean'
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    echo "Building Spring Boot application with Maven..."
                    try {
                        sh 'mvn package -DskipTests'
                    } catch (Exception e) {
                        env.BUILD_STATUS = 'FAILED'
                        throw e
                    }
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    echo "Running unit tests with SQLite test database..."
                    try {
                        sh '''
                            mvn test \
                                -Dspring.datasource.url=jdbc:sqlite:/tmp/test-db.sqlite \
                                -Dspring.jpa.hibernate.ddl-auto=create-drop
                        '''
                    } catch (Exception e) {
                        env.BUILD_STATUS = 'FAILED'
                        throw e
                    }
                }
            }
        }
        
        stage('Deploy via Ansible') {
            when {
                expression { env.BUILD_STATUS == 'SUCCESS' }
            }
            steps {
                script {
                    echo "Running Ansible playbook to deploy to Web Server..."
                    try {
                        sh '''
                            cd ${WORKSPACE}
                            ansible-playbook -i inventory ansible-playbook.yml \
                                -e "git_repo=${GIT_URL}" \
                                -e "git_branch=${GIT_BRANCH}"
                        '''
                    } catch (Exception e) {
                        env.BUILD_STATUS = 'DEPLOY_FAILED'
                        throw e
                    }
                }
            }
        }
    }
    
    post {
        always {
            script {
                echo "Cleaning up test database..."
                sh 'rm -f /tmp/test-db.sqlite 2>/dev/null || true'
            }
            
            // Archive test results
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
            
            // Archive build artifacts
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
        }
        
        success {
            script {
                echo "✅ Build and deployment successful!"
                emailext(
                    subject: "✅ Jenkins Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: '''
                        Build Status: SUCCESS
                        
                        Job: ${JOB_NAME}
                        Build Number: ${BUILD_NUMBER}
                        Build URL: ${BUILD_URL}
                        
                        Git Details:
                        - Repository: ${GIT_URL}
                        - Branch: ${GIT_BRANCH}
                        - Commit: ${GIT_COMMIT}
                        - Author: ${GIT_COMMIT_AUTHOR}
                        
                        Stages Completed:
                        ✅ Checkout
                        ✅ Clean
                        ✅ Build
                        ✅ Test (SQLite Database)
                        ✅ Deploy via Ansible
                        
                        The application has been successfully built, tested, and deployed to the Web Server.
                    ''',
                    to: 'srengty@gmail.com',
                    recipientProviders: [
                        developers(),
                        requestor()
                    ]
                )
            }
        }
        
        failure {
            script {
                echo "❌ Build failed! Notifying team..."
                emailext(
                    subject: "❌ Jenkins Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: '''
                        Build Status: FAILED
                        
                        Job: ${JOB_NAME}
                        Build Number: ${BUILD_NUMBER}
                        Build URL: ${BUILD_URL}
                        
                        Git Details:
                        - Repository: ${GIT_URL}
                        - Branch: ${GIT_BRANCH}
                        - Commit: ${GIT_COMMIT}
                        - Author: ${GIT_COMMIT_AUTHOR}
                        
                        Build Logs:
                        ${BUILD_LOG_EXCERPT}
                        
                        Please review the build log and fix the issues before the next commit.
                    ''',
                    attachLog: true,
                    to: 'srengty@gmail.com',
                    recipientProviders: [
                        developers(),
                        requestor(),
                        commiters(),
                        brokenBuildSuspects()
                    ]
                )
            }
        }
        
        unstable {
            script {
                echo "⚠️ Build unstable!"
                emailext(
                    subject: "⚠️ Jenkins Build UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: '''
                        Build Status: UNSTABLE
                        
                        Job: ${JOB_NAME}
                        Build Number: ${BUILD_NUMBER}
                        Build URL: ${BUILD_URL}
                        
                        Git Details:
                        - Repository: ${GIT_URL}
                        - Branch: ${GIT_BRANCH}
                        - Commit: ${GIT_COMMIT}
                        - Author: ${GIT_COMMIT_AUTHOR}
                        
                        Some tests may have failed. Please review the build log.
                    ''',
                    to: 'srengty@gmail.com',
                    recipientProviders: [
                        developers(),
                        requestor(),
                        commiters()
                    ]
                )
            }
        }
    }
}
