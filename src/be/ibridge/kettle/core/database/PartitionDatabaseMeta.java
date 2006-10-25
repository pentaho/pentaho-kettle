package be.ibridge.kettle.core.database;

/**
 * Class to contain the information needed to parition (cluster): id, hostname, port, database
 * 
 * @author Matt
 *
 */
public class PartitionDatabaseMeta
{
    String partitionId;
    
    String hostname;
    String port;
    String databaseName;
    
    public PartitionDatabaseMeta()
    {
    }
    
    /**
     * @param partitionId
     * @param hostname
     * @param port
     * @param database
     */
    public PartitionDatabaseMeta(String partitionId, String hostname, String port, String database)
    {
        super();
        
        this.partitionId = partitionId;
        this.hostname = hostname;
        this.port = port;
        this.databaseName = database;
    }

    /**
     * @return the partitionId
     */
    public String getPartitionId()
    {
        return partitionId;
    }

    /**
     * @param partitionId the partitionId to set
     */
    public void setPartitionId(String partitionId)
    {
        this.partitionId = partitionId;
    }

    /**
     * @return the database
     */
    public String getDatabaseName()
    {
        return databaseName;
    }
    
    /**
     * @param database the database to set
     */
    public void setDatabaseName(String database)
    {
        this.databaseName = database;
    }
    
    /**
     * @return the hostname
     */
    public String getHostname()
    {
        return hostname;
    }
    
    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }
    
    /**
     * @return the port
     */
    public String getPort()
    {
        return port;
    }
    
    /**
     * @param port the port to set
     */
    public void setPort(String port)
    {
        this.port = port;
    }
    
    
}
