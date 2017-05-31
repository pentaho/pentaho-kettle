/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput.reader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.SingleRowRowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.jsoninput.JsonInputField;
import org.pentaho.di.trans.steps.jsoninput.JsonInputMeta;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.ReadContext;
import org.pentaho.di.trans.steps.jsoninput.exception.JsonInputException;

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

  private ReadContext jsonReadContext;
  private Configuration jsonConfiguration;

  private boolean ignoreMissingPath;

  private JsonInputField[] fields;
  private JsonPath[] paths = null;
  private LogChannelInterface log;

  private static final Option[] DEFAULT_OPTIONS =
    { Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL };

  protected FastJsonReader( LogChannelInterface log ) throws KettleException {
    this.ignoreMissingPath = false;
    this.jsonConfiguration = Configuration.defaultConfiguration().addOptions( DEFAULT_OPTIONS );
    this.log = log;
  }

  public FastJsonReader( JsonInputField[] fields, LogChannelInterface log ) throws KettleException {
    this( log );
    this.fields = fields;
    this.paths = compilePaths( fields );
  }

  public void setIgnoreMissingPath( boolean value ) {
    this.ignoreMissingPath = value;
  }

  private ParseContext getParseContext() {
    return JsonPath.using( jsonConfiguration );
  }

  private ReadContext getReadContext() {
    return jsonReadContext;
  }

  private static JsonPath[] compilePaths( JsonInputField[] fields ) {
    JsonPath[] paths = new JsonPath[ fields.length ];
    int i = 0;
    for ( JsonInputField field : fields ) {
      paths[ i++ ] = JsonPath.compile( field.getPath() );
    }
    return paths;
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

  @Override
  public void setFields( JsonInputField[] fields ) throws KettleException {
    this.fields = fields;
    this.paths = compilePaths( fields );
  }

  @Override
  public RowSet parse( InputStream in ) throws KettleException {
    readInput( in );
    List<Object[]> results = evalCombinedResult();
    if ( log.isDetailed() ) {
      log.logDetailed( BaseMessages.getString( PKG, "JsonInput.Log.NrRecords", results.size() ) );
    }
    return new TransposedRowSet( results );
  }

  private static class TransposedRowSet extends SingleRowRowSet {
    private List<Object[]> results;

    public TransposedRowSet( List<Object[]> results ) {
      super();
      this.results = results;
    }

    @Override
    public Object[] getRow() {
      if ( !results.isEmpty() ) {
        return results.remove( 0 );
      }

      return null;
    }

    @Override
    public int size() {
      return results.size();
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

  private List<Object[]> evalCombinedResult() throws JsonInputException {
    int lastSize = -1;
    String prevPath = null;
    List<List<?>> inputs = new ArrayList<>( paths.length );
    int i = 0;
    for ( JsonPath path : paths ) {
      List<Object> input = getReadContext().read( path );
      if ( input.size() != lastSize && lastSize > 0 & input.size() != 0 ) {
        throw new JsonInputException( BaseMessages.getString(
            PKG, "JsonInput.Error.BadStructure", input.size(), fields[i].getPath(), prevPath, lastSize ) );
      }
      if ( ( isAllNull( input ) || input.size() == 0 ) && !isIgnoreMissingPath() ) {
        throw new JsonInputException( BaseMessages.getString( PKG, "JsonReader.Error.CanNotFindPath", fields[i].getPath() ) );
      }
      inputs.add( input );
      lastSize = input.size();
      prevPath = fields[i].getPath();
      i++;
    }

    List<Object[]> resultRows = convertInputsIntoResultRows( inputs );

    filterOutExcessRows( resultRows );

    if ( !ignoreMissingPath & paths.length != 0 ) {
      raiseExceptionIfAnyMissingPath( resultRows );
    }

    return resultRows;
  }

  private List<Object[]> convertInputsIntoResultRows( List<List<?>> inputs ) {

    List<Object[]> resultRows = null;

    if ( inputs.isEmpty() ) {
      resultRows = new ArrayList<>( 1 );
      resultRows.add( new Object[]{} );
      return resultRows;
    }

    int rowCount = inputs.stream().max( Comparator.comparingInt( List::size ) ).get().size();

    resultRows = new ArrayList<>( rowCount );

    Object[] resultRow = null;
    for ( int rownum = 0; rownum < rowCount; rownum++ ) {
      resultRow = new Object[ inputs.size() ];
      for ( int col = 0; col < inputs.size(); col++ ) {
        if ( inputs.get( col ).size() == 0 ) {
          resultRow[ col ] = null;
          continue;
        }
        resultRow[ col ] = inputs.get( col ).get( rownum );
      }
      resultRows.add( resultRow );
    }

    return resultRows;
  }

  private void filterOutExcessRows( List<Object[]> resultRows ) {
    boolean atLeastOneNonNull = resultRows.stream().anyMatch( el -> Arrays.stream( el ).anyMatch( in -> in != null ) );

    if ( !atLeastOneNonNull ) {
      resultRows.clear();
      resultRows.add( new Object[]{} );
      return;
    }

    resultRows.removeIf( el -> Arrays.stream( el ).allMatch( in -> in == null ) );

  }

  private void raiseExceptionIfAnyMissingPath( List<Object[]> resultRows ) throws JsonInputException {
    for ( Object[] resultRow : resultRows ) {
      for ( int i = 0; i < resultRow.length; i++ ) {
        if ( resultRow[i] == null ) {
          throw new JsonInputException( BaseMessages.getString( PKG, "JsonReader.Error.CanNotFindPath", fields[i].getPath() ) );
        }
      }
    }
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
