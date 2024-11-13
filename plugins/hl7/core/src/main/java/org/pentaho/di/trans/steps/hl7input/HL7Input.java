/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.hl7input;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.hl7input.common.HL7KettleParser;
import org.pentaho.di.trans.steps.hl7input.common.HL7Value;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

/**
 * Read HL7 Messages and output values
 * 
 */
public class HL7Input extends BaseStep implements StepInterface {
  private static Class<?> PKG = HL7InputMeta.class; // for i18n purposes, needed by Translator2!!

  private HL7InputMeta meta;
  private HL7InputData data;

  public HL7Input( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (HL7InputMeta) smi;
    data = (HL7InputData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) { // no more input to be expected...
      setOutputDone();
      return false;
    }

    if ( first ) {
      data.messageFieldIndex = getInputRowMeta().indexOfValue( meta.getMessageField() );
      if ( data.messageFieldIndex < 0 ) {
        throw new KettleException( "Unable to find field [" + meta.getMessageField() + "] in the input fields." );
      }

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      data.parser = new GenericParser();
      data.parser.setValidationContext( new NoValidation() );
    }

    String messageString = getInputRowMeta().getString( r, data.messageFieldIndex );

    try {
      Message message = data.parser.parse( messageString );
      List<HL7Value> values = HL7KettleParser.extractValues( message );

      for ( HL7Value value : values ) {
        Object[] output = RowDataUtil.createResizedCopy( r, data.outputRowMeta.size() );
        int outputIndex = getInputRowMeta().size();

        output[outputIndex++] = value.getParentGroup();
        output[outputIndex++] = value.getGroupName();
        output[outputIndex++] = value.getVersion();
        output[outputIndex++] = value.getStructureName();
        output[outputIndex++] = value.getStructureNumber();
        output[outputIndex++] = value.getFieldName();
        output[outputIndex++] = value.getCoordinate();
        output[outputIndex++] = value.getDataType();
        output[outputIndex++] = value.getDescription();
        output[outputIndex++] = value.getValue();

        putRow( data.outputRowMeta, output );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Error parsing message", e );
    }

    if ( checkFeedback( getLinesWritten() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "HL7Input.Log.LineNumber" ) + getLinesWritten() );
      }
    }

    return true;
  }
}
