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
import java.util.List;

import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBinding;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.DialogController;


/**
 *
 * This is the main XulEventHandler for the dialog. It sets up the main bindings for the user interface and responds
 * to some of the main UI events such as closing and accepting the dialog.
 * 
 */
public class MainController extends AbstractXulEventHandler implements DialogController{

	public static final int CANCELLED = 0;
	public static final int OK = 1;
	
	private int lastClicked = CANCELLED;

  private XulButton acceptButton;

  private XulDialog dialog;
  private List<DialogListener> listeners = new ArrayList<DialogListener>();
  
  BindingFactory bf;

  public MainController() {
  }
  
  public boolean getOkClicked() {
  	return lastClicked == OK;
  }

  public void init() {
    createBindings();
  }
  
  public void showDialog(){
    dialog.show();
  }
  
  private void createBindings(){

    dialog = (XulDialog) document.getElementById("repository-explorer-dialog");
    acceptButton = (XulButton) document.getElementById("repository-explorer-dialog_accept");

    try {
      // Fires the population of the repository tree of folders. 
    } catch (Exception e) {
      System.out.println(e.getMessage()); e.printStackTrace();
    }
    
  }

  public void setBindingFactory(BindingFactory bf) {
    this.bf = bf;
  }
  
  public String getName() {
    return "mainController";
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
  
  public void addDialogListener(DialogListener listener){
    if(listeners.contains(listener) == false){
      listeners.add(listener);
    }
  }
  
  public void removeDialogListener(DialogListener listener){
    if(listeners.contains(listener)){
      listeners.remove(listener);
    }
  }

  public void hideDialog() {
    closeDialog();
    
  }
  
}
