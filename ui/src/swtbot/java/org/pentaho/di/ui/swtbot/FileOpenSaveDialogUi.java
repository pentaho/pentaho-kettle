package org.pentaho.di.ui.swtbot;

import org.pentaho.di.ui.spoon.Spoon;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.hamcrest.Matcher;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;

/**
 * Drives the file open/save dialog from the file-open-save-new plugin
 * ({@code org.pentaho.di.plugins.fileopensave.dialog.FileOpenSaveDialog}), shown by File &gt; Open.
 */
public final class FileOpenSaveDialogUi {
  private static final String OPEN_TITLE = "Open";
  private static final String OPEN_BUTTON = "Open";
  private static final String CANCEL_BUTTON = "Cancel";

  private final SWTBotShell shell;

  private FileOpenSaveDialogUi( SWTBotShell shell ) {
    this.shell = shell;
  }

  /**
   * Clicks File &gt; Open... and waits for the Open dialog.
   */
  public static FileOpenSaveDialogUi openFromFileMenu( Spoon spoon ) {
    SpoonUi.mainShell( spoon ).menu().menu( "File", "Open..." ).click();
    SWTBotShell dialog = new SWTBot().shell( OPEN_TITLE );
    dialog.activate();
    return new FileOpenSaveDialogUi( dialog );
  }

  /**
   * Types {@code path} into the navigation box above the file list and presses Enter. A folder path selects that
   * folder; a file path also selects the file in the file list.
   */
  public FileOpenSaveDialogUi navigateTo( String path ) {
    SWTBotText navigation = navigationText();
    navigation.setText( path );
    pressEnter( navigation.widget );
    return this;
  }

  /**
   * The text of the navigation box above the file list.
   */
  public String navigationPath() {
    return navigationText().getText();
  }

  /**
   * Names of the files and folders in the file list (Name column), in display order.
   */
  public List<String> fileNames() {
    SWTBotTable table = fileTable();
    List<String> names = new ArrayList<>();
    for ( int row = 0; row < table.rowCount(); row++ ) {
      names.add( table.cell( row, 0 ) );
    }
    return names;
  }

  /**
   * Name of the selected entry in the file list, or null when nothing is selected.
   */
  public String selectedFileName() {
    SWTBotTable table = fileTable();
    return table.selectionCount() == 0 ? null : table.selection().get( 0, 0 );
  }

  public boolean isOpenEnabled() {
    return shell.bot().button( OPEN_BUTTON ).isEnabled();
  }

  /**
   * Double-clicks {@code fileName} in the file list.
   */
  public void doubleClickFile( String fileName ) {
    fileTable().getTableItem( fileName ).doubleClick();
  }

  /**
   * Clicks Open and waits for the dialog to close.
   */
  public void clickOpen() {
    SWTBotButton open = shell.bot().button( OPEN_BUTTON );
    open.click();
    waitUntilClosed();
  }

  /**
   * Clicks Cancel and waits for the dialog to close.
   */
  public void clickCancel() {
    shell.bot().button( CANCEL_BUTTON ).click();
    waitUntilClosed();
  }

  public void waitUntilClosed() {
    SpoonUi.waitFor( "the " + OPEN_TITLE + " dialog to close", () -> !shell.isOpen() );
  }

  public boolean isOpen() {
    return shell.isOpen();
  }

  /**
   * Closes the dialog if a test left it open (for {@code @After} cleanup).
   */
  public void closeIfOpen() {
    if ( shell.isOpen() ) {
      shell.close();
    }
  }

  /**
   * The navigation box is the only bordered Text in the Open dialog (the search box has no border). There is no
   * widget id to look it up by.
   */
  private SWTBotText navigationText() {
    Matcher<Text> borderedText = allOf( widgetOfType( Text.class ), withStyle( SWT.BORDER, "SWT.BORDER" ) );
    return new SWTBotText( shell.bot().widget( borderedText ) );
  }

  private SWTBotTable fileTable() {
    return shell.bot().table();
  }

  /**
   * The navigation box acts on Enter through a TraverseListener. SWTBot's keyboard posts OS-level key events, which
   * need extra OS permissions on macOS, so fire the same traverse event SWT generates for Enter.
   */
  private static void pressEnter( Text text ) {
    VoidResult enter = () -> {
      Event event = new Event();
      event.detail = SWT.TRAVERSE_RETURN;
      event.keyCode = SWT.CR;
      event.character = SWT.CR;
      event.doit = true;
      text.notifyListeners( SWT.Traverse, event );
    };
    UIThreadRunnable.syncExec( text.getDisplay(), enter );
  }
}
