/* gvSIG. Sistem a de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2007 Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 9638 62 495
 *      gvsig@gva.es
 *      www.gvsig.gva.es
 */
package org.gvsig.bxml.stream.impl.workers;

import java.io.IOException;

import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.stream.io.BxmlInputStream;

/**
 * TODO: implement SpaceEventWorker
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class SpaceEventWorker extends AbstractContentEventWorker {

    /**
     * @return {@link EventType#SPACE}
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#getEventType(org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    public EventType getEventType(final ParseState state) {
        return EventType.SPACE;
    }

    /**
     * Overrides to mark the content of this content event worker to be not yet consumed.
     * <p>
     * That is done by setting the {@code sharedState} valueLength to 1 and valueElementsReadCount
     * to zero.
     * </p>
     * 
     * @return {@code this}
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#initImpl(org.gvsig.bxml.stream.io.BxmlInputStream,
     *      org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    @Override
    protected EventTypeWorker initImpl(final BxmlInputStream stream, final ParseState sharedState)
            throws IOException {

        sharedState.setValueLength(1);
        sharedState.setValueElementsReadCount(0);
        return this;
    }

    /**
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#nextImpl(org.gvsig.bxml.stream.io.BxmlInputStream,
     *      org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    protected EventTypeWorker nextImpl(final BxmlInputStream stream, final ParseState state)
            throws IOException {
        // was the content read?
        final int readCount = state.getValueElementsReadCount();
        if (readCount == 0) {
            // skip
            stream.readCount();
            stream.skipString();
        } else if (readCount != 1) {
            throw new IllegalStateException(
                    "State inconsistency found, read count should be only 0 or 1 for this worker: "
                            + readCount);
        }

        state.setValueLength(0);
        state.setValueElementsReadCount(0);
        return super.nextImpl(stream, state);
    }

    /**
     * Reads the whitespace content as a String and notifies the ParseState the value has been read.
     * 
     * @see CharContentValueConverter#getValueAsString(BxmlInputStream, ParseState)
     */
    public String getValueAsString(final BxmlInputStream stream, final ParseState state)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        final long nBlankLines = stream.readCount();
        for (long i = 0; i < nBlankLines; i++) {
            sb.append('\n');
        }
        final String stringContent = stream.readString();
        sb.append(stringContent);
        state.notifyValueRead(1);
        return sb.toString();
    }

}
