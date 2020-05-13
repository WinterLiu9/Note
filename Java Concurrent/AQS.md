---
title: AQS
date: {date}
tags: Java
categories: Java Concurrent
---
[AQS](https://segmentfault.com/a/1190000017372067)

AbstractQueuedSynchronizer

Sync extends AbstractQueuedSynchronizer

Sync 有两个主要的实现类 NofairSync，FailSync

## NofairSync.lock()

```Java
final void lock() {
	if (compareAndSetState(0, 1)) //通过cas操作来修改state状态，表示争抢锁的操作
		setExclusiveOwnerThread(Thread.currentThread());//设置当前获得锁状态的线程
	else
		acquire(1); //尝试去获取锁
}
```

```Java
public final void acquire(int arg) {
	if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
		selfInterrupt();
}
protected final boolean tryAcquire(int acquires) {
	return nonfairTryAcquire(acquires);
}
```

```Java
final boolean nonfairTryAcquire(int acquires) {
	//获得当前执行的线程
	final Thread current = Thread.currentThread();
	int c = getState(); //获得state的值
	if (c == 0) { //state=0说明当前是无锁状态
		//通过cas操作来替换state的值改为1
		if (compareAndSetState(0, acquires)) {
			//保存当前获得锁的线程
			setExclusiveOwnerThread(current);
			return true;
		}
	}
	//这段逻辑就很简单了。如果是同一个线程来获得锁，则直接增加重入次数
	else if (current == getExclusiveOwnerThread()) {
		int nextc = c + acquires; //增加重入次数
		if (nextc < 0) // overflow
			throw new Error("Maximum lock count exceeded");
		setState(nextc);
		return true;
	}
	return false;
}
private Node enq(final Node node) {
	//自旋
	for (;;) {
		Node t = tail; //如果是第一次添加到队列，那么tail=null
		if (t == null) { // Must initialize
			//CAS的方式创建一个空的Node作为头结点
			if (compareAndSetHead(new Node()))
			//此时队列中只一个头结点，所以tail也指向它
			tail = head;
		} else {
			//进行第二次循环时，tail不为null，进入else区域。将当前线程的Node结点的prev指向tail，然后使用CAS将tail指向Node
			node.prev = t;
			if (compareAndSetTail(t, node)) {
			//t此时指向tail,所以可以CAS成功，将tail重新指向Node。此时t为更新前的tail的值，即指向空的头结点，t.next=node，就将头结点的后续结点指向Node，返回头结点
				t.next = node;
				return t;
			}
		}
	}
}
```

```Java
private Node addWaiter(Node mode) { //mode=Node.EXCLUSIVE
	//将当前线程封装成Node，并且mode为独占锁
	Node node = new Node(Thread.currentThread(), mode);
	// Try the fast path of enq; backup to full enq on failure
	// tail是AQS的中表示同步队列队尾的属性，刚开始为null，所以进行enq(node)方法
	Node pred = tail;
	if (pred != null) { //tail不为空的情况，说明队列中存在节点数据
		node.prev = pred; //讲当前线程的Node的prev节点指向tail
		if (compareAndSetTail(pred, node)) {//通过cas讲node添加到AQS队列
			pred.next = node;//cas成功，把旧的tail的next指针指向新的tail
			return node;
		}
	}
	enq(node); //tail=null，将node添加到同步队列中
	return node;
}
```

```Java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();// 获取prev节点,若为null即刻抛出NullPointException
            if (p == head && tryAcquire(arg)) {// 如果前驱为head才有资格进行锁的抢夺
                setHead(node); // 获取锁成功后就不需要再进行同步操作了,获取锁成功的线程作为新的head节点
//凡是head节点,head.thread与head.prev永远为null, 但是head.next不为null
                p.next = null; // help GC
                failed = false; //获取锁成功
                return interrupted;
            }
//如果获取锁失败，则根据节点的waitStatus决定是否需要挂起线程
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())// 若前面为true,则执行挂起,待下次唤醒的时候检测中断的标志
                interrupted = true;
        }
    } finally {
        if (failed) // 如果抛出异常则取消锁的获取,进行出队(sync queue)操作
            cancelAcquire(node);
    }
}
```

```Java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus; //前继节点的状态
    if (ws == Node.SIGNAL)//如果是SIGNAL状态，意味着当前线程需要被unpark唤醒
               return true;
//如果前节点的状态大于0，即为CANCELLED状态时，则会从前节点开始逐步循环找到一个没有被“CANCELLED”节点设置为当前节点的前节点，返回false。在下次循环执行shouldParkAfterFailedAcquire时，返回true。这个操作实际是把队列中CANCELLED的节点剔除掉。
    if (ws > 0) {// 如果前继节点是“取消”状态，则设置 “当前节点”的 “当前前继节点” 为 “‘原前继节点'的前继节点”。
       
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else { // 如果前继节点为“0”或者“共享锁”状态，则设置前继节点为SIGNAL状态。
        /*
         * waitStatus must be 0 or PROPAGATE.  Indicate that we
         * need a signal, but don't park yet.  Caller will need to
         * retry to make sure it cannot acquire before parking.
         */
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}

private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
}
```

## ReentrantLock.unlock

```Java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```

```Java
protected final boolean tryRelease(int releases) {
    int c = getState() - releases; // 这里是将锁的数量减1
    if (Thread.currentThread() != getExclusiveOwnerThread())// 如果释放的线程和获取锁的线程不是同一个，抛出非法监视器状态异常
        throw new IllegalMonitorStateException();
    boolean free = false;
    if (c == 0) { 
// 由于重入的关系，不是每次释放锁c都等于0，
    // 直到最后一次释放锁时，才会把当前线程释放
        free = true;
        setExclusiveOwnerThread(null);
    }
    setState(c);
    return free;
}
```

```Java
private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);
    Node s = node.next;
    if (s == null || s.waitStatus > 0) {//判断后继节点是否为空或者是否是取消状态,
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0) //然后从队列尾部向前遍历找到最前面的一个waitStatus小于0的节点, 至于为什么从尾部开始向前遍历，因为在doAcquireInterruptibly.cancelAcquire方法的处理过程中只设置了next的变化，没有设置prev的变化，在最后有这样一行代码：node.next = node，如果这时执行了unparkSuccessor方法，并且向后遍历的话，就成了死循环了，所以这时只有prev是稳定的
                s = t;
    }
//内部首先会发生的动作是获取head节点的next节点，如果获取到的节点不为空，则直接通过：“LockSupport.unpark()”方法来释放对应的被挂起的线程，这样一来将会有一个节点唤醒后继续进入循环进一步尝试tryAcquire()方法来获取锁
    if (s != null)
        LockSupport.unpark(s.thread); //释放许可
}
```

