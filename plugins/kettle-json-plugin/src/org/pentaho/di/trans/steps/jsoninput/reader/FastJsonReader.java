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
  private boolean defaultPathLeafToNull;

  private JsonInputField[] fields;
  private JsonPath[] paths = null;
  private LogChannelInterface log;

  private static final Option[] DEFAULT_OPTIONS = { Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST };

  protected FastJsonReader( LogChannelInterface log ) throws KettleException {
    this.ignoreMissingPath = false;
    this.defaultPathLeafToNull = false;
    this.jsonConfiguration = Configuration.defaultConfiguration().addOptions( DEFAULT_OPTIONS );
    this.log = log;
  }

  protected FastJsonReader( JsonInputField[] fields, LogChannelInterface log ) throws KettleException {
    this( log );
    this.fields = fields;
    this.paths = compilePaths( fields );
  }

  public FastJsonReader( JsonInputField[] fields, boolean defaultPathLeafToNull, LogChannelInterface log )
      throws KettleException {
    this( fields, log );
    setDefaultPathLeafToNull( defaultPathLeafToNull );
  }

  private Option[] getOptions() {
    ArrayList<Option> options = new ArrayList<>();
    for ( Option opt : DEFAULT_OPTIONS ) {
      options.add( opt );
    }
    if ( defaultPathLeafToNull ) {
      options.add( Option.DEFAULT_PATH_LEAF_TO_NULL );
    }
    return options.toArray( new Option[ options.size() ] );
  }

  private void updateConfig( Option option, boolean enabled ) {
    if ( enabled ) {
      jsonConfiguration = jsonConfiguration.addOptions( option );
    } else {
      jsonConfiguration = Configuration.defaultConfiguration().addOptions( getOptions() );
    }
  }

  public void setIgnoreMissingPath( boolean value ) {
    this.ignoreMissingPath = value;
  }

  public void setDefaultPathLeafToNull( boolean value ) {
    if ( value != this.defaultPathLeafToNull ) {
      this.defaultPathLeafToNull = value;
      updateConfig( Option.DEFAULT_PATH_LEAF_TO_NULL, value );
    }
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
    List<List<?>> results = evalCombinedResult();
    if ( log.isDetailed() ) {
      int len = results.isEmpty() ? 0 : results.get( 0 ).size();
      log.logDetailed( BaseMessages.getString( PKG, "JsonInput.Log.NrRecords", len ) );
    }
    return new TransposedRowSet( results );
  }

  private static class TransposedRowSet extends SingleRowRowSet {
    private List<List<?>> results;
    int rowCount;
    int rowNbr;

    public TransposedRowSet( List<List<?>> results ) {
      super();
      this.results = results;
      this.rowCount = results.isEmpty() ? 0 : results.get( 0 ).size();
    }

    @Override
    public Object[] getRow() {
      if ( rowNbr >= rowCount ) {
        results.clear();
        return null;
      }
      Object[] rowData = new Object[ results.size() ];
      for ( int col = 0; col < results.size(); col++ ) {
        rowData[ col ] = results.get( col ).get( rowNbr );
      }
      rowNbr++;
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

  private List<List<?>> evalCombinedResult() throws KettleException {
    int lastSize = -1;
    String prevPath = null;
    List<List<?>> results = new ArrayList<>( paths.length );
    int i = 0;
    for ( JsonPath path : paths ) {
      List<Object> res = getReadContext().read( path );
      if ( res.size() != lastSize && lastSize > 0 ) {
        throw new KettleException( BaseMessages.getString(
            PKG, "JsonInput.Error.BadStructure", res.size(), fields[i].getPath(), prevPath, lastSize ) );
      }
      if ( res.size() == 0 && !isIgnoreMissingPath() ) {
        throw new KettleException( BaseMessages.getString( PKG, "JsonReader.Error.CanNotFindPath", fields[i].getPath() ) );
      }
      results.add( res );
      lastSize = res.size();
      prevPath = fields[i].getPath();
      i++;
    }
    return results;
  }

}
