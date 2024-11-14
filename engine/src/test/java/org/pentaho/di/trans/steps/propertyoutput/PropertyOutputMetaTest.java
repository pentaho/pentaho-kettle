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


package org.pentaho.di.trans.steps.propertyoutput;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class PropertyOutputMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Test
  public void testSerialization() throws KettleException {
    List<String> attributes = Arrays.asList( "KeyField", "ValueField", "Comment", "FileNameInField",
      "FileNameField", "FileName", "Extension", "StepNrInFilename",
      //
      // Note - "partNrInFilename" not included above because while it seems to be serialized/deserialized in the meta,
      // there are no getters/setters and it's a private variable. Also, it's not included in the dialog. So it is
      // always serialized/deserialized as "false" (N).
      // MB - 5/2016
      "DateInFilename", "TimeInFilename", "CreateParentFolder", "AddToResult", "Append" );

    LoadSaveTester<PropertyOutputMeta> tester = new LoadSaveTester<PropertyOutputMeta>(
        PropertyOutputMeta.class, attributes );

    tester.testSerialization();
  }
}
