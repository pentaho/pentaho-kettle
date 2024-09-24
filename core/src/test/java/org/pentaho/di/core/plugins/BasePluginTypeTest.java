/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.plugins;

import org.apache.commons.vfs2.FileObject;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.vfs.KettleVFS;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BasePluginTypeTest {
  private static final String BASE_RAM_DIR = "ram:/basePluginTypeTest/";

  @Test
  public void testRegisterNativesCloseResAsStream() throws Exception {
    BasePluginType bpt = spy( DatabasePluginType.getInstance() );

    InputStream is = mock( InputStream.class );

    doReturn( is ).when( bpt ).getResAsStreamExternal( anyString() );
    doNothing().when( bpt ).registerPlugins( is );

    bpt.registerNatives();

    verify( is ).close();
  }

  @Test
  public void testRegisterNativesCloseFileInStream() throws Exception {
    BasePluginType bpt = spy( TwoWayPasswordEncoderPluginType.getInstance() );

    InputStream is = mock( InputStream.class );

    doReturn( "foo" ).when( bpt ).getPropertyExternal( anyString(), eq( null ) );
    doReturn( null ).when( bpt ).getResAsStreamExternal( anyString() );
    doReturn( is ).when( bpt ).getFileInputStreamExternal( anyString() );
    doNothing().when( bpt ).registerPlugins( is );
    doCallRealMethod().when( bpt ).registerNatives();

    bpt.registerNatives();

    verify( is ).close();
  }

  /*
   * [PDI-17862] Testing issue with a bad attempt to find annotations and the graceful reporting it completes.
   */
  /*
   Moving to jdk11 means abandoning the Whitebox test tool, which means this sort of test is now impossible given the
   structure of the LogChannel class.  TODO: update BasePluginType to be more testable PDI-19369
   */
  @Ignore
  @Test
  public void findAnnotatedClassFilesFailTest() throws Exception {
    KettleLogStore.init();
    LogChannel generalLog = mock( LogChannel.class );

    FileObject fileObj1 = KettleVFS.getFileObject( BASE_RAM_DIR + "testJar1.jar" );
    FileObject fileObj2 = KettleVFS.getFileObject( BASE_RAM_DIR + "testJar2.jar" );
    FileObject[] fileObjects = { fileObj1, fileObj2 };

    BasePluginType bpt = spy( DatabasePluginType.getInstance() );
    bpt.log = generalLog;
    List<PluginFolderInterface> pluginFolders = new ArrayList<>();
    PluginFolder pluginFolder =
      spy( new PluginFolder( BASE_RAM_DIR, false, true, false ) );
    pluginFolders.add( pluginFolder );
    bpt.setPluginFolders( pluginFolders );

    doReturn( fileObjects ).when( pluginFolder ).findJarFiles();
    doNothing().when( generalLog ).logError( any() );
    doNothing().when( generalLog ).logDebug( any(), any() );

    bpt.findAnnotatedClassFiles( "testClassName" );

    verify( generalLog, times( 2 ) ).logError( any() );
    verify( generalLog, times( 2 ) ).logDebug( any(), any() );
  }
}
