package org.pentaho.di.trans.step;

import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
    private long linesRejected;
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
        this.linesRead = baseStep.getLinesRead();
        this.linesWritten = baseStep.getLinesWritten();
        this.linesInput = baseStep.getLinesInput();
        this.linesOutput = baseStep.getLinesOutput();
        this.linesUpdated = baseStep.getLinesUpdated(); 
        this.linesRejected = baseStep.getLinesRejected();
        this.errors = baseStep.getErrors();
        this.statusDescription = baseStep.getStatusDescription();
        this.seconds = Math.floor((lapsed * 10) + 0.5) / 10;
        this.speed = lapsed == 0 ? "-" : "" + (in_speed > out_speed ? in_speed : out_speed); //$NON-NLS-1$ //$NON-NLS-2$
        this.priority = baseStep.isAlive() ? "" + baseStep.getPriority() + "/" + baseStep.rowsetInputSize() + "/" + baseStep.rowsetOutputSize() : "-"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        this.sleeps = "" + baseStep.getNrGetSleeps() + "/" + baseStep.getNrPutSleeps(); // $NON-NLS-1$ $NON-NLS-2$  
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
                    "<th>"+linesRejected+"</th> " +
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
                    XMLHandler.addTagValue("linesRejected", linesRejected, false) +
                    XMLHandler.addTagValue("errors", errors, false) +
                    XMLHandler.addTagValue("statusDescription", statusDescription, false) +
                    XMLHandler.addTagValue("seconds", seconds, false) +
                    XMLHandler.addTagValue("speed", speed, false) +
                    XMLHandler.addTagValue("priority", priority, false) +
                    XMLHandler.addTagValue("sleeps", sleeps, false) +
                "</"+XML_TAG+">";
    }
    
    public StepStatus(Node node)
    {
        stepname = XMLHandler.getTagValue(node, "stepname");
        copy = Integer.parseInt( XMLHandler.getTagValue(node, "copy") );
        linesRead = Long.parseLong( XMLHandler.getTagValue(node, "linesRead") );
        linesWritten = Long.parseLong( XMLHandler.getTagValue(node, "linesWritten") );
        linesInput = Long.parseLong( XMLHandler.getTagValue(node, "linesInput") );
        linesOutput = Long.parseLong( XMLHandler.getTagValue(node, "linesOutput") );
        linesUpdated = Long.parseLong( XMLHandler.getTagValue(node, "linesUpdated") );
        linesRejected = Long.parseLong( XMLHandler.getTagValue(node, "linesRejected") );
        errors = Long.parseLong( XMLHandler.getTagValue(node, "errors") );
        statusDescription = XMLHandler.getTagValue(node, "statusDescription");
        seconds = Double.parseDouble( XMLHandler.getTagValue(node, "seconds") );
        speed = XMLHandler.getTagValue(node, "speed");
        priority = XMLHandler.getTagValue(node, "priority");
        sleeps = XMLHandler.getTagValue(node, "sleeps");
    }
    
    public StepStatus fromXML(String xml) throws KettleXMLException
    {
        Document document = XMLHandler.loadXMLString(xml);
        return new StepStatus(XMLHandler.getSubNode(document, XML_TAG));
    }
    
    public String[] getSpoonLogFields()
    {
        String fields[] = new String[15];
        fields[1] = stepname;
        fields[2] = Integer.toString(copy);
        fields[3] = Long.toString(linesRead);
        fields[4] = Long.toString(linesWritten);
        fields[5] = Long.toString(linesInput);
        fields[6] = Long.toString(linesOutput);
        fields[7] = Long.toString(linesUpdated);
        fields[8] = Long.toString(linesRejected);
        fields[9] = Long.toString(errors);
        fields[10] = statusDescription;
        fields[11] = Double.toString(seconds);
        fields[12] = speed;
        fields[13] = priority;
        fields[14] = sleeps;
        
        return fields;
    }
    
    public String[] getSpoonSlaveLogFields()
    {
        String fields[] = getSpoonLogFields();
        String retval[] = new String[fields.length-1];
        for (int i=0;i<retval.length;i++)
        {
            retval[i] = fields[i+1];
        }
        return retval;
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

    /**
     * @return the linesRejected
     */
    public long getLinesRejected()
    {
        return linesRejected;
    }

    /**
     * @param linesRejected the linesRejected to set
     */
    public void setLinesRejected(long linesRejected)
    {
        this.linesRejected = linesRejected;
    }

}
