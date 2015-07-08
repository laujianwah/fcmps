package fcmps.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FCMPS_CLS_ImportOrderFromFOS_Var {
	private static FCMPS_CLS_ImportOrderFromFOS_Var instance=null;
	private List<CLS_RCCP_ERROR> ls_Message=new ArrayList<CLS_RCCP_ERROR>();
	private Map<String,List<String[]>> ls_SH_SIZE=Collections.synchronizedMap(new HashMap<String,List<String[]>>());

	private Map<String,Double> ls_SH_SIZE_MD_PAIR_QTY=Collections.synchronizedMap(new HashMap<String,Double>());
	private Map<String,List<String>> ls_SH_SHARE_PART=Collections.synchronizedMap(new HashMap<String,List<String>>());

	private Map<String,Double> ls_SH_MIN_CAP_QTY=Collections.synchronizedMap(new HashMap<String,Double>());	
	
	private Map<String,List<String>> ls_SH_NEED_PLAN_PROC=Collections.synchronizedMap(new HashMap<String,List<String>>());

	private FCMPS_CLS_ImportOrderFromFOS_Var() {
		
	}
	
	public static FCMPS_CLS_ImportOrderFromFOS_Var getInstance() {
		if(instance==null) instance=new FCMPS_CLS_ImportOrderFromFOS_Var();
		return instance;
	}
	
	public void init() {
		ls_Message.clear();
		ls_SH_SIZE.clear();
		ls_SH_SIZE_MD_PAIR_QTY.clear();
		ls_SH_SHARE_PART.clear();
		ls_SH_MIN_CAP_QTY.clear();
		ls_SH_NEED_PLAN_PROC.clear();
		
		Runtime.getRuntime().gc();		
	}
	
	public List<CLS_RCCP_ERROR> get_Messages() {
		return ls_Message;
	}

	public synchronized void add_Message(CLS_RCCP_ERROR Message) {
		ls_Message.add(Message);
	}

	public Map<String, Double> getLs_SH_MIN_CAP_QTY() {
		return ls_SH_MIN_CAP_QTY;
	}

	public void add_SH_MIN_CAP_QTY(String key,Double value) {
		synchronized(ls_SH_MIN_CAP_QTY) {
			Double org=ls_SH_MIN_CAP_QTY.get(key);
			if(org==null) ls_SH_MIN_CAP_QTY.put(key, value);
		}
	}

	public Map<String, List<String>> get_SH_NEED_PLAN_PROC() {
		return ls_SH_NEED_PLAN_PROC;
	}

	public synchronized void add_SH_NEED_PLAN_PROC(String key,List<String> value) {
		this.ls_SH_NEED_PLAN_PROC.put(key, value);
	}

	public Map<String, List<String>> get_SH_SHARE_PART() {
		return ls_SH_SHARE_PART;
	}

	public void add_SH_SHARE_PART(String key,List<String> value) {
		synchronized(ls_SH_SHARE_PART) {
			List<String> org=ls_SH_SHARE_PART.get(key);
			if(org==null)this.ls_SH_SHARE_PART.put(key, value);
		}
	}

	public Map<String, List<String[]>> get_SH_SIZE() {
		return ls_SH_SIZE;
	}

	public void add_SH_SIZE(String key,List<String[]> value) {
		synchronized(ls_SH_SIZE) {
			List<String[]> org=ls_SH_SIZE.get(key);
			if(org==null) this.ls_SH_SIZE.put(key, value);
		}
	}

	public Map<String, Double> get_SH_SIZE_MD_PAIR_QTY() {
		return ls_SH_SIZE_MD_PAIR_QTY;
	}

	public void add_SH_SIZE_MD_PAIR_QTY(String key,Double value) {
		synchronized(ls_SH_SIZE_MD_PAIR_QTY) {
			Double org=ls_SH_SIZE_MD_PAIR_QTY.get(key);
			if(org==null) this.ls_SH_SIZE_MD_PAIR_QTY.put(key, value);
		}
	}

}
