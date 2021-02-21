pipeline {
    agent any

    stages {
        stage('build') {
            steps {
                sh "build.sh"
            }
        stage('deploy') {
            steps {
                sh "deploy.sh"
            }
        }
    }
}