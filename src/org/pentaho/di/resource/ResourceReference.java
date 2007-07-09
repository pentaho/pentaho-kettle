package org.pentaho.di.resource;

import java.util.ArrayList;
import java.util.List;

public class ResourceReference {
	private Object referenceHolder;
	private List<ResourceEntry> entries;
	
	/**
	 * @param referenceHolder
	 * @param entries
	 */
	public ResourceReference(Object referenceHolder, List<ResourceEntry> entries) {
		super();
		this.referenceHolder = referenceHolder;
		this.entries = entries;
	}

	public ResourceReference(Object referenceHolder) {
		this.referenceHolder = referenceHolder;
		this.entries = new ArrayList<ResourceEntry>();
	}

	/**
	 * @return the referenceHolder
	 */
	public Object getReferenceHolder() {
		return referenceHolder;
	}

	/**
	 * @param referenceHolder
	 *            the referenceHolder to set
	 */
	public void setReferenceHolder(Object referenceHolder) {
		this.referenceHolder = referenceHolder;
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
