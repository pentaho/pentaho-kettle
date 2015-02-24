package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransMeta;

import java.util.List;

/**
 * An interface to conceal partitions' obtaining routine.
 *
 * @author Andrey Khayrutdinov
 */
public interface PartitionSchemasProvider {

  List<PartitionSchema> getPartitionSchemas( TransMeta transMeta ) throws KettleException;

  List<String> getPartitionSchemasNames( TransMeta transMeta ) throws KettleException;
}
