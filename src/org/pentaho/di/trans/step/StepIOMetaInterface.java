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
package org.pentaho.di.trans.step;

import java.util.List;

import org.pentaho.di.trans.step.errorhandling.StreamInterface;

public interface StepIOMetaInterface {
	
	public boolean isInputAcceptor();
	public boolean isOutputProducer();
	public boolean isInputOptional();
	public boolean isSortedDataRequired();
	public List<StreamInterface> getInfoStreams();
	public List<StreamInterface> getTargetStreams();
	
	public String[] getInfoStepnames();
	public String[] getTargetStepnames();
	
    /**
     * Replace the info steps with the supplied source steps.
     * 
     * @param infoSteps
     */
	public void setInfoSteps(StepMeta[] infoSteps);
	
	/**
	 * Add a stream to the steps I/O interface
	 * 
	 * @param stream The stream to add
	 */
	public void addStream(StreamInterface stream);
		
	/**
	 * Set the general info stream description
	 * @param string the info streams description
	 */
	public void setGeneralInfoDescription(String string);	

	/**
	 * Set the general target stream description
	 * @param string the target streams description
	 */
	public void setGeneralTargetDescription(String string);
			
	/**
	 * @return the generalTargetDescription
	 */
	public String getGeneralTargetDescription();

	/**
	 * @return the generalInfoDescription
	 */
	public String getGeneralInfoDescription();

	/**
	 * @return true if the output targets of this step are dynamic (variable)
	 */
	public boolean isOutputDynamic();

	/**
	 * @param outputDynamic set to true if the output targets of this step are dynamic (variable)
	 */
	public void setOutputDynamic(boolean outputDynamic);
	
	/**
	 * @return true if the input info sources of this step are dynamic (variable)
	 */
	public boolean isInputDynamic();

	/**
	 * @param inputDynamic set to true if the input info sources of this step are dynamic (variable)
	 */
	public void setInputDynamic(boolean inputDynamic);

	public StreamInterface findTargetStream(StepMeta targetStep);
	public StreamInterface findInfoStream(StepMeta infoStep);
}
