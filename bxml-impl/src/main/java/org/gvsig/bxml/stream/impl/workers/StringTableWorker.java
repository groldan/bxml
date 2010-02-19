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
import org.gvsig.bxml.stream.io.TokenType;

/**
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class StringTableWorker extends EventTypeWorker {

    /**
     * @throws UnsupportedOperationException
     *             always, since this method should never be called by the parse chain, as this
     *             worker does not maps to an event type.
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#getEventType(org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    @Override
    public EventType getEventType(final ParseState state) {
        throw new UnsupportedOperationException(
                "Should not be called since StringTable does not map to an EventType");
    }

    /**
     * Parses the stringtable entry and returns the worker for the
     * {@link #next(BxmlInputStream, ParseState) next()} token, this advancing one token in the
     * parse chain, as this worker does not map to an EventType but just builds a string table
     * segment out of a {@link TokenType#StringTable StringTable} token.
     * 
     * @param stream
     * @param sharedState
     * @return the worker for the next token in the parse chain
     * @throws IOException
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#init(org.gvsig.bxml.stream.io.BxmlInputStream,
     *      org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    @Override
    public EventTypeWorker initImpl(BxmlInputStream stream, final ParseState sharedState)
            throws IOException {
        final long position = stream.getPosition();
        final long nStrings = stream.readCount();
        final StringTable stringTable = sharedState.getStringTable();
        for (long i = 0; i < nStrings; i++) {
            String str = stream.readString();
            stringTable.add(str, position);
        }
        return super.nextImpl(stream, sharedState);
    }

}
