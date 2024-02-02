@GrabResolver(name='pentaho', root='https://${PENTAHO_JFROG_SA_USERNAME}:${PENTAHO_JFROG_SA_TOKEN}@one.hitachivantara.com/artifactory/pnt-mvn', m2Compatible='true')
@Grab(group='org.apache.commons', module='commons-compress', version='1.19')
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

@Grab(group='commons-io', module='commons-io', version='2.6')
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.Array;

String VERSION_FOR_DIR            = System.getProperty("VERSION_FOR_DIR");
String SUITE_RELEASE_VERSION      = System.getProperty("SUITE_RELEASE_VERSION");
String BUILD_NUMBER               = System.getProperty("BUILD_NUMBER");
String JAVADOC_SRC_DIR            = System.getProperty("JAVADOC_SRC_DIR");
String JAVADOC_TARGET_DIR         = System.getProperty("JAVADOC_TARGET_DIR");


System.out.println("VERSION_FOR_DIR           : " + VERSION_FOR_DIR);
System.out.println("SUITE_RELEASE_VERSION     : " + SUITE_RELEASE_VERSION);
System.out.println("BUILD_NUMBER              : " + BUILD_NUMBER);
System.out.println("JAVADOC_SRC_DIR           : " + JAVADOC_SRC_DIR);
System.out.println("JAVADOC_TARGET_DIR        : " + JAVADOC_TARGET_DIR);


public class ZipFile {
  
  public static void addToArchive( ArchiveOutputStream taos, File file, String dir ) {
    
    taos.putArchiveEntry( new ZipArchiveEntry( file, dir ) );
    if (file.isFile()) {
      // Add the file to the archive
      System.out.println("adding " + file.getName() + " to " + dir);
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
      IOUtils.copy(bis, taos);
      taos.closeArchiveEntry();
      bis.close();
    } else if (file.isDirectory()) {
      taos.closeArchiveEntry();
      for (File childFile : file.listFiles()) {
        ZipFile.addToArchive( taos, childFile, dir + "/" + childFile.getName());
      }
    }
     
  }
  
}


Path distDirPath = FileSystems.getDefault().getPath("dist").toAbsolutePath();
distDirPath.toFile().mkdir();
Path zipFilePath = FileSystems.getDefault().getPath("dist/javadoc-" + SUITE_RELEASE_VERSION + ".zip").toAbsolutePath();
FileOutputStream fos = new FileOutputStream( zipFilePath.toFile() );
ZipArchiveOutputStream zaos = new ZipArchiveOutputStream( new BufferedOutputStream(fos) );
zaos.setEncoding("UTF-8");
zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);


HashMap<String, List> javadocMap = new HashMap<String, List>();

FileReader fileReader = new FileReader(new File("resources/config/javadoc.properties"));
BufferedReader bufferedReader = new BufferedReader(fileReader);
String line = null;
while ((line = bufferedReader.readLine()) != null) {
  if ( line.startsWith("#") || line.length() == 0 ) {
    continue;
  }
  line = line.replace("\${VERSION_FOR_DIR}",VERSION_FOR_DIR);
  line = line.replace("\${SUITE_RELEASE_VERSION}",SUITE_RELEASE_VERSION);
  line = line.replace("\${BUILD_NUMBER}",BUILD_NUMBER);
  System.out.println(line);
  
  String[] tokens   = line.split(",");
  String parentDir  = tokens[0];
  String title      = tokens[1];
  String javadocDir = tokens[2];
  
  
  if (!javadocMap.containsKey(parentDir + "," + title)) {
    List<String> javadocDirs = new ArrayList<String>();
    javadocDirs.add(javadocDir);
    javadocMap.put(parentDir + "," + title, javadocDirs);
  } else {
    List<String> javadocDirs = javadocMap.get(parentDir + "," + title);
    javadocDirs.add(javadocDir);
  }
}

StringBuffer indexContents = null;

List<String> keys = new ArrayList(javadocMap.keySet());
Collections.sort(keys);
for ( String key : keys) {
  
  List<String> javadocDirs = javadocMap.get(key);
  String[] tokens = key.split(",");
  String parentDir = tokens[0];
  String title = null;
  if (Array.getLength(tokens) == 2) {
    title = tokens[1];
  }
  
  boolean requiresIndex = false;
  
  if ( javadocDirs.size() == 1 ) {

    Path filePath = FileSystems.getDefault().getPath( JAVADOC_SRC_DIR + "/" + javadocDirs.get(0) ).toAbsolutePath();
    ZipFile.addToArchive( zaos, filePath.toFile(), parentDir );

  } else {
    requiresIndex = true;
    Iterator iterator = javadocDirs.iterator();
    while (iterator.hasNext()) {
      String javadocDir = iterator.next();
      Path filePath = null;
      filePath = FileSystems.getDefault().getPath( JAVADOC_SRC_DIR + "/" + javadocDir ).toAbsolutePath();
      ZipFile.addToArchive( zaos, filePath.toFile(), parentDir + "/" + javadocDir );
    }
  }
  
  if (requiresIndex) {

    // look back to figure out if this is not a continuation
    if ( keys.indexOf(key) != 0 ) {
      // not the first item in list, so lookback possible ...
      String previousKey = keys.get( keys.indexOf(key) - 1 );
      String[] previousTokens = previousKey.split(",");
      String previousParentDir = previousTokens[0];
      
      boolean continuation = false;
      if ( parentDir.contains("/") && previousParentDir.contains("/") ) {
        String[] parentDirTokens = parentDir.split("/");
        String[] previousParentDirTokens = previousParentDir.split("/");
        String parentDirBase = parentDirTokens[0];
        String previousParentDirBase = previousParentDirTokens[0];
        if ( parentDirBase.equals( previousParentDirBase ) ) {
          continuation = true;
        }
      }
      
      if (!continuation) {
        indexContents = new StringBuffer();
        indexContents.append("<html><body>\n");
      }
      
    } else {
      indexContents = new StringBuffer();
      indexContents.append("<html><body>\n");
    }
    
    if (title != null) {
      indexContents.append("<h1>" + title + "</h1>\n");
    }
    
    if ( parentDir.contains("/") ) {
      String[] parentDirTokens = parentDir.split("/");
      String parentDirBase = parentDirTokens[0];
      String afterParentDirBase = parentDir.substring( parentDir.indexOf("/") + 1, parentDir.length() );
      
      Iterator iterator = javadocDirs.iterator();
      while (iterator.hasNext()) {
        String javadocDir = iterator.next();
        indexContents.append("<a href=\"" + afterParentDirBase + "/" + javadocDir + "/index.html\">" + javadocDir + "</a><br>\n");
      }
    
    } else {
    
      Iterator iterator = javadocDirs.iterator();
      while (iterator.hasNext()) {
        String javadocDir = iterator.next();
        indexContents.append("<a href=\"" + javadocDir + "/index.html\">" + javadocDir + "</a><br>\n");
      }
    
    }
    
    // look ahead to see if there is not a multi-list
    if ( keys.indexOf(key) < keys.size() - 1 ) {
      // we have at least one more key, so ...
      String nextKey = keys.get( keys.indexOf(key) + 1 );
      String[] nextTokens = nextKey.split(",");
      String nextParentDir = nextTokens[0];
      
      boolean continuation = false;
      if ( parentDir.contains("/") && nextParentDir.contains("/") ) {
        String[] parentDirTokens = parentDir.split("/");
        String[] nextParentDirTokens = nextParentDir.split("/");
        String parentDirBase = parentDirTokens[0];
        String nextParentDirBase = nextParentDirTokens[0];
        if ( parentDirBase.equals( nextParentDirBase ) ) {
          continuation = true;
        }
      }
      
      if (!continuation) {
        indexContents.append("</body></html>\n");
        if ( parentDir.contains("/") ) {
          String[] parentDirTokens = parentDir.split("/");
          String parentDirBase = parentDirTokens[0];
          
          System.out.println( "GENERATING " + parentDirBase + "/index.html ..." );
          System.out.println( indexContents.toString() );
          zaos.putArchiveEntry( new ZipArchiveEntry( parentDirBase + "/index.html" ) );
          BufferedInputStream bis = new BufferedInputStream( new StringBufferInputStream( indexContents.toString() ) );
          IOUtils.copy( bis, zaos );
          zaos.closeArchiveEntry();
          bis.close();
          
        } else {
          
          System.out.println( "GENERATING " + parentDir + "/index.html ..." );
          System.out.println( indexContents.toString() );
          zaos.putArchiveEntry( new ZipArchiveEntry( parentDir + "/index.html" ) );
          BufferedInputStream bis = new BufferedInputStream( new StringBufferInputStream( indexContents.toString() ) );
          IOUtils.copy( bis, zaos );
          zaos.closeArchiveEntry();
          bis.close();
          
        }
      }
      
    } else {
      // this must be the last key
      indexContents.append("</body></html>\n");
      
      System.out.println( "GENERATING " + parentDir + "/index.html ..." );
      System.out.println( indexContents.toString() );
      zaos.putArchiveEntry( new ZipArchiveEntry( parentDir + "/index.html" ) );
      BufferedInputStream bis = new BufferedInputStream( new StringBufferInputStream( indexContents.toString() ) );
      IOUtils.copy( bis, zaos );
      zaos.closeArchiveEntry();
      bis.close();
      
    }
    
  }

  
}

zaos.close();
fos.close();
