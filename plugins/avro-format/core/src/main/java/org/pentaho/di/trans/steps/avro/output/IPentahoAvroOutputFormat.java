/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.avro.output;

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

  @SuppressWarnings( "squid:S00112" )
  default void setVariableSpace( VariableSpace variableSpace ) {
    throw new UnsupportedOperationException();
  }
}
