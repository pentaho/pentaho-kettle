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
package org.pentaho.di.trans.step.errorhandling;

import org.pentaho.di.core.Const;
import org.pentaho.di.trans.step.StepMeta;


public class Stream implements StreamInterface {

	private String description;
	private StreamType streamType;
	private StepMeta stepMeta;
	private StreamIcon	streamIcon;
	private Object subject;
	
	/**
	 * @param streamType
	 * @param stepname
	 * @param stepMeta
	 * @param description
	 */
	public Stream(StreamType streamType, StepMeta stepMeta, String description, StreamIcon streamIcon, Object subject) {
		this.streamType = streamType;
		this.stepMeta = stepMeta;
		this.description = description;
		this.streamIcon = streamIcon;
		this.subject = subject;
	}

	public String toString() {
		if (stepMeta==null) {
			return "Stream type "+streamType+Const.CR+description;
		} else {
			return "Stream type "+streamType+" for step '"+stepMeta.getName()+"'"+Const.CR+description;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
	  if (!(obj instanceof StreamInterface)) return false;
	  if (obj == this) return true;
	  
	  StreamInterface stream = (StreamInterface) obj;
	  
	  if (description.equals(stream.getDescription())) return true;
	  	  
	  return false;
	}
	
	public String getStepname() {
		if (stepMeta==null) return null;
		return stepMeta.getName();
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the streamType
	 */
	public StreamType getStreamType() {
		return streamType;
	}
	/**
	 * @param streamType the streamType to set
	 */
	public void setStreamType(StreamType streamType) {
		this.streamType = streamType;
	}

	/**
	 * @return the stepMeta
	 */
	public StepMeta getStepMeta() {
		return stepMeta;
	}

	/**
	 * @param stepMeta the stepMeta to set
	 */
	public void setStepMeta(StepMeta stepMeta) {
		this.stepMeta = stepMeta;
	}

	/**
	 * @return the streamIcon
	 */
	public StreamIcon getStreamIcon() {
		return streamIcon;
	}

	/**
	 * @param streamIcon the streamIcon to set
	 */
	public void setStreamIcon(StreamIcon streamIcon) {
		this.streamIcon = streamIcon;
	}

	/**
	 * @return the subject
	 */
	public Object getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(Object subject) {
		this.subject = subject;
	}
	

}
