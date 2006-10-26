package be.ibridge.kettle.trans;

/**
 * A partition schema allow you to partition a step according into a number of partitions that run independendly.
 * It allows us to "map" 
 * 
 * @author Matt
 *
 */
public class PartitionSchema
{
    private String   name;

    private String[] partitionIDs;

    /**
     * @param name
     * @param partitionIDs
     */
    public PartitionSchema(String name, String[] partitionIDs)
    {
        super();
        this.name = name;
        this.partitionIDs = partitionIDs;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the partitionIDs
     */
    public String[] getPartitionIDs()
    {
        return partitionIDs;
    }

    /**
     * @param partitionIDs the partitionIDs to set
     */
    public void setPartitionIDs(String[] partitionIDs)
    {
        this.partitionIDs = partitionIDs;
    }

}
