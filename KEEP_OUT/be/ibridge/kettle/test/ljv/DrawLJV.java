package be.ibridge.kettle.test.ljv;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.value.Value;

public class DrawLJV
{
    public DrawLJV()
    {
    }
    
    public static void main(String[] args)
    {
        DrawLJV drawLJV = new DrawLJV();
        drawLJV.drawRow("row.png");
    }

    private void drawRow(String imageFilename)
    {
        Value v[] = new Value[] {
                new Value("v1", "Kettle"),
                new Value("v2", 2.5),
                new Value("v3", 20070422L),
                // new Value("v4", new Date()),
                // new Value("v5", true),
                // new Value("v6", new BigDecimal("39483401329430439843.3493849223204")),
        };
        Row row = new Row();
        for (int i=0;i<v.length;i++) row.addValue(v[i]);
        
        LJV.Context context = LJV.newContext();
        context.ignoreField("offset");
        context.ignoreField("hash");
        context.ignoreField("count");
        
        LJV.drawGraph(context, row, imageFilename);
    }
}
