/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.di.plugins.fileopensave.extension;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.plugins.fileopensave.api.providers.FileProvider;
import org.pentaho.di.plugins.fileopensave.providers.ProviderService;
import org.pentaho.di.plugins.fileopensave.providers.local.LocalFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.vfs.VFSFileProvider;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.spoon.Spoon;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class FileOpenSaveExtensionPointTest extends TestCase {

  private static final String FILE_OP_DUMMY_COMMAND = "testDummyCommand";

  @Test
  public void testResolveProvider_AlreadySet() throws Exception {
    // SETUP
    FileOpenSaveExtensionPoint testInstance = new FileOpenSaveExtensionPoint( null, null );

    FileDialogOperation opAlreadySet = new FileDialogOperation( FILE_OP_DUMMY_COMMAND  );
    String providerNonProductionValue = "DontChangeMe"; // NON production value, just want to ensure it deosn't get overwritten
    opAlreadySet.setProvider( providerNonProductionValue );
    opAlreadySet.setPath( "/tmp/someRandomPath" );

    // EXECUTE
    testInstance.resolveProvider( opAlreadySet );

    // VERIFY
    assertEquals( providerNonProductionValue, opAlreadySet.getProvider() );
  }

  @Test
  public void testResolveProvider_Vfs() throws Exception {
    // SETUP
    String vfsPath = "pvfs://someConnection/someFilePath";
    ProviderService mockProviderService = mock( ProviderService.class );
    VFSFileProvider mockVFSFileProvider = mock( VFSFileProvider.class );
    when( mockProviderService.get( VFSFileProvider.TYPE ) ).thenReturn( mockVFSFileProvider );
    when( mockVFSFileProvider.isSupported(  vfsPath ) ).thenReturn( true );

    FileOpenSaveExtensionPoint testInstance = new FileOpenSaveExtensionPoint( mockProviderService, null );

    FileDialogOperation opVfs = new FileDialogOperation( FILE_OP_DUMMY_COMMAND  );
    opVfs.setProvider( null );
    opVfs.setPath( vfsPath );

    // EXECUTE
    testInstance.resolveProvider( opVfs );

    // VERIFY
    assertEquals( VFSFileProvider.TYPE, opVfs.getProvider() );
  }

  @Test
  public void testResolveProvider_Repository() throws Exception  {
    // SETUP
    ProviderService mockProviderService = mock( ProviderService.class );
    VFSFileProvider mockVFSFileProvider = mock( VFSFileProvider.class );
    when( mockProviderService.get( VFSFileProvider.TYPE ) ).thenReturn( mockVFSFileProvider );
    when( mockVFSFileProvider.isSupported( any() ) ).thenReturn( false );
    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getRepository() ).thenReturn( null );
    Repository mockRepository = mock( Repository.class );
    when( mockSpoon.getRepository() ).thenReturn( mockRepository );
    FileOpenSaveExtensionPoint testInstance = new FileOpenSaveExtensionPoint( mockProviderService, () -> mockSpoon );

    FileDialogOperation opRepository = new FileDialogOperation( FILE_OP_DUMMY_COMMAND  );
    opRepository.setProvider( null );
    opRepository.setPath( "//home/randomUser/randomFile.rpt" );

    // EXECUTE
    testInstance.resolveProvider( opRepository );

    // VERIFY
    assertEquals( RepositoryFileProvider.TYPE, opRepository.getProvider() );
  }

  @Test
  public void testResolveProvider_Local() throws Exception  {
    // SETUP
    ProviderService mockProviderService = mock( ProviderService.class );
    VFSFileProvider mockVFSFileProvider = mock( VFSFileProvider.class );
    when( mockProviderService.get( VFSFileProvider.TYPE ) ).thenReturn( mockVFSFileProvider );
    when( mockVFSFileProvider.isSupported( any() ) ).thenReturn( false );
    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getRepository() ).thenReturn( null );
    FileOpenSaveExtensionPoint testInstance = new FileOpenSaveExtensionPoint( mockProviderService, () -> mockSpoon );

    FileDialogOperation opLocal = new FileDialogOperation( FILE_OP_DUMMY_COMMAND  );
    opLocal.setProvider( null );
    opLocal.setPath( "/someUser/someUnixFile" );

    // EXECUTE
    testInstance.resolveProvider( opLocal );

    // VERIFY
    assertEquals( LocalFileProvider.TYPE, opLocal.getProvider() );
  }

  @Test
  public void testIsVfsPath_nullFileProvider() throws Exception {
    // SETUP
    String vfsPath = "pvfs://someConnection/someFilePath";
    ProviderService mockProviderService = mock( ProviderService.class );
    when( mockProviderService.get( VFSFileProvider.TYPE ) ).thenReturn( null );

    FileOpenSaveExtensionPoint testInstance = new FileOpenSaveExtensionPoint( mockProviderService, null );

    assertFalse( testInstance.isVfsPath( vfsPath ) );
  }

  @Test
  public void testIsVfsPath_NotCorrectInstance() throws Exception {
    // SETUP
    String vfsPath = "pvfs://someConnection/someFilePath";
    ProviderService mockProviderService = mock( ProviderService.class );
    FileProvider mockGenericFileProvider = mock( FileProvider.class );
    when( mockProviderService.get( VFSFileProvider.TYPE ) ).thenReturn( mockGenericFileProvider );

    FileOpenSaveExtensionPoint testInstance = new FileOpenSaveExtensionPoint( mockProviderService, null );

    assertFalse( testInstance.isVfsPath( vfsPath ) );
  }

  @Test
  public void testIsVfsPath_NotSupported() throws Exception {
    // SETUP
    String vfsPath = "pvfs://someConnection/someFilePath";
    ProviderService mockProviderService = mock( ProviderService.class );
    VFSFileProvider mockVFSFileProvider = mock( VFSFileProvider.class );
    when( mockProviderService.get( VFSFileProvider.TYPE ) ).thenReturn( mockVFSFileProvider );
    when( mockVFSFileProvider.isSupported( any() ) ).thenReturn( false );

    FileOpenSaveExtensionPoint testInstance = new FileOpenSaveExtensionPoint( mockProviderService, null );

    assertFalse( testInstance.isVfsPath( vfsPath ) );
  }

  @Test
  public void testIsVfsPath() throws Exception {
    // SETUP
    String vfsPath = "pvfs://someConnection/someFilePath";
    ProviderService mockProviderService = mock( ProviderService.class );
    VFSFileProvider mockVFSFileProvider = mock( VFSFileProvider.class );
    when( mockProviderService.get( VFSFileProvider.TYPE ) ).thenReturn( mockVFSFileProvider );
    when( mockVFSFileProvider.isSupported( any() ) ).thenReturn( true );

    FileOpenSaveExtensionPoint testInstance = new FileOpenSaveExtensionPoint( mockProviderService, null );

    assertTrue( testInstance.isVfsPath( vfsPath ) );
  }
}
