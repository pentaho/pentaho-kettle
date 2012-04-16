package org.eobjects.datacleaner.kettle.jobentry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.widget.TextVar;

public class OutputFileSelectionWidget extends Composite {

    private final TextVar outputFilenameField;
    private final Button outputFileInResultCheckBox;

    OutputFileSelectionWidget(Composite composite, JobMeta jobMeta) {
        super(composite, SWT.NONE);

        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginWidth = 0;
        this.setLayout(gridLayout);

        outputFilenameField = new TextVar(jobMeta, this, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        outputFilenameField.setLayoutData(WidgetFactory.createGridData());

        outputFileInResultCheckBox = new Button(this, SWT.CHECK);
        outputFileInResultCheckBox.setText("Add to result filenames");
        outputFileInResultCheckBox.setSelection(true);
        outputFilenameField.setLayoutData(WidgetFactory.createGridData());
    }

    public String getOutputFilename() {
        return outputFilenameField.getText();
    }

    public void setOutputFilename(String filename) {
        outputFilenameField.setText(filename);
    }

    public boolean isOutputFileInResult() {
        return outputFileInResultCheckBox.getSelection();
    }

    public void setOutputFileInResult(boolean b) {
        outputFileInResultCheckBox.setSelection(b);
    }
}
