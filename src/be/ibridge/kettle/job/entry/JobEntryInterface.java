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
 
package be.ibridge.kettle.job.entry;
import java.util.ArrayList;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleJobException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.entry.eval.JobEntryEval;
import be.ibridge.kettle.job.entry.fileexists.JobEntryFileExists;
import be.ibridge.kettle.job.entry.ftp.JobEntryFTP;
import be.ibridge.kettle.job.entry.http.JobEntryHTTP;
import be.ibridge.kettle.job.entry.job.JobEntryJob;
import be.ibridge.kettle.job.entry.mail.JobEntryMail;
import be.ibridge.kettle.job.entry.sftp.JobEntrySFTP;
import be.ibridge.kettle.job.entry.shell.JobEntryShell;
import be.ibridge.kettle.job.entry.special.JobEntrySpecial;
import be.ibridge.kettle.job.entry.sql.JobEntrySQL;
import be.ibridge.kettle.job.entry.tableexists.JobEntryTableExists;
import be.ibridge.kettle.job.entry.trans.JobEntryTrans;
import be.ibridge.kettle.repository.Repository;


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

	public final static String type_desc[] =
		{
			"-", "TRANS", "JOB", "SHELL", "MAIL", "SQL", "FTP", "TABLE_EXISTS", "FILE_EXISTS", "EVAL", "SPECIAL", "SFTP", "HTTP"
		};

	public final static String type_desc_long[] =
		{
			"-", "Transformation", "Job", "Shell", "Mail", "SQL", "FTP files", "Table exists", "File exists", "Evaluation", "Special entries", "SFTP files", "Get a file using HTTP"
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
            "WEB.png"
		};
	
	public final static String type_tooltip_desc[] = 
		{
			"", 
			"Executes a transformation", 
			"Executes a job", 
			"Executes a shell script", 
			"Sends an e-Mail", 
			"Executes SQL on a certain database connection", 
			"Get files using FTP (File Transfer Protocol)", 
			"Checks if a table exists on a database connection", 
			"Checks if a file exists", 
			"Evaluates the result of the execution of a previous job entry", 
			"Special entries: start and dummy",
            "Get files using SFTP (Secure File Transfer Protocol)",
            "Get a file using the HTTP (HyperText Transfer Protocol)"
		};
	
	public final static Class type_classname[] = 
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
            JobEntryHTTP.class
		};
    

	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob) throws KettleJobException;
	
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

	public void    loadXML(Node entrynode, ArrayList databases) throws KettleXMLException;
	public String  getXML();
	public void    loadRep(Repository rep, long id_jobentry, ArrayList databases) throws KettleException;
	public void    saveRep(Repository rep, long id_job) throws KettleException;
	
	public int     getType();
	public String  getTypeDesc();
	public void    setType(int type);
	
	public boolean isStart();
	public boolean isDummy();
	public Object  clone();
	
	public boolean evaluates();
	public boolean isUnconditional();
	
	public boolean isEvaluation();
	public boolean isTransformation();
	public boolean isJob();
	public boolean isShell();
	public boolean isMail();
	public boolean isSpecial();
}

