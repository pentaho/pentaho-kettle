/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.di.trans.steps.hl7input;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
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

@Step( id = "HL7Input", image = "hl7-input.svg", i18nPackageName = "org.pentaho.di.trans.steps.hl7input",
    name = "HL7Input.Name", description = "HL7Input.TooltipDesc",
    documentationUrl = "http://wiki.pentaho.com/display/EAI/HL7+Input",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Input" )
public class HL7InputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = HL7InputMeta.class; // for i18n purposes, needed by Translator2!!

  private String messageField;

  public HL7InputMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  @Override
  public String getXML() throws KettleException {
    return XMLHandler.addTagValue( "message_field", messageField );
  }

  private void readData( Node stepnode ) {
    messageField = XMLHandler.getTagValue( stepnode, "message_field" );
  }

  @Override
  public void setDefault() {
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    messageField = rep.getStepAttributeString( id_step, "message_field" );
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    rep.saveStepAttribute( id_transformation, id_step, "message_field", messageField );
  }

  @Override
  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    ValueMetaInterface valueMeta = new ValueMetaString( "ParentGroup" );
    valueMeta.setOrigin( origin );
    rowMeta.addValueMeta( valueMeta );

    valueMeta = new ValueMetaString( "Group" );
    valueMeta.setOrigin( origin );
    rowMeta.addValueMeta( valueMeta );

    valueMeta = new ValueMetaString( "HL7Version" );
    valueMeta.setOrigin( origin );
    rowMeta.addValueMeta( valueMeta );

    valueMeta = new ValueMetaString( "StructureName" );
    valueMeta.setOrigin( origin );
    rowMeta.addValueMeta( valueMeta );

    valueMeta = new ValueMetaString( "StructureNumber" );
    valueMeta.setOrigin( origin );
    rowMeta.addValueMeta( valueMeta );

    valueMeta = new ValueMetaString( "FieldName" );
    valueMeta.setOrigin( origin );
    rowMeta.addValueMeta( valueMeta );

    valueMeta = new ValueMetaString( "Coordinates" );
    valueMeta.setOrigin( origin );
    rowMeta.addValueMeta( valueMeta );

    valueMeta = new ValueMetaString( "HL7DataType" );
    valueMeta.setOrigin( origin );
    rowMeta.addValueMeta( valueMeta );

    valueMeta = new ValueMetaString( "FieldDescription" );
    valueMeta.setOrigin( origin );
    rowMeta.addValueMeta( valueMeta );

    valueMeta = new ValueMetaString( "Value" );
    valueMeta.setOrigin( origin );
    rowMeta.addValueMeta( valueMeta );

  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString( PKG,
              "HL7InputMeta.CheckResult.NotReceivingFields" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "HL7InputMeta.CheckResult.StepRecevingData", prev.size() + "" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "HL7InputMeta.CheckResult.StepRecevingData2" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "HL7InputMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
      Trans trans ) {
    return new HL7Input( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new HL7InputData();
  }

  /**
   * @return the messageField
   */
  public String getMessageField() {
    return messageField;
  }

  /**
   * @param messageField
   *          the messageField to set
   */
  public void setMessageField( String messageField ) {
    this.messageField = messageField;
  }
}
