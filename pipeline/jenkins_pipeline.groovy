
void withNode(Closure func){
    withEnv(["PATH+NODEJS=${tool name: 'node_6.9.4', type: 'jenkins.plugins.nodejs.tools.NodeJSInstallation'}/bin"], func)
}

def npmrc = """
init.author.name = Jonas Eicher
init.author.email = jonas.eicher@bla.de
init.author.url = http://blog.example.com
# an email is required to publish npm packages
email=jonas.eicher@kite-consult.de
always-auth=true
_auth=ZWljaGVyLXRlc3Q6YWxsaWFuem5leHVz
"""

stage "git checkout"
node {
    // checkout the code to be build
    checkout([$class: 'GitSCM', branches: [[name: '*/master']],
              doGenerateSubmoduleConfigurations: false, extensions: [],
              submoduleCfg: [], userRemoteConfigs: [[
                credentialsId: 'TU-access-token',
                url: 'https://github.com/jonaseicher/learnreact.git']]
    ])

    writeFile(file: './.npmrc', text: npmrc, encoding: 'UTF-8')
    // now stash the source code to be available in other nodes as well
    stash includes: '**', name: 'source'
}

stage "npm install"
node {
    // get hands on the stuff of previous steps
    unstash 'source'

    withNode {
        sh "npm install"
    }
}

stage "npm publish to nexus"
node {
    // get hands on the stuff of previous steps
//    unstash 'source'

    withNode {
        sh "npm publish"
    }
}
