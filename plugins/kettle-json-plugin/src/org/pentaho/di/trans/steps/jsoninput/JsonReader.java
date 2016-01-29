/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.jsonpath.JsonJar;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;

@Deprecated
public class JsonReader {
  private static Class<?> PKG = JsonInputMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String JAVA_SCRIPT = "JavaScript";
  private static final String JSON_SCRIPT = "json.js";
  private static final String JSON_PATH_SCRIPT = "jsonpath.js";
  private static final String EVAL_FALSE = "false";
  private static final String EVAL = "var obj=";
  private static final String JSON_PATH = "jsonPath";

  private ScriptEngine jsEngine;

  private boolean ignoreMissingPath;

  public JsonReader() throws KettleException {
    init();
    this.ignoreMissingPath = false;
  }

  /**
   * @deprecated use {@link#setIgnoreMissingPath(boolean)}
   */
  @Deprecated
  public void SetIgnoreMissingPath( boolean value ) {
    setIgnoreMissingPath( value );
  }

  public void setIgnoreMissingPath( boolean value ) {
    this.ignoreMissingPath = value;
  }

  private void init() throws KettleException {

    try {

      ScriptEngineManager sm = new ScriptEngineManager();
      setEngine( sm.getEngineByName( JAVA_SCRIPT ) );
      if ( getEngine() == null ) {
        throw new KettleException( BaseMessages.getString( PKG, "JsonReader.Error.NoScriptEngineFound" ) );
      }

      // Load Json
      loadJsonScript( JSON_SCRIPT );

      // Load JsonPath
      loadJsonScript( JSON_PATH_SCRIPT );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JsonReader.Error.EngineInit", e.getMessage() ), e );
    }
  }

  private void loadJsonScript( String script ) throws Exception {
    InputStream is = null;
    InputStreamReader isr = null;
    try {
      is = JsonJar.class.getResource( script ).openStream();
      isr = new InputStreamReader( is );
      getEngine().eval( new BufferedReader( isr ) );
    } finally {
      try {
        if ( is != null ) {
          is.close();
        }
        if ( isr != null ) {
          isr.close();
        }
      } catch ( Exception e ) { /* Ignore */
      }
    }
  }

  private ScriptEngine getEngine() {
    return jsEngine;
  }

  private void setEngine( ScriptEngine script ) {
    jsEngine = script;
  }

  private Invocable getInvocable() {
    return (Invocable) getEngine();
  }

  public void readFile( String filename ) throws KettleException {
    InputStreamReader isr = null;
    try {
      isr = new InputStreamReader( KettleVFS.getInputStream( filename ) );
      Object o = JSONValue.parseWithException( isr );
      if ( o == null ) {
        throw new Exception( BaseMessages.getString( PKG, "JsonReader.Error.ReadFile.Null" ) );
      }
      eval( o );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JsonReader.Error.ParsingFile", e ) );
    } finally {
      try {
        if ( isr != null ) {
          isr.close();
        }
      } catch ( Exception e ) {
        // Ignore errors
      }
    }
  }

  public void readString( String value ) throws KettleException {
    try {
      Object o = JSONValue.parseWithException( value );
      if ( o == null ) {
        throw new Exception( BaseMessages.getString( PKG, "JsonReader.Error.ReadString.Null" ) );
      }
      eval( o );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JsonReader.Error.ParsingString", e ) );
    }
  }

  public void readUrl( String value ) throws KettleException {
    InputStreamReader is = null;
    try {
      URL url = new URL( value );
      is = new InputStreamReader( url.openConnection().getInputStream() );
      Object o = JSONValue.parse( is );
      if ( o == null ) {
        throw new Exception( BaseMessages.getString( PKG, "JsonReader.Error.ReadUrl.Null" ) );
      }
      eval( o );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JsonReader.Error.ParsingUrl", e ) );
    } finally {
      try {
        if ( is != null ) {
          is.close();
        }
      } catch ( Exception e ) {
        // Ignore errors
      }
    }
  }

  public void readInput( InputStream in ) throws KettleException {
    try ( InputStreamReader is = new InputStreamReader( in ) ) {
      Object o = JSONValue.parse( is );
      if ( o == null ) {
        throw new Exception( BaseMessages.getString( PKG, "JsonReader.Error.ReadUrl.Null" ) );
      }
      eval( o );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JsonReader.Error.ParsingUrl", e ) );
    }
  }

  private void eval( Object o ) throws Exception {
    getEngine().eval( EVAL + o.toString() );
  }

  public List<?> executePath( String value ) throws KettleException {
    try {
      String ro = getInvocable().invokeFunction( JSON_PATH, value ).toString();
      if ( !ro.equals( EVAL_FALSE ) ) {
        List<?> ra = (JSONArray) JSONValue.parse( ro );
        return ra;
      } else {
        if ( !isIgnoreMissingPath() ) {
          throw new KettleException( BaseMessages.getString( PKG, "JsonReader.Error.CanNotFindPath", value ) );
        } else {
          return null;
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }


  @Deprecated
  public NJSONArray getPath( String value ) throws KettleException {
    return new NJSONArray( (JSONArray) executePath( value ) );
  }

  public boolean isIgnoreMissingPath() {
    return this.ignoreMissingPath;
  }

}
