package be.ibridge.kettle.test.styledtext;
/* 
 * StyledText snippet: Draw a box around text.
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class Snippet244 {
	static String SEARCH_STRING = "box";
    public static void main(String[] args) {
        final Display display = new Display();
        final Color RED = display.getSystemColor(SWT.COLOR_RED);
        Shell shell = new Shell(display);
        shell.setBounds(10,10,250,250);
        final StyledText text = new StyledText(shell, SWT.NONE);
        text.setBounds(10,10,200,200);
        text.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event event) {
				String contents = text.getText();
				int stringWidth = event.gc.stringExtent(SEARCH_STRING).x;
				int lineHeight = text.getLineHeight();
				event.gc.setForeground(RED);
				int index = contents.indexOf(SEARCH_STRING);
				while (index != -1) {
					Point topLeft = text.getLocationAtOffset(index);
					event.gc.drawRectangle(topLeft.x - 1, topLeft.y, stringWidth + 1, lineHeight - 1);
					index = contents.indexOf(SEARCH_STRING, index + 1);
				}
			}
		});
        text.setText("This demonstrates drawing a box\naround every occurrence of the word\nbox in the StyledText");
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}