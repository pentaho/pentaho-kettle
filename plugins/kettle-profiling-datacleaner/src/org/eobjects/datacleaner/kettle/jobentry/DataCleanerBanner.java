package org.eobjects.datacleaner.kettle.jobentry;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;

public class DataCleanerBanner extends Composite {

    private static final int HEIGHT = 80;
    
    private final List<Object> resources;

    /**
     * Create the composite.
     *
     * @param parent
     * @param infoMessage
     */
    public DataCleanerBanner(Composite parent) {
        super(parent, SWT.NONE);

        resources = new ArrayList<Object>();

        GridLayout layout = new GridLayout(3, false);
        layout.verticalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        setLayout(layout);

        // left part 
        {
            Composite leftComposite = new Composite(this, SWT.NO_BACKGROUND);
            leftComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
                    false, 1, 1));
            leftComposite.setLayout(null);

            CLabel label = new CLabel(leftComposite, SWT.SHADOW_NONE);
            Font font = WidgetFactory.getHeader1Font();
            resources.add(font);
            label.setFont(font);
            ImageData imageData = new ImageData(getClass().getResourceAsStream(
                    "banner-left.png"));
            Image image = new Image(getShell().getDisplay(),
                    imageData);
            resources.add(image);
            label.setBackground(image);
            label.setLocation(0, 0);
            label.setSize(78, HEIGHT);

            Cursor cursor = new Cursor(leftComposite.getDisplay(),
                    SWT.CURSOR_HAND);
            resources.add(cursor);
            label.setCursor(cursor);
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent arg0) {
                    Program.launch("http://datacleaner.eobjects.org");
                }
            });

        }

        // background part
        {
            CLabel label = new CLabel(this, SWT.SHADOW_NONE);
            Font font = WidgetFactory.getHeader1Font();
            resources.add(font);
            label.setFont(font);
            ImageData imageData = new ImageData(getClass().getResourceAsStream(
                    "banner-bg.png")).scaledTo(2000, HEIGHT);
            Image image = new Image(getShell().getDisplay(), imageData);
            resources.add(image);
            label.setBackground(image);
            GridData backgroundLayoutData = new GridData();
            backgroundLayoutData.verticalAlignment = SWT.TOP;
            backgroundLayoutData.horizontalAlignment = SWT.FILL;
            backgroundLayoutData.grabExcessHorizontalSpace = true;
            backgroundLayoutData.grabExcessVerticalSpace = true;
            backgroundLayoutData.heightHint = HEIGHT;
            label.setLayoutData(backgroundLayoutData);
        }

        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent event) {
                for (Object resource : resources) {
                    if (resource instanceof Image) {
                        ((Image) resource).dispose();
                    }
                    if (resource instanceof Font) {
                        ((Font) resource).dispose();
                    }
                    if (resource instanceof Cursor) {
                        ((Cursor) resource).dispose();
                    }
                }
            }
        });
    }

}
