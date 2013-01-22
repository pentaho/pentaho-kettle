package org.pentaho.di.core;

public interface ExecutorInterface {
		
	public String getExecutingServer();

	public void setExecutingServer(String executingServer);
	
	public String getExecutingUser();
	
	public void setExecutingUser(String executingUser);
	
}
