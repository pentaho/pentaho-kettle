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

package org.pentaho.di.trans.steps.execprocess;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class ExecProcessMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
      Arrays.asList( "ProcessField", "ResultFieldName", "ErrorFieldName", "ExitValueFieldName",
        "FailWhenNotSuccess", "OutputLineDelimiter", "ArgumentsInFields", "ArgumentFieldNames" );

    LoadSaveTester<ExecProcessMeta> loadSaveTester = new LoadSaveTester<>( ExecProcessMeta.class, attributes );

    loadSaveTester.testSerialization();
  }
}
