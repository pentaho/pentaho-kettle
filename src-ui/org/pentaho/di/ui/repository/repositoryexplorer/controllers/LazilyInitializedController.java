package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public abstract class LazilyInitializedController extends AbstractXulEventHandler {
  
  private static Class<?> PKG = RepositoryExplorer.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  protected Repository repository;

  protected boolean initialized;

  public void init(Repository repository) throws ControllerInitializationException {
    this.repository = repository;
  }

  protected synchronized void lazyInit() {
    if (!initialized) {
      try {
        boolean succeeded = doLazyInit();
        if (succeeded) {
          initialized = true;
        } else {
          showErrorDialog(null);
        }
      } catch (Exception e) {
        e.printStackTrace();
        showErrorDialog(e);
      }
    }
  }

  private void showErrorDialog(final Exception e) {
    XulMessageBox messageBox = null;
    try {
      messageBox = (XulMessageBox) document.createElement("messagebox"); //$NON-NLS-1$
    } catch (XulException xe) {
      throw new RuntimeException(xe);
    }
    messageBox.setTitle(BaseMessages.getString(PKG, "Dialog.Error"));//$NON-NLS-1$
    messageBox.setAcceptLabel(BaseMessages.getString(PKG, "Dialog.Ok"));//$NON-NLS-1$
    if (e != null) {
      messageBox.setMessage(BaseMessages.getString(PKG,
          "LazilyInitializedController.Message.UnableToInitWithParam", e.getLocalizedMessage()));//$NON-NLS-1$
    } else {
      messageBox.setMessage(BaseMessages.getString(PKG,
          "LazilyInitializedController.Message.UnableToInit"));//$NON-NLS-1$
    }
    messageBox.open();
  }

  protected abstract boolean doLazyInit();

  protected void doWithBusyIndicator(final Runnable r) {
    BusyIndicator.showWhile(Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault(), r);
  }
  
  protected void doInEventThread(final Runnable r) {
    if(Display.getCurrent() != null) {
      r.run();
    } else {
      Display.getDefault().syncExec(r);
    }

  }
  
}
