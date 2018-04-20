package org.pentaho.di.ui.trans.steps.srstransformation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.trans.steps.setsrs.SRSList;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * {@link SRSTreeView} encapsulates the creation of the <code>Tree</code> in a
 * separate {@link Thread}. {@link SRSTreeView} needs the two root
 * {@link TreeItem} which are extended by their children nodes. The list of all
 * available SRS (delivered by GeoTools library) is created only once in a
 * separate thread ({@link SRSList}). All instances of {@link SRSTreeView}
 * have to wait until this worker-thread is completed.
 * 
 * @author phobus, sgoldinger
 * @since 12-nov-2008
 */
public class SRSTreeView extends Thread {
	public static final String SELECTION_CODE = "code";
	public static final String SELECTION_FACTORY = "FACTORY";
	private Tree tree;
	private HashMap<String, TreeItem> selectionMap;
	private Object mutex = new Object();
	private SRSTreeFiller treeFiller;

	/**
	 * Create a new instance of {@link SRSTreeView} that is a {@link Thread} and
	 * start it.
	 * 
	 * @param srsList The {@link Thread} that creates the list with all available SRS.
	 */
	public SRSTreeView(SRSList srsList, Tree tree, String selectedNodeText) {
		this.tree = tree;
		this.treeFiller = new SRSTreeFiller(srsList.getAllSRS(), tree, selectedNodeText);
		start();
	}

	public Map<String, String> getSRSMap(){
		return treeFiller.getSRSMap();
	}
	
	public Map<String, SRS> getDataMap(){
		return treeFiller.getDataMap();
	}
	
	public void run() {
		Display display = Display.getDefault();
		display.asyncExec(treeFiller);
	}

	public void markSelection(String selectedNodeText) {
		synchronized (mutex) {
			TreeItem selectedNode = selectionMap != null ? selectionMap.get(selectedNodeText) : null;
			if(!tree.isDisposed()) { 
				if (selectedNode != null) {
					tree.setSelection(selectedNode);
					Event e = new Event();
					e.item = selectedNode;
					e.type = SWT.Selection;
					tree.notifyListeners(SWT.Selection, e);
				} else 
					tree.deselectAll();
			}
		}
	}

	/**
	 * This helper-class is an instance of a {@link Runnable} that can be invoked by the GUI
	 * to fill-up the the <code>Tree</code> with a list of all {@link CoordinateReferenceSystem}
	 * provided via the constructor-call.
	 * 
	 * @author phobus, sgoldinger
	 * @since 15-nov-2008
	 */
	class SRSTreeFiller implements Runnable {
		private Tree tree;
		private TreeSet<SRS> treeEntries;
		private String selection;
		private Map<String, String> srsMap;
		private Map<String, SRS> dataMap;

		/**
		 * Creates a new {@link SRSTreeFiller} instance that can be invoked by a GUI thread.
		 * 
		 * @param treeEntries {@link TreeItem}s that should be added to a root-{@link TreeItem}.
		 * @param parent The parent {@link TreeItem}.
		 * @param selection The text of the {@link TreeItem} that should be selected.
		 */
		public SRSTreeFiller(TreeSet<SRS> treeEntries, Tree tree, String selection) {
			this.treeEntries = treeEntries;
			this.tree = tree;
			this.selection = selection;
			this.srsMap = new HashMap<String, String>(treeEntries.size());
			this.dataMap = new HashMap<String, SRS>(treeEntries.size());
		}
		
		public Map<String, String> getSRSMap(){
			return srsMap;
		}
		
		public Map<String, SRS> getDataMap(){
			return dataMap;
		}
		
		public void run() {
			// The selectionMap is used to quickly select an item. By using a HashMap
			// more memory is used, but the GUI thread is blocked for a much shorter time.
			// This trick allows selection with constant time consumption.
			boolean selected = (selection != null);

			synchronized (mutex) {
				selectionMap = selected ? new HashMap<String, TreeItem>() : null;
				for (SRS entry : treeEntries) {
					if (!tree.isDisposed()) {
						TreeItem child = new TreeItem(tree, SWT.NONE);
						String name = entry.description;
						StringBuffer code = new StringBuffer(entry.authority);
						code.append(":");
						code.append(entry.srid);
						child.setText(0, name);
						child.setText(1, code.toString());
						srsMap.put(name, code.toString());
						child.setData(entry);
						dataMap.put(name, entry);
						if (selected)
							selectionMap.put(entry.description, child);
					} else 
						break;
				}
			}

			if (selected)
				markSelection(selection);
		}
	}
}