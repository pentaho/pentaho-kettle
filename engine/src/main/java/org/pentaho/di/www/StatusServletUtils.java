/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.www;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.ui.xul.util.XmlParserFactoryProducer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

class StatusServletUtils {

  public static final String PENTAHO_SOLUTIONS_PATH_COMPONENT = "pentaho-solutions";
  public static final String RESOURCES_PATH_COMPONENT = "resources";
  @SuppressWarnings( "squid:S1075" ) static final String RESOURCES_PATH = "/content/common-ui/resources/themes";
  @SuppressWarnings( "squid:S1075" ) static final String STATIC_PATH = "/static";
  private static final String SYSTEM_PATH_COMPONENT = "system";
  private static final String PENTAHO_XML_FILENAME = "pentaho.xml";
  static final String PENTAHO_ROOT = "/pentaho";
  private static final String LINK_HTML_PREFIX = "<link rel=\"stylesheet\" type=\"text/css\" href=\"";
  public static final String THEMES_PATH_COMPONENT = "themes";

  private StatusServletUtils() {
  }


  @SuppressWarnings( "javasecurity:S2083" )
  // root is ultimately derived from the servlet uri, which must be correct otherwise we couldn't have ended up here
  static String getPentahoStyles( String root ) {
    String themeName = "ruby"; // default pentaho theme
    String themeCss = "globalRuby.css";
    String mantleThemeCss = "mantleRuby.css";

    try {
      String relativePathSeparator = ".." + File.separator;

      // Read in currently set theme from pentaho.xml file

      String themeSetting = relativePathSeparator
        + PENTAHO_SOLUTIONS_PATH_COMPONENT + File.separator + SYSTEM_PATH_COMPONENT + File.separator + PENTAHO_XML_FILENAME;
      File f = new File( themeSetting );

      // Check if file exists (may be different location depending on how server was started)
      if ( !f.exists() ) {
        //on loading pentaho by startup.bat or Windows service, the relative paths are different. This is meant to
        // allow both types of execution
        relativePathSeparator = ".." + File.separator + ".." + File.separator;
        themeSetting = relativePathSeparator
          + PENTAHO_SOLUTIONS_PATH_COMPONENT + File.separator + SYSTEM_PATH_COMPONENT + File.separator + PENTAHO_XML_FILENAME;
        f = new File( themeSetting );
      }

      themeName = buildThemeName( f );


      // Get theme CSS file
      String themeDirStr = relativePathSeparator
        + PENTAHO_SOLUTIONS_PATH_COMPONENT + File.separator + SYSTEM_PATH_COMPONENT + File.separator
        + "common-ui" + File.separator + RESOURCES_PATH_COMPONENT + File.separator
        + THEMES_PATH_COMPONENT + File.separator + themeName + File.separator;
      File themeDir = new File( themeDirStr );
      for ( File fName : Optional.ofNullable( themeDir.listFiles() ).orElse( new File[ 0 ] ) ) {
        if ( fName.getName().contains( ".css" ) ) {
          themeCss = fName.getName();
          break;
        }
      }

      // webapps folder will always be one directory closer to default directory, need to update relative path string
      relativePathSeparator = StringUtils.replaceOnce( relativePathSeparator, ".." + File.separator, "" );

      // Get mantle theme CSS file
      String mantleThemeDirStr = relativePathSeparator + "webapps" + root + File.separator + "mantle" + File.separator
        + THEMES_PATH_COMPONENT + File.separator + themeName + File.separator;
      File mantleThemeDir = new File( mantleThemeDirStr );
      for ( File fName : Optional.ofNullable( mantleThemeDir.listFiles() ).orElse( new File[ 0 ] ) ) {
        if ( fName.getName().contains( ".css" ) ) {
          mantleThemeCss = fName.getName();
          break;
        }
      }
    } catch ( ParserConfigurationException | IOException | SAXException e ) {
      //BISERVER-14930 As requested, the error was hidden from the logs
      //LogChannel.GENERAL.logError( e.getMessage(), e );
    }

    return buildCssPath( root, themeName, themeCss, mantleThemeCss );
  }

  private static String buildCssPath( String root, String themeName, String themeCss,
                                 String mantleThemeCss ) {
    StringBuilder sb = new StringBuilder();
    sb.append( LINK_HTML_PREFIX ).append( root ).append( "/content/common-ui/resources/themes/" ).append( themeName )
      .append( "/" ).append( themeCss ).append( "\"/>" );
    sb.append( LINK_HTML_PREFIX ).append( root ).append( "/mantle/themes/" ).append( themeName ).append( "/" )
      .append( mantleThemeCss ).append( "\"/>" );
    sb.append( LINK_HTML_PREFIX ).append( root ).append( "/mantle/MantleStyle.css\"/>" );
    return sb.toString();
  }

  private static String buildThemeName( File f )
    throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory dbFactory = XmlParserFactoryProducer.createSecureDocBuilderFactory();
    DocumentBuilder db = dbFactory.newDocumentBuilder();
    Document doc = db.parse( f );
    return doc.getElementsByTagName( "default-theme" ).item( 0 ).getTextContent();
  }

  static String getPentahoStyles( final ServletContext context, String root ) {

    if ( context == null ) {
      return getPentahoStyles( root );
    }

    String themeName = "ruby"; // default pentaho theme
    String themeCss = "globalRuby.css";
    String mantleThemeCss = "mantleRuby.css";
    String solutionPath = getSolutionPath( context );
    try {

      String themeSettingPath = solutionPath + File.separator + SYSTEM_PATH_COMPONENT + File.separator + PENTAHO_XML_FILENAME;

      File f = new File( themeSettingPath );

      themeName = buildThemeName( f );

      // Get theme CSS file
      String themeDirStr = solutionPath + File.separator + SYSTEM_PATH_COMPONENT + File.separator
        + "common-ui" + File.separator + RESOURCES_PATH_COMPONENT + File.separator
        + THEMES_PATH_COMPONENT + File.separator + themeName + File.separator;
      File themeDir = new File( themeDirStr );
      for ( File fName : Optional.ofNullable( themeDir.listFiles() ).orElse( new File[ 0 ] ) ) {
        if ( fName.getName().contains( ".css" ) ) {
          themeCss = fName.getName();
          break;
        }
      }

      String webappsPath = getWebappsPath( root );

      // Get mantle theme CSS file
      String mantleThemeDirStr = webappsPath + File.separator + "mantle" + File.separator
        + THEMES_PATH_COMPONENT + File.separator + themeName + File.separator;
      File mantleThemeDir = new File( mantleThemeDirStr );
      for ( File fName : Optional.ofNullable( mantleThemeDir.listFiles() ).orElse( new File[ 0 ] ) ) {
        if ( fName.getName().contains( ".css" ) ) {
          mantleThemeCss = fName.getName();
          break;
        }
      }
    } catch ( ParserConfigurationException | IOException | SAXException e ) {
      LogChannel.GENERAL.logError( e.getMessage(), e );
    }

    return buildCssPath( root, themeName, themeCss, mantleThemeCss );
  }

  private static String getWebappsPath( String root ) {

    String oneLvlUp = ".." + File.separator;

    //we expect the folder to exist in the tomcat/webapps folder
    //we try two levels up
    String webAppsPath = oneLvlUp + oneLvlUp + root;
    File webappsFolder = new File( webAppsPath );
    if (!webappsFolder.exists() || !"webapps".equals(webappsFolder.getName())) {
      //if not, we move three levels up
      webAppsPath = oneLvlUp + webAppsPath;
    }
    return webAppsPath;
  }

  private static String getSolutionPath( ServletContext context ) {

    if ( context != null ) {
      String rootPath = context.getInitParameter( "solution-path" );
      if ( rootPath != null && !rootPath.isEmpty() ) {
        File file = new File( rootPath );
        if ( file.isDirectory() ) {
          return rootPath;
        }
      }
    }
    //if we can't find the configured value, we get the value from runtime system relative path
    File file = new File( "" );
    file.deleteOnExit();
    int ps;
    String solutionPath = "";

    try {
      ps = file.getCanonicalPath().lastIndexOf( "pentaho-server" );
      if ( ps != -1 ) {
        solutionPath =
                file.getCanonicalPath().substring(0, ps) + "pentaho-server" + File.separator + PENTAHO_SOLUTIONS_PATH_COMPONENT;
      } else {
        LogChannel.GENERAL.logBasic( "solution path was not found in: " + file.getAbsolutePath()  + ". Please configure the property \"solution-path\" in your instalation's WEB-INF folder.");
      }
    } catch ( IOException e ) {
      LogChannel.GENERAL.logError( e.getMessage(), e );
    }

    return solutionPath;


  }
}
