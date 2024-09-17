package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {
    public StackImpl(){
        this.head = null;
    }
    private class Node<T> {
        T command;
        Node<T> next;

        private Node(T c) {
            if (c == null) {
                throw new IllegalArgumentException();
            }
            command = c;
            next = null;
        }
    }
    private Node<T> head;
    /**
     * @param element object to add to the Stack
     */
    public void push(T element){
        Node<T> bob = new Node<>(element);
        bob.next = head;
        head = bob;
    }

    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    public T pop(){
        if (size() == 0){
            return null;
        }
        Node<T> value = head;
        head = head.next;
        return value.command;
    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
    public T peek(){
        if (size() == 0){
            return null;
        }
        return head.command;
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    public int size(){
        int count = 0;
        Node<T> current = head;
        while (current != null) {
            count++;
            current = current.next;
        }
        return count;
    }
}
