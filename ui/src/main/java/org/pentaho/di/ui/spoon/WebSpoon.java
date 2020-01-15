/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 * Copyright (C) 2016 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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

package org.pentaho.di.ui.spoon;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.service.ResourceLoader;

public class WebSpoon implements ApplicationConfiguration {

  public void configure( Application application ) {
    application.addResource( "ui/images/spoon.ico", new ResourceLoader() {
      public InputStream getResourceAsStream( String resourceName ) throws IOException {
        return this.getClass().getClassLoader().getResourceAsStream( "ui/images/spoon.ico" );
      }
    } );
    Arrays.asList(
      "org/pentaho/di/ui/spoon/clipboard.js"
    ).stream().forEach( str -> {
      application.addResource( "js/" + FilenameUtils.getName( str ), new ResourceLoader() {
        @Override
        public InputStream getResourceAsStream( String resourceName ) throws IOException {
          return this.getClass().getClassLoader().getResourceAsStream( str );
        }
      } );
    });
    Map<String, String> properties = new HashMap<String, String>();
    properties.put( WebClient.PAGE_TITLE, "Data Transformation Editor" );
    properties.put( WebClient.FAVICON, "ui/images/spoon.ico" );
    application.addEntryPoint( "/spoon", WebSpoonEntryPoint.class, properties );
    application.setOperationMode( Application.OperationMode.SWT_COMPATIBILITY );

    application.addServiceHandler( "downloadServiceHandler", new DownloadServiceHandler() );
  }
}
