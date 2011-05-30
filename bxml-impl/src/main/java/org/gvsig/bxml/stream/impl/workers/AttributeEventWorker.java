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
 * TODO: document AttributeEventWorker
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class AttributeEventWorker extends EventTypeWorker {

    /**
     * @return {@link EventType#ATTRIBUTE}
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#getEventType(org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    @Override
    public EventType getEventType(final ParseState state) {
        return EventType.ATTRIBUTE;
    }

    /**
     * Reads the String table reference from the stream and sets the current attribute name, and
     * then parses the coalesced attribute value and stores the name/value pair in the
     * {@code sharedState}
     * 
     * @pre {sharedState.getCurrentTokenType() == AttributeStart}
     * @param stream
     * @param sharedState
     * @return the worker for the next ATTRIBUTE or ATTRIBUTES_END event
     * @throws IOException
     * @throws {@link IllegalStateException} if the next worker after this attribut value is not for
     *         an ATTRIBUTE or ATTRIBUTES_END event
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#initImpl(org.gvsig.bxml.stream.io.BxmlInputStream,
     *      org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    @Override
    protected EventTypeWorker initImpl(final BxmlInputStream stream, final ParseState sharedState)
            throws IOException {
        if (TokenType.AttributeStart != sharedState.getCurrentTokenType()) {
            throw new IllegalStateException("Expected current token AttributeStart, got "
                    + sharedState.getCurrentTokenType());
        }
        final long nameRef = stream.readCount();
        final String currentAttributeName = sharedState.getStringTable().get(nameRef);
        StringBuilder valueHolder = new StringBuilder();

        final EventTypeWorker worker = parseAttributeValue(stream, sharedState, valueHolder);

        final String coalescedAttributeValue = valueHolder.toString();

        sharedState.addAttribute(currentAttributeName, coalescedAttributeValue);

        return worker;
    }

    private EventTypeWorker parseAttributeValue(final BxmlInputStream stream,
            final ParseState sharedState, final StringBuilder valueHolder) throws IOException {
        EventType eventType;
        EventTypeWorker nextWorker;

        nextWorker = next(stream, sharedState);
        eventType = nextWorker.getEventType(sharedState);
        while (eventType.isValue()) {
            // parse value...
            CharContentValueConverter valueWorker = (CharContentValueConverter) nextWorker;
            String valueAsString = valueWorker.getValueAsString(stream, sharedState);
            valueHolder.append(valueAsString);
            // get next, may or may not be a value token
            nextWorker = next(stream, sharedState);
            eventType = nextWorker.getEventType(sharedState);
        }
        // got the worker for the next non value token, may be an attribute or attributes_end token
        if (eventType != EventType.ATTRIBUTE && eventType != EventType.ATTRIBUTES_END) {
            throw new IllegalStateException(
                    "After an attribute only an ATTRIBUTE or ATTRIBUTES_END event may appear, got "
                            + eventType);
        }

        return nextWorker;
    }
}
