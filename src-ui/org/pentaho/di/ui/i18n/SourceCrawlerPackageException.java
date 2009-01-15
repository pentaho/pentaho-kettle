package org.pentaho.di.ui.i18n;

public class SourceCrawlerPackageException {
	private String startsWith;
	private String packageName;

	/**
	 * @param startsWiths
	 * @param packageName
	 */
	public SourceCrawlerPackageException(String startsWith, String packageName) {
		this.startsWith = startsWith;
		this.packageName = packageName;
	}

	/**
	 * @return the startsWith
	 */
	public String getStartsWith() {
		return startsWith;
	}

	/**
	 * @param startsWith
	 *            the startsWith to set
	 */
	public void setStartsWith(String startsWith) {
		this.startsWith = startsWith;
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @param packageName
	 *            the packageName to set
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
