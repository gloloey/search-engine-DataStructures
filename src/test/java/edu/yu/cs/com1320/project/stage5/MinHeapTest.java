package edu.yu.cs.com1320.project.stage5;

import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;


import java.io.IOException;
import java.util.NoSuchElementException;
public class MinHeapTest {

    // Test insert method
    @Test
    public void testInsert() {
        MinHeap<Integer> minHeap = new MinHeapImpl<>();

        minHeap.insert(5);
        minHeap.insert(3);
        minHeap.insert(8);
        minHeap.insert(1);

        assertEquals(Integer.valueOf(1), minHeap.peek()); // Minimum element should be 1
    }

    // Test peek method
    @Test
    public void testPeek() {
        MinHeap<String> minHeap = new MinHeapImpl<>();

        minHeap.insert("apple");
        minHeap.insert("banana");
        minHeap.insert("orange");

        assertEquals("apple", minHeap.peek()); // Minimum element should be "apple"
    }

    // Test remove method
    @Test
    public void testRemove() {
        MinHeap<Integer> minHeap = new MinHeapImpl<>();

        minHeap.insert(5);
        minHeap.insert(3);
        minHeap.insert(8);
        minHeap.insert(1);

        assertEquals(Integer.valueOf(1), minHeap.remove()); // Remove and return the minimum element
        assertEquals(Integer.valueOf(3), minHeap.peek());   // New minimum element should be 3
    }

    // Test remove method on an empty heap
    @Test(expected = NoSuchElementException.class)
    public void testRemoveFromEmptyHeap() {
        MinHeap<Integer> minHeap = new MinHeapImpl<>();

        minHeap.remove(); // Attempt to remove from an empty heap, should throw NoSuchElementException
    }
}
