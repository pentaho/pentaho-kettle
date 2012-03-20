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

package org.pentaho.di.ui.imp;

import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.imp.ImportRules;
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.imp.rule.ImportRuleCompositeInterface;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.XulSpoonResourceBundle;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class ImportRulesDialog extends Dialog implements XulEventHandler {

  private static Class<?> PKG = ImportRulesDialog.class; // for i18n

  private static final String XUL_FILE_TOOLBAR = "ui/import-rules-toolbar.xul"; //$NON-NLS-1$

  private Shell parentShell;
  private Display display;
  private Shell shell;
  private PropsUI props;

  private XulToolbar toolbar;

  private boolean ok;
  private ImportRules importRules;
  
  private Button wOK, wExport, wImport, wCancel;

  private Table table;

  private List<ImportRuleCompositeInterface> compositesList;

  private ImportRules originalRules;

  public ImportRulesDialog(Shell parentShell, ImportRules importRules) {
    super(parentShell);
    this.parentShell = parentShell;
    this.originalRules = importRules;
    this.importRules = importRules.clone();
    this.props = PropsUI.getInstance();
    this.display = parentShell.getDisplay();
  }

  public boolean open() {
    
    shell = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    shell.setImage((Image) GUIResource.getInstance().getImageLogoSmall());
    
    FormLayout formLayout = new FormLayout ();
    formLayout.marginWidth  = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "ImportRulesDialog.Shell.Title")); //$NON-NLS-1$

    // Add the buttons at the very bottom
    //
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString("System.Button.OK"));
    wOK.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { ok(); } });
    wImport = new Button(shell, SWT.PUSH);
    wImport.setText(BaseMessages.getString(PKG, "ImportRulesDialog.Button.Import"));
    wImport.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { importRules(); } });
    wExport = new Button(shell, SWT.PUSH);
    wExport.setText(BaseMessages.getString(PKG, "ImportRulesDialog.Button.Export"));
    wExport.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { exportRules(); } });
    wCancel= new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString("System.Button.Cancel"));
    wCancel.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { cancel(); } });

    BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wImport, wExport, wCancel, }, Const.MARGIN, null);

    // Put a toolbar at the very top...
    //
    addToolBar();
    
    Control toolbarControl = (Control) toolbar.getManagedObject();
    props.setLook(toolbarControl);
    
    toolbarControl.setLayoutData(new FormData());
    FormData fdToolbar = new FormData();
    fdToolbar.left = new FormAttachment(0, 0); // First one in the left top corner
    fdToolbar.top = new FormAttachment(0, 0);
    fdToolbar.right = new FormAttachment(100, 0);
    toolbarControl.setLayoutData(fdToolbar);
    toolbarControl.setParent(shell);

    
    table = new Table(shell, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
    props.setLook(table);
    table.setHeaderVisible(true);
    
    // Enabled? column
    //
    TableColumn enabledColumn = new TableColumn(table, SWT.NONE);
    enabledColumn.setText("On?");
    enabledColumn.setWidth(50);

    // Rule description column
    //
    TableColumn descriptionColumn = new TableColumn(table, SWT.NONE);
    descriptionColumn.setText("Rule description");
    descriptionColumn.setWidth(400);

    // Rule description column
    //
    TableColumn compositeColumn = new TableColumn(table, SWT.NONE);
    compositeColumn.setText("Settings");
    compositeColumn.setWidth(500);

    
    
    FormData fdTable = new FormData();
    fdTable.top = new FormAttachment(toolbarControl,Const.MARGIN);
    fdTable.left = new FormAttachment(0,0);
    fdTable.right = new FormAttachment(100,0);
    fdTable.bottom = new FormAttachment(wOK, -Const.MARGIN*2);
    table.setLayoutData(fdTable);
    
    getCompositesData();
    
    table.layout(true,true);

    BaseStepDialog.setSize(shell);

    shell.open();
    while (!shell.isDisposed())
    {
        if (!display.readAndDispatch()) display.sleep();
    }

    return ok;
  }
  
  private void addToolBar() {

    try {
      XulLoader loader = new SwtXulLoader();
      loader.setSettingsManager(XulSpoonSettingsManager.getInstance());
      ResourceBundle bundle = new XulSpoonResourceBundle(Spoon.class);
      XulDomContainer xulDomContainer = loader.loadXul(XUL_FILE_TOOLBAR, bundle);
      xulDomContainer.addEventHandler(this);
      toolbar = (XulToolbar) xulDomContainer.getDocumentRoot().getElementById("import-rules-toolbar"); //$NON-NLS-1$

      ToolBar swtToolbar = (ToolBar) toolbar.getManagedObject();
      swtToolbar.layout(true, true);
    } catch (Throwable t) {
      LogChannel.GENERAL.logError(Const.getStackTracker(t));
      new ErrorDialog(shell, 
          BaseMessages.getString(PKG, "ImportRulesDialog.Exception.ErrorReadingXULFile.Title"), //$NON-NLS-1$ 
          BaseMessages.getString(PKG, "ImportRulesDialog.Exception.ErrorReadingXULFile.Message", XUL_FILE_TOOLBAR), new Exception(t)); //$NON-NLS-1$
    }
  }

  
  public void addRule() {
    PluginRegistry registry = PluginRegistry.getInstance();
    List<PluginInterface> plugins = registry.getPlugins(ImportRulePluginType.class);
    
    // Loop over the rules in the list and get rid of the ones that are unique in the plugins list.
    //
    for (ImportRuleInterface rule : importRules.getRules()) {
      if (rule.isUnique()) {
        
        int removeIndex=-1;
        for (int i=0;i<plugins.size();i++) {
          PluginInterface plugin = plugins.get(i);
          
          if (Const.indexOfString(rule.getId(), plugin.getIds())>=0) {
            removeIndex=i;
            break;
          }
        }
        
        if (removeIndex>=0) {
          plugins.remove(removeIndex);
        }
      }
    }
    
    // Those that are left can be presented to the user.
    //
    if (plugins.size()>0) {
      String names[] = new String[plugins.size()];
      for (int i=0;i<plugins.size();i++) {
        names[i] = plugins.get(i).getName()+" : "+plugins.get(i).getDescription();
      }
      
      EnterSelectionDialog esd = new EnterSelectionDialog(shell, names, "Select a rule", "Select a new rule to add to the list:");
      String name = esd.open();
      if (name!=null) {
        try {
          int index = Const.indexOfString(name, names);
          PluginInterface plugin = plugins.get(index);
          ImportRuleInterface rule = (ImportRuleInterface) registry.loadClass(plugin);
          rule.setEnabled(true);
          rule.setId(plugin.getIds()[0]);

          ImportRules newRules = new ImportRules();
          getInfo(newRules);
          
          newRules.getRules().add(rule);
          importRules = newRules;

          // Refresh the whole list..
          //
          getCompositesData();
          
        } catch(Exception e) {
          new ErrorDialog(shell, "Error", "Error loading rule class", e);
        }
      }
    }
  }
  
  public void removeRule() {
    MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.APPLICATION_MODAL | SWT.SHEET| SWT.YES | SWT.NO);
    box.setText("Warning");
    box.setMessage("Are you sure you want to remove the selected rules from the list?");
    int answer = box.open();
    if (answer!=SWT.YES){
      return;
    }

    int[] indices = table.getSelectionIndices();
    Arrays.sort(indices);
    
    for (int i=indices.length-1;i>=0;i--) {
      importRules.getRules().remove(indices[i]);
    }
    
    // Refresh the whole list..
    //
    getCompositesData();
  }
  
  /**
   * Save the rules to an XML file
   */
  protected void exportRules() {
    FileDialog dialog = new FileDialog(shell, SWT.SAVE);
    dialog.setFilterExtensions(new String[] { "*.xml;*.XML", "*" });
    dialog.setFilterNames(new String[] { BaseMessages.getString(PKG, "System.FileType.XMLFiles"), BaseMessages.getString(PKG, "System.FileType.AllFiles") });
    if (dialog.open() != null)
    {
      String filename = dialog.getFilterPath() + System.getProperty("file.separator") + dialog.getFileName();
      
      FileWriter fileWriter = null;
      try {
        fileWriter = new FileWriter(filename);
        fileWriter.write(XMLHandler.getXMLHeader());
        fileWriter.write(importRules.getXML());
      } catch(Exception e) {
        new ErrorDialog(shell, "Error", "There was an error while exporting to file '"+filename+"'", e);
      } finally {
        try { fileWriter.close();
        } catch( Exception e) {
          new ErrorDialog(shell, "Error", "There was an error closing file '"+filename+"'", e);
        }
      }
      
    }
  }

  /**
   * Import the rules from an XML rules file...
   */
  protected void importRules() {
    if (!importRules.getRules().isEmpty()) {
      MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.APPLICATION_MODAL| SWT.SHEET | SWT.YES | SWT.NO);
      box.setText("Warning");
      box.setMessage("Are you sure you want to load a new set of rules, replacing the current list?");
      int answer = box.open();
      if (answer!=SWT.YES){
        return;
      }
    }
    
    FileDialog dialog = new FileDialog(shell, SWT.OPEN);
    dialog.setFilterExtensions(new String[] { "*.xml;*.XML", "*" });
    dialog.setFilterNames(new String[] { BaseMessages.getString(PKG, "System.FileType.XMLFiles"), BaseMessages.getString(PKG, "System.FileType.AllFiles") });
    if (dialog.open() != null)
    {
      String filename = dialog.getFilterPath() + System.getProperty("file.separator") + dialog.getFileName();
      
      ImportRules newRules = new ImportRules();
      try {
        newRules.loadXML(XMLHandler.getSubNode(XMLHandler.loadXMLFile(filename), ImportRules.XML_TAG));
        importRules = newRules;
        
        // Re-load the dialog.
        //
        getCompositesData();
        
      } catch(Exception e) {
        new ErrorDialog(shell, "Error", "There was an error during the import of the import rules file, verify the XML format.", e);
      }
      
    }
  }

  protected void dispose() {
    shell.dispose();
  }
  
  protected void cancel() {
    ok=false;
    dispose();
  }
  
  protected void ok() {
    ok=true;
    getInfo(originalRules);
    dispose();
  }

  protected void getInfo(ImportRules ir) {
    ir.getRules().clear();
    
    for (int i=0;i<importRules.getRules().size();i++) {
      ImportRuleInterface rule = importRules.getRules().get(i);
      ImportRuleCompositeInterface importRuleComposite = compositesList.get(i);
      TableItem tableItem = table.getItem(i);

      importRuleComposite.getCompositeData(rule);
      rule.setEnabled(tableItem.getChecked());
      
      ir.getRules().add(rule);
    }
  }
  
  protected void getCompositesData() {
    
    for (TableItem item : table.getItems()) {
      item.dispose();
    }
    table.clearAll();
    
    // Fill the table items in the table with data from importRules:
    //
    compositesList = new ArrayList<ImportRuleCompositeInterface>();
    for (ImportRuleInterface rule : importRules.getRules()) {
      try {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setChecked(rule.isEnabled());
        
        PluginRegistry registry = PluginRegistry.getInstance();
        PluginInterface plugin = registry.getPlugin(ImportRulePluginType.class, rule);
        
        item.setText(1, Const.NVL(plugin.getName(), rule.getClass().getName()));
        
        // Put a composite in the 3rd column...
        //
        // First get the composite generating class...
        //
        ImportRuleCompositeInterface importRuleComposite = getImportRuleComposite(rule);
        compositesList.add(importRuleComposite); 
        final Composite composite = importRuleComposite.getComposite(table, rule);
        composite.layout(true, true);
        
        final TableEditor editor = new TableEditor(table);
        editor.grabHorizontal=true;
        editor.grabVertical=true;
        editor.setEditor(composite, item, 2);
        
        // Put actual data onto the composite
        //
        importRuleComposite.setCompositeData(rule);
        
        item.addDisposeListener(new DisposeListener() { public void widgetDisposed(DisposeEvent event) { composite.dispose(); } });

      } catch(Exception e) {
        new ErrorDialog(shell, "Error", "Error displaying rule options for rule: "+rule.toString(), e);
        compositesList.add(null);
      }
    }
  }
  
  public ImportRuleCompositeInterface getImportRuleComposite(ImportRuleInterface rule) throws KettleException
  {
    String compositeClassName = rule.getCompositeClassName();

    Class<?> compositeClass;
    Class<?>[] paramClasses = new Class[] { };
    Object[] paramArgs = new Object[] {  };
    Constructor<?> compositeConstructor;
    try
    {
      compositeClass = rule.getClass().getClassLoader().loadClass(compositeClassName);
      compositeConstructor = compositeClass.getConstructor(paramClasses);
      return (ImportRuleCompositeInterface) compositeConstructor.newInstance(paramArgs);
    } catch (Exception e)
    {
      throw new KettleException(e);
    }

  }

  
  public static void main(String[] args) throws Exception {

    Display display = new Display();
    
    KettleEnvironment.init();
    PropsUI.init(display, PropsUI.TYPE_PROPERTIES_SPOON);
    
    Shell shell = new Shell(display);
    
    ImportRules importRules = new ImportRules();
    importRules.loadXML(XMLHandler.getSubNode(XMLHandler.loadXMLFile("bin/import-rules.xml"), ImportRules.XML_TAG));
    
    ImportRulesDialog dialog = new ImportRulesDialog(shell, importRules);
    if (dialog.open()) {
      for (ImportRuleInterface rule : importRules.getRules()) {
        System.out.println(" - "+rule.toString());
      }
    }
  }







  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getData()
   */
  public Object getData() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getName()
   */
  public String getName() {
    return "importRules"; //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getXulDomContainer()
   */
  public XulDomContainer getXulDomContainer() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setData(java.lang.Object)
   */
  public void setData(Object data) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setName(java.lang.String)
   */
  public void setName(String name) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setXulDomContainer(org.pentaho.ui.xul.XulDomContainer)
   */
  public void setXulDomContainer(XulDomContainer xulDomContainer) {
    // TODO Auto-generated method stub

  }
}
