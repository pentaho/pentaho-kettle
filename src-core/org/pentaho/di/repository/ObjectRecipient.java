package org.pentaho.di.repository;


public interface ObjectRecipient {

	public static enum Type {
		USER, ROLE;
	}
	public String getName();
	public void setName(String name);
	public Type getType();
	public void setType(Type type);
}
