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

 
/*
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.trans.steps.addxml;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.addxml.AddXMLMeta;
import org.pentaho.di.trans.steps.addxml.XMLField;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

public class AddXMLDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = AddXMLMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private CTabFolder   wTabFolder;
    private FormData     fdTabFolder;
    
    private CTabItem     wContentTab, wFieldsTab;

    private FormData     fdContentComp, fdFieldsComp;

    private Label        wlOmitXMLHeader;
    private Button       wOmitXMLHeader;
    private FormData     fdlOmitXMLHeader, fdOmitXMLHeader;
    
    private Label        wlOmitNullValues;
    private Button       wOmitNullValues;
    private FormData     fdlOmitNullValues, fdOmitNullValues;
    
    private Label        wlEncoding;
    private CCombo       wEncoding;
    private FormData     fdlEncoding, fdEncoding;

    private Label        wlOutputValue;
    private CCombo       wOutputValue;
    private FormData     fdlOutputValue, fdOutputValue;

    private Label        wlRepeatElement;
    private CCombo       wRepeatElement;
    private FormData     fdlRepeatElement, fdRepeatElement;

    private TableView    wFields;
    private FormData     fdFields;

    private AddXMLMeta   input;
    
    private Button       wMinWidth;
    private Listener     lsMinWidth;
    private boolean      gotEncodings = false; 
    
    private ColumnInfo[] colinf;
    
	
    private Map<String, Integer> inputFields;
    
    public AddXMLDialog(Shell parent, Object in, TransMeta transMeta, String sname)
    {
        super(parent, (BaseStepMeta)in, transMeta, sname);
        input=(AddXMLMeta)in;
        inputFields =new HashMap<String, Integer>();
    }

    public String open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
        setShellImage(shell, input);

        ModifyListener lsMod = new ModifyListener() 
        {
            public void modifyText(ModifyEvent e) 
            {
                input.setChanged();
            }
        };
        changed = input.hasChanged();
        
        FormLayout formLayout = new FormLayout ();
        formLayout.marginWidth  = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "AddXMLDialog.DialogTitle"));
        
        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Stepname line
        wlStepname=new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname=new FormData();
        fdlStepname.left  = new FormAttachment(0, 0);
        fdlStepname.top   = new FormAttachment(0, margin);
        fdlStepname.right = new FormAttachment(middle, -margin);
        wlStepname.setLayoutData(fdlStepname);
        wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname=new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top  = new FormAttachment(0, margin);
        fdStepname.right= new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
        
        //////////////////////////
        // START OF CONTENT TAB///
        ///
        wContentTab=new CTabItem(wTabFolder, SWT.NONE);
        wContentTab.setText(BaseMessages.getString(PKG, "AddXMLDialog.ContentTab.TabTitle"));
        

        FormLayout contentLayout = new FormLayout ();
        contentLayout.marginWidth  = 3;
        contentLayout.marginHeight = 3;
        
        Composite wContentComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wContentComp);
        wContentComp.setLayout(contentLayout);

        wlEncoding=new Label(wContentComp, SWT.RIGHT);
        wlEncoding.setText(BaseMessages.getString(PKG, "AddXMLDialog.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left = new FormAttachment(0, 0);
        fdlEncoding.top  = new FormAttachment(null, margin);
        fdlEncoding.right= new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
        wEncoding.setEditable(true);
        props.setLook(wEncoding);
        wEncoding.addModifyListener(lsMod);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, 0);
        fdEncoding.top  = new FormAttachment(null, margin);
        fdEncoding.right= new FormAttachment(100, 0);
        wEncoding.setLayoutData(fdEncoding);
        wEncoding.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setEncodings();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );

        wlOutputValue=new Label(wContentComp, SWT.RIGHT);
        wlOutputValue.setText(BaseMessages.getString(PKG, "AddXMLDialog.OutputValue.Label"));
        props.setLook(wlOutputValue);
        fdlOutputValue=new FormData();
        fdlOutputValue.left = new FormAttachment(0, 0);
        fdlOutputValue.top  = new FormAttachment(wEncoding, margin);
        fdlOutputValue.right= new FormAttachment(middle, -margin);
        wlOutputValue.setLayoutData(fdlOutputValue);
        wOutputValue=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
        wOutputValue.setEditable(true);
        props.setLook(wOutputValue);
        wOutputValue.addModifyListener(lsMod);
        fdOutputValue=new FormData();
        fdOutputValue.left = new FormAttachment(middle, 0);
        fdOutputValue.top  = new FormAttachment(wEncoding, margin);
        fdOutputValue.right= new FormAttachment(100, 0);
        wOutputValue.setLayoutData(fdOutputValue);

        wlRepeatElement=new Label(wContentComp, SWT.RIGHT);
        wlRepeatElement.setText(BaseMessages.getString(PKG, "AddXMLDialog.RepeatElement.Label"));
        props.setLook(wlRepeatElement);
        fdlRepeatElement=new FormData();
        fdlRepeatElement.left = new FormAttachment(0, 0);
        fdlRepeatElement.top  = new FormAttachment(wOutputValue, margin);
        fdlRepeatElement.right= new FormAttachment(middle, -margin);
        wlRepeatElement.setLayoutData(fdlRepeatElement);
        wRepeatElement=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
        wRepeatElement.setEditable(true);
        props.setLook(wRepeatElement);
        wRepeatElement.addModifyListener(lsMod);
        fdRepeatElement=new FormData();
        fdRepeatElement.left = new FormAttachment(middle, 0);
        fdRepeatElement.top  = new FormAttachment(wOutputValue, margin);
        fdRepeatElement.right= new FormAttachment(100, 0);
        wRepeatElement.setLayoutData(fdRepeatElement);

        wlOmitXMLHeader=new Label(wContentComp, SWT.RIGHT);
        wlOmitXMLHeader.setText(BaseMessages.getString(PKG, "AddXMLDialog.OmitXMLHeader.Label")); //$NON-NLS-1$
        props.setLook(wlOmitXMLHeader);
        fdlOmitXMLHeader=new FormData();
        fdlOmitXMLHeader.left = new FormAttachment(0, 0);
        fdlOmitXMLHeader.top  = new FormAttachment(wRepeatElement, margin);
        fdlOmitXMLHeader.right= new FormAttachment(middle, -margin);
        wlOmitXMLHeader.setLayoutData(fdlOmitXMLHeader);
        wOmitXMLHeader=new Button(wContentComp, SWT.CHECK );
        props.setLook(wOmitXMLHeader);
        fdOmitXMLHeader=new FormData();
        fdOmitXMLHeader.left = new FormAttachment(middle, 0);
        fdOmitXMLHeader.top  = new FormAttachment(wRepeatElement, margin);
        fdOmitXMLHeader.right= new FormAttachment(100, 0);
        wOmitXMLHeader.setLayoutData(fdOmitXMLHeader);
        wOmitXMLHeader.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    input.setChanged();
                }
            }
        );
        
        wlOmitNullValues=new Label(wContentComp, SWT.RIGHT);
        wlOmitNullValues.setText(BaseMessages.getString(PKG, "AddXMLDialog.OmitNullValues.Label")); //$NON-NLS-1$
        props.setLook(wlOmitNullValues);
        fdlOmitNullValues=new FormData();
        fdlOmitNullValues.left = new FormAttachment(0, 0);
        fdlOmitNullValues.top  = new FormAttachment(wOmitXMLHeader, margin);
        fdlOmitNullValues.right= new FormAttachment(middle, -margin);
        wlOmitNullValues.setLayoutData(fdlOmitNullValues);
        wOmitNullValues=new Button(wContentComp, SWT.CHECK );
        props.setLook(wOmitNullValues);
        fdOmitNullValues=new FormData();
        fdOmitNullValues.left = new FormAttachment(middle, 0);
        fdOmitNullValues.top  = new FormAttachment(wOmitXMLHeader, margin);
        fdOmitNullValues.right= new FormAttachment(100, 0);
        wOmitNullValues.setLayoutData(fdOmitNullValues);
        wOmitNullValues.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    input.setChanged();
                }
            }
        );

        fdContentComp = new FormData();
        fdContentComp.left  = new FormAttachment(0, 0);
        fdContentComp.top   = new FormAttachment(0, 0);
        fdContentComp.right = new FormAttachment(100, 0);
        fdContentComp.bottom= new FormAttachment(100, 0);
        wContentComp.setLayoutData(fdContentComp);

        wContentComp.layout();
        wContentTab.setControl(wContentComp);
        
        /////////////////////////////////////////////////////////////
        /// END OF CONTENT TAB
        /////////////////////////////////////////////////////////////

        // Fields tab...
        //
        wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
        wFieldsTab.setText(BaseMessages.getString(PKG, "AddXMLDialog.FieldsTab.TabTitle"));
        
        FormLayout fieldsLayout = new FormLayout ();
        fieldsLayout.marginWidth  = Const.FORM_MARGIN;
        fieldsLayout.marginHeight = Const.FORM_MARGIN;
        
        Composite wFieldsComp = new Composite(wTabFolder, SWT.NONE);
        wFieldsComp.setLayout(fieldsLayout);
        props.setLook(wFieldsComp);

        wGet=new Button(wFieldsComp, SWT.PUSH);
        wGet.setText(BaseMessages.getString(PKG, "AddXMLDialog.Get.Button"));
        wGet.setToolTipText(BaseMessages.getString(PKG, "AddXMLDialog.Get.Tooltip"));

        wMinWidth =new Button(wFieldsComp, SWT.PUSH);
        wMinWidth.setText(BaseMessages.getString(PKG, "AddXMLDialog.MinWidth.Label"));
        wMinWidth.setToolTipText(BaseMessages.getString(PKG, "AddXMLDialog.MinWidth.Tooltip"));

        setButtonPositions(new Button[] { wGet, wMinWidth}, margin, null);

        final int FieldsRows=input.getOutputFields().length;
        
        // Prepare a list of possible formats...
        String dats[] = Const.getDateFormats();
        String nums[] = Const.getNumberFormats();
        int totsize = dats.length + nums.length;
        String formats[] = new String[totsize];
        for (int x=0;x<dats.length;x++) formats[x] = dats[x];
        for (int x=0;x<nums.length;x++) formats[dats.length+x] = nums[x];
        
        colinf=new ColumnInfo[]
          {
            new ColumnInfo(BaseMessages.getString(PKG, "AddXMLDialog.Fieldname.Column"),   ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
            new ColumnInfo(BaseMessages.getString(PKG, "AddXMLDialog.ElementName.Column"), ColumnInfo.COLUMN_TYPE_TEXT,   false),
            new ColumnInfo(BaseMessages.getString(PKG, "AddXMLDialog.Type.Column"),        ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() ),
            new ColumnInfo(BaseMessages.getString(PKG, "AddXMLDialog.Format.Column"),      ColumnInfo.COLUMN_TYPE_CCOMBO, formats),
            new ColumnInfo(BaseMessages.getString(PKG, "AddXMLDialog.Length.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,   false),
            new ColumnInfo(BaseMessages.getString(PKG, "AddXMLDialog.Precision.Column"),   ColumnInfo.COLUMN_TYPE_TEXT,   false),
            new ColumnInfo(BaseMessages.getString(PKG, "AddXMLDialog.Currency.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,   false),
            new ColumnInfo(BaseMessages.getString(PKG, "AddXMLDialog.Decimal.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,   false),
            new ColumnInfo(BaseMessages.getString(PKG, "AddXMLDialog.Group.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,   false),
            new ColumnInfo(BaseMessages.getString(PKG, "AddXMLDialog.Null.Column"),        ColumnInfo.COLUMN_TYPE_TEXT,   false),
            new ColumnInfo(BaseMessages.getString(PKG, "AddXMLDialog.Attribute.Column"),   ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(PKG, "System.Combo.Yes"), BaseMessages.getString(PKG, "System.Combo.No") }, true),
            new ColumnInfo(BaseMessages.getString(PKG, "AddXMLDialog.AttributeParentName.Column"), ColumnInfo.COLUMN_TYPE_TEXT,   false)
          };
        wFields=new TableView(transMeta, wFieldsComp, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
                              colinf, 
                              FieldsRows,  
                              lsMod,
                              props
                              );

        fdFields=new FormData();
        fdFields.left  = new FormAttachment(0, 0);
        fdFields.top   = new FormAttachment(0, 0);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom= new FormAttachment(wGet, -margin);
        wFields.setLayoutData(fdFields);
        
		  // 
        // Search the fields in the background
		
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null)
                {
                    try
                    {
                    	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                       
                        // Remember these fields...
                        for (int i=0;i<row.size();i++)
                        {
                            inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }
                        setComboBoxes();
                    }
                    catch(KettleException e)
                    {
                    	log.logError( BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();

        fdFieldsComp=new FormData();
        fdFieldsComp.left  = new FormAttachment(0, 0);
        fdFieldsComp.top   = new FormAttachment(0, 0);
        fdFieldsComp.right = new FormAttachment(100, 0);
        fdFieldsComp.bottom= new FormAttachment(100, 0);
        wFieldsComp.setLayoutData(fdFieldsComp);
        
        wFieldsComp.layout();
        wFieldsTab.setControl(wFieldsComp);
        
        fdTabFolder = new FormData();
        fdTabFolder.left  = new FormAttachment(0, 0);
        fdTabFolder.top   = new FormAttachment(wStepname, margin);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.bottom= new FormAttachment(100, -50);
        wTabFolder.setLayoutData(fdTabFolder);
        
        wOK=new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        
        wCancel=new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);

        // Add listeners
        lsOK       = new Listener() { public void handleEvent(Event e) { ok();       } };
        lsGet      = new Listener() { public void handleEvent(Event e) { get();      } };
        lsMinWidth    = new Listener() { public void handleEvent(Event e) { setMinimalWidth(); } };
        lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();   } };
        
        wOK.addListener    (SWT.Selection, lsOK    );
        wGet.addListener   (SWT.Selection, lsGet   );
        wMinWidth.addListener (SWT.Selection, lsMinWidth );
        wCancel.addListener(SWT.Selection, lsCancel);
        
        lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
        
        wStepname.addSelectionListener( lsDef );
        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        lsResize = new Listener() 
        {
            public void handleEvent(Event event) 
            {
                Point size = shell.getSize();
                wFields.setSize(size.x-10, size.y-50);
                wFields.table.setSize(size.x-10, size.y-50);
                wFields.redraw();
            }
        };
        shell.addListener(SWT.Resize, lsResize);

        wTabFolder.setSelection(0);
        
        // Set the shell size, based upon previous time...
        setSize();
        
        getData();
        input.setChanged(changed);
        
        shell.open();
        while (!shell.isDisposed())
        {
                if (!display.readAndDispatch()) display.sleep();
        }
        return stepname;
    }
    protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
        final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);

        String fieldNames[] = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        colinf[0].setComboValues(fieldNames);
    }
    private void setEncodings()
    {
        // Encoding of the text file:
        if (!gotEncodings)
        {
            gotEncodings = true;
            
            wEncoding.removeAll();
            List<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
            for (int i=0;i<values.size();i++)
            {
                Charset charSet = (Charset)values.get(i);
                wEncoding.add( charSet.displayName() );
            }
            
            // Now select the default!
            String defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
            int idx = Const.indexOfString(defEncoding, wEncoding.getItems() );
            if (idx>=0) wEncoding.select( idx );
            else 
            	wEncoding.select(Const.indexOfString("UTF-8", wEncoding.getItems() ));
        }
    }


    /**
     * Copy information from the meta-data input to the dialog fields.
     */ 
    public void getData()
    {
        if (input.getEncoding()      != null) wEncoding.setText(input.getEncoding());
        if (input.getValueName()     != null) wOutputValue.setText(input.getValueName());
        if (input.getRootNode() != null) wRepeatElement.setText(input.getRootNode());
        
        wOmitXMLHeader.setSelection(input.isOmitXMLheader());
        wOmitNullValues.setSelection(input.isOmitNullValues());
        
        logDebug(BaseMessages.getString(PKG, "AddXMLDialog.Log.GettingFieldsInfo"));
        
        for (int i=0;i<input.getOutputFields().length;i++)
        {
            XMLField field = input.getOutputFields()[i];

            TableItem item = wFields.table.getItem(i);
            if (field.getFieldName()!=null) item.setText(1, field.getFieldName());
            if (field.getElementName()!=null) item.setText(2, field.getElementName());
            item.setText(3, field.getTypeDesc());
            if (field.getFormat()!=null) item.setText(4, field.getFormat());
            if (field.getLength()>=0) item.setText(5, ""+field.getLength());
            if (field.getPrecision()>=0) item.setText(6, ""+field.getPrecision());
            if (field.getCurrencySymbol()!=null) item.setText(7, field.getCurrencySymbol());
            if (field.getDecimalSymbol()!=null) item.setText(8, field.getDecimalSymbol());
            if (field.getGroupingSymbol()!=null) item.setText(9, field.getGroupingSymbol());
            if (field.getNullString()!=null) item.setText(10, field.getNullString());
            item.setText(11, field.isAttribute() ? BaseMessages.getString(PKG, "System.Combo.Yes") : BaseMessages.getString(PKG, "System.Combo.No"));
            if (field.getAttributeParentName()!=null) item.setText(12, field.getAttributeParentName());
        }
        
        wFields.optWidth(true);
        wStepname.selectAll();
    }
    
    private void cancel()
    {
        stepname=null;
        
        input.setChanged(backupChanged);

        dispose();
    }
    
    private void getInfo(AddXMLMeta tfoi)
    {
        tfoi.setEncoding( wEncoding.getText() );
        tfoi.setValueName( wOutputValue.getText() );
        tfoi.setRootNode( wRepeatElement.getText() );

        tfoi.setOmitXMLheader( wOmitXMLHeader.getSelection() );
        tfoi.setOmitNullValues( wOmitNullValues.getSelection() );

        //Table table = wFields.table;
        
        int nrfields = wFields.nrNonEmpty();

        tfoi.allocate(nrfields);
        
        for (int i=0;i<nrfields;i++)
        {
            XMLField field = new XMLField();
            
            TableItem item = wFields.getNonEmpty(i);
            field.setFieldName( item.getText(1) );
            field.setElementName( item.getText(2) );
            
            if (field.getFieldName().equals(field.getElementName())) field.setElementName("");
            
            field.setType( item.getText(3) );
            field.setFormat( item.getText(4) );
            field.setLength( Const.toInt(item.getText(5), -1) );
            field.setPrecision( Const.toInt(item.getText(6), -1) );
            field.setCurrencySymbol( item.getText(7) );
            field.setDecimalSymbol( item.getText(8) );
            field.setGroupingSymbol( item.getText(9) );
            field.setNullString( item.getText(10) );
            field.setAttribute( BaseMessages.getString(PKG, "System.Combo.Yes").equals(item.getText(11)) );
            field.setAttributeParentName(item.getText(12));
            
            tfoi.getOutputFields()[i]  = field;
        }
    }
    
    private void ok()
    {
		if (Const.isEmpty(wStepname.getText())) return;
		
        stepname = wStepname.getText(); // return value
        
        getInfo(input);
        
        dispose();
    }
    
    private void get()
    {
        try
        {
            RowMetaInterface r = transMeta.getPrevStepFields(stepname);
            if (r!=null)
            {
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1, 2 }, new int[] { 3 }, 5, 6, new TableItemInsertListener()
                    {
                        public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                        {
                            if (v.isNumber())
                            {
                                if (v.getLength()>0)
                                {
                                    int le=v.getLength();
                                    int pr=v.getPrecision();
                                    
                                    if (v.getPrecision()<=0)
                                    {
                                        pr=0;
                                    }
                                    
                                    String mask=" ";
                                    for (int m=0;m<le-pr;m++)
                                    {
                                        mask+="0";
                                    }
                                    if (pr>0) mask+=".";
                                    for (int m=0;m<pr;m++)
                                    {
                                        mask+="0";
                                    }
                                    tableItem.setText(4, mask);
                                }
                            }
                            return true;
                        }
                    }
                );
            }
        }
        catch(KettleException ke)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);
        }

    }
    
    /**
     * Sets the output width to minimal width...
     *
     */
    public void setMinimalWidth()
    {
        int nrNonEmptyFields = wFields.nrNonEmpty();
        for (int i=0;i<nrNonEmptyFields;i++)
        {
            TableItem item = wFields.getNonEmpty(i);
            
            item.setText(5, "");
            item.setText(6, "");
            
            int type = ValueMeta.getType(item.getText(2));
            switch(type)
            {
            case ValueMetaInterface.TYPE_STRING:  item.setText(4, ""); break;
            case ValueMetaInterface.TYPE_INTEGER: item.setText(4, "0"); break;
            case ValueMetaInterface.TYPE_NUMBER: item.setText(4, "0.#####"); break;
            case ValueMetaInterface.TYPE_DATE: break;
            default: break;
            }
        }
        wFields.optWidth(true);
    }

    public String toString()
    {
        return this.getClass().getName();
    }
}
