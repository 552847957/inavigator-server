def artifactId = "CI00377171_i-Navigator"
def repositoryId = "nexus.deploy.repo"
def classifier = "distrib"
def nexus_url = "https://sbrf-nexus.sigma.sbrf.ru/nexus/content/repositories/Nexus_PROD"
def nexusCredentialsName = "sbrf-nexus.sigma.sbrf.ru_CI_CI00324280"
def ipadDistrUrl = "https://sbrf-nexus.sigma.sbrf.ru/nexus/service/local/repositories/Nexus_PROD/content/Nexus_PROD/CI00377171_i-Navigator/D-03.008.06-1-2019-JAN-28-REV-140BD87F1-A/CI00377171_i-Navigator-D-03.008.06-1-2019-JAN-28-REV-140BD87F1-A-distrib.zip"

node('Linux_Default') {
    try {
        def scmVars = null
        stage('prepare') {
            def mvnHome = tool 'Maven 3.3.9'
            env.PATH = "${mvnHome}/bin:${env.PATH}"
            cleanWs()
        }
        stage('check environment') {
            if (env.product_version == null) {
                error "Не установлена переменная env.product_version"
            } else {
                echo "env.product_version: $env.product_version"
            }
        }
        def version = env.product_version
        def distrib_file = "build_all/target/CI00377171_i-Navigator-$version-distrib.zip"
        stage('checkout') {
            scmVars = checkout(scm)
        }
        stage('install_jacob'){
            dir("inavigator-web/source/qlik-client/lib/jacob") {
                sh "mvn install:install-file -Dfile=./jacob.jar -DgroupId=com.jacob -DartifactId=jacob -Dversion=1.18  -Dpackaging=jar"
            }
        }
        stage ('download ipad distribution') {
            withCredentials([usernamePassword(credentialsId: "$nexusCredentialsName", passwordVariable: 'nexusDeployRepoPassword', usernameVariable: 'nexusDeployRepoUsername')]) {
                sh "curl -u $nexusDeployRepoUsername:$nexusDeployRepoPassword $ipadDistrUrl --output ./ipad/ipad.zip"
            }
        }
        stage('build') {
            copyArtifacts(projectName: 'VladDevOps-IFT-inavigator2-web', target: 'inavigator2-web')
            sh "ls"
            sh "ls inavigator2-web"
            sh "ls inavigator2-web/inavigator3-distr/target"
            withCredentials([usernamePassword(credentialsId: "$nexusCredentialsName", passwordVariable: 'nexusDeployRepoPassword', usernameVariable: 'nexusDeployRepoUsername')]) {
                sh "mvn -s settings.xml clean install -Dnexus.deploy.repo.username=$nexusDeployRepoUsername -Dnexus.deploy.repo.password=$nexusDeployRepoPassword"
            }
        }
        stage('deploy') {
            withCredentials([usernamePassword(credentialsId: "$nexusCredentialsName", passwordVariable: 'nexusDeployRepoPassword', usernameVariable: 'nexusDeployRepoUsername')]) {
                sh "mvn -s settings.xml deploy:deploy-file -DgroupId=Nexus_PROD " +
                        "-DartifactId=$artifactId " +
                        "-Dversion=$version " +
                        "-Dpackaging=zip " +
                        "-Dfile=$distrib_file " +
                        "-DrepositoryId=$repositoryId " +
                        "-Dclassifier=$classifier " +
                        "-Durl=$nexus_url " +
                        "-Dmaven.wagon.http.ssl.insecure=true " +
                        "-Dnexus.deploy.repo.username=$nexusDeployRepoUsername " +
                        "-Dnexus.deploy.repo.password=$nexusDeployRepoPassword"
            }
        }
    } catch (e) {
        throw e
    }
    stage('clean-after') {
        echo "Clearing workspace..."
        cleanWs cleanWhenAborted: false, cleanWhenFailure: false, cleanWhenNotBuilt: false, cleanWhenUnstable: false
    }
}
