package be.ibridge.kettle.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.trans.step.textfileinput.VariableButtonListenerFactory;

/**
 * A Widget that combines a Text widget with a Variable button that will insert an Environment variable.
 * The tooltip of the text widget shows the content of the Text widdget with expanded variables.
 * 
 * @author Matt
 * @since 17-may-2006
 */
public class TextVar extends Composite
{
    private static final Props props = Props.getInstance();

    private Text wText;
    private Button wVariable;
    
    public TextVar(Composite arg0, int flags)
    {
        super(arg0, SWT.NONE);
        props.setLook(this);
        
        // int margin = Const.MARGIN;
        
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth  = 0;
        formLayout.marginHeight = 0;
        formLayout.marginTop = 0;
        formLayout.marginBottom = 0;

        this.setLayout(formLayout);

        // add a text field on it...
        wText = new Text(this, flags);
        props.setLook(wText);
        wText.addModifyListener(getModifyListenerTooltipText(wText));
        SelectionAdapter lsVar = VariableButtonListenerFactory.getSelectionAdapter(this, wText);
        wText.addKeyListener(getControlSpaceKeyListener(wText, lsVar));
        
        // add a button...
        wVariable = new Button(this, SWT.PUSH);
        wVariable.setToolTipText("Insert variable (CTRL-SPACE)");
        props.setLook(wVariable);

        // wVariable.setText(Messages.getString("System.Button.Variable")); // $NON-NLS-1$
        wVariable.setText("$"); // $NON-NLS-1$
        wVariable.addSelectionListener(lsVar);
        
        FormData fdVariable = new FormData();
        fdVariable.top    = new FormAttachment(0,0);
        fdVariable.right  = new FormAttachment(100,0);
        wVariable.setLayoutData(fdVariable);

        FormData fdText = new FormData();
        fdText.top   = new FormAttachment(wVariable, 0, SWT.CENTER);
        fdText.left  = new FormAttachment(0,0);
        fdText.right = new FormAttachment(wVariable, 0);
        wText.setLayoutData(fdText);
    }
    
    public static final ModifyListener getModifyListenerTooltipText(final Text textField)
    {
        return new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                if (textField.getEchoChar()=='\0') // Can't show passwords ;-)
                {
                    textField.setToolTipText(StringUtil.environmentSubstitute( textField.getText() ) );
                }
            }
        };
    }
    
    public static final KeyListener getControlSpaceKeyListener(final Text textField, final SelectionListener lsVar)
    {
        return new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                // CTRL-<SPACE> --> Insert a variable
                if ((int)e.character == ' ' && (( e.stateMask&SWT.CONTROL)!=0) && (( e.stateMask&SWT.ALT)==0) ) 
                { 
                    Event event = new Event();
                    event.widget = textField;
                    SelectionEvent selectionEvent = new SelectionEvent(event);
                    lsVar.widgetSelected(selectionEvent);
                };
            }
        };
    }

    /**
     * Get the text in the Text widget
     * @return
     */
    public String getText()
    {
        return wText.getText();
    }
    
    /**
     * @param text the text in the Text widget to set.
     */
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
    
    /**
     * Add a modify listener to the text widget
     * @param modifyListener
     */
    public void addModifyListener(ModifyListener modifyListener)
    {
        wText.addModifyListener(modifyListener);
    }

    public void addSelectionListener(SelectionAdapter lsDef)
    {
        wText.addSelectionListener(lsDef);
    }

    public void setEchoChar(char c)
    {
        wText.setEchoChar(c);
    }
 
    public void setEnabled(boolean flag)
    {
        wText.setEnabled(flag);
        wVariable.setEnabled(flag);
    }
    
    public boolean setFocus()
    {
        return wText.setFocus();
    }
    
    public void addTraverseListener(TraverseListener tl)
    {
        wText.addTraverseListener(tl);
    }
}
