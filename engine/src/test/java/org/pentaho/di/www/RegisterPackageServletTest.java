/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.www;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.www.service.zip.ZipService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegisterPackageServletTest {

  protected RegisterPackageServlet servlet;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Before
  public void setup() {
    servlet = new RegisterPackageServlet();
  }

  @Test
  public void testGetContextPath() {
    assertEquals( "/kettle/registerPackage", servlet.getContextPath() );
  }

  @Test
  public void testIsJobHttpServletRequest() {

    // CASE: job
    HttpServletRequest requestJob = Mockito.mock( HttpServletRequest.class );
    when(requestJob.getParameter( RegisterPackageServlet.PARAMETER_TYPE ) ).thenReturn( "job" );

    assertTrue( servlet.isJob( requestJob ) );


    // CASE: transformation
    HttpServletRequest requestTrans = Mockito.mock( HttpServletRequest.class );
    when(requestJob.getParameter( RegisterPackageServlet.PARAMETER_TYPE ) ).thenReturn( "trans" );

    assertFalse( servlet.isJob( requestTrans ) );

  }

  @Test
  public void testIsJobString() {

    // CASE: job
    assertTrue( servlet.isJob( "job" ) );

    // CASE: Transformation
    assertFalse( servlet.isJob( "trans" ) );

    // CASE: random string
    assertFalse( servlet.isJob( "kettle" ) );

    // CASE: null
    String type = null;
    assertFalse( servlet.isJob( type ) );

    // CASE: empty string
    assertFalse( servlet.isJob( "" ) );

  }

  @Test
  public void testUseXML() {
    assertTrue( servlet.useXML(null) );
  }

  @Test
  public void testGetStartFileUrl() {
    String archiveUrl = "/tmp/some/path";
    String requestLoad = "export_33b89rnd04mglfh.zip";
    String expectedUrl = applyFileSeperator( "/tmp/some/path/export_33b89rnd04mglfh.zip" );

    assertEquals( expectedUrl, servlet.getStartFileUrl( archiveUrl, requestLoad) );
  }

  @Test
  public void testConcat() {
    String basePath = "/tmp/some/path";
    String relativePath = "that/is/temporary";
    String expectedPath = applyFileSeperator( "/tmp/some/path/that/is/temporary" );

    // CASE 1: Add separator
    assertEquals( expectedPath, servlet.concat( basePath, relativePath ) );

    // CASE 2: Don't add separator
    assertEquals( expectedPath, servlet.concat( basePath + "/", relativePath) );
  }

  @Test
  public void testGetConfigNode() throws Exception {
    try( MockedStatic<XMLHandler> xmlHandlerMockedStatic = mockStatic( XMLHandler.class ) ) {
      // Variables
      String archiveUrl = "/tmp/dafajfkdh/somePath/sub";
      String fileName = "execution_configuration__.xml";
      String xmlTag = "execution_configuration";
      String configUrl = applyFileSeperator( "/tmp/dafajfkdh/somePath/sub/execution_configuration__.xml" );

      Document configDoc = Mockito.mock( Document.class );
      Node node = Mockito.mock( Node.class );

      // SETUP
      xmlHandlerMockedStatic.when( () -> XMLHandler.loadXMLFile( eq( configUrl ) ) ).thenReturn( configDoc );
      xmlHandlerMockedStatic.when( () -> XMLHandler.getSubNode( eq( configDoc ), eq( xmlTag ) ) ).thenReturn( node );
      assertEquals( node, servlet.getConfigNode( archiveUrl, fileName, xmlTag ) );
    }
  }

  @Test
  public void testCreateTempDirString_TmpDir() {

    String systemTmpDir = System.getProperty("java.io.tmpdir" );

    String tempDir = servlet.createTempDirString();

    assertTrue( tempDir.contains( systemTmpDir ) );

  }

  @Test
  public void testCreateTempDirString_Unique() {

    // non-exhaustive test get unique directory
    int testSize = 10000;
    Set<String> set = new HashSet<>( testSize );

    for ( int i = 0 ; i < testSize; ++i )
    {
      set.add( servlet.createTempDirString() );
    }

    assertEquals( testSize, set.size() );
  }

  @Test
  public void testCreateTempDirString() {

    String baseDirectory = "/root/somePath/anotherPath";
    String folderName = "folderName";
    String expectedPath = applyFileSeperator( "/root/somePath/anotherPath/folderName" );

    assertEquals( expectedPath, servlet.createTempDirString( baseDirectory, folderName) );
  }

  @Test
  public void testCopyRequestToDirectory_Exception1() throws Exception {

    expectedEx.expect( KettleException.class );
    expectedEx.expectMessage("Could not copy request to directory");

    HttpServletRequest request = Mockito.mock( HttpServletRequest.class);

    when( request.getInputStream() ).thenThrow( IOException.class );

    servlet.copyRequestToDirectory( request, "/tmp/path");

  }

  @Test
  public void testCopyRequestToDirectory_Exception2() throws Exception {

    try ( MockedStatic<KettleVFS> kettleVFSMockedStatic = mockStatic( KettleVFS.class ) ) {
      expectedEx.expect( MockitoException.class );

      InputStream inputStream = Mockito.mock( InputStream.class );

      kettleVFSMockedStatic.when( () -> KettleVFS.getFileObject( anyString() ) ).thenThrow( IOException.class );

      servlet.copyRequestToDirectory( inputStream, "/tmp/path" );
    }
  }

  @Test
  public void testCopyAndClose() throws Exception {

    try ( MockedStatic<IOUtils> ioUtilsMockedStatic1 = mockStatic( IOUtils.class ) ) {
      int bytesCopied = 10;
      ioUtilsMockedStatic1.when( () -> IOUtils.copy( any( InputStream.class ), any( OutputStream.class ) ) ).thenReturn( bytesCopied );
      // Variables
      InputStream inputStream = Mockito.mock( InputStream.class );
      OutputStream outputStream = Mockito.mock( OutputStream.class );

      // EXECUTE
      servlet.copyAndClose( inputStream, outputStream );

      ioUtilsMockedStatic1.verify( () -> IOUtils.copy( eq( inputStream ), eq( outputStream ) ) );
      ioUtilsMockedStatic1.verify( () -> IOUtils.closeQuietly( eq( outputStream ) ) );
    }

  }

  @Test
  public void testExtract_String() throws Exception {

    ZipService mockZipService = Mockito.mock( ZipService.class );
    RegisterPackageServlet.setZipService( mockZipService );
    // a valid filepath is not necessary for test
    String path1 = "/root/subPath1/subPath2/zipFilePath002.zip";
    String expectedDirectory = applyFileSeperator( "/root/subPath1/subPath2" );

    assertEquals( expectedDirectory, servlet.extract( path1 ) );
  }

  @Test
  public void testExtract_StringString() throws Exception {

    ZipService mockZipService = Mockito.mock( ZipService.class );
    RegisterPackageServlet.setZipService( mockZipService );
    String path1 = "zipFilePath002";
    String path2 = "destinationDirectory003";

    servlet.extract( path1, path2 );

    verify( mockZipService ).extract( path1, path2 );

  }

  @Test
  public void testDeleteArchive_String() {
    try ( MockedStatic<FileUtils> fileUtilsMockedStatic1 = mockStatic( FileUtils.class ) ) {

      String fileName = "someRandom.zip"; // a valid filepath is not necessary for test

      File expectedFile = new File( fileName );

      fileUtilsMockedStatic1.when( () -> FileUtils.deleteQuietly( any( File.class ) ) ).thenReturn( true );

      servlet.deleteArchive( fileName );

      fileUtilsMockedStatic1.verify( () -> FileUtils.deleteQuietly( eq( expectedFile ) ) );
      FileUtils.deleteQuietly( expectedFile );
    }
  }

  private String applyFileSeperator( String path ){
    return path.replace( "/", File.separator );
  }
  
}
