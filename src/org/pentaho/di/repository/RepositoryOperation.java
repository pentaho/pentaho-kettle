package org.pentaho.di.repository;

public enum RepositoryOperation {

	READ_TRANSFORMATION("Read transformation"),
	MODIFY_TRANSFORMATION("Modify transformation"),
	DELETE_TRANSFORMATION("Delete transformation"),
	EXECUTE_TRANSFORMATION("Execute transformation"),
	
	READ_JOB("Read job"),
	MODIFY_JOB("Modify job"),
	DELETE_JOB("Delete job"),
	EXECUTE_JOB("Execute job"),
	
	MODIFY_DATABASE("Modify database connection"),
	DELETE_DATABASE("Delete database connection"),
	EXPLORE_DATABASE("Explore database connection"),
	
	;
		
	private final String description;
	
	RepositoryOperation(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String toString() {
		return description;
	}
}
