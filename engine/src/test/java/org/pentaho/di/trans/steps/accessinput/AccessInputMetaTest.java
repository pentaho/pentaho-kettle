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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class AccessInputMetaTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Test
  public void testClone() throws KettleException {
    AccessInputMeta meta = new AccessInputMeta();
    meta.allocate( 3, 2 );
    meta.setFileName( new String[] { "file1", "file2", "file3" } );
    meta.setFileMask( new String[] { "mask1", "mask2", "mask3" } );
    meta.setExcludeFileMask( new String[] { "exmask1", "exmask2", "exmask3" } );
    meta.setFileRequired( new String[] { "false", "true", "false" } );
    meta.setIncludeSubFolders( new String[] { "true", "false", "true" } );
    AccessInputField f1 = new AccessInputField( "field1" );
    AccessInputField f2 = new AccessInputField( "field2" );
    meta.setInputFields( new AccessInputField[] { f1, f2 } );
    // scalars should be cloned using super.clone() - makes sure they're calling super.clone()
    meta.setFilenameField( "aFileNameField" );
    AccessInputMeta aClone = (AccessInputMeta) meta.clone();
    assertFalse( aClone == meta ); // makes sure they're not the same object
    assertTrue( Arrays.equals( aClone.getFileName(), meta.getFileName() ) );
    assertTrue( Arrays.equals( aClone.getFileMask(), meta.getFileMask() ) );
    assertTrue( Arrays.equals( aClone.getExcludeFileMask(), meta.getExcludeFileMask() ) );
    assertTrue( Arrays.equals( aClone.getFileRequired(), meta.getFileRequired() ) );
    assertTrue( Arrays.equals( aClone.getIncludeSubFolders(), meta.getIncludeSubFolders() ) );
    AccessInputField[] clFields = aClone.getInputFields();
    assertEquals( f1.getName(), clFields[0].getName() );
    assertEquals( f2.getName(), clFields[1].getName() );
    assertEquals( meta.getFilenameField(), aClone.getFilenameField() );
    assertEquals( meta.getXML(), aClone.getXML() );
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

    FieldLoadSaveValidator<?> stringArrayValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 50 );
    FieldLoadSaveValidator<?> fileRequiredArrayValidator =
      new ArrayLoadSaveValidator<String>( new FileRequiredLoadSaveValidator(), 50 );
    FieldLoadSaveValidator<?> inputFieldArrayValidator =
      new ArrayLoadSaveValidator<AccessInputField>( new AccessInputFieldLoadSaveValidator(), 100 );

    Map<String, FieldLoadSaveValidator<?>> typeValidators = new HashMap<String, FieldLoadSaveValidator<?>>();
    Map<String, FieldLoadSaveValidator<?>> fieldValidators = new HashMap<String, FieldLoadSaveValidator<?>>();
    fieldValidators.put( "fileName", stringArrayValidator );
    fieldValidators.put( "fileMask", stringArrayValidator );
    fieldValidators.put( "excludeFileMask", stringArrayValidator );
    fieldValidators.put( "fileRequired", fileRequiredArrayValidator );
    fieldValidators.put( "includeSubFolders", stringArrayValidator );
    fieldValidators.put( "inputFields", inputFieldArrayValidator );

    LoadSaveTester<AccessInputMeta> loadSaveTester = new LoadSaveTester<AccessInputMeta>(
      AccessInputMeta.class, attributes, getterMap, setterMap, fieldValidators, typeValidators );

    loadSaveTester.testSerialization();
  }

  public static class FileRequiredLoadSaveValidator implements FieldLoadSaveValidator<String> {
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

  public static class AccessInputFieldLoadSaveValidator implements FieldLoadSaveValidator<AccessInputField> {
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
