package be.ibridge.kettle.trans.step.streamlookup;


public class KeyValue
{
    private RowData2 key;
    private RowData2 value;

    public KeyValue(RowData2 key, RowData2 value)
    {
        this.key = key;
        this.value = value;
    }
    
    public RowData2 getKey()
    {
        return key;
    }
    
    public RowData2 getValue()
    {
        return value;
    }
}
