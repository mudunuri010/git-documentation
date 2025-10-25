import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import hudson.plugins.git.GitSCM
import hudson.plugins.git.BranchSpec
import hudson.plugins.git.UserRemoteConfig
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import hudson.model.BooleanParameterDefinition
import org.biouno.unochoice.ChoiceParameter
import org.biouno.unochoice.CascadeChoiceParameter
import org.biouno.unochoice.model.GroovyScript

def jenkins = Jenkins.instance
def jobName = 'git-documentation-pipeline'

// Delete existing job if it exists
def existingJob = jenkins.getItem(jobName)
if (existingJob != null) {
    println "Deleting existing job: ${jobName}"
    existingJob.delete()
}

println "Creating job: ${jobName}"

// Create the pipeline job
def job = jenkins.createProject(WorkflowJob.class, jobName)
job.setDescription('Automated pipeline for git-documentation with dynamic parameters')

// Define parameters using Active Choices plugin
def parameters = []

// 1. ENVIRONMENT - Choice Parameter
def environmentParam = new ChoiceParameter(
    'ENVIRONMENT',
    'Select deployment environment',
    new GroovyScript(
        new org.biouno.unochoice.model.ScriptlerScript('', []),
        'return ["dev", "qa", "staging", "prod"]'
    ),
    org.biouno.unochoice.ChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
    false,
    1
)
parameters.add(environmentParam)

// 2. SERVER - Cascade Choice Parameter (depends on ENVIRONMENT)
def serverScript = '''
try {
    def env = ENVIRONMENT ?: "dev"
    def command = ["sh", "/var/jenkins_home/scripts/get_servers.sh", env]
    def process = command.execute()
    process.waitFor()
    def output = process.in.text.trim()
    if (process.exitValue() != 0 || !output) { 
        return ["ERR-ScriptFailed"] 
    }
    def servers = output.split(/\\n/) as List
    return servers ?: ["ERR-NoServers"]
} catch (Exception e) { 
    return ["EXCEPTION-" + e.class.simpleName] 
}
'''

def serverParam = new CascadeChoiceParameter(
    'SERVER',
    'Select the server dynamically based on environment',
    new GroovyScript(
        new org.biouno.unochoice.model.ScriptlerScript('', []),
        serverScript
    ),
    org.biouno.unochoice.ChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
    'ENVIRONMENT',
    false,
    1
)
parameters.add(serverParam)

// 3. CONTAINER_NAME - Cascade Choice Parameter (depends on SERVER)
def containerScript = '''
try {
    if (!SERVER || SERVER.startsWith("ERR-") || SERVER.startsWith("EXCEPTION-")) { 
        return ["error-server-param"] 
    }
    def command = ["sh", "/var/jenkins_home/scripts/generate_container_name.sh", SERVER]
    def process = command.execute()
    process.waitFor()
    def output = process.in.text.trim()
    if (process.exitValue() == 0 && output) { 
        return [output] 
    } else { 
        return ["error-generating-name"] 
    }
} catch (Exception e) { 
    return ["exception-generating-name"] 
}
'''

def containerParam = new CascadeChoiceParameter(
    'CONTAINER_NAME',
    'Auto-generate container name from server selection',
    new GroovyScript(
        new org.biouno.unochoice.model.ScriptlerScript('', []),
        containerScript
    ),
    org.biouno.unochoice.ChoiceParameter.PARAMETER_TYPE_SINGLE_SELECT,
    'SERVER',
    false,
    1
)
parameters.add(containerParam)

// 4. Regular string parameters
parameters.add(new StringParameterDefinition('GIT_BRANCH', 'master', 'Git branch to checkout'))
parameters.add(new StringParameterDefinition('GIT_URL', 'https://github.com/mudunuri010/git-documentation', 'Git repository URL'))
parameters.add(new BooleanParameterDefinition('FORCE_REMOVE', true, 'Force remove existing container before deploy?'))

def paramProp = new ParametersDefinitionProperty(parameters)
job.addProperty(paramProp)

// Configure pipeline to load from SCM (Jenkinsfile from Git)
def scm = new GitSCM([
    new UserRemoteConfig(
        'https://github.com/mudunuri010/git-documentation',
        null,
        null,
        'git-credentials'
    )
])
scm.branches = [new BranchSpec('*/master')]

def definition = new CpsScmFlowDefinition(scm, 'Jenkinsfile')
definition.setLightweight(false)
job.setDefinition(definition)

// Save the job
job.save()

println "✅ Job '${jobName}' created successfully with dynamic parameters!"