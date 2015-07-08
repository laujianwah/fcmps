package fcmps.ui;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockMap<K,V> {
	private ReentrantReadWriteLock lock=new ReentrantReadWriteLock();
	private Map<K,V> map=null;
	
	public ReadWriteLockMap(String classname) {
		try {
			Class cls=Class.forName(classname);
			map=(Map)cls.newInstance();
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void put(K key,V value){
		lock.writeLock().lock();
		try {
			if(map==null) return;
			
			map.put(key, value);
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			lock.writeLock().unlock();
		}
	}	
	
	public V get(K key) {
		V value=null;
		lock.readLock().lock();
		try {
			if(map==null) return null;
			
			value=map.get(key);
			
		}catch(Exception ex) {
			ex.printStackTrace();
			
		}finally {
			lock.readLock().unlock();
		}
		return value;
	}
	
	public Set<K> KeySet(){
		Set<K> value=null;
		lock.readLock().lock();
		try {
			value=map.keySet();
		}finally {
			lock.readLock().unlock();
		}
		return value;
	}
	
	public boolean isEmpty() {
		boolean isYes=false;
		lock.readLock().lock();
		try {
			isYes=map.isEmpty();
		}finally {
			lock.readLock().unlock();
		}
		return isYes;
	}
	
}
