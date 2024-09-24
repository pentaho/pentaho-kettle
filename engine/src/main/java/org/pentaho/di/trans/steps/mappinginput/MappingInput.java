/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.mappinginput;

import java.util.List;

import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;

/**
 * Do nothing. Pass all input data to the next steps.
 *
 * @author Matt
 * @since 2-jun-2003
 */
public class MappingInput extends BaseStep implements StepInterface {

  private static Class<?> PKG = MappingInputMeta.class; // for i18n purposes, needed by Translator2!!
  private int timeOut = 60000;
  private MappingInputMeta meta;

  private MappingInputData data;

  public MappingInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public void setTimeOut( int timeOut ) {
    this.timeOut = timeOut;
  }

  // ProcessRow is not doing anything
  // It's a place holder for accepting rows from the parent transformation...
  // So, basically, this is a glorified Dummy with a little bit of meta-data
  //
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (MappingInputMeta) smi;
    data = (MappingInputData) sdi;

    if ( !data.linked ) {
      //
      // Wait until we know were to read from the parent transformation...
      // However, don't wait forever, if we don't have a connection after 60 seconds: bail out!
      //
      int totalsleep = 0;
      while ( !isStopped() && data.sourceSteps == null ) {
        try {
          totalsleep += 10;
          Thread.sleep( 10 );
        } catch ( InterruptedException e ) {
          stopAll();
        }
        if ( totalsleep > timeOut ) {
          throw new KettleException( BaseMessages.getString( PKG,
              "MappingInput.Exception.UnableToConnectWithParentMapping", "" + ( totalsleep / 1000 ) ) );
        }
      }

      // OK, now we're ready to read from the parent source steps.
      data.linked = true;
    }

    Object[] row = getRow();
    if ( row == null ) {
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      // The Input RowMetadata is not the same as the output row meta-data.
      // The difference is described in the data interface
      //
      // String[] data.sourceFieldname
      // String[] data.targetFieldname
      //
      // --> getInputRowMeta() is not corresponding to what we're outputting.
      // In essence, we need to rename a couple of fields...
      //
      data.outputRowMeta = getInputRowMeta().clone();

      // Now change the field names according to the mapping specification...
      // That means that all fields go through unchanged, unless specified.
      //
      for ( MappingValueRename valueRename : data.valueRenames ) {
        ValueMetaInterface valueMeta = data.outputRowMeta.searchValueMeta( valueRename.getSourceValueName() );
        if ( valueMeta == null ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "MappingInput.Exception.UnableToFindMappedValue",
              valueRename.getSourceValueName() ) );
        }
        valueMeta.setName( valueRename.getTargetValueName() );

        valueMeta = getInputRowMeta().searchValueMeta( valueRename.getSourceValueName() );
        if ( valueMeta == null ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "MappingInput.Exception.UnableToFindMappedValue",
              valueRename.getSourceValueName() ) );
        }
        valueMeta.setName( valueRename.getTargetValueName() );
      }

      // This is typical side effect of ESR-4178
      data.outputRowMeta.setValueMetaList( data.outputRowMeta.getValueMetaList() );
      this.getInputRowMeta().setValueMetaList( this.getInputRowMeta().getValueMetaList() );

      // The input row meta has been manipulated correctly for the call to meta.getFields(), so create a blank
      // outputRowMeta
      meta.setInputRowMeta( getInputRowMeta() );
      if ( meta.isSelectingAndSortingUnspecifiedFields() ) {
        data.outputRowMeta = new RowMeta();
      } else {
        meta.setInputRowMeta( new RowMeta() );
      }

      // Fill the output row meta with the processed fields
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      if ( meta.isSelectingAndSortingUnspecifiedFields() ) {
        //
        // Create a list of the indexes to get the right order or fields on the output.
        //
        data.fieldNrs = new int[data.outputRowMeta.size()];
        for ( int i = 0; i < data.outputRowMeta.size(); i++ ) {
          data.fieldNrs[i] = getInputRowMeta().indexOfValue( data.outputRowMeta.getValueMeta( i ).getName() );
        }
      }
    }

    // Fill and send the output row
    if ( meta.isSelectingAndSortingUnspecifiedFields() ) {
      Object[] outputRowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
      for ( int i = 0; i < data.fieldNrs.length; i++ ) {
        outputRowData[i] = row[data.fieldNrs[i]];
      }
      putRow( data.outputRowMeta, outputRowData );
    } else {
      putRow( data.outputRowMeta, row );
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MappingInputMeta) smi;
    data = (MappingInputData) sdi;

    return super.init( smi, sdi );
  }

  public void setConnectorSteps( StepInterface[] sourceSteps, List<MappingValueRename> valueRenames,
      String mappingStepname ) {

    if ( sourceSteps == null ) {
      throw new IllegalArgumentException( BaseMessages
          .getString( PKG, "MappingInput.Exception.IllegalArgumentSourceStep" ) );
    }

    if ( valueRenames == null ) {
      throw new IllegalArgumentException( BaseMessages
          .getString( PKG, "MappingInput.Exception.IllegalArgumentValueRename" ) );
    }

    if ( sourceSteps.length != 0 ) {
      if ( mappingStepname == null ) {
        throw new IllegalArgumentException( BaseMessages
          .getString( PKG, "MappingInput.Exception.IllegalArgumentStepName" ) );
      }
    }

    for ( StepInterface sourceStep : sourceSteps ) {

      // We don't want to add the mapping-to-mapping rowset
      //
      if ( !sourceStep.isMapping() ) {
        // OK, before we leave, make sure there is a rowset that covers the path to this target step.
        // We need to create a new RowSet and add it to the Input RowSets of the target step
        //
        BlockingRowSet rowSet = new BlockingRowSet( getTransMeta().getSizeRowset() );

        // This is always a single copy, both for source and target...
        //
        rowSet.setThreadNameFromToCopy( sourceStep.getStepname(), 0, mappingStepname, 0 );

        // Make sure to connect it to both sides...
        //
        sourceStep.addRowSetToOutputRowSets( rowSet );
        sourceStep.identifyErrorOutput();
        addRowSetToInputRowSets( rowSet );
      }
    }
    data.valueRenames = valueRenames;

    data.sourceSteps = sourceSteps;
  }
}
