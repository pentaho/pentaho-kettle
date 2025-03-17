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


package org.pentaho.di.trans.steps.checksum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Caculate a checksum for each row.
 *
 * @author Samatar Hassan
 * @since 30-06-2008
 */
public class CheckSum extends BaseStep implements StepInterface {

  private static Class<?> PKG = CheckSumMeta.class; // for i18n purposes, needed by Translator2!!

  private CheckSumMeta meta;

  private CheckSumData data;

  public CheckSum( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @SuppressWarnings( "deprecation" )
  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (CheckSumMeta) smi;
    data = (CheckSumData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) {
      // no more input to be expected...
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      RowMetaInterface inputRowMeta = getInputRowMeta();
      data.outputRowMeta = inputRowMeta.clone();
      data.nrInfields = data.outputRowMeta.size();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      if ( meta.getFieldSeparatorString() != null && !meta.getFieldSeparatorString().isEmpty() ) {
        data.fieldSeparatorStringBytes = meta.getFieldSeparatorString().getBytes( StandardCharsets.UTF_8 );
      }

      int[] fieldIndexMapping;

      if ( meta.getFieldName() == null || meta.getFieldName().length > 0 ) {
        fieldIndexMapping = new int[meta.getFieldName().length];

        for ( int i = 0; i < meta.getFieldName().length; i++ ) {
          fieldIndexMapping[i] = getInputRowMeta().indexOfValue( meta.getFieldName()[i] );
          if ( fieldIndexMapping[i] < 0 ) {
            logError( BaseMessages.getString( PKG, "CheckSum.Log.CanNotFindField", meta.getFieldName()[i] ) );
            throw new KettleException( BaseMessages.getString( PKG, "CheckSum.Log.CanNotFindField", meta
                .getFieldName()[i] ) );
          }
        }
      } else {
        fieldIndexMapping = new int[inputRowMeta.size()];
        for ( int i = 0; i < fieldIndexMapping.length; i++ ) {
          fieldIndexMapping[i] = i;
        }
      }

      // Initialize the field converters
      initializeFieldConverters( inputRowMeta, fieldIndexMapping );

      // Initialize the checksum calculator
      initializeChecksumCalculator();

    } // end if first

    Object[] outputRowData = null;

    try {
      // Update the checksum with the field content
      for ( int i = 0; i < data.fieldConverters.length; i++ ) {
        if ( i != 0 && data.fieldSeparatorStringBytes != null ) {
          data.checksumCalculator.update( data.fieldSeparatorStringBytes );
        }
        data.checksumCalculator.update( data.fieldConverters[i].getBytes( r ) );
      }
      Object checksumResult = data.checksumCalculator.getResult();

      if ( checksumResult instanceof Long ) {
        outputRowData = RowDataUtil.addValueData( r, data.nrInfields, (Long) checksumResult );
      } else if ( checksumResult instanceof byte[] ) {
        byte[] o = (byte[]) checksumResult;
        switch ( meta.getResultType() ) {
          case CheckSumMeta.result_TYPE_BINARY:
            outputRowData = RowDataUtil.addValueData( r, data.nrInfields, o );
            break;
          case CheckSumMeta.result_TYPE_HEXADECIMAL:
            String hex =
                meta.isCompatibilityMode() ? byteToHexEncode_compatible( o ) : new String( Hex.encodeHex( o ) );
            outputRowData = RowDataUtil.addValueData( r, data.nrInfields, hex );
            break;
          default:
            outputRowData = RowDataUtil.addValueData( r, data.nrInfields, getStringFromBytes( o ) );
            break;
        }
      }

      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "CheckSum.Log.LineNumber", Long.toString( getLinesRead() ) ) );
        }
      }

      // add new values to the row.
      putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);
    } catch ( Exception e ) {
      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "CheckSum.ErrorInStepRunning" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, meta.getResultFieldName(), "CheckSum001" );
      }
    }
    return true;
  }

  private void initializeChecksumCalculator() throws KettleException {
    try {
      switch ( meta.getCheckSumType() ) {
        case CheckSumMeta.TYPE_MD5:
        case CheckSumMeta.TYPE_SHA1:
        case CheckSumMeta.TYPE_SHA256:
          data.checksumCalculator =
              new DigestChecksumCalculator( MessageDigest.getInstance( meta.getCheckSumType() ) );
          break;
        case CheckSumMeta.TYPE_ADLER32:
          data.checksumCalculator = new ChecksumChecksumCalculator( new Adler32() );
          break;
        case CheckSumMeta.TYPE_CRC32:
          data.checksumCalculator = new ChecksumChecksumCalculator( new CRC32() );
          break;
        default:
          throw new KettleException(
          BaseMessages.getString( PKG, "CheckSum.Error.UnknownChecksumType", meta.getCheckSumType() ) );
      }
    } catch ( KettleException e ) {
      // Rethrow it
      throw e;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "CheckSum.Error.Digest" ), e );
    }
  }

  private void initializeFieldConverters( RowMetaInterface inputRowMeta, int[] fieldIndexMapping ) throws KettleException {
    data.fieldConverters = new FieldToBytesConverter[ fieldIndexMapping.length ];
    for ( int i = 0; i < fieldIndexMapping.length; i++ ) {
      switch ( meta.getEvaluationMethod() ) {
        case CheckSumMeta.EVALUATION_METHOD_BYTES:
          data.fieldConverters[ i ] = new BytesToBytesConverter( inputRowMeta, fieldIndexMapping[ i ] );
          break;
        case CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS:
          data.fieldConverters[ i ] = new PentahoStringsToBytesConverter( inputRowMeta, fieldIndexMapping[ i ] );
          break;
        case CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS:
          data.fieldConverters[ i ] = new NativeStringsToBytesConverter( inputRowMeta, fieldIndexMapping[ i ] );
          break;
        default:
          throw new KettleException(
            BaseMessages.getString( PKG, "CheckSum.Error.UnknownEvaluationMethod", meta.getEvaluationMethod() ) );
      }
    }
  }

  private static String getStringFromBytes( byte[] bytes ) {
    StringBuilder sb = new StringBuilder();
    for ( int i = 0; i < bytes.length; i++ ) {
      byte b = bytes[i];
      sb.append( 0x00FF & b );
      if ( i + 1 < bytes.length ) {
        sb.append( "-" );
      }
    }
    return sb.toString();
  }

  public String byteToHexEncode_compatible( byte[] in ) {
    if ( in == null ) {
      return null;
    }
    final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    String hex = new String( in );

    char[] s = hex.toCharArray();
    StringBuilder hexString = new StringBuilder( 2 * s.length );

    for ( int i = 0; i < s.length; i++ ) {
      hexString.append( hexDigits[( s[i] & 0x00F0 ) >> 4] ); // hi nibble
      hexString.append( hexDigits[s[i] & 0x000F] ); // lo nibble
    }

    return hexString.toString();
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject getCheckSumTypesAction( Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    JSONArray checkSumTypes = new JSONArray();
    for ( int i = 0; i < CheckSumMeta.checksumtypeDescs.length; i++ ) {
      JSONObject checkSumType = new JSONObject();
      checkSumType.put( "id", CheckSumMeta.checksumtypeCodes[ i ] );
      checkSumType.put( "name", CheckSumMeta.checksumtypeDescs[ i ] );
      checkSumTypes.add( checkSumType );
    }

    response.put( "checkSumTypes", checkSumTypes );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject getEvaluationMethodsAction( Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    JSONArray evaluationMethods = new JSONArray();
    for ( int i = 0; i < CheckSumMeta.EVALUATION_METHOD_DESCS.length; i++ ) {
      JSONObject evaluationMethod = new JSONObject();
      evaluationMethod.put( "id", CheckSumMeta.EVALUATION_METHOD_CODES[ i ] );
      evaluationMethod.put( "name", CheckSumMeta.EVALUATION_METHOD_DESCS[ i ] );
      evaluationMethods.add( evaluationMethod );
    }

    response.put( "evaluationMethods", evaluationMethods );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject getResultTypesAction( Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    JSONArray resultTypes = new JSONArray();
    for ( int i = 0; i < CheckSumMeta.resultTypeCode.length; i++ ) {
      JSONObject resultType = new JSONObject();
      resultType.put( "id", CheckSumMeta.resultTypeCode[ i ] );
      resultType.put( "name", CheckSumMeta.resultTypeCode[ i ] );
      resultTypes.add( resultType );
    }

    response.put( "resultTypes", resultTypes );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  @SuppressWarnings( "deprecation" )
  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (CheckSumMeta) smi;
    data = (CheckSumData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( Utils.isEmpty( meta.getResultFieldName() ) ) {
        logError( BaseMessages.getString( PKG, "CheckSum.Error.ResultFieldMissing" ) );
        return false;
      }
      return true;
    }
    return false;
  }

  public interface GenericChecksumCalculator<R> {

    void update( byte[] contentBytes );

    R getResult();
  }

  public static class ChecksumChecksumCalculator implements GenericChecksumCalculator<Long> {

    private final Checksum checksum;

    public ChecksumChecksumCalculator( Checksum checksum ) {
      this.checksum = checksum;
    }

    @Override
    public void update( byte[] contentBytes ) {
      if ( contentBytes != null ) {
        checksum.update( contentBytes, 0, contentBytes.length );
      }
    }

    @Override
    public Long getResult() {
      try {
        return new Long( checksum.getValue() );
      } finally {
        checksum.reset();
      }
    }
  }

  public static class DigestChecksumCalculator implements GenericChecksumCalculator<byte[]> {

    private final MessageDigest digest;

    public DigestChecksumCalculator( MessageDigest digest ) {
      this.digest = digest;
    }

    @Override
    public void update( byte[] contentBytes ) {
      if ( contentBytes != null ) {
        digest.update( contentBytes );
      }
    }

    @Override
    public byte[] getResult() {
      return digest.digest();
    }
  }

  public interface FieldToBytesConverter {

    byte[] getBytes( Object[] row ) throws KettleException;

  }

  /**
   * <p>Use Pentaho String representation of fields using format masks (7.1 and below behavior).</p>
   */
  public static class PentahoStringsToBytesConverter implements FieldToBytesConverter {

    private final RowMetaInterface rmi;
    private final int fieldIndex;

    public PentahoStringsToBytesConverter( RowMetaInterface inputRowMeta, int fieldIndex ) {
      this.rmi = inputRowMeta;
      this.fieldIndex = fieldIndex;
    }

    @Override
    public byte[] getBytes( Object[] row ) throws KettleException {
      return String.valueOf( rmi.getString( row, fieldIndex ) ).getBytes();
    }
  }

  /**
   * <p>Use Native String representation of fields (8.0 behavior).</p>
   */
  public static class NativeStringsToBytesConverter implements FieldToBytesConverter {

    private final ValueMetaInterface vmi;
    private final int fieldIndex;

    public NativeStringsToBytesConverter( RowMetaInterface inputRowMeta, int fieldIndex ) {
      this.vmi = inputRowMeta.getValueMeta( fieldIndex );
      this.fieldIndex = fieldIndex;
    }

    @Override
    public byte[] getBytes( Object[] row ) throws KettleException {
      return String.valueOf( vmi.getNativeDataType( row[ fieldIndex ] ) ).getBytes();
    }
  }

  /**
   * <p>Use Byte Representation of fields (default behavior 8.1 forward).</p>
   */
  public static class BytesToBytesConverter implements FieldToBytesConverter {

    private final RowMetaInterface rmi;
    private final ValueMetaInterface vmi;
    private final int fieldIndex;
    private final boolean isBinary;

    public BytesToBytesConverter( RowMetaInterface inputRowMeta, int fieldIndex ) {
      this.rmi = inputRowMeta;
      this.vmi = rmi.getValueMeta( fieldIndex );
      this.fieldIndex = fieldIndex;
      this.isBinary = vmi.isBinary();
    }

    @Override
    public byte[] getBytes( Object[] row ) throws KettleException {
      return isBinary ? getBytesFromBinary( row ) : getBytesFromNonBinary( row );
    }

    private byte[] getBytesFromBinary( Object[] row ) throws KettleException {
      return rmi.getBinary( row, fieldIndex );
    }

    private byte[] getBytesFromNonBinary( Object[] row ) throws KettleException {
      byte[] ret = null;

      if ( null != row[ fieldIndex ] ) {
        ret = vmi.getNativeDataType( row[ fieldIndex ] ).toString().getBytes();
      }

      return ret;
    }
  }
}
