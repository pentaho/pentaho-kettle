/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.ui.repository;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.ui.core.dialog.EnterStringDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

public class RepositorySecurityUI {
	
	/**
	 * Verify a repository operation, show an error dialog if needed.
	 * 
	 * @param repositoryMeta The repository meta object
	 * @param userinfo The user information
	 * @param operations the operations you want to perform with the supplied user.
	 * 
	 * @return true if there is an error, false if all is OK.
	 */
	public static boolean verifyOperations(Shell shell, Repository repository, RepositoryOperation...operations) {
		
		String operationsDesc = "[";
		for (RepositoryOperation operation : operations) {
			if (operationsDesc.length()>1) operationsDesc+=", ";
			operationsDesc+=operation.getDescription();
		}
		operationsDesc+="]";
		
		try {
			if (repository==null) {
				return false; // always OK if there is no repository.
			}
			repository.getSecurityProvider().validateAction(operations);
		} catch(KettleException e) {
			new ErrorDialog(shell, "Security error", "There was a security error performing operations:"+Const.CR+operationsDesc, e);
			return true;
		}
		return false;
	}

	public static String getVersionComment(Shell shell, Repository repository, String operationDescription) {
		if (repository==null) {
			return null;
		}
		
		RepositorySecurityProvider provider = repository.getSecurityProvider();
		if (provider.allowsVersionComments()) {
			
			String explanation = "Enter a comment ";
			if (provider.isVersionCommentMandatory()) {
				explanation+="(Mandatory) : ";
			} else {
				explanation+=": ";
			}
			String versionComment = Const.VERSION_COMMENT_EDIT_VERSION+" of ["+operationDescription+"]";
			
			
			
			EnterStringDialog dialog = new EnterStringDialog(shell, versionComment, "Enter comment", explanation);
			dialog.setManditory(provider.isVersionCommentMandatory());
			versionComment = dialog.open();
			
			return versionComment;
		}
		return null;
	}

	/**
	 * @param shell the parent shell.
	 * @return true if we need to retry, false if we need to cancel the operation.
	 */
	public static boolean showVersionCommentMandatoryDialog(Shell shell) {
		MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_ERROR);
		box.setMessage("Version comments are mandatory for this repository."+Const.CR+"Do you want to enter a comment?");
		box.setText("Version comments are mandatory!");
		return box.open() == SWT.YES;
	}

}
