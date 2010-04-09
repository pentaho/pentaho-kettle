package org.pentaho.di.ui.repository;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryDialogInterface;
import org.pentaho.di.ui.repository.dialog.RepositoryDialogInterface.MODE;
import org.pentaho.di.ui.repository.model.RepositoriesModel;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.util.XulDialogCallback;

public class RepositoriesHelper {
  private static Class<?> PKG = RepositoriesHelper.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  private Shell shell;
  private PropsUI props;
  private RepositoriesMeta input;
  private Repository repository;
  private String prefRepositoryName;
  private ResourceBundle messages;
  private RepositoriesModel model;
  private Document document;
  private LogChannel log;  
  public RepositoriesHelper(RepositoriesModel model
      , Document document, ResourceBundle messages, Shell shell) {
    this.props = PropsUI.getInstance();
    this.input = new RepositoriesMeta();
    this.repository = null;
    this.model = model;
    this.document = document;
    this.messages = messages;
    this.shell = shell;
    log = new LogChannel("RepositoriesHelper"); //$NON-NLS-1$
    try {
      try {
        this.input.readData();
      } catch (KettleException e) {
        log.logDetailed(BaseMessages.getString(PKG, "RepositoryLogin.ErrorReadingRepositoryDefinitions", e.getLocalizedMessage()));//$NON-NLS-1$
        new ErrorDialog(shell, messages.getString("Dialog.Error"), BaseMessages.getString(PKG, "RepositoryLogin.ErrorReadingRepositoryDefinitions", e.getLocalizedMessage()), e); //$NON-NLS-1$ //$NON-NLS-2$
      }      
      List<RepositoryMeta> repositoryList = new ArrayList<RepositoryMeta>();
      for(int i=0; i<this.input.nrRepositories();i++) {
        repositoryList.add(this.input.getRepository(i));
      }
      model.setAvailableRepositories(repositoryList);
    } catch(Exception e) {
      log.logDetailed(BaseMessages.getString(PKG, "RepositoryLogin.ErrorReadingRepositoryDefinitions", e.getLocalizedMessage()));//$NON-NLS-1$
      new ErrorDialog(shell, messages.getString("Dialog.Error"), BaseMessages.getString(PKG, "RepositoryLogin.ErrorReadingRepositoryDefinitions", e.getLocalizedMessage()), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }
  public void newRepository() {
	PluginRegistry registry = PluginRegistry.getInstance();
	Class<? extends PluginTypeInterface> pluginType = RepositoryPluginType.class;
	List<PluginInterface> plugins = registry.getPlugins(pluginType);
	
	String[] names = new String[plugins.size()];
    for (int i = 0; i < names.length; i++) {
      PluginInterface plugin = plugins.get(i);
      names[i] = plugin.getName() + " : " + plugin.getDescription(); //$NON-NLS-1$
    }

    // TODO: make this a bit fancier!
    EnterSelectionDialog selectRepositoryType = new EnterSelectionDialog(this.shell, names,
        messages.getString("RepositoryLogin.SelectRepositoryType"), messages.getString("RepositoryLogin.SelectRepositoryTypeCreate")); //$NON-NLS-1$//$NON-NLS-2$
    String choice = selectRepositoryType.open();
    if (choice != null) {
      int index = selectRepositoryType.getSelectionNr();
      PluginInterface plugin = plugins.get(index);
      String id = plugin.getIds()[0];

      try {
        // With this ID we can create a new Repository object...
        //
        RepositoryMeta repositoryMeta = PluginRegistry.getInstance().loadClass(RepositoryPluginType.class, id, RepositoryMeta.class);
        RepositoryDialogInterface dialog = getRepositoryDialog(plugin, repositoryMeta, input, this.shell);
        RepositoryMeta meta = dialog.open(MODE.ADD);
        if (meta != null) {
          // If the repository meta is not null and the repository name does not exist in the repositories list. 
          // If it does then display a error to the user
          if(meta.getName() != null) {
            input.addRepository(meta);
            fillRepositories();
            model.setSelectedRepository(meta);
            writeData();            
          }
        }
      } catch (Exception e) {
        log.logDetailed(BaseMessages.getString(PKG, "RepositoryLogin.ErrorCreatingRepository", e.getLocalizedMessage()));//$NON-NLS-1$
        new ErrorDialog(shell, messages.getString("Dialog.Error"), BaseMessages.getString(PKG, "RepositoryLogin.ErrorCreatingRepository", e.getLocalizedMessage()), e); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }

  public void editRepository() {
    try {
        PluginInterface plugin = null; 
        RepositoryMeta ri = input.searchRepository(model.getSelectedRepository().getName());
        if (ri != null) {
          plugin = PluginRegistry.getInstance().getPlugin(RepositoryPluginType.class, ri.getId());
          if (plugin == null) {
            throw new KettleException(BaseMessages.getString(PKG, "RepositoryLogin.ErrorFindingPlugin", ri.getId())); //$NON-NLS-1$
          }
        }
          RepositoryDialogInterface dd = getRepositoryDialog(plugin, ri, input, this.shell);
          if (dd.open(MODE.EDIT) != null) {
            fillRepositories();
            int idx = input.indexOfRepository(ri);
            model.setSelectedRepository(input.getRepository(idx));
            writeData();
          }
    } catch (Exception e) {
      log.logDetailed(BaseMessages.getString(PKG, "RepositoryLogin.ErrorEditingRepository", e.getLocalizedMessage()));//$NON-NLS-1$
      new ErrorDialog(shell, messages.getString("Dialog.Error"), BaseMessages.getString(PKG, "RepositoryLogin.ErrorEditingRepository", e.getLocalizedMessage()), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public void deleteRepository() {
    try {
        XulConfirmBox confirmBox = (XulConfirmBox) document.createElement("confirmbox");//$NON-NLS-1$
        final RepositoryMeta repositoryMeta = input.searchRepository(model.getSelectedRepository().getName());
        if (repositoryMeta != null) {
          confirmBox.setTitle(messages.getString("RepositoryLogin.ConfirmDeleteRepositoryDialog.Title"));//$NON-NLS-1$
          confirmBox.setMessage(messages.getString("RepositoryLogin.ConfirmDeleteRepositoryDialog.Message"));//$NON-NLS-1$
          confirmBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
          confirmBox.setCancelLabel(messages.getString("Dialog.Cancel"));//$NON-NLS-1$
          confirmBox.addDialogCallback(new XulDialogCallback<Object>() {
    
            public void onClose(XulComponent sender, Status returnCode, Object retVal) {
              if (returnCode == Status.ACCEPT) {
                int idx = input.indexOfRepository(repositoryMeta);
                input.removeRepository(idx);
                fillRepositories();
                writeData();
              }
            }
    
            public void onError(XulComponent sender, Throwable t) {
              log.logDetailed(BaseMessages.getString(PKG, "RepositoryLogin.UnableToDeleteRepository", t.getLocalizedMessage()));//$NON-NLS-1$
              new ErrorDialog(shell, messages.getString("Dialog.Error"), BaseMessages.getString(PKG, "RepositoryLogin.UnableToDeleteRepository", t.getLocalizedMessage()), t);//$NON-NLS-1$ //$NON-NLS-2$
            }
          });
          confirmBox.open();
        }
    } catch (Exception e) {
      log.logDetailed(BaseMessages.getString(PKG, "RepositoryLogin.UnableToDeleteRepository", e.getLocalizedMessage()));//$NON-NLS-1$
      new ErrorDialog(shell, messages.getString("Dialog.Error"), BaseMessages.getString(PKG, "RepositoryLogin.UnableToDeleteRepository", e.getLocalizedMessage()), e); //$NON-NLS-1$ //$NON-NLS-2$
    }      
  }

  protected RepositoryDialogInterface getRepositoryDialog(PluginInterface plugin, RepositoryMeta repositoryMeta, RepositoriesMeta input2, Shell shell) throws Exception {
	String className = repositoryMeta.getDialogClassName();
    Class<? extends RepositoryDialogInterface> dialogClass = PluginRegistry.getInstance().getClass(plugin, className);
    Constructor<?> constructor = dialogClass.getConstructor(Shell.class, Integer.TYPE, RepositoryMeta.class, RepositoriesMeta.class);
    return (RepositoryDialogInterface) constructor.newInstance(new Object[] { shell, Integer.valueOf(SWT.NONE), repositoryMeta, input, });
  }
  

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getMetaData() {
    fillRepositories();

    String repname = props.getLastRepository();
    if (repname != null) {
      model.setSelectedRepositoryUsingName(repname);
      String username = props.getLastRepositoryLogin();
      if (username != null) {
        model.setUsername(username);
      }
    }

    // Do we have a preferred repository name to select
    if (prefRepositoryName != null) {
      model.setSelectedRepositoryUsingName(prefRepositoryName);
    }

    model.setShowDialogAtStartup(props.showRepositoriesDialogAtStartup());
    
  }
  

  
  public void fillRepositories() {
    model.getAvailableRepositories().clear();
    if(input.nrRepositories() == 0) {
      model.addToAvailableRepositories(null);
    } else {
      for (int i = 0; i < input.nrRepositories(); i++) {
        model.addToAvailableRepositories(input.getRepository(i));
      }
    }
  }
  
  public Repository getConnectedRepository() {
    return repository;
  }

  public void setPreferredRepositoryName(String repname) {
    prefRepositoryName = repname;
  }
  
  public void loginToRepository() throws KettleException, KettleSecurityException{
    if(model != null && model.getSelectedRepository() != null) {
      RepositoryMeta repositoryMeta = input.getRepository(model.getRepositoryIndex(model.getSelectedRepository()));
      repository = PluginRegistry.getInstance().loadClass(RepositoryPluginType.class, repositoryMeta.getId(), Repository.class);
      repository.init(repositoryMeta);
      repository.connect(model.getUsername(), model.getPassword());
      props.setLastRepository(repositoryMeta.getName());
      props.setLastRepositoryLogin(model.getUsername());
    } else {
      log.logDetailed(BaseMessages.getString(PKG, "RepositoryLogin.ErrorLoginToRepository"));//$NON-NLS-1$
      throw new KettleException(BaseMessages.getString(PKG, "RepositoryLogin.ErrorLoginToRepository"));//$NON-NLS-1$
    }
  }
  
  public void updateShowDialogOnStartup(boolean value) {
    props.setRepositoriesDialogAtStartupShown(value);
  }
  
  private void writeData() {
    try {
      input.writeData();
    } catch (Exception e) {
      log.logDetailed(BaseMessages.getString(PKG, "RepositoryLogin.ErrorSavingRepositoryDefinition", e.getLocalizedMessage())); //$NON-NLS-1$
      new ErrorDialog(shell, messages.getString("Dialog.Error"), BaseMessages.getString(PKG, "RepositoryLogin.ErrorSavingRepositoryDefinition", e.getLocalizedMessage()), e); //$NON-NLS-1$ //$NON-NLS-2$
    } 
  }
}
