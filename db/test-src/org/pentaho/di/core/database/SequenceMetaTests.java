package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SequenceMetaTests {

	@Test
	public void testSupport() {
	
		DatabaseInterface databaseInterface;
		
        // According to our Meta, Oracle, PostGres, 
		// Greenplum and Vertica support sequences
        databaseInterface = new OracleDatabaseMeta();
        assertTrue(databaseInterface.supportsSequences());

        databaseInterface = new OracleRDBDatabaseMeta();
        assertTrue(databaseInterface.supportsSequences());
        
        databaseInterface = new VerticaDatabaseMeta();
        assertTrue(databaseInterface.supportsSequences());
        
        databaseInterface = new PostgreSQLDatabaseMeta();
        assertTrue(databaseInterface.supportsSequences());
        
        databaseInterface = new GreenplumDatabaseMeta();
        assertTrue(databaseInterface.supportsSequences());
        
        databaseInterface = new AS400DatabaseMeta();
        assertTrue(databaseInterface.supportsSequences());
        
        databaseInterface = new DB2DatabaseMeta();
        assertTrue(databaseInterface.supportsSequences());
        
        databaseInterface = new HypersonicDatabaseMeta();
        assertTrue(databaseInterface.supportsSequences());
        
        databaseInterface = new KingbaseESDatabaseMeta();
        assertTrue(databaseInterface.supportsSequences());
        
        databaseInterface = new NetezzaDatabaseMeta();
        assertTrue(databaseInterface.supportsSequences());
 
        //  the rest of the database metas say they don't support sequences
        
        databaseInterface = new MySQLDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());

        databaseInterface = new InfiniDbDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new InfobrightDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
                                
        databaseInterface = new DbaseDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new DerbyDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new ExtenDBDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new FirebirdDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new GenericDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new GuptaDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new H2DatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new InformixDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new IngresDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());

        databaseInterface = new InterbaseDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());     
                
        databaseInterface = new LucidDBDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new MonetDBDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new MSAccessDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());

        databaseInterface = new MSSQLServerDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new MSSQLServerNativeDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
                 
        databaseInterface = new NeoviewDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
                        
        databaseInterface = new RemedyActionRequestSystemDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new SAPDBDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new SAPR3DatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new SQLiteDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new SybaseDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new SybaseIQDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
        
        databaseInterface = new TeradataDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());

        databaseInterface = new UniVerseDatabaseMeta();
        assertFalse(databaseInterface.supportsSequences());
	}
	
	@Test 
	public void testSQL() {
		
		DatabaseInterface databaseInterface;
		final String sequenceName = "sequence_name";
		
        databaseInterface = new OracleDatabaseMeta();
        assertEquals("SELECT sequence_name.nextval FROM dual", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("SELECT sequence_name.currval FROM DUAL", databaseInterface.getSQLCurrentSequenceValue(sequenceName));

        databaseInterface = new OracleRDBDatabaseMeta();
        assertEquals("SELECT sequence_name.nextval FROM dual", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("SELECT sequence_name.currval FROM DUAL", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new VerticaDatabaseMeta();
        assertEquals("SELECT nextval('sequence_name')", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("SELECT currval('sequence_name')", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new PostgreSQLDatabaseMeta();
        assertEquals("SELECT nextval('sequence_name')", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("SELECT last_value FROM sequence_name", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new GreenplumDatabaseMeta();
        assertEquals("SELECT nextval('sequence_name')", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("SELECT last_value FROM sequence_name", databaseInterface.getSQLCurrentSequenceValue(sequenceName));

        databaseInterface = new AS400DatabaseMeta();
        assertEquals("SELECT NEXT VALUE FOR sequence_name FROM SYSIBM.SYSDUMMY1", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("SELECT PREVIOUS VALUE FOR sequence_name FROM SYSIBM.SYSDUMMY1", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new DB2DatabaseMeta();
        assertEquals("SELECT NEXT VALUE FOR sequence_name FROM SYSIBM.SYSDUMMY1", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("SELECT PREVIOUS VALUE FOR sequence_name FROM SYSIBM.SYSDUMMY1", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
         
        databaseInterface = new HypersonicDatabaseMeta();
        assertEquals("SELECT NEXT VALUE FOR sequence_name FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME = 'SEQUENCE_NAME'", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("SELECT sequence_name.currval FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME = 'SEQUENCE_NAME'", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new KingbaseESDatabaseMeta();
        assertEquals("SELECT nextval('sequence_name')", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("SELECT currval('sequence_name')", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        
        databaseInterface = new NetezzaDatabaseMeta();
        assertEquals("select nextval('sequence_name')", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("select last_value from sequence_name", databaseInterface.getSQLCurrentSequenceValue(sequenceName));

        //  the rest of the database metas say they don't support sequences
        
        databaseInterface = new MySQLDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));

        databaseInterface = new InfiniDbDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new InfobrightDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
                                
        databaseInterface = new DbaseDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new DerbyDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new ExtenDBDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new FirebirdDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new GenericDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new GuptaDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new H2DatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new InformixDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));
        
        databaseInterface = new IngresDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));

        databaseInterface = new InterbaseDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName));     
                
        databaseInterface = new LucidDBDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 
        
        databaseInterface = new MonetDBDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 
        
        databaseInterface = new MSAccessDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 

        databaseInterface = new MSSQLServerDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 
        
        databaseInterface = new MSSQLServerNativeDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 
                 
        databaseInterface = new NeoviewDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 
                        
        databaseInterface = new RemedyActionRequestSystemDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 
        
        databaseInterface = new SAPDBDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 
        
        databaseInterface = new SAPR3DatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 
        
        databaseInterface = new SQLiteDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 
        
        databaseInterface = new SybaseDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 
        
        databaseInterface = new SybaseIQDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 
        
        databaseInterface = new TeradataDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 

        databaseInterface = new UniVerseDatabaseMeta();
        assertEquals("", databaseInterface.getSQLNextSequenceValue(sequenceName));
        assertEquals("", databaseInterface.getSQLCurrentSequenceValue(sequenceName)); 
	}
}
