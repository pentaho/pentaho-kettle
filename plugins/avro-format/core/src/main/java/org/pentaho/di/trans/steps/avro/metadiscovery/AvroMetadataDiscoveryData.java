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

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class AvroMetadataDiscoveryData extends BaseStepData implements StepDataInterface {

  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;

  public int schemaFieldIndex = -1;
  public int avroPathIndex = -1;
  public int avroNullableIndex = -1;
  public int avroTypeIndex = -1;
  public int avroKettleTypeIndex = -1;

  public int avroDataLocationIndex = -1;

  public int avroDataLocationTypeIndex = -1;

  public int avroSchemaLocationIndex = -1;

  public int avroSchemaLocationTypeIndex = -1;

  public int avroSourceFormatIndex = -1;

  public String avroDataLocation;

  public int avroDataLocationType;

  public String avroSchemaLocation;

  public int avroSchemaLocationType;

  public int avroSourceFormat;

  public String avroPathFieldName;

  public String nullableFieldName;

  public String avroTypeFieldName;

  public String kettleTypeFieldName;


  public RowMetaInterface getOutputRowMeta() {
    return outputRowMeta;
  }

  public void setOutputRowMeta( RowMetaInterface outputRowMeta ) {
    this.outputRowMeta = outputRowMeta;
  }

}
