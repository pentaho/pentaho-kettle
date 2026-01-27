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

package org.pentaho.amazon.s3;

import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.AmazonS3;
import junit.framework.TestCase;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.amazon.s3.provider.S3Provider;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
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

  public void testProcessS3PathSuccess() throws Exception {
    S3FileOutputHelper helper = new S3FileOutputHelper();

    // Mocks
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    S3FileOutputMeta meta = mock( S3FileOutputMeta.class );
    S3Provider s3Provider = mock( S3Provider.class );
    AmazonS3 s3Client = mock( AmazonS3.class );
    ListObjectsV2Result listing = mock( ListObjectsV2Result.class );

    Map<String, String> queryParams = Map.of( "stepName", "step1" );

    // Inject s3Provider via reflection
    Field providerField = S3FileOutputHelper.class.getDeclaredField( "s3Provider" );
    providerField.setAccessible( true );
    providerField.set( helper, s3Provider );

    // TransMeta → StepMeta → Meta
    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getAccessKey() ).thenReturn( "ak" );
    when( meta.getSecretKey() ).thenReturn( "sk" );

    // Provider behavior
    when( s3Provider.prepare( any() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    when( s3Provider.getS3Client( any() ) ).thenReturn( s3Client );

    // Folder
    when( listing.getCommonPrefixes() ).thenReturn( List.of( "folder1/" ) );

    // File
    S3ObjectSummary summary = new S3ObjectSummary();
    summary.setKey( "folder1/file1.txt" );
    summary.setSize( 123L );

    when( listing.getObjectSummaries() ).thenReturn( List.of( summary ) );
    when( listing.isTruncated() ).thenReturn( false );
    when( listing.getNextContinuationToken() ).thenReturn( null );

    // S3 call
    when( s3Client.listObjectsV2( any( ListObjectsV2Request.class ) ) ).thenReturn( listing );
    JSONArray contents = new JSONArray();
    helper.processS3Path(
        transMeta,
        queryParams,
        "s3://my-bucket/folder1",
        contents
    );
    // Assertions
    assertEquals( 2, contents.size() );
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

  public void testAddFilesAndFoldersAddsFoldersAndFiles() {
    S3FileOutputHelper helper = new S3FileOutputHelper();

    ListObjectsV2Result listing = mock( ListObjectsV2Result.class );
    JSONArray contents = new JSONArray();

    // Mock folders
    when( listing.getCommonPrefixes() ).thenReturn( List.of( "folder1/" ) );

    // Mock files
    S3ObjectSummary summary = new S3ObjectSummary();
    summary.setKey( "file1.txt" );
    summary.setSize( 100L );

    when( listing.getObjectSummaries() ).thenReturn( List.of( summary ) );

    helper.addFilesAndFolders( listing, "bucket", "", contents );

    assertEquals( 2, contents.size() );

    JSONObject folder = (JSONObject) contents.get( 0 );
    assertEquals( "folder", folder.get( "type" ) );

    JSONObject file = (JSONObject) contents.get( 1 );
    assertEquals( "file", file.get( "type" ) );
    assertEquals( 100L, file.get( "size" ) );
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