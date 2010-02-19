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
 * TODO: implement CDataSectionEventWorker
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class CDataSectionEventWorker extends AbstractContentEventWorker {

    @Override
    public EventType getEventType(final ParseState state) {
        return EventType.VALUE_CDATA;
    }

    protected EventTypeWorker initImpl(final BxmlInputStream stream, final ParseState sharedState)
            throws IOException {
        sharedState.setValueLength(1);
        sharedState.setValueElementsReadCount(0);
        return this;
    }

    protected EventTypeWorker nextImpl(final BxmlInputStream stream, final ParseState sharedState)
            throws IOException {
        final int valueElementsReadCount = sharedState.getValueElementsReadCount();
        if (valueElementsReadCount == 0) {
            // skip
            final TokenType tokenType = TokenType.CharContent;
            final EventTypeWorker delegate = getWorker(tokenType);
            delegate.initImpl(stream, sharedState);
            return delegate.nextImpl(stream, sharedState);
        }
        // reset value state
        sharedState.setValueLength(0);
        sharedState.setValueElementsReadCount(0);
        return super.nextImpl(stream, sharedState);
    }

    /**
     * @see CharContentValueConverter#getValueAsString(BxmlInputStream, ParseState)
     * @see TokenType#CDataSection
     */
    public String getValueAsString(final BxmlInputStream stream, final ParseState state)
            throws IOException {
        final TokenType tokenType = TokenType.CharContent;
        final EventTypeWorker delegate = getWorker(tokenType);
        delegate.initImpl(stream, state);
        final CharContentValueConverter converter = (CharContentValueConverter) delegate;
        String stringValue = converter.getValueAsString(stream, state);
        return stringValue;
    }
}
