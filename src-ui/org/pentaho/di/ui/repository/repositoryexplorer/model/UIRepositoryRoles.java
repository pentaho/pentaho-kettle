package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.List;

import org.pentaho.di.repository.IRole;
import org.pentaho.di.repository.RepositorySecurityManager;

public class UIRepositoryRoles extends AbstractModelNode<UIRepositoryRole> {
  
    public UIRepositoryRoles(){
    }
    
    public UIRepositoryRoles(List<UIRepositoryRole> roles){
      super(roles);
    }

    public UIRepositoryRoles(RepositorySecurityManager rsm) {

      List<IRole> roleList; 
      try {
        roleList = rsm.getRoles();
        for (IRole role : roleList) {
        this.add(new UIRepositoryRole(role));
        }
      } catch (Exception e) {
        // TODO: handle exception; can't get users???
      }
    }
    
    @Override
    protected void fireCollectionChanged() {
      this.changeSupport.firePropertyChange("children", null, this.getChildren());
    }

}
