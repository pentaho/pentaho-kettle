/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.RepositoryUserInterface;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.ui.repository.dialog.UserDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorerCallback;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryUsers;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtDialog;


/**
 *
 * This is the XulEventHandler for the browse panel of the repository explorer. It sets up the bindings for  
 * browse functionality.
 * 
 */
public class SecurityController extends AbstractXulEventHandler{

  private XulTree userTable;

  private RepositoryUserInterface rui; 
  private UIRepositoryUsers users;
  private RepositoryExplorerCallback callback;
  
  BindingFactory bf;
  Binding userBinding;
  
  public SecurityController() {
  }
  
  public void init() {
    createBindings();
  }
  
  private void createBindings(){
    userTable = (XulTree) document.getElementById("user-table");
    users = new UIRepositoryUsers(rui);

    // Bind the repository folder structure to the folder tree.
    bf.setBindingType(Binding.Type.ONE_WAY);
    userBinding = bf.createBinding(users, "children", userTable, "elements");
    
    bf.setBindingType(Binding.Type.ONE_WAY);
    BindingConvertor<int[], Boolean> forButtons = new BindingConvertor<int[], Boolean>() {

      @Override
      public Boolean sourceToTarget(int[] value) {
        return value != null && !(value.length<=0);
      }

      @Override
      public int[] targetToSource(Boolean value) {
        return null;
      }
    };
    bf.createBinding("user-table", "selectedRows", "user-edit", "!disabled", forButtons);
    bf.createBinding("user-table", "selectedRows", "user-remove", "!disabled", forButtons);
    
    try {
      // Fires the population of the repository tree of folders. 
      userBinding.fireSourceChanged();
    } catch (Exception e) {
      System.out.println(e.getMessage()); e.printStackTrace();
    }
  }

  public void setBindingFactory(BindingFactory bf) {
    this.bf = bf;
  }
  
  public String getName() {
    return "securityController";
  }
  
  public void setCallback(RepositoryExplorerCallback callback) {
    this.callback = callback;
  }

  public RepositoryUserInterface getRepositoryUserInterface() {
    return rui;
  }

  public void setRepositoryUserInterface(RepositoryUserInterface rui) {
    this.rui = rui;
  }

  public void newUser()throws Exception{
    UserDialog ud = new UserDialog(getShell(), SWT.NONE, (RepositorySecurityProvider)rui, new UserInfo());
    
    UserInfo ui = ud.open();
    if (ui!=null){
      users.add(new UIRepositoryUser(ui));
      users.sort(new UserComparator());
    }
    
  }
  
  public void editUser(){
    try {
      UIRepositoryUser user = (UIRepositoryUser)userTable.getSelectedItems().iterator().next();
      int index = users.indexOf(user);
      String prevLogin = user.getLogin();
      
      UserDialog ud = new UserDialog(getShell(), SWT.NONE, (RepositorySecurityProvider)rui, user.getUserInfo());
      UserInfo ui = ud.open();
      if (ui!=null){
        rui.saveUserInfo(ui);
      }
      if(ui!=null){
        users.remove(index);
        users.add(index, new UIRepositoryUser(ui));
        if (!ui.getLogin().equalsIgnoreCase(prevLogin)){
          users.sort(new UserComparator());
        }
      }
    }
    catch(Exception e){
      // TODO: deal with exceptions
    }
  }
 
  public void removeUser(){
    try{
      UIRepositoryUser user = (UIRepositoryUser)userTable.getSelectedItems().iterator().next();
      int index = users.indexOf(user);

      rui.delUser(user.getUserInfo().getObjectId());
      users.remove(index);
    }
    catch(Exception e){
      // TODO: deal with exceptions
    }
  }

  
  /**
   * TODO: get rid of this as soon as possible
   * @return
   */
  private Shell getShell(){
    SwtDialog dialog = (SwtDialog) this.getXulDomContainer().getDocumentRoot().getElementById("repository-explorer-dialog");
    return dialog.getShell();
    
  }

  class UserComparator implements Comparator<UIRepositoryUser>{

    public int compare(UIRepositoryUser user1, UIRepositoryUser user2) {
      String nameOne = user1.getLogin();
      String nameTwo = user2.getLogin();
      return nameOne.compareTo(nameTwo);
    }
    
  }

}
