package org.pentaho.di.ui.trans.steps.elasticsearchbulk;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.PropsUI;

/**
 * Label, numeric text input and drop down time unit selector
 * 
 */
public class LabelTimeComposite extends Composite {
  private static final PropsUI props = PropsUI.getInstance();

  private Label wLabel;
  private Text wText;
  private CCombo wTimeUnit;

  private String lastValidValue = "";

  public LabelTimeComposite(Composite composite, String labelText, String toolTipText) {
    super(composite, SWT.NONE);
    props.setLook(this);

    int middle = props.getMiddlePct();
    int threeQuarters = (middle + 100) / 2;
    int margin = Const.MARGIN;

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 0;
    formLayout.marginHeight = 0;
    formLayout.marginTop = 0;
    formLayout.marginBottom = 0;

    this.setLayout(formLayout);

    wText = new Text(this, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    FormData fdText = new FormData();
    fdText.left = new FormAttachment(middle, margin);
    fdText.right = new FormAttachment(threeQuarters, 0);
    wText.setLayoutData(fdText);
    wText.setToolTipText(toolTipText);

    wTimeUnit = new CCombo(this, SWT.SINGLE | SWT.DROP_DOWN | SWT.BORDER | SWT.LEFT);
    FormData fdCombo = new FormData();
    fdCombo.left = new FormAttachment(threeQuarters, margin);
    fdCombo.right = new FormAttachment(100, 0);
    wTimeUnit.setEditable(false);
    wTimeUnit.setLayoutData(fdCombo);
    wTimeUnit.setItems(getTimeUnits());
    wTimeUnit.setToolTipText(toolTipText);

    wLabel = new Label(this, SWT.RIGHT);
    props.setLook(wLabel);
    wLabel.setText(labelText);
    FormData fdLabel = new FormData();
    fdLabel.left = new FormAttachment(0, 0);
    fdLabel.right = new FormAttachment(middle, 0);
    fdLabel.top = new FormAttachment(wText, 0, SWT.CENTER);
    wLabel.setLayoutData(fdLabel);
    wLabel.setToolTipText(toolTipText);

    wText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        if (!StringUtils.isNumeric(wText.getText())) {
          wText.setText(lastValidValue);
        } else lastValidValue = wText.getText();
      }
    });
  }

  private String[] getTimeUnits() {
    ArrayList<String> timeUnits = new ArrayList<String>();
    for (TimeUnit timeUnit : TimeUnit.values()) {
      timeUnits.add(timeUnit.toString());
    }
    return timeUnits.toArray(new String[timeUnits.size()]);
  }

  public TimeUnit getTimeUnit() {
    return TimeUnit.valueOf(wTimeUnit.getItem(wTimeUnit.getSelectionIndex()));
  }

  public void setTimeUnit(TimeUnit tu) {
    for (int i = 0; i < wTimeUnit.getItemCount(); i++) {
      if (tu.toString().equals(wTimeUnit.getItem(i))) {
        wTimeUnit.select(i);
        break;
      }
    }
  }

  public void addModifyListener(ModifyListener lsMod) {
    wText.addModifyListener(lsMod);
  }

  public void addSelectionListener(SelectionAdapter lsDef) {
    wText.addSelectionListener(lsDef);
  }

  public void setText(String name) {
    wText.setText(name);
  }

  public String getText() {
    return wText.getText();
  }

  public void setEchoChar(char c) {
    wText.setEchoChar(c);
  }

  public void setEnabled(boolean flag) {
    wText.setEnabled(flag);
    wLabel.setEnabled(flag);
  }

  public boolean setFocus() {
    return wText.setFocus();
  }

  public void addTraverseListener(TraverseListener tl) {
    wText.addTraverseListener(tl);
  }

  public Text getTextWidget() {
    return wText;
  }

  public Label getLabelWidget() {
    return wLabel;
  }
}