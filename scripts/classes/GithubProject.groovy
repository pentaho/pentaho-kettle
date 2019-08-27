@GrabResolver(name='pentaho', root='https://nexus.pentaho.org/content/groups/omni')
@Grab('org.yaml:snakeyaml:1.18')
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions

import static groovy.json.JsonOutput.*

public class GithubProject {
  
  static String GITHUB_PROJECTS_CSV_PATH = System.getProperty("GITHUB_PROJECTS_CSV_PATH");
  static String BUILD_DATA_PATH = System.getProperty("BUILD_DATA_PATH");
  
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
  
  public static List<GithubProject> parse() {
     if (BUILD_DATA_PATH) {
        return parseYaml()
     }
     else if (GITHUB_PROJECTS_CSV_PATH) {
        return parseGithubProjectsCsv()
     }
     else {
        throw new RuntimeException( "*** FAILURE! BUILD_DATA_PATH and GITHUB_PROJECTS_CSV_PATH is unspecified" );
     }
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

  protected static String replaceFromMap(def str, def buildProperties) {
      buildProperties.each { k, v ->
        def token = "\${$k}"
        if (str.contains(token)) {
          str = str.replace(token, v)
        }
      }
      return str
  }

  public static List<GithubProject> parseYaml() {

    List<GithubProject> githubProjects = new ArrayList<GithubProject>();
    InputStream input = new FileInputStream(new File(BUILD_DATA_PATH));
    Yaml yaml = new Yaml()
    Map<String, Object> buildData = yaml.load(input)

    def buildProperties = buildData['buildProperties']
    //println prettyPrint(toJson(buildProperties))

    def scmData = []
    def jobGroups = buildData['jobGroups']
    jobGroups.each { jobGroup, jobItems ->
      jobItems.each { job ->
         def url = replaceFromMap(job['scmUrl'], buildProperties)
         def branch = replaceFromMap(job['scmBranch'], buildProperties) ?: buildProperties['DEFAULT_BRANCH'] ?: "undefined_branch"

         def item = [ url:url, branch: branch ]
         scmData.add(item)
      }
    }

    // making scm items unique by repo/branch
    scmData = scmData.unique(false) { left, right -> left.url == right.url && right.branch == right.branch ? 0 : 1 }

    //println prettyPrint(toJson(scmData))

    scmData.each { it -> 
      def url = it.url
      def branch = it.branch
      String project = url.substring( url.indexOf("://") + 3, url.indexOf(".git") )
      String[] orgNameSplit = project.split("/");
      GithubProject githubProject = new GithubProject( orgNameSplit[1],
                                                       orgNameSplit[2],
                                                       "RELEASE",
                                                       url,
                                                       branch,
                                                       "" );
      githubProjects.add(githubProject);
    }

    return githubProjects;
  }

  
}