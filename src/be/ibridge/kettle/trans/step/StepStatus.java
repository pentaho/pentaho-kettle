package be.ibridge.kettle.trans.step;

import be.ibridge.kettle.core.XMLHandler;

public class StepStatus
{
    public static final String XML_TAG = "stepstatus";
    
    private String stepname;
    private int copy;
    private long linesRead;
    private long linesWritten;
    private long linesInput;
    private long linesOutput;
    private long linesUpdated;
    private long errors;
    private String statusDescription;
    private double seconds;
    private String speed;
    private String priority;
    private String sleeps;
    
    public StepStatus(BaseStep baseStep)
    {
        // Proc: nr of lines processed: input + output!
        long in_proc = baseStep.linesInput + baseStep.linesRead;
        long out_proc = baseStep.linesOutput + baseStep.linesWritten + baseStep.linesUpdated;

        float lapsed = ((float) baseStep.getRuntime()) / 1000;
        double in_speed = 0;
        double out_speed = 0;

        if (lapsed != 0)
        {
            in_speed = Math.floor(10 * (in_proc / lapsed)) / 10;
            out_speed = Math.floor(10 * (out_proc / lapsed)) / 10;
        }

        this.stepname = baseStep.getStepname();
        this.copy = baseStep.getCopy();
        this.linesRead = baseStep.getLinesRead(); //$NON-NLS-1$
        this.linesWritten = baseStep.getLinesWritten(); //$NON-NLS-1$
        this.linesInput = baseStep.getLinesInput(); //$NON-NLS-1$
        this.linesOutput = baseStep.getLinesOutput(); //$NON-NLS-1$
        this.linesUpdated = baseStep.getLinesUpdated(); //$NON-NLS-1$
        this.errors = baseStep.getErrors(); //$NON-NLS-1$
        this.statusDescription = baseStep.getStatusDescription(); //$NON-NLS-1$
        this.seconds = Math.floor((lapsed * 10) + 0.5) / 10; //$NON-NLS-1$
        this.speed = lapsed == 0 ? "-" : "" + (in_speed > out_speed ? in_speed : out_speed); //$NON-NLS-1$ //$NON-NLS-2$
        this.priority = baseStep.isAlive() ? "" + baseStep.getPriority() + "/" + baseStep.rowsetInputSize() + "/" + baseStep.rowsetOutputSize() : "-"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        this.sleeps = "" + baseStep.getNrGetSleeps() + "/" + baseStep.getNrPutSleeps();
    }
    
    public String getHTMLTableRow()
    {
        return "<tr> " +
                    "<th>"+stepname+"</th> " +
                    "<th>"+copy+"</th> " +
                    "<th>"+linesRead+"</th> " +
                    "<th>"+linesWritten+"</th> " +
                    "<th>"+linesInput+"</th> " +
                    "<th>"+linesOutput+"</th> " +
                    "<th>"+linesUpdated+"</th> " +
                    "<th>"+errors+"</th> " +
                    "<th>"+statusDescription+"</th> " +
                    "<th>"+seconds+"</th> " +
                    "<th>"+speed+"</th> " +
                    "<th>"+priority+"</th> " +
                    "<th>"+sleeps+"</th> " +
                "</tr>";
    }

    public String getXML()
    {
        return  "<"+XML_TAG+">" +
                    XMLHandler.addTagValue("stepname", stepname, false) +
                    XMLHandler.addTagValue("copy", copy, false) +
                    XMLHandler.addTagValue("linesRead", linesRead, false) +
                    XMLHandler.addTagValue("linesWritten", linesWritten, false) +
                    XMLHandler.addTagValue("linesInput", linesInput, false) +
                    XMLHandler.addTagValue("linesOutput", linesOutput, false) +
                    XMLHandler.addTagValue("linesUpdated", linesUpdated, false) +
                    XMLHandler.addTagValue("errors", errors, false) +
                    XMLHandler.addTagValue("statusDescription", statusDescription, false) +
                    XMLHandler.addTagValue("seconds", seconds, false) +
                    XMLHandler.addTagValue("speed", speed, false) +
                    XMLHandler.addTagValue("priority", priority, false) +
                    XMLHandler.addTagValue("sleeps", sleeps, false) +
                "</"+XML_TAG+">";
    }
    
    public String[] getSpoonLogFields()
    {
        String fields[] = new String[14];
        fields[1] = stepname;
        fields[2] = ""+copy;
        fields[3] = "" + linesRead;
        fields[4] = "" + linesWritten;
        fields[5] = "" + linesInput;
        fields[6] = "" + linesOutput;
        fields[7] = "" + linesUpdated;
        fields[8] = "" + errors;
        fields[9] = statusDescription;
        fields[10] = "" + seconds;
        fields[11] = "" + speed;
        fields[12] = priority;
        fields[13] = sleeps;
        
        return fields;
    }
    
    /**
     * @return the copy
     */
    public int getCopy()
    {
        return copy;
    }

    /**
     * @param copy the copy to set
     */
    public void setCopy(int copy)
    {
        this.copy = copy;
    }

    /**
     * @return the errors
     */
    public long getErrors()
    {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(long errors)
    {
        this.errors = errors;
    }

    /**
     * @return the linesInput
     */
    public long getLinesInput()
    {
        return linesInput;
    }

    /**
     * @param linesInput the linesInput to set
     */
    public void setLinesInput(long linesInput)
    {
        this.linesInput = linesInput;
    }

    /**
     * @return the linesOutput
     */
    public long getLinesOutput()
    {
        return linesOutput;
    }

    /**
     * @param linesOutput the linesOutput to set
     */
    public void setLinesOutput(long linesOutput)
    {
        this.linesOutput = linesOutput;
    }

    /**
     * @return the linesRead
     */
    public long getLinesRead()
    {
        return linesRead;
    }

    /**
     * @param linesRead the linesRead to set
     */
    public void setLinesRead(long linesRead)
    {
        this.linesRead = linesRead;
    }

    /**
     * @return the linesUpdated
     */
    public long getLinesUpdated()
    {
        return linesUpdated;
    }

    /**
     * @param linesUpdated the linesUpdated to set
     */
    public void setLinesUpdated(long linesUpdated)
    {
        this.linesUpdated = linesUpdated;
    }

    /**
     * @return the linesWritten
     */
    public long getLinesWritten()
    {
        return linesWritten;
    }

    /**
     * @param linesWritten the linesWritten to set
     */
    public void setLinesWritten(long linesWritten)
    {
        this.linesWritten = linesWritten;
    }

    /**
     * @return the priority
     */
    public String getPriority()
    {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    /**
     * @return the seconds
     */
    public double getSeconds()
    {
        return seconds;
    }

    /**
     * @param seconds the seconds to set
     */
    public void setSeconds(double seconds)
    {
        this.seconds = seconds;
    }

    /**
     * @return the sleeps
     */
    public String getSleeps()
    {
        return sleeps;
    }

    /**
     * @param sleeps the sleeps to set
     */
    public void setSleeps(String sleeps)
    {
        this.sleeps = sleeps;
    }

    /**
     * @return the speed
     */
    public String getSpeed()
    {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(String speed)
    {
        this.speed = speed;
    }

    /**
     * @return the statusDescription
     */
    public String getStatusDescription()
    {
        return statusDescription;
    }

    /**
     * @param statusDescription the statusDescription to set
     */
    public void setStatusDescription(String statusDescription)
    {
        this.statusDescription = statusDescription;
    }

    /**
     * @return the stepname
     */
    public String getStepname()
    {
        return stepname;
    }

    /**
     * @param stepname the stepname to set
     */
    public void setStepname(String stepname)
    {
        this.stepname = stepname;
    }

}
