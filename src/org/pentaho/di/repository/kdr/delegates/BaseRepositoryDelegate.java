package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class BaseRepositoryDelegate {

	protected KettleDatabaseRepository repository;
	protected LogWriter log;
	
	public BaseRepositoryDelegate(KettleDatabaseRepository repository) {
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
