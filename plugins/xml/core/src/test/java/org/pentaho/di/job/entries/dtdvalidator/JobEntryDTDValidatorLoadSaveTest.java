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

package org.pentaho.di.job.entries.dtdvalidator;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryDTDValidatorLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryDTDValidator> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryDTDValidator> getJobEntryClass() {
    return JobEntryDTDValidator.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return asList( "xmlfilename", "dtdfilename", "dtdintern" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap( "xmlfilename", "getxmlFilename", "dtdfilename", "getdtdFilename", "dtdintern", "getDTDIntern" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap( "xmlfilename", "setxmlFilename", "dtdfilename", "setdtdFilename", "dtdintern", "setDTDIntern" );
  }
}
