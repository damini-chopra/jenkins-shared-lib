def call(Map config = [:]) {
    def serverName  = config.server  ?: 'sonarqube-local'
    def scannerName = config.scanner ?: 'SonarScanner'

    def scannerHome = tool scannerName
    withSonarQubeEnv(serverName) {
        sh "${scannerHome}/bin/sonar-scanner"
    }
}
