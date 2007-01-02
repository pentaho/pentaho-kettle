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

    public SharedObjects(String sharedObjectsFile) throws KettleXMLException
    {
        this.filename = createFilename(sharedObjectsFile);
        this.objectsMap = new Hashtable();

        // Extra information
        File file = new File(filename);
        
        // If we have a shared file, load the content, otherwise, just keep this one empty
        if (file.exists()) 
        {
            LogWriter.getInstance().logDetailed("SharedObjects", "Reading the shared objects file ["+file+"]");
            Document document = XMLHandler.loadXMLFile(file);
            Node sharedObjectsNode = XMLHandler.getSubNode(document, XML_TAG);
            if (sharedObjectsNode!=null)
            {
                List privateSlaveServers = new ArrayList();
                ArrayList privateDatabases = new ArrayList();
                
                NodeList childNodes = sharedObjectsNode.getChildNodes();
                // First load databases & slaves
                //
                for (int i=0;i<childNodes.getLength();i++)
                {
                    Node node = childNodes.item(i);
                    String nodeName = node.getNodeName();
                    
                    SharedObjectInterface isShared = null;
    
                    if (nodeName.equals(DatabaseMeta.XML_TAG))
                    {    
                        isShared = new DatabaseMeta(node);
                        privateDatabases.add(isShared);
                    }
                    else if (nodeName.equals(SlaveServer.XML_TAG)) 
                    {
                        isShared = new SlaveServer(node);
                        privateSlaveServers.add(isShared);
                    }

                    if (isShared!=null)
                    {
                        isShared.setShared(true);
                        storeObject(isShared);
                    }
                }

                // Then load the other objects that might reference databases & slaves
                //
                for (int i=0;i<childNodes.getLength();i++)
                {
                    Node node = childNodes.item(i);
                    String nodeName = node.getNodeName();
                    
                    SharedObjectInterface isShared = null;
    
                    if (nodeName.equals(StepMeta.XML_TAG))        
                    { 
                        StepMeta stepMeta = new StepMeta(node, privateDatabases, null);
                        stepMeta.setDraw(false); // don't draw it, keep it in the tree.
                        isShared = stepMeta;
                    }
                    else if (nodeName.equals(PartitionSchema.XML_TAG)) 
                    {
                        isShared = new PartitionSchema(node);
                    }
                    else if (nodeName.equals(ClusterSchema.XML_TAG)) 
                    {   
                        isShared = new ClusterSchema(node, privateSlaveServers);
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

    public SharedObjects() throws KettleXMLException
    {
        this(null);
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
