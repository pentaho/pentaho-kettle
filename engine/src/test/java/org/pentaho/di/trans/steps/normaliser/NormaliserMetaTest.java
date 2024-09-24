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
package org.pentaho.di.trans.steps.normaliser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.builder.EqualsBuilder;
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

public class NormaliserMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<NormaliserMeta> testMetaClass = NormaliserMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "typeField", "normaliserFields" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "normaliserFields",
        new ArrayLoadSaveValidator<NormaliserMeta.NormaliserField>( new NormaliserFieldLoadSaveValidator(), 5 ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof NormaliserMeta ) {
      ( (NormaliserMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  // NormaliserFieldLoadSaveValidator
  public class NormaliserFieldLoadSaveValidator implements FieldLoadSaveValidator<NormaliserMeta.NormaliserField> {
    final Random rand = new Random();
    @Override
    public NormaliserMeta.NormaliserField getTestObject() {
      NormaliserMeta.NormaliserField rtn = new NormaliserMeta.NormaliserField();
      rtn.setName( UUID.randomUUID().toString() );
      rtn.setNorm( UUID.randomUUID().toString() );
      rtn.setValue( UUID.randomUUID().toString() );
      return rtn;
    }

    @Override
    public boolean validateTestObject( NormaliserMeta.NormaliserField testObject, Object actual ) {
      if ( !( actual instanceof NormaliserMeta.NormaliserField ) ) {
        return false;
      }
      NormaliserMeta.NormaliserField another = (NormaliserMeta.NormaliserField) actual;
      return new EqualsBuilder()
        .append( testObject.getName(), another.getName() )
        .append( testObject.getNorm(), another.getNorm() )
        .append( testObject.getValue(), another.getValue() )
      .isEquals();
    }
  }

}
