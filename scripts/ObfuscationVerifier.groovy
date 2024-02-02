@GrabResolver(name='pentaho', root='https://one.hitachivantara.com/artifactory/pnt-mvn', m2Compatible='true')
@Grab(group='org.bitbucket.mstrobel', module='procyon-compilertools', version='0.5.32')
import com.strobel.decompiler.Decompiler
import com.strobel.decompiler.DecompilerSettings
import com.strobel.decompiler.PlainTextOutput

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.zip.ZipFile
import java.util.zip.ZipEntry
import java.util.PropertyResourceBundle
import java.util.regex.Matcher
import java.util.regex.Pattern

final DEPLOYMENT_FOLDER   = System.getProperty( "DEPLOYMENT_FOLDER" )
final BUILD_HOSTING_ROOT      = System.getProperty( "BUILD_HOSTING_ROOT" )
final RELEASE_BUILD_NUMBER    = System.getProperty( "RELEASE_BUILD_NUMBER" )

println "BUILD_HOSTING_ROOT   : ${BUILD_HOSTING_ROOT}"
println "DEPLOYMENT_FOLDER    : ${DEPLOYMENT_FOLDER}"
println "RELEASE_BUILD_NUMBER : ${RELEASE_BUILD_NUMBER}"

def warnings = []
def errors = []
def assemblyZipFileNames = []
def obfuscatedJarFileNamePatterns = []
def prohibitedStrings = []

// load assemblyZipFileNames
FileReader assembliesFileReader = new FileReader( new File( "scripts/obfuscation-ee-zip-files.txt" ) )
BufferedReader assembliesBufferedReader = new BufferedReader( assembliesFileReader )
def assemblyZipFileName
while ( ( assemblyZipFileName = assembliesBufferedReader.readLine() ) != null ) {
  if ( assemblyZipFileName.length() > 0 ) {
    assemblyZipFileNames.add( assemblyZipFileName )
  }
}

// load obfuscatedJarFileNamePatterns
FileReader patternsFileReader = new FileReader( new File( "scripts/obfuscation-ee-jar-patterns.txt" ) )
BufferedReader patternsBufferedReader = new BufferedReader( patternsFileReader )
def pattern
while ( ( pattern = patternsBufferedReader.readLine() ) != null ) {
  if ( pattern.length() > 0 ) {
    obfuscatedJarFileNamePatterns.add( pattern )
  }
}

// load prohibitedStrings
FileReader prohibitedStringsFileReader = new FileReader( new File( "scripts/obfuscation-ee-prohibited-strings.txt" ) )
BufferedReader prohibitedStringsBufferedReader = new BufferedReader( prohibitedStringsFileReader )
def prohibitedString
while ( ( prohibitedString = prohibitedStringsBufferedReader.readLine() ) != null ) {
  if ( prohibitedString.length() > 0 ) {
    prohibitedStrings.add( prohibitedString )
  }
}

// load version.properties
PropertyResourceBundle versionProperties = new PropertyResourceBundle( new FileReader( "resources/config/suite-release.versions" ) )


class ViolationChecker {

  def JARS_DIR = "jars"

  static BANNED_EXPORT_FILE_PATTERN  = System.getProperty( "BANNED_EXPORT_FILE_PATTERN" )

  private def warnings
  private def assemblyZipFileNames
  private def obfuscatedJarFileNamePatterns

  ViolationChecker(  def warnings,
                     def assemblyZipFileNames,
                     def obfuscatedJarFileNamePatterns ) {
    this.warnings = warnings
    this.assemblyZipFileNames = assemblyZipFileNames
    this.obfuscatedJarFileNamePatterns = obfuscatedJarFileNamePatterns
  }

  void scan( ZipFile zipFile, String parentZipPath ) {

    def zipFileFileName = zipFile.getName().substring(zipFile.getName().lastIndexOf("/") + 1, zipFile.getName().length())

    println "------------------------------------------------------------------------------------------------"
    if ( parentZipPath.length() > 0 ) {
      println "| scanning ${parentZipPath} - ${zipFileFileName}"
    } else {
      println "| scanning ${zipFileFileName}"
    }
    println "------------------------------------------------------------------------------------------------"

    Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries()
    while( zipFileEntries.hasMoreElements() ) {
      ZipEntry zipEntry = (ZipEntry) zipFileEntries.nextElement()
      String zipEntryFileName = zipEntry.getName()
      if (zipEntry.getName().contains("/")) {
        zipEntryFileName = zipEntry.getName().substring( zipEntry.getName().lastIndexOf("/") + 1, zipEntry.getName().length() )
      }

      // check for inclusion of obfuscated jars with obf still in the name
      if ( zipEntryFileName.endsWith("obf.jar") ) {
        warnings.add( "WARNING: " + zipFile.getName() + " contains " + zipEntry.getName() )
      }

      if ( zipEntryFileName.matches( BANNED_EXPORT_FILE_PATTERN.replace( ".", "\\." ).replace( "*", ".*" ) ) ) {
        warnings.add( "WARNING: " + zipFile.getName() + " contains " + zipEntry.getName() + " which matches the banned export file pattern " + BANNED_EXPORT_FILE_PATTERN )
      }

      // look for obfuscated jars within the zip
      for ( String pattern : this.obfuscatedJarFileNamePatterns ) {
        if ( Pattern.matches( pattern, zipEntryFileName ) ) {
          println "\"" + pattern + "\"" + " matches " + zipEntry.getName()

          // unpack the file
          String outputDirString = JARS_DIR + "/"
          if ( parentZipPath.length() > 0 ) {
            outputDirString = outputDirString + parentZipPath + "-" + zipFileFileName
          } else {
            outputDirString = outputDirString + zipFileFileName
          }

          if ( zipEntry.getName().contains("/") ) {
            outputDirString = outputDirString + "/" + zipEntry.getName().substring( 0, zipEntry.getName().lastIndexOf("/") )
          }

          InputStream inputStream = zipFile.getInputStream( zipEntry )
          unpack( inputStream, outputDirString, zipEntryFileName )

        }
      }


      // look for embedded zips and kars to unpack and scan, then recurse
      if ( zipEntryFileName.endsWith( ".zip" ) || zipEntryFileName.endsWith( ".kar" ) ) {

        String outputDirString
        if ( parentZipPath.length() > 0 ) {
          outputDirString = parentZipPath + "-" + zipFileFileName
        } else {
          outputDirString = zipFileFileName
        }

        InputStream inputStream = zipFile.getInputStream( zipEntry )
        unpack( inputStream, JARS_DIR + "/" + outputDirString , zipEntryFileName )

        ZipFile innerZipFile = new ZipFile( new File( JARS_DIR + "/" + outputDirString + "/" + zipEntryFileName ) )

        scan( innerZipFile, outputDirString )
      }

    }

  }


  static void unpack( InputStream inputStream, String outputDirectory, String outputFileName ) {

    File outputDir = new File( outputDirectory )
    if ( ! outputDir.exists() ) {
      //println "making dir " + outputDirectory )
      outputDir.mkdirs()
    }

    BufferedInputStream bufferedInputStream = new BufferedInputStream( inputStream )
    FileOutputStream fileOutputStream = new FileOutputStream( outputDirectory + "/" + outputFileName )
    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream( fileOutputStream )

    //println "unpacking " + outputFileName + " to " + outputDirectory )
    byte[] buffer = new byte[2048]
    int len
    while ( ( len = bufferedInputStream.read( buffer ) ) > 0 ) {
      bufferedOutputStream.write( buffer, 0, len )
    }
    bufferedInputStream.close()
    bufferedOutputStream.close()

  }

}



// look for jars in the jars dir and unpack class files
class JarFileVisitor extends SimpleFileVisitor<Path> {

  static String BUILD_URL = System.getProperty( "BUILD_URL" )

  static int jarFileCount = 0
  static int classFileCount = 0

  private def warnings

  JarFileVisitor( def warnings ) {
    this.warnings = warnings
  }

  @Override
  FileVisitResult visitFile(Path filePath, BasicFileAttributes fileAttrs) {

    if ( filePath.toString().endsWith(".jar") ) {
      jarFileCount++
      boolean hasAClass = false

      //println filePath.toString() )
      ZipFile jarZipFile = new ZipFile( new File( filePath.toString() ) )

      Enumeration<? extends ZipEntry> zipFileEntries = jarZipFile.entries()
      while( zipFileEntries.hasMoreElements() ) {
        ZipEntry zipEntry = (ZipEntry) zipFileEntries.nextElement()
        String zipEntryFileName = zipEntry.getName()

        String outputDirectory = jarZipFile.getName().replace( "jars", "classes" ) + "/"
        if (zipEntry.getName().contains("/")) {
          zipEntryFileName = zipEntry.getName().substring( zipEntry.getName().lastIndexOf("/") + 1, zipEntry.getName().length() )
          outputDirectory = outputDirectory + zipEntry.getName().substring( 0, zipEntry.getName().lastIndexOf("/") )
        } else {
          outputDirectory = outputDirectory + zipEntryFileName
        }

        if ( zipEntryFileName.endsWith(".class") ) {
          classFileCount++

          // test to see if it seems like this jar has been obfuscated
          if ( zipEntryFileName.equals( "a.class" ) ) {
            hasAClass = true
          }

          File outputDir = new File( outputDirectory )
          if ( ! outputDir.exists() ) {
            //println "mkdir " + outputDirectory )
            outputDir.mkdirs()
          }

          BufferedInputStream bufferedInputStream = new BufferedInputStream( jarZipFile.getInputStream( zipEntry ) )
          FileOutputStream fileOutputStream = new FileOutputStream( outputDirectory + "/" + zipEntryFileName )
          BufferedOutputStream bufferedOutputStream = new BufferedOutputStream( fileOutputStream )

          //println "unpacking " + zipEntryFileName + " to " + outputDirectory )
          byte[] buffer = new byte[2048]
          int len
          while ( ( len = bufferedInputStream.read( buffer ) ) > 0 ) {
            bufferedOutputStream.write( buffer, 0, len )
          }
          bufferedInputStream.close()
          bufferedOutputStream.close()

        }

      }

      if ( ! hasAClass ) {
        String url = BUILD_URL.substring( 0, BUILD_URL.lastIndexOf( "/", BUILD_URL.length() - 2 ) ) + "/ws/" + filePath.toString().replace( "jars", "java" )
        warnings.add( "WARNING: ${filePath} MAY NOT BE OBFUSCATED!  See: ${url}" )
      }

    }

    return FileVisitResult.CONTINUE
  }

}




// look for class files in the classes dir and decompile them
class ClassFileVisitor extends SimpleFileVisitor<Path> {

  static int successfulDecompiles = 0
  static int unsuccessfulDecompiles = 0

  private def warnings

  ClassFileVisitor(def warnings) {
    this.warnings = warnings
  }

  @Override
  FileVisitResult visitFile(Path filePath, BasicFileAttributes fileAttrs) {

    if ( filePath.toString().endsWith(".class") ) {

      String inputDirectory = filePath.toString().substring( 0, filePath.toString().lastIndexOf("/") )
      String outputDirectory = inputDirectory.replace( "classes", "java" )
      File outputDir = new File( outputDirectory )
      if ( ! outputDir.exists() ) {
        //println "mkdir " + outputDirectory )
        outputDir.mkdirs()
      }

      String classType = filePath.toString().substring( filePath.toString().lastIndexOf("/") + 1, filePath.toString().length() )
      classType = classType.substring( 0, classType.length() - 6 )

      //println "decompiling " + classType + ".class to " + outputDirectory + "/" + classType + ".java" )
      FileOutputStream stream = new FileOutputStream( outputDirectory + "/" + classType + ".java" )
      OutputStreamWriter writer = new OutputStreamWriter(stream)
      try {
        def classFile = inputDirectory + "/" + classType + ".class"
        //println "Decompiling ${classFile}"
        Decompiler.decompile( classFile,
                              new PlainTextOutput( writer ),
                              DecompilerSettings.javaDefaults() )
        successfulDecompiles++
      } catch (Exception e) {
        warnings.add( "WARNING: UNABLE TO DECOMPILE ${inputDirectory}/${classType}.class - " + e.getMessage() )
        unsuccessfulDecompiles++
      }
      writer.close()
      stream.close()
    }

    return FileVisitResult.CONTINUE
  }

}




// look for java files in the java dir and search them for pattern violations
class JavaFileVisitor extends SimpleFileVisitor<Path> {

  private def errors
  private def prohibitedStrings

  JavaFileVisitor( def errors, def prohibitedStrings ) {
    this.errors = errors
    this.prohibitedStrings = prohibitedStrings
  }

  @Override
  FileVisitResult visitFile(Path filePath, BasicFileAttributes fileAttrs) {

    if ( filePath.toString().endsWith(".java") ) {
      //println "scanning " + filePath.toString() )
      BufferedReader bufferedReader = new BufferedReader( new FileReader( filePath.toString() ) )
      String line
      while ( ( line = bufferedReader.readLine() ) != null ) {
        for ( prohibitedString in prohibitedStrings ) {
          if ( line.contains( prohibitedString ) ) {
            errors.add( "ERROR: " + filePath.toString() + " CONTAINS LINE \"" + line + "\" WITH PROHIBITED STRING \"" + prohibitedString + "\"" )
          }
        }
      }

    }

    return FileVisitResult.CONTINUE
  }

}





// scan each of the assemblies mentioned in the manifest for pattern matched jars and unpack them
println "------------------------------------------------------------------------------------------------"
println "| STEP 1)  UNPACK JAR FILES FOUND IN ASSEMBLIES RECURSIVELY INTO jars DIR"
println "------------------------------------------------------------------------------------------------"
for ( fileName in assemblyZipFileNames ) {

  Matcher matcher = Pattern.compile( "(@(.*)@)" ).matcher( fileName )
  matcher.find()
  if ( fileName.contains("@") ) {
    fileName = fileName.replace( matcher.group(1), versionProperties.getString( matcher.group(2) ) )
  }
  fileName = fileName.replace( "BUILDTAG", RELEASE_BUILD_NUMBER )
  println "searching for " + BUILD_HOSTING_ROOT + "/" + DEPLOYMENT_FOLDER + "/" + RELEASE_BUILD_NUMBER + "/" + fileName

  File file = new File( BUILD_HOSTING_ROOT + "/" + DEPLOYMENT_FOLDER + "/" + RELEASE_BUILD_NUMBER + "/" + fileName )
  if ( file.exists() ) {
    ZipFile zipFile = new ZipFile(file)
    ViolationChecker violationChecker = new ViolationChecker( warnings, assemblyZipFileNames, obfuscatedJarFileNamePatterns )
    violationChecker.scan( zipFile, "" )
  } else {
    def error = "ERROR: ${BUILD_HOSTING_ROOT}/${DEPLOYMENT_FOLDER}/${RELEASE_BUILD_NUMBER}/${fileName} NOT FOUND!"
    println error
    errors.add( error )
  }
}




// unzip the jars into classes
println "------------------------------------------------------------------------------------------------"
println "| STEP 2)  UNPACK .class FILES FOUND IN .jar FILES INTO classes DIR"
println "------------------------------------------------------------------------------------------------"
println "unpacking class files ..."
JarFileVisitor jarFileVisitor = new JarFileVisitor( warnings )
Files.walkFileTree( FileSystems.getDefault().getPath( "jars" ), jarFileVisitor )
println JarFileVisitor.classFileCount + " class files found in " + JarFileVisitor.jarFileCount + " jar files"

// decompile the classes to java
println "------------------------------------------------------------------------------------------------"
println "| STEP 3)  DECOMPILE .class FILES into .java FILES INTO java DIR"
println "------------------------------------------------------------------------------------------------"
println "decompiling class files (~30 mins) ..."
ClassFileVisitor classFileVisitor = new ClassFileVisitor( warnings )
Files.walkFileTree( FileSystems.getDefault().getPath( "classes" ), classFileVisitor )
println ClassFileVisitor.successfulDecompiles + " successful decompiles"
println ClassFileVisitor.unsuccessfulDecompiles + " unsuccessful decompiles"


// scan the java for prohibited strings
println "------------------------------------------------------------------------------------------------"
println "| STEP 4)  SEARCH .java FILES FOR PATTERN VIOLATIONS"
println "------------------------------------------------------------------------------------------------"
println "searching java files ..."
JavaFileVisitor javaFileVisitor = new JavaFileVisitor( errors, prohibitedStrings )
Files.walkFileTree( FileSystems.getDefault().getPath( "java" ), javaFileVisitor )


// print out any warnings
for ( String warning : warnings ) {
  println "------------------------------------------------------------------------------------------------"
  println "| ${warning}"
  println "------------------------------------------------------------------------------------------------"
}


// print out any errors
for ( String error : errors ) {
  println "------------------------------------------------------------------------------------------------"
  println "| ${error}"
  println "------------------------------------------------------------------------------------------------"
}


// fail the job if errors were found
if ( errors.size() > 0 ) {
  throw new RuntimeException( "ERRORS FOUND!" )
}
