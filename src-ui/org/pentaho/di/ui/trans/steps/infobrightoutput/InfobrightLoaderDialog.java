package org.pentaho.di.ui.trans.steps.infobrightoutput;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.infobrightoutput.InfobrightLoaderMetadata;
import org.pentaho.di.trans.steps.infobrightoutput.Messages;
import org.pentaho.di.ui.core.widget.TextVar;

import com.infobright.etl.model.DataFormat;

/**
 * Dialog box for the BrightHouse loader.
 */
public class InfobrightLoaderDialog extends InfobrightDatabaseDialog {

  private CCombo dataFormatSelect;
  private TextVar targetSchemaText;
  private TextVar targetTableText;  
  //private Button rejectInvalidRowsButton;
  
  /**
   * @param parent
   * @param in
   * @param transMeta
   * @param sname
   */
  public InfobrightLoaderDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
    super(parent, in, transMeta, sname);
    addMessagePackage(Messages.class.getPackage());
  }

  @Override
  public void getData() {
    super.getData();
    
    InfobrightLoaderMetadata input = (InfobrightLoaderMetadata) getInput();
    if (input.getSchemaName() != null) {
      targetSchemaText.setText(input.getSchemaName());
    }
    if (input.getTablename() != null) {
      targetTableText.setText(input.getTablename());
    }
    if (input.getInfobrightProductType() != null) {
      dataFormatSelect.setText(input.getInfobrightProductType());
    }
    //rejectInvalidRowsButton.setSelection(input.isRejectErrors());
  } 

  @Override
  protected void ok() {
    InfobrightLoaderMetadata input = (InfobrightLoaderMetadata) getInput();
    input.setSchemaName(targetSchemaText.getText());
    input.setTablename(targetTableText.getText());
    input.setDataFormat(DataFormat.valueForDisplayName(dataFormatSelect.getText()));
    //input.setRejectErrors(rejectInvalidRowsButton.getSelection());
    super.ok();
  }

  @Override
  protected Control addCustomInputs(Control prevControl) {
    String[] dataformats = new String[DataFormat.values().length];
    int i = 0;
    for (DataFormat format : DataFormat.values()) {
      dataformats[i++] = format.getDisplayText();
    }
    dataFormatSelect = addStandardSelect("BrightHouseLoaderDialog.Dataformat.Label", prevControl, dataformats);
    targetSchemaText = addStandardTextVar("BrightHouseLoaderDialog.TargetSchema.Label", prevControl /*dataFormatSelect*/);
    targetTableText = addStandardTextVar("BrightHouseLoaderDialog.TargetTable.Label", targetSchemaText);
    //rejectInvalidRowsButton = addStandardCheckBox("BrightHouseLoaderDialog.RejectErrors.Label", targetTableText);
    return targetTableText;
  }
}
