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
 *   -DPARENT_TARGET_CLONE_DIR=github \
 *   scripts/groovy/license-header-check.groovy
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

String PARENT_TARGET_CLONE_DIR  = System.getProperty("PARENT_TARGET_CLONE_DIR");

@Slf4j
@groovy.transform.InheritConstructors
public class SourceFileVisitor extends SimpleFileVisitor<Path> {

  GithubProject githubProject;
  boolean isPrivate = false;
  
  static HashMap<String,String> srcLicenses = new HashMap<String,String>();

  static {
    srcLicenses.put( "vantara",      "HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL" );
    srcLicenses.put( "pentaho",      "PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL" );
    srcLicenses.put( "mpl_v1_1",     "http://www.mozilla.org/MPL/MPL-1.1.txt" );
    srcLicenses.put( "gpl_v1",       "http://www.gnu.org/licenses/gpl-1.0.html" );
    srcLicenses.put( "gpl_v2",       "http://www.gnu.org/licenses/gpl-2.0.html" );
    srcLicenses.put( "gpl_v3",       "http://www.gnu.org/licenses/gpl-3.0.html" );
    srcLicenses.put( "lgpl_v2_1",    "GNU Lesser General Public License, version 2.1" );
    srcLicenses.put( "lgpl_v3",      "http://www.gnu.org/licenses/lgpl-3.0.html" );
    srcLicenses.put( "eupl_v1_1",    "Licensed under the EUPL" );
    srcLicenses.put( "fdl_v1_3",     "GNU Free Documentation License" );
    srcLicenses.put( "agpl_v3",      "GNU Affero General Public License" );
    srcLicenses.put( "apache_v2",    "Apache License" );
    srcLicenses.put( "cddl_v1",      "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0" );
    srcLicenses.put( "bsd_3",        "endorse or promote products derived from this software" );
    srcLicenses.put( "bsd_2",        "Redistributions of source code must retain the above copyright notice" );
    srcLicenses.put( "epl_v1",       "Eclipse Distribution License" );
    srcLicenses.put( "epl_only_v1",  "Eclipse Public License" );
    srcLicenses.put( "mit",          "Permission is hereby granted" );
  }
  
  
  SourceFileVisitor( GithubProject githubProject, boolean isPrivate ) {
    this.githubProject = githubProject;
    this.isPrivate = isPrivate;
  }
  
  SourceFileVisitor() {}
  
  
  @Override
  public FileVisitResult visitFile(Path filePath, BasicFileAttributes fileAttrs) {
          
    if (  
      filePath.toString().toLowerCase().endsWith( ".xml" ) ||
      filePath.toString().toLowerCase().endsWith( ".xul" ) ||
      filePath.toString().toLowerCase().endsWith( ".ktr" ) ||
      filePath.toString().toLowerCase().endsWith( ".kjb" ) ||
      filePath.toString().toLowerCase().endsWith( ".xsd" ) ||
      filePath.toString().toLowerCase().endsWith( ".dtd" ) ||
      filePath.toString().toLowerCase().endsWith( ".xreportspec" ) ||
      filePath.toString().toLowerCase().endsWith( ".xaction" ) ||
      filePath.toString().toLowerCase().endsWith( ".java" ) ||
      filePath.toString().toLowerCase().endsWith( ".js" ) ||
      filePath.toString().toLowerCase().endsWith( ".jsp" ) ||
      filePath.toString().toLowerCase().endsWith( ".css" ) ||
      filePath.toString().toLowerCase().endsWith( ".jsdoc" ) ||
      filePath.toString().toLowerCase().endsWith( ".json" ) ||
      filePath.toString().toLowerCase().endsWith( ".html" ) ||
      filePath.toString().toLowerCase().endsWith( ".htm" ) ||
      filePath.toString().toLowerCase().endsWith( ".sh" ) ||
      filePath.toString().toLowerCase().endsWith( ".mib" ) ||
      filePath.toString().toLowerCase().endsWith( ".bat" ) ||
      filePath.toString().toLowerCase().endsWith( ".cfg" ) ) {
      
      int startOfProjectName = filePath.toString().indexOf( this.githubProject.getName() );
      String localFilePath = filePath.toString().substring( startOfProjectName + this.githubProject.getName().length() + 1 );
      
      String fileString = new String( Files.readAllBytes( filePath ) );
            
      String licensesFound = "";
      
      for ( String licenseKey : srcLicenses.keySet() ) {
        String licenseValue = srcLicenses.get( licenseKey );
        if ( fileString.contains( licenseValue ) ) {
          if ( licenseKey.equals( "bsd_2" ) && licensesFound.contains( "bsd_3" ) ) {
            // bsd_3 also contains the semantics for bsd_2
            continue;
          }
          if ( licenseKey.equals( "epl_only_v1" ) && licensesFound.contains( "epl_v1" ) ) {
            // epl_v1 also contains the semantics for epl_only_v1
            continue;
          }
          if ( licensesFound.length() == 0 ) {
            licensesFound = licenseKey;
          } else {
            licensesFound = licensesFound + "&" + licenseKey;
          }
        }
      }
      
      String githubURL = "https://github.com/" + this.githubProject.getOrg() + "/" + 
                          this.githubProject.getName() + "/blob/" + this.githubProject.getBranch() + "/" + localFilePath;
      log.info( this.githubProject.getName() + "," + licensesFound + "," + githubURL );
      if ( this.isPrivate ) {
        PrintWriter eeLicenseHeaderCsvWriter = new PrintWriter( new FileWriter( new File( "ee-license-headers.csv" ),true));
        eeLicenseHeaderCsvWriter.println( this.githubProject.getName() + "," + licensesFound + "," + githubURL );
        eeLicenseHeaderCsvWriter.close();
      } else {
        PrintWriter ceLicenseHeaderCsvWriter = new PrintWriter( new FileWriter( new File( "ce-license-headers.csv" ),true));
        ceLicenseHeaderCsvWriter.println( this.githubProject.getName() + "," + licensesFound + "," + githubURL );
        ceLicenseHeaderCsvWriter.close();
      }
    }

    return FileVisitResult.CONTINUE;
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
    
  boolean isPrivate = Github.isProjectPrivate( githubProject.getOrg(), githubProject.getName() );
    
  LocalGit.clone( githubProject.getOrg(),
                  "git@github.com:" + githubProject.getOrg() + "/" + githubProject.getName() + ".git",
                  githubProject.getBranch(),
                  false,
                  new File(PARENT_TARGET_CLONE_DIR + "/" + githubProject.getName()) );
      
  SourceFileVisitor sourceFileVisitor = new SourceFileVisitor( githubProject, isPrivate );
  Files.walkFileTree(FileSystems.getDefault().getPath(PARENT_TARGET_CLONE_DIR + "/" + githubProject.getName()), sourceFileVisitor);

}

