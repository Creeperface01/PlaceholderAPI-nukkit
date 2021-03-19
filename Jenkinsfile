pipeline {
    agent any
    tools {
        maven 'Maven 3'
        jdk 'Java 8'
    }
    options {
        buildDiscarder(logRotator(artifactNumToKeepStr: '5'))
    }
    stages {
        stage ('Build') {
            steps {
                sh 'mvn clean package'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/PlaceholderAPI-*-SNAPSHOT.jar', fingerprint: true
                }
            }
        }

        stage ('Deploy') {
            when {
                branch "master"
            }

            steps {
                rtMavenDeployer (
                        id: "maven-deployer",
                        serverId: "opencollab-artifactory",
                        releaseRepo: "maven-releases",
                        snapshotRepo: "maven-snapshots"
                )
                rtMavenResolver (
                        id: "maven-resolver",
                        serverId: "opencollab-artifactory",
                        releaseRepo: "release",
                        snapshotRepo: "snapshot"
                )
                rtMavenRun (
                        pom: 'pom.xml',
                        goals: 'javadoc:javadoc javadoc:jar source:jar install -DskipTests',
                        deployerId: "maven-deployer",
                        resolverId: "maven-resolver"
                )
                rtPublishBuildInfo (
                        serverId: "opencollab-artifactory"
                )
            }
        }
    }

    post {
        always {
            deleteDir()
        }
    }
}