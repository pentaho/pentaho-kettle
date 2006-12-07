package be.ibridge.kettle.core.hash;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import be.ibridge.kettle.core.Row;


public class E
{
    private byte[] key;
    private byte[] value;
    
    public E(Row keyRow, Row valueRow)
    {
        key = extractData(keyRow);
        if (valueRow!=null) value = extractData(valueRow);
    }
    
    /**
     * @param key
     * @param value
     */
    public E(byte[] key, byte[] value)
    {
        this.key = key;
        this.value = value;
    }
    
    public int hashCode()
    {
        boolean up=true;
        int idx=0;
        int hashCode = 0;
        for (int i=0;i<key.length;i++)
        {
            hashCode^=Math.round( (0xFF<<idx)*key[i] );
            if (up)
            {
                idx++;
                if (idx==8)
                {
                    idx=6;
                    up=false;
                }
            }
            else
            {
                idx--;
                if (idx<0)
                {
                    idx=1;
                    up=true;
                }
            }
        }
        return hashCode;
    }
    
    /**
     * The row is the same if the value is the same
     * The data types are the same so no error is made here.
     */
    public boolean equals(Object obj)
    {
        E e = (E)obj;

        if (value.length != e.value.length) return false;
        for (int i=value.length-1;i>=0;i--)
        {
            if (value[i] != e.value[i]) return false;
        }
        return true;
    } 
    
    /**
     * @return the key
     */
    public byte[] getKey()
    {
        return key;
    }
    /**
     * @param key the key to set
     */
    public void setKey(byte[] key)
    {
        this.key = key;
    }
    /**
     * @return the value
     */
    public byte[] getValue()
    {
        return value;
    }
    /**
     * @param value the value to set
     */
    public void setValue(byte[] value)
    {
        this.value = value;
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
    
    public Row getKeyRow(Row metadata)
    {
        return getRow(key, metadata);
    }

    public Row getValueRow(Row metadata)
    {
        return getRow(value, metadata);
    }

    public Row getRow(byte[] data, Row metadata)
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
