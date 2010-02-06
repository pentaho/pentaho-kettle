package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.i18n.BaseMessages;

/**
 * 
 * @author matt
 *
 */
public class DatabaseFactory implements DatabaseFactoryInterface {

	private static Class<?> PKG = Database.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject("Database factory", LoggingObjectType.GENERAL, null);
    
	public DatabaseFactory() 
	{
	}
	
	public String getConnectionTestReport(DatabaseMeta databaseMeta) throws KettleDatabaseException
	{
		if (databaseMeta.getAccessType()!=DatabaseMeta.TYPE_ACCESS_PLUGIN) {

			StringBuffer report = new StringBuffer();
			
			
       
            Database db = new Database(loggingObject, databaseMeta);
            if (databaseMeta.isPartitioned())
            {
                PartitionDatabaseMeta[] partitioningInformation = databaseMeta.getPartitioningInformation();
                for (int i = 0; i < partitioningInformation.length; i++)
                {
                    try
                    {
                        db.connect(partitioningInformation[i].getPartitionId());
                        report.append(BaseMessages.getString(PKG, "DatabaseMeta.report.ConnectionWithPartOk", databaseMeta.getName(), partitioningInformation[i].getPartitionId()) + Const.CR); //$NON-NLS-1$
                    }
                    catch (KettleException e)
                    {
                        report.append(BaseMessages.getString(PKG, "DatabaseMeta.report.ConnectionWithPartError", databaseMeta.getName(), partitioningInformation[i].getPartitionId(), e.toString()) + Const.CR); //$NON-NLS-1$
                        report.append(Const.getStackTracker(e) + Const.CR);
                    }
                    finally
                    {
                        db.disconnect();
                    }
                    appendConnectionInfo(report, db.environmentSubstitute(partitioningInformation[i].getHostname()), 
                    		                     db.environmentSubstitute(partitioningInformation[i].getPort()), 
                    		                     db.environmentSubstitute(partitioningInformation[i].getDatabaseName()));
                    report.append(Const.CR);
                }
            }
            else
            {
                try
                {
                    db.connect();
                    report.append(BaseMessages.getString(PKG, "DatabaseMeta.report.ConnectionOk", databaseMeta.getName()) + Const.CR); //$NON-NLS-1$
                }
                catch (KettleException e)
                {
                    report.append(BaseMessages.getString(PKG, "DatabaseMeta.report.ConnectionError", databaseMeta.getName()) + e.toString() + Const.CR); //$NON-NLS-1$
                    report.append(Const.getStackTracker(e) + Const.CR);
                }
                finally
                {
                    db.disconnect();
                }
                if (databaseMeta.getAccessType() == DatabaseMeta.TYPE_ACCESS_JNDI) {
                  appendJndiConnectionInfo(report, db.environmentSubstitute(databaseMeta.getDatabaseName()));
                } else {
                  appendConnectionInfo(report, db.environmentSubstitute(databaseMeta.getHostname()), 
                  		                     db.environmentSubstitute(databaseMeta.getDatabasePortNumberString()), 
                  		                     db.environmentSubstitute(databaseMeta.getDatabaseName()));
                }
                report.append(Const.CR);
            }
            return report.toString();
		}
		else 
		{
			return BaseMessages.getString(PKG, "BaseDatabaseMeta.TestConnectionReportNotImplemented.Message"); // $NON-NLS-1
		}

	}
	
	private StringBuffer appendJndiConnectionInfo(StringBuffer report, String jndiName) {
    report.append(BaseMessages.getString(PKG, "DatabaseMeta.report.JndiName")).append(jndiName).append(Const.CR); //$NON-NLS-1$
    return report;
	}
	
	private StringBuffer appendConnectionInfo(StringBuffer report, String hostName, String portNumber, String dbName) {
        report.append(BaseMessages.getString(PKG, "DatabaseMeta.report.Hostname")).append(hostName).append(Const.CR); //$NON-NLS-1$
        report.append(BaseMessages.getString(PKG, "DatabaseMeta.report.Port")).append(portNumber).append(Const.CR); //$NON-NLS-1$
        report.append(BaseMessages.getString(PKG, "DatabaseMeta.report.DatabaseName")).append(dbName).append(Const.CR); //$NON-NLS-1$
        return report;
    }
}
