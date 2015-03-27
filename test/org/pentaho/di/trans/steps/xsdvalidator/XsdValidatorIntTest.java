/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.xsdvalidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.poi.util.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

public class XsdValidatorIntTest {

  private static final String RAMDIR = "ram://" + XsdValidatorIntTest.class.getSimpleName();
  private static FileObject schemaRamFile = null;
  private static FileObject dataRamFile = null;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @AfterClass
  public static void tearDownAfterClass() {
    try {
      if ( schemaRamFile != null && schemaRamFile.exists() ) {
        schemaRamFile.delete();
      }
      if ( dataRamFile != null && dataRamFile.exists() ) {
        dataRamFile.delete();
      }
    } catch ( Exception ignore ) {
      // Ignore
    }
  }

  @Test
  public void testVfsInputFiles() throws FileSystemException {
    testVfsFileTypes( getDataRamFile().getURL().toString(), getSchemaRamFile().getURL().toString(), true );
    testVfsFileTypes( getDataRamFile().getURL().toString(), getSchemaUrlFile().getURL().toString(), true );
    testVfsFileTypes( getDataUrlFile().getURL().toString(), getSchemaRamFile().getURL().toString(), true );
    testVfsFileTypes( getDataUrlFile().getURL().toString(), getSchemaUrlFile().getURL().toString(), true );
  }

  private FileObject getSchemaRamFile() {
    try {
      if ( schemaRamFile != null && schemaRamFile.exists() && schemaRamFile.getContent().getSize() > 0 ) {
        return schemaRamFile;
      }
      schemaRamFile = KettleVFS.getFileObject( RAMDIR + "/schema.xsd" );
      if ( loadRamFile( this.getClass().getResourceAsStream( "schema.xsd" ), schemaRamFile ) ) {
        return schemaRamFile;
      }
    } catch ( Exception e ) {
      return null;
    }
    return null;
  }

  private FileObject getDataRamFile() {
    try {
      if ( dataRamFile != null && dataRamFile.exists() && dataRamFile.getContent().getSize() > 0 ) {
        return dataRamFile;
      }
      dataRamFile = KettleVFS.getFileObject( RAMDIR + "/data.xml" );
      if ( loadRamFile( this.getClass().getResourceAsStream( "data.xml" ), dataRamFile ) ) {
        return dataRamFile;
      }
    } catch ( Exception e ) {
      return null;
    }
    return null;
  }

  private FileObject getSchemaUrlFile() {
    try {
      return KettleVFS.getFileObject( this.getClass().getResource( "schema.xsd" ).toString() );
    } catch ( KettleFileException e ) {
      return null;
    }
  }

  private FileObject getDataUrlFile() {
    try {
      return KettleVFS.getFileObject( this.getClass().getResource( "data.xml" ).toString() );
    } catch ( KettleFileException e ) {
      return null;
    }
  }

  private boolean loadRamFile( InputStream sourceStream, FileObject targetFile ) {
    if ( sourceStream == null || targetFile == null ) {
      return false;
    }
    boolean result = false;
    OutputStream targetStream = null;
    try {
      targetStream = targetFile.getContent().getOutputStream();
      IOUtils.copy( sourceStream, targetStream );
      result = true;
    } catch ( Exception e ) {
      // Ignore, we'll return false anyways
    }
    try {
      sourceStream.close();
      if ( targetStream != null ) {
        targetStream.close();
      }
    } catch ( Exception e ) {
      // Ignore
    }
    return result;
  }

  private void testVfsFileTypes( String dataFilename, String schemaFilename, boolean expected ) {
    assertNotNull( dataFilename );
    assertNotNull( schemaFilename );
    try {
      assertTrue( KettleVFS.getFileObject( dataFilename ).exists() );
      assertTrue( KettleVFS.getFileObject( schemaFilename ).exists() );
    } catch ( Exception e ) {
      fail();
    }

    RowMetaInterface inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta( new ValueMetaString( "DataFile" ) );
    inputRowMeta.addValueMeta( new ValueMetaString( "SchemaFile" ) );
    List<RowMetaAndData> inputData = new ArrayList<RowMetaAndData>();
    inputData.add( new RowMetaAndData( inputRowMeta, new Object[]{ dataFilename, schemaFilename } ) );

    String stepName = "XSD Validator";
    XsdValidatorMeta meta = new XsdValidatorMeta();
    meta.setDefault();
    meta.setXMLSourceFile( true );
    meta.setXMLStream( "DataFile" );
    meta.setXSDSource( meta.SPECIFY_FIELDNAME );
    meta.setXSDDefinedField( "SchemaFile" );
    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName );

    List<RowMetaAndData> result = null;
    try {
      result = TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
        TransTestFactory.DUMMY_STEPNAME, inputData );
    } catch ( KettleException e ) {
      fail();
    }

    assertNotNull( result );
    assertEquals( 1, result.size() );

    // Check Metadata
    assertEquals( ValueMetaInterface.TYPE_STRING, result.get( 0 ).getValueMeta( 0 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.get( 0 ).getValueMeta( 1 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, result.get( 0 ).getValueMeta( 2 ).getType() );
    assertEquals( "DataFile", result.get( 0 ).getValueMeta( 0 ).getName() );
    assertEquals( "SchemaFile", result.get( 0 ).getValueMeta( 1 ).getName() );
    assertEquals( "result", result.get( 0 ).getValueMeta( 2 ).getName() );

    // Check result
    try {
      assertEquals( dataFilename, result.get( 0 ).getString( 0, "default" ) );
      assertEquals( schemaFilename, result.get( 0 ).getString( 1, "default" ) );
      assertEquals( expected, result.get( 0 ).getBoolean( 2, !expected ) );
    } catch ( KettleValueException e ) {
      fail();
    }
  }
}
