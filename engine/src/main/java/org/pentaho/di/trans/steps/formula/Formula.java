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

package org.pentaho.di.trans.steps.formula;

import java.math.BigDecimal;
import java.util.Date;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.reporting.libraries.formula.LibFormulaErrorValue;
import org.pentaho.reporting.libraries.formula.parser.FormulaParser;

/**
 * Calculate new field values using pre-defined functions.
 *
 * @author Matt
 * @since 8-sep-2005
 */
public class Formula extends BaseStep implements StepInterface {
  private FormulaMeta meta;
  private FormulaData data;

  public Formula( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (FormulaMeta) smi;
    data = (FormulaData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Create the context
      data.context = new RowForumulaContext( data.outputRowMeta );
      data.parser = new FormulaParser();

      // Calculate replace indexes...
      //
      data.replaceIndex = new int[meta.getFormula().length];
      for ( int i = 0; i < meta.getFormula().length; i++ ) {
        FormulaMetaFunction fn = meta.getFormula()[i];
        if ( !Utils.isEmpty( fn.getReplaceField() ) ) {
          data.replaceIndex[i] = getInputRowMeta().indexOfValue( fn.getReplaceField() );
          if ( data.replaceIndex[i] < 0 ) {
            throw new KettleException( "Unknown field specified to replace with a formula result: ["
              + fn.getReplaceField() + "]" );
          }
        } else {
          data.replaceIndex[i] = -1;
        }
      }
    }

    if ( log.isRowLevel() ) {
      logRowlevel( "Read row #" + getLinesRead() + " : " + r );
    }

    Object[] outputRowData = calcFields( getInputRowMeta(), r );
    putRow( data.outputRowMeta, outputRowData ); // copy row to possible alternate rowset(s).

    if ( log.isRowLevel() ) {
      logRowlevel( "Wrote row #" + getLinesWritten() + " : " + r );
    }
    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( "Linenr " + getLinesRead() );
    }

    return true;
  }

  private Object[] calcFields( RowMetaInterface rowMeta, Object[] r ) throws KettleValueException {
    try {
      Object[] outputRowData = RowDataUtil.createResizedCopy( r, data.outputRowMeta.size() );
      int tempIndex = rowMeta.size();

      // Assign this tempRowData to the formula context
      //
      data.context.setRowData( outputRowData );

      // Initialize parsers etc. Only do it once.
      //
      if ( data.formulas == null ) {
        // Create a set of LValues to put the parsed results in...
        data.formulas = new org.pentaho.reporting.libraries.formula.Formula[meta.getFormula().length];
        for ( int i = 0; i < meta.getFormula().length; i++ ) {
          FormulaMetaFunction fn = meta.getFormula()[i];
          if ( !Utils.isEmpty( fn.getFieldName() ) ) {
            data.formulas[i] = data.createFormula( meta.getFormula()[i].getFormula() );
          } else {
            throw new KettleException( "Unable to find field name for formula ["
              + Const.NVL( fn.getFormula(), "" ) + "]" );
          }
        }
      }

      for ( int i = 0; i < meta.getFormula().length; i++ ) {
        FormulaMetaFunction fn = meta.getFormula()[i];
        if ( !Utils.isEmpty( fn.getFieldName() ) ) {
          if ( data.formulas[i] == null ) {
            data.formulas[i] = data.createFormula( meta.getFormula()[i].getFormula() );
          }

          // this is main part of all this step: calculate formula
          Object formulaResult = data.formulas[i].evaluate();
          if ( formulaResult instanceof LibFormulaErrorValue ) {
            // inspect why it is happens to get clear error message.
            throw new KettleException( "Error calculate formula. Formula "
                + fn.getFormula() + " output field: " + fn.getFieldName() + ", error is: " + formulaResult.toString() );
          }

          // Calculate the return type on the first row...
          // for most cases we can try to convert data on a fly.
          if ( data.returnType[i] < 0 ) {
            if ( formulaResult instanceof String ) {
              data.returnType[i] = FormulaData.RETURN_TYPE_STRING;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_STRING );
            } else if ( formulaResult instanceof Integer ) {
              data.returnType[i] = FormulaData.RETURN_TYPE_INTEGER;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_INTEGER );
            } else if ( formulaResult instanceof Long ) {
              data.returnType[i] = FormulaData.RETURN_TYPE_LONG;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_INTEGER );
            } else if ( formulaResult instanceof Date ) {
              data.returnType[i] = FormulaData.RETURN_TYPE_DATE;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_DATE );
            } else if ( formulaResult instanceof BigDecimal ) {
              data.returnType[i] = FormulaData.RETURN_TYPE_BIGDECIMAL;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_BIGNUMBER );
            } else if ( formulaResult instanceof Number ) {
              data.returnType[i] = FormulaData.RETURN_TYPE_NUMBER;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_NUMBER );
            // this types we will not make attempt to auto-convert
            } else if ( formulaResult instanceof byte[] ) {
              data.returnType[i] = FormulaData.RETURN_TYPE_BYTE_ARRAY;
              if ( fn.getValueType() != ValueMetaInterface.TYPE_BINARY ) {
                throw new KettleValueException( "Please specify a Binary type for field ["
                  + fn.getFieldName() + "] as a result of formula [" + fn.getFormula() + "]" );
              }
            } else if ( formulaResult instanceof Boolean ) {
              data.returnType[i] = FormulaData.RETURN_TYPE_BOOLEAN;
              if ( fn.getValueType() != ValueMetaInterface.TYPE_BOOLEAN ) {
                throw new KettleValueException( "Please specify a Boolean type for field ["
                  + fn.getFieldName() + "] as a result of formula [" + fn.getFormula() + "]" );
              }
            } else {
              data.returnType[i] = FormulaData.RETURN_TYPE_STRING;
              fn.setNeedDataConversion( fn.getValueType() != ValueMetaInterface.TYPE_STRING );
            }
          }

          int realIndex = ( data.replaceIndex[i] < 0 ) ? tempIndex++ : data.replaceIndex[i];
          outputRowData[realIndex] = getReturnValue( formulaResult, data.returnType[i], realIndex, fn );
        }
      }

      return outputRowData;
    } catch ( Throwable e ) {
      throw new KettleValueException( e );
    }
  }

  protected Object getReturnValue( Object formulaResult, int returnType, int realIndex, FormulaMetaFunction fn )
    throws KettleException {
    if ( formulaResult == null ) {
      return null;
    }
    Object value = null;
    switch ( returnType ) {
      case FormulaData.RETURN_TYPE_STRING:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = formulaResult.toString();
        }
        break;
      case FormulaData.RETURN_TYPE_NUMBER:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = new Double( ( (Number) formulaResult ).doubleValue() );
        }
        break;
      case FormulaData.RETURN_TYPE_INTEGER:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = new Long( ( (Integer) formulaResult ).intValue() );
        }
        break;
      case FormulaData.RETURN_TYPE_LONG:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = formulaResult;
        }
        break;
      case FormulaData.RETURN_TYPE_DATE:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = formulaResult;
        }
        break;
      case FormulaData.RETURN_TYPE_BIGDECIMAL:
        if ( fn.isNeedDataConversion() ) {
          value = convertDataToTargetValueMeta( realIndex, formulaResult );
        } else {
          value = formulaResult;
        }
        break;
      case FormulaData.RETURN_TYPE_BYTE_ARRAY:
        value = formulaResult;
        break;
      case FormulaData.RETURN_TYPE_BOOLEAN:
        value = formulaResult;
        break;
    } //if none case is caught - null is returned.
    return value;
  }

  private Object convertDataToTargetValueMeta( int i, Object formulaResult ) throws KettleException {
    if ( formulaResult == null ) {
      return formulaResult;
    }
    ValueMetaInterface target = data.outputRowMeta.getValueMeta( i );
    ValueMetaInterface actual = ValueMetaFactory.guessValueMetaInterface( formulaResult );
    Object value = target.convertData( actual, formulaResult );
    return value;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (FormulaMeta) smi;
    data = (FormulaData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.

      // Return data type discovery is expensive, let's discover them one time only.
      //
      data.returnType = new int[meta.getFormula().length];
      for ( int i = 0; i < meta.getFormula().length; i++ ) {
        data.returnType[i] = -1;
      }
      return true;
    }
    return false;
  }

}
