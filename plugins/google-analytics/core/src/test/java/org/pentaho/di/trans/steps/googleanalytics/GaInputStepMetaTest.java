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

package org.pentaho.di.trans.steps.googleanalytics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

/**
 * @author Andrey Khayrutdinov
 */
public class GaInputStepMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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

  @Test
  public void testPDI16559() throws Exception {
    StepMockHelper<GaInputStepMeta, GaInputStepData> mockHelper =
        new StepMockHelper<GaInputStepMeta, GaInputStepData>( "gaInput", GaInputStepMeta.class, GaInputStepData.class );

    GaInputStepMeta gaInput = new GaInputStepMeta();
    gaInput.setFeedField( new String[] { "field1", "field2", "field3", "field4", "field5" } );
    gaInput.setFeedFieldType( new String[] { "ftype1", "ftype2", "ftype3" } );
    gaInput.setOutputField( new String[] { "out1", "out2", "out3" } );
    gaInput.setOutputType( new int[] { 0, 1, 3, 2 } );
    gaInput.setConversionMask( new String[] { "conv1", "conv2" } );

    try {
      String badXml = gaInput.getXML();
      Assert.fail( "Before calling afterInjectionSynchronization, should have thrown an ArrayIndexOOB" );
    } catch ( Exception expected ) {
      // Do Nothing
    }
    gaInput.afterInjectionSynchronization();
    //run without a exception
    String ktrXml = gaInput.getXML();

    int targetSz = gaInput.getFeedField().length;

    Assert.assertEquals( targetSz, gaInput.getFeedFieldType().length );
    Assert.assertEquals( targetSz, gaInput.getOutputType().length );
    Assert.assertEquals( targetSz, gaInput.getOutputField().length );
    Assert.assertEquals( targetSz, gaInput.getConversionMask().length );

  }

}
