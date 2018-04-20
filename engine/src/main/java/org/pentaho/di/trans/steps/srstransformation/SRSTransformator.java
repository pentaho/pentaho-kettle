package org.pentaho.di.trans.steps.srstransformation;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

/**
 * This class is responsible for the transformation of a source spatial
 * reference system ({@link SRS}) to a target-SRS.
 * 
 * @author phobus, sgoldinger
 * @since 4-nov-2008
 */
public class SRSTransformator {
	/** The source spatial reference system **/
	private final SRS sourceSRS;
	/** The target spatial reference system **/
	private final SRS targetSRS;

	private static LogChannelInterface log = new LogChannel( "GeotoolsReader" );
	/**
	 * Creates an instance of the {@link SRSTransformator} class by defining the
	 * source- and the target {@link SRS}.
	 * 
	 * @param sourceSRS The source spatial reference system ({@link SRS}).
	 * @param targetSRS The target spatial reference system ({@link SRS}).
	 * @throws KettleException
	 */
	public SRSTransformator(final SRS sourceSRS, final SRS targetSRS) {
		this.sourceSRS = sourceSRS;
		this.targetSRS = targetSRS;
	}
	
	/**
	 * Transforms the specified source geometry in the source-{@link SRS} to a geometry
	 * in the target-{@link SRS}.
	 * 
	 * @precondition:  Pre1: The source spatial reference system must be set to start the transformation.
	 * @precondition:  Pre2: The target spatial reference system must be set to start the transformation.
	 * @precondition:  Pre3: The sourceGeometry object is not null.
	 * @precondition:  Pre4: If the source-SRID is unknown, there is a custom specified {@link SRS}.
	 * @precondition:  Pre5: SRID from target-SRS metadata is valid.
	 * 
	 * @param sourceGeometry The geometry object in the source-SRS.
	 * @return The geometry in the target-SRS.
	 * @throws KettleStepException
	 */
	public Geometry transform(Geometry sourceGeometry) throws KettleStepException {
		// checking Pre1
		if (sourceSRS == null || sourceSRS.equals(SRS.UNKNOWN))
			throw new KettleStepException("The source spatial reference system must be set to start the transformation!");
		// checking Pre2
		if (targetSRS == null || targetSRS.equals(SRS.UNKNOWN))
			throw new KettleStepException("The target spatial reference system must be set to start the transformation!");
		// checking Pre3
		if (sourceGeometry == null)
			throw new KettleStepException("The geometry could not be read (null).");
		// checking Pre4
		if (sourceSRS.getSRID() == SRS.UNKNOWN_SRID && !sourceSRS.is_custom)
			throw new KettleStepException("The source SRID is invalid.");
		// checking Pre5
		if (targetSRS.getSRID() == SRS.UNKNOWN_SRID && !targetSRS.is_custom)
			throw new KettleStepException("The target SRID is invalid.");
		
		// Do the transformation
		Geometry targetGeometry = null;
		try {
			MathTransform transform;
			try {
				transform = CRS.findMathTransform(sourceSRS.getCRS(), targetSRS.getCRS());
			}
			catch (FactoryException e) {
				log.logDetailed("Warning: using lenient datum shift (without Bursa Wolf parameters for SRS transform");
				// unable to find a transform, try again allowing a "lenient datum shift"
				// (without bursa wolf parameters)
				transform = CRS.findMathTransform(sourceSRS.getCRS(), targetSRS.getCRS(), true);
			}
			sourceGeometry.setSRID(sourceSRS.getSRID());
			targetGeometry = JTS.transform(sourceGeometry, transform);
			targetGeometry.setSRID(targetSRS.getSRID());
		} catch (Exception e) {
			throw new KettleStepException("The transformation could not be performed. "+e.getMessage());
		}

		log.logDetailed("Spatial transformation succeeded from "+ sourceGeometry.getSRID()+" to "+targetGeometry.getSRID());
		return targetGeometry;
	}
}
