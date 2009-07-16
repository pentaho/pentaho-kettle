package org.pentaho.di.repository;

public enum RepositoryObjectType {

	TRANSFORMATION("transformation", ".ktr"), 
	JOB("job", ".kjb"),
	DATABASE("database", ".kdb"),
	SLAVE_SERVER("slave server", ".ksl"),
	CLUSTER_SCHEMA("cluster schema", ".kcs"),
	PARTITION_SCHEMA("partition schema", ".kps"),
	STEP("step", ".kst"),
	JOB_ENTRY("job entry", ".kje"),

	// non-standard, Kettle database repository only!
	//
	// USER("user", ".usr"),

	; 
	
	private String	typeDescription;
	private String  extension;
	
	private RepositoryObjectType(String typeDescription, String extension) {
		this.typeDescription = typeDescription;
		this.extension = extension;
	}
	
	public String toString() {
		return typeDescription;
	}
	
	public String getTypeDescription() {
		return typeDescription;
	}
	
	public String getExtension() {
		return extension;
	}
}
