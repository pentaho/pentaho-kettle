/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.checksum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.commons.codec.binary.Hex;
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
      data.fieldConverters = new FieldToBytesConverter[fieldIndexMapping.length];
      for ( int i = 0; i < fieldIndexMapping.length; i++ ) {
        if ( meta.isOldChecksumBehaviour() ) {
          data.fieldConverters[i] = new LegacyFieldToBytesConverter( inputRowMeta, fieldIndexMapping[i] );
        } else {
          if ( inputRowMeta.getValueMeta( fieldIndexMapping[i] ).isBinary() ) {
            data.fieldConverters[i] = new BinaryFieldToBytesConverter( inputRowMeta, fieldIndexMapping[i] );
          } else {
            data.fieldConverters[i] = new NonBinaryFieldToBytesConverter( inputRowMeta, fieldIndexMapping[i] );
          }
        }
      }

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
        }
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString( PKG, "CheckSum.Error.Digest" ), e );
      }

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
      putRow( data.outputRowMeta, outputRowData ); // copy row to output
      // rowset(s);
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
      if ( meta.isCompatibilityMode() && meta.getCheckSumType() == CheckSumMeta.TYPE_SHA256 ) {
        logError( BaseMessages.getString( PKG, "CheckSumMeta.CheckResult.CompatibilityModeSHA256Error" ) );
        return false;
      }
      return true;
    }
    return false;
  }

  public static interface GenericChecksumCalculator<R> {

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

  public static interface FieldToBytesConverter {

    byte[] getBytes( Object[] row ) throws KettleException;

  }

  public static class BinaryFieldToBytesConverter implements FieldToBytesConverter {

    private final RowMetaInterface rmi;
    private final int fieldIndex;

    public BinaryFieldToBytesConverter( RowMetaInterface inputRowMeta, int fieldIndex ) {
      rmi = inputRowMeta;
      this.fieldIndex = fieldIndex;
    }

    @Override
    public byte[] getBytes( Object[] row ) throws KettleException {
      return rmi.getBinary( row, fieldIndex );
    }

  }

  public static class NonBinaryFieldToBytesConverter implements FieldToBytesConverter {

    private final ValueMetaInterface vmi;
    private final int fieldIndex;

    public NonBinaryFieldToBytesConverter( RowMetaInterface inputRowMeta, int fieldIndex ) {
      this.vmi = inputRowMeta.getValueMeta( fieldIndex );
      this.fieldIndex = fieldIndex;
    }

    @Override
    public byte[] getBytes( Object[] row ) throws KettleException {
      if ( row[fieldIndex] != null ) {
        return vmi.getNativeDataType( row[fieldIndex] ).toString().getBytes();
      }
      return null;
    }

  }

  public static class LegacyFieldToBytesConverter implements FieldToBytesConverter {

    private final ValueMetaInterface vmi;
    private final int fieldIndex;

    public LegacyFieldToBytesConverter( RowMetaInterface inputRowMeta, int fieldIndex ) {
      this.vmi = inputRowMeta.getValueMeta( fieldIndex );
      this.fieldIndex = fieldIndex;
    }

    @Override
    public byte[] getBytes( Object[] row ) throws KettleException {
      return String.valueOf( vmi.getNativeDataType( row[fieldIndex] ) ).getBytes();
    }

  }

}
