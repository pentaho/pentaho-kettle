import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


String REPLACE             = System.getProperty("REPLACE");
String PUSH                = System.getProperty("PUSH");
String COMMIT_MSG          = System.getProperty("COMMIT_MSG");
String CURRENT_GROUP_ID    = System.getProperty("CURRENT_GROUP_ID");
String CURRENT_ARTIFACT_ID = System.getProperty("CURRENT_ARTIFACT_ID");
String CURRENT_VERSION     = System.getProperty("CURRENT_VERSION");
String DESIRED_GROUP_ID    = System.getProperty("DESIRED_GROUP_ID");
String DESIRED_ARTIFACT_ID = System.getProperty("DESIRED_ARTIFACT_ID");
String DESIRED_VERSION     = System.getProperty("DESIRED_VERSION");


public class DepFileVisitor extends SimpleFileVisitor<Path> {
  
  private String currentGroupId;
  private String currentArtifactId;
  private String currentVersion;
  private String desiredGroupId;
  private String desiredArtifactId;
  private String desiredVersion; 
  private String mavenGavRegEx;
  private String ivyGavRegEx;
  private String bundleGavRegEx;
  
  private static int GROUP_ID_REG_EX_GROUP_NUM = 2;
  private static int ARTIFACT_ID_REG_EX_GROUP_NUM = 5;
  private static int VERSION_REG_EX_GROUP_NUM = 8;
  
  public DepFileVisitor(  String currentGroupId,
                          String currentArtifactId,
                          String currentVersion,
                          String desiredGroupId,
                          String desiredArtifactId,
                          String desiredVersion ) {
                          
    this.currentGroupId      = currentGroupId;
    this.currentArtifactId   = currentArtifactId;
    this.currentVersion      = currentVersion;
    this.desiredGroupId      = desiredGroupId;
    this.desiredArtifactId   = desiredArtifactId;
    this.desiredVersion      = desiredVersion;
    
    this.mavenGavRegEx  = "(<groupId>(" + currentGroupId + ")</groupId>)" +
                          "(\\s*?)" +
                          "(<artifactId>(" + currentArtifactId + ")</artifactId>)" +
                          "(\\s*?)" +
                          "(<version>(" + currentVersion + ")</version>)";
    this.ivyGavRegEx    = "(org=\"(" + currentGroupId + ")\")" +
                          "(\\s*?)" +
                          "(name=\"(" + currentArtifactId + ")\")" +
                          "(\\s*?)" +
                          "(rev=\"(" + currentVersion + ")\")";
    this.bundleGavRegEx = "(<bundle>mvn:(" + currentGroupId + "))" +
                          "(/)" +
                          "((" + currentArtifactId + "))" +
                          "(/)" +
                          "((" + currentVersion + ")</bundle>)";
  }
  

  @Override
  public FileVisitResult visitFile(Path filePath, BasicFileAttributes fileAttrs) {
          
    if ( filePath.toString().endsWith("pom.xml") ||
         filePath.toString().endsWith("ivy.xml") ||
         ( filePath.toString().endsWith(".xml") && filePath.toString().contains("features") ) ) {

      System.out.println("checking " + filePath.toString());
      
      BufferedReader reader = new BufferedReader(new FileReader(filePath.toString()));
      StringBuffer fileBuffer = new StringBuffer();
      String line = "";
      while ((line = reader.readLine()) != null) {
        fileBuffer.append(line + "\n");
      }
      reader.close();

      String fileContents = fileBuffer.toString();
      
      // MAVEN
      if ( filePath.toString().endsWith("pom.xml") ) {
        // get at least one match before diving into a recursive replacement
        if ( Pattern.compile(this.mavenGavRegEx).matcher(fileContents).find() ) {
          
          System.out.println("found possible match in " + filePath.toString());
  
          fileContents = this.replaceGAV(fileContents, this.mavenGavRegEx, 0);
          
          // write the new file
          System.out.println("performing replacement ...");
          FileWriter writer = new FileWriter(filePath.toString());
          writer.write(fileContents);
          writer.close();
        }
      }
      
      // IVY
      if ( filePath.toString().endsWith("ivy.xml") ) {
        System.out.println("checking " + filePath.toString());
        // get at least one match before diving into a recursive replacement
        if ( Pattern.compile(this.ivyGavRegEx).matcher(fileContents).find() ) {
          
          System.out.println("found possible match in " + filePath.toString());
  
          fileContents = this.replaceGAV(fileContents, this.ivyGavRegEx, 0);
          
          // write the new file
          System.out.println("performing replacement ...");
          FileWriter writer = new FileWriter(filePath.toString());
          writer.write(fileContents);
          writer.close();
        }
      }
      
      // BUNDLE
      if ( filePath.toString().endsWith(".xml") && filePath.toString().contains("features") ) {
        // get at least one match before diving into a recursive replacement
        if ( Pattern.compile(this.bundleGavRegEx).matcher(fileContents).find() ) {
          
          System.out.println("found possible match in " + filePath.toString());
  
          fileContents = this.replaceGAV(fileContents, this.bundleGavRegEx, 0);
          
          // write the new file
          System.out.println("performing replacement ...");
          FileWriter writer = new FileWriter(filePath.toString());
          writer.write(fileContents);
          writer.close();
        }
      }

    }
    return FileVisitResult.CONTINUE;
  }
  

  public String replaceGAV(String fileContents, String gavRegEx, int startPos) {

    StringBuilder xmlStringBuilder = new StringBuilder(fileContents);

    Matcher matcher = Pattern.compile(gavRegEx).matcher(fileContents);
    matcher.find(startPos);
    
    System.out.println("found:          " + matcher.group(GROUP_ID_REG_EX_GROUP_NUM) + ":" + matcher.group(ARTIFACT_ID_REG_EX_GROUP_NUM) + ":" + matcher.group(VERSION_REG_EX_GROUP_NUM));
    System.out.println("replacing with: " + this.desiredGroupId + ":" + this.desiredArtifactId + ":" + this.desiredVersion);
    
    xmlStringBuilder = xmlStringBuilder.replace(matcher.start(GROUP_ID_REG_EX_GROUP_NUM), matcher.end(GROUP_ID_REG_EX_GROUP_NUM), this.desiredGroupId);
    int offset = this.desiredGroupId.length() - (matcher.end(GROUP_ID_REG_EX_GROUP_NUM) - matcher.start(GROUP_ID_REG_EX_GROUP_NUM));
    xmlStringBuilder = xmlStringBuilder.replace(matcher.start(ARTIFACT_ID_REG_EX_GROUP_NUM) + offset, matcher.end(ARTIFACT_ID_REG_EX_GROUP_NUM) + offset, this.desiredArtifactId);
    offset = offset + this.desiredArtifactId.length() - (matcher.end(ARTIFACT_ID_REG_EX_GROUP_NUM) - matcher.start(ARTIFACT_ID_REG_EX_GROUP_NUM));
    xmlStringBuilder = xmlStringBuilder.replace(matcher.start(VERSION_REG_EX_GROUP_NUM) + offset, matcher.end(VERSION_REG_EX_GROUP_NUM) + offset, this.desiredVersion);
    
    String newFileContents = xmlStringBuilder.toString();
    
    if ( Pattern.compile(gavRegEx).matcher(newFileContents).find(matcher.end(VERSION_REG_EX_GROUP_NUM) + offset) ) {
      // there's more GAV(s), so recurse
      newFileContents = this.replaceGAV(newFileContents, gavRegEx, matcher.end(VERSION_REG_EX_GROUP_NUM) + offset);
    }
    
    return newFileContents;
    
  }

  
}




File githubDir = new File("github");
if (!githubDir.exists()) {
  githubDir.mkdir();
}



List<GithubProject> githubProjects = GithubProject.parseGithubProjectsCsv();
System.out.println( githubProjects.size() + " projects found" );
for (GithubProject githubProject : githubProjects ) {
  
  if ( !githubProject.getProjectType().equals("RELEASE") ) {
    continue;
  }
  
  String projectDirName = githubProject.getName() + "-" + githubProject.getBranch();
  
  if (REPLACE.toLowerCase().equals("true")) {
    
    System.out.println( "cloning " + githubProject.getGithubSshUrl() + " : " + githubProject.getBranch() + " to " + projectDirName );
 
    File projectDir = new File( "github/" + projectDirName );
    
    LocalGit.clone( githubProject.getGithubSshUrl(), githubProject.getBranch(), false, projectDir);

    System.out.println( "searching for files ..." );
    DepFileVisitor fileVisitor = new DepFileVisitor(  CURRENT_GROUP_ID,
                                                      CURRENT_ARTIFACT_ID,
                                                      CURRENT_VERSION,
                                                      DESIRED_GROUP_ID,
                                                      DESIRED_ARTIFACT_ID,
                                                      DESIRED_VERSION );

    Files.walkFileTree(FileSystems.getDefault().getPath("github/" + projectDirName), fileVisitor);
    
    LocalGit.commit("github/" + projectDirName + "/.git", COMMIT_MSG);

  }
  
  if (PUSH.toLowerCase().equals("true")) {
    
    System.out.println("pushing commits on " + githubProject.getGithubSshUrl() + " to branch " + githubProject.getBranch());
    LocalGit.pushCommits("github/" + projectDirName + "/.git");
   
  }
  
}
