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

import org.w3c.dom.CDATASection;

/**
 * Enumeration that defines the type-identification codes for the various token types.
 * <p>
 * Tokens are used to encode the content of the BXML file in discrete "packets" that correspond
 * roughly to XML markups.
 * </p>
 * <p>
 * <h3>Elements and Attributes</h3>
 * Elements and attribute definitions are split into the pieces that normally represent nodes in a
 * tree representation of an XML document. The element and attribute definitions are modeled after
 * the WAP-XML representation. An element starts with one of: {@link #EmptyElement},
 * {@link #EmptyAttrElement}, {@link #ContentElement}, or {@link #ContentAttrElement}. The type of
 * token identifies whether there are attributes or content present within the element. The
 * least-significant two bits of the code value can be used to identify whether the content
 * </p>
 * <p>
 * <h3>Content Representation</h3>
 * Several token types are used to represent various types of literal XML content. A content segment
 * of XML may be represented by any sequence and combination of these content tokens. The validity
 * of a sequence of content tokens within a BXML file is defined as the validity of the textual
 * equivalent to the tokens as it would appear in textual XML relative to the content-type
 * definition in XML-Schema or other definition languages. It is recognized that <i>the ideal
 * processing environment</i> for BXML is one in which <i>binary content is passed directly from
 * generator to parser to application</i> without ever being translated into text in between.
 * Therefore, it is recommended that the XML parser scan and store equivalent structures to the
 * content tokens defined in this section and that it avoid translating numbers and "blobs" into a
 * textual equivalent if at all possible in passing the content information to the reading
 * application.
 * </p>
 * <p>
 * NOTE: the documentation to explain the semantics of these tokens is extracted and adapted when
 * needed from the section 8.4 of the BXML spec, version 0.0.8.
 * </p>
 * 
 * @author Gabriel Roldan
 * @version $Id$
 */
public enum TokenType {
    /**
     * This token is equivalent to the XML empty-element markup, e.g., {@code "<blah/>"} (without
     * quotations).
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *   EmptyElementToken {
     *      TokenType type = 0x00;  // token-type code
     *      Count stringRef;        // symbol code for element name
     *  }
     * </pre>
     * </code>
     * </p>
     * <p>
     * This token is self-complete and does not need to be followed by any other specific tokens. A
     * symbol-string reference is used instead of a literal element name for space and processing
     * efficiency. The string reference is an index into the global string table, where index values
     * start from zero. Element-name values must conform to XML constraints.
     * </p>
     */
    EmptyElement(0x00),
    /**
     * This token is equivalent to the XML empty-element markup, e.g., {@code "<blah
     * attr="value"/>"}.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     * EmptyAttrElementToken {
     *     TokenType type = 0x01;   // token-type code
     *     Count stringRef;         // symbol code for element name
     *     }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * This token must be followed by a list of at least one AttributeStartToken plus associated
     * content, and the attribute list must be terminated by an AttributeListEndToken, or,
     * optionally, a StringTableToken may precede any AttributeStartToken, to simplify string-table
     * handling in writers.
     * </p>
     */
    EmptyAttrElement(0x01),
    /**
     * This token may be followed by any number of content tokens and embedded sub-elements,
     * including zero, and must be terminated by an {@link #ElementEnd} token.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  ContentElementToken { 
     *     TokenType type = 0x02;   // token-type code
     *     Count stringRef;         // symbol code for element name
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     */
    ContentElement(0x02),
    /**
     * This token must be followed by a list of at least one {@link #AttributeStart} token plus
     * associated content (with optional {@link #StringTable} token prefixes), and the attribute
     * list must be terminated by an {@link #AttributeListEnd} token. The attribute list may be
     * followed by any number of content tokens and embedded sub-elements, including zero, and the
     * token sequence must be terminated by an {@link #ElementEnd} token.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  ContentAttrElementToken { 
     *     TokenType type = 0x03;   // token-type code
     *     Count stringRef;         // symbol code for element name
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     */
    ContentAttrElement(0x03),
    /**
     * This token is equivalent to the XML element-closing markup, e.g., {@code "</blah>"} and is
     * used to terminate only element-token types that explicitly include content, i.e., {@code
     * ContentElement} token and {@code ContentAttrElement} token.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  ElementEndToken { 
     *     TokenType type = 0x04; // token-type code
     *  }
     * </pre>
     * <code>
     * 
     * </p>
     * <p>
     * The element-name equivalent <b>is omitted</b> since it is redundant in textual XML and it is
     * not useful in a binary environment, since humans are unlikely to edit binary XML by hand.
     * WAP-XML also omits the element name in its equivalent construct.
     * </p>
     */
    ElementEnd(0x04),
    /**
     * This token is equivalent to the starting portion of the XML attribute-definition markup,
     * e.g., {@code attr="}.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  AttributeStartToken { 
     *     TokenType type = 0x05;   // token-type code
     *     Count stringRef;         // symbol code for attribute name
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * The attribute name is referenced from the global string table for efficiency. This token must
     * be followed by some number of content tokens, including zero, that define the attribute
     * value. If the {@code strictXmlStrings} header flag is set, then this token is interpreted to
     * enclose the content in the double-quotation character {@code (")} and the attribute content
     * therefore must not include this character literally (an entity reference must be used
     * instead). If the header flag is not set, then character content may include any literal
     * characters. If textual XML is regenerated from the attribute content with a non-strict
     * string, the translator will need to select a quotation character to use to embed the content
     * (the double-quotation mark is suggested) and it will need to escape any instances of that
     * character in the attribute content using {@code "&quot;"} or {@code "&apos;"} as appropriate.
     * </p>
     * <p>
     * The attribute-value-definition content tokens must be followed by either another {@code
     * AttributeStart} token to continue the attribute list or an {@code AttributeListEnd} token to
     * terminate the attribute list of an element. However, as noted elsewhere, any {@code
     * AttributeStart} token may be directly preceded by a StringTableToken, to simplify writers.
     * </p>
     */
    AttributeStart(0x05),
    /**
     * Token code type for an event that signals the end of the attribute list for the current
     * element
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  AttributeListEndToken {      // end marker of attribute list
     *     TokenType type = 0x06;   // token-type code
     *  }
     * </pre>
     * <code>
     * 
     * </p>
     */
    AttributeListEnd(0x06),
    /**
     * The token that corresponds to regular textual XML content. The content after the token can
     * include any Value sub-type, including numbers and arrays.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  CharContentToken {          // regular character content
     *     TokenType type = 0x10;   // token-type code
     *     Value content;           // single content value
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * If the {@code strictXmlStrings} flag in the header is set, then the content must conform to
     * textual XML constraints, which means that the literal characters {@code "<"} and {@code "&"}
     * must not appear in the string and the sequence {@code "]]>"} must have its final {@code >}
     * character changed. These characters must be generated using entity references. Otherwise, if
     * the flag is not set, then the content may include any and all literal characters. Leading and
     * trailing whitespace characters that are included in a string may be considered to be
     * significant by a reader.
     * </p>
     * <p>
     * However, the representation of numbers and especially arrays of numbers can be much more
     * efficient than the equivalent text, and it also can be processed much more efficiently if the
     * parser and the application are capable of carrying the raw numeric representation through the
     * parsing and interpretation process.
     * </p>
     * <p>
     * The representation of numeric values used in content is independent of the DTD/XMLSchema/
     * RDF-Schema that defines a format. The most efficient or convenient representation of a
     * numeric value may be chosen by the writer. For instance, if XML Schema defines the content to
     * be a list of double numbers, an array of float values may be used instead for greater
     * efficiency if the data will be adequately represented as floats. The content could also be
     * provided as a character String, an array of bytes (if sufficient), or as being spread out
     * over multiple content tokens.
     * </p>
     */
    CharContent(0x10),
    /**
     * This token is equivalent to the {@code CharContent} token, except that instead of using
     * content that is stored in-line, it refers a string that is stored in the global string table.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  CharContentRefToken {        // character content
     *     TokenType type = 0x11;   // token-type code
     *     Count stringRef;         // string-table ref
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * This offers greater compactness than the {@code CharContent} token for representing content
     * strings that are repeated very frequently in a document (by consuming as little as two bytes
     * per use regardless of the string length).
     * </p>
     */
    CharContentRef(0x11),
    /**
     * This is equivalent to the {@code "<![CDATA[content]]>"} structure in textual XML.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  CDataSectionToken {         // &lt;![CDATA[content]]&gt;
     *     TokenType type = 0x12;   // token-type code
     *     Value content;           // single content value
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * This token is essentially equivalent to the {@code CharContent} token, except that its use
     * may be regarded as a hint to a translator to regenerate a {@code CDATA} section in textual
     * XML. If the {@code strictXmlStrings} header flag is set, then the content string must not
     * include the character sequence {@code "]]>"}. If this header flag is not set, then the
     * content may include the sequence. However, since XML {@code CDATA} sections must not include
     * the character sequence {@code "]]>"}, it may not be possible to regenerate a valid {@code
     * CDATA} section in textual XML in all cases. If it is not, then regular character content must
     * be regenerated with appropriate escape sequences. A {@code CDATA} section is normally used in
     * XML to represent strings with literal {@code "<"}, {@code ">"}, or {@code "&"} characters,
     * where these characters are used literally for the purposes of visual appearance or
     * convenience.
     * </p>
     */
    CDataSection(0x12),
    /**
     * Whitespace is defined in XML as strings of the Unicode characters {@code #x20}, {@code #x9},
     * {@code #xD}, and {@code #xA}.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     * WhitespaceToken {            // possibly insignificant whitespace
     *       TokenType type = 0x13; // token-type code
     *       Count nBlankLines;     // number of blank lines
     *       String content;        // literal whitespace content
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * The XML specification defines that all whitespace in an XML document is significant and must
     * be passed to the reading application. However, in practice with most applications, much of
     * the whitespace included for formatting and visual presentation of a textual XML document is
     * actually insignificant. Usage of the {@code Whitespace} token allows BXML readers to remove
     * insignificant whitespace efficiently. The writer is not required to use this token, but
     * whitespace that is included in other content tags may be considered to be significant by the
     * reader. A textual-XML translator may also wish to remove potentially insignificant whitespace
     * from a stream anyway, except maybe for completely blank lines.
     * <p>
     * "Potentially insignificant whitespace" is defined as all sequences of exclusively whitespace
     * characters that separate markup items in textual XML. This includes line-feeds ({@code #xA}
     * character) and indentation characters that are normally inserted before element opening and
     * closing tags.
     * </p>
     * <p>
     * The {@code nBlankLines} field records the number of completely blank lines that are included
     * within the whitespace token. These are normally inserted into an XML file for visual
     * separation between file structures and can make a file much more readable and essentially can
     * be considered to be comments. The number of completely blank lines can be counted as being
     * one less than the number of newline characters in the whitespace string. This information can
     * be useful to record and reproduce blank lines without any extraneous other whitespace
     * characters when generating textual XML from BXML.
     * </p>
     */
    Whitespace(0x13),
    /**
     * A "blob" is an opaque block of raw binary user data.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  BlobSectionToken {               // raw-binary blob
     *     TokenType type = 0x14;       // token-type code
     *     TextBlobType textEncoding;   // encoding to use in text XML
     *     Count length;                // length in bytes
     *     byte content[length];        // raw bytes of blob
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * Binary user data cannot be represented directly in textual XML, which is a major limitation
     * and source of headaches, but it can be in BXML. Binary data can either be referenced
     * indirectly with URIs or it can be stored in-line in a text encoding in textual XML. Base-64
     * encoding [BASE64] is frequently used for this purpose.
     * </p>
     * <p>
     * It is important to be able to translate a blob into a textual-XML representation if that
     * should become necessary, so the textEncoding field is provided to indicate what textual
     * representation to use when translating. An encoding value of None means that no textencoding
     * equivalent is available and a translator must report an error if the attempt is made.
     * </p>
     * 
     * @see TextBlobType
     */
    BlobSection(0x14),
    /**
     * This is equivalent to the XML {@code "&name;"} construct to reference an
     * environmentally-defined entity object.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  EntityRefToken {             // &amp;entity
     *     TokenType type = 0x15;   // token-type code
     *     Count stringRef;         // name reference
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * A string-table reference is used to identify the entity name, as with attribute and element
     * names. The entity references of {@code "&amp;"}, {@code "&lt;"}, {@code "&gt;"} , {@code
     * "&quot;"}, and {@code "&apos;"} are normally used as character-escape sequences in textual
     * XML, but it is suggested that the characters that these entities represent be used literally
     * in BXML content, for efficiency and convenience.
     * </p>
     */
    EntityRef(0x15),
    /**
     * This is equivalent to the XML {@code "&#char_code;"} Unicode-character-code entity reference.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  CharEntityRefToken { 
     *     TokenType type = 0x16;   // token-type code
     *     Count unicodeChar;       // unicode character number
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     */
    CharEntityRef(0x16), // &#char_ref;
    /**
     * This is equivalent to the XML {@code "<!--comment-->"} comment construct and it is provided
     * so that comments can be retained in a BXML file to better mirror the XML content.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  CommentToken { 
     *     TokenType type = 0x17;               // token-type code
     *     CommentPositionHint positioningHint; // comment positioning
     *     String content;                      // content of comment
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * If the {@code strictXmlStrings} header flag is set, then the content string must not include
     * the character sequence {@code "--"} (two hyphens). If the header flag is not set, then the
     * content may include this sequence, but if it does, then the sequence must be substituted with
     * something else if this comment is generated into textual XML, perhaps {@code "-="}. This case
     * is not considered a breach of translating "with no loss of information" since the object in
     * question is only a comment and it could not have originated from a textual-XML document.
     * </p>
     * <p>
     * The whitespace that is included in the content string may be considered to be significant by
     * an application, including leading and trailing whitespace. If there is no leading or trailing
     * whitespace in a string, a textual-XML writer may choose to insert a single space after and
     * before the {@code "<!--"} and {@code "-->"} sequences.
     * </p>
     * <p>
     * The {@code positioningHint} field is provided to give a hint of what line position in textual
     * XML a comment should be regenerated.
     * </p>
     * 
     * @see CommentPositionHint
     */
    Comment(0x17), // <!--comment-->
    /**
     * This is equivalent to the {@code "<?xml ...?>"} XML-declaration construct (where the
     * substring {@code "xml"} is case-insensitive) and it should normally be the first token
     * present in the BXML token stream.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  XmlDeclarationToken {
     *     TokenType type = 0x20;   // token-type code
     *     String version;          // XML version
     *     Bool standalone;         // document is standalone
     *     Bool standaloneIsSet;    // &quot;standalone&quot; is used
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * The semantics for the {@code version} and {@code standalone} fields are the same as for the
     * attributes of the same names for the XML-declaration. The version field may be logically
     * marked as not being present for the XML semantics by assigning them a zero-length string, and
     * the logical presence of the {@code standalone} field is indicated by the {@code
     * standaloneIsSet} field. No character-set {@code "encoding"} value is given here, but the
     * value implied for that attribute of the XML declaration is the {@code charEncoding} value
     * from the Header structure.
     * </p>
     */
    XmlDeclaration(0x20),
    /**
     * This is equivalent to the XML construct of the form {@code "<!name ...>"}.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  BangToken { 
     *     TokenType type = 0x21;   // token-type code
     *     Count nameRef;           // name of tag, string-table ref
     *     String content;          // verbatim character content
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * "Bang" is a synonym used sometimes for the exclamation mark (!). This construct is
     * infrequently used in practice. The name is given by a string-table reference and the {@code
     * content} is represented simply as a verbatim unparsed string. The name and content have the
     * same semantics and restrictions as in textual XML. An example name is {@code "DOCTYPE"} and
     * this type has a complex structure that is not worth tokenizing in the BXML file since
     * non-validating parsers/generators likely will not understand it, and the parsers that do will
     * most likely also support the textual XML format and will therefore be able to handle the
     * unparsed string anyway. {@code "ENTITY"} declarations also use this token, among others.
     * </p>
     */
    Bang(0x21), // <!name ...>
    /**
     * This token is equivalent to the XML construct {@code "<![name[...]]>"}, except that the
     * {@link CDATASection} token is used instead for the name being {@code "CDATA"}.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  BangBracketToken { 
     *     TokenType type = 0x22;   // token-type code
     *     Count nameRef;           // name of tag, string ref
     *     String content;          // verbatim character content
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * This type of markup is used in XML conditional sections which are rarely used in practice.
     * The name is given by a reference into the global string table and the content is an unparsed
     * verbatim string and these values have the same semantics and restrictions as in textual XML.
     * </p>
     */
    BangBracket(0x22), // <![name[...]]>
    /**
     * This is used to represent XML constructs of the form {@code "<?name ...?>"}, excluding the
     * XMLdeclaration construct.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  ProcessingInstrToken { 
     *     TokenType type = 0x23;   // token-type code
     *     Count nameRef;           // name of tag, string-table ref
     *     String content;          // verbatim content, or the empty string
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * Processing instructions are rarely used in practice. The name is given as a reference into
     * the global string table and the content is an unparsed verbatim string and these values have
     * the same semantics and restrictions as in textual XML.
     * </p>
     */
    ProcessingInstr(0x23),
    /**
     * A StringTable fragment start token.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  StringTableToken {              // string table (fragment)
     *     TokenType type = 0x30;       // token-type code
     *     Count nStrings;              // number of strings in frag.
     *     String strings[nStrings];    // string values
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * The global string table may be split into many string-table fragments. This is to make it
     * more convenient to implement a sequential writer by not requiring that it know every
     * symbol/string it might produce in advance. This may also allow the global string table to be
     * more compact, since only a small subset of available symbols/strings may be used in any
     * particular XML document, and it may not be practical to pre-compute this limited subset of
     * symbols before emitting the first XML tag. The writer has the choice of emitting all strings
     * up-front, or in batches as different portions of the generator program are executed, or
     * individually on an as-used basis (though this approach may be inefficient in terms of space
     * and parsing time).
     * </p>
     * <p>
     * Some constraints are placed on the logical global string table. All string-table fragments
     * define global string/symbol codes sequentially starting from zero, and any literal string
     * must appear in the global string table only once, even if it is used in different contexts
     * (e.g., as an element name and as an attribute name). Also, each symbol string must be defined
     * in the BXML stream before it is first referenced when the token stream is read sequentially;
     * however, there is no requirement that a particular string ever actually be referenced. The
     * strings when used as element/attribute symbol names are subject to the constraints on names
     * in textual XML.
     * </p>
     * <p>
     * The string-table index in the {@link #Trailer} token may be used to reassemble the global
     * string table for use during random access, or to access only selected string definitions on
     * an as-needed basis.
     * </p>
     */
    StringTable(0x30), // string table (fragment)
    /**
     * An index table is used to provide a simple mechanism to use to randomly access elements in
     * the token stream that have properties of certain values.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  IndexTableToken {           // index table
     *     TokenType type = 0x31;   // token-type code
     *     Count skipSize;          // size of rest of token
     *     String xpathExpr;        // XPath expression
     *     Count nEntries;          // number of index entries
     *     IndexTableEntry entries[nEntries]; // index-table entries
     *  }
     *  IndexTableEntry {           // index value-match entry
     *     Value value;             // match value
     *     Count nOffsets;          // number of matching offsets
     *     Count offsets[nOffsets]; // offsets to match elements
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * The {@code xpathExpr} value defines the property to be tested using an XPath expression
     * {@code [XPATH]}, and the {@code entries} give match values (equality comparison) for the test
     * property and file offsets to the start of the element tokens that include the referenced
     * property. The {@code skipSize} is provided to allow a reader to easily skip over the index
     * table if it is not interested in using the index.
     * </p>
     * <p>
     * Any number of index tables may be provided within a BXML file, but the XPath expression must
     * be unique for each. Index tables will normally be placed at the end of the BXML stream, since
     * this will be the point that the writer will have collected all of the necessary information.
     * There is no obligation for a writer to generate any index tables at all, but a processing
     * system may add them later (to the end of the stream) if it wishes.
     * </p>
     */
    IndexTable(0x31), // index table
    /**
     * This token marks the end of the BXML file for the reader and also provides a collected set of
     * string-table references that logically constitutes a global string table and a collected set
     * of index-table references.
     * <p>
     * Structure:
     * 
     * <code>
     * <pre>
     *  TrailerToken {                          // last token of every file
     *     TokenType tokenType = 0x32;          // token-type code
     *     byte id[4] = { 0x01, 'T', 'R', 0x00 }; // id
     *     StringTableIndex stringIndex;        // index of string tables
     *     IndexTableIndex indexIndex;          // index of index-tables
     *     int tokenLength;                     // length of this token
     *  }
     * </pre>
     * </code>
     * 
     * </p>
     * <p>
     * The four-byte {@code tokenLength} records the complete length of the {@code Trailer} token
     * and is required to be the fixed-size last field so that the start of the {@code Trailer}
     * token may be located to facilitate random access by reading the last four bytes of the BXML
     * file. (Consequently, the TrailerToken is restricted to being at most approximately 2GB in
     * size.) This token is required to be present at the end of every BXML file, even if the
     * {@code StringTableIndex} and {@code IndexTableIndex} are marked as being "unused".
     * </p>
     * <p>
     * The string-table index is defined as:
     * 
     * <code>
     * <pre>
     *  StringTableIndex {      // index of string-table fragments
     *     Bool isUsed;         // flag for whether this is active
     *     Count nFragments;    // number of fragments in index
     *     StringTableIndexEntry fragments[nFragments]; // string tables
     *  }
     *  StringTableIndexEntry {     // string-table index fragment
     *     Count nStringsDefined;   // number of strings defined in frag.
     *     Count fileOffset;        // file offset to string-table token
     *  }
     * </pre>
     * </code>
     * 
     * The {@code id} field serves a similar purpose to the {@code identifier} field of the {@code
     * Header} structure at the beginning of the BXML file. It is included in the trailer token to
     * help assure the detection of a truncated BXML file. If the BXML file is truncated, then
     * random access will not work, and the file can probably be discarded as a whole. To check for
     * truncation, the reader must first access the {@code tokenLength} field to locate the start of
     * the {@code Trailer} token, and then check that the {@code tokenType} and id fields have the
     * correct values.
     * </p>
     * <p>
     * The index-table index is defined as:
     * 
     * <code>
     * <pre>
     *  IndexTableIndex {       // index of index tables
     *     byte isUsed;         // flag for whether this is active
     *     Count nEntries;      // number of index-tables
     *     IndexTableIndexEntry entries[nEntries]; // index-table indexes
     *  }
     *  IndexTableIndexEntry {  // entry for index
     *     String xpathExpr;    // XPath expression that is indexed
     *     Count fileOffset;    // file offset of index-table token
     *  }
     * </pre>
     * </code>
     * 
     * This gives an index of all index tables present in the file. If no information is available,
     * {@code isUsed} must be set to {@code FALSE} and {@code nEntries} to zero.
     * </p>
     */
    Trailer(0x32) // trailer
    ;

    /**
     * Token identifier on a BXML document.
     * 
     * @invariant {0x00 <= tokenCode <= 0x32}
     */
    private final int tokenCode;

    private static Map<Integer, TokenType> byCodeMap;

    TokenType(final int tokenCode) {
        this.tokenCode = tokenCode;
        register(tokenCode);
    }

    private void register(final int tokenCode) {
        if (byCodeMap == null) {
            byCodeMap = new HashMap<Integer, TokenType>();
        }
        Integer valueOf = Integer.valueOf(tokenCode);
        byCodeMap.put(valueOf, this);
    }

    /**
     * Returns the token type code of this enum constant.
     * <p>
     * The token type code corresponds to the byte value used in a BXML document to identify the
     * start of such a token.
     * </p>
     * 
     * @return the token type code of this enum constant.
     */
    public int getCode() {
        return tokenCode;
    }

    /**
     * Returns the enum constant of this type with the specified token type code.
     * 
     * @param tokenCode
     *            a valid token identifier
     * @return the enum constant of this type with the specified token type code
     * @throws IllegalArgumentException
     *             if this enum type has no constant with the specified code
     */
    public static TokenType valueOf(final int tokenCode) throws IllegalArgumentException {
        TokenType value = byCodeMap.get(Integer.valueOf(tokenCode));
        if (value == null) {
            throw new IllegalArgumentException("A Token type with code "
                    + Integer.toHexString(tokenCode) + " does not exist");
        }
        return value;
    }
}
