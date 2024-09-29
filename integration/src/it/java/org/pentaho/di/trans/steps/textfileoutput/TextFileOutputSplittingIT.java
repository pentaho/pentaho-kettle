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

package org.pentaho.di.trans.steps.textfileoutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * The input transformation contains 3 steps. The last one is TextFileOutput step. It obtains the vector [1,2,...,5].
 * Split threshold is set to 3. The step creates in-memory files following this template: {@linkplain
 * TextFileOutputSplittingIT#OUTPUT_DIR}. The class covers different cases depending on header's and footer's
 * existence flags.
 *
 * @author Andrey Khayrutdinov
 */
public class TextFileOutputSplittingIT {

  private static final String OUTPUT_DIR = "ram://pdi-12847";

  private static final String H = "value";
  private static final String F = "value";

  private TransMeta transMeta;

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init( false );
  }

  @Before
  public void setUp() throws Exception {
    transMeta = new TransMeta( "src/it/resources/org/pentaho/di/trans/steps/textfileoutput/pdi-12847.ktr" );
    transMeta.setTransformationType( TransMeta.TransformationType.Normal );
  }

  @After
  public void tearDown() throws Exception {
    transMeta = null;

    FileObject folder = getFolder();
    for ( FileObject fileObject : folder.getChildren() ) {
      fileObject.delete();
    }
  }


  @Test
  public void splitWithNone() throws Exception {
    runTransformation( transMeta );

    FileObject[] children = getFolder().getChildren();
    // 2 files => [1,2,3], [4,5]
    assertEquals( 2, children.length );

    assertSplitFileIsCorrect( children[ 0 ], "data.txt_0", "1", "2", "3" );
    assertSplitFileIsCorrect( children[ 1 ], "data.txt_1", "4", "5" );
  }

  @Test
  public void splitWithHeader() throws Exception {
    TextFileOutputMeta meta = pickupTextFileOutputMeta();
    meta.setHeaderEnabled( true );

    runTransformation( transMeta );

    FileObject[] children = getFolder().getChildren();
    // 3 files => [h,1,2], [h,3,4], [h,5]
    assertEquals( 3, children.length );

    assertSplitFileIsCorrect( children[ 0 ], "data.txt_0", H, "1", "2" );
    assertSplitFileIsCorrect( children[ 1 ], "data.txt_1", H, "3", "4" );
    assertSplitFileIsCorrect( children[ 2 ], "data.txt_2", H, "5" );
  }

  @Test
  public void splitWithFooter() throws Exception {
    TextFileOutputMeta meta = pickupTextFileOutputMeta();
    meta.setFooterEnabled( true );

    runTransformation( transMeta );

    FileObject[] children = getFolder().getChildren();
    // 3 files => [1,2,f], [3,4,f], [5,f]
    assertEquals( 3, children.length );

    assertSplitFileIsCorrect( children[ 0 ], "data.txt_0", "1", "2", F );
    assertSplitFileIsCorrect( children[ 1 ], "data.txt_1", "3", "4", F );
    assertSplitFileIsCorrect( children[ 2 ], "data.txt_2", "5", F );
  }

  @Test
  public void splitWithBoth() throws Exception {
    TextFileOutputMeta meta = pickupTextFileOutputMeta();
    meta.setHeaderEnabled( true );
    meta.setFooterEnabled( true );

    runTransformation( transMeta );

    FileObject[] children = getFolder().getChildren();
    // 5 files => [h,1,f], [h,2,f], ..., [h,5,f]
    assertEquals( 5, children.length );

    for ( int i = 0; i < children.length; i++ ) {
      assertSplitFileIsCorrect( children[ i ], "data.txt_" + i, H, Integer.toString( i + 1 ), F );
    }
  }


  private TextFileOutputMeta pickupTextFileOutputMeta() throws Exception {
    return (TextFileOutputMeta) transMeta.getSteps().get( 2 ).getStepMetaInterface();
  }

  private static void runTransformation( TransMeta transMeta ) throws Exception {
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();

    assertEquals( 0, trans.getErrors() );
  }

  private static FileObject getFolder() throws FileSystemException, KettleFileException {
    return KettleVFS.getFileObject( OUTPUT_DIR );
  }

  private static void assertSplitFileIsCorrect( FileObject file, String expectedName, String... expectedLines )
    throws Exception {
    List<String> content = readContentOf( file );
    assertEquals( expectedName, file.getName().getBaseName() );
    assertEquals( expectedLines.length, content.size() );
    for ( int i = 0; i < content.size(); i++ ) {
      assertEquals( expectedLines[ i ], content.get( i ) );
    }
  }

  @SuppressWarnings( "unchecked" )
  private static List<String> readContentOf( FileObject fileObject ) throws Exception {
    return IOUtils.readLines( fileObject.getContent().getInputStream() );
  }
}
