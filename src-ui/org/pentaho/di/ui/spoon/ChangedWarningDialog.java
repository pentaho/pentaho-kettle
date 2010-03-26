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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class ChangedWarningDialog implements ChangedWarningInterface {
  
  private static ChangedWarningInterface instance = new ChangedWarningDialog();
  
  protected String result = null;
  
  protected XulDomContainer container = null;
  
  private static Class<?> PKG = Spoon.class;
  
  public ChangedWarningDialog() {
  }
  
  public static void setInstance(ChangedWarningInterface cwi) {
    // Cannot null out the instance
    if(cwi != null) {
      instance = cwi;
    }
  }
  
  public static ChangedWarningInterface getInstance() {
    return instance;
  }
  
  public String getName() {
    return "changedWarningController"; //$NON-NLS-1$
  }
  
  public int show() throws Exception {
    return show(null);
  }
  
  public int show(String fileName) throws Exception {
    return runXulChangedWarningDialog(fileName).open();
  }
  
  protected XulMessageBox runXulChangedWarningDialog(String fileName) throws IllegalArgumentException, XulException {
    container = Spoon.getInstance().getMainSpoonContainer();
        
    XulMessageBox messageBox = (XulMessageBox) container.getDocumentRoot().createElement("messagebox"); //$NON-NLS-1$
    messageBox.setTitle(BaseMessages.getString(PKG, "Spoon.Dialog.PromptSave.Title")); //$NON-NLS-1$
    
    if(fileName != null) {
      messageBox.setMessage(BaseMessages.getString("Spoon.Dialog.PromptToSave.Message", fileName)); //$NON-NLS-1$
    } else {
      messageBox.setMessage(BaseMessages.getString("Spoon.Dialog.PromptSave.Message")); //$NON-NLS-1$
    }
    
    messageBox.setButtons(new Integer[] {SWT.YES, SWT.NO, SWT.CANCEL});
    
    return messageBox;
  }
}
