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

package org.pentaho.di.ui.spoon;

import java.net.URL;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.XulHelper;
import org.pentaho.xul.swt.toolbar.Toolbar;
import org.pentaho.xul.toolbar.XulToolbarButton;

/**
 * This class handles the display of help information like the welcome page and JDBC info in an embedded browser.
 * 
 * @author Matt
 * @since November 2006
 * 
 */

public class SpoonBrowser implements TabItemInterface
{
	private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final LogWriter log = LogWriter.getInstance();
	
	private static final String XUL_FILE_BROWSER_TOOLBAR = "ui/browser-toolbar.xul";
	public static final String XUL_FILE_BROWSER_TOOLBAR_PROPERTIES = "ui/browser-toolbar.properties";

    private Shell            shell;
    private Spoon            spoon;
    private String           stringUrl;
    private Composite        composite;
	private Toolbar toolbar;
    
    private Browser browser;

    public SpoonBrowser(Composite parent, final Spoon spoon, final String stringUrl,boolean isURL) throws SWTError
    {
    	composite = new Composite(parent, SWT.NONE);
    	
        this.shell = parent.getShell();
        this.spoon = spoon;
        this.stringUrl = stringUrl;
        
        composite.setLayout(new FormLayout());
        
        addToolBar();
        addToolBarListeners();
        
        // HACK ALERT : setting this in some sort of property would be far nicer
        //
        Control swtToolBar = (Control)toolbar.getNativeObject();
        FormData fdToolBar= (FormData) swtToolBar.getLayoutData();
        fdToolBar.right = null;
        
        // Add a URL
        final Text urlText = new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.READ_ONLY | SWT.BORDER);
        FormData fdUrlText = new FormData();
        fdUrlText.top = new FormAttachment(swtToolBar, 0, SWT.CENTER);
        fdUrlText.left = new FormAttachment(swtToolBar, 20);
        fdUrlText.right = new FormAttachment(100,0);
        urlText.setLayoutData(fdUrlText);
        
        
        final XulToolbarButton back = toolbar.getButtonById("browse-back");
        back.setEnable(false);
        final XulToolbarButton forward = toolbar.getButtonById("browse-forward");
        forward.setText(BaseMessages.getString(PKG, "SpoonBrowser.Dialog.Forward"));
        forward.setEnable(false);
        
        browser = new Browser(composite, SWT.NONE);
        FormData fdBrowser = new FormData();
        fdBrowser.left = new FormAttachment(0,0);
        fdBrowser.right = new FormAttachment(100,0);
        fdBrowser.top = new FormAttachment((Control)toolbar.getNativeObject(),2);
        fdBrowser.bottom = new FormAttachment(100,0);
        browser.setLayoutData(fdBrowser);

        LocationListener locationListener = new LocationListener() {
            public void changed(LocationEvent event) {
                  Browser browser = (Browser)event.widget;
                  back.setEnable(browser.isBackEnabled());
                  forward.setEnable(browser.isForwardEnabled());
                  urlText.setText(browser.getUrl());
               }
            public void changing(LocationEvent event) {
               }
            };
            
        browser.addLocationListener(locationListener);
         
        composite.addKeyListener(spoon.defKeys);
        browser.addKeyListener(spoon.defKeys);
                 
        // Set the text
       if (isURL)
    	   browser.setUrl(stringUrl);
       else
    	   browser.setText(stringUrl);
    }

    private void addToolBar()
	{
		try {
			toolbar = XulHelper.createToolbar(XUL_FILE_BROWSER_TOOLBAR, composite, SpoonBrowser.this, new XulMessages());
			
			// Add a few default key listeners
			//
			ToolBar toolBar = (ToolBar) toolbar.getNativeObject();
			toolBar.addKeyListener(spoon.defKeys);
			
			addToolBarListeners();
		} catch (Throwable t ) {
			log.logError(toString(), Const.getStackTracker(t));
			new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Title"), BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_BROWSER_TOOLBAR), new Exception(t));
		}
	}

	public void addToolBarListeners()
	{
		try
		{
			// first get the XML document
			URL url = XulHelper.getAndValidate(XUL_FILE_BROWSER_TOOLBAR_PROPERTIES);
			Properties props = new Properties();
			props.load(url.openStream());
			String ids[] = toolbar.getMenuItemIds();
			for (int i = 0; i < ids.length; i++)
			{
				String methodName = (String) props.get(ids[i]);
				if (methodName != null)
				{
					toolbar.addMenuListener(ids[i], this, methodName);

				}
			}

		} catch (Throwable t ) {
			t.printStackTrace();
			new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Title"), 
					BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_BROWSER_TOOLBAR_PROPERTIES), new Exception(t));
		}
	}
	
    public void newFileDropDown() {
    	spoon.newFileDropDown(toolbar);
    }

    public void openFile() {
    	spoon.openFile();
    }

	public void browseBack() {
		browser.back();
	}
	
	public void browseForward() {
		browser.forward();
	}
	
  
    /**
     * @return the browser
     */
    public Browser getBrowser()
    {
        return browser;
    }

    /**
     * @return the shell
     */
    public Shell getShell()
    {
        return shell;
    }

    /**
     * @return the spoon
     */
    public Spoon getSpoon()
    {
        return spoon;
    }

    /**
     * @param spoon the spoon to set
     */
    public void setSpoon(Spoon spoon)
    {
        this.spoon = spoon;
    }

    public boolean applyChanges()
    {
        return true;
    }

    public boolean canBeClosed()
    {
        return true;
    }

    public Object getManagedObject()
    {
        return stringUrl;
    }

    public boolean hasContentChanged()
    {
        return false;
    }

    public int showChangedWarning()
    {
        return 0;
    }

    /**
     * @return the composite
     */
    public Composite getComposite()
    {
        return composite;
    }

    /**
     * @param composite the composite to set
     */
    public void setComposite(Composite composite)
    {
        this.composite = composite;
    }

    public EngineMetaInterface getMeta() {
    	return null;
    }

	public void setControlStates() {
		// TODO Auto-generated method stub
		
	}

    
}
