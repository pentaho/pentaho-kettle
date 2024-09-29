/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.jsoninput.reader;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.ReadContext;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.SingleRowRowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.jsoninput.JsonInput;
import org.pentaho.di.trans.steps.jsoninput.JsonInputField;
import org.pentaho.di.trans.steps.jsoninput.JsonInputMeta;
import org.pentaho.di.trans.steps.jsoninput.exception.JsonInputException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Samatar
 * @author edube
 * @author jadametz
 * @since 2015-08-18
 */
public class FastJsonReader implements IJsonReader {
  private static Class<?> PKG = JsonInputMeta.class; // for i18n purposes, needed by Translator2!!

  // as per RFC 7159, the default JSON encoding shall be UTF-8
  // see https://tools.ietf.org/html/rfc7159#section-8.1
  private static final String JSON_CHARSET = "UTF-8";

  private static final JsonInputField[] ZERO_INPUT_FIELDS = new JsonInputField[ 0 ];

  private ReadContext jsonReadContext;
  private Configuration jsonConfiguration;

  private final boolean defaultPathLeafToNull;
  private final boolean ignoreMissingPath;
  private boolean includeNulls = false;

  private final JsonInput step;

  private JsonInputField[] inputFields;
  private JsonPath[] compiledJsonPaths = null;
  private LogChannelInterface log;

  public FastJsonReader( JsonInput step, JsonInputField[] inputFields, boolean defaultPathLeafToNull,
                         boolean ignoreMissingPath, LogChannelInterface log ) throws KettleException {

    this.defaultPathLeafToNull = defaultPathLeafToNull;
    this.ignoreMissingPath = ignoreMissingPath;
    this.step = step;
    this.log = log;

    setJsonConfiguration( defaultPathLeafToNull );
    setInputFields( inputFields );
  }

  public FastJsonReader( JsonInput step, JsonInputField[] inputFields, boolean defaultPathLeafToNull,
                         boolean ignoreMissingPath, boolean includeNulls,
                         LogChannelInterface log ) throws KettleException {

    this.defaultPathLeafToNull = defaultPathLeafToNull;
    this.ignoreMissingPath = ignoreMissingPath;
    this.includeNulls = includeNulls;
    this.step = step;
    this.log = log;

    setJsonConfiguration( defaultPathLeafToNull );
    setInputFields( inputFields );
  }

  private void setJsonConfiguration( boolean defaultPathLeafToNull ) {
    List<Option> options = new ArrayList<>();
    options.add( Option.SUPPRESS_EXCEPTIONS );
    options.add( Option.ALWAYS_RETURN_LIST );

    if ( defaultPathLeafToNull ) {
      options.add( Option.DEFAULT_PATH_LEAF_TO_NULL );
    }

    this.jsonConfiguration = Configuration.defaultConfiguration().addOptions( options.toArray( new Option[ 0 ] ) );
  }

  public boolean isDefaultPathLeafToNull() {
    return defaultPathLeafToNull;
  }

  Configuration getJsonConfiguration() {
    return jsonConfiguration;
  }

  private ParseContext getParseContext() {
    return JsonPath.using( jsonConfiguration );
  }

  private ReadContext getReadContext() {
    return jsonReadContext;
  }

  protected void readInput( InputStream is ) throws KettleException {
    jsonReadContext = getParseContext().parse( is, JSON_CHARSET );
    if ( jsonReadContext == null ) {
      throw new KettleException( BaseMessages.getString( PKG, "JsonReader.Error.ReadUrl.Null" ) );
    }
  }

  public boolean isIgnoreMissingPath() {
    return this.ignoreMissingPath;
  }

  public void setInputFields( JsonInputField[] inputFields ) {
    if ( null != inputFields ) {
      this.inputFields = inputFields;

      compiledJsonPaths = new JsonPath[ inputFields.length ];
      int i = 0;
      for ( JsonInputField inputField : inputFields ) {
        if ( System.getProperty( Const.KETTLE_COMPATIBILITY_JSON_INPUT_LEGACY_MODE, "N" ).equals( "Y" ) ) {
          compiledJsonPaths[ i++ ] = JsonPath.compile( step.environmentSubstitute( inputField.getPath(), false ).trim() );
        } else {
          compiledJsonPaths[ i++ ] = JsonPath.compile( step.environmentSubstitute( inputField.getPath(), true ) );
        }

      }
    } else {
      this.inputFields = ZERO_INPUT_FIELDS;
    }
  }

  @Override
  public RowSet parse( InputStream in ) throws KettleException {
    readInput( in );
    List<List<?>> results = evalCombinedResult();
    int len = results.isEmpty() ? 0 : getMaxRowSize( results );
    if ( log.isDetailed() ) {
      log.logDetailed( BaseMessages.getString( PKG, "JsonInput.Log.NrRecords", len ) );
    }
    if ( len == 0 ) {
      return getEmptyResponse();
    }
    return new TransposedRowSet( results, includeNulls );
  }

  /**
   * Gets the max size of the result rows.
   * @param results A list of lists representing the result rows
   * @return the size of the largest row in the results
   */
  protected static int getMaxRowSize( List<List<?>> results ) {
    return results.stream().mapToInt( List::size ).max().getAsInt();
  }

  private RowSet getEmptyResponse() {
    RowSet nullInputResponse = new SingleRowRowSet();
    nullInputResponse.putRow( null, new Object[ inputFields.length ] );
    nullInputResponse.setDone();
    return nullInputResponse;
  }

  private static class TransposedRowSet extends SingleRowRowSet {
    private List<List<?>> results;
    private final int rowCount;
    private int rowNbr;
    /**
     * if should skip null-only rows; size won't be exact if set.
     */
    private boolean cullNulls = true;
    private boolean includeNulls; // Include null values in result set

    public TransposedRowSet( List<List<?>> results, boolean includeNulls ) {
      super();
      this.includeNulls = includeNulls;
      this.results = results;
      this.rowCount = results.isEmpty() ? 0 : FastJsonReader.getMaxRowSize( results );
    }

    @Override
    public Object[] getRow() {
      boolean allNulls = cullNulls && rowCount > 1;
      Object[] rowData = null;
      do {
        if ( rowNbr >= rowCount ) {
          results.clear();
          return null;
        }
        rowData = new Object[ results.size() ];
        for ( int col = 0; col < results.size(); col++ ) {
          if ( results.get( col ).isEmpty() ) {
            rowData[ col ] = null;
            continue;
          }
          Object val = results.get( col ).get( rowNbr );
          rowData[ col ] = val;
          allNulls &= ( val == null && !includeNulls );
        }
        rowNbr++;
      } while ( allNulls );
      return rowData;
    }

    @Override
    public int size() {
      return rowCount - rowNbr;
    }

    @Override
    public boolean isDone() {
      // built at ctor
      return true;
    }

    @Override
    public void clear() {
      results.clear();
    }
  }

  private List<List<?>> evalCombinedResult() throws JsonInputException {
    int lastSize = -1;
    String prevPath = null;
    List<List<?>> results = new ArrayList<>( compiledJsonPaths.length );
    int i = 0;
    for ( JsonPath path : compiledJsonPaths ) {
      List<Object> result = getReadContext().read( path );
      if ( result.size() != lastSize && lastSize > 0 && !result.isEmpty() ) {
        throw new JsonInputException( BaseMessages.getString(
          PKG, "JsonInput.Error.BadStructure", result.size(), inputFields[ i ].getPath(), prevPath, lastSize ) );
      }
      if ( !isIgnoreMissingPath() && ( isAllNull( result ) || result.isEmpty() ) ) {
        throw new JsonInputException(
          BaseMessages.getString( PKG, "JsonReader.Error.CanNotFindPath", inputFields[ i ].getPath() ) );
      }
      results.add( result );
      lastSize = result.size();
      prevPath = inputFields[ i ].getPath();
      i++;
    }
    return results;
  }

  public static boolean isAllNull( Iterable<?> list ) {
    for ( Object obj : list ) {
      if ( obj != null ) {
        return false;
      }
    }
    return true;
  }
}
