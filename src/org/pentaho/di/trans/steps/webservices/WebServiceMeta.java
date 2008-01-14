package org.pentaho.di.trans.steps.webservices;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepCategory;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

@Step(name="WebServiceLookup",image="ui/images/WSL.png",tooltip="BaseStep.TypeTooltipDesc.WebServiceLookup",description="BaseStep.TypeLongDesc.WebServiceLookup",
		category=StepCategory.CATEGORY_LOOKUP)
public class WebServiceMeta extends BaseStepMeta implements StepMetaInterface
{
    public static final String XSD_NS_URI = "http://www.w3.org/2001/XMLSchema";
    
    public static final int DEFAULT_STEP = 1000;
    
    /** Champs disponible dans le web services en entrees */
    private List<WebServiceField> fieldsIn;

    /** Champs disponible dans le web services en sortie */
    private List<WebServiceField> fieldsOut;

    /** Url du webService */
    private String url;

    /** Nom de l'operation utilise dans le web service */
    private String operationName;
    
    private String operationNamespace;

    /** Nom de l'objet qui encapsule les champs en entree lorsque l'on a un tableau */
    private String inFieldContainerName;

    /** Nom de l'objet en entree */
    private String inFieldArgumentName;

    /** Nom de l'objet qui encapsule les champs en sortie lorsque l'on a un tableau */
    private String outFieldContainerName;

    /** Nom de l'objet en sortie */
    private String outFieldArgumentName;
    
    private String proxyHost;
    
    private String proxyPort;
    
    private String httpLogin;
    
    private String httpPassword;
    
    /** Nombre de row a envoyer a chaque appel */
    private int callStep = DEFAULT_STEP;

    public WebServiceMeta()
    {
        super();
        fieldsIn = new ArrayList<WebServiceField>();
        fieldsOut = new ArrayList<WebServiceField>();
    }

    public WebServiceMeta(Node stepnode, List<DatabaseMeta> databases, Hashtable<String, Counter> counters) throws KettleXMLException
    {
        this();
        loadXML(stepnode, databases, counters);
    }

    public WebServiceMeta(Repository rep, long id_step, List<DatabaseMeta> databases, Hashtable<String, Counter> counters) throws KettleException
    {
        this();
        readRep(rep, id_step, databases, counters);
    }

    @Override
    public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException 
    {
        // Input rows and output rows are different in the webservice step
        // TODO Maybe input row are info rows
    	//
        r.clear();
        for (WebServiceField field : getFieldsOut())
        {
            ValueMetaInterface vValue = new ValueMeta(field.getName(), field.getType());
            vValue.setOrigin(name);
            r.addValueMeta(vValue);
        }
    }

    public Object clone()
    {
        Object retval = super.clone();
        return retval;
    }

    public void setDefault()
    {
        System.out.println("setDefaults");
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
    		String[] input, String[] output, RowMetaInterface info) 
    {
    	CheckResult cr;
        if (prev == null || prev.size() == 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepMeta);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving " + prev.size() + " fields", stepMeta);
            remarks.add(cr);
        }

        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
            remarks.add(cr);
        }
        else if (getInFieldArgumentName() != null || getInFieldContainerName() != null)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta);
            remarks.add(cr);
        }
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        //SAUVEGARDE DES DONNEES COMMUNES

        //Sauvegarde de l'URL du webService
        retval.append("    " + XMLHandler.addTagValue("wsURL", getUrl()));

        //Sauvegarde de l'op�ration 
        retval.append("    " + XMLHandler.addTagValue("wsOperation", getOperationName()));
        retval.append("    " + XMLHandler.addTagValue("wsOperationNamespace", getOperationNamespace()));
        retval.append("    " + XMLHandler.addTagValue("wsInFieldContainer", getInFieldContainerName()));
        retval.append("    " + XMLHandler.addTagValue("wsInFieldArgument", getInFieldArgumentName()));
        retval.append("    " + XMLHandler.addTagValue("wsOutFieldContainer", getOutFieldContainerName()));
        retval.append("    " + XMLHandler.addTagValue("wsOutFieldArgument", getOutFieldArgumentName()));
        retval.append("    " + XMLHandler.addTagValue("proxyHost", getProxyHost()));
        retval.append("    " + XMLHandler.addTagValue("proxyPort", getProxyPort()));
        retval.append("    " + XMLHandler.addTagValue("httpLogin", getHttpLogin()));
        retval.append("    " + XMLHandler.addTagValue("httpPassword", getHttpPassword()));
        retval.append("    " + XMLHandler.addTagValue("callStep", getCallStep()));

        //SAUVEGARDE DU PARAMETRAGE DES DONNEES EN ENTREES

        //Sauvegarde du lien entre les champs de l'�tape pr�c�dente et l'URL du WebService
        retval.append("    <fieldsIn>" + Const.CR);
        for (int i = 0; i < getFieldsIn().size(); i++)
        {
            WebServiceField vField = (WebServiceField) getFieldsIn().get(i);
            retval.append("    <field>" + Const.CR);
            retval.append("        " + XMLHandler.addTagValue("name", vField.getName()));
            retval.append("        " + XMLHandler.addTagValue("wsName", vField.getWsName()));
            retval.append("        " + XMLHandler.addTagValue("xsdType", vField.getXsdType()));
            retval.append("    </field>" + Const.CR);
        }
        retval.append("      </fieldsIn>" + Const.CR);

        //SAUVEGARDE DU PARAMETRAGE DES DONNES EN SORTIES
        retval.append("    <fieldsOut>" + Const.CR);
        for (int i = 0; i < getFieldsOut().size(); i++)
        {
            WebServiceField vField = (WebServiceField) getFieldsOut().get(i);
            retval.append("    <field>" + Const.CR);
            retval.append("        " + XMLHandler.addTagValue("name", vField.getName()));
            retval.append("        " + XMLHandler.addTagValue("wsName", vField.getWsName()));
            retval.append("        " + XMLHandler.addTagValue("xsdType", vField.getXsdType()));
            retval.append("    </field>" + Const.CR);
        }
        retval.append("      </fieldsOut>" + Const.CR);

        return retval.toString();
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException 
    {
    	// TODO Auto-generated method stub
        //Chargement de l'Url
        setUrl(XMLHandler.getTagValue(stepnode, "wsURL"));

        //Chargement de l'op�ration
        setOperationName(XMLHandler.getTagValue(stepnode, "wsOperation"));
        setOperationNamespace(XMLHandler.getTagValue(stepnode, "wsOperationNamespace"));
        setInFieldContainerName(XMLHandler.getTagValue(stepnode, "wsInFieldContainer"));
        setInFieldArgumentName(XMLHandler.getTagValue(stepnode, "wsInFieldArgument"));
        setOutFieldContainerName(XMLHandler.getTagValue(stepnode, "wsOutFieldContainer"));
        setOutFieldArgumentName(XMLHandler.getTagValue(stepnode, "wsOutFieldArgument"));
        setProxyHost(XMLHandler.getTagValue(stepnode, "proxyHost"));
        setProxyPort(XMLHandler.getTagValue(stepnode, "proxyPort"));
        setHttpLogin(XMLHandler.getTagValue(stepnode, "httpLogin"));
        setHttpPassword(XMLHandler.getTagValue(stepnode, "httpPassword"));
        setCallStep(Const.toInt(XMLHandler.getTagValue(stepnode, "callStep"), DEFAULT_STEP));

        //CHARGEMENT DES DONNEES EN ENTREES

        //Chargement des champs disponible dans l'�tape pr�c�dente et de leur matching
        getFieldsIn().clear();
        Node fields = XMLHandler.getSubNode(stepnode, "fieldsIn");
        int nrfields = XMLHandler.countNodes(fields, "field");

        for (int i = 0; i < nrfields; ++i)
        {
            Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

            WebServiceField field = new WebServiceField();
            field.setName(XMLHandler.getTagValue(fnode, "name"));
            field.setWsName(XMLHandler.getTagValue(fnode, "wsName"));
            field.setXsdType(XMLHandler.getTagValue(fnode, "xsdType"));
            getFieldsIn().add(field);

        }

        //CHARGEMENT DES DONNES EN SORTIES
        getFieldsOut().clear();

        //Chargement des champs disponibles dans l'�tape suivante
        fields = XMLHandler.getSubNode(stepnode, "fieldsOut");
        nrfields = XMLHandler.countNodes(fields, "field");

        for (int i = 0; i < nrfields; ++i)
        {
            Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

            WebServiceField field = new WebServiceField();
            field.setName(XMLHandler.getTagValue(fnode, "name"));
            field.setWsName(XMLHandler.getTagValue(fnode, "wsName"));
            field.setXsdType(XMLHandler.getTagValue(fnode, "xsdType"));
            getFieldsOut().add(field);
        }
    }

    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
    		throws KettleException 
    {
        // chargement de l'url
    	//
        setUrl(rep.getStepAttributeString(id_step, "wsUrl"));

        // Chargement de l'operation du web services
        //
        setOperationName(rep.getStepAttributeString(id_step, "wsOperation"));
        setOperationNamespace(rep.getStepAttributeString(id_step, "wsOperationNamespace"));
        setInFieldContainerName(rep.getStepAttributeString(id_step, "wsInFieldContainer"));
        setInFieldArgumentName(rep.getStepAttributeString(id_step, "wsInFieldArgument"));
        setOutFieldContainerName(rep.getStepAttributeString(id_step, "wsOutFieldContainer"));
        setOutFieldArgumentName(rep.getStepAttributeString(id_step, "wsOutFieldArgument"));
        setProxyHost(rep.getStepAttributeString(id_step, "proxyHost"));
        setProxyPort(rep.getStepAttributeString(id_step, "proxyPort"));
        setHttpLogin(rep.getStepAttributeString(id_step, "httpLogin"));
        setHttpPassword(rep.getStepAttributeString(id_step, "httpPassword"));
        setCallStep((int) rep.getStepAttributeInteger(id_step, "callStep"));

        //RESTAURATION DU PARAMETRAGE DES DONNEES EN ENTREES

        //Restauration des champs du web services
        int nb = rep.countNrStepAttributes(id_step, "fieldIn_ws_name");
        getFieldsIn().clear();
        for (int i = 0; i < nb; ++i)
        {
            WebServiceField field = new WebServiceField();
            field.setName(rep.getStepAttributeString(id_step, i, "fieldIn_name"));
            field.setWsName(rep.getStepAttributeString(id_step, i, "fieldIn_ws_name"));
            field.setXsdType(rep.getStepAttributeString(id_step, i, "fieldIn_xsd_type"));
            getFieldsIn().add(field);
        }

        //RESTAURATION DU PARAMETRAGE DES DONNES EN SORTIES
        nb = rep.countNrStepAttributes(id_step, "fieldOut_ws_name");
        getFieldsOut().clear();
        for (int i = 0; i < nb; ++i)
        {
            WebServiceField field = new WebServiceField();
            field.setName(rep.getStepAttributeString(id_step, i, "fieldOut_name"));
            field.setWsName(rep.getStepAttributeString(id_step, i, "fieldOut_ws_name"));
            field.setXsdType(rep.getStepAttributeString(id_step, i, "fieldOut_xsd_type"));
            getFieldsOut().add(field);
        }

    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {

        //Sauvegarde de l'URL du web services
        rep.saveStepAttribute(id_transformation, id_step, "wsUrl", getUrl());

        //Sauvegarde de l'op�ration du web services
        rep.saveStepAttribute(id_transformation, id_step, "wsOperation", getOperationName());
        rep.saveStepAttribute(id_transformation, id_step, "wsOperationNamespace", getOperationNamespace());
        rep.saveStepAttribute(id_transformation, id_step, "wsInFieldContainer", getInFieldContainerName());
        rep.saveStepAttribute(id_transformation, id_step, "wsInFieldArgument", getInFieldArgumentName());
        rep.saveStepAttribute(id_transformation, id_step, "wsOutFieldContainer", getOutFieldContainerName());
        rep.saveStepAttribute(id_transformation, id_step, "wsOutFieldArgument", getOutFieldArgumentName());
        rep.saveStepAttribute(id_transformation, id_step, "proxyHost", getProxyHost());
        rep.saveStepAttribute(id_transformation, id_step, "proxyPort", getProxyPort());
        rep.saveStepAttribute(id_transformation, id_step, "httpLogin", getHttpLogin());
        rep.saveStepAttribute(id_transformation, id_step, "httpPassword", getHttpPassword());
        rep.saveStepAttribute(id_transformation, id_step, "callStep", getCallStep());

        //SAUVEGARDE DU PARAMETRAGE DES DONNEES EN ENTREES

        //Sauvegarde des champs du web services 
        for (int i = 0; i < getFieldsIn().size(); ++i)
        {
            WebServiceField vField = (WebServiceField) getFieldsIn().get(i);
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldIn_name", vField.getName());
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldIn_ws_name", vField.getWsName());
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldIn_xsd_type", vField.getXsdType());
        }

        //SAUVEGARDE DU PARAMETRAGE DES DONNES EN SORTIES
        for (int i = 0; i < getFieldsOut().size(); ++i)
        {
            WebServiceField vField = (WebServiceField) getFieldsOut().get(i);
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldOut_name", vField.getName());
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldOut_ws_name", vField.getWsName());
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldOut_xsd_type", vField.getXsdType());
        }
    }

    public String getOperationName()
    {
        return operationName;
    }

    public void setOperationName(String operationName)
    {
        this.operationName = operationName;
    }
    
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
    {
        return new WebService(stepMeta, stepDataInterface, cnr, transMeta, disp);
    }

    public StepDataInterface getStepData()
    {
        return new WebServiceData();
    }

    public String[] getUsedVariables()
    {
        // Voir s'il faut retourner les variables que l'on peut mettre pour le proxy,
        // l'url du serveur et l'authentification http
        return null;
        //        List vList = new ArrayList();
//        for (Iterator iter = valueInLinkWebServiceFieldList.iterator(); iter.hasNext();)
//        {
//            FieldLinkWebServiceField vField = (FieldLinkWebServiceField) iter.next();
//            if (!vField.getField().isNull() && vField.getField().getString().startsWith("${"))
//            {
//                vList.add(vField.getField().getString());
//            }
//        }
//        return (String[])vList.toArray(new String[vList.size()]);
    }

    public WebServiceField getFieldInFromName(String name)
    {
        WebServiceField param = null;
        for (Iterator<WebServiceField> iter = getFieldsIn().iterator(); iter.hasNext();)
        {
            WebServiceField paramCour = (WebServiceField) iter.next();
            if (name.equals(paramCour.getName()))
            {
                param = paramCour;
                break;
            }
        }
        return param;
    }
    
    
    public WebServiceField getFieldOutFromWsName(String wsName)
    {
        WebServiceField param = null;
        for (Iterator<WebServiceField> iter = getFieldsOut().iterator(); iter.hasNext();)
        {
            WebServiceField paramCour = (WebServiceField) iter.next();
            if (paramCour.getWsName().equals(wsName))
            {
                param = paramCour;
                break;
            }
        }
        return param;
    }

    public List<WebServiceField> getFieldsIn()
    {
        return fieldsIn;
    }

    public void setFieldsIn(List<WebServiceField> fieldsIn)
    {
        this.fieldsIn = fieldsIn;
    }

    public boolean hasFieldsIn()
    {
        return fieldsIn != null && !fieldsIn.isEmpty();
    }
    public void addFieldIn(WebServiceField field)
    {
    	fieldsIn.add(field);
    }
    
    public List<WebServiceField> getFieldsOut()
    {
        return fieldsOut;
    }

    public void setFieldsOut(List<WebServiceField> fieldsOut)
    {
        this.fieldsOut = fieldsOut;
    }
    public void addFieldOut(WebServiceField field)
    {
    	fieldsOut.add(field);
    }

    public String getInFieldArgumentName()
    {
        return inFieldArgumentName;
    }

    public void setInFieldArgumentName(String inFieldArgumentName)
    {
        this.inFieldArgumentName = inFieldArgumentName;
    }

    public String getOutFieldArgumentName()
    {
        return outFieldArgumentName;
    }

    public void setOutFieldArgumentName(String outFieldArgumentName)
    {
        this.outFieldArgumentName = outFieldArgumentName;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public int getCallStep()
    {
        return callStep;
    }

    public void setCallStep(int callStep)
    {
        this.callStep = callStep;
    }

    public String getOperationNamespace()
    {
        return operationNamespace;
    }

    public void setOperationNamespace(String operationNamespace)
    {
        this.operationNamespace = operationNamespace;
    }

	public String getHttpLogin() {
		return httpLogin;
	}

	public void setHttpLogin(String httpLogin) {
		this.httpLogin = httpLogin;
	}

	public String getHttpPassword() {
		return httpPassword;
	}

	public void setHttpPassword(String httpPassword) {
		this.httpPassword = httpPassword;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

    public String getInFieldContainerName()
    {
        return inFieldContainerName;
    }

    public void setInFieldContainerName(String inFieldContainerName)
    {
        this.inFieldContainerName = inFieldContainerName;
    }

    public String getOutFieldContainerName()
    {
        return outFieldContainerName;
    }

    public void setOutFieldContainerName(String outFieldContainerName)
    {
        this.outFieldContainerName = outFieldContainerName;
    }
}
