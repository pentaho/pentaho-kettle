 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 package org.pentaho.di.trans.steps.script;


public class ScriptValuesScript {
	
	public static final int NORMAL_SCRIPT= -1;
	public static final int TRANSFORM_SCRIPT= 0;
	public static final int START_SCRIPT= 1;
	public static final int END_SCRIPT= 2;
	
	private int iScriptType;
	private boolean bScriptActive;
	private String sScriptName;
	private String sScript;
	// private Date dModDate;
	// private Date dFirstDate;
	
	public ScriptValuesScript(int iScriptType, String sScriptName, String sScript){
		super();
		this.iScriptType= iScriptType;
		this.sScriptName= sScriptName;
		this.sScript = sScript;
		bScriptActive = true;
		// dModDate = new Date();
		// dFirstDate = new Date();
	}
	
	public int getScriptType(){
		return iScriptType;
	}
	
	public void setScriptType(int iScriptType){
		this.iScriptType = iScriptType;
	}
	
	public String getScript(){
		return this.sScript;
	}
	
	public void setScript(String sScript){
		this.sScript = sScript;
	}
	
	public String getScriptName(){
		return sScriptName;
	}
	
	public void setScriptName(String sScriptName){
		this.sScriptName = sScriptName;
	}
	
	public boolean isTransformScript(){
		if(this.bScriptActive && this.iScriptType==TRANSFORM_SCRIPT) return true;
		else return false;
	}
	
	public boolean isStartScript(){
		if(this.bScriptActive && this.iScriptType==START_SCRIPT) return true;
		else return false;
	}
	
	public boolean isEndScript(){
		if(this.bScriptActive && this.iScriptType==END_SCRIPT) return true;
		else return false;
	}
	
	public boolean isActive(){
		return bScriptActive;
	}
	
}