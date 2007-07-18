package org.pentaho.di.resource;

import java.util.ArrayList;
import java.util.List;

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
	
	
}
