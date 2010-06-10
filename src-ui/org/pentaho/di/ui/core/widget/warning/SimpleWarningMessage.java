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
package org.pentaho.di.ui.core.widget.warning;

public class SimpleWarningMessage implements WarningMessageInterface {

	private String warningMessage;
	private boolean warning;
	
	/**
	 * @param warning
	 * @param warningMessage
	 */
	public SimpleWarningMessage(boolean warning, String warningMessage) {
		this.warning = warning;
		this.warningMessage = warningMessage;
	}

	/**
	 * @return the warningMessage
	 */
	public String getWarningMessage() {
		return warningMessage;
	}

	/**
	 * @param warningMessage the warningMessage to set
	 */
	public void setWarningMessage(String warningMessage) {
		this.warningMessage = warningMessage;
	}

	/**
	 * @return the warning
	 */
	public boolean isWarning() {
		return warning;
	}

	/**
	 * @param warning the warning to set
	 */
	public void setWarning(boolean warning) {
		this.warning = warning;
	}
}
