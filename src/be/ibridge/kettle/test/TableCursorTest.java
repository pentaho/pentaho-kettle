package be.ibridge.kettle.test;

//Send questions, comments, bug reports, etc. to the authors:

//Rob Warner (rwarner@interspatial.com)
//Robert Harris (rbrt_harris@yahoo.com)

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
* This class demonstrates TableCursor
*/
public class TableCursorTest {
// The number of rows and columns
private static final int NUM = 5;

/**
 * Runs the program
 */
public void run() {
  Display display = new Display();
  Shell shell = new Shell(display);
  shell.setText("Table Cursor Test");
  createContents(shell);
  shell.pack();
  shell.open();
  while (!shell.isDisposed()) {
    if (!display.readAndDispatch()) {
      display.sleep();
    }
  }
  display.dispose();
}

/**
 * Creates the main window's contents
 * 
 * @param shell the main window
 */
private void createContents(Shell shell) {
  shell.setLayout(new FillLayout());

  // Create the table
  final Table table = new Table(shell, SWT.SINGLE | SWT.FULL_SELECTION);
  table.setHeaderVisible(true);
  table.setLinesVisible(true);

  // Create the columns
  for (int i = 0; i < NUM; i++) {
    TableColumn column = new TableColumn(table, SWT.CENTER);
    column.setText("Column " + (i + 1));
    column.pack();
  }

  // Create the rows
  for (int i = 0; i < NUM; i++) {
    new TableItem(table, SWT.NONE);
  }

  // Create the TableCursor
  final TableCursor cursor = new TableCursor(table, SWT.NONE);

  // Create the editor
  // Use a ControlEditor, not a TableEditor, because the cursor is the parent
  final ControlEditor editor = new ControlEditor(cursor);
  editor.grabHorizontal = true;
  editor.grabVertical = true;

  // Add the event handling
  cursor.addSelectionListener(new SelectionAdapter() {
    // This is called as the user navigates around the table
    public void widgetSelected(SelectionEvent event) {
      // Select the row in the table where the TableCursor is
      table.setSelection(new TableItem[] { cursor.getRow()});
    }

    // This is called when the user hits Enter
    public void widgetDefaultSelected(SelectionEvent event) {
      // Begin an editing session
      // Notice that the parent of the Text is the TableCursor, not the Table
      final Text text = new Text(cursor, SWT.NONE);
      text.setFocus();

      // Copy the text from the cell to the Text
      text.setText(cursor.getRow().getText(cursor.getColumn()));
      text.setFocus();

      // Add a handler to detect key presses
      text.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent event) {
          // End the editing and save the text if the user presses Enter
          // End the editing and throw away the text if the user presses Esc
          switch (event.keyCode) {
          case SWT.CR:
            cursor.getRow().setText(cursor.getColumn(), text.getText());
          case SWT.ESC:
            text.dispose();
            break;
          }
        }
      });
      editor.setEditor(text);
    }
  });
}

/**
 * The application entry point
 * 
 * @param args the command line arguments
 */
public static void main(String[] args) {
  new TableCursorTest().run();
}
} 
