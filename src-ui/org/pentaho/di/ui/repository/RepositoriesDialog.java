package org.pentaho.di.ui.repository;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.repository.controllers.RepositoriesController;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class RepositoriesDialog {
  private static final Class<?> CLZ = RepositoriesDialog.class;
  private static Log log = LogFactory.getLog(RepositoriesDialog.class);
  private RepositoriesController repositoriesController = new RepositoriesController();
  private XulDomContainer container;
  private ILoginCallback callback;
  private ResourceBundle resourceBundle = new ResourceBundle() {

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject(String key) {
      return BaseMessages.getString(CLZ, key);
    }
    
  };  

  public RepositoriesDialog(Shell shell, String preferredRepositoryName, ILoginCallback callback) {
    try {
      this.callback = callback;
      SwtXulLoader swtLoader = new SwtXulLoader();
      swtLoader.setOuterContext(shell);
      container = new SwtXulLoader().loadXul("org/pentaho/di/ui/repository/xul/repositories.xul", resourceBundle); //$NON-NLS-1$
      final XulRunner runner = new SwtXulRunner();
      runner.addContainer(container);

      BindingFactory bf = new DefaultBindingFactory();
      bf.setDocument(container.getDocumentRoot());
      repositoriesController.setBindingFactory(bf);
      repositoriesController.setPreferredRepositoryName(preferredRepositoryName);
      repositoriesController.setMessages(resourceBundle);
      repositoriesController.setCallback(callback);
      repositoriesController.setShell(getShell());
      container.addEventHandler(repositoriesController);
      
      try {
        runner.initialize();
      } catch (XulException e) {
        SpoonFactory.getInstance().messageBox(e.getLocalizedMessage(), "Service Initialization Failed", false, Const.ERROR);          
        log.error(resourceBundle.getString("RepositoryLoginDialog.ErrorStartingXulApplication"), e);//$NON-NLS-1$
      }
    } catch (XulException e) {
     log.error(resourceBundle.getString("RepositoryLoginDialog.ErrorLoadingXulApplication"), e);//$NON-NLS-1$
    }
  }

  public Composite getDialogArea() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("repository-login-dialog"); //$NON-NLS-1$
    return (Composite) dialog.getManagedObject();
  }

  public void show() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("repository-login-dialog"); //$NON-NLS-1$
    dialog.show();
  }

  public ILoginCallback getCallback() {
    return callback;
  }
  
  public Shell getShell() {
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("repository-login-dialog"); //$NON-NLS-1$
    return (Shell) dialog.getRootObject();
  }
}
