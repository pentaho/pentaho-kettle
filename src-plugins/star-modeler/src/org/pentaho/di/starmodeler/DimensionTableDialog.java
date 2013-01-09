 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.starmodeler;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TableType;
import org.pentaho.pms.schema.concept.DefaultPropertyID;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Allows you to edit the Job settings.  Just pass a JobInfo object.
 * 
 * @author Matt Casters
 * @since  02-jul-2003
 */
public class DimensionTableDialog extends Dialog
{
	private static Class<?> PKG = DimensionTableDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;

	private CTabItem     wTableTab, wTablesTab;

	private PropsUI      props;
		
	private Label        wlTableName;
	private Text         wTableName;

	private Label        wlTableDescription;
	private Text         wTableDescription;;

	private CCombo       wDimensionType;

  private Label        wlPhysicalName;
  private Text         wPhysicalName;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private LogicalTable logicalTable;
	private Shell  shell;
	
	private SelectionAdapter lsDef;
	    
  // fields tab
	private TableView    wAttributes;

	private int middle;
	private int margin;

  private String locale;


	public DimensionTableDialog(Shell parent, LogicalTable logicalTable, String locale) {
		super(parent, SWT.DIALOG_TRIM);
		this.logicalTable=logicalTable;
		this.props=PropsUI.getInstance();
		this.locale = locale;
	}
	
	public LogicalTable open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);
		shell.setImage((Image) GUIResource.getInstance().getImageLogoSmall());
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "DimensionTableDialog.ShellText"));

		middle = props.getMiddlePct();
		margin = Const.MARGIN;
		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
		
		addTableTab();
		addAttributesTab();

		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(0, 0);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		//BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wSQL, wCancel }, margin, wSharedObjectsFile);
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, Const.MARGIN, null);
		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wTableName.addSelectionListener( lsDef );
    wTableDescription.addSelectionListener( lsDef );
    wPhysicalName.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wTabFolder.setSelection(0);
		getData();
		BaseStepDialog.setSize(shell);		

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return logicalTable;
	}


	private void addTableTab()
	{
		wTableTab=new CTabItem(wTabFolder, SWT.NONE);
		wTableTab.setText(BaseMessages.getString(PKG, "DimensionTableDialog.TableTab.Label")); //$NON-NLS-1$
        
		Composite wTableComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wTableComp);

		FormLayout transLayout = new FormLayout();
		transLayout.marginWidth  = Const.MARGIN;
		transLayout.marginHeight = Const.MARGIN;
		wTableComp.setLayout(transLayout);

		// Table  name:
		//
		wlTableName=new Label(wTableComp, SWT.RIGHT);
		wlTableName.setText(BaseMessages.getString(PKG, "DimensionTableDialog.TableName.Label"));
		props.setLook(wlTableName);
		FormData fdlTableName = new FormData();
		fdlTableName.left = new FormAttachment(0, 0);
		fdlTableName.right= new FormAttachment(middle, -margin);
		fdlTableName.top  = new FormAttachment(0, margin);
		wlTableName.setLayoutData(fdlTableName);
		wTableName=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTableName);
		FormData fdTableName = new FormData();
		fdTableName.left = new FormAttachment(middle, 0);
		fdTableName.top  = new FormAttachment(0, margin);
		fdTableName.right= new FormAttachment(100, 0);
		wTableName.setLayoutData(fdTableName);
		Control lastControl = wTableName;

		// Table description
		//
		wlTableDescription=new Label(wTableComp, SWT.RIGHT);
		wlTableDescription.setText(BaseMessages.getString(PKG, "DimensionTableDialog.TableDescription.Label"));
		props.setLook(wlTableDescription);
		FormData fdlTableDescription = new FormData();
		fdlTableDescription.left = new FormAttachment(0, 0);
		fdlTableDescription.right= new FormAttachment(middle, -margin);
		fdlTableDescription.top  = new FormAttachment(lastControl, margin);
		wlTableDescription.setLayoutData(fdlTableDescription);
		wTableDescription=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTableDescription);
		FormData fdTableDescription = new FormData();
		fdTableDescription.left = new FormAttachment(middle, 0);
		fdTableDescription.top  = new FormAttachment(lastControl, margin);
		fdTableDescription.right= new FormAttachment(100, 0);
		wTableDescription.setLayoutData(fdTableDescription);
		lastControl = wTableDescription;
    
    // Dimension type
    //
    Label wlDimensionType = new Label(wTableComp, SWT.RIGHT);
    wlDimensionType.setText(BaseMessages.getString(PKG, "DimensionTableDialog.DimensionType.Label"));
    props.setLook(wlDimensionType);
    FormData fdlDimensionType = new FormData();
    fdlDimensionType.left = new FormAttachment(0, 0);
    fdlDimensionType.right= new FormAttachment(middle, -margin);
    fdlDimensionType.top  = new FormAttachment(lastControl, margin);
    wlDimensionType.setLayoutData(fdlDimensionType);
    wDimensionType=new CCombo(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wDimensionType);
    FormData fdDimensionType = new FormData();
    fdDimensionType.left = new FormAttachment(middle, 0);
    fdDimensionType.top  = new FormAttachment(lastControl, margin);
    fdDimensionType.right= new FormAttachment(100, 0);
    wDimensionType.setLayoutData(fdDimensionType);
    for (DimensionType type : DimensionType.values()) {
      wDimensionType.add(type.name());
    }
    lastControl = wDimensionType;

    // Table description
    //
    wlPhysicalName=new Label(wTableComp, SWT.RIGHT);
    wlPhysicalName.setText(BaseMessages.getString(PKG, "DimensionTableDialog.PhysicalName.Label"));
    props.setLook(wlPhysicalName);
    FormData fdlPhysicalName = new FormData();
    fdlPhysicalName.left = new FormAttachment(0, 0);
    fdlPhysicalName.right= new FormAttachment(middle, -margin);
    fdlPhysicalName.top  = new FormAttachment(lastControl, margin);
    wlPhysicalName.setLayoutData(fdlPhysicalName);
    wPhysicalName=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wPhysicalName);
    FormData fdPhysicalName = new FormData();
    fdPhysicalName.left = new FormAttachment(middle, 0);
    fdPhysicalName.top  = new FormAttachment(lastControl, margin);
    fdPhysicalName.right= new FormAttachment(100, 0);
    wPhysicalName.setLayoutData(fdPhysicalName);
    lastControl = wPhysicalName;

    
		FormData fdTableComp = new FormData();
		fdTableComp.left  = new FormAttachment(0, 0);
		fdTableComp.top   = new FormAttachment(0, 0);
		fdTableComp.right = new FormAttachment(100, 0);
		fdTableComp.bottom= new FormAttachment(100, 0);

		wTableComp.setLayoutData(fdTableComp);
		wTableTab.setControl(wTableComp);
	}

    private void addAttributesTab()
    {
        wTablesTab=new CTabItem(wTabFolder, SWT.NONE);
        wTablesTab.setText(BaseMessages.getString(PKG, "DimensionTableDialog.AttributesTab.Label")); //$NON-NLS-1$

        FormLayout paramLayout = new FormLayout ();
        paramLayout.marginWidth  = Const.MARGIN;
        paramLayout.marginHeight = Const.MARGIN;
        
        Composite wTablesComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wTablesComp);
        wTablesComp.setLayout(paramLayout);
        
        Button wAddDefaultFields = new Button(wTablesComp, SWT.PUSH);
        wAddDefaultFields.setText(BaseMessages.getString(PKG, "DimensionTableDialog.AddDefaultFieldsButton.Label"));
        BaseStepDialog.positionBottomButtons(wTablesComp, new Button[] { wAddDefaultFields, }, margin, null);
        wAddDefaultFields.addSelectionListener(new SelectionAdapter() {  public void widgetSelected(SelectionEvent e) { addDefaultAttributes(); }});

        Label wlAtrributes = new Label(wTablesComp, SWT.RIGHT);
        wlAtrributes.setText(BaseMessages.getString(PKG, "DimensionTableDialog.Attributes.Label")); //$NON-NLS-1$
        props.setLook(wlAtrributes);
        FormData fdlAttributes = new FormData();
        fdlAttributes.left = new FormAttachment(0, 0);
        fdlAttributes.top  = new FormAttachment(0, 0);
        wlAtrributes.setLayoutData(fdlAttributes);
        
        final int FieldsRows=logicalTable.getLogicalColumns().size();
        
        List<DatabaseMeta> sharedDatabases= SharedDatabaseUtil.loadSharedDatabases();
        String[] databaseNames = SharedDatabaseUtil.getSortedDatabaseNames(sharedDatabases);
        
        // data types
        //
        String[] dataTypes = new String[DataType.values().length];
        for (int i=0;i<dataTypes.length;i++) {
          dataTypes[i] = DataType.values()[i].name();
        }

        // field types
        //
        String[] attributeTypes = new String[AttributeType.values().length];
        for (int i=0;i<AttributeType.values().length;i++) {
          attributeTypes[i] = AttributeType.values()[i].name();
        }

        
        //  name, description, field type, physical column name, data type, length, precision, source db, source table, source column, conversion remarks 
        //
        ColumnInfo[] colinf=new ColumnInfo[] {
            new ColumnInfo(BaseMessages.getString(PKG, "DimensionTableDialog.ColumnInfo.Name.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
            new ColumnInfo(BaseMessages.getString(PKG, "DimensionTableDialog.ColumnInfo.Description.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
            new ColumnInfo(BaseMessages.getString(PKG, "DimensionTableDialog.ColumnInfo.FieldType.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO, attributeTypes), //$NON-NLS-1$
            new ColumnInfo(BaseMessages.getString(PKG, "DimensionTableDialog.ColumnInfo.PhysicalName.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
            new ColumnInfo(BaseMessages.getString(PKG, "DimensionTableDialog.ColumnInfo.DataType.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO, dataTypes), //$NON-NLS-1$
            new ColumnInfo(BaseMessages.getString(PKG, "DimensionTableDialog.ColumnInfo.DataLength.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   true), //$NON-NLS-1$
            new ColumnInfo(BaseMessages.getString(PKG, "DimensionTableDialog.ColumnInfo.DataPrecision.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   true), //$NON-NLS-1$
            new ColumnInfo(BaseMessages.getString(PKG, "DimensionTableDialog.ColumnInfo.SourceDatabase.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO, databaseNames), //$NON-NLS-1$
            new ColumnInfo(BaseMessages.getString(PKG, "DimensionTableDialog.ColumnInfo.SourceTable.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
            new ColumnInfo(BaseMessages.getString(PKG, "DimensionTableDialog.ColumnInfo.SourceColumn.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
            new ColumnInfo(BaseMessages.getString(PKG, "DimensionTableDialog.ColumnInfo.ConversionLogicRemarks.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
        };
        
        wAttributes=new TableView(new Variables(), wTablesComp, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
                              colinf, 
                              FieldsRows,  
                              null,
                              props
                              );
       
        FormData fdTables = new FormData();
        fdTables.left  = new FormAttachment(0, 0);
        fdTables.top   = new FormAttachment(wlAtrributes, margin);
        fdTables.right = new FormAttachment(100, 0);
        fdTables.bottom= new FormAttachment(wAddDefaultFields, -margin*2);
        wAttributes.setLayoutData(fdTables);

        FormData fdTablesComp = new FormData();
        fdTablesComp.left  = new FormAttachment(0, 0);
        fdTablesComp.top   = new FormAttachment(0, 0);
        fdTablesComp.right = new FormAttachment(100, 0);
        fdTablesComp.bottom= new FormAttachment(100, 0);
        wTablesComp.setLayoutData(fdTablesComp);
        
        wTablesComp.layout();
        wTablesTab.setControl(wTablesComp);
    }	

    
    
    
	protected void addDefaultAttributes() {
	  try {
      DimensionType dimensionType = DimensionType.getDimensionType(wDimensionType.getText());
      switch(dimensionType) {
      case TIME:
        addAttributesFromFile("/org/pentaho/di/resources/default-time-attributes.xml");
        break;
      case DATE:
        addAttributesFromFile("/org/pentaho/di/resources/default-date-attributes.xml");
        break;
      case JUNK_DIMENSION:
        addAttribute("Dimension TK", "Technical/surrogate key", "???_tk", AttributeType.TECHNICAL_KEY, DataType.NUMERIC, 9, 0, "Generated by PDI");
        break;
      case SLOWLY_CHANGING_DIMENSION:
        addAttribute("Dimension TK", "Technical/surrogate key", "???_tk", AttributeType.TECHNICAL_KEY, DataType.NUMERIC, 9, 0, "Generated by PDI");
        addAttribute("Version", "The dimension version number (1, ...)", "version", AttributeType.VERSION_FIELD, DataType.NUMERIC, 4, 0, "Generated by PDI");
        addAttribute("Date From", "The start date of the dimension validity range", "date_from", AttributeType.DATE_START, DataType.DATE, -1, -1, "Generated by PDI");
        addAttribute("Date To", "The start date of the dimension validity range", "date_to", AttributeType.DATE_END, DataType.DATE, -1, -1, "Generated by PDI");
        addAttribute("Natural Key", "Natural key of the dimension", "???_id", AttributeType.NATURAL_KEY, DataType.NUMERIC, 9, -1, "Copy from source table");
        break;
      case OTHER:
        break;
      }
        
	  } catch(Exception e)  {
	    new ErrorDialog(shell, 
	        BaseMessages.getString(PKG, "DimensionTableDialog.ExceptionAddingDefaultAttributes.Title"), 
	        BaseMessages.getString(PKG, "DimensionTableDialog.ExceptionAddingDefaultAttributes.Message"), 
	        e
	     );
	  }
	  wAttributes.removeEmptyRows();
	  wAttributes.setRowNums();
	  wAttributes.optWidth(true);
  }

  private void addAttributesFromFile(String filename) throws KettleException {
    InputStream inputStream = getClass().getResourceAsStream(filename);
    Document document = XMLHandler.loadXMLFile(inputStream);
    Node attributesNode = XMLHandler.getSubNode(document, "attributes");
    List<Node> attributeNodes = XMLHandler.getNodes(attributesNode, "attribute");
    for (Node node : attributeNodes) {
      String name = XMLHandler.getTagValue(node, "name");
      String description = XMLHandler.getTagValue(node, "description");
      String phName = XMLHandler.getTagValue(node, "physicalname");
      AttributeType attributeType= AttributeType.getAttributeType(XMLHandler.getTagValue(node, "attribute_type"));
      DataType dataType= ConceptUtil.getDataType(XMLHandler.getTagValue(node, "data_type"));
      int length = Const.toInt(XMLHandler.getTagValue(node, "length"), -1);
      int precision = Const.toInt(XMLHandler.getTagValue(node, "precision"), -1);
      // String sourceDb = XMLHandler.getTagValue(node, "source_db");
      // String sourceTable = XMLHandler.getTagValue(node, "source_table");
      // String sourceColumn = XMLHandler.getTagValue(node, "source_column");
      String remarks = XMLHandler.getTagValue(node, "remarks");
      addAttribute(name, description, phName, attributeType, dataType, length, precision, remarks);
    }
    
  }

  private void addAttribute(String name, String description, String phName, AttributeType attributeType, DataType dataType, int length, int precision, String comment) {
    LogicalColumn column = new LogicalColumn();
    column.setLogicalTable(logicalTable);
    column.setName(new LocalizedString(locale, name));
    column.setDescription(new LocalizedString(locale, description));
    column.setDataType(dataType);
    column.setProperty(DefaultIDs.LOGICAL_COLUMN_ATTRIBUTE_TYPE, attributeType.name());
    column.setProperty(DefaultIDs.LOGICAL_COLUMN_PHYSICAL_COLUMN_NAME, phName);
    if (length>=0) column.setProperty(DefaultIDs.LOGICAL_COLUMN_LENGTH, Integer.toString(length));
    if (precision>=0) column.setProperty(DefaultIDs.LOGICAL_COLUMN_PRECISION, Integer.toString(precision));
    column.setProperty(DefaultIDs.LOGICAL_COLUMN_CONVERSION_REMARKS, comment);
    
    addLogicalColumnToAttributesList(column);
  }

  public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wTableName.setText( Const.NVL(ConceptUtil.getName(logicalTable,locale), "") );
		wTableDescription.setText( Const.NVL(ConceptUtil.getDescription(logicalTable, locale), "") );
		wPhysicalName.setText(Const.NVL(ConceptUtil.getString(logicalTable, DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME), ""));
    wDimensionType.setText(Const.NVL(ConceptUtil.getString(logicalTable, DefaultIDs.LOGICAL_TABLE_DIMENSION_TYPE), ""));
		
		refreshAttributesList();
	}
	
  protected void refreshAttributesList() {
    wAttributes.clearAll();
    
    for (LogicalColumn column : logicalTable.getLogicalColumns()) {
      addLogicalColumnToAttributesList(column);
    }
    wAttributes.removeEmptyRows();
    wAttributes.setRowNums();
    wAttributes.optWidth(true);
  }
   
	private void addLogicalColumnToAttributesList(LogicalColumn column) {
    TableItem item = new TableItem(wAttributes.table, SWT.NONE);
    
    //  name, description, physical column name, data type, length, precision, source db, source table, source column, conversion remarks 
    //
    int col=1;
    item.setText(col++, Const.NVL(ConceptUtil.getName(column,locale), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getDescription(column, locale), ""));
    item.setText(col++, ConceptUtil.getAttributeType(column).name());
    item.setText(col++, Const.NVL((String)column.getProperty(DefaultIDs.LOGICAL_COLUMN_PHYSICAL_COLUMN_NAME), ""));
    DataType dataType = (DataType) column.getProperty(DefaultPropertyID.DATA_TYPE.getId());
    item.setText(col++, dataType==null ? "" : dataType.name() );
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_LENGTH), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_PRECISION), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_SOURCE_DB), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_SOURCE_TABLE), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_SOURCE_COLUMN), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_CONVERSION_REMARKS), ""));
  }

  private void cancel()
	{
		props.setScreen(new WindowProperty(shell));
		logicalTable=null;
		dispose();
	}
		
	private void ok()
	{
	  
    if (Const.isEmpty(wTableName.getText())) {
      MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
      box.setText(BaseMessages.getString(PKG, "DimensionTableDialog.ErrorDimensionHasNoName.Title"));
      box.setMessage(BaseMessages.getString(PKG, "DimensionTableDialog.ErrorDimensionHasNoName.Message"));
      box.open();
      return;
    }
	  
		logicalTable.setName( new LocalizedString(locale, wTableName.getText()) );		
		logicalTable.setDescription( new LocalizedString(locale, wTableDescription.getText()) );
		logicalTable.setProperty(DefaultPropertyID.TABLE_TYPE.getId(), TableType.DIMENSION);
		logicalTable.setProperty(DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME, wPhysicalName.getText());
    logicalTable.setProperty(DefaultIDs.LOGICAL_TABLE_DIMENSION_TYPE, wDimensionType.getText());
		
    // name, description, field type, physical column name, data type, length, precision, 
		// source db, source table, source column, conversion remarks 
    //
		logicalTable.getLogicalColumns().clear();
		int nr = wAttributes.nrNonEmpty();
		for (int i=0;i<nr;i++) {
		  TableItem item = wAttributes.getNonEmpty(i);
		  LogicalColumn logicalColumn = new LogicalColumn();
		  logicalColumn.setId(UUID.randomUUID().toString());
		  
		  int col=1;
		  logicalColumn.setName(new LocalizedString(locale, item.getText(col++)));
		  logicalColumn.setDescription(new LocalizedString(locale, item.getText(col++)));
		  String fieldTypeString = item.getText(col++);
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_ATTRIBUTE_TYPE, AttributeType.getAttributeType(fieldTypeString).name());
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_PHYSICAL_COLUMN_NAME, item.getText(col++));
      String dataTypeString = item.getText(col++);
      logicalColumn.setDataType(Const.isEmpty(dataTypeString) ? null  : DataType.valueOf(dataTypeString));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_LENGTH, item.getText(col++));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_PRECISION, item.getText(col++));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_SOURCE_DB, item.getText(col++));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_SOURCE_TABLE, item.getText(col++));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_SOURCE_COLUMN, item.getText(col++));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_CONVERSION_REMARKS, item.getText(col++));
      
      logicalColumn.setLogicalTable(logicalTable);
      logicalTable.getLogicalColumns().add(logicalColumn);
		}

		dispose();
	}
}