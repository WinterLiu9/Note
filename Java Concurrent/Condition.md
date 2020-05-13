---
title: Condition
date: {date}
tags: Java
categories: Java Concurrent
---
# Condition接口与示例

```Java
public class BoundedQueue<T> {    
    private Object[] items;    // 添加的下标，删除的下标和数组当前数量    
    private int addIndex, removeIndex, count;    
    private Lock lock = new ReentrantLock();    
    private Condition notEmpty = lock.newCondition();    
    private Condition notFull = lock.newCondition();    
    
    public BoundedQueue(int size) {        
        items = new Object[size];    
    }   
    // 添加一个元素，如果数组满，则添加线程进入等待状态，直到有"空位"    
    public void add(T t) throws InterruptedException {        
        lock.lock();//首先需要获得锁，目的是确保数组修改的可见性和排他性
        try {            
            while (count == items.length)                
                notFull.await();            
            items[addIndex] = t;            
            if (++addIndex == items.length)                
                addIndex = 0;            
            ++count;            
            notEmpty.signal();        
        } finally {            
            lock.unlock();        
            }   
}    
// 由头部删除一个元素，如果数组空，则删除线程进入等待状态，直到有新添加元素    @SuppressWarnings("unchecked")    
public T remove() throws InterruptedException {
    lock.lock();        
    try {            
        while (count == 0)                
            notEmpty.await();            
        Object x = items[removeIndex];            
        if (++removeIndex == items.length)                
            removeIndex = 0;            
        --count;            
        notFull.signal();            
        return (T) x;        
    } finally {            
        lock.unlock();        
        }    
}

}
```
Condition定义了等待/通知两种类型的方法，当前线程调用这些方法时，需要提前获取到Condition对象关联的锁。Condition对象是由Lock对象（调用Lock对象的newCondition()方法）创建出来的，换句话说，Condition是依赖Lock对象的。

Condition的使用方式比较简单，需要注意在调用方法前获取锁。当调用await()方法后，当前线程会释放锁并在此等待，而其他线程调用Condition对象的signal()方法，通知当前线程后，当前线程才从await()方法返回，并且在返回前已经获取了锁。



# Condition的实现分析

1. 等待队列

![](https://github.com/Wayne-98/image/blob/master/Java%20Concurrent/ConditionQueue.png?raw=true)
如果从队列（同步队列和等待队列）的角度看await()方法，当调用await()方法时，相当于同步队列的首节点（获取了锁的节点）移动到Condition的等待队列中。
![](https://github.com/Wayne-98/image/blob/master/Java%20Concurrent/JoinConditionQueue.png?raw=true)
调用Condition的signal()方法，将会唤醒在等待队列中等待时间最长的节点（首节点），在唤醒节点之前，会将节点移到同步队列中。
![](https://github.com/Wayne-98/image/blob/master/Java%20Concurrent/removeFromConditionQueue.png?raw=true)