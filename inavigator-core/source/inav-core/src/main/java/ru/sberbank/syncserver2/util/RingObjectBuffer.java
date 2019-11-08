package ru.sberbank.syncserver2.util;

/**
 * Created by sbt-kozhinsky-lb on 26.02.14.
 */
public class RingObjectBuffer {
    private Object[] buffer;
    private int firstFree;
    private int size;
    private int maxCapacity;

    public RingObjectBuffer(int minCapacity, int maxCapacity) {
        minCapacity = Math.max(minCapacity,8);
        this.buffer = new Object[minCapacity];
        this.firstFree = 0;
        this.size = 0;
        this.maxCapacity = maxCapacity;
    }

    public void add(Object element){
        //1. Add space if required
        if(size+1==buffer.length && buffer.length<maxCapacity){
            int newCapacity = Math.min(buffer.length*2, maxCapacity);
            Object[] newBuffer = new Object[newCapacity];
            System.arraycopy(buffer,0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
            //System.out.println("FINISH SPACING "+buffer.length);
        }

        //2. Adding
        buffer[firstFree] = element;
        //System.out.println("FIRST FREE = "+firstFree+" SIZE = "+size+" CAPACITY = "+buffer.length);
        firstFree = (firstFree +1) % buffer.length;
        size = Math.min(size+1, buffer.length);
        //System.out.println("ADDING "+firstFree+ " "+size);
    }

    public Object[] toArray(){
        if(size<buffer.length){
            //System.out.println("SMALL = "+size);
            Object[] result = new Object[size];
            System.arraycopy(buffer,0,result,0,size);
            return result;
        } else {
            //System.out.println("LARGE");
            Object[] result = new Object[buffer.length];
            if(buffer.length-firstFree>0){
                System.arraycopy(buffer,firstFree,result,0,buffer.length-firstFree);
            }
            if(firstFree>0){
                System.arraycopy(buffer,0,result,buffer.length-firstFree,firstFree);
            }
            return result;
        }
    }

    /*
    public static void main(String[] args) {
        RingObjectBuffer test = new RingObjectBuffer(10,100);
        for(int i=0; i<100; i++){
            test.add(new Integer(i));
        }
        Object[] result = test.toArray();
        for(int i=0; i<result.length; i++){
            System.out.println(result[i]);
        }
    }*/
}
