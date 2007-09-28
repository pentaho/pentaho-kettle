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

 
/*
 * Created on 19-jun-2003
 *
 */

package be.ibridge.kettle.trans.step.scriptvalues;

import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.RowListener;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMetaInterface;


public class ScriptValuesDialog extends BaseStepDialog implements StepDialogInterface
{
	private SashForm     wSash;
	private FormData     fdSash;
	
	private Composite    wTop, wBottom;
	private FormData     fdTop, fdBottom;
	
	private Label        wlScript;
	private Text         wScript;
	private FormData     fdlScript, fdScript;

	private Label        wSeparator;
	private FormData     fdSeparator;
	
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private Label        wlPosition;
	private FormData     fdlPosition;
	
	private Button wVars, wTest;
	private Listener lsVars, lsTest;

	private ScriptValuesMeta input;
	
	/**
	 * Dummy class used for test().
	 */
	public class ScriptValuesDummy implements StepInterface
	{
		public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
			return false;
		}

		public void addRowListener(RowListener rowListener) {
		}

		public void dispose(StepMetaInterface sii, StepDataInterface sdi) {
		}

		public long getErrors() {
			return 0;
		}

		public List getInputRowSets() {
			return null;
		}

		public long getLinesInput() {
			return 0;
		}

		public long getLinesOutput() {
			return 0;
		}

		public long getLinesRead() {
			return 0;
		}

		public long getLinesUpdated() {
			return 0;
		}

		public long getLinesWritten() {
			return 0;
		}

		public List getOutputRowSets() {
			return null;
		}

		public String getPartitionID() {
			return null;
		}

		public Row getRow() throws KettleException {
			return null;
		}

		public List getRowListeners() {
			return null;
		}

		public String getStepID() {
			return null;
		}

		public String getStepname() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean init(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) {
			return false;
		}

		public boolean isAlive() {
			return false;
		}

		public boolean isPartitioned() {
			return false;
		}

		public boolean isStopped() {
			return false;
		}

		public void markStart() {
		}

		public void markStop() {
		}

		public void putRow(Row row) throws KettleException {
		}

		public void removeRowListener(RowListener rowListener) {
		}

		public void run() {
		}

		public void setErrors(long errors) {
		}

		public void setOutputDone() {
		}

		public void setPartitionID(String partitionID) {
		}

		public void start() {
		}

		public void stopAll() {
		}

		public void stopRunning(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) throws KettleException {
		}	
	}

	public ScriptValuesDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(ScriptValuesMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("ScriptValuesDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("ScriptValuesDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		wSash = new SashForm(shell, SWT.VERTICAL );
 		props.setLook(wSash);
		
		// Top sash form
		//
		wTop = new Composite(wSash, SWT.NONE);
 		props.setLook(wTop);

		FormLayout topLayout  = new FormLayout ();
		topLayout.marginWidth  = Const.FORM_MARGIN;
		topLayout.marginHeight = Const.FORM_MARGIN;
		wTop.setLayout(topLayout);
		
		// Script line
		wlScript=new Label(wTop, SWT.NONE);
		wlScript.setText(Messages.getString("ScriptValuesDialog.Javascript.Label")); //$NON-NLS-1$
 		props.setLook(wlScript);
		fdlScript=new FormData();
		fdlScript.left = new FormAttachment(0, 0);
		fdlScript.top  = new FormAttachment(0, 0);
		wlScript.setLayoutData(fdlScript);
		wScript=new Text(wTop, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		wScript.setText(Messages.getString("ScriptValuesDialog.Script.Label")); //$NON-NLS-1$
 		props.setLook(wScript, Props.WIDGET_STYLE_FIXED);
		wScript.addModifyListener(lsMod);
		fdScript=new FormData();
		fdScript.left   = new FormAttachment(0, 0);
		fdScript.top    = new FormAttachment(wlScript, margin);
		fdScript.right  = new FormAttachment(100, -5);
		fdScript.bottom = new FormAttachment(100, -30);
		wScript.setLayoutData(fdScript);

		wlPosition=new Label(wTop, SWT.NONE);
		wlPosition.setText(Messages.getString("ScriptValuesDialog.Position.Label")); //$NON-NLS-1$
 		props.setLook(wlPosition);
		fdlPosition=new FormData();
		fdlPosition.left  = new FormAttachment(0, 0);
		fdlPosition.right = new FormAttachment(100, 0);
		fdlPosition.top   = new FormAttachment(wScript, margin);
		wlPosition.setLayoutData(fdlPosition);

		fdTop=new FormData();
		fdTop.left  = new FormAttachment(0, 0);
		fdTop.top   = new FormAttachment(0, 0);
		fdTop.right = new FormAttachment(100, 0);
		fdTop.bottom= new FormAttachment(100, 0);
		wTop.setLayoutData(fdTop);
		
		wBottom = new Composite(wSash, SWT.NONE);
 		props.setLook(wBottom);
		
		FormLayout bottomLayout  = new FormLayout ();
		bottomLayout.marginWidth  = Const.FORM_MARGIN;
		bottomLayout.marginHeight = Const.FORM_MARGIN;
		wBottom.setLayout(bottomLayout);
		
		wSeparator = new Label(wBottom, SWT.SEPARATOR | SWT.HORIZONTAL);
		fdSeparator= new FormData();
		fdSeparator.left  = new FormAttachment(0, 0);
		fdSeparator.right = new FormAttachment(100, 0);
		fdSeparator.top   = new FormAttachment(0, -margin+2);
		wSeparator.setLayoutData(fdSeparator);
		
		wlFields=new Label(wBottom, SWT.NONE);
		wlFields.setText(Messages.getString("ScriptValuesDialog.Fields.Label")); //$NON-NLS-1$
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wSeparator, 0);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsRows=input.getName().length;
		
		ColumnInfo[] colinf=new ColumnInfo[]
           {
    		 new ColumnInfo(Messages.getString("ScriptValuesDialog.ColumnInfo.Filename"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
    		 new ColumnInfo(Messages.getString("ScriptValuesDialog.ColumnInfo.RenameTo"),  ColumnInfo.COLUMN_TYPE_TEXT,   false ), //$NON-NLS-1$
    		 new ColumnInfo(Messages.getString("ScriptValuesDialog.ColumnInfo.Type"),       ColumnInfo.COLUMN_TYPE_CCOMBO, Value.getTypes() ), //$NON-NLS-1$
    		 new ColumnInfo(Messages.getString("ScriptValuesDialog.ColumnInfo.Length"),     ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
    		 new ColumnInfo(Messages.getString("ScriptValuesDialog.ColumnInfo.Precision"),  ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
           };
		
		wFields=new TableView(wBottom, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  lsMod,
							  props
							  );
		
		fdFields=new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top  = new FormAttachment(wlFields, margin);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		wFields.setLayoutData(fdFields);

		fdBottom=new FormData();
		fdBottom.left  = new FormAttachment(0, 0);
		fdBottom.top   = new FormAttachment(0, 0);
		fdBottom.right = new FormAttachment(100, 0);
		fdBottom.bottom= new FormAttachment(100, 0);
		wBottom.setLayoutData(fdBottom);

		fdSash = new FormData();
		fdSash.left  = new FormAttachment(0, 0);
		fdSash.top   = new FormAttachment(wStepname, 0);
		fdSash.right = new FormAttachment(100, 0);
		fdSash.bottom= new FormAttachment(100, -50);
		wSash.setLayoutData(fdSash);
		
		wSash.setWeights(new int[] {60,40});

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("ScriptValuesDialog.InsertFiels.Button")); //$NON-NLS-1$
		wVars=new Button(shell, SWT.PUSH);
		wVars.setText(Messages.getString("ScriptValuesDialog.GetVariables.Button")); //$NON-NLS-1$
		wTest=new Button(shell, SWT.PUSH);
		wTest.setText(Messages.getString("ScriptValuesDialog.TestScript.Button")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wGet, wVars, wTest, wCancel }, margin, null);


		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();          } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();             } };
		lsTest     = new Listener() { public void handleEvent(Event e) { test(false, true); } };
		lsVars     = new Listener() { public void handleEvent(Event e) { test(true, true);  } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();              } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wGet.addListener   (SWT.Selection, lsGet   );
		wTest.addListener (SWT.Selection, lsTest  );
		wVars.addListener  (SWT.Selection, lsVars  );
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
				
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wScript.addKeyListener(new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) { setPosition(); }
			    public void keyReleased(KeyEvent e) { setPosition(); }
			} 
		);
		wScript.addFocusListener(new FocusAdapter() 
			{
				public void focusGained(FocusEvent e) { setPosition(); }
				public void focusLost(FocusEvent e) { setPosition(); }
			}
		);
		wScript.addMouseListener(new MouseAdapter() 
			{
				public void mouseDoubleClick(MouseEvent e) { setPosition(); }
				public void mouseDown(MouseEvent e) { setPosition(); }
				public void mouseUp(MouseEvent e) { setPosition(); }
			}
		);
		
		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	public void setPosition()
	{	
		String scr = wScript.getText();
		
		int linenr = wScript.getCaretLineNumber()+1;
		int posnr  = wScript.getCaretPosition();
		
		// Go back from position to last CR: how many positions?
		int colnr=0;
		while (posnr>0 && scr.charAt(posnr-1)!='\n' && scr.charAt(posnr-1)!='\r')
		{
			posnr--;
			colnr++;
		}
		wlPosition.setText(Messages.getString("ScriptValuesDialog.Position.Label2")+linenr+", "+colnr); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		for (int i=0;i<input.getName().length;i++)
		{
			if (input.getName()[i]!=null && input.getName()[i].length()>0)
			{	
				TableItem item = wFields.table.getItem(i);
				item.setText(1, input.getName()[i]);
				if (input.getRename()[i]!=null && !input.getName()[i].equals(input.getRename()[i]))
					item.setText(2, input.getRename()[i]);
				item.setText(3, Value.getTypeDesc(input.getType()[i]));
				if (input.getLength()[i]>=0) item.setText(4, ""+input.getLength()[i]); //$NON-NLS-1$
                if (input.getPrecision()[i]>=0) item.setText(5, ""+input.getPrecision()[i]); //$NON-NLS-1$
			}
		}
		if (input.getScript() != null) wScript.setText( input.getScript() );

		wFields.setRowNums();
		wFields.optWidth(true);
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value
		
		//Table table = wFields.table;

		input.setScript( wScript.getText() );
		int nrfields = wFields.nrNonEmpty();

		input.allocate(nrfields);

		for (int i=0;i<nrfields;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getName()  [i] = item.getText(1);
			input.getRename()[i] = item.getText(2);
			if (input.getRename()[i]==null || 
			    input.getRename()[i].length()==0 || 
			    input.getRename()[i].equalsIgnoreCase(input.getName()[i])
			   )
			{
				input.getRename()[i] = input.getName()[i];
			}
			input.getType()  [i] = Value.getType(item.getText(3));
			String slen = item.getText(4);
			String sprc = item.getText(5);
			input.getLength()   [i]=Const.toInt(slen, -1);
			input.getPrecision()[i]=Const.toInt(sprc, -1);
		}
		
		dispose();
	}
	
	private void get()
	{
		try
		{
			String script = wScript.getText();
			script+=Const.CR;
	
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
				for (int i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					
					switch(v.getType())
					{
					case Value.VALUE_TYPE_STRING : script+=v.getName()+".getString()"; break; //$NON-NLS-1$
					case Value.VALUE_TYPE_NUMBER : script+=v.getName()+".getNumber()"; break; //$NON-NLS-1$
					case Value.VALUE_TYPE_INTEGER: script+=v.getName()+".getInteger()"; break; //$NON-NLS-1$
					case Value.VALUE_TYPE_DATE   : script+=v.getName()+".getDate()"; break; //$NON-NLS-1$
					case Value.VALUE_TYPE_BOOLEAN: script+=v.getName()+".getBoolean"; break; //$NON-NLS-1$
					default: script+=v.getName(); break;
					}
					script+=";"+Const.CR; //$NON-NLS-1$
				}
				wScript.setText(script);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("ScriptValuesDialog.FailedToGetFields.DialogTitle"), Messages.getString("ScriptValuesDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public boolean test()
	{
		return test(false, false);
	}
	
	private boolean test(boolean getvars, boolean popup)
	{
		boolean retval=true;
		String scr = wScript.getText();
		String errorMessage = ""; //$NON-NLS-1$
		
		Context jscx;
		Scriptable jsscope;
		Script jsscript;

		jscx = Context.enter();
		jsscope = jscx.initStandardObjects(null);
			
		try
		{
			Row row = transMeta.getPrevStepFields(stepname);

			ScriptValuesDummy dummy = new ScriptValuesDummy(); 
		    Scriptable jsvalue = Context.toObject(dummy, jsscope);
  		    jsscope.put("_step_", jsscope, jsvalue); //$NON-NLS-1$

			if (row!=null)
			{
			   Scriptable jsrow = Context.toObject(row, jsscope);
			   jsscope.put("row", jsscope, jsrow); //$NON-NLS-1$
			                   
               for (int i=0;i<row.size();i++)
				{
					Value val = row.getValue(i); 
					// Set date and string values to something to simulate real thing
					if (val.isDate()) val.setValue(new Date());
					if (val.isString()) val.setValue("test value test value test value test value test value test value test value test value test value test value"); //$NON-NLS-1$
					Scriptable jsarg = Context.toObject(val, jsscope);
					jsscope.put(val.getName(), jsscope, jsarg);
				}
				
				try
				{
					jsscript=jscx.compileString(scr, "script", 1, null); //$NON-NLS-1$
					
					try
					{
						jsscript.exec(jscx, jsscope);
						
						if (getvars)
						{
							Object id[] = jsscope.getIds();
				
							for (int i=0;i<id.length;i++)
							{
								String varname = (String)id[i];
								if (!varname.equalsIgnoreCase("row") && //$NON-NLS-1$
								    row.searchValueIndex(varname)<0
								    )
								{
									int type=Value.VALUE_TYPE_STRING;
									int length=-1, precision=-1;
				
									Object result = jsscope.get(varname, jsscope);
									if (result!=null)
									{
										String classname = result.getClass().getName();
										
										if (classname.equalsIgnoreCase("java.lang.Byte")) //$NON-NLS-1$
										{
											// MAX = 127
											type=Value.VALUE_TYPE_INTEGER;
											length=3;
											precision=0;
										}
										else
										if (classname.equalsIgnoreCase("java.lang.Integer")) //$NON-NLS-1$
										{
											// MAX = 2147483647
											type=Value.VALUE_TYPE_INTEGER;
											length=9;
											precision=0;
										}
										else
										if (classname.equalsIgnoreCase("java.lang.Long")) //$NON-NLS-1$
										{
											// MAX = 9223372036854775807
											type=Value.VALUE_TYPE_INTEGER;
											length=18;
											precision=0;
										}
										else
										if (classname.equalsIgnoreCase("java.lang.Double")) //$NON-NLS-1$
										{
											type=Value.VALUE_TYPE_NUMBER;
											length=16;
											precision=2;
										}
										else
										if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeDate") || //$NON-NLS-1$
											classname.equalsIgnoreCase("java.lang.Date") //$NON-NLS-1$
										)
										{
											type=Value.VALUE_TYPE_DATE;
										}
										else
										if (classname.equalsIgnoreCase("java.lang.Boolean")) //$NON-NLS-1$
										{
											type=Value.VALUE_TYPE_BOOLEAN;
										}
									}
										
									TableItem ti = new TableItem(wFields.table, SWT.NONE);
									ti.setText(1, varname);
									ti.setText(2, varname);
									ti.setText(3, Value.getTypeDesc(type));
									ti.setText(4, ""+length); //$NON-NLS-1$
									ti.setText(5, ""+precision); //$NON-NLS-1$
								}
							}
							wFields.removeEmptyRows();
							wFields.setRowNums();
							wFields.optWidth(true);
						}
					}
					catch(JavaScriptException jse)
					{
						errorMessage=Messages.getString("ScriptValuesDialog.Exception.CouldNotExecuteScript")+Const.CR+jse.toString(); //$NON-NLS-1$
						retval=false;
					}
					catch(Exception e)
					{
						errorMessage=Messages.getString("ScriptValuesDialog.Exception.CouldNotExecuteScript2")+Const.CR+e.toString(); //$NON-NLS-1$
						retval=false;
					}
				}
				catch(Exception e)
				{
					errorMessage = Messages.getString("ScriptValuesDialog.Exception.CouldNotCompileScript")+Const.CR+e.toString(); //$NON-NLS-1$
					retval=false;
				}
			}
			else
			{
				errorMessage = Messages.getString("ScriptValuesDialog.Exception.CouldNotGetFields"); //$NON-NLS-1$
				retval=false;
			}
	
			if (popup)
			{
				if (retval)
				{
					if (!getvars)
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
						mb.setMessage(Messages.getString("ScriptValuesDialog.CompiledScriptOK.DialogMessage")+Const.CR); //$NON-NLS-1$
						mb.setText(Messages.getString("ScriptValuesDialog.CompiledScriptOK.DialogTitle")); //$NON-NLS-1$
						mb.open();
					}
				} 
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage(errorMessage);
					mb.setText(Messages.getString("ScriptValuesDialog.ErrorDialog.Title")); //$NON-NLS-1$
					mb.open(); 
				}
			}
		}
		catch(KettleException ke)
		{
			retval=false;
			new ErrorDialog(shell, Messages.getString("ScriptValuesDialog.TestFailed.DialogTitle"), Messages.getString("ScriptValuesDialog.TestFailed.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
		finally
		{
			if (jscx!=null) Context.exit();
		}
		return retval;
	}
		
	public String toString()
	{
		return this.getClass().getName();
	}
}