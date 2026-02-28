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

package org.pentaho.di.trans.steps.s3csvinput;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Helper class for S3 CSV Input step to support web UI interactions.
 * Provides actions for browsing S3 buckets, listing objects
 * and managing field configurations.
 */
public class S3CsvInputHelper extends BaseStepHelper {
  private static final Class<?> PKG = S3CsvInputMeta.class;
  // Action method names
  private static final String LIST_S3_BUCKETS = "listS3Buckets";
  private static final String LIST_S3_OBJECTS = "listS3Objects";
  private static final String GET_FIELDS_FROM_CSV = "getFieldsFromCsv";
  // Common parameter names
  private static final String STEP_NAME = "stepName";
  private static final String ERROR = "error";
  // Response keys
  private static final String BUCKETS_KEY = "buckets";
  private static final String OBJECTS_KEY = "objects";
  private static final String FIELDS_KEY = "fields";
  private static final String NAME_KEY = "name";
  private static final String TYPE_KEY = "type";
  private static final String SIZE_KEY = "size";
  private static final String SCAN_SUMMARY = "scanSummary";
  public S3CsvInputHelper() {
    super();
  }

  @SuppressWarnings( "unchecked" )
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      switch ( method ) {
        case LIST_S3_BUCKETS:
          response = listS3BucketsAction( transMeta, queryParams );
          break;
        case LIST_S3_OBJECTS:
          response = listS3ObjectsAction( transMeta, queryParams );
          break;
        case GET_FIELDS_FROM_CSV:
          response = getFieldsFromCsvAction( transMeta, queryParams );
          break;
        default:
          response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
          break;
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( ERROR, ex.getMessage() );
    }
    return response;
  }

  /**
   * Lists all S3 buckets accessible with the configured credentials.
   * Called from web UI bucket browser.
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject listS3BucketsAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JSONArray buckets = new JSONArray();
    try {
      String stepName = queryParams.get( STEP_NAME );
      S3CsvInputMeta meta = getStepMeta( transMeta, stepName );
      if ( meta == null ) {
        response.put( ERROR, "Missing or unknown stepName" );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }
      S3ObjectsProvider provider = new S3ObjectsProvider( meta.getS3Client( transMeta ) );
      String[] bucketNames = provider.getBucketsNames();
      for ( String bucketName : bucketNames ) {
        JSONObject bucketObj = new JSONObject();
        bucketObj.put( NAME_KEY, bucketName );
        bucketObj.put( TYPE_KEY, "bucket" );
        buckets.add( bucketObj );
      }
      response.put( BUCKETS_KEY, buckets );
      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    } catch ( Exception e ) {
      response.put( ERROR, "Failed to list S3 buckets: " + Const.NVL( e.getMessage(), "" ) );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Lists objects (files) in a specific S3 bucket.
   * Called from web UI when browsing bucket contents.
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject listS3ObjectsAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JSONArray objects = new JSONArray();
    try {
      String stepName = queryParams.get( STEP_NAME );
      S3CsvInputMeta meta = getStepMeta( transMeta, stepName );
      if ( meta == null ) {
        response.put( ERROR, Messages.getString( "S3CsvInputHelper.Error.StepNotFound" ) );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }
      String bucketName = meta.getBucket();
      if ( Utils.isEmpty( bucketName ) ) {
        response.put( ERROR, Messages.getString( "S3CsvInputHelper.Error.BucketNameRequired" ) );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }
      S3ObjectsProvider provider = new S3ObjectsProvider( meta.getS3Client( transMeta ) );
      String[] objectKeys = provider.getS3ObjectsNames( bucketName );
      Bucket bucket = provider.getBucket( bucketName );
      for ( String key : objectKeys ) {
        JSONObject objectInfo = createObjectInfo( provider, bucket, key );
        objects.add( objectInfo );
      }
      response.put( OBJECTS_KEY, objects );
      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    } catch ( Exception e ) {
      response.put( ERROR, BaseMessages.getString( PKG, "S3CsvInputHelper.Error.ListObjectsFailed", e.getMessage() ) );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  @SuppressWarnings( "unchecked" )
  private JSONObject createObjectInfo( S3ObjectsProvider provider, Bucket bucket, String key ) {
    JSONObject objectInfo = new JSONObject();
    objectInfo.put( NAME_KEY, key );
    objectInfo.put( TYPE_KEY, "file" );
    try {
      long size = provider.getS3ObjectContentLenght( bucket, key );
      objectInfo.put( SIZE_KEY, size );
    } catch ( Exception e ) {
      // Size not available, skip
    }
    return objectInfo;
  }

  /**
   * Analyzes CSV file and extracts field definitions from header row or first data row.
   */
  @SuppressWarnings( { "unchecked" } )
  public JSONObject getFieldsFromCsvAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      String stepName = queryParams.get( STEP_NAME );
      S3CsvInputMeta meta = getStepMeta( transMeta, stepName );
      if ( meta == null ) {
        response.put( ERROR, Messages.getString( "S3CsvInputHelper.Error.StepNotFound" ) );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }
      S3ObjectsProvider provider = new S3ObjectsProvider( meta.getS3Client( transMeta ) );
      S3Object s3Object = getS3ObjectForAnalysis( provider, meta, response );
      if ( s3Object == null ) {
        return response;
      }
      analyzeCsvAndBuildResponse( s3Object, meta, transMeta, response );
    } catch ( Exception e ) {
      response.put( ERROR, BaseMessages.getString( PKG, "S3CsvInputHelper.Error.GetFieldsFailed", e.getMessage() ) );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  @SuppressWarnings( "unchecked" )
  private S3Object getS3ObjectForAnalysis( S3ObjectsProvider provider, S3CsvInputMeta meta, JSONObject response ) {
    try {
      String bucketName = meta.getBucket();
      String fileName = meta.getFilename();
      Bucket bucket = provider.getBucket( bucketName );
      if ( bucket == null ) {
        response.put( ERROR, Messages.getString( "S3CsvInputHelper.Error.BucketNotFound", bucketName ) );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return null;
      }
      return provider.getS3Object( bucket, fileName );
    } catch ( Exception e ) {
      response.put( ERROR, e.getMessage() );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      return null;
    }
  }

  @SuppressWarnings( "unchecked" )
  private void analyzeCsvAndBuildResponse( S3Object s3Object,
                                           S3CsvInputMeta meta,
                                           TransMeta transMeta,
                                           JSONObject response ) throws IOException, KettleFileException {
    try ( InputStreamReader reader = new InputStreamReader( s3Object.getObjectContent() );
          BufferedReader bufferedReader = new BufferedReader( reader ) ) {
      String[] fieldNames = extractFieldNamesFromCsv( reader, meta );
      if ( fieldNames.length == 0 ) {
        response.put( ERROR, Messages.getString( "S3CsvInputHelper.Error.EmptyFile" ) );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return;
      }

      String delimiter = transMeta.environmentSubstitute( meta.getDelimiter() );
      String enclosure = transMeta.environmentSubstitute( meta.getEnclosure() );

      CsvStatistics stats = collectCsvStatistics( bufferedReader, fieldNames.length, delimiter, enclosure );
      buildResponseData( fieldNames, stats, response );

      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    }
  }

  private CsvStatistics collectCsvStatistics( BufferedReader reader, int fieldCount,
                                              String delimiter, String enclosure ) throws IOException {
    int[] maxLengths = new int[fieldCount];
    String[] minValues = new String[fieldCount];
    String[] maxValues = new String[fieldCount];
    int[] nullCounts = new int[fieldCount];
    int sampleRowCount = 0;
    int maxSampleRows = 100;

    String line;
    while ( ( line = reader.readLine() ) != null && sampleRowCount < maxSampleRows ) {
      String[] values = parseAndCleanCsvLine( line, delimiter, enclosure );
      updateFieldStatistics( values, fieldCount, maxLengths, minValues, maxValues, nullCounts );
      sampleRowCount++;
    }

    return new CsvStatistics( maxLengths, minValues, maxValues, nullCounts, sampleRowCount );
  }

  private String[] parseAndCleanCsvLine( String line, String delimiter, String enclosure ) {
    String[] values = Const.splitString( line, delimiter );
    if ( Utils.isEmpty( enclosure ) ) {
      return values;
    }
    for ( int i = 0; i < values.length; i++ ) {
      if ( values[i].startsWith( enclosure ) && values[i].endsWith( enclosure ) && values[i].length() > 1 ) {
        values[i] = values[i].substring( 1, values[i].length() - 1 );
      }
    }
    return values;
  }

  private void updateFieldStatistics( String[] values, int fieldCount, int[] maxLengths,
                                     String[] minValues, String[] maxValues, int[] nullCounts ) {
    for ( int i = 0; i < values.length && i < fieldCount; i++ ) {
      String value = Const.trim( values[i] );
      if ( Utils.isEmpty( value ) ) {
        nullCounts[i]++;
      } else {
        maxLengths[i] = Math.max( maxLengths[i], value.length() );
        if ( minValues[i] == null || value.compareTo( minValues[i] ) < 0 ) {
          minValues[i] = value;
        }
        if ( maxValues[i] == null || value.compareTo( maxValues[i] ) > 0 ) {
          maxValues[i] = value;
        }
      }
    }
  }

  @SuppressWarnings( "unchecked" )
  private void buildResponseData( String[] fieldNames, CsvStatistics stats, JSONObject response ) {
    JSONArray fields = new JSONArray();
    StringBuilder summary = new StringBuilder();
    summary.append( Messages.getString( "S3CsvInputDialog.ScanResults.DialogMessage" ) ).append( "\n" );
    summary.append( "Result after scanning " ).append( stats.sampleRowCount ).append( " lines.\n" );
    summary.append( "----------------------------------------------------\n" );

    for ( int i = 0; i < fieldNames.length; i++ ) {
      JSONObject fieldObj = new JSONObject();
      fieldObj.put( "name", fieldNames[i] );
      fieldObj.put( "type", ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_STRING ) );
      fieldObj.put( "length", stats.maxLengths[i] > 0 ? String.valueOf( stats.maxLengths[i] ) : "" );
      fields.add( fieldObj );

      summary.append( "Field nr. " ).append( i + 1 ).append( " :\n" );
      summary.append( "  Field name           : " ).append( fieldNames[i] ).append( "\n" );
      summary.append( "  Field type           : String\n" );
      summary.append( "  Maximum length       : " ).append( stats.maxLengths[i] ).append( "\n" );
      summary.append( "  Minimum value        : " ).append( stats.minValues[i] != null ? stats.minValues[i] : "" ).append( "\n" );
      summary.append( "  Maximum value        : " ).append( stats.maxValues[i] != null ? stats.maxValues[i] : "" ).append( "\n" );
      summary.append( "  Nr of null values    : " ).append( stats.nullCounts[i] ).append( "\n\n" );
    }

    response.put( FIELDS_KEY, fields );
    response.put( SCAN_SUMMARY, summary.toString() );
  }

  private static class CsvStatistics {
    final int[] maxLengths;
    final String[] minValues;
    final String[] maxValues;
    final int[] nullCounts;
    final int sampleRowCount;

    CsvStatistics( int[] maxLengths, String[] minValues, String[] maxValues, int[] nullCounts, int sampleRowCount ) {
      this.maxLengths = maxLengths;
      this.minValues = minValues;
      this.maxValues = maxValues;
      this.nullCounts = nullCounts;
      this.sampleRowCount = sampleRowCount;
    }
  }

  /**
   * Extracts field names from CSV input stream.
   * 
   * @param reader The input stream reader
   * @param meta The S3CsvInputMeta configuration
   * @return Array of field names
   */
  public static String[] extractFieldNamesFromCsv( InputStreamReader reader,
                                                   S3CsvInputMeta meta ) throws KettleFileException {
    try {
      BufferedReader bufferedReader = new BufferedReader( reader );
      // Read a line of data to determine the number of rows
      String line = bufferedReader.readLine();

      // Split the string, header or data into parts
      String[] fieldNames = Const.splitString( line, meta.getDelimiter() );

      if ( !meta.isHeaderPresent() ) {
        // Don't use field names from the header - generate field names F1 ... F10
        DecimalFormat df = new DecimalFormat( "000" );
        for ( int i = 0; i < fieldNames.length; i++ ) {
          fieldNames[i] = "Field_" + df.format( i );
        }
      } else if ( !Utils.isEmpty( meta.getEnclosure() ) ) {
        stripEnclosuresFromFields( fieldNames, meta.getEnclosure() );
      }

      // Trim the names
      for ( int i = 0; i < fieldNames.length; i++ ) {
        fieldNames[i] = Const.trim( fieldNames[i] );
      }
      return fieldNames;
    } catch ( IOException e ) {
      throw new KettleFileException( "Error reading CSV header line", e );
    }
  }

  private static void stripEnclosuresFromFields( String[] fieldNames, String enclosure ) {
    for ( int i = 0; i < fieldNames.length; i++ ) {
      if ( fieldNames[i].startsWith( enclosure ) && fieldNames[i].endsWith( enclosure ) && fieldNames[i].length() > 1 ) {
        fieldNames[i] = fieldNames[i].substring( 1, fieldNames[i].length() - 1 );
      }
    }
  }

  /**
   * Retrieves the S3CsvInputMeta from the transformation.
   */
  private S3CsvInputMeta getStepMeta( TransMeta transMeta, String stepName ) {
    if ( Utils.isEmpty( stepName ) ) {
      return null;
    }
    StepMeta stepMeta = transMeta.findStep( stepName );
    if ( stepMeta == null || !( stepMeta.getStepMetaInterface() instanceof S3CsvInputMeta ) ) {
      return null;
    }
    return (S3CsvInputMeta) stepMeta.getStepMetaInterface();
  }
}
