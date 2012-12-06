package org.eobjects.datacleaner.kettle.jobentry;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import plugin.DataCleanerJobEntry;

abstract class AbstractJobEntryDialog extends JobEntryDialog implements JobEntryDialogInterface, DisposeListener {

    private final String initialJobName;
    private Text jobNameField;
    private Button okButton;
    private Button cancelButton;

    private List<Object> resources = new ArrayList<Object>();

    public AbstractJobEntryDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta) {
        super(parent, jobEntry, rep, jobMeta);
        initialJobName = (jobEntry.getName() == null ? DataCleanerJobEntry.NAME : jobEntry.getName());
    }

    protected void initializeShell(Shell shell) {
        String id = PluginRegistry.getInstance().getPluginId(JobEntryPluginType.class, jobMeta);
        if (id != null) {
            shell.setImage(GUIResource.getInstance().getImagesSteps().get(id));
        }
    }

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    public final JobEntryInterface open() {
        final Shell parent = getParent();
        final Display display = parent.getDisplay();

        // initialize shell
        {
            shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
            initializeShell(shell);

            FormLayout shellLayout = new FormLayout();
            shellLayout.marginTop = 0;
            shellLayout.marginLeft = 0;
            shellLayout.marginRight = 0;
            shellLayout.marginBottom = 0;
            shellLayout.marginWidth = 0;
            shellLayout.marginHeight = 0;
            shell.setLayout(shellLayout);
            shell.setText(DataCleanerJobEntry.NAME + ": " + initialJobName);
        }

        final int middle = Const.MIDDLE_PCT;
        final int margin = Const.MARGIN;

        // HI banner
        final DataCleanerBanner banner = new DataCleanerBanner(shell);
        {
            final FormData bannerLayoutData = new FormData();
            bannerLayoutData.left = new FormAttachment(0, 0);
            bannerLayoutData.right = new FormAttachment(100, 0);
            bannerLayoutData.top = new FormAttachment(0, 0);
            banner.setLayoutData(bannerLayoutData);
        }

        // Step name
        {
            final Label stepNameLabel = new Label(shell, SWT.RIGHT);
            stepNameLabel.setText("Step name:");
            final FormData stepNameLabelLayoutData = new FormData();
            stepNameLabelLayoutData.left = new FormAttachment(0, margin);
            stepNameLabelLayoutData.right = new FormAttachment(middle, -margin);
            stepNameLabelLayoutData.top = new FormAttachment(banner, margin * 2);
            stepNameLabel.setLayoutData(stepNameLabelLayoutData);

            jobNameField = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
            jobNameField.setText(initialJobName);

            final FormData stepNameFieldLayoutData = new FormData();
            stepNameFieldLayoutData.left = new FormAttachment(middle, 0);
            stepNameFieldLayoutData.right = new FormAttachment(100, -margin);
            stepNameFieldLayoutData.top = new FormAttachment(banner, margin * 2);
            jobNameField.setLayoutData(stepNameFieldLayoutData);
        }

        // Properties Group
        final Group propertiesGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
        propertiesGroup.setText("Step configuration");
        final FormData propertiesGroupLayoutData = new FormData();
        propertiesGroupLayoutData.left = new FormAttachment(0, margin);
        propertiesGroupLayoutData.right = new FormAttachment(100, -margin);
        propertiesGroupLayoutData.top = new FormAttachment(jobNameField, margin);
        propertiesGroup.setLayoutData(propertiesGroupLayoutData);
        final GridLayout propertiesGroupLayout = new GridLayout(2, false);
        propertiesGroup.setLayout(propertiesGroupLayout);

        addConfigurationFields(propertiesGroup, margin, middle);

        okButton = new Button(shell, SWT.PUSH);
        Image saveImage = new Image(shell.getDisplay(), AbstractJobEntryDialog.class.getResourceAsStream("save.png"));
        resources.add(saveImage);
        okButton.setImage(saveImage);
        okButton.setText("OK");
        okButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ok();

                final String jobEntryName = jobNameField.getText();
                if (jobEntryName != null && jobEntryName.length() > 0 && !initialJobName.equals(jobEntryName)) {
                    jobEntryInt.setName(jobEntryName);
                }
                
                jobEntryInt.setChanged();
                shell.close();
            }
        });

        cancelButton = new Button(shell, SWT.PUSH);
        Image cancelImage = new Image(shell.getDisplay(),
                AbstractJobEntryDialog.class.getResourceAsStream("cancel.png"));
        resources.add(cancelImage);
        cancelButton.setImage(cancelImage);
        cancelButton.setText("Cancel");
        cancelButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event arg0) {
                cancel();
                jobNameField.setText("");
                shell.close();
            }
        });

        BaseStepDialog.positionBottomButtons(shell, new Button[] { okButton, cancelButton }, margin, propertiesGroup);

        // HI banner
        final HumanInferenceFooter footer = new HumanInferenceFooter(shell);
        {
            final FormData footerLayoutData = new FormData();
            footerLayoutData.left = new FormAttachment(0, 0);
            footerLayoutData.right = new FormAttachment(100, 0);
            footerLayoutData.top = new FormAttachment(okButton, margin * 2);
            footer.setLayoutData(footerLayoutData);
        }

        shell.addDisposeListener(this);

        shell.setSize(getDialogSize());

        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return jobEntryInt;
    }

    @Override
    public void widgetDisposed(DisposeEvent event) {
        for (Object resource : resources) {
            if (resource instanceof Image) {
                ((Image) resource).dispose();
            }
        }
    }

    protected Point getDialogSize() {
        Point clientAreaSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        int frameX = shell.getSize().x - shell.getClientArea().width;
        int frameY = shell.getSize().y - shell.getClientArea().height;
        return new Point(frameX + clientAreaSize.x, frameY + clientAreaSize.y);
    }

    protected abstract void addConfigurationFields(Group propertiesGroup, int margin, int middle);

    public void cancel() {
        // do nothing
    }

    public abstract void ok();

    protected void showWarning(String message) {
        MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.OK);
        messageBox.setText("EasyDataQuality - Warning");
        messageBox.setMessage(message);
        messageBox.open();
    }

    protected String getStepDescription() {
        return null;
    }

}
