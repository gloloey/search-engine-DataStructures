package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

import java.util.*;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> { // implements ??
    /**
     * Instances of HashTable should be constructed with two type parameters, one for the type of the keys in the table and one for the type of the values
     *
     * @param <Key>
     * @param <Value>
     */
        private class Entry<Key, Value> {
            Key key;
            Value value;
            Entry<Key, Value> next;

            private Entry(Key k, Value v) {//private?
                if (k == null) {
                    throw new IllegalArgumentException();
                }
                key = k;
                value = v;
                next = null;
            }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Entry<?, ?> entry = (Entry<?, ?>) obj;
            return key.equals(entry.key);
        }
        }
        private Entry<?, ?>[] table;
        public HashTableImpl() {
            this.misura = 1;
            this.table = new Entry[this.misura]; 
        }
        private int hashFunction(Key key) {

            return (key.hashCode() & 0x7fffffff) % this.misura;
        }
        private int misura;
        private void resize (){
            int oldSize = this.misura;
            int newSize = oldSize * 2;
            this.misura = newSize;
            Entry<?, ?>[] newTable = new Entry[this.misura];
            for (Entry<?, ?> head : this.table) {
                Entry<Key, Value> current = (Entry<Key, Value>) head;
                while (current != null) {
                    int index = this.hashFunction(current.key);
                    Entry<Key, Value> newHead = (Entry<Key, Value>) newTable[index];
                    if (newHead == null) {
                        newTable[index] = current;
                        current = current.next;
                        continue;
                    }
                    while (newHead != null) {
                        newHead = newHead.next;
                    }
                    newHead = current;
                    current = current.next;
                }
            }
            this.table = newTable;
        }

        /**
         * @param k the key whose value should be returned
         * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
         */
        public Value get(Key k) {
            int index = this.hashFunction(k);
            Entry<Key, Value> current = (Entry<Key, Value>) this.table[index];
            while (current != null) {
                if (current.key.equals(k)) {
                    return (Value) current.value;
                }
                current = current.next;
            }
            return null;
        }

        /**
         * @param k the key at which to store the value
         * @param v the value to store
         *          To delete an entry, put a null value.
         * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
         */
        public Value put(Key k, Value v) {
            if (size() >= this.misura * 0.75 && v != null) { 
                resize();
            }
            int index = this.hashFunction(k);
            Entry<Key, Value> newData = new Entry<>(k, v);
            Entry<Key, Value> head = (Entry<Key, Value>) this.table[index];
            if (head == null) {
                this.table[index] = newData;
                return null;
            }
            Entry<Key, Value> current = head;
            Entry<Key, Value> previous = null;
            while (current != null) {
                if (current.key.equals(k)) {
                    if (v != null) {
                        Value oldValue = current.value;
                        current.value = v;
                        return oldValue;
                    } else {
                        // Handle deletion
                        if (current == head) {
                            this.table[index] = current.next;
                        } else {
                            previous.next = current.next;
                        }
                        return current.value;}}
                previous = current;
                current = current.next;}
            if (v != null) {previous.next = newData;}return null;}

        /**
         * @param key the key whose presence in the hashtabe we are inquiring about
         * @return true if the given key is present in the hashtable as a key, false if not
         * @throws NullPointerException if the specified key is null
         */
        public boolean containsKey(Key key) {
            if (key == null){
                throw new NullPointerException();
            }
            int index = this.hashFunction(key);
            Entry<Key, Value> head = (Entry<Key, Value>) this.table[index];
            Entry<Key, Value> current = head;
            while (current != null) {
                if (current.key.equals(key)) {
                    return true;
                }
                current = current.next;
            }
            return false;
        }

        /**
         * @return an unmodifiable set of all the keys in this HashTable
         * @see Collections#unmodifiableSet(Set)
         */
        public Set<Key> keySet() {
            Set<Key> keys = new HashSet<>();
            for (Entry<?, ?> head : this.table) {
                Entry<Key, Value> current = (Entry<Key, Value>) head;
                while (current != null) {
                    keys.add(current.key);
                    current = current.next;
                }
            }
            return  Collections.unmodifiableSet(keys);
        }

        /**
         * @return an unmodifiable collection of all the values in this HashTable
         * @see Collections#unmodifiableCollection(Collection)
         */
        public Collection<Value> values(){
            Collection<Value> valuesColl = new ArrayList<>();
            for (Entry<?, ?> head : this.table) {
                Entry<Key, Value> current = (Entry<Key, Value>) head;
                while (current != null) {
                    valuesColl.add(current.value);
                    current = current.next;
                }
            }
            return Collections.unmodifiableCollection(valuesColl);
        }

        /**
         * @return how "many" entries there currently are in the HashTable
         */
        public int size(){
            int size = 0;
            for (Entry<?, ?> head : this.table) {
                Entry<Key, Value> current = (Entry<Key, Value>) head;
                while (current != null) {
                    if (current.value != null) { // Only count nodes with non-null values
                        size++;
                    }
                    current = current.next;
                }
            }
            return size;
        }
    }
