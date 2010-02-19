package org.gvsig.bxml.util;

import org.gvsig.bxml.stream.EventType;

public class ProgressListenerAdapter implements ProgressListener {

    protected boolean cancelled = false;

    /**
     * @see org.gvsig.bxml.util.ProgressListener#isCancelled()
     */
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * @see org.gvsig.bxml.util.ProgressListener#event(org.gvsig.bxml.stream.EventType)
     */
    public void event(EventType event) {
        // do nothing, override as appropriate
    }

    /**
     * @see org.gvsig.bxml.util.ProgressListener#end()
     */
    public void end() {
        // do nothing, override as appropriate
    }

    /**
     * @see org.gvsig.bxml.util.ProgressListener#start()
     */
    public void start() {
        // do nothing, override as appropriate
    }

}
