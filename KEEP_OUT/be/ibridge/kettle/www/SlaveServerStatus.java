package be.ibridge.kettle.www;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class SlaveServerStatus
{
    public static final String XML_TAG = "serverstatus";

    private String             statusDescription;
    private String             errorDescription;
    private List               transStatusList;
    

    public SlaveServerStatus()
    {
        transStatusList = new ArrayList();
    }
    
    public SlaveServerStatus(String statusDescription)
    {
        this();
        this.statusDescription = statusDescription;
    }


    /**
     * @param statusDescription
     * @param transStatus
     */
    public SlaveServerStatus(String statusDescription, List transStatusList)
    {
        this.statusDescription = statusDescription;
        this.transStatusList = transStatusList;
    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer();

        xml.append("<" + XML_TAG + ">").append(Const.CR);
        xml.append(XMLHandler.addTagValue("statusdesc", statusDescription));
        xml.append("  <transstatuslist>").append(Const.CR);
        for (int i = 0; i < transStatusList.size(); i++)
        {
            SlaveServerTransStatus transStatus = (SlaveServerTransStatus) transStatusList.get(i);
            xml.append("    ").append(transStatus.getXML()).append(Const.CR);
        }
        xml.append("  </transstatuslist>").append(Const.CR);
        xml.append("</" + XML_TAG + ">").append(Const.CR);

        return xml.toString();
    }

    public SlaveServerStatus(Node statusNode)
    {
        this();
        statusDescription = XMLHandler.getTagValue(statusNode, "statusdesc");
        Node listNode = XMLHandler.getSubNode(statusNode, "transstatuslist");
        int nr = XMLHandler.countNodes(listNode, SlaveServerTransStatus.XML_TAG);
        for (int i = 0; i < nr; i++)
        {
            Node transStatusNode = XMLHandler.getSubNodeByNr(listNode, SlaveServerTransStatus.XML_TAG, i);
            transStatusList.add(new SlaveServerTransStatus(transStatusNode));
        }
    }
    
    public static SlaveServerStatus fromXML(String xml) throws KettleXMLException
    {
        Document document = XMLHandler.loadXMLString(xml);
        return new SlaveServerStatus(XMLHandler.getSubNode(document, XML_TAG));
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
     * @return the transStatusList
     */
    public List getTransStatusList()
    {
        return transStatusList;
    }

    /**
     * @param transStatusList the transStatusList to set
     */
    public void setTransStatusList(List transStatusList)
    {
        this.transStatusList = transStatusList;
    }

    /**
     * @return the errorDescription
     */
    public String getErrorDescription()
    {
        return errorDescription;
    }

    /**
     * @param errorDescription the errorDescription to set
     */
    public void setErrorDescription(String errorDescription)
    {
        this.errorDescription = errorDescription;
    }

    public SlaveServerTransStatus findTransStatus(String transName)
    {
        for (int i=0;i<transStatusList.size();i++)
        {
            SlaveServerTransStatus transStatus = (SlaveServerTransStatus) transStatusList.get(i);
            if (transStatus.getTransName().equalsIgnoreCase(transName)) return transStatus;
        }
        return null;
    }
}
