import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import hudson.plugins.git.GitSCM
import hudson.plugins.git.BranchSpec

def jenkins = Jenkins.instance
def pipelineJobName = 'git-documentation-pipeline'

// Delete existing pipeline job if it exists
jenkins.getItem(pipelineJobName)?.delete()

println "🚀 Creating pipeline job: ${pipelineJobName}"

// Create a Pipeline job (not FreeStyle)
def pipelineJob = jenkins.createProject(WorkflowJob, pipelineJobName)
pipelineJob.setDescription('Pipeline for git-documentation with dynamic parameters')

// Configure Git SCM
def scm = new GitSCM('https://github.com/mudunuri010/git-documentation')
scm.branches = [new BranchSpec('*/master')]

// Set the pipeline definition to use SCM (Jenkinsfile from Git)
def flowDefinition = new CpsScmFlowDefinition(scm, 'Jenkinsfile')
flowDefinition.setLightweight(true)
pipelineJob.setDefinition(flowDefinition)

// Save the job
pipelineJob.save()

println "✅ Pipeline job '${pipelineJobName}' created successfully!"
println "📄 Pipeline will be loaded from Jenkinsfile in the Git repository"
