/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.amazon.s3.fileoutput;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.VFS;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.amazon.s3.provider.S3Provider;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class S3FileOutputHelperTest extends TestCase {

  public void testSetMinimalWidthActionReturnsUpdatedFields() throws Exception {
    S3FileOutputHelper helper = new S3FileOutputHelper();
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    S3FileOutputMeta s3Meta = mock( S3FileOutputMeta.class );
    TextFileField field = mock( TextFileField.class );
    Map<String, String> queryParams = Map.of( "stepName", "step1" );

    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( s3Meta );
    when( s3Meta.getOutputFields() ).thenReturn( new TextFileField[] { field } );
    when( field.getName() ).thenReturn( "field1" );
    when( field.getTypeDesc() ).thenReturn( "String" );
    when( field.getType() ).thenReturn( ValueMetaInterface.TYPE_STRING );
    when( field.getCurrencySymbol() ).thenReturn( "$" );
    when( field.getDecimalSymbol() ).thenReturn( "." );
    when( field.getGroupingSymbol() ).thenReturn( "," );
    when( field.getNullString() ).thenReturn( "NULL" );

    JSONObject result = helper.setMinimalWidthAction( transMeta, queryParams );
    assertNotNull( result.get( "updatedData" ) );
    assertEquals( 1, ( (JSONArray) result.get( "updatedData" ) ).size() );
  }

  public void testSetMinimalWidthActionWithEmptyStepNameReturnsEmptyArray() throws Exception {
    S3FileOutputHelper helper = new S3FileOutputHelper();
    TransMeta transMeta = mock( TransMeta.class );
    Map<String, String> queryParams = Map.of( "stepName", "" );

    JSONObject result = helper.setMinimalWidthAction( transMeta, queryParams );
    assertNotNull( result.get( "updatedData" ) );
    assertEquals( 0, ( (JSONArray) result.get( "updatedData" ) ).size() );
  }

  public void testGetEOLFormatsActionReturnsFormatsArray() {
    S3FileOutputHelper helper = new S3FileOutputHelper();
    JSONObject result = helper.getEOLFormatsAction();
    assertNotNull( result.get( "formats" ) );
  }

  public void testShowFilesActionReturnsFilteredFiles() {
    S3FileOutputHelper helper = new S3FileOutputHelper();
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    S3FileOutputMeta s3Meta = mock( S3FileOutputMeta.class );
    Map<String, String> queryParams = Map.of( "stepName", "step1", "filter", "file", "isRegex", "false" );

    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( s3Meta );
    when( s3Meta.getFiles( transMeta ) ).thenReturn( new String[] { "file1.txt", "other.txt" } );

    JSONObject result = helper.showFilesAction( transMeta, queryParams );
    JSONArray files = (JSONArray) result.get( "files" );
    assertTrue( files.contains( "file1.txt" ) );
    assertFalse( files.contains( "other.txt" ) );
  }

  public void testShowFilesActionWithRegexReturnsMatchingFiles() {
    S3FileOutputHelper helper = new S3FileOutputHelper();
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    S3FileOutputMeta s3Meta = mock( S3FileOutputMeta.class );
    Map<String, String> queryParams = Map.of( "stepName", "step1", "filter", "file\\d+\\.txt", "isRegex", "true" );

    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( s3Meta );
    when( s3Meta.getFiles( transMeta ) ).thenReturn( new String[] { "file1.txt", "file2.txt", "other.txt" } );

    JSONObject result = helper.showFilesAction( transMeta, queryParams );
    JSONArray files = (JSONArray) result.get( "files" );
    assertTrue( files.contains( "file1.txt" ) );
    assertTrue( files.contains( "file2.txt" ) );
    assertFalse( files.contains( "other.txt" ) );
  }

  public void testShowFilesActionWithEmptyStepNameReturnsEmptyArray() {
    S3FileOutputHelper helper = new S3FileOutputHelper();
    TransMeta transMeta = mock( TransMeta.class );
    Map<String, String> queryParams = Map.of( "stepName", "" );

    JSONObject result = helper.showFilesAction( transMeta, queryParams );
    assertNotNull( result.get( "files" ) );
    assertEquals( 0, ( (JSONArray) result.get( "files" ) ).size() );
  }

  public void testListS3BucketsActionReturnsBuckets() throws Exception {
    S3FileOutputHelper helper = new S3FileOutputHelper();
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    S3FileOutputMeta s3Meta = mock( S3FileOutputMeta.class );
    Map<String, String> queryParams = Map.of( "stepName", "step1" );
    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( s3Meta );
    when( s3Meta.getAccessKey() ).thenReturn( "ak" );
    when( s3Meta.getSecretKey() ).thenReturn( "sk" );
    S3Provider s3Provider = mock( S3Provider.class );
    VFSRoot bucket = mock( VFSRoot.class );
    Field s3ProviderField = S3FileOutputHelper.class.getDeclaredField( "s3Provider" );
    s3ProviderField.setAccessible( true );
    s3ProviderField.set( helper, s3Provider );
    when( bucket.getName() ).thenReturn( "bucket1" );
    when( bucket.getModifiedDate() ).thenReturn( null );
    when( s3Provider.getLocations( any() ) ).thenReturn( List.of( bucket ) );
    JSONObject result = helper.listS3BucketsAction( transMeta, queryParams );
    JSONArray buckets = (JSONArray) result.get( "buckets" );
    assertEquals( 1, buckets.size() );
    JSONObject bucketJson = (JSONObject) buckets.get( 0 );
    assertEquals( "bucket1", bucketJson.get( "name" ) );
    assertEquals( "bucket", bucketJson.get( "type" ) );
    assertTrue( bucketJson.get( "path" ).toString().startsWith( "s3://" ) );
  }

  public void testListS3BucketsActionHandlesException() throws Exception {
    S3FileOutputHelper helper = new S3FileOutputHelper();

    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    S3FileOutputMeta s3Meta = mock( S3FileOutputMeta.class );

    Map<String, String> queryParams = Map.of( "stepName", "step1" );

    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( s3Meta );

    S3Provider s3Provider = mock( S3Provider.class );

    Field s3ProviderField = S3FileOutputHelper.class.getDeclaredField( "s3Provider" );
    s3ProviderField.setAccessible( true );
    s3ProviderField.set( helper, s3Provider );

    when( s3Provider.getLocations( any() ) ).thenThrow( new RuntimeException( "fail" ) );

    JSONObject result = helper.listS3BucketsAction( transMeta, queryParams );
    assertEquals( "Action failed", result.get( "actionStatus" ) );
  }

  public void testListS3ContentsActionSuccess() throws Exception {
    // Spy so we can stub processS3Path
    S3FileOutputHelper helper = spy( new S3FileOutputHelper() );

    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    S3FileOutputMeta meta = mock( S3FileOutputMeta.class );

    Map<String, String> queryParams = Map.of( "stepName", "step1" );

    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getFileName() ).thenReturn( "s3://my-bucket/" );

    // Stub processS3Path to simulate success and populate contents
    doAnswer( invocation -> {
      JSONArray contents = invocation.getArgument( 3 );
      JSONObject item = new JSONObject();
      item.put(  "name", "file1.txt"  );
      item.put( "type", "file" );
      contents.add( item );
      return null;
    } ).when( helper ).processS3Path(  any(), any(), any(), any()  );

    JSONObject result = helper.listS3ContentsAction( transMeta, queryParams );

    JSONArray contents = (JSONArray) result.get( "contents" );
    assertNotNull( contents );
    assertEquals( 1, contents.size() );

    JSONObject file = (JSONObject) contents.get( 0 );
    assertEquals( "file1.txt", file.get( "name" ) );
    assertEquals( "file", file.get( "type" ) );
  }

  public void testListS3ContentsActionWithNullPath() {
    S3FileOutputHelper helper = new S3FileOutputHelper();

    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    S3FileOutputMeta meta = mock( S3FileOutputMeta.class );

    Map<String, String> queryParams = Map.of( "stepName", "step1" );

    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getFileName() ).thenReturn( null );
    JSONObject result = helper.listS3ContentsAction( transMeta, queryParams );

    assertEquals( "Action failed", result.get( "actionStatus" ) );
    assertEquals( "Invalid S3 path", result.get( "error" ) );

    assertNull( result.get( "contents" ) );
  }

  public void testProcessS3PathListsFoldersThenFiles() throws Exception {
    Path dir = Files.createTempDirectory( "s3-file-output" );
    try {
      Files.createDirectory( dir.resolve( "sub" ) );
      Files.writeString( dir.resolve( "file1.txt" ), "abc" );
      S3Provider s3Provider = providerServing( "my-bucket/folder1", dir );

      JSONArray contents = new JSONArray();
      helperWith( s3Provider ).processS3Path( transMetaWithStep(), Map.of( "stepName", "step1" ),
        "s3://my-bucket/folder1/", contents );

      assertEquals( 2, contents.size() );
      JSONObject folder = (JSONObject) contents.get( 0 );
      assertEquals( "folder", folder.get( "type" ) );
      assertEquals( "sub", folder.get( "name" ) );
      assertEquals( "s3://my-bucket/folder1/sub/", folder.get( "path" ) );
      JSONObject file = (JSONObject) contents.get( 1 );
      assertEquals( "file", file.get( "type" ) );
      assertEquals( "file1.txt", file.get( "name" ) );
      assertEquals( "s3://my-bucket/folder1/file1.txt", file.get( "path" ) );
      assertEquals( 3L, file.get( "size" ) );
      assertNotNull( file.get( "lastModified" ) );
    } finally {
      FileUtils.deleteDirectory( dir.toFile() );
    }
  }

  public void testProcessS3PathOnFileReturnsNoContents() throws Exception {
    Path file = Files.createTempFile( "s3-file-output", ".txt" );
    try {
      S3Provider s3Provider = providerServing( "my-bucket/out.txt", file );

      JSONArray contents = new JSONArray();
      helperWith( s3Provider ).processS3Path( transMetaWithStep(), Map.of( "stepName", "step1" ),
        "s3://my-bucket/out.txt", contents );

      assertTrue( contents.isEmpty() );
    } finally {
      Files.delete( file );
    }
  }

  private static S3Provider providerServing( String s3Path, Path localPath ) throws Exception {
    S3Provider s3Provider = mock( S3Provider.class );
    when( s3Provider.prepare( any() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    when( s3Provider.getDirectFile( any(), any(), eq( s3Path ) ) )
      .thenReturn( VFS.getManager().toFileObject( localPath.toFile() ) );
    return s3Provider;
  }

  private static S3FileOutputHelper helperWith( S3Provider s3Provider ) throws Exception {
    S3FileOutputHelper helper = new S3FileOutputHelper();
    Field providerField = S3FileOutputHelper.class.getDeclaredField( "s3Provider" );
    providerField.setAccessible( true );
    providerField.set( helper, s3Provider );
    return helper;
  }

  private static TransMeta transMetaWithStep() {
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( mock( S3FileOutputMeta.class ) );
    return transMeta;
  }

  public void testListS3ContentsActionException() throws Exception {
    S3FileOutputHelper helper = spy( new S3FileOutputHelper() );

    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    S3FileOutputMeta meta = mock( S3FileOutputMeta.class );

    Map<String, String> queryParams = Map.of( "stepName", "step1" );

    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getFileName() ).thenReturn( "s3://bucket" );

    doThrow( new RuntimeException( "boom" ) )
        .when( helper )
        .processS3Path( any(), any(), any(), any() );

    JSONObject result = helper.listS3ContentsAction( transMeta, queryParams );

    assertEquals( "Action failed", result.get( "actionStatus" ) );
  }

  public void testCreateS3DetailsAccessKeyAuth() {
    S3FileOutputHelper helper = new S3FileOutputHelper();

    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    S3FileOutputMeta meta = mock( S3FileOutputMeta.class );

    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getAccessKey() ).thenReturn( "ak" );
    when( meta.getSecretKey() ).thenReturn( "sk" );

    Map<String, String> params = Map.of( "stepName", "step1" );

    S3Details details = helper.createS3DetailsFromParams( transMeta, params );

    assertEquals( "0", details.getAuthType() );
    assertEquals( "ak", details.getAccessKey() );
  }

  public void testCreateS3DetailsCredentialsFileAuth() {
    S3FileOutputHelper helper = new S3FileOutputHelper();

    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    S3FileOutputMeta meta = mock( S3FileOutputMeta.class );

    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getAccessKey() ).thenReturn( null );
    when( meta.getSecretKey() ).thenReturn( null );

    Map<String, String> params = Map.of( "stepName", "step1" );

    S3Details details = helper.createS3DetailsFromParams( transMeta, params );

    assertEquals( "1", details.getAuthType() );
    assertEquals( "default", details.getProfileName() );
    assertNotNull( details.getCredentialsFilePath() );
  }

  public void testHandleStepActionUnknownMethod() {
    S3FileOutputHelper helper = new S3FileOutputHelper();
    JSONObject result = helper.handleStepAction(
        "unknownMethod",
        mock( TransMeta.class ),
        Map.of()
    );
    assertEquals( "Action failed with method not found", result.get( "actionStatus" ) );
  }

  public void testHandleStepActionHandlesException() {
    S3FileOutputHelper helper = spy( new S3FileOutputHelper() );

    TransMeta transMeta = mock( TransMeta.class );

    doThrow( new RuntimeException( "boom" ) )
        .when( helper )
        .showFilesAction( any(), any() );
    JSONObject result = helper.handleStepAction(
        "showFiles",
        transMeta,
        Map.of( "stepName", "step1" )
    );

    assertEquals( "Action failed", result.get( "actionStatus" ) );
  }
}