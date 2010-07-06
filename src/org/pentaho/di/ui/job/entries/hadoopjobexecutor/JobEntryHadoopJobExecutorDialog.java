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

package org.pentaho.di.ui.job.entries.hadoopjobexecutor;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.dom4j.DocumentException;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.hadoopjobexecutor.JobEntryHadoopJobExecutor;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.job.entries.hadoopjobexecutor.JobEntryHadoopJobExecutorController.AdvancedConfiguration;
import org.pentaho.di.ui.job.entries.hadoopjobexecutor.JobEntryHadoopJobExecutorController.SimpleConfiguration;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class JobEntryHadoopJobExecutorDialog extends JobEntryDialog implements JobEntryDialogInterface {
	private static final Class<?> CLZ = JobEntryHadoopJobExecutor.class;
	
	private JobEntryHadoopJobExecutor jobEntry;

	private JobEntryHadoopJobExecutorController controller = new JobEntryHadoopJobExecutorController();
	
	private XulDomContainer container;
	
	private BindingFactory bf;

	private ResourceBundle resourceBundle = new ResourceBundle() {
		@Override
		public Enumeration<String> getKeys() {
		  return null;
		}
		
		@Override
		protected Object handleGetObject(String key) {
		  return BaseMessages.getString(CLZ, key);
		}
	};
	
	public JobEntryHadoopJobExecutorDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta) throws XulException, DocumentException {
		super(parent, jobEntry, rep, jobMeta);
		
		this.jobEntry = (JobEntryHadoopJobExecutor)jobEntry;
		
		SwtXulLoader swtXulLoader = new SwtXulLoader();
		swtXulLoader.registerClassLoader(getClass().getClassLoader());
	    swtXulLoader.setOuterContext(shell);

	    container = swtXulLoader.loadXul("org/pentaho/di/ui/job/entries/hadoopjobexecutor/JobEntryHadoopJobExecutorDialog.xul", resourceBundle); //$NON-NLS-1$

	    final XulRunner runner = new SwtXulRunner();
	    runner.addContainer(container);
	    
	    container.addEventHandler(controller);
	    
	    bf = new DefaultBindingFactory();
	    bf.setDocument(container.getDocumentRoot());
	    bf.setBindingType(Type.BI_DIRECTIONAL);
	    
	    bf.createBinding("jobentry-name", "value", controller, JobEntryHadoopJobExecutorController.STEP_NAME); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    bf.createBinding("jobentry-hadoopjob-name", "value", controller, JobEntryHadoopJobExecutorController.HADOOP_JOB_NAME); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    bf.createBinding("jar-url", "value", controller, JobEntryHadoopJobExecutorController.JAR_URL); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	    bf.createBinding("command-line-arguments", "value", controller.getSimpleConfiguration(), SimpleConfiguration.CMD_LINE_ARGS); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    
	    bf.createBinding("classes-output-key-class", "value", controller.getAdvancedConfiguration(), AdvancedConfiguration.OUTPUT_KEY_CLASS); //$NON-NLS-1$ //$NON-NLS-2$
	    bf.createBinding("classes-output-key-value", "value", controller.getAdvancedConfiguration(), AdvancedConfiguration.OUTPUT_KEY_VALUE); //$NON-NLS-1$ //$NON-NLS-2$
	    bf.createBinding("classes-mapper-class", "value", controller.getAdvancedConfiguration(), AdvancedConfiguration.MAPPER_CLASS); //$NON-NLS-1$ //$NON-NLS-2$
	    bf.createBinding("classes-combiner-class", "value", controller.getAdvancedConfiguration(), AdvancedConfiguration.COMBINER_CLASS); //$NON-NLS-1$ //$NON-NLS-2$
	    bf.createBinding("classes-reducer-class", "value", controller.getAdvancedConfiguration(), AdvancedConfiguration.REDUCER_CLASS); //$NON-NLS-1$ //$NON-NLS-2$
	    bf.createBinding("classes-input-format", "value", controller.getAdvancedConfiguration(), AdvancedConfiguration.INPUT_FORMAT); //$NON-NLS-1$ //$NON-NLS-2$
	    bf.createBinding("classes-output-format", "value", controller.getAdvancedConfiguration(), AdvancedConfiguration.OUTPUT_FORMAT); //$NON-NLS-1$ //$NON-NLS-2$
	    
//	    bf.createBinding("", "value", controller, ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    
	}

	public JobEntryInterface open() {
		XulDialog dialog = (XulDialog) container.getDocumentRoot().getElementById("job-entry-dialog"); //$NON-NLS-1$
	    dialog.show();
		
	    return jobEntry;
	}

}
