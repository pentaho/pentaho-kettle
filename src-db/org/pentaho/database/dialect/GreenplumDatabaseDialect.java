package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseType;


public class GreenplumDatabaseDialect extends PostgreSQLDatabaseDialect {
	
	public static IDatabaseType DBTYPE = 
	    new DatabaseType(
	        "Greenplum",
	        "GREENPLUM",
	        DatabaseAccessType.getList(
	            DatabaseAccessType.NATIVE, 
	            DatabaseAccessType.ODBC, 
	            DatabaseAccessType.JNDI
	        ), 
	        5432, 
	        "http://jdbc.postgresql.org/documentation/83/connect.html#connection-parameters"
	    );
	  

	public IDatabaseType getDatabaseType() {
		return DBTYPE;
	}
	
	@Override
	public String[] getReservedWords() {
		int extraWords = 1;
		
		String[] pgWords = super.getReservedWords();
		String[] gpWords = new String[pgWords.length+extraWords];
		for (int i=0;i<pgWords.length;i++) gpWords[i]=pgWords[i];
		
		int index = pgWords.length;
		
		// Just add the ERRORS keyword for now
		//
		gpWords[index++] = "ERRORS";
		
		return gpWords;
	}
}
