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
package org.pentaho.di.ui.spoon;

import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.w3c.dom.Node;

public class TransFileListener implements FileListener {

	private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    public boolean open(Node transNode, String fname, boolean importfile)
    {
    	final Spoon spoon = Spoon.getInstance();
    	final PropsUI props = PropsUI.getInstance();
        try
        {
            TransMeta transMeta = new TransMeta();
            transMeta.loadXML(transNode, spoon.getRepository(), true, new Variables(), new OverwritePrompter() {
			
            	public boolean overwritePrompt(String message, String rememberText, String rememberPropertyName) {
            		MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
                		Object res[] = spoon.messageDialogWithToggle(BaseMessages.getString(PKG, "System.Button.Yes"), null, message, Const.WARNING, 
                					new String[] { BaseMessages.getString(PKG, "System.Button.Yes"), //$NON-NLS-1$ 
                     						   BaseMessages.getString(PKG, "System.Button.No") },//$NON-NLS-1$
                						1,
                						rememberText,
                						!props.askAboutReplacingDatabaseConnections() );
                		int idx = ((Integer)res[0]).intValue();
                		boolean toggleState = ((Boolean)res[1]).booleanValue();
                        props.setAskAboutReplacingDatabaseConnections(!toggleState);

                        return ((idx&0xFF)==0); // Yes means: overwrite
            	}
            	
			});
            spoon.setTransMetaVariables(transMeta);
            spoon.getProperties().addLastFile(LastUsedFile.FILE_TYPE_TRANSFORMATION, fname, null, false, null);
            spoon.addMenuLast();
            if (!importfile) transMeta.clearChanged();
            transMeta.setFilename(fname);
            spoon.addTransGraph(transMeta);
            spoon.sharedObjectsFileMap.put(transMeta.getSharedObjects().getFilename(), transMeta.getSharedObjects());


            SpoonPerspectiveManager.getInstance().activatePerspective(MainSpoonPerspective.class);
            spoon.refreshTree();
            spoon.refreshHistory();
            return true;
            
        }
        catch(KettleException e)
        {
            new ErrorDialog(spoon.getShell(), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorOpening.Title"), BaseMessages.getString(PKG, "Spoon.Dialog.ErrorOpening.Message")+fname, e);
        }
        return false;
    }

    public boolean save(EngineMetaInterface meta, String fname,boolean export) {
    	Spoon spoon = Spoon.getInstance();
    	EngineMetaInterface lmeta;
    	if (export)
    	{
    		lmeta = (TransMeta)((TransMeta)meta).realClone(false);
    	}
    	else
    		lmeta = meta;
    	return spoon.saveMeta(lmeta, fname);
    }
    
    public void syncMetaName(EngineMetaInterface meta,String name) {
    	((TransMeta)meta).setName(name);
    }

    public boolean accepts(String fileName) {
      if(fileName == null || fileName.indexOf('.') == -1){
        return false;
      }
      String extension = fileName.substring(fileName.lastIndexOf('.')+1);
      return extension.equals("ktr");
    }
    
    public boolean acceptsXml(String nodeName){
      if(nodeName.equals("transformation")){
        return true;
      }
      return false;
    }

    public String[] getFileTypeDisplayNames(Locale locale) {
      return new String[]{"Transformations", "XML"};
    }

    public String getRootNodeName() {
      return "transformation";
    }

    public String[] getSupportedExtensions() {
      return new String[]{"ktr", "xml"};
    }
    
    

}
