package com.example;

import java.util.Iterator;
import java.util.Objects;

/**
 * Created by Ian on 8/25/2017.
 */

public class Stack<T extends Comparable<T>> implements Iterable<T>{

    private Node head = new Node(null);

    public Stack(){
    }

    public Stack(Node element){
        head.next = Objects.requireNonNull(element);
    }

    public void add(T ele){
        Node now = head;
        while (now.next != null){
            now = now.next;
        }
        now.next = new Node(ele);
    }
    public void print(){
        for(T data : this){
            System.out.print(data.toString() + ", ");
        }
        System.out.println("\n");
    }

    public static final String BUBBLE_SORT = "bubble sort";
    public static final String MERGE_SORT = "merge sort";
    public void sort(String type){
        int size = size();
        long start = System.nanoTime();
        int round;
        switch (type){
            case MERGE_SORT:
                round = mergeSort();
                break;
            default:
                type = BUBBLE_SORT;
                round = bubbleSort();
                break;
        }
        long time = System.nanoTime() - start;
        System.out.println(type + " takes " + time + " nanos, " + "size is: " + size + ", and round is: " + round);
    }
    public Node get(int index){
        Node data = head;
        int count = -1;
        while ((data = data.next) != null){
            count++;
            if(index == count){
                return data;
            }
        }
        throw new IndexOutOfBoundsException("index is " + index + ", which is outside the bound: " + count + 1);
    }
    private int bubbleSort(){
        int ret = 0;
        int size = size();
        if(size < 2){
            return 0;
        }
        System.out.println("start bubble sort!");
        for (int i = 0; i < size - 1;i++){
            boolean sorted = true;
            Node cursor = head;
            for (int j = 0; j < size - 1 - i; cursor = cursor.next,j++) {
                Node first = cursor.next;
                Node later = cursor.next.next;
                Node last = cursor.next.next.next;
                int result = first.t.compareTo(later.t);
                if(result > 0){
                    cursor.next = later;
                    later.next = first;
                    first.next = last;
                    sorted = false;
                } else {
                    sorted &= true;
                }
                ret++;
            }
            if(sorted){
                return ret;
            }
        }
        return  ret;
    }

    private int mergeSort(){
        int size = size();
        if(size < 2){
            return 0;
        }
        int turn = 0;
        for (int i = 1; i <= size; i = i * 2) {
            int length = i * 2;
            Node start1 = head;
            Node end1 = skip(head,i);
            Node start2 = end1;
            if(start2.next == null){
                return turn;
            }
            Node end2 = skip(start2,i);
            for (int j = 0; j <= size / length; j++) {
                Node cursor1 = start1;
                Node cursor2 = start2;
                Node mark2 = end2.next;
                while (cursor1.next != null  && cursor2.next != null && cursor1 != cursor2){
                    int result = cursor1.next.t.compareTo(cursor2.next.t);
                    if(result > 0) {
                        Node data1 = cursor1.next;
                        Node data2 = cursor2.next;
                        Node endRight = cursor2.next.next;
                        cursor1.next = data2;
                        data2.next = data1;
                        cursor2.next = endRight;
                        if(cursor2.next == mark2){
                            turn++;
                            break;
                        }
                    }
                    cursor1 = cursor1.next;
                    turn++;
                }
                start1 = skip(start1,length);
                end1 = skip(start1,i);
                start2 = end1;
                if(start2.next == null){
                    break;
                }
                end2 = skip(start2,i);
            }
            turn++;
        }
        return turn;
    }

    private Node skip(Node data, int count){
        for (int i = 0; data.next != null && i < count ; i++) {
            data = data.next;
        }
        return data;
    }

    public void clear(){
        head.next = null;
    }
    public void replace(Stack<T> stack){
        this.head = stack.head;
    }

    public int size(){
        int size = 0;
        for (T t : this){
            size++;
        }
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new DataIterator(this);
    }

    private class DataIterator implements Iterator<T>{
        Node cursor;

        DataIterator(Stack<T> stack){
            Node head = stack.head;
            cursor = head.next;
        }

        @Override
        public boolean hasNext() {
            return cursor!= null;
        }

        @SuppressWarnings("unchecked")
        public T next() {
            Node now = cursor;
            cursor = cursor.next;
            return now.t;
        }

        @Override
        public void remove() {

        }
    }

    private class Node{
        T t;
        Node next;
        public Node(T t){
            this.t = t;
        }
        public T get(){
            return t;
        }

    }
}
