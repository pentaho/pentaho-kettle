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
String BRANCHES                 = System.getProperty("BRANCHES");
String TAGS                     = System.getProperty("TAGS");
String TARGET_BRANCH_TAG        = System.getProperty("TARGET_BRANCH_TAG");
String TARGET_MONDRIAN_BRANCH   = System.getProperty("TARGET_MONDRIAN_BRANCH");
String TARGET_MONDRIAN4_BRANCH  = System.getProperty("TARGET_MONDRIAN4_BRANCH");
String PROJECT_TYPE             = System.getProperty("PROJECT_TYPE");
String CE_PROJECTS              = System.getProperty("CE_PROJECTS");
String EE_PROJECTS              = System.getProperty("EE_PROJECTS");
String NOOP                     = System.getProperty("NOOP");
String PARENT_TARGET_CLONE_DIR  = System.getProperty("PARENT_TARGET_CLONE_DIR");

boolean isNOOP = NOOP.equals("true")
boolean supportsSeperateMondrianBranching = TARGET_BRANCH_TAG.startsWith("7.1")

List<GithubProject> githubProjects = GithubProject.parseGithubProjectsCsv();
for ( GithubProject githubProject : githubProjects ) {

  if ( !githubProject.getProjectType().equals( PROJECT_TYPE ) ) {
    continue;
  }

  boolean isPrivate = Github.isProjectPrivate( githubProject.org, githubProject.name );

  // Deal with the mondrian branches at least until we can stop branching 7.1
  def targetBranchTag = TARGET_BRANCH_TAG
  if ( supportsSeperateMondrianBranching && (githubProject.name.contains("mondrian") || githubProject.name.contains("schema")) ) {
    if ( githubProject.targetCloneDir.contains("mondrian4") ) {
      targetBranchTag = TARGET_MONDRIAN4_BRANCH
    }
    else {
      targetBranchTag = TARGET_MONDRIAN_BRANCH
    }
  }

  // Branch/Tag creation
  if ( COMMAND.equals("create") &&
       ( ( CE_PROJECTS.equals("true") && (isPrivate == false) )
         ||
         ( EE_PROJECTS.equals("true") && (isPrivate == true) )
       )
     ) {

     if ( BRANCHES.equals("true") ) {
       if ( isNOOP ) {
         println("##### NOOP!  NOT creating ${targetBranchTag} branch for ${githubProject.name}:${githubProject.branch} ...");
       } else {
         println("##### creating ${targetBranchTag} branch for ${githubProject.name}:${githubProject.branch} ...");
         Github.createBranch( githubProject.org, githubProject.name, githubProject.branch, targetBranchTag)
       }
     }

     if ( TAGS.equals("true") ) {
       if ( isNOOP ) {
         println("##### NOOP!  NOT creating ${targetBranchTag} tag for ${githubProject.name}:${githubProject.branch} ...");
       } else {
         println("##### creating ${targetBranchTag} tag for ${githubProject.name}:${githubProject.branch} ...");
         Github.createTag( githubProject.org, githubProject.name, githubProject.branch, targetBranchTag)
       }
     }

  }

  // Branch/Tag deletion
  if ( COMMAND.equals("delete") &&
       ( ( CE_PROJECTS.equals("true") && (isPrivate == false) )
         ||
         ( EE_PROJECTS.equals("true") && (isPrivate == true) )
       )
     ) {

     if ( BRANCHES.equals("true") ) {
       if ( isNOOP ) {
         println("##### NOOP!  NOT deleting ${targetBranchTag} branch for ${githubProject.name} ...");
       } else {
         println("##### deleting ${targetBranchTag} branch for ${githubProject.name} ...");
         Github.deleteBranch( githubProject.org, githubProject.name, targetBranchTag)
       }
     }

     if ( TAGS.equals("true") ) {
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
