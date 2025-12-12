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

package org.pentaho.di.job.entries.unzip;

import org.json.simple.JSONObject;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.util.ZipUnzipHelper;
import org.pentaho.di.job.entry.BaseJobEntryHelper;

import java.util.Map;

public class JobEntryUnZipFileHelper extends BaseJobEntryHelper {

  private static final String SHOW_FILE_NAME = "showFileName";
  private static final Class<?> PKG = JobEntryUnZip.class;

  private final JobEntryUnZip jobEntryUnZipFile;

  public JobEntryUnZipFileHelper( JobEntryUnZip jobEntryUnZipFile ) {
    super();
    this.jobEntryUnZipFile = jobEntryUnZipFile;
  }

  @Override
  protected JSONObject handleJobEntryAction( String method, JobMeta jobMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      if ( SHOW_FILE_NAME.equals( method ) ) {
        response = ZipUnzipHelper.showFileNameAction( jobMeta, queryParams, jobEntryUnZipFile, PKG );
      } else {
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

}
