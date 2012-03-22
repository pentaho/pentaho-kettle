package org.eobjects.datacleaner.kettle.jobentry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;

public class WidgetFactory {

    public static Combo createCombo(Composite composite) {
        Combo combo = new Combo(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
        combo.setLayoutData(createGridData());
        return combo;
    }

    public static GridData createGridData() {
        GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
        return gridData;
    }

    public static List createList(Composite composite, boolean multiSelect) {
        List list;
        if (multiSelect) {
            list = new List(composite, SWT.BORDER | SWT.MULTI);
        } else {
            list = new List(composite, SWT.BORDER);
        }
        list.setLayoutData(createGridData());
        return list;
    }

    public static Font getSmallFont() {
        final FontData smallFontData = new FontData();
        smallFontData.setStyle(SWT.NORMAL);
        smallFontData.setHeight(8);
        final Font headerFont = new Font(Display.getCurrent(), smallFontData);
        return headerFont;
    }

    public static Font getHeader2Font() {
        final FontData boldFontData = new FontData();
        boldFontData.setStyle(SWT.BOLD);
        boldFontData.setHeight(10);
        final Font boldFont = new Font(Display.getCurrent(), boldFontData);
        return boldFont;
    }

    public static Font getHeader1Font() {
        final FontData headerFontData = new FontData();
        headerFontData.setStyle(SWT.BOLD);
        headerFontData.setHeight(12);
        final Font headerFont = new Font(Display.getCurrent(), headerFontData);
        return headerFont;
    }
}
