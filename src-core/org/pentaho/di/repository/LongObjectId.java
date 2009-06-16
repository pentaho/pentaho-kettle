package org.pentaho.di.repository;

public class LongObjectId implements ObjectId, Comparable<LongObjectId> {
	private long id;
	
	public LongObjectId(long id) {
		this.id = id;
	}
	
	public LongObjectId(ObjectId objectId) {
		if (objectId instanceof LongObjectId) {
			this.id = ((LongObjectId)objectId).longValue();
		} else {
			this.id = Long.valueOf(objectId.getId());
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LongObjectId)) return false;
		
		LongObjectId objectId = (LongObjectId) obj;
		
		return id == objectId.longValue();
	}
	
	@Override
	public int hashCode() {
		return Long.valueOf(id).hashCode();
	}
	
	public int compareTo(LongObjectId o) {
		return Long.valueOf(id).compareTo(Long.valueOf(o.longValue()));
	}
	
	@Override
	public String toString() {
		return Long.toString(id);
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return Long.toString(id);
	}
	
	/**
	 * @return the id in its original form.
	 */
	public Long longValue() {
		return Long.valueOf(id);
	}

}
