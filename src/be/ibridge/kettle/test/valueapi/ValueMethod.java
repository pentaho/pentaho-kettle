/**
 * 
 */
package be.ibridge.kettle.test.valueapi;

import java.lang.reflect.Method;

import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;

/**
 * This contains a single Value class method
 * 
 * @author Matt
 */
public class ValueMethod
{
    private Method method;
    
    private Class parameters[];

    /**
     * Create a new Value Method
     * 
     * @param method  The method
     * @param parameters The parameter array
     */
    public ValueMethod(Method method, Class[] parameters)
    {
        super();
        this.method = method;
        this.parameters = parameters;
    }
    
    /**
     * @return the method
     */
    public Method getMethod()
    {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(Method method)
    {
        this.method = method;
    }

    /**
     * @return the parameters
     */
    public Class[] getParameters()
    {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(Class[] parameters)
    {
        this.parameters = parameters;
    }

    public String toString()
    {
        StringBuffer ret = new StringBuffer(100);
        
        ret.append(method.getName());
        ret.append(" (");
        
        for (int p=0;p<parameters.length;p++)
        {
            if (p>0) ret.append(", ");
            ret.append(parameters[p].getName());
        }
        ret.append(")");
        
        return ret.toString();
    }
 
    /**
     * Execute this method on a value and arguments
     * @param sourceValue the value to work on
     * @param arguments the arguments.
     * @return the value returned by the execution
     */
    public Object executeMethod(Value sourceValue, Value[] arguments) throws KettleException
    {
        try
        {
            return method.invoke(sourceValue, arguments);
        }
        catch(Exception e)
        {
            throw new KettleException("There was an error executing method ["+method.getName()+"] with "+arguments.length+" arguments", e);
        }
    }
}
