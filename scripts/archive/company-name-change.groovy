/*
 *
 * set +e
 * rm -rf github
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
 *   -DGITHUB_FEATURE_BRANCH=ENGOPS-3650 \
 *   -DGITHUB_FORCE_PUSH=TRUE \
 *   -DCOMMIT_MESSAGE="[ENGOPS-3650] change Pentaho to Hitachi Vantara and update copyright date" \
 *   -DPROP_FILE=scripts/groovy/company-name-change.properties \
 *   -DPR_LOG_FILE=company-name-pull-requests.log \
 *   scripts/groovy/company-name-change.groovy
 *
 *
*/

@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.21')

@Grab(group='commons-lang', module='commons-lang', version='2.6')
import org.apache.commons.lang.StringEscapeUtils;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

import groovy.util.logging.Slf4j;


String COMMAND_MAKE_COMMITS        = System.getProperty("COMMAND_MAKE_COMMITS");
String COMMAND_PUSH_COMMITS        = System.getProperty("COMMAND_PUSH_COMMITS");
String COMMAND_MAKE_PULL_REQUESTS  = System.getProperty("COMMAND_MAKE_PULL_REQUESTS");
String COMMAND_MERGE_PULL_REQUESTS = System.getProperty("COMMAND_MERGE_PULL_REQUESTS");
String PARENT_TARGET_CLONE_DIR     = System.getProperty("PARENT_TARGET_CLONE_DIR");
String GITHUB_FORK                 = System.getProperty("GITHUB_FORK");
String GITHUB_FEATURE_BRANCH       = System.getProperty("GITHUB_FEATURE_BRANCH");
String COMMIT_MESSAGE              = System.getProperty("COMMIT_MESSAGE");
String PR_LOG_FILE                 = System.getProperty("PR_LOG_FILE");

String PR_MESSAGE                  = "WARNING!  FOR MERGE BY PROCESS ONLY PLEASE!";

boolean GITHUB_FORCE_PUSH = false;
if ( System.getProperty("GITHUB_FORCE_PUSH").equals( "TRUE" ) ) {
  GITHUB_FORCE_PUSH = true;
}

@Slf4j
public class SourceFileVisitor extends SimpleFileVisitor<Path> {

  String PROP_FILE = System.getProperty( "PROP_FILE" );
  
  static final int NAME_REG_EX_GROUP_NUM           = 5;
  static final int NAME_TO_EOL_REG_EX_GROUP_NUM    = 6;
  static final int COPYRIGHT_UPPER_DATE_GROUP_NUM  = 4;
  static final String CURRENT_YEAR                 = "2017";
  static boolean propsLoaded = false;
  static String nameSearchRegExPrefix = "^(\\s)*(\\/\\*|\\/\\*\\*|\\*|\\*\\*|\\* \\*|<%--|~|#|REM|//)\\s*[^@.]*(( )(";
  static String nameSearchRegExSuffix = "))[^A-Z/'_](.*)";
  static String copyrightRegEx        = "(?i)Copyright\\s*((?i)\\(c\\)\\s*)?((\\d{4})\\s*-\\s*)?(\\d{4})(,)?\\s*(by\\s*)?((?i)Webdetails, a\\s*)?(?i)Hitachi";
  static HashMap<String,String> namesMap = new HashMap<String,String>();
  static List<String> nameNegationList = new ArrayList<String>();
  
  static int totalFilesChecked = 0;
  static int totalFilesChanged = 0;

  public SourceFileVisitor() {
    if (!propsLoaded) {
      log.info( "loading properties file ..." );
      InputStream propInputStream = this.getClass().getResourceAsStream( PROP_FILE );
      BufferedReader reader = new BufferedReader( new InputStreamReader( propInputStream ) );
      String line = "";
      while ((line = reader.readLine()) != null) {
        String[] namePair = line.split( "=" );
        String propertyName = namePair[0];
        String propertyValue = namePair[1];
        log.info( "registering translation '" + propertyName + "' => '" + propertyValue + "'" );
        namesMap.put( propertyName, propertyValue );
      }
      reader.close();
      propsLoaded = true;
    }
    if ( nameNegationList.size() == 0 ) {
      nameNegationList.add( "3.5" );
      nameNegationList.add( "Data" );
      nameNegationList.add( "Server" );
      nameNegationList.add( "server" );
      nameNegationList.add( "Platform" );
      nameNegationList.add( "platform" );
      nameNegationList.add( "Analyzer" );
      nameNegationList.add( "BI" );
      nameNegationList.add( "DI" );
      nameNegationList.add( "Interactive" );
      nameNegationList.add( "CCC" );
      nameNegationList.add( "Plugin" );
      nameNegationList.add( "plugin" );
      nameNegationList.add( "Reporting" );
      nameNegationList.add( "reporting" );
      nameNegationList.add( "Type" );
      nameNegationList.add( "type" );
      nameNegationList.add( "Aggregation" );
      nameNegationList.add( "object" );
      nameNegationList.add( "namespace" );
      nameNegationList.add( "Metadata" );
      nameNegationList.add( "metadata" );
      nameNegationList.add( "XML" );
      nameNegationList.add( "metastore" );
      nameNegationList.add( "Manifest" );
      nameNegationList.add( "MapReduce" );
      nameNegationList.add( "license manager" );
      nameNegationList.add( "result" );
      nameNegationList.add( "Deployer" );
      nameNegationList.add( "for Hadoop" );
      nameNegationList.add( "Administration console" );
    }
  }

  @Override
  public FileVisitResult visitFile(Path filePath, BasicFileAttributes fileAttrs) {

    if (  filePath.toString().endsWith("subfloor.xml") ) {
      totalFilesChecked++;
      log.info( filePath.toString() );
      log.info( "totalFilesChecked: " + totalFilesChecked );
      
      String fileString = new String( Files.readAllBytes( filePath ) );
      int startOfVendorElement = fileString.indexOf( "name=\"impl.vendor\" value=\"Pentaho Corporation\"" );
      if ( startOfVendorElement != -1 ) {
        StringBuilder stringBuilder = new StringBuilder( fileString );
        stringBuilder = stringBuilder.replace( startOfVendorElement + 26, startOfVendorElement + 45, "Hitachi Vantara" );
        BufferedWriter writer = new BufferedWriter( new FileWriter( filePath.toFile() ) );
        writer.write( stringBuilder.toString() );
        writer.close();
        totalFilesChanged++;
        log.info( "totalFilesChanged: " + totalFilesChanged );
      }
    }
    

    if (  filePath.toString().endsWith(".properties") ) {
      totalFilesChecked++;
      log.info( filePath.toString() );
      log.info( "totalFilesChecked: " + totalFilesChecked );

      
      // properties files may contain non-UTF-8 chars, so need to process byte-by-byte
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

          int startOfMatchedString = line.indexOf( "about_copyright=Copyright (c) 2008-{0} Pentaho Corporation" );
          if ( startOfMatchedString != -1 ) {
            log.info( "found: about_copyright=Copyright (c) 2008-{0} Pentaho Corporation" );
            StringBuilder stringBuilder = new StringBuilder( line );
            stringBuilder = stringBuilder.replace( startOfMatchedString + 39, startOfMatchedString + 58, "Hitachi Vantara" );
            line = stringBuilder.toString();
            lineChanged = true;
            fileChanged = true;
          }
          startOfMatchedString = line.indexOf( "impl.vendor=Pentaho Corporation" );
          if ( startOfMatchedString != -1 ) {
            log.info( "found: impl.vendor=Pentaho Corporation" );
            StringBuilder stringBuilder = new StringBuilder( line );
            stringBuilder = stringBuilder.replace( startOfMatchedString + 12, startOfMatchedString + 31, "Hitachi Vantara" );
            line = stringBuilder.toString();
            lineChanged = true;
            fileChanged = true;
          }
          startOfMatchedString = line.indexOf( "UI.USER_PRO_ABOUT_TEXT={0} Pentaho Corporation" );
          if ( startOfMatchedString != -1 ) {
            log.info( "found: UI.USER_PRO_ABOUT_TEXT={0} Pentaho Corporation" );
            StringBuilder stringBuilder = new StringBuilder( line );
            stringBuilder = stringBuilder.replace( startOfMatchedString + 27, startOfMatchedString + 46, "Hitachi Vantara" );
            line = stringBuilder.toString();
            lineChanged = true;
            fileChanged = true;
          }
          startOfMatchedString = line.indexOf( "UI.USER_ABOUT_TEXT={0} Pentaho Corporation" );
          if ( startOfMatchedString != -1 ) {
            log.info( "found: UI.USER_ABOUT_TEXT={0} Pentaho Corporation" );
            StringBuilder stringBuilder = new StringBuilder( line );
            stringBuilder = stringBuilder.replace( startOfMatchedString + 23, startOfMatchedString + 42, "Hitachi Vantara" );
            line = stringBuilder.toString();
            lineChanged = true;
            fileChanged = true;
          }
          startOfMatchedString = line.indexOf( "UI.PUC.LOGIN.COPYRIGHT=&copy; 2005-{0} Pentaho Corporation" );
          if ( startOfMatchedString != -1 ) {
            log.info( "found: UI.PUC.LOGIN.COPYRIGHT=&copy; 2005-{0} Pentaho Corporation" );
            StringBuilder stringBuilder = new StringBuilder( line );
            stringBuilder = stringBuilder.replace( startOfMatchedString + 39, startOfMatchedString + 58, "Hitachi Vantara" );
            line = stringBuilder.toString();
            lineChanged = true;
            fileChanged = true;
          }
          startOfMatchedString = line.indexOf( "MetaEditor.USER_HELP_PENTAHO_CORPORATION=(c) 2006-2010 Pentaho Corporation" );
          if ( startOfMatchedString != -1 ) {
            log.info( "found: MetaEditor.USER_HELP_PENTAHO_CORPORATION=(c) 2006-2010 Pentaho Corporation" );
            StringBuilder stringBuilder = new StringBuilder( line );
            stringBuilder = stringBuilder.replace( startOfMatchedString + 50, startOfMatchedString + 74, "2017 Hitachi Vantara" );
            line = stringBuilder.toString();
            lineChanged = true;
            fileChanged = true;
          }
          startOfMatchedString = line.indexOf( "MetaEditor.USER_HELP_PENTAHO_CORPORATION=(c) 2006-{0} Pentaho Corporation" );
          if ( startOfMatchedString != -1 ) {
            log.info( "found: MetaEditor.USER_HELP_PENTAHO_CORPORATION=(c) 2006-{0} Pentaho Corporation" );
            StringBuilder stringBuilder = new StringBuilder( line );
            stringBuilder = stringBuilder.replace( startOfMatchedString + 54, startOfMatchedString + 73, "Hitachi Vantara" );
            line = stringBuilder.toString();
            lineChanged = true;
            fileChanged = true;
          }
          startOfMatchedString = line.indexOf( "MetaEditor.USER_HELP_PENTAHO_COPYRIGHT=Copyright 2006-2010 Pentaho Corporation" );
          if ( startOfMatchedString != -1 ) {
            log.info( "found: MetaEditor.USER_HELP_PENTAHO_COPYRIGHT=Copyright 2006-2010 Pentaho Corporation" );
            StringBuilder stringBuilder = new StringBuilder( line );
            stringBuilder = stringBuilder.replace( startOfMatchedString + 54, startOfMatchedString + 79, "2017 Hitachi Vantara" );
            line = stringBuilder.toString();
            lineChanged = true;
            fileChanged = true;
          }
          startOfMatchedString = line.indexOf( "MetaEditor.USER_HELP_PENTAHO_COPYRIGHT=Copyright 2006-{0} Pentaho Corporation" );
          if ( startOfMatchedString != -1 ) {
            log.info( "found: MetaEditor.USER_HELP_PENTAHO_COPYRIGHT=Copyright 2006-{0} Pentaho Corporation" );
            StringBuilder stringBuilder = new StringBuilder( line );
            stringBuilder = stringBuilder.replace( startOfMatchedString + 58, startOfMatchedString + 77, "Hitachi Vantara" );
            line = stringBuilder.toString();
            lineChanged = true;
            fileChanged = true;
          }
          startOfMatchedString = line.indexOf( "System.CompanyInfo=(c) 2001-{0} Pentaho Corporation" );
          if ( startOfMatchedString != -1 ) {
            log.info( "found: System.CompanyInfo=(c) 2001-{0} Pentaho Corporation" );
            StringBuilder stringBuilder = new StringBuilder( line );
            stringBuilder = stringBuilder.replace( startOfMatchedString + 32, startOfMatchedString + 51, "Hitachi Vantara" );
            line = stringBuilder.toString();
            lineChanged = true;
            fileChanged = true;
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
    
    
    if (  filePath.toString().endsWith(".xml") ) {
      totalFilesChecked++;
      log.info( filePath.toString() );
      log.info( "totalFilesChecked: " + totalFilesChecked );
      
      String fileString = new String( Files.readAllBytes( filePath ) );
      int startOfVendorElement = fileString.indexOf( "<vendor>Pentaho Corporation</vendor>" );
      if ( startOfVendorElement != -1 ) {
        log.info( "found: <vendor>Pentaho Corporation</vendor>" );
        StringBuilder stringBuilder = new StringBuilder( fileString );
        stringBuilder = stringBuilder.replace( startOfVendorElement + 8, startOfVendorElement + 27, "Hitachi Vantara" );
        BufferedWriter writer = new BufferedWriter( new FileWriter( filePath.toFile() ) );
        writer.write( stringBuilder.toString() );
        writer.close();
        totalFilesChanged++;
        log.info( "totalFilesChanged: " + totalFilesChanged );
      }
    }
    
    
    
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
          filePath.toString().endsWith("README.txt") ||
          filePath.toString().endsWith("pdi-daemon") ||
          filePath.toString().endsWith("LICENSE.txt") ||
          filePath.toString().endsWith("licenseHeader.txt") ||
          filePath.toString().endsWith("license.txt") ) {
          
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
            
            String oldRegExName = oldName;
            if ( oldName.contains( "(" ) ) {
              oldRegExName = oldName.replace( "(", "\\(" );
              oldRegExName = oldRegExName.replace( ")", "\\)" );
            }
            
            int nameGroupNum = NAME_REG_EX_GROUP_NUM;
            String regex = nameSearchRegExPrefix + oldRegExName + nameSearchRegExSuffix;
            if (  filePath.toString().endsWith("LICENSE.txt") ||
                  filePath.toString().endsWith("licenseHeader.txt") ||
                  filePath.toString().endsWith("license.txt") ) {
              regex = "(" + oldRegExName + ")";
              nameGroupNum = 1;
            }

            // replace names
            Matcher matcher = Pattern.compile(regex).matcher(line);
            boolean matchFound = matcher.find();
            if ( matchFound ) {
              String match = matcher.group(nameGroupNum);
              String matchSuffix = "";
              if ( nameGroupNum == NAME_REG_EX_GROUP_NUM ) {
                matchSuffix = matcher.group( NAME_TO_EOL_REG_EX_GROUP_NUM );
              }
              if ( match != null ) {
                boolean negatedNameFound = false;
                for ( String nameNegation : nameNegationList ) {
                  if ( matchSuffix.contains( nameNegation ) ) {
                    negatedNameFound = true;
                    break;
                  }
                }
                if ( !negatedNameFound ) {
                  log.info( "found " + match );
                  line = this.replaceName(line, regex, nameGroupNum, oldName, namesMap.get(oldName), 0);
                  lineChanged = true;
                  fileChanged = true;
                } else {
                  log.info( "match of '" + match + "' negated by subsequent presence of '" + matchSuffix + "'" );
                }
              }
            }
            
            // replace copyright date
            matcher = Pattern.compile(copyrightRegEx).matcher(line);
            matchFound = matcher.find();
            if ( matchFound &&
                 matcher.group(COPYRIGHT_UPPER_DATE_GROUP_NUM) != null &&
                 !matcher.group(COPYRIGHT_UPPER_DATE_GROUP_NUM).equals(CURRENT_YEAR) ) {
              log.info( "found " + matcher.group(COPYRIGHT_UPPER_DATE_GROUP_NUM) );
              line = this.replaceCopyrightDate( line, copyrightRegEx, COPYRIGHT_UPPER_DATE_GROUP_NUM, 0 );
              lineChanged = true;
              fileChanged = true;
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

    if ( filePath.toString().endsWith(".xml") ||
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
      
      String fileString = new String( Files.readAllBytes( filePath ) );
      int startOfFirstComment = fileString.indexOf( "<!--" );
      int endOfFirstComment = fileString.indexOf( "-->" );
      if ( startOfFirstComment != -1 && endOfFirstComment != -1 ) {
      
        StringBuilder stringBuilder = new StringBuilder( fileString );
        String commentSection = fileString.substring( startOfFirstComment, endOfFirstComment );
        String newCommentSection = commentSection;
        
        for ( String oldName : namesMap.keySet() ) {
              
          String oldRegExName = oldName;
          if ( oldName.contains( "(" ) ) {
            oldRegExName = oldName.replace( "(", "\\(" );
            oldRegExName = oldRegExName.replace( ")", "\\)" );
          }
              
          int nameGroupNum = 1;
          String regex = "(" + oldRegExName + ")[^A-Z/'_](.*)";
  
          // replace names
          Matcher matcher = Pattern.compile(regex).matcher(newCommentSection);
          boolean matchFound = matcher.find();
          if ( matchFound ) {
            String match = matcher.group(nameGroupNum);
            String matchSuffix = matcher.group( 2 );
              
            if ( match != null ) {
              boolean negatedNameFound = false;
              for ( String nameNegation : nameNegationList ) {
                if ( matchSuffix.contains( nameNegation ) ) {
                  negatedNameFound = true;
                  break;
                }
              }
              if ( !negatedNameFound ) {
                log.info( "found " + match );
                newCommentSection = this.replaceName(newCommentSection, regex, nameGroupNum, oldName, namesMap.get(oldName), 0);
              } else {
                log.info( "match of '" + match + "' negated by subsequent presence of '" + matchSuffix + "'" );
              }
            }
            
          }
              
          // replace copyright date
          matcher = Pattern.compile(copyrightRegEx).matcher(newCommentSection);
          matchFound = matcher.find();
          if ( matchFound &&
               matcher.group(COPYRIGHT_UPPER_DATE_GROUP_NUM) != null &&
               !matcher.group(COPYRIGHT_UPPER_DATE_GROUP_NUM).equals(CURRENT_YEAR) ) {
            log.info( "found " + matcher.group(COPYRIGHT_UPPER_DATE_GROUP_NUM) );
            newCommentSection = this.replaceCopyrightDate( newCommentSection, copyrightRegEx, COPYRIGHT_UPPER_DATE_GROUP_NUM, 0 );
          }
          
        }
        
        if ( !commentSection.equals( newCommentSection ) ) {
          stringBuilder = stringBuilder.replace( startOfFirstComment, endOfFirstComment, newCommentSection );
          BufferedWriter writer = new BufferedWriter( new FileWriter( filePath.toFile() ) );
          writer.write( stringBuilder.toString() );
          writer.close();
          totalFilesChanged++;
          log.info( "totalFilesChanged: " + totalFilesChanged );
        }
        
      }

    }
    
    
    return FileVisitResult.CONTINUE;
  }
  
  
  private String replaceName( String text, String regex, int groupNum, String oldName, String newName, int startPos ) {
    
    StringBuilder stringBuilder = new StringBuilder(text);

    Matcher matcher = Pattern.compile(regex).matcher(text);
    boolean matchFound = matcher.find(startPos); // we wouldn't be here if we didn't already have a match
    int matchStart = matcher.start(groupNum);
    int matchEnd = matcher.end(groupNum);
    String match = matcher.group(groupNum);
    log.info( "replacing " + oldName + " with " + newName );
    stringBuilder = stringBuilder.replace(matchStart, matchEnd, newName);
    String newText = stringBuilder.toString();

    // check for more matches in same text and recurse
    matcher = Pattern.compile(regex).matcher(newText);
    startPos = matchStart + newName.length();
    matchFound = matcher.find(startPos);
    if (matchFound && matcher.group(groupNum) != null) {
      log.info( "found " + matcher.group(groupNum) );
      newText = this.replaceName( newText, regex, groupNum, oldName, newName, startPos );
    }
    
    return newText;
  }
  
  
  private String replaceCopyrightDate( String line, String regex, int groupNum, int startPos ) {
  
    StringBuilder stringBuilder = new StringBuilder(line);

    Matcher matcher = Pattern.compile(regex).matcher(line);
    boolean matchFound = matcher.find(startPos); // we wouldn't be here if we didn't already have a match
    int matchStart = matcher.start(groupNum);
    int matchEnd = matcher.end(groupNum);
    String match = matcher.group(groupNum);
    if ( matcher.group(groupNum - 1) == null ) { // we are not a range, so create one ...
      log.info ( "replacing copyright " + match + " with " + match + " - " + CURRENT_YEAR );
      stringBuilder = stringBuilder.replace(matchStart, matchEnd, match + " - " + CURRENT_YEAR);
    } else {
      log.info ( "replacing copyright " + match + " with " + CURRENT_YEAR );
      stringBuilder = stringBuilder.replace(matchStart, matchEnd, CURRENT_YEAR);
    }
    String newLine = stringBuilder.toString();

    return newLine;
  }
  
}



/*
example of this full regex might be:  <nameSearchRegExPrefix>Pentaho<nameSearchRegExSuffix>
this will match either a comment that contains 'Pentaho' or a comment that does not contain it
if there is a match, then group 6 will contain 'Pentaho', if it does, then we replace it with
the name we want and start over.  If group 6 does not contain 'Pentaho' then we move on to
the next match.  The contents of namesMap contain the translations we intend on achieving
*/

File githubDir = new File(PARENT_TARGET_CLONE_DIR);
if (!githubDir.exists()) {
  githubDir.mkdir();
}

if ( !COMMAND_MERGE_PULL_REQUESTS.equals("TRUE") ) {


  List<GithubProject> githubProjects = GithubProject.parseGithubProjectsCsv();
  for (GithubProject githubProject : githubProjects ) {
    
    if ( !githubProject.getProjectType().equals("RELEASE") ) {
      continue;
    }
    
    boolean isPrivate = Github.isProjectPrivate( githubProject.getOrg(), githubProject.getName() );
    
    String localWorkingGit = PARENT_TARGET_CLONE_DIR + "/" + githubProject.getName() + "/.git";
    
    if ( COMMAND_MAKE_COMMITS.equals("TRUE") ) {
      LocalGit.clone( githubProject.getOrg(),
                    "git@github.com:" + githubProject.getOrg() + "/" + githubProject.getName() + ".git",
                    githubProject.getBranch(),
                    false,
                    new File(PARENT_TARGET_CLONE_DIR + "/" + githubProject.getName()) );
  
      LocalGit.addRemote( localWorkingGit, GITHUB_FORK, "git@github.com:" + GITHUB_FORK + "/" + githubProject.getName() + ".git" );
      LocalGit.createBranch( localWorkingGit, githubProject.getBranch(), GITHUB_FEATURE_BRANCH );
  
      SourceFileVisitor sourceFileVisitor = new SourceFileVisitor();
      Files.walkFileTree(FileSystems.getDefault().getPath(PARENT_TARGET_CLONE_DIR + "/" + githubProject.getName()), sourceFileVisitor);
      
      LocalGit.commit( localWorkingGit, COMMIT_MESSAGE );
    }
    
    if ( COMMAND_PUSH_COMMITS.equals("TRUE") ) {
      LocalGit.pushCommits( localWorkingGit, GITHUB_FORK, GITHUB_FORCE_PUSH );
    }
    
    if ( COMMAND_MAKE_PULL_REQUESTS.equals("TRUE") ) {

      String pullRequestNumber = Github.createPullRequest(  githubProject.getOrg(),
                                                            githubProject.getName(),
                                                            githubProject.getBranch(),
                                                            GITHUB_FORK + ":" + GITHUB_FEATURE_BRANCH,
                                                            COMMIT_MESSAGE,
                                                            PR_MESSAGE ) ;

      // save the PR out to a file
      if ( pullRequestNumber != null ) {
        System.out.println( "logging PR #" + pullRequestNumber + " for " + githubProject.getOrg() + "/" + githubProject.getName() );
        PrintWriter prLogWriter = new PrintWriter( new FileWriter( new File( PR_LOG_FILE ),true));
        prLogWriter.println( githubProject.getOrg() + "," + githubProject.getName() + "," + pullRequestNumber );
        prLogWriter.close();
      }
      
    }

  }
  
} else {  // COMMAND_MERGE_PULL_REQUESTS=TRUE
    
    // load PRs from file
    System.out.println( "loading PR(s) from file ..." );
    FileReader fileReader = new FileReader( PR_LOG_FILE );
    BufferedReader bufferedReader = new BufferedReader( fileReader );
    String line;
    while( (line=bufferedReader.readLine()) != null ) {
      String[] prTokens = line.split( "," );
      String org = prTokens[0];
      String project = prTokens[1];
      String pullRequestNumber = prTokens[2];
    
      String mergeTitle = "Merge pull request #" + pullRequestNumber + " from " + GITHUB_FORK + "/" + GITHUB_FEATURE_BRANCH;
      String requiredHeadSHA = Github.getHeadSHA( GITHUB_FORK, project, GITHUB_FEATURE_BRANCH );
      Github.mergePullRequest(  org,
                                project,
                                pullRequestNumber,
                                mergeTitle,
                                COMMIT_MESSAGE,
                                requiredHeadSHA );
    }
    fileReader.close();

}

  