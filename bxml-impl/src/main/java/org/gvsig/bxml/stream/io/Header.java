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

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Models a Binary XML file Header.
 * <p>
 * The file header is used to identify the type of the file and provide the critical information
 * necessary for a reader to process the file. It has the following structure:
 * 
 * <pre>
 * &lt;code&gt;
 *  Header {
 *      Identifier identifier;      // file-format identifier
 *      Version version;            // BXML version number
 *      byte flags1;                // header bit flags
 *      byte flags2;                // more header bit flags
 *      Compression compression;    // file-body compression
 *      String charEncoding;        // character encoding used
 *  }
 *  
 *  Identifier {                // format identifier
 *      byte nonText = 0x01;    // ascii SOH
 *      byte name[5] = { 'B', 'X', 'M', 'L', 0x00 };
 *      byte binaryCheck[3] = { 0xFF, 0x0D, 0x0A }; // high-bit, CR+LF
 *  }
 *  
 *  Version { // version of BXML (not XML)
 *      byte major = 0; // x.y.z (0-255 for each component)
 *      byte minor = 0;
 *      byte point = 8;
 *  }
 * &lt;/code&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * Thus, a binary representation of a file header looks like the following:
 * 
 * <pre>
 * &lt;code&gt;
 * hex:   |01  42  58  4d  4c  00   ff  0d  0a  00  00  08    01     00    00     0a    49 53 4f 2d 38 38 35 39 2d 31|
 *        |SOH| B   X   M   L NUL| binaryCheck | 0 | 0 | 8 |      |      |     | byte  | I  S  O  -  8  8  5  9  -  1|
 *        |___|__________________|_____________|___|___|___|______|______|_____|_count_|_____________________________|
 *        |            Identifier              |  Version  |flags1|flags2|compr|            charEncoding             |
 *        |____________________________________|___________|______|______|_____|_____________________________________|
 * &lt;/code&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author gabriel
 */
public final class Header {

    /**
     * Singleton that repersents the identifier section of a BXML header
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    public static class Identifier {
        public static final Identifier INSTANCE = new Identifier();

        /**
         * Binary file mask
         */
        private static final int nonText = 0x01;

        /**
         * Format name identifier
         */
        private final byte[] name = { 'B', 'X', 'M', 'L', 0x00 };

        private final byte[] binaryCheck = { (byte) 0xFF, 0x0D, 0x0A };

        private Identifier() {
            // intentionally-blank
        }

        public int getNonTextMarker() {
            return nonText;
        }

        public byte[] getName() {
            // return a safe copy
            byte[] name = new byte[this.name.length];
            System.arraycopy(this.name, 0, name, 0, this.name.length);
            return name;
        }

        public byte[] getBinaryCheck() {
            // return a safe copy
            byte[] binaryCheck = new byte[this.binaryCheck.length];
            System.arraycopy(this.binaryCheck, 0, binaryCheck, 0, this.binaryCheck.length);
            return binaryCheck;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer("Identifier[]");
            return sb.toString();
        }
    }

    /**
     * Class to represent the Version section of a BXML header
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    public static class Version {
        private int major;

        private int minor;

        private int point;

        /**
         * The default version used for bxml documents {@value}
         */
        public static final Version DEFAULT_VERSION = new Version(0, 0, 8);

        /**
         * Private constructor, use the factory method {@link #valueOf(int, int, int)} to obtain a
         * Version instance.
         * 
         * @param major
         * @param minor
         * @param point
         */
        private Version(int major, int minor, int point) {
            this.major = major;
            this.minor = minor;
            this.point = point;
        }

        /**
         * @return the version's major number
         */
        public int getMajor() {
            return major;
        }

        /**
         * @return the version's minor number
         */
        public int getMinor() {
            return minor;
        }

        /**
         * @return the version's point number
         */
        public int getPoint() {
            return point;
        }

        /**
         * @return a new version instance representing the version {@code major.minor.point}
         */
        public static Version valueOf(int major, int minor, int point) {
            return new Version(major, minor, point);
        }

        /**
         * Hashcode based on major, minor, point
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return 37 * major * minor * point;
        }

        /**
         * Compares to versions for equality
         * 
         * @param o
         *            another Version instance
         * @return {@code true} if both versions are equivelent based on major, minor and point
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Version)) {
                return false;
            }
            Version v = (Version) o;
            return major == v.major && minor == v.minor && point == v.point;
        }

        /**
         * @return a human friendly representation of this Version
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return new StringBuffer("Version[").append(major).append(".").append(minor).append(".")
                    .append(point).append("]").toString();
        }
    }

    /**
     * Enum for the compression flag in a BXML Header
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    public enum Compression {
        /**
         * Enum value meaning not to use compression
         */
        NO_COMPRESSION(0x00),
        /**
         * Enum value meaning to use GZIP compression
         */
        GZIP(0x01);
        private int byteMarker;

        private Compression(int byteMarker) {
            this.byteMarker = byteMarker;
        }

        /**
         * Returns either {@code 0x00} or {@code 0x01} depending on if compression shall or shall
         * not be used, as defined in the bxml spec.
         * 
         * @return the compression code as defined in the bxml specification
         */
        public int compressionCode() {
            return byteMarker;
        }

        /**
         * Obtains the Compression instance corresponding to the given {@code compressionCode} if
         * valid
         * 
         * @param compressionCode
         *            either {@code 0x00} or {@code 0x01}
         * @return the Compression instance corresponding to the given {@code compressionCode}
         * @throws IllegalArgumentException
         *             if the given {@code compressionCode} is not one of the compression values
         *             defined in the bxml spec
         */
        public static Compression valueOf(final int compressionCode) {
            if (NO_COMPRESSION.compressionCode() == compressionCode) {
                return NO_COMPRESSION;
            } else if (GZIP.compressionCode() == compressionCode) {
                return GZIP;
            } else {
                throw new IllegalArgumentException(compressionCode
                        + " is not a valid compression code");
            }
        }

    }

    /**
     * Models the two-byte flags in a BXML header
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @version $Id$
     */
    public static class Flags {
        private static final int LITTLE_ENDIAN_MASK = 0x01;

        private static final int CHARS_LITTLE_ENDIAN_MASK = 0x02;

        private static final int HAS_RANDOM_ACCESS_INFO_BITMASK = 0x04;

        private static final int HAS_STRICT_XML_STRINGS_BITMASK = 0x08;

        private static final int IS_VALIDATED_BITMASK = 0x10;

        /**
         * The flags1 bitmask byte contains the flags for the currently used flag settings in a
         * Header
         */
        private int flags1;

        /**
         * flags2 is unused, keep it as a constant for now.
         */
        private static final int flags2 = 0x0;

        private Flags(final int flags1) {
            this.flags1 = flags1;
        }

        /**
         * Specifies the endian (byte order) that is used for all multi-byte binary numbers
         * throughout the file except for multi-byte character values. A value of
         * {@link ByteOrder#LITTLE_ENDIAN} means that little endian is used (least-significant byte
         * first) and a value of {@link ByteOrder#BIG_ENDIAN} means that big endian is used
         * (most-significant byte first).
         * <p>
         * This is a derived property from the {@link #getFlags1() flags1} bitmask field.
         * </p>
         * 
         * @return
         */
        public ByteOrder getEndianess() {
            final boolean isLittleEndian = (flags1 & LITTLE_ENDIAN_MASK) == LITTLE_ENDIAN_MASK;
            return isLittleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        }

        /**
         * Indicates the "default" byte order for multi-byte character codes, such as UTF-16. A
         * value of {@link ByteOrder#LITTLE_ENDIAN} means that little endian is used and
         * {@link ByteOrder#BIG_ENDIAN} means big endian. Some character encoding schemes may have
         * internal byte-order specifications that override this default value, and others such as
         * ISO-8859-1 do not require a byte order, in which case this value may be ignored.
         * <p>
         * This is a derived property from the {@link #getFlags1() flags1} bitmask field.
         * </p>
         * 
         * @return
         */
        public ByteOrder getCharactersEndianess() {
            final boolean isLittleEndian = (flags1 & CHARS_LITTLE_ENDIAN_MASK) == CHARS_LITTLE_ENDIAN_MASK;
            return isLittleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        }

        /**
         * <p>
         * This is a derived property from the {@link #getFlags1() flags1} bitmask field.
         * </p>
         * 
         * @return
         */
        public boolean hasRandomAccessInfo() {
            final boolean hasRandomAccessInfo;
            hasRandomAccessInfo = (flags1 & HAS_RANDOM_ACCESS_INFO_BITMASK) == HAS_RANDOM_ACCESS_INFO_BITMASK;
            return hasRandomAccessInfo;
        }

        /**
         * <p>
         * This is a derived property from the {@link #getFlags1() flags1} bitmask field.
         * </p>
         * 
         * @return
         */
        public boolean hasStrictXmlStrings() {
            final boolean hasStrictXmlStrings;
            hasStrictXmlStrings = (flags1 & HAS_STRICT_XML_STRINGS_BITMASK) == HAS_STRICT_XML_STRINGS_BITMASK;
            return hasStrictXmlStrings;
        }

        /**
         * <p>
         * This is a derived property from the {@link #getFlags1() flags1} bitmask field.
         * </p>
         * 
         * @return
         */
        public boolean isValidated() {
            final boolean isValidated;
            isValidated = (flags1 & IS_VALIDATED_BITMASK) == IS_VALIDATED_BITMASK;
            return isValidated;
        }

        /**
         * @param flagByteMask
         * @return
         */
        public static Flags valueOf(final int flagByteMask) {
            int actualMask = 0x00;
            actualMask |= LITTLE_ENDIAN_MASK & flagByteMask;
            actualMask |= CHARS_LITTLE_ENDIAN_MASK & flagByteMask;
            actualMask |= HAS_RANDOM_ACCESS_INFO_BITMASK & flagByteMask;
            actualMask |= HAS_STRICT_XML_STRINGS_BITMASK & flagByteMask;
            actualMask |= IS_VALIDATED_BITMASK & flagByteMask;
            return new Flags(actualMask);
        }

        public static Flags valueOf(final ByteOrder endianess, final ByteOrder charsEndianess,
                boolean hasRandomAccessInfo, final boolean hasStrictXmlStrings,
                final boolean isValidated) {

            int flags1 = 0x00;
            if (ByteOrder.LITTLE_ENDIAN == endianess) {
                flags1 |= LITTLE_ENDIAN_MASK;
            }
            if (ByteOrder.LITTLE_ENDIAN == charsEndianess) {
                flags1 |= CHARS_LITTLE_ENDIAN_MASK;
            }
            if (hasRandomAccessInfo) {
                flags1 |= HAS_RANDOM_ACCESS_INFO_BITMASK;
            }
            if (hasStrictXmlStrings) {
                flags1 |= HAS_STRICT_XML_STRINGS_BITMASK;
            }
            if (isValidated) {
                flags1 |= IS_VALIDATED_BITMASK;
            }
            return new Flags(flags1);
        }

        public int getFlags1() {
            return flags1;
        }

        public int getFlags2() {
            return flags2;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer("Flags[");
            sb.append("byte order=").append(getEndianess());
            sb.append(", chars byte order=").append(getCharactersEndianess());
            sb.append(", hasRandomAccessInfo=").append(hasRandomAccessInfo());
            sb.append(", hasStrictXmlStrings=").append(hasStrictXmlStrings());
            sb.append(", isValidated=").append(isValidated());
            sb.append("]");
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Flags)) {
                return false;
            }
            Flags f = (Flags) o;
            return flags1 == f.flags1 && flags2 == f.flags2;
        }
    }

    private final Identifier identifier;

    private final Version version;

    private final Flags flags;

    private final Compression compression;

    private final Charset characterEncoding;

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.nativeOrder();

    public static final Header DEFAULT = new Header();

    private Header() {
        this.identifier = Identifier.INSTANCE;
        this.version = Version.DEFAULT_VERSION;
        this.compression = Compression.NO_COMPRESSION;
        this.characterEncoding = DEFAULT_CHARSET;
        this.flags = Flags.valueOf(DEFAULT_BYTE_ORDER, ByteOrder.BIG_ENDIAN, false, false, false);
    }

    private Header(Version version, Flags flags, Compression compression, Charset charsEncoding) {
        this.version = version;
        this.identifier = Identifier.INSTANCE;
        this.compression = compression;
        this.characterEncoding = charsEncoding;
        this.flags = flags;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Version getVersion() {
        return version;
    }

    public Compression getCompression() {
        return compression;
    }

    public Charset getCharactersEncoding() {
        return characterEncoding;
    }

    public Flags getFlags() {
        return flags;
    }

    /**
     * Creates a new header with default {@link Version} and provided {@code flags},
     * {@code compression} and {@code charsEncoding}.
     */
    public static final Header valueOf(final Flags flags, final Compression compression,
            final Charset charsEncoding) {
        return valueOf(Version.DEFAULT_VERSION, flags, compression, charsEncoding);
    }

    /**
     * Creates a new header with default {@link Version} and provided {@code flags},
     * {@code compression} and {@code charsEncoding}.
     */
    public static final Header valueOf(final Version version, final Flags flags,
            final Compression compression, final Charset charsEncoding) {
        Header header = new Header(version, flags, compression, charsEncoding);
        return header;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Header[");
        sb.append(identifier.toString());
        sb.append(",");
        sb.append(flags.toString());
        sb.append(",");
        sb.append(compression);
        sb.append(", CharsEncoding=");
        sb.append(characterEncoding.name());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Header)) {
            return false;
        }
        Header h = (Header) o;
        return identifier.equals(h.identifier) && version.equals(h.version)
                && flags.equals(h.flags) && compression.equals(h.compression)
                && characterEncoding.equals(h.characterEncoding);
    }
}
