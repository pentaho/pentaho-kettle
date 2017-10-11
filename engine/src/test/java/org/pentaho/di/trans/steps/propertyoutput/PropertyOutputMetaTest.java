/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.propertyoutput;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class PropertyOutputMetaTest {
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
