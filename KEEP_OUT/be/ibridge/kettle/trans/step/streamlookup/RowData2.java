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
public class RowData2 
{
    private byte[] data;
    
    public RowData2(Row metadata, Row row)
    {
        data = extractData(row);
    }
    
    public int hashCode()
    {
        int hashCode = 0;
        
        // just do the last 4 bytes for performance reasons. 
        // for (int i=data.length-1;i>data.length-5 && i>=0;i--) hashCode^=new Byte(data[i]).hashCode();
        for (int i=0;i<data.length;i++) hashCode^=Math.round( (Math.pow(10, i)*data[i]) );
     
        // LogWriter.getInstance().logBasic("RowData2", "hashcode = "+hashCode);
        return hashCode;
    }
    
    /**
     * The row is the same if the value is the same
     * The data types are the same so no error is made here.
     */
    public boolean equals(Object obj)
    {
        RowData2 rowData = (RowData2) obj;

        if (data.length != rowData.data.length) return false;
        for (int i=data.length-1;i>=0;i--)
        {
            if (data[i] != rowData.data[i]) return false;
        }
        return true;
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
    
    public Row getRow(Row metadata)
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

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }
}
