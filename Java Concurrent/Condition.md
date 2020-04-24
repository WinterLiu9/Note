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
![1afc68a60bbc4c8f64d08cdd0aeadd93.png](en-resource://database/1365:1)
如果从队列（同步队列和等待队列）的角度看await()方法，当调用await()方法时，相当于同步队列的首节点（获取了锁的节点）移动到Condition的等待队列中。
![52e5183d573932ad91b24c0f2e88d5ea.png](en-resource://database/1367:1)
调用Condition的signal()方法，将会唤醒在等待队列中等待时间最长的节点（首节点），在唤醒节点之前，会将节点移到同步队列中。
![e43c137a7cb74f2f1114dd64ac4794b9.png](en-resource://database/1369:1)