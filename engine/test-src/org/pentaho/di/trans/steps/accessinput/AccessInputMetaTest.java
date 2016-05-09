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

package org.pentaho.di.trans.steps.accessinput;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class AccessInputMetaTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes = Arrays.asList( "includeFilename", "filenameField", "includeTablename",
      "dynamicFilenameField", "tablenameField", "includeRowNumber", "addResultFile", "fileField",
      "rowNumberField", "resetRowNumber", "tableName", "fileName", "fileMask", "excludeFileMask",
      "fileRequired", "includeSubFolders", "inputFields", "rowLimit", "shortFileNameField",
      "pathField", "hiddenField", "lastModificationDateField", "uriField", "rootUriField",
      "extensionField", "sizeField" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> typeValidators = new HashMap<String, FieldLoadSaveValidator<?>>();
    Map<String, FieldLoadSaveValidator<?>> fieldValidators = new HashMap<String, FieldLoadSaveValidator<?>>();
    fieldValidators.put( "fileName", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 50 ) );
    fieldValidators.put( "fileMask", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 50 ) );
    fieldValidators.put( "excludeFileMask", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 50 ) );
    fieldValidators.put( "fileRequired",
      new ArrayLoadSaveValidator<String>( new FileRequiredLoadSaveValidator(), 50 ) );
    fieldValidators.put( "includeSubFolders", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 50 ) );
    fieldValidators.put( "inputFields",
      new ArrayLoadSaveValidator<AccessInputField>( new AccessInputFieldLoadSaveValidator(), 100 ) );

    LoadSaveTester loadSaveTester =
      new LoadSaveTester( AccessInputMeta.class, attributes, getterMap, setterMap, fieldValidators, typeValidators );
    loadSaveTester.testXmlRoundTrip();
    loadSaveTester.testRepoRoundTrip();
  }

  public class FileRequiredLoadSaveValidator implements FieldLoadSaveValidator<String> {
    final Random rand = new Random();
    @Override
    public String getTestObject() {
      return AccessInputMeta.RequiredFilesDesc[rand.nextInt( AccessInputMeta.RequiredFilesDesc.length )];
    }

    @Override
    public boolean validateTestObject( String testObject, Object actual ) {
      return testObject.equals( actual );
    }
  }

  public class AccessInputFieldLoadSaveValidator implements FieldLoadSaveValidator<AccessInputField> {
    final Random rand = new Random();
    @Override
    public AccessInputField getTestObject() {
      AccessInputField field = new AccessInputField();
      field.setName( UUID.randomUUID().toString() );
      field.setColumn( UUID.randomUUID().toString() );
      field.setType( rand.nextInt( ValueMetaFactory.getAllValueMetaNames().length ) );
      field.setFormat( UUID.randomUUID().toString() );
      field.setLength( rand.nextInt() );
      field.setPrecision( rand.nextInt() );
      field.setCurrencySymbol( UUID.randomUUID().toString() );
      field.setDecimalSymbol( UUID.randomUUID().toString() );
      field.setGroupSymbol( UUID.randomUUID().toString() );
      field.setTrimType( rand.nextInt( AccessInputField.trimTypeCode.length ) );
      field.setRepeated( rand.nextBoolean() );
      return field;
    }

    @Override
    public boolean validateTestObject( AccessInputField testObject, Object actual ) {
      if ( !( actual instanceof AccessInputField ) ) {
        return false;
      }
      AccessInputField result = (AccessInputField) actual;
      if ( !testObject.getName().equals( result.getName() ) ) {
        return false;
      }
      if ( !testObject.getColumn().equals( result.getColumn() ) ) {
        return false;
      }
      if ( testObject.getType() != result.getType() ) {
        return false;
      }
      if ( !testObject.getFormat().equals( result.getFormat() ) ) {
        return false;
      }
      if ( testObject.getLength() != result.getLength() ) {
        return false;
      }
      if ( testObject.getPrecision() != result.getPrecision() ) {
        return false;
      }
      if ( !testObject.getCurrencySymbol().equals( result.getCurrencySymbol() ) ) {
        return false;
      }
      if ( !testObject.getDecimalSymbol().equals( result.getDecimalSymbol() ) ) {
        return false;
      }
      if ( !testObject.getGroupSymbol().equals( result.getGroupSymbol() ) ) {
        return false;
      }
      if ( testObject.getTrimType() != result.getTrimType() ) {
        return false;
      }
      if ( testObject.isRepeated() != result.isRepeated() ) {
        return false;
      }
      return true;
    }
  }
}
