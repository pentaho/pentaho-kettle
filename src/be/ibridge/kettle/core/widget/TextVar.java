package be.ibridge.kettle.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.trans.step.textfileinput.VariableButtonListenerFactory;

public class TextVar extends Composite
{
    private Text wText;
    private Button wVariable;
    
    public TextVar(Composite arg0, int arg1, int margin)
    {
        super(arg0, SWT.NONE);
        
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth  = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        this.setLayout(formLayout);

        // add a text field on it...
        wText = new Text(this, arg1);
        wText.addModifyListener(getModifyListenerTooltipText(wText));
        
        // add a button...
        wVariable = new Button(this, SWT.PUSH);
        wVariable.setText(Messages.getString("System.Button.Variable"));
        wVariable.addSelectionListener(VariableButtonListenerFactory.getSelectionAdapter(this, wText));
        
        FormData fdVariable = new FormData();
        fdVariable.top   = new FormAttachment(0,0);
        fdVariable.right = new FormAttachment(100,0);
        wVariable.setLayoutData(fdVariable);

        FormData fdText = new FormData();
        fdText.top   = new FormAttachment(0,0);
        fdText.left  = new FormAttachment(0,0);
        fdText.right = new FormAttachment(wVariable, -margin);
        wText.setLayoutData(fdText);
        
    }
    
    private ModifyListener getModifyListenerTooltipText(final Text textField)
    {
        return new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                textField.setToolTipText(StringUtil.environmentSubstitute( textField.getText() ) );
            }
        };
    }

    public String getText()
    {
        return wText.getText();
    }
    
    public void setText(String text)
    {
        wText.setText(text);
    }
    
    public Text getTextWidget()
    {
        return wText;
    }
 
    public Button getVariableWidget()
    {
        return wVariable;
    }
 
}
