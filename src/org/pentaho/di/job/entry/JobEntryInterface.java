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
 
package org.pentaho.di.job.entry;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.abort.JobEntryAbort;
import org.pentaho.di.job.entries.createfile.JobEntryCreateFile;
import org.pentaho.di.job.entries.delay.JobEntryDelay;
import org.pentaho.di.job.entries.deletefile.JobEntryDeleteFile;
import org.pentaho.di.job.entries.deletefiles.JobEntryDeleteFiles;
import org.pentaho.di.job.entries.eval.JobEntryEval;
import org.pentaho.di.job.entries.filecompare.JobEntryFileCompare;
import org.pentaho.di.job.entries.fileexists.JobEntryFileExists;
import org.pentaho.di.job.entries.ftp.JobEntryFTP;
import org.pentaho.di.job.entries.getpop.JobEntryGetPOP;
import org.pentaho.di.job.entries.http.JobEntryHTTP;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.mail.JobEntryMail;
import org.pentaho.di.job.entries.msgboxinfo.JobEntryMsgBoxInfo;
import org.pentaho.di.job.entries.mysqlbulkfile.JobEntryMysqlBulkFile;
import org.pentaho.di.job.entries.mysqlbulkload.JobEntryMysqlBulkLoad;
import org.pentaho.di.job.entries.ping.JobEntryPing;
import org.pentaho.di.job.entries.sftp.JobEntrySFTP;
import org.pentaho.di.job.entries.sftpput.JobEntrySFTPPUT;
import org.pentaho.di.job.entries.shell.JobEntryShell;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entries.sql.JobEntrySQL;
import org.pentaho.di.job.entries.success.JobEntrySuccess;
import org.pentaho.di.job.entries.tableexists.JobEntryTableExists;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entries.waitforfile.JobEntryWaitForFile;
import org.pentaho.di.job.entries.xsdvalidator.JobEntryXSDValidator;
import org.pentaho.di.job.entries.xslt.JobEntryXSLT;
import org.pentaho.di.job.entries.zipfile.JobEntryZipFile;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceReference;
import org.w3c.dom.Node;




/**
 * Interface for the different JobEntry classes.
 * 
 * @author Matt
 * @since 18-06-04
 * 
 */

public interface JobEntryInterface
{
	public final static int TYPE_JOBENTRY_NONE           =  0;
	public final static int TYPE_JOBENTRY_TRANSFORMATION =  1;
	public final static int TYPE_JOBENTRY_JOB            =  2;
	public final static int TYPE_JOBENTRY_SHELL          =  3;
	public final static int TYPE_JOBENTRY_MAIL           =  4;
	public final static int TYPE_JOBENTRY_SQL            =  5;
	public final static int TYPE_JOBENTRY_FTP            =  6;
	public final static int TYPE_JOBENTRY_TABLE_EXISTS   =  7;
	public final static int TYPE_JOBENTRY_FILE_EXISTS    =  8;
	public final static int TYPE_JOBENTRY_EVALUATION     =  9;
	public final static int TYPE_JOBENTRY_SPECIAL        = 10;
    public static final int TYPE_JOBENTRY_SFTP           = 11;
    public static final int TYPE_JOBENTRY_HTTP           = 12;
    public static final int TYPE_JOBENTRY_CREATE_FILE    = 13;
    public static final int TYPE_JOBENTRY_DELETE_FILE    = 14;
    public static final int TYPE_JOBENTRY_WAIT_FOR_FILE  = 15;
    public static final int TYPE_JOBENTRY_SFTPPUT        = 16;
    public static final int TYPE_JOBENTRY_FILE_COMPARE   = 17;
    public static final int TYPE_JOBENTRY_MYSQL_BULK_LOAD= 18;
	public static final int TYPE_JOBENTRY_MSGBOX_INFO= 19;
	public static final int TYPE_JOBENTRY_DELAY= 20;
	public static final int TYPE_JOBENTRY_ZIP_FILE= 21;
	public static final int TYPE_JOBENTRY_XSLT= 22;
	public static final int TYPE_JOBENTRY_MYSQL_BULK_FILE= 23;
    public static final int TYPE_JOBENTRY_ABORT= 24;
	public static final int TYPE_JOBENTRY_GET_POP= 25;
	public static final int TYPE_JOBENTRY_PING= 26;
	public static final int TYPE_JOBENTRY_DELETE_FILES    = 27;
	public static final int TYPE_JOBENTRY_SUCCESS    = 28;
	public static final int TYPE_JOBENTRY_XSD_VALIDATOR    = 29;

	public final static String typeCode[] =
		{
			"-",
            "TRANS",
            "JOB",
            "SHELL",
            "MAIL",
            "SQL",
            "FTP",
            "TABLE_EXISTS",
            "FILE_EXISTS",
            "EVAL",
            "SPECIAL",
            "SFTP",
            "HTTP",
            "CREATE_FILE",
            "DELETE_FILE",
            "WAIT_FOR_FILE",
            "SFTPPUT",
            "FILE_COMPARE",
            "MYSQL_BULK_LOAD",
			"MSGBOX_INFO",
			"DELAY",
			"ZIP_FILE",
			"XSLT",
			"MYSQL_BULK_FILE",
			"ABORT",
			"GET_POP",
			"PING",
			"DELETE_FILES",
			"SUCCESS",
			"XSD_VALIDATOR"
		};

	public final static String typeDesc[] =
		{
			"-",
            Messages.getString("JobEntry.Trans.TypeDesc"),
            Messages.getString("JobEntry.Job.TypeDesc"),
            Messages.getString("JobEntry.Shell.TypeDesc"),
            Messages.getString("JobEntry.Mail.TypeDesc"),
            Messages.getString("JobEntry.SQL.TypeDesc"),
            Messages.getString("JobEntry.FTP.TypeDesc"),
            Messages.getString("JobEntry.TableExists.TypeDesc"),
            Messages.getString("JobEntry.FileExists.TypeDesc"),
            Messages.getString("JobEntry.Evaluation.TypeDesc"),
            Messages.getString("JobEntry.Special.TypeDesc"),
            Messages.getString("JobEntry.SFTP.TypeDesc"),
            Messages.getString("JobEntry.HTTP.TypeDesc"),
            Messages.getString("JobEntry.CreateFile.TypeDesc"),
            Messages.getString("JobEntry.DeleteFile.TypeDesc"),
            Messages.getString("JobEntry.WaitForFile.TypeDesc"),
            Messages.getString("JobEntry.SFTPPut.TypeDesc"),            
            Messages.getString("JobEntry.FileCompare.TypeDesc"),
            Messages.getString("JobEntry.MysqlBulkLoad.TypeDesc"),
			Messages.getString("JobEntry.MsgBoxInfo.TypeDesc"),
			Messages.getString("JobEntry.Delay.TypeDesc"),
			Messages.getString("JobEntry.ZipFile.TypeDesc"),
			Messages.getString("JobEntry.XSLT.TypeDesc"),
			Messages.getString("JobEntry.MysqlBulkFile.TypeDesc"),
			Messages.getString("JobEntry.Abort.TypeDesc"),
			Messages.getString("JobEntry.GetPOP.TypeDesc"),
			Messages.getString("JobEntry.Ping.TypeDesc"),
			Messages.getString("JobEntry.DeleteFiles.TypeDesc"),
			Messages.getString("JobEntry.Success.TypeDesc"),
			Messages.getString("JobEntry.XSDValidator.TypeDesc"),
		};

	public final static String icon_filename[] = 
		{
		 	"",
			"TRN.png",
			"JOB.png",
		    "SHL.png",
			"MAIL.png",
			"SQL.png",
			"FTP.png",
			"TEX.png",
			"FEX.png",
			"RES.png",
            "",
            "SFT.png",
            "WEB.png",
            "CFJ.png",
            "DFJ.png",
            "WFF.png",           
            "SFP.png",                        
            "BFC.png",
            "MBL.png",
			"INF.png",
			"DLT.png",
			"ZIP.png",
			"XSLT.png",
			"MBF.png",
			"ABR.png",
			"GETPOP.png",
			"PNG.png",
			"DFS.png",
			"SUC.png",
			"XSD.png",
		};
	
	public final static String type_tooltip_desc[] = 
		{
			"", 
            Messages.getString("JobEntry.Trans.Tooltip"),
            Messages.getString("JobEntry.Job.Tooltip"),
            Messages.getString("JobEntry.Shell.Tooltip"),
            Messages.getString("JobEntry.Mail.Tooltip"),
            Messages.getString("JobEntry.SQL.Tooltip"),
            Messages.getString("JobEntry.FTP.Tooltip"),
            Messages.getString("JobEntry.TableExists.Tooltip"),
            Messages.getString("JobEntry.FileExists.Tooltip"),
            Messages.getString("JobEntry.Evaluation.Tooltip"),
            Messages.getString("JobEntry.Special.Tooltip"),
            Messages.getString("JobEntry.SFTP.Tooltip"),
            Messages.getString("JobEntry.HTTP.Tooltip"),
            Messages.getString("JobEntry.CreateFile.Tooltip"),
            Messages.getString("JobEntry.DeleteFile.Tooltip"),
            Messages.getString("JobEntry.WaitForFile.Tooltip"),
            Messages.getString("JobEntry.SFTPPut.Tooltip"),
            Messages.getString("JobEntry.FileCompare.Tooltip"),
            Messages.getString("JobEntry.MysqlBulkLoad.Tooltip"),
		    Messages.getString("JobEntry.MsgBoxInfo.Tooltip"),
			Messages.getString("JobEntry.Delay.Tooltip"),
			Messages.getString("JobEntry.ZipFile.Tooltip"),
			Messages.getString("JobEntry.XSLT.Tooltip"),
			Messages.getString("JobEntry.MysqlBulkFile.Tooltip"),
			Messages.getString("JobEntry.Abort.Tooltip"),
			Messages.getString("JobEntry.GetPOP.Tooltip"),
			Messages.getString("JobEntry.Ping.Tooltip"),
			Messages.getString("JobEntry.DeleteFiles.Tooltip"),
			Messages.getString("JobEntry.Success.Tooltip"),
			Messages.getString("JobEntry.XSDValidator.Tooltip"),
 		};
	
	public final static Class<?> type_classname[] = 
		{
	        null,
	        JobEntryTrans.class,
	        JobEntryJob.class,
	        JobEntryShell.class,
	        JobEntryMail.class,
	        JobEntrySQL.class,
	        JobEntryFTP.class,
	        JobEntryTableExists.class,
	        JobEntryFileExists.class,
	        JobEntryEval.class,
	        JobEntrySpecial.class,
            JobEntrySFTP.class,
            JobEntryHTTP.class,
            JobEntryCreateFile.class,
            JobEntryDeleteFile.class,
            JobEntryWaitForFile.class,
            JobEntrySFTPPUT.class,
            JobEntryFileCompare.class,
            JobEntryMysqlBulkLoad.class,
			JobEntryMsgBoxInfo.class,
			JobEntryDelay.class,
			JobEntryZipFile.class,
			JobEntryXSLT.class,
			JobEntryMysqlBulkFile.class,
			JobEntryAbort.class,
			JobEntryGetPOP.class,
			JobEntryPing.class,
			JobEntryDeleteFiles.class,
			JobEntrySuccess.class,
			JobEntryXSDValidator.class,
		};

	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob) throws KettleException;
	
	public void    clear();
	public long    getID();
	public void    setID(long id);
	public String  getName();
	public void    setName(String name);
	public String  getDescription();
	public void    setDescription(String description);
	public void    setChanged();
	public void    setChanged(boolean ch);
	public boolean hasChanged();

	public void    loadXML(Node entrynode, List<DatabaseMeta> databases, Repository rep) throws KettleXMLException;
	public String  getXML();
	public void    loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases) throws KettleException;
	public void    saveRep(Repository rep, long id_job) throws KettleException;
	
	public int     getType();
	public String  getTypeCode();
    public String  getPluginID();


	public boolean isStart();
	public boolean isDummy();
	public Object  clone();
	
	public boolean resetErrorsBeforeExecution();
	public boolean evaluates();
	public boolean isUnconditional();
	
	public boolean isEvaluation();
	public boolean isTransformation();
	public boolean isJob();
	public boolean isShell();
	public boolean isMail();
	public boolean isSpecial();
    
    public List<SQLStatement> getSQLStatements(Repository repository) throws KettleException;
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep);
    
    public String getFilename();
    public String getRealFilename();
    
    /**
     * This method returns all the database connections that are used by the job entry.
     * @return an array of database connections meta-data.
     *         Return an empty array if no connections are used.
     */
    public DatabaseMeta[] getUsedDatabaseConnections();

    public void setPluginID(String id);
    
    /**
     * Allows JobEntry objects to check themselves for consistency
     * @param remarks List of CheckResult objects indicating check status
     * @param jobMeta JobMeta
     */
    public void check(List<CheckResult> remarks, JobMeta jobMeta);
    
    /**
     * Get a list of all the resource dependencies that the step is depending on.
     * 
     * @return a list of all the resource dependencies that the step is depending on
     */
    public List<ResourceReference> getResourceDependencies();
}
