def call(Map config = [:]) {
    def image      = config.image
    def dockerfile = config.dockerfile ?: 'Dockerfile'
    def context    = config.context ?: '.'
    def buildArgs  = config.buildArgs ?: [:]
    def pull       = config.pull ?: false

    def args = ''
    for (entry in buildArgs) {
        args += " --build-arg ${entry.key}=${entry.value}"
    }
    if (pull) {
        args += ' --pull'
    }

    sh "docker build -t ${image} -f ${dockerfile}${args} ${context}"
}
