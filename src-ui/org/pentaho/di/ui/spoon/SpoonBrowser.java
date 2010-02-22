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

import java.util.ResourceBundle;

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
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;

/**
 * This class handles the display of help information like the welcome page and JDBC info in an embedded browser.
 * 
 * @author Matt
 * @since November 2006
 * 
 */

public class SpoonBrowser implements TabItemInterface, XulEventHandler {
  protected static final LogWriter log = LogWriter.getInstance();
  
  private static final Class<?> PKG = SpoonBrowser.class;

  private static final String XUL_FILE_BROWSER_TOOLBAR = "ui/browser-toolbar.xul";

  protected Shell shell;

  protected Spoon spoon;

  private String stringUrl;

  protected Composite composite;

  protected XulToolbar toolbar;

  protected Browser browser;

  private XulToolbarbutton back = null;

  private XulToolbarbutton forward = null;

  private Text urlText = null;

  public SpoonBrowser(Composite parent, final Spoon spoon, final String stringUrl, boolean isURL) throws SWTError {
    this(parent, spoon, stringUrl, isURL, true);
  }

  public SpoonBrowser(Composite parent, final Spoon spoon, final String stringUrl, boolean isURL, boolean showControls) throws SWTError {
    composite = new Composite(parent, SWT.NONE);

    this.shell = parent.getShell();
    this.spoon = spoon;
    this.stringUrl = stringUrl;

    composite.setLayout(new FormLayout());

    if (showControls) {
      addToolBar();

      // HACK ALERT : setting this in some sort of property would be far nicer
      //
      // TODO figure out what this was supposed to do.
      //          Control swtToolBar = (Control)toolbar.getManagedObject();
      //          FormData fdToolBar= (FormData) swtToolBar.getLayoutData();
      //          fdToolBar.right = null;
    }

    browser = createBrowser();

    // Nick's fix below -------
    Control toolbarControl = (Control) toolbar.getManagedObject();

    toolbarControl.setLayoutData(new FormData());
    // ------------------------

    FormData fdBrowser = new FormData();
    fdBrowser.left = new FormAttachment(0, 0);
    fdBrowser.right = new FormAttachment(100, 0);
    if (showControls) {
      fdBrowser.top = new FormAttachment((Control) toolbar.getManagedObject(), 2);
    } else {
      fdBrowser.top = new FormAttachment(0, 2);
    }
    fdBrowser.bottom = new FormAttachment(100, 0);
    browser.setLayoutData(fdBrowser);

    LocationListener locationListener = new LocationListener() {
      public void changed(LocationEvent event) {
        Browser browser = (Browser) event.widget;
        if (back != null) {
          back.setDisabled(!browser.isBackEnabled());
          forward.setDisabled(!browser.isForwardEnabled());
          urlText.setText(browser.getUrl());
        }
      }

      public void changing(LocationEvent event) {
      }
    };

    browser.addLocationListener(locationListener);

    // Set the text
    if (isURL)
      browser.setUrl(stringUrl);
    else
      browser.setText(stringUrl);
  }

  protected Browser createBrowser() {
    return new Browser(composite, SWT.NONE);
  }

  protected void addToolBar() {

    try {
      XulLoader loader = new SwtXulLoader();
      ResourceBundle bundle = GlobalMessages.getBundle("org/pentaho/di/ui/spoon/messages/messages");
      XulDomContainer xulDomContainer = loader.loadXul(XUL_FILE_BROWSER_TOOLBAR, bundle);
      xulDomContainer.addEventHandler(this);
      toolbar = (XulToolbar) xulDomContainer.getDocumentRoot().getElementById("nav-toolbar");

      ToolBar swtToolBar = (ToolBar) toolbar.getManagedObject();

      // Add a URL
      urlText = new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.READ_ONLY | SWT.BORDER);
      FormData fdUrlText = new FormData();
      fdUrlText.top = new FormAttachment(swtToolBar, 0, SWT.CENTER);
      fdUrlText.left = new FormAttachment(swtToolBar, 20);
      fdUrlText.right = new FormAttachment(100, 0);
      urlText.setLayoutData(fdUrlText);

      back = (XulToolbarbutton) toolbar.getElementById("browse-back");
      back.setDisabled(true);
      forward = (XulToolbarbutton) toolbar.getElementById("browse-forward");
      forward.setLabel(BaseMessages.getString(PKG, "SpoonBrowser.Dialog.Forward"));
      forward.setDisabled(false);

    } catch (Exception e) {
      e.printStackTrace();
      new ErrorDialog(shell, BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Title"), BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_BROWSER_TOOLBAR), e);
    }
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
  public Browser getBrowser() {
    return browser;
  }

  /**
   * @return the shell
   */
  public Shell getShell() {
    return shell;
  }

  /**
   * @return the spoon
   */
  public Spoon getSpoon() {
    return spoon;
  }

  /**
   * @param spoon the spoon to set
   */
  public void setSpoon(Spoon spoon) {
    this.spoon = spoon;
  }

  public boolean applyChanges() {
    return true;
  }

  public boolean canBeClosed() {
    return true;
  }

  public Object getManagedObject() {
    return stringUrl;
  }

  public boolean hasContentChanged() {
    return false;
  }

  public int showChangedWarning() {
    return 0;
  }

  /**
   * @return the composite
   */
  public Composite getComposite() {
    return composite;
  }

  /**
   * @param composite the composite to set
   */
  public void setComposite(Composite composite) {
    this.composite = composite;
  }

  public EngineMetaInterface getMeta() {
    return null;
  }

  public boolean canHandleSave() {
    return false;
  }

  public boolean setFocus() {
    // TODO Auto-generated method stub
    return false;
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
    return "browser";
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

  public void setControlStates() {
    
  }

  public ChangedWarningInterface getChangedWarning() {
        return null;
  }
}
