def call(Map config = [:]) {
    def deployment = config.deployment
    def container  = config.container ?: config.deployment
    def namespace  = config.namespace ?: 'dev'
    def image      = config.image
    def timeoutSec = config.timeoutSeconds ?: 120
    def rollback   = config.rollbackOnFailure != null ? config.rollbackOnFailure : true

    sh "kubectl set image deployment/${deployment} ${container}=${image} -n ${namespace}"

    int rc = sh(returnStatus: true,
                script: "kubectl rollout status deployment/${deployment} -n ${namespace} --timeout=${timeoutSec}s")

    if (rc != 0) {
        if (rollback) {
            echo "Deployment failed, rolling back..."
            sh "kubectl rollout undo deployment/${deployment} -n ${namespace}"
            error("Deployment failed and was rolled back")
        } else {
            error("Deployment failed")
        }
    }
}
