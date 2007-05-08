package org.pentaho.di.core.database;

import java.util.ArrayList;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.value.Value;

public class DatabaseConnectionPoolParameter
{
    private String parameter;
    private String defaultValue;
    private String description;
    
    public DatabaseConnectionPoolParameter()
    {
    }
    
    /**
     * @param parameter
     * @param defaultValue
     * @param description
     */
    public DatabaseConnectionPoolParameter(String parameter, String defaultValue, String description)
    {
        this();
        this.parameter = parameter;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the parameter
     */
    public String getParameter()
    {
        return parameter;
    }

    /**
     * @param parameter the parameter to set
     */
    public void setParameter(String parameter)
    {
        this.parameter = parameter;
    }
    
    public static final String[] getParameterNames(DatabaseConnectionPoolParameter[] poolParameters)
    {
        String names[] = new String[poolParameters.length];
        for (int i=0;i<names.length;i++)
        {
            names[i] = poolParameters[i].getParameter();
        }
        return names;
    }
    
    public static final DatabaseConnectionPoolParameter findParameter(String parameterName, DatabaseConnectionPoolParameter[] poolParameters)
    {
        for (int i=0;i<poolParameters.length;i++)
        {
            if (poolParameters[i].getParameter().equalsIgnoreCase(parameterName)) return poolParameters[i];
        }
        return null;
    }
    
    public static final ArrayList getRowList(DatabaseConnectionPoolParameter[] poolParameters, String titleParameter, String titleDefaultValue, String titleDescription)
    {
        ArrayList list = new ArrayList();
        
        for (int i=0;i<poolParameters.length;i++)
        {
            DatabaseConnectionPoolParameter p = poolParameters[i];
            
            Row row = new Row();
            
            row.addValue( new Value(titleParameter, p.getParameter()));
            row.addValue( new Value(titleDefaultValue, p.getDefaultValue()));
            row.addValue( new Value(titleDescription, p.getDescription()));
            list.add(row);
        }
        
        return list;
    }
}
