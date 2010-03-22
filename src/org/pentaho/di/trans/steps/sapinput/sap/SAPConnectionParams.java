package org.pentaho.di.trans.steps.sapinput.sap;

public class SAPConnectionParams {

	private String name;
	private String host;
	private String sysnr;
	private String client;
	private String user;
	private String password;
	private String lang;

	public SAPConnectionParams(String name, String host, String sysnr,
			String client, String user, String password, String lang) {
		super();
		this.name = name;
		this.host = host;
		this.sysnr = sysnr;
		this.client = client;
		this.user = user;
		this.password = password;
		this.lang = lang;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getSysnr() {
		return sysnr;
	}

	public void setSysnr(String sysnr) {
		this.sysnr = sysnr;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
}
