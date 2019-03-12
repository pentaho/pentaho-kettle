/*
 * updated 11/12/2017
 *
 * this will attempt to guess the license header of every source file and generate an ee and ce csv file
 * with the result
 *
 * set +e
 * rm -rf github
 * rm -f ee-license-headers.csv
 * rm -f ce-license-headers.csv
 * groovy -cp scripts/groovy/classes \
 *   -Dfile.encoding=UTF-8 \
 *   -DGITHUB_API_TOKEN=<token> \
 *   -DGITHUB_USERNAME=<username> \
 *   -DGITHUB_PASSWORD=<password> \
 *   -DGITHUB_USER_EMAIL=<email> \
 *   -DCOMMAND_MAKE_COMMITS=TRUE \
 *   -DCOMMAND_PUSH_COMMITS=FALSE \
 *   -DCOMMAND_MAKE_PULL_REQUESTS=FALSE \
 *   -DCOMMAND_MERGE_PULL_REQUESTS=FALSE \
 *   -DPARENT_TARGET_CLONE_DIR=github \
 *   -DGITHUB_FORK=smaring \
 *   -DGITHUB_FEATURE_BRANCH=ENGOPS-1757 \
 *   -DGITHUB_FORCE_PUSH=TRUE \
 *   -DCOMMIT_MESSAGE="[ENGOPS-1757] version name consolidation" \
 *   scripts/groovy/version-property-consolidation.groovy
 */

@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.21')

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;

import groovy.util.logging.Slf4j;


@Slf4j
@groovy.transform.InheritConstructors
public class SourceFileVisitor extends SimpleFileVisitor<Path> {

  static HashMap<String,String> namesMap = new HashMap<String,String>();
  GithubProject githubProject;
  List<String> namesUsed = new ArrayList<String>();

  static {
    log.info( "loading version-migration.properties file ..." );
    InputStream propInputStream = this.getClass().getResourceAsStream( "version-migration.properties" );
    BufferedReader reader = new BufferedReader( new InputStreamReader( propInputStream ) );
    String line = "";
    while ((line = reader.readLine()) != null) {
      String[] namePair = line.split( "=" );
      String propertyName = namePair[0];
      String propertyValue = namePair[1].trim();
      log.info( "registering translation '" + propertyName + "' => '" + propertyValue + "'" );
      namesMap.put( propertyName, propertyValue );
    }
    reader.close();
  }
  

  SourceFileVisitor( GithubProject githubProject ) {
    this.githubProject = githubProject;
  }

  SourceFileVisitor() {}

  @Override
  public FileVisitResult visitFile(Path filePath, BasicFileAttributes fileAttrs) {

    
    if (  filePath.toString().endsWith(".java") ||
          filePath.toString().endsWith(".js")  ||
          filePath.toString().endsWith(".jsp") ||
          filePath.toString().endsWith(".css") ||
          filePath.toString().endsWith(".jsdoc") ||
          filePath.toString().endsWith(".json") ||
          filePath.toString().endsWith(".html") ||
          filePath.toString().endsWith(".htm") ||
          filePath.toString().endsWith(".properties") ||
          filePath.toString().endsWith(".sh") ||
          filePath.toString().endsWith(".mib") ||
          filePath.toString().endsWith(".md") ||
          filePath.toString().endsWith(".bat") ||
          filePath.toString().endsWith(".cfg") ||
          filePath.toString().endsWith(".txt") ||
          filePath.toString().endsWith(".xml") ||
          filePath.toString().endsWith(".xul") ||
          filePath.toString().endsWith(".ktr") ||
          filePath.toString().endsWith(".kjb") ||
          filePath.toString().endsWith(".xsd") ||
          filePath.toString().endsWith(".dtd") ||
          filePath.toString().endsWith(".xreportspec") ||
          filePath.toString().endsWith(".xaction") ) {
          
      totalFilesChecked++;
      log.info( filePath.toString() );
      log.info( "totalFilesChecked: " + totalFilesChecked );

      byte[] bufferBytes = new byte[4096];
      
      FileInputStream inputStream = new FileInputStream( filePath.toFile() );
      ArrayList<Byte> fileByteBufferList = new ArrayList<Byte>();
      boolean fileChanged = false;
      int byteInt;
      int charPosition = 0;
      
      while ((byteInt = inputStream.read()) != -1) {
      
        bufferBytes[charPosition] = (byte) byteInt;
        char byteChar = (char) byteInt;
        charPosition++;
        if (charPosition == 4095) {
          // if a line is this long you are probably dealing with minified js, so just give up
          return FileVisitResult.CONTINUE;
        }
        
        if ( byteChar == '\n' || inputStream.available() == 0 ) {
          boolean lineChanged = false;
          byte[] originalLineBytes = Arrays.copyOfRange( bufferBytes, 0, charPosition );
          String line = new String( originalLineBytes, StandardCharsets.UTF_8 );
          
          for ( String oldName : namesMap.keySet() ) {
            
            String newName = namesMap.get( oldName );
            
            if ( line.contains( oldName ) ) {
              line = this.replaceName( line, oldName, newName, 0 )
              lineChanged = true;
              fileChanged = true;
            }
            
            if ( line.contains( newName ) ) {
              if ( !namesUsed.contains( newName ) ) {
                namesUsed.add( newName );
              }
            }

          }
          
          if (lineChanged) {
            byte[] modifiedLineBytes = line.getBytes( StandardCharsets.UTF_8 );
            for ( int i=0; i < modifiedLineBytes.length; i++ ) {
              fileByteBufferList.add( new Byte( modifiedLineBytes[i] ) );
            }
          } else {
            for ( int i=0; i < originalLineBytes.length; i++ ) {
              fileByteBufferList.add( new Byte( originalLineBytes[i] ) );
            }
          }
      
          charPosition = 0;
        }
      
      }
      
      inputStream.close();
      
      if ( fileChanged ) {
        FileOutputStream outputStream = new FileOutputStream( filePath.toFile() );
        byte[] fileBytes = new byte[ fileByteBufferList.size() ];
        for ( int i=0; i < fileByteBufferList.size(); i++ ) {
          Byte fileByte = fileByteBufferList.get(i);
          fileBytes[i] = fileByte.byteValue();
        }
        outputStream.write( fileBytes );
        outputStream.flush();
        outputStream.close();
        totalFilesChanged++;
        log.info( "totalFilesChanged: " + totalFilesChanged );
      }
      
    }

    
    return FileVisitResult.CONTINUE;
  }
  
  
  private String replaceName( String text, String oldName, String newName, int startPos ) {
    
    StringBuilder stringBuilder = new StringBuilder(text);

    Matcher matcher = Pattern.compile(oldName).matcher(text);
    boolean matchFound = matcher.find(startPos); // we wouldn't be here if we didn't already have a match
    int matchStart = matcher.start(groupNum);
    int matchEnd = matcher.end(groupNum);
    String match = matcher.group(groupNum);
    log.info( "replacing " + oldName + " with " + newName );
    stringBuilder = stringBuilder.replace(matchStart, matchEnd, newName);
    String newText = stringBuilder.toString();

    // check for more matches in same text and recurse
    matcher = Pattern.compile(oldName).matcher(newText);
    startPos = matchStart + newName.length();
    matchFound = matcher.find(startPos);
    if (matchFound && matcher.group(groupNum) != null) {
      log.info( "found " + matcher.group(groupNum) );
      newText = this.replaceName( newText, oldName, newName, startPos );
    }
    
    return newText;
  }
  
  
  
}



File githubDir = new File(PARENT_TARGET_CLONE_DIR);
if (!githubDir.exists()) {
  githubDir.mkdir();
}


List<GithubProject> githubProjects = GithubProject.parseGithubProjectsCsv();
for (GithubProject githubProject : githubProjects ) {

  if ( !githubProject.getProjectType().equals("RELEASE") ) {
    continue;
  }

  LocalGit.clone( githubProject.getOrg(),
      "git@github.com:" + githubProject.getOrg() + "/" + githubProject.getName() + ".git",
      githubProject.getBranch(),
      false,
      new File(PARENT_TARGET_CLONE_DIR + "/" + githubProject.getName()) );

  SourceFileVisitor sourceFileVisitor = new SourceFileVisitor( githubProject );
  Files.walkFileTree(FileSystems.getDefault().getPath(PARENT_TARGET_CLONE_DIR + "/" + githubProject.getName()), sourceFileVisitor);
}