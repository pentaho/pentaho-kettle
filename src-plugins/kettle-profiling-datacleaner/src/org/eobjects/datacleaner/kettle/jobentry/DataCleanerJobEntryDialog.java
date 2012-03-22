package org.eobjects.datacleaner.kettle.jobentry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.widget.TextVar;

import plugin.DataCleanerJobEntry;

public class DataCleanerJobEntryDialog extends AbstractJobEntryDialog implements JobEntryDialogInterface {

    private TextVar executableFilenameField;
    private TextVar jobFilenameField;
    private TextVar outputFilenameField;
    private EnumCombo<DataCleanerOutputType> outputTypeCombo;

    public DataCleanerJobEntryDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta) {
        super(parent, jobEntry, rep, jobMeta);
    }

    @Override
    protected void addConfigurationFields(Group propertiesGroup, int margin, int middle) {

        // Executable field
        {
            final Label fieldLabel = new Label(propertiesGroup, SWT.RIGHT);
            fieldLabel.setLayoutData(WidgetFactory.createGridData());
            fieldLabel.setText("DataCleaner executable:");

            executableFilenameField = new TextVar(jobMeta, propertiesGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
            executableFilenameField.setLayoutData(WidgetFactory.createGridData());
        }

        // Job file
        {
            final Label fieldLabel = new Label(propertiesGroup, SWT.RIGHT);
            fieldLabel.setLayoutData(WidgetFactory.createGridData());
            fieldLabel.setText("Job file:");

            jobFilenameField = new TextVar(jobMeta, propertiesGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
            jobFilenameField.setLayoutData(WidgetFactory.createGridData());
        }

        // Output file
        {
            final Label fieldLabel = new Label(propertiesGroup, SWT.RIGHT);
            fieldLabel.setLayoutData(WidgetFactory.createGridData());
            fieldLabel.setText("Output file:");

            outputFilenameField = new TextVar(jobMeta, propertiesGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
            outputFilenameField.setLayoutData(WidgetFactory.createGridData());
        }

        {
            // Output type
            final Label fieldLabel = new Label(propertiesGroup, SWT.RIGHT);
            fieldLabel.setText("Output type:");
            fieldLabel.setLayoutData(WidgetFactory.createGridData());

            outputTypeCombo = new EnumCombo<DataCleanerOutputType>(propertiesGroup, DataCleanerOutputType.class, false);
            outputTypeCombo.setToolTipText("Select HTML output, Text output or Serialized result");
        }

        // initialize values
        {
            final DataCleanerJobEntryConfiguration configuration = getConfiguration();
            outputTypeCombo.setValue(configuration.getOutputType());
            executableFilenameField.setText(configuration.getExecutableFile());
            jobFilenameField.setText(configuration.getJobFile());
            outputFilenameField.setText(configuration.getOutputFile());
        }
    }

    @Override
    public void ok() {
        DataCleanerJobEntryConfiguration configuration = getConfiguration();
        configuration.setExecutableFile(executableFilenameField.getText());
        configuration.setJobFile(jobFilenameField.getText());
        configuration.setOutputFile(outputFilenameField.getText());
        configuration.setOutputType(outputTypeCombo.getValue());
    }

    private DataCleanerJobEntryConfiguration getConfiguration() {
        DataCleanerJobEntry jobEntry = (DataCleanerJobEntry) jobEntryInt;
        DataCleanerJobEntryConfiguration configuration = jobEntry.getConfiguration();
        return configuration;
    }

}
