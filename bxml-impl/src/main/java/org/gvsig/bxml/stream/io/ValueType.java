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
package org.gvsig.bxml.stream.io;

import java.util.HashMap;
import java.util.Map;

public enum ValueType {
    SmallNum(Integer.MIN_VALUE, Integer.valueOf(0), Integer.valueOf(239)), // small number
    BoolCode(0xF0, Double.NaN, Double.NaN), // boolean value
    ByteCode(0xF1, Integer.valueOf(0), Integer.valueOf(255)), // 'byte' numeric value
    ShortCode(0xF2, Short.MIN_VALUE, Short.MAX_VALUE), // 'short' numeric value
    UShortCode(0xF3, Integer.valueOf(0), Integer.valueOf(65535)), // 'ushort' numeric type
    IntCode(0xF4, Integer.MIN_VALUE, Integer.MAX_VALUE), // 'int' numeric value
    LongCode(0xF6, Long.MIN_VALUE, Long.MAX_VALUE), // 'long' numeric value
    FloatCode(0xF8, Float.MIN_VALUE, Float.MAX_VALUE), // 'float' numeric value
    DoubleCode(0xF9, Double.MIN_VALUE, Double.MAX_VALUE), // 'double' numeric value
    StringCode(0xFA, Double.NaN, Double.NaN), // character string
    ArrayCode(0xFB, Double.NaN, Double.NaN); // string of scalar values

    /**
     * The maximum value code for a ValueType
     */
    public static int MAX_DEFINED_VALUETYPE_CODE = ArrayCode.getCode();

    /** byte length constant for a Double type */
    public static final int DOUBLE_BYTE_COUNT = 8;

    /** byte length constant for a Long type */
    public static final int LONG_BYTE_COUNT = 8;

    /** byte length constant for a Float type */
    public static final int FLOAT_BYTE_COUNT = 4;

    /** byte length constant for a Integer type */
    public static final int INTEGER_BYTE_COUNT = 4;

    /** byte length constant for a UShort type */
    public static final int USHORT_BYTE_COUNT = 2;

    /** byte length constant for a Short type */
    public static final int SHORT_BYTE_COUNT = 2;

    /** byte length constant for a Byte type */
    public static final int BYTE_BYTE_COUNT = 1;

    /** byte length constant for a SmallNum type */
    public static final int SMALLNUM_BYTE_COUNT = 1;

    /** byte length constant for a Boolean type */
    public static final int BOOLEAN_BYTE_COUNT = 1;

    /**
     * Array based ValueType list where the ValueType's codes are their array index
     */
    private static ValueType[] byCodeMap;
    static {
        synchronized (ValueType.class) {
            if (byCodeMap == null) {
                byCodeMap = new ValueType[1 + MAX_DEFINED_VALUETYPE_CODE];
                {
                    // fill up SmallNum cells, since its special
                    final int smallNumLowerLimit = SmallNum.getLowerLimit().intValue();
                    final int smallNumUpperLimit = SmallNum.getUpperLimit().intValue();
                    for (int smallNumCode = smallNumLowerLimit; smallNumCode <= smallNumUpperLimit; smallNumCode++) {
                        byCodeMap[smallNumCode] = SmallNum;
                    }
                }
                byCodeMap[BoolCode.getCode()] = BoolCode;
                byCodeMap[ByteCode.getCode()] = ByteCode;
                byCodeMap[ShortCode.getCode()] = ShortCode;
                byCodeMap[UShortCode.getCode()] = UShortCode;
                byCodeMap[IntCode.getCode()] = IntCode;
                byCodeMap[LongCode.getCode()] = LongCode;
                byCodeMap[FloatCode.getCode()] = FloatCode;
                byCodeMap[DoubleCode.getCode()] = DoubleCode;
                byCodeMap[StringCode.getCode()] = StringCode;
                byCodeMap[ArrayCode.getCode()] = ArrayCode;
            }
        }
    }

    private final int valueTypeCode;

    private final Number lowerLimit;

    private final Number upperLimit;

    private static final Map<ValueType, Integer> byteLenghts = new HashMap<ValueType, Integer>();
    static {
        byteLenghts.put(BoolCode, BOOLEAN_BYTE_COUNT);
        byteLenghts.put(SmallNum, BYTE_BYTE_COUNT);
        byteLenghts.put(ByteCode, BYTE_BYTE_COUNT);
        byteLenghts.put(DoubleCode, DOUBLE_BYTE_COUNT);
        byteLenghts.put(FloatCode, FLOAT_BYTE_COUNT);
        byteLenghts.put(IntCode, INTEGER_BYTE_COUNT);
        byteLenghts.put(LongCode, LONG_BYTE_COUNT);
        byteLenghts.put(ShortCode, SHORT_BYTE_COUNT);
        byteLenghts.put(UShortCode, USHORT_BYTE_COUNT);
    }

    /**
     * @param valueTypeCode
     * @pre (valueTypeCode > 239 && valueTypeCode < 256)
     */
    ValueType(final int valueTypeCode, final Number lowerLimit, final Number upperLimit) {
        this.valueTypeCode = valueTypeCode;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    /**
     * @return
     */
    public int getCode() {
        return valueTypeCode;
    }

    /**
     * Finds out a ValueType for the given value type identifier.
     * <p>
     * Values of {@code valueTypeCode} from {@code 0} to {@code 239} correspond to the special
     * purpose {@link #SmallNum} value type.
     * </p>
     * 
     * @param valueTypeCode
     *            the value type code, in the range 0 - {@link #MAX_DEFINED_VALUETYPE_CODE}
     * @return
     */
    public static ValueType valueOf(final int valueTypeCode) {
        ValueType value = byCodeMap[valueTypeCode];
        if (value == null) {
            throw new IllegalArgumentException("A ValueType with code "
                    + Integer.toHexString(valueTypeCode) + " does not exist");
        }
        return value;
    }

    /**
     * Returns the byte length of an element
     * 
     * @param valueType
     *            the value type enum member for which to get its byte length
     * @return the byte length for an element of type {@code vlaueType}
     * @throws IllegalArgumentException
     *             if there's no fixed byte length for the given valueType (ie, StringCode,
     *             ArrayCode)
     */
    public static int getByteLength(final ValueType valueType) {
        Integer byteLength = byteLenghts.get(valueType);
        if (byteLength == null) {
            throw new IllegalArgumentException("ValueType " + valueType
                    + " has not a fixed byte length");
        }
        return byteLength.intValue();
    }

    /**
     * @return the lowerLimit
     */
    public final Number getLowerLimit() {
        return lowerLimit;
    }

    /**
     * @return the upperLimit
     */
    public final Number getUpperLimit() {
        return upperLimit;
    }
}
