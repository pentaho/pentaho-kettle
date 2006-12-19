package be.ibridge.kettle.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import be.ibridge.kettle.cluster.ClusterSchema;
import be.ibridge.kettle.cluster.SlaveServer;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.partition.PartitionSchema;
import be.ibridge.kettle.trans.step.StepMeta;

/**
 * Based on a piece of XML, this factory will give back a list of objects.
 * In other words, it does XML de-serialisation
 * 
 * @author Matt
 *
 */
public class SharedObjects
{
    private class SharedEntry
    {
        public String className;
        public String objectName;
        
        /**
         * @param className
         * @param objectName
         */
        public SharedEntry(String className, String objectName)
        {
            this.className = className;
            this.objectName = objectName;
        }
        
        public boolean equals(Object obj)
        {
            SharedEntry sharedEntry = (SharedEntry) obj;
            return className.equals(sharedEntry.className) && objectName.equals(objectName);
        }
        
        public int hashCode()
        {
            return className.hashCode() ^ objectName.hashCode();
        }
        
    }
    private static final String XML_TAG = "sharedobjects";
    
    private String filename;

    private Map objectsMap;

    private ArrayList databases;
    private Hashtable counters;
    
    public SharedObjects(String sharedObjectsFile, ArrayList databases, Hashtable counters, List slaveServers) throws KettleXMLException
    {
        this.filename = createFilename(sharedObjectsFile);
        this.databases = databases;
        this.counters = counters;
        this.objectsMap = new Hashtable();

        // Extra information
        File file = new File(filename);
        
        // If we have a shared file, load the content, otherwise, just keep this one empty
        if (file.exists()) 
        {
            LogWriter.getInstance().logBasic("SharedObjects", "Reading the shared objects file ["+file+"]");
            Document document = XMLHandler.loadXMLFile(file);
            Node sharedObjectsNode = XMLHandler.getSubNode(document, XML_TAG);
            if (sharedObjectsNode!=null)
            {
                NodeList childNodes = sharedObjectsNode.getChildNodes();
                for (int i=0;i<childNodes.getLength();i++)
                {
                    Node node = childNodes.item(i);
                    String nodeName = node.getNodeName();
                    
                    SharedObjectInterface isShared = null;
    
                    if (nodeName.equals(DatabaseMeta.XML_TAG))
                    {    
                        isShared = new DatabaseMeta(node);
                    }
                    else if (nodeName.equals(StepMeta.XML_TAG))        
                    { 
                        StepMeta stepMeta = new StepMeta(node, databases, counters);
                        stepMeta.setDraw(false); // don't draw it, keep it in the tree.
                        isShared = stepMeta;
                    }
                    else if (nodeName.equals(PartitionSchema.XML_TAG)) 
                    {
                        isShared = new PartitionSchema(node);
                    }
                    else if (nodeName.equals(SlaveServer.XML_TAG)) 
                    {
                        isShared = new SlaveServer(node);
                    }
                    else if (nodeName.equals(ClusterSchema.XML_TAG)) 
                    {   
                        isShared = new ClusterSchema(node, slaveServers);
                    }
                    
                    if (isShared!=null)
                    {
                        isShared.setShared(true);
                        storeObject(isShared);
                    }
                }
            }
        }
    }
    
    public static final String createFilename(String sharedObjectsFile)
    {
        String filename;
        if (Const.isEmpty(sharedObjectsFile))
        {
            // First fallback is the environment/kettle variable ${KETTLE_SHARED_OBJECTS}
            // This points to the file
            filename = KettleVariables.getInstance().getVariable("KETTLE_SHARED_OBJECTS");
            
            // Last line of defence...
            if (Const.isEmpty(filename))
            {
                filename = Const.getSharedObjectsFile();
            }
        }
        else
        {
            filename = sharedObjectsFile;
        }
        return filename;
    }

    public SharedObjects(ArrayList databases, Hashtable counters, List slaveServers) throws KettleXMLException
    {
        this(null, databases, counters, slaveServers);
    }

    public Map getObjectsMap()
    {
        return objectsMap;
    }

    public void setObjectsMap(Map objects)
    {
        this.objectsMap = objects;
    }

    /**
     * @return the counters
     */
    public Hashtable getCounters()
    {
        return counters;
    }

    /**
     * @return the databases
     */
    public ArrayList getDatabases()
    {
        return databases;
    }

    /**
     * Store the sharedObject in the object map.
     * It is possible to have 2 different types of shared object with the same name.
     * They will be stored separately.
     * 
     * @param sharedObject
     */
    public void storeObject(SharedObjectInterface sharedObject)
    {
        SharedEntry key = new SharedEntry(sharedObject.getClass().getName(), sharedObject.getName());
        objectsMap.put(key, sharedObject);
    }

    public void saveToFile() throws IOException
    {
        File file = new File(filename);
        
        FileOutputStream outputStream = new FileOutputStream(file);
        
        PrintStream out = new PrintStream(outputStream);
        
        out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
        out.println("<"+XML_TAG+">");
        
        Collection collection = objectsMap.values();
        for (Iterator iter = collection.iterator(); iter.hasNext();)
        {
            SharedObjectInterface sharedObject = (SharedObjectInterface) iter.next();
            out.println(sharedObject.getXML());
        }

        out.println("</"+XML_TAG+">");
        
        out.flush();
        out.close();
        outputStream.close();
    }
}
