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

import static org.gvsig.bxml.stream.io.ValueType.IntCode;
import static org.gvsig.bxml.stream.io.ValueType.LongCode;
import static org.gvsig.bxml.stream.io.ValueType.SmallNum;
import static org.gvsig.bxml.stream.io.ValueType.UShortCode;

import java.io.IOException;

/**
 * Utility class to read {@code Count}s from a {@link ReadStrategy}.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
final class Counts {
    private static final CountIOHandler SMALL_COUNT_HANDLER = new SmallNumCountReader();

    private static final CountIOHandler USHORT_COUNT_HANDLER = new UShortCountReader();

    private static final CountIOHandler INT_COUNT_HANDLER = new IntCountReader();

    private static final CountIOHandler LONG_COUNT_HANDLER = new LongCountReader();

    private static final CountIOHandler ILLEGAL_COUNT_HANDLER = new IllegalCountCodeReader();

    /**
     * 
     */
    private static final CountIOHandler[] readers;
    static {
        readers = new CountIOHandler[256];
        for (int i = 0; i < 240; i++) {
            readers[i] = SMALL_COUNT_HANDLER;
        }
        for (int i = 240; i < 256; i++) {
            readers[i] = ILLEGAL_COUNT_HANDLER;
        }
        readers[ValueType.UShortCode.getCode()] = USHORT_COUNT_HANDLER;
        readers[ValueType.IntCode.getCode()] = INT_COUNT_HANDLER;
        readers[ValueType.LongCode.getCode()] = LONG_COUNT_HANDLER;
    }

    /**
     * @return
     */
    public final long readCount(final BxmlInputStream reader) throws IOException,
            IllegalTokenIdentifierException {
        final int countType = reader.readByte();
        try {
            return readers[countType].read(countType, reader);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalTokenIdentifierException("Illegal count identifier: 0x"
                    + Integer.toHexString(countType));
        }
    }

    /**
     * @param count
     * @throws IOException
     */
    public final void writeCount(final long count, final BxmlOutputStream writer)
            throws IOException {
        final CountIOHandler countWriter;
        if (count < 0) {
            countWriter = ILLEGAL_COUNT_HANDLER;
        } else if (count <= SmallNum.getUpperLimit().longValue()) {
            countWriter = SMALL_COUNT_HANDLER;
        } else if (count <= UShortCode.getUpperLimit().longValue()) {
            countWriter = USHORT_COUNT_HANDLER;
        } else if (count <= IntCode.getUpperLimit().longValue()) {
            countWriter = INT_COUNT_HANDLER;
        } else {
            countWriter = LONG_COUNT_HANDLER;
        }
        countWriter.write(count, writer);
    }

    /**
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static abstract class CountIOHandler {
        public abstract long read(final int countType, final BxmlInputStream readBuffer)
                throws IOException;

        public abstract void write(long count, final BxmlOutputStream writeBuffer)
                throws IOException;
    }

    /**
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class IllegalCountCodeReader extends CountIOHandler {
        @Override
        public long read(final int countType, final BxmlInputStream buffers) throws IOException {
            throw new IllegalTokenIdentifierException("Illegal Count type: " + countType);
        }

        @Override
        public void write(long count, BxmlOutputStream buffers) throws IOException {
            throw new IllegalTokenIdentifierException(count + " is not a valid count");
        }
    }

    /**
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class SmallNumCountReader extends CountIOHandler {
        /**
         * @pre countType >= 0 && countType <= 239
         */
        @Override
        public long read(final int countType, final BxmlInputStream buffers) throws IOException {
            // No need to check the precondition as this is code private to the enclosing class
            // which already took care of getting the correct count reader for the given count type.
            return countType;
        }

        /**
         * Writes the given count as a SmallNum token
         * 
         * @param count
         *            the count whose value shall be between zero and {@link ValueType#SmallNum 239
         *            (SmallNum upper limit}
         * @param buffers
         * @throws IOException
         * @see org.gvsig.bxml.stream.io.Counts.CountIOHandler#write(long,
         *      org.gvsig.bxml.stream.io.BxmlOutputStream)
         */
        @Override
        public void write(long count, BxmlOutputStream buffers) throws IOException {
            buffers.writeByte((int) count);
        }
    }

    /**
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class UShortCountReader extends CountIOHandler {
        /**
         * 
         */
        @Override
        public long read(final int countType, final BxmlInputStream buffers) throws IOException {
            return buffers.readUShort();
        }

        @Override
        public void write(long count, BxmlOutputStream buffers) throws IOException {
            buffers.writeByte(UShortCode.getCode());
            buffers.writeUShort((int) count);
        }
    }

    /**
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class IntCountReader extends CountIOHandler {
        /**
         * 
         */
        @Override
        public long read(final int countType, final BxmlInputStream buffers) throws IOException {
            int count = buffers.readInt();
            return count;
        }

        @Override
        public void write(long count, BxmlOutputStream buffers) throws IOException {
            buffers.writeByte(IntCode.getCode());
            buffers.writeInt((int) count);
        }
    }

    /**
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    private static class LongCountReader extends CountIOHandler {
        /**
         * 
         */
        @Override
        public long read(final int countType, final BxmlInputStream buffers) throws IOException {
            return buffers.readLong();
        }

        @Override
        public void write(long count, BxmlOutputStream buffers) throws IOException {
            buffers.writeByte(LongCode.getCode());
            buffers.writeLong(count);
        }
    }

}
