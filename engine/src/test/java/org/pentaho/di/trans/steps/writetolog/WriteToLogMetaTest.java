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
package org.pentaho.di.trans.steps.writetolog;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

public class WriteToLogMetaTest implements InitializerInterface<StepMetaInterface> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  LoadSaveTester loadSaveTester;
  Class<WriteToLogMetaSymmetric> testMetaClass = WriteToLogMetaSymmetric.class;

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "displayHeader", "limitRows", "limitRowsNumber", "logmessage", "loglevel", "fieldName" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "displayHeader", "isdisplayHeader" );
        put( "limitRows", "isLimitRows" );
        put( "limitRowsNumber", "getLimitRowsNumber" );
        put( "logmessage", "getLogMessage" );
        put( "loglevel", "getLogLevelString" );
        put( "fieldName", "getFieldName" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "displayHeader", "setdisplayHeader" );
        put( "limitRows", "setLimitRows" );
        put( "limitRowsNumber", "setLimitRowsNumber" );
        put( "logmessage", "setLogMessage" );
        put( "loglevel", "setLogLevelString" );
        put( "fieldName", "setFieldName" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fieldName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "loglevel", new LogLevelLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof WriteToLogMeta ) {
      ( (WriteToLogMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  public class LogLevelLoadSaveValidator implements FieldLoadSaveValidator<String> {
    final Random rand = new Random();

    @Override
    public String getTestObject() {
      int idx = rand.nextInt( ( WriteToLogMeta.logLevelCodes.length ) );
      return WriteToLogMeta.logLevelCodes[idx];
    }

    @Override
    public boolean validateTestObject( String testObject, Object actual ) {
      if ( !( actual instanceof String ) ) {
        return false;
      }
      String actualInput = (String) actual;
      return ( testObject.equals( actualInput ) );
    }
  }
}
