/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.spoon;

import org.eclipse.swt.SWT;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public abstract class AbstractChangedWarningDialog extends AbstractXulEventHandler implements ChangedWarningInterface {
  
  protected String result = null;
  
  protected XulDomContainer container = null;
  
  public abstract String getXulResource();
  
  public abstract String getXulDialogId();
  
  public abstract String getSpoonPluginManagerContainerNamespace();
  
  public abstract XulSpoonResourceBundle getXulResourceBundle();
  
  public Object getResult() {
    Integer retVal = 0;
    
    if(result.equalsIgnoreCase("yes")) {  //$NON-NLS-1$
      retVal = SWT.YES;
    } else if(result.equalsIgnoreCase("no")) { //$NON-NLS-1$
      retVal = SWT.NO;
    } else if(result.equalsIgnoreCase("cancel")) { //$NON-NLS-1$
      retVal = SWT.CANCEL;
    }
    
    return retVal;
  }
  
  @Override
  public String getName() {
    return "changedWarningController"; //$NON-NLS-1$
  }
  
  public void setResultYes() {
    result = "yes"; //$NON-NLS-1$
    closeDialog();
  }
  
  public void setResultNo() {
    result = "no";  //$NON-NLS-1$
    closeDialog();
  }
  
  public void setResultCancel() {
    result = "cancel"; //$NON-NLS-1$
    closeDialog();
  }
  
  protected void closeDialog() {
    XulDialog cwDialog = (XulDialog) document.getElementById(getXulDialogId());
    cwDialog.hide();      
  }
  
  public int show() throws Exception {
    runXulChangedWarningDialog();
    
    AbstractChangedWarningDialog handler = null;
    if(container != null) {
      handler = (AbstractChangedWarningDialog)container.getEventHandler(getName());
    } else {
      handler = this;
    }
    
    return ((Integer)handler.getResult()).intValue();
  }
  
  protected void runXulChangedWarningDialog() throws IllegalArgumentException, XulException {
    if(getXulResourceBundle() == null) {
      container = new SwtXulLoader().loadXul(getXulResource());
    } else {
      container = new SwtXulLoader().loadXul(getXulResource(), getXulResourceBundle());
    }
    
    container.addEventHandler(this);
    
    SpoonPluginManager.getInstance().applyPluginsForContainer(getSpoonPluginManagerContainerNamespace(), container);
    
    final XulRunner runner = new SwtXulRunner();
    runner.addContainer(container);

    try {
      runner.initialize();
    } catch (XulException e) {
      e.printStackTrace();
    }
    
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById(getXulDialogId());
    dialog.show();
  }
}
