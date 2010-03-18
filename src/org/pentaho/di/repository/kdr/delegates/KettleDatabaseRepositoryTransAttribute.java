package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryAttributeInterface;

public class KettleDatabaseRepositoryTransAttribute implements RepositoryAttributeInterface {

	private KettleDatabaseRepositoryConnectionDelegate	connectionDelegate;
	private ObjectId	transObjectId;

	public KettleDatabaseRepositoryTransAttribute(KettleDatabaseRepositoryConnectionDelegate connectionDelegate, ObjectId transObjectId) {
		this.connectionDelegate = connectionDelegate;
		this.transObjectId = transObjectId;
	}
	
	public boolean getAttributeBoolean(String code) throws KettleException {
		return connectionDelegate.getTransAttributeBoolean(transObjectId, 0, code);
	}

	public long getAttributeInteger(String code) throws KettleException {
		return connectionDelegate.getTransAttributeInteger(transObjectId, 0, code);
	}

	public String getAttributeString(String code) throws KettleException {
		return connectionDelegate.getTransAttributeString(transObjectId, 0, code);
	}

	public void setAttribute(String code, String value) throws KettleException {
		connectionDelegate.insertTransAttribute(transObjectId, 0, code, 0, value);
	}

	public void setAttribute(String code, boolean value) throws KettleException {
		connectionDelegate.insertTransAttribute(transObjectId, 0, code, 0, value?"Y":"N");
	}

	public void setAttribute(String code, long value) throws KettleException {
		connectionDelegate.insertTransAttribute(transObjectId, 0, code, value, null);
	}
}
