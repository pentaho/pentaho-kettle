package org.pentaho.di.ui.swtbot.ui.trans.steps.rowgenerator;

import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.swtbot.ui.core.widget.TableViewUi;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withRegex;

/**
 * The "Generate rows" step dialog ({@code RowGeneratorDialog}). Its labels end with a space ("Step name ", "Limit "),
 * and "Limit" is a {@link TextVar} (a text box with a variable button), so fields are found by the label before them.
 */
public final class RowGeneratorDialogUi {
  public static final String TITLE = "Generate rows";
  /** Fields grid columns; column 0 is the row number. */
  public static final int NAME = 1;
  public static final int TYPE = 2;
  public static final int FORMAT = 3;
  public static final int LENGTH = 4;
  public static final int PRECISION = 5;
  public static final int DECIMAL = 7;
  public static final int VALUE = 9;

  private static final String STEP_NAME_LABEL = "Step name";
  private static final String LIMIT_LABEL = "Limit";
  /** OK is labeled "  &amp;OK  " (padded), so a plain text match does not work. */
  private static final String OK_BUTTON_REGEX = "\\s*&?OK\\s*";

  private final SWTBotShell shell;

  private RowGeneratorDialogUi( SWTBotShell shell ) {
    this.shell = shell;
  }

  /**
   * Waits for the dialog to open and activates it.
   */
  public static RowGeneratorDialogUi await() {
    SWTBotShell shell = new SWTBot().shell( TITLE );
    shell.activate();
    return new RowGeneratorDialogUi( shell );
  }

  public void setStepName( String name ) {
    new SWTBotText( textAfterLabel( STEP_NAME_LABEL ) ).setText( name );
  }

  public void setLimit( String limit ) {
    new SWTBotText( textAfterLabel( LIMIT_LABEL ) ).setText( limit );
  }

  public TableViewUi fields() {
    return TableViewUi.in( shell.bot() );
  }

  /**
   * Clicks OK and waits for the dialog to close.
   */
  public void ok() {
    new SWTBotButton( shell.bot().widget( allOf( widgetOfType( Button.class ), withRegex( OK_BUTTON_REGEX ) ) ) )
      .click();
    shell.bot().waitUntil( Conditions.shellCloses( shell ) );
  }

  /**
   * The text box that follows the label whose trimmed text is {@code label}; for a {@link TextVar}, its inner text box.
   */
  private Text textAfterLabel( String label ) {
    Shell widget = shell.widget;
    Result<Text> text = () -> {
      Control[] children = widget.getChildren();
      for ( int i = 0; i < children.length - 1; i++ ) {
        if ( children[i] instanceof Label && ( (Label) children[i] ).getText().trim().equals( label ) ) {
          Control next = children[i + 1];
          if ( next instanceof TextVar ) {
            return ( (TextVar) next ).getTextWidget();
          }
          if ( next instanceof Text ) {
            return (Text) next;
          }
        }
      }
      throw new IllegalStateException( "no text box after label '" + label + "' in '" + TITLE + "'" );
    };
    return UIThreadRunnable.syncExec( widget.getDisplay(), text );
  }
}
