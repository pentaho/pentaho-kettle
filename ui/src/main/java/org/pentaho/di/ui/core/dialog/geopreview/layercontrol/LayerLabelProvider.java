package org.pentaho.di.ui.core.dialog.geopreview.layercontrol;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.ui.core.dialog.geopreview.Layer;
import org.pentaho.di.ui.core.dialog.geopreview.LayerCollection;
import org.pentaho.di.ui.core.dialog.geopreview.Symbolisation;

/**
 * Label provider for an object of type Layer
 *
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class LayerLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	private CheckboxTreeViewer tableViewer;
	
	public LayerLabelProvider(CheckboxTreeViewer tableViewer){
		this.tableViewer=tableViewer;
	}
	
	public String getColumnText(Object element, int columnIndex) {	
		String label = "";
		if(columnIndex == 0){
			if(element instanceof LayerCollection)
				label = ((LayerCollection)element).getName()+" ("+((LayerCollection)element).getDisplayCount()+")";
			else if(element instanceof Layer){
				tableViewer.setChecked(element, ((Layer)element).isVisible());
				if (((Layer)element).getType()== Layer.POINT_LAYER)
					label = ((Layer)element).labels[Layer.POINT_LAYER]+" ("+((Layer)element).getDisplayCount()+")";
				if (((Layer)element).getType()== Layer.COLLECTION_LAYER)
					label = ((Layer)element).labels[Layer.COLLECTION_LAYER]+" ("+((Layer)element).getDisplayCount()+")";
				if (((Layer)element).getType()== Layer.LINE_LAYER)
					label = ((Layer)element).labels[Layer.LINE_LAYER]+" ("+((Layer)element).getDisplayCount()+")";
				if (((Layer)element).getType()== Layer.POLYGON_LAYER)
					label = ((Layer)element).labels[Layer.POLYGON_LAYER]+" ("+((Layer)element).getDisplayCount()+")";
			}else if (element instanceof Symbolisation){
				tableViewer.setChecked(element, ((Symbolisation)element).isCustom());
				int index = ((Symbolisation)element).getStyleUsage();
				if(((Symbolisation)element).getLayerParent().getType()== Layer.COLLECTION_LAYER)
					index+=10;
				label = ((Symbolisation)element).getUsage(index);
			}
		}else if(columnIndex == 1)
			label = element instanceof Symbolisation ?((Symbolisation)element).getFeatureStyle().toString():"";
		return label;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		Image img = null;
		if (columnIndex == 0){	
			if (element instanceof Symbolisation){
				if((((Symbolisation)element).getStyleUsage()== Symbolisation.PointColor)||(((Symbolisation)element).getStyleUsage()== Symbolisation.PolygonFillColor)||(((Symbolisation)element).getStyleUsage()== Symbolisation.LineStrokeColor) ||(((Symbolisation)element).getStyleUsage()== Symbolisation.PolygonStrokeColor)){
					Display d =  Display.getCurrent();
					PaletteData paletteData = new PaletteData(new RGB[] {(RGB)((Symbolisation)element).getFeatureStyle(), (RGB)((Symbolisation)element).getFeatureStyle()});
					ImageData sourceData = new ImageData(10,10,1,paletteData);
					img = new Image(d,sourceData);
				}
			}
		}			
		return img;
    }

	public boolean getChecked(Object element){
		boolean checked;
		if(element instanceof LayerCollection)
			checked = ((LayerCollection)element).isVisible();
		else if(element instanceof Layer)
			checked = ((Layer)element).isVisible();
		else 
			checked = ((Symbolisation)element).isCustom();
		return checked;		
	}	
	
	public boolean getGrayed(Object element){
		return false;
	}
}