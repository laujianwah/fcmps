package fcmps.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FCMPS_CLS_ForeGenerateRccpPlan_Var {

	private static FCMPS_CLS_ForeGenerateRccpPlan_Var instance=null;
	
	private List<CLS_RCCP_ERROR> ls_Message=Collections.synchronizedList(new ArrayList<CLS_RCCP_ERROR>());
	private List<PROC_WORK_QTY> ls_PROC_WORK_QTY=Collections.synchronizedList(new ArrayList<PROC_WORK_QTY>());	    	
	private Map<String,List<SH_WORK_QTY>> ls_SH_CAP_QTY=Collections.synchronizedMap(new TreeMap<String,List<SH_WORK_QTY>>());
	
	private Map<String,SH_KEY_SIZE> ls_SH_KEY_SIZE=Collections.synchronizedMap(new TreeMap<String,SH_KEY_SIZE>());
	private Map<String,List<SH_KEY_SIZE>> ls_SH_SIZE_CAP=Collections.synchronizedMap(new TreeMap<String,List<SH_KEY_SIZE>>());
	private Map<String,List<SH_COLOR_SIZE>> ls_SH_COLOR_SIZE=Collections.synchronizedMap(new TreeMap<String,List<SH_COLOR_SIZE>>());
	
	private static Map<String,Integer> ls_SH_COLOR_ALLOW_COUNT=null;
	
	private Map<String,List<String[]>> ls_Share_Style=Collections.synchronizedMap(new TreeMap<String,List<String[]>>());
	
	private Map<String,Boolean> ls_SH_NEED_SHOOT=Collections.synchronizedMap(new TreeMap<String,Boolean>());
    
	private Map<String,List<Double>> ls_SH_PROC_SEQ=Collections.synchronizedMap(new TreeMap<String,List<Double>>());
	private Map<String,List<String[]>> ls_SH_NEED_PLAN_PROC=Collections.synchronizedMap(new TreeMap<String,List<String[]>>());	
	
	
	private FCMPS_CLS_ForeGenerateRccpPlan_Var() {
		
	}
	
	public static FCMPS_CLS_ForeGenerateRccpPlan_Var getInstance(String FA_NO,Connection conn) {
		if(instance==null) {
			instance=new FCMPS_CLS_ForeGenerateRccpPlan_Var();
			ls_SH_COLOR_ALLOW_COUNT=getColor_Allow_Count(FA_NO,conn);
		}
		return instance;
	}
	
	public void init(String FA_NO,Connection conn) {
		ls_Message.clear();
		ls_PROC_WORK_QTY.clear();
		ls_SH_CAP_QTY.clear();
		ls_SH_KEY_SIZE.clear();
		ls_SH_SIZE_CAP.clear();
		ls_SH_COLOR_SIZE.clear();
		ls_Share_Style.clear();
		ls_SH_NEED_SHOOT.clear();
		ls_SH_PROC_SEQ.clear();
		ls_SH_NEED_PLAN_PROC.clear();
		ls_SH_COLOR_ALLOW_COUNT.clear();
		ls_SH_COLOR_ALLOW_COUNT=getColor_Allow_Count(FA_NO,conn);
		Runtime.getRuntime().gc();
	}
	
	public List<CLS_RCCP_ERROR> getLs_Message() {
		return ls_Message;
	}

	public void addLs_Message(CLS_RCCP_ERROR message) {
		synchronized(ls_Message) {
			ls_Message.add(message);
		}
	}

	public synchronized List<PROC_WORK_QTY> getLs_PROC_WORK_QTY() {
		return ls_PROC_WORK_QTY;
	}

	public PROC_WORK_QTY getPROC_WORK_QTY(String FA_NO,String PROCID,int WORK_WEEK) {
		PROC_WORK_QTY iRet=null;
		List<PROC_WORK_QTY> ls_PROC_WORK_QTY=getLs_PROC_WORK_QTY();
		
		synchronized(ls_PROC_WORK_QTY) {
			if(!ls_PROC_WORK_QTY.isEmpty()) {    					
				for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
					PROC_WORK_QTY proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
					if(proc_Work_Qty.getFA_NO().equals(FA_NO)&& 
					   proc_Work_Qty.getPROCID().equals(PROCID)&&
					   proc_Work_Qty.getWORK_WEEK()==WORK_WEEK) {
						iRet=proc_Work_Qty;
						break;
					}
				}    	    			
			}
		}
		
		return iRet;
	}
	
	public void addLs_PROC_WORK_QTY(PROC_WORK_QTY proc_work_qty) {
		ls_PROC_WORK_QTY.add(proc_work_qty);
	}

	public Map<String, List<SH_WORK_QTY>> getLs_SH_CAP_QTY() {
		return ls_SH_CAP_QTY;
	}

	public void putLs_SH_CAP_QTY(String key, List<SH_WORK_QTY> value) {
		ls_SH_CAP_QTY.put(key, value);
	}

	public Map<String, Integer> getLs_SH_COLOR_ALLOW_COUNT() {
		return ls_SH_COLOR_ALLOW_COUNT;
	}

	public Map<String, List<SH_COLOR_SIZE>> getLs_SH_COLOR_SIZE() {
		return ls_SH_COLOR_SIZE;
	}

	public void putLs_SH_COLOR_SIZE(String key,List<SH_COLOR_SIZE> value) {
		ls_SH_COLOR_SIZE.put(key, value);
	}

	public Map<String, SH_KEY_SIZE> getLs_SH_KEY_SIZE() {
		return ls_SH_KEY_SIZE;
	}

	public void putLs_SH_KEY_SIZE(String key,SH_KEY_SIZE value) {
		ls_SH_KEY_SIZE.put(key, value);
	}

	public Map<String, List<String[]>> getLs_SH_NEED_PLAN_PROC() {
		return ls_SH_NEED_PLAN_PROC;
	}

	public void putLs_SH_NEED_PLAN_PROC(String key,List<String[]> value) {
		ls_SH_NEED_PLAN_PROC.put(key, value);
	}

	public Map<String, Boolean> getLs_SH_NEED_SHOOT() {
		return ls_SH_NEED_SHOOT;
	}

	public void putLs_SH_NEED_SHOOT(String key,Boolean value) {
		ls_SH_NEED_SHOOT.put(key, value);
	}

	public Map<String, List<Double>> getLs_SH_PROC_SEQ() {
		return ls_SH_PROC_SEQ;
	}

	public void putLs_SH_PROC_SEQ(String key,List<Double> value) {
		ls_SH_PROC_SEQ.put(key, value);
	}

	public Map<String, List<SH_KEY_SIZE>> getLs_SH_SIZE_CAP() {
		return ls_SH_SIZE_CAP;
	}

	public void putLs_SH_SIZE_CAP(String key,List<SH_KEY_SIZE> value) {
		ls_SH_SIZE_CAP.put(key, value);
	}

	public Map<String, List<String[]>> getLs_Share_Style() {
		return ls_Share_Style;
	}

	public void putLs_Share_Style(String key,List<String[]> value ) {
		ls_Share_Style.put(key, value);
	}

	/**
	 * 取得型體允許排產的配色個數
	 * @param FA_NO
	 * @param STYLE_NO
	 * @param SH_NO
	 * @param SH_SIZE
	 * @param ls_Share_Style
	 * @param WORK_WEEK
	 * @return
	 */
	private static Map<String,Integer> getColor_Allow_Count(String FA_NO,Connection conn) {
		
		Map<String,Integer> iRet=new TreeMap<String,Integer>();
		
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;		
		
		try{
			
			strSQL="select SH_NO,ALLOW_COLOR from fcmps022 where FA_NO='"+FA_NO+"'";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		if(rs.getObject("ALLOW_COLOR")==null) {
		    			iRet.put(rs.getString("SH_NO"), 0);
		    		}else {
		    			iRet.put(rs.getString("SH_NO"), rs.getInt("ALLOW_COLOR"));
		    		}
		    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();	
		    
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
	    return iRet;
	}
}
