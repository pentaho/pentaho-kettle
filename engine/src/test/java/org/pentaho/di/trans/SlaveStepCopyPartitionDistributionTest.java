/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.di.trans;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.pentaho.di.partition.PartitionSchema;

import java.util.ArrayList;
import java.util.List;

public class SlaveStepCopyPartitionDistributionTest {

    private SlaveStepCopyPartitionDistribution slaveStep;

    @Before
    public void setup() {

        slaveStep = new SlaveStepCopyPartitionDistribution();
    }

    @Test
    public void equalsNullTest() {

        Assert.assertFalse( slaveStep.equals( null ) );
    }

    @Test
    public void equalsDifferentClassesTest() {

        Assert.assertFalse( slaveStep.equals( Integer.valueOf(5) ) );
    }

    @Test
    public void equalsSameInstanceTest() {

        Assert.assertTrue( slaveStep.equals( slaveStep ) );
    }

    @Test
    public void equalsDifferentStepsTest() {

        SlaveStepCopyPartitionDistribution other = new SlaveStepCopyPartitionDistribution();
        List<PartitionSchema> schemas = new ArrayList<>();
        schemas.add( new PartitionSchema() );
        other.setOriginalPartitionSchemas( schemas );
        Assert.assertFalse( slaveStep.equals( other ) );
    }

    @Test
    public void equalsTest(){

        Assert.assertTrue( slaveStep.equals( new SlaveStepCopyPartitionDistribution() ) );
    }

    @Test
    public void hashCodeEqualsTest() {

        SlaveStepCopyPartitionDistribution other = new SlaveStepCopyPartitionDistribution();

        Assert.assertEquals( slaveStep.hashCode(), other.hashCode() );
    }

    @Test
    public void hashCodeDifferentTest() {

        SlaveStepCopyPartitionDistribution other = new SlaveStepCopyPartitionDistribution();
        List<PartitionSchema> schemas = new ArrayList<>();
        PartitionSchema schema = new PartitionSchema();
        schema.setName( "Test" );
        schemas.add( schema );
        other.setOriginalPartitionSchemas( schemas );

        Assert.assertNotEquals( slaveStep.hashCode(), other.hashCode() );

    }
}
