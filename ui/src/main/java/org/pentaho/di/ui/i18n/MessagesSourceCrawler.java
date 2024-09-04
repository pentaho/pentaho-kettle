/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.i18n;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLParserFactoryProducer;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class takes care of crawling through the source code
 *
 * @author matt
 */
public class MessagesSourceCrawler {

  /**
   * When searching for a scanPhrase, it may be spread over more than one source line. This string contains the
   * characters to be considered in the calculation of the different splits that each scanPhrase may have.
   *
   * @see #SPLIT_CHARACTERS
   * @see #SPLITTER_AND_WHITESPACES_PATTERN
   */
  private static final String SPLIT_CHARACTERS_STRING = ".(,";

  /**
   * When searching for a scanPhrase, it may be spread over more than one source line. These are the characters to be
   * considered in the calculation of the different splits that each scanPhrase may have.
   *
   * @see #SPLIT_CHARACTERS_STRING
   * @see #SPLITTER_AND_WHITESPACES_PATTERN
   */
  private static final char[] SPLIT_CHARACTERS = SPLIT_CHARACTERS_STRING.toCharArray();

  /**
   * Pattern to be used to ignore whitespaces around split characters.
   *
   * @see #SPLIT_CHARACTERS
   * @see #SPLIT_CHARACTERS_STRING
   */
  private static final String SPLITTER_AND_WHITESPACES_PATTERN = "\\s*[%c]\\s*";

  /**
   * Pattern to be used on the calculation of the regex to split the scanPhrase considering all split characters
   */
  private static final String SPLIT_AND_KEEP_DELIMITER_PATTERN = "((?<=[%1$c])|(?=[%1$c]))";

  /**
   * Format string to be used as a match pattern in identifying an incomplete scanPhrase (when it exists over more than
   * one source line).
   */
  private static final String SPLIT_LINE_PHRASE_FORMAT = ".*%s$";

  private static final String EMPTY_STRING = "";
  private static final String ASTERISK = "*";
  private static final String DOLLAR_SIGN = "$";
  private static final String DOT = ".";
  private static final String QUESTION_MARK = "?";
  private static final String SPACE = " ";
  private static final String TAB = "\t";
  private static final String N = "N";
  private static final String YES = "yes";
  private static final String DOT_CLASS = ".class";
  private static final String IMPORT_TOKEN = "import";
  private static final int IMPORT_TOKEN_LENGTH = IMPORT_TOKEN.length();
  private static final String JAVA_EXTENSION = "java";
  private static final String PACKAGE_END_MESSAGES = ".Messages;";
  private static final String PACKAGE_START_ORG_PENTAHO = "org.pentaho.";
  private static final String PACKAGE_START_SYSTEM = "System.";

  //REGEX expressions to be compiled
  private static final String PATTERN_PACKAGE_DECLARATION = "^\\s*package\\s*.*;\\s*$";
  private static final String PATTERN_ANY_PACKAGE_IMPORT =          "^\\s*import\\s*[a-z._0-9]*\\.([*]|([A-Z][a-z._0-9]*))\\s*;\\s*$";
  private static final String PATTERN_MESSAGES_PACKAGE_IMPORT = "^\\s*import\\s*[a-z._0-9]*\\.Messages;\\s*$";
  private static final String PATTERN_STRING_PKG_VARIABLE_DECLARATION = "^.*private static String PKG.*=.*$";
  private static final String PATTERN_CLASS_PKG_VARIABLE_DECLARATION = "^.*private static Class.*\\sPKG\\s*=.*$";

  /**
   * All phrases to scan for.
   */
  private String[] scanPhrases = {};

  /**
   * The source directories to crawl through
   */
  private List<String> sourceDirectories;

  /**
   * Source folder - package name - all the key occurrences in there
   */
  private Map<String, Map<String, List<KeyOccurrence>>> sourcePackageOccurrences = new HashMap<>();

  /**
   * The file names to avoid (base names)
   */
  private List<String> filesToAvoid = new ArrayList<>();

  private String singleMessagesFile;

  /**
   * The folders with XML files to scan for keys in
   */
  private List<SourceCrawlerXMLFolder> xmlFolders;

  private static Pattern packagePattern = Pattern.compile( PATTERN_PACKAGE_DECLARATION );
  private static Pattern importPattern = Pattern.compile( PATTERN_ANY_PACKAGE_IMPORT );
  private static Pattern importMessagesPattern = Pattern.compile( PATTERN_MESSAGES_PACKAGE_IMPORT );
  private static Pattern stringPkgPattern = Pattern.compile( PATTERN_STRING_PKG_VARIABLE_DECLARATION );
  private static Pattern classPkgPattern = Pattern.compile( PATTERN_CLASS_PKG_VARIABLE_DECLARATION );

  private LogChannelInterface log;

  /**
   * @param sourceDirectories  The source directories to crawl through
   * @param singleMessagesFile the messages file if there is only one, otherwise: null
   */
  public MessagesSourceCrawler( LogChannelInterface log, List<String> sourceDirectories,
                                String singleMessagesFile, List<SourceCrawlerXMLFolder> xmlFolders ) {
    super();
    this.log = log;
    this.sourceDirectories = ( null != sourceDirectories ) ? sourceDirectories : new ArrayList<>();
    this.singleMessagesFile = singleMessagesFile;
    this.xmlFolders = ( null != xmlFolders ) ? xmlFolders : new ArrayList<>();
  }

  /**
   * Add a key occurrence to the list of occurrences. The list is kept sorted on key and message package. If the key
   * already exists, we increment the number of occurrences.
   *
   * @param occ The key occurrence to add
   */
  public void addKeyOccurrence( KeyOccurrence occ ) {
    if ( null != occ ) {
      String sourceFolder = occ.getSourceFolder();
      if ( sourceFolder == null ) {
        throw new RuntimeException( "No source folder found for key: "
          + occ.getKey() + " in package " + occ.getMessagesPackage() );
      }
      String messagesPackage = occ.getMessagesPackage();

      // Do we have a map for the source folders?
      // If not, add one...
      //
      Map<String, List<KeyOccurrence>> packageOccurrences = sourcePackageOccurrences.get( sourceFolder );
      if ( packageOccurrences == null ) {
        packageOccurrences = new HashMap<>();
        sourcePackageOccurrences.put( sourceFolder, packageOccurrences );
      }

      // Do we have a map entry for the occurrences list in the source folder?
      // If not, add a list for the messages package
      //
      List<KeyOccurrence> occurrences = packageOccurrences.get( messagesPackage );
      if ( occurrences == null ) {
        occurrences = new ArrayList<>();
        occurrences.add( occ );
        packageOccurrences.put( messagesPackage, occurrences );
      } else {
        int index = Collections.binarySearch( occurrences, occ );
        if ( index < 0 ) {
          // Add it to the list, keep it sorted...
          //
          occurrences.add( -index - 1, occ );
        }
      }
    }
  }

  public void crawl() throws Exception {
    crawlSourceDirectories();
    // Also search for keys in the XUL files...
    crawlXmlFolders();
  }

  public void crawlSourceDirectories() throws Exception {

    for ( final String sourceDirectory : sourceDirectories ) {
      FileObject folder = KettleVFS.getFileObject( sourceDirectory );
      FileObject[] javaFiles = folder.findFiles( new FileSelector() {
        @Override
        public boolean traverseDescendents( FileSelectInfo info ) throws Exception {
          return true;
        }

        @Override
        public boolean includeFile( FileSelectInfo info ) throws Exception {
          FileObject file = info.getFile();
          FileName fileName = file.getName();

          return file.isFile() && JAVA_EXTENSION.equals( fileName.getExtension() ) && !filesToAvoid
            .contains( fileName.getBaseName() );
        }
      } );

      // Look for keys in each of the found files...
      for ( FileObject javaFile : javaFiles ) {
        lookForOccurrencesInFile( sourceDirectory, javaFile );
      }
    }
  }

  protected void crawlXmlFolders() throws Exception {

    for ( SourceCrawlerXMLFolder xmlFolder : xmlFolders ) {
      String[] xmlDirs = { xmlFolder.getFolder() };
      String[] xmlMasks = { xmlFolder.getWildcard() };
      String[] xmlReq = { N };
      boolean[] xmlSubdirs = { true }; // search sub-folders too

      FileInputList xulFileInputList =
        FileInputList.createFileList( new Variables(), xmlDirs, xmlMasks, xmlReq, xmlSubdirs );
      for ( FileObject fileObject : xulFileInputList.getFiles() ) {
        try {
          Document doc = XMLHandler.loadXMLFile( fileObject );

          // Scan for elements and tags in this file...
          //
          for ( SourceCrawlerXMLElement xmlElement : xmlFolder.getElements() ) {

            addLabelOccurrences( xmlFolder.getDefaultSourceFolder(), fileObject, doc
              .getElementsByTagName( xmlElement.getSearchElement() ), xmlFolder.getKeyPrefix(), xmlElement
              .getKeyTag(), xmlElement.getKeyAttribute(), xmlFolder.getDefaultPackage(), xmlFolder
              .getPackageExceptions() );
          }
        } catch ( KettleXMLException e ) {
          log.logError( "Unable to open XUL / XML document: " + fileObject );
        }
      }
    }
  }

  private void addLabelOccurrences( String sourceFolder, FileObject fileObject, NodeList nodeList,
                                    String keyPrefix, String tag, String attribute, String defaultPackage,
                                    List<SourceCrawlerPackageException> packageExcpeptions ) throws Exception {
    if ( nodeList == null ) {
      return;
    }

    TransformerFactory transformerFactory = XMLParserFactoryProducer.createSecureTransformerFactory();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, YES );
    transformer.setOutputProperty( OutputKeys.INDENT, YES );

    for ( int i = 0; i < nodeList.getLength(); i++ ) {
      Node node = nodeList.item( i );
      String labelString = null;

      if ( !Utils.isEmpty( attribute ) ) {
        labelString = XMLHandler.getTagAttribute( node, attribute );
      } else if ( !Utils.isEmpty( tag ) ) {
        labelString = XMLHandler.getTagValue( node, tag );
      }

      if ( labelString != null && labelString.startsWith( DOLLAR_SIGN ) ) {
        // just removed ${} around the key
        String key = labelString.substring( 2, labelString.length() - 1 ).trim();

        String messagesPackage = defaultPackage;
        for ( SourceCrawlerPackageException packageException : packageExcpeptions ) {
          if ( key.startsWith( packageException.getStartsWith() ) ) {
            messagesPackage = packageException.getPackageName();
          }
        }

        StringWriter bodyXML = new StringWriter();
        transformer.transform( new DOMSource( node ), new StreamResult( bodyXML ) );
        String xml = bodyXML.getBuffer().toString();

        KeyOccurrence keyOccurrence =
          new KeyOccurrence( fileObject, sourceFolder, messagesPackage, -1, -1, key, QUESTION_MARK, xml );
        addKeyOccurrence( keyOccurrence );
      }
    }
  }

  /**
   * Look for occurrences of keys in the specified file.
   *
   * @param sourceFolder The folder the java file and messages files live in
   * @param javaFile     The java source file to examine
   * @throws IOException In case there is a problem accessing the specified source file.
   */
  public void lookForOccurrencesInFile( String sourceFolder, FileObject javaFile ) throws IOException {

    try ( InputStreamReader is = new InputStreamReader( KettleVFS.getInputStream( javaFile ) );
          BufferedReader reader = new BufferedReader( is ) ) {
      String messagesPackage = null;
      String classPackage = null;
      int row = 0;

      Map<String, String> importedClasses = new HashMap<>(); // Remember the imports we do...

      String line = reader.readLine();
      List<String> splitLinePhrases = getSplitLinePhrases( scanPhrases );

      while ( line != null ) {
        row++;

        line = getCompleteLine( reader, line, splitLinePhrases );

        for ( String scanPhrase : scanPhrases ) {
          line = line.replaceAll( getWhitespacesSanitizeRegex( scanPhrase ), scanPhrase );
        }

        // Examine the line...

        // What we first look for is the import of the messages package.
        //
        // "package org.pentaho.di.trans.steps.sortedmerge;"
        //
        if ( packagePattern.matcher( line ).matches() ) {
          int beginIndex = line.indexOf( PACKAGE_START_ORG_PENTAHO );
          int endIndex = line.indexOf( ';' );
          if ( beginIndex >= 0 && endIndex > beginIndex ) {
            messagesPackage = line.substring( beginIndex, endIndex ); // this is the default
            classPackage = messagesPackage;
          }
        }

        // Remember all the imports...
        //
        if ( importPattern.matcher( line ).matches() ) {
          int beginIndex = line.indexOf( IMPORT_TOKEN ) + IMPORT_TOKEN_LENGTH + 1;
          int endIndex = line.indexOf( ';', beginIndex );
          if ( beginIndex >= 0 && endIndex > beginIndex ) {
            String expression = line.substring( beginIndex, endIndex );
            // The last word is the Class imported...
            // If it's * we ignore it.
            //
            int lastDotIndex = expression.lastIndexOf( '.' );
            if ( lastDotIndex > 0 ) {
              String packageName = expression.substring( 0, lastDotIndex );
              String className = expression.substring( lastDotIndex + 1 );
              if ( !ASTERISK.equals( className ) ) {
                importedClasses.put( className, packageName );
              }
            }
          }
        }

        // This is the alternative location of the messages package:
        //
        // "import org.pentaho.di.trans.steps.sortedmerge.Messages;"
        //
        if ( importMessagesPattern.matcher( line ).matches() ) {
          int beginIndex = line.indexOf( PACKAGE_START_ORG_PENTAHO );
          int endIndex = line.indexOf( PACKAGE_END_MESSAGES );
          if ( beginIndex >= 0 && endIndex > beginIndex ) {
            messagesPackage = line.substring( beginIndex, endIndex ); // if there is any specified, we take this one.
          }
        }

        // Look for the value of the PKG value...
        //
        // private static String PKG = "org.pentaho.foo.bar.somepkg";
        //
        if ( stringPkgPattern.matcher( line ).matches() ) {
          int beginIndex = line.indexOf( '"' ) + 1;
          int endIndex = line.indexOf( '"', beginIndex );
          if ( beginIndex >= 0 && endIndex > beginIndex ) {
            messagesPackage = line.substring( beginIndex, endIndex );
          }
        }

        // Look for the value of the PKG value as a fully qualified class...
        //
        // private static Class<?> PKG = Abort.class;
        //
        if ( classPackage != null && classPkgPattern.matcher( line ).matches() ) {
          int fromIndex = line.indexOf( '=' ) + 1;
          int toIndex = line.indexOf( DOT_CLASS, fromIndex );
          String expression = Const.trim( line.substring( fromIndex, toIndex ) );

          // If the expression doesn't contain any package, we'll look up the package in the imports. If not found
          // there,
          // it's a local package.
          //
          if ( expression.contains( DOT ) ) {
            int lastDotIndex = expression.lastIndexOf( '.' );
            messagesPackage = expression.substring( 0, lastDotIndex );
          } else {
            String packageName = importedClasses.get( expression );
            if ( packageName == null ) {
              messagesPackage = classPackage; // Local package
            } else {
              messagesPackage = packageName; // imported
            }
          }
        }

        // Now look for occurrences of "Messages.getString(", "BaseMessages.getString(PKG", ...
        //
        lookForOccurrencesInLine( sourceFolder, javaFile, messagesPackage, row, line );

        line = reader.readLine();
      }
    }
  }

  protected String getCompleteLine( BufferedReader reader, String line, List<String> splitLinePhrases )
    throws IOException {
    boolean extraLine;

    do {
      String line2 = line;
      extraLine = false;
      for ( String joinPhrase : splitLinePhrases ) {
        if ( line2.matches( joinPhrase ) ) {
          extraLine = true;
          break;
        }
      }
      if ( extraLine ) {
        line2 = reader.readLine();
        if ( null == line2 ) {
          break;
        }

        line += line2;
      }
    } while ( extraLine );

    return line;
  }

  /**
   * Look for occurrences of keys in the specified line.
   *
   * @param sourceFolder    the folder where the file that contains the line to examine exists
   * @param javaFile        the java source file that contains the line to examine
   * @param messagesPackage the message package used
   * @param row             the row number
   * @param line            the line to examine
   */
  protected void lookForOccurrencesInLine( String sourceFolder, FileObject javaFile, String messagesPackage, int row,
                                           String line ) {
    for ( String scanPhrase : scanPhrases ) {
      int index = line.indexOf( scanPhrase );
      while ( index >= 0 ) {
        // see if there's a character [a-z][A-Z] before the search string...
        // Otherwise we're looking at BaseMessages.getString(), etc.
        //
        if ( index == 0 || !Character.isJavaIdentifierPart( line.charAt( index - 1 ) ) ) {
          addLineOccurrence( sourceFolder, javaFile, messagesPackage, line, row, index, scanPhrase );
        }
        index = line.indexOf( scanPhrase, index + 1 );
      }
    }
  }

  /**
   * <p>Returns all regex expressions to detect an incomplete scanPhrase (when it exists over more than one source
   * line).</p>
   *
   * @param scanPhrases an array containing all scanPhrases to consider
   * @return all regex expressions to detect an incomplete scanPhrase
   * @see #getSplitLinePhrases(String)
   */
  private List<String> getSplitLinePhrases( String[] scanPhrases ) {
    List<String> joinPhrases = new ArrayList<>();

    if ( null != scanPhrases ) {
      for ( String scanPhrase : scanPhrases ) {
        joinPhrases.addAll( getSplitLinePhrases( scanPhrase ) );
      }
    }

    return joinPhrases;
  }

  /**
   * <p>Returns all regex expressions to detect when the given scanPhrase is incomplete (existing over more than one
   * source line).</p>
   *
   * @param scanPhrase the scanPhrase to consider
   * @return all regex expressions to detect when the given scanPhrase is incomplete
   * @see #getSplitLinePhrases(String[])
   */
  protected List<String> getSplitLinePhrases( String scanPhrase ) {
    List<String> joinPhrases = new ArrayList<>();

    // First split the phrase
    String regex = getSplitWithDelimitersRegex( SPLIT_CHARACTERS );
    String[] splitTmp = scanPhrase.split( regex );

    for ( String splitPart : splitTmp ) {
      StringBuilder sb = new StringBuilder();
      // If it's a split character, handle with care
      if ( 1 != splitPart.length() && !SPLIT_CHARACTERS_STRING.contains( splitPart ) ) {
        sb.append( splitPart.trim() ).append( "\\s*" );
      } else {
        sb.append( String.format( "[%s]\\s*", splitPart ) );
      }

      joinPhrases.add( String.format( SPLIT_LINE_PHRASE_FORMAT, sb.toString() ) );
    }

    return joinPhrases;
  }

  private String getSplitWithDelimitersRegex( char[] delimiters ) {
    StringBuilder regex = new StringBuilder();
    boolean notFirstTime = false;

    for ( char delimiter : delimiters ) {
      if ( notFirstTime ) {
        regex.append( '|' );
      }

      regex.append( String.format( SPLIT_AND_KEEP_DELIMITER_PATTERN, delimiter ) );
      notFirstTime = true;
    }

    return regex.toString();
  }


  /**
   * <p>Calculates a regex to identify the given text considering all possible {@link #SPLIT_CHARACTERS} can be
   * surrounded by whitespaces.</p>
   *
   * @param str the text to be used
   * @return regex to identify the given text considering all possible {@link #SPLIT_CHARACTERS} can be surrounded by
   * whitespaces
   */
  protected String getWhitespacesSanitizeRegex( String str ) {
    String whitespacesSanitizeRegex = str;

    if ( null != whitespacesSanitizeRegex ) {
      for ( char splitChar : SPLIT_CHARACTERS ) {
        String patternMatcher = String.format( SPLITTER_AND_WHITESPACES_PATTERN, splitChar );

        whitespacesSanitizeRegex =
          whitespacesSanitizeRegex.replaceAll( patternMatcher, Matcher.quoteReplacement( patternMatcher ) );
      }
    }

    return whitespacesSanitizeRegex;
  }

  /**
   * Extract the needed information from the line and the index on which Messages.getString() occurs.
   *
   * @param sourceFolder    the source folder the messages and java files live in
   * @param fileObject      the file we're reading
   * @param messagesPackage the messages package
   * @param line            the line
   * @param row             the row number
   * @param index           the index in the line on which "Messages.getString(" is located.
   */
  private void addLineOccurrence( String sourceFolder, FileObject fileObject, String messagesPackage, String line,
                                  int row, int index, String scanPhrase ) {
    // Right after the "Messages.getString(" string is the key, quoted (")
    // until the next comma...
    //
    int column = index + scanPhrase.length();
    String arguments = EMPTY_STRING;

    // we start at the double quote...
    //
    int startKeyIndex = line.indexOf( '"', column ) + 1;
    int endKeyIndex = line.indexOf( '"', startKeyIndex );

    String key;
    if ( endKeyIndex >= 0 ) {
      key = line.substring( startKeyIndex, endKeyIndex );

      // Can we also determine the arguments?
      // No, not always: only if the arguments are all on the same line.
      //

      // Look for the next closing bracket...
      //
      int bracketIndex = endKeyIndex;
      int nrOpen = 1;
      while ( nrOpen != 0 && bracketIndex < line.length() ) {
        int c = line.charAt( bracketIndex );
        if ( c == '(' ) {
          nrOpen++;
        }
        if ( c == ')' ) {
          nrOpen--;
        }
        bracketIndex++;
      }

      if ( bracketIndex + 1 < line.length() ) {
        arguments = line.substring( endKeyIndex + 1, bracketIndex );
      } else {
        arguments = line.substring( endKeyIndex + 1 );
      }
    } else {
      key = line.substring( startKeyIndex );
    }

    // Sanity check...
    //
    if ( key.contains( TAB ) || key.contains( SPACE ) ) {
      System.out.println( "Suspect key found: [" + key + "] in file [" + fileObject + "]" );
    }

    // OK, add the occurrence to the list...
    //
    // Make sure we pass the System key occurrences to the correct package.
    //
    if ( key.startsWith( PACKAGE_START_SYSTEM ) ) {
      String i18nPackage = BaseMessages.class.getPackage().getName();
      KeyOccurrence keyOccurrence =
        new KeyOccurrence( fileObject, sourceFolder, i18nPackage, row, column, key, arguments, line );

      // If we just add this key, we'll get doubles in the i18n package
      //
      KeyOccurrence lookup = getKeyOccurrence( key, i18nPackage );
      if ( lookup == null ) {
        addKeyOccurrence( keyOccurrence );
      } else {
        // Adjust the line of code...
        //
        lookup.setSourceLine( lookup.getSourceLine() + Const.CR + keyOccurrence.getSourceLine() );
        lookup.incrementOccurrences();
      }
    } else {
      KeyOccurrence keyOccurrence =
        new KeyOccurrence( fileObject, sourceFolder, messagesPackage, row, column, key, arguments, line );
      addKeyOccurrence( keyOccurrence );
    }
  }

  /**
   * @return A sorted {@link List} of distinct occurrences of the used message package names
   */
  public List<String> getMessagesPackagesList( String sourceFolder ) {
    Map<String, List<KeyOccurrence>> packageOccurrences = sourcePackageOccurrences.get( sourceFolder );
    List<String> list = new ArrayList<>( packageOccurrences.keySet() );
    Collections.sort( list );
    return list;
  }

  /**
   * Get all the key occurrences for a certain messages package.
   *
   * @param messagesPackage the package to hunt for
   * @return all the key occurrences for a certain messages package.
   */
  public List<KeyOccurrence> getOccurrencesForPackage( String messagesPackage ) {
    List<KeyOccurrence> list = new ArrayList<>();
    for ( Map<String, List<KeyOccurrence>> po : sourcePackageOccurrences.values() ) {
      List<KeyOccurrence> occurrences = po.get( messagesPackage );
      if ( occurrences != null ) {
        list.addAll( occurrences );
      }
    }
    return list;
  }

  public KeyOccurrence getKeyOccurrence( String key, String selectedMessagesPackage ) {
    for ( Map<String, List<KeyOccurrence>> po : sourcePackageOccurrences.values() ) {
      if ( po != null ) {
        List<KeyOccurrence> occurrences = po.get( selectedMessagesPackage );
        if ( occurrences != null ) {
          for ( KeyOccurrence keyOccurrence : occurrences ) {
            if ( keyOccurrence.getKey().equals( key )
              && keyOccurrence.getMessagesPackage().equals( selectedMessagesPackage ) ) {
              return keyOccurrence;
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Get the unique package-key
   *
   * @param sourceFolder
   */
  public List<KeyOccurrence> getKeyOccurrences( String sourceFolder ) {
    Map<String, KeyOccurrence> map = new HashMap<>();
    Map<String, List<KeyOccurrence>> po = sourcePackageOccurrences.get( sourceFolder );
    if ( po != null ) {
      for ( List<KeyOccurrence> keyOccurrences : po.values() ) {
        for ( KeyOccurrence keyOccurrence : keyOccurrences ) {
          String key = keyOccurrence.getMessagesPackage() + " - " + keyOccurrence.getKey();
          map.put( key, keyOccurrence );
        }
      }
    }

    return new ArrayList<>( map.values() );
  }

  /**
   * @return the {@link List} of source directories to crawl through
   */
  public List<String> getSourceDirectories() {
    return sourceDirectories;
  }

  /**
   * @param sourceDirectories the {@link List} of source directories to crawl through
   */
  public void setSourceDirectories( List<String> sourceDirectories ) {
    this.sourceDirectories = ( null != sourceDirectories ) ? sourceDirectories : new ArrayList<>();
  }

  /**
   * @return the {@link List} of files to avoid
   */
  public List<String> getFilesToAvoid() {
    return filesToAvoid;
  }

  /**
   * @param filesToAvoid the {@link List} of files to avoid
   */
  public void setFilesToAvoid( List<String> filesToAvoid ) {
    this.filesToAvoid = ( null != filesToAvoid ) ? filesToAvoid : new ArrayList<>();
  }

  /**
   * @return the singleMessagesFile
   */
  public String getSingleMessagesFile() {
    return singleMessagesFile;
  }

  /**
   * @param singleMessagesFile the singleMessagesFile to set
   */
  public void setSingleMessagesFile( String singleMessagesFile ) {
    this.singleMessagesFile = singleMessagesFile;
  }

  /**
   * @return the scanPhrases to search for
   */
  public String[] getScanPhrases() {
    return scanPhrases;
  }

  /**
   * @param scanPhrases the scanPhrases to search for
   */
  public void setScanPhrases( String[] scanPhrases ) {
    this.scanPhrases = ( null != scanPhrases ) ? scanPhrases : new String[] {};
  }

  /**
   * @return the sourcePackageOccurrences
   */
  public Map<String, Map<String, List<KeyOccurrence>>> getSourcePackageOccurrences() {
    return sourcePackageOccurrences;
  }

  /**
   * @param sourcePackageOccurrences the sourcePackageOccurrences to set
   */
  public void setSourcePackageOccurrences( Map<String, Map<String, List<KeyOccurrence>>> sourcePackageOccurrences ) {
    this.sourcePackageOccurrences = ( null != sourcePackageOccurrences ) ? sourcePackageOccurrences : new HashMap<>();
  }
}
