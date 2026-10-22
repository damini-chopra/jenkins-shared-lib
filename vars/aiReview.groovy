def call(Map config = [:]) {
    def repo      = config.repo
    def credId    = config.credId    ?: 'a8a8295f-15a8-4d8a-b2bd-46aeda17596b'
    def ollamaUrl = config.ollamaUrl ?: 'http://ollama:11434'
    def model     = config.model     ?: 'qwen2.5-coder:7b'
    def maxChars  = config.maxChars  ?: 12000

    withCredentials([usernamePassword(
        credentialsId: credId,
        usernameVariable: 'GH_USER',
        passwordVariable: 'GITHUB_TOKEN'
    )]) {
        withEnv(["REPO=${repo}", "OLLAMA_URL=${ollamaUrl}", "MODEL=${model}", "MAX_CHARS=${maxChars}"]) {
            sh '''
                git fetch origin ${CHANGE_TARGET}:refs/remotes/origin/${CHANGE_TARGET}
                git diff origin/${CHANGE_TARGET}...HEAD | head -c ${MAX_CHARS} > diff.txt

                jq -n --arg model "$MODEL" --rawfile diff diff.txt '{
                  model: $model,
                  stream: false,
                  options: {num_ctx: 8192},
                  messages: [{role: "user", content: ("Review this PR diff. List bugs, security issues and improvements, concisely.\\n\\n" + $diff)}]
                }' > req.json

                curl -s --max-time 600 $OLLAMA_URL/api/chat -d @req.json | jq -r '.message.content' > review.md

                jq -n --rawfile body review.md '{body: ("**AI Review (local LLM)**\\n\\n" + $body)}' | \
                curl -s -X POST \
                  -H "Authorization: Bearer $GITHUB_TOKEN" \
                  -H "Accept: application/vnd.github+json" \
                  https://api.github.com/repos/$REPO/issues/$CHANGE_ID/comments -d @-
            '''
        }
    }
}
