---
title: 剑指Offer复习
date: {date}
tags: Algorithms
categories: Algorithms
---

## 栈

1. [用两个栈实现队列](https://leetcode-cn.com/problems/yong-liang-ge-zhan-shi-xian-dui-lie-lcof/)

```Java
class CQueue {
    Deque<Integer> stack1;
    Deque<Integer> stack2;
    public CQueue() {
        stack1 = new ArrayDeque<>();
        stack2 = new ArrayDeque<>();
    }
    
    public void appendTail(int value) {
        //if(stack1.size() > 0) {
        //    while(stack1.size() > 0)
        //        stack2.push(stack1.pop());
        //}
        // 不需要每次判断 s1 是否有元素，只让 s1 负责进栈就好
        stack1.push(value);
    }
    
    public int deleteHead() {
        if(stack2.size() == 0) {
            if(stack1.size() == 0) return -1;
            while(stack1.size() > 0)
                stack2.push(stack1.pop());
        }
        return stack2.pop();
    }
}
```

2. [包含min函数的栈](https://leetcode-cn.com/problems/bao-han-minhan-shu-de-zhan-lcof/)

```Java
class MinStack {
    Deque<Integer> stack;
    Deque<Integer> minStack;
    
    public MinStack() {
        stack = new ArrayDeque<>();
        minStack = new ArrayDeque<>();
    }
    
    public void push(int x) {
        stack.push(x);
        if(minStack.isEmpty() || x <= minStack.peek())
            minStack.push(x);
    }
    
    public void pop() {
        int t = stack.pop();
        if(t == minStack.peek()) minStack.pop();
    }
    
    public int top() {
        return stack.peek();
    }
    
    public int min() {
        return minStack.peek();
    }
}
```

3. [滑动窗口的最大值](https://leetcode-cn.com/problems/hua-dong-chuang-kou-de-zui-da-zhi-lcof/)

* 用一个单调递减的双端队列（单调递减是数组的值递减，队列里记录的是数组的下标）
* 首先判断队列头的下标是否过期
* 然后判断窗口是否形成

```Java
class Solution {
    public int[] maxSlidingWindow(int[] nums, int k) {
        if(nums.length == 0) return new int[0];
        int[] res = new int[nums.length - k + 1];
        int index = 0;
        LinkedList<Integer> q = new LinkedList<>();
        for(int i = 0; i < nums.length; i++) {
            while(q.size() > 0 && nums[q.peekLast()] <= nums[i]) q.pollLast();
            q.offerLast(i);
            if(q.peekFirst() == (i - k)) q.pollFirst();
            if((i - k + 1) >= 0) res[index++] = nums[q.peekFirst()];
        }
        return res;
    }
}
```

4. [队列的最大值](https://leetcode-cn.com/problems/dui-lie-de-zui-da-zhi-lcof/)

```Java
class MaxQueue {

    LinkedList<Integer> list;
    LinkedList<Integer> maxList;
    public MaxQueue() {
        list = new LinkedList<>();
        maxList = new LinkedList<>();
    }
    
    public int max_value() {
        if(maxList.size() == 0) return -1;
        return maxList.peekFirst();
    }
    
    public void push_back(int value) {
        list.offerLast(value);
        while(maxList.size() > 0 && value > maxList.peekLast())
            maxList.pollLast();
        maxList.offerLast(value);
    }
    
    public int pop_front() {
        if(list.size() == 0) return -1;
        int t = list.pollFirst();
        if(t == maxList.peekFirst()) maxList.pollFirst();
        return t;
    }
}
```

## 堆

1. [最小的k个数](https://leetcode-cn.com/problems/zui-xiao-de-kge-shu-lcof/)

```Java
class Solution {
    public int[] getLeastNumbers(int[] arr, int k) {
        if(arr.length == 0 || k == 0 ||  k > arr.length) return new int[0];
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>((o1, o2) -> o2 - o1);
        for(int i = 0; i < k; i++) {
            maxHeap.offer(arr[i]);
        }
        for(int i = k; i < arr.length; i++) {
            if(arr[i] < maxHeap.peek()) {
                maxHeap.poll();
                maxHeap.offer(arr[i]);
            }
        }
        int[] res = new int[k];
        for(int i = 0; i < k; i++) {
            res[i] = maxHeap.poll();
        }
        return res;
    }
}
```

2. [数据流中的中位数](https://leetcode-cn.com/problems/shu-ju-liu-zhong-de-zhong-wei-shu-lcof/)

```Java
class MedianFinder {
     // 最小堆保存大的值，最大堆保存小的值
     // 那么每次取中位数，偶数情况下就是两个堆的堆顶值的和 / 2 ;
    PriorityQueue<Integer> minHeap = new PriorityQueue<>();
    PriorityQueue<Integer> maxHeap = new PriorityQueue<>((o1, o2) -> o2 - o1);
    int count = 0;
    /** initialize your data structure here. */
    public MedianFinder() {

    }
    
    public void addNum(int num) {
        count++;
        if((count & 1) == 1) { // 奇数放在最小堆中 
            // 放入最小堆前，要检查当前 num 的值是否小于最大堆的堆顶
            if(maxHeap.size() > 0 && num < maxHeap.peek()) {
                maxHeap.offer(num);
                num = maxHeap.poll();
            }
            minHeap.offer(num);
        } else {
            if(minHeap.size() > 0 && num > minHeap.peek()) {
                minHeap.offer(num);
                num = minHeap.poll();
            }
            maxHeap.offer(num);
        }
    }
    
    public double findMedian() {
        if((count & 1) == 1) return (double)minHeap.peek();
        else return (minHeap.peek() + maxHeap.peek()) / 2.0;
    }
}
```

# 排序

1. [把数组排成最小的数](https://leetcode-cn.com/problems/ba-shu-zu-pai-cheng-zui-xiao-de-shu-lcof/)

```Java
class Solution {
    public String minNumber(int[] nums) {
        if(nums.length == 0) return "";
        String[] strs = new String[nums.length];
        for(int i = 0; i < nums.length; i++) {
            strs[i] = String.valueOf(nums[i]);
        }
        Arrays.sort(strs, (o1, o2) -> (o1+o2).compareTo(o2 + o1));
        return String.join("", strs);
    }
}
```

* 手写快排实现

```Java
class Solution {
    public String minNumber(int[] nums) {
        if(nums.length == 0) return "";
        String[] strs = new String[nums.length];
        for(int i = 0; i < nums.length; i++) {
            strs[i] = String.valueOf(nums[i]);
        }
        sort(strs);
        return String.join("", strs);
    }
    private void sort(String[] strs) {
        sort(strs, 0, strs.length - 1);
    }
    private void sort(String[] strs, int low, int high) {
        if(low >= high) return;// 大于等于
        int pivot = partition(strs, low, high);
        sort(strs, low, pivot - 1);
        sort(strs, pivot + 1, high);
    }

    int partition(String[] strs, int low, int high) {
        int L = low, R = high;
        String pivot = strs[L];
        while(L < R) {
            while(L < R && (strs[R] + pivot).compareTo(pivot + strs[R]) >= 0) R--;
            strs[L] = strs[R];
            while(L < R && (strs[L] + pivot).compareTo(pivot + strs[L]) <= 0) L++;
            strs[R] = strs[L];
        }
        strs[L] = pivot;
        return L;
    }
}
```

## 位运算

1. [二进制中1的个数](https://leetcode-cn.com/problems/er-jin-zhi-zhong-1de-ge-shu-lcof/)

```Java
public class Solution {
    // you need to treat n as an unsigned value
    public int hammingWeight(int n) {
        int res = 0;
        while(n != 0) {
            if((n & 1) != 0) res++;
            n = (n >>> 1);// 注意此处需要用 >>> 而不是 >> ，无符号右移，前面全部补 0
        }
        return res;
    }
}
```

* **(n−1) 解析：** 二进制数字 *n* 最右边的 1 变成 0 ，此 1 右边的 0 都变成 1 。
* **n&(n−1) 解析：** 二进制数字 *n* 最右边的 1 变成 0 ，其余不变。

```Java
public class Solution {
    // you need to treat n as an unsigned value
    public int hammingWeight(int n) {
        int res = 0;
        while(n != 0) {
            n = (n & (n - 1));
            res++;
        }
        return res;
    }
}
```

2. [数组中出现次数超过一半的数字](https://leetcode-cn.com/problems/shu-zu-zhong-chu-xian-ci-shu-chao-guo-yi-ban-de-shu-zi-lcof/)

* 迷惑，为什么 LeetCode 分类到位运算了

```Java
class Solution {
    public int majorityElement(int[] nums) {
        int res = nums[0];
        int count = 1;
        for(int i = 1; i < nums.length; i++) {
            if(nums[i] == res) count++;
            else {
                if(--count == 0) {
                    count = 1;
                    res = nums[i];
                }
            }
        }
        return res;
    }
}
```

