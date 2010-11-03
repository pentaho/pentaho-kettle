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
 * Created on 19-jun-2003
 *
 */

package org.pentaho.di.ui.core.dialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleVariablesList;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.FieldDisabledListener;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * Allows the user to edit the kettle.properties file.
 *  
 * @author Matt
 *
 */
public class KettlePropertiesFileDialog extends Dialog
{
    private static Class<?> PKG = KettlePropertiesFileDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Label        wlFields;
    private TableView    wFields;
    private FormData     fdlFields, fdFields;
        
    private Button   wOK, wCancel;
    private Listener lsOK, lsCancel;

    private Shell          shell;
    private PropsUI            props;
    
    private Map<String,String> kettleProperties;
    
    /**
     * Constructs a new dialog
     * @param parent The parent shell to link to
     * @param style The style in which we want to draw this shell.
     * @param strings The list of rows to change.
     */
    public KettlePropertiesFileDialog(Shell parent, int style)
    {
        super(parent, style);
        props=PropsUI.getInstance();
        kettleProperties=null;
    }

    public Map<String,String> open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
        shell.setImage(GUIResource.getInstance().getImageTransGraph());
        props.setLook(shell);

        FormLayout formLayout = new FormLayout ();
        formLayout.marginWidth  = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "KettlePropertiesFileDialog.Title"));
        
        int margin = Const.MARGIN;
        
        // Message line
        //
        wlFields=new Label(shell, SWT.NONE);
        wlFields.setText(BaseMessages.getString(PKG, "KettlePropertiesFileDialog.Message"));
        props.setLook(wlFields);
        fdlFields=new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top  = new FormAttachment(0, margin);
        wlFields.setLayoutData(fdlFields);

        int FieldsRows=0;
        
        ColumnInfo[] colinf=new ColumnInfo[]
            {
                new ColumnInfo(BaseMessages.getString(PKG, "KettlePropertiesFileDialog.Name.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, false),
                new ColumnInfo(BaseMessages.getString(PKG, "KettlePropertiesFileDialog.Value.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, false),
                new ColumnInfo(BaseMessages.getString(PKG, "KettlePropertiesFileDialog.Description.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, true),                
            };
        colinf[2].setDisabledListener(new FieldDisabledListener() {
            public boolean isFieldDisabled(int rowNr) {
                return false;
            }
        });
        
        wFields=new TableView(Variables.getADefaultVariableSpace(),
                              shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
                              colinf, 
                              FieldsRows,  
                              null,
                              props
                );
        
        wFields.setReadonly(false);
        
        fdFields=new FormData();
        fdFields.left   = new FormAttachment(0, 0);
        fdFields.top    = new FormAttachment(wlFields, 30);
        fdFields.right  = new FormAttachment(100, 0);
        fdFields.bottom = new FormAttachment(100, -50);
        wFields.setLayoutData(fdFields);
        
        
        wOK=new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));

        wCancel=new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFields);

        // Add listeners
        lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
        lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };

        wOK.addListener (SWT.Selection, lsOK    );
        wCancel.addListener(SWT.Selection, lsCancel    );
        
        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        getData();

        BaseStepDialog.setSize(shell);

        
        
        shell.open();
        while (!shell.isDisposed())
        {
                if (!display.readAndDispatch()) display.sleep();
        }
        return kettleProperties;
    }

    public void dispose()
    {
        props.setScreen(new WindowProperty(shell));
        kettleProperties=null;
        shell.dispose();
    }
    
    /**
     * Copy information from the meta-data input to the dialog fields.
     */ 
    public void getData()
    {
        try {
            // Load the Kettle properties file...
            //
            String filename = getKettlePropertiesFilename();
            File file = new File(filename);
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            
            
            // These are the standard Kettle variables...
            //
            KettleVariablesList variablesList = KettleVariablesList.getInstance();
            
            // Add the standard variables to the properties if they are not in there already
            //
            for (String key : variablesList.getDescriptionMap().keySet()) {
              if (Const.isEmpty((String)properties.get(key))) {
                String defaultValue = variablesList.getDefaultValueMap().get(key);
                properties.put(key, Const.NVL(defaultValue, ""));
              }
            }

            // Obtain and sort the list of keys...
            //
            List<String> keys = new ArrayList<String>();
            Enumeration<Object> keysEnum = properties.keys();
            while (keysEnum.hasMoreElements()) {
                keys.add((String)keysEnum.nextElement());
            }
            Collections.sort(keys);

            // Populate the grid...
            //
            for (int i=0;i<keys.size();i++)
            {
                String key = keys.get(i);
                String value = properties.getProperty(key, "");
                String description = Const.NVL(variablesList.getDescriptionMap().get(key), "");
                
                TableItem item = new TableItem(wFields.table, SWT.NONE);
                item.setBackground(3, GUIResource.getInstance().getColorLightGray());

                int pos=1;
                item.setText(pos++, key);
                item.setText(pos++, value);
                item.setText(pos++, description);
            }
    
            wFields.removeEmptyRows();
            wFields.setRowNums();
            wFields.optWidth(true);
        } catch(Exception e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "KettlePropertiesFileDialog.Exception.ErrorLoadingData.Title"), BaseMessages.getString(PKG, "KettlePropertiesFileDialog.Exception.ErrorLoadingData.Message"), e);
        }
    }

    private String getKettlePropertiesFilename() {
        return Const.getKettleDirectory()+"/"+Const.KETTLE_PROPERTIES;
    }

    private void cancel()
    {
        dispose();
    }
    
    private void ok()
    {
        Properties properties = new Properties();
        kettleProperties = new HashMap<String, String>();
        
        int nr = wFields.nrNonEmpty();
        for (int i=0;i<nr;i++) {
            TableItem item = wFields.getNonEmpty(i);
            int pos=1;
            String variable = item.getText(pos++);
            String value = item.getText(pos++);
            
            if (!Const.isEmpty(variable)) {
                properties.put(variable, value);
                kettleProperties.put(variable, value);
            }
        }
        
        // Save the properties file...
        //
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(getKettlePropertiesFilename());
            properties.store(out, Const.getKettlePropertiesFileHeader());           
        } catch(Exception e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "KettlePropertiesFileDialog.Exception.ErrorSavingData.Title"), BaseMessages.getString(PKG, "KettlePropertiesFileDialog.Exception.ErrorSavingData.Message"), e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                LogChannel.GENERAL.logError(BaseMessages.getString(PKG, "KettlePropertiesFileDialog.Exception.ErrorSavingData.Message", Const.KETTLE_PROPERTIES, getKettlePropertiesFilename()), e);
            }
        }
        
        dispose();
    }
}
