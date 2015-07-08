package fcmps.ui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

public class FCMPS_CLS_ForeGenerateRccpPlanExTest extends TestCase {
	String FA_NO="FIC";
	String SH_NO="BISTRO";
	String SH_SIZE="6*";
	
	public void test_DoPrint() {

		Map<String,ArrayList<String[]>> ls_Share_Style=new TreeMap<String,ArrayList<String[]>>();
		ArrayList<String[]> ls_STYLE=null;
		
		Connection conn=getConnection();
    	
		try {
			
			double MD_PAIR_QTY=FCMPS_PUBLIC.getMD_PAIR_QTY(FA_NO, "CLIGHTBFLYCLGPS", "C12", conn,1416);
					
        	System.out.println(MD_PAIR_QTY);
        	
        	
        	System.out.println(WeekUtil.getWeekOfYear(new Date(),true));
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			closeConnection(conn);
		}
	}
	
	private ArrayList<String[]> getMD_Style_Share(
			String FA_NO,
			String STYLE_NO,
			String SH_NO,
			String SH_SIZE,
			Map<String,ArrayList<String[]>> ls_Share_Style,
			int WORK_WEEK,
			Connection conn) {
		ArrayList<String[]> iRet=null;
		
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;		
		
		try{
			
			strSQL="select DISTINCT EFFECTIVE_WEEK " +
			       "from FCMPS0022 A " +
			       "where FA_NO='"+FA_NO+"'" +
			       "  and SH_NO='" +SH_NO+"'"+
			       "  and SH_SIZE='"+SH_SIZE+"' "+
			       "order by EFFECTIVE_WEEK DESC";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		int EFFECTIVE_WEEK=rs.getInt("EFFECTIVE_WEEK");
		    		if(!FCMPS_PUBLIC.ChokePointPart_is_SharePart(FA_NO, SH_NO, SH_SIZE, getConnection(),EFFECTIVE_WEEK)) {
		    			ArrayList<String[]> ls_STYLE=new ArrayList<String[]>();
		    			ls_Share_Style.put(FA_NO+STYLE_NO+SH_NO+SH_SIZE+EFFECTIVE_WEEK,ls_STYLE);
		    			if(EFFECTIVE_WEEK<=WORK_WEEK && iRet==null) iRet=ls_STYLE;
		    					    			
		    		}else {												
		    			ArrayList<String[]> ls_STYLE_B=FCMPS_PUBLIC.getSH_Share_SIZE_Max_MD_CAP(SH_NO,SH_SIZE, FA_NO, getConnection(),EFFECTIVE_WEEK);
		    			ls_Share_Style.put(FA_NO+STYLE_NO+SH_NO+SH_SIZE+EFFECTIVE_WEEK,ls_STYLE_B);
		    			if(EFFECTIVE_WEEK<=WORK_WEEK && iRet==null) iRet=ls_Share_Style.get(FA_NO+STYLE_NO+SH_NO+SH_SIZE+EFFECTIVE_WEEK);
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
	
	
	private SH_KEY_SIZE getMD_Min_Week_Cap_QTY(String FA_NO,String SH_NO,String SH_SIZE,String PROCID,ArrayList<SH_KEY_SIZE> ls_SH_SIZE_CAP,int WORK_WEEK) {
		SH_KEY_SIZE iRet=null;
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			String subSQL="select " +
            "FCMPS003.STYLE_NO2,"+
            "FCMPS003.SH_NO2," +
            "FCMPS003.SH_SIZE2," +
            "FCMPS003.PART_NO2 "+
            "from FCMPS003 "+
			"where SH_NO='"+SH_NO+"' " +
			"  and SH_SIZE='"+SH_SIZE+"' "+
            " UNION ALL "+
            "select "+
            "FCMPS003.STYLE_NO,"+
            "FCMPS003.SH_NO," +
            "FCMPS003.SH_SIZE," +
            "FCMPS003.PART_NO "+
            "from FCMPS003 "+
			"where SH_NO2='"+SH_NO+"' "+
			"  and SH_SIZE2='"+SH_SIZE+"' ";
			
			subSQL="select " +
            "FCMPS003.STYLE_NO2,"+
            "FCMPS003.SH_NO2," +
            "FCMPS003.SH_SIZE2," +
            "FCMPS003.PART_NO2 "+
            "from FCMPS003 "+
            "where (STYLE_NO2, SH_NO2, SH_SIZE2, PART_NO2) in ("+subSQL+") "+
            "   or (STYLE_NO, SH_NO, SH_SIZE, PART_NO) in ("+subSQL+") "+
            " UNION ALL "+
            "select " +
            "FCMPS003.STYLE_NO,"+
            "FCMPS003.SH_NO," +
            "FCMPS003.SH_SIZE," +
            "FCMPS003.PART_NO "+
            "from FCMPS003 "+
            "where (STYLE_NO2, SH_NO2, SH_SIZE2, PART_NO2) in ("+subSQL+") "+
            "   or (STYLE_NO, SH_NO, SH_SIZE, PART_NO) in ("+subSQL+") ";
			
			strSQL="select DISTINCT EFFECTIVE_WEEK " +
				   "from FCMPS0021 " +
				   "where MD_CALC_CAP='Y' and MD_CFM='Y' and FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"' and SH_SIZE='"+SH_SIZE+"' "+
			       " union "+
			       "select DISTINCT EFFECTIVE_WEEK " +
			       "from FCMPS0021 " +
			       "where MD_CALC_CAP='Y' and MD_CFM='Y' and FA_NO='"+FA_NO+"' and (STYLE_NO,SH_NO,SH_SIZE,PART_NO) in ("+subSQL+")";	
						
			strSQL="select EFFECTIVE_WEEK from ("+strSQL+") A "+
			       "order by EFFECTIVE_WEEK DESC";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	
		    	do {
		    		int WORK_WEEK_END=rs.getInt("EFFECTIVE_WEEK");
		    		
		    		SH_KEY_SIZE sh_size_cap=new SH_KEY_SIZE();
		    		sh_size_cap.setFA_NO(FA_NO);
		    		sh_size_cap.setPROCID(PROCID);	    				
		    		sh_size_cap.setSH_SIZE(SH_SIZE);
		    		sh_size_cap.setWORK_WEEK(WORK_WEEK_END);		    		
	    			sh_size_cap.setSH_NO(SH_NO);
	    			sh_size_cap.setWORK_CAP_QTY(FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(FA_NO,SH_NO,SH_SIZE, getConnection(),WORK_WEEK_END));
		    		
		    		ls_SH_SIZE_CAP.add(sh_size_cap);	
					
		    		if(iRet==null && WORK_WEEK_END<=WORK_WEEK) {
		    			iRet=new SH_KEY_SIZE();
		    			iRet.setFA_NO(FA_NO);
		    			iRet.setPROCID(PROCID);	    				
		    			iRet.setSH_SIZE(SH_SIZE);
		    			iRet.setWORK_WEEK(WORK_WEEK_END);		    		
		    			iRet.setSH_NO(SH_NO);
		    			iRet.setWORK_CAP_QTY(sh_size_cap.getWORK_CAP_QTY());
		    		}
		    		
		    	}while(rs.next());		    	
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}
	    
	    return iRet;		
	}
	
	/**
	 * 取得FTI廠別正式數據庫連線
	 * @param COMMPANY_ID
	 * @return
	 */
	private Connection getConnection(){		
		Connection iRet=null;
		try{				

    		Class.forName("oracle.jdbc.driver.OracleDriver"); //加載驅動程序
    		String URL="jdbc:oracle:thin:@10.2.13.5:1521:ficdb02";
    		String USER="dsod";
    		String PSW="dsod";
    		iRet=DriverManager.getConnection(URL,USER,PSW);
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}			

		return iRet;
	}
	
	/**
	 * 結束connection，提供通用的close connection的目的是為了除錯，而且，如果關閉connecton
	 * 的方式目前是用close，但也有可能會有其他的方式來close，所以統一管理會比較容易修改.
	 * @param conn 要結束的connection.
	 */
	private void closeConnection(Connection conn) {
		try {
			if (conn != null && !conn.isClosed())
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
