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

package org.pentaho.di.trans.steps.fuzzymatch;

import java.util.Iterator;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.wcohen.ss.Jaro;
import com.wcohen.ss.JaroWinkler;
import com.wcohen.ss.NeedlemanWunsch;

/**
 * Performs a fuzzy match for each main stream field row An approximative match is done in a lookup stream
 *
 * @author Samatar
 * @since 03-mars-2008
 */
public class FuzzyMatch extends BaseStep implements StepInterface {
  private static Class<?> PKG = FuzzyMatchMeta.class; // for i18n purposes, needed by Translator2!!

  private FuzzyMatchMeta meta;
  private FuzzyMatchData data;

  public FuzzyMatch( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private boolean readLookupValues() throws KettleException {
    data.infoStream = meta.getStepIOMeta().getInfoStreams().get( 0 );
    if ( data.infoStream.getStepMeta() == null ) {
      logError( BaseMessages.getString( PKG, "FuzzyMatch.Log.NoLookupStepSpecified" ) );
      return false;
    }

    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "FuzzyMatch.Log.ReadingFromStream" )
        + data.infoStream.getStepname() + "]" );
    }

    boolean firstRun = true;
    // Which row set do we read from?
    //
    RowSet rowSet = findInputRowSet( data.infoStream.getStepname() );
    Object[] rowData = getRowFrom( rowSet ); // rows are originating from "lookup_from"

    while ( rowData != null ) {
      if ( firstRun ) {
        data.infoMeta = rowSet.getRowMeta().clone();
        // Check lookup field
        int indexOfLookupField = data.infoMeta.indexOfValue( environmentSubstitute( meta.getLookupField() ) );
        if ( indexOfLookupField < 0 ) {
          // The field is unreachable !
          throw new KettleException( BaseMessages.getString(
            PKG, "FuzzyMatch.Exception.CouldnotFindLookField", meta.getLookupField() ) );
        }
        data.infoCache = new RowMeta();
        ValueMetaInterface keyValueMeta = data.infoMeta.getValueMeta( indexOfLookupField );
        keyValueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
        data.infoCache.addValueMeta( keyValueMeta );
        // Add key
        data.indexOfCachedFields[0] = indexOfLookupField;

        // Check additional fields
        if ( data.addAdditionalFields ) {
          ValueMetaInterface additionalFieldValueMeta;
          for ( int i = 0; i < meta.getValue().length; i++ ) {
            int fi = i + 1;
            data.indexOfCachedFields[fi] = data.infoMeta.indexOfValue( meta.getValue()[i] );
            if ( data.indexOfCachedFields[fi] < 0 ) {
              // The field is unreachable !
              throw new KettleException( BaseMessages.getString(
                PKG, "FuzzyMatch.Exception.CouldnotFindLookField", meta.getValue()[i] ) );
            }
            additionalFieldValueMeta = data.infoMeta.getValueMeta( data.indexOfCachedFields[fi] );
            additionalFieldValueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
            data.infoCache.addValueMeta( additionalFieldValueMeta );
          }
          data.nrCachedFields += meta.getValue().length;
        }
      }
      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "FuzzyMatch.Log.ReadLookupRow" )
          + rowSet.getRowMeta().getString( rowData ) );
      }

      // Look up the keys in the source rows
      // and store values in cache

      Object[] storeData = new Object[data.nrCachedFields];
      // Add key field
      if ( rowData[data.indexOfCachedFields[0]] == null ) {
        storeData[0] = "";
      } else {
        ValueMetaInterface fromStreamRowMeta = rowSet.getRowMeta().getValueMeta( data.indexOfCachedFields[0] );
        if ( fromStreamRowMeta.isStorageBinaryString() ) {
          storeData[0] = fromStreamRowMeta.convertToNormalStorageType( rowData[data.indexOfCachedFields[0]] );
        } else {
          storeData[0] = rowData[data.indexOfCachedFields[0]];
        }
      }

      // Add additional fields?
      for ( int i = 1; i < data.nrCachedFields; i++ ) {
        ValueMetaInterface fromStreamRowMeta = rowSet.getRowMeta().getValueMeta( data.indexOfCachedFields[i] );
        if ( fromStreamRowMeta.isStorageBinaryString() ) {
          storeData[i] = fromStreamRowMeta.convertToNormalStorageType( rowData[data.indexOfCachedFields[i]] );
        } else {
          storeData[i] = rowData[data.indexOfCachedFields[i]];
        }
      }
      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "FuzzyMatch.Log.AddingValueToCache", data.infoCache
          .getString( storeData ) ) );
      }

      addToCache( storeData );

      rowData = getRowFrom( rowSet );

      if ( firstRun ) {
        firstRun = false;
      }
    }

    return true;
  }

  private Object[] lookupValues( RowMetaInterface rowMeta, Object[] row ) throws KettleException {
    if ( first ) {
      first = false;

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields(
        data.outputRowMeta, getStepname(), new RowMetaInterface[] { data.infoMeta }, null, this, repository,
        metaStore );

      // Check lookup field
      data.indexOfMainField = getInputRowMeta().indexOfValue( environmentSubstitute( meta.getMainStreamField() ) );
      if ( data.indexOfMainField < 0 ) {
        // The field is unreachable !
        throw new KettleException( BaseMessages.getString( PKG, "FuzzyMatch.Exception.CouldnotFindMainField", meta
          .getMainStreamField() ) );
      }
    }
    Object[] add = null;
    if ( row[ data.indexOfMainField ] == null ) {
      add = buildEmptyRow();
    } else {
      try {
        add = getFromCache( row );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }
    return RowDataUtil.addRowData( row, rowMeta.size(), add );
  }

  private void addToCache( Object[] value ) throws KettleException {
    try {
      data.look.add( value );
    } catch ( java.lang.OutOfMemoryError o ) {
      // exception out of memory
      throw new KettleException( BaseMessages.getString( PKG, "FuzzyMatch.Error.JavaHeap", o.toString() ) );
    }
  }

  private Object[] getFromCache( Object[] keyRow ) throws KettleValueException {
    if ( isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "FuzzyMatch.Log.ReadingMainStreamRow", getInputRowMeta().getString(
        keyRow ) ) );
    }
    Object[] retval = null;
    switch ( meta.getAlgorithmType() ) {
      case FuzzyMatchMeta.OPERATION_TYPE_LEVENSHTEIN:
      case FuzzyMatchMeta.OPERATION_TYPE_DAMERAU_LEVENSHTEIN:
      case FuzzyMatchMeta.OPERATION_TYPE_NEEDLEMAN_WUNSH:
        retval = doDistance( keyRow );
        break;
      case FuzzyMatchMeta.OPERATION_TYPE_DOUBLE_METAPHONE:
      case FuzzyMatchMeta.OPERATION_TYPE_METAPHONE:
      case FuzzyMatchMeta.OPERATION_TYPE_SOUNDEX:
      case FuzzyMatchMeta.OPERATION_TYPE_REFINED_SOUNDEX:
        retval = doPhonetic( keyRow );
        break;
      case FuzzyMatchMeta.OPERATION_TYPE_JARO:
      case FuzzyMatchMeta.OPERATION_TYPE_JARO_WINKLER:
      case FuzzyMatchMeta.OPERATION_TYPE_PAIR_SIMILARITY:
        retval = doSimilarity( keyRow );
        break;
      default:

        break;
    }

    return retval;
  }

  private Object[] doDistance( Object[] row ) throws KettleValueException {
    // Reserve room
    Object[] rowData = buildEmptyRow();

    Iterator<Object[]> it = data.look.iterator();

    long distance = -1;

    // Object o=row[data.indexOfMainField];
    String lookupvalue = getInputRowMeta().getString( row, data.indexOfMainField );

    while ( it.hasNext() ) {
      // Get cached row data
      Object[] cachedData = it.next();
      // Key value is the first value
      String cacheValue = (String) cachedData[0];

      int cdistance = -1;
      String usecacheValue = cacheValue;
      String uselookupvalue = lookupvalue;
      if ( !meta.isCaseSensitive() ) {
        usecacheValue = cacheValue.toLowerCase();
        uselookupvalue = lookupvalue.toLowerCase();
      }

      switch ( meta.getAlgorithmType() ) {
        case FuzzyMatchMeta.OPERATION_TYPE_DAMERAU_LEVENSHTEIN:
          cdistance = Utils.getDamerauLevenshteinDistance( usecacheValue, uselookupvalue );
          break;
        case FuzzyMatchMeta.OPERATION_TYPE_NEEDLEMAN_WUNSH:
          cdistance = Math.abs( (int) new NeedlemanWunsch().score( usecacheValue, uselookupvalue ) );
          break;
        default:
          cdistance = StringUtils.getLevenshteinDistance( usecacheValue, uselookupvalue );
          break;
      }

      if ( data.minimalDistance <= cdistance && cdistance <= data.maximalDistance ) {
        if ( meta.isGetCloserValue() ) {
          if ( cdistance < distance || distance == -1 ) {
            // Get closer value
            // minimal distance
            distance = cdistance;
            int index = 0;
            rowData[index++] = cacheValue;
            // Add metric value?
            if ( data.addValueFieldName ) {
              rowData[index++] = distance;
            }
            // Add additional return values?
            if ( data.addAdditionalFields ) {
              for ( int i = 0; i < meta.getValue().length; i++ ) {
                int nr = i + 1;
                int nf = i + index;
                rowData[nf] = cachedData[nr];
              }
            }
          }
        } else {
          // get all values separated by values separator
          if ( rowData[0] == null ) {
            rowData[0] = cacheValue;
          } else {
            rowData[0] = (String) rowData[0] + data.valueSeparator + cacheValue;
          }
        }
      }
    }

    return rowData;
  }

  private Object[] doPhonetic( Object[] row ) {
    // Reserve room
    Object[] rowData = buildEmptyRow();

    Iterator<Object[]> it = data.look.iterator();

    Object o = row[data.indexOfMainField];
    String lookupvalue = (String) o;

    String lookupValueMF = getEncodedMF( lookupvalue, meta.getAlgorithmType() );

    while ( it.hasNext() ) {
      // Get cached row data
      Object[] cachedData = it.next();
      // Key value is the first value
      String cacheValue = (String) cachedData[0];

      String cacheValueMF = getEncodedMF( cacheValue, meta.getAlgorithmType() );

      if ( lookupValueMF.equals( cacheValueMF ) ) {

        // Add match value
        int index = 0;
        rowData[index++] = cacheValue;

        // Add metric value?
        if ( data.addValueFieldName ) {
          rowData[index++] = cacheValueMF;
        }
        // Add additional return values?
        if ( data.addAdditionalFields ) {
          for ( int i = 0; i < meta.getValue().length; i++ ) {
            int nf = i + index;
            int nr = i + 1;
            rowData[nf] = cachedData[nr];
          }
        }
      }
    }

    return rowData;
  }

  private String getEncodedMF( String value, Integer algorithmType ) {
    String encodedValueMF = "";
    switch ( algorithmType ) {
      case FuzzyMatchMeta.OPERATION_TYPE_METAPHONE:
        encodedValueMF = ( new Metaphone() ).metaphone( value );
        break;
      case FuzzyMatchMeta.OPERATION_TYPE_DOUBLE_METAPHONE:
        encodedValueMF = ( ( new DoubleMetaphone() ).doubleMetaphone( value ) );
        break;
      case FuzzyMatchMeta.OPERATION_TYPE_SOUNDEX:
        encodedValueMF = ( new Soundex() ).encode( value );
        break;
      case FuzzyMatchMeta.OPERATION_TYPE_REFINED_SOUNDEX:
        encodedValueMF = ( new RefinedSoundex() ).encode( value );
        break;
      default:
        break;
    }
    return encodedValueMF;
  }

  private Object[] doSimilarity( Object[] row ) {

    // Reserve room
    Object[] rowData = buildEmptyRow();
    // prepare to read from cache ...
    Iterator<Object[]> it = data.look.iterator();
    double similarity = 0;

    // get current value from main stream
    Object o = row[data.indexOfMainField];

    String lookupvalue = o == null ? "" : (String) o;

    while ( it.hasNext() ) {
      // Get cached row data
      Object[] cachedData = it.next();
      // Key value is the first value
      String cacheValue = (String) cachedData[0];

      double csimilarity = new Double( 0 );

      switch ( meta.getAlgorithmType() ) {
        case FuzzyMatchMeta.OPERATION_TYPE_JARO:
          csimilarity = new Jaro().score( cacheValue, lookupvalue );
          break;
        case FuzzyMatchMeta.OPERATION_TYPE_JARO_WINKLER:
          csimilarity = new JaroWinkler().score( cacheValue, lookupvalue );
          break;
        default:
          // Letters pair similarity
          csimilarity = LetterPairSimilarity.getSimiliarity( cacheValue, lookupvalue );
          break;
      }

      if ( data.minimalSimilarity <= csimilarity && csimilarity <= data.maximalSimilarity ) {
        if ( meta.isGetCloserValue() ) {
          if ( csimilarity > similarity || ( csimilarity == 0 && cacheValue.equals( lookupvalue ) ) ) {
            similarity = csimilarity;
            // Update match value
            int index = 0;
            rowData[index++] = cacheValue;
            // Add metric value?
            if ( data.addValueFieldName ) {
              rowData[index++] = new Double( similarity );
            }

            // Add additional return values?
            if ( data.addAdditionalFields ) {
              for ( int i = 0; i < meta.getValue().length; i++ ) {
                int nf = i + index;
                int nr = i + 1;
                rowData[nf] = cachedData[nr];
              }
            }
          }
        } else {
          // get all values separated by values separator
          if ( rowData[0] == null ) {
            rowData[0] = cacheValue;
          } else {
            rowData[0] = (String) rowData[0] + data.valueSeparator + cacheValue;
          }
        }
      }
    }

    return rowData;
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
    meta = (FuzzyMatchMeta) smi;
    data = (FuzzyMatchData) sdi;

    if ( data.readLookupValues ) {
      data.readLookupValues = false;

      // Read values from lookup step (look)
      if ( !readLookupValues() ) {
        logError( BaseMessages.getString( PKG, "FuzzyMatch.Log.UnableToReadDataFromLookupStream" ) );
        setErrors( 1 );
        stopAll();
        return false;
      }
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "FuzzyMatch.Log.ReadValuesInMemory", data.look.size() ) );
      }
    }

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) {
      // no more input to be expected...
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "FuzzyMatch.Log.StoppedProcessingWithEmpty", getLinesRead() ) );
      }
      setOutputDone();
      return false;
    }

    try {

      // Do the actual lookup in the hastable.
      Object[] outputRow = lookupValues( getInputRowMeta(), r );
      if ( outputRow == null ) {
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      putRow( data.outputRowMeta, outputRow ); // copy row to output rowset(s);

      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "FuzzyMatch.Log.LineNumber" ) + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {
      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "FuzzyMatch.Log.ErrorInStepRunning" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, meta.getMainStreamField(), "FuzzyMatch001" );
      }

    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (FuzzyMatchMeta) smi;
    data = (FuzzyMatchData) sdi;

    if ( super.init( smi, sdi ) ) {

      // Check lookup and main stream field
      if ( Utils.isEmpty( meta.getMainStreamField() ) ) {
        logError( BaseMessages.getString( PKG, "FuzzyMatch.Error.MainStreamFieldMissing" ) );
        return false;
      }
      if ( Utils.isEmpty( meta.getLookupField() ) ) {
        logError( BaseMessages.getString( PKG, "FuzzyMatch.Error.LookupStreamFieldMissing" ) );
        return false;
      }

      // Checks output fields
      String matchField = environmentSubstitute( meta.getOutputMatchField() );
      if ( Utils.isEmpty( matchField ) ) {
        logError( BaseMessages.getString( PKG, "FuzzyMatch.Error.OutputMatchFieldMissing" ) );
        return false;
      }

      // We need to add metrics (distance, similarity, ...)
      // only when the fieldname is provided
      // and user want to return the closer value
      data.addValueFieldName =
        ( !Utils.isEmpty( environmentSubstitute( meta.getOutputValueField() ) ) && meta.isGetCloserValue() );

      // Set the number of fields to cache
      // default value is one
      int nrFields = 1;

      if ( meta.getValue() != null && meta.getValue().length > 0 ) {

        if ( meta.isGetCloserValue()
          || ( meta.getAlgorithmType() == FuzzyMatchMeta.OPERATION_TYPE_DOUBLE_METAPHONE )
          || ( meta.getAlgorithmType() == FuzzyMatchMeta.OPERATION_TYPE_SOUNDEX )
          || ( meta.getAlgorithmType() == FuzzyMatchMeta.OPERATION_TYPE_REFINED_SOUNDEX )
          || ( meta.getAlgorithmType() == FuzzyMatchMeta.OPERATION_TYPE_METAPHONE ) ) {
          // cache also additional fields
          data.addAdditionalFields = true;
          nrFields += meta.getValue().length;
        }
      }
      data.indexOfCachedFields = new int[nrFields];

      switch ( meta.getAlgorithmType() ) {
        case FuzzyMatchMeta.OPERATION_TYPE_LEVENSHTEIN:
        case FuzzyMatchMeta.OPERATION_TYPE_DAMERAU_LEVENSHTEIN:
        case FuzzyMatchMeta.OPERATION_TYPE_NEEDLEMAN_WUNSH:
          data.minimalDistance = Const.toInt( environmentSubstitute( meta.getMinimalValue() ), 0 );
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "FuzzyMatch.Log.MinimalDistance", data.minimalDistance ) );
          }
          data.maximalDistance = Const.toInt( environmentSubstitute( meta.getMaximalValue() ), 5 );
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "FuzzyMatch.Log.MaximalDistance", data.maximalDistance ) );
          }
          if ( !meta.isGetCloserValue() ) {
            data.valueSeparator = environmentSubstitute( meta.getSeparator() );
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "FuzzyMatch.Log.Separator", data.valueSeparator ) );
            }
          }
          break;
        case FuzzyMatchMeta.OPERATION_TYPE_JARO:
        case FuzzyMatchMeta.OPERATION_TYPE_JARO_WINKLER:
        case FuzzyMatchMeta.OPERATION_TYPE_PAIR_SIMILARITY:
          data.minimalSimilarity = Const.toDouble( environmentSubstitute( meta.getMinimalValue() ), 0 );
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "FuzzyMatch.Log.MinimalSimilarity", data.minimalSimilarity ) );
          }
          data.maximalSimilarity = Const.toDouble( environmentSubstitute( meta.getMaximalValue() ), 1 );
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "FuzzyMatch.Log.MaximalSimilarity", data.maximalSimilarity ) );
          }
          if ( !meta.isGetCloserValue() ) {
            data.valueSeparator = environmentSubstitute( meta.getSeparator() );
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "FuzzyMatch.Log.Separator", data.valueSeparator ) );
            }
          }
          break;
        default:
          break;
      }

      data.readLookupValues = true;

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (FuzzyMatchMeta) smi;
    data = (FuzzyMatchData) sdi;
    data.look.clear();
    super.dispose( smi, sdi );
  }

}
