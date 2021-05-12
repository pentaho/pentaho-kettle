def GetScmProjectName() {
    return scm.getUserRemoteConfigs()[0].getUrl().tokenize('/').last().split("\\.")[0]
}

pipeline {
    agent any
    environment {
        GITHUB_TOKEN = credentials('9e494763-394a-4837-a25f-c1e9e61a7289')
        repo         = GetScmProjectName()
        version      = 'nightly'
        dist         = '9.3.0.0-SNAPSHOT'
        username     = 'HiromuHota'
    }
    stages {
        stage('Build') {
            steps {
                sh '''
                    mvn -DskipTests clean install
                '''
            }
        }
        stage('Test') {
            steps {
                sh '''
                    mvn test
                '''
            }
        }
        stage('Deliver') {
            steps {
                sh '''#!/bin/bash
                    github-release delete --user $username --repo $repo --tag webspoon/$version
                    git tag -f webspoon/$version
                    git push -f https://${GITHUB_TOKEN}@github.com/$username/$repo.git webspoon/$version
                    github-release release --user $username --repo $repo --tag webspoon/$version --name "webSpoon/$version" --description "Auto-build by Jenkins on $(date +'%F %T %Z')" --pre-release

                    paths=()
                    paths+=(`find assemblies/client/target | grep "spoon.war"`)
                    paths+=(`find core/target | grep -E "kettle-core-$dist-[0-9]*(-sources)?\\.jar"`)
                    paths+=(`find engine/target | grep -E "kettle-engine-$dist-[0-9]*(-sources)?\\.jar"`)
                    paths+=(`find ui/target | grep -E "kettle-ui-swt-$dist-[0-9]*(-sources)?\\.jar"`)
                    paths+=(`find security/target | grep -E "webspoon-security-$dist-[0-9]*(-sources)?\\.jar"`)
                    paths+=(`find plugins/repositories/core/target | grep -E "repositories-plugin-core-$dist(-sources)?\\.jar"`)
                    paths+=(`find plugins/engine-configuration/ui/target | grep -E "pdi-engine-configuration-ui-$dist(-sources)?\\.jar"`)
                    paths+=(`find plugins/file-open-save/core/target | grep -E "file-open-save-core-$dist(-sources)?\\.jar"`)
                    paths+=(`find plugins/file-open-save-new/core/target | grep -E "file-open-save-new-core-$dist(-sources)?\\.jar"`)
                    paths+=(`find plugins/get-fields/core/target | grep -E "get-fields-core-$dist(-sources)?\\.jar"`)
                    paths+=(`find plugins/connections/ui/target | grep -E "connections-ui-$dist(-sources)?\\.jar"`)

                    for path in ${paths[@]}
                    do
                      echo $path
                      file=`echo $path | awk -F/ '{print $NF}'`
                      echo $file
                      github-release upload --user $username --repo $repo --tag webspoon/$version --name "$file" --file $path
                    done
                '''
            }
        }
    }
}
