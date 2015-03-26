package org.pentaho.di.trans.steps.excelinput;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelInputMetaTest {
  LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {

    List<String> attributes =
        Arrays.asList( "fileName", "fileMask", "excludeFileMask", "fileRequired", "includeSubFolders", "field",
            "sheetName", "startRow", "startColumn", "spreadSheetType", "fileField", "sheetField", "sheetRowNumberField",
            "rowNumberField", "shortFileFieldName", "extensionFieldName", "pathFieldName", "sizeFieldName",
            "hiddenFieldName", "lastModificationTimeFieldName", "uriNameFieldName", "rootUriNameFieldName" );

    Map<String, String> getterMap = new HashMap<String, String>() {{
      put( "excludeFileMask", "getExludeFileMask" );

      put( "shortFileFieldName", "getShortFileNameField" );
      put( "extensionFieldName", "getExtensionField" );
      put( "pathFieldName", "getPathField" );
      put( "sizeFieldName", "getSizeField" );
      put( "hiddenFieldName", "isHiddenField" );
      put( "lastModificationTimeFieldName", "getLastModificationDateField" );
      put( "uriNameFieldName", "getUriField" );
      put( "rootUriNameFieldName", "getRootUriField" );
    }};
    Map<String, String> setterMap = new HashMap<String, String>() {{

      put( "shortFileFieldName", "setShortFileNameField" );
      put( "extensionFieldName", "setExtensionField" );
      put( "pathFieldName", "setPathField" );
      put( "sizeFieldName", "setSizeField" );
      put( "hiddenFieldName", "setIsHiddenField" );
      put( "lastModificationTimeFieldName", "setLastModificationDateField" );
      put( "uriNameFieldName", "setUriField" );
      put( "rootUriNameFieldName", "setRootUriField" );
    }};
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 1 );
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fileName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "sheetName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "excludeFileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileRequired", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "includeSubFolders", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "field", new ExcelInputFieldFieldLoadSaveValidator() );
    attrValidatorMap.put( "spreadSheetType", new SpreadSheetTypeFieldLoadSaveValidator() );
    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeValidatorMap.put( int[].class.getCanonicalName(), new PrimitiveIntArrayLoadSaveValidator(
        new IntLoadSaveValidator(), 1 ) );

    loadSaveTester =
        new LoadSaveTester( ExcelInputMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testLoadSaveXML() throws KettleException {
    loadSaveTester.testXmlRoundTrip();
  }

  @Test
  public void testLoadSaveRepo() throws KettleException {
    loadSaveTester.testRepoRoundTrip();
  }

  public class ExcelInputFieldFieldLoadSaveValidator implements FieldLoadSaveValidator<ExcelInputField[]> {

    @Override public ExcelInputField[] getTestObject() {
      return new ExcelInputField[] { };
    }

    @Override public boolean validateTestObject( ExcelInputField[] testObject, Object actual ) {
      return true;
    }
  }

  public class SpreadSheetTypeFieldLoadSaveValidator implements FieldLoadSaveValidator<SpreadSheetType> {
    @Override public SpreadSheetType getTestObject() {
      return SpreadSheetType.POI;
    }

    @Override public boolean validateTestObject( SpreadSheetType testObject, Object actual ) {
      return true;
    }
  }
}
