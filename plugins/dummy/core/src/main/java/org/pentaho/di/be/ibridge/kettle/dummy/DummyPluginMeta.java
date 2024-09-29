/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.be.ibridge.kettle.dummy;

import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.*;
import org.pentaho.di.core.annotations.*;
import org.pentaho.di.core.database.*;
import org.pentaho.di.core.exception.*;
import org.pentaho.di.core.row.*;
import org.pentaho.di.core.row.value.*;
import org.pentaho.di.core.variables.*;
import org.pentaho.di.core.xml.*;
import org.pentaho.di.repository.*;
import org.pentaho.di.trans.*;
import org.pentaho.di.trans.step.*;
import org.w3c.dom.*;

import java.util.List;
import java.util.*;

/*
 * Created on 02-jun-2003
 *
 */
// BACKLOG-38582 Remove specific deprecated steps and job entries from PDI, Please uncomment to enable plugin if needed.
//@Step( id = "DummyStep",
//      image = "ui/images/deprecated.svg",
//      i18nPackageName = "be.ibridge.kettle.dummy",
//      name = "DummyPlugin.Step.Name",
//      description = "DummyPlugin.Step.Description",
//      categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Deprecated",
//      suggestion = "DummyPlugin.Step.SuggestedStep" )
public class DummyPluginMeta extends BaseStepMeta implements StepMetaInterface {
  private ValueMetaAndData value;

  public DummyPluginMeta() {
    super(); // allocate BaseStepInfo
  }

  /**
   * @return Returns the value.
   */
  public ValueMetaAndData getValue() {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public void setValue( ValueMetaAndData value ) {
    this.value = value;
  }

  @Override
  public String getXML() throws KettleException {
    String retval = "";

    retval += "    <values>" + Const.CR;
    if ( value != null ) {
      retval += value.getXML();
    }
    retval += "      </values>" + Const.CR;

    return retval;
  }

  @Override
  public void getFields( RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space ) {
    if ( value != null ) {
      ValueMetaInterface v = value.getValueMeta();
      v.setOrigin( origin );

      r.addValueMeta( v );
    }
  }

  @Override
  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters ) throws KettleXMLException {
    try {
      value = new ValueMetaAndData();

      Node valnode = XMLHandler.getSubNode( stepnode, "values", "value" );
      if ( valnode != null ) {
        System.out.println( "reading value in " + valnode );
        value.loadXML( valnode );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to read step info from XML node", e );
    }
  }

  @Override
  public void setDefault() {
    value = new ValueMetaAndData( new ValueMetaNumber( "valuename" ), new Double( 123.456 ) );
    value.getValueMeta().setLength( 12 );
    value.getValueMeta().setPrecision( 4 );
  }

  @Override
  public void readRep( Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters ) throws KettleException {
    try {
      String name = rep.getStepAttributeString( id_step, 0, "value_name" );
      String typedesc = rep.getStepAttributeString( id_step, 0, "value_type" );
      String text = rep.getStepAttributeString( id_step, 0, "value_text" );
      boolean isnull = rep.getStepAttributeBoolean( id_step, 0, "value_null" );
      int length = (int) rep.getStepAttributeInteger( id_step, 0, "value_length" );
      int precision = (int) rep.getStepAttributeInteger( id_step, 0, "value_precision" );

      int type = ValueMetaFactory.getIdForValueMeta( typedesc );
      value = new ValueMetaAndData( new ValueMeta( name, type ), null );
      value.getValueMeta().setLength( length );
      value.getValueMeta().setPrecision( precision );

      if ( isnull ) {
        value.setValueData( null );
      } else {
        ValueMetaInterface stringMeta = new ValueMetaString( name );
        if ( type != ValueMetaInterface.TYPE_STRING ) {
          text = Const.trim( text );
        }
        value.setValueData( value.getValueMeta().convertData( stringMeta, text ) );
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "error reading step with id_step=" + id_step + " from the repository", dbe );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step with id_step=" + id_step + " from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "value_name", value.getValueMeta().getName() );
      rep.saveStepAttribute( id_transformation, id_step, 0, "value_type", value.getValueMeta().getTypeDesc() );
      rep.saveStepAttribute( id_transformation, id_step, 0, "value_text", value.getValueMeta().getString( value.getValueData() ) );
      rep.saveStepAttribute( id_transformation, id_step, 0, "value_null", value.getValueMeta().isNull( value.getValueData() ) );
      rep.saveStepAttribute( id_transformation, id_step, 0, "value_length", value.getValueMeta().getLength() );
      rep.saveStepAttribute( id_transformation, id_step, 0, "value_precision", value.getValueMeta().getPrecision() );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save step information to the repository, id_step=" + id_step, dbe );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepMeta );
      remarks.add( cr );
    } else {
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving " + prev.size() + " fields", stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta );
      remarks.add( cr );
    } else {
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta );
      remarks.add( cr );
    }
  }

  public StepDialogInterface getDialog( Shell shell, StepMetaInterface meta, TransMeta transMeta, String name ) {
    return new DummyPluginDialog( shell, meta, transMeta, name );
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp ) {
    return new DummyPlugin( stepMeta, stepDataInterface, cnr, transMeta, disp );
  }

  @Override
  public StepDataInterface getStepData() {
    return new DummyPluginData();
  }
}
