def call(Map config = [:]) {
    def waitMinutes = config.timeoutMinutes ?: 5

    timeout(time: waitMinutes, unit: 'MINUTES') {
        waitForQualityGate abortPipeline: true
    }
}
