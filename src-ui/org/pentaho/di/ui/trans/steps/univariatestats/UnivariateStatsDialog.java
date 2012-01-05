/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.univariatestats;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.univariatestats.UnivariateStatsMeta;
import org.pentaho.di.trans.steps.univariatestats.UnivariateStatsMetaFunction;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * The UI class for the UnivariateStats transform
 *
 * @author Mark Hall (mhall{[at]}pentaho.org
 * @version 1.0
 */
public class UnivariateStatsDialog extends BaseStepDialog
  implements StepDialogInterface {

  private static Class<?> PKG = UnivariateStatsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  /** various UI bits and pieces for the dialog */
  private Label m_wlStepname;
  private Text m_wStepname;
  private FormData m_fdlStepname;
  private FormData m_fdStepname;
  private Label m_wlFields;
  private TableView m_wFields;
  private FormData m_fdlFields;
  private FormData m_fdFields;

  /**
   * meta data for the step. A copy is made so
   * that changes, in terms of choices made by the
   * user, can be detected.
   */
  private UnivariateStatsMeta m_currentMeta;
  private UnivariateStatsMeta m_originalMeta;

  // holds the names of the fields entering this step
  private Map<String, Integer> m_inputFields;
  private ColumnInfo[] m_colinf;

  public UnivariateStatsDialog(Shell parent, 
                               Object in, 
                               TransMeta tr, 
                               String sname) {

    super(parent, (BaseStepMeta) in, tr, sname);

    // The order here is important... 
    //m_currentMeta is looked at for changes
    m_currentMeta = (UnivariateStatsMeta) in;
    m_originalMeta = (UnivariateStatsMeta) m_currentMeta.clone();
    m_inputFields = new HashMap<String, Integer>();
  }

  /**
   * Open the dialog
   *
   * @return the step name
   */
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = 
      new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);

    props.setLook(shell);
    setShellImage(shell, m_currentMeta);

    // used to listen to a text field (m_wStepname)
    ModifyListener lsMod = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          m_currentMeta.setChanged();
        }
      };

    changed = m_currentMeta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "UnivariateStatsDialog.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    m_wlStepname = new Label(shell, SWT.RIGHT);
    m_wlStepname.
      setText(BaseMessages.getString(PKG, "UnivariateStatsDialog.StepName.Label"));
    props.setLook(m_wlStepname);

    m_fdlStepname = new FormData();
    m_fdlStepname.left = new FormAttachment(0, 0);
    m_fdlStepname.right = new FormAttachment(middle, -margin);
    m_fdlStepname.top = new FormAttachment(0, margin);
    m_wlStepname.setLayoutData(m_fdlStepname);
    m_wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    m_wStepname.setText(stepname);
    props.setLook(m_wStepname);
    m_wStepname.addModifyListener(lsMod);

    // format the text field
    m_fdStepname = new FormData();
    m_fdStepname.left = new FormAttachment(middle, 0);
    m_fdStepname.top = new FormAttachment(0, margin);
    m_fdStepname.right = new FormAttachment(100, 0);
    m_wStepname.setLayoutData(m_fdStepname);

    m_wlFields = new Label(shell, SWT.NONE);
    m_wlFields.setText(BaseMessages.getString(PKG, "UnivariateStatsDialog.Fields.Label"));
    props.setLook(m_wlFields);
    m_fdlFields = new FormData();
    m_fdlFields.left = new FormAttachment(0, 0);
    m_fdlFields.top = new FormAttachment(m_wStepname, margin);
    m_wlFields.setLayoutData(m_fdlFields);

    final int fieldsRows = 
      (m_currentMeta.getInputFieldMetaFunctions() != null)
      ? m_currentMeta.getNumFieldsToProcess() 
      : 1;

    m_colinf = new ColumnInfo[] {
        new ColumnInfo(BaseMessages.getString(PKG, "UnivariateStatsDialog.InputFieldColumn.Column"),
             ColumnInfo.COLUMN_TYPE_CCOMBO, new String [] { "" }, true),
        new ColumnInfo(BaseMessages.getString(PKG, "UnivariateStatsDialog.NColumn.Column"),
             ColumnInfo.COLUMN_TYPE_CCOMBO, 
             new String [] { "True", "False" }, true),
        new ColumnInfo(BaseMessages.getString(PKG, "UnivariateStatsDialog.MeanColumn.Column"),
             ColumnInfo.COLUMN_TYPE_CCOMBO, 
             new String [] { "True", "False" }, true),
        new ColumnInfo(BaseMessages.getString(PKG, "UnivariateStatsDialog.StdDevColumn.Column"),
             ColumnInfo.COLUMN_TYPE_CCOMBO, 
             new String [] { "True", "False" }, true),
        new ColumnInfo(BaseMessages.getString(PKG, "UnivariateStatsDialog.MinColumn.Column"),
             ColumnInfo.COLUMN_TYPE_CCOMBO, 
             new String [] { "True", "False" }, true),
        new ColumnInfo(BaseMessages.getString(PKG, "UnivariateStatsDialog.MaxColumn.Column"),
             ColumnInfo.COLUMN_TYPE_CCOMBO, 
             new String [] { "True", "False" }, true),
        new ColumnInfo(BaseMessages.getString(PKG, "UnivariateStatsDialog.MedianColumn.Column"),
             ColumnInfo.COLUMN_TYPE_CCOMBO, 
             new String [] { "True", "False" }, true),
        new ColumnInfo(BaseMessages.getString(PKG, "UnivariateStatsDialog.PercentileColumn.Column"),
            ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "UnivariateStatsDialog.InterpolateColumn.Column"),
             ColumnInfo.COLUMN_TYPE_CCOMBO, 
             new String [] { "True", "False" }, true)
    };

    m_wFields = new TableView(transMeta, shell,
        SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
        m_colinf, fieldsRows, lsMod,
        props);

    m_fdFields = new FormData();
    m_fdFields.left = new FormAttachment(0, 0);
    m_fdFields.top = new FormAttachment(m_wlFields, margin);
    m_fdFields.right = new FormAttachment(100, 0);
    m_fdFields.bottom = new FormAttachment(100, -50);
    m_wFields.setLayoutData(m_fdFields);


    // Search the fields in the background
    final Runnable runnable = new Runnable() {
        public void run() {
          StepMeta stepMeta = transMeta.findStep(stepname);

          if (stepMeta != null) {
            try {
              RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);

              // Remember these fields...
              for (int i = 0; i < row.size(); i++) {
                ValueMetaInterface field = row.getValueMeta(i);
                // limit the choices to only numeric input fields
                if (field.isNumeric()) {
                  m_inputFields.put(field.getName(),
                                    Integer.valueOf(i));
                }
              }

              setComboBoxes();
            } catch (KettleException e) {
              logError(
                BaseMessages.getString(PKG, "UnivariateStatsDialog.Log.UnableToFindInput"));
            }
          }
        }
      };

    new Thread(runnable).start();

    m_wFields.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent arg0) {
          // Now set the combo's
          shell.getDisplay().asyncExec(new Runnable() {
              public void run() {
                setComboBoxes();
              }
            });
        }
      });
    
    // Some buttons
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

    // Add listeners
    lsCancel = new Listener() {
        public void handleEvent(Event e) {
          cancel();
        }
      };
    lsOK = new Listener() {
        public void handleEvent(Event e) {
          ok();
        }
      };

    wCancel.addListener(SWT.Selection, lsCancel);
    wOK.addListener(SWT.Selection, lsOK);

    lsDef = new SelectionAdapter() {
        public void widgetDefaultSelected(SelectionEvent e) {
          ok();
        }
      };

    m_wStepname.addSelectionListener(lsDef);

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
        public void shellClosed(ShellEvent e) {
          cancel();
        }
      });

    // Set the shell size, based upon previous time...
    setSize();

    getData(); // read stats settings from the step

    m_currentMeta.setChanged(changed);

    shell.open();

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }

    return stepname;
  }

  /**
   * Set up the input field combo box
   */
  protected void setComboBoxes() {
    Set<String> keySet = m_inputFields.keySet();
    List<String> entries = new ArrayList<String>(keySet);
    String[] fieldNames = 
      (String[]) entries.toArray(new String[entries.size()]);
    Const.sortStrings(fieldNames);
    m_colinf[0].setComboValues(fieldNames);
  }

  /**
   * Copy information from the meta-data m_currentMeta to the 
   * dialog fields.
   */
  public void getData() {
    m_wStepname.selectAll();

    if (m_currentMeta.getInputFieldMetaFunctions() != null) {
      for (int i = 0; i < m_currentMeta.getNumFieldsToProcess(); i++) {
        UnivariateStatsMetaFunction fn = 
          m_currentMeta.getInputFieldMetaFunctions()[i];

        TableItem item = m_wFields.table.getItem(i);

        item.setText(1, Const.NVL(fn.getSourceFieldName(), ""));
        item.setText(2, Const.NVL(
                                  (fn.getCalcN())
                                  ? "True"
                                  : "False", ""));
        item.setText(3, Const.NVL(
                                  (fn.getCalcMean())
                                  ? "True"
                                  : "False", ""));
        item.setText(4, Const.NVL(
                                  (fn.getCalcStdDev())
                                  ? "True"
                                  : "False", ""));
        item.setText(5, Const.NVL(
                                  (fn.getCalcMin())
                                  ? "True"
                                  : "False", ""));
        item.setText(6, Const.NVL(
                                  (fn.getCalcMax())
                                  ? "True"
                                  : "False", ""));
        item.setText(7, Const.NVL(
                                  (fn.getCalcMedian())
                                  ? "True"
                                  : "False", ""));
        double p = fn.getCalcPercentile();
        NumberFormat pF = NumberFormat.getInstance();
        pF.setMaximumFractionDigits(2);
        String res = (p < 0) 
          ? ""
          : pF.format(p * 100);
        item.setText(8, Const.NVL(res, ""));

        item.setText(9, Const.NVL(
                                  (fn.getInterpolatePercentile())
                                  ? "True"
                                  : "False", ""));
      }

      m_wFields.setRowNums();
      m_wFields.optWidth(true);
    }
  }

  private void cancel() {
    stepname = null;
    m_currentMeta.setChanged(changed);
    dispose();
  }

  private void ok() {
    if (Const.isEmpty(m_wStepname.getText())) {
      return;
    }

    stepname = m_wStepname.getText(); // return value

    int nrNonEmptyFields = m_wFields.nrNonEmpty();
    m_currentMeta.allocate(nrNonEmptyFields);

    for (int i = 0; i < nrNonEmptyFields; i++) {
      TableItem item = m_wFields.getNonEmpty(i);
      
      String inputFieldName = item.getText(1);
      boolean n = item.getText(2).equalsIgnoreCase("True");
      boolean mean = item.getText(3).equalsIgnoreCase("True");
      boolean stdDev = item.getText(4).equalsIgnoreCase("True");
      boolean min = item.getText(5).equalsIgnoreCase("True");
      boolean max = item.getText(6).equalsIgnoreCase("True");
      boolean median = item.getText(7).equalsIgnoreCase("True");
      String percentileS = item.getText(8);
      double percentile = -1;
      if (percentileS.length() > 0) {
        // try to parse percentile
        try {
          percentile = Double.parseDouble(percentileS);
          if (percentile < 0) {
            percentile = -1;
          } else if (percentile > 1 && percentile <= 100) {
            percentile /= 100;
          }          
        } catch (Exception ex) {          
        }
      }
      boolean interpolate = item.getText(9).equalsIgnoreCase("True");

      m_currentMeta.getInputFieldMetaFunctions()[i] = 
        new UnivariateStatsMetaFunction(inputFieldName, n, mean, stdDev,
                                        min, max, median, percentile,
                                        interpolate);
    }

    if (!m_originalMeta.equals(m_currentMeta)) {
      m_currentMeta.setChanged();
      changed = m_currentMeta.hasChanged();
    }

    dispose();
  }
}
