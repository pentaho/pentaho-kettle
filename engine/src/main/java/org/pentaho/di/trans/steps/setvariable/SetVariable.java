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

package org.pentaho.di.trans.steps.setvariable;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Convert Values in a certain fields to other values
 *
 * @author Matt
 * @since 27-apr-2006
 */
public class SetVariable extends BaseStep implements StepInterface {
  private static Class<?> PKG = SetVariableMeta.class; // for i18n purposes, needed by Translator2!!

  private SetVariableMeta meta;
  private SetVariableData data;

  public SetVariable( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (SetVariableMeta) smi;
    data = (SetVariableData) sdi;

    // Get one row from one of the rowsets...
    //
    Object[] rowData = getRow();
    if ( rowData == null ) { // means: no more input to be expected...

      if ( first ) {
        // We do not received any row !!
        logBasic( BaseMessages.getString( PKG, "SetVariable.Log.NoInputRowSetDefault" ) );
        for ( int i = 0; i < meta.getFieldName().length; i++ ) {
          if ( !Utils.isEmpty( meta.getDefaultValue()[i] ) ) {
            setValue( rowData, i, true );
          }
        }
      }

      logBasic( "Finished after " + getLinesWritten() + " rows." );
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      data.outputMeta = getInputRowMeta().clone();

      logBasic( BaseMessages.getString( PKG, "SetVariable.Log.SettingVar" ) );

      for ( int i = 0; i < meta.getFieldName().length; i++ ) {
        setValue( rowData, i, false );
      }

      putRow( data.outputMeta, rowData );
      return true;
    }

    throw new KettleStepException( BaseMessages.getString(
      PKG, "SetVariable.RuntimeError.MoreThanOneRowReceived.SETVARIABLE0007" ) );
  }

  private void setValue( Object[] rowData, int i, boolean usedefault ) throws KettleException {
    // Set the appropriate environment variable
    //
    String value = null;
    if ( usedefault ) {
      value = environmentSubstitute( meta.getDefaultValue()[i] );
    } else {
      int index = data.outputMeta.indexOfValue( meta.getFieldName()[i] );
      if ( index < 0 ) {
        throw new KettleException( "Unable to find field [" + meta.getFieldName()[i] + "] in input row" );
      }
      ValueMetaInterface valueMeta = data.outputMeta.getValueMeta( index );
      Object valueData = rowData[index];

      // Get variable value
      //
      if ( meta.isUsingFormatting() ) {
        value = valueMeta.getString( valueData );
      } else {
        value = valueMeta.getCompatibleString( valueData );
      }

    }

    if ( value == null ) {
      value = "";
    }

    // Get variable name
    String varname = meta.getVariableName()[i];

    if ( Utils.isEmpty( varname ) ) {
      if ( Utils.isEmpty( value ) ) {
        throw new KettleException( "Variable name nor value was specified on line #" + ( i + 1 ) );
      } else {
        throw new KettleException( "There was no variable name specified for value [" + value + "]" );
      }
    }

    Job parentJob = null;

    // We always set the variable in this step and in the parent transformation...
    //
    setVariable( varname, value );

    // Set variable in the transformation
    //
    Trans trans = getTrans();
    trans.setVariable( varname, value );

    // Make a link between the transformation and the parent transformation (in a sub-transformation)
    //
    while ( trans.getParentTrans() != null ) {
      trans = trans.getParentTrans();
      trans.setVariable( varname, value );
    }

    // The trans object we have now is the trans being executed by a job.
    // It has one or more parent jobs.
    // Below we see where we need to this value as well...
    //
    switch ( meta.getVariableType()[i] ) {
      case SetVariableMeta.VARIABLE_TYPE_JVM:

        System.setProperty( varname, value );

        parentJob = trans.getParentJob();
        while ( parentJob != null ) {
          parentJob.setVariable( varname, value );
          parentJob = parentJob.getParentJob();
        }

        break;
      case SetVariableMeta.VARIABLE_TYPE_ROOT_JOB:
        // Comments by SB
        // VariableSpace rootJob = null;
        parentJob = trans.getParentJob();
        while ( parentJob != null ) {
          parentJob.setVariable( varname, value );
          // rootJob = parentJob;
          parentJob = parentJob.getParentJob();
        }
        break;

      case SetVariableMeta.VARIABLE_TYPE_GRAND_PARENT_JOB:
        // Set the variable in the parent job
        //
        parentJob = trans.getParentJob();
        if ( parentJob != null ) {
          parentJob.setVariable( varname, value );
        } else {
          throw new KettleStepException( "Can't set variable ["
            + varname + "] on parent job: the parent job is not available" );
        }

        // Set the variable on the grand-parent job
        //
        VariableSpace gpJob = trans.getParentJob().getParentJob();
        if ( gpJob != null ) {
          gpJob.setVariable( varname, value );
        } else {
          throw new KettleStepException( "Can't set variable ["
            + varname + "] on grand parent job: the grand parent job is not available" );
        }
        break;

      case SetVariableMeta.VARIABLE_TYPE_PARENT_JOB:
        // Set the variable in the parent job
        //
        parentJob = trans.getParentJob();
        if ( parentJob != null ) {
          parentJob.setVariable( varname, value );
        } else {
          throw new KettleStepException( "Can't set variable ["
            + varname + "] on parent job: the parent job is not available" );
        }
        break;

      default:
        break;
    }

    logBasic( BaseMessages.getString( PKG, "SetVariable.Log.SetVariableToValue", meta.getVariableName()[i], value ) );
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SetVariableMeta) smi;
    data = (SetVariableData) sdi;

    super.dispose( smi, sdi );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SetVariableMeta) smi;
    data = (SetVariableData) sdi;

    if ( super.init( smi, sdi ) ) {
      return true;
    }
    return false;
  }

}
