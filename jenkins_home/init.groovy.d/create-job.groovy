import jenkins.model.Jenkins
import javaposse.jobdsl.plugin.ExecuteDslScripts
import javaposse.jobdsl.plugin.RemovedJobAction
import javaposse.jobdsl.plugin.RemovedViewAction
import javaposse.jobdsl.plugin.LookupStrategy

def jenkins = Jenkins.instance
def seedJobName = 'seed-create-pipeline'

// Delete existing seed and pipeline jobs if they exist
jenkins.getItem(seedJobName)?.delete()
jenkins.getItem('git-documentation-pipeline')?.delete()

println "🧱 Creating new seed job..."

def seedJob = jenkins.createProject(hudson.model.FreeStyleProject, seedJobName)
seedJob.setDescription('Seed job to create git-documentation pipeline with dynamic dropdown parameters.')

def jobDslScript = '''
pipelineJob("git-documentation-pipeline") {
    description("Pipeline for git-documentation with dynamic dropdown parameters")

    parameters {
        // 1️⃣ Environment dropdown
        activeChoiceParam("ENVIRONMENT") {
            description("Select deployment environment")
            choiceType("SINGLE_SELECT")
            groovyScript {
                script("return ['dev', 'qa', 'staging', 'prod']")
                fallbackScript("return ['dev']")
            }
        }

        // 2️⃣ Server dropdown dynamically populated
        activeChoiceReactiveParam("SERVER") {
            description("Select server based on environment")
            choiceType("SINGLE_SELECT")
            referencedParameter("ENVIRONMENT")
            groovyScript {
                script('''
                    def env = ENVIRONMENT ?: "dev"
                    def cmd = ["bash", "/var/jenkins_home/scripts/get_servers.sh", env]
                    def process = cmd.execute()
                    process.waitFor()
                    def out = process.in.text.trim()
                    return out ? out.split("\\n") : ["no-server-found"]
                ''')
                fallbackScript("return ['fallback-server']")
            }
        }

        // 3️⃣ Container name auto-generated
        activeChoiceReactiveParam("CONTAINER_NAME") {
            description("Auto-generated container name based on server")
            choiceType("SINGLE_SELECT")
            referencedParameter("SERVER")
            groovyScript {
                script('''
                    if (SERVER && !SERVER.contains("error") && !SERVER.contains("fallback")) {
                        def cmd = ["bash", "/var/jenkins_home/scripts/generate_container_name.sh", SERVER]
                        def process = cmd.execute()
                        process.waitFor()
                        def out = process.in.text.trim()
                        return out ? [out] : ["error-container"]
                    }
                    return ["no-container"]
                ''')
                fallbackScript("return ['fallback-container']")
            }
        }

        // 4️⃣ Port auto-generated
        activeChoiceReactiveParam("PORT") {
            description("Auto-selected port based on environment")
            choiceType("SINGLE_SELECT")
            referencedParameter("ENVIRONMENT")
            groovyScript {
                script('''
                    def env = ENVIRONMENT ?: "dev"
                    def cmd = ["bash", "/var/jenkins_home/scripts/get_port.sh", env]
                    def process = cmd.execute()
                    process.waitFor()
                    def out = process.in.text.trim()
                    return out ? [out] : ["error-port"]
                ''')
                fallbackScript("return ['3000']")
            }
        }

        // Optional additional params
        stringParam("GIT_BRANCH", "master", "Git branch to checkout")
        booleanParam("FORCE_REMOVE", true, "Remove existing container before deploying?")
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url("https://github.com/mudunuri010/git-documentation")
                        credentials("git-credentials")
                    }
                    branch("*/master")
                }
            }
            scriptPath("Jenkinsfile")
        }
    }
}
'''

def jobDslBuilder = new ExecuteDslScripts(
    new ExecuteDslScripts.ScriptLocation(null, null, jobDslScript),
    false,
    RemovedJobAction.DELETE,
    RemovedViewAction.DELETE,
    LookupStrategy.JENKINS_ROOT,
    null
)

seedJob.getBuildersList().add(jobDslBuilder)
seedJob.save()
seedJob.scheduleBuild2(0)

println "✅ Seed job and pipeline created successfully."
'''

