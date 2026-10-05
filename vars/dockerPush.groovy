def call(Map config = [:]) {
    def image    = config.image
    def credsId  = config.credentialsId
    def registry = config.registry ?: ''   // Docker Hub ke liye khaali, Nexus ke liye host:port

    withCredentials([usernamePassword(credentialsId: credsId, usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
        sh "echo \$REG_PASS | docker login ${registry} -u \$REG_USER --password-stdin"
        sh "docker push ${image}"
    }
}
