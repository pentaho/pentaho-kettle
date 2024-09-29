/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.steps.mapping.MappingIODefinition;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;

/**
 * This class takes care of mapping output data from the mapping step back to the parent transformation, renaming
 * columns mainly.
 *
 * @author matt
 *
 */
public class RowOutputDataMapper extends RowAdapter {

  private MappingIODefinition inputDefinition;
  private MappingIODefinition outputDefinition;
  private boolean first = true;
  private RowMetaInterface renamedRowMeta;
  private PutRowInterface putRowInterface;

  public RowOutputDataMapper( MappingIODefinition inputDefinition, MappingIODefinition outputDefinition,
    PutRowInterface putRowInterface ) {
    this.inputDefinition = inputDefinition;
    this.outputDefinition = outputDefinition;
    this.putRowInterface = putRowInterface;
  }

  @Override
  public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {

    if ( first ) {
      first = false;
      renamedRowMeta = rowMeta.clone();

      if ( inputDefinition.isRenamingOnOutput() ) {
        for ( MappingValueRename valueRename : inputDefinition.getValueRenames() ) {
          ValueMetaInterface valueMeta = renamedRowMeta.searchValueMeta( valueRename.getTargetValueName() );
          if ( valueMeta != null ) {
            valueMeta.setName( valueRename.getSourceValueName() );
          }
        }
      }
      for ( MappingValueRename valueRename : outputDefinition.getValueRenames() ) {
        ValueMetaInterface valueMeta = renamedRowMeta.searchValueMeta( valueRename.getSourceValueName() );
        if ( valueMeta != null ) {
          valueMeta.setName( valueRename.getTargetValueName() );
        }
      }
    }

    putRowInterface.putRow( renamedRowMeta, row );
  }
}
