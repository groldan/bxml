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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test suite for the {@link Counts} utility class
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public class CountsTest {

    private BxmlInputStream mockReader;

    private BxmlOutputStream mockWriter;

    private Counts countsReader;

    private Counts countsWriter;

    @Before
    public void setUp() throws Exception {
        mockReader = EasyMock.createMock(BxmlInputStream.class);
        mockWriter = EasyMock.createMock(BxmlOutputStream.class);
        countsReader = new Counts();
        countsWriter = new Counts();
    }

    @After
    public void tearDown() throws Exception {
        countsReader = null;
        mockReader = null;
    }

    /**
     * Assert boundary conditions for {@link Counts#readCount()}.
     * <p>
     * The fact that Counts stores the count readers in an internal array for the fastest possible
     * access to reading code by the {@link ValueType#getCode() ValueType code} is an internal
     * detail, and invalid data may produce an ArrayIndexOutOfBoundsException. Yet
     * Counts.readCount() need to throw a IOException.
     * </p>
     * 
     * @throws IOException
     */
    @Test
    public void testReadIllegalCountIdentifier() throws IOException {
        // simulate bad data by returning an illegal count type identifier
        final int ILLEGAL_COUNT_TYPE_ID = -1;
        expect(mockReader.readByte()).andReturn(ILLEGAL_COUNT_TYPE_ID);
        replay(mockReader);

        try {
            countsReader.readCount(mockReader);
            fail("Expected IOException when an illegal count type is found");
        } catch (IOException ioe) {
            assertTrue(true);
        } catch (ArrayIndexOutOfBoundsException e) {
            fail("got ArrayIndexOutOfBoundsException when it should have been a IOException");
        }

        // countTypes 0-239 are SmallNum values. ValueTypes between 240 and 255 other than UShort,
        // Int and Long are invalid Count codes.
        final int ushortCode = ValueType.UShortCode.getCode();
        final int intCode = ValueType.IntCode.getCode();
        final int longCode = ValueType.LongCode.getCode();
        for (int valueTypeId = 240; valueTypeId < 256; valueTypeId++) {
            if (valueTypeId == ushortCode || valueTypeId == intCode || valueTypeId == longCode) {
                continue;
            }
            mockReader = EasyMock.createMock(BxmlInputStream.class);
            countsReader = new Counts();
            expect(mockReader.readByte()).andReturn(valueTypeId);
            replay(mockReader);

            try {
                countsReader.readCount(mockReader);
                fail("Expected IOException when an illegal count type is found");
            } catch (IOException ioe) {
                assertTrue(true);
            }
            verify(mockReader);
        }
    }

    @Test
    public void testWriteIllegalCountIdentifier() throws IOException {
        // simulate bad data by returning an illegal count type identifier
        final int ILLEGAL_COUNT_TYPE_ID = -1;
        replay(mockReader);

        try {
            countsReader.writeCount(ILLEGAL_COUNT_TYPE_ID, mockWriter);
            fail("Expected IllegalArgumentException when an illegal count type is found");
        } catch (IOException iae) {
            assertTrue(true);
        }
    }

    @Test
    public void testReadSmallNum() throws IOException {
        for (int smallNum = 0; smallNum < 240; smallNum++) {
            mockReader = createMock(BxmlInputStream.class);
            countsReader = new Counts();
            expect(mockReader.readByte()).andReturn(smallNum);
            replay(mockReader);

            long count = countsReader.readCount(mockReader);
            assertEquals(smallNum, count);
            verify(mockReader);
        }
    }

    @Test
    public void testWriteSmallNum() throws IOException {
        for (int smallNum = 0; smallNum < 240; smallNum++) {
            mockWriter = createMock(BxmlOutputStream.class);
            mockWriter.writeByte(smallNum);
            replay(mockWriter);
            countsReader = new Counts();
            countsReader.writeCount(smallNum, mockWriter);
            // verify reader.putByte were called
            verify(mockWriter);
        }
    }

    @Test
    public void testReadUShort() throws IOException {
        int maxUshort = ValueType.UShortCode.getUpperLimit().intValue();
        expect(mockReader.readByte()).andReturn(ValueType.UShortCode.getCode());
        expect(mockReader.readUShort()).andReturn(maxUshort);
        replay(mockReader);

        long count = countsReader.readCount(mockReader);
        assertEquals(maxUshort, count);
        verify(mockReader);
    }

    @Test
    public void testWriteUShort() throws IOException {
        int maxUshort = ValueType.UShortCode.getUpperLimit().intValue();
        mockWriter.writeByte(ValueType.UShortCode.getCode());
        mockWriter.writeUShort(maxUshort);
        replay(mockWriter);
        countsWriter.writeCount(maxUshort, mockWriter);
        verify(mockWriter);
    }

    @Test
    public void testReadInt() throws IOException {
        expect(mockReader.readByte()).andReturn(ValueType.IntCode.getCode());
        expect(mockReader.readInt()).andReturn(256);
        replay(mockReader);

        long count = countsReader.readCount(mockReader);
        assertEquals(256, count);
        verify(mockReader);
    }

    @Test
    public void testWriteInt() throws IOException {
        final int minIntCount = 1 + ValueType.UShortCode.getUpperLimit().intValue();
        final int maxInt = ValueType.IntCode.getUpperLimit().intValue();
        mockWriter.writeByte(ValueType.IntCode.getCode());
        mockWriter.writeInt(minIntCount);
        mockWriter.writeByte(ValueType.IntCode.getCode());
        mockWriter.writeInt(maxInt);
        replay(mockWriter);
        countsWriter.writeCount(minIntCount, mockWriter);
        countsWriter.writeCount(maxInt, mockWriter);
        verify(mockWriter);
    }

    @Test
    public void testReadLong() throws IOException {
        expect(mockReader.readByte()).andReturn(ValueType.LongCode.getCode());
        final long expectedValue = Integer.MAX_VALUE + 1;
        expect(mockReader.readLong()).andReturn(expectedValue);
        replay(mockReader);

        long count = countsReader.readCount(mockReader);
        assertEquals(expectedValue, count);
        verify(mockReader);
    }

    @Test
    public void testWriteLong() throws IOException {
        final long minLongCount = 1L + ValueType.IntCode.getUpperLimit().longValue();
        final long maxLong = ValueType.LongCode.getUpperLimit().longValue();

        mockWriter.writeByte(ValueType.LongCode.getCode());
        mockWriter.writeLong(minLongCount);
        mockWriter.writeByte(ValueType.LongCode.getCode());
        mockWriter.writeLong(maxLong);
        replay(mockReader);
        countsWriter.writeCount(minLongCount, mockWriter);
        countsWriter.writeCount(maxLong, mockWriter);
        verify(mockReader);
    }
}
