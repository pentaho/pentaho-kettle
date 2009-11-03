package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleException;

public interface TransListener {
	public void transFinished(Trans trans) throws KettleException;
}
