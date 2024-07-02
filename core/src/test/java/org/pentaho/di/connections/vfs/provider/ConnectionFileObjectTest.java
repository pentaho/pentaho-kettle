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

package org.pentaho.di.connections.vfs.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.operations.FileOperations;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSConnectionManagerHelper;

import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionFileObjectTest {
  ConnectionFileObject fileObject;
  ConnectionFileName fileName;

  AbstractFileObject<?> resolvedFileObject;

  ConnectionFileSystem connectionFileSystem;

  public static class ConnectionFileObjectForTesting extends ConnectionFileObject {
    @Nullable
    private final AbstractFileObject<?> resolvedFileObject;

    protected ConnectionFileObjectForTesting(
      @NonNull ConnectionFileName name,
      @NonNull ConnectionFileSystem fs,
      @Nullable AbstractFileObject<?> resolvedFileObject ) {
      super( name, fs );
      this.resolvedFileObject = resolvedFileObject;
    }

    @Nullable @Override
    public FileObject getResolvedFileObject() {
      return resolvedFileObject;
    }

    @NonNull @Override
    protected AbstractFileObject<?> requireResolvedFileObject() throws FileSystemException {
      if ( resolvedFileObject == null ) {
        throw mock( FileSystemException.class );
      }

      return resolvedFileObject;
    }

    @Override
    public long doGetContentSize() throws Exception {
      return super.doGetContentSize();
    }

    @Override
    public InputStream doGetInputStream() throws Exception {
      return super.doGetInputStream();
    }
  }

  @Before
  public void setup() throws FileSystemException {
    resolvedFileObject = mock( AbstractFileObject.class );

    ConnectionFileName rootFileName = new ConnectionFileName( null );

    FileSystemOptions fileSystemOptions = mock( FileSystemOptions.class );
    ConnectionManager manager = mock( ConnectionManager.class );
    VFSConnectionManagerHelper vfsManagerHelper = mock( VFSConnectionManagerHelper.class );

    connectionFileSystem =
      spy( new ConnectionFileSystem( rootFileName, fileSystemOptions, manager, vfsManagerHelper ) );

    fileName = mock( ConnectionFileName.class );

    fileObject = new ConnectionFileObjectForTesting(
      fileName,
      connectionFileSystem,
      resolvedFileObject );

  }

  @Test
  public void testGetTypeReturnsImaginary() throws FileSystemException {
    assertEquals( FileType.IMAGINARY, fileObject.getType() );
  }

  // region getContent et al.
  @Test
  public void testDoGetContentSizeReturns0() throws Exception {
    // For coverage. This method is abstract and must be overridden, however, it's not supposed to be called,
    // given getContent is overridden and delegated to the resolved file object.
    assertEquals( 0, fileObject.doGetContentSize() );
  }

  @Test
  public void testDelegatesGetContent() throws FileSystemException {
    FileContent fileContent = mock( FileContent.class );

    when( resolvedFileObject.getContent() ).thenReturn( fileContent );

    assertSame( fileContent, fileObject.getContent() );
  }

  @Test
  public void testDelegatesGetRandomAccessContent() throws FileSystemException {
    RandomAccessContent randomAccessContent = mock( RandomAccessContent.class );

    when( resolvedFileObject.getRandomAccessContent( any( RandomAccessMode.class ) ) )
      .thenReturn( randomAccessContent );

    assertSame( randomAccessContent, fileObject.getRandomAccessContent( RandomAccessMode.READ ) );
  }
  // endregion

  // region getInputStream et al.
  @Test
  public void testDoGetInputStreamReturnsNull() throws Exception {
    // For coverage. This method is not supposed to be called, given getInputStream is overridden and delegated to the
    // resolved file object. My guess is it's overridden for safety just in case it gets called by some subclass...
    assertNull( fileObject.doGetInputStream() );
  }

  @Test
  public void testDelegatesGetInputStream() throws FileSystemException {
    InputStream inputStream = mock( InputStream.class );

    when( resolvedFileObject.getInputStream() ).thenReturn( inputStream );

    assertSame( inputStream, fileObject.getInputStream() );
  }

  @Test
  public void testDelegatesGetInputStreamWithBufferSize() throws FileSystemException {
    InputStream inputStream = mock( InputStream.class );

    when( resolvedFileObject.getInputStream( anyInt() ) ).thenReturn( inputStream );

    assertSame( inputStream, fileObject.getInputStream( 10 ) );
  }
  // endregion

  // region getOutputStream et al.
  @Test
  public void testDelegatesGetOutputStream() throws FileSystemException {
    OutputStream outputStream = mock( OutputStream.class );

    when( resolvedFileObject.getOutputStream() ).thenReturn( outputStream );

    assertSame( outputStream, fileObject.getOutputStream() );
  }

  @Test
  public void testDelegatesGetOutputStreamWithAppend() throws FileSystemException {
    OutputStream outputStream = mock( OutputStream.class );

    when( resolvedFileObject.getOutputStream( anyBoolean() ) ).thenReturn( outputStream );

    assertSame( outputStream, fileObject.getOutputStream( true ) );
  }
  // endregion

  @Test
  public void testDelegatesGetFileOperations() throws FileSystemException {
    FileOperations fileOperations = mock( FileOperations.class );

    when( resolvedFileObject.getFileOperations() ).thenReturn( fileOperations );

    assertSame( fileOperations, fileObject.getFileOperations() );
  }

  @Test
  public void testDelegatesClose() throws FileSystemException {
    fileObject.close();

    verify( resolvedFileObject, times( 1 ) ).close();
  }

  @Test
  public void testDelegatesCopyFrom() throws FileSystemException {
    FileObject fromFileObject = mock( FileObject.class );
    FileSelector fileSelector = mock( FileSelector.class );

    fileObject.copyFrom( fromFileObject, fileSelector );

    verify( resolvedFileObject, times( 1 ) ).copyFrom( fromFileObject, fileSelector );
  }

  @Test
  public void testDelegatesMoveTo() throws FileSystemException {
    FileObject toFileObject = mock( FileObject.class );

    fileObject.moveTo( toFileObject );

    verify( resolvedFileObject, times( 1 ) ).moveTo( toFileObject );
  }

  @Test
  public void testDelegatesRefresh() throws FileSystemException {
    fileObject.refresh();

    verify( resolvedFileObject, times( 1 ) ).refresh();
  }

  @Test
  public void testDelegatesCreateFile() throws FileSystemException {
    fileObject.createFile();

    verify( resolvedFileObject, times( 1 ) ).createFile();
  }

  @Test
  public void testDelegatesCreateFolder() throws FileSystemException {
    fileObject.createFolder();

    verify( resolvedFileObject, times( 1 ) ).createFolder();
  }

  @Test
  public void testDelegatesDelete() throws FileSystemException {
    fileObject.delete();

    verify( resolvedFileObject, times( 1 ) ).delete();
  }

  @Test
  public void testDelegatesDeleteWithFileSelector() throws FileSystemException {
    FileSelector fileSelector = mock( FileSelector.class );

    fileObject.delete( fileSelector );

    verify( resolvedFileObject, times( 1 ) ).delete( fileSelector );
  }

  @Test
  public void testDelegatesDeleteAll() throws FileSystemException {
    fileObject.deleteAll();

    verify( resolvedFileObject, times( 1 ) ).deleteAll();
  }

  // region getChild(name)
  @Test
  public void testGetChildDelegatesAndWrapsReturnedResolvedChildFileObject() throws FileSystemException {
    FileObject childResolvedFileObject = mock( FileObject.class );
    when( resolvedFileObject.getChild( "childName" ) ).thenReturn( childResolvedFileObject );

    ConnectionFileObject childFileObject = mock( ConnectionFileObject.class );
    doReturn( childFileObject ).when( connectionFileSystem ).createChild( fileObject, childResolvedFileObject );

    FileObject resultFileObject = fileObject.getChild( "childName" );

    assertSame( childFileObject, resultFileObject );

    verify( resolvedFileObject, times( 1 ) ).getChild( "childName" );
  }

  @Test
  public void testGetChildDelegatesAndReturnsNullIfResolvedChildFileObjectIsNull() throws FileSystemException {
    FileObject childResolvedFileObject = mock( FileObject.class );
    when( resolvedFileObject.getChild( "childName" ) ).thenReturn( null );

    ConnectionFileObject childFileObject = mock( ConnectionFileObject.class );
    doReturn( childFileObject ).when( connectionFileSystem ).createChild( fileObject, childResolvedFileObject );

    FileObject resultFileObject = fileObject.getChild( "childName" );

    assertNull( resultFileObject );

    verify( resolvedFileObject, times( 1 ) ).getChild( "childName" );
  }
  // endregion

  // region getChildren()
  @Test
  public void testGetChildrenDelegatesAndWrapsResolvedChildFileObjects() throws FileSystemException {
    FileObject child1ResolvedFileObject = mock( FileObject.class );
    FileObject child2ResolvedFileObject = mock( FileObject.class );
    FileObject child3ResolvedFileObject = mock( FileObject.class );

    when( resolvedFileObject.getChildren() )
      .thenReturn( new FileObject[] {
        child1ResolvedFileObject,
        child2ResolvedFileObject,
        child3ResolvedFileObject
      } );

    ConnectionFileObject child1FileObject = mock( ConnectionFileObject.class );
    ConnectionFileObject child2FileObject = mock( ConnectionFileObject.class );
    ConnectionFileObject child3FileObject = mock( ConnectionFileObject.class );

    doReturn( child1FileObject ).when( connectionFileSystem ).createChild( fileObject, child1ResolvedFileObject );
    doReturn( child2FileObject ).when( connectionFileSystem ).createChild( fileObject, child2ResolvedFileObject );
    doReturn( child3FileObject ).when( connectionFileSystem ).createChild( fileObject, child3ResolvedFileObject );

    FileObject[] resultFileObjects = fileObject.getChildren();

    assertEquals( 3, resultFileObjects.length );
    assertSame( child1FileObject, resultFileObjects[ 0 ] );
    assertSame( child2FileObject, resultFileObjects[ 1 ] );
    assertSame( child3FileObject, resultFileObjects[ 2 ] );

    verify( resolvedFileObject, times( 1 ) ).getChildren();
  }
  // endregion

  @Test
  public void testGetOriginalURIStringDelegatesToGetNameToString() {
    when( fileName.toString() ).thenReturn( "pvfs://connection/test-name-to-string" );
    String result = fileObject.getOriginalURIString();

    assertEquals( "pvfs://connection/test-name-to-string", result );
  }

  @Test( expected = UnsupportedOperationException.class )
  public void testGetAELSafeURIString() {
    fileObject.getAELSafeURIString();
  }
}
