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

package org.pentaho.di.trans.steps.gpload;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;

import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class GPLoadMetaTest {
  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
        Arrays.asList( "localHosts", "localhostPort", "schemaName", "tableName", "errorTableName", "gploadPath", "controlFile", "dataFile",
            "logFile", "nullAs", "databaseMeta", "fieldTable", "fieldStream", "matchColumn", "updateColumn", "dateMask", "maxErrors",
            "loadMethod", "loadAction", "encoding", "eraseFiles", "encloseNumbers", "delimiter", "updateCondition" );

    Map<String, String> getterMap = new HashMap<>();
    Map<String, String> setterMap = new HashMap<>();

    for ( String attribute : attributes ) {

      // Most attributes can be set by convention apart from isEraseFiles (is, not get) and setMatchColumns (pluralised)

      if ( attribute.equals( "eraseFiles" ) ) {
        getterMap.put( "eraseFiles", "isEraseFiles" );
      } else {
        getterMap.put( attribute, "get" + attribute.substring( 0, 1 ).toUpperCase() + attribute.substring( 1 ) );
      }

      if ( attribute.equals( "matchColumn" ) ) {
        setterMap.put( "matchColumn", "setMatchColumns" );
      } else {
        setterMap.put( attribute, "set" + attribute.substring( 0, 1 ).toUpperCase() + attribute.substring( 1 ) );
      }

    }

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap = new HashMap<>();
    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap = new HashMap<>();

    fieldLoadSaveValidatorAttributeMap.put( "dateMask", new ArrayLoadSaveValidator<>( new DateMaskLoadSaveValidator(), 10 ) );
    fieldLoadSaveValidatorTypeMap.put( DatabaseMeta.class.getCanonicalName(), new DatabaseMetaFieldLoadSaveValidator() );
    fieldLoadSaveValidatorTypeMap.put( String[].class.getCanonicalName(), new ArrayLoadSaveValidator<>( new StringLoadSaveValidator(), 10 ) );
    fieldLoadSaveValidatorTypeMap.put( boolean[].class.getCanonicalName(), new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 10 ) );

    LoadSaveTester loadSaveTester = new LoadSaveTester( GPLoadMeta.class, attributes, getterMap, setterMap,
        fieldLoadSaveValidatorAttributeMap, fieldLoadSaveValidatorTypeMap );

    loadSaveTester.testRepoRoundTrip();
    loadSaveTester.testXmlRoundTrip();
  }

  public class DatabaseMetaFieldLoadSaveValidator implements
      FieldLoadSaveValidator<DatabaseMeta> {
    @Override
    public DatabaseMeta getTestObject() {
      return null;
    }

    @Override
    public boolean validateTestObject( DatabaseMeta testObject, Object actual ) {
      return testObject == null;
    }
  }

  public class DateMaskLoadSaveValidator implements FieldLoadSaveValidator<String> {
    public DateMaskLoadSaveValidator() {
    }

    public String getTestObject() {
      return Math.random() < 0.5 ? GPLoadMeta.DATE_MASK_DATE : GPLoadMeta.DATE_MASK_DATETIME;
    }

    public boolean validateTestObject( String test, Object actual ) {
      return test.equals( actual );
    }
  }

  @Test
  public void testPDI16559() throws Exception {
    StepMockHelper<GPLoadMeta, GPLoadData> mockHelper =
        new StepMockHelper<GPLoadMeta, GPLoadData>( "gPLoad", GPLoadMeta.class, GPLoadData.class );

    GPLoadMeta gPLoad = new GPLoadMeta();
    gPLoad.setLocalHosts( new String[] { "http://foo.bar", "http://bar.foo" } );
    gPLoad.setFieldTable( new String[] { "field1", "field2", "field3", "field4", "field5" } );
    gPLoad.setFieldStream( new String[] { "fstr1", "fstr2", "fstr3"} );
    gPLoad.setDateMask( new String[] { "dMask1", "dMask2" } );
    gPLoad.setMatchColumns( new boolean[] { false, true } );
    gPLoad.setUpdateColumn( new boolean[] { true, false, true } );

    try {
      String badXml = gPLoad.getXML();
      Assert.fail( "Before calling afterInjectionSynchronization, should have thrown an ArrayIndexOOB" );
    } catch ( Exception expected ) {
      // Do Nothing
    }
    gPLoad.afterInjectionSynchronization();
    //run without a exception
    String ktrXml = gPLoad.getXML();

    int targetSz = gPLoad.getFieldTable().length;

    Assert.assertEquals( targetSz, gPLoad.getFieldStream().length );
    Assert.assertEquals( targetSz, gPLoad.getDateMask().length );
    Assert.assertEquals( targetSz, gPLoad.getMatchColumn().length );
    Assert.assertEquals( targetSz, gPLoad.getUpdateColumn().length );

  }
}
