package be.ibridge.kettle.trans.step.webservices;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.webservices.ui.BodetPluginSWTPane;
import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLParameter;

public class BodetPluginMeta extends BaseStepMeta implements StepMetaInterface
{

    /** Champs disponible dans le web services en entrées */
    // TODO Voir avec David si c'est normal que ce soit des String et pas des types plus élaborés
    protected List/*<WSDLParameter>*/ fieldInWebService;

    /** Paramètres disponible dans le web services en entrées */
    // TODO Voir avec David si c'est normal que ce soit des String et pas des types plus élaborés
    protected List/*<String>*/ valueInWebService;

    /** Ca ne sert à rien mis à part qu'il faut un field pour détecter les variables utilisées dans la méta
     * c.f. code dans StringSearcher.findMetaData
     * on a également besoin de l'accesseur qui va bien */
    // @SuppressWarnings("unused")
    // private String[] usedVariables;

    /** Champs disponible dans le web services en sortie */
    // TODO Voir avec David si c'est normal que ce soit des String et pas des types plus élaborés
    protected List/*<WSDLParameter>*/ fieldOutWebService;

    /** Url du webService */
    protected String urlWebService;

    /** Nom de l'opération utilisé dans le web service */
    private String operationNameWebService;

    private String inFieldArgumentNameWebService;

    private String inValueArgumentNameWebService;

    private String outFieldArgumentNameWebService;

    /** Liste des liens entre les champs de l'étape précédente */
    protected List/*<FieldLinkWebServiceField>*/ fieldInLinkWebServiceFieldList;

    /** Liste des liens entre les valeurs en entrée de l'étape courante et les champs du web services*/
    protected List/*<FieldLinkWebServiceField>*/ valueInLinkWebServiceFieldList;

    /** Liste des liens entres les champs de l'étape courante et l'étape suivante */
    protected List/*<FieldLinkWebServiceField>*/ fieldOutLinkWebServiceFieldList;

    public BodetPluginMeta()
    {
        super();
        fieldInWebService = new ArrayList/*<WSDLParameter>*/();
        valueInWebService = new ArrayList/*<String>*/();
        fieldOutWebService = new ArrayList/*<WSDLParameter>*/();
        fieldInLinkWebServiceFieldList = new ArrayList/*<FieldLinkWebServiceField>*/();
        valueInLinkWebServiceFieldList = new ArrayList/*<FieldLinkWebServiceField>*/();
        fieldOutLinkWebServiceFieldList = new ArrayList/*<FieldLinkWebServiceField>*/();
    }

    public BodetPluginMeta(Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException
    {
        this();
        loadXML(stepnode, databases, counters);
    }

    public BodetPluginMeta(Repository rep, long id_step, ArrayList databases, Hashtable counters) throws KettleException
    {
        this();
        readRep(rep, id_step, databases, counters);
    }

    public Row getFields(Row r, String name, Row info)
    {
        for (Iterator iter = fieldOutLinkWebServiceFieldList.iterator(); iter.hasNext();)
        {
            FieldLinkWebServiceField field = (FieldLinkWebServiceField) iter.next();
            Value vValue = new Value(field.getField().getName(), field.getField().getType());
            r.addValue(vValue);
        }
        return r;
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

    // @SuppressWarnings("unchecked")
    public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
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
        else
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
        retval.append("    " + XMLHandler.addTagValue("wsURL", urlWebService));

        //Sauvegarde de l'opération 
        retval.append("    " + XMLHandler.addTagValue("wsOperation", operationNameWebService));
        retval.append("    " + XMLHandler.addTagValue("wsInFieldArgument", inFieldArgumentNameWebService));
        retval.append("    " + XMLHandler.addTagValue("wsOutFieldArgument", outFieldArgumentNameWebService));
        retval.append("    " + XMLHandler.addTagValue("wsValueFieldArgument", inValueArgumentNameWebService));

        //SAUVEGARDE DU PARAMETRAGE DES DONNEES EN ENTREES

        //Sauvegarde du lien entre les champs de l'étape précédente et l'URL du WebService
        retval.append("    <fieldsIn>" + Const.CR);
        for (int i = 0; i < fieldInLinkWebServiceFieldList.size(); i++)
        {
            FieldLinkWebServiceField vField = (FieldLinkWebServiceField) fieldInLinkWebServiceFieldList.get(i);
            retval.append("    <field>" + Const.CR);
            retval.append("        " + XMLHandler.addTagValue("name", vField.getField().getName()));
            retval.append("        " + XMLHandler.addTagValue("matching", vField.getWebServiceField()));
            retval.append("    </field>" + Const.CR);
        }
        retval.append("      </fieldsIn>" + Const.CR);

        //Sauvegarde des champs du web services
        retval.append("    <wsfieldsIn>" + Const.CR);
        for (int i = 0; i < fieldInWebService.size(); ++i)
        {
            retval.append("    <field>" + Const.CR);
            retval.append("        " + XMLHandler.addTagValue("name", ((WSDLParameter)fieldInWebService.get(i)).getName()));
            retval.append("        " + XMLHandler.addTagValue("type", ((WSDLParameter)fieldInWebService.get(i)).getType()));
            retval.append("    </field>" + Const.CR);
        }
        retval.append("    </wsfieldsIn>" + Const.CR);

        //sauvegardes des paramètres uniques
        retval.append("    <valuesIn>" + Const.CR);
        for (int i = 0; i < valueInLinkWebServiceFieldList.size(); i++)
        {
            FieldLinkWebServiceField vField = (FieldLinkWebServiceField) valueInLinkWebServiceFieldList.get(i);
            retval.append("    <field>" + Const.CR);
            retval.append("        " + XMLHandler.addTagValue("name", vField.getField().getName()));
            retval.append("        " + XMLHandler.addTagValue("value", vField.getField().getString()));
            retval.append("        " + XMLHandler.addTagValue("matching", vField.getWebServiceField()));
            retval.append("    </field>" + Const.CR);
        }
        retval.append("      </valuesIn>" + Const.CR);

        //Sauvegarde des champs du web services
        retval.append("    <wsfieldsOut>" + Const.CR);
        for (int i = 0; i < fieldOutWebService.size(); ++i)
        {
            retval.append("    <field>" + Const.CR);
            retval.append("        " + XMLHandler.addTagValue("name", ((WSDLParameter)fieldOutWebService.get(i)).getName()));
            retval.append("        " + XMLHandler.addTagValue("type", ((WSDLParameter)fieldOutWebService.get(i)).getType()));
            retval.append("    </field>" + Const.CR);
        }
        retval.append("    </wsfieldsOut>" + Const.CR);

        //SAUVEGARDE DU PARAMETRAGE DES DONNES EN SORTIES
        retval.append("    <fieldsOut>" + Const.CR);
        for (int i = 0; i < fieldOutLinkWebServiceFieldList.size(); i++)
        {
            FieldLinkWebServiceField vField = (FieldLinkWebServiceField) fieldOutLinkWebServiceFieldList.get(i);
            retval.append("    <field>" + Const.CR);
            retval.append("        " + XMLHandler.addTagValue("name", vField.getField().getName()));
            retval.append("        " + XMLHandler.addTagValue("matching", vField.getWebServiceField()));
            retval.append("    </field>" + Const.CR);
        }
        retval.append("      </fieldsOut>" + Const.CR);

        return retval.toString();
    }

    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException
    {
        //Chargement de l'Url
        urlWebService = XMLHandler.getTagValue(stepnode, "wsURL");

        //Chargement de l'opération
        operationNameWebService = XMLHandler.getTagValue(stepnode, "wsOperation");
        inFieldArgumentNameWebService = XMLHandler.getTagValue(stepnode, "wsInFieldArgument");
        outFieldArgumentNameWebService = XMLHandler.getTagValue(stepnode, "wsOutFieldArgument");

        //CHARGEMENT DES DONNEES EN ENTREES

        //Chargement des champs disponible dans l'étape précédente et de leur matching
        fieldInLinkWebServiceFieldList.clear();
        Node fields = XMLHandler.getSubNode(stepnode, "fieldsIn");
        int nrfields = XMLHandler.countNodes(fields, "field");

        for (int i = 0; i < nrfields; ++i)
        {
            Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

            Value vValue = new Value();
            vValue.setName(XMLHandler.getTagValue(fnode, "name"));
            FieldLinkWebServiceField vField = new FieldLinkWebServiceField(vValue, XMLHandler.getTagValue(fnode, "matching"));
            fieldInLinkWebServiceFieldList.add(vField);

        }

        //Chargement des champs disponible dans le web service
        fieldInWebService.clear();
        fields = XMLHandler.getSubNode(stepnode, "wsfieldsIn");
        nrfields = XMLHandler.countNodes(fields, "field");

        for (int i = 0; i < nrfields; ++i)
        {
            Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
            fieldInWebService.add(new WSDLParameter(XMLHandler.getTagValue(fnode, "name"), XMLHandler.getTagValue(fnode, "type")));
        }

        valueInLinkWebServiceFieldList.clear();
        valueInWebService.clear();
        fields = XMLHandler.getSubNode(stepnode, "valuesIn");
        nrfields = XMLHandler.countNodes(fields, "field");

        for (int i = 0; i < nrfields; ++i)
        {
            Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

            Value vValue = new Value();
            vValue.setName(XMLHandler.getTagValue(fnode, "name"));
            vValue.setValue((String) XMLHandler.getTagValue(fnode, "value"));
            FieldLinkWebServiceField vField = new FieldLinkWebServiceField(vValue, XMLHandler.getTagValue(fnode, "matching"));
            valueInLinkWebServiceFieldList.add(vField);
            valueInWebService.add(vField.getWebServiceField());

        }

        //Chargement des champs disponible dans le web service
        fieldOutWebService.clear();
        fields = XMLHandler.getSubNode(stepnode, "wsfieldsOut");
        nrfields = XMLHandler.countNodes(fields, "field");

        for (int i = 0; i < nrfields; ++i)
        {
            Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
            fieldOutWebService.add(new WSDLParameter(XMLHandler.getTagValue(fnode, "name"), XMLHandler.getTagValue(fnode, "type")));
        }

        //CHARGEMENT DES DONNES EN SORTIES
        fieldOutLinkWebServiceFieldList.clear();

        //Chargement des champs disponibles dans l'étape suivante
        fields = XMLHandler.getSubNode(stepnode, "fieldsOut");
        nrfields = XMLHandler.countNodes(fields, "field");

        for (int i = 0; i < nrfields; ++i)
        {
            Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

            Value vValue = new Value();
            String wsName = XMLHandler.getTagValue(fnode, "matching");
            vValue.setName(XMLHandler.getTagValue(fnode, "name"));
            vValue.setType(getWsOutField(wsName)!=null ? getWsOutField(wsName).getValueType() : Value.VALUE_TYPE_STRING);
            fieldOutLinkWebServiceFieldList.add(new FieldLinkWebServiceField(vValue, wsName));
        }
    }

    public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters) throws KettleException
    {
        //chargement de l'url
        urlWebService = rep.getStepAttributeString(id_step, "wsUrl");

        //Chargement de l'opération du web services
        operationNameWebService = rep.getStepAttributeString(id_step, "wsOperation");
        inFieldArgumentNameWebService = rep.getStepAttributeString(id_step, "wsInFieldArgument");
        outFieldArgumentNameWebService = rep.getStepAttributeString(id_step, "wsOutFieldArgument");
        inValueArgumentNameWebService = rep.getStepAttributeString(id_step, "wsInValueArgument");

        //RESTAURATION DU PARAMETRAGE DES DONNEES EN ENTREES

        //Restauration du lien entre les champs de l'étape précédente et l'URL du WebService
        int nb = rep.countNrStepAttributes(id_step, "fieldIn_name");
        fieldInLinkWebServiceFieldList.clear();
        for (int i = 0; i < nb; ++i)
        {
            String name = rep.getStepAttributeString(id_step, i, "fieldIn_name");
            String matching = rep.getStepAttributeString(id_step, i, "fieldIn_matching");
            fieldInLinkWebServiceFieldList.add(new FieldLinkWebServiceField(new Value(name), matching));
        }

        //Restauration des champs du web services
        nb = rep.countNrStepAttributes(id_step, "fieldIn_ws_name");
        fieldInWebService.clear();
        for (int i = 0; i < nb; ++i)
        {
            String name = rep.getStepAttributeString(id_step, i, "fieldIn_ws_name");
            String type = rep.getStepAttributeString(id_step, i, "fieldIn_ws_type");
            fieldInWebService.add(new WSDLParameter(name, type));
        }
        //Restauration des paramètres uniques
        nb = rep.countNrStepAttributes(id_step, "valueIn_name");
        valueInLinkWebServiceFieldList.clear();
        valueInWebService.clear();
        for (int i = 0; i < nb; ++i)
        {
            String name = rep.getStepAttributeString(id_step, i, "valueIn_name");
            String value = rep.getStepAttributeString(id_step, i, "valueIn_value");
            String matching = rep.getStepAttributeString(id_step, i, "valueIn_matching");
            valueInLinkWebServiceFieldList.add(new FieldLinkWebServiceField(new Value(name, value), matching));
            valueInWebService.add(matching);
        }

        //RESTAURATION DU PARAMETRAGE DES DONNES EN SORTIES
        nb = rep.countNrStepAttributes(id_step, "fieldOut_ws_name");
        fieldOutWebService.clear();
        for (int i = 0; i < nb; ++i)
        {
            String name = rep.getStepAttributeString(id_step, i, "fieldOut_ws_name");
            String type = rep.getStepAttributeString(id_step, i, "fieldOut_ws_type");
            fieldOutWebService.add(new WSDLParameter(name, type));
        }

        nb = rep.countNrStepAttributes(id_step, "fieldOut_name");
        fieldOutLinkWebServiceFieldList.clear();
        for (int i = 0; i < nb; ++i)
        {
            String name = rep.getStepAttributeString(id_step, i, "fieldOut_name");
            String matching = rep.getStepAttributeString(id_step, i, "fieldOut_matching");
            fieldOutLinkWebServiceFieldList.add(new FieldLinkWebServiceField(new Value(name), matching));
        }
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {

        //Sauvegarde de l'URL du web services
        rep.saveStepAttribute(id_transformation, id_step, "wsUrl", urlWebService);

        //Sauvegarde de l'opération du web services
        rep.saveStepAttribute(id_transformation, id_step, "wsOperation", operationNameWebService);
        rep.saveStepAttribute(id_transformation, id_step, "wsInFieldArgument", inFieldArgumentNameWebService);
        rep.saveStepAttribute(id_transformation, id_step, "wsOutFieldArgument", outFieldArgumentNameWebService);
        rep.saveStepAttribute(id_transformation, id_step, "wsInValueArgument", inValueArgumentNameWebService);

        //SAUVEGARDE DU PARAMETRAGE DES DONNEES EN ENTREES

        //Sauvegarde du lien entre les champs de l'étape précédente et l'URL du WebService
        for (int i = 0; i < fieldInLinkWebServiceFieldList.size(); i++)
        {
            FieldLinkWebServiceField vField = (FieldLinkWebServiceField) fieldInLinkWebServiceFieldList.get(i);
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldIn_name", vField.getField().getName());
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldIn_matching", vField.getWebServiceField());
        }
        //Sauvegarde des champs du web services 
        for (int i = 0; i < fieldInWebService.size(); ++i)
        {
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldIn_ws_name", ((WSDLParameter)fieldInWebService.get(i)).getName());
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldIn_ws_type", ((WSDLParameter)fieldInWebService.get(i)).getType());
        }

        //sauvegardes des paramètres uniques
        for (int i = 0; i < valueInLinkWebServiceFieldList.size(); i++)
        {
            FieldLinkWebServiceField vField = (FieldLinkWebServiceField) valueInLinkWebServiceFieldList.get(i);
            rep.saveStepAttribute(id_transformation, id_step, i, "valueIn_name", vField.getField().getName());
            rep.saveStepAttribute(id_transformation, id_step, i, "valueIn_value", vField.getField().getString());
            rep.saveStepAttribute(id_transformation, id_step, i, "valueIn_matching", vField.getWebServiceField());
        }

        //SAUVEGARDE DU PARAMETRAGE DES DONNES EN SORTIES
        for (int i = 0; i < fieldOutWebService.size(); ++i)
        {
            WSDLParameter vParameter = (WSDLParameter)fieldOutWebService.get(i);
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldOut_ws_name", vParameter.getName());
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldOut_ws_type", vParameter.getType());
        }

        for (int i = 0; i < fieldOutLinkWebServiceFieldList.size(); i++)
        {
            FieldLinkWebServiceField vField = (FieldLinkWebServiceField) fieldOutLinkWebServiceFieldList.get(i);
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldOut_name", vField.getField().getName());
            rep.saveStepAttribute(id_transformation, id_step, i, "fieldOut_matching", vField.getWebServiceField());
        }
    }

    public List/*<WSDLParameter>*/ getFieldInWebService()
    {
        return fieldInWebService;
    }

    public List/*<WSDLParameter>*/ getFieldOutWebService()
    {
        return fieldOutWebService;
    }

    public String getUrlWebService()
    {
        return urlWebService;
    }

    public void setUrlWebService(String urlWebService)
    {
        this.urlWebService = urlWebService;
    }

    public String getOperationNameWebService()
    {
        return operationNameWebService;
    }

    public void setOperationNameWebService(String operationNameWebService)
    {
        this.operationNameWebService = operationNameWebService;
    }

    public List/*<FieldLinkWebServiceField>*/ getFieldInLinkWebServiceFieldList()
    {
        return fieldInLinkWebServiceFieldList;
    }

    public List/*<FieldLinkWebServiceField>*/ getFieldOutLinkWebServiceFieldList()
    {
        return fieldOutLinkWebServiceFieldList;
    }

    public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name)
    {
        //return new BodetPluginDialog(shell, (BaseStepMeta)meta, transMeta, name);
        return new BodetPluginSWTPane(shell, (BaseStepMeta) meta, transMeta, name);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp)
    {
        return new BodetPlugin(stepMeta, stepDataInterface, cnr, transMeta, disp);
    }

    public StepDataInterface getStepData()
    {
        return new BodetPluginData();
    }

    public List/*<String>*/ getValueInWebService()
    {
        return valueInWebService;
    }

    public String[] getUsedVariables()
    {
        List/*<String>*/ vList = new ArrayList/*<String>*/();
        for (Iterator iter = valueInLinkWebServiceFieldList.iterator(); iter.hasNext();)
        {
            FieldLinkWebServiceField vField = (FieldLinkWebServiceField) iter.next();
            if (!vField.getField().isNull() && vField.getField().getString().startsWith("${"))
            {
                vList.add(vField.getField().getString());
            }
        }
        return (String[])vList.toArray(new String[vList.size()]);
    }

    public List/*<FieldLinkWebServiceField>*/ getValueInLinkWebServiceFieldList()
    {
        return valueInLinkWebServiceFieldList;
    }

    public WSDLParameter getWsOutField(String wsName)
    {
        WSDLParameter param = null;
        for (Iterator iter = fieldOutWebService.iterator(); iter.hasNext();)
        {
            WSDLParameter paramCour = (WSDLParameter) iter.next();
            if (paramCour.getName().equals(wsName))
            {
                param = paramCour;
                break;
            }
        }
        return param;
    }

    public FieldLinkWebServiceField getWsOutLink(String wsName)
    {
        FieldLinkWebServiceField ret = null;
        for (Iterator iter = fieldOutLinkWebServiceFieldList.iterator(); iter.hasNext();)
        {
            FieldLinkWebServiceField cour = (FieldLinkWebServiceField) iter.next();
            if (cour.getWebServiceField().equals(wsName))
            {
                ret = cour;
                break;
            }
        }
        return ret;
    }

    public String getInFieldArgumentNameWebService()
    {
        return inFieldArgumentNameWebService;
    }

    public void setInFieldArgumentNameWebService(String inArgumentNameWebService)
    {
        // System.out.println("------------------------- > setInFieldArgumentNameWebService " + inArgumentNameWebService);
        this.inFieldArgumentNameWebService = inArgumentNameWebService;
    }

    public String getInValueArgumentNameWebService()
    {
        return inValueArgumentNameWebService;
    }

    public void setInValueArgumentNameWebService(String inValueArgumentNameWebService)
    {
        this.inValueArgumentNameWebService = inValueArgumentNameWebService;
    }

    public String getOutFieldArgumentNameWebService()
    {
        return outFieldArgumentNameWebService;
    }

    public void setOutFieldArgumentNameWebService(String outFieldArgumentNameWebService)
    {
        this.outFieldArgumentNameWebService = outFieldArgumentNameWebService;
    }
}
