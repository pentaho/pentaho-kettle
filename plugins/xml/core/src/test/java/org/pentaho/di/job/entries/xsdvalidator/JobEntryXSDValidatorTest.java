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

package org.pentaho.di.job.entries.xsdvalidator;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryXSDValidatorTest extends JobEntryLoadSaveTestSupport<JobEntryXSDValidator> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryXSDValidator> getJobEntryClass() {
    return JobEntryXSDValidator.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return asList( "xmlfilename", "xsdfilename" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap( "xmlfilename", "getxmlFilename", "xsdfilename", "getxsdFilename" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap( "xmlfilename", "setxmlFilename", "xsdfilename", "setxsdFilename" );
  }

}
