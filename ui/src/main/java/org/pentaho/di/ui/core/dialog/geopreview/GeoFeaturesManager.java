package org.pentaho.di.ui.core.dialog.geopreview;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.eclipse.swt.graphics.RGB;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.pentaho.di.ui.core.util.geo.GeometryConverter;
import org.pentaho.di.ui.core.util.geo.renderer.swt.LayerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

/**
 * 	From the book "Design Patterns - Elements of Reusable Object-Oriented Software"
 *  Define an object that encapsulates how a set of objects interact. Mediator promotes
 * 	loose coupling by keeping objects from referring to each other explicitly, and
 * 	it lets you vary their interaction independently.
 * 
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class GeoFeaturesManager extends Observable implements ILayerListViewer {
	private int canvasWidth;
	private int canvasHeight;
	private ReferencedEnvelope envelope;
	private ReferencedEnvelope layersExtent;
	private ArrayList<LayerCollection> layerList;
	
	private FeatureCollection<SimpleFeatureType, SimpleFeature> pointFeatures;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> lineFeatures;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> polygonFeatures;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> allFeatures;
	
	private int featurePosition;
		 
	private MapContext mapContext;

	private Layer[] orderedLayers;
	private ArrayList<Object> visibleLayerIndexes;
	private int[] bufferMultiplier;
	
	private final double ZOOM_OUT_FACTOR = 2.0; 
	private final double ZOOM_IN_FACTOR = 0.5;

	private final String FEATURE_COLLECTION_ID = "1";

	public GeoFeaturesManager(ArrayList<LayerCollection> layerList){
		this.layerList = layerList; 
		orderedLayers =  new Layer[layerList.size()*4];
		visibleLayerIndexes = new ArrayList<Object>();
		bufferMultiplier = new int[layerList.size()*4];
		
		//order layers to ease getFeatureIndex() method
		Iterator<LayerCollection> itLayerCollection = layerList.iterator();
		int collectionIndex = 0;
		while(itLayerCollection.hasNext()){
			ArrayList<Layer> layers = itLayerCollection.next().getLayers();
			orderedLayers[collectionIndex+ Layer.POINT_LAYER*layerList.size()] = layers.get(Layer.POINT_LAYER);
			orderedLayers[collectionIndex+ Layer.LINE_LAYER*layerList.size()] = layers.get(Layer.LINE_LAYER);
			orderedLayers[collectionIndex+ Layer.POLYGON_LAYER*layerList.size()] = layers.get(Layer.POLYGON_LAYER);
			orderedLayers[collectionIndex+ Layer.COLLECTION_LAYER*layerList.size()] = layers.get(Layer.COLLECTION_LAYER);
			collectionIndex++;			
		}				
	}	

	public void setCanvasSize(int width, int height){
		canvasWidth = width;
		canvasHeight = height;
		if(envelope!=null)envelope = lockAspectRatio(envelope);
		setChanged();
		notifyObservers();
	}

	public void move(int diffXOnScreen, int diffYOnScreen){
		double diffX = (envelope.getMaxX() - envelope.getMinX()) * diffXOnScreen / getCanvasWidth(); 
		double diffY = -(envelope.getMaxY() - envelope.getMinY()) * diffYOnScreen / getCanvasHeight();
		
		double minX = envelope.getMinX()-diffX;
		double maxX =  envelope.getMaxX()-diffX;
		double minY = envelope.getMinY()-diffY;
		double maxY =  envelope.getMaxY()-diffY;
		
		envelope = new ReferencedEnvelope();
		envelope.init( minX, maxX, minY, maxY);	
		
		setChanged();
		notifyObservers();
	}

	public int getCanvasWidth(){
		return canvasWidth;
	}

	public int getCanvasHeight(){
		return canvasHeight;
	}
	
	public ReferencedEnvelope getEnvelope(){
		//updateGeoFeatures();
		return envelope;
	}

	public int getGeometryCount(){
		//updateGeoFeatures();
		return allFeatures==null?-1:allFeatures.size();
	}
	
	public MapContext getMapContext() {
		//updateGeoFeatures();		
		return mapContext;
	}

	public int getFeaturePosition(){
		return featurePosition;
	}
	
	public void updateGeoFeatures(){
		GeometryWrapper geom;
    	SimpleFeature feature;
    	String lineStrokeColor=""; 
    	String polygonStrokeColor=""; 
    	String polygonFillColor="";
    	String pointColor="";
    	String pointOpacity="";
    	String lineOpacity="";
    	String polygonOpacity="";
    	String radius="";
    	String lineStrokeWidth="";   
    	String polygonStrokeWidth=""; 
    	int collectionIndex = 0;
    	int minimumBufferMultiplier = 2;
    	
    	LayerFactory layerFactory = new LayerFactory();
    	MapLayer allFeatureLayer = null;
		
		allFeatures = new DefaultFeatureCollection(FEATURE_COLLECTION_ID, GeometryConverter.featureType);
		if(mapContext!=null)
			mapContext.dispose();
		mapContext = layerFactory.createMapContext();
		
		Iterator<LayerCollection> i = getCollections().iterator();
				
		while(i.hasNext()){           
			LayerCollection lc= i.next();
			ArrayList<Layer> layers = lc.getLayers();
			Iterator<Layer> itLayers = layers.iterator();
			
			while(itLayers.hasNext()){
				pointFeatures = null;
				lineFeatures = null;
				polygonFeatures = null;
				Layer layer = itLayers.next();
				List<Symbolisation> featureSymbolisation=layer.getStyle();
				Iterator<Symbolisation> itSymbol=featureSymbolisation.iterator();
							 
				while (itSymbol.hasNext()){
					Symbolisation s=itSymbol.next();
					
					RGB RGBColor;
					Color AWTColor;
					
					switch (s.getStyleUsage()){
						case Symbolisation.LineStrokeColor:
							RGBColor=(RGB)s.getFeatureStyle();
							AWTColor=new Color(RGBColor.red,RGBColor.green,RGBColor.blue);					
							lineStrokeColor= "#"+Integer.toHexString(AWTColor.getRGB()).substring(2);
							break;
						case Symbolisation.PolygonStrokeColor:
							RGBColor=(RGB)s.getFeatureStyle();
							AWTColor=new Color(RGBColor.red,RGBColor.green,RGBColor.blue);					
							polygonStrokeColor= "#"+Integer.toHexString(AWTColor.getRGB()).substring(2);
							break;
						case Symbolisation.LineStrokeWidth:
							lineStrokeWidth=(String)s.getFeatureStyle();
							break;
						case Symbolisation.PolygonStrokeWidth:
							polygonStrokeWidth=(String)s.getFeatureStyle();
							break;
						case Symbolisation.PolygonFillColor:
							RGBColor=(RGB)s.getFeatureStyle();
							AWTColor=new Color(RGBColor.red,RGBColor.green,RGBColor.blue);				
							polygonFillColor= "#"+Integer.toHexString(AWTColor.getRGB()).substring(2);
							break;
						case Symbolisation.PointColor:
							RGBColor=(RGB)s.getFeatureStyle();
							AWTColor=new Color(RGBColor.red,RGBColor.green,RGBColor.blue);				
							pointColor= "#"+Integer.toHexString(AWTColor.getRGB()).substring(2);
							break;
						case Symbolisation.PointOpacity:
							pointOpacity=(String)s.getFeatureStyle();
							break;
						case Symbolisation.LineOpacity:
							lineOpacity=(String)s.getFeatureStyle();
							break;
						case Symbolisation.PolygonOpacity:
							polygonOpacity=(String)s.getFeatureStyle();
							break;
						case Symbolisation.Radius:
							radius=(String)s.getFeatureStyle();
							break;
					}
				}
					
				if (layer.isVisible() == true){
					if(!visibleLayerIndexes.contains((Object) (collectionIndex+layerList.size()*layer.getType())))
						visibleLayerIndexes.add((Object) (collectionIndex+layerList.size()*layer.getType()));
									
					for (int x = 0; x < layer.getGeometryCount(); x++){	    			
		    			geom = layer.getGeometry(x);	    			
		    			if (geom.getJTSGeom() instanceof MultiPoint || geom.getJTSGeom() instanceof Point){
		    				if (pointFeatures == null)
		    					pointFeatures = new DefaultFeatureCollection(FEATURE_COLLECTION_ID, GeometryConverter.featureType);
		    				feature = geom.getGeotoolsFeature();
	    					
	    					if (feature != null){	
	    						pointFeatures.add(feature);
	    						allFeatures.add(feature);					    						
	    					}
		    			}else if (geom.getJTSGeom() instanceof MultiLineString || geom.getJTSGeom() instanceof LineString){
		    				if (lineFeatures == null)
		    					lineFeatures = new DefaultFeatureCollection(FEATURE_COLLECTION_ID, GeometryConverter.featureType);
		    				feature = geom.getGeotoolsFeature();
	    					
	    					if (feature != null){	
	    						lineFeatures.add(feature);
	    						allFeatures.add(feature);					    						
	    					}
		    			}else if (geom.getJTSGeom() instanceof MultiPolygon || geom.getJTSGeom() instanceof Polygon){
		    				if (polygonFeatures == null)
		    					polygonFeatures = new DefaultFeatureCollection(FEATURE_COLLECTION_ID, GeometryConverter.featureType);
		    				feature = geom.getGeotoolsFeature();
	    					
	    					if (feature != null){	
	    						polygonFeatures.add(feature);
	    						allFeatures.add(feature);					    						
	    					}
		    			}	    			
		    		}	    		
				}else{
					if(visibleLayerIndexes.contains((Object) (collectionIndex+layerList.size()*layer.getType())))
						visibleLayerIndexes.remove((Object) (collectionIndex+layerList.size()*layer.getType()));					
				}
				
		    	try {	            
		            if (pointFeatures != null && !pointFeatures.isEmpty()){
		            	MapLayer pointLayer = new MapLayer(pointFeatures, layerFactory.createDefaultPointLayerStyle(radius, pointColor, pointOpacity));
		    			mapContext.addLayer(pointLayer);
		    			bufferMultiplier[collectionIndex+ Layer.POINT_LAYER*layerList.size()]=Integer.parseInt(radius)>=minimumBufferMultiplier?Integer.parseInt(radius):minimumBufferMultiplier;
		    		}
		    		if (lineFeatures != null && !lineFeatures.isEmpty()){
		    			MapLayer lineLayer = new MapLayer(lineFeatures, layerFactory.createDefaultLineLayerStyle(lineStrokeWidth, lineStrokeColor, lineOpacity));
		    			mapContext.addLayer(lineLayer);
		    			bufferMultiplier[collectionIndex+ Layer.LINE_LAYER*layerList.size()]=Integer.parseInt(lineStrokeWidth)>=minimumBufferMultiplier?Integer.parseInt(lineStrokeWidth):minimumBufferMultiplier;
		    		}	    		
		    		if (polygonFeatures != null && !polygonFeatures.isEmpty()){
		    			MapLayer polygonLayer = new MapLayer(polygonFeatures, layerFactory.createDefaultPolygonLayerStyle(polygonStrokeColor, polygonStrokeWidth, polygonFillColor, polygonOpacity));
		    			mapContext.addLayer(polygonLayer);
		    		}		
		    	}catch (Exception ex) {
		            ex.printStackTrace();
		            return;	        
		        }
			}			
	    	collectionIndex++;
		}
    	
    	if (allFeatures != null && !allFeatures.isEmpty()){
			allFeatureLayer = new MapLayer(allFeatures, layerFactory.createDefaultLayerStyles());    	
			try {
				layersExtent = lockAspectRatio(new ReferencedEnvelope(allFeatureLayer.getFeatureSource().getBounds()));
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}else
    		return;
    	
        if (envelope == null){
			try {
				envelope = lockAspectRatio(new ReferencedEnvelope(allFeatureLayer.getFeatureSource().getBounds()));
			} catch (Exception e) {
				e.printStackTrace();
			}  
        }
	}

	public ReferencedEnvelope lockAspectRatio(ReferencedEnvelope env){
		double ratioX  = env.getWidth() / canvasWidth;
		double ratioY   = env.getHeight() / canvasHeight;
		double diff, width, height;
		if (ratioY > ratioX){
			width = ratioY*canvasWidth;
			diff = (width-env.getWidth())/2;
			env.init(env.getMinX()-diff, env.getMaxX()+diff, env.getMinY(), env.getMaxY());
		}else{
			height = ratioX*canvasHeight;
			diff = (height-env.getHeight())/2;
			env.init(env.getMinX(), env.getMaxX(), env.getMinY()-diff, env.getMaxY()+diff);		
		}
		return env;
	}
	
	public void addLayerEvent(Layer layer) {
		updateGeoFeatures();		
		setChanged();
		notifyObservers();
	}

	public void removeLayerEvent(Layer layer) {
		updateLayerEvent(layer);
	}

	public void updateLayerEvent(Layer layer) {
		updateGeoFeatures();	
		setChanged();
		notifyObservers();
	}	

	public ArrayList<LayerCollection> getCollections(){
		return layerList;
	}

	public void zoomInOnPoint(int x, int y){
		recenterCalculator(x, y);
		
		double newEnvelopewidth = envelope.getWidth() * ZOOM_IN_FACTOR;
		double newEnvelopeHeight = envelope.getHeight() * ZOOM_IN_FACTOR;
		double diffX = (envelope.getWidth() - newEnvelopewidth) / 2;
		double diffY = (envelope.getHeight() - newEnvelopeHeight) / 2;		
		
		double minX = envelope.getMinX()+diffX;
		double maxX =  envelope.getMaxX()-diffX;
		double minY = envelope.getMinY()+diffY;
		double maxY =  envelope.getMaxY()-diffY;
		
		envelope = new ReferencedEnvelope();
		envelope.init(minX, maxX, minY, maxY);	

		setChanged();
		notifyObservers();
	}	

	public void zoomOutOnPoint(int x, int y){		
		recenterCalculator(x, y);
		
		double newEnvelopewidth = envelope.getWidth() * ZOOM_OUT_FACTOR;
		double newEnvelopeHeight = envelope.getHeight() * ZOOM_OUT_FACTOR;
		double diffX = (envelope.getWidth() - newEnvelopewidth) / 2;
		double diffY = (envelope.getHeight() - newEnvelopeHeight) / 2;
		
		double minX = envelope.getMinX()+diffX;
		double maxX =  envelope.getMaxX()-diffX;
		double minY = envelope.getMinY()+diffY;
		double maxY =  envelope.getMaxY()-diffY;
		
		envelope = new ReferencedEnvelope();
		envelope.init( minX, maxX, minY, maxY);	

		setChanged();
		notifyObservers();
	}
	
	public void zoomToLayersExtent(){	
		envelope = layersExtent;		
		setChanged();
		notifyObservers();
	}
	
	public double getX(int x){
		double pixelValueX = envelope.getWidth() / getCanvasWidth();		
		double referencedX = envelope.getMinX() + x * pixelValueX;		
		return referencedX;
	}
	
	public double getY(int y){
		double pixelValueY = envelope.getHeight() / getCanvasHeight();		
		double referencedY = envelope.getMinY() + (canvasHeight-y) * pixelValueY;		
		return referencedY;
	}
	
	private void recenterCalculator(int x, int y){
		double pixelValueX = envelope.getWidth() / getCanvasWidth();
		double pixelValueY = envelope.getHeight() / getCanvasHeight();
		
		double newX = envelope.getMinX() + x * pixelValueX;
		double newY = envelope.getMinY() + (canvasHeight-y) * pixelValueY;
		
		double minX = newX-envelope.getWidth()/2;
		double maxX = newX+envelope.getWidth()/2;
		double minY = newY-envelope.getHeight()/2;
		double maxY = newY+envelope.getHeight()/2;
		
		envelope = new ReferencedEnvelope();
		envelope.init( minX, maxX, minY, maxY);	
	}

	public int getFeatureIndex(int x, int y){		
		double pixelValueX = envelope.getWidth() / getCanvasWidth();
		double pixelValueY = envelope.getHeight() / getCanvasHeight();
		
		double newX = envelope.getMinX() + x * pixelValueX;
		double newY = envelope.getMinY() + (canvasHeight-y) * pixelValueY;
		
		double pixelValueAvg = (pixelValueX + pixelValueY) /2;
		
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
		Coordinate coord = new Coordinate(newX, newY);
		Point point = geometryFactory.createPoint( coord );
		
	    int collectionIndex = -1;
	    int layerIndex = -1;
		int geometryIndex = -1;
		
	    Geometry geom = null;
	    Geometry bufferedGeom = null;
		boolean featureDetected = false;
		
		orderVisibleLayerIndexes();
		
		Iterator<Object> itVisibleLayers= visibleLayerIndexes.iterator();
	
		while(itVisibleLayers.hasNext()&&!featureDetected){
			layerIndex = Integer.parseInt(itVisibleLayers.next().toString());
				
			Layer layer = orderedLayers[layerIndex];

			if(layer.getType()== Layer.COLLECTION_LAYER){
				String lineStrokeWidth = "1";
				String radius = "5";
							
				Iterator<Symbolisation> itSymbol = layer.getStyle().iterator();
				
				while (itSymbol.hasNext()){
					Symbolisation s = itSymbol.next();
					int usage = s.getStyleUsage();
					if(usage== Symbolisation.LineStrokeWidth)
						lineStrokeWidth=(String)s.getFeatureStyle();
					else if(usage== Symbolisation.Radius)
						radius=(String)s.getFeatureStyle();
				}
				
				for(geometryIndex = 0;geometryIndex<layer.getGeometryCount();geometryIndex++){
	        		geom = (Geometry)layer.getGeometry(geometryIndex).getJTSGeom();
	        		int buffer = 0;
	        		if(Point.class.isAssignableFrom(geom.getClass()) || MultiPoint.class.isAssignableFrom(geom.getClass()))
	        			buffer = Integer.parseInt(radius) >= 2?Integer.parseInt(radius):2;
	        		else if(LineString.class.isAssignableFrom(geom.getClass()) || MultiLineString.class.isAssignableFrom(geom.getClass()))
	        			buffer = Integer.parseInt(lineStrokeWidth)>=2?Integer.parseInt(lineStrokeWidth):2;
	        			        			
	        		bufferedGeom = geom.buffer(buffer*pixelValueAvg);
	        		if(bufferedGeom.contains(point)){
	        			collectionIndex = layerIndex - layerList.size() * layer.getType();	
	        			featureDetected = true;
	        			break;
	        		}		        		
	        	}
			}else{
	        	for(geometryIndex = 0;geometryIndex<layer.getGeometryCount();geometryIndex++){
	        		geom = (Geometry)layer.getGeometry(geometryIndex).getJTSGeom();
	        		bufferedGeom = geom.buffer(bufferMultiplier[layerIndex]*pixelValueAvg);
	        		if(bufferedGeom.contains(point)){
	        			collectionIndex = layerIndex - layerList.size() * layer.getType();	
	        			featureDetected = true;
	        			break;
	        		}		        		
	        	}
			}
		}
		return featureDetected?Integer.parseInt(layerList.get(collectionIndex).getFeatureIndexes().get(orderedLayers[layerIndex].getType()).get(geometryIndex).toString()):-1;		
	}
	
	public void recenter(int x, int y){
		recenterCalculator(x, y);
		setChanged();
		notifyObservers();
	}	
	
	public void orderVisibleLayerIndexes(){
		ArrayList<Object> array = new ArrayList<Object>();
		for(int i = 0; i<=3;i++){//3 possible types
			for(int j = layerList.size()-1;j>=0;j--){
				if(visibleLayerIndexes.contains((Object) (j+layerList.size()*i)))
					array.add((Object) (j+layerList.size()*i));
			}
		}		
		visibleLayerIndexes = array;
	}
	
	public void dispose(){
		if(mapContext!=null)
			mapContext.dispose();					
	}
}
