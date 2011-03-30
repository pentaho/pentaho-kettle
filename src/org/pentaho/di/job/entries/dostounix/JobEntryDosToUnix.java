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

package org.pentaho.di.job.entries.dostounix;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This defines a 'Dos to Unix' job entry.
 * 
 * @author Samatar Hassan
 * @since 26-03-2008
 */

public class JobEntryDosToUnix extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static final int LF = 0x0a;
    private static final int CR = 0x0d;
    
	private static Class<?> PKG = JobEntryDosToUnix.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String[] ConversionTypeDesc = new String[] { 
		BaseMessages.getString(PKG, "JobEntryDosToUnix.ConversionType.Guess.Label"),	
		BaseMessages.getString(PKG, "JobEntryDosToUnix.ConversionType.DosToUnix.Label"),
		BaseMessages.getString(PKG, "JobEntryDosToUnix.ConversionType.UnixToDos.Label")
	};
	public static final String[] ConversionTypeCode = new String[] { 
		"guess",
		"dostounix", 
		"unixtodos"
	};
	
	public static final int CONVERTION_TYPE_GUESS=0;
	public static final int CONVERTION_TYPE_DOS_TO_UNIX=1;
	public static final int CONVERTION_TYPE_UNIX_TO_DOS=2;

	
	
	private static final int TYPE_DOS_FILE=0;
	private static final int TYPE_UNIX_FILE=1;
	private static final int TYPE_BINAY_FILE=2;
	
	public static final String ADD_NOTHING="nothing";
	public  static final String SUCCESS_IF_AT_LEAST_X_FILES_PROCESSED="success_when_at_least";
	public  static final String SUCCESS_IF_ERROR_FILES_LESS="success_if_error_files_less";
	public  static final String SUCCESS_IF_NO_ERRORS="success_if_no_errors";

	public static final String ADD_ALL_FILENAMES="all_filenames";
	public static final String ADD_PROCESSED_FILES_ONLY="only_processed_filenames";
	public static final String ADD_ERROR_FILES_ONLY="only_error_filenames";
	
	public  boolean arg_from_previous;
	public  boolean include_subfolders;
	
	public  String  source_filefolder[];
	public  String  wildcard[];
	public int ConversionTypes[];
	
	private String nr_errors_less_than;
	private String success_condition;
	private String resultfilenames;

	int NrAllErrors=0;
	int NrErrorFiles=0;
	int NrProcessedFiles=0;
	int limitFiles=0;
	int NrErrors=0;
	
	boolean successConditionBroken=false;
	boolean successConditionBrokenExit=false;
	
	private static String tempFolder;
	
	public JobEntryDosToUnix(String n)
	{
		super(n, "");
		resultfilenames=ADD_ALL_FILENAMES;
		arg_from_previous=false;
		source_filefolder=null;
		ConversionTypes=null;
		wildcard=null;
		include_subfolders=false;
		nr_errors_less_than="10";
		success_condition=SUCCESS_IF_NO_ERRORS;
		
		setID(-1L);
	} 

	public JobEntryDosToUnix()
	{
		this("");
	}


	public Object clone()
	{
		JobEntryDosToUnix je = (JobEntryDosToUnix) super.clone();
		return je;
	}
    
	public String getXML()
	{
		StringBuffer retval = new StringBuffer(300);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous",  arg_from_previous));
		retval.append("      ").append(XMLHandler.addTagValue("include_subfolders", include_subfolders));
		retval.append("      ").append(XMLHandler.addTagValue("nr_errors_less_than", nr_errors_less_than));
		retval.append("      ").append(XMLHandler.addTagValue("success_condition", success_condition));
		retval.append("      ").append(XMLHandler.addTagValue("resultfilenames", resultfilenames));
		retval.append("      <fields>").append(Const.CR);
		if (source_filefolder!=null)
		{
			for (int i=0;i<source_filefolder.length;i++)
			{
				retval.append("        <field>").append(Const.CR);
				retval.append("          ").append(XMLHandler.addTagValue("source_filefolder",     source_filefolder[i]));
				retval.append("          ").append(XMLHandler.addTagValue("wildcard", wildcard[i]));
				retval.append("          ").append(XMLHandler.addTagValue("ConversionType",getConversionTypeCode(ConversionTypes[i])));
				retval.append("        </field>").append(Const.CR);
			}
		}
		retval.append("      </fields>").append(Const.CR);
		
		return retval.toString();
	}
	private static String getConversionTypeCode(int i) {
		if (i < 0 || i >= ConversionTypeCode.length)
			return ConversionTypeCode[0];
		return ConversionTypeCode[i];
	}
	public static String getConversionTypeDesc(int i) {
		if (i < 0 || i >= ConversionTypeDesc.length)
			return ConversionTypeDesc[0];
		return ConversionTypeDesc[i];
	}
	public static int getConversionTypeByDesc(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < ConversionTypeDesc.length; i++) {
			if (ConversionTypeDesc[i].equalsIgnoreCase(tt))
				return i;
		}

		// If this fails, try to match using the code.
		return getConversionTypeByCode(tt);
	}
	private static int getConversionTypeByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < ConversionTypeCode.length; i++) {
			if (ConversionTypeCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
	
	  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	  {
	    try
	    {
	      super.loadXML(entrynode, databases, slaveServers);
			
			arg_from_previous   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "arg_from_previous") );
			include_subfolders = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "include_subfolders") );
	
			nr_errors_less_than          = XMLHandler.getTagValue(entrynode, "nr_errors_less_than");
			success_condition          = XMLHandler.getTagValue(entrynode, "success_condition");
			resultfilenames          = XMLHandler.getTagValue(entrynode, "resultfilenames");
			
			
			Node fields = XMLHandler.getSubNode(entrynode, "fields");
			
			// How many field arguments?
			int nrFields = XMLHandler.countNodes(fields, "field");	
			source_filefolder = new String[nrFields];
			wildcard = new String[nrFields];
			ConversionTypes = new int[nrFields];
			
			// Read them all...
			for (int i = 0; i < nrFields; i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				source_filefolder[i] = XMLHandler.getTagValue(fnode, "source_filefolder");
				wildcard[i] = XMLHandler.getTagValue(fnode, "wildcard");
				ConversionTypes[i] = getConversionTypeByCode(Const.NVL(XMLHandler.getTagValue(fnode,	"ConversionType"), ""));			}
		}
	
		catch(KettleXMLException xe)
		{
			
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobDosToUnix.Error.Exception.UnableLoadXML"), xe);
		}
	}

	  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	  {
	    try
	    {
			arg_from_previous   = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
			include_subfolders = rep.getJobEntryAttributeBoolean(id_jobentry, "include_subfolders");

			nr_errors_less_than  = rep.getJobEntryAttributeString(id_jobentry, "nr_errors_less_than");
			success_condition  = rep.getJobEntryAttributeString(id_jobentry, "success_condition");
			resultfilenames  = rep.getJobEntryAttributeString(id_jobentry, "resultfilenames");

			// How many arguments?
			int argnr = rep.countNrJobEntryAttributes(id_jobentry, "source_filefolder");
			source_filefolder = new String[argnr];
			wildcard = new String[argnr];
			ConversionTypes = new int[argnr];
			// Read them all...
			for (int a=0;a<argnr;a++) 
			{
				source_filefolder[a]= rep.getJobEntryAttributeString(id_jobentry, a, "source_filefolder");
				wildcard[a]= rep.getJobEntryAttributeString(id_jobentry, a, "wildcard");
				ConversionTypes[a] = getConversionTypeByCode(Const.NVL(rep.getJobEntryAttributeString(id_jobentry,"ConversionType"), ""));
			}
		}
		catch(KettleException dbe)
		{
			
			throw new KettleException(BaseMessages.getString(PKG, "JobDosToUnix.Error.Exception.UnableLoadRep")+id_jobentry, dbe);
		}
	}
	
		public void saveRep(Repository rep, ObjectId id_job) throws KettleException
		{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "arg_from_previous",  arg_from_previous);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "include_subfolders", include_subfolders);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "nr_errors_less_than",  nr_errors_less_than);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "success_condition",    success_condition);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "resultfilenames",      resultfilenames);
			
			// save the arguments...
			if (source_filefolder!=null)
			{
				for (int i=0;i<source_filefolder.length;i++) 
				{
					rep.saveJobEntryAttribute(id_job, getObjectId(), i, "source_filefolder",     source_filefolder[i]);
					rep.saveJobEntryAttribute(id_job, getObjectId(), i, "wildcard", wildcard[i]);
					rep.saveJobEntryAttribute(id_job, getObjectId(),"ConversionType", getConversionTypeCode(ConversionTypes[i]));
				}
			}
		}
		catch(KettleDatabaseException dbe)
		{
			
			throw new KettleException(BaseMessages.getString(PKG, "JobDosToUnix.Error.Exception.UnableSaveRep")+id_job, dbe);
		}
	}

	public Result execute(Result previousResult, int nr) throws KettleException 
	{
		Result result = previousResult;
		result.setNrErrors(1);
		result.setResult(false);

		List<RowMetaAndData> rows = previousResult.getRows();
		RowMetaAndData resultRow = null;  
		
	    NrErrors=0;
	    NrProcessedFiles=0;
	    NrErrorFiles=0;
	    limitFiles=Const.toInt(environmentSubstitute(getNrErrorsLessThan()),10);
		successConditionBroken=false;
		successConditionBrokenExit=false;
		tempFolder = environmentSubstitute("%%java.io.tmpdir%%");

		// Get source and destination files, also wildcard
		String vsourcefilefolder[] = source_filefolder;
		String vwildcard[] = wildcard;
			
		if (arg_from_previous)
		{
			if (isDetailed())
				logDetailed(BaseMessages.getString(PKG, "JobDosToUnix.Log.ArgFromPrevious.Found",(rows!=null?rows.size():0)+ ""));
			
		}
		if (arg_from_previous && rows!=null) // Copy the input row to the (command line) arguments
		{
			for (int iteration=0;iteration<rows.size() && !parentJob.isStopped();iteration++) 
			{
				if(successConditionBroken)
				{
					if(!successConditionBrokenExit)
					{
						logError(BaseMessages.getString(PKG, "JobDosToUnix.Error.SuccessConditionbroken",""+NrAllErrors));
						successConditionBrokenExit=true;
					}
					result.setEntryNr(NrAllErrors);
					result.setNrLinesRejected(NrErrorFiles);
					result.setNrLinesWritten(NrProcessedFiles);
					return result;
				}
				
				resultRow = rows.get(iteration);
			
				// Get source and destination file names, also wildcard
				String vsourcefilefolder_previous = resultRow.getString(0,null);
				String vwildcard_previous = resultRow.getString(1, null);
				int convertion_type = JobEntryDosToUnix.getConversionTypeByCode(resultRow.getString(2, null));
				
				if(isDetailed())
					logDetailed(BaseMessages.getString(PKG, "JobDosToUnix.Log.ProcessingRow",vsourcefilefolder_previous,vwildcard_previous));

				ProcessFileFolder(vsourcefilefolder_previous, vwildcard_previous, convertion_type, parentJob,result);
			}
		}
		else if (vsourcefilefolder!=null)
		{
			for (int i=0;i<vsourcefilefolder.length && !parentJob.isStopped();i++)
			{
				if(successConditionBroken)
				{
					if(!successConditionBrokenExit)
					{
						logError(BaseMessages.getString(PKG, "JobDosToUnix.Error.SuccessConditionbroken",""+NrAllErrors));
						successConditionBrokenExit=true;
					}
					result.setEntryNr(NrAllErrors);
					result.setNrLinesRejected(NrErrorFiles);
					result.setNrLinesWritten(NrProcessedFiles);
					return result;
				}
				
				if(isDetailed())
					logDetailed(BaseMessages.getString(PKG, "JobDosToUnix.Log.ProcessingRow",vsourcefilefolder[i],vwildcard[i]));
					
				ProcessFileFolder(vsourcefilefolder[i], vwildcard[i], ConversionTypes[i], parentJob,result);
				
			}
		}	
		
		// Success Condition
		result.setNrErrors(NrAllErrors);
		result.setNrLinesRejected(NrErrorFiles);
		result.setNrLinesWritten(NrProcessedFiles);
		if(getSuccessStatus())
		{
			result.setNrErrors(0);
			result.setResult(true);
		}
		
		displayResults();
		
		return result;
	}
	private void displayResults()
	{
		if(isDetailed())
		{
			logDetailed("=======================================");
			logDetailed(BaseMessages.getString(PKG, "JobDosToUnix.Log.Info.Errors", NrErrors));
			logDetailed(BaseMessages.getString(PKG, "JobDosToUnix.Log.Info.ErrorFiles",NrErrorFiles));
			logDetailed(BaseMessages.getString(PKG, "JobDosToUnix.Log.Info.FilesProcessed",NrProcessedFiles));
			logDetailed("=======================================");
		}
	}
	
	private boolean checkIfSuccessConditionBroken()
	{
		boolean retval=false;
		if ((NrAllErrors>0 && getSuccessCondition().equals(SUCCESS_IF_NO_ERRORS))
				|| (NrErrorFiles>=limitFiles && getSuccessCondition().equals(SUCCESS_IF_ERROR_FILES_LESS)))
		{
			retval=true;	
		}
		return retval;
	}
	private boolean getSuccessStatus()
	{
		boolean retval=false;
		
		if ((NrAllErrors==0 && getSuccessCondition().equals(SUCCESS_IF_NO_ERRORS))
				|| (NrProcessedFiles>=limitFiles && getSuccessCondition().equals(SUCCESS_IF_AT_LEAST_X_FILES_PROCESSED))
				|| (NrErrorFiles<limitFiles && getSuccessCondition().equals(SUCCESS_IF_ERROR_FILES_LESS)))
			{
				retval=true;	
			}
		
		return retval;
	}
	private void updateErrors()
	{
		NrErrors++;
		updateAllErrors();
		if(checkIfSuccessConditionBroken())
		{
			// Success condition was broken
			successConditionBroken=true;
		}
	}
	private void updateAllErrors()
	{
		NrAllErrors=NrErrors+NrErrorFiles;
	}
	private static int getFileType(FileObject file) throws Exception {
	    int aCount = 0; // occurences of LF
	    int dCount = 0; // occurences of CR
	    FileInputStream in = new FileInputStream( file.getName().getPathDecoded());
	    while (in.available() > 0) {
	      int b = in.read();
	      if (b == CR) {
	        dCount++;
	        if (in.available() > 0) {
	          b = in.read();
	          if (b == LF) {
	            aCount++;
	          } else {
	            return TYPE_BINAY_FILE;
	          }
	        }
	      }
	      else if (b == LF) {
	        aCount++;
	      }
	    }
	    in.close();
	    if (aCount == dCount) {
	      return TYPE_DOS_FILE;
	    } else {
	      return TYPE_UNIX_FILE;
	    }
	  }
		 
	private boolean convert(FileObject file, boolean toUnix) {
		boolean retval=false;
		// CR = CR
		// LF = LF
		try{
			String localfilename=KettleVFS.getFilename(file);
			File source=new File(localfilename);
			if(isDetailed()) {
				if(toUnix) {
					logDetailed(BaseMessages.getString(PKG, "JobDosToUnix.Log.ConvertingFileToUnix",source.getAbsolutePath()));
				}else {
					logDetailed(BaseMessages.getString(PKG, "JobDosToUnix.Log.ConvertingFileToDos",source.getAbsolutePath()));
				}
			}
			File tempFile= new File(tempFolder, source.getName()+ ".tmp");
	
			if(isDebug()) {
				logDebug(BaseMessages.getString(PKG, "JobDosToUnix.Log.CreatingTempFile",tempFile.getAbsolutePath()));
			}
			FileOutputStream out = new FileOutputStream(tempFile);
		    FileInputStream in = new FileInputStream( localfilename);
		    
	        if(toUnix) {
	        	// Dos to Unix
		        while (in.available() > 0) {
		            int b1 = in.read();
		            if (b1 == CR) {
		              int b2 = in.read();
		              if (b2 == LF) {
		                out.write(LF);
		              } else {
		                out.write(b1);
		                out.write(b2);
		              }
		            } else {
		              out.write(b1);
		            }
		          }
	        }else {
	        	// Unix to Dos
	        	 while (in.available() > 0) {
        	        int b1 = in.read();
        	        if (b1 == CR) {
        	          boolean b = true;
        	          while(b) {
        	            if (in.available() > 0) {
        	              int b2 = in.read();
        	              if (b2 != CR) {
        	                b = false;
        	              }
        	            } else {
        	              b = false;
        	            }
        	          }
        	          out.write(CR);
        	          out.write(LF);
        	        } else {
        	          out.write(b1);
        	        }
        	      }
	        }
		        
	        in.close();
	        out.close();


			if(isDebug()) {
				logDebug(BaseMessages.getString(PKG, "JobDosToUnix.Log.DeletingSourceFile",localfilename));
			}
	        file.delete();
	        if(isDebug()) {
				logDebug(BaseMessages.getString(PKG, "JobDosToUnix.Log.RenamingTempFile",tempFile.getAbsolutePath(), source.getAbsolutePath()));
			}
	        tempFile.renameTo(source);
	        
	        retval=true;

	    } catch (Exception e) {
	        logError(BaseMessages.getString(PKG, "JobDosToUnix.Log.ErrorConvertingFile",file.toString(),e.getMessage()));
	    }
	  
	    return retval; 
	}

	private boolean ProcessFileFolder(String sourcefilefoldername,String wildcard,
			int convertion, Job parentJob,Result result)
	{
		boolean entrystatus = false ;
		FileObject sourcefilefolder = null;
		FileObject CurrentFile = null;
		
		// Get real source file and wilcard
		String realSourceFilefoldername = environmentSubstitute(sourcefilefoldername);
		if(Const.isEmpty(realSourceFilefoldername)) {
			logError(BaseMessages.getString(PKG, "JobDosToUnix.log.FileFolderEmpty",sourcefilefoldername));
			// Update Errors
			updateErrors();
			
			return entrystatus;
		}
		String realWildcard=environmentSubstitute(wildcard);

		try {
			sourcefilefolder = KettleVFS.getFileObject(realSourceFilefoldername);
			
			if (sourcefilefolder.exists()) {
				 if(isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobDosToUnix.Log.FileExists",sourcefilefolder.toString()));
				 if(sourcefilefolder.getType() == FileType.FILE) {
					 entrystatus=convertOneFile(sourcefilefolder, convertion, result,parentJob);
					 
				 }else if(sourcefilefolder.getType() == FileType.FOLDER)  {
					 FileObject[] fileObjects = sourcefilefolder.findFiles(
                             new AllFileSelector() 
                             {	
                                 public boolean traverseDescendents(FileSelectInfo info)
                                 {
                                	 return info.getDepth()==0 || include_subfolders;
                                 }
                                 
                                 public boolean includeFile(FileSelectInfo info)
                                 {
                                 
                                 	FileObject fileObject = info.getFile();
                                 	try {
                                 	    if ( fileObject == null) return false;
                                 	    if(fileObject.getType() != FileType.FILE) return false;
                                 	}
                                 	catch (Exception ex)
                                 	{
                                 		// Upon error don't process the file.
                                 		return false;
                                 	}
                                 	
                                 	finally 
                             		{
                             			if ( fileObject != null )
                             			{
                             				try  {fileObject.close();} catch ( IOException ex ) {};
                             			}
        
                             		}
                                 	return true;
                                 }
                             }
                         );
						
					 if (fileObjects != null) 
                     {
                         for (int j = 0; j < fileObjects.length && !parentJob.isStopped(); j++)
                         {
                        	 if(successConditionBroken)
             				{
             					if(!successConditionBrokenExit)
             					{
             						logError(BaseMessages.getString(PKG, "JobDosToUnix.Error.SuccessConditionbroken",""+NrAllErrors));
             						successConditionBrokenExit=true;
             					}
             					return false;
             				}
                         	// Fetch files in list one after one ...
                             CurrentFile=fileObjects[j];
                             
                             if (!CurrentFile.getParent().toString().equals(sourcefilefolder.toString()))
                			 {
                            	// Not in the Base Folder..Only if include sub folders  
                				 if (include_subfolders)
                				 {
                					if(GetFileWildcard(CurrentFile.toString(),realWildcard))
                					{
                						convertOneFile(CurrentFile, convertion, result,parentJob);
                					}
                				 }
                            	 
                			 }else
                			 {
                				 // In the base folder
                				if (GetFileWildcard(CurrentFile.toString(),realWildcard))
          						{	
                					convertOneFile(CurrentFile, convertion, result,parentJob);
          						}
                			 }        
                         }
                     }	 
				 }else
				 {
					 logError(BaseMessages.getString(PKG, "JobDosToUnix.Error.UnknowFileFormat",sourcefilefolder.toString()));					
					 // Update Errors
					 updateErrors(); 
				 }
			} 
			else
			{	
				logError(BaseMessages.getString(PKG, "JobDosToUnix.Error.SourceFileNotExists",realSourceFilefoldername));					
				// Update Errors
				updateErrors();
			}
		} // end try
	
		catch (Exception e) 
		{
			logError(BaseMessages.getString(PKG, "JobDosToUnix.Error.Exception.Processing",realSourceFilefoldername.toString(),e.getMessage()));					
			// Update Errors
			updateErrors();
		}
		finally 
		{
			if ( sourcefilefolder != null )
			{
				try{
					sourcefilefolder.close();
				}catch ( IOException ex ) {};

			}
			if ( CurrentFile != null )
			{
				try {
					CurrentFile.close();
				}catch ( IOException ex ) {};
			}
		}
		return entrystatus;
	}
	
	private boolean convertOneFile(FileObject file, int convertion, Result result,Job parentJob)
	{
		boolean retval=false;
		try
		{
		 // We deal with a file..

		 boolean convertToUnix=true;
		 
		 if(convertion==CONVERTION_TYPE_GUESS) {
			 // Get file Type
			 int fileType= getFileType(file);
			 if(fileType== TYPE_DOS_FILE)  {
				 // File type is DOS
				 // We need to convert it to UNIX
				 convertToUnix=true;
			 } else {
				 // File type is not DOS
				 // so let's convert it to DOS
				 convertToUnix=false;
			 }
		 }else  if(convertion==CONVERTION_TYPE_DOS_TO_UNIX) {
			 convertToUnix=true;
		 }else {
			 convertToUnix=false;
		 }
		 
		 retval=convert(file, convertToUnix);
		 
		 if(!retval) {
			 logError(BaseMessages.getString(PKG, "JobDosToUnix.Error.FileNotConverted",file.toString()));					
			 // Update Bad files number
			 updateBadFormed(); 
			 if(resultfilenames.equals(ADD_ALL_FILENAMES) || resultfilenames.equals(ADD_ERROR_FILES_ONLY))
				 addFileToResultFilenames(file, result,parentJob);
		 }else {
			 if(isDetailed()) {
				 logDetailed("---------------------------");
				 logDetailed(BaseMessages.getString(PKG, "JobDosToUnix.Error.FileConverted",file, 
						 convertToUnix?"UNIX":"DOS"));					
			 }
			 // Update processed files number
			 updateProcessedFormed(); 
			 if(resultfilenames.equals(ADD_ALL_FILENAMES) || resultfilenames.equals(ADD_PROCESSED_FILES_ONLY))
					 addFileToResultFilenames(file, result,parentJob);
		 }
		
		}catch (Exception e){}
		 return retval;
	}

	private void updateProcessedFormed()
	{
		NrProcessedFiles++;
	}
	private void updateBadFormed()
	{
		NrErrorFiles++;
		updateAllErrors();
	}
	private void addFileToResultFilenames(FileObject fileaddentry, Result result,Job parentJob) {	
		try	 {
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, fileaddentry, parentJob.getName(), toString());
			result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
	    
			if(isDetailed())
			{
				logDetailed(BaseMessages.getString(PKG, "JobDosToUnix.Log.FileAddedToResultFilesName",fileaddentry));
			}
			
		}catch (Exception e) {
			logError(BaseMessages.getString(PKG, "JobDosToUnix.Error.AddingToFilenameResult",fileaddentry.toString(),e.getMessage()));
		}
  }
	
	
	/**********************************************************
	 * 
	 * @param selectedfile
	 * @param wildcard
	 * @return True if the selectedfile matches the wildcard
	 **********************************************************/
	private boolean GetFileWildcard(String selectedfile, String wildcard)
	{
		Pattern pattern = null;
		boolean getIt=true;
	
        if (!Const.isEmpty(wildcard))
        {
        	 pattern = Pattern.compile(wildcard);
			// First see if the file matches the regular expression!
			if (pattern!=null)
			{
				Matcher matcher = pattern.matcher(selectedfile);
				getIt = matcher.matches();
			}
        }
		
		return getIt;
	}


	public void setIncludeSubfolders(boolean include_subfoldersin) 
	{
		this.include_subfolders = include_subfoldersin;
	}
	
	
	
	public void setArgFromPrevious(boolean argfrompreviousin) 
	{
		this.arg_from_previous = argfrompreviousin;
	}
	
	
	public void setNrErrorsLessThan(String nr_errors_less_than)
	{
		this.nr_errors_less_than=nr_errors_less_than;
	}
	
	public String getNrErrorsLessThan()
	{
		return nr_errors_less_than;
	}
	
	
	public void setSuccessCondition(String success_condition)
	{
		this.success_condition=success_condition;
	}
	public String getSuccessCondition()
	{
		return success_condition;
	}
	
	public void setResultFilenames(String resultfilenames)
	{
		this.resultfilenames=resultfilenames;
	}
	public String getResultFilenames()
	{
		return resultfilenames;
	}

   public boolean evaluates() {
		return true;
   }

}