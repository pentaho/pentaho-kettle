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

package org.pentaho.di.trans.steps.calculator;

import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.Utils;
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
import org.w3c.dom.Node;

/**
 * Contains the meta-data for the Calculator step: calculates predefined formula's
 *
 * @since 08 september 2005
 */
public class CalculatorMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = CalculatorMeta.class; // for i18n purposes, needed by Translator2!!

  /** The calculations to be performed */
  private CalculatorMetaFunction[] calculation;

  public CalculatorMetaFunction[] getCalculation() {
    return calculation;
  }

  public void setCalculation( CalculatorMetaFunction[] calcTypes ) {
    this.calculation = calcTypes;
  }

  public void allocate( int nrCalcs ) {
    calculation = new CalculatorMetaFunction[nrCalcs];
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    int nrCalcs = XMLHandler.countNodes( stepnode, CalculatorMetaFunction.XML_TAG );
    allocate( nrCalcs );
    for ( int i = 0; i < nrCalcs; i++ ) {
      Node calcnode = XMLHandler.getSubNodeByNr( stepnode, CalculatorMetaFunction.XML_TAG, i );
      calculation[i] = new CalculatorMetaFunction( calcnode );
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    if ( calculation != null ) {
      for ( CalculatorMetaFunction aCalculation : calculation ) {
        retval.append( aCalculation.getXML() );
      }
    }

    return retval.toString();
  }

  @Override
  public boolean equals( Object obj ) {
    if ( obj != null && ( obj.getClass().equals( this.getClass() ) ) ) {
      CalculatorMeta m = (CalculatorMeta) obj;
      return ( getXML().equals( m.getXML() ) );
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode( calculation );
  }

  @Override
  public Object clone() {
    CalculatorMeta retval = (CalculatorMeta) super.clone();
    if ( calculation != null ) {
      retval.allocate( calculation.length );
      for ( int i = 0; i < calculation.length; i++ ) {
        ( retval.getCalculation() )[i] = (CalculatorMetaFunction) calculation[i].clone();
      }
    } else {
      retval.allocate( 0 );
    }
    return retval;
  }

  @Override
  public void setDefault() {
    calculation = new CalculatorMetaFunction[0];
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    int nrCalcs = rep.countNrStepAttributes( id_step, "field_name" );
    allocate( nrCalcs );
    for ( int i = 0; i < nrCalcs; i++ ) {
      calculation[i] = new CalculatorMetaFunction( rep, id_step, i );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    for ( int i = 0; i < calculation.length; i++ ) {
      calculation[i].saveRep( rep, metaStore, id_transformation, id_step, i );
    }
  }

  @Override
  public void getFields( RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    for ( CalculatorMetaFunction fn : calculation ) {
      if ( !fn.isRemovedFromResult() ) {
        if ( !Utils.isEmpty( fn.getFieldName() ) ) { // It's a new field!
          ValueMetaInterface v = getValueMeta( fn, origin );
          row.addValueMeta( v );
        }
      }
    }
  }

  private ValueMetaInterface getValueMeta( CalculatorMetaFunction fn, String origin ) {
    ValueMetaInterface v;
    // What if the user didn't specify a data type?
    // In that case we look for the default data type
    //
    int defaultResultType = fn.getValueType();
    if ( defaultResultType == ValueMetaInterface.TYPE_NONE ) {
      defaultResultType = CalculatorMetaFunction.getCalcFunctionDefaultResultType( fn.getCalcType() );
    }
    try {
      v = ValueMetaFactory.createValueMeta( fn.getFieldName(), defaultResultType );
    } catch ( Exception ex ) {
      return null;
    }
    v.setLength( fn.getValueLength() );
    v.setPrecision( fn.getValuePrecision() );
    v.setOrigin( origin );
    v.setComments( fn.getCalcTypeDesc() );
    v.setConversionMask( fn.getConversionMask() );
    v.setDecimalSymbol( fn.getDecimalSymbol() );
    v.setGroupingSymbol( fn.getGroupingSymbol() );
    v.setCurrencySymbol( fn.getCurrencySymbol() );

    return v;
  }

  public RowMetaInterface getAllFields( RowMetaInterface inputRowMeta ) {
    RowMetaInterface rowMeta = inputRowMeta.clone();

    for ( CalculatorMetaFunction fn : getCalculation() ) {
      if ( !Utils.isEmpty( fn.getFieldName() ) ) { // It's a new field!
        ValueMetaInterface v = getValueMeta( fn, null );
        rowMeta.addValueMeta( v );
      }
    }
    return rowMeta;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "CalculatorMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );

      if ( prev == null || prev.size() == 0 ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
            PKG, "CalculatorMeta.CheckResult.ExpectedInputError" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "CalculatorMeta.CheckResult.FieldsReceived", "" + prev.size() ), stepMeta );
        remarks.add( cr );
      }
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "CalculatorMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new Calculator( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new CalculatorData();
  }
}
