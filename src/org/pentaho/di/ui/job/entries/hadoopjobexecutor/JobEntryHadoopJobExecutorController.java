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

import java.util.List;

import org.pentaho.di.job.entries.hadoopjobexecutor.JobEntryHadoopJobExecutor.UserDefinedItem;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class JobEntryHadoopJobExecutorController extends AbstractXulEventHandler {
	public static final String STEP_NAME = "step_name"; //$NON-NLS-1$
	public static final String HADOOP_JOB_NAME = "hadoop_job_name"; //$NON-NLS-1$
	public static final String JAR_URL = "jar_url"; //$NON-NLS-1$
	public static final String IS_SIMPLE = "is_simple"; //$NON-NLS-1$
	
	private String stepName;
	private String hadoopJobName;
	private String jarUrl;
	
	private boolean isSimple;
	
	private IConfiguration conf;
	
	@Override
	public String getName() {
		return "jobEntryController"; //$NON-NLS-1$
	}
	
	public String getStepName() {
		return stepName;
	}

	public void setStepName(String stepName) {
		String previousVal = this.stepName;
		String newVal = stepName;
		
		this.stepName = stepName;
		firePropertyChange(JobEntryHadoopJobExecutorController.STEP_NAME, previousVal, newVal);
	}

	public String getHadoopJobName() {
		return hadoopJobName;
	}

	public void setHadoopJobName(String hadoopJobName) {
		String previousVal = this.hadoopJobName;
		String newVal = hadoopJobName;
		
		this.hadoopJobName = hadoopJobName;
		firePropertyChange(JobEntryHadoopJobExecutorController.HADOOP_JOB_NAME, previousVal, newVal);
	}

	public String getJarUrl() {
		return jarUrl;
	}

	public void setJarUrl(String jarUrl) {
		String previousVal = this.jarUrl;
		String newVal = jarUrl;
		
		this.jarUrl = jarUrl;
		firePropertyChange(JobEntryHadoopJobExecutorController.JAR_URL, previousVal, newVal);
	}

	public boolean isSimple() {
		return isSimple;
	}

	public void setSimple(boolean isSimple) {
		boolean previousVal = this.isSimple;
		boolean newVal = isSimple;
		
		this.isSimple = isSimple;
		firePropertyChange(JobEntryHadoopJobExecutorController.IS_SIMPLE, previousVal, newVal);
	}

	public interface IConfiguration {
		public String getCommandLineArgs();
		public void setCommandLineARgs(String cmdLineArgs);
	}
	
	public class SimpleConfiguration extends XulEventSourceAdapter implements IConfiguration {
		public static final String CMD_LINE_ARGS = "command_line_args"; //$NON-NLS-1$
		
		private String cmdLineArgs;

		public String getCommandLineArgs() {
			return cmdLineArgs;
		}

		public void setCommandLineARgs(String cmdLineArgs) {
			String previousVal = this.cmdLineArgs;
			String newVal = cmdLineArgs;
			
			this.cmdLineArgs = cmdLineArgs;
			
			firePropertyChange(SimpleConfiguration.CMD_LINE_ARGS, previousVal, newVal);
		}
	}
	
	public class AdvancedConfiguration extends SimpleConfiguration{
		public static final String OUTPUT_KEY_CLASS = "output_key_class"; //$NON-NLS-1$
		public static final String OUTPUT_KEY_VALUE = "output_key_value"; //$NON-NLS-1$
		public static final String MAPPER_CLASS = "mapper_class"; //$NON-NLS-1$
		public static final String COMBINER_CLASS = "combiner_class"; //$NON-NLS-1$
		public static final String REDUCER_CLASS = "reducer_class"; //$NON-NLS-1$
		public static final String INPUT_FORMAT = "input_format"; //$NON-NLS-1$
		public static final String OUTPUT_FORMAT = "output_format"; //$NON-NLS-1$
		public static final String WORKING_DIRECTORY = "working_directory"; //$NON-NLS-1$
		public static final String FS_DEFAULT_NAME = "fs_default_name"; //$NON-NLS-1$
		public static final String INPUT_PATH = "input_path"; //$NON-NLS-1$
		public static final String OUTPUT_PATH = "output_path"; //$NON-NLS-1$
		public static final String USER_DEFINED = "user_defined"; //$NON-NLS-1$
		
		private String outputKeyClass;
		private String outputKeyValue;
		private String mapperClass;
		private String combinerClass;
		private String reducerClass;
		private String inputFormat;
		private String outputFormat;
		
		private String workingDirectory;
		private String fsDefaultName;
		private String inputPath;
		private String outputPath;
		
		private List<UserDefinedItem> userDefined;

		public String getOutputKeyClass() {
			return outputKeyClass;
		}

		public void setOutputKeyClass(String outputKeyClass) {
			String previousVal = this.outputKeyClass;
			String newVal = outputKeyClass;
			
			this.outputKeyClass = outputKeyClass;
			firePropertyChange(AdvancedConfiguration.OUTPUT_KEY_CLASS, previousVal, newVal);
		}

		public String getOutputKeyValue() {
			return outputKeyValue;
		}

		public void setOutputKeyValue(String outputKeyValue) {
			String previousVal = this.outputKeyValue;
			String newVal = outputKeyValue;
			
			this.outputKeyValue = outputKeyValue;
			firePropertyChange(AdvancedConfiguration.OUTPUT_KEY_VALUE, previousVal, newVal);
		}

		public String getMapperClass() {
			return mapperClass;
		}

		public void setMapperClass(String mapperClass) {
			String previousVal = this.mapperClass;
			String newVal = mapperClass;
			
			this.mapperClass = mapperClass;
			firePropertyChange(AdvancedConfiguration.MAPPER_CLASS, previousVal, newVal);
		}

		public String getCombinerClass() {
			return combinerClass;
		}

		public void setCombinerClass(String combinerClass) {
			String previousVal = this.combinerClass;
			String newVal = combinerClass;
			
			this.combinerClass = combinerClass;
			firePropertyChange(AdvancedConfiguration.COMBINER_CLASS, previousVal, newVal);
		}

		public String getReducerClass() {
			return reducerClass;
		}

		public void setReducerClass(String reducerClass) {
			String previousVal = this.reducerClass;
			String newVal = reducerClass;
			
			this.reducerClass = reducerClass;
			firePropertyChange(AdvancedConfiguration.REDUCER_CLASS, previousVal, newVal);
		}

		public String getInputFormat() {
			return inputFormat;
		}

		public void setInputFormat(String inputFormat) {
			String previousVal = this.inputFormat;
			String newVal = inputFormat;
			
			this.inputFormat = inputFormat;
			firePropertyChange(AdvancedConfiguration.INPUT_FORMAT, previousVal, newVal);
		}

		public String getOutputFormat() {
			return outputFormat;
		}

		public void setOutputFormat(String outputFormat) {
			String previousVal = this.outputFormat;
			String newVal = outputFormat;
			
			this.outputFormat = outputFormat;
			firePropertyChange(AdvancedConfiguration.OUTPUT_FORMAT, previousVal, newVal);
		}

		public String getWorkingDirectory() {
			return workingDirectory;
		}

		public void setWorkingDirectory(String workingDirectory) {
			String previousVal = this.workingDirectory;
			String newVal = workingDirectory;
			
			this.workingDirectory = workingDirectory;
			firePropertyChange(AdvancedConfiguration.WORKING_DIRECTORY, previousVal, newVal);
		}

		public String getFsDefaultName() {
			return fsDefaultName;
		}

		public void setFsDefaultName(String fsDefaultName) {
			String previousVal = this.fsDefaultName;
			String newVal = fsDefaultName;
			
			this.fsDefaultName = fsDefaultName;
			firePropertyChange(AdvancedConfiguration.FS_DEFAULT_NAME, previousVal, newVal);
		}

		public String getInputPath() {
			return inputPath;
		}

		public void setInputPath(String inputPath) {
			String previousVal = this.inputPath;
			String newVal = inputPath;
			
			this.inputPath = inputPath;
			firePropertyChange(AdvancedConfiguration.INPUT_PATH, previousVal, newVal);
		}

		public String getOutputPath() {
			return outputPath;
		}

		public void setOutputPath(String outputPath) {
			String previousVal = this.outputPath;
			String newVal = outputPath;
			
			this.outputPath = outputPath;
			firePropertyChange(AdvancedConfiguration.OUTPUT_PATH, previousVal, newVal);
		}

		public List<UserDefinedItem> getUserDefined() {
			return userDefined;
		}

		public void setUserDefined(List<UserDefinedItem> userDefined) {
			List<UserDefinedItem> previousVal = this.userDefined;
			List<UserDefinedItem> newVal = userDefined;
			
			this.userDefined = userDefined;
			firePropertyChange(AdvancedConfiguration.USER_DEFINED, previousVal, newVal);
		}
	}
}