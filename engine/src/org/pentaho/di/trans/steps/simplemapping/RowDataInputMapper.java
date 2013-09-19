/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.simplemapping;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.steps.mapping.MappingIODefinition;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;

/**
 * This class renamed fields in rows before passing them to the row producer specified 
 * @author matt
 *
 */
public class RowDataInputMapper {
  private RowProducer rowProducer;
  private MappingIODefinition inputDefinition;
  private boolean first = true;
  private RowMetaInterface renamedRowMeta;
  
  public RowDataInputMapper(MappingIODefinition inputDefinition, RowProducer rowProducer) {
    this.inputDefinition = inputDefinition;
    this.rowProducer = rowProducer;
  }
  
  public void putRow(RowMetaInterface rowMeta, Object[] row) {
    if (first) {
      first = false;
      renamedRowMeta = rowMeta.clone();
      
      for (MappingValueRename valueRename : inputDefinition.getValueRenames()) {
        ValueMetaInterface valueMeta = renamedRowMeta.searchValueMeta(valueRename.getSourceValueName());
        if (valueMeta!=null) {
          valueMeta.setName(valueRename.getTargetValueName());
        }
      }      
    }
    rowProducer.putRow(renamedRowMeta, row);
  }
  
  public void finished() {
    rowProducer.finished();
  }
}
