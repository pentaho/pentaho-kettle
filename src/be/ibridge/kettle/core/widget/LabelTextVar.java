package be.ibridge.kettle.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;

/**
 * Adds a line of text with a label and a variable to a composite (like a dialog shell)
 * 
 * @author Matt
 * @since 17-may-2006
 *
 */
public class LabelTextVar extends Composite
{
    private static final Props props = Props.getInstance();
    
    private Label wLabel;
    private TextVar wText;
    
    public LabelTextVar(Composite composite, String labelText, String toolTipText)
    {
        super(composite, SWT.NONE);
        props.setLook(this);
        
        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;
        
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth  = 0;
        formLayout.marginHeight = 0;
        this.setLayout(formLayout);
        
        wText = new TextVar(this, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        FormData fdText = new FormData();
        fdText.left = new FormAttachment(middle, 0);
        fdText.right= new FormAttachment(100, 0);
        wText.setLayoutData(fdText);
        wText.getTextWidget().setToolTipText(toolTipText);

        wLabel = new Label(this, SWT.RIGHT);
        props.setLook(wLabel);
        wLabel.setText(labelText);
        FormData fdLabel = new FormData();
        fdLabel.left = new FormAttachment(0,0);
        fdLabel.right= new FormAttachment(middle, -margin);
        fdLabel.top  = new FormAttachment(wText, 0, SWT.CENTER);
        wLabel.setLayoutData(fdLabel);
        wLabel.setToolTipText(toolTipText);
    }
    
}
