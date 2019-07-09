
public class GithubProject {
  
  static String GITHUB_PROJECTS_CSV_PATH = System.getProperty("GITHUB_PROJECTS_CSV_PATH");
  
  private String org;
  private String name;
  private String projectType;
  private String githubSshUrl;
  private String branch;
  private String targetCloneDir;
  
  public GithubProject( String org,
                        String name,
                        String projectType,
                        String githubSshUrl,
                        String branch,
                        String targetCloneDir) {
    this.org = org;
    this.name = name;
    this.projectType = projectType;
    this.githubSshUrl = githubSshUrl;
    this.branch = branch;
    this.targetCloneDir = targetCloneDir;
  }
  
  public String getOrg() {
    return this.org;
  }
  
  public String getName() {
    return this.name;
  }
                        
  public String getProjectType() {
    return this.projectType;
  }
  
  public String getGithubSshUrl() {
    return this.githubSshUrl;
  }

  public String getBranch() {
    return this.branch;
  }
   
  public String getTargetCloneDir() {
    return this.targetCloneDir;
  }
  
  
  
  public static List<GithubProject> parseGithubProjectsCsv() {

    List<GithubProject> githubProjects = new ArrayList<GithubProject>();
    FileReader githubFileReader = new FileReader(new File(GITHUB_PROJECTS_CSV_PATH));
    BufferedReader bufferedReader = new BufferedReader(githubFileReader);
    String line = null;
    while ((line = bufferedReader.readLine()) != null) {
      String[] lineItems    = line.split(",", -1);
      String projectType    = lineItems[0];
      String githubSshUrl   = lineItems[1];
      String branch         = lineItems[2];
      String targetCloneDir = lineItems[3];
      String githubProjectName = githubSshUrl.substring( githubSshUrl.indexOf(":") + 1, githubSshUrl.indexOf(".git") );
      String[] orgNameSplit = githubProjectName.split("/");
      GithubProject githubProject = new GithubProject( orgNameSplit[0],
                                                       orgNameSplit[1],
                                                       projectType,
                                                       githubSshUrl,
                                                       branch,
                                                       targetCloneDir );
      githubProjects.add(githubProject);
    }

    return githubProjects;
  }

  
}