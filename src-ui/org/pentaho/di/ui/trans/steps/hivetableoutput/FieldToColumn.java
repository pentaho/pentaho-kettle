package org.pentaho.di.ui.trans.steps.hivetableoutput;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.ui.trans.step.StepTableDataObject;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class FieldToColumn extends XulEventSourceAdapter implements StepTableDataObject {
   private String fieldName = "";
   private String columnName = "";
   
   public FieldToColumn(String fieldName) {
      this.fieldName = fieldName;
   }
   
   public FieldToColumn(String fieldName, String columnName) {
      this.fieldName = fieldName;
      this.columnName = columnName;
   }
   
   public void setFieldName(String fieldName) {
      this.fieldName = fieldName;
   }
   
   public String getFieldName() {
      return fieldName;
   }
    
   public void setColumnName(String columnName) {
      this.columnName = columnName;
   }
   
   public String getColumnName() {
      return columnName;
   }
   
   @Override
   public String getName() {
      return fieldName;
   }

   @Override
   public String getDataType() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public int getLength() {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public int getPrecision() {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public StepTableDataObject createNew(ValueMetaInterface val) {
      // TODO Auto-generated method stub
      return null;
   }
}