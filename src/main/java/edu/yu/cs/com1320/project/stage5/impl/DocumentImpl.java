package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {
    //stage4
    private HashMap<String, Integer> quantities;
    private HashTableImpl<String, String> hashTable = new HashTableImpl<>();
    private URI uri;
    private String txt;
    private byte[] binaryData;
    private long lastUseTime;
    //DocumentImpl must provide the following two constructors, which should throw an
    //java.lang.IllegalArgumentException if either argument is null or empty/blank:
    public DocumentImpl(URI uri, String txt){
        if(uri == null || uri.toString().isEmpty() || txt == null || txt.isEmpty()){
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.txt = txt;
        this.lastUseTime = System.nanoTime();

        this.quantities = new HashMap<>(getStrings().size());
        for (String str : getStrings()) {
            this.quantities.put(str, myCounter(str));
        }
    }
    public DocumentImpl(URI uri, byte[] binaryData){
        if(uri == null || uri.toString().isEmpty() || binaryData == null || binaryData.length == 0){
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.binaryData = binaryData;
        this.lastUseTime = System.nanoTime();
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (txt != null ? txt.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }
    @Override
    public boolean equals(Object doc) {
        if (doc == null || doc.getClass() != this.getClass()){
            return false;
        }
        if (this == doc){
            return true;
        }
        Document document = (DocumentImpl) doc;
        return document.hashCode() == hashCode();
    }
    /**
     * @param key   key of document metadata to store a value for
     * @param value value to store
     * @return old value, or null if there was no old value
     * @throws IllegalArgumentException if the key is null or blank
     */
    @Override
    public String setMetadataValue(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException();
        }
        return hashTable.put(key, value);
    }

    /**
     * @param key metadata key whose value we want to retrieve
     * @return corresponding value, or null if there is no such key
     * @throws IllegalArgumentException if the key is null or blank
     */
    @Override
    public String getMetadataValue(String key) {
        if (key == null || key.isEmpty()){
            throw new IllegalArgumentException();
        }
        String metaValue = hashTable.get(key);
        if (metaValue == null || metaValue.isEmpty()){
            return null;
        }
        return metaValue;
    }

    /**
     * @return a COPY of the metadata saved in this document
     */
    @Override
    public HashTableImpl<String, String> getMetadata() {
        HashTableImpl<String, String> copyMetadata = new HashTableImpl<>();
        // Iterate through the original metadata and put each key-value pair into the copy
        for (String key : hashTable.keySet()) {
            String value = hashTable.get(key);
            copyMetadata.put(key, value);
        }
        return copyMetadata;
    }

    /**
     * @return content of text document
     */
    @Override
    public String getDocumentTxt() {
        return this.txt;
    }

    /**
     * @return content of binary data document
     */
    @Override
    public byte[] getDocumentBinaryData() {
        return this.binaryData;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    @Override
    public URI getKey() {
        return this.uri;
    }

    //***************STAGE 4 ADDITIONS

    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    public int wordCount(String word){
        //return this.quantities.get(word);
        return myCounter(word);
    }

    private int myCounter(String word) {
        int wordCount = 0;
        if (word == null || word.isEmpty()) {
            return wordCount;
        }
        if (this.txt == null || this.txt.isEmpty()){
            return wordCount;
        }

        String word1 = word.replaceAll("[^\\p{IsAlphabetic}0-9\\s]+","");


        for (String parola : getStringsAsList()) {
            if (parola.equals(word1)) {
                wordCount++;
            }
        }
        return wordCount;
    }

    /**
     * @return all the words that appear in the document
     */
    public Set<String> getWords(){
        return this.quantities.keySet();
    }


    private Set<String> getStrings() {
        Set<String> mySet = new HashSet<>();
        if (this.txt == null || this.txt.isEmpty()){
            return mySet;
        }
        String myText = this.txt.replaceAll("[^\\p{IsAlphabetic}0-9\\s]+","");
        String[] lista = myText.split( " ");
        for (String str : lista){
            str = str.replaceAll("[^\\p{IsAlphabetic}0-9\\s]+","");
            str = str.trim();
        }
        mySet.addAll(Arrays.asList(lista));
        //System.out.println(mySet);
        return mySet;
    }

    private List<String> getStringsAsList() {
        List<String> mySet = new ArrayList<>();
        if (this.txt == null || this.txt.isEmpty()){
            return mySet;
        }
        String myText = this.txt.replaceAll("[^\\p{IsAlphabetic}0-9\\s]+","");
        String[] lista = myText.split( " ");
        for (String str : lista){
            str = str.replaceAll("[^\\p{IsAlphabetic}0-9\\s]+","");
            str = str.trim();
        }
        mySet.addAll(Arrays.asList(lista));
        //System.out.println(mySet);
        return mySet;
    }


    //STAGE 5

    /**
     * return the last time this document was used, via put/get or via a search result
     * (for stage 4 of project)
     */
    @Override
    public long getLastUseTime() {
        return this.lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.lastUseTime = timeInNanoseconds;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure {@link Integer#signum
     * signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))} for
     * all {@code x} and {@code y}.  (This implies that {@code
     * x.compareTo(y)} must throw an exception if and only if {@code
     * y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code
     * x.compareTo(y)==0} implies that {@code signum(x.compareTo(z))
     * == signum(y.compareTo(z))}, for all {@code z}.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     * @apiNote It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     */
    @Override
    public int compareTo(Document o) {
        if (o == null) {
            throw new NullPointerException("Cannot compare with a null document");
        }

        // Cast the input Document to DocumentImpl to access its lastUseddTime
        DocumentImpl otherDocument = (DocumentImpl) o;

        // Compare lastUseddTime of this document with the lastUseddTime of the other document
        if (this.lastUseTime < otherDocument.getLastUseTime()) {
            return -1; // This document was used earlier (comes before)
        } else if (this.lastUseTime > otherDocument.getLastUseTime()) {
            return 1; // This document was used later (comes after)
        } else {
            return 0; // Both documents have the same lastUseddTime
        }
    }
}
