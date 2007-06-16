package be.ibridge.kettle.core;

import java.util.ArrayList;
import java.util.List;

import be.ibridge.kettle.trans.step.StepMeta;

/**
 * A StepField is a field entering (input field) or exiting (output field) a step.
 * 
 * @author Matt
 * @since 12-sep-2005
 */
public class StepField
{
    private StepMeta stepMeta;
    
    private String name;
    private int    type;
    private int    length;
    private int    precision;
    private List   origin;  // List of StepMeta objects: the previous steps...
    
    public StepField(StepMeta stepMeta, String name, int type)
    {
        this.stepMeta = stepMeta;
        this.name     = name;
        this.type     = type;
        this.origin   = new ArrayList();
    }

    /**
     * @return Returns the length.
     */
    public int getLength()
    {
        return length;
    }

    /**
     * @param length The length to set.
     */
    public void setLength(int length)
    {
        this.length = length;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return Returns the origin.
     */
    public List getOrigin()
    {
        return origin;
    }

    /**
     * @param origin The origin to set.
     */
    public void setOrigin(List origin)
    {
        this.origin = origin;
    }

    /**
     * @return Returns the precision.
     */
    public int getPrecision()
    {
        return precision;
    }

    /**
     * @param precision The precision to set.
     */
    public void setPrecision(int precision)
    {
        this.precision = precision;
    }

    /**
     * @return Returns the stepMeta.
     */
    public StepMeta getStepMeta()
    {
        return stepMeta;
    }

    /**
     * @param stepMeta The stepMeta to set.
     */
    public void setStepMeta(StepMeta stepMeta)
    {
        this.stepMeta = stepMeta;
    }

    /**
     * @return Returns the type.
     */
    public int getType()
    {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(int type)
    {
        this.type = type;
    }
    
    
}
