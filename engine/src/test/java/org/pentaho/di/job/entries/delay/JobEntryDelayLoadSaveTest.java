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

package org.pentaho.di.job.entries.delay;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.util.Arrays;
import java.util.List;

public class JobEntryDelayLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryDelay> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Override protected Class<JobEntryDelay> getJobEntryClass() {
    return JobEntryDelay.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return Arrays.asList( "maximumTimeout", "scaleTime" );
  }
}
