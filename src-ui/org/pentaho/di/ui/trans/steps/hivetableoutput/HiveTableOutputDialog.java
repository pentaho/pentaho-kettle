/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.di.ui.trans.steps.hivetableoutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.HiveDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.hivetableoutput.HiveTableOutput;
import org.pentaho.di.trans.steps.hivetableoutput.HiveTableOutputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.trans.step.BaseStepXulDialog;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulTree;

public class HiveTableOutputDialog extends BaseStepXulDialog implements StepDialogInterface {

   private final static String DIALOG_XUL_FILE_NAME = "org/pentaho/di/ui/trans/steps/hivetableoutput/HiveTableOutputDialog.xul";
   private static final Class<?> CLZ = HiveTableOutput.class;
   private HiveTableOutputModel model = new HiveTableOutputModel();
   private XulTextbox stepName;
   private XulTextbox connectionName;
   private XulTextbox targetTableName;
   private XulCheckbox truncateTable;
   private XulTextbox loggingInterval;
   private XulCheckbox targetNameInField;
   private XulMenuList fieldWithTableName;
   private XulCheckbox storeTableName;
   private XulTree fieldsToColumns;
   
   public HiveTableOutputDialog( Shell parentShell, Object hiveTableOutputMeta, TransMeta transMeta, String stepName ) {
		  super(DIALOG_XUL_FILE_NAME, parentShell, (BaseStepMeta) hiveTableOutputMeta, transMeta, stepName);
	     init((HiveTableOutputMeta) hiveTableOutputMeta);
     }
  
   @Override
   public String getName() {
 	  return "controller";
   }

   private void init(HiveTableOutputMeta hiveTableOutputMeta) {
	  
      try {
         //  give the model the step meta
         model.setStepMeta(hiveTableOutputMeta);
         
         //  set a reference gui components
         targetTableName = ((XulTextbox)document.getElementById("target-table-name"));
         stepName = ((XulTextbox)document.getElementById("step-name"));
         connectionName = ((XulTextbox)document.getElementById("connection-name"));
      	truncateTable = ((XulCheckbox)document.getElementById("truncate-table"));
         loggingInterval = ((XulTextbox)document.getElementById("logging-interval"));
      	targetNameInField = ((XulCheckbox)document.getElementById("table-name-in-field"));
      	fieldWithTableName = ((XulMenuList)document.getElementById("field-with-table-name"));
      	storeTableName = ((XulCheckbox)document.getElementById("store-table-name"));
      	fieldsToColumns = ((XulTree)document.getElementById("fields-to-columns"));
         
      	//  set component values
         stepName.setValue(stepname);
         connectionName.setValue(model.getConnectionName());
      	targetTableName.setValue(model.getTargetTableName());
      	truncateTable.setChecked(model.getTruncateTable());
      	loggingInterval.setValue(String.valueOf(model.getLoggingInterval()));
      	targetNameInField.setChecked(model.getTableNameInField());
      	fieldWithTableName.setElements(getFieldNames());
      	if (!Const.isEmpty(model.getFieldWithTableName())) {
      	   fieldWithTableName.setSelectedItem(model.getFieldWithTableName());
      	}
      	storeTableName.setChecked(model.getStoreTableName());
      	fieldsToColumns.setElements(model.getFieldsToColumns());
      	
      	// I may need this for port's string - integer conversion
         final BindingConvertor<String, Integer> bindingConverter = new BindingConvertor<String, Integer>() {
            
            public Integer sourceToTarget(String value) {
               return Integer.parseInt(value);
            }
         
            public String targetToSource(Integer value) {
               return value.toString();
            }
         };
      	
      	//  create bindings from gui to model 
      	bf.setBindingType(Type.BI_DIRECTIONAL);	
      	bf.createBinding(model, HiveTableOutputModel.TRUNCATE_TABLE, truncateTable, "selected"); //$NON-NLS-1$ //$NON-NLS-2$  
         bf.createBinding(model, HiveTableOutputModel.CONNECTION_NAME, connectionName, "value"); //$NON-NLS-1$ //$NON-NLS-2$ 
         bf.createBinding(model, HiveTableOutputModel.STORE_TABLE_NAME, storeTableName, "selected"); //$NON-NLS-1$ //$NON-NLS-2$ 
         bf.createBinding(model, HiveTableOutputModel.LOGGING_INTERVAL, loggingInterval, "value"); //$NON-NLS-1$ //$NON-NLS-2$ 
         bf.createBinding(model, HiveTableOutputModel.FIELD_WITH_TABLE_NAME, fieldWithTableName, "selectedItem"); //$NON-NLS-1$ //$NON-NLS-2$ 
         bf.createBinding(model, HiveTableOutputModel.TARGET_TABLE_NAME, targetTableName, "value"); //$NON-NLS-1$ //$NON-NLS-2$ 
         bf.createBinding(model, HiveTableOutputModel.FIELDS_TO_COLUMNS, fieldsToColumns, "elements"); //$NON-NLS-1$ //$NON-NLS-2$ 
      }
      catch (Exception e) {
         e.printStackTrace();
      }
  }
  
   public void editConnection() {
      DatabaseMeta databaseMeta = transMeta.findDatabase(model.getConnectionName());
      setConnection(databaseMeta);
   }
   
   public void newConnection() {
      setConnection(null);
   }

   private void setConnection(DatabaseMeta databaseMeta) {
      
      DatabaseDialog databaseDialog = getDatabaseDialog(parentShell);
      if (databaseMeta != null) {
        databaseMeta.shareVariablesWith(transMeta);
        databaseDialog.setDatabaseMeta(databaseMeta);
      }
      
      databaseDialog.setModalDialog(true);
      if (databaseDialog.open() != null) {
         log.logError("getConnection(DatabaseMeta) could not open the connection dialog.");
      }
   
      if (databaseMeta != null) {
         model.setConnectionName(databaseMeta.getName()); 
      }
   }
   
   public void getConnection() {
      System.out.println("getConnection has been invoked.");
      HiveTableOutputMeta hiveTableOutputMeta = model.getStepMeta();
      transMeta.addDatabase(hiveTableOutputMeta.getDatabaseMeta());
   }
  
	public void getTableName() {

		DatabaseMeta databaseMeta = new DatabaseMeta();
		databaseMeta.setDatabaseInterface(model.getHiveDatabaseMeta());
		
      DatabaseExplorerDialog databaseExplorerDialog = new DatabaseExplorerDialog(parentShell, SWT.NONE, databaseMeta, null, true);
      if (databaseExplorerDialog.open() != null) {
         model.setTargetTableName(Const.NVL(databaseExplorerDialog.getTableName(), ""));
      }
    	else {
	      MessageBox mb = new MessageBox(parentShell, SWT.OK | SWT.ICON_ERROR );
	      mb.setMessage(BaseMessages.getString(CLZ, "HiveTableOutput.ConnectionError.DialogMessage"));
	      mb.setText(BaseMessages.getString(CLZ, "System.Dialog.Error.Title"));
	      mb.open();                     
	   }
	}
  
   public void getFields() {
      try {
         RowMetaInterface rowMetaInterface = transMeta.getPrevStepFields(stepname);
         if (rowMetaInterface != null) {
            
            ArrayList<FieldToColumn> fieldToColumnList = new ArrayList<FieldToColumn>();
            FieldToColumn fieldToColumn = null;
            
            String[] fieldNames = rowMetaInterface.getFieldNames();
            for(String fieldName: fieldNames) {  
               fieldToColumn = new FieldToColumn(fieldName);
               fieldToColumnList.add(fieldToColumn);
            }
            
            model.setFieldsToColumns(fieldToColumnList);
         }
	   }
	   catch (KettleException ke) {
	      new ErrorDialog(dialogShell, BaseMessages.getString(CLZ, "System.Dialog.GetFieldsFailed.Title"),
	               BaseMessages.getString(CLZ, "System.Dialog.GetFieldsFailed.Message"), ke);
	   }
	}
	
   private List<String> getFieldNames() {
      
      List<String> fieldNames = null;
      try {
         
         RowMetaInterface rowMetaInterface = transMeta.getPrevStepFields(stepname);
         if (rowMetaInterface != null) {
            fieldNames = Arrays.asList(rowMetaInterface.getFieldNames());
         }
         return fieldNames;
      }
      catch (KettleException ke) {
        log.logError(ke.getMessage());
        return fieldNames;
      }
      
   }
   
	public void addNewFieldsRow() {
	   model.addNewFieldRow();
	}
	
  	public void onAccept() {
  	   stepname = stepName.getValue();
   	model.saveMeta();
		dispose();
   }

	@Override
	public void onCancel() {
		dispose();
	}	

	
	public void onSQL() {
      try {      
      
         HiveTableOutputMeta info = model.getStepMeta();
         RowMetaInterface prev = transMeta.getPrevStepFields(stepname);
         if (model.getTableNameInField() && model.getFieldWithTableName().length()>0) {
             int idx = prev.indexOfValue(model.getFieldWithTableName());
   	       if (idx>=0) prev.removeValueMeta(idx);
   	   }
   	   StepMeta stepMeta = transMeta.findStep(stepname);
   	               
         /*  Only use the fields that were specified.
   	   RowMetaInterface prevNew = new RowMeta();
   	                  
   	   for (int i=0;i<info.getFieldDatabase().length;i++) {
   	     ValueMetaInterface insValue = prev.searchValueMeta( info.getFieldStream()[i]); 
   	     if ( insValue != null ) {
               ValueMetaInterface insertValue = insValue.clone();
               insertValue.setName(info.getFieldDatabase()[i]);
               prevNew.addValueMeta( insertValue ); 
   	     }
           else  {
   	        throw new KettleStepException(BaseMessages.getString(CLZ, "HiveTableOutputDialog.FailedToFindField.Message", info.getFieldStream()[i]));  //$NON-NLS-1$
   	     }
           
   	     prev = prevNew;
        }
        */
   	                  
        SQLStatement sql = info.getSQLStatements(transMeta, stepMeta, prev, model.getTargetTableName());
   	  if (!sql.hasError()) {
   	     if (sql.hasSQL()) {
               SQLEditor sqledit = new SQLEditor(getShell(), SWT.NONE, model.getDatabaseMeta(), transMeta.getDbCache(), sql.getSQL());
   	         sqledit.open();
   	     }
   	     else {
   	        MessageBox mb = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION );
   	        mb.setMessage(BaseMessages.getString(CLZ, "HiveTableOutputDialog.NoSQL.DialogMessage"));
   	        mb.setText(BaseMessages.getString(CLZ, "HiveTableOutputDialog.NoSQL.DialogTitle"));
   	        mb.open(); 
   	     }
   	  }
   	  else {
            MessageBox mb = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(sql.getError());
            mb.setText(BaseMessages.getString(CLZ, "System.Dialog.Error.Title"));
            mb.open(); 
        }
	  }
	  catch(KettleException ke) {
        new ErrorDialog(getShell(), BaseMessages.getString(CLZ, "HiveTableOutputDialog.BuildSQLError.DialogTitle"), 
	                                 BaseMessages.getString(CLZ, "HiveTableOutputDialog.BuildSQLError.DialogMessage"), ke);
	  }
	}
	
	@Override
	protected Class<?> getClassForMessages() {
		return CLZ;
	}
	
	public void invertBlocking() {
		model.setBlocking(!model.getBlocking());
	}

	public void invertTableNameInField() {
	   model.setTableNameInField(!model.getTableNameInField());
	}
	
	public void invertTruncateTable() {
		model.setTruncateTable(!model.getTruncateTable());
	}
	
	public void invertStoreTableName() {
		model.setStoreTableName(!model.getStoreTableName());
	}

   /**
    * Reads in the fields from the previous steps and from the ONE next step and opens an 
    * EnterMappingDialog with this information. After the user did the mapping, those information 
    * is put into the Select/Rename table.
    */
   public void mapFields() {

      // Determine the source and target fields...
      //
      RowMetaInterface sourceFields;
      RowMetaInterface targetFields;

      try {
         sourceFields = transMeta.getPrevStepFields(stepMeta);
      } catch(KettleException e) {
         new ErrorDialog(parentShell, BaseMessages.getString(CLZ, "HiveTableOutputDialog.DoMapping.UnableToFindSourceFields.Title"), BaseMessages.getString(CLZ, "TableOutputDialog.DoMapping.UnableToFindSourceFields.Message"), e);
         return;
      }
      
      StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();
      try {
         targetFields = stepMetaInterface.getRequiredFields(transMeta);
      } catch (KettleException e) {
         new ErrorDialog(parentShell, BaseMessages.getString(CLZ, "HiveOutputDialog.DoMapping.UnableToFindTargetFields.Title"), BaseMessages.getString(CLZ, "TableOutputDialog.DoMapping.UnableToFindTargetFields.Message"), e);
         return;
      }

      String[] inputNames = new String[sourceFields.size()];
      for (int i = 0; i < sourceFields.size(); i++) {
         ValueMetaInterface value = sourceFields.getValueMeta(i);
         inputNames[i] = value.getName()+
              EnterMappingDialog.STRING_ORIGIN_SEPARATOR+value.getOrigin()+")";
      }

      // Create the existing mapping list...
      //
      List<SourceToTargetMapping> mappings = new ArrayList<SourceToTargetMapping>();
      StringBuffer missingSourceFields = new StringBuffer();
      StringBuffer missingTargetFields = new StringBuffer();

      ArrayList<FieldToColumn> fieldsToColumns = model.getFieldsToColumns();

      for (FieldToColumn fieldToColumn: fieldsToColumns) {
         String fieldName = fieldToColumn.getFieldName();
         String columName = fieldToColumn.getColumnName();
         
         int sourceIndex = sourceFields.indexOfValue(fieldName); 
         if (sourceIndex<0) {
            missingSourceFields.append(Const.CR + "   " + fieldName+" --> " + columName);
         }
         int targetIndex = targetFields.indexOfValue(columName);
         if (targetIndex<0) {
            missingTargetFields.append(Const.CR + "   " + fieldName+" --> " + columName);
         }
         if (sourceIndex<0 || targetIndex<0) {
            continue;
         }

         SourceToTargetMapping mapping = new SourceToTargetMapping(sourceIndex, targetIndex);
         mappings.add(mapping);
         
      }

      // show a confirm dialog if some missing field was found
      //
      if (missingSourceFields.length()>0 || missingTargetFields.length()>0){
         
         String message="";
         if (missingSourceFields.length()>0) {
            message+=BaseMessages.getString(CLZ, "HiveTableOutputDialog.DoMapping.SomeSourceFieldsNotFound", missingSourceFields.toString())+Const.CR;
         }
         if (missingTargetFields.length()>0) {
            message+=BaseMessages.getString(CLZ, "HiveTableOutputDialog.DoMapping.SomeTargetFieldsNotFound", missingSourceFields.toString())+Const.CR;
         }
         message+=Const.CR;
         message+=BaseMessages.getString(CLZ, "HiveTableOutputDialog.DoMapping.SomeFieldsNotFoundContinue")+Const.CR;
         MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
         boolean goOn = MessageDialog.openConfirm(parentShell, BaseMessages.getString(CLZ, "HiveTableOutputDialog.DoMapping.SomeFieldsNotFoundTitle"), message);
         if (!goOn) {
            return;
         }
      }
      EnterMappingDialog d = new EnterMappingDialog(parentShell, sourceFields.getFieldNames(), targetFields.getFieldNames(), mappings);
      mappings = d.open();

      // mappings == null if the user pressed cancel
      //
      if (mappings!=null) {
         // Clear and re-populate!
         
         FieldToColumn fieldToColumn;
         model.removeAllFieldsToColumns();
         for(SourceToTargetMapping mapping: mappings ) {
            fieldToColumn = new FieldToColumn(sourceFields.getValueMeta(mapping.getSourcePosition()).getName());
            fieldToColumn.setColumnName(targetFields.getValueMeta(mapping.getTargetPosition()).getName());       
         }
      } 
   }
}
