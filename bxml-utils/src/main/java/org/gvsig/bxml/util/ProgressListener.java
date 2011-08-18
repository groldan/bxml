/**
 * 
 */
package org.gvsig.bxml.util;

import org.gvsig.bxml.stream.EventType;

public interface ProgressListener {

    public ProgressListener NULL = new ProgressListener() {

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void start() {
        }

        @Override
        public void end() {
        }

        @Override
        public void event(EventType event) {
        }
    };

    /**
     * Notifies the subject to cancel processing
     * 
     * @return whether to cancel processing or not
     */
    public boolean isCancelled();

    public void start();

    public void end();

    public void event(final EventType event);
}