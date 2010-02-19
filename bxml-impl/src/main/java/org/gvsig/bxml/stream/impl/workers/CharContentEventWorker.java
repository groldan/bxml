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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.gvsig.bxml.stream.EventType;
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.ValueType;

/**
 * TODO: document CharContentEventWorker
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public final class CharContentEventWorker extends AbstractContentEventWorker {

    private static final Map<ValueType, EventType> valueToEventMapping;
    static {
        Map<ValueType, EventType> events = new HashMap<ValueType, EventType>();
        events.put(ValueType.SmallNum, EventType.VALUE_INT);
        events.put(ValueType.ShortCode, EventType.VALUE_INT);
        events.put(ValueType.UShortCode, EventType.VALUE_INT);
        events.put(ValueType.IntCode, EventType.VALUE_INT);

        events.put(ValueType.ByteCode, EventType.VALUE_BYTE);
        events.put(ValueType.LongCode, EventType.VALUE_LONG);
        events.put(ValueType.FloatCode, EventType.VALUE_FLOAT);
        events.put(ValueType.DoubleCode, EventType.VALUE_DOUBLE);
        events.put(ValueType.BoolCode, EventType.VALUE_BOOL);
        events.put(ValueType.StringCode, EventType.VALUE_STRING);

        valueToEventMapping = Collections.unmodifiableMap(events);
    }

    @Override
    protected EventTypeWorker initImpl(final BxmlInputStream stream, final ParseState state)
            throws IOException {
        // get the value type identifier for this content token
        final int charContentValueTypeCode = stream.readByte();
        final ValueType currentValueType = ValueType.valueOf(charContentValueTypeCode);

        // if its an array, find out the array length and the concrete type for the array elements
        if (ValueType.ArrayCode == currentValueType) {
            final int arrayValueTypeCode = stream.readByte();
            final ValueType arrayValueType = ValueType.valueOf(arrayValueTypeCode);
            final int valueLength = (int) stream.readCount();
            state.setCurrentValueType(arrayValueType);
            state.setValueLength(valueLength);
        } else {
            state.setCurrentValueType(currentValueType);
            state.setValueLength(1);
            // This is sort of hacky, but SmallNums codes are not followed by a value, but the value
            // is the code itself. The reader.getIntValue() method will have to check
            if (ValueType.SmallNum == currentValueType) {
                state.setSmallNumValue(charContentValueTypeCode);
            }
        }
        // reset the number of elements read for a value token
        state.setValueElementsReadCount(0);

        return this;
    }

    @Override
    protected EventTypeWorker nextImpl(final BxmlInputStream stream, final ParseState state)
            throws IOException {
        // was the content of this value event fully read?
        final int valueLength = state.getValueLength();
        final int readCount = state.getValueElementsReadCount();
        if (readCount < valueLength) {
            final int elementsToSkip = valueLength - readCount;
            final ValueType currentValueType = state.getCurrentValueType();
            skipRemainingValues(stream, elementsToSkip, currentValueType);
        } else if (readCount > valueLength) {
            throw new IllegalStateException("The number of elements read (" + readCount
                    + ") can't be larger than the declared element count (" + valueLength + ")");
        }
        // reset the value type, it only makes sense for value events
        state.setCurrentValueType(null);
        state.setValueLength(0);
        state.setValueElementsReadCount(0);

        return super.nextImpl(stream, state);
    }

    /**
     * Skips {@code elementsToSkip} elements from the {@code stream}
     * 
     * @param stream
     *            the stream where to skip content from
     * @param elementsToSkip
     *            how many elements to skip
     * @param currentValueType
     *            the value type enum for the current value event, used to identify the byte length
     *            of a single element of this type in case it represents a primitive type
     * @throws IOException
     */
    private void skipRemainingValues(final BxmlInputStream stream, final int elementsToSkip,
            final ValueType currentValueType) throws IOException {
        if (ValueType.StringCode == currentValueType) {
            if (elementsToSkip != 1) {
                throw new IllegalArgumentException(
                        "There are no such thing as string arrays, so the elements"
                                + " to skip shall only be 1: " + elementsToSkip);
            }
            stream.skipString();
        } else if (ValueType.SmallNum == currentValueType) {
            // do nothing, no need to skip
        } else {
            final int byteLength = ValueType.getByteLength(currentValueType);
            final int skipByteCount = byteLength * elementsToSkip;
            stream.skip(skipByteCount);
        }
    }

    /**
     * Returns the mapping {@code EventType.VALUE_XXX} for the current
     * {@link ParseState#getCurrentValueType() value type}.
     * 
     * @return
     * @see org.gvsig.bxml.stream.impl.workers.EventTypeWorker#getEventType(ParseState)
     */
    @Override
    public EventType getEventType(final ParseState state) {
        final ValueType currentValueType = state.getCurrentValueType();
        final EventType eventType = getEventType(currentValueType);
        return eventType;
    }

    private EventType getEventType(final ValueType valueType) {
        final EventType eventType = valueToEventMapping.get(valueType);
        return eventType;
    }

    /**
     * @see CharContentValueConverter#getValueAsString(BxmlInputStream, ParseState)
     */
    public String getValueAsString(final BxmlInputStream stream, final ParseState state)
            throws IOException {
        final ValueType valueType = state.getCurrentValueType();
        String value;
        if (ValueType.StringCode == valueType) {
            value = stream.readString();
            state.notifyValueRead(1);
        } else {
            final int valueLength = state.getValueLength();
            final int readCount = state.getValueElementsReadCount();
            final int remaining = valueLength - readCount;
            value = XmlStreamUtils.parseStringValue(stream, valueType, remaining);
            state.notifyValueRead(remaining);
        }
        return value;
    }
}
