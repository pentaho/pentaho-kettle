package org.pentaho.di.ui.core.util.geo.renderer.swt;

import org.eclipse.swt.graphics.GC;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.opengis.geometry.MismatchedDimensionException;
import org.pentaho.di.ui.core.util.geo.renderer.util.GraphicsConverter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Renderer to a swt graphics
 * 
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class SWTMapRenderer 
{
	final private Color MAP_DEFAULT_BACKGROUND_COLOR = Color.WHITE;
   
	private GC gc;

    protected MapLayer[] layers;
	
    public SWTMapRenderer(GC gc){
		this.gc = gc;
    }	
	
	public void render(MapContext map, ReferencedEnvelope envelope){
		GraphicsConverter graphicsConverter = new GraphicsConverter();
		graphicsConverter.prepareRendering(gc);

		Graphics2D graphics = graphicsConverter.getGraphics2D();
		
		try {
			graphics.setColor(this.MAP_DEFAULT_BACKGROUND_COLOR);
            graphics.fillRect(0,0,gc.getClipping().width, gc.getClipping().height);
            
            GTRenderer renderer = new StreamingRenderer();

            // Set the map on the renderer
            renderer.setContext(map);
            
            // Set the hints
            // Hints provides a way to control low-level details used by Renderers of GeoTools
            RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));
            renderer.setJava2DHints(hints);
            Map<Object, Object> rendererParams = new HashMap<Object, Object>();
            rendererParams.put("optimizedDataLoadingEnabled", Boolean.TRUE);
            renderer.setRendererHints(rendererParams);
            
            renderer.paint(graphics, new Rectangle(gc.getClipping().width, gc.getClipping().height), envelope);
        }catch (FactoryRegistryException ex) {
            ex.printStackTrace();
            return;
        }catch (MismatchedDimensionException ex){
            ex.printStackTrace();
            return;
        } 
		graphicsConverter.render(gc);   	
		return;
	}

	public void setGC(GC gc){
		this.gc = gc;
	}
}