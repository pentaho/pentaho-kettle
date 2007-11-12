/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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
 /**********************************************************************
 **                                                                   **
 ** This Script has been developed for more StyledText Enrichment     **
 ** December-2006 by proconis GmbH / Germany                          **
 **                                                                   ** 
 ** http://www.proconis.de                                            **
 ** info@proconis.de                                                  **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.ui.core.widget;

public class UndoRedoStack {

	
	public final static int DELETE =0;
	public final static int INSERT =1;
	
	private String strNewText;
	private String strReplacedText;
	private int iCursorPosition;
	private int iEventLength;
	private int iType;
	
	public UndoRedoStack(int iCursorPosition, String strNewText, String strReplacedText, int iEventLength, int iType){
		this.iCursorPosition = iCursorPosition;
		this.strNewText = strNewText;
		this.strReplacedText = strReplacedText;
		this.iEventLength = iEventLength;
		this.iType = iType;
	}

	public String getReplacedText(){
		return this.strReplacedText;
	}

	public String getNewText(){
		return this.strNewText;
	}
	
	public int getCursorPosition(){
		return this.iCursorPosition;
	}
	
	public int getEventLength(){
		return iEventLength;
	}
	
	public int getType(){
		return iType;
	}
	
}
