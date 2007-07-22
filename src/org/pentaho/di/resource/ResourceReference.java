package org.pentaho.di.resource;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.util.StringUtil;

public class ResourceReference {
	private ResourceHolderInterface resourceReferenceHolder;
	private List<ResourceEntry> entries;
  
	/**
	 * @param referenceHolder
	 * @param entries
	 */
	public ResourceReference(ResourceHolderInterface resourceReferenceHolder, List<ResourceEntry> entries) {
		super();
		this.resourceReferenceHolder = resourceReferenceHolder;
		this.entries = entries;
	}

	public ResourceReference(ResourceHolderInterface resourceReferenceHolder) {
		this.resourceReferenceHolder = resourceReferenceHolder;
		this.entries = new ArrayList<ResourceEntry>();
	}

	/**
	 * @return the referenceHolder
	 */
	public ResourceHolderInterface getReferenceHolder() {
		return resourceReferenceHolder;
	}

	/**
	 * @param referenceHolder
	 *            the referenceHolder to set
	 */
	public void setReferenceHolder(ResourceHolderInterface resourceReferenceHolder) {
		this.resourceReferenceHolder = resourceReferenceHolder;
	}

	/**
	 * @return the entries
	 */
	public List<ResourceEntry> getEntries() {
		return entries;
	}

	/**
	 * @param entries
	 *            the entries to set
	 */
	public void setEntries(List<ResourceEntry> entries) {
		this.entries = entries;
	}
	
  public String toXml() {
    return toXml(null, 0);
  }
  
  public String toXml(ResourceXmlPropertyEmitterInterface injector) {
    return toXml(injector, 0);
  }
  
  public String toXml(int indentLevel) {
    return toXml(null, indentLevel);
  }
  
  public String toXml(ResourceXmlPropertyEmitterInterface injector, int indentLevel) {
    StringBuffer buff = new StringBuffer();
    addXmlElementWithAttribute(buff, indentLevel, "ActionComponent", "type", resourceReferenceHolder.getHolderType()); //$NON-NLS-1$ //$NON-NLS-2$
    indentLevel++;
    addXmlElement(buff, indentLevel, "ComponentName", resourceReferenceHolder.getName()); //$NON-NLS-1$
    addXmlElement(buff, indentLevel, "ComponentId", resourceReferenceHolder.getTypeId()); //$NON-NLS-1$
    addXmlElement(buff, indentLevel, "ComponentResources"); //$NON-NLS-1$
    indentLevel++;
    for (ResourceEntry entry : this.getEntries()) {
      buff.append(entry.toXml(indentLevel));
    }
    indentLevel--;
    addXmlCloseElement(buff, indentLevel, "ComponentResources"); //$NON-NLS-1$
    if (injector != null) {
      addXmlElement(buff, indentLevel, "ComponentProperties"); //$NON-NLS-1$
      indentLevel++;
      buff.append(injector.getExtraResourceProperties(resourceReferenceHolder, indentLevel));
      indentLevel--;
      addXmlCloseElement(buff, indentLevel, "ComponentProperties"); //$NON-NLS-1$
    }
    indentLevel--;
    addXmlCloseElement(buff, indentLevel, "ActionComponent"); //$NON-NLS-1$
    return buff.toString();
  }

  public void addXmlElementWithAttribute(StringBuffer buff, int indentLevel, String elementName, String attrName, String attrValue) {
    buff.append(StringUtil.getIndent(indentLevel)).append("<").append(elementName).append(" ").append(attrName).append("='");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    buff.append(attrValue).append("'>").append(StringUtil.CRLF); //$NON-NLS-1$
  }
  
  public void addXmlCloseElement(StringBuffer buff, int indentLevel, String elementName) {
    buff.append(StringUtil.getIndent(indentLevel)).append("</").append(elementName).append(">").append(StringUtil.CRLF);//$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public void addXmlElement(StringBuffer buff, int indentLevel, String elementName) {
    buff.append(StringUtil.getIndent(indentLevel)).append("<").append(elementName).append(">").append(StringUtil.CRLF);//$NON-NLS-1$ //$NON-NLS-2$ 
  }
  
  public void addXmlElement(StringBuffer buff, int indentLevel, String elementName, String elementValue) {
    buff.append(StringUtil.getIndent(indentLevel)).append("<").append(elementName).append("><![CDATA[").append(elementValue).append("]]></").append(elementName).append(">").append(StringUtil.CRLF); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

}
