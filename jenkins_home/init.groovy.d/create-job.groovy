import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import hudson.plugins.git.GitSCM
import hudson.plugins.git.BranchSpec
import hudson.plugins.git.UserRemoteConfig
import hudson.plugins.git.extensions.impl.CleanBeforeCheckout

def jenkins = Jenkins.instance
def jobName = 'git-documentation-pipeline'

println "🔍 Checking for existing job: ${jobName}"

// Delete existing job if it exists
def existingJob = jenkins.getItem(jobName)
if (existingJob != null) {
    println "🧹 Deleting existing job..."
    existingJob.delete()
}

println "⚙️ Creating new job: ${jobName}"

// Create the pipeline job
def job = jenkins.createProject(WorkflowJob.class, jobName)
job.setDescription('Automated CI/CD pipeline for git-documentation')

// Configure to use Jenkinsfile from Git repository
def scm = new GitSCM(
    [new UserRemoteConfig(
        'https://github.com/mudunuri010/git-documentation',
        '',
        '',
        'git-credentials'
    )],
    [new BranchSpec('*/master')],
    false,
    [],
    null,
    null,
    [new CleanBeforeCheckout()]
)

def definition = new CpsScmFlowDefinition(scm, 'Jenkinsfile')
definition.setLightweight(true)
job.setDefinition(definition)

// Save the job
println "💾 Saving job..."
job.save()

println "✅ Job '${jobName}' created successfully!"
println "📝 The job will use the Jenkinsfile from the repository with all parameter definitions."