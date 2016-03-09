/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Andrey Khayrutdinov
 */
public class MappingInputMetaCloningTest {

  @Test
  public void clonesCorrectly() throws Exception {
    MappingInputMeta meta = new MappingInputMeta();
    meta.setFieldName( new String[] { "f1", "f2" } );
    meta.setFieldType( new int[] { ValueMetaInterface.TYPE_INTEGER, ValueMetaInterface.TYPE_STRING } );
    meta.setFieldLength( new int[] { 1, 2 } );
    meta.setFieldPrecision( new int[] { 3, 4 } );
    meta.setChanged();
    meta.setValueRenames( Collections.singletonList( new MappingValueRename( "f1", "r1" ) ) );

    Object clone = meta.clone();
    if ( !EqualsBuilder.reflectionEquals( meta, clone ) ) {
      String template = ""
        + "clone() is expected to handle all values.\n"
        + "\tOriginal object:\n"
        + "%s\n"
        + "\tCloned object:\n"
        + "%s";
      String metaStr = ToStringBuilder.reflectionToString( meta );
      String cloneStr = ToStringBuilder.reflectionToString( clone );
      fail( String.format( template, metaStr, cloneStr ) );
    }
  }

}
