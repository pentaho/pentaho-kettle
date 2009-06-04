package org.pentaho.di.repository.delegates;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;

public class RepositorySlaveServerDelegate extends BaseRepositoryDelegate {

	private static Class<?> PKG = SlaveServer.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	public RepositorySlaveServerDelegate(Repository repository) {
		super(repository);
	}
	
    public RowMetaAndData getSlaveServer(long id_slave) throws KettleException
    {
        return repository.connectionDelegate.getOneRow(quoteTable(Repository.TABLE_R_SLAVE), quote(Repository.FIELD_SLAVE_ID_SLAVE), id_slave);
    }

    public synchronized long getSlaveID(String name) throws KettleException
    {
        return repository.connectionDelegate.getIDWithValue(quoteTable(Repository.TABLE_R_SLAVE), quote(Repository.FIELD_SLAVE_ID_SLAVE), quote(Repository.FIELD_SLAVE_NAME), name);
    }

    public void saveSlaveServer(SlaveServer slaveServer) throws KettleException
    {
        saveSlaveServer(slaveServer, -1L, false);
    }
    
    public void saveSlaveServer(SlaveServer slaveServer, long id_transformation, boolean isUsedByTransformation) throws KettleException
    {
        slaveServer.setID(getSlaveID(slaveServer.getName()));
        
        if (slaveServer.getID()<0)
        {
        	slaveServer.setID(insertSlave(slaveServer));
        }
        else
        {
            updateSlave(slaveServer);
        }
        
        // Save the trans-slave relationship too.
        if (id_transformation>=0 && isUsedByTransformation) {
        	repository.insertTransformationSlave(id_transformation, slaveServer.getID());
        }
    }
    
    public SlaveServer loadSlaveServer(long id_slave_server) throws KettleException
    {
        SlaveServer slaveServer = new SlaveServer();
        
        slaveServer.setID(id_slave_server);
        
        RowMetaAndData row = getSlaveServer(id_slave_server);
        if (row==null)
        {
            throw new KettleDatabaseException(BaseMessages.getString(PKG, "SlaveServer.SlaveCouldNotBeFound", Long.toString(id_slave_server))); //$NON-NLS-1$
        }
        
        slaveServer.setName(          row.getString(Repository.FIELD_SLAVE_NAME, null) ); //$NON-NLS-1$
        slaveServer.setHostname(      row.getString(Repository.FIELD_SLAVE_HOST_NAME, null) ); //$NON-NLS-1$
        slaveServer.setPort(          row.getString(Repository.FIELD_SLAVE_PORT, null) ); //$NON-NLS-1$
        slaveServer.setUsername(      row.getString(Repository.FIELD_SLAVE_USERNAME, null) ); //$NON-NLS-1$
        slaveServer.setPassword(      row.getString(Repository.FIELD_SLAVE_PASSWORD, null) ); //$NON-NLS-1$
        slaveServer.setProxyHostname( row.getString(Repository.FIELD_SLAVE_PROXY_HOST_NAME, null) ); //$NON-NLS-1$
        slaveServer.setProxyPort(     row.getString(Repository.FIELD_SLAVE_PROXY_PORT, null) ); //$NON-NLS-1$
        slaveServer.setNonProxyHosts( row.getString(Repository.FIELD_SLAVE_NON_PROXY_HOSTS, null) ); //$NON-NLS-1$
        slaveServer.setMaster(        row.getBoolean(Repository.FIELD_SLAVE_MASTER, false) ); //$NON-NLS-1$
        
        return slaveServer;
    }

    
    public synchronized long insertSlave(SlaveServer slaveServer) throws KettleException
    {
        long id = repository.connectionDelegate.getNextSlaveServerID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id));
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_NAME, ValueMetaInterface.TYPE_STRING), slaveServer.getName());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_HOST_NAME, ValueMetaInterface.TYPE_STRING), slaveServer.getHostname());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_PORT, ValueMetaInterface.TYPE_STRING), slaveServer.getPort());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_USERNAME, ValueMetaInterface.TYPE_STRING), slaveServer.getUsername());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_PASSWORD, ValueMetaInterface.TYPE_STRING), slaveServer.getPassword());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_PROXY_HOST_NAME, ValueMetaInterface.TYPE_STRING), slaveServer.getProxyHostname());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_PROXY_PORT, ValueMetaInterface.TYPE_STRING), slaveServer.getProxyPort());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_NON_PROXY_HOSTS, ValueMetaInterface.TYPE_STRING), slaveServer.getNonProxyHosts());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_MASTER, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(slaveServer.isMaster()));

        repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), Repository.TABLE_R_SLAVE);
        repository.connectionDelegate.getDatabase().setValuesInsert(table);
        repository.connectionDelegate.getDatabase().insertRow();
        repository.connectionDelegate.getDatabase().closeInsert();

        return id;
    }
    
    public synchronized void updateSlave(SlaveServer slaveServer) throws KettleException
    {
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_NAME, ValueMetaInterface.TYPE_STRING), slaveServer.getName());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_HOST_NAME, ValueMetaInterface.TYPE_STRING), slaveServer.getHostname());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_PORT, ValueMetaInterface.TYPE_STRING), slaveServer.getPort());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_USERNAME, ValueMetaInterface.TYPE_STRING), slaveServer.getUsername());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_PASSWORD, ValueMetaInterface.TYPE_STRING), slaveServer.getPassword());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_PROXY_HOST_NAME, ValueMetaInterface.TYPE_STRING), slaveServer.getProxyHostname());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_PROXY_PORT, ValueMetaInterface.TYPE_STRING), slaveServer.getProxyPort());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_NON_PROXY_HOSTS, ValueMetaInterface.TYPE_STRING), slaveServer.getNonProxyHosts());
        table.addValue(new ValueMeta(Repository.FIELD_SLAVE_MASTER, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(slaveServer.isMaster()));

        repository.connectionDelegate.updateTableRow(Repository.TABLE_R_SLAVE, Repository.FIELD_SLAVE_ID_SLAVE, table, slaveServer.getID());
    }
    


}
