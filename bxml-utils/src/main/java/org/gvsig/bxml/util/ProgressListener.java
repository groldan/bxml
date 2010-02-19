/**
 * 
 */
package org.gvsig.bxml.util;

import org.gvsig.bxml.stream.EventType;

public interface ProgressListener {
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