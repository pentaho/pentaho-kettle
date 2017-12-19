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

package org.pentaho.di.trans.steps.constant;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Generates a number of (empty or the same) rows
 *
 * @author Matt
 * @since 4-apr-2003
 */
public class Constant extends BaseStep implements StepInterface {
  private static Class<?> PKG = ConstantMeta.class; // for i18n purposes, needed by Translator2!!

  private ConstantMeta meta;
  private ConstantData data;

  public Constant( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    meta = (ConstantMeta) getStepMeta().getStepMetaInterface();
    data = (ConstantData) stepDataInterface;
  }

  public static final RowMetaAndData buildRow( ConstantMeta meta, ConstantData data,
    List<CheckResultInterface> remarks ) {
    RowMetaInterface rowMeta = new RowMeta();
    Object[] rowData = new Object[meta.getFieldName().length];

    for ( int i = 0; i < meta.getFieldName().length; i++ ) {
      int valtype = ValueMetaFactory.getIdForValueMeta( meta.getFieldType()[i] );
      if ( meta.getFieldName()[i] != null ) {
        ValueMetaInterface value = null;
        try {
          value = ValueMetaFactory.createValueMeta( meta.getFieldName()[i], valtype );
        } catch ( Exception exception ) {
          remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, exception.getMessage(), null ) );
          continue;
        }
        value.setLength( meta.getFieldLength()[i] );
        value.setPrecision( meta.getFieldPrecision()[i] );

        if ( meta.isSetEmptyString()[i] ) {
          // Just set empty string
          rowData[i] = StringUtil.EMPTY_STRING;
        } else {

          String stringValue = meta.getValue()[i];

          // If the value is empty: consider it to be NULL.
          if ( stringValue == null || stringValue.length() == 0 ) {
            rowData[i] = null;

            if ( value.getType() == ValueMetaInterface.TYPE_NONE ) {
              String message =
                BaseMessages.getString(
                  PKG, "Constant.CheckResult.SpecifyTypeError", value.getName(), stringValue );
              remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
            }
          } else {
            switch ( value.getType() ) {
              case ValueMetaInterface.TYPE_NUMBER:
                try {
                  if ( meta.getFieldFormat()[i] != null
                    || meta.getDecimal()[i] != null || meta.getGroup()[i] != null
                    || meta.getCurrency()[i] != null ) {
                    if ( meta.getFieldFormat()[i] != null && meta.getFieldFormat()[i].length() >= 1 ) {
                      data.df.applyPattern( meta.getFieldFormat()[i] );
                    }
                    if ( meta.getDecimal()[i] != null && meta.getDecimal()[i].length() >= 1 ) {
                      data.dfs.setDecimalSeparator( meta.getDecimal()[i].charAt( 0 ) );
                    }
                    if ( meta.getGroup()[i] != null && meta.getGroup()[i].length() >= 1 ) {
                      data.dfs.setGroupingSeparator( meta.getGroup()[i].charAt( 0 ) );
                    }
                    if ( meta.getCurrency()[i] != null && meta.getCurrency()[i].length() >= 1 ) {
                      data.dfs.setCurrencySymbol( meta.getCurrency()[i] );
                    }

                    data.df.setDecimalFormatSymbols( data.dfs );
                  }

                  rowData[i] = new Double( data.nf.parse( stringValue ).doubleValue() );
                } catch ( Exception e ) {
                  String message =
                    BaseMessages.getString(
                      PKG, "Constant.BuildRow.Error.Parsing.Number", value.getName(), stringValue, e
                        .toString() );
                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
                }
                break;

              case ValueMetaInterface.TYPE_STRING:
                rowData[i] = stringValue;
                break;

              case ValueMetaInterface.TYPE_DATE:
                try {
                  if ( meta.getFieldFormat()[i] != null ) {
                    data.daf.applyPattern( meta.getFieldFormat()[i] );
                    data.daf.setDateFormatSymbols( data.dafs );
                  }

                  rowData[i] = data.daf.parse( stringValue );
                } catch ( Exception e ) {
                  String message =
                    BaseMessages.getString(
                      PKG, "Constant.BuildRow.Error.Parsing.Date", value.getName(), stringValue, e.toString() );
                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
                }
                break;

              case ValueMetaInterface.TYPE_INTEGER:
                try {
                  rowData[i] = new Long( Long.parseLong( stringValue ) );
                } catch ( Exception e ) {
                  String message =
                    BaseMessages.getString(
                      PKG, "Constant.BuildRow.Error.Parsing.Integer", value.getName(), stringValue, e
                        .toString() );
                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
                }
                break;

              case ValueMetaInterface.TYPE_BIGNUMBER:
                try {
                  rowData[i] = new BigDecimal( stringValue );
                } catch ( Exception e ) {
                  String message =
                    BaseMessages.getString(
                      PKG, "Constant.BuildRow.Error.Parsing.BigNumber", value.getName(), stringValue, e
                        .toString() );
                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
                }
                break;

              case ValueMetaInterface.TYPE_BOOLEAN:
                rowData[i] =
                  Boolean
                    .valueOf( "Y".equalsIgnoreCase( stringValue ) || "TRUE".equalsIgnoreCase( stringValue ) );
                break;

              case ValueMetaInterface.TYPE_BINARY:
                rowData[i] = stringValue.getBytes();
                break;

              case ValueMetaInterface.TYPE_TIMESTAMP:
                try {
                  rowData[i] = Timestamp.valueOf( stringValue );
                } catch ( Exception e ) {
                  String message =
                    BaseMessages.getString(
                      PKG, "Constant.BuildRow.Error.Parsing.Timestamp", value.getName(), stringValue, e
                        .toString() );
                  remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
                }
                break;

              default:
                String message =
                  BaseMessages.getString(
                    PKG, "Constant.CheckResult.SpecifyTypeError", value.getName(), stringValue );
                remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, message, null ) );
            }
          }
        }
        // Now add value to the row!
        // This is in fact a copy from the fields row, but now with data.
        rowMeta.addValueMeta( value );

      } // end if
    } // end for

    return new RowMetaAndData( rowMeta, rowData );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] r = null;
    r = getRow();

    if ( r == null ) { // no more rows to be expected from the previous step(s)
      setOutputDone();
      return false;
    }

    if ( data.firstRow ) {
      // The output meta is the original input meta + the
      // additional constant fields.

      data.firstRow = false;
      data.outputMeta = getInputRowMeta().clone();

      RowMetaInterface constants = data.constants.getRowMeta();
      data.outputMeta.mergeRowMeta( constants );
    }

    // Add the constant data to the end of the row.
    r = RowDataUtil.addRowData( r, getInputRowMeta().size(), data.constants.getData() );

    putRow( data.outputMeta, r );

    if ( log.isRowLevel() ) {
      logRowlevel( BaseMessages.getString(
        PKG, "Constant.Log.Wrote.Row", Long.toString( getLinesWritten() ), getInputRowMeta().getString( r ) ) );
    }

    if ( checkFeedback( getLinesWritten() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "Constant.Log.LineNr", Long.toString( getLinesWritten() ) ) );
      }
    }

    return true;
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ConstantMeta) smi;
    data = (ConstantData) sdi;

    data.firstRow = true;

    if ( super.init( smi, sdi ) ) {
      // Create a row (constants) with all the values in it...
      List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>(); // stores the errors...
      data.constants = buildRow( meta, data, remarks );
      if ( remarks.isEmpty() ) {
        return true;
      } else {
        for ( int i = 0; i < remarks.size(); i++ ) {
          CheckResultInterface cr = remarks.get( i );
          logError( cr.getText() );
        }
      }
    }
    return false;
  }

}
