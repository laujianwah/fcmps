package fcmps.ui;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockList<E> {
	private ReentrantReadWriteLock lock=new ReentrantReadWriteLock();
	private List<E> list=null;
	
	public ReadWriteLockList(String classname) {
		try {
			Class cls=Class.forName(classname);
			list=(List)cls.newInstance();
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public boolean isEmpty() {
		boolean iRet=false;
		lock.readLock().lock();
		try {
			iRet=list.isEmpty();
		}finally {
			lock.readLock().unlock();
		}
		return iRet;
	}
	
	public boolean add(E o) {
		boolean iRet=false;
		lock.writeLock().lock();
		try {
			iRet=list.add(o);
		}finally {
			lock.writeLock().unlock();
		}
		return iRet;
	}
	
	public boolean remove(Object o) {
		boolean iRet=false;
		lock.writeLock().lock();
		try {
			iRet=list.remove(o);
		}finally {
			lock.writeLock().unlock();
		}
		return iRet;
	}
	
	public void clear() {
		lock.writeLock().lock();
		try {
			list.clear();
		}finally {
			lock.writeLock().unlock();
		}
	}
	
	public E get(int index) {
		E iRet=null;
		lock.readLock().lock();
		try {
			iRet=list.get(index);
		}finally {
			lock.readLock().unlock();
		}
		return iRet;
	}
	
	public int size() {
		int iRet=0;
		lock.readLock().lock();
		try {
			iRet=list.size();
		}finally {
			lock.readLock().unlock();
		}
		return iRet;
	}
	
	public Iterator<E> iterator(){		
		Iterator<E> iRet=null;
		lock.readLock().lock();
		try {
			iRet=list.iterator();
		}finally {
			lock.readLock().unlock();
		}
		return iRet;
	}

	public List<E> getList() {
		return list;
	}	
	
}
