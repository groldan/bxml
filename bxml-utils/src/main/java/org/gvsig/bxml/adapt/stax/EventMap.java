package org.gvsig.bxml.adapt.stax;

import javax.xml.stream.XMLStreamConstants;

import org.gvsig.bxml.stream.EventType;

class EventMap {

    private static final int[] STAX_CONSTANTS;

    private static final EventType[] BXML_CONSTANTS;

    static {
        STAX_CONSTANTS = new int[EventType.values().length];
        STAX_CONSTANTS[EventType.ATTRIBUTE.ordinal()] = -1;
        STAX_CONSTANTS[EventType.ATTRIBUTES_END.ordinal()] = -1;
        STAX_CONSTANTS[EventType.COMMENT.ordinal()] = XMLStreamConstants.COMMENT;
        STAX_CONSTANTS[EventType.END_DOCUMENT.ordinal()] = XMLStreamConstants.END_DOCUMENT;
        STAX_CONSTANTS[EventType.END_ELEMENT.ordinal()] = XMLStreamConstants.END_ELEMENT;
        STAX_CONSTANTS[EventType.NAMESPACE_DECL.ordinal()] = XMLStreamConstants.NAMESPACE;
        STAX_CONSTANTS[EventType.NONE.ordinal()] = -1;
        STAX_CONSTANTS[EventType.SPACE.ordinal()] = XMLStreamConstants.SPACE;
        STAX_CONSTANTS[EventType.START_DOCUMENT.ordinal()] = XMLStreamConstants.START_DOCUMENT;
        STAX_CONSTANTS[EventType.START_ELEMENT.ordinal()] = XMLStreamConstants.START_ELEMENT;
        STAX_CONSTANTS[EventType.VALUE_BOOL.ordinal()] = -1;
        STAX_CONSTANTS[EventType.VALUE_BYTE.ordinal()] = -1;
        STAX_CONSTANTS[EventType.VALUE_CDATA.ordinal()] = XMLStreamConstants.CDATA;
        STAX_CONSTANTS[EventType.VALUE_DOUBLE.ordinal()] = -1;
        STAX_CONSTANTS[EventType.VALUE_FLOAT.ordinal()] = -1;
        STAX_CONSTANTS[EventType.VALUE_INT.ordinal()] = -1;
        STAX_CONSTANTS[EventType.VALUE_LONG.ordinal()] = -1;
        STAX_CONSTANTS[EventType.VALUE_STRING.ordinal()] = XMLStreamConstants.CHARACTERS;
        // XMLStreamConstants.ENTITY_DECLARATION;??
        // XMLStreamConstants.ENTITY_REFERENCE;
        // XMLStreamConstants.NOTATION_DECLARATION;
        // XMLStreamConstants.PROCESSING_INSTRUCTION;

        BXML_CONSTANTS = new EventType[16];
        BXML_CONSTANTS[XMLStreamConstants.ATTRIBUTE] = null;
        BXML_CONSTANTS[XMLStreamConstants.CDATA] = EventType.VALUE_CDATA;
        BXML_CONSTANTS[XMLStreamConstants.CHARACTERS] = EventType.VALUE_STRING;
        BXML_CONSTANTS[XMLStreamConstants.COMMENT] = EventType.COMMENT;
        BXML_CONSTANTS[XMLStreamConstants.DTD] = null;
        BXML_CONSTANTS[XMLStreamConstants.END_DOCUMENT] = EventType.END_DOCUMENT;
        BXML_CONSTANTS[XMLStreamConstants.END_ELEMENT] = EventType.END_ELEMENT;
        BXML_CONSTANTS[XMLStreamConstants.ENTITY_DECLARATION] = null;
        BXML_CONSTANTS[XMLStreamConstants.ENTITY_REFERENCE] = null;
        BXML_CONSTANTS[XMLStreamConstants.NAMESPACE] = EventType.NAMESPACE_DECL;
        BXML_CONSTANTS[XMLStreamConstants.NOTATION_DECLARATION] = null;
        BXML_CONSTANTS[XMLStreamConstants.PROCESSING_INSTRUCTION] = null;
        BXML_CONSTANTS[XMLStreamConstants.SPACE] = EventType.SPACE;
        BXML_CONSTANTS[XMLStreamConstants.START_DOCUMENT] = EventType.START_DOCUMENT;
        BXML_CONSTANTS[XMLStreamConstants.START_ELEMENT] = EventType.START_ELEMENT;
    }

    public static int staxEvent(EventType event) {
        int ordinal = event.ordinal();
        return STAX_CONSTANTS[ordinal];
    }

    /**
     * @param staxEvent
     *            on of the events in {@link XMLStreamConstants}
     * @return the mapping {@link EventType}
     */
    public static EventType event(final int staxEvent) {
        return BXML_CONSTANTS[staxEvent];
    }
}
