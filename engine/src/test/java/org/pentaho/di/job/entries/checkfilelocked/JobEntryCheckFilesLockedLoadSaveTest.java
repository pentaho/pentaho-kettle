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

package org.pentaho.di.job.entries.checkfilelocked;


import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.util.List;

import java.util.Arrays;

public class JobEntryCheckFilesLockedLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryCheckFilesLocked> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Override protected Class<JobEntryCheckFilesLocked> getJobEntryClass() {
    return JobEntryCheckFilesLocked.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return Arrays.asList( "argFromPrevious", "includeSubfolders" );
  }
}
