package org.pentaho.di.repository;


public class RepositoryObjectRecipient implements ObjectRecipient{

	private String name;

	private Type type;

	// ~ Constructors
	// ====================================================================================================

	public RepositoryObjectRecipient(String name) {
		this(name, Type.USER);
	}

	public RepositoryObjectRecipient(String name, Type type) {
		super();
		this.name = name;
		this.type = type;
	}

	// ~ Methods
	// =========================================================================================================

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
