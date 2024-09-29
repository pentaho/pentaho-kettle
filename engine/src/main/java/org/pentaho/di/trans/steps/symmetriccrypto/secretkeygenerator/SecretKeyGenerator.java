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

package org.pentaho.di.trans.steps.symmetriccrypto.secretkeygenerator;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm.CryptoException;
import org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm.SymmetricCrypto;
import org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm.SymmetricCryptoMeta;

/**
 * Generate secret key. for symmetric algorithms
 *
 * @author Samatar
 * @since 01-4-2011
 */
public class SecretKeyGenerator extends BaseStep implements StepInterface {
  private static Class<?> PKG = SecretKeyGeneratorMeta.class; // for i18n purposes, needed by Translator2!!

  private SecretKeyGeneratorMeta meta;

  private SecretKeyGeneratorData data;

  public SecretKeyGenerator( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Build an empty row based on the meta-data...
   *
   * @return
   */

  private Object[] buildEmptyRow() {
    Object[] rowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );

    return rowData;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    Object[] row;
    Object[] rowIn = null;

    if ( data.readsRows ) {
      rowIn = getRow();
      if ( rowIn == null ) {
        setOutputDone();
        return false;
      }

      if ( first ) {
        first = false;
        data.prevNrField = getInputRowMeta().size();
        data.outputRowMeta = getInputRowMeta().clone();
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
      }

    } else {

      if ( first ) {
        first = false;
        data.outputRowMeta = new RowMeta();
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
      }
    }
    for ( int i = 0; i < data.nr && !isStopped(); i++ ) {

      for ( int j = 0; j < data.secretKeyCount[i] && !isStopped(); j++ ) {

        // Create a new row
        row = buildEmptyRow();
        incrementLinesRead();

        int index = 0;

        try {
          // Return secret key
          if ( meta.isOutputKeyInBinary() ) {
            row[index++] = data.cryptoTrans[i].generateKey( data.secretKeyLen[i] );
          } else {
            row[index++] = data.cryptoTrans[i].generateKeyAsHex( data.secretKeyLen[i] );
          }

        } catch ( CryptoException k ) {
          throw new KettleException( BaseMessages.getString( PKG, "SecretKeyGenerator.KeyGenerationError", i ), k );
        }

        if ( data.addAlgorithmOutput ) {
          // add algorithm
          row[index++] = meta.getAlgorithm()[i];
        }

        if ( data.addSecretKeyLengthOutput ) {
          // add secret key len
          row[index++] = new Long( data.secretKeyLen[i] );
        }

        if ( data.readsRows ) {
          // build output row
          row = RowDataUtil.addRowData( rowIn, data.prevNrField, row );
        }

        if ( isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "SecretKeyGenerator.Log.ValueReturned", data.outputRowMeta
            .getString( row ) ) );
        }

        putRow( data.outputRowMeta, row );
      }
    }

    setOutputDone();
    return false;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SecretKeyGeneratorMeta) smi;
    data = (SecretKeyGeneratorData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.

      if ( Utils.isEmpty( meta.getAlgorithm() ) ) {
        logError( BaseMessages.getString( PKG, "SecretKeyGenerator.Log.NoFieldSpecified" ) );
        return false;
      }

      if ( Utils.isEmpty( meta.getSecretKeyFieldName() ) ) {
        logError( BaseMessages.getString( PKG, "SecretKeyGenerator.Log.secretKeyFieldMissing" ) );
        return false;
      }

      data.nr = meta.getAlgorithm().length;
      data.algorithm = new int[data.nr];
      data.scheme = new String[data.nr];
      data.secretKeyLen = new int[data.nr];
      data.secretKeyCount = new int[data.nr];

      for ( int i = 0; i < data.nr; i++ ) {
        data.algorithm[i] = SymmetricCryptoMeta.getAlgorithmTypeFromCode( meta.getAlgorithm()[i] );
        String len = environmentSubstitute( meta.getSecretKeyLength()[i] );
        data.secretKeyLen[i] = Const.toInt( len, -1 );
        if ( data.secretKeyLen[i] < 0 ) {
          logError( BaseMessages.getString( PKG, "SecretKeyGenerator.Log.WrongLength", len, String.valueOf( i ) ) );
          return false;
        }
        String size = environmentSubstitute( meta.getSecretKeyCount()[i] );
        data.secretKeyCount[i] = Const.toInt( size, -1 );
        if ( data.secretKeyCount[i] < 0 ) {
          logError( BaseMessages.getString( PKG, "SecretKeyGenerator.Log.WrongSize", size, String.valueOf( i ) ) );
          return false;
        }
        data.scheme[i] = environmentSubstitute( meta.getScheme()[i] );
      }

      data.readsRows = getStepMeta().getRemoteInputSteps().size() > 0;
      List<StepMeta> previous = getTransMeta().findPreviousSteps( getStepMeta() );
      if ( previous != null && previous.size() > 0 ) {
        data.readsRows = true;
      }

      data.addAlgorithmOutput = !Utils.isEmpty( meta.getAlgorithmFieldName() );
      data.addSecretKeyLengthOutput = !Utils.isEmpty( meta.getSecretKeyLengthFieldName() );

      data.cryptoTrans = new SymmetricCrypto[data.nr];
      for ( int i = 0; i < data.nr; i++ ) {
        try {
          // Define a new cryptotrans meta instance
          SymmetricCryptoMeta cryptoTransMeta = new SymmetricCryptoMeta( meta.getAlgorithm()[i] );

          // Initialize a new cryptotrans object
          data.cryptoTrans[i] = new SymmetricCrypto( cryptoTransMeta, data.scheme[i] );

        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "SecretKey.Init.Error" ), e );
          return false;
        }
      }

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    super.dispose( smi, sdi );
    if ( data.cryptoTrans != null ) {
      int nr = data.cryptoTrans.length;
      for ( int i = 0; i < nr; i++ ) {
        data.cryptoTrans[i].close();
      }
    }
  }

}
