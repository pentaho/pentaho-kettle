package org.pentaho.di.trans.steps.mapping;

public class MappingValueRename implements Cloneable {
	private String sourceValueName;
	private String targetValueName;

	/**
	 * @param sourceValueName
	 * @param targetValueName
	 */
	public MappingValueRename(String sourceValueName, String targetValueName) {
		super();
		this.sourceValueName = sourceValueName;
		this.targetValueName = targetValueName;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	
	@Override
	public String toString() {
		return sourceValueName+"-->"+targetValueName;
	}
	
	@Override
	public boolean equals(Object obj) {
		return sourceValueName.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return sourceValueName.hashCode();
	}
	
	/**
	 * @return the sourceValueName
	 */
	public String getSourceValueName() {
		return sourceValueName;
	}
	/**
	 * @param sourceValueName the sourceValueName to set
	 */
	public void setSourceValueName(String sourceValueName) {
		this.sourceValueName = sourceValueName;
	}
	/**
	 * @return the targetValueName
	 */
	public String getTargetValueName() {
		return targetValueName;
	}
	/**
	 * @param targetValueName the targetValueName to set
	 */
	public void setTargetValueName(String targetValueName) {
		this.targetValueName = targetValueName;
	}
}
