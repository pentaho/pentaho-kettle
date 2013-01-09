/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.autodoc;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.jfree.ui.Drawable;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;

public class KettleFileTableModel implements TableModel {
	
	public enum Field {
		location(ReportSubjectLocation.class),
		filename(String.class), 
		name(String.class), 
		description(String.class), 
		extended_description(String.class), 
		logging(String.class), 
		creation(String.class), 
		modification(String.class), 
		last_exec_result(String.class), 
		image(Drawable.class),
		;
		
		private Class<?> clazz;
		
		private Field(Class<?> clazz) {
			this.clazz = clazz;
		}
		
		public Class<?> getClazz() {
			return clazz;
		}
	}
	
	private List<ReportSubjectLocation> locations;
	private LoggingObjectInterface	parentObject;
	private LogChannelInterface	log;
	
	public KettleFileTableModel() {
  }

	public KettleFileTableModel(LoggingObjectInterface parentObject, List<ReportSubjectLocation> locations) {
		this.parentObject = parentObject;
		this.locations = locations;
		this.log = new LogChannel("Kettle File Table Model", parentObject);
	}

	public void addTableModelListener(TableModelListener tableModelListener) {
	}

	public Class<?> getColumnClass(int index) {
		return Field.values()[index].getClazz();
	}

	public int getColumnCount() {
		return Field.values().length;
	}

	public String getColumnName(int index) {
		return Field.values()[index].name();
	}

	public int getRowCount() {
		return locations.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		
		ReportSubjectLocation location = locations.get(rowIndex);
		
		Field field = Field.values()[columnIndex];
		
		try {
			switch(field) {
			case location: return location;
			case filename : return location.getFilename();
			case name : return getName(location); 
			case description : return getDescription(location);
			case extended_description: return getExtendedDescription(location);
			case logging: return getLogging(location);
			case creation: return getCreation(location);
			case modification : return getModification(location);
			case last_exec_result: return getLastExecutionResult(log, parentObject, location);
			case image: return getImage(location);
			default:
				throw new RuntimeException("Unhandled field type: "+field+" in function getValueAt()");
			}
		} catch(Exception e) {
			log.logError("Unable to get data for field: "+field+" : "+e.getMessage());
			return null;
		}
	}

	public static String getName(ReportSubjectLocation filename) throws KettleException {
		if (filename.isTransformation()) {
			return TransformationInformation.getInstance().getTransMeta(filename).getName();
		} else {
			return JobInformation.getInstance().getJobMeta(filename).getName();
		}
	}

	public static String getDescription(ReportSubjectLocation filename) throws KettleException {
		if (filename.isTransformation()) {
			return TransformationInformation.getInstance().getTransMeta(filename).getDescription();
		} else {
			return JobInformation.getInstance().getJobMeta(filename).getDescription();
		}
	}

	public static String getExtendedDescription(ReportSubjectLocation filename) throws KettleException {
		if (filename.isTransformation()) {
			return TransformationInformation.getInstance().getTransMeta(filename).getExtendedDescription();
		} else {
			return JobInformation.getInstance().getJobMeta(filename).getExtendedDescription();
		}
	}

	public static String getLogging(ReportSubjectLocation filename) throws KettleException {
		List<LogTableInterface> logTables;
		if (filename.isTransformation()) {
			TransMeta transMeta = TransformationInformation.getInstance().getTransMeta(filename);
			logTables = transMeta.getLogTables();
		} else {
			JobMeta jobMeta  = JobInformation.getInstance().getJobMeta(filename);
			logTables = jobMeta.getLogTables();
		}
		String logging="";
		for (Iterator<LogTableInterface> iterator = logTables.iterator(); iterator.hasNext();) {
			LogTableInterface logTableInterface = (LogTableInterface) iterator.next();
			if (logTableInterface.getDatabaseMeta()!=null && !Const.isEmpty(logTableInterface.getTableName())) {
				if (logging.length()>0) logging+=", ";
				logging+=logTableInterface.getTableName()+"@"+logTableInterface.getDatabaseMeta().getName();
			}
		}
		return logging;
	}

	public static BufferedImage getImage(ReportSubjectLocation filename) throws KettleException {
		if (filename.isTransformation()) {
			return TransformationInformation.getInstance().getImage(filename);
		} else {
			return JobInformation.getInstance().getImage(filename);
		}
	}

	public static String getCreation(ReportSubjectLocation filename) throws KettleException {
		Date date = null;
		String user = null ;
		if (filename.isTransformation()) {
			date = TransformationInformation.getInstance().getTransMeta(filename).getCreatedDate();
			user = TransformationInformation.getInstance().getTransMeta(filename).getCreatedUser();
		} else {
			date = JobInformation.getInstance().getJobMeta(filename).getCreatedDate();
			user = JobInformation.getInstance().getJobMeta(filename).getCreatedUser();
		}
		return Const.NVL(XMLHandler.date2string(date), "-")+" by "+Const.NVL(user, "-");
	}

	public static String getModification(ReportSubjectLocation filename)  throws KettleException {
		Date date = null;
		String user = null ;
		if (filename.isTransformation()) {
			date = TransformationInformation.getInstance().getTransMeta(filename).getModifiedDate();
			user = TransformationInformation.getInstance().getTransMeta(filename).getModifiedUser();
		} else {
			date = JobInformation.getInstance().getJobMeta(filename).getModifiedDate();
			user = JobInformation.getInstance().getJobMeta(filename).getModifiedUser();
		}
		return Const.NVL(XMLHandler.date2string(date), "-")+" by "+Const.NVL(user, "-");
	}

	public static String getLastExecutionResult(LogChannelInterface log, LoggingObjectInterface parentObject, ReportSubjectLocation filename) throws KettleException {
	
		LogTableInterface logTable=null;
		if (filename.isTransformation()) {
			TransMeta transMeta = TransformationInformation.getInstance().getTransMeta(filename);
			logTable = transMeta.getTransLogTable();
		} else {
			JobMeta jobMeta  = JobInformation.getInstance().getJobMeta(filename);
			logTable = jobMeta.getJobLogTable();
		}
		if (logTable!=null && logTable.isDefined()) {
			DatabaseMeta dbMeta = logTable.getDatabaseMeta();
			Database database = new Database(parentObject, dbMeta);
			try {
				database.connect();
				String sql="SELECT ";
				sql+=dbMeta.quoteField(logTable.getStatusField().getFieldName())+", ";
				sql+=dbMeta.quoteField(logTable.getLogDateField().getFieldName())+", ";
				sql+=dbMeta.quoteField(logTable.getErrorsField().getFieldName())+"";
				sql+=" FROM ";
				sql+=dbMeta.getQuotedSchemaTableCombination(logTable.getSchemaName(), logTable.getTableName());
				sql+=" ORDER BY "+dbMeta.quoteField(logTable.getLogDateField().getFieldName())+" DESC";
				
				RowMetaAndData oneRow = database.getOneRow(sql);
				String status = oneRow.getString(0, "?");
				Date date = oneRow.getDate(1, null);
				Long nrErrors = oneRow.getInteger(2);
				
				String evaluation;
				if (status.equalsIgnoreCase(LogStatus.END.getStatus())) {
					evaluation = "Ended";
				} else if (status.equalsIgnoreCase(LogStatus.START.getStatus())) {
					evaluation = "Started";
				} else if (status.equalsIgnoreCase(LogStatus.STOP.getStatus())) {
					evaluation = "Stopped";
				} else if (status.equalsIgnoreCase(LogStatus.RUNNING.getStatus())) {
					evaluation = "Running";
				} else if (status.equalsIgnoreCase(LogStatus.PAUSED.getStatus())) {
					evaluation = "Paused";
				} else if (status.equalsIgnoreCase(LogStatus.ERROR.getStatus())) {
					evaluation = "Failed";
				} else {
					evaluation = "Unknown";
				}
				if (nrErrors>0) {
					evaluation+=" with errors";
				} else {
					evaluation+=" with success";
				}
				
				return evaluation+" at "+XMLHandler.date2string(date);
				
			} catch(Exception e) {
				log.logBasic("Unable to get logging information from log table"+logTable);
			} finally {
				database.disconnect();
			}
		}
		return null;
	}


	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

	public void removeTableModelListener(TableModelListener arg0) {
	}

	public void setValueAt(Object arg0, int arg1, int arg2) {
	}

	
}
