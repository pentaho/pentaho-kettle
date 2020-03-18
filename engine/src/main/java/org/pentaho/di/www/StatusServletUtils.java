/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2019 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.ui.xul.util.XmlParserFactoryProducer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

class StatusServletUtils {

  @SuppressWarnings( "squid:S1075" ) static final String RESOURCES_PATH = "/content/common-ui/resources/themes";
  @SuppressWarnings( "squid:S1075" ) static final String STATIC_PATH = "/static";
  static final String PENTAHO_ROOT = "/pentaho";
  private static final String LINK_HTML_PREFIX = "<link rel=\"stylesheet\" type=\"text/css\" href=\"";

  private StatusServletUtils() {
  }

  @SuppressWarnings( "javasecurity:S2083" )
  // root is ultimately derived from the servlet uri, which must be correct otherwise we couldn't have ended up here
  static String getPentahoStyles( String root ) {
    StringBuilder sb = new StringBuilder();
    String themeName = "ruby"; // default pentaho theme
    String themeCss = "globalRuby.css";
    String mantleThemeCss = "mantleRuby.css";

    try {
      String relativePathSeparator = ".." + File.separator + ".." + File.separator;

      // Read in currently set theme from pentaho.xml file
      String themeSetting = relativePathSeparator
        + "pentaho-solutions" + File.separator + "system" + File.separator + "pentaho.xml";
      File f = new File( themeSetting );

      // Check if file exists (may be different location depending on how server was started)
      if ( !f.exists() ) {
        relativePathSeparator = ".." + File.separator;
      }

      DocumentBuilderFactory dbFactory = XmlParserFactoryProducer.createSecureDocBuilderFactory();
      DocumentBuilder db = dbFactory.newDocumentBuilder();
      Document doc = db.parse( f );
      themeName = doc.getElementsByTagName( "default-theme" ).item( 0 ).getTextContent();

      // Get theme CSS file
      String themeDirStr = relativePathSeparator
        + "pentaho-solutions" + File.separator + "system" + File.separator
        + "common-ui" + File.separator + "resources" + File.separator
        + "themes" + File.separator + themeName + File.separator;
      File themeDir = new File( themeDirStr );
      for ( File fName : Optional.ofNullable( themeDir.listFiles() ).orElse( new File[ 0 ] ) ) {
        if ( fName.getName().contains( ".css" ) ) {
          themeCss = fName.getName();
          break;
        }
      }

      // webapps folder will always be one directory closer to default directory, need to update relative path string
      relativePathSeparator = relativePathSeparator.replaceFirst( "(\\.\\.\\\\)", "" );

      // Get mantle theme CSS file
      String mantleThemeDirStr = relativePathSeparator + "webapps" + root + File.separator + "mantle" + File.separator
        + "themes" + File.separator + themeName + File.separator;
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

    sb.append( LINK_HTML_PREFIX ).append( root ).append( "/content/common-ui/resources/themes/" ).append( themeName )
      .append( "/" ).append( themeCss ).append( "\"/>" );
    sb.append( LINK_HTML_PREFIX ).append( root ).append( "/mantle/themes/" ).append( themeName ).append( "/" )
      .append( mantleThemeCss ).append( "\"/>" );
    sb.append( LINK_HTML_PREFIX ).append( root ).append( "/mantle/MantleStyle.css\"/>" );
    return sb.toString();
  }
}
