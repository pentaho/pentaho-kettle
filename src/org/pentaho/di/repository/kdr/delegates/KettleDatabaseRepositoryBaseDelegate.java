package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositoryBaseDelegate {

	protected KettleDatabaseRepository repository;
	protected LogChannelInterface log;
	
	public KettleDatabaseRepositoryBaseDelegate(KettleDatabaseRepository repository) {
		this.repository = repository;
		this.log = repository.getLog();
	}

    public String quote(String identifier) {
		return repository.connectionDelegate.getDatabaseMeta().quoteField(identifier);
	}

    public String quoteTable(String table) {
    	return repository.connectionDelegate.getDatabaseMeta().getQuotedSchemaTableCombination(null, table);
    }
}
