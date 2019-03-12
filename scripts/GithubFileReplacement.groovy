import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileSystems;
import java.nio.file.Files;


String REPLACE          = System.getProperty("REPLACE");
String PUSH             = System.getProperty("PUSH");
String COMMIT_MSG       = System.getProperty("COMMIT_MSG");
String IVYSETTINGS_XML  = System.getProperty("IVYSETTINGS_XML");
String SUBFLOOR_XML     = System.getProperty("SUBFLOOR_XML");
String SUBFLOOR_EE_XML  = System.getProperty("SUBFLOOR_EE_XML");
String SUBFLOOR_PKG_XML = System.getProperty("SUBFLOOR_PKG_XML");
String SUBFLOOR_GWT_XML = System.getProperty("SUBFLOOR_GWT_XML");
String SUBFLOOR_JS_XML  = System.getProperty("SUBFLOOR_JS_XML");


System.out.println("REPLACE         : " + REPLACE);
System.out.println("PUSH            : " + PUSH);
System.out.println("COMMIT_MSG      : " + COMMIT_MSG);
System.out.println("IVYSETTINGS_XML : " + IVYSETTINGS_XML);
System.out.println("SUBFLOOR_XML    : " + SUBFLOOR_XML);
System.out.println("SUBFLOOR_EE_XML : " + SUBFLOOR_EE_XML);
System.out.println("SUBFLOOR_PKG_XML: " + SUBFLOOR_PKG_XML);
System.out.println("SUBFLOOR_GWT_XML: " + SUBFLOOR_GWT_XML);
System.out.println("SUBFLOOR_JS_XML : " + SUBFLOOR_JS_XML);


List<String> newFiles = new ArrayList<String>();
if (IVYSETTINGS_XML.toLowerCase().equals("true")) {
  newFiles.add("ivysettings.xml");
}
if (SUBFLOOR_XML.toLowerCase().equals("true")) {
  newFiles.add("subfloor.xml");
}
if (SUBFLOOR_EE_XML.toLowerCase().equals("true")) {
  newFiles.add("subfloor-ee.xml");
}
if (SUBFLOOR_PKG_XML.toLowerCase().equals("true")) {
  newFiles.add("subfloor-pkg.xml");
}
if (SUBFLOOR_GWT_XML.toLowerCase().equals("true")) {
  newFiles.add("subfloor-gwt.xml");
}
if (SUBFLOOR_JS_XML.toLowerCase().equals("true")) {
  newFiles.add("subfloor-js.xml");
}



public class Diff {
  
  public static boolean same(File file1, File file2) throws FileNotFoundException, IOException {

      FileReader fileReader1 = new FileReader(file1);
      FileReader fileReader2 = new FileReader(file2);

      BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
      BufferedReader bufferedReader2 = new BufferedReader(fileReader2);

      String line1 = null;
      String line2 = null;
      
      boolean same = true;
      
      while ((same == true) && ((line1 = bufferedReader1.readLine()) != null)
              && ((line2 = bufferedReader2.readLine()) != null)) {
          if (!line1.trim().equalsIgnoreCase(line2.trim())) {
              same = false;
              System.out.println("OLD: " + line2);
              System.out.println("NEW: " + line1);
          }
      }
              
      bufferedReader1.close();
      bufferedReader2.close();
      
      return same;
  }
  
}


public class SubfloorFileVisitor extends SimpleFileVisitor<Path> {
  
  private List<String> newFiles;
  private File changeLog;
  
  public SubfloorFileVisitor( List<String> newFiles, File changeLog ) {
    this.newFiles = newFiles;
    this.changeLog = changeLog;
  }
  
  @Override
  public FileVisitResult visitFile(Path filePath, BasicFileAttributes fileAttrs) {
          
    for (String newFileName : newFiles) {
      if (filePath.toString().endsWith(newFileName)) {
        System.out.println("checking " + filePath.toString());
          
        File newFile = new File( "subfloor/master-copies/" + newFileName);
        File existingFile = new File( filePath.toString() );
        if ( !Diff.same(newFile, existingFile) ) {
          
          System.out.println(filePath.toString() + " DIFFS FOUND! COMMITTING UPDATES");
          PrintWriter changeLogWriter = new PrintWriter(new FileWriter(this.changeLog,true));
          changeLogWriter.println(filePath.toString());
          changeLogWriter.close();

          // replace the file
          FileInputStream inStream = new FileInputStream(newFile);
          FileOutputStream outStream = new FileOutputStream(existingFile);
          byte[] buffer = new byte[1024];
          int length;
          while ((length = inStream.read(buffer)) > 0){
            outStream.write(buffer, 0, length);
          }
          if (inStream != null) {
            inStream.close();
          }
          if (outStream != null) {
            outStream.close();
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
  
  if (REPLACE.toLowerCase().equals("true")) {
    File changeLog = new File(  "replacements.log" );
    
    System.out.println( "cloning " + githubProject.getGithubSshUrl() + " : " + githubProject.getBranch() + " to " + projectDirName );
 
    File projectDir = new File( "github/" + projectDirName );
    
    Github.clone( githubProject.getGithubSshUrl(), githubProject.getBranch(), false, projectDir);

    System.out.println( "searching for files ..." );
    SubfloorFileVisitor fileVisitor = new SubfloorFileVisitor(newFiles, changeLog);
    Files.walkFileTree(FileSystems.getDefault().getPath("github/" + projectDirName), fileVisitor);
    
    Github.commit("github/" + projectDirName + "/.git", COMMIT_MSG);

  }
  
  if (PUSH.toLowerCase().equals("true")) {
    
    System.out.println("pushing commits on " + githubProject.getGithubSshUrl() + " to branch " + githubProject.getBranch());
    Github.pushCommits("github/" + projectDirName + "/.git");
   
  }
  
}


