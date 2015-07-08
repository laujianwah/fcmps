package fcmps.test;

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;
import fcmps.ui.ReadWriteLockMap;

public class ReadWriteLockMapTest extends TestCase {
	ReadWriteLockMap<String,Double> map=new ReadWriteLockMap<String,Double>("java.util.TreeMap");
	public void testReadWriteLockMap() {
		map.put("b", 2.0);
		map.put("a", 1.0);
		map.put("d", 4.0);
		map.put("c", 3.0);
//		System.out.println(map.get("a"));
		
		Set<String> keys=map.KeySet();
		Iterator<String> it=keys.iterator();
		while(it.hasNext()) {
			String key=it.next();
			System.out.println(map.get(key));
		}
	}

}
