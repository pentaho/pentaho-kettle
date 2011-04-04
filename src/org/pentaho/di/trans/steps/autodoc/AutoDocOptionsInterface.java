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
