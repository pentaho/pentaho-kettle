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

package org.pentaho.di.trans.steps.rssinput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class RssInputMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<RssInputMeta> testMetaClass = RssInputMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "includeRowNumber", "rowNumberField", "includeUrl", "urlField", "rowLimit", "readfrom",
            "urlInField", "urlFieldname", "url", "inputFields" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "includeRowNumber", "includeRowNumber" );
        put( "rowNumberField", "getRowNumberField" );
        put( "includeUrl", "includeUrl" );
        put( "urlField", "geturlField" );
        put( "rowLimit", "getRowLimit" );
        put( "readfrom", "getReadFrom" );
        put( "urlInField", "urlInField" );
        put( "urlFieldname", "getUrlFieldname" );
        put( "url", "getUrl" );
        put( "inputFields", "getInputFields" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "includeRowNumber", "setIncludeRowNumber" );
        put( "rowNumberField", "setRowNumberField" );
        put( "includeUrl", "setIncludeUrl" );
        put( "urlField", "seturlField" );
        put( "rowLimit", "setRowLimit" );
        put( "readfrom", "setReadFrom" );
        put( "urlInField", "seturlInField" );
        put( "urlFieldname", "setUrlFieldname" );
        put( "url", "setUrl" );
        put( "inputFields", "setInputFields" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );
    FieldLoadSaveValidator<RssInputField[]> rssInputFieldArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<RssInputField>( new RssInputFieldLoadSaveValidator(), 5 );


    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "url", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "inputFields", rssInputFieldArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof RssInputMeta ) {
      ( (RssInputMeta) someMeta ).allocate( 5, 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }


  //RssInputField
  public class RssInputFieldLoadSaveValidator implements FieldLoadSaveValidator<RssInputField> {
    final Random rand = new Random();
    @Override
    public RssInputField getTestObject() {
      RssInputField rtn = new RssInputField();
      rtn.setCurrencySymbol( UUID.randomUUID().toString() );
      rtn.setDecimalSymbol( UUID.randomUUID().toString() );
      rtn.setFormat( UUID.randomUUID().toString() );
      rtn.setGroupSymbol( UUID.randomUUID().toString() );
      rtn.setName( UUID.randomUUID().toString() );
      rtn.setTrimType( rand.nextInt( ValueMetaBase.trimTypeCode.length ) );
      rtn.setPrecision( rand.nextInt( 9 ) );
      rtn.setRepeated( rand.nextBoolean() );
      rtn.setLength( rand.nextInt( 50 ) );
      rtn.setType( rand.nextInt( ValueMetaBase.typeCodes.length ) );
      rtn.setColumn( rand.nextInt( RssInputField.ColumnCode.length ) );
      return rtn;
    }

    @Override
    public boolean validateTestObject( RssInputField testObject, Object actual ) {
      if ( !( actual instanceof RssInputField ) ) {
        return false;
      }
      RssInputField actualInput = (RssInputField) actual;
      boolean tst1 = ( testObject.getXML().equals( actualInput.getXML() ) );
      RssInputField aClone = (RssInputField) ( (RssInputField) actual ).clone();
      boolean tst2 = ( testObject.getXML().equals( aClone.getXML() ) );
      return ( tst1 && tst2 );
    }
  }
}
