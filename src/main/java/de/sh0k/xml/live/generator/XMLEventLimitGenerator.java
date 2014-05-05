

package de.sh0k.xml.live.generator;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Generates an xml string event oriented with a maximal size of the final xml string.
 *
 * @param <T> The object type that should be mapped later on
 */
public class XMLEventLimitGenerator<T> {

    /**
     * The max size that should not be exceeded.
     * If it is exceeded too much you have to fit the {@link #exceptionFactor}.
     */
    private final long maxSize;

    /**
     * Root Node.
     */
    private final String rootNode;

    /**
     * Maps a document to its xml representation.
     */
    private final DocumentMapper<T> documentMapper;

    /**
     * Writes the xml stream to something the outer program wants.
     */
    private final DocumentWriter documentWriter;

    /**
     * The factor of which every document size is multiplied for automatic size generation.
     */
    private final double exceptionFactor;

    /**
     * The output stream that holds all the data till it is flushed.
     */
    private ByteArrayOutputStream os;

    /**
     * The xml event factory to generate new xml nodes and attributes.
     */
    private XMLEventFactory xmlEventFactory;

    /**
     * The xml event writer that adds all the {@link #xmlEventFactory} events to the {@link #os}.
     */
    private XMLEventWriter writer;

    /**
     * Automatic calculated average document size. This size is vague.
     */
    private long averageSize = 0;

    /**
     * The document count till the {@link #writer} is flushed.
     */
    private long documentCount = 0;

    /**
     * Minimal constructor.
     *
     * @param maxSize        {@link #maxSize}
     * @param rootNode       {@link #rootNode}
     * @param documentMapper {@link #documentMapper}
     * @param documentWriter {@link #documentWriter}
     * @throws XMLStreamException XML cannot be streamed (or opened)
     */
    public XMLEventLimitGenerator(final long maxSize, final String rootNode, final DocumentMapper<T> documentMapper,
                                  final DocumentWriter documentWriter) throws XMLStreamException {
        this(maxSize, rootNode, documentMapper, documentWriter, 1.1);
    }

    /**
     * Maximal constructor.
     *
     * @param maxSize         {@link #maxSize}
     * @param rootNode        {@link #rootNode}
     * @param documentMapper  {@link #documentMapper}
     * @param documentWriter  {@link #documentWriter}
     * @param exceptionFactor {@link #exceptionFactor}
     * @throws XMLStreamException XML cannot be streamed (or opened)
     */
    public XMLEventLimitGenerator(final long maxSize, final String rootNode, final DocumentMapper<T> documentMapper,
                                  final DocumentWriter documentWriter, final double exceptionFactor) throws XMLStreamException {
        this.maxSize = maxSize;
        this.rootNode = rootNode;
        this.exceptionFactor = exceptionFactor;
        this.documentMapper = documentMapper;
        this.documentWriter = documentWriter;
        os = new ByteArrayOutputStream();
        writer = getWriter(os);
    }

    /**
     * Generates a new writer with fresh settings.
     *
     * @param stream the output stream that should be used for the writer.
     * @return a new instance of {@link javax.xml.stream.XMLEventWriter}
     * @throws XMLStreamException If the writer cannot be instantiated
     */
    XMLEventWriter getWriter(final OutputStream stream) throws XMLStreamException {
        xmlEventFactory = XMLEventFactory.newInstance();
        final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        final XMLEventWriter writer = xmlOutputFactory.createXMLEventWriter(stream);
        writer.add(xmlEventFactory.createStartDocument("UTF-8", "1.0"));
        writer.add(xmlEventFactory.createStartElement("", "", rootNode));
        return writer;
    }

    /**
     * Resets the writer and flushes all the content to the {@link #documentWriter}.
     *
     * @throws XMLStreamException           If the stream could not be wrote to the writer.
     * @throws UnsupportedEncodingException the default encoding is UTF-8 and cannot be changed.
     */
    void resetWriter() throws XMLStreamException, UnsupportedEncodingException {
        writer.add(xmlEventFactory.createEndElement("", "", rootNode));
        writer.add(xmlEventFactory.createEndDocument());
        writer.flush();
        if (documentCount > 0) {
            documentWriter.write(os.toString("UTF-8"));
            documentCount = 0;
        }
    }

    /**
     * Push a new document to the stream by mapping it with the {@link #documentMapper}.
     *
     * @param document the document
     * @throws XMLStreamException           if some elements does not fit correctly to the stream.
     * @throws UnsupportedEncodingException the default encoding is UTF-8 and cannot be changed.
     */
    public void push(final T document) throws XMLStreamException, UnsupportedEncodingException {
        final int completeSize = os.size();
        if (averageSize == 0) {
            averageSize = (long) (completeSize * exceptionFactor);
        }
        if (completeSize >= (maxSize - averageSize)) {
            if (documentCount == 0) {
                throw new RuntimeException("no documents added, rethink your xml size");
            }
            resetWriter();
            os = new ByteArrayOutputStream();
            writer = getWriter(os);
        }
        documentMapper.map(writer, xmlEventFactory, document);
        documentCount++;
        final int documentSize = (int) ((os.size() - completeSize) * exceptionFactor);
        if (documentSize > averageSize) {
            averageSize = documentSize;
        }
    }

    /**
     * Close all writers and flushes the last content (if there is some) to {@link #documentWriter}.
     *
     * @throws XMLStreamException if the writers cannot be closed gracefully
     * @throws IOException        if the output stream cannot be closed gracefully
     */
    public void close() throws XMLStreamException, IOException {
        resetWriter();
        writer.close();
        os.close();
    }

    /**
     * The Document Mapper Interface, that specifies the mapping method.
     *
     * @param <T> The object type to map
     */
    public interface DocumentMapper<T> {

        /**
         * Map the object by it generic type from outside.
         *
         * @param writer          {@link #writer}
         * @param xmlEventFactory {@link #xmlEventFactory}
         * @param document        the document that should be mapped
         * @throws XMLStreamException if some elements does not fit correctly
         */
        void map(final XMLEventWriter writer, final XMLEventFactory xmlEventFactory, final T document)
                throws XMLStreamException;
    }

    /**
     * The Document Writer Interface, that specifies the writer of the finally xml string (fired as event).
     */
    public interface DocumentWriter {

        /**
         * Write the xml string to something you want.
         *
         * @param xmlString the xml string
         */
        void write(final String xmlString);
    }
}
