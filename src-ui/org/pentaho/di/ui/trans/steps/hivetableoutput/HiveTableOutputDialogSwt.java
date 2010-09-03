package org.pentaho.di.ui.trans.steps.hivetableoutput;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.hivetableoutput.HiveTableOutputMeta;
import org.pentaho.di.ui.trans.steps.textfileoutput.TextFileOutputDialog;

public class HiveTableOutputDialogSwt 
       extends TextFileOutputDialog {

	private static Class<?> PKG = HiveTableOutputMeta.class;
	
	public HiveTableOutputDialogSwt(Shell parent, Object in, TransMeta transMeta,
			String sname) {
		super(parent, in, transMeta, sname);
	}
}
