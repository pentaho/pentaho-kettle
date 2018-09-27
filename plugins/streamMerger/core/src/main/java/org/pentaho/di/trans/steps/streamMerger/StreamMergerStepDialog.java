package org.pentaho.di.trans.steps.streamMerger;

import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class StreamMergerStepDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = StreamMergerStepMeta.class; // for i18n purposes

	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed 
	private StreamMergerStepMeta meta;

	private String[] previousSteps;  // steps sending data in to this step

	// text field holding the name of the field to add to the row stream
	private Label wlSteps;
	private TableView wSteps;
	private FormData fdlSteps, fdSteps;


	public StreamMergerStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (StreamMergerStepMeta) in;
	}

	public String open() {

		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, meta);
		
		changed = meta.hasChanged();
		
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				meta.setChanged();
			}
		};
		
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		
		ResourceBundle bundle = ResourceBundle.getBundle("AramisPlugin");
		shell.setText("Stream Merger Version " + bundle.getString("pluginVersion"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName")); 
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

        // OK, get and cancel buttons
        wOK = new Button( shell, SWT.PUSH );
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wGet = new Button( shell, SWT.PUSH );
        wGet.setText( "Get" );
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        setButtonPositions(new Button[]{wOK, wGet, wCancel}, margin, null);

		// Table with fields for inputting step names
		wlSteps = new Label( shell, SWT.NONE );
		wlSteps.setText("Steps to Merge (first one defines order of initial fields)");
		props.setLook(wlSteps);
		fdlSteps = new FormData();
		fdlSteps.left = new FormAttachment( 0, 0 );
		fdlSteps.top = new FormAttachment( wStepname, margin );
		wlSteps.setLayoutData(fdlSteps);

		final int FieldsCols = 1;
        final int FieldsRows = meta.getNumberOfSteps();

        previousSteps = transMeta.getPrevStepNames(stepname);

		ColumnInfo[] colinf = new ColumnInfo[FieldsCols];
		colinf[0] = new ColumnInfo("Steps",ColumnInfo.COLUMN_TYPE_CCOMBO, previousSteps, false );

		wSteps = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

		fdSteps = new FormData();
		fdSteps.left = new FormAttachment( 0, 0 );
		fdSteps.top = new FormAttachment(wlSteps, margin );
		fdSteps.right = new FormAttachment( 100, 0 );
		fdSteps.bottom = new FormAttachment( wOK, -2 * margin );
		wSteps.setLayoutData(fdSteps);

		lsCancel = new Listener() {
			public void handleEvent(Event e) {cancel();}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {ok();}
		};
        lsGet = new Listener() {
            public void handleEvent( Event e ) {
                get();
            }
        };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);
        wGet.addListener( SWT.Selection, lsGet );

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {ok();}
		};
		wStepname.addSelectionListener(lsDef);

		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {cancel();}
		});
		
		setSize();
		populateDialog();
		
		meta.setChanged(changed);
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return stepname;
	}
	

    private void populateDialog() {
        Table table = wSteps.table;
        
        if ( meta.getNumberOfSteps() > 0 ) 
            table.removeAll();
        
        String[] stepNames = meta.getStepsToMerge();
        for ( int i = 0; i < stepNames.length; i++ ) {
            TableItem ti = new TableItem( table, SWT.NONE );
            ti.setText( 0, "" + ( i + 1 ) );
            
            if ( stepNames[i] != null ) 
                ti.setText( 1, stepNames[i] );
            
        }

        wSteps.removeEmptyRows();
        wSteps.setRowNums();
        wSteps.optWidth(true);

        wStepname.selectAll();
        wStepname.setFocus();
	}

    private void get() {
        wSteps.removeAll();
        Table table = wSteps.table;

        for ( int i = 0; i < previousSteps.length; i++ ) {
            TableItem ti = new TableItem( table, SWT.NONE );
            ti.setText( 0, "" + ( i + 1 ) );
            ti.setText( 1, previousSteps[i] );
        }
        wSteps.removeEmptyRows();
        wSteps.setRowNums();
        wSteps.optWidth(true);

    }

	private void cancel() {
		stepname = null;
		meta.setChanged(changed);
		dispose();
	}

    private void getMeta(String[] inputSteps) {
        List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();

        if ( infoStreams.size() == 0 || inputSteps.length < infoStreams.size()) {
            if ( inputSteps.length != 0 ) {
                meta.wipeStepIoMeta();
                
                for (String inputStep : inputSteps) 
                    meta.getStepIOMeta().addStream(new Stream(StreamInterface.StreamType.INFO, null, "", StreamIcon.INFO, null));
                
                infoStreams = meta.getStepIOMeta().getInfoStreams();
            }
        } 
        else if ( infoStreams.size() < inputSteps.length ) {
            int requiredStreams = inputSteps.length - infoStreams.size();

            for ( int i = 0; i < requiredStreams; i++ ) 
                meta.getStepIOMeta().addStream(new Stream( StreamInterface.StreamType.INFO, null, "", StreamIcon.INFO, null ) );
            
            infoStreams = meta.getStepIOMeta().getInfoStreams();
        }
        
        int streamCount = infoStreams.size();
        String[] stepsToMerge = meta.getStepsToMerge();
        
        for ( int i = 0; i < streamCount; i++ ) {
            String step = stepsToMerge[i];
            StreamInterface infoStream = infoStreams.get( i );
            infoStream.setStepMeta( transMeta.findStep( step ) );
            infoStream.setSubject(step);
        }
    }

	private void ok() {

		stepname = wStepname.getText(); 

        int nrsteps = wSteps.nrNonEmpty();
        String[] stepNames = new String[nrsteps];
        
        for ( int i = 0; i < nrsteps; i++ ) {
            TableItem ti = wSteps.getNonEmpty(i);
            StepMeta tm = transMeta.findStep(ti.getText(1));
            
            if (tm != null) 
                stepNames[i] = tm.getName();
            
        }
        meta.setStepsToMerge(stepNames);
		getMeta(stepNames);

		dispose();
	}
}
