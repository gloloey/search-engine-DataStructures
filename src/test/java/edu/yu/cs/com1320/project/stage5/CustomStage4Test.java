package edu.yu.cs.com1320.project.stage5;


import org.junit.Before;
import org.junit.Test;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import static edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat.TXT;
import static org.junit.Assert.*;

public class CustomStage4Test {
    private DocumentStoreImpl customDocumentStore = new DocumentStoreImpl();
    private URI customUri1;
    private URI customUri2;
    private URI customUri3;
    private URI customUri4;

    @Before
    public void setUp() throws URISyntaxException {
        customUri1 = new URI("http://www.example.com/doc1");
        customUri2 = new URI("http://www.example.com/doc2");
        customUri3 = new URI("http://www.example.com/doc3");
        customUri4 = new URI("http://www.example.com/doc4");
    }

    @Test
    public void customPutAndGet() throws IOException {
        String content1 = "This is the content of document 1.";
        String content2 = "This is the content of document 2.";
        InputStream inputStream1 = new ByteArrayInputStream(content1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(content2.getBytes());

        // Put documents into the custom document store
        customDocumentStore.put(inputStream1, customUri1, TXT);
        customDocumentStore.put(inputStream2, customUri2, TXT);

        // Retrieve documents from the custom document store
        Document retrievedDocument1 = customDocumentStore.get(customUri1);
        Document retrievedDocument2 = customDocumentStore.get(customUri2);

        // Check if documents are retrieved correctly
        assertNotNull(retrievedDocument1);
        assertEquals(content1, retrievedDocument1.getDocumentTxt());

        assertNotNull(retrievedDocument2);
        assertEquals(content2, retrievedDocument2.getDocumentTxt());
    }

    @Test
    public void customDeleteAndGet() throws IOException {
        // Put a document into the custom document store
        String content = "This is the content of the document.";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        customDocumentStore.put(inputStream, customUri1, TXT);

        // Delete the document from the custom document store
        assertTrue(customDocumentStore.delete(customUri1));

        // Check if the document is deleted
        assertNull(customDocumentStore.get(customUri1));
    }

    @Test
    public void customUndo() throws IOException {
        String content = "This is the content of the document.";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        customDocumentStore.put(inputStream, customUri1, TXT);

        // Delete the document from the custom document store
        assertTrue(customDocumentStore.delete(customUri1));

        // Undo the delete operation
        customDocumentStore.undo();

        // Check if the document is restored after undo
        assertNotNull(customDocumentStore.get(customUri1));
    }

    @Test(expected = IllegalStateException.class)
    public void customUndoWithEmptyStackForURI() {
        // Attempt to undo when the stack for a specific URI is empty
        customDocumentStore.undo(customUri1);
    }

    @Test
    public void customSetAndGetMetadata() throws IOException {
        String content = "This is a sample document.";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        customDocumentStore.put(inputStream, customUri1, TXT);

        // Set metadata for the document
        String oldValue = customDocumentStore.setMetadata(customUri1, "author", "John Smith");
        String retrievedValue = customDocumentStore.getMetadata(customUri1, "author");
        String nonExistentValue = customDocumentStore.getMetadata(customUri1, "nonexistent");

        // Check if metadata is set and retrieved correctly
        assertNull(oldValue);
        assertEquals("John Smith", retrievedValue);
        assertNull(nonExistentValue);
    }

    @Test
    public void customSetMetadataAndGetOldValue() throws IOException, URISyntaxException {
        // Put a document into the custom document store
        DocumentStore customStore = new DocumentStoreImpl();
        String text = "Sample text.";
        String key = "author";
        String value = "Jane Doe";
        customStore.put(new ByteArrayInputStream(text.getBytes()), customUri1, TXT);

        // Set metadata for the document and retrieve the old value
        String oldValue = customStore.setMetadata(customUri1, key, value);

        // Check if the old value is null and the new value is set correctly
        assertNull(oldValue);
        assertEquals(value, customStore.getMetadata(customUri1, key));
    }

    @Test
    public void customUndoAfterMultiplePuts() {
        DocumentStore customStore = new DocumentStoreImpl();

        try {
            // Perform multiple puts
            customStore.put(new ByteArrayInputStream("Document 1".getBytes()), customUri1, TXT);
            customStore.put(new ByteArrayInputStream("Document 2".getBytes()), customUri2, TXT);
            customStore.put(new ByteArrayInputStream("Document 3".getBytes()), customUri3, TXT);
            customStore.put(new ByteArrayInputStream("Document 4".getBytes()), customUri4, TXT);

            // Undo each put in reverse order
            customStore.undo(customUri4);
            customStore.undo(customUri3);
            customStore.undo(customUri2);
            customStore.undo(customUri1);

            // Verify that the documents are not present after undoing
            assertNull(customStore.get(customUri1));
            assertNull(customStore.get(customUri2));
            assertNull(customStore.get(customUri3));
            assertNull(customStore.get(customUri4));

        } catch (IOException e) {
            fail("IOException should not be thrown during puts.");
        }

        // Undo when there are no actions to undo should throw an exception
        assertThrows(IllegalStateException.class, customStore::undo);
    }

    @Test
    public void customUndoAfterOnePut() {
        DocumentStore customStore = new DocumentStoreImpl();

        try {
            // Perform a single put
            customStore.put(new ByteArrayInputStream("Document 1".getBytes()), customUri1, TXT);

            // Undo the put
            customStore.undo();

            // Verify that the document is not present after undoing
            assertNull(customStore.get(customUri1));

        } catch (IOException e) {
            fail("IOException should not be thrown during put.");
        }

        // Undo when there are no actions to undo should throw an exception
        assertThrows(IllegalStateException.class, customStore::undo);
    }
}

