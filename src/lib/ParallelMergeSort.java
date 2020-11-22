package lib;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ParallelMergeSort<T> extends RecursiveTask<T[]> {
    
    private static final int DEFAULT_THRESHOLD = 50;
    
    public static <T> T[] sort(T[] arr, Comparator<? super T> cmp, Class<T> clazz) {
        return sort(arr, 0, arr.length - 1, DEFAULT_THRESHOLD, cmp, clazz);
    }
    
    public static <T> T[] sort(T[] arr, int threshold, Comparator<? super T> cmp, Class<T> clazz) {
        return sort(arr, 0, arr.length - 1, threshold, cmp, clazz);
    }
    
    public static <T> T[] sort(T[] arr, int left, int right, Comparator<? super T> cmp, Class<T> clazz) {
        return sort(arr, left, right, DEFAULT_THRESHOLD, cmp, clazz);
    }
    
    public static <T> T[] sort(T[] arr, int left, int right, int threshold, Comparator<? super T> cmp, Class<T> clazz) {
        Object obj = new ForkJoinPool().invoke(new ParallelMergeSort<>(arr, left, right, threshold, cmp, clazz));
        return (T[]) obj;
    }
    
    private final T[] arr;
    private final int left, right;
    private final int threshold;
    private final Comparator<? super T> cmp;
    private final Class<T> clazz;

    private ParallelMergeSort(T[] arr, int left, int right, int threshold, Comparator<? super T> cmp, Class<T> clazz) {
        this.arr = arr;
        this.left = left;
        this.right = right;
        this.threshold = threshold;
        this.cmp = cmp;
        this.clazz = clazz;
    }

    @Override
    protected T[] compute() {
        int n = right - left + 1;
        if (n <= threshold) {
            insertionSort(arr, left, right, cmp);
            return n == arr.length ? arr : null;
        } else {
            int mid = left + (right - left) / 2;
            ParallelMergeSort<T> l = new ParallelMergeSort<>(arr, left, mid, threshold, cmp, clazz);
            ParallelMergeSort<T> r = new ParallelMergeSort<>(arr, mid + 1, right, threshold, cmp, clazz);
            l.fork();
            T[] rightArr = r.compute();
            T[] leftArr = l.join();
            return merge(arr, leftArr, rightArr, left, mid, right, cmp, clazz);
        }
    }

    private static <T> T[] merge(T[] src, T[] leftArr, T[] rightArr, int leftIdx, int midIdx, int rightIdx, Comparator<? super T> cmp, Class<T> clazz) {
        T[] res = (T[]) Array.newInstance(clazz, rightIdx - leftIdx + 1);
        int k = 0;
        int i = leftArr == null ? leftIdx : 0;
        int j = rightArr == null ? midIdx + 1 : 0;
        int iBound = leftArr == null ? midIdx + 1 : leftArr.length;
        int jBound = rightArr == null ? rightIdx + 1 : rightArr.length;
        T[] left = leftArr == null ? src : leftArr;
        T[] right = rightArr == null ? src : rightArr;
        while (i < iBound && j < jBound) {
            if (cmp.compare(left[i], right[j]) < 0) res[k++] = left[i++];
            else if (cmp.compare(left[i], right[j]) > 0) res[k++] = right[j++];
            else {
                res[k++] = left[i++];
                res[k++] = right[j++];
            }
        }
        if (i < iBound) do res[k++] = left[i++]; while (i < iBound);
        else if (j < jBound) do res[k++] = right[j++]; while (j < jBound);
        return res;
    }
    

    private static <T> void insertionSort(T[] arr, int left, int right, Comparator<? super T> cmp) {
        for (int i = left; i <= right; i++) {
            int j = i - 1;
            T temp = arr[i];
            while (j >= left && cmp.compare(temp, arr[j]) < 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = temp;
        }
    }

}
