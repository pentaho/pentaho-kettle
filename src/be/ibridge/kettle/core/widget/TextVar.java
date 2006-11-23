package be.ibridge.kettle.core.widget;

import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextControlCreator;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
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
    private String toolTipText;
    
    private static final Props props = Props.getInstance();

    private DecoratedField decoratedField;

    public TextVar(Composite composite, int flags)
    {
        this(composite, flags, null);
    }
    
    public TextVar(Composite composite, int flags, String toolTipText)
    {
        super(composite, SWT.NONE);
        this.toolTipText = toolTipText;
        
        props.setLook(this);
        
        // int margin = Const.MARGIN;
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth  = 0;
        formLayout.marginHeight = 0;
        formLayout.marginTop = 0;
        formLayout.marginBottom = 0;

        this.setLayout(formLayout);

        // add a text field on it...
        decoratedField = new DecoratedField(this, flags, new TextControlCreator());
        Text wText = (Text) decoratedField.getControl();
        props.setLook(wText);
        wText.addModifyListener(getModifyListenerTooltipText(wText));
        SelectionAdapter lsVar = VariableButtonListenerFactory.getSelectionAdapter(this, wText);
        wText.addKeyListener(getControlSpaceKeyListener(wText, lsVar));
        
        // Put some decorations on it...
        FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
        registry.registerFieldDecoration("variable.field", "Enter CTRL-SPACE to insert a variable from a selection", GUIResource.getInstance().getImageVariable());
        decoratedField.addFieldDecoration(registry.getFieldDecoration("variable.field"), SWT.TOP | SWT.RIGHT, false);
        
        FormData fdText = new FormData();
        fdText.top   = new FormAttachment(0, 0);
        fdText.left  = new FormAttachment(0 ,0);
        fdText.right = new FormAttachment(100, 0);
        decoratedField.getLayoutControl().setLayoutData(fdText);
    }
    
    private ModifyListener getModifyListenerTooltipText(final Text textField)
    {
        return new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                if (textField.getEchoChar()=='\0') // Can't show passwords ;-)
                {
                    String tip = textField.getText();
                    if (!Const.isEmpty(tip))
                    {
                        tip+=Const.CR+Const.CR+toolTipText;
                    }
                    else
                    {
                        tip=toolTipText;
                    }
                    textField.setToolTipText(StringUtil.environmentSubstitute( tip ) );
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
     * @return the text in the Text widget   
     */
    public String getText()
    {
        return ((Text) decoratedField.getControl()).getText();
    }
    
    /**
     * @param text the text in the Text widget to set.
     */
    public void setText(String text)
    {
        ((Text) decoratedField.getControl()).setText(text);
    }
    
    public Text getTextWidget()
    {
        return (Text) decoratedField.getControl();
    }
 
    /**
     * Add a modify listener to the text widget
     * @param modifyListener
     */
    public void addModifyListener(ModifyListener modifyListener)
    {
        ((Text)decoratedField.getControl()).addModifyListener(modifyListener);
    }

    public void addSelectionListener(SelectionAdapter lsDef)
    {
        ((Text)decoratedField.getControl()).addSelectionListener(lsDef);
    }

    public void setEchoChar(char c)
    {
        ((Text)decoratedField.getControl()).setEchoChar(c);
    }
 
    public void setEnabled(boolean flag)
    {
        ((Text)decoratedField.getControl()).setEnabled(flag);
    }
    
    public boolean setFocus()
    {
        return ((Text)decoratedField.getControl()).setFocus();
    }
    
    public void addTraverseListener(TraverseListener tl)
    {
        ((Text)decoratedField.getControl()).addTraverseListener(tl);
    }
    
    public void setToolTipText(String toolTipText)
    {
        this.toolTipText = toolTipText;
        ((Text)decoratedField.getControl()).setToolTipText(toolTipText);
    }

    public void setEditable(boolean editable)
    {
        ((Text)decoratedField.getControl()).setEditable(editable);
    }

    public void setSelection(int i)
    {
        ((Text)decoratedField.getControl()).setSelection(i);
    }

    public void selectAll()
    {
        ((Text)decoratedField.getControl()).selectAll();
    }
}
