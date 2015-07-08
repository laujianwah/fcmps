package fcmps.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FCMPS_RCCP_INFO {
	
	private List<PROC_WORK_QTY> ls_PROC_WORK_QTY=Collections.synchronizedList(new ArrayList<PROC_WORK_QTY>());
	private List<SH_WORK_QTY> ls_SH_WORK_QTY=Collections.synchronizedList(new ArrayList<SH_WORK_QTY>());
	private List<SH_KEY_SIZE> ls_SH_KEY_SIZE=Collections.synchronizedList(new ArrayList<SH_KEY_SIZE>());
	private List<SH_COLOR_SIZE> ls_SH_COLOR_SIZE=Collections.synchronizedList(new ArrayList<SH_COLOR_SIZE>());
	
	private Map<String,List<String[]>> ls_Share_Style_Size=Collections.synchronizedMap(new TreeMap<String,List<String[]>>());
	
	private Map<String,Boolean> ls_SH_NEED_SHOOT=Collections.synchronizedMap(new TreeMap<String,Boolean>());
	    	
	private Map<String,Boolean> ls_SH_USE_CAP=Collections.synchronizedMap(new TreeMap<String,Boolean>());
	
	private Map<String,List<String[]>> ls_SH_NEED_PLAN_PROC=Collections.synchronizedMap(new TreeMap<String,List<String[]>>());
	
	private Map<String,List<SH_COLOR_SIZE>> ls_SH_COLOR_QTY=Collections.synchronizedMap(new TreeMap<String,List<SH_COLOR_SIZE>>());
	
	private Map<String,Integer> ls_SH_COLOR_ALLOW_COUNT=null;
	
	private List<CLS_RCCP_ERROR> ls_Message=Collections.synchronizedList(new ArrayList<CLS_RCCP_ERROR>());
	
	private List<String> ls_Finshed_SH=Collections.synchronizedList(new ArrayList<String>());
	
	private List<String[]> ls_Not_Plan_PO_Size=Collections.synchronizedList(new ArrayList<String[]>());	
		
	private Connection conn=null;
	private String FA_NO=""; //廠別
	
	public FCMPS_RCCP_INFO(String FA_NO,Connection conn) {
		this.FA_NO=FA_NO;
		this.conn=conn;
	}
	
	public FCMPS_RCCP_INFO() {

	}
	
	/**
	 * 取廠別
	 * @return
	 */
	public String getFA_NO() {
		return FA_NO;
	}
		
	public void setFA_NO(String fa_no) {
		FA_NO = fa_no;
	}

	public Connection getConnection() {
		return conn;
	}

	public void setConnection(Connection conn) {
		this.conn=conn;
	}
	
	public List<String> getLs_Finshed_SH() {
		return ls_Finshed_SH;
	}

	public List<CLS_RCCP_ERROR> getLs_Message() {
		return ls_Message;
	}

	public List<String[]> getLs_Not_Plan_PO_Size() {
		return ls_Not_Plan_PO_Size;
	}

	public List<PROC_WORK_QTY> getLs_PROC_WORK_QTY() {
		return ls_PROC_WORK_QTY;
	}

	public Map<String, Integer> getLs_SH_COLOR_ALLOW_COUNT() {
		if(ls_SH_COLOR_ALLOW_COUNT==null) {
			ls_SH_COLOR_ALLOW_COUNT=getColor_Allow_Count(getFA_NO(), getConnection());
		}
		return ls_SH_COLOR_ALLOW_COUNT;
	}

	public Map<String, List<SH_COLOR_SIZE>> getLs_SH_COLOR_QTY() {
		return ls_SH_COLOR_QTY;
	}

	public List<SH_COLOR_SIZE> getLs_SH_COLOR_SIZE() {
		return ls_SH_COLOR_SIZE;
	}

	public List<SH_KEY_SIZE> getLs_SH_KEY_SIZE() {
		return ls_SH_KEY_SIZE;
	}

	public Map<String, List<String[]>> getLs_SH_NEED_PLAN_PROC() {
		return ls_SH_NEED_PLAN_PROC;
	}

	public Map<String, Boolean> getLs_SH_NEED_SHOOT() {
		return ls_SH_NEED_SHOOT;
	}

	public Map<String, Boolean> getLs_SH_USE_CAP() {
		return ls_SH_USE_CAP;
	}

	public List<SH_WORK_QTY> getLs_SH_WORK_QTY() {
		return ls_SH_WORK_QTY;
	}

	public Map<String, List<String[]>> getLs_Share_Style_Size() {
		return ls_Share_Style_Size;
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
	private Map<String,Integer> getColor_Allow_Count(String FA_NO,Connection conn) {
		
		Map<String,Integer> iRet=Collections.synchronizedMap(new TreeMap<String,Integer>());
		
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;		
		
		try{
			
			strSQL="select SH_NO,ALLOW_COLOR from fcmps022 where FA_NO='"+FA_NO+"'";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		iRet.put(rs.getString("SH_NO"), rs.getInt("ALLOW_COLOR"));
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
