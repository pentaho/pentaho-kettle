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

import org.pentaho.di.trans.steps.autodoc.KettleReportBuilder.OutputType;

public interface AutoDocOptionsInterface {
	
	/**
	 * @return the outputType
	 */
	public OutputType getOutputType();
	
	/**
	 * @return the includingName
	 */
	public boolean isIncludingName();

	/**
	 * @return the includingDescription
	 */
	public boolean isIncludingDescription();

	/**
	 * @return the includingExtendedDescription
	 */
	public boolean isIncludingExtendedDescription();

	/**
	 * @return the includingCreated
	 */
	public boolean isIncludingCreated();

	/**
	 * @return the includingModified
	 */
	public boolean isIncludingModified();

	/**
	 * @return the includingLoggingConfiguration
	 */
	public boolean isIncludingLoggingConfiguration();

	/**
	 * @return the includingImage
	 */
	public boolean isIncludingImage();

	/**
	 * @return the includingLastExecutionResult
	 */
	public boolean isIncludingLastExecutionResult();
	
}
