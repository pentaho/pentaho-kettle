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
package org.pentaho.di.trans.steps.infobrightoutput;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

import com.infobright.etl.model.DataFormat;

public class InfobrightLoaderMetaTest {
  LoadSaveTester loadSaveTester;
  Class<InfobrightLoaderMeta> testMetaClass = InfobrightLoaderMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes = Arrays.asList( "agentPort", "debugFile", "charset", "dataFormat" );

    // Note - "rejectErrors" isn't loaded/saved in InfobrightLoaderMeta, and not in dialog. I
    // conclude it's not used for this purpose, so it's not included in load/save validation.
    // MB - 5/2016

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "charset", new CharsetLoadSaveValidator() );
    attrValidatorMap.put( "dataFormat", new DataFormatLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester = new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  public class CharsetLoadSaveValidator implements FieldLoadSaveValidator<Charset> {
    final Random rand = new Random();
    @Override
    public Charset getTestObject() {
      Charset rtn = null;
      Map<String, Charset> charsets = Charset.availableCharsets();
      int iterations = rand.nextInt( charsets.size() );
      Set<Map.Entry<String, Charset>> chit = charsets.entrySet();
      int i = 0;
      for ( Map.Entry<String, Charset> chEntry : chit ) { // find random Charset
        if ( i == iterations ) {
          rtn = chEntry.getValue();
          break;
        }
        i++;
      }
      return rtn;
    }

    @Override
    public boolean validateTestObject( Charset testObject, Object actual ) {
      if ( !( actual instanceof Charset ) ) {
        return false;
      }
      Charset another = (Charset) actual;
      return ( testObject.compareTo( another ) == 0 );
    }
  }

  public class DataFormatLoadSaveValidator implements FieldLoadSaveValidator<DataFormat> {
    final Random rand = new Random();
    @Override
    public DataFormat getTestObject() {
      DataFormat[] vals = DataFormat.values();
      int dfNum = rand.nextInt( vals.length );
      DataFormat rtn = vals[dfNum];
      return rtn;
    }

    @Override
    public boolean validateTestObject( DataFormat testObject, Object actual ) {
      if ( !( actual instanceof DataFormat ) ) {
        return false;
      }
      DataFormat another = (DataFormat) actual;
      return ( testObject.toString().equals( another.toString() ) );
    }
  }

}
