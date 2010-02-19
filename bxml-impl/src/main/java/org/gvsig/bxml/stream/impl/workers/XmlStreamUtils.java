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
import java.util.HashMap;
import java.util.Map;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.io.BxmlInputStream;
import org.gvsig.bxml.stream.io.ValueType;

/**
 * A set of utility methods to deal with xml streams
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
class XmlStreamUtils {

    /**
     * Registry of stateless singleton converters from a value type to String
     */
    private static final Map<ValueType, ValueToStringConverter> converters = new HashMap<ValueType, ValueToStringConverter>();
    static {
        converters.put(ValueType.BoolCode, new BoolConverter());
        converters.put(ValueType.ByteCode, new ByteConverter());
        converters.put(ValueType.DoubleCode, new DoubleConverter());
        converters.put(ValueType.FloatCode, new FloatConverter());
        converters.put(ValueType.IntCode, new IntConverter());

        converters.put(ValueType.ShortCode, new ShortConverter());
        converters.put(ValueType.UShortCode, new UShortConverter());

        converters.put(ValueType.LongCode, new LongConverter());
        converters.put(ValueType.StringCode, new StringConverter());
    }

    /**
     * Empty private constructor to force using this class as a pure utility through its static
     * members
     */
    private XmlStreamUtils() {
        // do nothing
    }

    /**
     * Parses the contents of a value token other than VALUE_STRING to a String
     * 
     * @param reader
     *            the reader where to get the primitive values from
     * @param valueType
     *            an EventType where isValue() == true, but not EventType.VALUE_STRING
     * @param valueCount
     *            the number of elements of the primitive type denoted by the velueType to read and
     *            parse into the output string
     * @return the String representation of the array or single element of a given primitive type
     *         read from {@code reader} and given by the {@code eventType} and {@code valueCount}
     * @throws IOException
     */
    public static String parseStringValue(final BxmlInputStream reader, final ValueType valueType,
            final int valueCount) throws IOException {
        if (ValueType.StringCode == valueType) {
            throw new IllegalArgumentException(
                    "Can't parse String values to Strings, they should be returned directly by the BxmlStreamReader");
        }

        ValueToStringConverter converter = converters.get(valueType);
        String stringValue = converter.convert(reader, valueCount);
        return stringValue;
    }

    /**
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static abstract class ValueToStringConverter {
        /**
         * Implements the loop of {@code count} cycles to build the string, delegating to the
         * template method {@link #readSingleValue(BxmlStreamReader)} for subclasses to provide a
         * String for each single element of the specific primitive type.
         * <p>
         * Note under certain circumstances this approach may be less performant than reading the
         * whole array at once and then convert it to String (for example, by using
         * {@link BxmlStreamReader#getValue(double[], int, int)}, but potentially saves a lot of
         * memory.
         * </p>
         * 
         * @param reader
         * @param count
         * @return
         * @throws IOException
         */
        public final String convert(BxmlInputStream reader, int count) throws IOException {
            StringBuilder sb = new StringBuilder();
            String singleValue;
            // loop N - 1 times to append a space at the end
            for (int i = 0; i < (count - 1); i++) {
                singleValue = readSingleValue(reader);
                sb.append(singleValue);
                sb.append(' ');
            }
            // and once more for the final element without space at the end
            singleValue = readSingleValue(reader);
            sb.append(singleValue);

            return sb.toString();
        }

        protected abstract String readSingleValue(BxmlInputStream reader) throws IOException;
    }

    /**
     * A ValueToStringConverter specialized in converting long values read from a BxmlStreamReader
     * to String.
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class LongConverter extends ValueToStringConverter {

        /**
         * @see org.gvsig.bxml.stream.impl.workers.XmlStreamUtils.ValueToStringConverter#readSingleValue(org.gvsig.bxml.stream.BxmlStreamReader)
         */
        @Override
        protected String readSingleValue(BxmlInputStream reader) throws IOException {
            long value = reader.readLong();
            return String.valueOf(value);
        }

    }

    /**
     * A ValueToStringConverter specialized in converting int values read from a BxmlStreamReader to
     * String.
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class IntConverter extends ValueToStringConverter {
        /**
         * @see org.gvsig.bxml.stream.impl.workers.XmlStreamUtils.ValueToStringConverter#readSingleValue(org.gvsig.bxml.stream.BxmlStreamReader)
         */
        @Override
        protected String readSingleValue(BxmlInputStream reader) throws IOException {
            int value = reader.readInt();
            return String.valueOf(value);
        }
    }

    /**
     * A ValueToStringConverter specialized in converting float values read from a BxmlStreamReader
     * to String.
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class FloatConverter extends ValueToStringConverter {
        /**
         * @see org.gvsig.bxml.stream.impl.workers.XmlStreamUtils.ValueToStringConverter#readSingleValue(org.gvsig.bxml.stream.BxmlStreamReader)
         */
        @Override
        protected String readSingleValue(BxmlInputStream reader) throws IOException {
            float value = reader.readFloat();
            return String.valueOf(value);
        }
    }

    /**
     * A ValueToStringConverter specialized in converting double values read from a BxmlStreamReader
     * to String.
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class DoubleConverter extends ValueToStringConverter {
        /**
         * @see org.gvsig.bxml.stream.impl.workers.XmlStreamUtils.ValueToStringConverter#readSingleValue(org.gvsig.bxml.stream.BxmlStreamReader)
         */
        @Override
        protected String readSingleValue(BxmlInputStream reader) throws IOException {
            double value = reader.readDouble();
            return String.valueOf(value);
        }
    }

    /**
     * A ValueToStringConverter specialized in converting byte values read from a BxmlStreamReader
     * to String.
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class ByteConverter extends ValueToStringConverter {
        /**
         * @see org.gvsig.bxml.stream.impl.workers.XmlStreamUtils.ValueToStringConverter#readSingleValue(org.gvsig.bxml.stream.BxmlStreamReader)
         */
        @Override
        protected String readSingleValue(BxmlInputStream reader) throws IOException {
            int value = reader.readByte();
            return String.valueOf(value);
        }
    }

    /**
     * A ValueToStringConverter specialized in converting bool values read from a BxmlStreamReader
     * to String.
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class BoolConverter extends ValueToStringConverter {
        /**
         * @return the String {@code "true"} or {@code "false"} according to the boolean value read
         *         from the {@code reader}
         * @see org.gvsig.bxml.stream.impl.workers.XmlStreamUtils.ValueToStringConverter#readSingleValue(org.gvsig.bxml.stream.BxmlStreamReader)
         */
        @Override
        protected String readSingleValue(BxmlInputStream reader) throws IOException {
            boolean value = reader.readBoolean();
            return String.valueOf(value);
        }
    }

    private static class StringConverter extends ValueToStringConverter {

        @Override
        protected String readSingleValue(BxmlInputStream reader) throws IOException {
            String value = reader.readString();
            return value;
        }

    }

    private static class UShortConverter extends ValueToStringConverter {

        @Override
        protected String readSingleValue(BxmlInputStream reader) throws IOException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

    }

    private static class ShortConverter extends ValueToStringConverter {

        @Override
        protected String readSingleValue(BxmlInputStream reader) throws IOException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

    }

}
