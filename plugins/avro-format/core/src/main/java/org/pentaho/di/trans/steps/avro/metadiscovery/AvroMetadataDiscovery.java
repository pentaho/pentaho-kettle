/*******************************************************************************
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2018 - 2023 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 ******************************************************************************/
package org.pentaho.di.trans.steps.avro.metadiscovery;

import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.apache.avro.Schema;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.avro.input.AvroInputField;
import org.pentaho.di.trans.steps.avro.input.AvroNestedFieldGetter;
import org.pentaho.di.trans.steps.avro.input.IAvroInputField;

import java.io.IOException;
import java.util.List;


public class AvroMetadataDiscovery extends BaseStep implements StepInterface {

  private static Class<?> PKG = AvroMetadataDiscovery.class;

  private AvroMetadataDiscoveryMeta meta;
  private AvroMetadataDiscoveryData data;

  public AvroMetadataDiscovery( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                                Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (AvroMetadataDiscoveryMeta) smi;
    data = (AvroMetadataDiscoveryData) sdi;
    return super.init( smi, sdi );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    Object[] row = getRow();

    if ( first ) {

      first = false;

      data.setOutputRowMeta( new RowMeta() );
      meta.getFields( data.outputRowMeta, getStepname(), null, null, getTransMeta(), getRepository(), getMetaStore() );

      data.avroSourceFormat = meta.getFormat();
      data.avroDataLocation = meta.getDataLocation();
      data.avroDataLocationType = meta.dataLocationType;
      data.avroSchemaLocation = meta.getSchemaLocation();
      data.avroSchemaLocationType = meta.schemaLocationType;

      data.avroSourceFormatIndex = data.outputRowMeta.indexOfValue( "Data format" );
      data.avroDataLocationIndex = data.outputRowMeta.indexOfValue( "Data location" );
      data.avroDataLocationTypeIndex = data.outputRowMeta.indexOfValue( String.valueOf( "Data location type" ) );
      data.avroSchemaLocationIndex = data.outputRowMeta.indexOfValue( "Schema location" );
      data.avroSchemaLocationTypeIndex = data.outputRowMeta.indexOfValue( String.valueOf( "Schema location type" ) );

      data.avroPathIndex = data.outputRowMeta.indexOfValue( meta.getAvroPathFieldName() );
      data.avroTypeIndex = data.outputRowMeta.indexOfValue( meta.getAvroTypeFieldName() );
      data.avroNullableIndex = data.outputRowMeta.indexOfValue( meta.getNullableFieldName() );
      data.avroKettleTypeIndex = data.outputRowMeta.indexOfValue( meta.getKettleTypeFieldName() );
    }

    String schemaStr = null;
    if ( meta.getFormat() != 0 && meta.schemaLocationType == 0 && !Utils.isEmpty( meta.getSchemaLocation() ) ) {
      try {
        schemaStr =
          String.valueOf( new Schema.Parser().parse( KettleVFS.getInputStream( meta.getSchemaLocation(), null ) ) );
      } catch ( IOException e ) {
        throw new RuntimeException( e );
      }
    }

    try {
      Schema schema = new Schema.Parser().parse( schemaStr );

      List<AvroInputField> schemaLeafFields = (List<AvroInputField>) getLeafFields( schema );

      for ( IAvroInputField avroInputField : schemaLeafFields ) {

        Object[] outputRow = RowDataUtil.allocateRowData( data.getOutputRowMeta().size() );

        if ( data.avroSourceFormatIndex > -1 ) {
          outputRow[ data.avroSourceFormatIndex ] = data.avroSourceFormat;
        }
        if ( data.avroDataLocationIndex > -1 ) {
          outputRow[ data.avroDataLocationIndex ] = data.avroDataLocation;
        }
        if ( data.avroDataLocationTypeIndex > -1 ) {
          outputRow[ data.avroDataLocationTypeIndex ] = data.avroDataLocationType;
        }
        if ( data.avroSchemaLocationIndex > -1 ) {
          outputRow[ data.avroSchemaLocationIndex ] = data.avroSchemaLocation;
        }
        if ( data.avroSchemaLocationTypeIndex > -1 ) {
          outputRow[ data.avroSchemaLocationTypeIndex ] = data.avroSchemaLocationType;
        }

        if ( data.avroPathIndex > -1 ) {
          outputRow[ data.avroPathIndex ] = avroInputField.getAvroFieldName();
        }
        if ( data.avroTypeIndex > -1 ) {
          outputRow[ data.avroTypeIndex ] = meta.setAvroFieldType( avroInputField.getAvroType().getBaseType(),
            avroInputField.getAvroType().getLogicalType() );
        }
        if ( data.avroNullableIndex > -1 ) {
          outputRow[ data.avroNullableIndex ] = false;
        }
        if ( data.avroKettleTypeIndex > -1 ) {
          outputRow[ data.avroKettleTypeIndex ] = ValueMetaFactory.getValueMetaName( avroInputField.getPentahoType() );
        }

        putRow( data.outputRowMeta, outputRow );

      }

    } catch ( Exception e ) {
      throw new KettleException( e );
    }

    if ( row == null ) {
      setOutputDone();
      return false;
    }

    return true;
  }

  /**
   * Access getLeafFields Method from Avro Input implementation
   *
   * @param schema
   * @return
   * @throws KettleException
   */
  private static List<? extends IAvroInputField> getLeafFields( Schema schema ) throws KettleException {
    return AvroNestedFieldGetter.getLeafFields( schema );
  }

}
