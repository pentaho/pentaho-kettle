package org.pentaho.di.trans.steps.sapinput.sap;

public class SAPFunction {

	private String name;
	private String description;
	private String group;
	private String application;
	private String host;

	public SAPFunction(String name) {
		super();
		this.name = name;
	}

	public SAPFunction(String name, String description, String group, String application, String host) {
		super();
		this.name = name;
		this.description = description;
		this.group = group;
		this.application = application;
		this.host = host;
	}

	@Override
	public String toString() {
		return "SAPFunction [name=" + name + ", decription=" + description
				+ "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

}
