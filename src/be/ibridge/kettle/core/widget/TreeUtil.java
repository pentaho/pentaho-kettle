package be.ibridge.kettle.core.widget;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class TreeUtil
{
    public static final void setOptimalWidthOnColumns(Tree tree)
    {
        int nrCols = tree.getColumnCount();
        int[] max = new int[nrCols];
        Image image = new Image(tree.getDisplay(), 10, 10);
        GC gc = new GC(image);
        
        for (int i=0;i<max.length;i++)
        {
            TreeColumn treeColumn = tree.getColumn(i); 
            Point point = gc.textExtent(treeColumn.getText());
            max[i] = point.x;
        }
        
        getMaxWidths(tree.getItems(), max, gc);
        
        gc.dispose();
        image.dispose();
        
        for (int i=0;i<max.length;i++)
        {
            TreeColumn treeColumn = tree.getColumn(i); 
            treeColumn.setWidth(max[i]+20);
        }
    }

    private static final void getMaxWidths(TreeItem[] items, int[] max, GC gc)
    {
        for (int i=0;i<items.length;i++)
        {
            for (int c=0;c<max.length;c++)
            {
                String string = items[i].getText(c);
                Point point = gc.textExtent(string);
                if (point.x>max[c]) max[c]=point.x;
            }
            getMaxWidths(items[i].getItems(), max, gc);
        }
    }
}
