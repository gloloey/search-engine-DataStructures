package edu.yu.cs.com1320.project.stage5.impl;

import com.sun.jdi.Value;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import javax.print.Doc;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class DocumentStoreImpl implements DocumentStore {
    private int maxDocCount = 0;
    private int maxDocBytes = 0;
    private TrieImpl<Document> myTrie = new TrieImpl<>();
    private HashTableImpl<URI, Document> hashTable1;
    private StackImpl<Undoable> myStack = new StackImpl<>();
    private MinHeap<Document> myHeap = new MinHeapImpl<>();

    // Public no-argument constructor
    public DocumentStoreImpl() {
        hashTable1 = new HashTableImpl<>();
    }

    /**
     * set the given key-value metadata pair for the document at the given uri
     *
     * @param uri
     * @param key
     * @param value
     * @return the old value, or null if there was no previous value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    @Override
    public String setMetadata(URI uri, String key, String value) {
        if (uri == null || uri.toString().isBlank() || key == null || key.isBlank() || hashTable1.get(uri) == null) {
            throw new IllegalArgumentException();
        }
        Document doc = hashTable1.get(uri);
        if (doc == null){
            return null;
        }
        String old = doc.getMetadataValue(key);
        // Lambda for undoing a put command
        this.myStack.push(new GenericCommand<>(uri, target -> {
            doc.setMetadataValue(key, old);
        }));

        //STAGE 5 add: Im gonna make the docs timer RE-start and then im gonna reheapify
        refreshDocTimer(doc);
        myHeap.reHeapify(doc);
        return doc.setMetadataValue(key, value);
    }
    /**
     * get the value corresponding to the given metadata key for the document at the given uri
     *
     * @param uri
     * @param key
     * @return the value, or null if there was no value
     * @throws IllegalArgumentException if the uri is null or blank, if there is no document stored at that uri, or if the key is null or blank
     */
    @Override
    public String getMetadata(URI uri, String key) {
        if (uri == null || uri.toString().isBlank() || hashTable1.get(uri) == null || key == null || key.isBlank()){
            throw new IllegalArgumentException();
        }
        Document doc1 = hashTable1.get(uri);
        if (doc1 == null){
            return null;
        }
        //STAGE 5 add: Im gonna make the docs timer RE-start and then im gonna reheapify
        refreshDocTimer(doc1);
        myHeap.reHeapify(doc1);
        return doc1.getMetadataValue(key);
    }

    /**
     * @param input  the document being put
     * @param uri    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc.
     * If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException              if there is an issue reading input
     * @throws IllegalArgumentException if uri is null or empty, or format is null
     */
    @Override
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null || uri.toString().isEmpty() || format == null){
            throw new IllegalArgumentException();
        }
        Integer x = getInteger(input, uri);
        if (x != null){
            return x;
        }
        try{
            byte[] array = input.readAllBytes();
            DocumentImpl doc;
            if (format == DocumentFormat.TXT) {
                String str = new String(array);
                doc = new DocumentImpl(uri, str);
                for (String word : doc.getWords()) {
                    myTrie.put(word, doc);
                }
            } else {
                doc = new DocumentImpl(uri, array);
            }
            Document oldValue = hashTable1.get(uri);
            hashTable1.put(uri, doc);
            //Im gonna put it in the heap, then Im gonna make the dos's timer start, and only then im gonna reheapify
            myHeap.insert(doc);
            refreshDocTimer(doc);
            myHeap.reHeapify(doc);
            docPolice();
            if (oldValue == null) {
                return 0;
            } else{
                return oldValue.hashCode();
            }
        } catch (IOException e){
            throw new IOException();}}

    private void docPolice() {
        if (this.maxDocCount != 0) {
            docPoliceForCount(this.maxDocCount);
        }
        if (this.maxDocBytes != 0) {
            docPoliceForBytes(this.maxDocBytes);
        }
    }

    private void refreshDocTimer(Document doc) {
        doc.setLastUseTime(System.nanoTime());
    }

    private Integer getInteger(InputStream input, URI uri) {
        Document old = hashTable1.get(uri);
        if (input != null) { //if its not a delete
            // Lambda for undoing a put command
            if (old != null) {//if before there was an old document
                this.myStack.push(new GenericCommand<>(uri, target -> {
                    this.hashTable1.put(target, old); 
                    for (String word : old.getWords()) {
                        myTrie.put(word, old);
                    };
                }));
            } else {
                this.myStack.push(new GenericCommand<>(uri, target -> {
                    this.hashTable1.put(target, null);
                }));
            }
        }else if (input == null) { 
            if (hashTable1.get(uri) == null) {
                delete(uri);
                return 0;
            }else{
                delete(uri);
                return hashTable1.get(uri).hashCode();
            }
        }return null;}
    private void deleteTrieNode(Document doc) {
        for (String word : doc.getWords()) {
            Set<Document> values = myTrie.get(word);
            if (values != null && values.contains(doc)) {
                Comparator<Document> myComp = new myComparator(word);
                List<Document> myList = myTrie.getAllWithPrefixSorted(word, myComp);
                if (myTrie.get(word).size() < myList.size() || myTrie.get(word).size() > 1) {
                    myTrie.delete(word, doc);
                } else {
                    myTrie.delete(word, doc);
                    cleaner(word);
                }
            }
        }
    }
    private void cleaner(String word) {
        String parola = word;
        for (int i = word.length(); i > 0; i--) {
            if (myTrie.get(parola).isEmpty() || myTrie.get(parola) == null) {
                myTrie.deleteAllWithPrefix(parola);
            } else {
                break;
            }
            parola = parola.substring(0, i);
        }
    }
    /**
     * @param url the unique identifier of the document to get
     * @return the given document
     */
    @Override
    public Document get(URI url) {//remember timer start or RE-start non cambia un cavolo
        if(hashTable1.get(url) != null) {
            refreshDocTimer(hashTable1.get(url));
            myHeap.reHeapify(hashTable1.get(url));
        }
        return hashTable1.get(url);
    }

    /**
     * @param url the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean delete(URI url) {
        if (hashTable1.containsKey(url)) {
            //delete from hashtable
            Document oldDocument = hashTable1.put(url, null);
            //delete from heap
            deleteCertainDocInHeap(oldDocument);
            if (oldDocument != null) {
                //delete from Trie
                deleteTrieNode(oldDocument);
                this.myStack.push(new GenericCommand<>(url, target -> {
                    //put back in hashtable
                    this.hashTable1.put(target, oldDocument);
                    //put back in heap
                    this.myHeap.insert(oldDocument);
                    refreshDocTimer(oldDocument);
                    this.myHeap.reHeapify(oldDocument);
                    //put back in Trie
                    for (String word : oldDocument.getWords()) {
                        myTrie.put(word, oldDocument);
                    };
                }));
            }
            return true;
        }
        return false;
    }

    private void deleteCertainDocInHeap (Document doc) {
        doc.setLastUseTime(0);
        myHeap.reHeapify(doc);
        myHeap.remove();
    }

    //**********STAGE 3 ADDITIONS

    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    public void undo() throws IllegalStateException {
        if (myStack.size() == 0 || myStack.toString().isEmpty() || myStack == null) {
            throw new IllegalStateException();
        }
        Undoable lastCommand = myStack.pop();
        if (lastCommand instanceof GenericCommand<?>) {
            GenericCommand<?> mioCommand = (GenericCommand<?>) lastCommand;
            mioCommand.undo();

        } else if (lastCommand instanceof CommandSet<?>) {
            ((CommandSet<?>) lastCommand).undoAll();
        } else {
            throw new IllegalStateException();
        }
        docPolice();
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     * @param url
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    public void undo(URI url) throws IllegalStateException {
        if (myStack.size() == 0) {
            throw new IllegalStateException();
        }
        int size = myStack.size();
        StackImpl<Undoable> tempStack = new StackImpl<>();
        Undoable lastCommand = myStack.peek();

        for (int i = 0; i < myStack.size(); ) {
            lastCommand = myStack.pop();
            if (lastCommand instanceof GenericCommand<?>) {
                GenericCommand myCommand = (GenericCommand) lastCommand;
                if (myCommand.getTarget().equals(url)) {
                    break;
                } else {
                    i++;
                    tempStack.push(lastCommand);
                }
            } else if (lastCommand instanceof CommandSet<?>) {
                CommandSet myCommand = (CommandSet) lastCommand;
                if (myCommand.containsTarget(url)) {
                    break;
                } else {
                    i++;
                    tempStack.push(lastCommand);
                }

            }
        }

        if (size == tempStack.size()) {
            throw new IllegalStateException();
        }

        if (lastCommand instanceof GenericCommand<?>) {
            lastCommand.undo();
        } else if (lastCommand instanceof CommandSet<?>) {
            ((CommandSet<?>) lastCommand).undoAll();
        } else {
            throw new IllegalStateException();
        }

        while (tempStack.size() != 0) {
            Undoable theComm = tempStack.pop();
            myStack.push(theComm);
        }
        docPolice();
    }

    private class myComparator implements Comparator<Document> {
        private String word;

        private myComparator(String w) {
            this.word = w;
        }

        @Override
        public int compare(Document doc1, Document doc2) {

            int count1 = doc1.wordCount(this.word);
            int count2 = doc2.wordCount(this.word);

            if (count1 > count2) {
                return -1;
            } else if (count1 < count2) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    
    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> search(String keyword){
        Comparator<Document> comp = new myComparator(keyword);
        List <Document> listOfDocs = myTrie.getSorted(keyword, comp);
        long tempo = System.nanoTime();
        for (Document doc : listOfDocs) {
            if (doc != null) {
                doc.setLastUseTime(tempo);
                myHeap.reHeapify(doc);
            }
        }
        return listOfDocs;
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByPrefix(String keywordPrefix) {
        Comparator<Document> comp = new myComparator(keywordPrefix);
        List <Document> listOfDocs = myTrie.getAllWithPrefixSorted(keywordPrefix, comp);
        long tempo = System.nanoTime();
        for (Document doc : listOfDocs) {
            if (doc != null) {
                //STAGE 5 add: Im gonna make the docs timer RE-start and then im gonna reheapify
                doc.setLastUseTime(tempo);
                myHeap.reHeapify(doc);
            }
        }
        return listOfDocs;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAll(String keyword) {
        Set<URI> deletedDocuments = new HashSet<>();

        // Retrieve all documents containing the keyword
        List<Document> documents = search(keyword);

        // Delete each document and collect its URI
        for (Document document : documents) {
            URI uri = document.getKey();
            boolean deleted = delete(uri);
            if (deleted) {
                deletedDocuments.add(uri);
            }
        }

        return deletedDocuments;
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) { // do I have to put undo lamdas in these methods?!?!?!?, also look at all the piazza questions and dont finish in this situa ever again
        Set<URI> deletedDocuments = new HashSet<>();

        // Retrieve all documents whose text starts with the given prefix
        List<Document> documents = searchByPrefix(keywordPrefix);

        // Delete each document and collect its URI
        for (Document document : documents) {
            URI uri = document.getKey();
            deletedDocuments.add(uri);
            hashTable1.put(uri, null);
            for (String word : document.getWords()) {
                myTrie.delete(word, document);
                cleaner(word); //?
            }
        }
        return deletedDocuments;
    }

    /**
     * @param keysValues metadata key-value pairs to search for
     * @return a List of all documents whose metadata contains ALL OF the given values for the given keys. If no documents contain all the given key-value pairs, return an empty list.
     */
    public List<Document> searchByMetadata(Map<String, String> keysValues) {
        List<Document> matchingDocuments = new ArrayList<>();

        // Iterate over all documents
        for (Document document : hashTable1.values()) {
            boolean matches = true;

            // Check if the document contains all the specified key-value pairs
            for (Map.Entry<String, String> entry : keysValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (document.getMetadataValue(key) == null) {
                    matches = false;
                    break;
                }
                // If ANY key-value pair does not match, skip to the next document (DAVKA any and not every)
                if (!document.getMetadataValue(key).equals(value)) {
                    matches = false;
                    break;
                }
            }

            // If all key-value pairs match, add the document to the list of matching documents
            if (matches) {
                matchingDocuments.add(document);
            }
        }

        long tempo = System.nanoTime();
        for (Document doc : matchingDocuments) {
            if (doc != null) {
                doc.setLastUseTime(tempo);
                myHeap.reHeapify(doc);
            }
        }
        return matchingDocuments;
    }


    /**
     * Retrieve all documents whose text contains the given keyword AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword
     * @param keysValues
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        List<Document> matchingDocuments = new ArrayList<>();

        // Step 1: Search for documents containing the keyword
        List<Document> keywordMatches = myTrie.getSorted(keyword, new myComparator(keyword));

        // Step 2: Filter the keyword matches based on metadata key-value pairs
        for (Document document : keywordMatches) {
            boolean metadataMatches = true;

            // Check if the document contains all the specified metadata key-value pairs
            for (Map.Entry<String, String> entry : keysValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // If any key-value pair does not match, skip to the next document
                if (!document.getMetadataValue(key).equals(value)) {
                    metadataMatches = false;
                    break;
                }
            }

            // If all key-value pairs match, add the document to the list of matching documents
            if (metadataMatches) {
                matchingDocuments.add(document);
            }
        }

        long tempo = System.nanoTime();
        for (Document doc : matchingDocuments) {
            if (doc != null) {
                doc.setLastUseTime(tempo);
                myHeap.reHeapify(doc);
            }
        }
        return matchingDocuments;
    }


    /**
     * Retrieve all documents that contain text which starts with the given prefix AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        List<Document> matchingDocuments = new ArrayList<>();

        // Step 1: Retrieve documents containing text starting with the given prefix
        List<Document> prefixMatches = myTrie.getAllWithPrefixSorted(keywordPrefix, new myComparator(keywordPrefix));

        // Step 2: Filter the prefix matches based on metadata key-value pairs
        for (Document document : prefixMatches) {
            boolean metadataMatches = true;

            // Check if the document contains all the specified metadata key-value pairs
            for (Map.Entry<String, String> entry : keysValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // If any key-value pair does not match, skip to the next document
                if (!document.getMetadataValue(key).equals(value)) {
                    metadataMatches = false;
                    break;
                }
            }

            // If all key-value pairs match, add the document to the list of matching documents
            if (metadataMatches) {
                matchingDocuments.add(document);
            }
        }

        long tempo = System.nanoTime();
        for (Document doc : matchingDocuments) {
            if (doc != null) {
                doc.setLastUseTime(tempo);
                myHeap.reHeapify(doc);
            }
        }
        return matchingDocuments;
    }


    /**
     * Completely remove any trace of any document which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) {
        Set<URI> deletedURIs = new HashSet<>();

        // Iterate through all documents
        for (URI uri : hashTable1.keySet()) {
            Document document = hashTable1.get(uri);
            boolean metadataMatches = true;

            // Check if the document contains all the specified metadata key-value pairs
            for (Map.Entry<String, String> entry : keysValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // If any key-value pair does not match, skip to the next document
                if (!document.getMetadataValue(key).equals(value)) {
                    metadataMatches = false;
                    break;
                }
            }

            // If all key-value pairs match, delete the document
            if (metadataMatches) {
                delete(uri);
                deletedURIs.add(uri);
            }
        }

        return deletedURIs;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        Set<URI> deletedURIs = new HashSet<>();

        // Iterate through all documents
        for (URI uri : hashTable1.keySet()) {
            Document document = hashTable1.get(uri);

            // Check if the document contains the specified keyword
            if (document.getWords().contains(keyword)) { //chiedi se deve contenere la parola strippata o quella non strippata !!

                boolean metadataMatches = true;

                // Check if the document contains all the specified metadata key-value pairs
                for (Map.Entry<String, String> entry : keysValues.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    // If any key-value pair does not match, skip to the next document
                    if (!document.getMetadataValue(key).equals(value)) {
                        metadataMatches = false;
                        break;
                    }
                }

                // If all conditions are satisfied, delete the document
                if (metadataMatches) {
                    delete(uri);
                    deletedURIs.add(uri);
                }
            }
        }

        return deletedURIs;
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        Set<URI> deletedURIs = new HashSet<>();

        // Iterate through all documents
        for (URI uri : hashTable1.keySet()) {
            Document document = hashTable1.get(uri);

            Comparator<Document> comp = new myComparator(keywordPrefix);
            // Check if the document contains text starting with the specified prefix
            if (myTrie.getAllWithPrefixSorted(keywordPrefix, comp).contains(document)) {

                boolean metadataMatches = true;

                // Check if the document contains all the specified metadata key-value pairs
                for (Map.Entry<String, String> entry : keysValues.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    // If any key-value pair does not match, skip to the next document
                    if (!document.getMetadataValue(key).equals(value)) {
                        metadataMatches = false;
                        break;
                    }
                }

                // If all conditions are satisfied, delete the document
                if (metadataMatches) {
                    delete(uri);
                    deletedURIs.add(uri);
                }
            }
        }

        return deletedURIs;
    }

    //**********STAGE 5 ADDITIONS

    /**
     * set maximum number of documents that may be stored
     * @param limit
     * @throws IllegalArgumentException if limit < 1
     */
    public void setMaxDocumentCount(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException();
        }
        this.maxDocCount = limit;
        docPoliceForCount(limit);
    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents in memory combined
     * @param limit
     * @throws IllegalArgumentException if limit < 1
     */
    public void setMaxDocumentBytes(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException();
        }
        this.maxDocBytes = limit;
        docPoliceForBytes(limit);
    }

    /**
     * check that the both document limits are respected and if not delete the excess
    */
    private void docPoliceForCount (int newLimit) {
        List<Document> allDocs = new ArrayList<>();
        for (URI k : hashTable1.keySet()) {
            allDocs.add(hashTable1.get(k));
        }

        int totCount = allDocs.size();
        if(totCount > newLimit) {
            while (totCount > newLimit) {
                Document doc = myHeap.peek();
                if (doc == null) {
                    continue;
                }
                delete(doc.getKey());
                myStack.pop();
                totCount -= 1;
            }
        }
    }
    private void docPoliceForBytes (int newLimit) {
        List<Document> allDocs = new ArrayList<>();
        for (URI k : hashTable1.keySet()) {
            allDocs.add(hashTable1.get(k));
        }
        int totBytes = 0;
        for (Document d : allDocs) {
            String docTxt = d.getDocumentTxt();
            if (docTxt != null) {
                byte[] array = docTxt.getBytes();
                totBytes += array.length;
            }
            byte[] array1 = d.getDocumentBinaryData();
            if (array1 != null) {
                totBytes += array1.length;
            }
        }

        if(totBytes > newLimit) {
            while (totBytes > newLimit) {
                Document doc = myHeap.peek();
                if (doc == null) {
                    continue;
                }
                delete(doc.getKey());
                myStack.pop();
                String docTxt = doc.getDocumentTxt();
                if (docTxt != null) {
                    byte[] array = docTxt.getBytes();
                    totBytes -= array.length;
                }
                byte[] array1 = doc.getDocumentBinaryData();
                if (array1 != null) {
                    totBytes -= array1.length;
                }
            }
        }
    }
}
