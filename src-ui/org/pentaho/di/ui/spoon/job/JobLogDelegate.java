package org.pentaho.di.ui.spoon.job;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.logging.BufferChangedListener;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class JobLogDelegate extends SpoonDelegate implements XulEventHandler {

  private static final String XUL_FILE_TRANS_LOG_TOOLBAR = "ui/job-log-toolbar.xul";
  private static final LogWriter log = LogWriter.getInstance();

  private JobGraph jobGraph;

  private CTabItem jobLogTab;

  public Text jobLogText;

  /**
   * The number of lines in the log tab
   */
  private int textSize;

  private Composite jobLogComposite;

  private XulToolbar toolbar;

  /**
   * @param spoon
   */
  public JobLogDelegate(Spoon spoon, JobGraph jobGraph) {
    super(spoon);
    this.jobGraph = jobGraph;
  }

  public void addJobLog() {
    // First, see if we need to add the extra view...
    //
    if (jobGraph.extraViewComposite == null || jobGraph.extraViewComposite.isDisposed()) {
      jobGraph.addExtraView();
    } else {
      if (jobLogTab != null && !jobLogTab.isDisposed()) {
        // just set this one active and get out...
        //
        jobGraph.extraViewTabFolder.setSelection(jobLogTab);
        return;
      }
    }

    // Add a transLogTab : display the logging...
    //
    jobLogTab = new CTabItem(jobGraph.extraViewTabFolder, SWT.NONE);
    jobLogTab.setImage(GUIResource.getInstance().getImageShowLog());
    jobLogTab.setText(Messages.getString("JobGraph.LogTab.Name"));

    jobLogComposite = new Composite(jobGraph.extraViewTabFolder, SWT.NONE);
    jobLogComposite.setLayout(new FormLayout());

    addToolBar();

    jobLogText = new Text(jobLogComposite, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    spoon.props.setLook(jobLogText);
    FormData fdText = new FormData();
    fdText.left = new FormAttachment(0, 0);
    fdText.right = new FormAttachment(100, 0);
    fdText.top = new FormAttachment((Control) toolbar.getManagedObject(), 0);
    fdText.bottom = new FormAttachment(100, 0);
    jobLogText.setLayoutData(fdText);

    jobLogTab.setControl(jobLogComposite);

    // Create a new String appender to the log and capture that directly...
    //
    final Log4jStringAppender stringAppender = LogWriter.createStringAppender();
    stringAppender.setMaxNrLines(Props.getInstance().getMaxNrLinesInLog());
    stringAppender.addBufferChangedListener(new BufferChangedListener() {

      public void contentWasAdded(final StringBuffer content, final String extra, final int nrLines) {
        spoon.getDisplay().asyncExec(new Runnable() {

          public void run() {
            if (!jobLogText.isDisposed()) {
              textSize++;

              // OK, now what if the number of lines gets too big?
              // We allow for a few hundred lines buffer over-run.
              // That way we reduce flicker...
              //
              if (textSize >= nrLines + 200) {
                jobLogText.setText(content.toString());
                jobLogText.setSelection(content.length());
                jobLogText.showSelection();
                jobLogText.clearSelection();
                textSize = nrLines;
              } else {
                jobLogText.append(extra);
              }
            }
          }

        });
      }

    });
    log.addAppender(stringAppender);
    jobLogTab.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        log.removeAppender(stringAppender);
      }
    });

    jobGraph.extraViewTabFolder.setSelection(jobLogTab);
  }

  private void addToolBar() {

    try {
      XulLoader loader = new SwtXulLoader();
      ResourceBundle bundle = ResourceBundle.getBundle("org/pentaho/di/ui/spoon/messages/messages", LanguageChoice.getInstance().getDefaultLocale());
      XulDomContainer xulDomContainer = loader.loadXul(XUL_FILE_TRANS_LOG_TOOLBAR, bundle);
      xulDomContainer.addEventHandler(this);
      toolbar = (XulToolbar) xulDomContainer.getDocumentRoot().getElementById("nav-toolbar");

      ToolBar swtToolbar = (ToolBar) toolbar.getManagedObject();
      swtToolbar.layout(true, true);
    } catch (Throwable t) {
      log.logError(toString(), Const.getStackTracker(t));
      new ErrorDialog(jobLogComposite.getShell(), Messages.getString("Spoon.Exception.ErrorReadingXULFile.Title"), Messages.getString("Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_TRANS_LOG_TOOLBAR), new Exception(t));
    }
  }

  public void clearLog() {
    if (jobLogText != null && !jobLogText.isDisposed())
      jobLogText.setText(""); //$NON-NLS-1$
  }

  public void showLogSettings() {
    spoon.setLog();
  }

  public void showErrors() {
    String all = jobLogText.getText();
    ArrayList<String> err = new ArrayList<String>();

    int i = 0;
    int startpos = 0;
    int crlen = Const.CR.length();

    while (i < all.length() - crlen) {
      if (all.substring(i, i + crlen).equalsIgnoreCase(Const.CR)) {
        String line = all.substring(startpos, i);
        if (line.toUpperCase().indexOf(Messages.getString("JobLog.System.ERROR")) >= 0 || //$NON-NLS-1$
            line.toUpperCase().indexOf(Messages.getString("JobLog.System.EXCEPTION")) >= 0 //$NON-NLS-1$
        ) {
          err.add(line);
        }
        // New start of line
        startpos = i + crlen;
      }

      i++;
    }
    String line = all.substring(startpos);
    if (line.toUpperCase().indexOf(Messages.getString("JobLog.System.ERROR")) >= 0 || //$NON-NLS-1$
        line.toUpperCase().indexOf(Messages.getString("JobLog.System.EXCEPTION")) >= 0 //$NON-NLS-1$
    ) {
      err.add(line);
    }

    if (err.size() > 0) {
      String err_lines[] = new String[err.size()];
      for (i = 0; i < err_lines.length; i++)
        err_lines[i] = err.get(i);

      EnterSelectionDialog esd = new EnterSelectionDialog(jobGraph.getShell(), err_lines, Messages.getString("JobLog.Dialog.ErrorLines.Title"), Messages.getString("JobLog.Dialog.ErrorLines.Message")); //$NON-NLS-1$ //$NON-NLS-2$
      line = esd.open();
      if (line != null) {
        JobMeta jobMeta = jobGraph.getManagedObject();
        for (i = 0; i < jobMeta.nrJobEntries(); i++) {
          JobEntryCopy entryCopy = jobMeta.getJobEntry(i);
          if (line.indexOf(entryCopy.getName()) >= 0) {
            spoon.editJobEntry(jobMeta, entryCopy);
          }
        }
        // System.out.println("Error line selected: "+line);
      }
    }
  }

  /**
   * @return the transLogTab
   */
  public CTabItem getJobLogTab() {
    return jobLogTab;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getData()
   */
  public Object getData() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getName()
   */
  public String getName() {
    return "joblog";
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getXulDomContainer()
   */
  public XulDomContainer getXulDomContainer() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setData(java.lang.Object)
   */
  public void setData(Object data) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setName(java.lang.String)
   */
  public void setName(String name) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setXulDomContainer(org.pentaho.ui.xul.XulDomContainer)
   */
  public void setXulDomContainer(XulDomContainer xulDomContainer) {
    // TODO Auto-generated method stub

  }

}
