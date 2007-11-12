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
package org.pentaho.di.job;

import org.pentaho.di.job.entry.Messages;

public enum JobEntryType
{
	NONE("-"),
  // Commented out next line and reverted to TRANS by MB because currently, saved jobs have
  // TRANS instead of TRANSFORMATION. This causes getType() in JobEntryCopy to fail.
	// TRANSFORMATION(Messages.getString("JobEntry.Trans.TypeDesc")),
    TRANS(Messages.getString("JobEntry.Trans.TypeDesc")),
	JOB(Messages.getString("JobEntry.Job.TypeDesc")),
	SHELL(Messages.getString("JobEntry.Shell.TypeDesc")),
	MAIL(Messages.getString("JobEntry.Mail.TypeDesc")),
	SQL(Messages.getString("JobEntry.SQL.TypeDesc")),
	FTP(Messages.getString("JobEntry.FTP.TypeDesc")),
	TABLE_EXISTS(Messages.getString("JobEntry.TableExists.TypeDesc")),
	FILE_EXISTS(Messages.getString("JobEntry.FileExists.TypeDesc")),
  // Commented out and reverted to EVAL by MB because currently,
  // saved jobs have EVAL instead of EVALUATION. This causes getType() in JobEntryCopy to fail.
	// EVALUATION(Messages.getString("JobEntry.Evaluation.TypeDesc")),
    EVAL(Messages.getString("JobEntry.Evaluation.TypeDesc")),
	SPECIAL(Messages.getString("JobEntry.Special.TypeDesc")),
    SFTP(Messages.getString("JobEntry.SFTP.TypeDesc")),
    HTTP(Messages.getString("JobEntry.HTTP.TypeDesc")),
    CREATE_FILE(Messages.getString("JobEntry.CreateFile.TypeDesc")),
    DELETE_FILE(Messages.getString("JobEntry.DeleteFile.TypeDesc")),
    WAIT_FOR_FILE(Messages.getString("JobEntry.WaitForFile.TypeDesc")),
    SFTPPUT(Messages.getString("JobEntry.SFTPPut.TypeDesc")),
    FILE_COMPARE(Messages.getString("JobEntry.FileCompare.TypeDesc")),
    MYSQL_BULK_LOAD(Messages.getString("JobEntry.MysqlBulkLoad.TypeDesc")),
	MSGBOX_INFO(Messages.getString("JobEntry.MsgBoxInfo.TypeDesc")),
	DELAY(Messages.getString("JobEntry.Delay.TypeDesc")),
	ZIP_FILE(Messages.getString("JobEntry.ZipFile.TypeDesc")),
	XSLT(Messages.getString("JobEntry.XSLT.TypeDesc")),
	MYSQL_BULK_FILE(Messages.getString("JobEntry.MysqlBulkFile.TypeDesc")),
    ABORT(Messages.getString("JobEntry.Abort.TypeDesc")),
	GET_POP(Messages.getString("JobEntry.GetPOP.TypeDesc")),
	PING(Messages.getString("JobEntry.Ping.TypeDesc")),
	DELETE_FILES(Messages.getString("JobEntry.DeleteFiles.TypeDesc")),
	SUCCESS(Messages.getString("JobEntry.Success.TypeDesc")),
	XSD_VALIDATOR(Messages.getString("JobEntry.XSDValidator.TypeDesc")),
	XACTION(Messages.getString("JobEntry.XAction.TypeDesc")),
	WRITE_TO_LOG(Messages.getString("JobEntry.WriteToLog.TypeDesc")),
	COPY_FILES(Messages.getString("JobEntry.CopyFiles.TypeDesc")),
	DTD_VALIDATOR(Messages.getString("JobEntry.DTDValidator.TypeDesc")),
	FTP_PUT(Messages.getString("JobEntry.FTPPUT.TypeDesc")),
	UNZIP(Messages.getString("JobEntry.UnZip.TypeDesc"));
	
	private String description;
	
	JobEntryType(String description)
	{
		this.description = description;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String getTypeCode()
	{
		return name();
	}
	

}
