package edu.yu.cs.com1320.project.stage4;
import static org.junit.Assert.*;

//import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;

public class HashTableTest {
    private HashTableImpl<String, String> hashTable = new HashTableImpl<>();

    @Test
    public void testGet() {
        hashTable.put("key", "value");
        assertEquals(hashTable.get("key2"), null);
    }

    @Test
    public void testContainsKey() {
        hashTable.put("key1", "value1");
        assertTrue(hashTable.containsKey("key1"));
        assertFalse(hashTable.containsKey("nonExistentKey"));
    }

    @Test
    public void testKeySet() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();
        hashTable.put("key1", 1);
        hashTable.put("key2", 2);
        Set<String> keySet = hashTable.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("key1"));
        assertTrue(keySet.contains("key2"));
    }

    @Test
    public void HashTableTestoldval() {
        hashTable.put("key", "value");
        assertEquals(hashTable.put("key", "null"), "value");

    }

    @Test
    public void testSize() {
        hashTable.put("key1", "value1");
        hashTable.put("key2", "value2");
        hashTable.put("key3", "value3");
        hashTable.put("key4", "value4");
        assertEquals(hashTable.size(), 4);
    }

    @Test
    public void testdelete() {
        HashTableImpl<Integer, Integer> hashTable = new HashTableImpl<Integer, Integer>();
        hashTable.put(1, 2);
        hashTable.put(1, null);
        assertFalse(hashTable.containsKey(1));
    }
    @Test
    public  void testdelte2(){
        HashTableImpl<Integer, Integer> hashTable = new HashTableImpl<Integer, Integer>();
        hashTable.put(1,1);
        hashTable.put(2,2);
        hashTable.put(1,null);
        assertNull(hashTable.get(1));
        assertTrue(hashTable.containsKey(2));
    }
    @Test
    public void hashTabledelete2(){
        HashTableImpl<String, String> hashTable = new HashTableImpl<>();
        hashTable.put("key", "val");
        hashTable.put("key2", "val2");
        hashTable.put("key", null);
        assertFalse(hashTable.containsKey("key"));
        assertEquals(hashTable.get("key"), null);
        assertEquals(hashTable.get("key2"), ("val2"));
}
}
