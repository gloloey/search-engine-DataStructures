package edu.yu.cs.com1320.project.stage5;


import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import java.util.*;

public class TrieImplTest {

    private TrieImpl<Integer> trie;

    @Before
    public void setUp() {
        trie = new TrieImpl<>();
    }

    @Test
    public void testPutAndGet() {
        trie.put("hello", 1);
        trie.put("world", 2);
        trie.put("java", 3);

        Set<Integer> values1 = trie.get("hello");
        Set<Integer> values2 = trie.get("world");
        Set<Integer> values3 = trie.get("java");

        assertEquals(1, values1.size());
        assertTrue(values1.contains(1));

        assertEquals(1, values2.size());
        assertTrue(values2.contains(2));

        assertEquals(1, values3.size());
        assertTrue(values3.contains(3));
    }

    @Test
    public void testGetSorted() {
        trie.put("hello", 1);
        trie.put("world", 2);
        trie.put("java", 3);

        List<Integer> sortedValues = trie.getSorted("hello", Comparator.naturalOrder());
        assertEquals(1, sortedValues.size());
        assertEquals(1, (int) sortedValues.get(0));
    }

    @Test
    public void testGetAllWithPrefixSorted() {
        trie.put("hello", 1);
        trie.put("help", 2);
        trie.put("helps", 3);
        trie.put("world", 4);

        List<Integer> sortedValues = trie.getAllWithPrefixSorted("hel", Comparator.naturalOrder());
        assertEquals(3, sortedValues.size());
        assertEquals(3, (int) sortedValues.get(0));
        assertEquals(2, (int) sortedValues.get(1));
        assertEquals(1, (int) sortedValues.get(2));
    }

    @Test
    public void testDeleteAllWithPrefix() {
        trie.put("hello", 1);
        trie.put("help", 2);
        trie.put("helps", 3);
        trie.put("world", 4);

        Set<Integer> deletedValues = trie.deleteAllWithPrefix("hel");
        assertEquals(3, deletedValues.size());
        assertTrue(deletedValues.contains(1));
        assertTrue(deletedValues.contains(2));
        assertTrue(deletedValues.contains(3));
        assertFalse(deletedValues.contains(4));

        assertEquals(0, trie.get("hello").size());
        assertEquals(0, trie.get("help").size());
        assertEquals(0, trie.get("helps").size());
        assertEquals(1, trie.get("world").size());
    }

    @Test
    public void testDeleteAll() {
        trie.put("hello", 1);
        trie.put("world", 2);

        Set<Integer> deletedValues = trie.deleteAll("hello");
        assertEquals(1, deletedValues.size());
        assertTrue(deletedValues.contains(1));

        assertEquals(0, trie.get("hello").size());
        assertEquals(1, trie.get("world").size());
    }

    @Test
    public void testDelete() {
        trie.put("hello", 1);
        trie.put("world", 2);

        Integer deletedValue = trie.delete("hello", 1);
        assertEquals(1, (int) deletedValue);

        assertEquals(0, trie.get("hello").size());
        assertEquals(1, trie.get("world").size());

        // Try to delete non-existent value
        assertNull(trie.delete("world", 999));
    }
}
