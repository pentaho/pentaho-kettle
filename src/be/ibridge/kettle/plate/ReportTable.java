package be.ibridge.kettle.plate;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;

import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;

public class ReportTable
{
    private String tableName;
    private Point  location;
    private List   xAxisFields;
    private List   yAxisFields;
    private List   measureFields;
    
    /**
     * @param tableName
     */
    public ReportTable(String tableName)
    {
        this.tableName = tableName;
        
        xAxisFields = new ArrayList();
        yAxisFields = new ArrayList();
        measureFields = new ArrayList();
    }


    /**
     * @return Returns the location.
     */
    public Point getLocation()
    {
        return location;
    }


    /**
     * @param location The location to set.
     */
    public void setLocation(Point location)
    {
        this.location = location;
    }


    /**
     * @return Returns the measureFields.
     */
    public List getMeasureFields()
    {
        return measureFields;
    }


    /**
     * @param measureFields The measureFields to set.
     */
    public void setMeasureFields(List measureFields)
    {
        this.measureFields = measureFields;
    }


    /**
     * @return Returns the tableName.
     */
    public String getTableName()
    {
        return tableName;
    }


    /**
     * @param tableName The tableName to set.
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }


    /**
     * @return Returns the xAxisFields.
     */
    public List getXAxisFields()
    {
        return xAxisFields;
    }


    /**
     * @param axisFields The xAxisFields to set.
     */
    public void setXAxisFields(List axisFields)
    {
        xAxisFields = axisFields;
    }


    /**
     * @return Returns the yAxisFields.
     */
    public List getYAxisFields()
    {
        return yAxisFields;
    }


    /**
     * @param axisFields The yAxisFields to set.
     */
    public void setYAxisFields(List axisFields)
    {
        yAxisFields = axisFields;
    }
    
    private int[] getFieldNrs(List fields, Row row) throws KettleException
    {
        int fieldNrs[] = new int[fields.size()];
        for (int i=0;i<fields.size();i++)
        {
            ReportTableField field = (ReportTableField) fields.get(i);
            fieldNrs[i] = row.searchValueIndex( field.getFieldName() );
            if (fieldNrs[i]<0)
            {
                throw new KettleException("Unable to find field ["+field.getFieldName()+"] in row "+row);
            }
        }

        return fieldNrs;
    }
    
    private void getSortFieldNrs(Row row, List fieldNrs, List ascending) throws KettleException
    {
        for ( int i=0 ; i<xAxisFields.size() ; i++)
        {
            ReportTableField field = (ReportTableField) xAxisFields.get(i);
            if (field.getSortType()!=ReportTableField.SORT_TYPE_NONE)
            {
                fieldNrs.add(new Integer(row.searchValueIndex( field.getFieldName() )));
                ascending.add(new Boolean( field.getSortType()==ReportTableField.SORT_TYPE_ASCENDING ) );
            }
        }
        
        for ( int i=0 ; i<yAxisFields.size() ; i++)
        {
            ReportTableField field = (ReportTableField) yAxisFields.get(i);
            if (field.getSortType()!=ReportTableField.SORT_TYPE_NONE)
            {
                fieldNrs.add(new Integer(row.searchValueIndex( field.getFieldName() )));
                ascending.add(new Boolean( field.getSortType()==ReportTableField.SORT_TYPE_ASCENDING ) );
            }
        }
    }
   
    public void draw(GC gc, List rows) throws KettleException
    {
        if (rows.size()==0) return; // Nothing to do ;-)
        
        Row firstRow = (Row)rows.get(0);
        
        int xAxisFieldNrs[]   = getFieldNrs(xAxisFields,   firstRow);
        int yAxisFieldNrs[]   = getFieldNrs(yAxisFields,   firstRow);
        // int measureFieldNrs[] = getFieldNrs(measureFields, firstRow);
        
        List sortFieldNrsList  = new ArrayList();
        List ascendingList     = new ArrayList();
        
        getSortFieldNrs(firstRow, sortFieldNrsList, ascendingList);
        
        int sortFieldNrs[]  = new int[sortFieldNrsList.size()];
        boolean ascending[] = new boolean[sortFieldNrsList.size()];
        for (int i=0;i<sortFieldNrsList.size();i++)
        {
            sortFieldNrs[i] = ((Integer)sortFieldNrsList.get(i)).intValue();
            ascending[i]    = ((Boolean)ascendingList.get(i)).booleanValue();
        }
        
        // Sort the rows in input:
        Row.sortRows(rows, sortFieldNrs, ascending);
        
        for (int r=0;r<rows.size();r++)
        {
            Row row = (Row)rows.get(r);
            
            // Loop over the x-fields
            for (int x=0;x<xAxisFieldNrs.length;x++)
            {
               //  ReportTableField xField = (ReportTableField) xAxisFields.get(x);
                Value xValue = row.getValue( xAxisFieldNrs[x] );
                
                System.out.print(" ["+xValue.toString()+"]");
            }
            
            // Loop over the y-fields
            for (int y=0;y<yAxisFieldNrs.length;y++)
            {
                // ReportTableField yField = (ReportTableField) yAxisFields.get(y);
                Value yValue = row.getValue( yAxisFieldNrs[y] );
                
                System.out.print(" ["+yValue.toString()+"]");
            }

        }
    }
}
