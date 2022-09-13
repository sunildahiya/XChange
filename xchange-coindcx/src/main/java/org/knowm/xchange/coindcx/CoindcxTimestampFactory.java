package org.knowm.xchange.coindcx;

import si.mazi.rescu.SynchronizedValueFactory;

public class CoindcxTimestampFactory implements SynchronizedValueFactory<Long> {

    @Override
    public Long createValue() {
        return System.currentTimeMillis();
    }
}
