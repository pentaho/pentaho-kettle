package org.pentaho.di.ui.swtbot.ui.spoon;

import org.pentaho.di.ui.spoon.Spoon;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

/**
 * The "Design" tab of Spoon's left panel: a search box above a tree of step folders ("Input", "Flow", ...). The tree
 * lists steps only while a transformation is the active editor tab. Step items show the plugin name, e.g.
 * "Generate rows" (lowercase r) and "Dummy (do nothing)".
 */
public final class SpoonDesignTabUi {
  private static final String DESIGN_TAB = "Design";

  private final Spoon spoon;
  private final Tree tree;
  private final Text searchBox;

  private SpoonDesignTabUi( Spoon spoon, SWTBot bot ) {
    this.spoon = spoon;
    this.tree = bot.widget( widgetOfType( Tree.class ) );
    this.searchBox = bot.widget( widgetOfType( Text.class ) );
  }

  /**
   * Clicks the "Design" tab and returns it.
   */
  public static SpoonDesignTabUi open( Spoon spoon ) {
    SWTBotCTabItem tab = new SWTBot( spoon.getShell() ).cTabItem( DESIGN_TAB );
    tab.activate();
    Result<Control> control = tab.widget::getControl;
    return new SpoonDesignTabUi( spoon, new SWTBot( SpoonUi.onUiThread( spoon, control ) ) );
  }

  /**
   * Empties the search boxes of the View and Design tabs, so the next scenario sees the full trees.
   */
  public static void clearSearch( Spoon spoon ) {
    Result<Boolean> clear = () -> {
      spoon.clearSearchFilter();
      return true;
    };
    SpoonUi.onUiThread( spoon, clear );
  }

  /**
   * Types {@code text} in the search box. Spoon filters the tree and expands every folder.
   */
  public void search( String text ) {
    new SWTBotText( searchBox ).setText( text );
  }

  /**
   * Expands {@code folder} and selects its {@code step} item, ready to drag.
   */
  public SWTBotTreeItem selectStep( String folder, String step ) {
    SpoonUi.waitFor( "'" + step + "' under '" + folder + "' in the Design tab",
      () -> stepNames( folder ).contains( step ) );
    SWTBotTreeItem item = new SWTBotTree( tree ).getTreeItem( folder ).expand().getNode( step );
    item.select();
    return item;
  }

  /**
   * Names of the step items under {@code folder}; empty when the folder is not listed.
   */
  public List<String> stepNames( String folder ) {
    Result<List<String>> names = () -> {
      List<String> result = new ArrayList<>();
      for ( TreeItem folderItem : tree.getItems() ) {
        if ( folderItem.getText().equals( folder ) ) {
          for ( TreeItem stepItem : folderItem.getItems() ) {
            result.add( stepItem.getText() );
          }
        }
      }
      return result;
    };
    return SpoonUi.onUiThread( spoon, names );
  }

  /**
   * Names of the step items under every folder.
   */
  public List<String> allStepNames() {
    Result<List<String>> names = () -> {
      List<String> result = new ArrayList<>();
      for ( TreeItem folderItem : tree.getItems() ) {
        for ( TreeItem stepItem : folderItem.getItems() ) {
          result.add( stepItem.getText() );
        }
      }
      return result;
    };
    return SpoonUi.onUiThread( spoon, names );
  }
}
