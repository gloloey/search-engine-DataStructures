package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {

    private static final int DEFAULT_CAPACITY = 1;

    public MinHeapImpl() {
        this.elements = (E[]) new Comparable[DEFAULT_CAPACITY];
    }
    @Override
    public void reHeapify(E element) {
        int index = getArrayIndex(element);
        // Percolate the element up or down depending on its new priority
        upHeap(index);
        downHeap(index);
    }

    @Override
    //RIMETTILO PROTECTED
    protected int getArrayIndex(E element) {
        for (int i = 1; i <= count; i++) {
            if (elements[i].equals(element)) {
                return i;
            }
        }
        throw new NoSuchElementException("Element not found in the heap");
        // Element not found
    }

    @Override
    protected void doubleArraySize() {
        int newCapacity = elements.length * 2;
        E[] newElements = (E[]) new Comparable[newCapacity];
        for (int i = 1; i <= count; i++) {
            newElements[i] = elements[i];
        }
        elements = newElements;
    }
}