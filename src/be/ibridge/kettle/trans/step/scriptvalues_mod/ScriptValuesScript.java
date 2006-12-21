 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 /**********************************************************************
 **                                                                   **
 ** This Script has been modified for higher performance              **
 ** and more functionality in December-2006,                          **
 ** by proconis GmbH / Germany                                        **
 **                                                                   ** 
 ** http://www.proconis.de                                            **
 ** info@proconis.de                                                  **
 **                                                                   **
 **********************************************************************/

package be.ibridge.kettle.trans.step.scriptvalues_mod;


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
