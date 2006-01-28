 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 

package be.ibridge.kettle.core.dialog;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.value.Value;

/**
 * Dialog to enter a Kettle Value
 * 
 * @author Matt
 * @since 01-11-2004
 * 
 */
public class EnterValueDialog extends Dialog
{
	private Display      display; 

	/*
	 * Type of Value: String, Number, Date, Boolean, Integer
	 */
	private Label        wlValueType;
	private CCombo       wValueType;
    private FormData     fdlValueType, fdValueType;

    private Label        wlInputString;
    private Text         wInputString;
    private FormData     fdlInputString, fdInputString;

	private Label        wlFormat;
	private CCombo       wFormat;
    private FormData     fdlFormat, fdFormat;
    
    private Label        wlLength;
    private Text         wLength;
    private FormData     fdlLength, fdLength;

    private Label        wlPrecision;
    private Text         wPrecision;
    private FormData     fdlPrecision, fdPrecision;

	private Button wOK, wCancel, wTest;
	private FormData fdOK, fdCancel, fdTest;
	private Listener lsOK, lsCancel, lsTest;

	private Shell  shell;
	private SelectionAdapter lsDef;
	private Props props;
	private Value value;
	
	
	private NumberFormat nf;
	private DecimalFormat df;
	private SimpleDateFormat daf;

	public EnterValueDialog(Shell parent, int style, Props props, Value value)
	{
		super(parent, style);
		this.props = props;
		this.value = value;
		
		nf = NumberFormat.getInstance();
		df = (DecimalFormat)nf;
		daf = new SimpleDateFormat();
	}

	public Value open()
	{
		Shell parent = getParent();
		display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE );
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Enter a value");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
		
		// Type of value
		wlValueType=new Label(shell, SWT.RIGHT);
		wlValueType.setText("Type ");
 		props.setLook(wlValueType);
		fdlValueType=new FormData();
		fdlValueType.left = new FormAttachment(0, 0);
		fdlValueType.right= new FormAttachment(middle, -margin);
		fdlValueType.top  = new FormAttachment(0, margin);
		wlValueType.setLayoutData(fdlValueType);
		wValueType=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		wValueType.setItems(Value.getTypes());
 		props.setLook(wValueType);
		fdValueType=new FormData();
		fdValueType.left = new FormAttachment(middle, 0);
		fdValueType.top  = new FormAttachment(0, margin);
		fdValueType.right= new FormAttachment(100, -margin);
		wValueType.setLayoutData(fdValueType);
		wValueType.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setFormats();
			}
		});

		
		// Iconsize line
		wlInputString=new Label(shell, SWT.RIGHT);
		wlInputString.setText("Value ");
 		props.setLook(wlInputString);
		fdlInputString=new FormData();
		fdlInputString.left = new FormAttachment(0, 0);
		fdlInputString.right= new FormAttachment(middle, -margin);
		fdlInputString.top  = new FormAttachment(wValueType, margin);
		wlInputString.setLayoutData(fdlInputString);
		wInputString=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInputString);
		fdInputString=new FormData();
		fdInputString.left = new FormAttachment(middle, 0);
		fdInputString.top  = new FormAttachment(wValueType, margin);
		fdInputString.right= new FormAttachment(100, -margin);
		wInputString.setLayoutData(fdInputString);

		// Format mask
		wlFormat=new Label(shell, SWT.RIGHT);
		wlFormat.setText("Conversion format ");
 		props.setLook(wlFormat);
		fdlFormat=new FormData();
		fdlFormat.left = new FormAttachment(0, 0);
		fdlFormat.right= new FormAttachment(middle, -margin);
		fdlFormat.top  = new FormAttachment(wInputString, margin);
		wlFormat.setLayoutData(fdlFormat);
		wFormat=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFormat);
		fdFormat=new FormData();
		fdFormat.left = new FormAttachment(middle, 0);
		fdFormat.right= new FormAttachment(100, -margin);
		fdFormat.top  = new FormAttachment(wInputString, margin);
		wFormat.setLayoutData(fdFormat);

		// Length line
		wlLength=new Label(shell, SWT.RIGHT);
		wlLength.setText("Length ");
 		props.setLook(wlLength);
		fdlLength=new FormData();
		fdlLength.left = new FormAttachment(0, 0);
		fdlLength.right= new FormAttachment(middle, -margin);
		fdlLength.top  = new FormAttachment(wFormat, margin);
		wlLength.setLayoutData(fdlLength);
		wLength=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLength);
		fdLength=new FormData();
		fdLength.left = new FormAttachment(middle, 0);
		fdLength.right= new FormAttachment(100, -margin);
		fdLength.top  = new FormAttachment(wFormat, margin);
		wLength.setLayoutData(fdLength);
		
		// Precision line
		wlPrecision=new Label(shell, SWT.RIGHT);
		wlPrecision.setText("Precision ");
 		props.setLook(wlPrecision);
		fdlPrecision=new FormData();
		fdlPrecision.left = new FormAttachment(0, 0);
		fdlPrecision.right= new FormAttachment(middle, -margin);
		fdlPrecision.top  = new FormAttachment(wLength, margin);
		wlPrecision.setLayoutData(fdlPrecision);
		wPrecision=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wPrecision);
		fdPrecision=new FormData();
		fdPrecision.left = new FormAttachment(middle, 0);
		fdPrecision.right= new FormAttachment(100, -margin);
		fdPrecision.top  = new FormAttachment(wLength, margin);
		wPrecision.setLayoutData(fdPrecision);



		// Some buttons
		wOK=new Button(shell, SWT.PUSH );
		wOK.setText("  &OK  ");
		wTest=new Button(shell, SWT.PUSH );
		wTest.setText("  &Test ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");

		fdOK=new FormData();
		fdOK.left       = new FormAttachment(25, 0);
		fdOK.top        = new FormAttachment(wPrecision, margin*2);
		wOK.setLayoutData(fdOK);
		fdTest=new FormData();
		fdTest.left       = new FormAttachment(50, 0);
		fdTest.top        = new FormAttachment(wPrecision, margin*2);
		wTest.setLayoutData(fdTest);
		fdCancel=new FormData();
		fdCancel.left   = new FormAttachment(75, 0);
		fdCancel.top    = new FormAttachment(wPrecision, margin*2);
		wCancel.setLayoutData(fdCancel);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsTest     = new Listener() { public void handleEvent(Event e) { test();   } };
		
		wCancel.addListener(SWT.Selection, lsCancel );
		wOK.addListener    (SWT.Selection, lsOK     );
		wTest.addListener  (SWT.Selection, lsTest   );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wInputString.addSelectionListener  (lsDef);
		wLength.addSelectionListener       (lsDef);
		wPrecision.addSelectionListener    (lsDef);
		
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		getData();

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return value;
	}

	public void dispose()
	{
		shell.dispose();
	}
	
	public void getData()
	{
		wValueType.setText(value.getTypeDesc());
		if (value.getString()!=null) wInputString.setText(value.toString());
		setFormats();
		
		if (value.isNumber())
		{
			wFormat.setText(Const.numberFormats[0]);
		}
		if (value.isDate())
		{
			wFormat.setText(Const.dateFormats[0]);
		}
		
		wLength.setText(""+value.getLength());
		wPrecision.setText(""+value.getPrecision());
		
		wInputString.setFocus();
		wInputString.selectAll();
	}
	
	public void setFormats()
	{
		wFormat.removeAll();
		int valtype = Value.getType( wValueType.getText() );
		switch(valtype)
		{
		case Value.VALUE_TYPE_NUMBER:
			
			for (int i=0;i<Const.numberFormats.length;i++) 
				wFormat.add(Const.numberFormats[i]);
			break;
		case Value.VALUE_TYPE_DATE:
			for (int i=0;i<Const.dateFormats.length;i++) 
				wFormat.add(Const.dateFormats[i]);
			break;
		case Value.VALUE_TYPE_STRING  : 
		case Value.VALUE_TYPE_BOOLEAN : 
		case Value.VALUE_TYPE_INTEGER : 
		default                       : break;
		}
	}
	
	private void cancel()
	{
		props.setScreen(new WindowProperty(shell));
		value=null;
		dispose();
	}
	
	private Value getValue(String valuename)
	{
		int valtype = Value.getType(wValueType.getText()); 
		Value val = new Value(valuename, wInputString.getText());

		switch(valtype)
		{
		case Value.VALUE_TYPE_NUMBER: 
			try
			{
				df.applyPattern(wFormat.getText());
				double d = df.parse(Const.trim( wInputString.getText() )).doubleValue() ;
				val.setValue( d );
			}
			catch(ParseException e)
			{
				val.setType(valtype);
				val.setNull();
				val.setOrigin(e.toString());
			}
			break;
		case Value.VALUE_TYPE_DATE:
			try
			{
				val.trim();
				daf.applyPattern(wFormat.getText());
				val.setValue( daf.parse(wInputString.getText()) );
			}
			catch(ParseException e)
			{
				val.setType(valtype);
				val.setNull();
				val.setOrigin(e.toString());
			}
			break;
		case Value.VALUE_TYPE_STRING  : 
			break;
		case Value.VALUE_TYPE_BOOLEAN :
			val.trim();
			val.setValue(val.getBoolean());
			break;
		case Value.VALUE_TYPE_INTEGER : 
			val.trim();
			val.setValue(val.getInteger());
			break;
        case Value.VALUE_TYPE_BIGNUMBER: 
            val.trim();
            val.setValue(val.getBigNumber());
            break;
		default: break;
		}
		
		int length    = Const.toInt( wLength.getText(), -1);
		int precision = Const.toInt( wPrecision.getText(), -1);
		val.setLength(length, precision);

		return val;
	}
	
	private void ok()
	{
		value = getValue(value.getName()); // Keep the same name...
		dispose();
	}
	
	/**
	 * Test the entered value
	 *
	 */
	public void test()
	{
		Value v = getValue(value.getName());
		MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
		mb.setMessage("The resulting value is : "+Const.CR+Const.CR+"    "+v.toString()+Const.CR+"    "+v.toStringMeta()+(v.getOrigin()!=null?Const.CR+"    "+v.getOrigin():""));
		mb.setText("Value");
		mb.open();
	}
}
