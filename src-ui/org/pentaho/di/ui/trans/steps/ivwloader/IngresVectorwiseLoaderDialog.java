package org.pentaho.di.ui.trans.steps.ivwloader;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.IngresDatabaseMeta;
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
import org.pentaho.di.trans.steps.ivwloader.IngresVectorwiseLoaderMeta;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Dialog box for the VectorWise loader.
 */
public class IngresVectorwiseLoaderDialog extends BaseStepDialog implements StepDialogInterface {
  
  private static Class<?> PKG = IngresVectorwiseLoaderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private static final char PASSWD_ECHO_CHAR = '*';
  
  private int middle;
  private int margin;
  
  private CCombo serverConnection;

  protected IngresVectorwiseLoaderMeta input;
  
  private ModifyListener lsMod;
  private SelectionAdapter lsSelMod;

  private TextVar wTable;
  private TextVar   wFifoFile;
  private TextVar   wSqlPath;
  private TableView wFields;
  private TextVar   wDelimiter;
  private TextVar   wCharSet;
  private TextVar   wErrorFile;
  private Button  wContinueOnError;
  private Button  wUseStandardConversion;
  private Button  wUseDynamicVNode;
  private Button  wUseSSV;

  /**
   * List of ColumnInfo that should have the field names of the selected database table
   */
  private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();
 
  /**
   * @param parent
   * @param in
   * @param tr
   * @param sname
   */
  public IngresVectorwiseLoaderDialog(Shell parent, Object in, TransMeta tr, String sname) {
    super(parent, (BaseStepMeta) in, tr, sname);
    input = (IngresVectorwiseLoaderMeta) in;
    
    lsMod = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        input.setChanged();
      }
    };
    
    lsSelMod = new SelectionAdapter()
    {
        public void widgetSelected(SelectionEvent arg0)
        {
            input.setChanged();
        }
    };
  }
  
  /**
   * {@inheritDoc}
   * @see org.pentaho.di.trans.step.StepDialogInterface#open()
   */
  public String open() {
    shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
    props.setLook(shell);
    
    setShellImage(shell, (StepMetaInterface) input);

    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText("Ingres VectorWise Loader");

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    /************************************** Step name line ***************************/
    // label
    wlStepname = new Label(shell, SWT.RIGHT);
    wlStepname.setText("Step Name");
    wlStepname.setLayoutData(standardLabelSpacing(null, null));
    props.setLook(wlStepname);

    // text entry
    wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wStepname.setText(stepname);
    wStepname.addModifyListener(lsMod);
    wStepname.setLayoutData(standardInputSpacing(null, wlStepname));
    props.setLook(wStepname);
    

    Control lastControl = addDbConnectionInputs();
    lastControl = addCustomInputs(lastControl);
    
    addVerticalPadding(2 * margin);
    
    /********************************** OK and Cancel buttons **************************/
    addDefaultButtons(margin, lastControl);

    lastControl = addFieldSelection(lastControl);

    getData();
    input.setChanged(changed);

    // Add listeners
//    Listener lsVisualize   = new Listener() { public void handleEvent(Event e) { quickVisualize(); } };
    
//    wVisualize.addListener(SWT.Selection, lsVisualize );

    shell.open();
    while (!shell.isDisposed()) {
      Display display = getParent().getDisplay();

      if (!display.readAndDispatch())
        display.sleep();
    }
    return stepname;
  }

  private Control addFieldSelection(Control lastControl) {
    // The fields table
    Label wlFields = new Label(shell, SWT.NONE);
    wlFields.setText(BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.InsertFields.Label")); //$NON-NLS-1$
    props.setLook(wlFields);
    FormData fdlUpIns=new FormData();
    fdlUpIns.left  = new FormAttachment(0, 0);
    fdlUpIns.top   = new FormAttachment(lastControl, 2*margin);
    wlFields.setLayoutData(fdlUpIns);

    int tableCols=2;
    int UpInsRows= (input.getFieldStream()!=null?input.getFieldStream().length:1);

    ColumnInfo[] ciFields = new ColumnInfo[tableCols];
    ciFields[0]=new ColumnInfo(BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.ColumnInfo.TableField"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
    ciFields[1]=new ColumnInfo(BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.ColumnInfo.StreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
    tableFieldColumns.add(ciFields[0]);
    wFields=new TableView(transMeta, shell,
                          SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
                          ciFields,
                          UpInsRows,
                          lsMod,
                          props
                          );
    
    Button wGetFields = new Button(shell, SWT.PUSH);
    wGetFields.setText(BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.GetFields.Button")); //$NON-NLS-1$
    FormData fdGetFields = new FormData();
    fdGetFields.top   = new FormAttachment(wlFields, margin);
    fdGetFields.right = new FormAttachment(100, 0);
    wGetFields.setLayoutData(fdGetFields);
    
    Button wDoMapping = new Button(shell, SWT.PUSH);
    wDoMapping.setText(BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.DoMapping.Button")); //$NON-NLS-1$
    FormData fdDoMapping = new FormData();
    fdDoMapping.top   = new FormAttachment(wGetFields, margin);
    fdDoMapping.right = new FormAttachment(100, 0);
    wDoMapping.setLayoutData(fdDoMapping);

    wGetFields.addListener(SWT.Selection, new Listener() {  public void handleEvent(Event e) { get(); } });
    wDoMapping.addListener(SWT.Selection, new Listener() {  public void handleEvent(Event arg0) { generateMappings();}});
    
    FormData fdFields=new FormData();
    fdFields.left  = new FormAttachment(0, 0);
    fdFields.top   = new FormAttachment(wlFields, margin);
    fdFields.right = new FormAttachment(wGetFields, -margin);
    fdFields.bottom= new FormAttachment(wOK, -2 * margin);
    wFields.setLayoutData(fdFields);

    return wFields;
  }

  /**
   * Adds db connection text boxes for input
   * @return the last control specified
   */
  protected Control addDbConnectionInputs() {
  List<String> ibConnections = new ArrayList<String>();
  for (DatabaseMeta dbMeta : transMeta.getDatabases()) {
    if (dbMeta.getDatabaseInterface() instanceof IngresDatabaseMeta) {
      ibConnections.add(dbMeta.getName());
    }
  }
  serverConnection = addStandardSelect("Connection", wStepname, ibConnections.toArray(new String[ibConnections.size()]));
  
    return serverConnection;
  }

  /**
   * Adds any custom inputs
   * @param prevControl
   * @return the last control
   */
  protected Control addCustomInputs(Control prevControl) {
    wTable = addStandardTextVar(BaseMessages.getString(PKG, "IngresVectorwiseLoaderDialog.TargetTable.Label"), prevControl);
    wUseDynamicVNode = addStandardCheckBox(BaseMessages.getString(PKG, "IngresVectorwiseLoaderDialog.UseDynamicVNode.Label"), wTable);
    wUseDynamicVNode.addSelectionListener(lsSelMod);
    wFifoFile = addStandardTextVar(BaseMessages.getString(PKG, "IngresVectorwiseLoaderDialog.FifoFile.Label"), wUseDynamicVNode);
    wSqlPath = addStandardTextVar(BaseMessages.getString(PKG, "IngresVectorwiseLoaderDialog.SqlPath.Label"), wFifoFile);
    wUseSSV = addStandardCheckBox(BaseMessages.getString(PKG, "IngresVectorwiseLoaderDialog.UseSSVDelimiter.Label"), wSqlPath);
    wUseSSV.addSelectionListener(lsSelMod);
    wUseSSV.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent se)
                {
                    if (wUseSSV.getSelection())  {
                      wDelimiter.setEnabled(false);
                    }else{
                      wDelimiter.setEnabled(true);
                    }
                }
            }
        );
    wDelimiter = addStandardTextVar(BaseMessages.getString(PKG, "IngresVectorwiseLoaderDialog.Delimiter.Label"), wUseSSV);
    wCharSet = addStandardTextVar(BaseMessages.getString(PKG, "IngresVectorwiseLoaderDialog.Charset.Label"), wDelimiter);
    wUseStandardConversion = addStandardCheckBox(BaseMessages.getString(PKG, "IngresVectorwiseLoaderDialog.UseStandardConversion.Label"), wCharSet);
    wUseStandardConversion.addSelectionListener(lsSelMod);
    wContinueOnError = addStandardCheckBox(BaseMessages.getString(PKG, "IngresVectorwiseLoaderDialog.ContinueOnError.Label"), wUseStandardConversion);
    wContinueOnError.addSelectionListener(lsSelMod);
    wErrorFile = addStandardTextVar(BaseMessages.getString(PKG, "IngresVectorwiseLoaderDialog.ErrorFile.Label"), wContinueOnError);
    wErrorFile.addModifyListener(lsMod);
    //standard is disabled
    wErrorFile.setEnabled(false);
    wContinueOnError.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent se)
                {
                    if (wContinueOnError.getSelection())  {
                      wErrorFile.setEnabled(true);
                    }else{
                      wErrorFile.setEnabled(false);
                    }
                }
            }
        );
   
    
    return wErrorFile;
  }

  protected CCombo addStandardSelect(String labelMessageKey, Control prevControl, String[] choices) {
    int vertPad = verticalPadding;
    Label label = addStandardLabel(labelMessageKey, prevControl);
    verticalPadding = vertPad;
    CCombo combo = new CCombo(shell, SWT.BORDER);
    combo.setItems(choices);
    combo.addModifyListener(lsMod);
    combo.setLayoutData(standardInputSpacing(prevControl, label));
    return combo;
  }
  
  protected TextVar addStandardTextVar(String labelMessageKey, Control prevControl) {
    int vertPad = verticalPadding;
    Label label = addStandardLabel(labelMessageKey, prevControl);
    verticalPadding = vertPad;
    TextVar targetControl = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    targetControl.addModifyListener(lsMod);
    targetControl.setLayoutData(standardInputSpacing(prevControl, label));
    targetControl.addSelectionListener(new SelectionAdapter() { @Override
    public void widgetDefaultSelected(SelectionEvent event) { ok(); } });
    return targetControl;
  }
 
  protected TextVar addPasswordTextVar(String labelMessageKey, Control prevControl) {
    TextVar textVar = addStandardTextVar(labelMessageKey, prevControl);
    textVar.setEchoChar(PASSWD_ECHO_CHAR);
    return textVar;
  }
  

  
  protected Button addStandardCheckBox(String labelMessageKey, Control prevControl) {
      Label label = addStandardLabel(labelMessageKey, prevControl);
      Button targetControl = new Button(shell, SWT.CHECK);
      targetControl.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          input.setChanged();
        }
      }
      );
      targetControl.setLayoutData(standardInputSpacing(prevControl, label));
      return targetControl;
    }
  
  private Label addStandardLabel(String messageString, Control previousControl) {
    Label label = new Label(shell, SWT.RIGHT);
    label.setText(messageString);
    label.setLayoutData(standardLabelSpacing(previousControl, label));
    props.setLook(label);
    return label;
  }
  
  private int verticalPadding = 0;
  private void addVerticalPadding(int amount) {
    verticalPadding += amount;
  }
  
  private FormData standardLabelSpacing(Control control, Control centerControl) {
    return standardSpacing(control, centerControl, true);
  }
  
  private FormData standardInputSpacing(Control control, Control centerControl) {
    return standardSpacing(control, centerControl, false);
  }
  
  private FormData standardSpacing(Control control, Control centerControl, boolean isLabel) {
    FormData fd = new FormData();
    
    if (isLabel)
      fd.left = new FormAttachment(0, 0);
    else  
      fd.left = new FormAttachment(middle, 0);
    
    if (isLabel)
      fd.right = new FormAttachment(middle, -Const.MARGIN);
    else
      fd.right = new FormAttachment(100, 0);
    
    if (!isLabel && centerControl!=null) {
      fd.top = new FormAttachment(centerControl, 0, SWT.CENTER);
    } else {
      int extraSpace;
      if (isLabel) {
        extraSpace=Const.MARGIN+verticalPadding;
      } else {
        extraSpace=0;
      }
      if (control != null)
        fd.top = new FormAttachment(control, Const.MARGIN+verticalPadding+extraSpace);
      else
        fd.top = new FormAttachment(0, Const.MARGIN+verticalPadding+extraSpace);
    }
    
    verticalPadding = 0;
    return fd;
  }
  
  private void addDefaultButtons(int margin, Control lastControl) {
    // Some buttons
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wCreate=new Button(shell, SWT.PUSH);
    wCreate.setText(BaseMessages.getString(PKG, "System.Button.SQL"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    setButtonPositions(new Button[] { wOK, wCreate, wCancel }, margin, null);

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent(Event e) {
        cancel();
      }
    };
    
    lsCreate = new Listener() {
      public void handleEvent(Event e) {
        sql();
      }
    };

    lsOK = new Listener() {
      public void handleEvent(Event e) {
        ok();
      }
    };
    
    wCancel.addListener(SWT.Selection, lsCancel);
    wCreate.addListener(SWT.Selection, lsCreate);
    wOK.addListener(SWT.Selection, lsOK);

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected(SelectionEvent e) {
        ok();
      }
    };
    wStepname.addSelectionListener(lsDef);

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });

    // Set the shell size, based upon previous time...
    setSize();    
  }
  
  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wStepname.selectAll();
    if (input.getDatabaseMeta()!=null) {
      serverConnection.setText(input.getDatabaseMeta().getName());
    }
    wTable.setText(Const.NVL(input.getTablename(), ""));
    wFifoFile.setText(Const.NVL(input.getFifoFileName(), ""));
    wSqlPath.setText(Const.NVL(input.getSqlPath(), ""));
    wUseSSV.setSelection(input.isUseSSV());
    if(input.isUseSSV()){
      wDelimiter.setEnabled(false);
    }
    wDelimiter.setText(Const.NVL(input.getDelimiter(), ""));   //$NON-NLS-1$
    wCharSet.setText(Const.NVL(input.getEncoding(), ""));   //$NON-NLS-1$
    
    wUseStandardConversion.setSelection(input.isUseStandardConversion());
    wUseDynamicVNode.setSelection(input.isUseDynamicVNode());
    wContinueOnError.setSelection(input.isContinueOnError());
    wErrorFile.setText(Const.NVL(input.getErrorFileName(),""));
    if(input.isContinueOnError()){
      wErrorFile.setEnabled(true);
    }
    

    for (int i=0; i<input.getFieldDatabase().length; i++)
    {
        TableItem item = wFields.table.getItem(i);
        if (input.getFieldDatabase()[i]!=null ) item.setText(1, input.getFieldDatabase()[i]);
        if (input.getFieldStream()[i]!=null )   item.setText(2, input.getFieldStream()[i]);
    }
  }

  protected void cancel() {
    stepname = null;
    input.setChanged(changed);
    dispose();
  }
  
  private void getInfo(IngresVectorwiseLoaderMeta input) {
    input.setDatabaseMeta( transMeta.findDatabase(serverConnection.getText()) );
    input.setTablename(wTable.getText());
    input.setFifoFileName(wFifoFile.getText());
    input.setSqlPath(wSqlPath.getText());
    input.setUseSSV(wUseSSV.getSelection());
    input.setDelimiter( wDelimiter.getText() );
    input.setEncoding( wCharSet.getText() );
    input.setUseStandardConversion(wUseStandardConversion.getSelection());
    input.setContinueOnError(wContinueOnError.getSelection());
    input.setErrorFileName(wErrorFile.getText());
    input.setUseDynamicVNode(wUseDynamicVNode.getSelection());

    int nrRows = wFields.nrNonEmpty();        
    input.allocate(nrRows);      
    for (int i=0; i<nrRows; i++)
    {
        TableItem item = wFields.getNonEmpty(i);
        input.getFieldDatabase()[i]  = Const.NVL(item.getText(1), "");
        input.getFieldStream()[i]    = Const.NVL(item.getText(2), "");
    }

  }

  protected void ok() {
    stepname = wStepname.getText(); // return value
    getInfo(input);    
    dispose();
  }

  public BaseStepMeta getInput() {
    return input;
  }

  /**
   * Reads in the fields from the previous steps and from the ONE next step and opens an 
   * EnterMappingDialog with this information. After the user did the mapping, those information 
   * is put into the Select/Rename table.
   */
  private void generateMappings() {

      // Determine the source and target fields...
      //
      RowMetaInterface sourceFields;
      RowMetaInterface targetFields;

      try {
          sourceFields = transMeta.getPrevStepFields(stepMeta);
      } catch(KettleException e) {
          new ErrorDialog(shell, BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.DoMapping.UnableToFindSourceFields.Title"), 
              BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.DoMapping.UnableToFindSourceFields.Message"), e);
          return;
      }
      
      // refresh data
      input.setDatabaseMeta(transMeta.findDatabase(serverConnection.getText()) );
      input.setTablename(transMeta.environmentSubstitute(wTable.getText()));
      StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();
      try {
          targetFields = stepMetaInterface.getRequiredFields(transMeta);
      } catch (KettleException e) {
          new ErrorDialog(shell, BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.DoMapping.UnableToFindTargetFields.Title"), BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.DoMapping.UnableToFindTargetFields.Message"), e);
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

      int nrFields = wFields.nrNonEmpty();
      for (int i = 0; i < nrFields ; i++) {
          TableItem item = wFields.getNonEmpty(i);
          String source = item.getText(2);
          String target = item.getText(1);
          
          int sourceIndex = sourceFields.indexOfValue(source); 
          if (sourceIndex<0) {
              missingSourceFields.append(Const.CR + "   " + source+" --> " + target);
          }
          int targetIndex = targetFields.indexOfValue(target);
          if (targetIndex<0) {
              missingTargetFields.append(Const.CR + "   " + source+" --> " + target);
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
              message+=BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.DoMapping.SomeSourceFieldsNotFound", missingSourceFields.toString())+Const.CR;
          }
          if (missingTargetFields.length()>0) {
              message+=BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.DoMapping.SomeTargetFieldsNotFound", missingSourceFields.toString())+Const.CR;
          }
          message+=Const.CR;
          message+=BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.DoMapping.SomeFieldsNotFoundContinue")+Const.CR;
          MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
          boolean goOn = MessageDialog.openConfirm(shell, BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.DoMapping.SomeFieldsNotFoundTitle"), message);
          if (!goOn) {
              return;
          }
      }
      EnterMappingDialog d = new EnterMappingDialog(IngresVectorwiseLoaderDialog.this.shell, sourceFields.getFieldNames(), targetFields.getFieldNames(), mappings);
      mappings = d.open();

      // mappings == null if the user pressed cancel
      //
      if (mappings!=null) {
          // Clear and re-populate!
          //
          wFields.table.removeAll();
          wFields.table.setItemCount(mappings.size());
          for (int i = 0; i < mappings.size(); i++) {
              SourceToTargetMapping mapping = (SourceToTargetMapping) mappings.get(i);
              TableItem item = wFields.table.getItem(i);
              item.setText(2, sourceFields.getValueMeta(mapping.getSourcePosition()).getName());
              item.setText(1, targetFields.getValueMeta(mapping.getTargetPosition()).getName());
          }
          wFields.setRowNums();
          wFields.optWidth(true);
      }
  }

  private void sql()
  {
      try
      {
          IngresVectorwiseLoaderMeta info = new IngresVectorwiseLoaderMeta();
          getInfo(info);
          RowMetaInterface prev = transMeta.getPrevStepFields(stepname);
          StepMeta stepMeta = transMeta.findStep(stepname);
                  
          // Only use the fields that were specified.
          // 
          RowMetaInterface prevNew = new RowMeta();
               
          for (int i=0;i<info.getFieldDatabase().length;i++) 
          {
              ValueMetaInterface insValue = prev.searchValueMeta( info.getFieldStream()[i]); 
              if ( insValue != null )
              {
                  ValueMetaInterface insertValue = insValue.clone();
                  insertValue.setName(info.getFieldDatabase()[i]);
                  prevNew.addValueMeta( insertValue );
              }
              else  {
                  throw new KettleStepException(BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.FailedToFindField.Message", info.getFieldStream()[i]));  //$NON-NLS-1$
              }
          }
          prev = prevNew;
                      
          SQLStatement sql = info.getSQLStatements(transMeta, stepMeta, prev);
          if (!sql.hasError())
          {
              if (sql.hasSQL())
              {
                  SQLEditor sqledit = new SQLEditor(transMeta, shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(), sql.getSQL());
                  sqledit.open();
              }
              else
              {
                  MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
                  mb.setMessage(BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.NoSQL.DialogMessage"));
                  mb.setText(BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.NoSQL.DialogTitle"));
                  mb.open(); 
              }
          }
          else
          {
              MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
              mb.setMessage(sql.getError());
              mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
              mb.open(); 
          }
      }
      catch(KettleException ke)
      {
          new ErrorDialog(shell, BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.BuildSQLError.DialogTitle"), 
              BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.BuildSQLError.DialogMessage"), ke);
      }
  }

  private void get()
  {
      try
      {
          RowMetaInterface r = transMeta.getPrevStepFields(stepname);
          if (r!=null && !r.isEmpty())
          {
              BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1, 2}, new int[] {}, -1, -1, null);
          }
      }
      catch(KettleException ke)
      {
          new ErrorDialog(shell, 
                          BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.FailedToGetFields.DialogTitle"), 
                          BaseMessages.getString(PKG, "IngresVectorWiseLoaderDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
      }

  }   

}
