package org.pentaho.di.repository;

import java.util.Date;

public class SimpleObjectVersion implements ObjectVersion {
	private String label;
	private String description;
	private String previousVersion;
	private Date plannedReleaseDate;
	
	/**
	 * @param label
	 * @param description
	 * @param previousVersion
	 * @param plannedReleaseDate
	 */
	public SimpleObjectVersion(String label, String description, String previousVersionLabel, Date plannedReleaseDate) {
		this.label = label;
		this.description = description;
		this.previousVersion = previousVersionLabel;
		this.plannedReleaseDate = plannedReleaseDate;
	}
	
	public String toString() {
		return label;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ObjectVersion)) return false;
		if (this == obj) return true;
	
		return label.equals( ((ObjectVersion)obj).getLabel() );
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
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
	 * @return the previousVersion label
	 */
	public String getPreviousVersion() {
		return previousVersion;
	}
	
	/**
	 * @param previousVersionLabel the label to the previousVersion to set
	 */
	public void setPreviousVersion(String previousVersionLabel) {
		this.previousVersion = previousVersionLabel;
	}
	
	/**
	 * @return the plannedReleaseDate
	 */
	public Date getPlannedReleaseDate() {
		return plannedReleaseDate;
	}
	
	/**
	 * @param plannedReleaseDate the plannedReleaseDate to set
	 */
	public void setPlannedReleaseDate(Date plannedReleaseDate) {
		this.plannedReleaseDate = plannedReleaseDate;
	}
	
	
	
}
