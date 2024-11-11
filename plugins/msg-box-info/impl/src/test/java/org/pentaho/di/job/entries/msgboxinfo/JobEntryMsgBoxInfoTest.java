/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.job.entries.msgboxinfo;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryMsgBoxInfoTest extends JobEntryLoadSaveTestSupport<JobEntryMsgBoxInfo> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryMsgBoxInfo> getJobEntryClass() {
    return JobEntryMsgBoxInfo.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "bodymessage",
        "titremessage" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "bodymessage", "getBodyMessage",
        "titremessage", "getTitleMessage" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "bodymessage", "setBodyMessage",
        "titremessage", "setTitleMessage" );
  }

}
