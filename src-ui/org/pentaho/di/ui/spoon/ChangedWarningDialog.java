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

package org.pentaho.di.ui.spoon;

import org.eclipse.swt.SWT;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMessageBox;

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
      messageBox.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.PromptToSave.Message", fileName)); //$NON-NLS-1$
    } else {
      messageBox.setMessage(BaseMessages.getString(PKG, "Spoon.Dialog.PromptSave.Message")); //$NON-NLS-1$
    }
    
    messageBox.setButtons(new Integer[] {SWT.YES, SWT.NO, SWT.CANCEL});
    
    return messageBox;
  }
}
