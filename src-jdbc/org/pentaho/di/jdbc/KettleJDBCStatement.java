/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/lgpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Bayon Technologies, Inc.  All rights reserved. 
 */

package org.pentaho.di.jdbc;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;

public class KettleJDBCStatement implements Statement {
	private transient static final Log log = LogFactory.getLog(KettleJDBCStatement.class);
	private String url = null;
	private static Map<String,String[]> stepsMap = new Hashtable<String,String[]>();
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	protected List<RowMetaAndData> rowAndDatas;
	protected ConnectionJDBC3 connection;

	public void setConnection(ConnectionJDBC3 connection) {
		this.connection = connection;
	}

//	public KettleJDBCStatement() {
//
//	}

	public KettleJDBCStatement(ConnectionJDBC3 connection,String url) {
		log.debug("KettleJDBCStatement");
		this.url = url;
		try {
			KettleEnvironment.init();
		} catch (KettleException e) {
			throw new RuntimeException("Unable to initialize Kettle", e);
		}
		this.connection = connection;
	}

	RowStepListener listener = null;
	private boolean closed = false;
	protected String columnStr;
	private int queryTimeout=100000;
	private int maxrows=50000;
	private int fetchsize;

	private int maxfieldsize=100;

	public void addBatch(String sql) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void cancel() throws SQLException {
		

	}

	public void clearBatch() throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void clearWarnings() throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void close() throws SQLException {
		
	}

	public boolean execute(String sql) throws SQLException {
		if (sql == null || sql.length() == 0) {
			throw new SQLException(Messages.get("error.generic.nosql"), "HY000");
		}

		// checkOpen();
		initialize();
		TransMeta meta = null;
		Trans trans = null;
		try {
			String tmp[] = SQLParser.parse(sql, null, connection, true);
			String table = tmp[3];
			String where = tmp[1];
			String fileName = "";
			String stepName = "";
			int index = table.indexOf(".");
			if(index!=-1)
			{
				stepName = table.substring(index+1);
				fileName = table.substring(0,index);
			}
			else
			{
				stepName = table;
			}
			columnStr = tmp[2];
			
			String kettleurl = this.url.substring(url
					.indexOf(KettleDriver.driverPrefix)
					+ KettleDriver.driverPrefix.length());
			URLParser p = new URLParser();
			p.parse(kettleurl);
			
			
			//if the kettleurl is a directory, then we should visit all transformations or jobs under this directory
			String fileUrl = p.getKettleUrl();
			if (fileUrl.indexOf("file://") != -1) {
				fileUrl = fileUrl.substring(fileUrl.indexOf("file://") + 7);
			} else if (fileUrl.indexOf("file:///") != -1) {
				fileUrl = fileUrl.substring(fileUrl.indexOf("file://") + 8);
			}
			File f = new File(fileUrl);
			
			if(f.isDirectory())
			{
				if( visitDirectory(f))
				{
					//execute the transformation
					log.debug("file="+p.getKettleUrl()+File.separator+fileName+".ktr");
					meta = new TransMeta(p.getKettleUrl()+File.separator+fileName+".ktr");	
				}
			}
			else
			{
				meta = new TransMeta(p.getKettleUrl());
//				meta = new TransMeta(f.getAbsolutePath());
			}
			if(meta==null)
			{
				throw new SQLException(
				"The  transformation or job"+fileUrl+"  is not valid.");
			}
			if(p.getOptions()!=null)
			{
				//loop the options and set them into kettle meta
				setVariables(meta,p.getOptions());
				
			}
			//set the where clause
			if(where!=null&&where.length()>1)
			{
				if(where.indexOf(" and")==-1)
				{
					int windex= where.indexOf("=");
					if(windex!=-1)
					{
						String variableName=where.substring(0,windex);
						variableName = variableName.trim();
						int kindex = variableName.lastIndexOf(".");
						if(kindex!=-1)
						{
							variableName=variableName.substring(kindex+1);
						}
						String variableValue=where.substring(windex+1);
						meta.setVariable(variableName, variableValue);
					}
				}
				else
				{
					int andIndex = where.indexOf("and");
					while(andIndex!=-1)
					{
						String tmpStr =where.substring(0,andIndex);
						where = where.substring(andIndex+3);
						andIndex = where.indexOf("and");
						tmpStr=tmpStr.trim();
						int windex= tmpStr.indexOf("=");
						if(windex!=-1)
						{
							String variableName=tmpStr.substring(0,windex);
							variableName = variableName.trim();
							int kindex = variableName.lastIndexOf(".");
							if(kindex!=-1)
							{
								variableName=variableName.substring(kindex+1);
							}
							String variableValue=tmpStr.substring(windex+1);
							meta.setVariable(variableName, variableValue);
						}
					}
				}
			}
			trans = new Trans(meta);
			trans.prepareExecution(null);
			this.listener = new RowStepListener();
			
			
			//
			String[] stepNames = meta.getStepNames();
			if (stepNames == null || stepNames.length == 0) {
				throw new SQLException(
						"The  transformation or job  is not valid.");
			}
	
			StepInterface si1 = trans.getStepInterface(stepName, 0);
			si1.addRowListener(listener);
			trans.startThreads();
			trans.waitUntilFinished();
//			System.out.println(listener.getRowsWritten());
			this.rowAndDatas = listener.getRowsWritten();

			if (trans.getErrors() == 0)
				return true;
		} catch (KettleException e) {
			e.printStackTrace();
			log.error(e.getMessage());
			throw new SQLException(e.getMessage());
		}

		return false;
	}

	private void setVariables(TransMeta meta, String[] options) {
		for (int i = 0; i < options.length; i++) {
			String option = options[i];
			int index = option.indexOf("=");
			String variableName=option.substring(0,index);
			String variableValue=option.substring(index+1);
			meta.setVariable(variableName, variableValue);
		}
		
	}

	public static boolean visitDirectory(File f) {
		File[] files= f.listFiles();
		if(files!=null)
		{
			for (int i = 0; i < files.length; i++) {
				try {
					if(files[i].getName().lastIndexOf("ktr")!=-1)
					addMetadata(files[i]);
				} catch (KettleXMLException e) {
					
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
		
	}

	private static void addMetadata(File file) throws KettleXMLException {
		TransMeta tm = new TransMeta(file.getAbsolutePath());
		log.debug(java.util.Arrays.toString(tm.getStepNames()));;
		stepsMap.put(file.getName(),tm.getStepNames());
	}

	private void initialize() {
		
		try {
			KettleEnvironment.init();
		} catch (KettleException e) {
			throw new RuntimeException("Unable to initialize Kettle", e);
		}
	}

	/**
	 * Check that this statement is still open.
	 * 
	 * @throws SQLException
	 *             if statement closed.
	 */
	protected void checkOpen() throws SQLException {
		if (closed || connection == null || connection.isClosed()) {
			throw new SQLException(Messages.get("error.generic.closed",
					"Statement"), "HY010");
		}
	}

	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {

		return execute(sql);
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public int[] executeBatch() throws SQLException {
		throw new UnsupportedOperationException();

	}

	public ResultSet executeQuery(String sql) throws SQLException {
		
		execute(sql);
		KettleJDBCResultSet rs = new KettleJDBCResultSet(this,rowAndDatas,columnStr);

		return rs;
	}

	public int executeUpdate(String sql) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		throw new UnsupportedOperationException();

	}

	public Connection getConnection() throws SQLException {
		
		return this.connection;
	}

	public int getFetchDirection() throws SQLException {
		throw new UnsupportedOperationException();

	}

	public int getFetchSize() throws SQLException {
		
		if(this.fetchsize<=0)
		{
			this.fetchsize = 10000;
		}
		log.debug("getFetchSize():"+this.fetchsize);
		return this.fetchsize;

	}

	public ResultSet getGeneratedKeys() throws SQLException {
		throw new UnsupportedOperationException();

	}

	public int getMaxFieldSize() throws SQLException {
		return this.maxfieldsize;

	}

	public int getMaxRows() throws SQLException {
		return this.maxrows;

	}

	public boolean getMoreResults() throws SQLException {
		throw new UnsupportedOperationException();

	}

	public boolean getMoreResults(int current) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public int getQueryTimeout() throws SQLException {
		return this.queryTimeout;

	}

	public ResultSet getResultSet() throws SQLException {
		KettleJDBCResultSet rs = new KettleJDBCResultSet(this,rowAndDatas,columnStr);
		return rs;
	}

	public int getResultSetConcurrency() throws SQLException {
		throw new UnsupportedOperationException();

	}

	public int getResultSetHoldability() throws SQLException {
		throw new UnsupportedOperationException();

	}

	public int getResultSetType() throws SQLException {
		throw new UnsupportedOperationException();

	}

	public int getUpdateCount() throws SQLException {
		throw new UnsupportedOperationException();

	}

	public SQLWarning getWarnings() throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setCursorName(String name) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setFetchDirection(int direction) throws SQLException {
		throw new UnsupportedOperationException();

	}

	public void setFetchSize(int rows) throws SQLException {
		this.fetchsize = rows;

	}

	public void setMaxFieldSize(int max) throws SQLException {
//		this.maxfieldsize = max;

	}

	public void setMaxRows(int max) throws SQLException {
		
//		this.maxrows = max;
	}

	public void setQueryTimeout(int seconds) throws SQLException {
//		this.queryTimeout = seconds;

	}
	
	public static void main(String[] args) throws Exception {
	  KettleEnvironment.init();
		EnvUtil.environmentInit();
		String fileDir="E:/project/kettlejdbc-google/trunk/samples";
		KettleJDBCStatement.visitDirectory(new File(fileDir));
	}

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isClosed() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isPoolable() throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  public void setPoolable(boolean poolable) throws SQLException {
    // TODO Auto-generated method stub
  }

}
