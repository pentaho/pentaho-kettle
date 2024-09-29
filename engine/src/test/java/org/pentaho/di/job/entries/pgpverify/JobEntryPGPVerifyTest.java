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

package org.pentaho.di.job.entries.pgpverify;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryPGPVerifyTest extends JobEntryLoadSaveTestSupport<JobEntryPGPVerify> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryPGPVerify> getJobEntryClass() {
    return JobEntryPGPVerify.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "gpglocation",
        "filename",
        "detachedfilename",
        "useDetachedSignature" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "gpglocation", "getGPGLocation",
        "filename", "getFilename",
        "detachedfilename", "getDetachedfilename",
        "useDetachedSignature", "useDetachedfilename" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "gpglocation", "setGPGLocation",
        "filename", "setFilename",
        "detachedfilename", "setDetachedfilename",
        "useDetachedSignature", "setUseDetachedfilename" );
  }

}
