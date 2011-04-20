package org.pentaho.test.ui.database;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.ui.database.DatabaseConnectionDialog;
import org.pentaho.ui.database.Messages;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swing.SwingXulLoader;


public class SwingTest {

	DatabaseMeta database = null;

	public static void main(String[] args) {

	    try {
	      KettleEnvironment.init();
	    } catch (KettleException e) {
	      e.printStackTrace();
	      System.exit(1);
	    }
	  
		SwingTest harness = new SwingTest();

		try {
			InputStream in = DatabaseDialogHarness.class.getClassLoader()
					.getResourceAsStream("org/pentaho/ui/database/databasedialog.xul"); //$NON-NLS-1$
			if (in == null) {
				System.out.println("Invalid Input"); //$NON-NLS-1$
				return;
			}

			SAXReader rdr = new SAXReader();
			final Document doc = rdr.read(in);

			harness.showDialog(doc);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void showDialog(final Document doc) {

		XulDomContainer container = null;
		try {
			container = new SwingXulLoader().loadXul(DatabaseConnectionDialog.DIALOG_DEFINITION_FILE, Messages.getBundle());
			if (database != null) {
				container.getEventHandler("dataHandler").setData(database); //$NON-NLS-1$
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		XulDialog dialog = (XulDialog) container.getDocumentRoot().getRootElement();
		container.initialize();
		dialog.show();
		try{
			@SuppressWarnings("unused")
      Object data = container.getEventHandler("dataHandler").getData(); //$NON-NLS-1$
			int i=0;
			i++;
		} catch(XulException e){
			System.out.println("Error getting data"); //$NON-NLS-1$
		}
	}

}
