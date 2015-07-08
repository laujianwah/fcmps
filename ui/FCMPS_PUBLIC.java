package fcmps.ui;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.hibernate.validator.Length;
import org.springframework.beans.BeanUtils;

import dsc.echo2app.program.Config;

public class FCMPS_PUBLIC {
	/**
	 * 射出制程代號
	 */
	public final static String PROCID_SHOOT="100";
	/**
	 * 針車制程代號
	 */
	public final static String PROCID_STITCHING="200";
	/**
	 * 組底制程代號
	 */
	public final static String PROCID_ASSEMBLE="300";
	
	/**
	 * 射出每個型體每個配色的最小生產量
	 */
//	public final static double SHOOT_MIN_PRODUCE_QTY=516;
	
	/**
	 * 系統開始排產周次
	 */
	public final static int SYS_BEGIN_PLAN_WEEK=1507;
	
	/**
	 * 每周一臺機最多可以換多少個型體配色
	 */
	public final static int WEEK_MH_MAX_PRODUCE_COLOR_COUNT=3;
	
	/**
	 * 每周一臺機最多可以換多少個型體部位
	 */
	public final static int WEEK_MH_MAX_PRODUCE_PART_COUNT=2;
	
	/**
	 * 往後指定周數是哪周<BR>
	 * @param WeekOfYear 當前周次, 格式如: 1322
	 * @param weeks 周數
	 * @return
	 */
	public static int getNext_Week(int WeekOfYear,int weeks) {
		int iRet=-1;
		int year=0;
		int week=0;
		
		if(String.valueOf(WeekOfYear).length()<3) return iRet;
		
		if(String.valueOf(WeekOfYear).length()==3) {
			year=Integer.valueOf(String.valueOf(WeekOfYear).substring(0,1))+2000;
			week=Integer.valueOf(String.valueOf(WeekOfYear).substring(1));
		}
		   		
		if(String.valueOf(WeekOfYear).length()==4) {
			year=Integer.valueOf(String.valueOf(WeekOfYear).substring(0,2))+2000;
			week=Integer.valueOf(String.valueOf(WeekOfYear).substring(2));
		}
		
		for(int i=1;i<=weeks;i++) {
			if(WeekUtil.getMaxWeekNumOfYear(year)==week) {
				year=year+1;
				week=1;
			}else {
				week=week+1;				
			}
		}

		return Integer.valueOf(String.valueOf(year).substring(2)+WeekUtil.Pad(String.valueOf(Integer.valueOf(week)),"0",2,0));

	}
	
	/**
	 * 往前指定周數是哪周<BR>
	 * @param WeekOfYear  當前周次, 格式如: 1322
	 * @param weeks 周數
	 * @return
	 */
	public static int getPrevious_Week(int WeekOfYear,int weeks) {
		int iRet=-1;
		int year=0;
		int week=0;
		
		if(String.valueOf(WeekOfYear).length()<3) return iRet;
		
		if(String.valueOf(WeekOfYear).length()==3) {
			year=Integer.valueOf(String.valueOf(WeekOfYear).substring(0,1))+2000;
			week=Integer.valueOf(String.valueOf(WeekOfYear).substring(1));
		}
		   		
		if(String.valueOf(WeekOfYear).length()==4) {
			year=Integer.valueOf(String.valueOf(WeekOfYear).substring(0,2))+2000;
			week=Integer.valueOf(String.valueOf(WeekOfYear).substring(2));
		}
		
		for(int i=1;i<=weeks;i++) {
			if(1==week) {
				year=year-1;
				week=WeekUtil.getMaxWeekNumOfYear(year);
			}else {
				week=week-1;				
			}
		}

		return Integer.valueOf(String.valueOf(year).substring(2)+WeekUtil.Pad(String.valueOf(Integer.valueOf(week)),"0",2,0));

	}
	
    /**
     * 取得型體模具最小周產能<br>
     * 最小產能==型體size各部位中模具產能最小的<br>
     * 需要考慮型體共模<br>
     * 默認每周工作5天
     * @param FA_NO   廠別
     * @param SH_NO   型體
     * @param conn2
     * @param Work_DAYS  工作天數
     * @return
     */
	public static double getMD_Min_Week_Cap_QTY(String FA_NO,String SH_NO,Connection conn,int WORK_WEEK,double... Work_DAYS) {
		double iRet=0;
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		double days=5;
		if(Work_DAYS.length>0) days=Work_DAYS[0];
		
		try{
			String strFields="";
			for(int i=1;i<=40;i++) {
				if(!strFields.equals("")) strFields=strFields+",";
				strFields=strFields+"U"+i+",T"+i;
			}
			
			strSQL="select "+strFields+" from DSSH05 where SH_NO='"+SH_NO+"'";			
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	for(int i=1;i<=40;i++) {
		    		if(getValue(rs.getString("U"+i)).equals("T")) {
		    			String SH_SIZE=getValue(getValue(rs.getString("T"+i)));    		    			
		    		    
		    			iRet=iRet+FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(FA_NO, SH_NO, SH_SIZE, conn, WORK_WEEK, Work_DAYS);
		    			
		    		}
		    	}
		    }
		    rs.close();
		    pstmtData.close();
	
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }	
		return iRet;
	}
	
    /**
     * 取得型體模具最小周產能<br>
     * 最小產能==型體size各部位中模具產能最小的<br>
     * 需要考慮型體共模<br>
     * 默認每周工作5天
     * @param FA_NO   廠別
     * @param SH_NO   型體
     * @param SH_SIZE size
     * @param conn2
     * @param Work_DAYS  工作天數
     * @return
     */
	public static double getMD_Min_Week_Cap_QTY(
			String FA_NO,
			String SH_NO,
			String SH_SIZE,
			Connection conn,
			int WORK_WEEK,
			double... Work_DAYS) {
		double iRet=0;
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		double days=5;
		if(Work_DAYS.length>0) days=Work_DAYS[0];
		
		try{
						
			strSQL="select PART_NO,sum(MD_PER_QTY * MD_IE * MD_NUM * "+days+") CAP_QTY " +
				   "from FCMPS0022 " +
				   "where FA_NO='"+FA_NO+"' " +
				   "  and SH_NO='"+SH_NO+"' " +
				   "  and SH_SIZE='"+SH_SIZE+"' " +
				   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
				   "                      from FCMPS0022 A " +
				   "                      where FA_NO=FCMPS0022.FA_NO " +
				   "                        and SH_NO=FCMPS0022.SH_NO " +
				   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
				   "                        and PART_NO=FCMPS0022.PART_NO " +
				   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") "+
				   "group by PART_NO "+
				   "order by sum(MD_PER_QTY * MD_IE * MD_NUM * "+days+") asc ";				    			
			
//			System.out.println(strSQL);
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=getDouble(rs.getDouble("CAP_QTY"));		    		    	
		    }
		    rs.close();
		    pstmtData.close();	
	
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }		
		return iRet;
	}
	
	/**
	 * 取得型體瓶頸制程周產能
	 * @param FA_NO
	 * @param SH_NO
	 * @param conn2
	 * @return
	 */
	public static double getSH_Min_Week_Cap_QTY(String FA_NO,String SH_NO,int WORK_WEEK,Connection conn) {
		double iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
		    
			//如果型體需要射出,且沒有設定產能,就取模具產能
			if(FCMPS_PUBLIC.getSH_Need_PROC(SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, conn)) {
				iRet=FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(FA_NO, SH_NO, conn,WORK_WEEK);
			}
			
			strSQL="select min(WORK_CAP_QTY) WORK_CAP_QTY from FCMPS012 " +
		           "where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"'";
	
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(iRet>getDouble(rs.getDouble("WORK_CAP_QTY")))iRet=getDouble(rs.getDouble("WORK_CAP_QTY"));
		    }
		    rs.close();
		    pstmtData.close();	
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }	
		return iRet;
	}	
	
    /**
     * 取得型體Size瓶頸部位的每模雙數<br>
     * 考慮型體共模<br>
     * @param FA_NO   廠別
     * @param SH_NO   型體
     * @param SH_SIZE size
     * @param conn2
     * @return
     */
	public static double getMD_PAIR_QTY(
			String FA_NO,
			String SH_NO,
			String SH_SIZE,
			Connection conn,
			int WORK_WEEK) {
		double iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try	{
			
			strSQL="select PART_NO,sum(MD_PER_QTY  * MD_NUM ) CAP_QTY " +
				   "from FCMPS0022 where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"' and SH_SIZE='"+SH_SIZE+"' " +
				   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
				   "                      from FCMPS0022 A " +
				   "                      where FA_NO=FCMPS0022.FA_NO " +
				   "                        and SH_NO=FCMPS0022.SH_NO " +
				   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
				   "                        and PART_NO=FCMPS0022.PART_NO " +
				   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") "+
				   "group by PART_NO "+
				   "order by sum(MD_PER_QTY  * MD_NUM ) ";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=getDouble(rs.getDouble("CAP_QTY"));
		    }
		    rs.close();
		    pstmtData.close();
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }		
		return iRet;
	}
	
    /**
     * 取得型體Size指定部位的每模雙數<br>
     * 考慮型體共模<br>
     * @param FA_NO   廠別
     * @param SH_NO   型體
     * @param SH_SIZE size
     * @param PART_NO 部位
     * @param conn2
     * @return
     */
	public static double getMD_PAIR_QTY(
			String FA_NO,
			String SH_NO,
			String SH_SIZE,
			String PART_NO,
			Connection conn,
			int WORK_WEEK) {
		double iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			
			strSQL="select sum(MD_PER_QTY  * MD_NUM ) CAP_QTY " +
				   "from FCMPS0022 " +
				   "where FA_NO='"+FA_NO+"' " +
				   "  and SH_NO='"+SH_NO+"' " +
				   "  and SH_SIZE='"+SH_SIZE+"' " +
				   "  and PART_NO='"+PART_NO+"' " +
				   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
				   "                      from FCMPS0022 A " +
				   "                      where FA_NO=FCMPS0022.FA_NO " +
				   "                        and SH_NO=FCMPS0022.SH_NO " +
				   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
				   "                        and PART_NO=FCMPS0022.PART_NO " +
				   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") ";
				   
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=getDouble(rs.getDouble("CAP_QTY"));
		    }
		    rs.close();
		    pstmtData.close();
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }		
		return iRet;
	}
	
    /**
     * 取得型體Size指定部位的每模雙數<br>
     * 考慮型體共模<br>
     * @param FA_NO   廠別
     * @param SH_NO   型體
     * @param SH_SIZE size
     * @param PART_NO 部位
     * @param MD_PER_QTY 每模雙數
     * @param conn
     * @param WORK_WEEK 周次
     * @return
     */
	public static double getMD_PAIR_QTY(
			String FA_NO,
			String SH_NO,
			String SH_SIZE,
			String PART_NO,
			double MD_PER_QTY,
			Connection conn,
			int WORK_WEEK) {
		double iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			
			strSQL="select sum(MD_PER_QTY  * MD_NUM ) CAP_QTY " +
				   "from FCMPS0022 " +
				   "where FA_NO='"+FA_NO+"' " +
				   "  and SH_NO='"+SH_NO+"' " +
				   "  and SH_SIZE='"+SH_SIZE+"' " +
				   "  and PART_NO='"+PART_NO+"' " +
				   "  and MD_PER_QTY="+MD_PER_QTY+" " +
				   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
				   "                      from FCMPS0022 A " +
				   "                      where FA_NO=FCMPS0022.FA_NO " +
				   "                        and SH_NO=FCMPS0022.SH_NO " +
				   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
				   "                        and PART_NO=FCMPS0022.PART_NO " +
				   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") ";
				   
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=getDouble(rs.getDouble("CAP_QTY"));
		    }
		    rs.close();
		    pstmtData.close();
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }		
		return iRet;
	}

	/**
	 * 型體size是否有此部位的模具
	 * @param FA_NO
	 * @param SH_NO
	 * @param SH_SIZE
	 * @param PART_NO
	 * @param conn2
	 * @return
	 */
	public static boolean is_Exist_PART_MD(
			String FA_NO,
			String SH_NO,
			String SH_SIZE,
			String PART_NO,
			Connection conn,
			int WORK_WEEK) {
		boolean iRet=false;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
						
			strSQL="select PART_NO2 from FCMPS0022 " +
				   "where FA_NO='"+FA_NO+"' " +
				   "  and SH_NO='"+SH_NO+"' " +
				   "  and SH_SIZE='"+SH_SIZE+"' " +
				   "  and PART_NO='"+PART_NO+"' " +
				   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
				   "                      from FCMPS0022 A " +
				   "                      where FA_NO=FCMPS0022.FA_NO " +
				   "                        and SH_NO=FCMPS0022.SH_NO " +
				   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
				   "                        and PART_NO=FCMPS0022.PART_NO " +
				   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") ";
		    		
						
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=true;
		    }
		    rs.close();
		    pstmtData.close();
		    
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }		
		return iRet;
	}
	
    /**
     * 取部位的名稱
     * @param PART_NO
     * @return
     */
	public static String getPART_NA(String PART_NO,Connection conn){
		String iRet="";
		String strSQL="";
		PreparedStatement pstmtData = null;			
		ResultSet rs=null;			
		try{

			strSQL="select PART_NO,nvl(PART_NA,' ') PART_NA from FCSC01 where PART_LNO='A' and PART_NO='"+PART_NO+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    		    		    
		    if(rs.next()){
		    	iRet=rs.getString("PART_NA").trim();    				    
		    }
		    
		    rs.close();	
		    pstmtData.close();	    															    
		    
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }	
		return iRet;
	}
	

	

	/**
	 * 取得型體瓶頸制程的周產量
	 * @param FA_NO
	 * @param SH_NO
	 * @param NEED_SHOOT
	 * @param WORK_WEEK
	 * @param conn
	 * @param WORK_DAYS
	 * @return
	 */
	public static double get_SH_Plan_QTY(
			String FA_NO,
			String SH_NO,
			boolean NEED_SHOOT,
			int WORK_WEEK,
			Connection conn,
			double... WORK_DAYS) {
		double iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		double days=5;
		if(WORK_DAYS.length>0) days=WORK_DAYS[0];
		
		try{
			
	    	//如果型體需要射出,取模具的產能
	    	if(NEED_SHOOT) {	    		
	    		iRet=FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(FA_NO, SH_NO, conn,WORK_WEEK,days);
	    		
	    	}
	    	
			strSQL="select WORK_CAP_QTY from FCMPS011 " +
		           "where FA_NO='"+FA_NO+"' and WORK_WEEK="+WORK_WEEK+" and SH_NO='"+SH_NO+"'";
	
	        strSQL=strSQL+" UNION ALL ";
	
            strSQL=strSQL+"select WORK_CAP_QTY from FCMPS012 " +
                          "where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"'";
 
            strSQL="select MIN(WORK_CAP_QTY) WORK_CAP_QTY from ("+strSQL+")";
      
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
			  
		    if(rs.next()){
    	    	if(rs.getDouble("WORK_CAP_QTY")>0) {
    	    		if(iRet>0) {
    	    			if(rs.getDouble("WORK_CAP_QTY")/5*days<iRet) iRet=Math.round(rs.getDouble("WORK_CAP_QTY")/5*days);
    	    		}else {
    	    			iRet=Math.round(rs.getDouble("WORK_CAP_QTY")/5*days);
    	    		}    	    	
    	    	}
		    }
		    rs.close();
		    pstmtData.close();	
		    			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		return iRet;
	}
	
	/**
	 * 取得型體瓶頸制程的周產量
	 * @param FA_NO
	 * @param SH_NO
	 * @param NEED_SHOOT
	 * @param WORK_WEEK
	 * @param conn
	 * @param WORK_DAYS
	 * @return
	 */
	public static double get_SH_Plan_QTY(
			String FA_NO,
			String SH_NO,
			boolean NEED_SHOOT,
			int WORK_WEEK_START,
			int WORK_WEEK_END,
			Connection conn,
			double... WORK_DAYS) {
		double iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		double days=5;
		if(WORK_DAYS.length>0) days=WORK_DAYS[0];
		
		try{
			
	    	//如果型體需要射出,取模具的產能
	    	if(NEED_SHOOT) {	 
	    		for(int iWEEK=WORK_WEEK_START;iWEEK<=WORK_WEEK_END;iWEEK=FCMPS_PUBLIC.getNext_Week(iWEEK, 1)) {
	    			double qty=FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(FA_NO, SH_NO, conn,iWEEK,days);
	    			if(iRet==0) {
	    				iRet=qty;
	    			}else {
	    				if(iRet>qty) iRet=qty;
	    			}
	    			
	    		}
	    			    		
	    	}
	    	
			strSQL="select WORK_CAP_QTY from FCMPS011 " +
		           "where FA_NO='"+FA_NO+"' and WORK_WEEK>="+WORK_WEEK_START+" and WORK_WEEK<="+WORK_WEEK_END+" and SH_NO='"+SH_NO+"'";
	
	        strSQL=strSQL+" UNION ALL ";
	
            strSQL=strSQL+"select WORK_CAP_QTY from FCMPS012 " +
                          "where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"'";
 
            strSQL="select MIN(WORK_CAP_QTY) WORK_CAP_QTY from ("+strSQL+")";
      
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
			  
		    if(rs.next()){
    	    	if(rs.getDouble("WORK_CAP_QTY")>0) {
    	    		if(iRet>0) {
    	    			if(rs.getDouble("WORK_CAP_QTY")/5*days<iRet) iRet=Math.round(rs.getDouble("WORK_CAP_QTY")/5*days);
    	    		}else {
    	    			iRet=Math.round(rs.getDouble("WORK_CAP_QTY")/5*days);
    	    		}    	    	
    	    	}
		    }
		    rs.close();
		    pstmtData.close();	
		    			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		return iRet;
	}
	
	/**
	 * 取得型體瓶頸制程的周產量
	 * @param FA_NO
	 * @param SH_NO
	 * @param NEED_SHOOT
	 * @param WORK_WEEK
	 * @param conn
	 * @param WORK_DAYS
	 * @return
	 */
	public static double get_SH_Plan_QTY(
			String FA_NO,
			String SH_NO,
			String PROCID,
			boolean NEED_SHOOT,
			int WORK_WEEK,
			Connection conn,
			double... WORK_DAYS) {

		double iRet=0;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			
			int Work_Week_Start=WORK_WEEK;
			
			strSQL="select distinct PROC_SEQ  "+
                   "  from fcps22_1 "+
                   " where sh_aritcle = '"+SH_NO+"' "+
                   "   and proc_seq < (select proc_seq "+
                   "                     from fcps22_1 "+
                   "                    where sh_aritcle = '"+SH_NO+"' "+
                   "                      and pb_ptno = '"+PROCID+"') "+
                   "   and proc_seq >= (select min(proc_seq) "+
                   "                      from fcps22_1 "+
                   "                     where sh_aritcle = '"+SH_NO+"') ";
			
			strSQL="select count(*) iCount from ("+strSQL+")";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {
		    	Work_Week_Start=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, rs.getInt("iCount"));
		    }
			rs.close();
			pstmtData.close();

			int iWork_Week=Work_Week_Start;
			
			strSQL="select DISTINCT PROC_SEQ "+
                   "  from fcps22_1 "+
                   " where sh_aritcle = '"+SH_NO+"' and PROC_SEQ is not null order by PROC_SEQ ";
	
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {		
		    	iWork_Week=FCMPS_PUBLIC.getPrevious_Week(iWork_Week, 1);
		    	do {
					
					int iwk=0;
					do {
			    		double workdays=FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,iWork_Week,conn);
			    		if(workdays==0) {
			    			iWork_Week=FCMPS_PUBLIC.getNext_Week(iWork_Week, 1);
			    		}else {
			    			break;
			    		}
			    		if(iwk==4)break;
			    		iwk++;
					}while(true);
					
					iWork_Week=FCMPS_PUBLIC.getNext_Week(iWork_Week, 1);
					
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();
			
			iRet=FCMPS_PUBLIC.get_SH_Plan_QTY(
					FA_NO,
					SH_NO,
					NEED_SHOOT,
					Work_Week_Start,
					iWork_Week,
					conn,
					WORK_DAYS);
						
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
			
	}
	
	/**
	 * 取得型體瓶頸制程的周產量
	 * @param FA_NO
	 * @param SH_NO
	 * @param WORK_WEEK
	 * @param conn
	 * @param WORK_DAYS
	 * @return
	 */
	public static double get_SH_Plan_QTY(
			String FA_NO,
			String SH_NO,
			int WORK_WEEK,
			Connection conn,
			double... WORK_DAYS) {
		double iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		double days=5;
		if(WORK_DAYS.length>0) days=WORK_DAYS[0];
		
		try{
			
	    	//如果型體需要射出,取模具的產能
	    	if(FCMPS_PUBLIC.getSH_Need_PROC(SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, conn)) {	    		
	    		iRet=FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(FA_NO, SH_NO, conn,WORK_WEEK,days);
	    		
	    	}
	    	
			strSQL="select WORK_CAP_QTY from FCMPS011 " +
		           "where FA_NO='"+FA_NO+"' and WORK_WEEK="+WORK_WEEK+" and SH_NO='"+SH_NO+"'";
	
	        strSQL=strSQL+" UNION ALL ";
	
            strSQL=strSQL+"select WORK_CAP_QTY from FCMPS012 " +
                          "where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"'";
 
            strSQL="select MIN(WORK_CAP_QTY) WORK_CAP_QTY from ("+strSQL+")";
      
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
			  
		    if(rs.next()){
    	    	if(rs.getDouble("WORK_CAP_QTY")>0) {
    	    		if(iRet>0) {
    	    			if(rs.getDouble("WORK_CAP_QTY")/5*days<iRet) iRet=Math.round(rs.getDouble("WORK_CAP_QTY")/5*days);
    	    		}else {
    	    			iRet=Math.round(rs.getDouble("WORK_CAP_QTY")/5*days);
    	    		}    	    	
    	    	}
		    }
		    rs.close();
		    pstmtData.close();	
		    			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		return iRet;
	}
	
	/**
	 * 取得型體射出的部位
	 * @param FA_NO
	 * @param SH_NO
	 * @param SH_SIZE
	 * @param conn
	 * @return 部位
	 */
	public static ArrayList<String[]> getMD_Style_Part(
			String FA_NO,
			String SH_NO,
			Connection conn,
			int WORK_WEEK) {
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";
				
		strSQL="select distinct PART_NO from FCMPS0022 " +
			   "where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"' " +
			   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
			   "                      from FCMPS0022 A " +
			   "                      where FA_NO=FCMPS0022.FA_NO " +
			   "                        and SH_NO=FCMPS0022.SH_NO " +
			   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
			   "                        and PART_NO=FCMPS0022.PART_NO " +
			   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") ";
				
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		String part[]=new String[] {SH_NO,rs.getString("PART_NO")};
		    		iRet.add(part);
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	/**
	 * 取得型體射出的共模型體,Size,部位
	 * @param FA_NO
	 * @param SH_NO
	 * @param SH_SIZE
	 * @param PART_NO
	 * @param conn
	 * @return 共模的部位
	 */
	public static ArrayList<String[]> getMD_Style_Share_Part(
			String FA_NO,
			String SH_NO,
			String SH_SIZE,
			String PART_NO,
			Connection conn,int WORK_WEEK) {
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";
				
		strSQL="select distinct SH_NO2,PART_NO2 from FCMPS0022 " +
			   "where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"' and SH_SIZE='"+SH_SIZE+"' and PART_NO='"+PART_NO+"' " +
			   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
			   "                      from FCMPS0022 A " +
			   "                      where FA_NO=FCMPS0022.FA_NO " +
			   "                        and SH_NO=FCMPS0022.SH_NO " +
			   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
			   "                        and PART_NO=FCMPS0022.PART_NO " +
			   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") ";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		String part[]=new String[] {rs.getString("SH_NO2"),rs.getString("PART_NO2")};
		    		iRet.add(part);
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }	
	    		
		return iRet;
	}
	
	/**
	 * 取得型體射出的共模型體,Size,部位
	 * @param FA_NO
	 * @param SH_NO
	 * @param SH_SIZE
	 * @param conn
	 * @return 共模的部位
	 */
	public static ArrayList<String[]> getMD_Style_Share_Part(
			String FA_NO,
			String SH_NO,
			String SH_SIZE,
			Connection conn,
			int WORK_WEEK) {
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";
				
		strSQL="select distinct SH_NO2,PART_NO2 from FCMPS0022 " +
			   "where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"' and SH_SIZE='"+SH_SIZE+"' " +
			   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
			   "                      from FCMPS0022 A " +
			   "                      where FA_NO=FCMPS0022.FA_NO " +
			   "                        and SH_NO=FCMPS0022.SH_NO " +
			   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
			   "                        and PART_NO=FCMPS0022.PART_NO " +
			   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") ";
				
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		String part[]=new String[] {rs.getString("SH_NO2"),rs.getString("PART_NO2")};
		    		iRet.add(part);
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	/**
	 * 取得型體射出的部位
	 * @param FA_NO
	 * @param SH_NO
	 * @param SH_SIZE
	 * @param conn
	 * @return 部位
	 */
	public static ArrayList<String> getMD_Style_Share_Part2(
			String FA_NO,
			String SH_NO,
			String SH_SIZE,
			Connection conn,
			int WORK_WEEK) {
		ArrayList<String> iRet=new ArrayList<String>();
		String strSQL="";
				
		strSQL="select distinct PART_NO from FCMPS0022 " +
			   "where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"' and SH_SIZE='"+SH_SIZE+"' " +
			   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
			   "                      from FCMPS0022 A " +
			   "                      where FA_NO=FCMPS0022.FA_NO " +
			   "                        and SH_NO=FCMPS0022.SH_NO " +
			   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
			   "                        and PART_NO=FCMPS0022.PART_NO " +
			   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") ";
				
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		iRet.add(rs.getString("PART_NO"));
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	/**
	 * 取得型體射出的共模型體,Size,部位
	 * @param FA_NO
	 * @param SH_NO
	 * @param SH_SIZE
	 * @param conn
	 * @return 共模的部位
	 */
	public static ArrayList<String[]> getMD_Style_Share(
			String FA_NO,
			String SH_NO,
			String SH_SIZE,
			Connection conn,
			int WORK_WEEK) {
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";
		
		strSQL="select distinct SH_NO2,SH_SIZE2 from FCMPS0022 " +
			   "where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"' and SH_SIZE='"+SH_SIZE+"' "+
			   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
			   "                      from FCMPS0022 A " +
			   "                      where FA_NO=FCMPS0022.FA_NO " +
			   "                        and SH_NO=FCMPS0022.SH_NO " +
			   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
			   "                        and PART_NO=FCMPS0022.PART_NO " +
			   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") ";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		String part[]=new String[] {rs.getString("SH_NO2"),rs.getString("SH_SIZE2")};
		    		iRet.add(part);
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		return iRet;
	}

	/**
	 * 取得型體射出的共模型體,Size,部位
	 * @param FA_NO
	 * @param SH_NO
	 * @param SH_SIZE
	 * @param conn
	 * @return 共模的部位
	 */
	public static ArrayList<String[]> getMD_Style_Share(String FA_NO,String SH_NO,Connection conn,int WORK_WEEK) {
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";
				
		strSQL="select distinct SH_NO2,SH_SIZE2 " +
			   "from FCMPS0022 " +
			   "where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"' " +
			   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
			   "                      from FCMPS0022 A " +
			   "                      where FA_NO=FCMPS0022.FA_NO " +
			   "                        and SH_NO=FCMPS0022.SH_NO " +
			   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
			   "                        and PART_NO=FCMPS0022.PART_NO " +
			   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") ";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		String part[]=new String[] {rs.getString("SH_NO2"),rs.getString("SH_SIZE2")};
		    		iRet.add(part);
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	/**
	 * 是否共模型體
	 * @param SH_NO
	 * @param conn2
	 * @return
	 */
	public static boolean Style_Is_Share_MD(String FA_NO,String SH_NO,Connection conn) {
		boolean iRet=false;
		String strSQL="";
				
		strSQL="select count(*) iCount from FCMPS0022 where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"' and SH_NO2<>'"+SH_NO+"'";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(rs.getInt("iCount")>0) iRet=true;
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	/**
	 * 型體SIZE的共模部位是否就是瓶頸部位
	 * @param FA_NO
	 * @param SH_NO
	 * @param SH_SIZE
	 * @param conn2
	 * @return
	 */
	public static boolean ChokePointPart_is_SharePart(String FA_NO,String SH_NO,String SH_SIZE,Connection conn,int WORK_WEEK) {
		boolean iRet=false;
		String strSQL="";
		
		String PART_NO=FCMPS_PUBLIC.getMD_Choke_Point_Part(FA_NO, SH_NO, SH_SIZE, conn,WORK_WEEK);
				
		strSQL="select count(*) iCount from FCMPS0022 " +
			   "where FA_NO='"+FA_NO+"' " +
			   "  and  SH_NO='"+SH_NO+"' " +
			   "  and  SH_NO<>SH_NO2 " +
			   "  and  PART_NO='"+PART_NO+"' " +
			   "  and  SH_SIZE='"+SH_SIZE+"'"+
			   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
			   "                      from FCMPS0022 A " +
			   "                      where FA_NO=FCMPS0022.FA_NO " +
			   "                        and SH_NO=FCMPS0022.SH_NO " +
			   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
			   "                        and PART_NO=FCMPS0022.PART_NO " +
			   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") ";			   
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(rs.getInt("iCount")>0) iRet=true;
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	/**
	 * 訂單是否需要某個制程
	 * @param OD_PONO1
	 * @param PROCID
	 * @return boolean
	 */
	public static boolean getOrder_Need_PROC(String OD_PONO1,String PROCID,Connection conn) {
		boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select count(*) iCount from FCMPS010 where OD_PONO1='"+OD_PONO1+"' AND PROCID='"+PROCID+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(rs.getInt("iCount")>0) iRet=true;
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }	
		
		return iRet;
	}
	
	/**
	 * 型體是否需要某個制程
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	public static List<String> getSH_PROC(String SH_NO,Connection conn) {
		List<String> iRet=new ArrayList<String>();
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select PB_PTNO from fcps22_1 where SH_ARITCLE='"+SH_NO+"' AND NEED_PLAN='Y' ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		iRet.add(rs.getString("PB_PTNO"));
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	/**
	 * 型體是否需要某個制程
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	public static boolean getSH_Need_PROC(String SH_NO,String PROCID,Connection conn) {
		boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select count(*) iCount from fcps22_1 where SH_ARITCLE='"+SH_NO+"' AND PB_PTNO='"+PROCID+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(rs.getInt("iCount")>0) iRet=true;
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	/**
	 * 型體是否需要某個制程
	 * @param SH_NO
	 * @param PROCID
	 * @param 
	 * @return
	 */
	public static boolean getSH_Need_PROC(String SH_NO,String PROCID,double PROC_SEQ,Connection conn) {
		boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select count(*) iCount from fcps22_1 " +
				   "where SH_ARITCLE='"+SH_NO+"' " +
				   "  AND PB_PTNO='"+PROCID+"'"+
				   "  AND PROC_SEQ="+PROC_SEQ;
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(rs.getInt("iCount")>0) iRet=true;
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
    /**
     * 取得制程的周產量
     * @param FA_NO
     * @param WORK_WEEK
     * @param PROCID
     * @return [0] 標準產能  [1]极限產能
     */
	public static double[] get_PROC_Plan_QTY(String FA_NO,int WORK_WEEK,String PROCID,Connection conn) {
		double iRet[]=new double[] {0,0};
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		double PR_QTY=0;
		
		try{
/*			
			int pre_WORK_WEEK=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, 1);
			//取前一周總的工令欠數
			strSQL="select sum(SIZE_QTY) SIZE_QTY from FCMPS014 " +
			   "where FA_NO='"+FA_NO+"' " +
			   "  and WORK_WEEK="+pre_WORK_WEEK+
			   "  and EXISTS(SELECT * FROM FCPS22_1 WHERE SH_ARITCLE=FCMPS014.SH_ARITCLE AND PB_PTNO='"+PROCID+"')";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	PR_QTY=rs.getDouble("SIZE_QTY");
		    }
		    rs.close();
		    pstmtData.close();
*/						
		    //取制程的最大周產能
			strSQL="select WORK_CAP_QTY,WORK_MAX_CAP_QTY from FCMPS008 " +
				   "where FA_NO='"+FA_NO+"' and WORK_WEEK="+WORK_WEEK+" and PROCID='"+PROCID+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(!rs.next()){
				strSQL="select WORK_CAP_QTY,WORK_MAX_CAP_QTY from FCMPS009 " +
				   "where FA_NO='"+FA_NO+"' and PROCID='"+PROCID+"'";
			    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    rs2=pstmtData2.executeQuery();
			    if(rs2.next()) {
			    	iRet[0]=rs2.getDouble("WORK_CAP_QTY");
			    	iRet[1]=rs2.getDouble("WORK_MAX_CAP_QTY");
			    }
				rs2.close();
				pstmtData2.close();
		    }else {
		    	iRet[0]=rs.getDouble("WORK_CAP_QTY");
		    	iRet[1]=rs.getDouble("WORK_MAX_CAP_QTY");
		    }
			rs.close();
			pstmtData.close();

			//周產能要減去工令欠數
			iRet[0]=iRet[0]-PR_QTY;
			if(iRet[0]<0)iRet[0]=0;

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
    /**
     * 取得制程的周產量
     * @param FA_NO
     * @param WORK_WEEK
     * @param PROCID
     * @return
     */
	public static double get_PROC_Plan_QTY(String FA_NO,String PROCID,Connection conn) {
		double iRet=0;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
				
		    //取制程的最大周產能
			strSQL="select WORK_CAP_QTY from FCMPS009 " +
			       "where FA_NO='"+FA_NO+"' and PROCID='"+PROCID+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {
		    	iRet=rs.getDouble("WORK_CAP_QTY");
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	

    /**
     * 取得制程已排的數量
     * @param FA_NO
     * @param PLAN_WEEK 計劃周次
     * @param CUR_PROCID 當前制程
     * @param CUR_WORK_WEEK 當前制程所在周次
     * @param conn
     * @return
     */
	public static double get_PROC_Planed_QTY(String FA_NO,int PLAN_WEEK,String CUR_PROCID,int CUR_WORK_WEEK,Connection conn) {
		double iRet=0;
		String strSQL="";
		String PLAN_NO="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		try{
				
			int max_SEQ=0;
			strSQL="select max(proc_seq) proc_seq from (select sh_aritcle, count(proc_seq) proc_seq "+
                   "                                      from (select distinct sh_aritcle, proc_seq from FCPS22_1 "+
                   "                                             where proc_seq is not null) "+
                   "                                    group by sh_aritcle" +
                   ")";
		
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {
		    	max_SEQ=rs.getInt("proc_seq");
		    }
			rs.close();
			pstmtData.close();
		
					
			strSQL="SELECT PLAN_NO FROM FCMPS006 "+
                   " WHERE WORK_WEEK>="+FCMPS_PUBLIC.getPrevious_Week(PLAN_WEEK, max_SEQ)+
                   "   AND WORK_WEEK<"+PLAN_WEEK+ 
                   "   AND IS_SURE='Y' "+
                   "   AND FA_NO='"+FA_NO+"' ";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {
		    	Map<String,List<Double>> set_PROC_SEQ=new HashMap<String,List<Double>>();
		    	
		    	do {
		    		PLAN_NO=rs.getString("PLAN_NO");
		    		
					strSQL="select distinct " +
				           "SH_NO," +
				           "FCMPS006.PROCID," +
				           "FCMPS006.WORK_WEEK " +
				           " FROM FCMPS006,FCMPS007 " +
				           "WHERE  FCMPS006.PLAN_NO=FCMPS007.PLAN_NO" +
				           "  AND FCMPS006.PLAN_NO='"+PLAN_NO+"' "+
				           "order by SH_NO";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    rs2.setFetchDirection(ResultSet.FETCH_FORWARD);
				    rs2.setFetchSize(3000);
				    
				    if(rs2.next()){
				    	
				    	Map<String,Integer> ls_WORK_WEEK_START=new HashMap<String,Integer>();
				    					    	
				    	do {
				    		String SH_NO=rs2.getString("SH_NO");
				    		String PROCID=rs2.getString("PROCID");
				    		int WORK_WEEK=rs2.getInt("WORK_WEEK");
				    		
				    		Integer Work_Week_Start=ls_WORK_WEEK_START.get(SH_NO);
				    		if(Work_Week_Start==null) {
				    			strSQL="select distinct PROC_SEQ  "+
			                           "  from fcps22_1 "+
			                           " where sh_aritcle = '"+SH_NO+"' "+
			                           "   and proc_seq < (select proc_seq "+
			                           "                     from fcps22_1 "+
			                           "                    where sh_aritcle = '"+SH_NO+"' "+
			                           "                      and pb_ptno = '"+PROCID+"') "+
			                           "   and proc_seq >= (select min(proc_seq) "+
			                           "                      from fcps22_1 "+
			                           "                     where sh_aritcle = '"+SH_NO+"') ";
			    			
				    			strSQL="select count(*) iCount from ("+strSQL+")";
				    			
				    		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    		    rs3=pstmtData3.executeQuery();
				    		    if(rs3.next()) {
				    		    	Work_Week_Start=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, rs3.getInt("iCount"));
				    		    	ls_WORK_WEEK_START.put(SH_NO, Work_Week_Start);
				    		    }
				    			rs3.close();
				    			pstmtData3.close();
				    		}
				    		
				    		List<Double> ls_PROC_SEQ=set_PROC_SEQ.get(SH_NO);
				    		if(ls_PROC_SEQ==null) {
				    			
				    			strSQL="select distinct PROC_SEQ from FCPS22_1 " +
				    				   "where SH_ARITCLE='"+SH_NO+"' order by PROC_SEQ";
				    		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    		    rs3=pstmtData3.executeQuery();
				    		    
				    		    if(rs3.next()){
				    		    	ls_PROC_SEQ=new ArrayList<Double>();
				    		    	do {		    		    		
				    		    		ls_PROC_SEQ.add(rs3.getDouble("PROC_SEQ"));
				    		    	}while(rs3.next());
				    		    	set_PROC_SEQ.put(SH_NO, ls_PROC_SEQ);
				    		    }
				    			rs3.close();
				    			pstmtData3.close();    					    			    					    			
				    		}
			     			
			    			for(int i=0;i<ls_PROC_SEQ.size();i++) {
			    				int iWork_Week=Work_Week_Start;
			    			    for(int iweek=0;iweek<i;iweek++) {
				    				iWork_Week=FCMPS_PUBLIC.getNext_Week(iWork_Week, 1);

				    				do {
					    				if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO, iWork_Week, conn)==0) {
					    					iWork_Week=FCMPS_PUBLIC.getNext_Week(iWork_Week, 1);
					    				}else {
					    					break;
					    				}
				    				}while(true);
			    			    }
			    				
			    			    if(iWork_Week!=CUR_WORK_WEEK) continue; //不是同一周次
			    			    	    			    	
			    			    boolean is_Same=false;
			    			    
			    				strSQL="select PB_PTNO from FCPS22_1 " +
			    					   "where SH_ARITCLE='"+SH_NO+"' " +
			    					   "  and IS_USE_CAP='Y'"+
			    					   "  and PROC_SEQ="+ls_PROC_SEQ.get(i)+" " +
			    					   "  and PB_PTNO='"+CUR_PROCID+"'";
			    				
				    		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    		    rs3=pstmtData3.executeQuery();
				    		    if(rs3.next()) {
				    		    	is_Same=true;
				    		    }
				    			rs3.close();
				    			pstmtData3.close();

				    			if(!is_Same) continue; //不是相同的制程
				    			
				    			strSQL="select sum(WORK_PLAN_QTY) WORK_PLAN_QTY from FCMPS007 " +
				    				   " where PLAN_NO='"+PLAN_NO+"' "+
				    				   "   and SH_NO='"+SH_NO+"'";
				    		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    		    rs3=pstmtData3.executeQuery();
				    		    if(rs3.next()) {
				    		    	iRet=iRet+getDouble(rs3.getDouble("WORK_PLAN_QTY"));				    		    	
				    		    }
				    			rs3.close();
				    			pstmtData3.close();
				    							    			
			    			}
			    					    		
				    	}while(rs2.next());
				    }
					rs2.close();
					pstmtData2.close();
					
		    	}while(rs.next());		    	
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	
    /**
     * 取得型體Size模具的瓶頸部位<br>
     * 默認每周工作5天
     * @param FA_NO   廠別
     * @param SH_NO   型體
     * @param SH_SIZE size
     * @param conn2
     * @param Work_DAYS  工作天數
     * @return
     */
	public static String getMD_Choke_Point_Part(
			String FA_NO,
			String SH_NO,
			String SH_SIZE,
			Connection conn,
			int WORK_WEEK,
			double... Work_DAYS) {
		String iRet="";
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		double days=5;
		if(Work_DAYS.length>0) days=Work_DAYS[0];
		
		try{
			
			strSQL="select PART_NO,sum(MD_PER_QTY * MD_IE * MD_NUM * "+days+") CAP_QTY " +
				   "from FCMPS0022 " +
				   "where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"' and SH_SIZE='"+SH_SIZE+"' "+ 
				   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
				   "                      from FCMPS0022 A " +
				   "                      where FA_NO=FCMPS0022.FA_NO " +
				   "                        and SH_NO=FCMPS0022.SH_NO " +
				   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
				   "                        and PART_NO=FCMPS0022.PART_NO " +
				   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") "+
			       "group by PART_NO "+
			       "order by sum(MD_PER_QTY * MD_IE * MD_NUM * "+days+") ";			    			
			
//			System.out.println(strSQL);
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=rs.getString("PART_NO");		    		    	
		    }
		    rs.close();
		    pstmtData.close();	
	
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }	
		return iRet;
	}
	
    /**
     * 取得型體模具的瓶頸部位<br>
     * 默認每周工作5天
     * @param FA_NO   廠別
     * @param SH_NO   型體
     * @param conn2
     * @param Work_DAYS  工作天數
     * @return
     */
	public static String getMD_Choke_Point_Part(String FA_NO,String SH_NO,Connection conn,int WORK_WEEK,double... Work_DAYS) {
		String iRet="";
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		double days=5;
		if(Work_DAYS.length>0) days=Work_DAYS[0];
		
		try{
						
			strSQL="select PART_NO,sum(MD_PER_QTY * MD_IE * MD_NUM * "+days+") CAP_QTY " +
				   "from FCMPS0022 " +
				   "where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"' " +
				   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
				   "                      from FCMPS0022 A " +
				   "                      where FA_NO=FCMPS0022.FA_NO " +
				   "                        and SH_NO=FCMPS0022.SH_NO " +
				   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
				   "                        and PART_NO=FCMPS0022.PART_NO " +
				   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") "+
	    		   "group by PART_NO "+
	    		   "order by sum(MD_PER_QTY * MD_IE * MD_NUM * "+days+")";		    			
			
//			System.out.println(strSQL);
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=rs.getString("PART_NO");		    		    	
		    }
		    rs.close();
		    pstmtData.close();	
	
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	/**
	 * 取SZIE的共模型體中最大產能的型體
	 * @param ls_STYLE_SIZE 共模型體列表
	 * @param FA_NO   廠別
	 * @param SH_SIZE SIZE
	 * @param conn2
	 * @param Work_DAYS 工作天數
	 * @return
	 */
	public static ArrayList<String[]> getSH_Share_SIZE_Max_MD_CAP(
			ArrayList<String[]> ls_STYLE_SIZE,
			String FA_NO,
			Connection conn,
			int WORK_WEEK,
			double...Work_DAYS){
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		if(ls_STYLE_SIZE==null) return iRet;
		if(ls_STYLE_SIZE.isEmpty()) return iRet;
		
		double days=5;
		if(Work_DAYS.length>0) days=Work_DAYS[0];
		
		String SH_NO="";
		
		for(int i=0;i<ls_STYLE_SIZE.size();i++) {
			if(!SH_NO.equals(""))SH_NO=SH_NO+",";
			SH_NO=SH_NO+"('"+ls_STYLE_SIZE.get(i)[0]+"','"+ls_STYLE_SIZE.get(i)[1]+"')";
		}
		
		try{
						
			strSQL="select SH_NO2,SH_SIZE2,PART_NO2,sum(MD_PER_QTY * MD_IE * MD_NUM * "+days+") CAP_QTY " +
				   "from FCMPS0022 where FA_NO='"+FA_NO+"' and (SH_NO,SH_SIZE) in ("+SH_NO+") " +
				   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
				   "                      from FCMPS0022 A " +
				   "                      where FA_NO=FCMPS0022.FA_NO " +
				   "                        and SH_NO=FCMPS0022.SH_NO " +
				   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
				   "                        and PART_NO=FCMPS0022.PART_NO " +
				   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") "+
                   "group by SH_NO2,SH_SIZE2,PART_NO2 ";	    		
			
			strSQL="select SH_NO2,SH_SIZE2,min(CAP_QTY) CAP_QTY2 "+
                   "from ("+strSQL+") "+
                   "group by SH_NO2,SH_SIZE2 "+
                   "order by min(CAP_QTY) DESC";
			
//			strSQL="select * "+
//                   "from ("+strSQL+") order by CAP_QTY desc";				    			
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet.add(new String[] {rs.getString("SH_NO2"),rs.getString("SH_SIZE2")});	    		    	
		    }
		    rs.close();
		    pstmtData.close();	
	
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	/**
	 * 取SZIE的共模型體中最大產能的型體
	 * @param ls_STYLE_SIZE 共模型體列表
	 * @param FA_NO   廠別
	 * @param SH_SIZE SIZE
	 * @param conn2
	 * @param Work_DAYS 工作天數
	 * @return
	 */
	public static ArrayList<String[]> getSH_Share_SIZE_Max_MD_CAP(
			String SH_NO,
			String SH_SIZE,
			String FA_NO,
			Connection conn,
			int WORK_WEEK,
			double...Work_DAYS){
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;		
		
		double days=5;
		if(Work_DAYS.length>0) days=Work_DAYS[0];
		
		try{
			
			strSQL="select SH_NO2,SH_SIZE2,PART_NO2,sum(MD_PER_QTY * MD_IE * MD_NUM * "+days+") CAP_QTY " +
			   "from FCMPS0022 where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO+"' and SH_SIZE='"+SH_SIZE+"' " +
			   "  and EFFECTIVE_WEEK=(select max(EFFECTIVE_WEEK) " +
			   "                      from FCMPS0022 A " +
			   "                      where FA_NO=FCMPS0022.FA_NO " +
			   "                        and SH_NO=FCMPS0022.SH_NO " +
			   "                        and SH_SIZE=FCMPS0022.SH_SIZE " +
			   "                        and PART_NO=FCMPS0022.PART_NO " +
			   "                        and EFFECTIVE_WEEK<="+WORK_WEEK+") "+
            "group by SH_NO2,SH_SIZE2,PART_NO2 ";	    			
			
			strSQL="select SH_NO2,SH_SIZE2,min(CAP_QTY) CAP_QTY2 "+
            "from ("+strSQL+") "+
            "group by SH_NO2,SH_SIZE2 "+
            "order by min(CAP_QTY) DESC";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet.add(new String[] {rs.getString("SH_NO2"),rs.getString("SH_SIZE2")});	    		    	
		    }
		    rs.close();
		    pstmtData.close();	
	
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	/**
	 * 制程是否需要占用產能
	 * @param SH_NO
	 * @param PROCID
	 * @param conn
	 * @return
	 */
	public static boolean get_SH_USE_CAP(String SH_NO,String PROCID,Connection conn) {
		boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
				
		    //取制程的最大周產能
			strSQL="select nvl(IS_USE_CAP,'N') IS_USE_CAP from FCPS22_1 " +
			       "where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+PROCID+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {
		    	if(rs.getString("IS_USE_CAP").equals("Y")) iRet=true;
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	

    /**
     * 取得型體當周的排產天數
     * @param FA_NO
     * @param SH_NO
     * @param Work_Week
     * @param conn
     * @param WORK_DAYS
     * @return
     */
	public static double getSH_WorkDaysOfWeek(
			String FA_NO,
			String SH_NO,
			int Work_Week,
			Connection conn,
			double...WORK_DAYS) {
		double iRet=0;
		String strSQL="";

		if(WORK_DAYS.length>0) iRet=WORK_DAYS[0];
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
				
		    //型體行事歷設定的天數
			strSQL="select SCHEDULE_DAY  from FCMPS024 " +
				   "where SH_NO='"+SH_NO+"' " +
				   "  and TO_CHAR(SCHEDULE_DAY,'IYIW')='"+Work_Week +"'"+
				   "  and IS_ACTIVE='Y' " +
				   "  and FA_NO='"+FA_NO+"' ";
			
			strSQL=strSQL+" UNION ";
			
			strSQL=strSQL+"select SCHEDULE_DAY  from FCMPS023 " +
		       "where TO_CHAR(SCHEDULE_DAY,'IYIW')='"+Work_Week +"'"+
		       "  and IS_ACTIVE='Y' " +
		       "  and FA_NO='"+FA_NO+"' "+
		       "  and not exists(select * from FCMPS024 where SH_NO='"+SH_NO+"' and SCHEDULE_DAY=FCMPS023.SCHEDULE_DAY and FA_NO='"+FA_NO+"')";
			
			strSQL="select count(*) Work_Week_days  from (" +strSQL+")";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {
		    	if(rs.getDouble("Work_Week_days")>0) iRet=rs.getDouble("Work_Week_days");
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}


	/**
	 * 取得型體制程周數內的最小排產天數
	 * @param FA_NO
	 * @param Share_SH_NO
	 * @param Work_Week
	 * @param conn
	 * @return
	 */
	public static double getSH_WorkDaysOfWeek(
			String FA_NO,
			String SH_NO,
			String PROCID,
			int Work_Week,
			Connection conn,
			double...WORK_DAYS) {
		double iRet=0;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			
			int Work_Week_Start=Work_Week;
			
			strSQL="select distinct PROC_SEQ  "+
                   "  from fcps22_1 "+
                   " where sh_aritcle = '"+SH_NO+"' "+
                   "   and proc_seq < (select proc_seq "+
                   "                     from fcps22_1 "+
                   "                    where sh_aritcle = '"+SH_NO+"' "+
                   "                      and pb_ptno = '"+PROCID+"') "+
                   "   and proc_seq >= (select min(proc_seq) "+
                   "                      from fcps22_1 "+
                   "                     where sh_aritcle = '"+SH_NO+"') ";
			
			strSQL="select count(*) iCount from ("+strSQL+")";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {
		    	Work_Week_Start=FCMPS_PUBLIC.getPrevious_Week(Work_Week, rs.getInt("iCount"));
		    }
			rs.close();
			pstmtData.close();

			strSQL="select DISTINCT PROC_SEQ "+
                   "  from fcps22_1 "+
                   " where sh_aritcle = '"+SH_NO+"' and PROC_SEQ is not null order by PROC_SEQ ";
	
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {
		    	int iWork_Week=Work_Week_Start;
		    	do {				
		    		
		    		int iwk=0;
					do {
			    		double workdays=getSys_WorkDaysOfWeek(FA_NO,iWork_Week,conn);
			    		if(workdays==0) {
			    			iWork_Week=FCMPS_PUBLIC.getNext_Week(iWork_Week, 1);
			    		}else {
			    			break;
			    		}
			    		if(iwk==4)break;
			    		iwk++;
					}while(true);
					
					double work_days=getSH_WorkDaysOfWeek(FA_NO,SH_NO,iWork_Week,conn,WORK_DAYS);
					if(iRet==0) iRet=work_days;
					if(work_days<iRet)iRet=work_days;	
					
					iWork_Week=FCMPS_PUBLIC.getNext_Week(iWork_Week, 1);
					
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
	
	/**
	 * 取得系統當周的排產天數
	 * @param FA_NO
	 * @param Work_Week
	 * @param conn
	 * @return
	 */
	public static double getSys_WorkDaysOfWeek(String FA_NO,int Work_Week,Connection conn) {
		double iRet=0;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
				
		    //系統行事歷設定的天數
			
			strSQL="select count(*) Work_Week_days  from FCMPS023 " +
				   "where TO_CHAR(SCHEDULE_DAY,'IYIW')='"+Work_Week+"'" +
		           "  and IS_ACTIVE='Y' " +
		           "  and FA_NO='"+FA_NO+"' ";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {
		    	if(rs.getDouble("Work_Week_days")>0) iRet=rs.getDouble("Work_Week_days");
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
		return iRet;
	}
		
    /**
     * 型體後關制程的排產量
     * @param FA_NO
     * @param PROC_SEQ
     * @param PROCID
     * @param SH_NO
     * @param WORK_WEEK
     * @return
     */
	public static Double getSH_OtherWeek_Plan_QTY(
			String FA_NO,
			double PROC_SEQ,
			String PROCID,
			String SH_NO,
			int WORK_WEEK,
			Connection conn) {
		Double iRet=null;
		String strSQL="";
//		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		try{

			//相同順序的制程有沒有排產
			strSQL="select PROC_SEQ,PB_PTNO from FCPS22_1 " +
				   "where SH_ARITCLE='"+SH_NO+"' " +
				   "  and NEED_PLAN='Y' " +
				   "  and PROC_SEQ="+PROC_SEQ+" " +
				   "  and PB_PTNO<>'"+PROCID+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	
				strSQL="select sum(WORK_PLAN_QTY) WORK_PLAN_QTY from FCMPS007,FCMPS006 " +
				   "where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO " +
				   "  and FCMPS006.WORK_WEEK="+WORK_WEEK+
				   "  and FCMPS006.IS_SURE='Y' " +
				   "  and FCMPS007.SH_NO='"+SH_NO+"'"+
				   "  and FCMPS007.PROCID='"+rs.getString("PB_PTNO")+"'"+
				   "  and FCMPS007.PROC_SEQ='"+PROC_SEQ+"'"+
				   "  and FCMPS006.FA_NO='"+FA_NO+"' " ;
			
			    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    rs2=pstmtData2.executeQuery();
			    
			    if(rs2.next()){
			    	if(rs2.getObject("WORK_PLAN_QTY")!=null) iRet=rs2.getDouble("WORK_PLAN_QTY");			    	
			    }
				rs2.close();
				pstmtData2.close();		    	
		    	
		    }
			rs.close();
			pstmtData.close();
					
			if(iRet!=null) return iRet;
			
			strSQL="select distinct PROC_SEQ from FCPS22_1 " +
			   "where SH_ARITCLE='"+SH_NO+"' " +
			   "  and PROC_SEQ>"+PROC_SEQ+" " +
			   "  and PB_PTNO<>'"+PROCID+"'"+
			   "order by PROC_SEQ ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	int Interval=1;
		    	do {
		    		
	    			int iwk=0;
					do {
			    		double workdays=FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getNext_Week(WORK_WEEK, Interval),conn);
			    		if(workdays==0) {
			    			Interval++;
			    		}else {
			    			break;
			    		}
			    		if(iwk==4)break;
			    		iwk++;
					}while(true);
					
		    		strSQL="select PB_PTNO,nvl(NEED_PLAN,'N') NEED_PLAN from FCPS22_1 "+
				           "where SH_ARITCLE='"+SH_NO+"' " +
				           "  and PROC_SEQ="+rs.getInt("PROC_SEQ")+" " +
				           "  and PB_PTNO<>'"+PROCID+"'";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
		    		if(rs2.next()) {
		    			do {
		    				if(rs2.getString("NEED_PLAN").equals("Y")) {
		    							    					
								strSQL="select sum(WORK_PLAN_QTY) WORK_PLAN_QTY from FCMPS007,FCMPS006 " +
							       "where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO " +
							       "  and FCMPS006.WORK_WEEK="+FCMPS_PUBLIC.getNext_Week(WORK_WEEK, Interval)+
							       "  and FCMPS006.IS_SURE='Y' " +
							       "  and FCMPS007.SH_NO='"+SH_NO+"'"+
							       "  and FCMPS007.PROCID='"+rs2.getString("PB_PTNO")+"'"+
							       "  and FCMPS007.PROC_SEQ='"+rs.getInt("PROC_SEQ")+"'"+
							       "  and FCMPS006.FA_NO='"+FA_NO+"' " ;

							    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
							    rs3=pstmtData3.executeQuery();
							    
							    if(rs3.next()){
							    	if(rs3.getObject("WORK_PLAN_QTY")!=null) iRet=rs3.getDouble("WORK_PLAN_QTY");	
							    }
								rs3.close();
								pstmtData3.close();		
								
								if(iRet!=null) break;			    					
		    				}

		    			}while(rs2.next());
		    		}
		    		rs2.close();
		    		pstmtData2.close();
		    		
		    		if(iRet!=null) break;	
		    		
		    		Interval++;

		    	}while(rs.next());
    			    	
		    }
			rs.close();
			pstmtData.close();
			
			if(iRet!=null) return iRet;
			
			strSQL="select distinct PROC_SEQ from FCPS22_1 " +
			       "where SH_ARITCLE='"+SH_NO+"' " +
			       "  and PROC_SEQ<"+PROC_SEQ+" " +
			       "  and PB_PTNO<>'"+PROCID+"'"+
			       "order by PROC_SEQ DESC";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	int Interval=1;
		    	do {
		    		
					do {
			    		double workdays=FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, Interval),conn);
			    		if(workdays==0) {
			    			Interval++;
			    		}else {
			    			break;
			    		}

					}while(true);
					
		    		strSQL="select PB_PTNO,nvl(NEED_PLAN,'N') NEED_PLAN from FCPS22_1 "+
				           "where SH_ARITCLE='"+SH_NO+"' " +
				           "  and PROC_SEQ="+rs.getInt("PROC_SEQ")+" " +
				           "  and PB_PTNO<>'"+PROCID+"'";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
		    		if(rs2.next()) {
		    			do {
		    				if(rs2.getString("NEED_PLAN").equals("Y")) {
		    													
								strSQL="select sum(WORK_PLAN_QTY) WORK_PLAN_QTY from FCMPS007,FCMPS006 " +
							       "where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO " +
							       "  and FCMPS006.WORK_WEEK="+FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, Interval)+
							       "  and FCMPS006.IS_SURE='Y' " +
							       "  and FCMPS007.SH_NO='"+SH_NO+"'"+
							       "  and FCMPS007.PROCID='"+rs2.getString("PB_PTNO")+"'"+
							       "  and FCMPS007.PROC_SEQ='"+rs.getInt("PROC_SEQ")+"'"+
							       "  and FCMPS006.FA_NO='"+FA_NO+"' " ;

							    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
							    rs3=pstmtData3.executeQuery();
							    
							    if(rs3.next()){
							    	if(rs3.getObject("WORK_PLAN_QTY")!=null) iRet=rs3.getDouble("WORK_PLAN_QTY");	
							    }
								rs3.close();
								pstmtData3.close();		
								
								if(iRet!=null) break;			    					
		    				}

		    			}while(rs2.next());
		    		}
		    		rs2.close();
		    		pstmtData2.close();
		    		
		    		if(iRet!=null) break;	
		    		
		    		Interval++;

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
     * 型體Size後關制程的排產量
     * @param FA_NO
     * @param PROC_SEQ
     * @param PROCID
     * @param SH_NO
     * @param SH_SIZE
     * @param WORK_WEEK
     * @return
     */
	public static Double getSH_SIZE_OtherWeek_Plan_QTY(
			String FA_NO,
			double PROC_SEQ,
			String PROCID,
			String SH_NO,
			String SH_SIZE,
			int WORK_WEEK,
			Connection conn) {
		Double iRet=null;
		String strSQL="";
//		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		try{

			//相同順序的制程有沒有排產
			strSQL="select PROC_SEQ,PB_PTNO from FCPS22_1 " +
				   "where SH_ARITCLE='"+SH_NO+"' " +
				   "  and NEED_PLAN='Y' " +
				   "  and PROC_SEQ="+PROC_SEQ+" " +
				   "  and PB_PTNO<>'"+PROCID+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	

		    	PROCID=rs.getString("PB_PTNO");
		    	
				strSQL="select sum(WORK_PLAN_QTY) WORK_PLAN_QTY from FCMPS007,FCMPS006 " +
				   "where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO " +
				   "  and FCMPS006.WORK_WEEK="+WORK_WEEK+
				   "  and FCMPS006.IS_SURE='Y' " +
				   "  and FCMPS007.SH_NO='"+SH_NO+"'"+
				   "  and FCMPS007.SH_SIZE='"+SH_SIZE+"'"+
				   "  and FCMPS007.PROCID='"+PROCID+"'"+
				   "  and FCMPS007.PROC_SEQ='"+PROC_SEQ+"'"+
				   "  and FCMPS006.FA_NO='"+FA_NO+"' " ;
			
			    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    rs2=pstmtData2.executeQuery();
			    
			    if(rs2.next()){
			    	if(rs2.getObject("WORK_PLAN_QTY")!=null) iRet=rs2.getDouble("WORK_PLAN_QTY");			    	
			    }
				rs2.close();
				pstmtData2.close();		    	
		    	
		    }
			rs.close();
			pstmtData.close();
					
			if(iRet!=null) return iRet;
			
			strSQL="select distinct PROC_SEQ from FCPS22_1 " +
			   "where SH_ARITCLE='"+SH_NO+"' " +
			   "  and PROC_SEQ>"+PROC_SEQ+" " +
			   "  and PB_PTNO<>'"+PROCID+"'"+
			   "order by PROC_SEQ ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	int Interval=1;
		    	do {
		    		
		    		strSQL="select PB_PTNO,nvl(NEED_PLAN,'N') NEED_PLAN from FCPS22_1 "+
				           "where SH_ARITCLE='"+SH_NO+"' " +
				           "  and PROC_SEQ="+rs.getInt("PROC_SEQ")+" " +
				           "  and PB_PTNO<>'"+PROCID+"'";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
		    		if(rs2.next()) {
		    			do {
		    				if(rs2.getString("NEED_PLAN").equals("Y")) {
								strSQL="select sum(WORK_PLAN_QTY) WORK_PLAN_QTY from FCMPS007,FCMPS006 " +
							       "where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO " +
							       "  and FCMPS006.WORK_WEEK="+FCMPS_PUBLIC.getNext_Week(WORK_WEEK, Interval)+
							       "  and FCMPS006.IS_SURE='Y' " +
							       "  and FCMPS007.SH_NO='"+SH_NO+"'"+
							       "  and FCMPS007.SH_SIZE='"+SH_SIZE+"'"+
							       "  and FCMPS007.PROCID='"+rs2.getString("PB_PTNO")+"'"+
							       "  and FCMPS007.PROC_SEQ='"+rs.getInt("PROC_SEQ")+"'"+
							       "  and FCMPS006.FA_NO='"+FA_NO+"' " ;

							    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
							    rs3=pstmtData3.executeQuery();
							    
							    if(rs3.next()){
							    	if(rs3.getObject("WORK_PLAN_QTY")!=null) iRet=rs3.getDouble("WORK_PLAN_QTY");	
							    }
								rs3.close();
								pstmtData3.close();		
								
								if(iRet!=null) break;			    					
		    				}

		    			}while(rs2.next());
		    		}
		    		rs2.close();
		    		pstmtData2.close();
		    		
		    		if(iRet!=null) break;	
		    		
		    		Interval++;

		    	}while(rs.next());
    			    	
		    }
			rs.close();
			pstmtData.close();
			
			if(iRet!=null) return iRet;
			
			strSQL="select distinct PROC_SEQ from FCPS22_1 " +
			       "where SH_ARITCLE='"+SH_NO+"' " +
			       "  and PROC_SEQ<"+PROC_SEQ+" " +
			       "  and PB_PTNO<>'"+PROCID+"'"+
			       "order by PROC_SEQ DESC";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	int Interval=1;
		    	do {
		    		
		    		strSQL="select PB_PTNO,nvl(NEED_PLAN,'N') NEED_PLAN from FCPS22_1 "+
				           "where SH_ARITCLE='"+SH_NO+"' " +
				           "  and PROC_SEQ="+rs.getInt("PROC_SEQ")+" " +
				           "  and PB_PTNO<>'"+PROCID+"'";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
		    		if(rs2.next()) {
		    			do {
		    				if(rs2.getString("NEED_PLAN").equals("Y")) {
								strSQL="select sum(WORK_PLAN_QTY) WORK_PLAN_QTY from FCMPS007,FCMPS006 " +
							       "where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO " +
							       "  and FCMPS006.WORK_WEEK="+FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, Interval)+
							       "  and FCMPS006.IS_SURE='Y' " +
							       "  and FCMPS007.SH_NO='"+SH_NO+"'"+
							       "  and FCMPS007.SH_SIZE='"+SH_SIZE+"'"+
							       "  and FCMPS007.PROCID='"+rs2.getString("PB_PTNO")+"'"+
							       "  and FCMPS007.PROC_SEQ='"+rs.getInt("PROC_SEQ")+"'"+
							       "  and FCMPS006.FA_NO='"+FA_NO+"' " ;

							    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
							    rs3=pstmtData3.executeQuery();
							    
							    if(rs3.next()){
							    	if(rs3.getObject("WORK_PLAN_QTY")!=null) iRet=rs3.getDouble("WORK_PLAN_QTY");	
							    }
								rs3.close();
								pstmtData3.close();		
								
								if(iRet!=null) break;			    					
		    				}

		    			}while(rs2.next());
		    		}
		    		rs2.close();
		    		pstmtData2.close();
		    		
		    		if(iRet!=null) break;	
		    		
		    		Interval++;

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
	 * 取型體需要的制程順序
	 * @param SH_NO
	 * @return
	 */
	public static double getPROC_SEQ(String SH_NO,String PROCID,Connection conn) {
		double iRet=-1;
		String strSQL="";
//		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select PROC_SEQ from fcps22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+PROCID+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(rs.getObject("PROC_SEQ")!=null) iRet=rs.getDouble("PROC_SEQ");
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
     * 兩個不同的型體從一個制程到另一個制程,是否需要相同的周數
     * @param SH_NO
     * @param Other_SH_NO
     * @param from_PROCID
     * @param to_PROCID
     * @return
     */
	public static boolean is_Same_PROC_Weeks(String SH_NO,String Other_SH_NO,String from_PROCID,String to_PROCID,Connection conn) {
		boolean iRet=false;
		String strSQL="";
//		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			int iWeeks1=-1;
			int iWeeks2=-1;
			
			strSQL="select FCPS22_1.PB_PTNO," +
		           "       FCPS22_1.PROC_SEQ," +
		           "       FCPS22_2.PB_PTNA," +
		           "       nvl(FCPS22_1.NEED_PLAN,'N') NEED_PLAN " +
		           "from FCPS22_1,FCPS22_2 " +
                   "where FCPS22_1.PB_PTNO=FCPS22_2.PB_PTNO" +
                   "  and FCPS22_1.SH_ARITCLE='"+SH_NO+"' " +
                   "  and FCPS22_1.PROC_SEQ<=(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+from_PROCID+"') "+
                   "  and exists(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+to_PROCID+"')"+
                   "  and FCPS22_1.PB_PTNO<>'"+from_PROCID+"' "+
                   "ORDER BY PROC_SEQ DESC";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		
		    	iWeeks1=0;
		    	double PROC_SEQ=rs.getDouble("PROC_SEQ");
		    	do {
		    		if(to_PROCID.equals(rs.getString("PB_PTNO")))break;
		    		
					if(PROC_SEQ!=rs.getDouble("PROC_SEQ")) {
						iWeeks1++;
						PROC_SEQ=rs.getDouble("PROC_SEQ");
					}					
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();

			strSQL="select FCPS22_1.PB_PTNO," +
	               "       FCPS22_1.PROC_SEQ," +
	               "       FCPS22_2.PB_PTNA," +
	               "       nvl(FCPS22_1.NEED_PLAN,'N') NEED_PLAN " +
	               "from FCPS22_1,FCPS22_2 " +
                   "where FCPS22_1.PB_PTNO=FCPS22_2.PB_PTNO" +
                   "  and FCPS22_1.SH_ARITCLE='"+Other_SH_NO+"' " +
                   "  and FCPS22_1.PROC_SEQ<=(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+Other_SH_NO+"' and PB_PTNO='"+from_PROCID+"') "+
                   "  and exists(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+Other_SH_NO+"' and PB_PTNO='"+to_PROCID+"')"+
                   "  and FCPS22_1.PB_PTNO<>'"+from_PROCID+"' "+
                   "ORDER BY PROC_SEQ DESC";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		
		    	iWeeks2=0;
		    	double PROC_SEQ=rs.getDouble("PROC_SEQ");
		    	do {
		    		if(to_PROCID.equals(rs.getString("PB_PTNO")))break;
					if(PROC_SEQ!=rs.getDouble("PROC_SEQ")) {
						iWeeks2++;
						PROC_SEQ=rs.getDouble("PROC_SEQ");
					}					 			
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();			
			
			if(iWeeks2==iWeeks1 && iWeeks2!=-1 && iWeeks1!=-1) iRet=true;
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	/**
	 * 型體的後關制程的計劃是否已排定
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	public static boolean isPlanned_Next_PROC_SEQ(String FA_NO,String SH_NO,String PROCID,int WORK_WEEK,Connection conn) {
		boolean iRet=false;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		try{
			
			//取此制程的後關制程
			strSQL="select distinct PROC_SEQ from FCPS22_1 " +
			       "where SH_ARITCLE='"+SH_NO+"' " +
			       "  and PROC_SEQ>(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+PROCID+"') "+
			       "ORDER BY PROC_SEQ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	int Week_Interval=1;
		    	do {
		    		
	    			int iwk=0;
					do {
			    		double workdays=FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getNext_Week(WORK_WEEK, Week_Interval),conn);
			    		if(workdays==0) {
			    			Week_Interval++;
			    		}else {
			    			break;
			    		}
			    		if(iwk==4)break;
			    		iwk++;
					}while(true);
		    		
					strSQL="select PB_PTNO,PROC_SEQ,NEED_PLAN from FCPS22_1 " +
	                       "where SH_ARITCLE='"+SH_NO+"' " +
	                       "  and PROC_SEQ="+rs.getDouble("PROC_SEQ")+
	                       "  and NEED_PLAN='Y' ";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){
				    	do {
							strSQL="select plan_no from FCMPS006 " +
						           "where IS_SURE='Y' " +
						           "  and WORK_WEEK="+FCMPS_PUBLIC.getNext_Week(WORK_WEEK, Week_Interval)+" " +
						           "  and FA_NO='"+FA_NO+"'"+
							       "  and PROCID='"+rs2.getString("PB_PTNO")+"'";
						   
						    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
						    rs3=pstmtData3.executeQuery();
						    
						    if(rs3.next()){
						    	iRet=true;
						    }
							rs3.close();
							pstmtData3.close();		
							
							if(iRet) break;
							
				    	}while(rs2.next());
				    				    	
				    }
		    		rs2.close();
		    		pstmtData2.close();
		    		
		    		if(iRet) break;
		    		
		    		Week_Interval++;
		    		
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();	
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	System.out.println(strSQL);
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	/**
	 * 型體的前關制程的計劃是否已排定
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	public static boolean isPlanned_Prev_PROC_SEQ(String FA_NO,String SH_NO,String PROCID,int WORK_WEEK,Connection conn) {
		boolean iRet=false;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		try{
			//取此制程的前關制程
			strSQL="select distinct PROC_SEQ from FCPS22_1 " +
			       "where SH_ARITCLE='"+SH_NO+"' " +
			       "  and PROC_SEQ<(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+PROCID+"') "+
			       "ORDER BY PROC_SEQ DESC";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()){
		    	int Week_Interval=1;
		    	do {

					do {
						double workdays=FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, Week_Interval),conn);
						if(workdays==0) {
							Week_Interval++;
						}else {
							break;
						}
					}while(true);
		    		
					strSQL="select PB_PTNO,PROC_SEQ,NEED_PLAN from FCPS22_1 " +
	                       "where SH_ARITCLE='"+SH_NO+"' " +
	                       "  and PROC_SEQ="+rs.getDouble("PROC_SEQ")+
	                       "  and NEED_PLAN='Y' ";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){
				    	do {
							strSQL="select plan_no from FCMPS006 " +
						           "where IS_SURE='Y' " +
						           "  and WORK_WEEK="+FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, Week_Interval)+" " +
						           "  and FA_NO='"+FA_NO+"'"+
						           "  and PROCID='"+rs2.getString("PB_PTNO")+"'";
						   
						    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
						    rs3=pstmtData3.executeQuery();
						    
						    if(rs3.next()){
						    	iRet=true;
						    }
							rs3.close();
							pstmtData3.close();		
							
							if(iRet) break;
							
							strSQL="SELECT WORK_CAP_QTY FROM FCMPS008 " +
									"WHERE WORK_WEEK="+FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, Week_Interval)+" "+
									"  AND PROCID='"+rs2.getString("PB_PTNO")+"'"+
									"  AND FA_NO='"+FA_NO+"'"+
									"  AND WORK_CAP_QTY=0";
							   
						    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
						    rs3=pstmtData3.executeQuery();
						    
						    if(rs3.next()){
						    	iRet=true;
						    }
							rs3.close();
							pstmtData3.close();		
							
							if(iRet) break;
							
				    	}while(rs2.next());				    	
			    	
				    }
		    		rs2.close();
		    		pstmtData2.close();
		    		
		    		if(iRet) break;
		    		
		    		//周次已小於系統啟用周次
		    		if(FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, Week_Interval)<FCMPS_PUBLIC.SYS_BEGIN_PLAN_WEEK) {
		    			iRet=true;
		    			break;
		    		}
		    		
		    		Week_Interval++;
		    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();		    
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	System.out.println(strSQL);
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	/**
	 * 型體的相同制程的計劃是否已排定
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	public static boolean isPlanned_Same_PROC_SEQ(String FA_NO,String SH_NO,String PROCID,int WORK_WEEK,Connection conn) {
		boolean iRet=false;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select plan_no from FCMPS006 " +
				   "where IS_SURE='Y' " +
				   "  and WORK_WEEK="+WORK_WEEK+" " +
				   "  and FA_NO='"+FA_NO+"' " +
				   "  and PROCID in ("+
			       "select PB_PTNO from fcps22_1 " +
				   "where SH_ARITCLE='"+SH_NO+"'"+
				   "  and PB_PTNO<>'"+PROCID+"'"+
				   "  and nvl(NEED_PLAN,'N')='Y' "+
				   "  and PROC_SEQ=(select max(PROC_SEQ) from FCPS22_1 " +
				   "                where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+PROCID+"' and nvl(NEED_PLAN,'N')='Y')) ";
				   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=true;
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
	 * 型體制程的計劃是否已排定
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	public static boolean isPlanned_PROC_SEQ(String FA_NO,String SH_NO,String SH_COLOR,String PROCID,int WORK_WEEK,Connection conn) {
		boolean iRet=false;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select distinct FCMPS006.plan_no from FCMPS006,FCMPS007 " +
				   "where FCMPS006.PLAN_NO=FCMPS007.PLAN_NO" +
				   "  and FCMPS006.IS_SURE='Y' " +
				   "  and FCMPS006.FA_NO='"+FA_NO+"' " +
				   "  and FCMPS006.WORK_WEEK="+WORK_WEEK+" " +
				   "  and FCMPS006.PROCID='"+PROCID+"'"+
				   "  and FCMPS007.SH_NO='"+SH_NO+"'"+
				   "  and FCMPS007.SH_COLOR='"+SH_COLOR+"'";
				   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=true;
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
	 * 型體制程的計劃是否已排定
	 * @param SH_NO
	 * @param SH_COLOR
	 * @param OD_PONO1
	 * @param PROCID
	 * @return
	 */
	public static boolean isPlanned_PROC_SEQ(String FA_NO,String SH_NO,String SH_COLOR,String OD_PONO1,String PROCID,int WORK_WEEK,Connection conn) {
		boolean iRet=false;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select distinct FCMPS006.plan_no from FCMPS006,FCMPS007 " +
				   "where FCMPS006.PLAN_NO=FCMPS007.PLAN_NO" +
				   "  and FCMPS006.IS_SURE='Y' " +
				   "  and FCMPS006.FA_NO='"+FA_NO+"' " +
				   "  and FCMPS006.WORK_WEEK="+WORK_WEEK+" " +
				   "  and FCMPS006.PROCID='"+PROCID+"'"+
				   "  and FCMPS007.OD_PONO1='"+OD_PONO1+"'"+
				   "  and FCMPS007.SH_COLOR='"+SH_COLOR+"'"+				   
				   "  and FCMPS007.SH_NO='"+SH_NO+"'";
				   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=true;
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
	 * 取當前制程到下一個需要排計劃的制程的間隔周數
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	public static int getPROC_Interval(String SH_NO,String PROCID,Connection conn) {
		int iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		try{

			strSQL="select distinct PROC_SEQ from fcps22_1 " +
				   "where SH_ARITCLE='"+SH_NO+"'"+
				   "  and PROC_SEQ>(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+PROCID+"') "+
				   "order by PROC_SEQ ASC ";
				   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		iRet++;
					strSQL="select NEED_PLAN from fcps22_1 " +
					       "where SH_ARITCLE='"+SH_NO+"'"+
					       "  and PROC_SEQ="+rs.getDouble("PROC_SEQ")+
					       "  and NEED_PLAN='Y' ";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){
						rs2.close();
						pstmtData2.close();
				    	break;
				    }
					rs2.close();
					pstmtData2.close();

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
	 * 取當前制程到下一個需要排計劃的制程的間隔周數
	 * @param SH_NO     型體
	 * @param frmPROCID 起始制程
	 * @param toPROCID  終止制程
	 * @param conn      資料連線
	 * @return
	 */
	public static int getPROC_Interval(String SH_NO,String frmPROCID,String toPROCID,Connection conn) {
		int iRet=1;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select distinct PROC_SEQ from fcps22_1 " +
			       "where SH_ARITCLE='"+SH_NO+"'"+
			       "  and PROC_SEQ>(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+frmPROCID+"') "+
			       "  and PROC_SEQ<(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+toPROCID+"') ";
				   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		iRet++;

		    	}while(rs.next());	
		    }
			rs.close();
			pstmtData.close();

			if(iRet>1) return iRet;
			
			strSQL="select distinct PROC_SEQ from fcps22_1 " +
		           "where SH_ARITCLE='"+SH_NO+"'"+
		           "  and PROC_SEQ>(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+toPROCID+"') "+
		           "  and PROC_SEQ<(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+frmPROCID+"') ";
			   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet++;		    	
		    	do {
		    		iRet++;

		    	}while(rs.next());	
		    }
			rs.close();
			pstmtData.close();
		
			if(iRet<0) return iRet;
			
			strSQL="select distinct PROC_SEQ from fcps22_1 " +
		       "where SH_ARITCLE='"+SH_NO+"'"+
		       "  and PROC_SEQ=(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+frmPROCID+"') "+
		       "  and PROC_SEQ=(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+toPROCID+"') ";
			   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=0;
		    }
			rs.close();
			pstmtData.close();
			
			strSQL="select PROC_SEQ from fcps22_1 " +
		           "where SH_ARITCLE='"+SH_NO+"'"+
		           "  and PB_PTNO='"+toPROCID+"'";
			   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(!rs.next()){
		    	iRet=0;
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
	 * 取當前制程到前一個需要排計劃的制程的間隔周數
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	public static int getPROC_Prev_Interval(String SH_NO,String PROCID,Connection conn) {
		int iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		try{

			strSQL="select distinct PROC_SEQ from fcps22_1 " +
				   "where SH_ARITCLE='"+SH_NO+"'"+
				   "  and PROC_SEQ<(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+PROCID+"') "+
				   "order by PROC_SEQ ASC ";
				   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		iRet++;
					strSQL="select NEED_PLAN from fcps22_1 " +
				           "where SH_ARITCLE='"+SH_NO+"'"+
				           "  and PROC_SEQ="+rs.getDouble("PROC_SEQ")+
				           "  and NEED_PLAN='Y' ";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){
						rs2.close();
						pstmtData2.close();
				    	break;
				    }
					rs2.close();
					pstmtData2.close();
				
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
	 * 取得型體的制造途程,且由當前制程和周次,計算出每個制程需在哪周排產
	 * @param SH_NO  型體
	 * @param Current_PROCID 當前制程
	 * @param Current_Work_Week 當前周次
	 * @param conn
	 * @return Map<String,Integer> 一個由制程代號和排產周次組成的列表
	 */
	public static Map<String,Integer> getSH_Produce_Way(
			String SH_NO,
			String Current_PROCID,
			int Current_Work_Week,
			Connection conn){
		Map<String,Integer> list=new HashMap<String,Integer>();
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		try{

			strSQL="select distinct PROC_SEQ from fcps22_1 " +
				   "where SH_ARITCLE='"+SH_NO+"'"+
				   "  and PROC_SEQ<(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+Current_PROCID+"') "+
				   "order by PROC_SEQ DESC ";
				   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	int interval_Week=0;
		    	do {
		    		interval_Week++;
					strSQL="select PB_PTNO from fcps22_1 " +
					       "where SH_ARITCLE='"+SH_NO+"'"+
					       "  and PROC_SEQ="+rs.getDouble("PROC_SEQ")+
					       "  and NEED_PLAN='Y' ";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){
				    	do {
				    		list.put(rs2.getString("PB_PTNO"), FCMPS_PUBLIC.getPrevious_Week(Current_Work_Week, interval_Week));
				    	}while(rs2.next());
				    }
					rs2.close();
					pstmtData2.close();

		    	}while(rs.next());		    	
		    }
			rs.close();
			pstmtData.close();
			
			strSQL="select distinct PROC_SEQ from fcps22_1 " +
			       "where SH_ARITCLE='"+SH_NO+"'"+
			       "  and PROC_SEQ=(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+Current_PROCID+"') "+
			       "order by PROC_SEQ ";
			   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
					strSQL="select PB_PTNO from fcps22_1 " +
					       "where SH_ARITCLE='"+SH_NO+"'"+
					       "  and PROC_SEQ="+rs.getDouble("PROC_SEQ")+
					       "  and NEED_PLAN='Y' ";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){
				    	do {
				    		list.put(rs2.getString("PB_PTNO"),Current_Work_Week);
				    	}while(rs2.next());
				    }
					rs2.close();
					pstmtData2.close();

		    	}while(rs.next());		    	
		    }
			rs.close();
			pstmtData.close();
		
			strSQL="select distinct PROC_SEQ from fcps22_1 " +
		       "where SH_ARITCLE='"+SH_NO+"'"+
		       "  and PROC_SEQ>(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+Current_PROCID+"') "+
		       "order by PROC_SEQ ";
		   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	int interval_Week=0;
		    	do {
		    		interval_Week++;
					strSQL="select PB_PTNO from fcps22_1 " +
					       "where SH_ARITCLE='"+SH_NO+"'"+
					       "  and PROC_SEQ="+rs.getDouble("PROC_SEQ")+
					       "  and NEED_PLAN='Y' ";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){
				    	do {
				    		list.put(rs2.getString("PB_PTNO"), FCMPS_PUBLIC.getNext_Week(Current_Work_Week, interval_Week));
				    	}while(rs2.next());
				    }
					rs2.close();
					pstmtData2.close();

		    	}while(rs.next());		    	
		    }
			rs.close();
			pstmtData.close();			

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}	
		return list;
	}
	
	/**
	 * 取共模主型體size上周排了多少數量
	 * @param FA_NO
	 * @param SHARE_SH_NO
	 * @param SHARE_SIZE
	 * @param PROCID
	 * @param SHOOT_WORK_WEEK
	 * @param conn
	 * @return
	 */
	public static double getShare_SH_Prev_Week_Plan_QTY(
			String FA_NO,
			String SHARE_SH_NO,
			String SHARE_SIZE,
			String PROCID,
			int SHOOT_WORK_WEEK,
			Connection conn) {
		double iRet=0;
		
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try {
			strSQL="select sum(WORK_PLAN_QTY) WORK_PLAN_QTY " +
				   "from FCMPS007,FCMPS006 " +
				   "where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO " +
				   "  and FCMPS006.IS_SURE='Y'"+
				   "  and FCMPS006.FA_NO='"+FA_NO+"'"+
				   "  and FCMPS006.PROCID='"+PROCID+"'"+
				   "  and ((FCMPS007.SHARE_SH_NO='"+SHARE_SH_NO+"' and FCMPS007.SHARE_SIZE='"+SHARE_SIZE+"') OR  (FCMPS007.SH_NO='"+SHARE_SH_NO+"' and FCMPS007.SH_SIZE='"+SHARE_SIZE+"')) " +
				   "  and FCMPS007.SHOOT_WORK_WEEK="+SHOOT_WORK_WEEK;
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=getDouble(rs.getDouble("WORK_PLAN_QTY"));
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
	 * 型體的射出周次
	 * @param FA_NO
	 * @param SH_NO
	 * @param PROCID
	 * @param WORK_WEEK
	 * @param conn
	 * @return
	 */
	public static int getSHOOT_WORK_WEEK(
			String FA_NO,
			String SH_NO,
			String PROCID,
			int WORK_WEEK,
			Connection conn) {
		int iRet=WORK_WEEK;
		
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try {
			strSQL="select distinct PROC_SEQ  "+
            "  from fcps22_1 "+
            " where sh_aritcle = '"+SH_NO+"' "+
            "   and proc_seq < (select proc_seq "+
            "                     from fcps22_1 "+
            "                    where sh_aritcle = '"+SH_NO+"' "+
            "                      and pb_ptno = '"+PROCID+"') "+
            "   and proc_seq >= (select min(proc_seq) "+
            "                      from fcps22_1 "+
            "                     where sh_aritcle = '"+SH_NO+"') "+
            "order by PROC_SEQ DESC";
								
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {
		    	int Work_Week_Start=WORK_WEEK;
		    	do {
		    		
		    		Work_Week_Start=FCMPS_PUBLIC.getPrevious_Week(Work_Week_Start, 1);
					do {
			    		double workdays=getSys_WorkDaysOfWeek(FA_NO,Work_Week_Start,conn);
			    		if(workdays==0) {
			    			Work_Week_Start=FCMPS_PUBLIC.getPrevious_Week(Work_Week_Start, 1);
			    		}else {
			    			break;
			    		}
			    		
					}while(true);

				    
		    	}while(rs.next());
		    	iRet=Work_Week_Start;
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
	
	
	
	
	public static String getFCMPS002_SQL(int WORK_WEEK,String conditions) {
		String iRet="";
		iRet="SELECT A.fa_no, A.style_no, A.sh_no, A.sh_size,A.part_no,C.part_na, A.md_per_qty, " +
	         "       A.md_ie,A.md_calc_cap,A.md_num-nvl(B.md_num,0) md_num "+
             "FROM  "+
             "(select fa_no, style_no, sh_no, sh_size, part_no, md_per_qty, md_ie,md_calc_cap,SUM(md_num) md_num  "+
             " from fcmps0021 where effective_week<="+WORK_WEEK+" and MD_IO='I' and MD_CFM='Y' "+ conditions+" "+
             " group by fa_no, style_no, sh_no, sh_size, part_no, md_per_qty, md_ie,md_calc_cap) A, "+
             "(select fa_no, style_no, sh_no, sh_size, part_no, md_per_qty, md_ie,md_calc_cap,SUM(md_num) md_num  "+
             " from fcmps0021 where effective_week<="+WORK_WEEK+" and MD_IO='O' and MD_CFM='Y' "+ conditions+" "+
             " group by fa_no, style_no, sh_no, sh_size, part_no, md_per_qty, md_ie,md_calc_cap) B, "+
             "(select PART_NO,PART_NA from FCSC01 where PART_LNO='A') C "+
             "WHERE A.fa_no=B.fa_no(+)  "+
             "  and A.style_no=B.style_no(+) "+
             "  and A.sh_no=B.sh_no(+) "+
             "  and A.sh_size=B.sh_size(+) "+
             "  and A.part_no=B.part_no(+) "+
             "  and A.md_per_qty=B.md_per_qty(+) "+
             "  and A.md_ie=B.md_ie(+) "+
             "  and A.md_calc_cap=B.md_calc_cap(+) "+
             "  and A.part_no=C.part_no(+) "+
             "  and A.md_num-nvl(B.md_num,0)>0 ";
		return iRet;
	}
	
	public static String getFCMPS002_SQL(String conditions) {
		String iRet="";
		iRet="SELECT A.fa_no, A.style_no, A.sh_no, A.sh_size,A.part_no,C.part_na, A.md_per_qty, " +
	         "       A.md_ie,A.md_calc_cap,A.md_num-nvl(B.md_num,0) md_num "+
             "FROM  "+
             "(select fa_no, style_no, sh_no, sh_size, part_no, md_per_qty, md_ie,md_calc_cap,SUM(md_num) md_num  "+
             " from fcmps0021 where MD_IO='I' and MD_CFM='Y' "+ conditions+" "+
             " group by fa_no, style_no, sh_no, sh_size, part_no, md_per_qty, md_ie,md_calc_cap) A, "+
             "(select fa_no, style_no, sh_no, sh_size, part_no, md_per_qty, md_ie,md_calc_cap,SUM(md_num) md_num  "+
             " from fcmps0021 where MD_IO='O' and MD_CFM='Y' "+ conditions+" "+
             " group by fa_no, style_no, sh_no, sh_size, part_no, md_per_qty, md_ie,md_calc_cap) B, "+
             "(select PART_NO,PART_NA from FCSC01 where PART_LNO='A') C "+
             "WHERE A.fa_no=B.fa_no(+)  "+
             "  and A.style_no=B.style_no(+) "+
             "  and A.sh_no=B.sh_no(+) "+
             "  and A.sh_size=B.sh_size(+) "+
             "  and A.part_no=B.part_no(+) "+
             "  and A.md_per_qty=B.md_per_qty(+) "+
             "  and A.md_ie=B.md_ie(+) "+
             "  and A.md_calc_cap=B.md_calc_cap(+) "+
             "  and A.part_no=C.part_no(+) "+
             "  and A.md_num-nvl(B.md_num,0)>0 ";
		return iRet;
	}
	
    /**
     * 將字串左邊或右邊補特定字符以達到指定的長度,並返回新字串
     * @param str  需要補充的字串
     * @param pstr 補充的字符
     * @param len  長度
     * @param direction 方向,0為左邊,1為右邊
     * @return
     */
    public static String Pad(String str,String pstr,int len,int direction){
    	String iRet=str;
    	
    	while(iRet.length()<len){
    		if(direction==0){
    			iRet=pstr+iRet;
    		}else{
    			iRet=iRet+pstr;
    		}    		
    	}
    	return iRet;
    }
    
	/**
	 * 判斷Object是否為null,是則返回空值,如果為Clob類型,則將其轉化為String返回.
	 * @param o Object
	 * @return String
	 */
    public static String getValue(Object o) {
        if (o == null)
            return "";
        if (o instanceof String) {
            return (String)o;
        } else if (o instanceof Clob) {
        	try {
	        	Clob c = (Clob)o;
	        	return c.getSubString(1, (int)c.length());
        	} catch(Exception e){e.printStackTrace();}
        }
        return o.toString();
	}
    
	/**
	 * 
	 * 判斷Object是否為null,是則返回0,否則返回Double類型本身
	 * @param o Object
	 * @return double
	 */
    public static double getDouble(Object o) {
    	Double iRet=0.0;
        if (o == null)
            return 0;
        if (o instanceof Double) {
        	iRet=(Double)o;
        }else{
        	try{
        		iRet=Double.valueOf(String.valueOf(o).equals("")?"0":String.valueOf(o));
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}        	
        }
        return iRet;
	}
    
	public static Date parseDate(String date){
		Date pDate=null;
	    try{
	    	SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");
	    	pDate=sdf.parse(date);
	    }catch(ParseException pe){}	
	   
	    return pDate;
	}
	
	/**
	 * 
	 * 判斷Object是否為null,是則返回"",否則返回指定格式日期字串
	 * @param o Object 日期型Object
	 * @param dateFormat 日期格式
	 * @return String
	 */
    public static String getDate(Object o,String dateFormat) {
    	String iRet="";
        if (o == null)
            return "";
        SimpleDateFormat sdf=new SimpleDateFormat(dateFormat);
        if (o instanceof Date) {        	
        	iRet=sdf.format((Date)o);
        } 
        if (o instanceof String) {
        	try{        		
        		iRet=sdf.format(parseDate(String.valueOf(o)));
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}        	
        } 
        return iRet;
	}
    
	/**
	 * 因為Cell中存放的只要是數字,則有可能用getStringCellValue方法取不回字串.
	 * 所以才要判斷HSSFCell是用什麼類型存取,並用相應的方法取值.
	 * @param cell
	 * @return
	 */
    public static Object getCellValue(HSSFCell cell){
    	Object iRet=null;
    	if(cell==null){
    		return iRet;
    	}
		if(cell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
			iRet=getDouble(cell.getNumericCellValue());	
		}
		
		if(cell.getCellType()==HSSFCell.CELL_TYPE_STRING){
			iRet=getValue(cell.getStringCellValue());			
		}
		
		if(cell.getCellType()==HSSFCell.CELL_TYPE_FORMULA){
			iRet=getValue(cell.getNumericCellValue());			
		}
		
		return iRet;
    }
    
	/**
	 * 因為Cell中存放的只要是數字,則有可能用getStringCellValue方法取不回字串.
	 * 所以才要判斷HSSFCell是用什麼類型存取,並用相應的方法取值.
	 * @param cell
	 * @param format 如果是數字,0:轉成double 1:轉成int
	 * @return
	 */
    public static Object getCellValue(HSSFCell cell,int format){
    	Object iRet=null;
    	if(cell==null){
    		return iRet;
    	}
		if(cell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
			if(format==0){
				iRet=getDouble(cell.getNumericCellValue());	
			}else if(format==1){
				iRet=(int)cell.getNumericCellValue();	
			}else {
				iRet=(long)cell.getNumericCellValue();	
			}
		}
		
		if(cell.getCellType()==HSSFCell.CELL_TYPE_STRING){
			iRet=getValue(cell.getStringCellValue());			
		}
		
		if(cell.getCellType()==HSSFCell.CELL_TYPE_FORMULA){
			if(format==0){
				iRet=getDouble(cell.getNumericCellValue());	
			}else if(format==1){
				iRet=(int)cell.getNumericCellValue();	
			}else {
				iRet=(long)cell.getNumericCellValue();	
			}
		}
		
		return iRet;
    }
    
	/**
	 * 
	 * 判斷Object是否為null,是則返回0,否則返回Integer類型本身
	 * @param o Object
	 * @return int
	 */
    public static int getInt(Object o) {
    	int iRet=0;
        if (o == null)
            return 0;        
        if (o instanceof Integer) {
        	iRet=(Integer)o;
        } 
        if (o instanceof Double) {
        	double d=(Double)o;
        	iRet=(int)d; 
        }
        if (o instanceof String) {
        	try{
        		if(String.valueOf(o).equals("")) return 0;
        		double d=Double.valueOf(String.valueOf(o));  //如果字串是4.8帶小數位,則Integer.valueOf(String.valueOf(o))會出錯,
        		iRet=(int)d;                                 //所以先轉成double,再強制轉成int
//        		iRet=Integer.valueOf(String.valueOf(o));
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}        	
        } 
        return iRet;
	}
    
	public static String doCheckFieldLength(Object data){
		String iRet="";
		
		Class fConfiguration=data.getClass();
		Field[] fields = fConfiguration.getDeclaredFields();
		try{
			for (Field field : fields) {
				if (field.isAnnotationPresent(Length.class)) {
					Length lg = field.getAnnotation(Length.class);
					int length=lg.max();
					if(getStringBytes(getValue(field.get(data)))>length){
						if (field.isAnnotationPresent(Config.class)) {
							Config ca = field.getAnnotation(Config.class);
							if(ca!=null){
								iRet=ca.key()+" Length than "+String.valueOf(length);
							}									
						}else{
							PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(fConfiguration, field.getName());
							if (pd.getReadMethod().isAnnotationPresent(Config.class)) {
								Config ca = pd.getReadMethod().getAnnotation(Config.class);
								iRet=ca.key()+" Length than "+String.valueOf(length);
							}
						}
					}					
				}else{
					PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(fConfiguration, field.getName());
					if (pd.getReadMethod().isAnnotationPresent(Length.class)) {
						Length lg = pd.getReadMethod().getAnnotation(Length.class);
						int length=lg.max();
						if(getStringBytes(getValue(pd.getReadMethod().invoke(data, null)))>length){
							if (field.isAnnotationPresent(Config.class)) {
								Config ca = field.getAnnotation(Config.class);
								if(ca!=null){
									iRet=ca.key()+" Length than "+String.valueOf(length);
								}									
							}else{
								if (pd.getReadMethod().isAnnotationPresent(Config.class)) {
									Config ca = pd.getReadMethod().getAnnotation(Config.class);
									iRet=ca.key()+" Length than "+String.valueOf(length);
								}
							}
						}																		
					}					
				}
			}				
		}catch(Exception ex){
			ex.printStackTrace();
		}
	
		return iRet;
	}
	
	/**
	 * 取得字串的字節數
	 * @param str
	 * @return
	 */
	public static int getStringBytes(String str){
		int iRet=0;
		byte[] ch = str.getBytes();//字節数组
		iRet=ch.length;
		return iRet;
	}	
	
	/**
	 * 取得廠別正式數據庫連線
	 * @param FA_NO
	 * @return
	 */
	public static Connection getConnectionByCompany(String FA_NO,Connection conn){		
		Connection iRet=null;

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{				

			String strSQL="select CONN_URL,CONN_USER,CONN_PSW from DSPB_DBLINK " +
		                  "where PB_FANO='"+FA_NO+"' ";
	
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	String URL=rs.getString("CONN_URL");
	    		String USER=rs.getString("CONN_USER");
	    		String PSW=rs.getString("CONN_PSW");
	        	Class.forName("oracle.jdbc.driver.OracleDriver"); //加載驅動程序
	    		iRet=DriverManager.getConnection(URL,USER,PSW);    		
		    }
		    rs.close();
		    pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }			

		return iRet;
	}
	
}
