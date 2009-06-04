package org.pentaho.di.repository.delegates;

import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.repository.Repository;

public class BaseRepositoryDelegate {

	protected Repository repository;
	protected LogWriter log;
	
	public BaseRepositoryDelegate(Repository repository) {
		this.repository = repository;
		this.log = repository.log;
	}

    public String quote(String identifier) {
		return repository.connectionDelegate.getDatabaseMeta().quoteField(identifier);
	}

    public String quoteTable(String table) {
    	return repository.connectionDelegate.getDatabaseMeta().getQuotedSchemaTableCombination(null, table);
    }

}
