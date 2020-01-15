/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 * Copyright (C) 2017 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.service.ServiceHandler;

public class DownloadServiceHandler implements ServiceHandler {

  public void service( HttpServletRequest request, HttpServletResponse response )
    throws IOException, ServletException {
    // Which file to download?
    String fileName = request.getParameter( "filename" );
    // Get the file content
    File file = new File( fileName );
    FileInputStream fin = null;
    byte[] download = null;
    try {
      fin = new FileInputStream( file );
      download = new byte[(int) file.length() ];
      fin.read( download );
    } catch ( IOException e ) {
      throw new IOException( e.getMessage() );
    }
    fin.close();
    /*
     *  TODO : if the following line is not executed, the exported xml file remains in the server.
     *  There might be a better way to ensure the file is deleted.
     */
    file.delete();
    // Send the file in the response
    response.setContentType( "application/octet-stream" );
    response.setContentLength( download.length );
    String contentDisposition = "attachment; filename=\"" + file.getName() + "\"";
    response.setHeader( "Content-Disposition", contentDisposition );
    response.getOutputStream().write( download );
  }
}
