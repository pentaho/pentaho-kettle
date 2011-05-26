package org.pentaho.di.ui.trans.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.spoon.XulSpoonResourceBundle;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtBindingFactory;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

/**
 * Created by IntelliJ IDEA.
 * User: nbaker
 * Date: Jun 7, 2010
 * Time: 9:02:06 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseStepXulDialog extends AbstractXulEventHandler {
  private static Class<?> PKG = StepInterface.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject("Step dialog",
      LoggingObjectType.STEPDIALOG, null);

  protected static VariableSpace variables = new Variables();

  protected String stepname;

  protected XulLabel wlStepname;

  protected XulTextbox wStepname;

  protected XulButton wOK, wGet, wPreview, wSQL, wCreate, wCancel;

  protected Listener lsOK, lsGet, lsPreview, lsSQL, lsCreate, lsCancel;

  protected TransMeta transMeta;

  protected Shell parentShell;

  protected Listener lsResize;

  protected boolean changed, backupChanged;

  protected BaseStepMeta baseStepMeta;

  protected PropsUI props;

  protected Repository repository;

  protected StepMeta stepMeta;

  protected LogChannel log;

  protected static final int BUTTON_ALIGNMENT_CENTER = 0;

  protected static final int BUTTON_ALIGNMENT_LEFT = 1;

  protected static final int BUTTON_ALIGNMENT_RIGHT = 2;

  protected static int buttonAlignment = BUTTON_ALIGNMENT_CENTER;

  protected DatabaseDialog databaseDialog;

  private String xulFile;

  private XulDomContainer container;

  private XulRunner runner;

  protected XulDialog xulDialog;

  protected Shell dialogShell;

  protected BindingFactory bf = new SwtBindingFactory();

  static {
    // Get the button alignment
    buttonAlignment = getButtonAlignment();
  }

  public BaseStepXulDialog( String xulFile, Shell parent, BaseStepMeta baseStepMeta, TransMeta transMeta, String stepname ) {

    this.log = new LogChannel(baseStepMeta);
    this.transMeta = transMeta;
    this.stepname = stepname;
    this.stepMeta = transMeta.findStep(stepname);
    this.baseStepMeta = baseStepMeta;
    this.backupChanged = baseStepMeta.hasChanged();
    this.props = PropsUI.getInstance();
    this.xulFile = xulFile;
    this.parentShell = parent;

    try {
      initializeXul();
    } catch (Exception e) {
      e.printStackTrace();
      log.logError("Error initializing ("+stepname+") step dialog", e);
      throw new IllegalStateException("Cannot load dialog due to error in initialization", e);
    }
  }

  public BaseStepXulDialog( String xulFile, Shell parent, int nr, BaseStepMeta in, TransMeta tr) {
    this(xulFile, parent, in, tr, null);
  }

  private void initializeXul() throws XulException {
    SwtXulLoader loader = new SwtXulLoader();
    loader.registerClassLoader(getClass().getClassLoader());
    loader.setSettingsManager(XulSpoonSettingsManager.getInstance());
    loader.setOuterContext(parentShell);
    container = loader.loadXul( xulFile, new XulSpoonResourceBundle(getClassForMessages()));
    bf.setDocument(container.getDocumentRoot());
    
    for(XulEventHandler h : getEventHandlers()){
      container.addEventHandler(h); 
    }

    runner = new SwtXulRunner();
    runner.addContainer(container);

    // try and get the dialog
    xulDialog = (XulDialog) container.getDocumentRoot().getRootElement();
    dialogShell = (Shell) xulDialog.getRootObject();

    runner.initialize();
  }

  protected BindingFactory getBindingFactory(){
    return bf;
  }

  protected List<XulEventHandler> getEventHandlers(){
    return Collections.singletonList((XulEventHandler) this);
  }

  public String getName(){
    return "handler";
  }

  public String open(){
    xulDialog.show();
    return stepname;
  }

  public void close(){
    xulDialog.hide();
  }

  public abstract void onAccept();

  public abstract void onCancel();

  protected abstract Class<?> getClassForMessages();

  public void setShellImage( Shell shell, StepMetaInterface stepMetaInterface ) {
    try {
      String id = PluginRegistry.getInstance().getPluginId(StepPluginType.class, stepMetaInterface);
      if (getShell() != null && id != null) {
        getShell().setImage(GUIResource.getInstance().getImagesSteps().get(id));
      }
    } catch (Throwable e) {
    }
  }

  public void dispose() {
	Shell shell = (Shell)this.xulDialog.getRootObject();
	
	if(!shell.isDisposed()) {
	    WindowProperty winprop = new WindowProperty(shell);
	    props.setScreen(winprop);
	    ((Composite) this.xulDialog.getManagedObject()).dispose();
        shell.dispose();
    }
  }

  public Shell getShell() {
    return dialogShell;
  }

  /**
   * Set the shell size, based upon the previous time the geometry was saved in the Properties file.
   */
  public void setSize() {
    setSize(dialogShell);
  }


  /**
   * Returns the default alignment for the buttons. This is set in the
   * LAF properties with the key <code>Button_Position</code>.
   * The valid values are:<UL>
   * <LI><code>left</code>
   * <LI><code>center</code>
   * <LI><code>right</code>
   * </UL>
   * NOTE: if the alignment is not provided or contains an invalid value, <code>center</code>
   * will be used as a default
   *
   * @return a constant which indicates the button alignment
   */
  protected static int getButtonAlignment() {
    String buttonAlign = BasePropertyHandler.getProperty("Button_Position",
        "center").toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
    if ("center".equals(buttonAlign)) { //$NON-NLS-1$
      return BUTTON_ALIGNMENT_CENTER;
    } else if ("left".equals(buttonAlign)) { //$NON-NLS-1$
      return BUTTON_ALIGNMENT_LEFT;
    } else {
      return BUTTON_ALIGNMENT_RIGHT;
    }
  }


  public void addDatabases( XulMenuList<?> wConnection ) {
    addDatabases(wConnection, null);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void addDatabases( XulMenuList wConnection, Class<? extends DatabaseInterface> databaseType ) {
    List<String> databases = new ArrayList<String>();
    for (int i = 0; i < transMeta.nrDatabases(); i++) {
      DatabaseMeta ci = transMeta.getDatabase(i);
      if (databaseType == null || ci.getDatabaseInterface().getClass().equals(databaseType)) {
        databases.add(ci.getName());
      }
    }
    wConnection.setElements(databases);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void selectDatabase( XulMenuList wConnection, String name ) {
    wConnection.setSelectedItem(wConnection);
  }

  protected DatabaseDialog getDatabaseDialog( Shell shell ) {
    if (databaseDialog == null) {
      databaseDialog = new DatabaseDialog(shell);
    }
    return databaseDialog;
  }

  public void storeScreenSize() {
    props.setScreen(new WindowProperty(dialogShell));
  }

  /**
   * @return Returns the repository.
   */
  public Repository getRepository() {
    return repository;
  }

  /**
   * @param repository The repository to set.
   */
  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

  public static void setSize( Shell shell ) {
    setSize(shell, -1, -1, true);
  }

  public static void setSize( Shell shell, int minWidth, int minHeight, boolean packIt ) {
    PropsUI props = PropsUI.getInstance();

    WindowProperty winprop = props.getScreen(shell.getText());
    if (winprop != null) {
      winprop.setShell(shell, minWidth, minHeight);
    } else {
      if (packIt) {
        shell.pack();
      } else {
        shell.layout();
      }

      // OK, sometimes this produces dialogs that are waay too big.
      // Try to limit this a bit, m'kay?
      // Use the same algorithm by cheating :-)
      //
      winprop = new WindowProperty(shell);
      winprop.setShell(shell, minWidth, minHeight);

      // Now, as this is the first time it gets opened, try to put it in the middle of the screen...
      Rectangle shellBounds = shell.getBounds();
      Monitor monitor = shell.getDisplay().getPrimaryMonitor();
      if (shell.getParent() != null) {
        monitor = shell.getParent().getMonitor();
      }
      Rectangle monitorClientArea = monitor.getClientArea();

      int middleX = monitorClientArea.x + (monitorClientArea.width - shellBounds.width) / 2;
      int middleY = monitorClientArea.y + (monitorClientArea.height - shellBounds.height) / 2;

      shell.setLocation(middleX, middleY);
    }
  }


  /**
   * Gets unused fields from previous steps and inserts them as rows into a table view.
   *
   * @param row             the input fields
   * @param tableView       the table view to modify
   * @param keyColumn       the column in the table view to match with the names of the fields, checks for existance if >0
   * @param nameColumn      the column numbers in which the name should end up in
   * @param dataTypeColumn  the target column numbers in which the data type should end up in
   * @param lengthColumn    the length column where the length should end up in (if >0)
   * @param precisionColumn the length column where the precision should end up in (if >0)
   * @param listener        A listener that you can use to do custom modifications to the inserted table item, based on a value from the provided row
   */
  public static final void getFieldsFromPrevious(RowMetaInterface row, XulTree tableView, int keyColumn,
        int nameColumn[], int dataTypeColumn[], int lengthColumn, int precisionColumn, TableItemInsertListener listener) {
      if (row == null || row.size() == 0)
        return; // nothing to do

      Table table = ((TableViewer) tableView.getManagedObject()).getTable();

      // get a list of all the non-empty keys (names)
      //
      List<String> keys = new ArrayList<String>();
      for (int i = 0; i < table.getItemCount(); i++) {
        TableItem tableItem = table.getItem(i);
        String key = tableItem.getText(keyColumn);
        if (!Const.isEmpty(key) && keys.indexOf(key) < 0)
          keys.add(key);
      }

      int choice = 0;

      if (keys.size() > 0) {
        // Ask what we should do with the existing data in the step.
        //
        Shell shell = ((TableViewer) tableView.getManagedObject()).getTable().getShell();
        MessageDialog md = new MessageDialog(shell, BaseMessages.getString(PKG, "BaseStepDialog.GetFieldsChoice.Title"),//"Warning!"  //$NON-NLS-1$
            null, BaseMessages.getString(PKG, "BaseStepDialog.GetFieldsChoice.Message", "" + keys.size(), "" + row.size()), //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
            MessageDialog.WARNING, new String[] { BaseMessages.getString(PKG, "BaseStepDialog.AddNew"), //$NON-NLS-1$
                BaseMessages.getString(PKG, "BaseStepDialog.Add"), BaseMessages.getString(PKG, "BaseStepDialog.ClearAndAdd"), //$NON-NLS-1$  //$NON-NLS-2$
                BaseMessages.getString(PKG, "BaseStepDialog.Cancel"), }, 0); //$NON-NLS-1$
        MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
        int idx = md.open();
        choice = idx & 0xFF;
      }

      if (choice == 3 || choice == 255 /* 255 = escape pressed */)
        return; // Cancel clicked

      if (choice == 2) {
        tableView.getRootChildren().removeAll();
      }

      for (int i = 0; i < row.size(); i++) {
        ValueMetaInterface v = row.getValueMeta(i);

        boolean add = true;

        if (choice == 0) // hang on, see if it's not yet in the table view
        {
          if (keys.indexOf(v.getName()) >= 0)
            add = false;
        }

        if (add) {
          XulTreeRow tRow = tableView.getRootChildren().addNewRow();

          for (int c = 0; c < nameColumn.length; c++) {
            tRow.addCellText(nameColumn[c], Const.NVL(v.getName(), ""));
          }
          if ( dataTypeColumn != null )
          {
              for (int c = 0; c < dataTypeColumn.length; c++) {
                tRow.addCellText(dataTypeColumn[c], v.getTypeDesc());
              }
          }
          if (lengthColumn > 0) {
            if (v.getLength() >= 0)
                tRow.addCellText(lengthColumn, Integer.toString(v.getLength()));
          }
          if (precisionColumn > 0) {
            if (v.getPrecision() >= 0)
              tRow.addCellText(precisionColumn, Integer.toString(v.getPrecision()));
          }

          if (listener != null) {
            if (!listener.tableItemInserted(table.getItem(tRow.getParent().getParent().getChildNodes().indexOf(tRow.getParent())), v)) {
              tRow.getParent().getParent().removeChild(tRow.getParent());
            }
          }
        }
      }
//    tableView.removeEmptyRows();
//    tableView.setRowNums();
//    tableView.optWidth(true);
    }
    

  /**
   * Gets fields from previous steps and populate a ComboVar.
   *
   * @param comboVar  the comboVar to populate
   * @param TransMeta the source transformation
   * @param StepMeta  the source step
   */
  public static final void getFieldsFromPrevious( ComboVar comboVar, TransMeta transMeta, StepMeta stepMeta ) {
    String selectedField = null;
    int indexField = -1;
    try {
      RowMetaInterface r = transMeta.getPrevStepFields(stepMeta);
      selectedField = comboVar.getText();
      comboVar.removeAll();

      if (r != null && !r.isEmpty()) {
        r.getFieldNames();
        comboVar.setItems(r.getFieldNames());
        indexField = r.indexOfValue(selectedField);
      }
      // Select value if possible...
      if (indexField > -1) {
        comboVar.select(indexField);
      } else {
        if (selectedField != null) {
          comboVar.setText(selectedField);
        }
      }
      ;
    } catch (KettleException ke) {
      new ErrorDialog(comboVar.getShell(),
          BaseMessages.getString(PKG, "BaseStepDialog.FailedToGetFieldsPrevious.DialogTitle"),
          BaseMessages.getString(PKG, "BaseStepDialog.FailedToGetFieldsPrevious.DialogMessage"), ke);
    }
  }

  /**
   * Create a new field mapping between source and target steps.
   *
   * @param shell        the shell of the parent window
   * @param sourceFields the source fields
   * @param targetFields the target fields
   * @param fieldMapping the list of source to target mappings to default to (can be empty but not null)
   * @throws KettleException in case something goes wrong during the field mapping
   */
  public static final void generateFieldMapping( Shell shell, RowMetaInterface sourceFields,
                                                 RowMetaInterface targetFields,
                                                 java.util.List<SourceToTargetMapping> fieldMapping ) throws
                                                                                                      KettleException {
    // Build the mapping: let the user decide!!
    String[] source = sourceFields.getFieldNames();
    for (int i = 0; i < source.length; i++) {
      ValueMetaInterface v = sourceFields.getValueMeta(i);
      source[i] += EnterMappingDialog.STRING_ORIGIN_SEPARATOR + v.getOrigin() + ")";
    }
    String[] target = targetFields.getFieldNames();

    EnterMappingDialog dialog = new EnterMappingDialog(shell, source, target, fieldMapping);
    java.util.List<SourceToTargetMapping> newMapping = dialog.open();
    if (newMapping != null) {
      fieldMapping.clear();
      fieldMapping.addAll(newMapping);
    }
  }

  public boolean isBasic() {
    return log.isBasic();
  }

  public boolean isDetailed() {
    return log.isDetailed();
  }

  public boolean isDebug() {
    return log.isDebug();
  }

  public boolean isRowLevel() {
    return log.isRowLevel();
  }

  public void logMinimal( String message ) {
    log.logMinimal(message);
  }

  public void logMinimal( String message, Object... arguments ) {
    log.logMinimal(message, arguments);
  }

  public void logBasic( String message ) {
    log.logBasic(message);
  }

  public void logBasic( String message, Object... arguments ) {
    log.logBasic(message, arguments);
  }

  public void logDetailed( String message ) {
    log.logDetailed(message);
  }

  public void logDetailed( String message, Object... arguments ) {
    log.logDetailed(message, arguments);
  }

  public void logDebug( String message ) {
    log.logDebug(message);
  }

  public void logDebug( String message, Object... arguments ) {
    log.logDebug(message, arguments);
  }

  public void logRowlevel( String message ) {
    log.logRowlevel(message);
  }

  public void logRowlevel( String message, Object... arguments ) {
    log.logRowlevel(message, arguments);
  }

  public void logError( String message ) {
    log.logError(message);
  }

  public void logError( String message, Throwable e ) {
    log.logError(message, e);
  }

  public void logError( String message, Object... arguments ) {
    log.logError(message, arguments);
  }


  public static void getFieldsFromPrevious( RowMetaInterface row, XulTree tableView, List<Object> fields,
                                            StepTableDataObject field, TableItemInsertXulListener listener ) {
    if (row == null || row.size() == 0)
        return; // nothing to do

      // get a list of all the non-empty keys (names)
      //
      List<String> keys = new ArrayList<String>();
      for(Object entry : fields){
        keys.add(((StepTableDataObject) entry).getName());
      }

      int choice = 0;

      if (keys.size() > 0) {
        // Ask what we should do with the existing data in the step.
        //
        Shell shell = ((TableViewer) tableView.getManagedObject()).getTable().getShell();
        MessageDialog md = new MessageDialog(shell, BaseMessages.getString(PKG, "BaseStepDialog.GetFieldsChoice.Title"),//"Warning!"  //$NON-NLS-1$
            null, BaseMessages.getString(PKG, "BaseStepDialog.GetFieldsChoice.Message", "" + keys.size(), "" + row.size()), //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
            MessageDialog.WARNING, new String[] { BaseMessages.getString(PKG, "BaseStepDialog.AddNew"), //$NON-NLS-1$
                BaseMessages.getString(PKG, "BaseStepDialog.Add"), BaseMessages.getString(PKG, "BaseStepDialog.ClearAndAdd"), //$NON-NLS-1$  //$NON-NLS-2$
                BaseMessages.getString(PKG, "BaseStepDialog.Cancel"), }, 0); //$NON-NLS-1$
        MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
        int idx = md.open();
        choice = idx & 0xFF;
      }

      if (choice == 3 || choice == 255 /* 255 = escape pressed */)
        return; // Cancel clicked

      if (choice == 2) {
        fields.clear();
      }

      for (int i = 0; i < row.size(); i++) {
        ValueMetaInterface v = row.getValueMeta(i);

        if (choice == 0) // hang on, see if it's not yet in the table view
        {
          if (keys.indexOf(v.getName()) >= 0)
            continue;
        }

        if (listener != null && !listener.tableItemInsertedFor(v)) {
          continue;
        }

        StepTableDataObject newField = field.createNew(v);
        fields.add(newField);
      }

  }
}