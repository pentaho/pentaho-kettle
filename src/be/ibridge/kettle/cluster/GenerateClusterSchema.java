package be.ibridge.kettle.cluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import be.ibridge.kettle.core.SharedObjects;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.database.PartitionDatabaseMeta;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.partition.PartitionSchema;

/**
 * The program generates a piece of XML that defines a (shared) Cluster Schema
 * 
 * @author Matt
 *
 */
public class GenerateClusterSchema
{
    public static final String PREFIX = "SLAVE_SERVER_";
    public static final String PORT = "_PORT";
    public static final String IP = "_IP";
    
    /**
     * @param args <br> 
     *    - the properties file to read
     *    - the shared file to write to
     *    - the name of the cluster schema
     *    - 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws KettleXMLException 
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, KettleXMLException
    {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(args[0])));

        SharedObjects sharedObjects = new SharedObjects(args[1]);
        
        DatabaseMeta mysql = new DatabaseMeta("MySQL EC2", "MySQL", "JDBC", null, "test", "3306", "matt", "abcd");
        
        ClusterSchema clusterSchema = new ClusterSchema();
        clusterSchema.setName(args[2]);
        clusterSchema.setBasePort("40000");
        clusterSchema.setSocketsBufferSize("100000");
        clusterSchema.setSocketsFlushInterval("0");
        clusterSchema.setSocketsCompressed(true);
        
        int max = 1;
        while (properties.getProperty(PREFIX+max+IP)!=null) max++;
        max--;
        
        mysql.setPartitioned(max>1);
        PartitionDatabaseMeta[] partDbMeta = new PartitionDatabaseMeta[max-1];
        
        for (int i=1;i<=max;i++)
        {
            String serverIp   = properties.getProperty(PREFIX+i+IP);
            String serverPort = properties.getProperty(PREFIX+i+PORT);
            
            if (i==1) // use the first as the master
            {
                // add the master
                SlaveServer master = new SlaveServer("EC_MASTER_"+i, serverIp, serverPort, "cluster", "cluster", null, null, null, true);
                sharedObjects.storeObject(master);
                clusterSchema.getSlaveServers().add(master);
                mysql.setHostname(serverIp);
                
                if (max==1) // if there is just one server here, so we add a slave too besides the master 
                {
                    SlaveServer slaveServer = new SlaveServer("EC_SLAVE_"+i, serverIp, serverPort, "cluster", "cluster");
                    sharedObjects.storeObject(slaveServer);
                    clusterSchema.getSlaveServers().add(slaveServer);
                }
            }
            else
            {
                // Add a slave server
                SlaveServer slaveServer = new SlaveServer("EC_SLAVE_"+i, serverIp, serverPort, "cluster", "cluster");
                sharedObjects.storeObject(slaveServer);
                clusterSchema.getSlaveServers().add(slaveServer);
                // Add a db partition
                partDbMeta[i-2] = new PartitionDatabaseMeta("P"+i, serverIp, "3306", "test");
            }
        }
       
        sharedObjects.storeObject(clusterSchema);
        
        mysql.setPartitioningInformation(partDbMeta);
        sharedObjects.storeObject(mysql);
        
        String[] partitionIds= new String[mysql.getPartitioningInformation().length];
        for (int i=0;i<partitionIds.length;i++) partitionIds[i] = mysql.getPartitioningInformation()[i].getPartitionId();
        PartitionSchema partitionSchema = new PartitionSchema("MySQL EC2 Schema", partitionIds);
        sharedObjects.storeObject(partitionSchema);
        
        sharedObjects.saveToFile();
    }
}
