package wspp;

import static java.util.Arrays.copyOf;

public class IntList {
    private static final int DEF_SIZE = 100; 
    private int[] arr;
    private int[] supArr;
    private int size;
    
    public IntList() {
        arr = new int[DEF_SIZE];
        supArr = new int[DEF_SIZE];
        size = 0;
    }
    
    
    
    public void increase() {
        arr = copyOf(arr, arr.length * 3 / 2);
        supArr = copyOf(supArr, supArr.length * 3 / 2);
    }
    
    public void add(int x) {
        if(size >= arr.length){
            increase();
        }
        arr[size] = x;
        size++;
    }

    public void add(int x, int y) {
        if(size >= arr.length){
            increase();
        }
        arr[size] = x;
        supArr[size] = y;
        size++;
    }
    
    public int getSize(){
        return size;
    }
    
    public int get(int index) {
        if(arr.length > index) {
            return arr[index];
        } else {
            return 0;
        }
        // throws indexoutofbounds
    }

    public int getSup(int index) {
        if(arr.length > index) {
            return supArr[index];
        } else {
            return 0;
        }
    }
   
}
