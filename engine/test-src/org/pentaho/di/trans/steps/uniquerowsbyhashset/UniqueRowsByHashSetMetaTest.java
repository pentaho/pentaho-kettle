package org.pentaho.di.trans.steps.uniquerowsbyhashset;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class UniqueRowsByHashSetMetaTest {

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
      Arrays.asList( "store_values", "reject_duplicate_row", "error_description", "name" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "store_values", "getStoreValues" );
    getterMap.put( "reject_duplicate_row", "isRejectDuplicateRow" );
    getterMap.put( "error_description", "getErrorDescription" );
    getterMap.put( "name", "getCompareFields" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "store_values", "setStoreValues" );
    setterMap.put( "reject_duplicate_row", "setRejectDuplicateRow" );
    setterMap.put( "error_description", "setErrorDescription" );
    setterMap.put( "name", "setCompareFields" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    //Arrays need to be consistent length
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 );

    fieldLoadSaveValidatorAttributeMap.put( "name", stringArrayLoadSaveValidator );

    LoadSaveTester loadSaveTester =
      new LoadSaveTester( UniqueRowsByHashSetMeta.class, attributes, getterMap, setterMap,
          fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testRepoRoundTrip();
    loadSaveTester.testXmlRoundTrip();
  }
}
