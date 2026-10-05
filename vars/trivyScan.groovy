def call(Map config = [:]) {
    def image    = config.image
    def severity = config.severity ?: 'HIGH,CRITICAL'
    def blocking = config.blocking ?: false

    // network issue sirf DB download mein aata hai, isliye retry wahin
    retry(3) {
        sh 'trivy image --download-db-only --no-progress'
    }

    int rc = sh(returnStatus: true, script: """
        trivy image --no-progress --severity ${severity} --exit-code 1 \
        --format template --template '@/usr/local/share/trivy/templates/html.tpl' \
        -o trivy-report.html ${image}
    """)

    publishHTML(target: [
        allowMissing: true,
        alwaysLinkToLastBuild: true,
        keepAll: true,
        reportDir: '.',
        reportFiles: 'trivy-report.html',
        reportName: 'Trivy Security Report'
    ])

    if (rc != 0) {
        if (blocking) {
            error("Trivy: ${severity} vulnerabilities found (or scan failed)")
        } else {
            unstable("Trivy: ${severity} vulnerabilities found (or scan failed)")
        }
    }
}
