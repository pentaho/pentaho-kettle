package be.ibridge.kettle.core;

import java.util.Date;

public class TimedRow
{
    private Date logDate;

    private Row  row;

    /**
     * @param logDate
     * @param row
     */
    public TimedRow(Date logDate, Row row)
    {
        this.logDate = logDate;
        this.row = row;
    }

    /**
     * @param row
     */
    public TimedRow(Row row)
    {
        this.logDate = new Date();
        this.row = row;
    }
    
    public String toString()
    {
        return row.toString();
    }
    
    public boolean equals(Object obj)
    {
        TimedRow timedRow = (TimedRow) obj;
        return row.equals(timedRow.row);
    }
    
    public int hashCode()
    {
        return row.hashCode();
    }

    /**
     * @return the row
     */
    public Row getRow()
    {
        return row;
    }

    /**
     * @param row the row to set
     */
    public void setRow(Row row)
    {
        this.row = row;
    }

    /**
     * @return the logDate
     */
    public Date getLogDate()
    {
        return logDate;
    }

    /**
     * @param logDate the logDate to set
     */
    public void setLogDate(Date logDate)
    {
        this.logDate = logDate;
    }

    /**
     * Get the logging time for this row.
     * @return the logging time for this row.
     */
    public long getLogtime()
    {
        if (logDate == null) return 0L;
        return logDate.getTime();
    }

    
}
