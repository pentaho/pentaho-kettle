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

package org.pentaho.di.trans.steps.scriptvalues_mod;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.pentaho.di.compatibility.Row;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.plugins.KettleURLClassLoader;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/*
 * Created on 2-jun-2003
 *
 */
public class ScriptValuesMetaMod extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = ScriptValuesMetaMod.class; // for i18n purposes, needed by Translator2!!

  private static final String JSSCRIPT_TAG_TYPE = "jsScript_type";
  private static final String JSSCRIPT_TAG_NAME = "jsScript_name";
  private static final String JSSCRIPT_TAG_SCRIPT = "jsScript_script";

  public static final String OPTIMIZATION_LEVEL_DEFAULT = "9";

  private ScriptValuesAddClasses[] additionalClasses;
  private ScriptValuesScript[] jsScripts;

  private String[] fieldname;
  private String[] rename;
  private int[] type;
  private int[] length;
  private int[] precision;
  private boolean[] replace; // Replace the specified field.

  private boolean compatible;
  private String optimizationLevel;

  public ScriptValuesMetaMod() {
    super(); // allocate BaseStepMeta
    compatible = true;
    try {
      parseXmlForAdditionalClasses();
    } catch ( Exception e ) { /* Ignore */
    }
  }

  /**
   * @return Returns the length.
   */
  public int[] getLength() {
    return length;
  }

  /**
   * @param length
   *          The length to set.
   */
  public void setLength( int[] length ) {
    this.length = length;
  }

  /**
   * @return Returns the name.
   */
  public String[] getFieldname() {
    return fieldname;
  }

  /**
   * @param fieldname
   *          The name to set.
   */
  public void setFieldname( String[] fieldname ) {
    this.fieldname = fieldname;
  }

  /**
   * @return Returns the precision.
   */
  public int[] getPrecision() {
    return precision;
  }

  /**
   * @param precision
   *          The precision to set.
   */
  public void setPrecision( int[] precision ) {
    this.precision = precision;
  }

  /**
   * @return Returns the rename.
   */
  public String[] getRename() {
    return rename;
  }

  /**
   * @param rename
   *          The rename to set.
   */
  public void setRename( String[] rename ) {
    this.rename = rename;
  }

  /**
   * @return Returns the type.
   */
  public int[] getType() {
    return type;
  }

  /**
   * @param type
   *          The type to set.
   */
  public void setType( int[] type ) {
    this.type = type;
  }

  public int getNumberOfJSScripts() {
    return jsScripts.length;
  }

  public String[] getJSScriptNames() {
    String[] strJSNames = new String[jsScripts.length];
    for ( int i = 0; i < jsScripts.length; i++ ) {
      strJSNames[i] = jsScripts[i].getScriptName();
    }
    return strJSNames;
  }

  public ScriptValuesScript[] getJSScripts() {
    return jsScripts;
  }

  public void setJSScripts( ScriptValuesScript[] jsScripts ) {
    this.jsScripts = jsScripts;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrfields ) {
    fieldname = new String[nrfields];
    rename = new String[nrfields];
    type = new int[nrfields];
    length = new int[nrfields];
    precision = new int[nrfields];
    replace = new boolean[nrfields];
  }

  public Object clone() {
    ScriptValuesMetaMod retval = (ScriptValuesMetaMod) super.clone();

    int nrfields = fieldname.length;

    retval.allocate( nrfields );
    System.arraycopy( fieldname, 0, retval.fieldname, 0, nrfields );
    System.arraycopy( rename, 0, retval.rename, 0, nrfields );
    System.arraycopy( type, 0, retval.type, 0, nrfields );
    System.arraycopy( length, 0, retval.length, 0, nrfields );
    System.arraycopy( precision, 0, retval.precision, 0, nrfields );
    System.arraycopy( replace, 0, retval.replace, 0, nrfields );

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      String script = XMLHandler.getTagValue( stepnode, "script" );
      String strCompatible = XMLHandler.getTagValue( stepnode, "compatible" );
      optimizationLevel = XMLHandler.getTagValue( stepnode, "optimizationLevel" );

      if ( strCompatible == null ) {
        compatible = true;
      } else {
        compatible = "Y".equalsIgnoreCase( strCompatible );
      }

      // When in compatibility mode, we load the script, not the other tabs...
      //
      if ( !Utils.isEmpty( script ) ) {
        jsScripts = new ScriptValuesScript[1];
        jsScripts[0] = new ScriptValuesScript( ScriptValuesScript.TRANSFORM_SCRIPT, "ScriptValue", script );
      } else {
        Node scripts = XMLHandler.getSubNode( stepnode, "jsScripts" );
        int nrscripts = XMLHandler.countNodes( scripts, "jsScript" );
        jsScripts = new ScriptValuesScript[nrscripts];
        for ( int i = 0; i < nrscripts; i++ ) {
          Node fnode = XMLHandler.getSubNodeByNr( scripts, "jsScript", i );

          jsScripts[i] =
            new ScriptValuesScript(
              Integer.parseInt( XMLHandler.getTagValue( fnode, JSSCRIPT_TAG_TYPE ) ), XMLHandler.getTagValue(
                fnode, JSSCRIPT_TAG_NAME ), XMLHandler.getTagValue( fnode, JSSCRIPT_TAG_SCRIPT ) );
        }
      }

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldname[i] = XMLHandler.getTagValue( fnode, "name" );
        rename[i] = XMLHandler.getTagValue( fnode, "rename" );
        type[i] = ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( fnode, "type" ) );

        String slen = XMLHandler.getTagValue( fnode, "length" );
        String sprc = XMLHandler.getTagValue( fnode, "precision" );
        length[i] = Const.toInt( slen, -1 );
        precision[i] = Const.toInt( sprc, -1 );
        replace[i] = "Y".equalsIgnoreCase( XMLHandler.getTagValue( fnode, "replace" ) );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "ScriptValuesMetaMod.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    jsScripts = new ScriptValuesScript[1];
    jsScripts[0] =
      new ScriptValuesScript( ScriptValuesScript.TRANSFORM_SCRIPT, BaseMessages.getString(
        PKG, "ScriptValuesMod.Script1" ), "//"
        + BaseMessages.getString( PKG, "ScriptValuesMod.ScriptHere" ) + Const.CR + Const.CR );

    int nrfields = 0;
    allocate( nrfields );

    for ( int i = 0; i < nrfields; i++ ) {
      fieldname[i] = "newvalue";
      rename[i] = "newvalue";
      type[i] = ValueMetaInterface.TYPE_NUMBER;
      length[i] = -1;
      precision[i] = -1;
      replace[i] = false;
    }

    compatible = false;
    optimizationLevel = OPTIMIZATION_LEVEL_DEFAULT;
  }

  public void getFields( RowMetaInterface row, String originStepname, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    try {
      for ( int i = 0; i < fieldname.length; i++ ) {
        if ( !Utils.isEmpty( fieldname[i] ) ) {
          int valueIndex = -1;
          ValueMetaInterface v;
          if ( replace[i] ) {
            valueIndex = row.indexOfValue( fieldname[i] );
            if ( valueIndex < 0 ) {
              // The field was not found using the "name" field
              if ( Utils.isEmpty( rename[i] ) ) {
                // There is no "rename" field to try; Therefore we cannot find the
                // field to replace
                throw new KettleStepException( BaseMessages.getString(
                  PKG, "ScriptValuesMetaMod.Exception.FieldToReplaceNotFound", fieldname[i] ) );
              } else {
                // Lookup the field to replace using the "rename" field
                valueIndex = row.indexOfValue( rename[i] );
                if ( valueIndex < 0 ) {
                  // The field was not found using the "rename" field"; Therefore
                  // we cannot find the field to replace
                  //
                  throw new KettleStepException( BaseMessages.getString(
                    PKG, "ScriptValuesMetaMod.Exception.FieldToReplaceNotFound", rename[i] ) );
                }
              }
            }

            // Change the data type to match what's specified...
            //
            ValueMetaInterface source = row.getValueMeta( valueIndex );
            v = ValueMetaFactory.cloneValueMeta( source, type[i] );
            row.setValueMeta( valueIndex, v );
          } else {
            if ( !Utils.isEmpty( rename[i] ) ) {
              v = ValueMetaFactory.createValueMeta( rename[i], type[i] );
            } else {
              v = ValueMetaFactory.createValueMeta( fieldname[i], type[i] );
            }
          }
          v.setLength( length[i] );
          v.setPrecision( precision[i] );
          v.setOrigin( originStepname );
          if ( !replace[i] ) {
            row.addValueMeta( v );
          }
        }
      }
    } catch ( KettleException e ) {
      throw new KettleStepException( e );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "compatible", compatible ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "optimizationLevel", optimizationLevel ) );

    retval.append( "    <jsScripts>" );
    for ( int i = 0; i < jsScripts.length; i++ ) {
      retval.append( "      <jsScript>" );
      retval
        .append( "        " ).append( XMLHandler.addTagValue( JSSCRIPT_TAG_TYPE, jsScripts[i].getScriptType() ) );
      retval
        .append( "        " ).append( XMLHandler.addTagValue( JSSCRIPT_TAG_NAME, jsScripts[i].getScriptName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( JSSCRIPT_TAG_SCRIPT, jsScripts[i].getScript() ) );
      retval.append( "      </jsScript>" );
    }
    retval.append( "    </jsScripts>" );

    retval.append( "    <fields>" );
    for ( int i = 0; i < fieldname.length; i++ ) {
      retval.append( "      <field>" );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", fieldname[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "rename", rename[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "type",
        ValueMetaFactory.getValueMetaName( type[i] ) ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "length", length[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "precision", precision[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "replace", replace[i] ) );
      retval.append( "      </field>" );
    }
    retval.append( "    </fields>" );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      String script = rep.getStepAttributeString( id_step, "script" );
      compatible = rep.getStepAttributeBoolean( id_step, 0, "compatible", true );
      optimizationLevel = rep.getStepAttributeString( id_step, 0, "optimizationLevel" );

      // When in compatibility mode, we load the script, not the other tabs...
      //
      if ( !Utils.isEmpty( script ) ) {
        jsScripts = new ScriptValuesScript[1];
        jsScripts[0] = new ScriptValuesScript( ScriptValuesScript.TRANSFORM_SCRIPT, "ScriptValue", script );
      } else {
        int nrScripts = rep.countNrStepAttributes( id_step, JSSCRIPT_TAG_NAME );
        jsScripts = new ScriptValuesScript[nrScripts];
        for ( int i = 0; i < nrScripts; i++ ) {
          jsScripts[i] = new ScriptValuesScript(
            (int) rep.getStepAttributeInteger( id_step, i, JSSCRIPT_TAG_TYPE ),
            rep.getStepAttributeString( id_step, i, JSSCRIPT_TAG_NAME ),
            rep.getStepAttributeString( id_step, i, JSSCRIPT_TAG_SCRIPT ) );
        }
      }

      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );
      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldname[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        rename[i] = rep.getStepAttributeString( id_step, i, "field_rename" );
        type[i] = ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "field_type" ) );
        length[i] = (int) rep.getStepAttributeInteger( id_step, i, "field_length" );
        precision[i] = (int) rep.getStepAttributeInteger( id_step, i, "field_precision" );
        replace[i] = rep.getStepAttributeBoolean( id_step, i, "field_replace" );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ScriptValuesMetaMod.Exception.UnexpectedErrorInReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, 0, "compatible", compatible );
      rep.saveStepAttribute( id_transformation, id_step, 0, "optimizationLevel", optimizationLevel );

      for ( int i = 0; i < jsScripts.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, JSSCRIPT_TAG_NAME, jsScripts[i].getScriptName() );
        rep.saveStepAttribute( id_transformation, id_step, i, JSSCRIPT_TAG_SCRIPT, jsScripts[i].getScript() );
        rep.saveStepAttribute( id_transformation, id_step, i, JSSCRIPT_TAG_TYPE, jsScripts[i].getScriptType() );
      }

      for ( int i = 0; i < fieldname.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldname[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_rename", rename[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_type",
          ValueMetaFactory.getValueMetaName( type[i] ) );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_length", length[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_precision", precision[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_replace", replace[i] );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages
        .getString( PKG, "ScriptValuesMetaMod.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    boolean error_found = false;
    String error_message = "";
    CheckResult cr;

    Context jscx;
    Scriptable jsscope;
    Script jsscript;

    jscx = ContextFactory.getGlobal().enterContext();
    jsscope = jscx.initStandardObjects( null, false );
    try {
      jscx.setOptimizationLevel( Integer.valueOf( transMeta.environmentSubstitute( optimizationLevel ) ) );
    } catch ( NumberFormatException nfe ) {
      error_message =
        "Error with optimization level.  Could not convert the value of "
          + transMeta.environmentSubstitute( optimizationLevel ) + " to an integer.";
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    } catch ( IllegalArgumentException iae ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, iae.getMessage(), stepMeta );
      remarks.add( cr );
    }

    String strActiveStartScriptName = "";
    String strActiveEndScriptName = "";

    String strActiveScript = "";
    String strActiveStartScript = "";
    String strActiveEndScript = "";

    // Building the Scripts
    if ( jsScripts.length > 0 ) {
      for ( int i = 0; i < jsScripts.length; i++ ) {
        if ( jsScripts[i].isTransformScript() ) {
          // strActiveScriptName =jsScripts[i].getScriptName();
          strActiveScript = jsScripts[i].getScript();
        } else if ( jsScripts[i].isStartScript() ) {
          strActiveStartScriptName = jsScripts[i].getScriptName();
          strActiveStartScript = jsScripts[i].getScript();
        } else if ( jsScripts[i].isEndScript() ) {
          strActiveEndScriptName = jsScripts[i].getScriptName();
          strActiveEndScript = jsScripts[i].getScript();
        }
      }
    }

    if ( prev != null && strActiveScript.length() > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ScriptValuesMetaMod.CheckResult.ConnectedStepOK", String.valueOf( prev.size() ) ), stepMeta );
      remarks.add( cr );

      // Adding the existing Scripts to the Context
      for ( int i = 0; i < getNumberOfJSScripts(); i++ ) {
        Scriptable jsR = Context.toObject( jsScripts[i].getScript(), jsscope );
        jsscope.put( jsScripts[i].getScriptName(), jsscope, jsR );
      }

      // Modification for Additional Script parsing
      try {
        if ( getAddClasses() != null ) {
          for ( int i = 0; i < getAddClasses().length; i++ ) {
            Object jsOut = Context.javaToJS( getAddClasses()[i].getAddObject(), jsscope );
            ScriptableObject.putProperty( jsscope, getAddClasses()[i].getJSName(), jsOut );
          }
        }
      } catch ( Exception e ) {
        error_message = ( "Couldn't add JavaClasses to Context! Error:" );
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      }

      // Adding some default JavaScriptFunctions to the System
      try {
        Context.javaToJS( ScriptValuesAddedFunctions.class, jsscope );
        ( (ScriptableObject) jsscope )
          .defineFunctionProperties(
            ScriptValuesAddedFunctions.jsFunctionList, ScriptValuesAddedFunctions.class,
            ScriptableObject.DONTENUM );
      } catch ( Exception ex ) {
        error_message = "Couldn't add Default Functions! Error:" + Const.CR + ex.toString();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      }

      // Adding some Constants to the JavaScript
      try {
        jsscope.put( "SKIP_TRANSFORMATION", jsscope, Integer.valueOf( ScriptValuesMod.SKIP_TRANSFORMATION ) );
        jsscope.put( "ABORT_TRANSFORMATION", jsscope, Integer.valueOf( ScriptValuesMod.ABORT_TRANSFORMATION ) );
        jsscope.put( "ERROR_TRANSFORMATION", jsscope, Integer.valueOf( ScriptValuesMod.ERROR_TRANSFORMATION ) );
        jsscope
          .put( "CONTINUE_TRANSFORMATION", jsscope, Integer.valueOf( ScriptValuesMod.CONTINUE_TRANSFORMATION ) );
      } catch ( Exception ex ) {
        error_message = "Couldn't add Transformation Constants! Error:" + Const.CR + ex.toString();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      }

      try {
        ScriptValuesModDummy dummyStep = new ScriptValuesModDummy( prev, transMeta.getStepFields( stepMeta ) );
        Scriptable jsvalue = Context.toObject( dummyStep, jsscope );
        jsscope.put( "_step_", jsscope, jsvalue );

        Object[] row = new Object[prev.size()];
        Scriptable jsRowMeta = Context.toObject( prev, jsscope );
        jsscope.put( "rowMeta", jsscope, jsRowMeta );
        for ( int i = 0; i < prev.size(); i++ ) {
          ValueMetaInterface valueMeta = prev.getValueMeta( i );
          Object valueData = null;

          // Set date and string values to something to simulate real thing
          //
          if ( valueMeta.isDate() ) {
            valueData = new Date();
          }
          if ( valueMeta.isString() ) {
            valueData = "test value test value test value test value test value "
              + "test value test value test value test value test value";
          }
          if ( valueMeta.isInteger() ) {
            valueData = Long.valueOf( 0L );
          }
          if ( valueMeta.isNumber() ) {
            valueData = new Double( 0.0 );
          }
          if ( valueMeta.isBigNumber() ) {
            valueData = BigDecimal.ZERO;
          }
          if ( valueMeta.isBoolean() ) {
            valueData = Boolean.TRUE;
          }
          if ( valueMeta.isBinary() ) {
            valueData = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, };
          }

          row[i] = valueData;

          if ( isCompatible() ) {
            Value value = valueMeta.createOriginalValue( valueData );
            Scriptable jsarg = Context.toObject( value, jsscope );
            jsscope.put( valueMeta.getName(), jsscope, jsarg );
          } else {
            Scriptable jsarg = Context.toObject( valueData, jsscope );
            jsscope.put( valueMeta.getName(), jsscope, jsarg );
          }
        }
        // Add support for Value class (new Value())
        Scriptable jsval = Context.toObject( Value.class, jsscope );
        jsscope.put( "Value", jsscope, jsval );

        // Add the old style row object for compatibility reasons...
        //
        if ( isCompatible() ) {
          Row v2Row = RowMeta.createOriginalRow( prev, row );
          Scriptable jsV2Row = Context.toObject( v2Row, jsscope );
          jsscope.put( "row", jsscope, jsV2Row );
        } else {
          Scriptable jsRow = Context.toObject( row, jsscope );
          jsscope.put( "row", jsscope, jsRow );
        }
      } catch ( Exception ev ) {
        error_message = "Couldn't add Input fields to Script! Error:" + Const.CR + ev.toString();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      }

      try {
        // Checking for StartScript
        if ( strActiveStartScript != null && strActiveStartScript.length() > 0 ) {
          /* Object startScript = */jscx.evaluateString( jsscope, strActiveStartScript, "trans_Start", 1, null );
          error_message = "Found Start Script. " + strActiveStartScriptName + " Processing OK";
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, error_message, stepMeta );
          remarks.add( cr );
        }
      } catch ( Exception e ) {
        error_message = "Couldn't process Start Script! Error:" + Const.CR + e.toString();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      }

      try {
        jsscript = jscx.compileString( strActiveScript, "script", 1, null );

        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "ScriptValuesMetaMod.CheckResult.ScriptCompiledOK" ), stepMeta );
        remarks.add( cr );

        try {

          jsscript.exec( jscx, jsscope );

          cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "ScriptValuesMetaMod.CheckResult.ScriptCompiledOK2" ), stepMeta );
          remarks.add( cr );

          if ( fieldname.length > 0 ) {
            StringBuilder message =
              new StringBuilder( BaseMessages.getString(
                PKG, "ScriptValuesMetaMod.CheckResult.FailedToGetValues", String.valueOf( fieldname.length ) )
                + Const.CR + Const.CR );

            if ( error_found ) {
              cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message.toString(), stepMeta );
            } else {
              cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, message.toString(), stepMeta );
            }
            remarks.add( cr );
          }
        } catch ( JavaScriptException jse ) {
          Context.exit();
          error_message =
            BaseMessages.getString( PKG, "ScriptValuesMetaMod.CheckResult.CouldNotExecuteScript" )
              + Const.CR + jse.toString();
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          remarks.add( cr );
        } catch ( Exception e ) {
          Context.exit();
          error_message =
            BaseMessages.getString( PKG, "ScriptValuesMetaMod.CheckResult.CouldNotExecuteScript2" )
              + Const.CR + e.toString();
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          remarks.add( cr );
        }

        // Checking End Script
        try {
          if ( strActiveEndScript != null && strActiveEndScript.length() > 0 ) {
            /* Object endScript = */jscx.evaluateString( jsscope, strActiveEndScript, "trans_End", 1, null );
            error_message = "Found End Script. " + strActiveEndScriptName + " Processing OK";
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, error_message, stepMeta );
            remarks.add( cr );
          }
        } catch ( Exception e ) {
          error_message = "Couldn't process End Script! Error:" + Const.CR + e.toString();
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
          remarks.add( cr );
        }
      } catch ( Exception e ) {
        Context.exit();
        error_message =
          BaseMessages.getString( PKG, "ScriptValuesMetaMod.CheckResult.CouldNotCompileScript" )
            + Const.CR + e.toString();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      }
    } else {
      Context.exit();
      error_message =
        BaseMessages.getString( PKG, "ScriptValuesMetaMod.CheckResult.CouldNotGetFieldsFromPreviousStep" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ScriptValuesMetaMod.CheckResult.ConnectedStepOK2" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "ScriptValuesMetaMod.CheckResult.NoInputReceived" ), stepMeta );
      remarks.add( cr );
    }
  }

  public String getFunctionFromScript( String strFunction, String strScript ) {
    String sRC = "";
    int iStartPos = strScript.indexOf( strFunction );
    if ( iStartPos > 0 ) {
      iStartPos = strScript.indexOf( '{', iStartPos );
      int iCounter = 1;
      while ( iCounter != 0 ) {
        if ( strScript.charAt( iStartPos++ ) == '{' ) {
          iCounter++;
        } else if ( strScript.charAt( iStartPos++ ) == '}' ) {
          iCounter--;
        }
        sRC = sRC + strScript.charAt( iStartPos );
      }
    }
    return sRC;
  }

  public boolean getValue( Scriptable scope, int i, Value res, StringBuilder message ) {
    boolean error_found = false;

    if ( fieldname[i] != null && fieldname[i].length() > 0 ) {
      res.setName( rename[i] );
      res.setType( type[i] );

      try {

        Object result = scope.get( fieldname[i], scope );
        if ( result != null ) {

          String classname = result.getClass().getName();

          switch ( type[i] ) {
            case ValueMetaInterface.TYPE_NUMBER:
              if ( classname.equalsIgnoreCase( "org.mozilla.javascript.Undefined" ) ) {
                res.setNull();
              } else if ( classname.equalsIgnoreCase( "org.mozilla.javascript.NativeJavaObject" ) ) {
                // Is it a java Value class ?
                Value v = (Value) Context.jsToJava( result, Value.class );
                res.setValue( v.getNumber() );
              } else {
                res.setValue( ( (Double) result ).doubleValue() );
              }
              break;
            case ValueMetaInterface.TYPE_INTEGER:
              if ( classname.equalsIgnoreCase( "java.lang.Byte" ) ) {
                res.setValue( ( (java.lang.Byte) result ).longValue() );
              } else if ( classname.equalsIgnoreCase( "java.lang.Short" ) ) {
                res.setValue( ( (Short) result ).longValue() );
              } else if ( classname.equalsIgnoreCase( "java.lang.Integer" ) ) {
                res.setValue( ( (Integer) result ).longValue() );
              } else if ( classname.equalsIgnoreCase( "java.lang.Long" ) ) {
                res.setValue( ( (Long) result ).longValue() );
              } else if ( classname.equalsIgnoreCase( "org.mozilla.javascript.Undefined" ) ) {
                res.setNull();
              } else if ( classname.equalsIgnoreCase( "org.mozilla.javascript.NativeJavaObject" ) ) {
                // Is it a java Value class ?
                Value v = (Value) Context.jsToJava( result, Value.class );
                res.setValue( v.getInteger() );
              } else {
                res.setValue( Math.round( ( (Double) result ).doubleValue() ) );
              }
              break;
            case ValueMetaInterface.TYPE_STRING:
              if ( classname.equalsIgnoreCase( "org.mozilla.javascript.NativeJavaObject" )
                || classname.equalsIgnoreCase( "org.mozilla.javascript.Undefined" ) ) {
                // Is it a java Value class ?
                try {
                  Value v = (Value) Context.jsToJava( result, Value.class );
                  res.setValue( v.getString() );
                } catch ( Exception ev ) {
                  // A String perhaps?
                  String s = (String) Context.jsToJava( result, String.class );
                  res.setValue( s );
                }
              } else {
                res.setValue( ( (String) result ) );
              }
              break;
            case ValueMetaInterface.TYPE_DATE:
              double dbl = 0;
              if ( classname.equalsIgnoreCase( "org.mozilla.javascript.Undefined" ) ) {
                res.setNull();
              } else {
                if ( classname.equalsIgnoreCase( "org.mozilla.javascript.NativeDate" ) ) {
                  dbl = Context.toNumber( result );
                } else if ( classname.equalsIgnoreCase( "org.mozilla.javascript.NativeJavaObject" ) ) {
                  // Is it a java Date() class ?
                  try {
                    Date dat = (Date) Context.jsToJava( result, java.util.Date.class );
                    dbl = dat.getTime();
                  } catch ( Exception e ) { // Nope, try a Value

                    Value v = (Value) Context.jsToJava( result, Value.class );
                    Date dat = v.getDate();
                    if ( dat != null ) {
                      dbl = dat.getTime();
                    } else {
                      res.setNull();
                    }
                  }
                } else { // Finally, try a number conversion to time

                  dbl = ( (Double) result ).doubleValue();
                }
                long lng = Math.round( dbl );
                Date dat = new Date( lng );
                res.setValue( dat );
              }
              break;
            case ValueMetaInterface.TYPE_BOOLEAN:
              res.setValue( ( (Boolean) result ).booleanValue() );
              break;
            default:
              res.setNull();
          }
        } else {
          res.setNull();
        }
      } catch ( Exception e ) {
        message.append( BaseMessages.getString(
          PKG, "ScriptValuesMetaMod.CheckResult.ErrorRetrievingValue", fieldname[i] )
          + " : " + e.toString() );
        error_found = true;
      }
      res.setLength( length[i], precision[i] );

      message.append( BaseMessages.getString(
        PKG, "ScriptValuesMetaMod.CheckResult.RetrievedValue", fieldname[i], res.toStringMeta() ) );
    } else {
      message.append( BaseMessages.getString( PKG, "ScriptValuesMetaMod.CheckResult.ValueIsEmpty", String
        .valueOf( i ) ) );
      error_found = true;
    }

    return error_found;
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new ScriptValuesMod( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new ScriptValuesModData();
  }

  // This is for Additional Classloading
  public void parseXmlForAdditionalClasses() throws KettleException {
    try {
      Properties sysprops = System.getProperties();
      String strActPath = sysprops.getProperty( "user.dir" );
      Document dom = XMLHandler.loadXMLFile( strActPath + "/plugins/steps/ScriptValues_mod/plugin.xml" );
      Node stepnode = dom.getDocumentElement();
      Node libraries = XMLHandler.getSubNode( stepnode, "js_libraries" );
      int nbOfLibs = XMLHandler.countNodes( libraries, "js_lib" );
      additionalClasses = new ScriptValuesAddClasses[nbOfLibs];
      for ( int i = 0; i < nbOfLibs; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( libraries, "js_lib", i );
        String strJarName = XMLHandler.getTagAttribute( fnode, "name" );
        String strClassName = XMLHandler.getTagAttribute( fnode, "classname" );
        String strJSName = XMLHandler.getTagAttribute( fnode, "js_name" );

        Class<?> addClass =
          LoadAdditionalClass( strActPath + "/plugins/steps/ScriptValues_mod/" + strJarName, strClassName );
        Object addObject = addClass.newInstance();
        additionalClasses[i] = new ScriptValuesAddClasses( addClass, addObject, strJSName );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ScriptValuesMetaMod.Exception.UnableToParseXMLforAdditionalClasses" ), e );
    }
  }

  private static Class<?> LoadAdditionalClass( String strJar, String strClassName ) throws KettleException {
    try {
      Thread t = Thread.currentThread();
      ClassLoader cl = t.getContextClassLoader();
      URL u = new URL( "jar:file:" + strJar + "!/" );
      // We never know what else the script wants to load with the class loader, so lets not close it just like that.
      @SuppressWarnings( "resource" )
      KettleURLClassLoader kl = new KettleURLClassLoader( new URL[] { u }, cl );
      Class<?> toRun = kl.loadClass( strClassName );
      return toRun;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ScriptValuesMetaMod.Exception.UnableToLoadAdditionalClass" ), e );
    }
  }

  public ScriptValuesAddClasses[] getAddClasses() {
    return additionalClasses;
  }

  /**
   * @return the compatible
   */
  public boolean isCompatible() {
    return compatible;
  }

  /**
   * @param compatible
   *          the compatible to set
   */
  public void setCompatible( boolean compatible ) {
    this.compatible = compatible;
  }

  public boolean supportsErrorHandling() {
    return true;
  }

  public String getDialogClassName() {
    return "org.pentaho.di.ui.trans.steps.scriptvalues_mod.ScriptValuesModDialog";
  }

  /**
   * @return the replace
   */
  public boolean[] getReplace() {
    return replace;
  }

  /**
   * @param replace
   *          the replace to set
   */
  public void setReplace( boolean[] replace ) {
    this.replace = replace;
  }

  public void setOptimizationLevel( String optimizationLevel ) {
    this.optimizationLevel = optimizationLevel;
  }

  public String getOptimizationLevel() {
    return this.optimizationLevel;
  }
}
