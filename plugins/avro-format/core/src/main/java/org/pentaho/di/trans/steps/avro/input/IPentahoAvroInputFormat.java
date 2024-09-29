/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.avro.input;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;

import java.io.InputStream;
import java.util.List;

public interface IPentahoAvroInputFormat extends IPentahoInputFormat {

  /**
   * Set schema for file reading.
   */
  List<? extends IAvroInputField> getFields() throws Exception;

  List<? extends IAvroLookupField> getLookupFields();

  /**
   * Set schema for file reading.
   */
  void setInputFields( List<? extends IAvroInputField> fields ) throws Exception;

  void setLookupFields( List<? extends IAvroLookupField> fields );


  /**
   * Set input file.
   */
  void setInputFile( String file ) throws Exception;

  /**
   * Set input file.
   */
  void setInputSchemaFile( String schemaFile ) throws Exception;

  /**
   * Split size, bytes.
   */
  void setSplitSize( long blockSize ) throws Exception;

  String getInputStreamFieldName();

  void setInputStreamFieldName( String inputStreamFieldName );

  boolean isUseFieldAsInputStream();

  void setInputStream( InputStream inputStream );

  void setVariableSpace( VariableSpace variableSpace );

  void setIncomingFields( Object[] incomingFields );

  void setIncomingRowMeta( RowMetaInterface incomingRowMeta );

  void setOutputRowMeta( RowMetaInterface outputRowMeta );

  List<? extends IAvroInputField> getLeafFields() throws Exception;

  void setIsDataBinaryEncoded( boolean isBinary );

  void setDatum( boolean isDatum );

  void setUseFieldAsSchema( boolean useFieldFromSchema );

  void setSchemaFieldName( String schemaFieldName );

  void setUseFieldAsInputStream( boolean useFieldAsInputStream );
}
