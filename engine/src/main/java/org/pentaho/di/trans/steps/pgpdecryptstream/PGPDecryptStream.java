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

package org.pentaho.di.trans.steps.pgpdecryptstream;

import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.pgpencryptfiles.GPG;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Decrypt a stream with GPG *
 *
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class PGPDecryptStream extends BaseStep implements StepInterface {
  private static Class<?> PKG = PGPDecryptStreamMeta.class; // for i18n purposes, needed by Translator2!!

  private PGPDecryptStreamMeta meta;
  private PGPDecryptStreamData data;

  public PGPDecryptStream( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (PGPDecryptStreamMeta) smi;
    data = (PGPDecryptStreamData) sdi;

    boolean sendToErrorRow = false;
    String errorMessage = null;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    try {
      if ( first ) {
        first = false;
        // get the RowMeta
        data.previousRowMeta = getInputRowMeta().clone();
        data.NrPrevFields = data.previousRowMeta.size();
        data.outputRowMeta = data.previousRowMeta;
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

        // Check is stream data field is provided
        if ( Utils.isEmpty( meta.getStreamField() ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "PGPDecryptStream.Error.DataStreamFieldMissing" ) );
        }

        if ( meta.isPassphraseFromField() ) {
          // Passphrase from field
          String fieldname = meta.getPassphraseFieldName();
          if ( Utils.isEmpty( fieldname ) ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "PGPDecryptStream.Error.PassphraseFieldMissing" ) );
          }
          data.indexOfPassphraseField = data.previousRowMeta.indexOfValue( fieldname );
          if ( data.indexOfPassphraseField < 0 ) {
            // The field is unreachable !
            throw new KettleException( BaseMessages.getString(
              PKG, "PGPDecryptStream.Exception.CouldnotFindField", fieldname ) );
          }
        } else {
          // Check is passphrase is provided
          data.passPhrase =
            Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( meta.getPassphrase() ) );
          if ( Utils.isEmpty( data.passPhrase ) ) {
            throw new KettleException( BaseMessages.getString( PKG, "PGPDecryptStream.Error.PassphraseMissing" ) );
          }
        }

        // cache the position of the field
        if ( data.indexOfField < 0 ) {
          data.indexOfField = data.previousRowMeta.indexOfValue( meta.getStreamField() );
          if ( data.indexOfField < 0 ) {
            // The field is unreachable !
            throw new KettleException( BaseMessages.getString(
              PKG, "PGPDecryptStream.Exception.CouldnotFindField", meta.getStreamField() ) );
          }
        }
      } // End If first

      // allocate output row
      Object[] outputRow = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
      for ( int i = 0; i < data.NrPrevFields; i++ ) {
        outputRow[i] = r[i];
      }

      if ( meta.isPassphraseFromField() ) {
        data.passPhrase = data.previousRowMeta.getString( r, data.indexOfPassphraseField );
        if ( Utils.isEmpty( data.passPhrase ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "PGPDecryptStream.Error.PassphraseMissing" ) );
        }
      }
      // get stream, data to decrypt
      String encryptedData = data.previousRowMeta.getString( r, data.indexOfField );

      if ( Utils.isEmpty( encryptedData ) ) {
        // no data..we can not continue with this row
        throw new KettleException( BaseMessages.getString( PKG, "PGPDecryptStream.Error.DataToDecryptEmpty" ) );
      }

      // let's decrypt data
      String decryptedData = data.gpg.decrypt( encryptedData, data.passPhrase );

      // Add encrypted data to input stream
      outputRow[data.NrPrevFields] = decryptedData;

      // add new values to the row.
      putRow( data.outputRowMeta, outputRow ); // copy row to output rowset(s);

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "PGPDecryptStream.LineNumber", getLinesRead()
          + " : " + getInputRowMeta().getString( r ) ) );
      }
    } catch ( Exception e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "PGPDecryptStream.ErrorInStepRunning" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, meta.getResultFieldName(), "PGPDecryptStreamO01" );
      }
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (PGPDecryptStreamMeta) smi;
    data = (PGPDecryptStreamData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( Utils.isEmpty( meta.getResultFieldName() ) ) {
        logError( BaseMessages.getString( PKG, "PGPDecryptStream.Error.ResultFieldMissing" ) );
        return false;
      }

      try {
        // initiate a new GPG encryptor
        data.gpg = new GPG( environmentSubstitute( meta.getGPGLocation() ), log );
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "PGPDecryptStream.Init.Error" ), e );
        return false;
      }

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (PGPDecryptStreamMeta) smi;
    data = (PGPDecryptStreamData) sdi;

    super.dispose( smi, sdi );
  }

}
