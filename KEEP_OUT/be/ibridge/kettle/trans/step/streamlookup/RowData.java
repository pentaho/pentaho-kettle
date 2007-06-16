package be.ibridge.kettle.trans.step.streamlookup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import be.ibridge.kettle.core.Row;

/**
 * Serialises objects to save space
 * 
 * @author Matt
 *
 */
public class RowData
{
    private Row metadata;
    private byte[] data;
    
    public RowData(Row metadata, Row row)
    {
        this.metadata = metadata;
        data = extractData(row);
    }
    
    public int hashCode()
    {
        return data.hashCode();
    }
    
    public boolean equals(Object obj)
    {
        RowData rowData = (RowData) obj;
        
        return rowData.getRow().equals(getRow());
    } 

    public static final byte[] extractData(Row row)
    {
        try
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            row.writeData( dataOutputStream );
            dataOutputStream.close();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        }
        catch(Exception e)
        {
            throw new RuntimeException("Error serializing row to byte array: "+row, e);
        }
    }
    
    public Row getRow()
    {
        try
        {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
            return new Row(dataInputStream, metadata.size(), metadata);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Error de-serializing row from byte array", e);
        }
    }
}
