package org.pentaho.di.repository;

import java.util.Date;

/**
 * Any revision of repository object can be given a version against which it is being developed.
 * 
 * As such, if you refer to a certain version, you are referencing a group of objects that have been labeled with that specific version.
 * 
 * @author matt
 */
public interface ObjectVersion {
	
	/**
	 * The version label, short code to uniquely identify the version.
	 * For example, "1.0.3-M1"
	 * 
	 * @return the version label
	 */
	public String getLabel();
	
	/**
	 * Set the version label, a short code to uniquely identify the version.
	 * For example, "1.0.3-M1"
	 * @param label the version label to set.
	 */
	public void setLabel(String label);
	
	/**
	 * The version description is the long description of the version.
	 * For example, "1.0.3 Milestone 1"
	 * 
	 * @return the version description
	 */
	public String getDescription();
	
	/**
	 * Set the version description, the long description of the version.
	 * For example, "1.0.3 Milestone 1"
	 * 
	 * @param description the version description to set
	 */
	public void setDescription(String description);
	
	/**
	 * Get the label of the previous version.
	 * 
	 * @return The label of the previous version or null if none is defined.
	 */
	public String getPreviousVersion();
	
	/**
	 * Set the label of the previous version
	 * @param previousVersionLabel The label of the previous version to set or null if none is defined.
	 */
	public void setPreviousVersion(String previousVersionLabel);
	
	/**
	 * The planned release date.
	 * @return the planned release date or null if none is set.
	 */
	public Date getPlannedReleaseDate();

	/**
	 * Set the planned release date.
	 * @param the planned release date to set or null if none is set.
	 */
	public void setPlannedReleaseDate(Date plannedReleaseDate);
}
