package org.pentaho.di.ui.core.dialog.geopreview;

//import org.geotools.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.pentaho.di.ui.core.util.geo.GeometryConverter;

/**
 * Wrap geometry between Geotools and JTS
 * 
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class GeometryWrapper 
{
	private Geometry jtsGeom;
	private SimpleFeature feature;
	
	public GeometryWrapper(Geometry jtsGeom){
		this.jtsGeom = jtsGeom;		
		this.feature = GeometryConverter.JTSGeomToGeoToolsFeature(jtsGeom);
	}
	
	public Geometry getJTSGeom(){
		return jtsGeom;
	}
	
	public SimpleFeature getGeotoolsFeature(){
		return feature;
	}
}
