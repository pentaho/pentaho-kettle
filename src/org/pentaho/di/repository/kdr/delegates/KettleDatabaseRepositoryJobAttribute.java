package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryAttributeInterface;

public class KettleDatabaseRepositoryJobAttribute implements RepositoryAttributeInterface {

	private KettleDatabaseRepositoryConnectionDelegate	connectionDelegate;
	private ObjectId	jobObjectId;

	public KettleDatabaseRepositoryJobAttribute(KettleDatabaseRepositoryConnectionDelegate connectionDelegate, ObjectId jobObjectId) {
		this.connectionDelegate = connectionDelegate;
		this.jobObjectId = jobObjectId;
	}
	
	public boolean getAttributeBoolean(String code) throws KettleException {
		return connectionDelegate.getJobAttributeBoolean(jobObjectId, 0, code);
	}

	public long getAttributeInteger(String code) throws KettleException {
		return connectionDelegate.getJobAttributeInteger(jobObjectId, 0, code);
	}

	public String getAttributeString(String code) throws KettleException {
		return connectionDelegate.getJobAttributeString(jobObjectId, 0, code);
	}

	public void setAttribute(String code, String value) throws KettleException {
		connectionDelegate.insertJobAttribute(jobObjectId, 0, code, 0, value);
	}

	public void setAttribute(String code, boolean value) throws KettleException {
		connectionDelegate.insertJobAttribute(jobObjectId, 0, code, 0, value?"Y":"N");
	}

	public void setAttribute(String code, long value) throws KettleException {
		connectionDelegate.insertJobAttribute(jobObjectId, 0, code, value, null);
	}
}
