package org.pentaho.di.ui.imp.rules;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.imp.rules.JobHasDescriptionImportRule;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.imp.rule.ImportRuleCompositeInterface;

public class JobHasDescriptionImportRuleComposite implements ImportRuleCompositeInterface{

  private Text text;
  private Composite composite;
  
  public Composite getComposite(Composite parent, ImportRuleInterface importRule) {
    PropsUI props = PropsUI.getInstance();
    
    composite = new Composite(parent, SWT.NONE);
    props.setLook(composite);
    composite.setLayout(new FillLayout());

    Label label = new Label(composite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    props.setLook(label);
    label.setText("Minimum length: ");

    text = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    props.setLook(text);

    return composite;
  }
  
  public void setCompositeData(ImportRuleInterface importRule) {
    JobHasDescriptionImportRule rule = (JobHasDescriptionImportRule) importRule;
    text.setText(Integer.toString(rule.getMinLength()));
  }
  
  public void getCompositeData(ImportRuleInterface importRule) {
    JobHasDescriptionImportRule rule = (JobHasDescriptionImportRule) importRule;
    rule.setMinLength(Const.toInt(text.getText(), 0));
  }
}

