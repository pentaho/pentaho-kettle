/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon;

import java.util.Locale;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.w3c.dom.Node;

public class JobFileListener implements FileListener {

	private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
    public boolean open(Node jobNode, String fname, boolean importfile)
    {
    	Spoon spoon = Spoon.getInstance();
        try
        {
            JobMeta jobMeta = new JobMeta();
            jobMeta.loadXML(jobNode, spoon.getRepository(), spoon);
            jobMeta.setRepositoryDirectory(spoon.getDefaultSaveLocation(jobMeta));
            spoon.setJobMetaVariables(jobMeta);
            spoon.getProperties().addLastFile(LastUsedFile.FILE_TYPE_JOB, fname, null, false, null);
            spoon.addMenuLast();
            if (!importfile) jobMeta.clearChanged();
            jobMeta.setFilename(fname);
            spoon.delegates.jobs.addJobGraph(jobMeta);
            
            spoon.refreshTree();
            SpoonPerspectiveManager.getInstance().activatePerspective(MainSpoonPerspective.class);
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
    		lmeta = (JobMeta)((JobMeta)meta).realClone(false);
    	}
    	else
    		lmeta = meta;
    	
    	return spoon.saveMeta(lmeta, fname);
    }
    
    public void syncMetaName(EngineMetaInterface meta,String name) {
    	((JobMeta)meta).setName(name);
    }

    public boolean accepts(String fileName) {
      if(fileName == null || fileName.indexOf('.') == -1){
        return false;
      }
      String extension = fileName.substring(fileName.lastIndexOf('.')+1);
      return extension.equals("kjb");
    }
    

    public boolean acceptsXml(String nodeName) {
      return nodeName.equals("job");
    }

    public String[] getFileTypeDisplayNames(Locale locale) {
      return new String[]{"Jobs", "XML"};
    }

    public String[] getSupportedExtensions() {
      return new String[]{"kjb", "xml"};
    }

    public String getRootNodeName() {
      return "job";
    }
    
    
}
