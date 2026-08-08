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

package org.pentaho.di.job.entries.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.unzip.JobEntryUnZip;
import org.pentaho.di.job.entries.zipfile.JobEntryZipFile;
import java.util.Map;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.ACTION_STATUS;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.SUCCESS_RESPONSE;

public class ZipUnzipHelper {

  private ZipUnzipHelper() {
    // private constructor to prevent instantiation of utility class
  }

  public static JSONObject showFileNameAction( JobMeta jobMeta, Map<String, String> queryParams, Object jobEntryZipUnZipFileInstance, Class<?> pkg ) {
    JSONObject response = new JSONObject();
    JSONArray fileList = new JSONArray();

    String zipFilename = Const.NVL( queryParams.get( "zipfilename" ), "" );
    zipFilename = jobMeta.environmentSubstitute( zipFilename );

    boolean addDate = "Y".equals( queryParams.get( "adddate" ) );
    boolean addTime = "Y".equals( queryParams.get( "addtime" ) );
    boolean specifyFormat = "Y".equals( queryParams.get( "SpecifyFormat" ) );
    String dateTimeFormat = Const.NVL( queryParams.get( "date_time_format" ), "" );

    if ( Utils.isEmpty( zipFilename ) ) {
      response.put( "message", BaseMessages.getString( pkg, "JobZipFiles.ZipFilename.Tooltip" ) );
    } else {
      String finalZipName = "";
      if ( jobEntryZipUnZipFileInstance instanceof JobEntryUnZip jobEntryUnZip ) {
        finalZipName = jobEntryUnZip.getFullFilename( zipFilename, addDate, addTime, specifyFormat, dateTimeFormat );
            } else if ( jobEntryZipUnZipFileInstance instanceof JobEntryZipFile jobEntryZipFile ) {
        finalZipName = jobEntryZipFile.getFullFilename(
                     zipFilename, addDate, addTime, specifyFormat, dateTimeFormat
            );
      }
      fileList.add( finalZipName );
      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    }

    response.put( "files", fileList );
    return response;
  }
}