/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright ( C ) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
package org.pentaho.di.trans.steps.s3csvinput;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class S3CsvInputHelperTest {

  private S3CsvInputHelper helper;
  private TransMeta transMeta;
  private StepMeta stepMeta;
  private S3CsvInputMeta meta;

  @Before
  public void setUp() {
    helper = new S3CsvInputHelper();
    transMeta = mock( TransMeta.class );
    stepMeta = mock( StepMeta.class );
    meta = mock( S3CsvInputMeta.class );
  }

  @Test
  @SuppressWarnings( "deprecation" )
  public void listsS3BucketsSuccessfully() {
    AmazonS3 s3Client = mock( AmazonS3.class );
    Bucket bucket1 = new Bucket( "bucket1" );
    Bucket bucket2 = new Bucket( "bucket2" );
    List<Bucket> buckets = Arrays.asList( bucket1, bucket2 );
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getS3Client( any( VariableSpace.class ) ) ).thenReturn( s3Client );
    when( s3Client.listBuckets() ).thenReturn( buckets );
    JSONObject response = helper.listS3BucketsAction( transMeta, queryParams );
    assertEquals( "Action successful", response.get( "actionStatus" ) );
    assertNotNull( response.get( "buckets" ) );
    JSONArray bucketsArray = (JSONArray) response.get( "buckets" );
    assertEquals( 2, bucketsArray.size() );
  }

  @Test
  public void failsToListS3BucketsWhenStepNotFound() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "nonExistentStep" );
    when( transMeta.findStep( "nonExistentStep" ) ).thenReturn( null );

    JSONObject response = helper.listS3BucketsAction( transMeta, queryParams );

    assertEquals( "Action failed", response.get( "actionStatus" ) );
  }

  @Test
  public void failsToListS3BucketsWhenStepNameMissing() {
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = helper.listS3BucketsAction( transMeta, queryParams );
    assertEquals( "Action failed", response.get( "actionStatus" ) );
  }

  @Test
  public void listsS3ObjectsSuccessfully() {
    AmazonS3 s3Client = mock( AmazonS3.class );
    ObjectListing objectListing = mock( ObjectListing.class );
    ObjectMetadata metadata = mock( ObjectMetadata.class );
    S3ObjectSummary summary1 = new S3ObjectSummary();
    summary1.setKey( "file1.csv" );
    S3ObjectSummary summary2 = new S3ObjectSummary();
    summary2.setKey( "file2.csv" );
    List<S3ObjectSummary> summaries = Arrays.asList( summary1, summary2 );
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getBucket() ).thenReturn( "test-bucket" );
    when( meta.getS3Client( any( VariableSpace.class ) ) ).thenReturn( s3Client );
    when( s3Client.doesBucketExistV2( "test-bucket" ) ).thenReturn( true );
    when( s3Client.listObjects( "test-bucket" ) ).thenReturn( objectListing );
    when( objectListing.getObjectSummaries() ).thenReturn( summaries );
    when( s3Client.getObjectMetadata( "test-bucket", "file1.csv" ) ).thenReturn( metadata );
    when( s3Client.getObjectMetadata( "test-bucket", "file2.csv" ) ).thenReturn( metadata );
    when( metadata.getContentLength() ).thenReturn( 1024L );
    JSONObject response = helper.listS3ObjectsAction( transMeta, queryParams );
    assertEquals( "Action successful", response.get( "actionStatus" ) );
    assertNotNull( response.get( "objects" ) );
    JSONArray objectsArray = (JSONArray) response.get( "objects" );
    assertEquals( 2, objectsArray.size() );
  }

  @Test
  public void failsToListS3ObjectsWhenBucketNameMissing() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getBucket() ).thenReturn( "" );
    JSONObject response = helper.listS3ObjectsAction( transMeta, queryParams );

    assertEquals( "Action failed", response.get( "actionStatus" ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void failsToListS3ObjectsWhenStepNotFound() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "nonExistentStep" );
    when( transMeta.findStep( "nonExistentStep" ) ).thenReturn( null );
    JSONObject response = helper.listS3ObjectsAction( transMeta, queryParams );
    assertEquals( "Action failed", response.get( "actionStatus" ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void extractsFieldsFromCsvSuccessfully() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getBucket() ).thenReturn( "test-bucket" );
    when( meta.getFilename() ).thenReturn( "test.csv" );
    JSONObject response = helper.getFieldsFromCsvAction( transMeta, queryParams );
    assertNotNull( response );
    assertNotNull( response.get( "actionStatus" ) );
  }

  @Test
  public void failsToExtractFieldsWhenStepNotFound() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "nonExistentStep" );
    when( transMeta.findStep( "nonExistentStep" ) ).thenReturn( null );
    JSONObject response = helper.getFieldsFromCsvAction( transMeta, queryParams );
    assertEquals( "Action failed", response.get( "actionStatus" ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void failsToExtractFieldsWhenStepNameMissing() {
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = helper.getFieldsFromCsvAction( transMeta, queryParams );
    assertEquals( "Action failed", response.get( "actionStatus" ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void handlesExceptionInListS3Buckets() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getS3Client( any( VariableSpace.class ) ) ).thenThrow( new RuntimeException( "S3 connection failed" ) );
    JSONObject response = helper.listS3BucketsAction( transMeta, queryParams );
    assertEquals( "Action failed", response.get( "actionStatus" ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void handlesExceptionInListS3Objects() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getBucket() ).thenReturn( "test-bucket" );
    when( meta.getS3Client( any( VariableSpace.class ) ) ).thenThrow( new RuntimeException( "S3 connection failed" ) );
    JSONObject response = helper.listS3ObjectsAction( transMeta, queryParams );
    assertEquals( "Action failed", response.get( "actionStatus" ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void handlesExceptionInGetFieldsFromCsv() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getBucket() ).thenReturn( "test-bucket" );
    when( meta.getFilename() ).thenReturn( "test.csv" );
    when( meta.getS3Client( any( VariableSpace.class ) ) ).thenThrow( new RuntimeException( "S3 connection failed" ) );
    JSONObject response = helper.getFieldsFromCsvAction( transMeta, queryParams );
    assertEquals( "Action failed", response.get( "actionStatus" ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void testStepActionRouting() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    // Test routing to listS3Buckets
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    JSONObject response1 = helper.handleStepAction( "listS3Buckets", transMeta, queryParams );
    assertNotNull( response1 );
    assertNotNull( response1.get( "actionStatus" ) );

    // Test routing to listS3Objects
    when( meta.getBucket() ).thenReturn( "test-bucket" );
    JSONObject response2 = helper.handleStepAction( "listS3Objects", transMeta, queryParams );
    assertNotNull( response2 );
    assertNotNull( response2.get( "actionStatus" ) );

    // Test routing to getFieldsFromCsv
    when( meta.getFilename() ).thenReturn( "test.csv" );
    JSONObject response3 = helper.handleStepAction( "getFieldsFromCsv", transMeta, queryParams );
    assertNotNull( response3 );
    assertNotNull( response3.get( "actionStatus" ) );

    // Test exception handling
    when( transMeta.findStep( "testStep" ) ).thenThrow( new RuntimeException( "Unexpected error" ) );
    JSONObject response4 = helper.handleStepAction( "listS3Buckets", transMeta, queryParams );
    assertEquals( "Action failed", response4.get( "actionStatus" ) );
    assertNotNull( response4.get( "error" ) );
  }

  @Test
  public void testGetFieldsWithoutHeader() {
    String csvData = "John,25,NYC\nJane,30,LA";
    ByteArrayInputStream inputStream = new ByteArrayInputStream( csvData.getBytes() );
    AmazonS3 s3Client = mock( AmazonS3.class );
    S3Object s3Object = new S3Object();
    S3ObjectInputStream s3InputStream = new S3ObjectInputStream( inputStream, null );
    s3Object.setObjectContent( s3InputStream );
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getBucket() ).thenReturn( "test-bucket" );
    when( meta.getFilename() ).thenReturn( "test.csv" );
    when( meta.getS3Client( any( VariableSpace.class ) ) ).thenReturn( s3Client );
    when( meta.getDelimiter() ).thenReturn( "," );
    when( meta.getEnclosure() ).thenReturn( "\"" );
    when( meta.isHeaderPresent() ).thenReturn( false );
    when( transMeta.environmentSubstitute( "," ) ).thenReturn( "," );
    when( transMeta.environmentSubstitute( "\"" ) ).thenReturn( "\"" );
    when( s3Client.doesBucketExistV2( "test-bucket" ) ).thenReturn( true );
    when( s3Client.getObject( "test-bucket", "test.csv" ) ).thenReturn( s3Object );
    JSONObject response = helper.getFieldsFromCsvAction( transMeta, queryParams );
    assertEquals( "Action successful", response.get( "actionStatus" ) );
    JSONArray fieldsArray = (JSONArray) response.get( "fields" );
    assertEquals( 3, fieldsArray.size() );
    JSONObject field1 = (JSONObject) fieldsArray.get( 0 );
    assertEquals( "Field_000", field1.get( "name" ) );
  }

  @Test
  public void testGetFieldsWithEnclosures() {
    String csvData = "\"Name\",\"Age\",\"City\"\n\"John Smith\",\"25\",\"New York\"";
    ByteArrayInputStream inputStream = new ByteArrayInputStream( csvData.getBytes() );
    AmazonS3 s3Client = mock( AmazonS3.class );
    S3Object s3Object = new S3Object();
    S3ObjectInputStream s3InputStream = new S3ObjectInputStream( inputStream, null );
    s3Object.setObjectContent( s3InputStream );
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getBucket() ).thenReturn( "test-bucket" );
    when( meta.getFilename() ).thenReturn( "test.csv" );
    when( meta.getS3Client( any( VariableSpace.class ) ) ).thenReturn( s3Client );
    when( meta.getDelimiter() ).thenReturn( "," );
    when( meta.getEnclosure() ).thenReturn( "\"" );
    when( meta.isHeaderPresent() ).thenReturn( true );
    when( transMeta.environmentSubstitute( "," ) ).thenReturn( "," );
    when( transMeta.environmentSubstitute( "\"" ) ).thenReturn( "\"" );
    when( s3Client.doesBucketExistV2( "test-bucket" ) ).thenReturn( true );
    when( s3Client.getObject( "test-bucket", "test.csv" ) ).thenReturn( s3Object );
    JSONObject response = helper.getFieldsFromCsvAction( transMeta, queryParams );
    assertEquals( "Action successful", response.get( "actionStatus" ) );
    JSONArray fieldsArray = (JSONArray) response.get( "fields" );
    JSONObject field1 = (JSONObject) fieldsArray.get( 0 );
    assertEquals( "Name", field1.get( "name" ) );
  }

  @Test
  public void testGetFieldsWithEmptyValues() {
    String csvData = "Name,Age,City\nJohn,,NYC\n,30,\nBob,35,Chicago";
    ByteArrayInputStream inputStream = new ByteArrayInputStream( csvData.getBytes() );
    AmazonS3 s3Client = mock( AmazonS3.class );
    S3Object s3Object = new S3Object();
    S3ObjectInputStream s3InputStream = new S3ObjectInputStream( inputStream, null );
    s3Object.setObjectContent( s3InputStream );
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getBucket() ).thenReturn( "test-bucket" );
    when( meta.getFilename() ).thenReturn( "test.csv" );
    when( meta.getS3Client( any( VariableSpace.class ) ) ).thenReturn( s3Client );
    when( meta.getDelimiter() ).thenReturn( "," );
    when( meta.getEnclosure() ).thenReturn( "\"" );
    when( meta.isHeaderPresent() ).thenReturn( true );
    when( transMeta.environmentSubstitute( "," ) ).thenReturn( "," );
    when( transMeta.environmentSubstitute( "\"" ) ).thenReturn( "\"" );
    when( s3Client.doesBucketExistV2( "test-bucket" ) ).thenReturn( true );
    when( s3Client.getObject( "test-bucket", "test.csv" ) ).thenReturn( s3Object );
    JSONObject response = helper.getFieldsFromCsvAction( transMeta, queryParams );
    assertEquals( "Action successful", response.get( "actionStatus" ) );
    String scanSummary = (String) response.get( "scanSummary" );
    assertTrue( scanSummary.contains( "Nr of null values" ) );
  }

  @Test
  public void testGetFieldsWithSemicolonDelimiter() {
    String csvData = "Name;Age;City\nJohn;25;NYC\nJane;30;LA";
    ByteArrayInputStream inputStream = new ByteArrayInputStream( csvData.getBytes() );

    AmazonS3 s3Client = mock( AmazonS3.class );
    S3Object s3Object = new S3Object();
    S3ObjectInputStream s3InputStream = new S3ObjectInputStream( inputStream, null );
    s3Object.setObjectContent( s3InputStream );

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );

    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( meta.getBucket() ).thenReturn( "test-bucket" );
    when( meta.getFilename() ).thenReturn( "test.csv" );
    when( meta.getS3Client( any( VariableSpace.class ) ) ).thenReturn( s3Client );
    when( meta.getDelimiter() ).thenReturn( ";" );
    when( meta.getEnclosure() ).thenReturn( "\"" );
    when( meta.isHeaderPresent() ).thenReturn( true );
    when( transMeta.environmentSubstitute( ";" ) ).thenReturn( ";" );
    when( transMeta.environmentSubstitute( "\"" ) ).thenReturn( "\"" );
    when( s3Client.doesBucketExistV2( "test-bucket" ) ).thenReturn( true );
    when( s3Client.getObject( "test-bucket", "test.csv" ) ).thenReturn( s3Object );

    JSONObject response = helper.getFieldsFromCsvAction( transMeta, queryParams );

    assertEquals( "Action successful", response.get( "actionStatus" ) );
    JSONArray fieldsArray = (JSONArray) response.get( "fields" );
    assertEquals( 3, fieldsArray.size() );
  }

  @Test
  public void testParseAndCleanCsvLine() throws Exception {
    // Test the private parseAndCleanCsvLine method using reflection
    Method parseMethod = S3CsvInputHelper.class.getDeclaredMethod(
      "parseAndCleanCsvLine", String.class, String.class, String.class );
    parseMethod.setAccessible( true );

    // Test case 1: All values enclosed with quotes
    String line1 = "\"John Doe\",\"25\",\"New York\"";
    String[] result1 = (String[]) parseMethod.invoke( helper, line1, ",", "\"" );
    assertArrayEquals( new String[]{ "John Doe", "25", "New York" }, result1 );

    // Test case 2: Mixed - some values enclosed, some not
    String line2 = "\"John Doe\",25,\"New York\"";
    String[] result2 = (String[]) parseMethod.invoke( helper, line2, ",", "\"" );
    assertArrayEquals( new String[]{ "John Doe", "25", "New York" }, result2 );

    // Test case 3: No enclosure - values should remain unchanged
    String line3 = "John,25,NYC";
    String[] result3 = (String[]) parseMethod.invoke( helper, line3, ",", "\"" );
    assertArrayEquals( new String[]{ "John", "25", "NYC" }, result3 );

    // Test case 4: Empty enclosure parameter - should not modify values
    String line4 = "\"John\",\"25\",\"NYC\"";
    String[] result4 = (String[]) parseMethod.invoke( helper, line4, ",", "" );
    assertArrayEquals( new String[]{ "\"John\"", "\"25\"", "\"NYC\"" }, result4 );

    // Test case 5: Single character value with enclosure
    String line5 = "\"A\",\"B\",\"C\"";
    String[] result5 = (String[]) parseMethod.invoke( helper, line5, ",", "\"" );
    assertArrayEquals( new String[]{ "A", "B", "C" }, result5 );

    // Test case 6: Value with only opening enclosure (should not be cleaned)
    String line6 = "\"John,25,NYC";
    String[] result6 = (String[]) parseMethod.invoke( helper, line6, ",", "\"" );
    assertArrayEquals( new String[]{ "\"John", "25", "NYC" }, result6 );

    // Test case 7: Empty string value with enclosure
    String line7 = "\"\",\"25\",\"\"";
    String[] result7 = (String[]) parseMethod.invoke( helper, line7, ",", "\"" );
    assertArrayEquals( new String[]{ "", "25", "" }, result7 );

    // Test case 8: Single quote as enclosure
    String line8 = "'John','25','NYC'";
    String[] result8 = (String[]) parseMethod.invoke( helper, line8, ",", "'" );
    assertArrayEquals( new String[]{ "John", "25", "NYC" }, result8 );

    // Test case 9: Null enclosure parameter - should not modify values
    String line9 = "\"John\",\"25\",\"NYC\"";
    String[] result9 = (String[]) parseMethod.invoke( helper, line9, ",", null );
    assertArrayEquals( new String[]{ "\"John\"", "\"25\"", "\"NYC\"" }, result9 );
  }

  @Test
  public void testUpdateFieldStatisticsBasicFunctionality() throws Exception {
    Method updateStatsMethod = S3CsvInputHelper.class.getDeclaredMethod(
      "updateFieldStatistics", String[].class, int.class, int[].class,
      String[].class, String[].class, int[].class );
    updateStatsMethod.setAccessible( true );

    int fieldCount = 3;
    int[] maxLengths = new int[fieldCount];
    String[] minValues = new String[fieldCount];
    String[] maxValues = new String[fieldCount];
    int[] nullCounts = new int[fieldCount];

    // Test initial values
    updateStatsMethod.invoke( helper, new String[]{ "John", "25", "NYC" }, fieldCount, maxLengths, minValues, maxValues, nullCounts );
    assertEquals( 4, maxLengths[0] );
    assertEquals( "John", minValues[0] );
    assertEquals( "John", maxValues[0] );
    assertEquals( 0, nullCounts[0] );

    // Test updating with new values (min/max changes)
    updateStatsMethod.invoke( helper, new String[]{ "Alice", "30", "Chicago" }, fieldCount, maxLengths, minValues, maxValues, nullCounts );
    assertEquals( 5, maxLengths[0] );
    assertEquals( 7, maxLengths[2] );
    assertEquals( "Alice", minValues[0] );
    assertEquals( "John", maxValues[0] );
  }

  @Test
  public void testUpdateFieldStatisticsEdgeCases() throws Exception {
    Method updateStatsMethod = S3CsvInputHelper.class.getDeclaredMethod(
      "updateFieldStatistics", String[].class, int.class, int[].class,
      String[].class, String[].class, int[].class );
    updateStatsMethod.setAccessible( true );

    int fieldCount = 3;
    int[] maxLengths = new int[fieldCount];
    String[] minValues = new String[fieldCount];
    String[] maxValues = new String[fieldCount];
    int[] nullCounts = new int[fieldCount];

    // Test null/empty values
    updateStatsMethod.invoke( helper, new String[]{ "", "  ", null }, fieldCount, maxLengths, minValues, maxValues, nullCounts );
    assertEquals( 1, nullCounts[0] );
    assertEquals( 1, nullCounts[1] );
    assertEquals( 1, nullCounts[2] );

    // Test fewer values than fieldCount
    updateStatsMethod.invoke( helper, new String[]{ "Alice", "30", "Chicago" }, fieldCount, maxLengths, minValues, maxValues, nullCounts );
    updateStatsMethod.invoke( helper, new String[]{ "Bob" }, fieldCount, maxLengths, minValues, maxValues, nullCounts );
    assertEquals( 5, maxLengths[0] );
    assertEquals( "Alice", minValues[0] );
    assertEquals( 2, maxLengths[1] );
  }

  @Test
  public void testExtractFieldNamesFromCsv() throws Exception {
    // Test with header present
    ByteArrayInputStream inputStream1 = new ByteArrayInputStream( "Name,Age,City\nJohn,25,NYC".getBytes() );
    InputStreamReader reader1 = new InputStreamReader( inputStream1 );
    when( meta.getDelimiter() ).thenReturn( "," );
    when( meta.getEnclosure() ).thenReturn( "\"" );
    when( meta.isHeaderPresent() ).thenReturn( true );
    String[] fieldNames1 = S3CsvInputHelper.extractFieldNamesFromCsv( reader1, meta );
    assertArrayEquals( new String[]{ "Name", "Age", "City" }, fieldNames1 );

    // Test without header - should generate Field_000, Field_001, etc.
    ByteArrayInputStream inputStream2 = new ByteArrayInputStream( "John,25,NYC".getBytes() );
    InputStreamReader reader2 = new InputStreamReader( inputStream2 );
    when( meta.isHeaderPresent() ).thenReturn( false );
    String[] fieldNames2 = S3CsvInputHelper.extractFieldNamesFromCsv( reader2, meta );
    assertArrayEquals( new String[]{ "Field_000", "Field_001", "Field_002" }, fieldNames2 );

    // Test with enclosure - should strip quotes
    ByteArrayInputStream inputStream3 = new ByteArrayInputStream( "\"Full Name\",\"Age\",\"City\"".getBytes() );
    InputStreamReader reader3 = new InputStreamReader( inputStream3 );
    when( meta.isHeaderPresent() ).thenReturn( true );
    String[] fieldNames3 = S3CsvInputHelper.extractFieldNamesFromCsv( reader3, meta );
    assertArrayEquals( new String[]{ "Full Name", "Age", "City" }, fieldNames3 );

    // Test with whitespace - should trim
    ByteArrayInputStream inputStream4 = new ByteArrayInputStream( "  Name  , Age , City  ".getBytes() );
    InputStreamReader reader4 = new InputStreamReader( inputStream4 );
    String[] fieldNames4 = S3CsvInputHelper.extractFieldNamesFromCsv( reader4, meta );
    assertArrayEquals( new String[]{ "Name", "Age", "City" }, fieldNames4 );
  }
}