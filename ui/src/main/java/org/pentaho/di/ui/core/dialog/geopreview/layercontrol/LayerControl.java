package org.pentaho.di.ui.core.dialog.geopreview.layercontrol;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.geopreview.Layer;
import org.pentaho.di.ui.core.dialog.geopreview.LayerCollection;
import org.pentaho.di.ui.core.dialog.geopreview.Symbolisation;
import org.pentaho.di.ui.core.util.geo.renderer.swt.LayerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class LayerControl {

    private static Class<?> PKG = Layer.class;

    private Tree table;
    private FormData fd;

    private CheckboxTreeViewer tableViewer;

    private ArrayList<LayerCollection> layerList;

    private String[] columnNames = new String[]{"", BaseMessages.getString( PKG,"PreviewRowsDialog.LayerTreeViewer.ColumnName.Title"), BaseMessages.getString( PKG,"PreviewRowsDialog.LayerTreeViewer.ColumnStyle.Title")};

    public LayerControl(Composite parent, ArrayList<LayerCollection> layerList, FormData fd) {
        this.layerList = layerList;
        this.fd = fd;
        addChildControls(parent);
    }

    private void addChildControls(Composite composite) {
        createTable(composite, fd);
        createTableViewer();
        table.selectAll();
    }

    private void createTable(Composite parent, FormData fd) {
        table = new Tree(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK | SWT.HIDE_SELECTION);
        table.setLayoutData(fd);
        table.setHeaderVisible(true);
    }

    static class StyleEditingSupport extends EditingSupport {
        private Viewer viewer;

        public StyleEditingSupport(ColumnViewer viewer) {
            super(viewer);
            this.viewer = viewer;
        }

        protected boolean canEdit(Object element) {
            return true;
        }

        protected CellEditor getCellEditor(Object element) {
            CellEditor editor = null;
            if (element instanceof Symbolisation) {
                Symbolisation s = (Symbolisation) element;
                int usage = s.getStyleUsage();
                if (usage == Symbolisation.PointColor || usage == Symbolisation.PolygonFillColor || usage == Symbolisation.LineStrokeColor || usage == Symbolisation.PolygonStrokeColor)
                    editor = new ColorCellEditor((Composite) viewer.getControl());
                else
                    editor = new TextCellEditor((Composite) viewer.getControl());
            }
            return editor;
        }

        protected Object getValue(Object element) {
            return ((Symbolisation) element).getFeatureStyle();
        }

        protected void setValue(Object element, Object value) {
            double val = 0;
            Symbolisation current = (Symbolisation) element;
            int usage = current.getStyleUsage();
            if (usage == Symbolisation.PolygonOpacity || usage == Symbolisation.PointOpacity || usage == Symbolisation.LineOpacity) {
                try {
                    val = Double.parseDouble((String) value);
                    if (val > 1)
                        value = this.getValue(current);
                } catch (Exception e) {
                    value = this.getValue(current);
                }
            } else if (usage == Symbolisation.LineStrokeWidth || usage == Symbolisation.PolygonStrokeWidth || usage == Symbolisation.Radius) {
                try {
                    Integer.parseInt((String) value);
                } catch (Exception e) {
                    value = this.getValue(current);
                }
            }
            if (!current.isCustom()) {
                current.setIsCustom(true);
                ((CheckboxTreeViewer) viewer).setChecked(current, current.isCustom());
            }
            current.setLastFeatureStyle(value);
            current.setFeatureStyle(value);
            current.updateParent();
            viewer.refresh();
        }
    }

    private void createTableViewer() {
        tableViewer = new CheckboxTreeViewer(table);
        tableViewer.setUseHashlookup(true);

        TreeViewerColumn cNames = new TreeViewerColumn(tableViewer, SWT.CENTER);
        cNames.getColumn().setText(BaseMessages.getString( PKG,"PreviewRowsDialog.LayerTreeViewer.GeometryFields"));

        TreeViewerColumn cLegend = new TreeViewerColumn(tableViewer, SWT.CENTER);
        cLegend.getColumn().setText(BaseMessages.getString( PKG,"PreviewRowsDialog.LayerTreeViewer.Legend"));
        cLegend.setEditingSupport(new StyleEditingSupport(tableViewer));

        for (int i = 0, n = table.getColumnCount(); i < n; i++) {
            table.getColumn(i).setWidth(200);
        }

        tableViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getElement() instanceof LayerCollection)
                    ((LayerCollection) event.getElement()).setVisible(event.getChecked(), true);
                else if (event.getElement() instanceof Layer) {
                    ((Layer) event.getElement()).setVisible(event.getChecked());
                    if (event.getChecked()) {
                        LayerCollection lc = ((Layer) event.getElement()).getLayerCollectionParent();
                        if (!lc.isVisible()) {
                            lc.setVisible(event.getChecked(), false);
                            tableViewer.setChecked(lc, event.getChecked());
                        }
                    }
                } else {
                    Symbolisation sym = (Symbolisation) event.getElement();
                    sym.setIsCustom(event.getChecked());
                    if (event.getChecked()) {
                        sym.setFeatureStyle(sym.getLastFeatureStyle());
                    } else {
                        int usage = sym.getStyleUsage();
                        if (usage == Symbolisation.LineStrokeColor || usage == Symbolisation.PointColor || usage == Symbolisation.PolygonFillColor || usage == Symbolisation.PolygonStrokeColor)
                            sym.setFeatureStyle((Object) LayerFactory.getDefaultColor());
                        else if (usage == Symbolisation.LineStrokeWidth || usage == Symbolisation.PolygonStrokeWidth)
                            sym.setFeatureStyle(LayerFactory.DEFAULT_STROKE_WIDTH);
                        else if (usage == Symbolisation.Radius)
                            sym.setFeatureStyle(LayerFactory.DEFAULT_RADIUS);
                        else if (usage == Symbolisation.LineOpacity || usage == Symbolisation.PolygonOpacity || usage == Symbolisation.PointOpacity)
                            sym.setFeatureStyle(LayerFactory.DEFAULT_OPACITY);
                    }
                    sym.updateParent();
                    tableViewer.refresh();
                }
            }
        });
        tableViewer.setContentProvider(new LayerTreeContentProvider(layerList, tableViewer));
        tableViewer.setLabelProvider(new LayerLabelProvider(tableViewer));
        tableViewer.setInput(layerList);
        tableViewer.setAllChecked(true);
    }

    public List<String> getColumnNames() {
        return Arrays.asList(columnNames);
    }

    public ISelection getSelection() {
        return tableViewer.getSelection();
    }

    public Control getControl() {
        return table;
    }
}