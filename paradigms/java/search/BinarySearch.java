package search;

import static java.lang.Integer.parseInt;

public class BinarySearch {
    //Pre: args.length > 0 && \forall i, j >= 1 && < args.length: i < j \Rightarrow args[i] >= args[j]
    // && \forall i >=0 && < args.length String -> int = vremennoe
    public static void main(String[] args) {
        //vremennoe
        int k = parseInt(args[0]);
        // vremennoe && k = args[0] (int)
        int[] arr = new int[args.length - 1];
        // vremennoe && k = args[0] (int) && arr.length = args.length - 1
        // && arr[-1] = +inf && arr[arr.length] = -inf
        for(int i = 1;
            // vremennoe && k = args[0] (int) && arr.length = args.length - 1 && i > 0 && i - 1 >= 0
            i < args.length; i++) {
            // vremennoe && k = args[0] (int) && arr.length = args.length - 1 && i > 0
            // && i < args.length && i - 1 >= 0 && i - 1 < arr.length
            arr[i - 1] = parseInt(args[i]);
            // vremennoe && k = args[0] (int) && arr.length = args.length - 1 && i > 0
            // && i < args.length && i - 1 >= 0 && i - 1 < arr.length && arr[i - 1] = args[i] (int)
        }
        // vremennoe && k = args[0] (int) && arr.length = args.length - 1 && i > 0
        // && i = args.length && i - 1 >= 0 && i - 1 = arr.length
        // && arr[0...arr.length - 1] = args[1...args.length - 1] (int)
//        System.out.println(binaryIt(arr, k));
        System.out.println(binaryRec(arr, -1, arr.length, k));
        //Post: R < arr.length && R > 0 && arr[R] <= k
    }

    //Pre: vremennoe && k = args[0] (int) && arr.length = args.length - 1 && i > 0
    // && i = args.length && i - 1 >= 0 && i - 1 = arr.length
    // && arr[0...arr.length - 1] = args[1...args.length - 1] (int) = invar
    private static int binaryIt(int[] arr, int k) {
        // invar
        int l = -1, r = arr.length, m;
        // invar && arr[l] exist && arr[r] exist && arr[r] <= k && arr[l] > k
        // && r - l >= 1
        while(r - l > 1) {
            // invar && arr[l] exist && arr[r] exist && arr[r] <= k && arr[l] > k
            // && r - l > 1
            m = (r + l) / 2;
            // invar && arr[l] exist && arr[r] exist && arr[r] <= k && arr[l] > k
            // && r - l > 1 && m = (r + l) / 2 && m >= l && m <= r
            if(arr[m] <= k) {
                // invar && arr[l] exist && arr[r] exist && arr[r] <= k && arr[l] > k
                // && r - l > 1 && m = (r + l) / 2 && m >= l && m <= r
                // && arr[m] <= k \Rightarrow arr[r] <= arr[m] <= k && r ! min ind
                r = m;
                // invar && arr[l] exist && arr[r] exist && arr[r] <= k && arr[l] > k
                // && r - l >= 1 && m = (r + l) / 2 && m >= l && m <= r
                // && arr[m] <= k \Rightarrow arr[r] <= arr[m] <= k && r' = m && arr[r'] <= k
                // && r' - l < r - l
            } else {
                // invar && arr[l] exist && arr[r] exist && arr[r] <= k && arr[l] > k
                // && r - l > 1 && m = (r + l) / 2 && m >= l && m <= r
                // && arr[m] > k \Rightarrow arr[l] >= arr[m] > k && l ! max ind
                l = m;
                // invar && arr[l] exist && arr[r] exist && arr[r] <= k && arr[l] > k
                // && r - l > 1 && m = (r + l) / 2 && m >= l && m <= r
                // && arr[m] > k \Rightarrow arr[l] >= arr[m] > k && l' = m && arr[l'] > k
                // && r - l' < r - l
            }
            // invar && arr[l] exist && arr[r] exist && arr[r] <= k && arr[l] > k
            // && r - l > 1 && m = (r + l) / 2 && m >= l && m <= r && r' - l' < r - l
        }
        // invar && arr[l] exist && arr[r] exist && arr[r] <= k && arr[l] > k
        // && r - l <= 1 \Rightarrow arr[r] <= k && arr[r - 1] > k \Rightarrow r - min
        return r;
        //Post: arr[r] <= k && r min (0...arr.size): arr[r] <= k
    }

    //Pre: + l exist && r exist && l < r && arr[l] > k && arr[r] <= k
    private static int binaryRec(int[] arr, int l, int r, int k) {
        // l exist && r exist && l < r && arr[l] > k && arr[r] <= k
        if(r - l <= 1) {
            // l exist && r exist && l < r && arr[l] > k && arr[r] <= k
            // && r - l <= 1 \Rightarrow arr[r] <= k && arr[r + 1] > k
            // \Rightarrow r min (0...arr.length): arr[r] <= k
            return r;
            //Post: arr[r] <= k && r min (0...arr.size): arr[r] <= k
        }
        // l exist && r exist && l < r && arr[l] > k && arr[r] <= k
        // && r - l > 1
        int m = (l + r) / 2;
        // l exist && r exist && l < r && arr[l] > k && arr[r] <= k
        // && r - l > 1 && l <= m && m <= r
        if(arr[m] <= k) {
            // l exist && r exist && l < r && arr[l] > k && arr[r] <= k
            // && r - l > 1 && l <= m && m <= r && arr[m] <= k
            // \Rightarrow arr[r] <= arr[m] <= k \Rightarrow r !min
            return binaryRec(arr, l, m, k);
            //Post: m - l < r - l && arr[m] <= k
            // l exist && m exist && l < m && arr[l] > k && arr[m] <= k
            // && l <= m
        } else {
            // l exist && r exist && l < r && arr[l] > k && arr[r] <= k
            // && r - l > 1 && l <= m && m <= r && arr[m] > k
            // \Rightarrow arr[l] >= arr[m] > k \Rightarrow l !max
            return binaryRec(arr, m, r, k);
            //Post: r - m < r - l && arr[m] > k
            // m exist && r exist && m < r && arr[m] > k && arr[r] <= k
            // && m <= r
        }
    }
}
