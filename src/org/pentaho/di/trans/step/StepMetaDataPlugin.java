package org.pentaho.di.trans.step;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;


public interface StepMetaDataPlugin {
	
	public void saveToRepository(Repository repository, ObjectId transformationId, ObjectId stepId) throws KettleException;
	public void loadFromRepository(Repository repository, ObjectId transformationId, ObjectId stepId) throws KettleException;
	
}
