package edu.yu.cs.com1320.project.stage5;


import edu.yu.cs.com1320.project.impl.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat.TXT;
import static org.junit.Assert.*;

public class CostumDocumentStoreTest {
    private DocumentStoreImpl documentStore;
    private URI uri1, uri2, uri3, uri4;

    @Before
    public void setUp() throws Exception {
        documentStore = new DocumentStoreImpl();
        uri1 = new URI("http://www.example.com/document1");
        uri2 = new URI("http://www.example.com/document2");
        uri3 = new URI("http://www.example.com/document3");
        uri4 = new URI("http://www.example.com/document4");
    }

    @Test
    public void testPutAndGetTextDocument() throws IOException {
        String content = "This is the content of the document";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        int hashCode = documentStore.put(inputStream, uri1, TXT);
        Document document = documentStore.get(uri1);

        assertNotNull(document);
        assertEquals(content, document.getDocumentTxt());
    }

    @Test
    public void testDeleteAndGetDocument() throws IOException {
        String content = "This is the content of the document";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        documentStore.put(inputStream, uri1, TXT);

        assertTrue(documentStore.delete(uri1));
        assertNull(documentStore.get(uri1));
    }

    @Test
    public void testUndoDeleteOperation() throws IOException {
        String content = "This is the content of the document";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        documentStore.put(inputStream, uri1, TXT);
        assertTrue(documentStore.delete(uri1));

        documentStore.undo();
        assertNotNull(documentStore.get(uri1));
    }

    @Test(expected = IllegalStateException.class)
    public void testUndoEmptyStackForURI() {
        documentStore.undo(uri1);
    }

    @Test
    public void testSetAndGetMetadata() throws IOException {
        String content = "Sample document.";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        documentStore.put(inputStream, uri1, TXT);

        String oldValue = documentStore.setMetadata(uri1, "author", "John Doe");
        String retrievedValue = documentStore.getMetadata(uri1, "author");
        String nonExistentValue = documentStore.getMetadata(uri1, "non-existent");

        assertNull(oldValue);
        assertEquals("John Doe", retrievedValue);
        assertNull(nonExistentValue);
    }

    @Test
    public void testUndoWithMultiplePuts() throws IOException {
        documentStore.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, TXT);
        documentStore.put(new ByteArrayInputStream("Document 2".getBytes()), uri2, TXT);
        documentStore.put(new ByteArrayInputStream("Document 3".getBytes()), uri3, TXT);
        documentStore.put(new ByteArrayInputStream("Document 4".getBytes()), uri4, TXT);

        documentStore.undo(uri4);
        documentStore.undo(uri3);
        documentStore.undo(uri2);
        documentStore.undo(uri1);

        assertNull(documentStore.get(uri1));
        assertNull(documentStore.get(uri2));
        assertNull(documentStore.get(uri3));
        assertNull(documentStore.get(uri4));
    }

    @Test
    public void testUndoAfterSinglePut() throws IOException {
        documentStore.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, TXT);
        documentStore.undo();

        assertNull(documentStore.get(uri1));
    }

    @Test(expected = IllegalStateException.class)
    public void testUndoEmptyStack() {
        documentStore.undo();
    }

    @Test
    public void testSearchByKeyword() throws IOException, URISyntaxException {
        String content1 = "Mami leches me encantas";
        String content2 = "leches broder";
        InputStream inputStream1 = new ByteArrayInputStream(content1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(content2.getBytes());

        documentStore.put(inputStream1, new URI("leches"), TXT);
        documentStore.put(inputStream2, new URI("broder"), TXT);

        List<Document> expectedDocuments = Arrays.asList(
                documentStore.get(new URI("leches")),
                documentStore.get(new URI("broder"))
        );

        assertEquals(expectedDocuments, documentStore.search("leches"));
    }

    @Test
    public void testWordCountForTextDocument() {
        String textContent = "Nessim wurmann 1 don't. Dont Don't leches cachai, pap 1. the The tHe";
        Document document = new DocumentImpl(uri1, textContent);

        assertEquals(2, document.wordCount("1"));
        assertEquals(1, document.wordCount("dont"));
        assertEquals(2, document.wordCount("Dont"));
        assertEquals(1, document.wordCount("the"));
        assertEquals(1, document.wordCount("The"));
        assertEquals(1, document.wordCount("tHe"));
    }

    @Test
    public void testWordCountForBinaryDocument() {
        byte[] binaryData = {0x01, 0x02, 0x03, 0x04, 0x05};
        Document document = new DocumentImpl(uri1, binaryData);

        assertEquals(0, document.wordCount("test"));
    }

    @Test
    public void testDeleteAllWithKeyword() throws IOException {
        documentStore.put(new ByteArrayInputStream("Document leches 1".getBytes()), uri1, TXT);
        documentStore.put(new ByteArrayInputStream("Document leches 2".getBytes()), uri2, TXT);
        documentStore.put(new ByteArrayInputStream("Document 3".getBytes()), uri3, TXT);

        Set<URI> deletedDocuments = documentStore.deleteAll("leches");

        assertEquals(2, deletedDocuments.size());
        assertTrue(deletedDocuments.contains(uri1));
        assertTrue(deletedDocuments.contains(uri2));
        assertNull(documentStore.get(uri1));
        assertNull(documentStore.get(uri2));
        assertNotNull(documentStore.get(uri3));
    }

    @Test
    public void testUndoAfterDeleteAll() throws IOException {
        documentStore.put(new ByteArrayInputStream("Document leches 1".getBytes()), uri1, TXT);
        documentStore.put(new ByteArrayInputStream("Document leches 2".getBytes()), uri2, TXT);
        documentStore.put(new ByteArrayInputStream("Document 3".getBytes()), uri3, TXT);

        documentStore.deleteAll("leches");
        documentStore.undo();

        assertNotNull(documentStore.get(uri1));
        assertNotNull(documentStore.get(uri2));
        assertNotNull(documentStore.get(uri3));
    }
}

