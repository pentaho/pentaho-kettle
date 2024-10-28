/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.www;

import org.apache.commons.io.IOUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
