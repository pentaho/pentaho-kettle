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

package org.pentaho.di.trans.steps.loadfileinput;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class LoadFileInputTest {

  private FileSystemManager fs;
  private String filesPath;

  private String transName;
  private TransMeta transMeta;
  private Trans trans;

  private LoadFileInputMeta stepMetaInterface;
  private StepDataInterface stepDataInterface;
  private StepMeta stepMeta;
  private FileInputList stepInputFiles;
  private int stepCopyNr;

  private LoadFileInput stepLoadFileInput;

  private StepMetaInterface runtimeSMI;
  private StepDataInterface runtimeSDI;

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setup() throws KettleException, FileSystemException {
    fs = VFS.getManager();
    filesPath = '/' + this.getClass().getPackage().getName().replace( '.', '/' ) + "/files/";

    transName = "LoadFileInput";
    transMeta = new TransMeta();
    transMeta.setName( transName );
    trans = new Trans( transMeta );

    stepMetaInterface = spy( new LoadFileInputMeta() );
    stepInputFiles = new FileInputList();
    Mockito.doReturn( stepInputFiles ).when( stepMetaInterface ).getFiles( any( VariableSpace.class ) );
    String stepId = PluginRegistry.getInstance().getPluginId( StepPluginType.class, stepMetaInterface );
    stepMeta = new StepMeta( stepId, "Load File Input", stepMetaInterface );
    transMeta.addStep( stepMeta );

    stepDataInterface = new LoadFileInputData();

    stepCopyNr = 0;

    stepLoadFileInput = new LoadFileInput( stepMeta, stepDataInterface, stepCopyNr, transMeta, trans );

    assertSame( stepMetaInterface, stepMeta.getStepMetaInterface() );

    runtimeSMI = stepMetaInterface;
    runtimeSDI = runtimeSMI.getStepData();
    stepLoadFileInput.init( runtimeSMI, runtimeSDI );
  }

  private FileObject getFile( final String filename ) {
    try {
      FileObject fo = fs.resolveFile( this.getClass().getResource( filesPath + filename ) );
      return fo;
    } catch ( Exception e ) {
      throw new RuntimeException( "fail. " + e.getMessage(), e );
    }
  }

  @Test
  public void testOpenNextFile_noFiles() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_noFiles_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_0() throws FileSystemException {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_0_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_000() throws FileSystemException {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );

  }

  @Test
  public void testOpenNextFile_000_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_10() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input1.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_10_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input1.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }


  @Test
  public void testOpenNextFile_01() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input1.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_01_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input1.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_010() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input1.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_010_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input1.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }
}
