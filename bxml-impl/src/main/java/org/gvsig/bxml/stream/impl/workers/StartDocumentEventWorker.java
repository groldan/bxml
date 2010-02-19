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

import static org.gvsig.bxml.stream.io.TokenType.XmlDeclaration;

import java.io.IOException;

import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.TokenType;

/**
 * TODO: document StartDocumentEventWorker
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class StartDocumentEventWorker extends EventTypeWorker {

    /**
     * @param state
     * @return {@link EventType#NONE} if parsing has not been yet started,
     *         {@link EventType#START_DOCUMENT} otherwise
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#getEventType(org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    @Override
    public EventType getEventType(final ParseState state) {
        final boolean parsingStarted = state.isParsingStarted();
        if (parsingStarted) {
            return EventType.START_DOCUMENT;
        }
        return EventType.NONE;
    }

    /**
     * Called at the bxml stream readed initialization time, sets up to start parsing.
     * <p>
     * The logic for this special EventTypeWorker will take care of setting up the initial EventType
     * to {@link EventType#NONE NONE} followed by an {@link EventType#START_DOCUMENT START_DOCUMENT}
     * regardless of the {@link TokenType#XmlDeclaration xml declaration} token being present as the
     * first token in the stream or not.
     * </p>
     * <p>
     * Sets the parsing started state to false.
     * </p>
     * 
     * @param stream
     * @param state
     * @return {@code this}
     * @throws IOException
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#init(org.gvsig.bxml.stream.io.BxmlInputStream,
     *      org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    @Override
    public EventTypeWorker initImpl(final BxmlInputStream stream, final ParseState state)
            throws IOException {
        // this should be the fist most token in the stream after the header
        // i
        final TokenType firstTokenType = stream.readTokenType();
        state.setCurrentTokenType(firstTokenType);
        state.setParsingStarted(false);

        String xmlVersion;
        boolean standalone;
        boolean standaloneIsSet;
        if (firstTokenType == XmlDeclaration) {
            xmlVersion = stream.readString();
            standalone = stream.readBoolean();
            standaloneIsSet = stream.readBoolean();
        } else {
            // XmlDeclaration is not the first token, so its not present at all
            // TODO: check the default value for an xml declaration "standalone" property on the xml
            // spec
            standalone = true;
            standaloneIsSet = false;
            xmlVersion = "1.0";
        }
        state.setXmlVersion(xmlVersion);
        state.setStandalone(standalone);
        state.setStandaloneIsSet(standaloneIsSet);
        return this;
    }

    /**
     * @param stream
     * @param state
     * @return
     * @throws IOException
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#next(org.gvsig.bxml.stream.io.BxmlInputStream,
     *      org.gvsig.bxml.stream.impl.workers.ParseState)
     */
    @Override
    public EventTypeWorker nextImpl(final BxmlInputStream stream, final ParseState state)
            throws IOException {

        final boolean parsingStarted = state.isParsingStarted();
        EventTypeWorker nextWorker;
        if (parsingStarted) {
            // if the fist token was XmlDeclaration, its content was parsed and we need to get the
            // next token. If it was not XmlDeclaration, we already have it.
            TokenType currentTokenType = state.getCurrentTokenType();
            if (XmlDeclaration == currentTokenType) {
                currentTokenType = stream.readTokenType();
                state.setCurrentTokenType(currentTokenType);
            }
            nextWorker = getWorker(currentTokenType);
            nextWorker = nextWorker.init(stream, state);
        } else {
            // just mark parsing to be started and return this
            state.setParsingStarted(true);
            nextWorker = this;
        }
        return nextWorker;
    }
}
