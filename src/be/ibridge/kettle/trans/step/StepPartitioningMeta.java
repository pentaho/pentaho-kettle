package be.ibridge.kettle.trans.step;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.value.Value;

public class StepPartitioningMeta implements XMLInterface
{
    public static final int PARTITIONING_METHOD_NONE = 0;
    public static final int PARTITIONING_METHOD_MOD  = 1;
    
    public static final String[] methodCodes        = new String[] { "none", "Mod" };
    public static final String[] methodDescriptions = new String[] { "none", "Rest of division" };

    private int             method;
    private String          fieldName;

    
    public StepPartitioningMeta()
    {
        method = PARTITIONING_METHOD_NONE;
    }
    
    /**
     * @param method
     * @param fieldName
     */
    public StepPartitioningMeta(int method, String fieldName)
    {
        super();
        this.method = method;
        this.fieldName = fieldName;
    }

    /**
     * @return the partitionColumn
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @param fieldName the field name to set
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @return the partitioningMethod
     */
    public int getMethod()
    {
        return method;
    }

    /**
     * @param method the partitioning method to set
     */
    public void setMethod(int method)
    {
        this.method = method;
    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer();

        xml.append("         <partitioning>"+Const.CR);
        xml.append("           "+XMLHandler.addTagValue("method",    getMethodCode()));
        xml.append("           "+XMLHandler.addTagValue("field_name", fieldName));
        xml.append("           </partitioning>"+Const.CR);
        
        return xml.toString();
    }
    
    public StepPartitioningMeta(Node partitioningMethodNode)
    {
        method = getMethod( XMLHandler.getTagValue(partitioningMethodNode, "method") );
        fieldName = XMLHandler.getTagValue(partitioningMethodNode, "field_name");
    }
    
    public String getMethodCode()
    {
        return methodCodes[method];
    }

    public String getMethodDescription()
    {
        return methodDescriptions[method];
    }

    public static final int getMethod(String description)
    {
        for (int i=0;i<methodDescriptions.length;i++)
        {
            if (methodDescriptions[i].equalsIgnoreCase(description)) return i;
        }
        
        for (int i=0;i<methodCodes.length;i++)
        {
            if (methodCodes[i].equalsIgnoreCase(description)) return i;
        }
        return PARTITIONING_METHOD_NONE;
    }

    public boolean isPartitioned()
    {
        return method!=PARTITIONING_METHOD_NONE;
    }

    public int getPartitionNr(Value value, int nrPartitions)
    {
        int nr = 0;
        switch(method)
        {
        case PARTITIONING_METHOD_MOD:
            nr = (int)(value.getInteger() % nrPartitions);
            break;
        }
        return nr;
    }
}
