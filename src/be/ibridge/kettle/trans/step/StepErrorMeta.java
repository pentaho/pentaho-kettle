package be.ibridge.kettle.trans.step;

import java.util.List;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.ChangedFlag;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.value.Value;

/**
 * This class contains the metadata to handle proper error handling on a step level.
 * 
 * @author Matt
 * 
 */
public class StepErrorMeta extends ChangedFlag implements XMLInterface, Cloneable
{
    public static final String XML_TAG = "error";
    
    /** The source step that can send the error rows */
    private StepMeta sourceStep;
    
    /** The target step to send the error rows to */
    private StepMeta targetStep;
    
    /** Is the error handling enabled? */
    private boolean  enabled;

    /** the name of the field value to contain the number of errors (null or empty means it's not needed) */  
    private String   nrErrorsValuename;

    /** the name of the field value to contain the error description(s) (null or empty means it's not needed) */  
    private String   errorDescriptionsValuename;

    /** the name of the field value to contain the fields for which the error(s) occured (null or empty means it's not needed) */  
    private String   errorFieldsValuename;

    /** the name of the field value to contain the error code(s) (null or empty means it's not needed) */  
    private String   errorCodesValuename;
    
    /**
     * Create a new step error handling metadata object
     * @param sourceStep The source step that can send the error rows
     */
    public StepErrorMeta(StepMeta sourceStep)
    {
        this.sourceStep = sourceStep;
        this.enabled = true;
    }

    /**
     * Create a new step error handling metadata object
     * @param sourceStep The source step that can send the error rows
     * @param targetStep The target step to send the error rows to
     */
    public StepErrorMeta(StepMeta sourceStep, StepMeta targetStep)
    {
        this.sourceStep = sourceStep;
        this.targetStep = targetStep;
        this.enabled = true;
    }
    
    /**
     * Create a new step error handling metadata object
     * @param sourceStep The source step that can send the error rows
     * @param targetStep The target step to send the error rows to
     * @param nrErrorsValuename the name of the field value to contain the number of errors (null or empty means it's not needed) 
     * @param errorDescriptionsValuename the name of the field value to contain the error description(s) (null or empty means it's not needed) 
     * @param errorFieldsValuename the name of the field value to contain the fields for which the error(s) occured (null or empty means it's not needed)
     * @param errorCodesValuename the name of the field value to contain the error code(s) (null or empty means it's not needed)
     */
    public StepErrorMeta(StepMeta sourceStep, StepMeta targetStep, String nrErrorsValuename, String errorDescriptionsValuename, String errorFieldsValuename, String errorCodesValuename)
    {
        this.sourceStep = sourceStep;
        this.targetStep = targetStep;
        this.enabled = true;
        this.nrErrorsValuename = nrErrorsValuename;
        this.errorDescriptionsValuename = errorDescriptionsValuename;
        this.errorFieldsValuename = errorFieldsValuename;
        this.errorCodesValuename = errorCodesValuename;
    }
    
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch(CloneNotSupportedException e)
        {
            return null;
        }
    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer();

        xml.append("      ").append(XMLHandler.openTag(XML_TAG)).append(Const.CR);
        xml.append("        ").append(XMLHandler.addTagValue("source_step", sourceStep!=null ? sourceStep.getName() : ""));
        xml.append("        ").append(XMLHandler.addTagValue("target_step", targetStep!=null ? targetStep.getName() : ""));
        xml.append("        ").append(XMLHandler.addTagValue("is_enabled", enabled));
        xml.append("        ").append(XMLHandler.addTagValue("nr_valuename", nrErrorsValuename));
        xml.append("        ").append(XMLHandler.addTagValue("descriptions_valuename", errorDescriptionsValuename));
        xml.append("        ").append(XMLHandler.addTagValue("fields_valuename", errorFieldsValuename));
        xml.append("        ").append(XMLHandler.addTagValue("codes_valuename", errorCodesValuename));
        xml.append("      ").append(XMLHandler.closeTag(XML_TAG)).append(Const.CR);
        
        return xml.toString();
    }
    
    public StepErrorMeta(Node node, List steps)
    {
        sourceStep = StepMeta.findStep(steps, XMLHandler.getTagValue(node, "source_step"));
        targetStep = StepMeta.findStep(steps, XMLHandler.getTagValue(node, "target_step"));
        enabled = "Y".equals( XMLHandler.getTagValue(node, "is_enabled") );
        nrErrorsValuename = XMLHandler.getTagValue(node, "nr_valuename");
        errorDescriptionsValuename = XMLHandler.getTagValue(node, "descriptions_valuename");
        errorFieldsValuename = XMLHandler.getTagValue(node, "fields_valuename");
        errorCodesValuename = XMLHandler.getTagValue(node, "codes_valuename");
    }

    /**
     * @return the error codes valuename
     */
    public String getErrorCodesValuename()
    {
        return errorCodesValuename;
    }

    /**
     * @param errorCodesValuename the error codes valuename to set
     */
    public void setErrorCodesValuename(String errorCodesValuename)
    {
        this.errorCodesValuename = errorCodesValuename;
    }

    /**
     * @return the error descriptions valuename
     */
    public String getErrorDescriptionsValuename()
    {
        return errorDescriptionsValuename;
    }

    /**
     * @param errorDescriptionsValuename the error descriptions valuename to set
     */
    public void setErrorDescriptionsValuename(String errorDescriptionsValuename)
    {
        this.errorDescriptionsValuename = errorDescriptionsValuename;
    }

    /**
     * @return the error fields valuename
     */
    public String getErrorFieldsValuename()
    {
        return errorFieldsValuename;
    }

    /**
     * @param errorFieldsValuename the error fields valuename to set
     */
    public void setErrorFieldsValuename(String errorFieldsValuename)
    {
        this.errorFieldsValuename = errorFieldsValuename;
    }

    /**
     * @return the nr errors valuename
     */
    public String getNrErrorsValuename()
    {
        return nrErrorsValuename;
    }

    /**
     * @param nrErrorsValuename the nr errors valuename to set
     */
    public void setNrErrorsValuename(String nrErrorsValuename)
    {
        this.nrErrorsValuename = nrErrorsValuename;
    }

    /**
     * @return the target step
     */
    public StepMeta getTargetStep()
    {
        return targetStep;
    }

    /**
     * @param targetStep the target step to set
     */
    public void setTargetStep(StepMeta targetStep)
    {
        this.targetStep = targetStep;
    }

    /**
     * @return The source step can send the error rows
     */
    public StepMeta getSourceStep()
    {
        return sourceStep;
    }

    /**
     * @param sourceStep The source step can send the error rows
     */
    public void setSourceStep(StepMeta sourceStep)
    {
        this.sourceStep = sourceStep;
    }

    /**
     * @return the enabled flag: Is the error handling enabled?
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * @param enabled the enabled flag to set: Is the error handling enabled?
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public Row getErrorFields()
    {
        return getErrorFields(0L, null, null, null);
    }
    
    public Row getErrorFields(long nrErrors, String errorDescriptions, String fieldNames, String errorCodes)
    {
        Row row = new Row();
        if (!Const.isEmpty(getNrErrorsValuename()))
        {
            Value v = new Value(getNrErrorsValuename(), nrErrors);
            v.setLength(3);
            row.addValue(v);
        }
        if (!Const.isEmpty(getErrorDescriptionsValuename()))
        {
            Value v = new Value(getErrorDescriptionsValuename(), errorDescriptions);
            row.addValue(v);
        }
        if (!Const.isEmpty(getErrorFieldsValuename()))
        {
            Value v = new Value(getErrorFieldsValuename(), fieldNames);
            row.addValue(v);
        }
        if (!Const.isEmpty(getErrorCodesValuename()))
        {
            Value v = new Value(getErrorCodesValuename(), errorCodes);
            row.addValue(v);
        }
        
        return row;
    }
}
