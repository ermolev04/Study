package search;

import static java.lang.Integer.parseInt;

public class BinarySearchClosestD {
    //Pre: args.length > 0 && \forall i, j >= 1 && < args.length: i < j \Rightarrow args[i] >= args[j]
    // && \forall i >=0 && < args.length String -> int = vremennoe
    public static void main(String[] args) {
        //vremennoe
        int sum = 0;
        //vremennoe && sum = 0
        int k = parseInt(args[0]);
        // vremennoe && k = args[0] (int)
        sum += k;
        // vremennoe && k = args[0] (int) && sum = args[0] (int)
        int[] arr = new int[args.length - 1];
        // vremennoe && k = args[0] (int) && sum = args[0] (int) && arr.length = args.length - 1
        // && arr[-1] = +inf && arr[arr.length] = -inf
        for(int i = 1;
            // vremennoe && k = args[0] (int) && sum = args[0] (int) && arr.length = args.length - 1 && i > 0 && i - 1 >= 0
            i < args.length; i++) {
            // vremennoe && k = args[0] (int) && sum = sum (0...i-1) args[] && (int)
            // && arr.length = args.length - 1 && i > 0
            // && i < args.length && i - 1 >= 0 && i - 1 < arr.length
            arr[i - 1] = parseInt(args[i]);
            // vremennoe && k = args[0] (int) && sum = sum (0...i-1) args[]
            // && arr.length = args.length - 1 && i > 0
            // && i < args.length && i - 1 >= 0 && i - 1 < arr.length && arr[i - 1] = args[i] (int)
            sum += arr[i - 1];
            // vremennoe && k = args[0] (int) && sum = sum (0...i) args[]
            // && arr.length = args.length - 1 && i > 0
            // && i < args.length && i - 1 >= 0 && i - 1 < arr.length && arr[i - 1] = args[i] (int)
        }
        // k = args[0] (int) && sum = sum (0...args.length) args[]
        // && arr.length = args.length - 1
        // && i = args.length
        // && arr[0...arr.length - 1] = args[1...args.length - 1] (int)
        if(sum % 2 == 0) {
            // k = args[0] (int) && sum = sum (0...args.length) args[] && sum % 2 == 0
            // && arr[0...arr.length - 1] = args[1...args.length - 1] (int)
            System.out.println(arr[binaryRec(arr, -1, arr.length - 1, k)]);
            //Post: R < arr.length && R > 0 && |arr[R] - k| min
        } else {
            // k = args[0] (int) && sum = sum (0...args.length) args[] && sum % 2 == 1
            // && arr[0...arr.length - 1] = args[1...args.length - 1] (int)
            System.out.println(arr[binaryIt(arr, k)]);
            //Post: R < arr.length && R > 0 && |arr[R] - k| min
        }
        //Post: R < arr.length && R > 0 && |arr[R] - k| min
    }

    //Pre: arr.length >= 0 && arr[-1] = +inf && arr[arr.length] = -inf
    // && exist -1 <= i <= arr.length: |arr[i] - k| min = invar
    private static int binaryIt(int[] arr, int k) {
        // invar
        int l = -1, r = arr.length - 1, m;
        // invar && arr[l] exist && arr[r] exist && arr[l] >= k
        // && r - l >= 1
        while(r - l > 1) {
            // invar && arr[l] exist && arr[r] exist && arr[l] >= k
            // && r - l > 1
            m = (r + l) / 2;
            // invar && arr[l] exist && arr[r] exist && arr[l] >= k
            // && r - l > 1 && m = (r + l) / 2 && m >= l && m <= r
            if(arr[m] <= k) {
                // invar && arr[l] exist && arr[r] exist && arr[l] >= k
                // && r - l > 1 && m = (r + l) / 2 && m >= l && m <= r
                // && arr[m] \Rightarrow arr[r] <= arr[m] && r ! min ind
                r = m;
                // invar && arr[l] exist && arr[r] exist && arr[l] >= k
                // && r - l >= 1 && m = (r + l) / 2 && m >= l && m <= r
                // && arr[m] \Rightarrow arr[r] <= arr[m] && r' = m
                // && r' - l < r - l
            } else {
                // invar && arr[l] exist && arr[r] exist && arr[l] > k
                // && r - l > 1 && m = (r + l) / 2 && m >= l && m <= r
                // && arr[m] > k \Rightarrow arr[l] >= arr[m] > k && l ! max ind
                l = m;
                // invar && arr[l] exist && arr[r] exist && arr[l] >= k
                // && r - l > 1 && m = (r + l) / 2 && m >= l && m <= r
                // && arr[m] > k \Rightarrow arr[l] >= arr[m] > k && l' = m && arr[l'] > k
                // && r - l' < r - l
            }
            // invar && arr[l] exist && arr[r] exist && arr[l] > k
            // && r - l > 1 && r' - l' < r - l
        }
        // invar && arr[l] exist && arr[r] exist && arr[l] >= k
        // && r - l <= 1  \Rightarrow r <= l + 1
        if(l >= 0 && Math.abs(k - arr[l]) <= Math.abs(k - arr[r]) && k >= arr[r]) {
            // invar && arr[l] exist && arr[r] exist && arr[l] >= k
            // && r - l <= 1 \Rightarrow r <= l + 1
            // l >= 0 && Math.abs(k - arr[l]) <= Math.abs(k - arr[r]) && k >= arr[r]
            // && l max: arr[l] >= k && |arr[l] - k| <= |arr[r] - k| \Rightarrow |arr[l] - k| min
            return l;
            //Post: |arr[l] - k| min && l min (0...arr.size): |arr[l] - k| min
        } else {
            // invar && arr[l] exist && arr[r] exist && arr[l] >= k
            // && r - l <= 1 \Rightarrow r <= l + 1
            // {
            // (l < 0 || Math.abs(k - arr[l]) > Math.abs(k - arr[r]) || k < arr[r])
            // = Math.abs(k - arr[l]) > Math.abs(k - arr[r])
            // }
            // && |arr[r] - k| <= |arr[l] - k|
            return r;
            //Post: |arr[r] - k| min && r min (0...arr.size): |arr[r] - k| min
        }

        //Post: |arr[R] - k| min && R min (0...arr.size): |arr[R] - k| min
    }

    //Pre: arr.length >= 0 && arr[-1] = +inf && arr[arr.length] = -inf
    // && exist -1 <= i <= arr.length: |arr[i] - k| min
    // && exist l && exist r && l <= r && arr[l] >= k = invar

    private static int binaryRec(int[] arr, int l, int r, int k) {
        // invar
        if(r - l <= 1) {
            // invar && r - l <= 1
            if(l >= 0 && Math.abs(k - arr[l]) <= Math.abs(k - arr[r]) && k >= arr[r]) {
                // invar && r - l <= 1 \Rightarrow r <= l + 1
                // l >= 0 && Math.abs(k - arr[l]) <= Math.abs(k - arr[r]) && k >= arr[r]
                // && l max: arr[l] >= k && |arr[l] - k| <= |arr[r] - k|
                return l;
                //Post: |arr[l] - k| min && l min (0...arr.size): |arr[l] - k| min
            } else {
                // invar && r - l <= 1 \Rightarrow r <= l + 1
                // {
                // (l < 0 || Math.abs(k - arr[l]) > Math.abs(k - arr[r]) || k < arr[r])
                // = Math.abs(k - arr[l]) > Math.abs(k - arr[r])
                // }
                // && |arr[r] - k| <= |arr[l] - k|
                return r;
                //Post: |arr[r] - k| min && r min (0...arr.size): |arr[r] - k| min
            }
            //Post: |arr[R] - k| min && R min (0...arr.size): |arr[R] - k| min
        }
        // invar && r - l > 1
        int m = (l + r) / 2;
        // invar && r - l > 1 && l <= m && m <= r
        if(arr[m] <= k) {
            // invar
            // && r - l > 1 && m = (r + l) / 2 && m >= l && m <= r
            // && arr[m] <= k \Rightarrow arr[r] <= arr[m] <= k && r ! min ind
            return binaryRec(arr, l, m, k);
            //Post: |arr[R] - k| min && R min (0...arr.size): |arr[R] - k| min
        } else {
            // invar
            // && r - l > 1 && m = (r + l) / 2 && m >= l && m <= r
            // && arr[m] > k \Rightarrow arr[l] >= arr[m] > k && l ! max ind
            return binaryRec(arr, m, r, k);
            //Post: |arr[R] - k| min && R min (0...arr.size): |arr[R] - k| min
        }
    }
}
