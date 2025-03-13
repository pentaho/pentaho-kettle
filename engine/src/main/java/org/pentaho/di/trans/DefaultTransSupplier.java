package org.pentaho.di.trans;

import org.pentaho.di.core.logging.LogChannelInterface;

import java.util.function.Supplier;

public class DefaultTransSupplier implements TransSupplier {
    @Override
    public Trans get( TransMeta transMeta, LogChannelInterface log, Supplier<Trans> fallbackSupplier ) {
        return fallbackSupplier.get();
    }
}
