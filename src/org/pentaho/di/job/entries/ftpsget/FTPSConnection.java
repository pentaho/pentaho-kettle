/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Samatar HASSAN and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/*
 * Created on 08-03-2010
 *
 */
package org.pentaho.di.job.entries.ftpsget;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.vfs.FileObject;
import org.ftp4che.FTPConnection;
import org.ftp4che.FTPConnectionFactory;
import org.ftp4che.event.FTPEvent;
import org.ftp4che.event.FTPListener;
import org.ftp4che.util.ftpfile.FTPFile;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;



public class FTPSConnection implements FTPListener {
	
	private static Class<?> PKG = JobEntryFTPSGet.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String HOME_FOLDER ="/";
	public static final String COMMAND_SUCCESSUL = "COMMAND SUCCESSFUL";
	
	public static final int CONNECTION_TYPE_FTP=0;
	public static final int CONNECTION_TYPE_FTP_IMPLICIT_SSL=1;
	public static final int CONNECTION_TYPE_FTP_AUTH_SSL=2;
	public static final int CONNECTION_TYPE_FTP_AUTH_TLS=3;
	public static final int CONNECTION_TYPE_FTP_IMPLICIT_TLS=4;
	

	public static final String[] connection_type_Desc = new String[] { 
		BaseMessages.getString(PKG, "JobFTPS.ConnectionType.FTP"), 
		BaseMessages.getString(PKG, "JobFTPS.ConnectionType.ImplicitSSL"),
		BaseMessages.getString(PKG, "JobFTPS.ConnectionType.AuthSSL"),
		BaseMessages.getString(PKG, "JobFTPS.ConnectionType.AuthTLS"),
		BaseMessages.getString(PKG, "JobFTPS.ConnectionType.ImplicitTLS")
	};
	
	public static final String[] connection_type_Code = new String[] {
		"FTP_CONNECTION", 
		"IMPLICIT_SSL_FTP_CONNECTION",
		"AUTH_SSL_FTP_CONNECTION", 
		"AUTH_TLS_FTP_CONNECTION",
		"IMPLICIT_TLS_FTP_CONNECTION"
	};	
	
	private FTPConnection connection = null;
	private ArrayList<String> replies = new ArrayList<String>();
	
	private String hostName;
	private int portNumber;
	private String userName;
	private String passWord;
	private int connectionType;
	private int timeOut;
	private boolean passiveMode;
	
	private String proxyHost;
	private String proxyUser;
	private String proxyPassword;
	private int proxyPort;
	
	public FTPSConnection(int connectionType, String hostname, int port, String username, String password) {
		this.hostName=hostname;
		this.portNumber=port;
		this.userName=username;
		this.passWord=password;
		this.connectionType=connectionType;
		this.passiveMode=false;
	}
	
	 /**
     * 
     * this method is used to set the proxy host
     * 
     * @param type
     *            true: proxy host
     */
	public void setProxyHost(String proxyhost) {
		this.proxyHost=proxyhost;
	}
	
	 /**
     * 
     * this method is used to set the proxy port
     * 
     * @param type
     *            true: proxy port
     */
	public void setProxyPort(int proxyport) {
		this.proxyPort=proxyport;
	}
	 /**
     * 
     * this method is used to set the proxy username
     * 
     * @param type
     *            true: proxy username
     */
	public void setProxyUser(String username){
		this.proxyUser=username;
	}
	 /**
     * 
     * this method is used to set the proxy password
     * 
     * @param type
     *            true: proxy password
     */
	public void setProxyPassword(String password){
		this.proxyPassword=password;
	}
	 /**
     * 
     * this method is used to connect to a remote host
     * 
     * @throws KettleException
     */
	public void connect() throws KettleException {
	    try {
	      connection = FTPConnectionFactory.getInstance(getProperties(hostName, portNumber, 
	    		  userName, passWord, connectionType, timeOut, passiveMode));
	      connection.addFTPStatusListener(this);
	      connection.connect();
	    } catch (Exception e)  {
	    	throw new KettleException(BaseMessages.getString(PKG, "JobFTPS.Error.Connecting",hostName),e);
	    }
	  }
	private Properties getProperties(String hostname, int port, String username, String password, 
			int connectionType, int timeout, boolean passiveMode) {
	    Properties pt = new Properties();
	    pt.setProperty("connection.host", hostname);
	    pt.setProperty("connection.port", String.valueOf(port));
	    pt.setProperty("user.login", username);
	    pt.setProperty("user.password", password);
	    pt.setProperty("connection.type", getConnectionType(connectionType));
	    pt.setProperty("connection.timeout", String.valueOf(timeout));
	    pt.setProperty("connection.passive", String.valueOf(passiveMode));
	    // Set proxy
	    if(this.proxyHost!=null) 	 pt.setProperty("proxy.host", this.proxyHost);
	    if(this.proxyPort!=0) 	 	 pt.setProperty("proxy.port", String.valueOf(this.proxyPort));
	    if(this.proxyUser!=null) 	 pt.setProperty("proxy.user", this.proxyUser);
	    if(this.proxyPassword!=null) pt.setProperty("proxy.pass", this.proxyPassword);
	    
	    return pt;
	  }
	 
	 public static String getConnectionTypeDesc(String tt) {
   	 if(Const.isEmpty(tt)) return connection_type_Desc[0]; 
		if(tt.equalsIgnoreCase(connection_type_Code[1]))
			return connection_type_Desc[1];
		else
			return connection_type_Desc[0]; 
    }
    public static String getConnectionTypeCode(String tt) {
   	if(tt==null) return connection_type_Code[0]; 
		if(tt.equals(connection_type_Desc[1]))
			return connection_type_Code[1];
		else
			return connection_type_Code[0]; 
    }
    public static String getConnectionTypeDesc(int i) {
		if (i < 0 || i >= connection_type_Desc.length)
			return connection_type_Desc[0];
		return connection_type_Desc[i];
	}
    public static String getConnectionType(int i) {
    	return connection_type_Code[i]; 
    }
	public static int getConnectionTypeByDesc(String tt) {
		if (tt == null) return 0;

		for (int i = 0; i < connection_type_Desc.length; i++) {
			if (connection_type_Desc[i].equalsIgnoreCase(tt))
				return i;
		}
		// If this fails,return the first value
		return 0;
	}
	public static int getConnectionTypeByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < connection_type_Code.length; i++) {
			if (connection_type_Code[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
	public static String getConnectionTypeCode(int i) {
		if (i < 0 || i >= connection_type_Code.length)
			return connection_type_Code[0];
		return connection_type_Code[i];
	}

    /**
     * public void setBinaryMode(boolean type)
     * 
     * this method is used to set the transfer type to binary
     * 
     * @param type
     *            true: Binary
     * @throws KettleException
     */
    public void setBinaryMode(boolean type) throws KettleException {
    	try {
	    	connection.setTransferType(true);
    	}catch(Exception e) {
    		throw new KettleException(e);
    	}
    }

    /**
     * 
     * this method is used to set the mode to passive
     * 
     * @param type
     *            true: passive mode
     */
	public void setPassiveMode(boolean passivemode) {
		this.passiveMode=passivemode;	
	}
	
	 /**
     * 
     * this method is used to return the passive mode
     * 
     * @return TRUE if we use passive mode
     *    
     */
	public boolean isPassiveMode() {
		return this.passiveMode;	
	}
	 /**
     * 
     * this method is used to set the timeout
     * 
     * @param timeout
     *    
     */
	public void setTimeOut(int timeout) {
		this.timeOut=timeout;	
	}
	 /**
     * 
     * this method is used to return the timeout
     * 
     * @return timeout
     *    
     */
	public int getTimeOut() {
		return this.timeOut;	
	}
	
	public ArrayList<String> getReplies() {
		return replies;
	}

    /**
     * 
     * this method is used to set the connection type
     * 
     * @param type
     *            true: connection type
     */
	public void setConnectionType(int connectiontype) {
		this.connectionType = connectiontype;	
	}
	
	public int getConnectionType() {
		return this.connectionType;	
	}
	
	public void connectionStatusChanged(FTPEvent arg0) {}

	public void replyMessageArrived(FTPEvent event) {
		this.replies = new ArrayList<String>();
		for (String e : event.getReply().getLines()) {
			if (!e.trim().equals("")) {
				e = e.substring(3).trim().replace("\n", "");
				if (!e.toUpperCase().contains(COMMAND_SUCCESSUL)) {
					e = e.substring(1).trim();
					replies.add(e);
				}
			}
		}
	}
	 /**
     * 
     * this method change FTP working directory
     * 
     * @param directory
     *           change the working directory
     * @throws KettleException
     */
	public void changeDirectory(String directory) throws KettleException {
		try {
			this.connection.changeDirectory(directory);
		}catch(Exception f) {
			throw new KettleException(BaseMessages.getString(PKG, "JobFTPS.Error.ChangingFolder", directory), f);
		}
	}
	 /**
     * 
     * this method is used to create a directory in remote host
     * 
     * @param  directory 
     * 		   directory name on remote host	 
     * @throws KettleException	
     */
	public void createDirectory(String directory) throws KettleException {
		try {
			this.connection.makeDirectory(directory);
		}catch(Exception f) {
			throw new KettleException(BaseMessages.getString(PKG, "JobFTPS.Error.CreationFolder",directory), f);
		}
	}

	public List<FTPFile> getFileList(String folder) throws KettleException{
		try {
			if (connection != null) {
				List<FTPFile> response = connection.getDirectoryListing(folder);
				return response;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new KettleException(e);
		}
	}
	 /**
     * 
     * this method is used to download a file from a remote host
     * 
     * @param  file 
     * 		   remote file to download	 
     * @param  localFilename
     * 		   target filename in local host 	
     * @throws KettleException	
     */
	public void downloadFile(FTPFile file, String localFilename) throws KettleException {
		try {
			File localFile= new File(localFilename);
			writeToFile(connection.downloadStream(file), localFile);
		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	private void writeToFile(InputStream is, File file)  throws KettleException  {
		try {
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			int c;
			while((c = is.read()) != -1) {
				out.writeByte(c);
			}
			is.close();
			out.close();
		} catch(IOException e) {
			throw new KettleException(BaseMessages.getString(PKG, "JobFTPS.Error.WritingToFile", file.getName()), e);
		}
	}
	
	 /**
     * 
     * this method is used to upload a file to a remote host
     * 
     * @param  localFileName 
     * 		   Local full filename	 
     * @param  shortFileName
     * 		   Filename in remote host 	
     * @throws KettleException	
     */
	public void uploadFile(String localFileName, String shortFileName) throws KettleException{
		FileObject file = null;
		
		try {
			file=KettleVFS.getFileObject(localFileName);
			this.connection.uploadStream(file.getContent().getInputStream(), new FTPFile(new File(shortFileName)));
		}catch(Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "JobFTPS.Error.UuploadingFile", localFileName), e);
		}finally {
			if(file!=null) {
				try {
					file.close();
				}catch(Exception e) {};
			}
		}
	}
	
	 /**
     * 
     * this method is used to delete a file in remote host
     * 
     * @param  file 
     * 		   File on remote host to delete	 
     * @throws KettleException	
     */
	public void deleteFile(FTPFile file) throws KettleException{
		try {
			this.connection.deleteFile(file);
		}catch(Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "JobFTPS.Error.DeletingFile", file.getName()), e);
		}
	}
	 /**
     * 
     * this method is used to move a file to remote directory
     * 
     * @param  fromFile 
     * 		   File on remote host to move	 
     * @param  targetFoldername
     * 		   Target remote folder
     * @throws KettleException	
     */
	public void moveToFolder(FTPFile fromFile, String targetFoldername) throws KettleException{
		try {
			this.connection.renameFile(fromFile, new FTPFile(targetFoldername, fromFile.getName()));
		}catch(Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "JobFTPS.Error.MovingFileToFolder", fromFile.getName(), targetFoldername), e);
		}
	}
	
    /**
     * 
     * Checks if a directory exists
     * 
     * @return 
     * 	true if the directory exists
     *   
     */
	public boolean isDirectoryExists(String directory) {
		String currectDirectory=null;
		boolean retval = false;
		try {
			// Before save current directory
			currectDirectory = this.connection.getWorkDirectory();
			// Change directory
			this.connection.changeDirectory(directory);
			retval= true;
		}catch(Exception e) {}
		finally {
			// switch back to the current directory
			if(currectDirectory!=null) {
				try {
					this.connection.changeDirectory(currectDirectory);
				}catch(Exception e){};
			}
		}
		return retval;
	}
	
	
    /**
     * 
     * Returns the working directory
     * 
     * @return working directory
     * @throws Exception
     */
	public String getWorkingDirectory() throws Exception {
		return this.connection.getWorkDirectory();
	}

	/**
     * 
     * this method is used to disconnect the connection
     * 
     */
	public void disconnect() {
		if(this.connection!=null) this.connection.disconnect();
		if(this.replies!=null) this.replies.clear();
	}

}
