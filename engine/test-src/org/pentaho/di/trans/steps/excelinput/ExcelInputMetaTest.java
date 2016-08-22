/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.excelinput;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class ExcelInputMetaTest {
  LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
  }

  @Test
  public void testSerialization() throws KettleException {
    List<String> attributes =
        Arrays.asList( "fileName", "fileMask", "excludeFileMask", "fileRequired", "includeSubFolders", "field",
            "sheetName", "startRow", "startColumn", "spreadSheetType", "fileField", "sheetField", "sheetRowNumberField",
            "rowNumberField", "shortFileFieldName", "extensionFieldName", "pathFieldName", "sizeFieldName",
            "hiddenFieldName", "lastModificationTimeFieldName", "uriNameFieldName", "rootUriNameFieldName" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "excludeFileMask", "getExludeFileMask" );
        put( "shortFileFieldName", "getShortFileNameField" );
        put( "extensionFieldName", "getExtensionField" );
        put( "pathFieldName", "getPathField" );
        put( "sizeFieldName", "getSizeField" );
        put( "hiddenFieldName", "isHiddenField" );
        put( "lastModificationTimeFieldName", "getLastModificationDateField" );
        put( "uriNameFieldName", "getUriField" );
        put( "rootUriNameFieldName", "getRootUriField" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "shortFileFieldName", "setShortFileNameField" );
        put( "extensionFieldName", "setExtensionField" );
        put( "pathFieldName", "setPathField" );
        put( "sizeFieldName", "setSizeField" );
        put( "hiddenFieldName", "setIsHiddenField" );
        put( "lastModificationTimeFieldName", "setLastModificationDateField" );
        put( "uriNameFieldName", "setUriField" );
        put( "rootUriNameFieldName", "setRootUriField" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fileName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "sheetName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "excludeFileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileRequired", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "includeSubFolders", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "field",
        new ArrayLoadSaveValidator<ExcelInputField>( new ExcelInputFieldLoadSaveValidator(), 5 ) );
    attrValidatorMap.put( "spreadSheetType", new SpreadSheetTypeFieldLoadSaveValidator() );
    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeValidatorMap.put( int[].class.getCanonicalName(), new PrimitiveIntArrayLoadSaveValidator(
        new IntLoadSaveValidator(), 5 ) );

    loadSaveTester =
        new LoadSaveTester( ExcelInputMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );

    loadSaveTester.testSerialization();
  }

  public class ExcelInputFieldLoadSaveValidator implements FieldLoadSaveValidator<ExcelInputField> {
    final Random rand = new Random();

    @Override
    public ExcelInputField getTestObject() {
      ExcelInputField rtn = new ExcelInputField( );
      rtn.setCurrencySymbol( UUID.randomUUID().toString() );
      rtn.setDecimalSymbol( UUID.randomUUID().toString() );
      rtn.setFormat( UUID.randomUUID().toString() );
      rtn.setGroupSymbol( UUID.randomUUID().toString() );
      rtn.setName( UUID.randomUUID().toString() );
      rtn.setTrimType( rand.nextInt( 4 ) );
      rtn.setPrecision( rand.nextInt( 9 ) );
      rtn.setRepeated( rand.nextBoolean() );
      rtn.setLength( rand.nextInt( 50 ) );
      rtn.setType( rand.nextInt( 5 ) + 1 );
      return rtn;
    }

    @Override
    public boolean validateTestObject( ExcelInputField testObject, Object actual ) {
      if ( !( actual instanceof ExcelInputField ) ) {
        return false;
      }
      ExcelInputField another = (ExcelInputField) actual;
      return new EqualsBuilder()
        .append( testObject.getName(), another.getName() )
        .append( testObject.getType(), another.getType() )
        .append( testObject.getLength(), another.getLength() )
        .append( testObject.getFormat(), another.getFormat() )
        .append( testObject.getTrimType(), another.getTrimType() )
        .append( testObject.getPrecision(), another.getPrecision() )
        .append( testObject.getCurrencySymbol(), another.getCurrencySymbol() )
        .append( testObject.getDecimalSymbol(), another.getDecimalSymbol() )
        .append( testObject.getGroupSymbol(), another.getGroupSymbol() )
        .append( testObject.isRepeated(), another.isRepeated() )
      .isEquals();
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

  @Test
  public void testRepoRoundTripWithNullAttr() throws KettleException {
    List<String> attributes =
      Arrays.asList( "fileName", "fileMask", "excludeFileMask", "fileRequired", "includeSubFolders", "field",
        "sheetName", "startRow", "startColumn", "spreadSheetType", "fileField", "sheetField", "sheetRowNumberField",
        "rowNumberField", "shortFileFieldName", "extensionFieldName", "pathFieldName", "sizeFieldName",
        "hiddenFieldName", "lastModificationTimeFieldName", "uriNameFieldName", "rootUriNameFieldName" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "excludeFileMask", "getExludeFileMask" );
        put( "shortFileFieldName", "getShortFileNameField" );
        put( "extensionFieldName", "getExtensionField" );
        put( "pathFieldName", "getPathField" );
        put( "sizeFieldName", "getSizeField" );
        put( "hiddenFieldName", "isHiddenField" );
        put( "lastModificationTimeFieldName", "getLastModificationDateField" );
        put( "uriNameFieldName", "getUriField" );
        put( "rootUriNameFieldName", "getRootUriField" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "shortFileFieldName", "setShortFileNameField" );
        put( "extensionFieldName", "setExtensionField" );
        put( "pathFieldName", "setPathField" );
        put( "sizeFieldName", "setSizeField" );
        put( "hiddenFieldName", "setIsHiddenField" );
        put( "lastModificationTimeFieldName", "setLastModificationDateField" );
        put( "uriNameFieldName", "setUriField" );
        put( "rootUriNameFieldName", "setRootUriField" );
      }
    };

    FieldLoadSaveValidator<String[]> nullStringArrayLoadSaveValidator = new NullStringArrayLoadSaveValidator();

    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 1 );

    NullNameExcelInputArrayFieldLoadSaveValidator nullNameExcelInputArrayFieldLoadSaveValidator =
      new NullNameExcelInputArrayFieldLoadSaveValidator();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fileName", nullStringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "excludeFileMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fileRequired", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "includeSubFolders", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "sheetName", nullStringArrayLoadSaveValidator );
    attrValidatorMap.put( "field", nullNameExcelInputArrayFieldLoadSaveValidator );
    attrValidatorMap.put( "spreadSheetType", new SpreadSheetTypeFieldLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeValidatorMap.put( int[].class.getCanonicalName(), new PrimitiveIntArrayLoadSaveValidator(
      new IntLoadSaveValidator(), 1 ) );

    loadSaveTester = new LoadSaveTester( ExcelInputMeta.class, attributes, getterMap,
      setterMap, attrValidatorMap, typeValidatorMap );

    loadSaveTester.testRepoRoundTrip();
  }

  public class NullStringArrayLoadSaveValidator implements FieldLoadSaveValidator<String[]> {
    @Override public String[] getTestObject() {
      return new String[] { null };
    }

    @Override public boolean validateTestObject( String[] original, Object actualObject ) {
      String[] actual = actualObject instanceof String[] ? ( (String[]) actualObject ) : null;
      if ( actual == null || actual.length != 1 || original.length != 1 || original[ 0 ] != null ) {
        return false;
      }
      return StringUtils.EMPTY.equals( actual[ 0 ] );
    }
  }

  public class NullNameExcelInputArrayFieldLoadSaveValidator implements FieldLoadSaveValidator<ExcelInputField[]> {
    @Override public ExcelInputField[] getTestObject() {
      ExcelInputField rtn = new ExcelInputField();
      rtn.setName( null );
      return new ExcelInputField[] { rtn };
    }

    @Override public boolean validateTestObject( ExcelInputField[] original, Object actualObject ) {
      ExcelInputField[] actual =
        actualObject instanceof ExcelInputField[] ? ( (ExcelInputField[]) actualObject ) : null;
      if ( actual == null || actual.length != 1 || original.length != 1 || original[ 0 ].getName() != null ) {
        return false;
      }
      return StringUtils.EMPTY.equals( actual[ 0 ].getName() );
    }
  }

  // Note - removed cloneTest as it's now covered by the load/save tester.
}
