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

package org.pentaho.di.job.entries.hadoopjobexecutor;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

@JobEntry (id="HadoopJobExecutorPlugin", name="Hadoop Job Executor", categoryDescription="Hadoop", description="Execute Map/Reduce jobs in Hadoop", image = "HDE.png")
public class JobEntryHadoopJobExecutor extends JobEntryBase implements Cloneable, JobEntryInterface{
	private String hadoopJobName;
	private String jarUrl;
	
	private boolean isSimple;
	
	private String cmdLineArgs;
	
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
	
	public class UserDefinedItem {
		private String name;
		private String value;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	
	public Result execute(Result arg0, int arg1) throws KettleException {
		// TODO Auto-generated method stub
		return null;
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	  {
//		try
//		{
//			super.loadXML(entrynode, databases, slaveServers);
//			copy_empty_folders      = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "copy_empty_folders")); //$NON-NLS-1$ //$NON-NLS-2$
//			arg_from_previous   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "arg_from_previous") ); //$NON-NLS-1$ //$NON-NLS-2$
//			overwrite_files      = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "overwrite_files") ); //$NON-NLS-1$ //$NON-NLS-2$
//			include_subfolders = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "include_subfolders") ); //$NON-NLS-1$ //$NON-NLS-2$
//			remove_source_files = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "remove_source_files") ); //$NON-NLS-1$ //$NON-NLS-2$
//			add_result_filesname = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_result_filesname") ); //$NON-NLS-1$ //$NON-NLS-2$
//			destination_is_a_file = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "destination_is_a_file") ); //$NON-NLS-1$ //$NON-NLS-2$
//			create_destination_folder = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "create_destination_folder") ); //$NON-NLS-1$ //$NON-NLS-2$
//					
//			Node fields = XMLHandler.getSubNode(entrynode, "fields"); //$NON-NLS-1$
//			
//			// How many field arguments?
//			int nrFields = XMLHandler.countNodes(fields, "field");	//$NON-NLS-1$ 
//			source_filefolder = new String[nrFields];
//			destination_filefolder = new String[nrFields];
//			wildcard = new String[nrFields];
//			
//			// Read them all...
//			for (int i = 0; i < nrFields; i++)
//			{
//				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);//$NON-NLS-1$ 
//				
//				source_filefolder[i] = XMLHandler.getTagValue(fnode, "source_filefolder");//$NON-NLS-1$ 
//				destination_filefolder[i] = XMLHandler.getTagValue(fnode, "destination_filefolder");//$NON-NLS-1$ 
//				wildcard[i] = XMLHandler.getTagValue(fnode, "wildcard");//$NON-NLS-1$ 
//			}
//		}
//	
//		catch(KettleXMLException xe)
//		{
//			
//			throw new KettleXMLException(BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.UnableLoadXML"), xe);
//		}
	}
	
	@Override
	public String getXML()
	{
		StringBuffer retval = new StringBuffer(300);
		
//		retval.append(super.getXML());		
//		retval.append("      ").append(XMLHandler.addTagValue("copy_empty_folders",      copy_empty_folders)); //$NON-NLS-1$ //$NON-NLS-2$
//		retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous",  arg_from_previous)); //$NON-NLS-1$ //$NON-NLS-2$
//		retval.append("      ").append(XMLHandler.addTagValue("overwrite_files",      overwrite_files)); //$NON-NLS-1$ //$NON-NLS-2$
//		retval.append("      ").append(XMLHandler.addTagValue("include_subfolders", include_subfolders)); //$NON-NLS-1$ //$NON-NLS-2$
//		retval.append("      ").append(XMLHandler.addTagValue("remove_source_files", remove_source_files)); //$NON-NLS-1$ //$NON-NLS-2$
//		retval.append("      ").append(XMLHandler.addTagValue("add_result_filesname", add_result_filesname)); //$NON-NLS-1$ //$NON-NLS-2$
//		retval.append("      ").append(XMLHandler.addTagValue("destination_is_a_file", destination_is_a_file)); //$NON-NLS-1$ //$NON-NLS-2$
//		retval.append("      ").append(XMLHandler.addTagValue("create_destination_folder", create_destination_folder)); //$NON-NLS-1$ //$NON-NLS-2$
//		
//		retval.append("      <fields>").append(Const.CR); //$NON-NLS-1$
//		if (source_filefolder!=null)
//		{
//			for (int i=0;i<source_filefolder.length;i++)
//			{
//				retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
//				retval.append("          ").append(XMLHandler.addTagValue("source_filefolder",     source_filefolder[i])); //$NON-NLS-1$ //$NON-NLS-2$
//				retval.append("          ").append(XMLHandler.addTagValue("destination_filefolder",     destination_filefolder[i])); //$NON-NLS-1$ //$NON-NLS-2$
//				retval.append("          ").append(XMLHandler.addTagValue("wildcard", wildcard[i])); //$NON-NLS-1$ //$NON-NLS-2$
//				retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
//			}
//		}
//		retval.append("      </fields>").append(Const.CR);
		
		return retval.toString();
	}
	
	@Override
	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	  {
//		try
//		{
//    copy_empty_folders      = rep.getJobEntryAttributeBoolean(id_jobentry, "copy_empty_folders");//$NON-NLS-1$ 
//    arg_from_previous   = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");//$NON-NLS-1$ 
//    overwrite_files      = rep.getJobEntryAttributeBoolean(id_jobentry, "overwrite_files");//$NON-NLS-1$ 
//    include_subfolders = rep.getJobEntryAttributeBoolean(id_jobentry, "include_subfolders");//$NON-NLS-1$ 
//    remove_source_files = rep.getJobEntryAttributeBoolean(id_jobentry, "remove_source_files");//$NON-NLS-1$ 
//			
//			add_result_filesname = rep.getJobEntryAttributeBoolean(id_jobentry, "add_result_filesname");//$NON-NLS-1$ 
//			destination_is_a_file = rep.getJobEntryAttributeBoolean(id_jobentry, "destination_is_a_file");//$NON-NLS-1$ 
//			create_destination_folder = rep.getJobEntryAttributeBoolean(id_jobentry, "create_destination_folder");//$NON-NLS-1$ 
//				
//			// How many arguments?
//			int argnr = rep.countNrJobEntryAttributes(id_jobentry, "source_filefolder");//$NON-NLS-1$ 
//			source_filefolder = new String[argnr];
//			destination_filefolder = new String[argnr];
//			wildcard = new String[argnr];
//			
//			// Read them all...
//			for (int a=0;a<argnr;a++) 
//			{
//				source_filefolder[a]= rep.getJobEntryAttributeString(id_jobentry, a, "source_filefolder");//$NON-NLS-1$ 
//				destination_filefolder[a]= rep.getJobEntryAttributeString(id_jobentry, a, "destination_filefolder");//$NON-NLS-1$ 
//				wildcard[a]= rep.getJobEntryAttributeString(id_jobentry, a, "wildcard");//$NON-NLS-1$ 
//			}
//		}
//		catch(KettleException dbe)
//		{
//			
//			throw new KettleException(BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.UnableLoadRep")+id_jobentry, dbe);
//		}
	}
	
	@Override
	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
//		try
//		{
//			rep.saveJobEntryAttribute(id_job, getObjectId(), "copy_empty_folders",      copy_empty_folders);
//			rep.saveJobEntryAttribute(id_job, getObjectId(), "arg_from_previous",  arg_from_previous);
//			rep.saveJobEntryAttribute(id_job, getObjectId(), "overwrite_files",      overwrite_files);
//			rep.saveJobEntryAttribute(id_job, getObjectId(), "include_subfolders", include_subfolders);
//			rep.saveJobEntryAttribute(id_job, getObjectId(), "remove_source_files", remove_source_files);
//			rep.saveJobEntryAttribute(id_job, getObjectId(), "add_result_filesname", add_result_filesname);
//			rep.saveJobEntryAttribute(id_job, getObjectId(), "destination_is_a_file", destination_is_a_file);
//			rep.saveJobEntryAttribute(id_job, getObjectId(), "create_destination_folder", create_destination_folder);
//			
//			// save the arguments...
//			if (source_filefolder!=null)
//			{
//				for (int i=0;i<source_filefolder.length;i++) 
//				{
//					rep.saveJobEntryAttribute(id_job, getObjectId(), i, "source_filefolder",     source_filefolder[i]);
//					rep.saveJobEntryAttribute(id_job, getObjectId(), i, "destination_filefolder",     destination_filefolder[i]);
//					rep.saveJobEntryAttribute(id_job, getObjectId(), i, "wildcard", wildcard[i]);
//				}
//			}
//		}
//		catch(KettleDatabaseException dbe)
//		{
//			
//			throw new KettleException(BaseMessages.getString(PKG, "JobCopyFiles.Error.Exception.UnableSaveRep")+id_job, dbe);
//		}
	}

}
