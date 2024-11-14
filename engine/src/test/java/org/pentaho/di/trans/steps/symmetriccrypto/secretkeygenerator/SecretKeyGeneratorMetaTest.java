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

package org.pentaho.di.trans.steps.symmetriccrypto.secretkeygenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class SecretKeyGeneratorMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<SecretKeyGeneratorMeta> testMetaClass = SecretKeyGeneratorMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "algorithm", "scheme", "secretKeyLength", "secretKeyCount", "secretKeyFieldName", "secretKeyLengthFieldName",
            "algorithmFieldName", "outputKeyInBinary" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "algorithm", "getAlgorithm" );
        put( "scheme", "getScheme" );
        put( "secretKeyLength", "getSecretKeyLength" );
        put( "secretKeyCount", "getSecretKeyCount" );
        put( "secretKeyFieldName", "getSecretKeyFieldName" );
        put( "secretKeyLengthFieldName", "getSecretKeyLengthFieldName" );
        put( "algorithmFieldName", "getAlgorithmFieldName" );
        put( "outputKeyInBinary", "isOutputKeyInBinary" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "algorithm", "setFieldAlgorithm" );
        put( "scheme", "setScheme" );
        put( "secretKeyLength", "setSecretKeyLength" );
        put( "secretKeyCount", "setSecretKeyCount" );
        put( "secretKeyFieldName", "setSecretKeyFieldName" );
        put( "secretKeyLengthFieldName", "setSecretKeyLengthFieldName" );
        put( "algorithmFieldName", "setAlgorithmFieldName" );
        put( "outputKeyInBinary", "setOutputKeyInBinary" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );


    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "algorithm", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "scheme", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "secretKeyLength", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "secretKeyCount", stringArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof SecretKeyGeneratorMeta ) {
      ( (SecretKeyGeneratorMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }
}
