package org.pentaho.di.trans;

import org.pentaho.di.core.logging.LogChannelInterface;

import java.util.function.Supplier;

public interface TransSupplier {
    Trans get( TransMeta transMeta, LogChannelInterface log, Supplier<Trans> fallbackSupplier );
}
