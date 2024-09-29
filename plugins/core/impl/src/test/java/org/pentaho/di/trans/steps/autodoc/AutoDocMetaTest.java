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

package org.pentaho.di.trans.steps.autodoc;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.EnumLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Khayrutdinov
 */
public class AutoDocMetaTest {

  private LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {

    List<String> attributes =
      Arrays.asList( "filenameField", "fileTypeField", "targetFilename", "includingName", "includingDescription",
        "includingExtendedDescription", "includingCreated", "includingModified",
        "includingImage", "includingLoggingConfiguration", "includingLastExecutionResult",
        "includingImageAreaList", "outputType" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "outputType", new EnumLoadSaveValidator<KettleReportBuilder.OutputType>( KettleReportBuilder.OutputType.PDF ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
      new LoadSaveTester( AutoDocMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }
}
