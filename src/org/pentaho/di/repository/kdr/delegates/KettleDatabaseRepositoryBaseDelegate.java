/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
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
