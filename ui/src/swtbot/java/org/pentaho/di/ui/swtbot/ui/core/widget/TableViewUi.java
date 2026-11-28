package org.pentaho.di.ui.swtbot.ui.core.widget;

import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

/**
 * A Kettle {@link TableView} grid, edited the way a user does: click a cell to open its editor (a text box or a combo
 * box placed over the cell), set the value, and press Enter. Pressing Down Arrow in the last row adds a row.
 * <p>
 * Column 0 is the row number; data columns start at 1, in the order of the dialog's {@code ColumnInfo} array.
 */
public final class TableViewUi {
  private static final long EDITOR_TIMEOUT_MS = 10000;
  private static final long EDITOR_INTERVAL_MS = 100;

  private final SWTBot bot;
  private final Table table;

  private TableViewUi( SWTBot bot, Table table ) {
    this.bot = bot;
    this.table = table;
  }

  /**
   * The first {@link TableView} found by {@code bot}, e.g. a dialog shell's bot.
   */
  public static TableViewUi in( SWTBot bot ) {
    TableView tableView = bot.widget( widgetOfType( TableView.class ) );
    return new TableViewUi( bot, tableView.table );
  }

  public int rowCount() {
    Result<Integer> count = table::getItemCount;
    return UIThreadRunnable.syncExec( table.getDisplay(), count );
  }

  /**
   * Clicks the cell, enters {@code value} in its editor, and presses Enter. Adds rows first if {@code row} is past
   * the end.
   */
  public void setCell( int row, int column, String value ) {
    while ( rowCount() <= row ) {
      appendRow();
    }
    Control editor = openEditor( row, column );
    UIThreadRunnable.syncExec( table.getDisplay(), () -> {
      Control input = inputOf( editor );
      if ( input instanceof Text ) {
        ( (Text) input ).setText( value );
      } else if ( input instanceof CCombo ) {
        ( (CCombo) input ).setText( value );
      } else {
        throw new IllegalStateException( "unsupported cell editor " + input.getClass().getName() );
      }
      pressKey( input, SWT.CR, SWT.CR );
    } );
  }

  /**
   * Text of a cell as the grid shows it.
   */
  public String cell( int row, int column ) {
    Result<String> text = () -> table.getItem( row ).getText( column );
    return UIThreadRunnable.syncExec( table.getDisplay(), text );
  }

  /**
   * Opens the editor of the last row's first column and presses Down Arrow, which adds an empty row.
   */
  private void appendRow() {
    int before = rowCount();
    Control editor = openEditor( before - 1, 1 );
    UIThreadRunnable.syncExec( table.getDisplay(), () -> pressKey( inputOf( editor ), (char) 0, SWT.ARROW_DOWN ) );
    bot.waitUntil( new DefaultCondition() {
      @Override
      public boolean test() {
        return rowCount() == before + 1;
      }

      @Override
      public String getFailureMessage() {
        return "Timed out waiting for Down Arrow to add row " + ( before + 1 );
      }
    }, EDITOR_TIMEOUT_MS, EDITOR_INTERVAL_MS );
  }

  /**
   * Clicks the cell and returns the editor control TableView places over it.
   */
  private Control openEditor( int row, int column ) {
    Result<Set<Control>> children = () -> new HashSet<>( Arrays.asList( table.getChildren() ) );
    Set<Control> before = UIThreadRunnable.syncExec( table.getDisplay(), children );
    new SWTBotTable( table ).click( row, column );
    AtomicReference<Control> editor = new AtomicReference<>();
    bot.waitUntil( new DefaultCondition() {
      @Override
      public boolean test() {
        Result<Control> created = () -> {
          for ( Control child : table.getChildren() ) {
            if ( !before.contains( child ) && !child.isDisposed() ) {
              return child;
            }
          }
          return null;
        };
        editor.set( UIThreadRunnable.syncExec( table.getDisplay(), created ) );
        return editor.get() != null;
      }

      @Override
      public String getFailureMessage() {
        return "Timed out waiting for the editor of cell (" + row + ", " + column + ")";
      }
    }, EDITOR_TIMEOUT_MS, EDITOR_INTERVAL_MS );
    return editor.get();
  }

  /** The widget that takes input and key presses inside a cell editor. Call on the UI thread. */
  private static Control inputOf( Control editor ) {
    if ( editor instanceof TextVar ) {
      return ( (TextVar) editor ).getTextWidget();
    }
    if ( editor instanceof ComboVar ) {
      return ( (ComboVar) editor ).getCComboWidget();
    }
    return editor;
  }

  /** Sends a key press to {@code control}. Call on the UI thread. */
  private static void pressKey( Control control, char character, int keyCode ) {
    Event event = new Event();
    event.widget = control;
    event.display = control.getDisplay();
    event.character = character;
    event.keyCode = keyCode;
    event.doit = true;
    control.notifyListeners( SWT.KeyDown, event );
  }
}
