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


package org.pentaho.di.trans.steps.randomvalue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.UUID4Util;
import org.pentaho.di.core.util.UUIDUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Get random value.
 *
 * @author Matt, Samatar
 * @since 8-8-2008
 */
public class RandomValue extends BaseStep implements StepInterface {

  private static Class<?> PKG = RandomValueMeta.class; // for i18n purposes, needed by Translator2!!

  private RandomValueMeta meta;

  private RandomValueData data;

  public RandomValue( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private Object[] getRandomValue( RowMetaInterface inputRowMeta, Object[] inputRowData ) {
    Object[] row = new Object[data.outputRowMeta.size()];
    for ( int i = 0; i < inputRowMeta.size(); i++ ) {
      row[i] = inputRowData[i]; // no data is changed, clone is not
      // needed here.
    }

    for ( int i = 0, index = inputRowMeta.size(); i < meta.getFieldName().length; i++, index++ ) {
      switch ( meta.getFieldType()[i] ) {
        case RandomValueMeta.TYPE_RANDOM_NUMBER:
          row[index] = data.randomgen.nextDouble();
          break;
        case RandomValueMeta.TYPE_RANDOM_INTEGER:
          row[index] = new Long( data.randomgen.nextInt() ); // nextInt() already returns all 2^32 numbers.
          break;
        case RandomValueMeta.TYPE_RANDOM_STRING:
          row[index] = Long.toString( Math.abs( data.randomgen.nextLong() ), 32 );
          break;
        case RandomValueMeta.TYPE_RANDOM_UUID:
          row[index] = UUIDUtil.getUUIDAsString();
          break;
        case RandomValueMeta.TYPE_RANDOM_UUID4:
          row[index] = data.u4.getUUID4AsString();
          break;
        case RandomValueMeta.TYPE_RANDOM_MAC_HMACMD5:
          try {
            row[index] = generateRandomMACHash( RandomValueMeta.TYPE_RANDOM_MAC_HMACMD5 );
          } catch ( Exception e ) {
            logError( BaseMessages.getString( PKG, "RandomValue.Log.ErrorGettingRandomHMACMD5", e.getMessage() ) );
            setErrors( 1 );
            stopAll();
          }
          break;
        case RandomValueMeta.TYPE_RANDOM_MAC_HMACSHA1:
          try {
            row[index] = generateRandomMACHash( RandomValueMeta.TYPE_RANDOM_MAC_HMACSHA1 );
          } catch ( Exception e ) {
            logError( BaseMessages.getString( PKG, "RandomValue.Log.ErrorGettingRandomHMACSHA1", e.getMessage() ) );
            setErrors( 1 );
            stopAll();
          }
          break;
        default:
          break;
      }
    }

    return row;
  }

  private String generateRandomMACHash( int algorithm ) throws Exception {
    // Generates a secret key
    SecretKey sk = null;
    switch ( algorithm ) {
      case RandomValueMeta.TYPE_RANDOM_MAC_HMACMD5:
        sk = data.keyGenHmacMD5.generateKey();
        break;
      case RandomValueMeta.TYPE_RANDOM_MAC_HMACSHA1:
        sk = data.keyGenHmacSHA1.generateKey();
        break;
      default:
        break;
    }

    if ( sk == null ) {
      throw new KettleException( BaseMessages.getString( PKG, "RandomValue.Log.SecretKeyNull" ) );
    }

    // Create a MAC object using HMAC and initialize with key
    Mac mac = Mac.getInstance( sk.getAlgorithm() );
    mac.init( sk );
    // digest
    byte[] hashCode = mac.doFinal();
    StringBuilder encoded = new StringBuilder();
    for ( int i = 0; i < hashCode.length; i++ ) {
      String _b = Integer.toHexString( hashCode[i] );
      if ( _b.length() == 1 ) {
        _b = "0" + _b;
      }
      encoded.append( _b.substring( _b.length() - 2 ) );
    }

    return encoded.toString();

  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] row;
    if ( data.readsRows ) {
      row = getRow();
      if ( row == null ) {
        setOutputDone();
        return false;
      }

      if ( first ) {
        first = false;
        data.outputRowMeta = getInputRowMeta().clone();
        meta.getFields( getTransMeta().getBowl(), data.outputRowMeta, getStepname(), null, null, this, repository,
          metaStore );
      }
    } else {
      row = new Object[] {}; // empty row
      incrementLinesRead();

      if ( first ) {
        first = false;
        data.outputRowMeta = new RowMeta();
        meta.getFields( getTransMeta().getBowl(), data.outputRowMeta, getStepname(), null, null, this, repository,
          metaStore );
      }
    }

    RowMetaInterface imeta = getInputRowMeta();
    if ( imeta == null ) {
      imeta = new RowMeta();
      this.setInputRowMeta( imeta );
    }

    row = getRandomValue( imeta, row );

    if ( log.isRowLevel() ) {
      logRowlevel( BaseMessages.getString( PKG, "RandomValue.Log.ValueReturned", data.outputRowMeta
        .getString( row ) ) );
    }

    putRow( data.outputRowMeta, row );

    if ( !data.readsRows ) { // Just one row and then stop!

      setOutputDone();
      return false;
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (RandomValueMeta) smi;
    data = (RandomValueData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.readsRows = getStepMeta().getRemoteInputSteps().size() > 0;
      List<StepMeta> previous = getTransMeta().findPreviousSteps( getStepMeta() );
      if ( previous != null && previous.size() > 0 ) {
        data.readsRows = true;
      }
      boolean genHmacMD5 = false;
      boolean genHmacSHA1 = false;
      boolean uuid4 = false;

      for ( int i = 0; i < meta.getFieldName().length; i++ ) {
        switch ( meta.getFieldType()[i] ) {
          case RandomValueMeta.TYPE_RANDOM_MAC_HMACMD5:
            genHmacMD5 = true;
            break;
          case RandomValueMeta.TYPE_RANDOM_MAC_HMACSHA1:
            genHmacSHA1 = true;
            break;
          case RandomValueMeta.TYPE_RANDOM_UUID4:
            uuid4 = true;
            break;
          default:
            break;
        }
      }
      if ( genHmacMD5 ) {
        try {
          data.keyGenHmacMD5 = KeyGenerator.getInstance( "HmacMD5" );
        } catch ( NoSuchAlgorithmException s ) {
          logError( BaseMessages.getString( PKG, "RandomValue.Log.HmacMD5AlgorithmException", s.getMessage() ) );
          return false;
        }
      }
      if ( genHmacSHA1 ) {
        try {
          data.keyGenHmacSHA1 = KeyGenerator.getInstance( "HmacSHA1" );
        } catch ( NoSuchAlgorithmException s ) {
          logError( BaseMessages.getString( PKG, "RandomValue.Log.HmacSHA1AlgorithmException", s.getMessage() ) );
          return false;
        }
      }
      if ( uuid4 ) {
        data.u4 = new UUID4Util();
      }
      return true;
    }
    return false;
  }

  /**
   * Generates a JSON response containing the available random function types.
   * <p>
   * Iterates through the list of random value functions defined in {@link RandomValueMeta#functions},
   * and constructs a JSON array where each element represents a function type with its code and description.
   * The resulting JSON object contains this array under the key "randomFunctionTypes".
   * </p>
   *
   * <p>
   * <b>Note:</b> This method is invoked dynamically using reflection from {@link StepInterface#doAction}.
   * </p>
   *
   * @param queryParamToValues a map of query parameter names to their values (currently unused)
   * @return a {@link JSONObject} containing the available random function types
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject getRandomFunctionTypesAction( Map<String, String> queryParamToValues ) {
    JSONArray functionTypes = new JSONArray();

    Arrays.stream( RandomValueMeta.functions )
      .filter( Objects::nonNull )
      .forEach( func -> {
        JSONObject type = new JSONObject();
        type.put( "id", func.getCode() );
        type.put( "name", func.getDescription() );
        functionTypes.add( type );
      } );

    JSONObject response = new JSONObject();
    response.put( "randomFunctionTypes", functionTypes );
    return response;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    super.dispose( smi, sdi );
  }

}
