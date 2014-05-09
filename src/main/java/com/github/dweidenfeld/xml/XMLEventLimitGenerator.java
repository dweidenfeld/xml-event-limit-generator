package com.github.dweidenfeld.xml;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Generates an xml byte array event oriented with a maximal size of the final xml byte array.
 *
 * @param <T> The object type that should be mapped later on
 */
public class XMLEventLimitGenerator<T> implements XMLGenerator<T> {

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
     * The encoding that will be used. (default to utf-8)
     */
    private final String encoding;

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
     * @throws XMLException XML cannot be streamed (or opened)
     */
    public XMLEventLimitGenerator(final long maxSize, final String rootNode, final DocumentMapper<T> documentMapper,
                                  final DocumentWriter documentWriter) throws XMLException {
        this(maxSize, rootNode, documentMapper, documentWriter, "UTF-8", 1.1);
    }

    /**
     * Maximal constructor.
     *
     * @param maxSize         {@link #maxSize}
     * @param rootNode        {@link #rootNode}
     * @param documentMapper  {@link #documentMapper}
     * @param documentWriter  {@link #documentWriter}
     * @param encoding        {@link #encoding}
     * @param exceptionFactor {@link #exceptionFactor}
     * @throws XMLException XML cannot be streamed (or opened)
     */
    public XMLEventLimitGenerator(final long maxSize, final String rootNode, final DocumentMapper<T> documentMapper,
                                  final DocumentWriter documentWriter, final String encoding,
                                  final double exceptionFactor)
            throws XMLException {
        this.maxSize = maxSize;
        this.rootNode = rootNode;
        this.encoding = encoding;
        this.exceptionFactor = exceptionFactor;
        this.documentMapper = documentMapper;
        this.documentWriter = documentWriter;
        os = new ByteArrayOutputStream();
        writer = getWriter(os, encoding);
    }

    /**
     * Generates a new writer with fresh settings.
     *
     * @param stream the output stream that should be used for the writer.
     * @return a new instance of {@link javax.xml.stream.XMLEventWriter}
     * @throws XMLException If the writer cannot be instantiated
     */
    private XMLEventWriter getWriter(final OutputStream stream, final String encoding) throws XMLException {
        xmlEventFactory = XMLEventFactory.newInstance();
        final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        final XMLEventWriter writer;
        try {
            writer = xmlOutputFactory.createXMLEventWriter(stream, encoding);
            writer.add(xmlEventFactory.createStartDocument(encoding, "1.0"));
            writer.add(xmlEventFactory.createStartElement("", "", rootNode));
        } catch (final XMLStreamException e) {
            throw new XMLException(e);
        }
        return writer;
    }

    /**
     * Resets the writer and flushes all the content to the {@link #documentWriter}.
     *
     * @throws XMLException If the stream could not be wrote to the writer.
     */
    private void resetWriter() throws XMLException {
        try {
            writer.add(xmlEventFactory.createEndElement("", "", rootNode));
            writer.add(xmlEventFactory.createEndDocument());
            writer.flush();
            if (documentCount > 0) {
                documentWriter.write(os.toByteArray());
                documentCount = 0;
            }
        } catch (final XMLStreamException e) {
            throw new XMLException(e);
        }
    }

    @Override
    public void push(final T document) throws XMLException {
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
            writer = getWriter(os, encoding);
        }
        try {
            documentMapper.map(writer, xmlEventFactory, document);
        } catch (final XMLStreamException e) {
            throw new XMLException("cannot map document", e);
        }
        documentCount++;
        final int documentSize = (int) ((os.size() - completeSize) * exceptionFactor);
        if (documentSize > averageSize) {
            averageSize = documentSize;
        }
    }

    @Override
    public void close() throws XMLException {
        resetWriter();
        try {
            writer.close();
            os.close();
        } catch (final XMLStreamException e) {
            throw new XMLException("cannot close xml event writer", e);
        } catch (final IOException e) {
            throw new XMLException("cannot close binary stream", e);
        }
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
     * The Document Writer Interface, that specifies the writer of the finally xml byte array (fired as event).
     */
    public interface DocumentWriter {

        /**
         * Write the xml byte array to something you want.
         *
         * @param xml the xml as byte
         */
        void write(final byte[] xml);
    }
}
