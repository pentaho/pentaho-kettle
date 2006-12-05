package be.ibridge.kettle.cluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import be.ibridge.kettle.core.SharedObjects;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.database.PartitionDatabaseMeta;
import be.ibridge.kettle.core.exception.KettleXMLException;

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

        SharedObjects sharedObjects = new SharedObjects(args[1], new ArrayList(), new Hashtable());
        
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
        
        mysql.setPartitioned(true);
        PartitionDatabaseMeta[] partDbMeta = new PartitionDatabaseMeta[max];
        
        for (int i=1;i<=max;i++)
        {
            String serverIp   = properties.getProperty(PREFIX+i+IP);
            String serverPort = properties.getProperty(PREFIX+i+PORT);
            
            if (i==1) // use the first as the master
            {
                // add the master
                clusterSchema.getSlaveServers().add(new SlaveServer(serverIp, serverPort, "cluster", "cluster", null, null, null, true));
                mysql.setHostname(serverIp);
                partDbMeta[i-1] = new PartitionDatabaseMeta("P"+i, serverIp, "3306", "test");
                
                if (max==1) // if there is just one server here, so we add a slave too besides the master 
                {
                    clusterSchema.getSlaveServers().add(new SlaveServer(serverIp, serverPort, "cluster", "cluster"));
                }
            }
            else
            {
                // Add a slave server
                clusterSchema.getSlaveServers().add(new SlaveServer(serverIp, serverPort, "cluster", "cluster"));
            }
        }
       
        sharedObjects.storeObject(clusterSchema);
        
        mysql.setPartitioningInformation(partDbMeta);
        sharedObjects.storeObject(mysql);
        
        sharedObjects.saveToFile();
    }
}
