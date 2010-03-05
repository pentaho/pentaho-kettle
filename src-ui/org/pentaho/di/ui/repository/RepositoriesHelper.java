package org.pentaho.di.ui.repository;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
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
import org.pentaho.di.ui.repository.dialog.RepositoryDialogInterface;
import org.pentaho.di.ui.repository.model.RepositoriesModel;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulMessageBox;
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
  private XulMessageBox messageBox;
  private XulConfirmBox confirmBox;
  private LogChannel log;  
  public RepositoriesHelper(RepositoriesModel model
      , XulMessageBox messagebox, XulConfirmBox confirmBox, ResourceBundle messages, Shell shell) {
    this.props = PropsUI.getInstance();
    this.input = new RepositoriesMeta();
    this.repository = null;
    this.model = model;
    this.messageBox = messagebox;
    this.confirmBox = confirmBox;
    this.messages = messages;
    this.shell = shell;
    log = new LogChannel("RepositoriesHelper"); //$NON-NLS-1$
    try {
      this.input.readData();
      List<RepositoryMeta> repositoryList = new ArrayList<RepositoryMeta>();
      for(int i=0; i<this.input.nrRepositories();i++) {
        repositoryList.add(this.input.getRepository(i));
      }
      model.setAvailableRepositories(repositoryList);
    } catch (Exception e) {
      log.logDetailed("Error Reading Repositories Data: " + e.getLocalizedMessage());
      messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
      messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
      messageBox.setMessage(BaseMessages.getString(PKG, "RepositoryLogin.ErrorReadingRepositoryDefinitions", e.getLocalizedMessage()));//$NON-NLS-1$
      messageBox.open();
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
    //
    EnterSelectionDialog selectRepositoryType = new EnterSelectionDialog(this.shell, names, "Select the repository type", "Select the repository type to create"); //$NON-NLS-1$//$NON-NLS-2$
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
        RepositoryMeta meta = dialog.open();
        if (meta != null) {
          input.addRepository(meta);
          fillRepositories();
          model.setSelectedRepository(meta);
          writeData();
        }
      } catch (Exception e) {
        log.logDetailed("Error creating new repository: " + e.getLocalizedMessage());
        messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
        messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
        messageBox.setMessage(BaseMessages.getString(PKG, "RepositoryLogin.ErrorCreatingRepository", e.getLocalizedMessage()));//$NON-NLS-1$
        messageBox.open();
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
            throw new KettleException("Unable to find repository plugin for id [" + ri.getId() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
          RepositoryDialogInterface dd = getRepositoryDialog(plugin, ri, input, this.shell);
          if (dd.open() != null) {
            fillRepositories();
            int idx = input.indexOfRepository(ri);
            model.setSelectedRepository(input.getRepository(idx));
            writeData();
          }
    } catch (Exception e) {
      log.logDetailed("Error edit repository : " + e.getLocalizedMessage());
      messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
      messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
      messageBox.setMessage(BaseMessages.getString(PKG, "RepositoryLogin.ErrorEditingRepository", e.getLocalizedMessage()));//$NON-NLS-1$
      messageBox.open();
    }
  }

  public void deleteRepository() {

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
            log.logDetailed("Error deleting repository : " + t.getLocalizedMessage());
            messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
            messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
            messageBox.setMessage(BaseMessages.getString(RepositoryExplorer.class,
                "RepositoryLogin.UnableToDeleteRepository", t.getLocalizedMessage()));//$NON-NLS-1$
            messageBox.open();
          }
        });
        confirmBox.open();
      }
      
  }
  protected RepositoryDialogInterface getRepositoryDialog(PluginInterface plugin, RepositoryMeta repositoryMeta, RepositoriesMeta input2, Shell shell) throws Exception {
    Class<? extends RepositoryDialogInterface> dialogClass = PluginRegistry.getInstance().getClass(plugin, RepositoryDialogInterface.class);
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
    for (int i = 0; i < input.nrRepositories(); i++) {
      model.addToAvailableRepositories(input.getRepository(i));
    }
  }
  
  public Repository getConnectedRepository() {
    return repository;
  }

  public void setPreferredRepositoryName(String repname) {
    prefRepositoryName = repname;
  }
  
  public void loginToRepository() throws KettleException {
      RepositoryMeta repositoryMeta = input.getRepository(model.getRepositoryIndex(model.getSelectedRepository()));
      repository = PluginRegistry.getInstance().loadClass(RepositoryPluginType.class, repositoryMeta.getId(), Repository.class);
      repository.init(repositoryMeta);
      repository.connect(model.getUsername(), model.getPassword());
      props.setLastRepository(repositoryMeta.getName());
      props.setLastRepositoryLogin(model.getUsername());
  }
  
  public void updateShowDialogOnStartup(boolean value) {
    props.setRepositoriesDialogAtStartupShown(value);
  }
  
  private void writeData() {
    try {
      input.writeData();
    } catch (Exception e) {
      log.logDetailed("Error deleting repository : " + e.getLocalizedMessage());
      messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
      messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
      messageBox.setMessage(messages.getString("RepositoryLogin.ErrorSavingRepositoryDefinition"));//$NON-NLS-1$
      messageBox.open();
    } 
  }
}
