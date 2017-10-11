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
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterJobServlet extends BaseJobServlet {

  private static final long serialVersionUID = 7416802722393075758L;
  public static final String CONTEXT_PATH = "/kettle/registerJob";

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

  @Override
  WebResult generateBody( HttpServletRequest request, HttpServletResponse response, boolean useXML ) throws IOException, KettleException {

    final String xml = IOUtils.toString( request.getInputStream() );

    // Parse the XML, create a job configuration
    JobConfiguration jobConfiguration = JobConfiguration.fromXML( xml );

    Job job = createJob( jobConfiguration );

    String message = "Job '" + job.getJobname() + "' was added to the list with id " + job.getContainerObjectId();
    return new WebResult( WebResult.STRING_OK, message, job.getContainerObjectId() );
  }
}
