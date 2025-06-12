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

package org.pentaho.di.trans.steps.avro.output;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.variables.VariableSpace;

import java.util.List;

public interface IPentahoAvroOutputFormat extends IPentahoOutputFormat {

  enum COMPRESSION {
    UNCOMPRESSED, SNAPPY, DEFLATE
  }

  void setFields( List<? extends IAvroOutputField> fields ) throws Exception;

  void setOutputFile( String file, boolean override ) throws Exception;

  void setCompression( COMPRESSION compression );

  void setNameSpace( String namespace );

  void setRecordName( String recordName );

  void setDocValue( String docValue );

  void setSchemaFilename( String schemaFilename );

  void setBowl( Bowl bowl );

  @SuppressWarnings( "squid:S00112" )
  default void setVariableSpace( VariableSpace variableSpace ) {
    throw new UnsupportedOperationException();
  }
}
