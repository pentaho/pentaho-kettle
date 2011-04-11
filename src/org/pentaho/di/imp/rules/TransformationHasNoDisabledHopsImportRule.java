package org.pentaho.di.imp.rules;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.pentaho.di.imp.rule.ImportValidationResultType;
import org.pentaho.di.imp.rule.ImporterRuleInterface;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.w3c.dom.Node;

public class TransformationHasNoDisabledHopsImportRule extends BaseImportRule implements ImporterRuleInterface {

  public TransformationHasNoDisabledHopsImportRule() {
    super();
  }
  
  @Override
  public List<ImportValidationFeedback> verifyRule(Object subject) {
    
    List<ImportValidationFeedback> feedback = new ArrayList<ImportValidationFeedback>();
    
    if (!isEnabled()) return feedback;
    if (!(subject instanceof TransMeta)) return feedback;
    
    TransMeta transMeta = (TransMeta)subject;

    for (int i=0;i<transMeta.nrTransHops();i++) {
      TransHopMeta hop = transMeta.getTransHop(i);
      if (!hop.isEnabled()) {
        feedback.add( new ImportValidationFeedback(this, ImportValidationResultType.ERROR, "There is a disabled hop in the transformation.") );
      }
    }

    if (feedback.isEmpty()) {
      feedback.add( new ImportValidationFeedback(this, ImportValidationResultType.APPROVAL, "All hops are enabled in this transformation.") );
    }
    
    return feedback;
  }

  @Override
  public String getXML() {
    
    StringBuilder xml = new StringBuilder();
    xml.append(XMLHandler.openTag(XML_TAG));

    xml.append(super.getXML()); // id, enabled
        
    xml.append(XMLHandler.closeTag(XML_TAG));
    return xml.toString();
  }

  @Override
  public void loadXML(Node ruleNode) throws KettleException {
    super.loadXML(ruleNode);
  }

}
