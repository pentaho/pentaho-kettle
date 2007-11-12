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
package org.pentaho.di.job.entries.sftp;

import java.net.InetAddress;

import org.pentaho.di.core.exception.KettleJobException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SFTPClient {
	
	InetAddress serverIP;
	int serverPort;
	String userName;
	String password;	
	
	private Session s;
	private ChannelSftp c;
	
	/**
	 * Init Helper Class with connection settings
	 * @param serverIP IP address of remote server
	 * @throws KettleJobException 
	 */
	public SFTPClient(InetAddress serverIP, int serverPort, String userName) throws KettleJobException{
		
		if(		serverIP == null ||
				serverPort < 0 ||
				userName == null ||
				userName.equals("")){
			throw new KettleJobException("For a SFTP connection server name and username must be set and server port must be greater than zero.");
		}

		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.userName = userName;
		
		JSch jsch = new JSch();
		try {
			s = jsch.getSession(userName, serverIP.getHostAddress(), serverPort);
		} catch (JSchException e) {
			throw new KettleJobException(e);
		}
	}

	public String getPassword() {
		return password;
	}

	public int getServerPort() {
		return serverPort;
	}

	public String getUserName() {
		return userName;
	}

	public InetAddress getServerIP() {
		return serverIP;
	}

	public void login(String password) throws KettleJobException{
		this.password = password;
		
		s.setPassword(this.getPassword());
		try {
			java.util.Properties config=new java.util.Properties();
		    config.put("StrictHostKeyChecking", "no");
		    s.setConfig(config);
			s.connect();
			Channel channel=s.openChannel("sftp");
		    channel.connect();
		    c=(ChannelSftp)channel;
		} catch (JSchException e) {
			throw new KettleJobException(e);
		}
	}
	
	public void chdir(String dirToChangeTo) throws KettleJobException {
		try {
			c.cd(dirToChangeTo);
		} catch (SftpException e) {
			throw new KettleJobException(e);
		}
	}
	
	public String[] dir() throws KettleJobException {
		String[] fileList = null;
		
		try {
			java.util.Vector<?> v=c.ls(".");
			java.util.Vector<String> o = new java.util.Vector<String>();
			if(v!=null){
			  for(int i=0; i<v.size(); i++){
		    	  Object obj=v.elementAt(i);
		    	  if(obj!= null && obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry){
		    		  LsEntry lse = (com.jcraft.jsch.ChannelSftp.LsEntry)obj;
		    		  if(!lse.getAttrs().isDir()) o.add(lse.getFilename());
		    	  }
		      }
			}
			if(o.size()>0){
				fileList = new String[o.size()];
				o.copyInto(fileList);
			}
		} catch (SftpException e) {
			throw new KettleJobException(e);
		}
		
		return fileList;
	}
	
	public void get (String localFilePath, String remoteFile) throws KettleJobException {
		int mode=ChannelSftp.OVERWRITE;
		try {
			c.get(remoteFile, localFilePath, null, mode);
		} catch (SftpException e) {
			throw new KettleJobException(e);
		}
	}
	
	public void put (String localFilePath, String remoteFile) throws KettleJobException {
		int mode=ChannelSftp.OVERWRITE;
		try {
			c.put(localFilePath, remoteFile, null, mode);
		} catch (SftpException e) {
			throw new KettleJobException(e);
		}
	}
	
	public void delete (String file) throws KettleJobException {
		try {
			c.rm(file);
		} catch (SftpException e) {
			throw new KettleJobException(e);
		}
	}
	
	public void disconnect() {
		c.disconnect();
		s.disconnect();
	}
}
