package org.pentaho.di.ui.i18n;

public class SourceCrawlerXMLElement {
	private String element;
	private String label;
	
	/**
	 * @param element
	 * @param label
	 */
	public SourceCrawlerXMLElement(String element, String label) {
		this.element = element;
		this.label = label;
	}
	/**
	 * @return the element
	 */
	public String getElement() {
		return element;
	}
	/**
	 * @param element the element to set
	 */
	public void setElement(String element) {
		this.element = element;
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
}
