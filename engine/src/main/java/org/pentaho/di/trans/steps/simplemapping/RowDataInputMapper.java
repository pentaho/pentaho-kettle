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


package org.pentaho.di.trans.steps.simplemapping;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.steps.mapping.MappingIODefinition;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;

/**
 * This class renamed fields in rows before passing them to the row producer specified
 *
 * @author matt
 */
public class RowDataInputMapper {
  private final RowProducer rowProducer;
  private final MappingIODefinition inputDefinition;

  private boolean first = true;
  private RowMetaInterface renamedRowMeta;

  public RowDataInputMapper( MappingIODefinition inputDefinition, RowProducer rowProducer ) {
    this.inputDefinition = inputDefinition;
    this.rowProducer = rowProducer;
  }

  /**
   * Attempts to put the <code>row</code> onto the underlying <code>rowProducer</code> during its timeout period.
   * Returns <code>true</code> if the operation completed successfully and <code>false</code> otherwise.
   *
   * @param rowMeta input row's meta data
   * @param row     input row
   * @return <code>true</code> if the <code>row</code> was put successfully
   */
  public boolean putRow( RowMetaInterface rowMeta, Object[] row ) {
    if ( first ) {
      first = false;
      renamedRowMeta = rowMeta.clone();

      for ( MappingValueRename valueRename : inputDefinition.getValueRenames() ) {
        ValueMetaInterface valueMeta = renamedRowMeta.searchValueMeta( valueRename.getSourceValueName() );
        if ( valueMeta != null ) {
          valueMeta.setName( valueRename.getTargetValueName() );
        }
      }
    }
    return rowProducer.putRow( renamedRowMeta, row, false );
  }

  public void finished() {
    rowProducer.finished();
  }
}
