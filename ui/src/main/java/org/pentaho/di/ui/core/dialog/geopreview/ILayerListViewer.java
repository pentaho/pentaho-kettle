package org.pentaho.di.ui.core.dialog.geopreview;

/**
 * Interface for a viewer on the LayerList class
 * 
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public interface ILayerListViewer 
{
	public void addLayerEvent(Layer layer);
	public void removeLayerEvent(Layer Layer);
	public void updateLayerEvent(Layer layer);
}
