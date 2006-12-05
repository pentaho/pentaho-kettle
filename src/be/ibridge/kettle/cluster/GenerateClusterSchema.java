package be.ibridge.kettle.cluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

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
     *    - the name of the cluster schema
     *    - 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(args[0])));

        ClusterSchema clusterSchema = new ClusterSchema();
        clusterSchema.setName("EC2");
        
        int max = 1;
        while (properties.getProperty(PREFIX+max+IP)!=null) max++;
        max--;
        
        for (int i=1;i<=max;i++)
        {
            String serverIp   = properties.getProperty(PREFIX+i+IP);
            String serverPort = properties.getProperty(PREFIX+i+PORT);
            
            if (i==1) // use the first as the master
            {
                // add the master
                clusterSchema.getSlaveServers().add(new SlaveServer(serverIp, serverPort, "cluster", "cluster", null, null, null, true));
                
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
        System.out.println(clusterSchema.getXML());
    }
}
