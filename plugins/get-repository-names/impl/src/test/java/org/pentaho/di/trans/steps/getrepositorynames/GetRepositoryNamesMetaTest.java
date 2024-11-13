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


package org.pentaho.di.trans.steps.getrepositorynames;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class GetRepositoryNamesMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
      Arrays.asList( "object_type", "rownum", "rownum_field",
        "directory", "name_mask", "exclude_name_mask", "include_subfolders" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "object_type", "getObjectTypeSelection" );
    getterMap.put( "rownum", "isIncludeRowNumber" );
    getterMap.put( "rownum_field", "getRowNumberField" );
    getterMap.put( "name_mask", "getNameMask" );
    getterMap.put( "exclude_name_mask", "getExcludeNameMask" );
    getterMap.put( "include_subfolders", "getIncludeSubFolders" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "object_type", "setObjectTypeSelection" );
    setterMap.put( "rownum", "setIncludeRowNumber" );
    setterMap.put( "rownum_field", "setRowNumberField" );
    setterMap.put( "exclude_name_mask", "setExcludeNameMask" );
    setterMap.put( "include_subfolders", "setIncludeSubFolders" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    fieldLoadSaveValidatorTypeMap.put( String[].class.getCanonicalName(),
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 50 ) );
    fieldLoadSaveValidatorTypeMap.put( boolean[].class.getCanonicalName(),
      new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 50 ) );
    fieldLoadSaveValidatorTypeMap.put( ObjectTypeSelection.class.getCanonicalName(),
      new ObjectTypeSelectionLoadSaveValidator() );

    LoadSaveTester tester = new LoadSaveTester( GetRepositoryNamesMeta.class, attributes, getterMap, setterMap,
      new HashMap<String, FieldLoadSaveValidator<?>>(), fieldLoadSaveValidatorTypeMap );

    tester.testSerialization();
  }

  public class ObjectTypeSelectionLoadSaveValidator implements FieldLoadSaveValidator<ObjectTypeSelection> {

    final Random rnd = new Random();

    @Override
    public ObjectTypeSelection getTestObject() {
      int index = rnd.nextInt( ObjectTypeSelection.values().length );
      return ObjectTypeSelection.values()[index];
    }

    @Override
    public boolean validateTestObject( ObjectTypeSelection testObject, Object actual ) {
      return testObject.equals( actual );
    }

  }

}
