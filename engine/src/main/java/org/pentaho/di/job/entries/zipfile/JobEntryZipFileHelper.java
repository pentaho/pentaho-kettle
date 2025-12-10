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

package org.pentaho.di.job.entries.zipfile;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.BaseJobEntryHelper;

import java.util.Map;

public class JobEntryZipFileHelper extends BaseJobEntryHelper {

  private static final String SHOW_FILE_NAME = "showFileName";
  private static final Class<?> PKG = JobEntryZipFile.class;

  private final JobEntryZipFile jobEntryZipFile;

  public JobEntryZipFileHelper( JobEntryZipFile jobEntryZipFile ) {
    super();
    this.jobEntryZipFile = jobEntryZipFile;
  }

  @Override
  protected JSONObject handleJobEntryAction( String method, JobMeta jobMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      if ( SHOW_FILE_NAME.equals( method ) ) {
        response = showFileNameAction( jobMeta, queryParams );
      } else {
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  public JSONObject showFileNameAction( JobMeta jobMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JSONArray fileList = new JSONArray();

    String zipFilename = Const.NVL( queryParams.get( "zipfilename" ), "" );
    zipFilename = jobMeta.environmentSubstitute( zipFilename );

    boolean addDate = "Y".equals( queryParams.get( "adddate" ) );
    boolean addTime = "Y".equals( queryParams.get( "addtime" ) );
    boolean specifyFormat = "Y".equals( queryParams.get( "SpecifyFormat" ) );
    String dateTimeFormat = Const.NVL( queryParams.get( "date_time_format" ), "" );

    if ( Utils.isEmpty( zipFilename ) ) {
      response.put( "message", BaseMessages.getString( PKG, "JobZipFiles.ZipFilename.Tooltip" ) );
    } else {
      String finalZipName = jobEntryZipFile.getFullFilename(
        zipFilename,
        addDate,
        addTime,
        specifyFormat,
        dateTimeFormat
      );
      fileList.add( finalZipName );
      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    }

    response.put( "files", fileList );
    return response;
  }
}
