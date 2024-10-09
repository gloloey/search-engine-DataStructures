package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {
    private static final int alphabetSize = 256; // extended ASCII
    private Node<Value> root; // root of trie

    public TrieImpl(){
        this.root = new Node<>();
    }
    private class Node<Value> {
        private Set<Value> values;
        private Node[] children;
        private Node() {
            this.values = new HashSet<>();
            this.children = new Node[TrieImpl.alphabetSize];
        }
    }

    /**
     * add the given value at the given key
     *
     * @param key
     * @param val
     */
    @Override
    public void put(String key, Value val) {
        if (!(key == null || val == null || key.isEmpty())) {
            root = put(root, key, val, 0);
        }
    }

    private Node<Value> put(Node<Value> x, String key, Value val, int d) {
        if (x == null) {
            x = new Node<>();
        }
        if (d == key.length()) {
            x.values.add(val);
            return x;
        } else {
            char c = key.charAt(d);
            x.children[c] = this.put(x.children[c], key, val, d + 1);
            return x;
        }
    }

    /**
     * Get all exact matches for the given key, sorted in descending order, where "descending" is defined by the comparator.
     * NOTE FOR COM1320 PROJECT: FOR PURPOSES OF A *KEYWORD* SEARCH, THE COMPARATOR SHOULD DEFINE ORDER AS HOW MANY TIMES THE KEYWORD APPEARS IN THE DOCUMENT.
     * Search is CASE SENSITIVE.
     *
     * @param key
     * @param comparator used to sort values
     * @return a List of matching Values. Empty List if no matches.
     */
    @Override
    public List<Value> getSorted(String key, Comparator<Value> comparator) {
        List<Value> myList = new ArrayList<>();
        if (key==null || key.isEmpty()){
            return myList;
        }
        Set<Value> values = get(key);
        myList.addAll(values);
        myList.sort(comparator);
        return myList;
    }

    /**
     * get all exact matches for the given key.
     * Search is CASE SENSITIVE.
     *
     * @param key
     * @return a Set of matching Values. Empty set if no matches.
     */
    @Override
    public Set<Value> get(String key) {
        Set<Value> values = new HashSet<>();
        if (key==null || key.isEmpty()) {
            return values;
        }
        Node<Value> node = root;
        for (int i = 0; i < key.length(); i++) {
            char ch = key.charAt(i);
            int index = ch;
            if (node.children[index] == null) {
                return values;
            }
            node = node.children[index];
        }
        return node.values;
    }

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order, where "descending" is defined by the comparator.
     * NOTE FOR COM1320 PROJECT: FOR PURPOSES OF A *KEYWORD* SEARCH, THE COMPARATOR SHOULD DEFINE ORDER AS HOW MANY TIMES THE KEYWORD APPEARS IN THE DOCUMENT.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE SENSITIVE.
     *
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order. Empty List if no matches.
     */
    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        List<Value> myList = new ArrayList<>();
        if (prefix==null || prefix.isEmpty()){
            return myList;
        }
        Node<Value> node = root;
        for (int i = 0; i < prefix.length(); i++) {
            char ch = prefix.charAt(i);
            int index = ch; // Convert character to index
            node = node.children[index];
            if (node == null) {
                return myList;
            }
        }
        collectAllWithPrefix(node, myList);
        myList.sort(comparator);
        return myList;
    }
    //recursively collect all values with the given prefix
    private void collectAllWithPrefix(Node<Value> node, List<Value> result) {
        if (node.values != null) {
            result.addAll(node.values); // Add values at the current node
        }
        for (Node<Value> child : node.children) {
            if (child != null) {
                collectAllWithPrefix(child, result); // Recursively traverse children
            }
        }
    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE SENSITIVE.
     *
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    private Set<Value> deletedValues = new HashSet<>();
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        deletedValues = new HashSet<>();
        Node<Value> node = getNode(prefix); // Get the node corresponding to the prefix
        if (node != null) {
            deletedValues.addAll(node.values); // Add values to deleted set
            node.values.clear(); // Clear values at the current node
            deleteSubtree(node); // Delete subtree rooted at the current node
        }
        return deletedValues;
    }

    // method to recursively delete subtree rooted at the given node
    private void deleteSubtree(Node<Value> node) {
        for (int i = 0; i < node.children.length; i++) {
            deletedValues.addAll(node.values);
            if (node.children[i] != null) {
                deleteSubtree(node.children[i]); // Recursively delete children
            }
            node.children[i] = null; // Remove reference to child node
        }
    }
    //get the node corresponding to the prefix
    private Node<Value> getNode(String prefix) {
        Node<Value> node = root;
        for (int i = 0; i < prefix.length(); i++) {
            char ch = prefix.charAt(i);
            int index = ch; // Convert character to index
            if (node.children[index] == null) {
                return null; // Prefix not found
            }
            node = node.children[index];
        }
        return node;
    }

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     *
     * @param key
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAll(String key) {
        Set<Value> deletedValues = new HashSet<>();
        Node<Value> node = getNode(key); // Get the node corresponding to the key
        if (node != null) {
            deletedValues.addAll(node.values); // Add values to deleted set
            node.values.clear(); // Clear values at the current node
        }
        return deletedValues;
    }

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     *
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    @Override
    public Value delete(String key, Value val) {
        Node<Value> node = getNode(key); // Get the node corresponding to the key
        if (node != null && node.values.remove(val)) {
            return val; // Value successfully removed
        }
        return null; // Value not found or key not found
    }
}
