package fcmps.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fcmps.domain.FCMPS026;

public class FCMPS_CLS_Calcu_MH_List {
	private String FA_NO=""; //廠別
	private Integer WORK_WEEK=0; //計劃周次
	private double WORK_DAYS=5.0; //周工作天數
	private Connection conn=null;
	private List<FCMPS026> ls_FCMPS026=new ArrayList<FCMPS026>();
	
	public Connection getConnection() {
		return conn;
	}
	public void setConnection(Connection conn) {
		this.conn = conn;
	}
	public String getFA_NO() {
		return FA_NO;
	}
	public void setFA_NO(String fa_no) {
		FA_NO = fa_no;
	}
	public double getWORK_DAYS() {
		return WORK_DAYS;
	}
	public void setWORK_DAYS(double work_days) {
		WORK_DAYS = work_days;
	}
	public Integer getWORK_WEEK() {
		return WORK_WEEK;
	}
	public void setWORK_WEEK(Integer work_week) {
		WORK_WEEK = work_week;
	}
	
	public List<FCMPS026> getLs_FCMPS026() {
		return ls_FCMPS026;
	}
	
	/**
	 * 生成機臺站位列表
	 *
	 */
	public boolean doGenerateList() throws Exception{
		boolean iRet=false;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		strSQL="select fa_no, work_place, mh_no, mh_station_num, mh_gun_num, mh_with_oven " +
			   "from fcmps026 where FA_NO='"+this.getFA_NO()+"'";
				   
		 pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		 rs=pstmtData.executeQuery();
		 
		 if(rs.next()){
			 do {
				 
				 for(int i=1;i<=rs.getInt("mh_gun_num");i++) {
					 for(int n=1;n<=rs.getInt("mh_station_num");n++) {
						 FCMPS026 data=new FCMPS026();
						 data.setFA_NO(getFA_NO());
						 data.setMH_GUN_NUM(i);
						 data.setMH_NO(rs.getString("mh_no"));
						 data.setMH_STATION_NUM(n);
						 data.setMH_WITH_OVEN(rs.getString("mh_with_oven"));
						 data.setWORK_DAYS(0.0);
						 data.setWORK_WEEK(getWORK_WEEK());
						 data.setUSE_DAYS(0.0);
						 getLs_FCMPS026().add(data);
					 }
				 }
			 }while(rs.next());
		 }
		 rs.close();
		 pstmtData.close();
	
		 iRet=true;
		 
		 return iRet;
	}
		
}
