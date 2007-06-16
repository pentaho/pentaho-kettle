package be.ibridge.kettle.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;

/**
 * Adds a line of text with a label and a variable to a composite (like a dialog shell)
 * 
 * @author Matt
 * @since 17-may-2006
 *
 */
public class LabelText extends Composite
{
    private static final Props props = Props.getInstance();
    
    private Label wLabel;
    private Text  wText;

    public LabelText(Composite composite, String labelText, String toolTipText)
    {
        this(composite, labelText, toolTipText, props.getMiddlePct(), Const.MARGIN);
    }

    public LabelText(Composite composite, String labelText, String toolTipText, int middle, int margin)
    {
        super(composite, SWT.NONE);
        props.setLook(this);
        
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth  = 0;
        formLayout.marginHeight = 0;
        this.setLayout(formLayout);
        
        wText = new Text(this, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        FormData fdText = new FormData();
        fdText.left = new FormAttachment(middle, margin);
        fdText.right= new FormAttachment(100, 0);
        wText.setLayoutData(fdText);
        wText.setToolTipText(toolTipText);

        wLabel = new Label(this, SWT.RIGHT);
        props.setLook(wLabel);
        wLabel.setText(labelText);
        FormData fdLabel = new FormData();
        fdLabel.left = new FormAttachment(0,0);
        fdLabel.right= new FormAttachment(middle, 0);
        fdLabel.top  = new FormAttachment(wText, 0, SWT.CENTER);
        wLabel.setLayoutData(fdLabel);
        wLabel.setToolTipText(toolTipText);
    }
    
    public String getText()
    {
        return wText.getText();
    }
    
    public void setText(String string)
    {
        wText.setText(string);
    }
    
    public Text getTextWidget()
    {
        return wText;
    }

    public void addModifyListener(ModifyListener lsMod)
    {
        wText.addModifyListener(lsMod);
    }

    public void addSelectionListener(SelectionListener lsDef)
    {
        wText.addSelectionListener(lsDef);
    }
    
    public void setEnabled(boolean flag)
    {
        wText.setEnabled(flag);
        wLabel.setEnabled(flag);
    }
}
