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

