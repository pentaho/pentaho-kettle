/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class StatusServletUtils {

  public static String getPentahoStyles() {
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

      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbFactory.newDocumentBuilder();
      Document doc = db.parse( f );
      themeName = doc.getElementsByTagName( "default-theme" ).item( 0 ).getTextContent();

      // Get theme CSS file
      String themeDirStr = relativePathSeparator
          + "pentaho-solutions" + File.separator + "system" + File.separator
          + "common-ui" + File.separator + "resources" + File.separator
          + "themes" + File.separator + themeName + File.separator;
      File themeDir = new File( themeDirStr );
      for ( File fName : themeDir.listFiles() ) {
        if ( fName.getName().contains( ".css" ) ) {
          themeCss = fName.getName();
          break;
        }
      }

      // webapps folder will always be one directory closer to default directory, need to update relative path string
      relativePathSeparator = relativePathSeparator.replaceFirst( "(\\.\\.\\\\)", "" );

      // Get mantle theme CSS file
      String mantleThemeDirStr = relativePathSeparator + "webapps" + File.separator
          + "pentaho" + File.separator + "mantle" + File.separator
          + "themes" + File.separator + themeName + File.separator;
      File mantleThemeDir = new File( mantleThemeDirStr );
      for ( File fName : mantleThemeDir.listFiles() ) {
        if ( fName.getName().contains( ".css" ) ) {
          mantleThemeCss = fName.getName();
          break;
        }
      }
    } catch ( Exception ex ) {
      // log here
    }

    sb.append( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/pentaho/content/common-ui/resources/themes/" + themeName + "/" + themeCss + "\"/>" );
    sb.append( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/pentaho/mantle/themes/" + themeName + "/" + mantleThemeCss + "\"/>" );
    sb.append( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/pentaho/mantle/MantleStyle.css\"/>" );
    return sb.toString();
  }
}
