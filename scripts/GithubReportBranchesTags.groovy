
List<GithubProject> githubProjects = GithubProject.parseGithubProjectsCsv();
for (GithubProject githubProject : githubProjects ) {
  
  if ( !githubProject.getProjectType().equals("RELEASE") ) {
    continue;
  }
  
  ArrayList<String> repoBranches = Github.getRepoBranches( projectName );
  ArrayList<String> repoTags = Github.getRepoTags( projectName );
  
  githubProject.setBranches(repoBranches);
  githubProject.setTags(repoTags);
  
  int numBranches = repoBranches.size();
  int numTags = repoTags.size();
  
  String formattedHeader = String.format("PROJECT: %-40s %12s BRANCHES %16s TAGS", projectName, numBranches, numTags);
  System.out.println("==============================================================================================");
  System.out.println(formattedHeader);
  System.out.println("==============================================================================================");
  
  int count = 0;
  while (( count < numBranches) || (count < numTags) ) {
    String branch = "";
    String tag = "";
    
    if (count < numBranches) {
      branch = repoBranches.get(count);
    }
    
    if (count < numTags) {
      tag = repoTags.get(count);
    }
    
    String formattedLine = String.format( "%71s %21s", branch, tag );
    System.out.println(formattedLine);
    
    count++;
  }
}
