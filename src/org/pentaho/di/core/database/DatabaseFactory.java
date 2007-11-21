package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;

/**
 * 
 * @author matt
 *
 */
public class DatabaseFactory {
	
	public DatabaseFactory() 
	{
	}
	
	public String getConnectionTestReport(DatabaseMeta databaseMeta) throws KettleDatabaseException
	{
		if (databaseMeta.getAccessType()!=DatabaseMeta.TYPE_ACCESS_PLUGIN) {

			StringBuffer report = new StringBuffer();
       
            Database db = new Database(databaseMeta);
            if (databaseMeta.isPartitioned())
            {
                PartitionDatabaseMeta[] partitioningInformation = databaseMeta.getPartitioningInformation();
                for (int i = 0; i < partitioningInformation.length; i++)
                {
                    try
                    {
                        db.connect(partitioningInformation[i].getPartitionId());
                        report.append(Messages.getString("DatabaseMeta.report.ConnectionWithPartOk", databaseMeta.getName(), partitioningInformation[i].getPartitionId()) + Const.CR); //$NON-NLS-1$
                    }
                    catch (KettleException e)
                    {
                        report.append(Messages.getString("DatabaseMeta.report.ConnectionWithPartError", databaseMeta.getName(), partitioningInformation[i].getPartitionId(), e.toString()) + Const.CR); //$NON-NLS-1$
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
                    report.append(Messages.getString("DatabaseMeta.report.ConnectionOk", databaseMeta.getName()) + Const.CR); //$NON-NLS-1$
                }
                catch (KettleException e)
                {
                    report.append(Messages.getString("DatabaseMeta.report.ConnectionError", databaseMeta.getName()) + e.toString() + Const.CR); //$NON-NLS-1$
                    report.append(Const.getStackTracker(e) + Const.CR);
                }
                finally
                {
                    db.disconnect();
                }
                appendConnectionInfo(report, db.environmentSubstitute(databaseMeta.getHostname()), 
                		                     db.environmentSubstitute(databaseMeta.getDatabasePortNumberString()), 
                		                     db.environmentSubstitute(databaseMeta.getDatabaseName()));
                report.append(Const.CR);
            }
            return report.toString();
		}
		else 
		{
			return Messages.getString("BaseDatabaseMeta.TestConnectionReportNotImplemented.Message"); // $NON-NLS-1
		}

	}
	
	private StringBuffer appendConnectionInfo(StringBuffer report, String hostName, String portNumber, String dbName) {
        report.append(Messages.getString("DatabaseMeta.report.Hostname")).append(hostName).append(Const.CR); //$NON-NLS-1$
        report.append(Messages.getString("DatabaseMeta.report.Port")).append(portNumber).append(Const.CR); //$NON-NLS-1$
        report.append(Messages.getString("DatabaseMeta.report.DatabaseName")).append(dbName).append(Const.CR); //$NON-NLS-1$
        return report;
    }
}
