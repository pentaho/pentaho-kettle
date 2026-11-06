package org.pentaho.di.ui.swtbot.ui.plugins.fileopensave.dialog;

import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.swtbot.ui.spoon.SpoonUi;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.hamcrest.Matcher;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;

/**
 * Drives the file open/save dialog from the file-open-save-new plugin
 * ({@code org.pentaho.di.plugins.fileopensave.dialog.FileOpenSaveDialog}), shown by File &gt; Open and, for a file
 * that has never been saved, File &gt; Save.
 */
public final class FileOpenSaveDialogUi {
  private static final String OPEN_TITLE = "Open";
  private static final String SAVE_AS_TITLE = "Save As";
  private static final String OPEN_BUTTON = "Open";
  private static final String SAVE_BUTTON = "Save";
  private static final String CANCEL_BUTTON = "Cancel";
  private static final String FILE_NAME_LABEL = "File name:";

  private final SWTBotShell shell;
  private final String title;

  private FileOpenSaveDialogUi( SWTBotShell shell, String title ) {
    this.shell = shell;
    this.title = title;
  }

  /**
   * Clicks File &gt; Open... and waits for the Open dialog.
   */
  public static FileOpenSaveDialogUi openFromFileMenu( Spoon spoon ) {
    SpoonUi.mainShell( spoon ).menu().menu( "File", "Open..." ).click();
    return await( OPEN_TITLE );
  }

  /**
   * Clicks File &gt; Save and waits for the Save As dialog. Spoon shows it only for a file that has never been saved;
   * otherwise File &gt; Save writes the file without a dialog.
   */
  public static FileOpenSaveDialogUi saveFromFileMenu( Spoon spoon ) {
    SpoonUi.mainShell( spoon ).menu().menu( "File", "Save" ).click();
    return await( SAVE_AS_TITLE );
  }

  private static FileOpenSaveDialogUi await( String title ) {
    SWTBotShell dialog = new SWTBot().shell( title );
    dialog.activate();
    return new FileOpenSaveDialogUi( dialog, title );
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

  /**
   * Types {@code name} into the "File name:" box of the Save As dialog.
   */
  public FileOpenSaveDialogUi setFileName( String name ) {
    Text fileName = fileNameText();
    if ( fileName == null ) {
      throw new IllegalStateException( "the " + title + " dialog has no '" + FILE_NAME_LABEL + "' box" );
    }
    new SWTBotText( fileName ).setText( name );
    return this;
  }

  /**
   * Waits for Save to be enabled (a folder is selected and a file name is set), clicks it, and waits for the dialog to
   * close.
   */
  public void clickSave() {
    SWTBotButton save = shell.bot().button( SAVE_BUTTON );
    SpoonUi.waitFor( "the " + SAVE_BUTTON + " button to be enabled", save::isEnabled );
    save.click();
    waitUntilClosed();
  }

  public void waitUntilClosed() {
    SpoonUi.waitFor( "the " + title + " dialog to close", () -> !shell.isOpen() );
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
   * The navigation box is the bordered Text that is not the Save As "File name:" box (the search box has no border).
   * There is no widget id to look it up by.
   */
  private SWTBotText navigationText() {
    Matcher<Text> borderedText = allOf( widgetOfType( Text.class ), withStyle( SWT.BORDER, "SWT.BORDER" ) );
    Text fileName = fileNameText();
    for ( Text text : shell.bot().widgets( borderedText ) ) {
      if ( text != fileName ) {
        return new SWTBotText( text );
      }
    }
    throw new IllegalStateException( "the " + title + " dialog has no navigation box" );
  }

  /**
   * The Save As "File name:" box, or null in the Open dialog. The box is created just before its label, so
   * {@code textWithLabel} (which looks after the label) cannot find it.
   */
  private Text fileNameText() {
    Matcher<Label> label = allOf( widgetOfType( Label.class ), withText( FILE_NAME_LABEL ) );
    List<? extends Label> labels = shell.bot().getFinder().findControls( shell.widget, label, true );
    if ( labels.isEmpty() ) {
      return null;
    }
    Label fileNameLabel = labels.get( 0 );
    Result<Text> before = () -> {
      Control[] siblings = fileNameLabel.getParent().getChildren();
      for ( int i = 1; i < siblings.length; i++ ) {
        if ( siblings[i] == fileNameLabel && siblings[i - 1] instanceof Text ) {
          return (Text) siblings[i - 1];
        }
      }
      return null;
    };
    return UIThreadRunnable.syncExec( fileNameLabel.getDisplay(), before );
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
