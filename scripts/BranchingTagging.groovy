/*
 * example execution:
 *
 * groovy -cp scripts/groovy/classes \
 *  -Dfile.encoding=UTF-8 \
 *  -DGITHUB_API_TOKEN=<your token> \
 *  -DGITHUB_USERNAME=<your username> \
 *  -DGITHUB_PASSWORD=<your password> \
 *  -DGITHUB_USER_EMAIL=<your email> \
 *  -DCOMMAND=delete \
 *  -DBRANCHES=false \
 *  -DTAGS=true \
 *  -DPROJECT_TYPE=RELEASE \
 *  -DTARGET_BRANCH_TAG=8.0.0.1-R \
 *  -DCE_PROJECTS=true \
 *  -DEE_PROJECTS=true \
 *  -DNOOP=false \
 *  -DPARENT_TARGET_CLONE_DIR=github \
 *  -DGITHUB_PROJECTS_CSV_PATH=github-projects.csv \
 *  scripts/groovy/BranchingTagging.groovy
 */

String COMMAND                  = System.getProperty("COMMAND");
String BRANCHES                 = System.getProperty("BRANCHES") ?: "false";
String TAGS                     = System.getProperty("TAGS") ?: "false";
String TARGET_BRANCH_TAG        = System.getProperty("TARGET_BRANCH_TAG");
String PROJECT_TYPE             = System.getProperty("PROJECT_TYPE") ?: "RELEASE";
String CE_PROJECTS              = System.getProperty("CE_PROJECTS") ?: "true";
String EE_PROJECTS              = System.getProperty("EE_PROJECTS") ?: "true";
String NOOP                     = System.getProperty("NOOP");

boolean doCreateCommand = COMMAND.equals("create")
boolean doDeleteCommand = COMMAND.equals("delete")
boolean doBranchCommand = BRANCHES.equals("true")
boolean doTagCommand = TAGS.equals("true")
def targetBranchTag = TARGET_BRANCH_TAG
boolean isNOOP = NOOP.equals("true")

List<GithubProject> githubProjects = GithubProject.parseGithubProjectsCsv();
for ( GithubProject githubProject : githubProjects ) {

  if ( !githubProject.getProjectType().equals( PROJECT_TYPE ) ) {
    continue;
  }

  boolean isPrivate = Github.isProjectPrivate( githubProject.org, githubProject.name );
  boolean doCommandForCERepos = CE_PROJECTS.equals("true") && (isPrivate == false)
  boolean doCommandForEERepos = EE_PROJECTS.equals("true") && (isPrivate == true)

  // Branch/Tag creation
  if ( doCreateCommand && (doCommandForCERepos || doCommandForEERepos) ) {

     if ( doBranchCommand ) {
       if ( isNOOP ) {
         println("##### NOOP!  NOT creating ${targetBranchTag} branch for ${githubProject.name}:${githubProject.branch} ...");
       } else {
         println("##### creating ${targetBranchTag} branch for ${githubProject.name}:${githubProject.branch} ...");
         Github.createBranch( githubProject.org, githubProject.name, githubProject.branch, targetBranchTag)
       }
     }

     if ( doTagCommand ) {
       if ( isNOOP ) {
         println("##### NOOP!  NOT creating ${targetBranchTag} tag for ${githubProject.name}:${githubProject.branch} ...");
       } else {
         println("##### creating ${targetBranchTag} tag for ${githubProject.name}:${githubProject.branch} ...");
         Github.createTag( githubProject.org, githubProject.name, githubProject.branch, targetBranchTag)
       }
     }

  }

  // Branch/Tag deletion
  if ( doDeleteCommand && (doCommandForCERepos || doCommandForEERepos) ) {

     if ( doBranchCommand ) {
       if ( isNOOP ) {
         println("##### NOOP!  NOT deleting ${targetBranchTag} branch for ${githubProject.name} ...");
       } else {
         println("##### deleting ${targetBranchTag} branch for ${githubProject.name} ...");
         Github.deleteBranch( githubProject.org, githubProject.name, targetBranchTag)
       }
     }

     if ( doTagCommand ) {
       if ( isNOOP ) {
         println("##### NOOP!  NOT deleting ${targetBranchTag} tag for ${githubProject.name} ... ");
       } else {
         println("##### deleting ${targetBranchTag} tag for ${githubProject.name} ...");
         Github.deleteTag( githubProject.org, githubProject.name, targetBranchTag)
       }
     }

  }

  println ""
}
