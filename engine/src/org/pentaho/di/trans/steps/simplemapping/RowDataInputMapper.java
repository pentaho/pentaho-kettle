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
