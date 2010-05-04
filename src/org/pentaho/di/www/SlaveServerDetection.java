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
package org.pentaho.di.www;

import java.util.Date;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

/**
 * This class is what describes the information that we keep when we detect a slave server in the cloud / cluster.<br>
 * It contains the slave server information as well as the time it was last seen alive, whether it is still alive, etc.<br>
 * <br>
 * @author matt
 * @since 2008-OCT-08
 */
public class SlaveServerDetection {
	
	public static final String XML_TAG = "SlaveServerDetection";
	
	public enum SlaveRelationShip {

		SLAVE_SEVER("SLAVE_SERVER", "Slave server"),  // Regular slave server
		FAILOVER_MASTER("FAILOVER_MASTER", "Fail-over master server"); // Fail-over master server
		
		private String code, description;
		
		private SlaveRelationShip(String code, String description) {
			this.code = code;
			this.description = description;
		}
		
		/**
		 * @return the code
		 */
		public String getCode() {
			return code;
		}

		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}
	}
	
	public static final SlaveRelationShip getSlaveRelationShip(String code) {
		for (SlaveRelationShip slaveRelationShip : SlaveRelationShip.values()) {
			if (slaveRelationShip.getCode().equalsIgnoreCase(code)) {
				return slaveRelationShip;
			}
		}
		return null;
	}
		
	private SlaveServer slaveServer;
	private boolean     active;
	private Date        lastActiveDate;
	private Date        lastInactiveDate;
	
	
	public SlaveServerDetection(SlaveServer slaveServer) {
		this.slaveServer = slaveServer;
		this.active = true;
		this.lastActiveDate = new Date();
		this.lastInactiveDate = null; // It's active
	}
	
	public String getXML() {
		StringBuffer xml = new StringBuffer();

        xml.append(XMLHandler.openTag(XML_TAG)).append(Const.CR);
        xml.append(slaveServer.getXML());
        xml.append(XMLHandler.addTagValue("active", active));
        xml.append(XMLHandler.addTagValue("last_active_date", lastActiveDate));
        xml.append(XMLHandler.addTagValue("last_inactive_date", lastInactiveDate));
        xml.append(XMLHandler.closeTag(XML_TAG)).append(Const.CR);

        return xml.toString();
	}
	
	public SlaveServerDetection(Node node)
    {
		slaveServer = new SlaveServer(XMLHandler.getSubNode(node, SlaveServer.XML_TAG));
		active = "Y".equalsIgnoreCase(XMLHandler.getTagValue(node, "active"));
		lastActiveDate = XMLHandler.stringToDate(XMLHandler.getTagValue(node, "last_active_date"));
		lastInactiveDate = XMLHandler.stringToDate(XMLHandler.getTagValue(node, "last_inactive_date"));
    }
	
	public boolean equals(Object ssd) {
		return slaveServer.equals(((SlaveServerDetection)ssd).getSlaveServer());
	}
	
	public int hashCode() {
		return slaveServer.hashCode();
	}

	/**
	 * @return the slaveServer
	 */
	public SlaveServer getSlaveServer() {
		return slaveServer;
	}

	/**
	 * @param slaveServer the slaveServer to set
	 */
	public void setSlaveServer(SlaveServer slaveServer) {
		this.slaveServer = slaveServer;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the lastActiveDate
	 */
	public Date getLastActiveDate() {
		return lastActiveDate;
	}

	/**
	 * @param lastActiveDate the lastActiveDate to set
	 */
	public void setLastActiveDate(Date lastActiveDate) {
		this.lastActiveDate = lastActiveDate;
	}

	/**
	 * @return the lastInactiveDate
	 */
	public Date getLastInactiveDate() {
		return lastInactiveDate;
	}

	/**
	 * @param lastInactiveDate the lastInactiveDate to set
	 */
	public void setLastInactiveDate(Date lastInactiveDate) {
		this.lastInactiveDate = lastInactiveDate;
	}
}
