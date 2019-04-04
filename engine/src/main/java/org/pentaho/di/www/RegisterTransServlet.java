/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.io.IOUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterTransServlet extends BaseJobServlet {

  private static final long serialVersionUID = 468054102740138751L;
  public static final String CONTEXT_PATH = "/kettle/registerTrans";

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

  @Override
  WebResult generateBody( HttpServletRequest request, HttpServletResponse response, boolean useXML ) throws IOException, KettleException  {

    final String xml = IOUtils.toString( request.getInputStream() );

    // Parse the XML, create a transformation configuration
    TransConfiguration transConfiguration = TransConfiguration.fromXML( xml );

    Trans trans = createTrans( transConfiguration );

    String message = "Transformation '" + trans.getName() + "' was added to Carte with id " + trans.getContainerObjectId();
    return new WebResult( WebResult.STRING_OK, message, trans.getContainerObjectId() );
  }

}
