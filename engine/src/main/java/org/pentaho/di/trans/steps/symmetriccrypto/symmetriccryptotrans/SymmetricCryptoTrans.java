/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.symmetriccrypto.symmetriccryptotrans;

import org.apache.commons.codec.binary.Hex;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm.SymmetricCrypto;
import org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm.SymmetricCryptoMeta;

/**
 * Symmetric algorithm Executes a SymmetricCryptoTrans on the values in the input stream. Selected calculated values can
 * then be put on the output stream.
 *
 * @author Samatar
 * @since 5-apr-2003
 *
 */
public class SymmetricCryptoTrans extends BaseStep implements StepInterface {
  private static Class<?> PKG = SymmetricCryptoTransMeta.class; // for i18n purposes, needed by Translator2!!

  private SymmetricCryptoTransMeta meta;
  private SymmetricCryptoTransData data;

  public SymmetricCryptoTrans( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (SymmetricCryptoTransMeta) smi;
    data = (SymmetricCryptoTransData) sdi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!

    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }
    if ( first ) {
      first = false;

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Let's check that Result Field is given
      if ( Utils.isEmpty( meta.getResultfieldname() ) ) {
        // Result field is missing !
        throw new KettleStepException( BaseMessages.getString(
          PKG, "SymmetricCryptoTrans.Exception.ErrorResultFieldMissing" ) );
      }

      // Check if The message field is given
      if ( Utils.isEmpty( meta.getMessageFied() ) ) {
        // Message Field is missing !
        throw new KettleStepException( BaseMessages.getString(
          PKG, "SymmetricCryptoTrans.Exception.MissingMessageField" ) );
      }
      // Try to get Field index
      data.indexOfMessage = getInputRowMeta().indexOfValue( meta.getMessageFied() );

      // Let's check the Field
      if ( data.indexOfMessage < 0 ) {
        // The field is unreachable !
        throw new KettleStepException( BaseMessages.getString(
          PKG, "SymmetricCryptoTrans.Exception.CouldnotFindField", meta.getMessageFied() ) );
      }

      if ( !meta.isSecretKeyInField() ) {
        String realSecretKey =
          Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( meta.getSecretKey() ) );
        if ( Utils.isEmpty( realSecretKey ) ) {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "SymmetricCryptoTrans.Exception.SecretKeyMissing" ) );
        }
        // We have a static secret key
        // Set secrete key
        setSecretKey( realSecretKey );

      } else {
        // dynamic secret key
        if ( Utils.isEmpty( meta.getSecretKeyField() ) ) {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "SymmetricCryptoTrans.Exception.SecretKeyFieldMissing" ) );
        }
        // Try to get secret key field index
        data.indexOfSecretkeyField = getInputRowMeta().indexOfValue( meta.getSecretKeyField() );

        // Let's check the Field
        if ( data.indexOfSecretkeyField < 0 ) {
          // The field is unreachable !
          throw new KettleStepException( BaseMessages.getString(
            PKG, "SymmetricCryptoTrans.Exception.CouldnotFindField", meta.getSecretKeyField() ) );
        }
      }

    }

    try {

      // handle dynamic secret key
      Object realSecretKey;
      if ( meta.isSecretKeyInField() ) {
        if ( meta.isReadKeyAsBinary() ) {
          realSecretKey = getInputRowMeta().getBinary( r, data.indexOfSecretkeyField );
          if ( realSecretKey == null ) {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "SymmetricCryptoTrans.Exception.SecretKeyMissing" ) );
          }
        } else {
          realSecretKey =
            Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( getInputRowMeta().getString(
              r, data.indexOfSecretkeyField ) ) );
          if ( Utils.isEmpty( (String) realSecretKey ) ) {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "SymmetricCryptoTrans.Exception.SecretKeyMissing" ) );
          }
        }

        // Set secrete key
        setSecretKey( realSecretKey );
      }

      // Get the field value

      Object result = null;

      if ( meta.getOperationType() == SymmetricCryptoTransMeta.OPERATION_TYPE_ENCRYPT ) {

        // encrypt plain text
        byte[] encrBytes = data.Crypt.encrDecryptData( getInputRowMeta().getBinary( r, data.indexOfMessage ) );

        // return encrypted value
        if ( meta.isOutputResultAsBinary() ) {
          result = encrBytes;
        } else {
          result = new String( Hex.encodeHex( ( encrBytes ) ) );
        }
      } else {
        // Get encrypted value
        String s = getInputRowMeta().getString( r, data.indexOfMessage );

        byte[] dataBytes = Hex.decodeHex( s.toCharArray() );

        // encrypt or decrypt message and return result
        byte[] encrBytes = data.Crypt.encrDecryptData( dataBytes );

        // we have decrypted value
        if ( meta.isOutputResultAsBinary() ) {
          result = encrBytes;
        } else {
          result = new String( encrBytes );
        }
      }

      Object[] outputRowData = RowDataUtil.addValueData( r, getInputRowMeta().size(), result );

      putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);

    } catch ( Exception e ) {
      boolean sendToErrorRow = false;
      String errorMessage;
      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "SymmetricCryptoTrans.Log.ErrorInStepRunning" ), e );
        logError( Const.getStackTracker( e ) );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, null, "EncDecr001" );
      }
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SymmetricCryptoTransMeta) smi;
    data = (SymmetricCryptoTransData) sdi;
    if ( super.init( smi, sdi ) ) {
      // Add init code here.

      try {
        // Define a new instance
        data.CryptMeta = new SymmetricCryptoMeta( meta.getAlgorithm() );
        // Initialize a new crypto trans object
        data.Crypt = new SymmetricCrypto( data.CryptMeta, environmentSubstitute( meta.getSchema() ) );

      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "SymmetricCryptoTrans.ErrorInit." ), e );
        return false;
      }

      return true;
    }
    return false;
  }

  private void setSecretKey( Object key ) throws KettleException {

    // Set secrete key
    if ( key instanceof byte[] ) {
      data.Crypt.setSecretKey( (byte[]) key );
    } else {
      data.Crypt.setSecretKey( (String) key );
    }

    if ( meta.getOperationType() == SymmetricCryptoTransMeta.OPERATION_TYPE_ENCRYPT ) {
      data.Crypt.setEncryptMode();
    } else {
      data.Crypt.setDecryptMode();
    }

  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SymmetricCryptoTransMeta) smi;
    data = (SymmetricCryptoTransData) sdi;

    if ( data.Crypt != null ) {
      data.Crypt.close();
    }

    super.dispose( smi, sdi );
  }
}
