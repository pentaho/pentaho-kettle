package org.pentaho.di.ui.repository.repositoryexplorer;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectCreationException;
import org.pentaho.di.ui.repository.repositoryexplorer.uisupport.IRepositoryExplorerUISupport;

public class UISupportRegistery {

  private static UISupportRegistery instance;
  
  private static Map<Class<? extends IRepositoryService>, Class<? extends IRepositoryExplorerUISupport>> uiSupportMap;

  private UISupportRegistery() {
    uiSupportMap = new HashMap<Class<? extends IRepositoryService>, Class<? extends IRepositoryExplorerUISupport>>();
  }

  public static UISupportRegistery getInstance() {
    if (instance == null) {
      instance = new UISupportRegistery();
    }
    return instance;
  }

  public void registerUISupport(Class<? extends IRepositoryService> service, Class<? extends IRepositoryExplorerUISupport> supportClass) {
    uiSupportMap.put(service, supportClass);
  }

  public IRepositoryExplorerUISupport createUISupport(Class<? extends IRepositoryService> service) throws UIObjectCreationException {
    Class<? extends IRepositoryExplorerUISupport> supportClass = uiSupportMap.get(service);
    if(supportClass != null) {
      return contruct(supportClass);  
    } else {
      return null;
    }
    
  }  
  
  private IRepositoryExplorerUISupport contruct(Class<? extends IRepositoryExplorerUISupport> supportClass) throws UIObjectCreationException {
    try {
      return  (IRepositoryExplorerUISupport) supportClass.newInstance();
    } catch (Throwable th) {
      throw new UIObjectCreationException(th);
      
    }
  }
}
