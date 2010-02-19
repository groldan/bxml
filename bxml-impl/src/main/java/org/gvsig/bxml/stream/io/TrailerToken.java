package org.gvsig.bxml.stream.io;

import java.util.Set;

/**
 * Models a {@link TokenType#Trailer} Token, which is the final token in a bxml stream and contains
 * a file-offset index of the string table fragments and a file-offset index of the indexed XPath
 * expressions.
 * <p>
 * Whether these two index tables are used or not is optional.
 * </p>
 * <p>
 * The following is the encoding structure of a trailer token:
 * 
 * <pre>
 * &lt;code&gt;
 *  TrailerToken {                          // last token of every file
 *     TokenType tokenType = 0x32;          // token-type code
 *     byte id[4] = { 0x01, 'T', 'R', 0x00 }; // id
 *     StringTableIndex stringIndex;        // index of string tables
 *     IndexTableIndex indexIndex;          // index of index-tables
 *     int tokenLength;                     // length of this token
 *  }
 *  StringTableIndex {      // index of string-table fragments
 *     Bool isUsed;         // flag for whether this is active
 *     Count nFragments;    // number of fragments in index
 *     StringTableIndexEntry fragments[nFragments]; // string tables
 *  }
 *  StringTableIndexEntry {     // string-table index fragment
 *     Count nStringsDefined;   // number of strings defined in frag.
 *     Count fileOffset;        // file offset to string-table token
 *  }
 *  IndexTableIndex {       // index of index tables
 *     byte isUsed;         // flag for whether this is active
 *     Count nEntries;      // number of index-tables
 *     IndexTableIndexEntry entries[nEntries]; // index-table indexes
 *  }
 *  IndexTableIndexEntry {  // entry for index
 *     String xpathExpr;    // XPath expression that is indexed
 *     Count fileOffset;    // file offset of index-table token
 *  }
 * &lt;/code&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * Note all that structure is greatly simplified in this interface, as most data structure
 * attributes are derived properties written as convenience for the encoding format, yet they can be
 * easily derived from this simpler structure:
 * <ul>
 * <li>{@code StringTableIndex.isUsed = getStringTableIndex().size() > 0}
 * <li>{@code StringTableIndex.nFragments = getStringTableIndex().size()}
 * <li>{@code StringTableIndex.fragments = getStringTableIndex()}
 * <li>{@code IndexTableIndex.isUsed = getIndexTableIndex().size() > 0}
 * <li>{@code IndexTableIndex.nEntries = getIndexTableIndex().size()}
 * <li>{@code IndexTableIndex.entries = getIndexTableIndex()}
 * </ul>
 * Also, the {@code TrailerToken.tokenType} and {@code TrailerToken.id} attributes are of no
 * interest for the runtime usage of this structure, and thus omitted from this interface.
 * </p>
 * <p>
 * Finally, the {@code TrailerToken.tokenLength} attribute is useful at decoding time, yet we're
 * defining a {@link #getPosition() position} property instead, as a derived property of the {@code
 * filesize - TrailerToken.tokenLength - 4}, which marks the file-offset of the trailer token, and
 * is of more help to runtime execution usages, such as to gain direct access to the trailer token
 * during reading, and during writing if it needs to be overridden.
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 */
public interface TrailerToken {

    /**
     * Indicates the file-offset of the byte-mark for the Trailer Token
     * 
     * @return a long indicating the byte position of the trailer token in a bxml file.
     */
    public long getPosition();

    /**
     * Returns a possibly empty but never {@code null} set of string table fragments file offsets
     * 
     * @return the set of string table fragments positions or an empty set if the string table is
     *         not indexed, or {@code null} if the StringTableIndex is not used in the trailer
     *         token.
     */
    public Set<StringTableIndexEntry> getStringTableIndex();

    /**
     * Returns a possibly empty but never {@code null} set of XPath based file offsets to locate
     * specific elements in the bxml file.
     * 
     * @return the set of XPath location path index entries signaling the file offset of the
     *         elements addressed by the xpath expressions, or the empty set if no index information
     *         is available; or {@code null} if the IndexTableIndex is not used in the trailer
     *         token.
     */
    public Set<IndexTableIndexEntry> getIndexTableIndex();
}
