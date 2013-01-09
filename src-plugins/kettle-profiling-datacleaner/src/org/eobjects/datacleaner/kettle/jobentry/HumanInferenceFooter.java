package org.eobjects.datacleaner.kettle.jobentry;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class HumanInferenceFooter extends Composite {

    private final List<Object> resources;

	/**
	 * Create the composite.
	 *
	 * @param parent
	 */
	public HumanInferenceFooter(Composite parent) {
		super(parent, SWT.NONE);

        resources = new ArrayList<Object>();

		GridLayout layout = new GridLayout(3, false);
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		setLayout(layout);

		// left part, copyright + email
		{
			Label label = new Label(this, SWT.SHADOW_NONE | SWT.RIGHT);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false,
					1, 1));
            Image image = new Image(getShell().getDisplay(), new ImageData(
					getClass().getResourceAsStream("footer-left.png")));
            resources.add(image);
            label.setImage(image);
		}

		// background part
		{
			CLabel label = new CLabel(this, SWT.SHADOW_NONE);
            Font font = WidgetFactory.getHeader1Font();
            resources.add(font);
            label.setFont(font);
            Color color = new Color(getDisplay(), 233, 233,
                    233);
            resources.add(color);
            label.setForeground(color);
			ImageData imageData = new ImageData(getClass().getResourceAsStream(
					"footer-bg.png")).scaledTo(2000, 46);
            Image image = new Image(getShell().getDisplay(), imageData);
            resources.add(image);
            label.setBackground(image);
			GridData backgroundLayoutData = new GridData();
			backgroundLayoutData.horizontalAlignment = SWT.FILL;
			backgroundLayoutData.grabExcessHorizontalSpace = true;
			backgroundLayoutData.grabExcessVerticalSpace = true;
			backgroundLayoutData.heightHint = 46;
			label.setLayoutData(backgroundLayoutData);
		}

		// right part (HI logo)
		{
			Label label = new Label(this, SWT.SHADOW_NONE | SWT.RIGHT);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false,
					1, 1));
            Image image = new Image(getShell().getDisplay(), new ImageData(
                    getClass().getResourceAsStream("footer-right.png")));
            resources.add(image);
            label.setImage(image);
            Cursor cursor = new Cursor(label.getDisplay(), SWT.CURSOR_HAND);
            resources.add(cursor);
            label.setCursor(cursor);
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent arg0) {
					Program.launch("http://www.humaninference.com");
				}
			});
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
                    if (resource instanceof Color) {
                        ((Color) resource).dispose();
                    }
                }
            }
        });
	}

}
