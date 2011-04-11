package org.pentaho.di.imp.rules;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.pentaho.di.imp.rule.ImportValidationResultType;
import org.pentaho.di.imp.rule.ImporterRuleInterface;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.w3c.dom.Node;

public class JobHasNoDisabledHopsImportRule extends BaseImportRule implements ImporterRuleInterface {

  public JobHasNoDisabledHopsImportRule() {
    super();
  }
  
  @Override
  public List<ImportValidationFeedback> verifyRule(Object subject) {
    
    List<ImportValidationFeedback> feedback = new ArrayList<ImportValidationFeedback>();
    
    if (!isEnabled()) return feedback;
    if (!(subject instanceof JobMeta)) return feedback;
    
    JobMeta jobMeta = (JobMeta)subject;

    for (int i=0;i<jobMeta.nrJobHops();i++) {
      JobHopMeta hop = jobMeta.getJobHop(i);
      if (!hop.isEnabled()) {
        feedback.add( new ImportValidationFeedback(this, ImportValidationResultType.ERROR, "There is a disabled hop in the job.") );
      }
    }

    if (feedback.isEmpty()) {
      feedback.add( new ImportValidationFeedback(this, ImportValidationResultType.APPROVAL, "All hops are enabled in this job.") );
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
