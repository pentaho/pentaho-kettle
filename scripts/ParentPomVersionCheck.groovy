import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


String PARENT_POM_VERSION     = System.getProperty("PARENT_POM_VERSION");
String GIT_CLONE_FROM         = System.getProperty("GIT_CLONE_FROM");
String GITHUBAK_BASE_URL      = System.getProperty("GITHUBAK_BASE_URL");


public class PomFileVisitor extends SimpleFileVisitor<Path> {

  public static int returnCode = 0;

  private String parentPomVersion;
  private List<String> parentPomRegExList = new ArrayList<String>();

  private static int GROUP_ID_REG_EX_GROUP_NUM = 2;
  private static int ARTIFACT_ID_REG_EX_GROUP_NUM = 5;
  private static int VERSION_REG_EX_GROUP_NUM = 8;


  public PomFileVisitor(  String parentPomVersion ) {

    this.parentPomVersion = parentPomVersion;

    String[][] parentPomGavs = new String[6][2];
    parentPomGavs[0][0] = "org.pentaho";
    parentPomGavs[0][1] = "pentaho-ce-parent-pom";
    parentPomGavs[1][0] = "org.pentaho";
    parentPomGavs[1][1] = "pentaho-ce-jar-parent-pom";
    parentPomGavs[2][0] = "org.pentaho";
    parentPomGavs[2][1] = "pentaho-ce-bundle-parent-pom";
    parentPomGavs[3][0] = "com.pentaho";
    parentPomGavs[3][1] = "pentaho-ee-parent-pom";
    parentPomGavs[4][0] = "com.pentaho";
    parentPomGavs[4][1] = "pentaho-ee-jar-parent-pom";
    parentPomGavs[5][0] = "com.pentaho";
    parentPomGavs[5][1] = "pentaho-ee-bundle-parent-pom";

    for (String[] parentPomGav : parentPomGavs ) {
      String groupId =    parentPomGav[0];
      String artifactId = parentPomGav[1];
      String version =    ".*?";
      String parentPomRegEx = "(<groupId>("     + groupId +     ")</groupId>)" +
                              "(\\s*?)" +
                              "(<artifactId>("  + artifactId +  ")</artifactId>)" +
                              "(\\s*?)" +
                              "(<version>("     + version +     ")</version>)";
      parentPomRegExList.add( parentPomRegEx );
    }

  }


  @Override
  public FileVisitResult visitFile(Path filePath, BasicFileAttributes fileAttrs) {

    if ( filePath.toString().endsWith("pom.xml") ) {

      //System.out.println("checking " + filePath.toString());

      BufferedReader reader = new BufferedReader(new FileReader(filePath.toString()));
      StringBuffer fileBuffer = new StringBuffer();
      String line = "";
      while ((line = reader.readLine()) != null) {
        fileBuffer.append(line + "\n");
      }
      reader.close();

      String fileContents = fileBuffer.toString();


      Iterator<String> parentPomRegExListIterator = parentPomRegExList.iterator();

      while( parentPomRegExListIterator.hasNext() ) {
        String parentPomRegEx = parentPomRegExListIterator.next();

        Matcher matcher = Pattern.compile(parentPomRegEx).matcher(fileContents);

        if ( matcher.find() ) {
          String foundVersion = matcher.group(VERSION_REG_EX_GROUP_NUM);
          if ( foundVersion.equals(this.parentPomVersion) ) {
            System.out.println( matcher.group(GROUP_ID_REG_EX_GROUP_NUM) + ":" +
                                matcher.group(ARTIFACT_ID_REG_EX_GROUP_NUM) + ":" +
                                matcher.group(VERSION_REG_EX_GROUP_NUM) + " in " + filePath.toString());
          } else {
            System.err.println( "################################################################################" );
            System.err.println( "FAILURE: " + matcher.group(GROUP_ID_REG_EX_GROUP_NUM) + ":" +
                                matcher.group(ARTIFACT_ID_REG_EX_GROUP_NUM) + ":" +
                                matcher.group(VERSION_REG_EX_GROUP_NUM) + " in " + filePath.toString());
            System.err.println( "################################################################################" );
            PomFileVisitor.returnCode = 1;
          }
        }
      }

    }

    return FileVisitResult.CONTINUE;
  }

}



File githubDir = new File("github");
if (!githubDir.exists()) {
  githubDir.mkdir();
}

List<GithubProject> githubProjects = GithubProject.parseGithubProjectsCsv();
for (GithubProject githubProject : githubProjects ) {

  if ( !githubProject.getProjectType().equals("RELEASE") ) {
    continue;
  }

  String projectDirName = githubProject.getName() + "-" + githubProject.getBranch();

  File projectDir = new File( "github/" + projectDirName );

  if ( GIT_CLONE_FROM.equals("githubak") ) {

    //System.out.println("cloning " + githubProject.getGithubSshUrl() + " " + githubProject.getBranch() + " from Githubak to " + githubProject.getTargetCloneDir());

    LocalGit.clone( GITHUBAK_BASE_URL + "/" + githubProject.getOrg() + "/latest/" + githubProject.getName() + ".git",
                    githubProject.getBranch(),
                    false,
                    projectDir);

  } else {

    //System.out.println("cloning " + githubProject.getGithubSshUrl() + " " + githubProject.getBranch() + " from Github to " + githubProject.getTargetCloneDir());

    LocalGit.clone( githubProject.getGithubSshUrl(),
                    githubProject.getBranch(),
                    false,
                    projectDir);

  }

  //System.out.println( "searching for files ..." );
  PomFileVisitor fileVisitor = new PomFileVisitor(  PARENT_POM_VERSION );

  Files.walkFileTree(FileSystems.getDefault().getPath("github/" + projectDirName), fileVisitor);

}

System.exit(PomFileVisitor.returnCode);
