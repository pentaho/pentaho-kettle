/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.dragdrop;

import org.eclipse.swt.dnd.TransferData;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.providers.local.LocalFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.vfs.VFSFileProvider;

import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ElementTransferTest {
  private VariableSpace space = new Variables();
  ElementTransfer elementTransfer;
  private static boolean skipTests = false;

  /**
   * To configure swt
   * There are three options:
   * <ol>
   *   <li><code>-Djava.library.path={runtime-library-path}</code></li>
   *   <li>environment variable: <code>LD_LIBRARY_PATH</code></li>
   *   <li>Copy the SWT library (swt.jar) to a directory that is already on the Java library path</li>
   * </ol>
   *
   * For more complete instructions @see <a href="https://www.eclipse.org/swt/faq.php#missingdll">Elipse SWT FAQ: Missing DLL </a>.
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    org.junit.Assume.assumeFalse( skipTests );
    try {
      elementTransfer = ElementTransfer.getInstance();
    } catch ( UnsatisfiedLinkError e ) {
      System.out.println( "UnsatisfiedLinkError likely due to swt configuration, "
          + "os specific swt.jar needs to be added to classpath. Skipping tests" );
      e.printStackTrace();
      skipTests = true;
      org.junit.Assume.assumeFalse( skipTests );
    }
    ElementTransfer.testMode = true; //Set mode to test so it doesn't pass the data to the OS
  }

  @Test
  public void getTypeIds() {

    assertArrayEquals( new int[]{ ElementTransfer.TYPEID },  elementTransfer.getTypeIds() );
  }

  @Test
  public void getTypeNames() {
    assertArrayEquals( new String[] { ElementTransfer.TYPE_NAME }, elementTransfer.getTypeNames() );
  }

  @Test
  public void javaToNativeAndBack() {
    // SETUP
    String testLocalFileName = "testFile.csv";
    String testLocalPath = Paths.get( System.getProperty( "java.io.tmpdir" ) ).resolve( testLocalFileName ).toString();
    Element elementLocal = new Element( testLocalFileName, EntityType.LOCAL_FILE,
        testLocalPath, LocalFileProvider.TYPE );

    String testRepoFileName = "randomFile.rpt";
    String testRepoPath = "//home/randomUser/" + testRepoFileName;
    String testRepositoryName = "testRepositoryName";
    Element elementRepository = new Element( testRepoFileName, EntityType.REPOSITORY_FILE, testRepoPath,
        RepositoryFileProvider.TYPE, testRepositoryName );

    String testVfsFileName = "someFile.txt";
    String testVfsPath = "pvfs://randomConnectionName/someDir/" + testVfsFileName;
    Element elementVfs = new Element( testVfsFileName, EntityType.VFS_FILE, testVfsPath,
        VFSFileProvider.TYPE );

    TransferData transferData = new TransferData();

    Object[] transferObjects = new Object[] {
        elementLocal.convertToFile( space ),
        elementRepository.convertToFile( space ),
        elementVfs.convertToFile( space )
    };

    // EXECUTE
    elementTransfer.javaToNative( transferObjects, transferData );
    Element[] elements = (Element[]) elementTransfer.nativeToJava( transferData );

    // VERIFY
    assertEquals( transferObjects.length, elements.length );
    assertEquals( elementLocal, elements[0] );
    assertEquals( elementRepository, elements[1] );
    assertEquals( elementRepository.getRepositoryName(), elements[1].getRepositoryName() );
    assertEquals( elementVfs, elements[2] );
  }
}
