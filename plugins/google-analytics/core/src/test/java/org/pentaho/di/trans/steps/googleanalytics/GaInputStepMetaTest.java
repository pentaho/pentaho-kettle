/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.googleanalytics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

/**
 * @author Andrey Khayrutdinov
 */
public class GaInputStepMetaTest {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void testSerialization() throws KettleException {
    List<String> attributes = Arrays.asList( "OAuthServiceAccount", "GaAppName", "OAuthKeyFile", "GaProfileName",
      "GaProfileTableId", "GaCustomTableId", "UseCustomTableId", "StartDate", "EndDate", "Dimensions", "Metrics",
      "Filters", "Sort", "UseSegment", "UseCustomSegment", "CustomSegment", "SegmentId", "SegmentName",
      "SamplingLevel", "RowLimit", "FeedFieldType", "FeedField", "OutputField", "OutputType", "ConversionMask" );

    Map<String, FieldLoadSaveValidator<?>> attributeMap = new HashMap<>();
    Map<String, FieldLoadSaveValidator<?>> typeMap = new HashMap<>();
    int maxValueCount = ValueMetaFactory.getAllValueMetaNames().length;

    attributeMap.put( "FeedField", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 ) );
    attributeMap.put( "FeedFieldType", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 ) );
    attributeMap.put( "OutputField", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 ) );
    attributeMap.put( "OutputType",
      new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( maxValueCount ), 25 ) );
    attributeMap.put( "ConversionMask", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 ) );

    LoadSaveTester<GaInputStepMeta> tester = new LoadSaveTester<>( GaInputStepMeta.class, attributes,
      new HashMap<String, String>(), new HashMap<String, String>(), attributeMap, typeMap );

    tester.testSerialization();
  }
}
