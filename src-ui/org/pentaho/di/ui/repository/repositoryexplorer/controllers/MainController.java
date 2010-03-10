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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorerCallback;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.DialogController;


/**
 *
 * This is the main XulEventHandler for the dialog. It sets up the main bindings for the user interface and responds
 * to some of the main UI events such as closing and accepting the dialog.
 * 
 */
public class MainController extends AbstractXulEventHandler implements DialogController<Object>{
 
  private ResourceBundle messages = new ResourceBundle() {

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject(String key) {
      return BaseMessages.getString(RepositoryExplorer.class, key);
    }
    
  };  
  
  private static Class<?> PKG = RepositoryExplorerDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  private RepositoryExplorerCallback callback;

  public static final int CANCELLED = 0;
	public static final int OK = 1;
	
	private int lastClicked = CANCELLED;

  // private XulButton acceptButton;

  private XulDialog dialog;
  private List<DialogListener<Object>> listeners = new ArrayList<DialogListener<Object>>();
  
  private Repository repository = null;
  
  BindingFactory bf;

  public MainController() {
  }
  
  public boolean getOkClicked() {
  	return lastClicked == OK;
  }

  public void init() {
    bf = new DefaultBindingFactory();
    bf.setDocument(this.getXulDomContainer().getDocumentRoot());
    createBindings();
    
    if(dialog != null && repository != null) {
      dialog.setTitle(BaseMessages.getString(PKG, "RepositoryExplorerDialog.DevTitle", repository.getName())); //$NON-NLS-1$
    }
  }
  
  public void showDialog(){
    dialog.show();
  }
  
  private void createBindings(){

    dialog = (XulDialog) document.getElementById("repository-explorer-dialog");//$NON-NLS-1$
    // acceptButton = (XulButton) document.getElementById("repository-explorer-dialog_accept");
  }

  public RepositoryExplorerCallback getCallback() {
    return callback;
  }

  public void setCallback(RepositoryExplorerCallback callback) {
    this.callback = callback;
  }
  
  public void setRepository(Repository rep) {
    this.repository = rep;
  }
  
  public String getName() {
    return "mainController";//$NON-NLS-1$
  }
  
  @Bindable
  public void closeDialog(){
  	lastClicked = CANCELLED;
    this.dialog.hide();
    
    // listeners may remove themselves, old-style iteration
    for(int i=0; i<listeners.size(); i++){
      listeners.get(i).onDialogCancel();
    }
  }
  
  public void addDialogListener(DialogListener<Object> listener){
    if(listeners.contains(listener) == false){
      listeners.add(listener);
    }
  }
  
  public void removeDialogListener(DialogListener<Object> listener){
    if(listeners.contains(listener)){
      listeners.remove(listener);
    }
  }

  public void hideDialog() {
    closeDialog();
    
  }
}
