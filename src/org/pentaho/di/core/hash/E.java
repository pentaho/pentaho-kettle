package org.pentaho.di.core.hash;

public class E
{
    private byte[] key;

    private byte[] value;

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
        boolean up = true;
        int idx = 0;
        int hashCode = 0;
        for (int i = 0; i < key.length; i++)
        {
            hashCode ^= Math.round((0xFF << idx) * key[i]);
            if (up)
            {
                idx++;
                if (idx == 8)
                {
                    idx = 6;
                    up = false;
                }
            }
            else
            {
                idx--;
                if (idx < 0)
                {
                    idx = 1;
                    up = true;
                }
            }
        }
        return hashCode;
    }

    /**
     * The row is the same if the value is the same The data types are the same so no error is made here.
     */
    public boolean equals(Object obj)
    {
        E e = (E) obj;

        if (value.length != e.value.length) return false;
        for (int i = value.length - 1; i >= 0; i--)
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
}
