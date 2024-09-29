/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.ConnectionProvider;
import org.pentaho.di.connections.vfs.provider.ConnectionFileName;
import org.pentaho.di.connections.vfs.provider.ConnectionFileNameUtils;
import org.pentaho.di.connections.vfs.provider.ConnectionFileProvider;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VFSConnectionManagerHelperTest {

  static final String TEST_CONNECTION_NAME = "TEST_CONNECTION_NAME";
  static final String TEST_CONNECTION_TYPE = "testscheme";
  static final String TEST_ROOT_PATH = "root/path";

  VFSConnectionManagerHelper vfsConnectionManagerHelper;
  Bowl bowl;
  VFSConnectionDetails vfsConnectionDetails;
  VariableSpace space;
  VFSConnectionProvider<VFSConnectionDetails> vfsConnectionProvider;
  ConnectionManager connectionManager;
  MemoryMetaStore memoryMetaStore;
  ConnectionFileNameUtils connectionFileNameUtils;
  VFSConnectionFileNameTransformer<VFSConnectionDetails> vfsConnectionFileNameTransformer;
  FileSystemOptions fileSystemOptions;
  IKettleVFS kettleVFS;
  FileObject connectionRootProviderFileObject;
  ConnectionFileName connectionRootFileName;

  @SuppressWarnings( "unchecked" )
  @Before
  public void setup() throws Exception {

    // Default configuration is for a successful positive test call.

    bowl = mock( Bowl.class );
    vfsConnectionFileNameTransformer = mock( VFSConnectionFileNameTransformer.class );
    fileSystemOptions = mock( FileSystemOptions.class );
    connectionFileNameUtils = ConnectionFileNameUtils.getInstance();
    kettleVFS = mock( IKettleVFS.class );

    // ---

    space = mock( VariableSpace.class );

    // Default impl just returns the given string.
    doAnswer( invocationOnMock -> invocationOnMock.getArgument( 0 ) )
      .when( space )
      .environmentSubstitute( anyString() );

    // ---

    vfsConnectionDetails = mock( VFSConnectionDetails.class );
    when( vfsConnectionDetails.getName() ).thenReturn( TEST_CONNECTION_NAME );
    when( vfsConnectionDetails.getType() ).thenReturn( TEST_CONNECTION_TYPE );
    when( vfsConnectionDetails.isRootPathSupported() ).thenReturn( true );
    when( vfsConnectionDetails.isRootPathRequired() ).thenReturn( true );
    when( vfsConnectionDetails.getRootPath() ).thenReturn( TEST_ROOT_PATH );
    when( vfsConnectionDetails.getSpace() ).thenReturn( space );

    // ---

    connectionManager = mock( ConnectionManager.class );

    // ---

    vfsConnectionProvider = (VFSConnectionProvider<VFSConnectionDetails>) mock( VFSConnectionProvider.class );
    when( vfsConnectionProvider.getKey() ).thenReturn( TEST_CONNECTION_TYPE );
    when( vfsConnectionProvider.getFileNameTransformer( connectionManager ) ).thenReturn(
      vfsConnectionFileNameTransformer );

    // By default, provider testing is successful.
    when( vfsConnectionProvider.test( any( VFSConnectionDetails.class ) ) ).thenReturn( true );
    when( vfsConnectionProvider.getOpts( vfsConnectionDetails ) ).thenReturn( fileSystemOptions );

    // ---

    vfsConnectionManagerHelper = spy( new VFSConnectionManagerHelper( connectionFileNameUtils ) );
    when( vfsConnectionManagerHelper.getKettleVFS( bowl ) ).thenReturn( kettleVFS );

    // ---

    memoryMetaStore = new MemoryMetaStore();

    doReturn( vfsConnectionProvider ).when( connectionManager ).getConnectionProvider( TEST_CONNECTION_TYPE );
    doReturn( bowl ).when( connectionManager ).getBowl();

    // ---

    // PVFS connection root file name
    connectionRootFileName = new ConnectionFileName( TEST_CONNECTION_NAME );

    // Provider root path FileObject
    connectionRootProviderFileObject = mock( FileObject.class );
    when( connectionRootProviderFileObject.exists() ).thenReturn( true );
    when( connectionRootProviderFileObject.isFolder() ).thenReturn( true );

    doAnswer( invocationOnMock -> getConnectionRootProviderFileName() )
      .when( vfsConnectionFileNameTransformer )
      .toProviderFileName( connectionRootFileName, vfsConnectionDetails );

    when( kettleVFS.getFileObject(
      argThat( uri -> {
        try {
          return getConnectionRootProviderFileName().getURI().equals( uri );
        } catch ( KettleException e ) {
          // Should not happen, but rethrowing JIC, to catch any testing bugs.
          throw new RuntimeException( e );
        }
      } ),
      any( VariableSpace.class ),
      eq( fileSystemOptions ) ) )
      .thenReturn( connectionRootProviderFileObject );
  }

  // This method assumes that `getResolvedRootPath` is not being tested.
  FileName getConnectionRootProviderFileName() throws KettleException {
    FileName fileName = mock( FileName.class );
    doReturn( TEST_CONNECTION_TYPE + "://" + vfsConnectionManagerHelper.getResolvedRootPath( vfsConnectionDetails ) )
      .when( fileName ).getURI();
    return fileName;
  }

  // region getProviders( manager )
  @Test
  public void testGetProvidersReturnsListOfCastedItemsFromManager() {
    ConnectionProvider<? extends ConnectionDetails> provider1 =
      (ConnectionProvider<? extends ConnectionDetails>) mock( VFSConnectionProvider.class );
    ConnectionProvider<? extends ConnectionDetails> provider2 =
      (ConnectionProvider<? extends ConnectionDetails>) mock( VFSConnectionProvider.class );

    doReturn( Arrays.asList( provider1, provider2 ) )
      .when( connectionManager )
      .getProvidersByType( VFSConnectionProvider.class );

    List<VFSConnectionProvider<VFSConnectionDetails>> providers =
      vfsConnectionManagerHelper.getProviders( connectionManager );

    assertNotNull( providers );
    assertEquals( 2, providers.size() );
    assertSame( provider1, providers.get( 0 ) );
    assertSame( provider2, providers.get( 1 ) );
  }
  // endregion

  // region getProvider( manager, key )
  @Test
  public void testGetProviderReturnsExistingCastedProviderFromManager() {
    String provider1Key = "provider1Key";

    ConnectionProvider<? extends ConnectionDetails> provider1 =
      (ConnectionProvider<? extends ConnectionDetails>) mock( VFSConnectionProvider.class );

    doReturn( provider1 ).when( connectionManager ).getConnectionProvider( provider1Key );

    VFSConnectionProvider<VFSConnectionDetails> result =
      vfsConnectionManagerHelper.getProvider( connectionManager, provider1Key );

    assertSame( provider1, result );
  }

  @Test
  public void testGetProviderReturnsNullForNonExistingProviderInManager() {
    VFSConnectionProvider<VFSConnectionDetails> result =
      vfsConnectionManagerHelper.getProvider( connectionManager, "missingProvider1" );

    assertNull( result );
  }
  // endregion

  // region getProvider( manager, details )
  @Test
  public void testGetProviderOfDetailsReturnsExistingCastedProviderFromManager() {
    String provider1Key = "provider1Key";

    VFSConnectionDetails details1 = mock( VFSConnectionDetails.class );
    doReturn( provider1Key ).when( details1 ).getType();

    ConnectionProvider<? extends ConnectionDetails> provider1 =
      (ConnectionProvider<? extends ConnectionDetails>) mock( VFSConnectionProvider.class );

    doReturn( provider1 ).when( connectionManager ).getConnectionProvider( provider1Key );

    VFSConnectionProvider<VFSConnectionDetails> result =
      vfsConnectionManagerHelper.getProvider( connectionManager, details1 );

    assertSame( provider1, result );
  }

  @Test
  public void testGetProviderOfDetailsReturnsNullForNonExistingProviderInManager() {
    String provider1Key = "missingProvider1";

    VFSConnectionDetails details1 = mock( VFSConnectionDetails.class );
    doReturn( provider1Key ).when( details1 ).getType();

    VFSConnectionProvider<VFSConnectionDetails> result =
      vfsConnectionManagerHelper.getProvider( connectionManager, details1 );

    assertNull( result );
  }

  @Test
  public void testGetProviderOfDetailsReturnsNullWhenGivenNull() {
    VFSConnectionProvider<VFSConnectionDetails> result =
      vfsConnectionManagerHelper.getProvider( connectionManager, (VFSConnectionDetails) null );

    assertNull( result );
  }
  // endregion

  // region getExistingProvider( manager, key )
  @Test
  public void testGetExistingProviderReturnsExistingCastedProviderFromManager() throws KettleException {
    String provider1Key = "provider1Key";

    ConnectionProvider<? extends ConnectionDetails> provider1 =
      (ConnectionProvider<? extends ConnectionDetails>) mock( VFSConnectionProvider.class );

    doReturn( provider1 ).when( connectionManager ).getConnectionProvider( provider1Key );

    VFSConnectionProvider<VFSConnectionDetails> result =
      vfsConnectionManagerHelper.getExistingProvider( connectionManager, provider1Key );

    assertSame( provider1, result );
  }

  @Test( expected = KettleException.class )
  public void testGetExistingProviderReturnsNullForNonExistingProviderInManager() throws KettleException {
    vfsConnectionManagerHelper.getExistingProvider( connectionManager, "missingProvider1" );
  }
  // endregion

  // region getExistingProvider( manager, details )
  @Test
  public void testGetExistingProviderOfDetailsReturnsExistingCastedProviderFromManager() throws KettleException {
    String provider1Key = "provider1Key";

    VFSConnectionDetails details1 = mock( VFSConnectionDetails.class );
    doReturn( provider1Key ).when( details1 ).getType();

    ConnectionProvider<? extends ConnectionDetails> provider1 =
      (ConnectionProvider<? extends ConnectionDetails>) mock( VFSConnectionProvider.class );

    doReturn( provider1 ).when( connectionManager ).getConnectionProvider( provider1Key );

    VFSConnectionProvider<VFSConnectionDetails> result =
      vfsConnectionManagerHelper.getExistingProvider( connectionManager, details1 );

    assertSame( provider1, result );
  }

  @Test( expected = KettleException.class )
  public void testGetExistingProviderOfDetailsReturnsNullForNonExistingProviderInManager() throws KettleException {
    String provider1Key = "missingProvider1";

    VFSConnectionDetails details1 = mock( VFSConnectionDetails.class );
    doReturn( provider1Key ).when( details1 ).getType();

    vfsConnectionManagerHelper.getExistingProvider( connectionManager, details1 );
  }
  // endregion

  // region getAllDetails( manager )
  @Test
  public void testGetAllDetailsReturnsAllDetailsFromAllVFSProvidersFromManager() {
    ConnectionProvider<? extends ConnectionDetails> provider1 =
      (ConnectionProvider<? extends ConnectionDetails>) mock( VFSConnectionProvider.class );
    ConnectionProvider<? extends ConnectionDetails> provider2 =
      (ConnectionProvider<? extends ConnectionDetails>) mock( VFSConnectionProvider.class );
    ConnectionProvider<? extends ConnectionDetails> provider3 =
      (ConnectionProvider<? extends ConnectionDetails>) mock( VFSConnectionProvider.class );

    doReturn( Arrays.asList( provider1, provider2, provider3 ) )
      .when( connectionManager )
      .getProvidersByType( VFSConnectionProvider.class );

    VFSConnectionDetails details1 = mock( VFSConnectionDetails.class );
    VFSConnectionDetails details2 = mock( VFSConnectionDetails.class );
    VFSConnectionDetails details3 = mock( VFSConnectionDetails.class );
    VFSConnectionDetails details4 = mock( VFSConnectionDetails.class );

    doReturn( Arrays.asList( details3, details4 ) ).when( provider1 ).getConnectionDetails( connectionManager );
    doReturn( null ).when( provider2 ).getConnectionDetails( connectionManager );
    doReturn( Arrays.asList( details1, details2 ) ).when( provider3 ).getConnectionDetails( connectionManager );

    List<VFSConnectionDetails> allDetails = vfsConnectionManagerHelper.getAllDetails( connectionManager );

    assertNotNull( allDetails );
    assertEquals( 4, allDetails.size() );
    assertSame( details3, allDetails.get( 0 ) );
    assertSame( details4, allDetails.get( 1 ) );
    assertSame( details1, allDetails.get( 2 ) );
    assertSame( details2, allDetails.get( 3 ) );
  }
  // endregion

  // region getResolvedRootPath(..)
  void assertGetResolvedRootPath( String rootPath, String resolvedRootPath ) throws KettleException {
    when( vfsConnectionDetails.getRootPath() ).thenReturn( rootPath );

    assertEquals( resolvedRootPath, vfsConnectionManagerHelper.getResolvedRootPath( vfsConnectionDetails ) );
  }

  void assertGetResolvedRootPath( String rootPath, String substitutedRootPath, String resolvedRootPath )
    throws KettleException {

    when( space.environmentSubstitute( rootPath ) ).thenReturn( substitutedRootPath );
    assertGetResolvedRootPath( rootPath, resolvedRootPath );
  }

  void assertGetResolvedRootPathThrows( String rootPath ) {
    when( vfsConnectionDetails.getRootPath() ).thenReturn( rootPath );

    try {
      vfsConnectionManagerHelper.getResolvedRootPath( vfsConnectionDetails );
      fail( "Should have thrown a 'KettleException' exception." );
    } catch ( KettleException e ) {
      assertNotNull( e );
    }
  }

  @Test
  public void testGetResolvedRootPathReturnsNullForNullOrEmptyOrBlankRootPath() throws KettleException {
    assertGetResolvedRootPath( null, null );
    assertGetResolvedRootPath( "", null );
    assertGetResolvedRootPath( "   ", null );
  }

  @Test
  public void testGetResolvedRootPathResolvesRelativeSegments() throws KettleException {
    assertGetResolvedRootPath( ".", null );
    assertGetResolvedRootPath( "./", null );
    assertGetResolvedRootPath( "./foo", "foo" );
    assertGetResolvedRootPath( "foo/.", "foo" );
    assertGetResolvedRootPath( "foo/./bar", "foo/bar" );
    assertGetResolvedRootPath( "./foo/././bar/.", "foo/bar" );

    // ---

    assertGetResolvedRootPath( "foo/../bar", "bar" );
    assertGetResolvedRootPath( "foo/..", null );
    assertGetResolvedRootPath( "foo/../bar/..", null );
    assertGetResolvedRootPath( "foo/bar/../..", null );
  }

  @Test
  public void testGetResolvedRootPathThrowsOnInvalidRelativeSegments() {
    assertGetResolvedRootPathThrows( "/.." );
    assertGetResolvedRootPathThrows( "foo/../.." );
  }

  @Test
  public void testGetResolvedRootPathTrimsPathSeparator() throws KettleException {
    assertGetResolvedRootPath( "/foo", "foo" );
    assertGetResolvedRootPath( "foo/", "foo" );
    assertGetResolvedRootPath( "/foo/", "foo" );
    assertGetResolvedRootPath( "/c:/foo", "c:/foo" );
  }

  @Test
  public void testGetResolvedRootPathTrimsSpaces() throws KettleException {
    assertGetResolvedRootPath( "  foo", "foo" );
    assertGetResolvedRootPath( "foo  ", "foo" );
    assertGetResolvedRootPath( "  foo  ", "foo" );
  }

  @Test
  public void testGetResolvedRootPathRemovesEmptySegments() throws KettleException {
    assertGetResolvedRootPath( "//", null );
    assertGetResolvedRootPath( "//foo", "foo" );
    assertGetResolvedRootPath( "foo//", "foo" );
    assertGetResolvedRootPath( "foo//bar", "foo/bar" );
    assertGetResolvedRootPath( "foo///bar", "foo/bar" );
    assertGetResolvedRootPath( "foo//bar//guru", "foo/bar/guru" );
  }

  @Test
  public void testGetResolvedRootPathConvertsBackslashes() throws KettleException {
    assertGetResolvedRootPath( "\\", null );
    assertGetResolvedRootPath( "\\//", null );
    assertGetResolvedRootPath( "\\foo", "foo" );
    assertGetResolvedRootPath( "c:\\foo\\bar", "c:/foo/bar" );
  }

  @Test
  public void testGetResolvedRootPathPreservesEncoding() throws KettleException {
    // "foo%"
    assertGetResolvedRootPath( "foo%25", "foo%25" );

    // "foo bar"
    assertGetResolvedRootPath( "foo%20bar", "foo%20bar" );
  }

  @Test
  public void testGetResolvedRootPathSubstitutesVariablesWithDefaultSpace() throws KettleException {
    when( vfsConnectionDetails.getSpace() ).thenReturn( null );

    // Default variable space is functional and has no variables, so no substitution actually occurs.
    assertGetResolvedRootPath( "root/${0}/path", "root/${0}/path", "root/${0}/path" );
  }

  @Test
  public void testGetResolvedRootPathSubstitutesVariablesAndThenNormalizes() throws KettleException {
    assertGetResolvedRootPath( "root/${0}/path", "root///path", "root/path" );

    assertGetResolvedRootPath( "${0}", "///", null );

    assertGetResolvedRootPath( "${0}", null, null );
  }

  @Test
  public void testGetResolvedRootPathSubstitutesEnvironmentVariables() throws Exception {

    String rootPath = "C:\\Users";
    System.setProperty( "folder_location", rootPath );
    when( vfsConnectionDetails.getSpace() ).thenReturn( Variables.getADefaultVariableSpace() );

    assertGetResolvedRootPath( "${folder_location}", "C:/Users" );
  }
  // endregion

  // region getConnectionRootFileName( . )
  @Test
  public void testGetConnectionRootFileNameReturnsTheConnectionRoot() {

    String connectionNameWithNoReservedChars = "connection-name";
    when( vfsConnectionDetails.getName() ).thenReturn( connectionNameWithNoReservedChars );

    // pvfs://connection-name/
    ConnectionFileName fileName = vfsConnectionManagerHelper.getConnectionRootFileName( vfsConnectionDetails );
    assertEquals( ConnectionFileProvider.SCHEME, fileName.getScheme() );
    assertEquals( connectionNameWithNoReservedChars, fileName.getConnection() );
    assertEquals( ConnectionFileName.SEPARATOR, fileName.getPath() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testGetConnectionRootFileNameThrowsIllegalArgumentGivenConnectionHasNullName() {
    when( vfsConnectionDetails.getName() ).thenReturn( null );
    vfsConnectionManagerHelper.getConnectionRootFileName( vfsConnectionDetails );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testGetConnectionRootFileNameThrowsIllegalArgumentGivenConnectionHasEmptyName() {
    when( vfsConnectionDetails.getName() ).thenReturn( "" );
    vfsConnectionManagerHelper.getConnectionRootFileName( vfsConnectionDetails );
  }
  // endregion

  // region getConnectionRootProviderFileName( . )
  @Test
  public void testGetConnectionRootProviderUriReturnsCorrectUriWhenRootPath() throws KettleException {
    FileName fileName = vfsConnectionManagerHelper
      .getConnectionRootProviderFileName( vfsConnectionFileNameTransformer, vfsConnectionDetails );

    assertNotNull( fileName );
    assertEquals( TEST_CONNECTION_TYPE + "://" + TEST_ROOT_PATH, fileName.getURI() );
  }

  @Test
  public void testGetConnectionRootProviderFileNameIncludesNormalizedRootPath() throws KettleException {

    when( vfsConnectionDetails.getRootPath() ).thenReturn( "./sub-folder/\\../other-sub-folder//" );

    FileName fileName = vfsConnectionManagerHelper
      .getConnectionRootProviderFileName( vfsConnectionFileNameTransformer, vfsConnectionDetails );

    assertNotNull( fileName );
    assertEquals( TEST_CONNECTION_TYPE + "://" + "other-sub-folder", fileName.getURI() );
  }

  @Test( expected = KettleException.class )
  public void testGetConnectionRootProviderFileNameThrowsWhenRootPathHasInvalidRelativeSegments()
    throws KettleException {

    when( vfsConnectionDetails.getRootPath() ).thenReturn( "../other-connection" );

    vfsConnectionManagerHelper
      .getConnectionRootProviderFileName( vfsConnectionFileNameTransformer, vfsConnectionDetails );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testGetConnectionRootProviderFileNameThrowsWhenConnectionNameIsNull() throws KettleException {

    when( vfsConnectionDetails.getName() ).thenReturn( null );
    when( vfsConnectionDetails.getRootPath() ).thenReturn( "root/path" );

    vfsConnectionManagerHelper
      .getConnectionRootProviderFileName( vfsConnectionFileNameTransformer, vfsConnectionDetails );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testGetConnectionRootProviderFileNameThrowsWhenConnectionNameIsEmpty() throws KettleException {

    when( vfsConnectionDetails.getName() ).thenReturn( "" );
    when( vfsConnectionDetails.getRootPath() ).thenReturn( "root/path" );

    vfsConnectionManagerHelper
      .getConnectionRootProviderFileName( vfsConnectionFileNameTransformer, vfsConnectionDetails );
  }
  // endregion

  // region test(..)
  @Test
  public void testTestReturnsTrueWhenRootPathIsValid() throws KettleException {
    assertTrue( vfsConnectionManagerHelper.test(  connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );
  }

  @Test
  public void testTestReturnsTrueWithValidButNotNormalizedRootPath() throws KettleException {

    when( vfsConnectionDetails.getRootPath() ).thenReturn( "./sub-folder/\\../other-sub-folder//" );

    assertTrue( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );
  }

  @Test
  public void testTestReturnsFalseWhenRootPathHasInvalidRelativeSegments() throws KettleException {

    when( vfsConnectionDetails.getRootPath() ).thenReturn( "../other-connection" );

    assertFalse( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );
  }

  @Test
  public void testTestReturnsFalseWhenConnectionNameIsNull() throws KettleException {

    when( vfsConnectionDetails.getName() ).thenReturn( null );

    assertFalse( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );
  }

  @Test
  public void testTestReturnsFalseWhenProviderTestReturnsFalse() throws KettleException {

    when( vfsConnectionProvider.test( vfsConnectionDetails ) ).thenReturn( false );

    assertFalse( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );
  }

  @Test
  public void testTestReturnsTrueWhenRootPathInvalidAndConnectionDoesNotSupportRootPath() throws KettleException {

    when( vfsConnectionDetails.isRootPathSupported() ).thenReturn( false );

    when( vfsConnectionDetails.getRootPath() ).thenReturn( "../invalid" );

    assertTrue( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );
  }

  @Test
  public void testTestReturnsTrueWhenRootPathInvalidAndOptionsToIgnoreRootPath() throws KettleException {

    when( vfsConnectionDetails.getRootPath() ).thenReturn( "../invalid" );

    assertTrue( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsRootPathIgnored() ) );
  }

  @Test
  public void testTestChecksRootPathWhenGivenNullOptions() throws KettleException {

    assertTrue( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, null ) );

    // ---

    when( vfsConnectionDetails.getRootPath() ).thenReturn( "../invalid" );

    assertFalse( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, null ) );
  }

  @Test
  public void testTestReturnsFalseWhenResolvedRootPathIsNullAndRequired() throws Exception {

    when( vfsConnectionDetails.getRootPath() ).thenReturn( null );

    assertFalse( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );

    verify( vfsConnectionManagerHelper, never() ).getConnectionRootProviderFileName( any(), any() );

    // ---

    when( vfsConnectionDetails.getRootPath() ).thenReturn( "./." );

    assertFalse( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );

    verify( vfsConnectionManagerHelper, never() ).getConnectionRootProviderFileName( any(), any() );
  }

  @Test
  public void testTestReturnsTrueWhenResolvedRootPathIsNullOrEmptyAndNotRequired() throws Exception {

    when( vfsConnectionDetails.isRootPathRequired() ).thenReturn( false );

    when( vfsConnectionDetails.getRootPath() ).thenReturn( null );

    assertTrue( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );

    verify( vfsConnectionManagerHelper, never() ).getConnectionRootProviderFileName( any(), any() );

    // ---

    when( vfsConnectionDetails.getRootPath() ).thenReturn( "./." );

    assertTrue( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );

    verify( vfsConnectionManagerHelper, never() ).getConnectionRootProviderFileName( any(), any() );
  }

  @Test
  public void testTestReturnsFalseWhenRootPathFileDoesNotExist() throws Exception {

    when( connectionRootProviderFileObject.exists() ).thenReturn( false );

    assertFalse( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );
  }

  @Test
  public void testTestReturnsFalseWhenRootPathFileExistsButIsNotAFolder() throws Exception {

    when( connectionRootProviderFileObject.isFolder() ).thenReturn( false );

    assertFalse( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );
  }

  @Test
  public void testTestReturnsFalseWhenRootPathFileExistsButCheckingIfFolderThrows() throws Exception {

    when( connectionRootProviderFileObject.isFolder() ).thenThrow( FileSystemException.class );

    assertFalse( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, getTestOptionsCheckRootPath() ) );
  }
  // endregion

  // region usesBuckets
  @Test
  public void testUsesBucketsReturnsFalseIfNoBuckets() throws KettleException {

    when( vfsConnectionDetails.hasBuckets() ).thenReturn( false );
    // when( vfsConnectionDetails.getRootPath() ).thenReturn();
    assertFalse( vfsConnectionManagerHelper.usesBuckets( vfsConnectionDetails ) );
  }

  @Test
  public void testUsesBucketsReturnsTrueIfHasBucketsAndNoRootPath() throws KettleException {

    when( vfsConnectionDetails.hasBuckets() ).thenReturn( true );
    when( vfsConnectionDetails.getRootPath() ).thenReturn( null );
    assertTrue( vfsConnectionManagerHelper.usesBuckets( vfsConnectionDetails ) );
  }

  @Test
  public void testUsesBucketsReturnsFalseIfHasBucketsAndRootPath() throws KettleException {

    when( vfsConnectionDetails.hasBuckets() ).thenReturn( true );
    assertFalse( vfsConnectionManagerHelper.usesBuckets( vfsConnectionDetails ) );
  }
  // endregion

  // region Helpers
  @NonNull
  private static VFSConnectionTestOptions getTestOptionsCheckRootPath() {
    return new VFSConnectionTestOptions( false );
  }

  @NonNull
  private static VFSConnectionTestOptions getTestOptionsRootPathIgnored() {
    return new VFSConnectionTestOptions( true );
  }
  // endregion
}
