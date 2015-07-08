package fcmps.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FCMPS_CLS_GenerateRccpPlan {
	private String FA_NO=""; //廠別
	private String PLAN_NO=""; //計劃編號
	private Integer WORK_WEEK=0; //計劃周次
	private String PLAN_BY_DATE="A";	
	private double WORK_DAYS=5.0; //周工作天數
	
	private double PER_SIZE_ALLOW_OVER_NUM=1.5; //size允許超出產能多少小時的數量
	
	private String PLAN_PRIORITY_TYPE="1"; //計劃優先方式
	private String PROCID=""; //計劃制程
	private String SH_NO=""; //指定排產的型體
	private String UP_USER="";
	private String config_xml="";
	
	private boolean is_ReGenerateRccpPlan=false; //是否重排計劃
	
	private double WEEK_MAX_CAP_QTY=0;
	
	private boolean is_FORE_PLAN_WEEKS=false;  //如果本周排不滿, 是否將後續訂單往前排.
	private int FORE_PLAN_WEEKS=0;
	
	private Connection conn=null;
	
	private boolean is_UP_TO_CAP=false; //是否已達到最大周產能
	
	private String SH_NOT_IN=""; //需要排除的型體列表

	private int SHOOT_MIN_PRODUCE_QTY=516; //射出的最小排產量
	
	private boolean ONLY_COSTAR_PLAN=false;
	
	//以下記錄變量是為了方便作業畫面實時顯示進度
	private String Current_Calac_Step=""; //當前計算的步驟
	private String Current_Calac_Color=""; //當前步驟計算的配色名稱
	private int Step_Calac_Color_Numbers=0; //當前計算的配色總數
	private int Step_Calac_Color_Seq=0;  //當前計算的配色順序
	
	private FCMPS_RCCP_INFO FCMPS_RCCP_INFO_Var=new FCMPS_RCCP_INFO();
	
	/**
	 * 取廠別
	 * @return
	 */
	public String getFA_NO() {
		return FA_NO;
	}
	
	/**
	 * 設定廠別
	 * @param fa_no
	 */
	public void setFA_NO(String fa_no) {
		FA_NO = fa_no;
	}
	
	/**
	 * 取得計劃編號
	 * @return
	 */
	public String getPLAN_NO() {
		return PLAN_NO;
	}
	
	/**
	 * 設定計劃編號
	 * @param plan_no
	 */
	public void setPLAN_NO(String plan_no) {
		PLAN_NO = plan_no;
	}
	
	/**
	 * 取得工作天數
	 * @return
	 */
	public double getWORK_DAYS() {
		return WORK_DAYS;
	}
	
	/**
	 * 設定工作天數
	 * @param work_days
	 */
	public void setWORK_DAYS(double work_days) {
		WORK_DAYS = work_days;
	}
	
	/**
	 * 取得計劃周次
	 * @return
	 */
	public Integer getWORK_WEEK() {
		return WORK_WEEK;
	}
	
	/**
	 * 設定計劃周次
	 * @param work_week
	 */
	public void setWORK_WEEK(Integer work_week) {
		WORK_WEEK = work_week;
	}

	/**
	 * 取得周計劃依據日期
	 * @return
	 */
	public String getPLAN_BY_DATE() {
		return PLAN_BY_DATE;
	}

	/**
	 * 設定周計劃依據日期
	 * @param plan_by_date
	 */
	public void setPLAN_BY_DATE(String plan_by_date) {
		PLAN_BY_DATE = plan_by_date;
	}

	/**
	 * 取得計劃優先方式
	 * @return
	 */
	public String getPLAN_PRIORITY_TYPE() {
		return PLAN_PRIORITY_TYPE;
	}

	/**
	 * 設定計劃優先方式
	 * @param plan_priority_type
	 */
	public void setPLAN_PRIORITY_TYPE(String plan_priority_type) {
		PLAN_PRIORITY_TYPE = plan_priority_type;
	}
	
	/**
	 * 是否重排周計劃
	 * @return
	 */
    public boolean is_ReGenerateRccpPlan() {
		return is_ReGenerateRccpPlan;
	}

    /**
     * 設定是否重排周計劃
     * @param is_ReGenerateRccpPlan
     */
	public void set_ReGenerateRccpPlan(boolean is_ReGenerateRccpPlan) {
		this.is_ReGenerateRccpPlan = is_ReGenerateRccpPlan;
	}

	/**
	 * 取得周最大產能
	 * @return
	 */
	public double getWEEK_MAX_CAP_QTY() {
		return WEEK_MAX_CAP_QTY;
	}

	/**
	 * 設定周最大產能
	 * @param week_assemble_max_cap_qty
	 */
	public void setWEEK_MAX_CAP_QTY(double week_max_cap_qty) {
		WEEK_MAX_CAP_QTY = week_max_cap_qty;
	}
	
	/**
	 * 取得計劃制程
	 * @return
	 */
	public String getPROCID() {
		return PROCID;
	}

	/**
	 * 設定計劃制程
	 * @param procid
	 */
	public void setPROCID(String procid) {
		PROCID = procid;
	}
	
	/**
	 * 如果本周排不滿, 是否將後續訂單往前排
	 * @return
	 */
	public boolean Is_FORE_PLAN_WEEKS() {
		return is_FORE_PLAN_WEEKS;
	}
	
	/**
	 * 如果本周排不滿, 是否將後續訂單往前排
	 * @param is_FORE_PLAN_WEEKS
	 */

	public void setIs_FORE_PLAN_WEEKS(boolean is_FORE_PLAN_WEEKS) {
		this.is_FORE_PLAN_WEEKS = is_FORE_PLAN_WEEKS;
	}
	
	/**
	 * 如果本周排不滿, 將後面幾周的訂單往前排
	 * @return
	 */

	public int getFORE_PLAN_WEEKS() {
		return FORE_PLAN_WEEKS;
	}
	
	/**
	 * 如果本周排不滿, 將後面幾周的訂單往前排
	 * @param fore_plan_weeks
	 */

	public void setFORE_PLAN_WEEKS(int fore_plan_weeks) {
		FORE_PLAN_WEEKS = fore_plan_weeks;
	}
	
	
	/**
	 * 取得指定排產的型體
	 * @return
	 */
	public String getSH_NO() {
		return SH_NO;
	}

	/**
	 * 指定排產的型體
	 * @param sh_no
	 */
	public void setSH_NO(String sh_no) {
		SH_NO = sh_no;
	}

	public String getUP_USER() {
		return UP_USER;
	}

	public void setUP_USER(String up_user) {
		UP_USER = up_user;
	}

	/**
	 * 設定射出的最小排產量
	 * @param shoot_min_produce_qty
	 */
	public void setSHOOT_MIN_PRODUCE_QTY(int shoot_min_produce_qty) {
		SHOOT_MIN_PRODUCE_QTY = shoot_min_produce_qty;
	}
	
    /**
     * size允許超出產能多少小時的數量
     * @param per_size_allow_over_num
     */
	public void setPER_SIZE_ALLOW_OVER_NUM(double per_size_allow_over_num) {
		PER_SIZE_ALLOW_OVER_NUM = per_size_allow_over_num;
	}

	public void setConfig_xml(String config_xml) {
		this.config_xml = config_xml;
	}

	public Connection getConnection() {
		return conn;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public List<CLS_RCCP_ERROR> getMessage() {
		return FCMPS_RCCP_INFO_Var.getLs_Message();
	}

	private void setMessage(List<CLS_RCCP_ERROR> ls_Message) {
		if(!ls_Message.isEmpty()) {
			for(CLS_RCCP_ERROR msg:ls_Message) {
				FCMPS_RCCP_INFO_Var.getLs_Message().add(msg);
			}
		}		
	}
		
	public boolean isONLY_COSTAR_PLAN() {
		return ONLY_COSTAR_PLAN;
	}

	/**
	 * 只做為配套計劃才排的
	 * @param only_costar_plan
	 */
	public void setONLY_COSTAR_PLAN(boolean only_costar_plan) {
		ONLY_COSTAR_PLAN = only_costar_plan;
	}

	public String getCurrent_Calac_Color() {
		return Current_Calac_Color;
	}

	public String getCurrent_Calac_Step() {
		return Current_Calac_Step;
	}

	public int getStep_Calac_Color_Numbers() {
		return Step_Calac_Color_Numbers;
	}

	public int getStep_Calac_Color_Seq() {
		return Step_Calac_Color_Seq;
	}

	public boolean Is_UP_TO_CAP() {
		return is_UP_TO_CAP;
	}
	
	/**
	 * 不用排產的型體
	 * @param sh_not_in
	 */
	public void setSH_NOT_PLAN(String sh_not_in) {
		if(sh_not_in.trim().equals("")) return;
		String ls_sh_no_plan[]=sh_not_in.split(",");
		if(ls_sh_no_plan.length==0) return;
		for(String sh_no:ls_sh_no_plan) {
			FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().add(sh_no);
		}
	}
	

	public boolean doGeneratePlan() {
		boolean iRet=false;
		Connection conn =getConnection();
		
		if(is_Sure(getPLAN_NO(), conn)) {
			return iRet;
		}
    	
		FCMPS_RCCP_INFO_Var.setFA_NO(getFA_NO());
		FCMPS_RCCP_INFO_Var.setConnection(conn);
		FCMPS_RCCP_INFO_Var.getLs_SH_COLOR_ALLOW_COUNT();
		
    	if(is_ReGenerateRccpPlan()) {
    		if(getSH_NO().equals("")) deletePlan(PLAN_NO,getPROCID(), conn);
    		if(!getSH_NO().equals("")) deletePlan(PLAN_NO,getPROCID(), conn,getSH_NO());
    	}
    	
    	doDeductPlannedProcQty(getPLAN_NO(), getFA_NO(), getPROCID(), conn); 
    	doDeductPlannedSHQty(getPLAN_NO(), getFA_NO(), getPROCID(), conn); 
    	    	
		if(!FCMPS_RCCP_INFO_Var.getLs_SH_WORK_QTY().isEmpty()) {		
			SH_WORK_QTY sh_Work_Qty=null;
			for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_SH_WORK_QTY().size();i++) {
				sh_Work_Qty=FCMPS_RCCP_INFO_Var.getLs_SH_WORK_QTY().get(i);
	    		//不重排計劃時, 記錄已排滿的型體
	    		if(sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY()<=0) {
	    			if(!SH_NOT_IN.equals(""))SH_NOT_IN=SH_NOT_IN+",";
	    			SH_NOT_IN=SH_NOT_IN+"'"+sh_Work_Qty.getSH_NO()+"'";
	    		}				
			}
		}
		
		try {
			
			if(getSH_NO().equals("")) {
				int iResult=doGeneratePlan(
						conn,
						getPROCID(),
						PLAN_NO,
						FA_NO,
						WORK_WEEK);
				
				if(iResult!=-1)iRet=true;
				
			}else {
				int iResult=doGeneratePlan(
						conn,
						getPROCID(),
						PLAN_NO,
						FA_NO,
						WORK_WEEK,
						getSH_NO());

				if(iResult!=-1)iRet=true;
			}
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}
	    
		return iRet;
	}
	
	private boolean initFCMPS010(String FA_NO,String PROCID,Connection conn,String... SH_NO) {
        boolean iRet=false;
		
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		
		try{
		    
			strSQL="update FCMPS010 set EXPECT_PLAN_QTY=0 " +
				   "where OD_QTY-nvl(WORK_PLAN_QTY,0)>0 "+
//				   "  and PROCID='"+PROCID+"'"+
                   "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
                   "  and FCMPS010.IS_DISABLE='N' "+
                   "  and FCMPS010.OD_FGDATE is not null "+ 
//                   "  and STYLE_NO='14672'"+
                   "  and FCMPS010.FA_NO='"+FA_NO+"' ";
			
			if(SH_NO.length>0) {
				strSQL="update FCMPS010 set EXPECT_PLAN_QTY=0 " +
				       "where SH_NO IN (" +SH_NO[0]+")"+
				       "  and OD_QTY-nvl(WORK_PLAN_QTY,0)>0 "+
                       "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
                       "  and FCMPS010.IS_DISABLE='N' "+                       
                       "  and FCMPS010.OD_FGDATE is not null "+ 
                       "  and FCMPS010.FA_NO='"+FA_NO+"' ";				
			}
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    pstmtData.execute();
		    pstmtData.close();
		    
		    iRet=true;
		    
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
	    return iRet;
	}
	
	/**
	 * 生成計劃
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param WORK_WEEK
	 * @param SHIP_STATUS 0 交期已過或是在當前周次 , 1 交期為當前周次之後
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @return -1 出錯, 0 查無資料或是未排滿 , 1 已達到產能或是不可再排
	 */
	private int doGeneratePlan(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int WORK_WEEK,
    		String... SH_NO
    		) {
    	int iRet=0;		
		
		try {

			initFCMPS010(FA_NO,PROCID, conn,SH_NO);
			
			System.out.println(new Date()+" 計算指定周次的訂單");
			
			if(!isONLY_COSTAR_PLAN()) {
				iRet=doGeneratePlan_Order_Spec_Week(
						conn, 
						PROCID, 
						PLAN_NO, 
						FA_NO, 
						WORK_WEEK, 
						SH_NO);

				
				 if(is_UP_TO_CAP) {
				    
					    iRet=doGeneratePlan_Append_Less_Than_516(
					    		conn, 
					    		PROCID, 
					    		PLAN_NO,
					    		FA_NO,
					    		WORK_WEEK,
					    		getFORE_PLAN_WEEKS(),
					    		SHOOT_MIN_PRODUCE_QTY,
					    		SH_NO);	 
					    
					    iRet=doGeneratePlan_Append_Less_Than_516(
					    		conn, 
					    		PROCID, 
					    		PLAN_NO,
					    		FA_NO,
					    		WORK_WEEK,
					    		4,
					    		SHOOT_MIN_PRODUCE_QTY,
					    		SH_NO);
					    
					 return iRet;			 		        		        		    
				 }					
			}				    		    
			    
//============================================================================================================
		    
		    System.out.println(new Date()+" 計算優先排產型體的訂單");
		    
		    if(!isONLY_COSTAR_PLAN()) {
			    iRet=doGeneratePlan_SH_Priority(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO,
			    		FA_NO,
			    		WORK_WEEK,
			    		SH_NO);
			    
			    if(is_UP_TO_CAP) {
				    
				    iRet=doGeneratePlan_Append_Less_Than_516(
				    		conn, 
				    		PROCID, 
				    		PLAN_NO,
				    		FA_NO,
				    		WORK_WEEK,
				    		getFORE_PLAN_WEEKS(),
				    		SHOOT_MIN_PRODUCE_QTY,
				    		SH_NO);	
				    
				    iRet=doGeneratePlan_Append_Less_Than_516(
				    		conn, 
				    		PROCID, 
				    		PLAN_NO,
				    		FA_NO,
				    		WORK_WEEK,
				    		4,
				    		SHOOT_MIN_PRODUCE_QTY,
				    		SH_NO);
				    
			    	return iRet;
			    }
		    }
		    
//			============================================================================================================
			System.out.println(new Date()+" 計算剩餘訂單--已達到最晚完工周次必須要排");

			Current_Calac_Step="已達到最晚完工周次必須要排"; //當前計算的步驟
			Current_Calac_Color=""; //當前步驟計算的配色名稱
			Step_Calac_Color_Numbers=0; //當前計算的配色總數
			Step_Calac_Color_Seq=0;  //當前計算的配色順序
			 
			iRet=doGeneratePlan_Current_In_Work_Week_End(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO, 
			    		FA_NO, 
			    		WORK_WEEK, 
			    		SH_NO);
			    
			if(is_UP_TO_CAP) {			    
			    iRet=doGeneratePlan_Append_Less_Than_516(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO,
			    		FA_NO,
			    		WORK_WEEK,
			    		getFORE_PLAN_WEEKS(),
			    		SHOOT_MIN_PRODUCE_QTY,
			    		SH_NO);	
			    
			    iRet=doGeneratePlan_Append_Less_Than_516(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO,
			    		FA_NO,
			    		WORK_WEEK,
			    		4,
			    		SHOOT_MIN_PRODUCE_QTY,
			    		SH_NO);
			    
				return iRet;		    
			}
		    
//			============================================================================================================
			 System.out.println(new Date()+" 計算剩餘訂單--已達到最晚開工周次必須要排");

			 Current_Calac_Step="已達到最晚開工周次必須要排"; //當前計算的步驟
			 Current_Calac_Color=""; //當前步驟計算的配色名稱
			 Step_Calac_Color_Numbers=0; //當前計算的配色總數
			 Step_Calac_Color_Seq=0;  //當前計算的配色順序
				
			 iRet=doGeneratePlan_Current_In_Work_Week_Start(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO, 
			    		FA_NO, 
			    		WORK_WEEK, 
			    		SH_NO);
			    
			if(is_UP_TO_CAP) {
		    
			    iRet=doGeneratePlan_Append_Less_Than_516(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO,
			    		FA_NO,
			    		WORK_WEEK,
			    		getFORE_PLAN_WEEKS(),
			    		SHOOT_MIN_PRODUCE_QTY,
			    		SH_NO);	
			    
			    iRet=doGeneratePlan_Append_Less_Than_516(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO,
			    		FA_NO,
			    		WORK_WEEK,
			    		4,
			    		SHOOT_MIN_PRODUCE_QTY,
			    		SH_NO);
			    
				return iRet;			 
			}
		
//============================================================================================================

		    System.out.println(new Date()+" 計算剩餘訂單--預排時排入本周的訂單");

			Current_Calac_Step="預排時排入本周的訂單"; //當前計算的步驟
			Current_Calac_Color=""; //當前步驟計算的配色名稱
			Step_Calac_Color_Numbers=0; //當前計算的配色總數
			Step_Calac_Color_Seq=0;  //當前計算的配色順序
		    
		    doGeneratePlan_Current_Week_Order(
		    		conn, 
		    		PROCID, 
		    		PLAN_NO, 
		    		FA_NO, 
		    		WORK_WEEK, 
		    		SH_NO);
		    
		    if(is_UP_TO_CAP) {
			    
			    iRet=doGeneratePlan_Append_Less_Than_516(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO,
			    		FA_NO,
			    		WORK_WEEK,
			    		getFORE_PLAN_WEEKS(),
			    		SHOOT_MIN_PRODUCE_QTY,
			    		SH_NO);	
			    
			    iRet=doGeneratePlan_Append_Less_Than_516(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO,
			    		FA_NO,
			    		WORK_WEEK,
			    		4,
			    		SHOOT_MIN_PRODUCE_QTY,
			    		SH_NO);
			    
		    	return iRet;
		    }
		    
//============================================================================================================

		    if(!Is_FORE_PLAN_WEEKS() || getFORE_PLAN_WEEKS()==0) return iRet;
		    		   		   
		    
//============================================================================================================	    
		    System.out.println(new Date()+" 計算未排滿可提前排訂單--已排入本周的型體配色");
		    
			Current_Calac_Step="訂單提前排--已排入本周的型體配色"; //當前計算的步驟
			Current_Calac_Color=""; //當前步驟計算的配色名稱
			Step_Calac_Color_Numbers=0; //當前計算的配色總數
			Step_Calac_Color_Seq=0;  //當前計算的配色順序
		    
		    iRet=doGeneratePlan_Bring_In_Planned_Color_Size(
		    		conn, 
		    		PROCID, 
		    		PLAN_NO,
		    		FA_NO,
		    		WORK_WEEK,
		    		SH_NO);	        		
		    
		    if(is_UP_TO_CAP) {
			    
			    iRet=doGeneratePlan_Append_Less_Than_516(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO,
			    		FA_NO,
			    		WORK_WEEK,
			    		getFORE_PLAN_WEEKS(),
			    		SHOOT_MIN_PRODUCE_QTY,
			    		SH_NO);	
			    
			    iRet=doGeneratePlan_Append_Less_Than_516(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO,
			    		FA_NO,
			    		WORK_WEEK,
			    		4,
			    		SHOOT_MIN_PRODUCE_QTY,
			    		SH_NO);
			    
		    	return iRet;
		    }
		    
//============================================================================================================
		    
		    System.out.println(new Date()+" 計算未排滿可提前排訂單--已排入本周的同型體不同配色和共模型體,但必須是大於516的型體配色");		    		    			

    		int NEXT_FORE_PLAN_WEEK=WORK_WEEK;

    		for(int iWeek=1;iWeek<getFORE_PLAN_WEEKS();iWeek++) {
    			NEXT_FORE_PLAN_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_FORE_PLAN_WEEK, 1);
        		do {
        			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,NEXT_FORE_PLAN_WEEK,conn)==0) {
        				NEXT_FORE_PLAN_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_FORE_PLAN_WEEK,1);
        			}else {
        				break;
        			}
        			
        		}while(true);
    		}
    		
			Current_Calac_Step=NEXT_FORE_PLAN_WEEK+"周訂單提前排--已排入本周的大於516的同型體不同配色和共模型體配色"; //當前計算的步驟
			Current_Calac_Color=""; //當前步驟計算的配色名稱
			Step_Calac_Color_Numbers=0; //當前計算的配色總數
			Step_Calac_Color_Seq=0;  //當前計算的配色順序
    		
		    iRet=doGeneratePlan_Bring_In_Planned_SH(
		    		conn, 
		    		PROCID, 
		    		PLAN_NO,
		    		FA_NO,
		    		NEXT_FORE_PLAN_WEEK,
		    		SH_NO);	     		    
		    
		    if(is_UP_TO_CAP) {
			    
			    iRet=doGeneratePlan_Append_Less_Than_516(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO,
			    		FA_NO,
			    		WORK_WEEK,
			    		getFORE_PLAN_WEEKS(),
			    		SHOOT_MIN_PRODUCE_QTY,
			    		SH_NO);	
			    
			    iRet=doGeneratePlan_Append_Less_Than_516(
			    		conn, 
			    		PROCID, 
			    		PLAN_NO,
			    		FA_NO,
			    		WORK_WEEK,
			    		4,
			    		SHOOT_MIN_PRODUCE_QTY,
			    		SH_NO);
			    
		    	return iRet;
		    }
	    
//============================================================================================================	    
		    
		    System.out.println(new Date()+" 將工廠產能lead time周次內小於516的型體配色追加進計劃中");		    
		    		    
		    iRet=doGeneratePlan_Append_Less_Than_516(
		    		conn, 
		    		PROCID, 
		    		PLAN_NO,
		    		FA_NO,
		    		WORK_WEEK,
		    		getFORE_PLAN_WEEKS(),
		    		SHOOT_MIN_PRODUCE_QTY,
		    		SH_NO);		    

//============================================================================================================
		    
		    System.out.println(new Date()+" 將後續4周內小於516的型體配色追加進計劃中");		    
		    		    
		    iRet=doGeneratePlan_Append_Less_Than_516(
		    		conn, 
		    		PROCID, 
		    		PLAN_NO,
		    		FA_NO,
		    		WORK_WEEK,
		    		4,
		    		SHOOT_MIN_PRODUCE_QTY,
		    		SH_NO);	
		    
		    if(is_UP_TO_CAP)  return iRet;
		    
    		//是否已達到制程產能
    		if(!FCMPS_RCCP_INFO_Var.getLs_PROC_WORK_QTY().isEmpty()) {
    			for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_PROC_WORK_QTY().size();i++) {
    				PROC_WORK_QTY proc_Work_Qty=FCMPS_RCCP_INFO_Var.getLs_PROC_WORK_QTY().get(i);
    				if(proc_Work_Qty.getFA_NO().equals(getFA_NO())&& 
    				   proc_Work_Qty.getPROCID().equals(PROCID)&&
    				   proc_Work_Qty.getWORK_WEEK()==getWORK_WEEK()) {
    					if(proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY()<=0) {
    						return iRet;
    					}
    				}
    			}
    		}
				    
//============================================================================================================
		    
		    System.out.println(new Date()+" 計算未排滿可提前排訂單--提前沒有排入本周的型體配色,但必須是大於516的型體配色");

			Current_Calac_Step=NEXT_FORE_PLAN_WEEK+"周訂單提前排--沒有排入本周的大於516的型體配色"; //當前計算的步驟
			Current_Calac_Color=""; //當前步驟計算的配色名稱
			Step_Calac_Color_Numbers=0; //當前計算的配色總數
			Step_Calac_Color_Seq=0;  //當前計算的配色順序
			
		    iRet=doGeneratePlan_Non_Bring_In_Planned_Color_Size(
		    		conn, 
		    		PROCID, 
		    		PLAN_NO,
		    		FA_NO,
		    		NEXT_FORE_PLAN_WEEK,
		    		SH_NO);	
			    		    
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		return iRet;
    }
	
	
	/**
	 * 計算指定周次的訂單
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param WORK_WEEK
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @param ls_SH_KEY_SIZE
	 * @param ls_SH_COLOR_SIZE
	 * @param ls_Share_Style_Size
	 * @param SH_NO
	 * @return
	 */
	private int doGeneratePlan_Order_Spec_Week(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int WORK_WEEK,
    		String... SH_NO
    		) {
		int iRet=0;
		
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		try {
			strSQL="select distinct " +
            "FCMPS010.SH_NO,"+
            "FCMPS010.SH_COLOR "+
            "from FCMPS010,FCMPS013 "+
            "where FCMPS010.PROCID='"+PROCID+"'"+
            "  and FCMPS010.FA_NO='"+FA_NO+"'"+
            (SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SH_NO[0]+") ":"")+
            (SH_NOT_IN.equals("")?"":" and FCMPS010.SH_NO NOT IN ("+SH_NOT_IN+") ")+ //不重排計劃時,需要將已排滿的型體排除在外
            "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
            "  and FCMPS010.IS_DISABLE='N' "+
            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(FCMPS010.EXPECT_PLAN_QTY,0)>0 "+  
            "  and FCMPS013.WORK_WEEK="+WORK_WEEK+
	        "  and FCMPS010.FA_NO=FCMPS013.FA_NO(+) "+	        
	        "  and FCMPS010.OD_PONO1=FCMPS013.OD_PONO1(+) "+
	        "  and FCMPS010.PROCID=FCMPS013.PROCID(+)" +
	        "  and FCMPS010.SH_NO=FCMPS013.SH_NO(+) "+
	        "  and FCMPS010.SH_COLOR=FCMPS013.SH_COLOR(+) "+
            "  and FCMPS010.SH_SIZE=FCMPS013.SH_SIZE(+) "+
            "  and FCMPS013.WORK_PLAN_QTY>0";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){	
		    	do {
		    		String SH_NO2=rs.getString("SH_NO");
		    		String SH_COLOR=rs.getString("SH_COLOR");
		    		
					strSQL="select " +
		            "FCMPS010.OD_PONO1, "+
		            "FCMPS010.STYLE_NO,"+
		            "FCMPS010.SH_NO,"+
		            "FCMPS010.SH_COLOR,"+
		            "FCMPS010.SH_SIZE,"+
		            "FCMPS010.PROC_SEQ,"+
		            "FCMPS010.OD_QTY,"+
		            "(CASE WHEN FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>FCMPS013.WORK_PLAN_QTY " +
		            "      then FCMPS013.WORK_PLAN_QTY " +
		            "      else FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0) " +
		            "      end ) WORK_PLAN_QTY,"+            
		            "FCMPS010.WORK_WEEK_START,"+
		            "FCMPS010.WORK_WEEK_END,"+
		            "FCMPS010.OD_SHIP,"+
		            "to_char(FCMPS010.OD_SHIP,'IYIW') od_ship_week,"+
		            "FCMPS010.OD_FGDATE, "+			
		            "to_char(FCMPS010.OD_FGDATE,'IYIW') od_fgdate_week "+
		            "from FCMPS010,FCMPS013 "+
		            "where FCMPS010.PROCID='"+PROCID+"'"+
		            "  and FCMPS010.FA_NO='"+FA_NO+"'"+
		            "  and FCMPS010.SH_NO='"+SH_NO2+"'"+
		            "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
		            
		            (SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SH_NO[0]+") ":"")+
		            (SH_NOT_IN.equals("")?"":" and FCMPS010.SH_NO NOT IN ("+SH_NOT_IN+") ")+ //不重排計劃時,需要將已排滿的型體排除在外
		            "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
		            "  and FCMPS010.IS_DISABLE='N' "+
		            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(FCMPS010.EXPECT_PLAN_QTY,0)>0 "+  
		            "  and FCMPS013.WORK_WEEK="+WORK_WEEK+
			        "  and FCMPS010.FA_NO=FCMPS013.FA_NO(+) "+	        
			        "  and FCMPS010.OD_PONO1=FCMPS013.OD_PONO1(+) "+
			        "  and FCMPS010.PROCID=FCMPS013.PROCID(+)" +
			        "  and FCMPS010.SH_NO=FCMPS013.SH_NO(+) "+
			        "  and FCMPS010.SH_COLOR=FCMPS013.SH_COLOR(+) "+
		            "  and FCMPS010.SH_SIZE=FCMPS013.SH_SIZE(+) "+
		            "  and FCMPS013.WORK_PLAN_QTY>0";

					
					if(getPLAN_PRIORITY_TYPE().equals("1")) {
						if(getPLAN_BY_DATE().equals("A")) {
							strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP ";
						}else {
							strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE ";
						}
					}
				
					if(getPLAN_PRIORITY_TYPE().equals("2")) {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,WORK_WEEK_START ";
					}
					
					if(getPLAN_PRIORITY_TYPE().equals("3")) {
						if(getPLAN_BY_DATE().equals("A")) {
							strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP,WORK_WEEKS ";
						}else {
							strSQL=strSQL+" Order by FCMPS010.SH_SIZE,OD_FGDATE,WORK_WEEKS ";
						}
					}
					
//					System.out.println(strSQL);
					
					pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);	
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){	 	
			 		    
				    	FCMPS_CLS_GenerateOrderPlan_MultiThread cls_GenerateOrderPlan=new FCMPS_CLS_GenerateOrderPlan_MultiThread();
				    	cls_GenerateOrderPlan.setFA_NO(FA_NO);
				    	cls_GenerateOrderPlan.setPLAN_BY_DATE(getPLAN_BY_DATE());
				    	cls_GenerateOrderPlan.setPLAN_NO(PLAN_NO);
				    	cls_GenerateOrderPlan.setWORK_WEEK(WORK_WEEK);
				    	cls_GenerateOrderPlan.setConnection(conn);
				    	cls_GenerateOrderPlan.setIs_FORE_PLAN_WEEKS(is_FORE_PLAN_WEEKS);
				    	cls_GenerateOrderPlan.setFORE_PLAN_WEEKS(getFORE_PLAN_WEEKS());
				    	cls_GenerateOrderPlan.setUP_USER(getUP_USER());
				    	cls_GenerateOrderPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
				    	cls_GenerateOrderPlan.setConfig_xml(config_xml);
				    	cls_GenerateOrderPlan.setPER_SIZE_ALLOW_OVER_NUM(PER_SIZE_ALLOW_OVER_NUM);
				    	
				    	boolean iResult=cls_GenerateOrderPlan.doGeneratePlan(rs2,PROCID,true);	
				    			    	
				    	if(!cls_GenerateOrderPlan.getMessage().isEmpty())setMessage(cls_GenerateOrderPlan.getMessage());
				    	if(!iResult) {
				    		System.out.println("計算指定周次的訂單出現錯誤退出!");
						    iRet=-1;
				    	}
				    	
				    	//當達到本周最大產能,退出
				 		if(cls_GenerateOrderPlan.is_UP_TO_CAP()) {
				 			is_UP_TO_CAP=true;
				 			System.out.println("達到最大周產能,退出!");
				 			iRet=1;	  			    
				 		}
				    }
				    rs2.close();
				    pstmtData2.close();		    		
		    		
				    if(is_UP_TO_CAP) break;
		    		
		    	}while(rs.next());
		    }
			rs.close();
		    pstmtData.close();	

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
	}
	
	/**
	 * 計算優先排產型體的訂單
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param WORK_WEEK
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @param ls_SH_KEY_SIZE
	 * @param ls_SH_COLOR_SIZE
	 * @param ls_Share_Style_Size
	 * @param SH_NO
	 * @return
	 */
	private int doGeneratePlan_SH_Priority(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int WORK_WEEK,
    		String... SH_NO
    		) {
		int iRet=0;
		
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;		
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;	
		
		try {
			 
	    		if(!FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().isEmpty()) {
	    			SH_NOT_IN="";
		    		for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().size();i++) {
		    			if(!SH_NOT_IN.equals(""))SH_NOT_IN=SH_NOT_IN+",";
		    			SH_NOT_IN=SH_NOT_IN+"'"+FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().get(i)+"'";
		    		}	
	    		}
			    
	 		     //設定了優先排產的型體
			    String SQL_FCMPS018=
			    	"select FA_NO,SH_NO from FCMPS018 where FA_NO='"+FA_NO+"' " +
			        " union " +
			        "select FA_NO,SH_NO from FCMPS019 where FA_NO='"+FA_NO+"' and WORK_WEEK=" +WORK_WEEK;
				pstmtData = conn.prepareStatement(SQL_FCMPS018,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs=pstmtData.executeQuery();
				
			    if(!rs.next()){
			    	return iRet;
			    }else {
			    	do {
			    		
						strSQL="select " +
		                   "FCMPS010.OD_PONO1, "+
		                   "FCMPS010.STYLE_NO,"+
		                   "FCMPS010.SH_NO,"+
		                   "FCMPS010.SH_COLOR,"+
		                   "FCMPS010.SH_SIZE,"+
		                   "FCMPS010.PROC_SEQ,"+
		                   "FCMPS010.OD_QTY,"+
		                   "FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0) WORK_PLAN_QTY,"+
		                   "FCMPS010.WORK_WEEK_START,"+
		                   "FCMPS010.WORK_WEEK_END,"+
		                   "FCMPS010.OD_SHIP,"+
		                   "to_char(FCMPS010.OD_SHIP,'IYIW') od_ship_week,"+
		                   "FCMPS010.OD_FGDATE, "+			
		                   "to_char(FCMPS010.OD_FGDATE,'IYIW') od_fgdate_week "+
		                   "from FCMPS010,("+SQL_FCMPS018+") FCMPS018 "+
		                   "where FCMPS010.FA_NO=FCMPS018.FA_NO" +
		                   "  and FCMPS010.SH_NO=FCMPS018.SH_NO" +
		                   "  and FCMPS010.PROCID='"+PROCID+"'"+
		                   "  and FCMPS010.FA_NO='"+FA_NO+"'"+
		                   "  and FCMPS010.SH_NO='"+rs.getString("SH_NO")+"'"+
		                   "  and FCMPS010.WORK_WEEK_END="+getWORK_WEEK()+
		                   
		                   (SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SH_NO[0]+") ":"")+
		                   "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
		                   "  and FCMPS010.IS_DISABLE='N' "+   
		                   (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
		                   (SH_NOT_IN.equals("")?"":" and FCMPS010.SH_NO NOT IN ("+SH_NOT_IN+") ")+ //不重排計劃時,需要將已排滿的型體排除在外
		                   "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+
		                   
		                   "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
		                   "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
		                   "       FROM FCMPS021 " +
		                   "      WHERE PROCID = '"+PROCID+"' " +
		                   "        AND FA_NO = '"+FA_NO+"' " +
		                   "        AND WORK_WEEK<="+getWORK_WEEK()+") "+
		                   
	   		    		   "  and (SH_NO,SH_COLOR,SH_SIZE) NOT IN (SELECT SH_NO,SH_COLOR,SH_SIZE FROM FCMPS013 " +
			    		   "                    WHERE FA_NO='"+FA_NO+"' " +
			    	       "                      AND WORK_WEEK="+WORK_WEEK+" " +
			    	       "                      AND PROCID='"+PROCID+"' " +
			    	       "                      AND ALLOW_APPEND='N') ";
					
						if(getPLAN_PRIORITY_TYPE().equals("1")) {
							if(getPLAN_BY_DATE().equals("A")) {
								strSQL=strSQL+" Order by FCMPS010.SH_COLOR,FCMPS010.SH_SIZE,FCMPS010.OD_SHIP ";
							}else {
								strSQL=strSQL+" Order by FCMPS010.SH_COLOR,FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE ";
							}
						}
					
						if(getPLAN_PRIORITY_TYPE().equals("2")) {
							strSQL=strSQL+" Order by FCMPS010.SH_COLOR,FCMPS010.SH_SIZE,WORK_WEEK_START ";
						}
						
						if(getPLAN_PRIORITY_TYPE().equals("3")) {
							if(getPLAN_BY_DATE().equals("A")) {
								strSQL=strSQL+" Order by FCMPS010.SH_COLOR,FCMPS010.SH_SIZE,FCMPS010.OD_SHIP,WORK_WEEKS ";
							}else {
								strSQL=strSQL+" Order by FCMPS010.SH_COLOR,FCMPS010.SH_SIZE,OD_FGDATE,WORK_WEEKS ";
							}
						}
						
//						System.out.println(strSQL);
						
						pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
					    rs2=pstmtData2.executeQuery();
				    
						rs2.setFetchDirection(ResultSet.FETCH_FORWARD);
						rs2.setFetchSize(3000);
						
					    if(rs2.next()){
				    		FCMPS_CLS_GenerateOrderPlan_MultiThread cls_GenerateOrderPlan=new FCMPS_CLS_GenerateOrderPlan_MultiThread();
					    	cls_GenerateOrderPlan.setFA_NO(FA_NO);
					    	cls_GenerateOrderPlan.setPLAN_BY_DATE(getPLAN_BY_DATE());
					    	cls_GenerateOrderPlan.setPLAN_NO(PLAN_NO);
					    	cls_GenerateOrderPlan.setWORK_WEEK(WORK_WEEK);
					    	cls_GenerateOrderPlan.setConnection(conn);
					    	cls_GenerateOrderPlan.setIs_FORE_PLAN_WEEKS(is_FORE_PLAN_WEEKS);
					    	cls_GenerateOrderPlan.setFORE_PLAN_WEEKS(getFORE_PLAN_WEEKS());
					    	cls_GenerateOrderPlan.setUP_USER(getUP_USER());
					    	cls_GenerateOrderPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
					    	cls_GenerateOrderPlan.setConfig_xml(config_xml);
					    	cls_GenerateOrderPlan.setPER_SIZE_ALLOW_OVER_NUM(PER_SIZE_ALLOW_OVER_NUM);
					    	cls_GenerateOrderPlan.setFCMPS_RCCP_INFO_Var(FCMPS_RCCP_INFO_Var);
					    	
					    	boolean iResult=cls_GenerateOrderPlan.doGeneratePlan(rs2,PROCID,false);	
					    			    	
					    	if(!cls_GenerateOrderPlan.getMessage().isEmpty())setMessage(cls_GenerateOrderPlan.getMessage());
					    	if(!iResult) {
					    		System.out.println("計算設定了優先排入的型體出現錯誤退出!");
							    iRet=-1;
					    	}
					    	
					    	//當達到本周最大產能,退出
					 		if(cls_GenerateOrderPlan.is_UP_TO_CAP()) {
					 			is_UP_TO_CAP=true;
					 			System.out.println("達到最大周產能,退出!");
					 			iRet=1;
					 		}		    	
					    }
					    rs2.close();
					    pstmtData2.close();			    		
			    		
			    		if(is_UP_TO_CAP) break;
			    		
			    	}while(rs.next());
			    }
			    rs.close();
			    pstmtData.close();
			    			   								    
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
	}
	
	/**
	 * 計算已達到最晚完工周次的訂單
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param WORK_WEEK
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @param ls_SH_KEY_SIZE
	 * @param ls_SH_COLOR_SIZE
	 * @param ls_Share_Style_Size
	 * @param is_Must   true: 已達出貨周次,必須排 
	 * @param SH_NO
	 * @return
	 */
	private int doGeneratePlan_Current_In_Work_Week_End(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int WORK_WEEK,
    		String... SPEC_SH_NO
    		) {
    	int iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
				
		try {
    		
    		if(!FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().isEmpty()) {
    			SH_NOT_IN="";
	    		for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().size();i++) {
	    			if(!SH_NOT_IN.equals(""))SH_NOT_IN=SH_NOT_IN+",";
	    			SH_NOT_IN=SH_NOT_IN+"'"+FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().get(i)+"'";
	    		}	
    		}
    		
    		int NEXT_WEEK=WORK_WEEK;
    		do {
    			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1),conn)==0) {
    				NEXT_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1);
    			}else {
    				break;
    			}
    			
    		}while(true);
    		
    		//已達到交期必須在本周完成,可能沒有516了, 但應該排入
			strSQL="select " +
            "SH_NO,"+
            "SH_COLOR,"+
            "min(WORK_WEEK_END) WORK_WEEK_END "+
            "from FCMPS010 "+
            "where FCMPS010.PROCID='"+PROCID+"'"+
            "  and FCMPS010.FA_NO='"+FA_NO+"'"+
            (SH_NOT_IN.equals("")?"":" and FCMPS010.SH_NO not in("+SH_NOT_IN+") ")+
            (SPEC_SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
            "  and nvl(OD_CODE,'N')='N' "+
            "  and IS_DISABLE='N' "+
            "  and WORK_WEEK_END<='"+NEXT_WEEK+"'"+ //先只排必須在本周或是前周完工的訂單
            
            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+		                   		                   
            
            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+

            //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
            //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
            "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
            "                       FROM FCPS22_1 "+
            "                       WHERE NEED_PLAN = 'Y' "+
            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
            "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
            "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
            "                     from FCMPS007,FCMPS006 " +
            "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
            "                     and FCMPS006.IS_SURE='Y'"+
            "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
            "                     and (FCMPS006.WORK_WEEK>="+getWORK_WEEK()+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
            "         or          "+
            "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
            "                       FROM FCPS22_1 "+
            "                       WHERE NEED_PLAN = 'Y' "+
            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))"+
            ") ";		                   
		
		
		   //指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
		   //為了避免因為後關提前導致其前關也提前
		   //有些訂單只有部分數量指定到某周,所以需要數量也一樣	
		   strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) not in " +
			"             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
            "               where FA_NO=FCMPS010.FA_NO "+
            "                 and WORK_WEEK>"+getWORK_WEEK()+
            "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
            "                 and PROCID=FCMPS010.PROCID" +
            "                 and SH_NO=FCMPS010.SH_NO "+
            "                 and SH_COLOR=FCMPS010.SH_COLOR "+
            "                 and SH_SIZE=FCMPS010.SH_SIZE) ";
            
		    strSQL=strSQL+" group by SH_NO,SH_COLOR order by WORK_WEEK_END" ;
		    
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.last()) {
		    	Step_Calac_Color_Numbers=rs.getRow();
		    }
		    
		    rs.beforeFirst();
		    
		    if(rs.next()){		
				Current_Calac_Color=""; //當前步驟計算的配色名稱
				Step_Calac_Color_Seq=0;  //當前計算的配色順序
						    			    	
		    	do {		
		    		
		    		Step_Calac_Color_Seq++;
		    		
		    		Current_Calac_Color="Style:"+rs.getString("SH_NO")+" Color:"+rs.getString("SH_COLOR");
		    		
//			    	System.out.println(rs.getString("SH_NO")+" "+rs.getString("SH_COLOR")+" "+rs.getString("WORK_PLAN_QTY"));
			    	
				    iRet=doGeneratePlan_Current_In_Work_Week_End(
				    		conn, 
				    		PROCID, 
				    		PLAN_NO, 
				    		FA_NO, 
				    		WORK_WEEK, 
				    		rs.getString("SH_NO"),
				    		rs.getString("SH_COLOR"),
				    		true);	
				    
				    if(is_UP_TO_CAP) break;
				    
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
		
	}

    /**
     * 計算本周剩餘的訂單<br>
     * @param conn
     * @param PROCID 
     * @param PLAN_NO
     * @param FA_NO
     * @param WORK_WEEK
     * @param WORK_WEEK_END   最晚完工周次
     * @param SH_NO
     * @param SH_COLOR
     * @param ls_PROC_WORK_QTY
     * @param ls_SH_WORK_QTY
     * @param ls_SH_KEY_SIZE
     * @param ls_SH_COLOR_SIZE
     * @param ls_SH_COLOR_ALLOW_COUNT
     * @param ls_SH_COLOR_QTY
     * @param ls_Share_Style_Size
     * @param ls_SH_NEED_SHOOT
     * @param ls_SH_USE_CAP
     * @param ls_SH_NEED_PLAN_PROC
     * @param ls_Not_Again_Plan
     * @param is_Spec_Week_Plan
     * @return
     */
	private int doGeneratePlan_Current_In_Work_Week_End(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int WORK_WEEK,
    		String SH_NO,
    		String SH_COLOR,
    		boolean is_Spec_Week_Plan
    		) {
    	int iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
				
		try {

    		int NEXT_WEEK=WORK_WEEK;
    		do {
    			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1),conn)==0) {
    				NEXT_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1);
    			}else {
    				break;
    			}
    			
    		}while(true);
    		
			strSQL="select " +
            "FCMPS010.OD_PONO1, "+
            "FCMPS010.STYLE_NO,"+
            "FCMPS010.SH_NO,"+
            "FCMPS010.SH_COLOR,"+
            "FCMPS010.SH_SIZE,"+
            "FCMPS010.PROC_SEQ,"+
            "FCMPS010.OD_QTY,"+
            "FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0) WORK_PLAN_QTY,"+
            "FCMPS010.WORK_WEEK_START,"+
            "FCMPS010.WORK_WEEK_END,"+
            "FCMPS010.OD_SHIP,"+
            "to_char(FCMPS010.OD_SHIP,'IYIW') od_ship_week,"+
            "FCMPS010.OD_FGDATE, "+			
            "to_char(FCMPS010.OD_FGDATE,'IYIW') od_fgdate_week "+
            "from FCMPS010 "+
            "where FCMPS010.SH_NO='"+SH_NO+"'"+
            "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
            "  and FCMPS010.PROCID='"+PROCID+"'"+
            "  and FCMPS010.FA_NO='"+FA_NO+"'"+                
            "  and nvl(OD_CODE,'N')='N' "+
            "  and IS_DISABLE='N' "+
            "  and WORK_WEEK_END<="+NEXT_WEEK+ //先只排必須在本周或是前周完工的訂單
            
            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+		                   		                   
            
            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+

               //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
            //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
            "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
            "                       FROM FCPS22_1 "+
            "                       WHERE NEED_PLAN = 'Y' "+
            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
            "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
            "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
            "                     from FCMPS007,FCMPS006 " +
            "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
            "                     and FCMPS006.IS_SURE='Y'"+
            "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
            "                     and (FCMPS006.WORK_WEEK>="+getWORK_WEEK()+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
            "         or          "+
            "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
            "                       FROM FCPS22_1 "+
            "                       WHERE NEED_PLAN = 'Y' "+
            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))) ";		                   
		
			//指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
			//為了避免因為後關提前導致其前關也提前
			strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) not in " +
				   "             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
	               "               where FA_NO=FCMPS010.FA_NO "+
	               "                 and WORK_WEEK>"+getWORK_WEEK()+
	               "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
	               "                 and PROCID=FCMPS010.PROCID" +
	               "                 and SH_NO=FCMPS010.SH_NO "+
	               "                 and SH_COLOR=FCMPS010.SH_COLOR "+
	               "                 and SH_SIZE=FCMPS010.SH_SIZE) "; 					
						
			if(getPLAN_PRIORITY_TYPE().equals("1")) {
				if(getPLAN_BY_DATE().equals("A")) {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP ";
				}else {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE ";
				}
			}
		
			if(getPLAN_PRIORITY_TYPE().equals("2")) {
				strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.WORK_WEEK_START";
			}
			
			if(getPLAN_PRIORITY_TYPE().equals("3")) {
				if(getPLAN_BY_DATE().equals("A")) {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP,FCMPS010.WORK_WEEKS ";
				}else {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE,FCMPS010.WORK_WEEKS ";
				}
			}

//			System.out.println(strSQL);
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
			rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			rs.setFetchSize(3000);
		    
		    if(rs.next()){
	    		FCMPS_CLS_GenerateOrderPlan_MultiThread cls_GenerateOrderPlan=new FCMPS_CLS_GenerateOrderPlan_MultiThread();
		    	cls_GenerateOrderPlan.setFA_NO(FA_NO);
		    	cls_GenerateOrderPlan.setPLAN_BY_DATE(getPLAN_BY_DATE());
		    	cls_GenerateOrderPlan.setPLAN_NO(PLAN_NO);
		    	cls_GenerateOrderPlan.setWORK_WEEK(WORK_WEEK);
		    	cls_GenerateOrderPlan.setConnection(conn);
		    	cls_GenerateOrderPlan.setIs_FORE_PLAN_WEEKS(is_FORE_PLAN_WEEKS);
		    	cls_GenerateOrderPlan.setFORE_PLAN_WEEKS(getFORE_PLAN_WEEKS());
		    	cls_GenerateOrderPlan.setUP_USER(getUP_USER());
		    	cls_GenerateOrderPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
		    	cls_GenerateOrderPlan.setConfig_xml(config_xml);
		    	cls_GenerateOrderPlan.setPER_SIZE_ALLOW_OVER_NUM(PER_SIZE_ALLOW_OVER_NUM);
		    	cls_GenerateOrderPlan.setFCMPS_RCCP_INFO_Var(FCMPS_RCCP_INFO_Var);
		    	
		    	boolean iResult=cls_GenerateOrderPlan.doGeneratePlan(rs,PROCID,is_Spec_Week_Plan);			    	
		    	
		    	if(!cls_GenerateOrderPlan.getMessage().isEmpty())setMessage(cls_GenerateOrderPlan.getMessage());
		    	if(!iResult) {
		    		System.out.println("計算剩餘訂單--已達到最晚完工周次必須要排--出現錯誤退出!");
				    iRet=-1;
		    	}
		    	
		    	//當達到本周最大產能,退出
	    		if(cls_GenerateOrderPlan.is_UP_TO_CAP()) {
	    			is_UP_TO_CAP=true;
	    			System.out.println("達到最大周產能,退出!");
	    			iRet=1;
	    		}	 		    	
		    }
		    rs.close();
		    pstmtData.close();			    	    	   

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
		
	}
	
	/**
	 * 計算已達到最晚開工周次的訂單
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param WORK_WEEK
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @param ls_SH_KEY_SIZE
	 * @param ls_SH_COLOR_SIZE
	 * @param ls_SH_COLOR_ALLOW_COUNT
	 * @param ls_SH_COLOR_QTY
	 * @param ls_Share_Style_Size
	 * @param ls_SH_NEED_SHOOT
	 * @param ls_SH_USE_CAP
	 * @param ls_SH_NEED_PLAN_PROC
	 * @param ls_Not_Again_Plan
	 * @param SPEC_SH_NO
	 * @return
	 */
	private int doGeneratePlan_Current_In_Work_Week_Start(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int WORK_WEEK,
    		String... SPEC_SH_NO
    		) {
    	int iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
				
		try {
    		
    		if(!FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().isEmpty()) {
    			SH_NOT_IN="";
	    		for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().size();i++) {
	    			if(!SH_NOT_IN.equals(""))SH_NOT_IN=SH_NOT_IN+",";
	    			SH_NOT_IN=SH_NOT_IN+"'"+FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().get(i)+"'";
	    		}	
    		}
    		
    		int NEXT_WEEK=WORK_WEEK;
    		do {
    			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1),conn)==0) {
    				NEXT_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1);
    			}else {
    				break;
    			}
    			
    		}while(true);
    		
    		//已達到交期必須在本周完成,可能沒有516了, 但應該排入
			strSQL="select " +
            "SH_NO,"+
            "SH_COLOR,"+
            "min(WORK_WEEK_START) WORK_WEEK_START "+
            "from FCMPS010 "+
            "where FCMPS010.PROCID='"+PROCID+"'"+
            "  and FCMPS010.FA_NO='"+FA_NO+"'"+
            (SH_NOT_IN.equals("")?"":" and FCMPS010.SH_NO not in("+SH_NOT_IN+") ")+
            (SPEC_SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
            "  and nvl(OD_CODE,'N')='N' "+
            "  and IS_DISABLE='N' "+
            "  and WORK_WEEK_START<='"+NEXT_WEEK+"'"+ //先只排必須在本周或是前周開工的訂單
                        
            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+		                   		                   
            
            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+

            //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
            //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
            "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
            "                       FROM FCPS22_1 "+
            "                       WHERE NEED_PLAN = 'Y' "+
            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
            "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
            "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
            "                     from FCMPS007,FCMPS006 " +
            "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
            "                     and FCMPS006.IS_SURE='Y'"+
            "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
            "                     and (FCMPS006.WORK_WEEK>="+getWORK_WEEK()+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
            "         or          "+
            "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
            "                       FROM FCPS22_1 "+
            "                       WHERE NEED_PLAN = 'Y' "+
            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))"+
            ") ";		                   
		
		
		   //指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
		   //為了避免因為後關提前導致其前關也提前
		   //有些訂單只有部分數量指定到某周,所以需要數量也一樣	
		   strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) not in " +
			"             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
            "               where FA_NO=FCMPS010.FA_NO "+
            "                 and WORK_WEEK>"+getWORK_WEEK()+
            "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
            "                 and PROCID=FCMPS010.PROCID" +
            "                 and SH_NO=FCMPS010.SH_NO "+
            "                 and SH_COLOR=FCMPS010.SH_COLOR "+
            "                 and SH_SIZE=FCMPS010.SH_SIZE) ";
            
		    strSQL=strSQL+" group by SH_NO,SH_COLOR order by WORK_WEEK_START" ;
		    
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.last()) {
		    	Step_Calac_Color_Numbers=rs.getRow();
		    }
		    
		    rs.beforeFirst();
		    
			Current_Calac_Color=""; //當前步驟計算的配色名稱
			Step_Calac_Color_Seq=0;  //當前計算的配色順序
			 
		    if(rs.next()){	
		    			    	
		    	do {		
			    	
		    		Step_Calac_Color_Seq++;
		    		Current_Calac_Color="Style:"+rs.getString("SH_NO")+" Color:"+rs.getString("SH_COLOR");
		    		
//			    	System.out.println(rs.getString("SH_NO")+" "+rs.getString("SH_COLOR")+" "+rs.getString("WORK_PLAN_QTY"));
			    	
				    iRet=doGeneratePlan_Current_In_Work_Week_Start(
				    		conn, 
				    		PROCID, 
				    		PLAN_NO, 
				    		FA_NO, 
				    		WORK_WEEK, 
				    		rs.getString("SH_NO"),
				    		rs.getString("SH_COLOR"),
				    		false);	
				    
				    if(is_UP_TO_CAP) break;
				    
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
		
	}

	/**
	 * 計算最晚開工周次的訂單
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param WORK_WEEK
	 * @param WORK_WEEK_START    最晚開工周次
	 * @param SH_NO
	 * @param SH_COLOR
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @param ls_SH_KEY_SIZE
	 * @param ls_SH_COLOR_SIZE
	 * @param ls_SH_COLOR_ALLOW_COUNT
	 * @param ls_SH_COLOR_QTY
	 * @param ls_Share_Style_Size
	 * @param ls_SH_NEED_SHOOT
	 * @param ls_SH_USE_CAP
	 * @param ls_SH_NEED_PLAN_PROC
	 * @param ls_Not_Again_Plan
	 * @param is_Spec_Week_Plan
	 * @return
	 */
	private int doGeneratePlan_Current_In_Work_Week_Start(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int WORK_WEEK,
    		String SH_NO,
    		String SH_COLOR,
    		boolean is_Spec_Week_Plan
    		) {
    	int iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
				
		try {
   		
    		int NEXT_WEEK=WORK_WEEK;
    		do {
    			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1),conn)==0) {
    				NEXT_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1);
    			}else {
    				break;
    			}
    			
    		}while(true);
    		
	    	
			strSQL="select " +
            "FCMPS010.OD_PONO1, "+
            "FCMPS010.STYLE_NO,"+
            "FCMPS010.SH_NO,"+
            "FCMPS010.SH_COLOR,"+
            "FCMPS010.SH_SIZE,"+
            "FCMPS010.PROC_SEQ,"+
            "FCMPS010.OD_QTY,"+
            "FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0) WORK_PLAN_QTY,"+
            "FCMPS010.WORK_WEEK_START,"+
            "FCMPS010.WORK_WEEK_END,"+
            "FCMPS010.OD_SHIP,"+
            "to_char(FCMPS010.OD_SHIP,'IYIW') od_ship_week,"+
            "FCMPS010.OD_FGDATE, "+			
            "to_char(FCMPS010.OD_FGDATE,'IYIW') od_fgdate_week "+
            "from FCMPS010 "+
            "where FCMPS010.SH_NO='"+SH_NO+"'"+
            "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
            "  and FCMPS010.PROCID='"+PROCID+"'"+
            "  and FCMPS010.FA_NO='"+FA_NO+"'"+                
            "  and nvl(OD_CODE,'N')='N' "+
            "  and IS_DISABLE='N' "+
            "  and WORK_WEEK_START<="+NEXT_WEEK+ //先只排必須在本周或是前周開工的訂單
            
            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+		                   		                   
            
            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+

               //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
            //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
            "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
            "                       FROM FCPS22_1 "+
            "                       WHERE NEED_PLAN = 'Y' "+
            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
            "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
            "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
            "                     from FCMPS007,FCMPS006 " +
            "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
            "                     and FCMPS006.IS_SURE='Y'"+
            "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
            "                     and (FCMPS006.WORK_WEEK>="+getWORK_WEEK()+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
            "         or          "+
            "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
            "                       FROM FCPS22_1 "+
            "                       WHERE NEED_PLAN = 'Y' "+
            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))) ";		                   
		
			//指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
			//為了避免因為後關提前導致其前關也提前
			strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) not in " +
				   "             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
	               "               where FA_NO=FCMPS010.FA_NO "+
	               "                 and WORK_WEEK>"+getWORK_WEEK()+
	               "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
	               "                 and PROCID=FCMPS010.PROCID" +
	               "                 and SH_NO=FCMPS010.SH_NO "+
	               "                 and SH_COLOR=FCMPS010.SH_COLOR "+
	               "                 and SH_SIZE=FCMPS010.SH_SIZE) "; 					
						
			if(getPLAN_PRIORITY_TYPE().equals("1")) {
				if(getPLAN_BY_DATE().equals("A")) {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP ";
				}else {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE ";
				}
			}
		
			if(getPLAN_PRIORITY_TYPE().equals("2")) {
				strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.WORK_WEEK_START";
			}
			
			if(getPLAN_PRIORITY_TYPE().equals("3")) {
				if(getPLAN_BY_DATE().equals("A")) {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP,FCMPS010.WORK_WEEKS ";
				}else {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE,FCMPS010.WORK_WEEKS ";
				}
			}

//			System.out.println(strSQL);
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
			rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			rs.setFetchSize(3000);
		    
		    if(rs.next()){
	    		FCMPS_CLS_GenerateOrderPlan_MultiThread cls_GenerateOrderPlan=new FCMPS_CLS_GenerateOrderPlan_MultiThread();
		    	cls_GenerateOrderPlan.setFA_NO(FA_NO);
		    	cls_GenerateOrderPlan.setPLAN_BY_DATE(getPLAN_BY_DATE());
		    	cls_GenerateOrderPlan.setPLAN_NO(PLAN_NO);
		    	cls_GenerateOrderPlan.setWORK_WEEK(WORK_WEEK);
		    	cls_GenerateOrderPlan.setConnection(conn);
		    	cls_GenerateOrderPlan.setIs_FORE_PLAN_WEEKS(is_FORE_PLAN_WEEKS);
		    	cls_GenerateOrderPlan.setFORE_PLAN_WEEKS(getFORE_PLAN_WEEKS());
		    	cls_GenerateOrderPlan.setUP_USER(getUP_USER());
		    	cls_GenerateOrderPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
		    	cls_GenerateOrderPlan.setConfig_xml(config_xml);
		    	cls_GenerateOrderPlan.setPER_SIZE_ALLOW_OVER_NUM(PER_SIZE_ALLOW_OVER_NUM);
		    	cls_GenerateOrderPlan.setIs_Must_Append(true);
		    	cls_GenerateOrderPlan.setFCMPS_RCCP_INFO_Var(FCMPS_RCCP_INFO_Var);
		    	
		    	boolean iResult=cls_GenerateOrderPlan.doGeneratePlan(rs,PROCID,is_Spec_Week_Plan);			    	
		    	
		    	if(!cls_GenerateOrderPlan.getMessage().isEmpty())setMessage(cls_GenerateOrderPlan.getMessage());
		    	if(!iResult) {
		    		System.out.println("計算剩餘訂單--已達到最晚開工周次必須要排--出現錯誤退出!");
				    iRet=-1;
		    	}
		    	
		    	//當達到本周最大產能,退出
	    		if(cls_GenerateOrderPlan.is_UP_TO_CAP()) {
	    			is_UP_TO_CAP=true;
	    			System.out.println("達到最大周產能,退出!");
	    			iRet=1;
	    		}	 		    	
		    }
		    rs.close();
		    pstmtData.close();			    	    	   

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
		
	}
	
	/**
	 * 計算本周剩餘的訂單
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param WORK_WEEK
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @param ls_SH_KEY_SIZE
	 * @param ls_SH_COLOR_SIZE
	 * @param ls_Share_Style_Size
	 * @param is_Must   true: 已達出貨周次,必須排 
	 * @param SH_NO
	 * @return
	 */
	private int doGeneratePlan_Current_Week_Order(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int WORK_WEEK,
    		String... SPEC_SH_NO
    		) {
    	int iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
				
		try {
			
    		if(!FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().isEmpty()) {
    			SH_NOT_IN="";
	    		for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().size();i++) {
	    			if(!SH_NOT_IN.equals(""))SH_NOT_IN=SH_NOT_IN+",";
	    			SH_NOT_IN=SH_NOT_IN+"'"+FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().get(i)+"'";
	    		}	
    		}
    		    		
    		Map<Integer,List<String[]>> set_SH_COLOR=new TreeMap<Integer,List<String[]>>();
    		
    		int NEXT_WEEK=WORK_WEEK;
    		do {
    			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1),conn)==0) {
    				NEXT_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1);
    			}else {
    				break;
    			}
    			
    		}while(true);
    		
    		int NEXT_FORE_PLAN_WEEK=WORK_WEEK;

    		for(int iWeek=1;iWeek<getFORE_PLAN_WEEKS();iWeek++) {
    			NEXT_FORE_PLAN_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_FORE_PLAN_WEEK, 1);
        		do {
        			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,NEXT_FORE_PLAN_WEEK,conn)==0) {
        				NEXT_FORE_PLAN_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_FORE_PLAN_WEEK,1);
        			}else {
        				break;
        			}
        			
        		}while(true);
    		}
    	    
			Current_Calac_Step="預排時排入"+WORK_WEEK+"周的訂單--已排入本周計劃的型體配色"; //當前計算的步驟
			Current_Calac_Color=""; //當前步驟計算的配色名稱
			Step_Calac_Color_Numbers=0; //當前計算的配色總數
			Step_Calac_Color_Seq=0;  //當前計算的配色順序
			
    		
    		//已排入本周計劃的型體配色
			strSQL="select " +
            "SH_NO,"+
            "SH_COLOR,"+
            "min(WORK_WEEK_START) OD_FGDATE_WEEK "+
            "from FCMPS010 "+
            "where FCMPS010.PROCID='"+PROCID+"'"+
            "  and FCMPS010.FA_NO='"+FA_NO+"'"+
            (SH_NOT_IN.equals("")?"":" and FCMPS010.SH_NO not in ("+SH_NOT_IN+")")+
            (SPEC_SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
            "  and nvl(OD_CODE,'N')='N' "+
            "  and IS_DISABLE='N' "+                        
//            "  and WORK_WEEK_END>="+WORK_WEEK+            
//            "  and WORK_WEEK_END<="+NEXT_FORE_PLAN_WEEK+
            
            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+		                   		                   
            
            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+
		
            //已排入本周計劃的型體配色
            "  and (SH_NO,SH_COLOR) in "+
            "      (select SH_NO,SH_COLOR from FCMPS007 where PLAN_NO='"+getPLAN_NO()+"' and PROCID='"+PROCID+"' and SH_NO=FCMPS010.SH_NO and SH_COLOR=FCMPS010.SH_COLOR)"+
            
            //預排時已排入本周
            "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
            "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
            "       FROM FCMPS021 " +
            "       WHERE PROCID = '"+PROCID+"' " +
            "         AND FA_NO = '"+FA_NO+"' " +
//            "         AND OD_PONO1 =FCMPS010.OD_PONO1 " +
//            "         AND SH_NO =FCMPS010.SH_NO " +
//            "         AND SH_COLOR =FCMPS010.SH_COLOR " +
//            "         AND SH_SIZE =FCMPS010.SH_SIZE " +
            "         AND WORK_WEEK<="+WORK_WEEK+") "+

            //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
            //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
            "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
            "                       FROM FCPS22_1 "+
            "                       WHERE NEED_PLAN = 'Y' "+
            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
            "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
            "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
            "                     from FCMPS007,FCMPS006 " +
            "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
            "                     and FCMPS006.IS_SURE='Y'"+
            "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
            "                     and (FCMPS006.WORK_WEEK>="+getWORK_WEEK()+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
            "         or          "+
            "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
            "                       FROM FCPS22_1 "+
            "                       WHERE NEED_PLAN = 'Y' "+
            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))"+
            ") ";		                   
		
		
		   //指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
		   //為了避免因為後關提前導致其前關也提前
		   strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) not in " +
			"             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
            "               where FA_NO= '"+FA_NO+"' " +
            "                 and WORK_WEEK>"+getWORK_WEEK()+
//            "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
//            "                 and PROCID=FCMPS010.PROCID" +
//            "                 and SH_NO=FCMPS010.SH_NO "+
//            "                 and SH_COLOR=FCMPS010.SH_COLOR "+
//            "                 and SH_SIZE=FCMPS010.SH_SIZE" +
            "              ) ";
            
		    strSQL=strSQL+" group by SH_NO,SH_COLOR order by OD_FGDATE_WEEK" ;
		    
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.last()) {
		    	Step_Calac_Color_Numbers=rs.getRow();
		    }
		    
		    if(rs.next()){		    	
		    	do {
		    		
		    		Step_Calac_Color_Seq++;
		    		
		    		Current_Calac_Color="Style:"+rs.getString("SH_NO")+" Color:"+rs.getString("SH_COLOR");
		    		
				    iRet=doGeneratePlan_Current_Week_Order(
				    		conn, 
				    		PROCID, 
				    		PLAN_NO, 
				    		FA_NO, 
				    		WORK_WEEK, 
				    		NEXT_FORE_PLAN_WEEK,
				    		rs.getString("SH_NO"),
				    		rs.getString("SH_COLOR"),
				    		false);	
				    
				    if(is_UP_TO_CAP) break;		    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();    		

		    if(is_UP_TO_CAP) return iRet;
		    
    		if(!FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().isEmpty()) {
    			SH_NOT_IN="";
	    		for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().size();i++) {
	    			if(!SH_NOT_IN.equals(""))SH_NOT_IN=SH_NOT_IN+",";
	    			SH_NOT_IN=SH_NOT_IN+"'"+FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().get(i)+"'";
	    		}	
    		}

    		//已排入本周計劃的同型體,不同配色
			strSQL="SELECT SH_NO,SH_COLOR,min(OD_FGDATE_WEEK) OD_FGDATE_WEEK2 "+
                   "  FROM FCMPS021 " +
                   " WHERE PROCID = '"+PROCID+"' " +
                   "   AND FA_NO = '"+FA_NO+"' " +
                   "   AND WORK_WEEK<="+WORK_WEEK+
                   (SH_NOT_IN.equals("")?"":" and SH_NO not in ("+SH_NOT_IN+")")+
                   (SPEC_SH_NO.length>0?" and SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
                   "   AND (SH_NO,SH_COLOR) not in "+
                   "       (select distinct SH_NO,SH_COLOR from FCMPS007 where PLAN_NO='"+getPLAN_NO()+"' AND PROCID='"+PROCID+"' AND SH_NO=FCMPS021.SH_NO AND SH_COLOR=FCMPS021.SH_COLOR)"+
                   "   AND SH_NO in (select distinct SH_NO from FCMPS007 where PLAN_NO='"+getPLAN_NO()+"' AND PROCID='"+PROCID+"' AND SH_NO=FCMPS021.SH_NO) "+
                   "group by SH_NO,SH_COLOR order by OD_FGDATE_WEEK2";
		    
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	do {
		    		
		    		List<String[]> ls_SH_COLOR=set_SH_COLOR.get(rs.getInt("OD_FGDATE_WEEK2"));
		    		if(ls_SH_COLOR==null) {
		    			ls_SH_COLOR=new ArrayList<String[]>();
		    			ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
		    			set_SH_COLOR.put(rs.getInt("OD_FGDATE_WEEK2"), ls_SH_COLOR);
		    		}else {
			    		boolean isExist=false;
			    		for(String[] item:ls_SH_COLOR) {
			    			if(item[0].equals(rs.getString("SH_NO")) && item[1].equals(rs.getString("SH_COLOR"))) {
			    				isExist=true;
			    				break;
			    			}
			    		}
			    		if(!isExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});	
		    		}
		    			    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();		    
		    
		    
		    //已排入本周計劃的共模型體
		    
			strSQL="SELECT SH_NO,SH_COLOR,min(OD_FGDATE_WEEK) OD_FGDATE_WEEK2 "+
                   "  FROM FCMPS021 " +
                   " WHERE PROCID = '"+PROCID+"' " +
                   "   AND FA_NO = '"+FA_NO+"' " +
                   "   AND WORK_WEEK<="+WORK_WEEK+
                   (SH_NOT_IN.equals("")?"":" and SH_NO not in ("+SH_NOT_IN+")")+
                   (SPEC_SH_NO.length>0?" and SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
                   "   AND (SH_NO,SH_SIZE) in (select distinct sh_no2,sh_size2 from fcmps0022 where (sh_no,sh_size) in (select distinct SH_NO,sh_size from FCMPS007 where PLAN_NO = '"+getPLAN_NO()+"')) "+
                   "group by SH_NO,SH_COLOR order by OD_FGDATE_WEEK2";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	do {
		    		List<String[]> ls_SH_COLOR=set_SH_COLOR.get(rs.getInt("OD_FGDATE_WEEK2"));
		    		if(ls_SH_COLOR==null) {
		    			ls_SH_COLOR=new ArrayList<String[]>();
		    			ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
		    			set_SH_COLOR.put(rs.getInt("OD_FGDATE_WEEK2"), ls_SH_COLOR);
		    		}else {
			    		boolean isExist=false;
			    		for(String[] item:ls_SH_COLOR) {
			    			if(item[0].equals(rs.getString("SH_NO")) && item[1].equals(rs.getString("SH_COLOR"))) {
			    				isExist=true;
			    				break;
			    			}
			    		}
			    		if(!isExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});	
		    		}	    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();
	    
    		//上一周排的型體配色
			strSQL="SELECT FCMPS007.SH_NO,FCMPS007.SH_COLOR "+
                   "  FROM FCMPS007,FCMPS006 " +
                   " WHERE FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
                   "   AND FCMPS006.PROCID = '"+PROCID+"' " +
                   "   AND FCMPS006.FA_NO = '"+FA_NO+"' " +
                   "   AND FCMPS006.WORK_WEEK="+FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, 1)+
                   (SH_NOT_IN.equals("")?"":" and FCMPS007.SH_NO not in ("+SH_NOT_IN+")")+
                   (SPEC_SH_NO.length>0?" and FCMPS007.SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
                   "GROUP BY FCMPS007.SH_NO,FCMPS007.SH_COLOR ";
		    
			strSQL="SELECT SH_NO,SH_COLOR,min(OD_FGDATE_WEEK) OD_FGDATE_WEEK2 "+
                   "  FROM FCMPS021 " +
                   " WHERE PROCID = '"+PROCID+"' " +
                   "   AND FA_NO = '"+FA_NO+"' " +
                   "   AND WORK_WEEK<="+WORK_WEEK+
                   "   AND (SH_NO,SH_COLOR) IN ("+strSQL+") "+
                   "group by SH_NO,SH_COLOR order by OD_FGDATE_WEEK2";			
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	do {
		    		List<String[]> ls_SH_COLOR=set_SH_COLOR.get(rs.getInt("OD_FGDATE_WEEK2"));
		    		if(ls_SH_COLOR==null) {
		    			ls_SH_COLOR=new ArrayList<String[]>();
		    			ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
		    			set_SH_COLOR.put(rs.getInt("OD_FGDATE_WEEK2"), ls_SH_COLOR);
		    		}else {
			    		boolean isExist=false;
			    		for(String[] item:ls_SH_COLOR) {
			    			if(item[0].equals(rs.getString("SH_NO")) && item[1].equals(rs.getString("SH_COLOR"))) {
			    				isExist=true;
			    				break;
			    			}
			    		}
			    		if(!isExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});	
		    		}		    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();		    
		    
		    //預排入本周的其它型體
			strSQL="SELECT SH_NO,SH_COLOR,min(OD_FGDATE_WEEK) OD_FGDATE_WEEK2  "+
                   "  FROM FCMPS021 " +
                   " WHERE PROCID = '"+PROCID+"' " +
                   "   AND FA_NO = '"+FA_NO+"' " +
                   "   AND WORK_WEEK<="+WORK_WEEK+
                   (SH_NOT_IN.equals("")?"":" and SH_NO not in ("+SH_NOT_IN+")")+
                   (SPEC_SH_NO.length>0?" and SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
                   "   AND SH_NO not in (select distinct SH_NO from FCMPS007 where PLAN_NO='"+getPLAN_NO()+"' AND PROCID='"+PROCID+"' AND SH_NO=FCMPS021.SH_NO) "+
                   "group by SH_NO,SH_COLOR order by OD_FGDATE_WEEK2";
		
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	do {
		    		List<String[]> ls_SH_COLOR=set_SH_COLOR.get(rs.getInt("OD_FGDATE_WEEK2"));
		    		if(ls_SH_COLOR==null) {
		    			ls_SH_COLOR=new ArrayList<String[]>();
		    			ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
		    			set_SH_COLOR.put(rs.getInt("OD_FGDATE_WEEK2"), ls_SH_COLOR);
		    		}else {
			    		boolean isExist=false;
			    		for(String[] item:ls_SH_COLOR) {
			    			if(item[0].equals(rs.getString("SH_NO")) && item[1].equals(rs.getString("SH_COLOR"))) {
			    				isExist=true;
			    				break;
			    			}
			    		}
			    		if(!isExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});	
		    		}		    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();
		    		  
		    //同型体不同配色的优先顺序及共模型体的优先顺序要以交期先后顺序来排产
		    Iterator<Integer> lt=set_SH_COLOR.keySet().iterator();
		    List<String[]> ls_SH_COLOR=new ArrayList<String[]>();
		    
		    while(lt.hasNext()) {
		    	Integer key=lt.next();
		    	 List<String[]> tmp_SH_COLOR=set_SH_COLOR.get(key);
		    	 for(String[] item:tmp_SH_COLOR) {
		    		 boolean isExist=false;
		    		 for(String[] itemb:ls_SH_COLOR) {
			    		if(item[0].equals(itemb[0]) && item[1].equals(itemb[1])) {
			    			isExist=true;
			    			break;
			    		}
		    		 }
		    		 
		    		 if(!isExist)ls_SH_COLOR.add(new String[] {item[0],item[1]});
		    		 
		    	 }
		    	
		    }

			Current_Calac_Step="預排時排入"+WORK_WEEK+"周的訂單--其它的型體配色"; //當前計算的步驟
			Current_Calac_Color=""; //當前步驟計算的配色名稱
			Step_Calac_Color_Numbers=ls_SH_COLOR.size(); //當前計算的配色總數
			Step_Calac_Color_Seq=0;  //當前計算的配色順序
		    
		    for(Step_Calac_Color_Seq=0;Step_Calac_Color_Seq<ls_SH_COLOR.size();Step_Calac_Color_Seq++) {
		    	String[] arrSH_COLOR=ls_SH_COLOR.get(Step_Calac_Color_Seq);
		    			    			    	
		    	String SH_NO=arrSH_COLOR[0];
		    	String SH_COLOR=arrSH_COLOR[1];
		    	
		    	Current_Calac_Color="Style:"+SH_NO+" Color:"+SH_COLOR;
		    	
				strSQL="select " +
	            "SH_NO,"+
	            "SH_COLOR,"+
	            "PROC_SEQ,"+
	            "SH_SIZE,"+
	            "sum(OD_QTY-nvl(WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)) WORK_PLAN_QTY "+
	            "from FCMPS010 "+
	            "where FCMPS010.SH_NO='"+SH_NO+"'"+
	            "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
	            "  and FCMPS010.FA_NO='"+FA_NO+"'"+
	            "  and FCMPS010.PROCID='"+PROCID+"'"+
	            "  and nvl(OD_CODE,'N')='N' "+
	            "  and IS_DISABLE='N' "+	            
//	            "  and WORK_WEEK_END>="+WORK_WEEK+
//	            "  and WORK_WEEK_END<="+NEXT_FORE_PLAN_WEEK+
	            
	            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+		                   		                   
	            
	            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+
			    
	            //預排時已排入本周
	            "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
	            "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
	            "         FROM FCMPS021 " +
	            "        WHERE WORK_WEEK<="+WORK_WEEK+
	            "          AND SH_NO='"+SH_NO+"'"+
	            "          AND SH_COLOR='"+SH_COLOR+"'"+
	            "          AND PROCID = '"+PROCID+"' " +
	            "          AND FA_NO = '"+FA_NO+"') "+

	            //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
	            //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
	            "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
	            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
	            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
	            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
	            "                       FROM FCPS22_1 "+
	            "                       WHERE NEED_PLAN = 'Y' "+
	            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
	            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
	            "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
	            "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
	            "                     from FCMPS007,FCMPS006 " +
	            "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
	            "                     and FCMPS006.IS_SURE='Y'"+
	            "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
	            "                     and (FCMPS006.WORK_WEEK>="+NEXT_WEEK+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
	            "         or          "+
	            "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
	            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
	            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
	            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
	            "                       FROM FCPS22_1 "+
	            "                       WHERE NEED_PLAN = 'Y' "+
	            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
	            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))"+
	            ") ";		                   
			
			
			   //指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
			   //為了避免因為後關提前導致其前關也提前
			    strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) not in " +
				"             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
	            "               where FA_NO=FCMPS010.FA_NO "+
	            "                 and WORK_WEEK>"+WORK_WEEK+
	            "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
	            "                 and PROCID=FCMPS010.PROCID" +
	            "                 and SH_NO=FCMPS010.SH_NO "+
	            "                 and SH_COLOR=FCMPS010.SH_COLOR "+
	            "                 and SH_SIZE=FCMPS010.SH_SIZE) ";
	            
			    strSQL=strSQL+" group by SH_NO,SH_COLOR,SH_SIZE,PROC_SEQ " ;
			    
				pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs=pstmtData.executeQuery();
			    
			    if(rs.next()){
			    	PROC_WORK_QTY proc_Work_Qty= getCurrent_PROC_WORK_QTY(FA_NO, WORK_WEEK, PROCID, FCMPS_RCCP_INFO_Var.getLs_PROC_WORK_QTY());
			    	double PROC_ALLOW_QTY=proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY();
			    	if(PROC_ALLOW_QTY<SHOOT_MIN_PRODUCE_QTY) {
			    		is_UP_TO_CAP=true;
					    rs.close();
					    pstmtData.close();
			    		return iRet;
			    	}
			    	
			    	double PROC_SEQ=rs.getDouble("PROC_SEQ");
			    	
			    	SH_WORK_QTY sh_Work_Qty=getSH_WORK_PLAN_QTY(FA_NO, SH_NO, PROCID, PROC_SEQ, WORK_WEEK, FCMPS_RCCP_INFO_Var.getLs_SH_WORK_QTY(), FCMPS_PUBLIC.getSH_WorkDaysOfWeek(FA_NO, SH_NO, WORK_WEEK, conn));
			    	double SH_ALLOW_QTY=sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY();
			    	
			    	double OTHER_PROC_ALLOW_QTY=getOther_PROC_Allow_Plan_QTY(FA_NO, SH_NO, PROCID, WORK_WEEK, FCMPS_RCCP_INFO_Var.getLs_PROC_WORK_QTY(), FCMPS_RCCP_INFO_Var.getLs_SH_WORK_QTY());
			    	if(OTHER_PROC_ALLOW_QTY<SH_ALLOW_QTY) SH_ALLOW_QTY=OTHER_PROC_ALLOW_QTY;
			    	
			    	if(SH_ALLOW_QTY<SHOOT_MIN_PRODUCE_QTY) {
					    rs.close();
					    pstmtData.close();
					    continue;
			    	}
			    	
			    	boolean NEED_SHOOT=FCMPS_PUBLIC.getSH_Need_PROC(SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, conn);
			    	double SH_SIZE_ALLOW_QTY=0;
			    	
			    	do {
			    		
			    		PROC_SEQ=rs.getDouble("PROC_SEQ");
			    		String SH_SIZE=rs.getString("SH_SIZE");	
		    			
			    		SH_KEY_SIZE sh_key_size=getSH_SIZE_Allow_Plan_QTY(SH_NO, SH_SIZE, PROCID, PROC_SEQ, FCMPS_RCCP_INFO_Var.getLs_Share_Style_Size(),FCMPS_RCCP_INFO_Var.getLs_SH_KEY_SIZE());
			    		
			    		if(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY()==0) continue;
			    		
			    		if(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY()>rs.getDouble("WORK_PLAN_QTY")) {
				    		SH_SIZE_ALLOW_QTY=SH_SIZE_ALLOW_QTY+rs.getDouble("WORK_PLAN_QTY");
			    		}else {
				    		SH_SIZE_ALLOW_QTY=SH_SIZE_ALLOW_QTY+(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY());
			    		}
			    		
	    		    	
		    			if(NEED_SHOOT) {
			    			if(SH_SIZE_ALLOW_QTY>=SHOOT_MIN_PRODUCE_QTY && 
			    			   SH_ALLOW_QTY>=SHOOT_MIN_PRODUCE_QTY &&
			    			   PROC_ALLOW_QTY>=SHOOT_MIN_PRODUCE_QTY) {

							    iRet=doGeneratePlan_Current_Week_Order(
							    		conn, 
							    		PROCID, 
							    		PLAN_NO, 
							    		FA_NO, 
							    		WORK_WEEK, 
							    		NEXT_FORE_PLAN_WEEK,
							    		SH_NO,
							    		SH_COLOR,
							    		false);	
							    
							    break;			    		    				    				
			    			}
		    			}else {

						    iRet=doGeneratePlan_Current_Week_Order(
						    		conn, 
						    		PROCID, 
						    		PLAN_NO, 
						    		FA_NO, 
						    		WORK_WEEK, 
						    		NEXT_FORE_PLAN_WEEK,
						    		SH_NO,
						    		SH_COLOR,
						    		false);	
						    
						    break;			    				
		    			}
			    		
			    	}while(rs.next());

			    }
			    rs.close();
			    pstmtData.close();
			    
			    if(is_UP_TO_CAP) break;
		    }

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
		
	}
	
    /**
     * 計算本周剩餘的訂單
     * @param conn
     * @param PROCID
     * @param PLAN_NO
     * @param FA_NO
     * @param WORK_WEEK
     * @param SH_NO
     * @param SH_COLOR
     * @param ls_PROC_WORK_QTY
     * @param ls_SH_WORK_QTY
     * @param ls_SH_KEY_SIZE
     * @param ls_SH_COLOR_SIZE
     * @param ls_SH_COLOR_ALLOW_COUNT
     * @param ls_SH_COLOR_QTY
     * @param ls_Share_Style_Size
     * @param ls_SH_NEED_SHOOT
     * @param ls_SH_USE_CAP
     * @param ls_SH_NEED_PLAN_PROC
     * @param is_Must
     * @param SPEC_SH_NO
     * @return
     */
	private int doGeneratePlan_Current_Week_Order(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int WORK_WEEK,
    		int NEXT_FORE_PLAN_WEEK,
    		String SH_NO,
    		String SH_COLOR,
    		boolean is_Spec_Week_Plan
    		) {
    	int iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
				
		try {
	    	
    		int NEXT_WEEK=WORK_WEEK;
    		do {
    			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1),conn)==0) {
    				NEXT_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1);
    			}else {
    				break;
    			}
    			
    		}while(true);
			
			strSQL="select " +
            "FCMPS010.OD_PONO1, "+
            "FCMPS010.STYLE_NO,"+
            "FCMPS010.SH_NO,"+
            "FCMPS010.SH_COLOR,"+
            "FCMPS010.SH_SIZE,"+
            "FCMPS010.PROC_SEQ,"+
            "FCMPS010.OD_QTY,"+
            "FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0) WORK_PLAN_QTY,"+
            "FCMPS010.WORK_WEEK_START,"+
            "FCMPS010.WORK_WEEK_END,"+
            "FCMPS010.OD_SHIP,"+
            "to_char(FCMPS010.OD_SHIP,'IYIW') od_ship_week,"+
            "FCMPS010.OD_FGDATE, "+			
            "to_char(FCMPS010.OD_FGDATE,'IYIW') od_fgdate_week "+
            "from FCMPS010 "+
            "where FCMPS010.SH_NO='"+SH_NO+"'"+
            "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
            "  and FCMPS010.PROCID='"+PROCID+"'"+
            "  and FCMPS010.FA_NO='"+FA_NO+"'"+                
            "  and nvl(OD_CODE,'N')='N' "+
            "  and IS_DISABLE='N' "+            
//            "  and WORK_WEEK_END>="+WORK_WEEK+
//            "  and WORK_WEEK_END<="+NEXT_FORE_PLAN_WEEK+
            
            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+		                   		                   
            
            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+
		
            "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
            "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
            "       FROM FCMPS021 " +
            "      WHERE SH_NO='" +SH_NO+"'"+
            "        AND SH_COLOR = '"+SH_COLOR+"' " +
            "        AND PROCID = '"+PROCID+"' " +
            "        AND FA_NO = '"+FA_NO+"' " +
            "        AND WORK_WEEK<="+WORK_WEEK+") "+

               //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
            //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
            "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
            "                       FROM FCPS22_1 "+
            "                       WHERE NEED_PLAN = 'Y' "+
            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
            "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
            "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
            "                     from FCMPS007,FCMPS006 " +
            "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
            "                     and FCMPS006.IS_SURE='Y'"+
            "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
            "                     and (FCMPS006.WORK_WEEK>="+getWORK_WEEK()+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
            "         or          "+
            "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
            "                       FROM FCPS22_1 "+
            "                       WHERE NEED_PLAN = 'Y' "+
            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))) ";		                   
		
//            if(!is_Must) {
            strSQL=strSQL+"  and (SH_NO,SH_COLOR,SH_SIZE) NOT IN " +
            	   "                  (SELECT SH_NO,SH_COLOR,SH_SIZE FROM FCMPS013 " +
	    	       "                    WHERE FA_NO='"+FA_NO+"' " +
	    	       "                      AND WORK_WEEK="+getWORK_WEEK()+" " +
                   "                      AND SH_NO = '"+SH_NO+"' " +
                   "                      AND SH_COLOR = '"+SH_COLOR+"' " +
	    	       "                      AND PROCID='"+PROCID+"' " +
	    	       "                      AND ALLOW_APPEND='N')";
//            }
		
			//指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
			//為了避免因為後關提前導致其前關也提前
			strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) not in " +
				   "             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
	               "               where FA_NO='"+FA_NO+"' " +
	               "                 and WORK_WEEK>"+getWORK_WEEK()+
//	               "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
//	               "                 and PROCID=FCMPS010.PROCID" +
//	               "                 and SH_NO=FCMPS010.SH_NO "+
//	               "                 and SH_COLOR=FCMPS010.SH_COLOR "+
//	               "                 and SH_SIZE=FCMPS010.SH_SIZE" +
	               "              ) "; 					
						
			if(getPLAN_PRIORITY_TYPE().equals("1")) {
				if(getPLAN_BY_DATE().equals("A")) {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP ";
				}else {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE ";
				}
			}
		
			if(getPLAN_PRIORITY_TYPE().equals("2")) {
				strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.WORK_WEEK_START";
			}
			
			if(getPLAN_PRIORITY_TYPE().equals("3")) {
				if(getPLAN_BY_DATE().equals("A")) {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP,FCMPS010.WORK_WEEKS ";
				}else {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE,FCMPS010.WORK_WEEKS ";
				}
			}

//			System.out.println(strSQL);
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
			rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			rs.setFetchSize(3000);
		    
		    if(rs.next()){
	    		FCMPS_CLS_GenerateOrderPlan_MultiThread cls_GenerateOrderPlan=new FCMPS_CLS_GenerateOrderPlan_MultiThread();
		    	cls_GenerateOrderPlan.setFA_NO(FA_NO);
		    	cls_GenerateOrderPlan.setPLAN_BY_DATE(getPLAN_BY_DATE());
		    	cls_GenerateOrderPlan.setPLAN_NO(PLAN_NO);
		    	cls_GenerateOrderPlan.setWORK_WEEK(getWORK_WEEK());
		    	cls_GenerateOrderPlan.setConnection(conn);
		    	cls_GenerateOrderPlan.setIs_FORE_PLAN_WEEKS(is_FORE_PLAN_WEEKS);
		    	cls_GenerateOrderPlan.setFORE_PLAN_WEEKS(getFORE_PLAN_WEEKS());
		    	cls_GenerateOrderPlan.setUP_USER(getUP_USER());
		    	cls_GenerateOrderPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
		    	cls_GenerateOrderPlan.setConfig_xml(config_xml);
		    	cls_GenerateOrderPlan.setPER_SIZE_ALLOW_OVER_NUM(PER_SIZE_ALLOW_OVER_NUM);
		    	cls_GenerateOrderPlan.setFCMPS_RCCP_INFO_Var(FCMPS_RCCP_INFO_Var);
		    	
		    	boolean iResult=cls_GenerateOrderPlan.doGeneratePlan(rs,PROCID,is_Spec_Week_Plan);			    	
		    	
		    	if(!cls_GenerateOrderPlan.getMessage().isEmpty())setMessage(cls_GenerateOrderPlan.getMessage());
		    	if(!iResult) {
		    		System.out.println("計算剩餘訂單--已達到最晚完工周次必須要排--出現錯誤退出!");
				    iRet=-1;
		    	}
		    	
		    	//當達到本周最大產能,退出
	    		if(cls_GenerateOrderPlan.is_UP_TO_CAP()) {
	    			is_UP_TO_CAP=true;
	    			System.out.println("達到最大周產能,退出!");
	    			iRet=1;
	    		}	 		    	
		    }
		    rs.close();
		    pstmtData.close();			    	    	   

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
		
	}
	
	/**
	 * 計算未排滿可提前排訂單--提前將已排入本周的型體配色
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param WORK_WEEK
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @param ls_SH_KEY_SIZE
	 * @param ls_SH_COLOR_SIZE
	 * @param ls_Share_Style_Size
	 * @param SH_NO
	 * @return
	 */
	private int doGeneratePlan_Bring_In_Planned_Color_Size(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int WORK_WEEK,
    		String... SH_NO
    		) {
    	int iRet=0;
    	
    	if(!is_FORE_PLAN_WEEKS || getFORE_PLAN_WEEKS()==0) return iRet;	
    	
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		try {
			
			ArrayList<String[]> ls_SH_COLOR=new ArrayList<String[]>();
			
    		if(!FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().isEmpty()) {
    			SH_NOT_IN="";
	    		for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().size();i++) {
	    			if(!SH_NOT_IN.equals(""))SH_NOT_IN=SH_NOT_IN+",";
	    			SH_NOT_IN=SH_NOT_IN+"'"+FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().get(i)+"'";
	    		}	
    		}
			
    		int NEXT_WEEK=getWORK_WEEK();
    		do {
    			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1),conn)==0) {
    				NEXT_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_WEEK,1);
    			}else {
    				break;
    			}
    			
    		}while(true);
    		
    		
    		//已排本周的同型體,同配色,同size
		    strSQL="select SH_NO,SH_COLOR,SH_SIZE,sum(WORK_PLAN_QTY) WORK_PLAN_QTY2 " +
		    		"from FCMPS007 " +
		    		"where PLAN_NO='"+getPLAN_NO()+"' "+
		    		(SH_NOT_IN.equals("")?"":" and SH_NO not in ("+SH_NOT_IN+") ")+
		    		(SH_NO.length>0?" and SH_NO IN ("+SH_NO[0]+") ":" ")+
		    		"group by SH_NO,SH_COLOR,SH_SIZE "+
		    		"order by sum(WORK_PLAN_QTY)";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    if(rs.next()){
		    	do {
		    		ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR"),rs.getString("SH_SIZE")});
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();

//		    int Next_Week=FCMPS_PUBLIC.getNext_Week(getWORK_WEEK(), getFORE_PLAN_WEEKS()-1);
		    
    		int NEXT_FORE_PLAN_WEEK=WORK_WEEK;

    		for(int iWeek=1;iWeek<getFORE_PLAN_WEEKS();iWeek++) {
    			NEXT_FORE_PLAN_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_FORE_PLAN_WEEK, 1);
        		do {
        			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,NEXT_FORE_PLAN_WEEK,conn)==0) {
        				NEXT_FORE_PLAN_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_FORE_PLAN_WEEK,1);
        			}else {
        				break;
        			}
        			
        		}while(true);
    		}
    		
		    //已排本周的同型體,同配色,不同size
		    strSQL="select " +
                   "SH_NO,"+
                   "SH_COLOR,"+
                   "SH_SIZE, "+
                   "min(WORK_WEEK_START) OD_FGDATE_WEEK "+
                   "from FCMPS010 "+
                   "where FCMPS010.PROCID='"+PROCID+"'"+
                   "  and FCMPS010.FA_NO='"+FA_NO+"'"+
                   "  and nvl(OD_CODE,'N')='N' "+
                   "  and IS_DISABLE='N' "+
                   "  and WORK_WEEK_END>="+getWORK_WEEK()+
                   "  and WORK_WEEK_END<="+NEXT_FORE_PLAN_WEEK+
                   
                   (SH_NO.length>0?" and SH_NO IN ("+SH_NO[0]+") ":" ")+
                   "  and (SH_NO,SH_COLOR) IN " +
                   "      (SELECT SH_NO,SH_COLOR from FCMPS007 where PLAN_NO='"+getPLAN_NO()+"' group by SH_NO,SH_COLOR)"+            
            
                   (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
			                   
                   "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                   
			
//                   "  and FCMPS010.WORK_WEEK_START<="+Next_Week+" "+
                   "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
                   "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
                   "         FROM FCMPS021 " +
                   "        WHERE PROCID = '"+PROCID+"' " +
                   "          AND FA_NO = '"+FA_NO+"' " +
                   "          AND WORK_WEEK<="+NEXT_FORE_PLAN_WEEK+") "+
                   
                   "group by SH_NO,SH_COLOR,SH_SIZE "+
                   "order by OD_FGDATE_WEEK";

			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    if(rs.next()){
		    	do {
		    		boolean iExist=false;
		    		for(int i=0;i<ls_SH_COLOR.size();i++) {
		    			if(rs.getString("SH_NO").equals(ls_SH_COLOR.get(i)[0]) && 
		    			   rs.getString("SH_COLOR").equals(ls_SH_COLOR.get(i)[1]) && 
		    			   rs.getString("SH_SIZE").equals(ls_SH_COLOR.get(i)[2])) {
		    				iExist=true;
		    				break;
		    			}
		    		}
		    		if(!iExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR"),rs.getString("SH_SIZE")});
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();
		    		    		    
		    String SH_NO2="";
		    
			Current_Calac_Color=""; //當前步驟計算的配色名稱
			Step_Calac_Color_Numbers=ls_SH_COLOR.size(); //當前計算的配色總數
			Step_Calac_Color_Seq=0;  //當前計算的配色順序
		    
		    for(Step_Calac_Color_Seq=0;Step_Calac_Color_Seq<ls_SH_COLOR.size();Step_Calac_Color_Seq++) {
		    	SH_NO2=ls_SH_COLOR.get(Step_Calac_Color_Seq)[0];
        			        		
	    		if(!FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().isEmpty()) {
	    			boolean isFinished=false;
		    		for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().size();i++) {
		    			if(SH_NO2.equals(FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().get(i))) {
		    				isFinished=true;
		    				break;
		    			}
		    		}
		    		if(isFinished) continue;
	    		}
	    		
		    	String SH_COLOR2=ls_SH_COLOR.get(Step_Calac_Color_Seq)[1];
		    	String SH_SIZE2=ls_SH_COLOR.get(Step_Calac_Color_Seq)[2];
		    	
		    	Current_Calac_Color="Style:"+SH_NO2+" Color:"+SH_COLOR2;		    	
		    	
			    strSQL="select " +
                "FCMPS010.OD_PONO1, "+
                "FCMPS010.STYLE_NO,"+
                "FCMPS010.SH_NO,"+
                "FCMPS010.SH_COLOR,"+
                "FCMPS010.SH_SIZE,"+
                "FCMPS010.PROC_SEQ,"+
                "FCMPS010.OD_QTY,"+
                "FCMPS010.OD_QTY-" +
                "nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0) WORK_PLAN_QTY,"+
                "FCMPS010.WORK_WEEK_START,"+
                "FCMPS010.WORK_WEEK_END,"+
                "FCMPS010.OD_SHIP,"+
                "to_char(FCMPS010.OD_SHIP,'IYIW') od_ship_week,"+
                "FCMPS010.OD_FGDATE, "+			
                "to_char(FCMPS010.OD_FGDATE,'IYIW') od_fgdate_week "+
                "from FCMPS010 "+
                "where FCMPS010.PROCID='"+PROCID+"'"+
                "  and FCMPS010.FA_NO='"+FA_NO+"'"+
                "  and nvl(OD_CODE,'N')='N' "+
                "  and IS_DISABLE='N' "+
                "  and FCMPS010.SH_NO='"+SH_NO2+"'"+
                "  and FCMPS010.SH_COLOR='"+SH_COLOR2+"'"+
                "  and FCMPS010.SH_SIZE='"+SH_SIZE2+"'"+
                "  and FCMPS010.WORK_WEEK_END>="+getWORK_WEEK()+
                "  and FCMPS010.WORK_WEEK_END<="+NEXT_FORE_PLAN_WEEK+
                
                
                (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
  			                   
                "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                                
   
                "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
                "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
                "         FROM FCMPS021 " +
                "        WHERE PROCID = '"+PROCID+"' " +
                "          AND FA_NO = '"+FA_NO+"' " +
                "          AND SH_NO = '"+SH_NO2+"' " +
                "          AND SH_COLOR = '"+SH_COLOR2+"' " +
                "          AND WORK_WEEK<="+NEXT_FORE_PLAN_WEEK+") "+
                
	            //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
                //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
                "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
                "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
                "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
                "                      (SELECT SH_ARITCLE, PROC_SEQ "+
                "                       FROM FCPS22_1 "+
                "                       WHERE NEED_PLAN = 'Y' "+
                "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
                "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
                "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
                "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
                "                     from FCMPS007,FCMPS006 " +
                "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
                "                     and FCMPS006.IS_SURE='Y'"+
                "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
                "                     and (FCMPS006.WORK_WEEK>="+getWORK_WEEK()+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
                "         or          "+
                "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
                "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
                "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
                "                      (SELECT SH_ARITCLE, PROC_SEQ "+
                "                       FROM FCPS22_1 "+
                "                       WHERE NEED_PLAN = 'Y' "+
                "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
                "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))) "+
                
	    		"  and (FCMPS010.SH_NO,FCMPS010.SH_COLOR,FCMPS010.SH_SIZE) NOT IN (" +
	    		"                   SELECT SH_NO,SH_COLOR,SH_SIZE FROM FCMPS013 " +
	    		"                    WHERE FA_NO='"+FA_NO+"' " +
	    	    "                      AND WORK_WEEK="+getWORK_WEEK()+" " +
	    	    "                      AND SH_NO='"+SH_NO2+"'"+
	    	    "                      AND PROCID='"+PROCID+"' " +
	    	    "                      AND ALLOW_APPEND='N')";
	                   	      								
              //指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
              //為了避免因為後關提前導致其前關也提前
			    strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID)not in" +
                "             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
                "               where FA_NO='"+FA_NO+"' " +
                "                 and WORK_WEEK>"+getWORK_WEEK()+
//                "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
//                "                 and PROCID=FCMPS010.PROCID" +
//                "                 and SH_NO=FCMPS010.SH_NO "+
//                "                 and SH_COLOR=FCMPS010.SH_COLOR "+
//                "                 and SH_SIZE=FCMPS010.SH_SIZE" +
                "              ) "; 
							 					
				if(getPLAN_PRIORITY_TYPE().equals("1")) {
					if(getPLAN_BY_DATE().equals("A")) {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP ";
					}else {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE ";
					}
				}
			
				if(getPLAN_PRIORITY_TYPE().equals("2")) {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.WORK_WEEK_START";
				}
				
				if(getPLAN_PRIORITY_TYPE().equals("3")) {
					if(getPLAN_BY_DATE().equals("A")) {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP,FCMPS010.WORK_WEEKS ";
					}else {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE,FCMPS010.WORK_WEEKS ";
					}
				}
				
//				System.out.println(strSQL);
				pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs3=pstmtData3.executeQuery();
			    
				rs3.setFetchDirection(ResultSet.FETCH_FORWARD);
				rs3.setFetchSize(3000);
			    
			    if(rs3.next()){
			    	FCMPS_CLS_GenerateOrderPlan_MultiThread cls_GenerateOrderPlan=new FCMPS_CLS_GenerateOrderPlan_MultiThread();
			    	cls_GenerateOrderPlan.setFA_NO(FA_NO);
			    	cls_GenerateOrderPlan.setPLAN_BY_DATE(getPLAN_BY_DATE());
			    	cls_GenerateOrderPlan.setPLAN_NO(PLAN_NO);
			    	cls_GenerateOrderPlan.setWORK_WEEK(getWORK_WEEK());
			    	cls_GenerateOrderPlan.setConnection(conn);
			    	cls_GenerateOrderPlan.setIs_FORE_PLAN_WEEKS(is_FORE_PLAN_WEEKS);
			    	cls_GenerateOrderPlan.setFORE_PLAN_WEEKS(getFORE_PLAN_WEEKS());
			    	cls_GenerateOrderPlan.setUP_USER(getUP_USER());
			    	cls_GenerateOrderPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
			    	cls_GenerateOrderPlan.setConfig_xml(config_xml);
			    	cls_GenerateOrderPlan.setPER_SIZE_ALLOW_OVER_NUM(PER_SIZE_ALLOW_OVER_NUM);
			    	cls_GenerateOrderPlan.setFCMPS_RCCP_INFO_Var(FCMPS_RCCP_INFO_Var);
			    	
			    	boolean iResult=cls_GenerateOrderPlan.doGeneratePlan(rs3,PROCID,false);			    	
			    	
			    	if(!cls_GenerateOrderPlan.getMessage().isEmpty())setMessage(cls_GenerateOrderPlan.getMessage());
			    	if(!iResult) {
			    		System.out.println("計算提前排產訂單出現錯誤退出!");
					    iRet=-1;
			    	}
			    	
			    	//當達到本周最大產能,退出
		    		if(cls_GenerateOrderPlan.is_UP_TO_CAP()) {
		    			is_UP_TO_CAP=true;
		    			System.out.println("達到最大周產能,退出!");
		    			iRet=1;
		    		}	 		    	
			    }
			    rs3.close();
			    pstmtData3.close();	    
			    
			    if(iRet!=0) break;
		
		    }	

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
	}
	

	/**
	 * 計算未排滿可提前排訂單--提前將已排入本周且可以在本周排滿516的型體
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param WORK_WEEK
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @param ls_SH_KEY_SIZE
	 * @param ls_SH_COLOR_SIZE
	 * @param ls_SH_COLOR_ALLOW_COUNT
	 * @param ls_SH_COLOR_QTY
	 * @param ls_Share_Style_Size
	 * @param ls_SH_NEED_SHOOT
	 * @param ls_SH_USE_CAP
	 * @param ls_SH_NEED_PLAN_PROC
	 * @param ls_Not_Again_Plan
	 * @param SH_NO
	 * @return
	 */
	private int doGeneratePlan_Bring_In_Planned_SH(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int NEXT_WEEK,
    		String... SPEC_SH_NO
    		) {

    	int iRet=0;
    	
    	if(!is_FORE_PLAN_WEEKS || getFORE_PLAN_WEEKS()==0) return iRet;	
    	
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try {
			
    		if(!FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().isEmpty()) {
    			SH_NOT_IN="";
	    		for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().size();i++) {
	    			if(!SH_NOT_IN.equals(""))SH_NOT_IN=SH_NOT_IN+",";
	    			SH_NOT_IN=SH_NOT_IN+"'"+FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().get(i)+"'";
	    		}	
    		}    		
    		
    		Map<Integer,List<String[]>> set_SH_COLOR=new TreeMap<Integer,List<String[]>>();
    		
    		//同型體,不同配色
			strSQL="SELECT SH_NO,SH_COLOR,min(WORK_WEEK) OD_FGDATE_WEEK2 "+
                   "  FROM FCMPS021 " +
                   " WHERE PROCID = '"+PROCID+"' " +
                   "   AND FA_NO = '"+FA_NO+"' " +
                   "   AND WORK_WEEK<="+NEXT_WEEK+
                   (SPEC_SH_NO.length>0?" and SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
                   (SH_NOT_IN.equals("")?"":" and SH_NO not in ("+SH_NOT_IN+")")+
                   "   AND (SH_NO,SH_COLOR) not in "+
                   "       (select distinct SH_NO,SH_COLOR from FCMPS007 where PLAN_NO='"+getPLAN_NO()+"' AND SH_NO=FCMPS021.SH_NO AND SH_COLOR=FCMPS021.SH_COLOR)"+
                   "   AND SH_NO in (select distinct SH_NO from FCMPS007 where PLAN_NO='"+getPLAN_NO()+"' AND SH_NO=FCMPS021.SH_NO) "+
                   "group by SH_NO,SH_COLOR order by OD_FGDATE_WEEK2";
		    
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	do {
//		    		ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});		
		    		List<String[]> ls_SH_COLOR=set_SH_COLOR.get(rs.getInt("OD_FGDATE_WEEK2"));
		    		if(ls_SH_COLOR==null) {
		    			ls_SH_COLOR=new ArrayList<String[]>();
		    			ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
		    			set_SH_COLOR.put(rs.getInt("OD_FGDATE_WEEK2"), ls_SH_COLOR);
		    		}else {
			    		boolean isExist=false;
			    		for(String[] item:ls_SH_COLOR) {
			    			if(item[0].equals(rs.getString("SH_NO")) && item[1].equals(rs.getString("SH_COLOR"))) {
			    				isExist=true;
			    				break;
			    			}
			    		}
			    		if(!isExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});	
		    		}
		    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();		    
		    
		    
		    //共模型體		    
			strSQL="SELECT SH_NO,SH_COLOR,min(WORK_WEEK) OD_FGDATE_WEEK2 "+
                   "  FROM FCMPS021 " +
                   " WHERE PROCID = '"+PROCID+"' " +
                   "   AND FA_NO = '"+FA_NO+"' " +
                   "   AND WORK_WEEK<="+NEXT_WEEK+
                   (SPEC_SH_NO.length>0?" and SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
                   (SH_NOT_IN.equals("")?"":" and SH_NO not in ("+SH_NOT_IN+")")+
                   "   AND (SH_NO,SH_SIZE) in (" +
                   "     select distinct sh_no2,sh_size2 from fcmps0022 "+
                   "     where (sh_no,sh_size) in (select distinct sh_no,sh_size from FCMPS007 where PLAN_NO = '"+getPLAN_NO()+"')) "+
                   "group by SH_NO,SH_COLOR order by OD_FGDATE_WEEK2";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	do {
		    		List<String[]> ls_SH_COLOR=set_SH_COLOR.get(rs.getInt("OD_FGDATE_WEEK2"));
		    		if(ls_SH_COLOR==null) {
		    			ls_SH_COLOR=new ArrayList<String[]>();
		    			ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
		    			set_SH_COLOR.put(rs.getInt("OD_FGDATE_WEEK2"), ls_SH_COLOR);
		    		}else {
			    		boolean isExist=false;
			    		for(String[] item:ls_SH_COLOR) {
			    			if(item[0].equals(rs.getString("SH_NO")) && item[1].equals(rs.getString("SH_COLOR"))) {
			    				isExist=true;
			    				break;
			    			}
			    		}
			    		if(!isExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});	
		    		}		    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();
		    
		    
		    //同型体不同配色的优先顺序及共模型体的优先顺序要以交期先后顺序来排产
		    Iterator<Integer> lt=set_SH_COLOR.keySet().iterator();
		    List<String[]> ls_SH_COLOR=new ArrayList<String[]>();
		    
		    while(lt.hasNext()) {
		    	Integer key=lt.next();
		    	 List<String[]> tmp_SH_COLOR=set_SH_COLOR.get(key);
		    	 for(String[] item:tmp_SH_COLOR) {
		    		 boolean isExist=false;
		    		 for(String[] itemb:ls_SH_COLOR) {
			    		if(item[0].equals(itemb[0]) && item[1].equals(itemb[1])) {
			    			isExist=true;
			    			break;
			    		}
		    		 }
		    		 
		    		 if(!isExist)ls_SH_COLOR.add(new String[] {item[0],item[1]});
		    		 
		    	 }
		    	
		    }
		    
			Current_Calac_Color=""; //當前步驟計算的配色名稱
			Step_Calac_Color_Numbers=ls_SH_COLOR.size(); //當前計算的配色總數
			Step_Calac_Color_Seq=0;  //當前計算的配色順序
			
		    for(Step_Calac_Color_Seq=0;Step_Calac_Color_Seq<ls_SH_COLOR.size();Step_Calac_Color_Seq++) {
		    	String[] arrSH_COLOR=ls_SH_COLOR.get(Step_Calac_Color_Seq);
		    	
		    	String SH_NO=arrSH_COLOR[0];
		    	String SH_COLOR=arrSH_COLOR[1];
		    	
		    	Current_Calac_Color="Style:"+SH_NO+" Color:"+SH_COLOR;
		    	
				strSQL="select " +
				"      SH_NO," +
				"      SH_COLOR," +
				"      PROC_SEQ," +
				"      SH_SIZE," +
				"      min(WORK_WEEK_START) OD_FGDATE_WEEK,"+
				"      SUM(OD_QTY-nvl(WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)) WORK_PLAN_QTY "+
	            "from FCMPS010 "+
	            "where FCMPS010.SH_NO='"+SH_NO+"'"+
	            "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
	            "  and FCMPS010.PROCID='"+PROCID+"'"+
	            "  and FCMPS010.FA_NO='"+FA_NO+"'"+
	            "  and nvl(OD_CODE,'N')='N' "+
	            "  and IS_DISABLE='N' "+
                "  and WORK_WEEK_END>="+getWORK_WEEK()+
                "  and WORK_WEEK_END<="+NEXT_WEEK+
	            
	            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
	            (SPEC_SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
	            (SH_NOT_IN.equals("")?"":" and FCMPS010.SH_NO NOT IN ("+SH_NOT_IN+") ")+ //不重排計劃時,需要將已排滿的型體排除在外
	     
	            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                   			                            			       

	            "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
	            "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
	            "         FROM FCMPS021 " +
	            "        WHERE PROCID = '"+PROCID+"' " +
	            "          AND FA_NO = '"+FA_NO+"' " +
	            "          AND SH_NO = '"+SH_NO+"' " +
	            "          AND SH_COLOR = '"+SH_COLOR+"' " +
	            "          AND WORK_WEEK<="+NEXT_WEEK+") "+
	            	            
	    		"  and (FA_NO,SH_NO,SH_COLOR,SH_SIZE) NOT IN " +
	    		"      (SELECT FA_NO,SH_NO,SH_COLOR,SH_SIZE FROM FCMPS013 " +
	    		"       WHERE FA_NO='"+FA_NO+"' " +
	    		"         AND SH_NO='"+SH_NO+"'"+
	    		"         AND SH_COLOR='"+SH_COLOR+"'"+
	    	    "         AND WORK_WEEK="+getWORK_WEEK()+" " +
	    	    "         AND PROCID='"+PROCID+"' " +
	    	    "         AND ALLOW_APPEND='N')"+
				
	            //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
	            //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
	            "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
	            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
	            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
	            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
	            "                       FROM FCPS22_1 "+
	            "                       WHERE NEED_PLAN = 'Y' "+
	            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
	            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
	            "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
	            "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
	            "                     from FCMPS007,FCMPS006 " +
	            "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
	            "                     and FCMPS006.IS_SURE='Y'"+
	            "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
	            "                     and (FCMPS006.WORK_WEEK>="+getWORK_WEEK()+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
	            "         or          "+
	            "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
	            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
	            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
	            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
	            "                       FROM FCPS22_1 "+
	            "                       WHERE NEED_PLAN = 'Y' "+
	            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
	            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))) ";
	                               	      								
	            //指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
	            //為了避免因為後關提前導致其前關也提前
		        strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID)not in" +
		                 "             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
	                     "               where FA_NO='"+FA_NO+"' " +
	                     "                 and WORK_WEEK>"+getWORK_WEEK()+
//	                     "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
//	                     "                 and PROCID=FCMPS010.PROCID" +
//	                     "                 and SH_NO=FCMPS010.SH_NO "+
//	                     "                 and SH_COLOR=FCMPS010.SH_COLOR "+
//	                     "                 and SH_SIZE=FCMPS010.SH_SIZE" +
	                     "              ) ";			
				
				strSQL=strSQL+ " group by SH_NO,SH_COLOR,SH_SIZE,PROC_SEQ order by OD_FGDATE_WEEK";			
				
				pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs=pstmtData.executeQuery();
			    if(rs.next()){

			    	PROC_WORK_QTY proc_Work_Qty= getCurrent_PROC_WORK_QTY(FA_NO, getWORK_WEEK(), PROCID, FCMPS_RCCP_INFO_Var.getLs_PROC_WORK_QTY());
			    	double PROC_ALLOW_QTY=proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY();
			    	if(PROC_ALLOW_QTY<SHOOT_MIN_PRODUCE_QTY) {
			    		is_UP_TO_CAP=true;
					    rs.close();
					    pstmtData.close();
			    		return iRet;
			    	}
			    	
			    	double PROC_SEQ=rs.getDouble("PROC_SEQ");
			    	
			    	SH_WORK_QTY sh_Work_Qty=getSH_WORK_PLAN_QTY(FA_NO, SH_NO, PROCID, PROC_SEQ, getWORK_WEEK(),FCMPS_RCCP_INFO_Var.getLs_SH_WORK_QTY(), FCMPS_PUBLIC.getSH_WorkDaysOfWeek(FA_NO, SH_NO, getWORK_WEEK(), conn));
			    	double SH_ALLOW_QTY=sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY();

			    	double OTHER_PROC_ALLOW_QTY=getOther_PROC_Allow_Plan_QTY(FA_NO, SH_NO, PROCID, getWORK_WEEK(), FCMPS_RCCP_INFO_Var.getLs_PROC_WORK_QTY(), FCMPS_RCCP_INFO_Var.getLs_SH_WORK_QTY());
			    	if(OTHER_PROC_ALLOW_QTY<SH_ALLOW_QTY) SH_ALLOW_QTY=OTHER_PROC_ALLOW_QTY;
			    	if(SH_ALLOW_QTY<SHOOT_MIN_PRODUCE_QTY) {
					    rs.close();
					    pstmtData.close();
			    		continue;
			    	}
			    				    	
		    		String SH_SIZE=rs.getString("SH_SIZE");
		    		
			    	boolean NEED_SHOOT=NEED_SHOOT=FCMPS_PUBLIC.getSH_Need_PROC(SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, conn);
			    	double SH_SIZE_ALLOW_QTY=0;
			    	
			    	do {
			    		
			    		PROC_SEQ=rs.getDouble("PROC_SEQ");
			    		SH_SIZE=rs.getString("SH_SIZE");
		    	    	
		    	    	SH_KEY_SIZE sh_key_size=getSH_SIZE_Allow_Plan_QTY(SH_NO, SH_SIZE, PROCID, PROC_SEQ, FCMPS_RCCP_INFO_Var.getLs_Share_Style_Size(),FCMPS_RCCP_INFO_Var.getLs_SH_KEY_SIZE());
		    	    	if(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY()==0) continue;
		    	    	
			    		if(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY()>rs.getDouble("WORK_PLAN_QTY")) {
			    			SH_SIZE_ALLOW_QTY=SH_SIZE_ALLOW_QTY+rs.getDouble("WORK_PLAN_QTY");
			    		}else {
			    			SH_SIZE_ALLOW_QTY=SH_SIZE_ALLOW_QTY+(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY());
			    		}
		    	    	
		    			if(NEED_SHOOT) {
			    			if(SH_SIZE_ALLOW_QTY>=SHOOT_MIN_PRODUCE_QTY && 
			    			   SH_ALLOW_QTY>=SHOOT_MIN_PRODUCE_QTY &&
			    			   PROC_ALLOW_QTY>=SHOOT_MIN_PRODUCE_QTY
			    			   ) {
//			    				System.out.println(NEXT_WEEK+" "+SH_NO+" "+SH_COLOR+" "+SH_SIZE_ALLOW_QTY);
			    			    iRet=doGeneratePlan_Non_Bring_In_Planned_Color_Size(
			    			    		conn, 
			    			    		PROCID, 
			    			    		PLAN_NO,
			    			    		FA_NO,
			    			    		NEXT_WEEK,
			    			    		SH_NO,
			    			    		SH_COLOR);		
			    			    
			    			    break;
			    			}
		    			}else {
//		    				System.out.println(SH_NO+" "+SH_COLOR+" "+SH_SIZE_ALLOW_QTY);
		    				
		    			    iRet=doGeneratePlan_Non_Bring_In_Planned_Color_Size(
		    			    		conn, 
		    			    		PROCID, 
		    			    		PLAN_NO,
		    			    		FA_NO,
		    			    		NEXT_WEEK,
		    			    		SH_NO,
		    			    		SH_COLOR);	
		    			    
		    			    break;
		    			}
			    		
			    	}while(rs.next());
			    		 		    				    	
			    }
			    rs.close();
			    pstmtData.close();		
			    
			    if(is_UP_TO_CAP) break;
		    }
		    
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
			
	}
	
	/**
	 * 計算未排滿可提前排訂單--提前沒有排入本周但必須是可以在本周排完的型體配色SIZE 
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param NEXT_WEEK
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @param ls_SH_KEY_SIZE
	 * @param ls_SH_COLOR_SIZE
	 * @param ls_Share_Style_Size
	 * @param SH_NO
	 * @return
	 */
	private int doGeneratePlan_Non_Bring_In_Planned_Color_Size(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int NEXT_WEEK,
    		String... SPEC_SH_NO
    		) {
    	int iRet=0;
    	
    	if(!is_FORE_PLAN_WEEKS || getFORE_PLAN_WEEKS()==0) return iRet;	
    	
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try {			
			
			Map<Integer,List<String[]>> set_SH_COLOR=new TreeMap<Integer,List<String[]>>();
			
			strSQL="select SH_NO,SH_COLOR,min(WORK_WEEK_START) OD_FGDATE_WEEK "+
            "from FCMPS010 "+
            "where FCMPS010.PROCID='"+PROCID+"'"+
            "  and FCMPS010.FA_NO='"+FA_NO+"'"+
            "  and nvl(OD_CODE,'N')='N' "+
            "  and IS_DISABLE='N' "+
            "  and WORK_WEEK_END>="+getWORK_WEEK()+
            "  and WORK_WEEK_END<="+NEXT_WEEK+
            
            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
            (SPEC_SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
            (SH_NOT_IN.equals("")?"":" and FCMPS010.SH_NO NOT IN ("+SH_NOT_IN+") ")+ //不重排計劃時,需要將已排滿的型體排除在外
                 
            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                   			                            			            

            "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
            "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
            "         FROM FCMPS021 " +
            "        WHERE PROCID = '"+PROCID+"' " +
            "          AND FA_NO = '"+FA_NO+"' " +
//            "          AND OD_PONO1=FCMPS010.OD_PONO1 " +
//            "          AND SH_NO=FCMPS010.SH_NO " +
//            "          AND SH_COLOR=FCMPS010.SH_COLOR " +
//            "          AND SH_SIZE=FCMPS010.SH_SIZE " +
//            "          AND PROCID=FCMPS010.PROCID " +            
            "          AND WORK_WEEK<="+NEXT_WEEK+") "+
                        
            // 只提前本周計劃共模的型體配色
            "  and (SH_NO,FA_NO) IN (SELECT distinct SH_NO,FA_NO FROM FCMPS0022 " +
            "                 WHERE SH_NO2=FCMPS010.SH_NO)"+
            
            // 只提前本周計劃未排的型體配色
            "  and (FA_NO,SH_NO,SH_COLOR) NOT IN "+
            "      (SELECT FCMPS006.FA_NO,FCMPS007.SH_NO,FCMPS007.SH_COLOR "+
            "       FROM FCMPS007,FCMPS006 " +
            "       WHERE FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
            "         and FCMPS006.PLAN_NO='"+getPLAN_NO()+"'" +
//            "         and FCMPS007.SH_NO=FCMPS010.SH_NO " +
//            "         and FCMPS007.SH_COLOR=FCMPS010.SH_COLOR "+  
            "       )"+
            
    		"  and (FA_NO,SH_NO,SH_COLOR,SH_SIZE) NOT IN " +
    		"      (SELECT FA_NO,SH_NO,SH_COLOR,SH_SIZE FROM FCMPS013 " +
    		"       WHERE FA_NO='"+FA_NO+"' " +
    	    "         AND WORK_WEEK="+getWORK_WEEK()+" " +
    	    "         AND PROCID='"+PROCID+"' " +
//            "         AND OD_PONO1=FCMPS010.OD_PONO1 " +
//            "         AND SH_NO=FCMPS010.SH_NO " +
//            "         AND SH_COLOR=FCMPS010.SH_COLOR " +
//            "         AND SH_SIZE=FCMPS010.SH_SIZE " +
//            "         AND PROCID=FCMPS010.PROCID " +    
    	    "         AND ALLOW_APPEND='N')";
					
			strSQL=strSQL+ " group by SH_NO,SH_COLOR order by OD_FGDATE_WEEK ";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	do {
//		    		ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});  
		    		List<String[]> ls_SH_COLOR=set_SH_COLOR.get(rs.getInt("OD_FGDATE_WEEK"));
		    		if(ls_SH_COLOR==null) {
		    			ls_SH_COLOR=new ArrayList<String[]>();
		    			ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
		    			set_SH_COLOR.put(rs.getInt("OD_FGDATE_WEEK"), ls_SH_COLOR);
		    		}else {
			    		boolean isExist=false;
			    		for(String[] item:ls_SH_COLOR) {
			    			if(item[0].equals(rs.getString("SH_NO")) && item[1].equals(rs.getString("SH_COLOR"))) {
			    				isExist=true;
			    				break;
			    			}
			    		}
			    		if(!isExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});	
		    		}
		    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();
		    
			strSQL="select SH_NO,SH_COLOR,min(WORK_WEEK_START) OD_FGDATE_WEEK "+
            "from FCMPS010 "+
            "where FCMPS010.PROCID='"+PROCID+"'"+
            "  and FCMPS010.FA_NO='"+FA_NO+"'"+
            "  and nvl(OD_CODE,'N')='N' "+
            "  and IS_DISABLE='N' "+
            "  and WORK_WEEK_END>="+getWORK_WEEK()+
            "  and WORK_WEEK_END<="+NEXT_WEEK+
            
            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
            (SPEC_SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
            (SH_NOT_IN.equals("")?"":" and FCMPS010.SH_NO NOT IN ("+SH_NOT_IN+") ")+ //不重排計劃時,需要將已排滿的型體排除在外
     
            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                   			                            			       

            "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
            "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
            "         FROM FCMPS021 " +
            "        WHERE PROCID = '"+PROCID+"' " +
            "          AND FA_NO = '"+FA_NO+"' " +
//            "          AND OD_PONO1=FCMPS010.OD_PONO1 " +
//            "          AND SH_NO=FCMPS010.SH_NO " +
//            "          AND SH_COLOR=FCMPS010.SH_COLOR " +
//            "          AND SH_SIZE=FCMPS010.SH_SIZE " +
//            "          AND PROCID=FCMPS010.PROCID " +            
            "          AND WORK_WEEK<="+NEXT_WEEK+") "+
            
            // 提前上周計劃排的型體配色
            "  and (FA_NO,SH_NO,SH_COLOR) IN "+
            "      (SELECT FCMPS006.FA_NO,FCMPS007.SH_NO,FCMPS007.SH_COLOR "+
            "       FROM FCMPS007,FCMPS006 " +
            "       WHERE FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
            "         and FCMPS006.WORK_WEEK='"+FCMPS_PUBLIC.getPrevious_Week(getWORK_WEEK(), 1)+"'" +
            "         and FCMPS006.IS_SURE='Y'"+
            "         and FCMPS006.FA_NO='"+FA_NO+"'"+
            "         and FCMPS006.PROCID='"+PROCID+"'"+
//            "         and FCMPS007.SH_NO=FCMPS010.SH_NO " +
//            "         and FCMPS007.SH_COLOR=FCMPS010.SH_COLOR" +
            "       ) "+
            
            // 只提前本周計劃未排的型體配色
            "  and (FA_NO,SH_NO,SH_COLOR) NOT IN "+
            "      (SELECT FCMPS006.FA_NO,FCMPS007.SH_NO,FCMPS007.SH_COLOR "+
            "       FROM FCMPS007,FCMPS006 " +
            "       WHERE FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
            "         and FCMPS006.PLAN_NO='"+getPLAN_NO()+"'" +
//            "         and FCMPS007.SH_NO=FCMPS010.SH_NO " +
//            "         and FCMPS007.SH_COLOR=FCMPS010.SH_COLOR" +
            "      ) "+  
            
    		"  and (FA_NO,SH_NO,SH_COLOR,SH_SIZE) NOT IN " +
    		"      (SELECT FA_NO,SH_NO,SH_COLOR,SH_SIZE FROM FCMPS013 " +
    		"       WHERE FA_NO='"+FA_NO+"' " +
    	    "         AND WORK_WEEK="+getWORK_WEEK()+" " +
    	    "         AND PROCID='"+PROCID+"' " +
//            "         AND OD_PONO1=FCMPS010.OD_PONO1 " +
//            "         AND SH_NO=FCMPS010.SH_NO " +
//            "         AND SH_COLOR=FCMPS010.SH_COLOR " +
//            "         AND SH_SIZE=FCMPS010.SH_SIZE " +
//            "         AND PROCID=FCMPS010.PROCID " +    
    	    "         AND ALLOW_APPEND='N')";
					
			strSQL=strSQL+ " group by SH_NO,SH_COLOR order by OD_FGDATE_WEEK ";
			
			
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	do {
		    		List<String[]> ls_SH_COLOR=set_SH_COLOR.get(rs.getInt("OD_FGDATE_WEEK"));
		    		if(ls_SH_COLOR==null) {
		    			ls_SH_COLOR=new ArrayList<String[]>();
		    			ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
		    			set_SH_COLOR.put(rs.getInt("OD_FGDATE_WEEK"), ls_SH_COLOR);
		    		}else {
			    		boolean isExist=false;
			    		for(String[] item:ls_SH_COLOR) {
			    			if(item[0].equals(rs.getString("SH_NO")) && item[1].equals(rs.getString("SH_COLOR"))) {
			    				isExist=true;
			    				break;
			    			}
			    		}
			    		if(!isExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});	
		    		}  		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();	
			
		    
			strSQL="select SH_NO,SH_COLOR,min(WORK_WEEK_START) OD_FGDATE_WEEK "+
            "from FCMPS010 "+
            "where FCMPS010.PROCID='"+PROCID+"'"+
            "  and FCMPS010.FA_NO='"+FA_NO+"'"+
            "  and nvl(OD_CODE,'N')='N' "+
            "  and IS_DISABLE='N' "+
            "  and WORK_WEEK_END>="+getWORK_WEEK()+
            "  and WORK_WEEK_END<="+NEXT_WEEK+
            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
            (SPEC_SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
            (SH_NOT_IN.equals("")?"":" and FCMPS010.SH_NO NOT IN ("+SH_NOT_IN+") ")+ //不重排計劃時,需要將已排滿的型體排除在外
     
            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                   			                            			          

            "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
            "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
            "         FROM FCMPS021 " +
            "        WHERE PROCID = '"+PROCID+"' " +
            "          AND FA_NO = '"+FA_NO+"' " +
//            "          AND OD_PONO1=FCMPS010.OD_PONO1 " +
//            "          AND SH_NO=FCMPS010.SH_NO " +
//            "          AND SH_COLOR=FCMPS010.SH_COLOR " +
//            "          AND SH_SIZE=FCMPS010.SH_SIZE " +
//            "          AND PROCID=FCMPS010.PROCID " +            
            "          AND WORK_WEEK<="+NEXT_WEEK+") "+
            
            // 提前上周計劃排的型體
            "  and (FA_NO,SH_NO) IN "+
            "      (SELECT FCMPS006.FA_NO,FCMPS007.SH_NO "+
            "       FROM FCMPS007,FCMPS006 " +
            "       WHERE FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
            "         and FCMPS006.WORK_WEEK='"+FCMPS_PUBLIC.getPrevious_Week(getWORK_WEEK(), 1)+"'" +
            "         and FCMPS006.IS_SURE='Y'"+
            "         and FCMPS006.FA_NO='"+FA_NO+"'"+
            "         and FCMPS006.PROCID='"+PROCID+"'"+
//            "         and FCMPS007.SH_NO=FCMPS010.SH_NO" +
            "       ) "+
            
            // 只提前本周計劃未排的型體配色
            "  and (FA_NO,SH_NO,SH_COLOR) NOT IN "+
            "      (SELECT FCMPS006.FA_NO,FCMPS007.SH_NO,FCMPS007.SH_COLOR "+
            "       FROM FCMPS007,FCMPS006 " +
            "       WHERE FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
            "         and FCMPS006.PLAN_NO='"+getPLAN_NO()+"'" +
//            "         and FCMPS007.SH_NO=FCMPS010.SH_NO " +
//            "         and FCMPS007.SH_COLOR=FCMPS010.SH_COLOR" +
            "       ) "+  
            
    		"  and (FA_NO,SH_NO,SH_COLOR,SH_SIZE) NOT IN " +
    		"      (SELECT FA_NO,SH_NO,SH_COLOR,SH_SIZE FROM FCMPS013 " +
    		"       WHERE FA_NO='"+FA_NO+"' " +
    	    "         AND WORK_WEEK="+getWORK_WEEK()+" " +
    	    "         AND PROCID='"+PROCID+"' " +
//            "         AND OD_PONO1=FCMPS010.OD_PONO1 " +
//            "         AND SH_NO=FCMPS010.SH_NO " +
//            "         AND SH_COLOR=FCMPS010.SH_COLOR " +
//            "         AND SH_SIZE=FCMPS010.SH_SIZE " +
//            "         AND PROCID=FCMPS010.PROCID " +    
    	    "         AND ALLOW_APPEND='N')";
					
			strSQL=strSQL+ " group by SH_NO,SH_COLOR order by OD_FGDATE_WEEK ";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	do {
		    		List<String[]> ls_SH_COLOR=set_SH_COLOR.get(rs.getInt("OD_FGDATE_WEEK"));
		    		if(ls_SH_COLOR==null) {
		    			ls_SH_COLOR=new ArrayList<String[]>();
		    			ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
		    			set_SH_COLOR.put(rs.getInt("OD_FGDATE_WEEK"), ls_SH_COLOR);
		    		}else {
			    		boolean isExist=false;
			    		for(String[] item:ls_SH_COLOR) {
			    			if(item[0].equals(rs.getString("SH_NO")) && item[1].equals(rs.getString("SH_COLOR"))) {
			    				isExist=true;
			    				break;
			    			}
			    		}
			    		if(!isExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});	
		    		} 	    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();
		    
			strSQL="select SH_NO,SH_COLOR,min(WORK_WEEK_START) OD_FGDATE_WEEK "+
            "from FCMPS010 "+
            "where FCMPS010.PROCID='"+PROCID+"'"+
            "  and FCMPS010.FA_NO='"+FA_NO+"'"+
            "  and nvl(OD_CODE,'N')='N' "+
            "  and IS_DISABLE='N' "+
            "  and WORK_WEEK_END>="+getWORK_WEEK()+
            "  and WORK_WEEK_END<="+NEXT_WEEK+
            
            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
            (SPEC_SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
            (SH_NOT_IN.equals("")?"":" and FCMPS010.SH_NO NOT IN ("+SH_NOT_IN+") ")+ //不重排計劃時,需要將已排滿的型體排除在外
                             
            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                   			                            			       

            "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
            "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
            "         FROM FCMPS021 " +
            "        WHERE PROCID = '"+PROCID+"' " +
            "          AND FA_NO = '"+FA_NO+"' " +
//            "          AND OD_PONO1=FCMPS010.OD_PONO1 " +
//            "          AND SH_NO=FCMPS010.SH_NO " +
//            "          AND SH_COLOR=FCMPS010.SH_COLOR " +
//            "          AND SH_SIZE=FCMPS010.SH_SIZE " +
//            "          AND PROCID=FCMPS010.PROCID " +            
            "          AND WORK_WEEK<="+NEXT_WEEK+") "+
            
            // 只提前本周計劃未排的型體配色
            "  and (FA_NO,SH_NO,SH_COLOR) NOT IN "+
            "      (SELECT FCMPS006.FA_NO,FCMPS007.SH_NO,FCMPS007.SH_COLOR "+
            "       FROM FCMPS007,FCMPS006 " +
            "       WHERE FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
            "         and FCMPS006.PLAN_NO='"+getPLAN_NO()+"'" +
//            "         and FCMPS007.SH_NO=FCMPS010.SH_NO " +
//            "         and FCMPS007.SH_COLOR=FCMPS010.SH_COLOR" +
            "       ) "+  
            
    		"  and (FA_NO,SH_NO,SH_COLOR,SH_SIZE) NOT IN " +
    		"      (SELECT FA_NO,SH_NO,SH_COLOR,SH_SIZE FROM FCMPS013 " +
    		"       WHERE FA_NO='"+FA_NO+"' " +
    	    "         AND WORK_WEEK="+getWORK_WEEK()+" " +
    	    "         AND PROCID='"+PROCID+"' " +
//            "         AND OD_PONO1=FCMPS010.OD_PONO1 " +
//            "         AND SH_NO=FCMPS010.SH_NO " +
//            "         AND SH_COLOR=FCMPS010.SH_COLOR " +
//            "         AND SH_SIZE=FCMPS010.SH_SIZE " +
//            "         AND PROCID=FCMPS010.PROCID " +    
    	    "         AND ALLOW_APPEND='N')";
					
			strSQL=strSQL+ " group by SH_NO,SH_COLOR order by OD_FGDATE_WEEK ";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	do {
		    		List<String[]> ls_SH_COLOR=set_SH_COLOR.get(rs.getInt("OD_FGDATE_WEEK"));
		    		if(ls_SH_COLOR==null) {
		    			ls_SH_COLOR=new ArrayList<String[]>();
		    			ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
		    			set_SH_COLOR.put(rs.getInt("OD_FGDATE_WEEK"), ls_SH_COLOR);
		    		}else {
			    		boolean isExist=false;
			    		for(String[] item:ls_SH_COLOR) {
			    			if(item[0].equals(rs.getString("SH_NO")) && item[1].equals(rs.getString("SH_COLOR"))) {
			    				isExist=true;
			    				break;
			    			}
			    		}
			    		if(!isExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});	
		    		} 		    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();
		    
		    //同型体不同配色的优先顺序及共模型体的优先顺序要以交期先后顺序来排产
		    Iterator<Integer> lt=set_SH_COLOR.keySet().iterator();
		    List<String[]> ls_SH_COLOR=new ArrayList<String[]>();
		    
		    while(lt.hasNext()) {
		    	Integer key=lt.next();
		    	 List<String[]> tmp_SH_COLOR=set_SH_COLOR.get(key);
		    	 for(String[] item:tmp_SH_COLOR) {
		    		 boolean isExist=false;
		    		 for(String[] itemb:ls_SH_COLOR) {
			    		if(item[0].equals(itemb[0]) && item[1].equals(itemb[1])) {
			    			isExist=true;
			    			break;
			    		}
		    		 }
		    		 
		    		 if(!isExist)ls_SH_COLOR.add(new String[] {item[0],item[1]});
		    		 
		    	 }
		    	
		    }
		    
		    String subSQL="";
		    for(int n=0;n<ls_SH_COLOR.size();n++) {
		    	String SH_NO=ls_SH_COLOR.get(n)[0];
		    	String SH_COLOR=ls_SH_COLOR.get(n)[1];
		    	
		    	if(!subSQL.equals("")) subSQL=subSQL+",";
		    	subSQL=subSQL+"('"+SH_NO+"','"+SH_COLOR+"')";		    	
		    	
		    }
		    
		    if(!ls_SH_COLOR.isEmpty()) {
			    strSQL="select SH_NO,SH_COLOR from FCMPS021 " +
		    	       "where (SH_NO,SH_COLOR) IN ("+subSQL+") AND PROCID='"+PROCID+"' " +
		    	       "ORDER BY WORK_WEEK ASC";
				pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs=pstmtData.executeQuery();

			    if(rs.next()){
			    	ls_SH_COLOR.clear();
			    	do {
			    		
			    		boolean iExist=false;
			    		if(!FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().isEmpty()) {
				    		for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().size();i++) {
				    			if(rs.getString("SH_NO").equals(FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().get(i))) {
				    				iExist=true;
				    				break;
				    			}
				    		}	
			    		}
			    		
			    		if(!iExist) {
			    			for(int i=0;i<ls_SH_COLOR.size();i++) {
			    				if(rs.getString("SH_NO").equals(ls_SH_COLOR.get(i)[0]) && rs.getString("SH_COLOR").equals(ls_SH_COLOR.get(i)[1])) {
			    					iExist=true;
			    					break;
			    				}
			    			}
			    			
			    			if(!iExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
			    		}
			    		
			    	}while(rs.next());		    	
			    }
			    rs.close();
			    pstmtData.close();
		    }

    		int NEXT_PROC_WEEK=getWORK_WEEK();
    		do {
    			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getNext_Week(NEXT_PROC_WEEK,1),conn)==0) {
    				NEXT_PROC_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_PROC_WEEK,1);
    			}else {
    				break;
    			}
    			
    		}while(true);

			Current_Calac_Color=""; //當前步驟計算的配色名稱
			Step_Calac_Color_Numbers=ls_SH_COLOR.size(); //當前計算的配色總數
			Step_Calac_Color_Seq=0;  //當前計算的配色順序
    		
		    for(Step_Calac_Color_Seq=0;Step_Calac_Color_Seq<ls_SH_COLOR.size();Step_Calac_Color_Seq++) {
		    	String SH_NO=ls_SH_COLOR.get(Step_Calac_Color_Seq)[0];
		    	String SH_COLOR=ls_SH_COLOR.get(Step_Calac_Color_Seq)[1];
		    	
		    	Current_Calac_Color="Style:"+SH_NO+" Color:"+SH_COLOR;
		    	
				//未來幾周有哪些型體
				strSQL="select SH_NO," +
				"              SH_COLOR," +
				"              PROC_SEQ," +
				"              SH_SIZE," +
				"              min(WORK_WEEK_START) OD_FGDATE_WEEK,"+
				"              SUM(OD_QTY-nvl(WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)) WORK_PLAN_QTY "+
	            "from FCMPS010 "+
	            "where FCMPS010.PROCID='"+PROCID+"'"+
	            "  and FCMPS010.FA_NO='"+FA_NO+"'"+
	            "  and FCMPS010.SH_NO='"+SH_NO+"'"+
	            "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
	            "  and nvl(OD_CODE,'N')='N' "+
	            "  and IS_DISABLE='N' "+
	            "  and WORK_WEEK_END>="+getWORK_WEEK()+
	            "  and WORK_WEEK_END<="+NEXT_WEEK+
	            
	            (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
	            (SPEC_SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
	            (SH_NOT_IN.equals("")?"":" and FCMPS010.SH_NO NOT IN ("+SH_NOT_IN+") ")+ //不重排計劃時,需要將已排滿的型體排除在外
	     
	            "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                   			                            			        

	            "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
	            "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
	            "         FROM FCMPS021 " +
	            "        WHERE PROCID = '"+PROCID+"' " +
	            "          AND FA_NO = '"+FA_NO+"' " +
	            "          AND SH_NO = '"+SH_NO+"' " +
	            "          AND SH_COLOR = '"+SH_COLOR+"' " +
	            "          AND WORK_WEEK<="+NEXT_WEEK+") "+	            
				
	            //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
	            //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
	            "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
	            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
	            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
	            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
	            "                       FROM FCPS22_1 "+
	            "                       WHERE NEED_PLAN = 'Y' "+
	            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
	            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
	            "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
	            "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
	            "                     from FCMPS007,FCMPS006 " +
	            "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
	            "                     and FCMPS006.IS_SURE='Y'"+
	            "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
	            "                     and (FCMPS006.WORK_WEEK>="+NEXT_PROC_WEEK+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
	            "         or          "+
	            "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
	            "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
	            "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
	            "                      (SELECT SH_ARITCLE, PROC_SEQ "+
	            "                       FROM FCPS22_1 "+
	            "                       WHERE NEED_PLAN = 'Y' "+
	            "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
	            "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))) "+
	            
	            // 只提前本周計劃未排的型體配色
	            "  and (FA_NO,SH_NO,SH_COLOR) NOT IN "+
	            "      (SELECT FCMPS006.FA_NO,FCMPS007.SH_NO,FCMPS007.SH_COLOR "+
	            "       FROM FCMPS007,FCMPS006 " +
	            "       WHERE FCMPS007.PLAN_NO=FCMPS006.PLAN_NO " +
	            "         and FCMPS006.PLAN_NO='"+getPLAN_NO()+"'" +
//	            "         and FCMPS007.SH_NO=FCMPS010.SH_NO " +
//	            "         and FCMPS007.SH_COLOR=FCMPS010.SH_COLOR" +
	            "       ) "+  
	            
	    		"  and (FA_NO,SH_NO,SH_COLOR,SH_SIZE) NOT IN " +
	    		"      (SELECT FA_NO,SH_NO,SH_COLOR,SH_SIZE FROM FCMPS013 " +
	    		"       WHERE FA_NO='"+FA_NO+"' " +
	    	    "         AND WORK_WEEK="+getWORK_WEEK()+" " +
	    	    "         AND PROCID='"+PROCID+"' " +
//	            "         AND SH_NO=FCMPS010.SH_NO " +
//	            "         AND SH_COLOR=FCMPS010.SH_COLOR " +
//	            "         AND SH_SIZE=FCMPS010.SH_SIZE " +
//	            "         AND PROCID=FCMPS010.PROCID " +    
	    	    "         AND ALLOW_APPEND='N')";
				
	            //指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
	            //為了避免因為後關提前導致其前關也提前
		        strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID)not in" +
		                 "             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
	                     "               where FA_NO='"+FA_NO+"' " +
	                     "                 and WORK_WEEK>"+getWORK_WEEK()+
//	                     "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
//	                     "                 and PROCID=FCMPS010.PROCID" +
//	                     "                 and SH_NO=FCMPS010.SH_NO "+
//	                     "                 and SH_COLOR=FCMPS010.SH_COLOR "+
//	                     "                 and SH_SIZE=FCMPS010.SH_SIZE" +
	                     "              ) ";			
				
				strSQL=strSQL+ " group by SH_NO,SH_COLOR,SH_SIZE,PROC_SEQ order by OD_FGDATE_WEEK ";			
				
				pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs=pstmtData.executeQuery();
			    if(rs.next()){

			    	PROC_WORK_QTY proc_Work_Qty= getCurrent_PROC_WORK_QTY(FA_NO, getWORK_WEEK(), PROCID,FCMPS_RCCP_INFO_Var.getLs_PROC_WORK_QTY());
			    	double PROC_ALLOW_QTY=proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY();
			    	if(PROC_ALLOW_QTY<SHOOT_MIN_PRODUCE_QTY) {
			    		is_UP_TO_CAP=true;
					    rs.close();
					    pstmtData.close();	
			    		return iRet;
			    	}
			    	
			    	double PROC_SEQ=rs.getDouble("PROC_SEQ");
			    	
			    	SH_WORK_QTY sh_Work_Qty=getSH_WORK_PLAN_QTY(FA_NO, SH_NO, PROCID, PROC_SEQ, getWORK_WEEK(), FCMPS_RCCP_INFO_Var.getLs_SH_WORK_QTY(), FCMPS_PUBLIC.getSH_WorkDaysOfWeek(FA_NO, SH_NO, getWORK_WEEK(), conn));
			    	double SH_ALLOW_QTY=sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY();
		    		
	    			double OTHER_PROC_ALLOW_QTY=getOther_PROC_Allow_Plan_QTY(FA_NO, SH_NO, PROCID, getWORK_WEEK(), FCMPS_RCCP_INFO_Var.getLs_PROC_WORK_QTY(), FCMPS_RCCP_INFO_Var.getLs_SH_WORK_QTY());
	    	    	if(OTHER_PROC_ALLOW_QTY<SH_ALLOW_QTY) SH_ALLOW_QTY=OTHER_PROC_ALLOW_QTY;
	    	    	
	    	    	if(SH_ALLOW_QTY<SHOOT_MIN_PRODUCE_QTY) {
					    rs.close();
					    pstmtData.close();	
			    		continue;
	    	    	}
	    	    	
		    		String SH_SIZE=rs.getString("SH_SIZE");
		    		
			    	boolean NEED_SHOOT=FCMPS_PUBLIC.getSH_Need_PROC(SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, conn);
			    	double SH_SIZE_ALLOW_QTY=0;
			    	
			    	do {
			    		
			    		PROC_SEQ=rs.getDouble("PROC_SEQ");
			    		SH_SIZE=rs.getString("SH_SIZE");
			    		
			    		SH_KEY_SIZE sh_key_size=getSH_SIZE_Allow_Plan_QTY(SH_NO, SH_SIZE, PROCID, PROC_SEQ,FCMPS_RCCP_INFO_Var.getLs_Share_Style_Size(), FCMPS_RCCP_INFO_Var.getLs_SH_KEY_SIZE());
			    		
			    		if(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY()==0) continue;
			    		
			    		if(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY()>rs.getDouble("WORK_PLAN_QTY")) {
			    			SH_SIZE_ALLOW_QTY=SH_SIZE_ALLOW_QTY+rs.getDouble("WORK_PLAN_QTY");
			    		}else {
			    			SH_SIZE_ALLOW_QTY=SH_SIZE_ALLOW_QTY+(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY());
			    		}
			    		
		    			if(NEED_SHOOT) {
			    			if(SH_SIZE_ALLOW_QTY>=SHOOT_MIN_PRODUCE_QTY && 
			    			   SH_ALLOW_QTY>=SHOOT_MIN_PRODUCE_QTY &&
			    			   PROC_ALLOW_QTY>=SHOOT_MIN_PRODUCE_QTY
			    			   ) {
//			    				System.out.println(SH_NO+" "+SH_COLOR+" "+SH_SIZE_ALLOW_QTY);
			    			    iRet=doGeneratePlan_Non_Bring_In_Planned_Color_Size(
			    			    		conn, 
			    			    		PROCID, 
			    			    		PLAN_NO,
			    			    		FA_NO,
			    			    		NEXT_WEEK,
			    			    		SH_NO,
			    			    		SH_COLOR);					    			    	   
			    			    		    
			    			    iRet=doGeneratePlan_Append_Less_Than_516(
			    			    		conn, 
			    			    		PROCID, 
			    			    		PLAN_NO,
			    			    		FA_NO,
			    			    		WORK_WEEK,
			    			    		getFORE_PLAN_WEEKS(),
			    			    		SHOOT_MIN_PRODUCE_QTY,
			    			    		"'"+SH_NO+"'");		    

//			    	============================================================================================================
			    			    
//			    			    System.out.println("將後續4周內小於516的型體配色追加進計劃中");		    
			    			    		    
			    			    iRet=doGeneratePlan_Append_Less_Than_516(
			    			    		conn, 
			    			    		PROCID, 
			    			    		PLAN_NO,
			    			    		FA_NO,
			    			    		WORK_WEEK,
			    			    		4,
			    			    		SHOOT_MIN_PRODUCE_QTY,
			    			    		"'"+SH_NO+"'");
			    			    
			    			    break;
			    			    
			    			}
		    			}else {
//		    				System.out.println(SH_NO+" "+SH_COLOR+" "+SH_SIZE_ALLOW_QTY);
		    				
		    			    iRet=doGeneratePlan_Non_Bring_In_Planned_Color_Size(
		    			    		conn, 
		    			    		PROCID, 
		    			    		PLAN_NO,
		    			    		FA_NO,
		    			    		WORK_WEEK,
		    			    		SH_NO,
		    			    		SH_COLOR);	
		    			    
		    			    break;
		    			}
			    				    		    		
			    	}while(rs.next());
			    }
			    rs.close();
			    pstmtData.close();			    	
		    	
		    	if(is_UP_TO_CAP) break;
		    }

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
	}			
			
	/**
	 * 計算未排滿可提前排訂單--提前沒有排入本周但必須是可以在本周排完的型體配色SIZE 
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param NEXT_WEEK
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @param ls_SH_KEY_SIZE
	 * @param ls_SH_COLOR_SIZE
	 * @param ls_Share_Style_Size
	 * @param SH_NO
	 * @return
	 */
	private int doGeneratePlan_Non_Bring_In_Planned_Color_Size(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int NEXT_WEEK,
    		String SH_NO,
    		String SH_COLOR
    		) {
    	int iRet=0;
    	
    	if(!is_FORE_PLAN_WEEKS || getFORE_PLAN_WEEKS()==0) return iRet;	
    	
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try {
	    	
    		int NEXT_PROC_WEEK=getWORK_WEEK();
    		do {
    			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,FCMPS_PUBLIC.getNext_Week(NEXT_PROC_WEEK,1),conn)==0) {
    				NEXT_PROC_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_PROC_WEEK,1);
    			}else {
    				break;
    			}
    			
    		}while(true);

			strSQL="select " +
                "FCMPS010.OD_PONO1, "+
                "FCMPS010.STYLE_NO,"+
                "FCMPS010.SH_NO,"+
                "FCMPS010.SH_COLOR,"+
                "FCMPS010.SH_SIZE,"+
                "FCMPS010.PROC_SEQ,"+
                "FCMPS010.OD_QTY,"+
                "FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0) WORK_PLAN_QTY,"+
                "FCMPS010.WORK_WEEK_START,"+
                "FCMPS010.WORK_WEEK_END,"+
                "FCMPS010.OD_SHIP,"+
                "to_char(FCMPS010.OD_SHIP,'IYIW') od_ship_week,"+
                "FCMPS010.OD_FGDATE, "+			
                "to_char(FCMPS010.OD_FGDATE,'IYIW') od_fgdate_week "+
                "from FCMPS010 "+
                "where SH_NO='" +SH_NO+"'"+
                "  and SH_COLOR='"+SH_COLOR+"'"+
                "  and FCMPS010.PROCID='"+PROCID+"'"+
                "  and FCMPS010.FA_NO='"+FA_NO+"'"+
                "  and nvl(OD_CODE,'N')='N' "+
                "  and IS_DISABLE='N' "+
                "  and WORK_WEEK_END>="+getWORK_WEEK()+
                "  and WORK_WEEK_END<="+NEXT_WEEK+
                
                (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
                             
                "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	        
                
                "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
                "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
                "       FROM FCMPS021 " +
                "       WHERE PROCID = '"+PROCID+"' " +
                "         AND FA_NO = '"+FA_NO+"' " +
        	    "         AND SH_NO='"+SH_NO+"'"+
        	    "         AND SH_COLOR='"+SH_COLOR+"'"+           
                "         AND WORK_WEEK<="+NEXT_WEEK+") "+
                
                //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
                //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
                "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
                "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
                "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
                "                      (SELECT SH_ARITCLE, PROC_SEQ "+
                "                       FROM FCPS22_1 "+
                "                       WHERE NEED_PLAN = 'Y' "+
                "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
                "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
                "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
                "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
                "                     from FCMPS007,FCMPS006 " +
                "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
                "                     and FCMPS006.IS_SURE='Y'"+
                "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
                "                     and (FCMPS006.WORK_WEEK>="+getWORK_WEEK()+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
                "         or          "+
                "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
                "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
                "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
                "                      (SELECT SH_ARITCLE, PROC_SEQ "+
                "                       FROM FCPS22_1 "+
                "                       WHERE NEED_PLAN = 'Y' "+
                "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
                "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))) "+
                
        		"  and (FA_NO,SH_NO,SH_COLOR,SH_SIZE) NOT IN " +
        		"      (SELECT FA_NO,SH_NO,SH_COLOR,SH_SIZE FROM FCMPS013 " +
        		"       WHERE FA_NO='"+FA_NO+"' " +
        	    "         AND WORK_WEEK="+getWORK_WEEK()+" " +
        	    "         AND SH_NO='"+SH_NO+"'"+
        	    "         AND SH_COLOR='"+SH_COLOR+"'"+
        	    "         AND PROCID='"+PROCID+"' " +
        	    "         AND ALLOW_APPEND='N')";
                       	      								
                //指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
                //為了避免因為後關提前導致其前關也提前
    	        strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID)not in" +
    	                 "             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
                         "               where FA_NO='"+FA_NO+"' " +
                         "                 and WORK_WEEK>"+getWORK_WEEK()+
//                         "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
//                         "                 and PROCID=FCMPS010.PROCID" +
//                         "                 and SH_NO=FCMPS010.SH_NO "+
//                         "                 and SH_COLOR=FCMPS010.SH_COLOR "+
//                         "                 and SH_SIZE=FCMPS010.SH_SIZE" +
                         "              ) ";
    	        
				if(getPLAN_PRIORITY_TYPE().equals("1")) {
					if(getPLAN_BY_DATE().equals("A")) {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP ";
					}else {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE ";
					}
				}
			
				if(getPLAN_PRIORITY_TYPE().equals("2")) {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.WORK_WEEK_START";
				}
				
				if(getPLAN_PRIORITY_TYPE().equals("3")) {
					if(getPLAN_BY_DATE().equals("A")) {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP,FCMPS010.WORK_WEEKS ";
					}else {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE,FCMPS010.WORK_WEEKS ";
					}
				}
				
//			System.out.println(strSQL);
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
			rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			rs.setFetchSize(3000);
		    
		    if(rs.next()){
	    		FCMPS_CLS_GenerateOrderPlan_MultiThread cls_GenerateOrderPlan=new FCMPS_CLS_GenerateOrderPlan_MultiThread();
		    	cls_GenerateOrderPlan.setFA_NO(FA_NO);
		    	cls_GenerateOrderPlan.setPLAN_BY_DATE(getPLAN_BY_DATE());
		    	cls_GenerateOrderPlan.setPLAN_NO(PLAN_NO);
		    	cls_GenerateOrderPlan.setWORK_WEEK(getWORK_WEEK());
		    	cls_GenerateOrderPlan.setConnection(conn);
		    	cls_GenerateOrderPlan.setIs_FORE_PLAN_WEEKS(is_FORE_PLAN_WEEKS);
		    	cls_GenerateOrderPlan.setFORE_PLAN_WEEKS(getFORE_PLAN_WEEKS());
		    	cls_GenerateOrderPlan.setUP_USER(getUP_USER());
		    	cls_GenerateOrderPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
		    	cls_GenerateOrderPlan.setConfig_xml(config_xml);
		    	cls_GenerateOrderPlan.setPER_SIZE_ALLOW_OVER_NUM(PER_SIZE_ALLOW_OVER_NUM);
		    	cls_GenerateOrderPlan.setFCMPS_RCCP_INFO_Var(FCMPS_RCCP_INFO_Var);
		    	
		    	boolean iResult=cls_GenerateOrderPlan.doGeneratePlan(rs,PROCID,false);			    	
		    	
		    	if(!cls_GenerateOrderPlan.getMessage().isEmpty())setMessage(cls_GenerateOrderPlan.getMessage());
		    	if(!iResult) {
		    		System.out.println("計算提前排產訂單出現錯誤退出!");
				    iRet=-1;
		    	}
		    	
		    	//當達到本周最大產能,退出
	    		if(cls_GenerateOrderPlan.is_UP_TO_CAP()) {
	    			is_UP_TO_CAP=true;
	    			System.out.println("達到最大周產能,退出!");
	    			iRet=1;
	    		}	 		    	
		    }
		    rs.close();
		    pstmtData.close();	

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
	}	
	

	/**
	 * 計劃計算完成後,已排型體配色,後續訂單數小於516的,追加數量至本周
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param WORK_WEEK
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @param ls_SH_KEY_SIZE
	 * @param ls_SH_COLOR_SIZE
	 * @param ls_SH_COLOR_ALLOW_COUNT
	 * @param ls_SH_COLOR_QTY
	 * @param ls_Share_Style_Size
	 * @param ls_SH_NEED_SHOOT
	 * @param ls_SH_USE_CAP
	 * @param ls_SH_NEED_PLAN_PROC
	 * @param SHOOT_MIN_PRODUCE_QTY
	 * @param SH_NO
	 * @return
	 */
	private int doGeneratePlan_Append_Less_Than_516(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int WORK_WEEK,
    		int SHOOT_MIN_PRODUCE_QTY,
    		String... SH_NO
    		) {
    	int iRet=0;
    	
    	if(!is_FORE_PLAN_WEEKS || getFORE_PLAN_WEEKS()==0) return iRet;	
    	
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		try {
			
			ArrayList<String> ls_SH_NO=new ArrayList<String>();
			
			ArrayList<String[]> ls_SH_COLOR=new ArrayList<String[]>();
    		
			//本周有哪些型體配色,先補
		    strSQL="select SH_NO,SH_COLOR,sum(WORK_PLAN_QTY) WORK_PLAN_QTY2 " +
		    		"from FCMPS007 " +
		    		"where PLAN_NO='"+getPLAN_NO()+"' "+
		    		(SH_NO.length>0?" and SH_NO IN ("+SH_NO[0]+") ":" ")+
		    		"group by SH_NO,SH_COLOR ";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    if(rs.next()){
		    	do {
		    		ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();		    
		    			
			//本周有哪些型體
		    strSQL="select SH_NO,sum(WORK_PLAN_QTY) WORK_PLAN_QTY2 " +
		    		"from FCMPS007 " +
		    		"where PLAN_NO='"+getPLAN_NO()+"' "+
		    		(SH_NO.length>0?" and SH_NO IN ("+SH_NO[0]+") ":" ")+
		    		"group by SH_NO ";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    if(rs.next()){
		    	do {
		    		ls_SH_NO.add(rs.getString("SH_NO"));
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();

		    //本周有哪些共模的型體配色,再補,這樣是為了減少換模
		    for(String key:ls_SH_NO) {
			    strSQL="select " +
			           "sh_no,"+
			           "sh_color,"+
                       "min(WORK_WEEK_START) OD_FGDATE_WEEK "+
                       "from FCMPS010 "+
                       "where FCMPS010.PROCID='"+PROCID+"'"+
                       "  and FCMPS010.FA_NO='"+FA_NO+"'"+
                       "  and nvl(OD_CODE,'N')='N' "+
                       "  and IS_DISABLE='N' "+ 
                       "  and FCMPS010.SH_NO IN (SELECT distinct SH_NO FROM FCMPS0022 WHERE SH_NO2 IN (select distinct SH_NO2 from FCMPS0022 WHERE SH_NO='"+key+"'))"+
                       (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+  			                   
                       "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                   				
//                       "  and FCMPS010.WORK_WEEK_START>="+getWORK_WEEK()+" "+
                       "group by sh_no,sh_color order by OD_FGDATE_WEEK";
				pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs=pstmtData.executeQuery();
			    if(rs.next()){
			    	do {
			    		boolean iExist=false;
			    		for(int iSH_COLOR=0;iSH_COLOR<ls_SH_COLOR.size();iSH_COLOR++) {
			    			if(ls_SH_COLOR.get(iSH_COLOR)[0].equals(rs.getString("SH_NO")) && ls_SH_COLOR.get(iSH_COLOR)[1].equals(rs.getString("SH_COLOR"))) {
			    				iExist=true;
			    				break;
			    			}
			    		}
			    		if(!iExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
			    	}while(rs.next());
			    }
			    rs.close();
			    pstmtData.close();			    
		    }
		    
		    ls_SH_NO.clear();
		    
			//上周有哪些型體配色,再補,這樣是為了減少換模
			strSQL="SELECT FCMPS007.SH_NO, "+
			       "       FCMPS007.SH_COLOR "+
                   "  FROM FCMPS007,FCMPS006 " +
                   " WHERE FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
                   "   AND FCMPS006.PROCID = '"+PROCID+"' " +
                   "   AND FCMPS006.FA_NO = '"+FA_NO+"' " +
                   "   AND FCMPS006.WORK_WEEK="+FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, 1)+
                  (SH_NO.length>0?" and FCMPS007.SH_NO IN ("+SH_NO[0]+") ":"")+
                  "GROUP BY FCMPS007.SH_NO,FCMPS007.SH_COLOR ";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    if(rs.next()){
		    	do {
		    		boolean iExist=false;
		    		for(int iSH_COLOR=0;iSH_COLOR<ls_SH_COLOR.size();iSH_COLOR++) {
		    			if(ls_SH_COLOR.get(iSH_COLOR)[0].equals(rs.getString("SH_NO")) && ls_SH_COLOR.get(iSH_COLOR)[1].equals(rs.getString("SH_COLOR"))) {
		    				iExist=true;
		    				break;
		    			}
		    		}
		    		if(!iExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});		    			    		
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();
		    
			//上周有哪些型體
			strSQL="SELECT FCMPS007.SH_NO "+
                   "  FROM FCMPS007,FCMPS006 " +
                   " WHERE FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
                   "   AND FCMPS006.PROCID = '"+PROCID+"' " +
                   "   AND FCMPS006.FA_NO = '"+FA_NO+"' " +
                   "   AND FCMPS006.WORK_WEEK="+FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, 1)+
                  (SH_NO.length>0?" and FCMPS007.SH_NO IN ("+SH_NO[0]+") ":"")+
                  "GROUP BY FCMPS007.SH_NO ";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    if(rs.next()){
		    	do {
		    		ls_SH_NO.add(rs.getString("SH_NO"));
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();
		    		    
		    //上周與本周有哪些共模的型體配色,再補,這樣是為了減少換模
		    for(String key:ls_SH_NO) {
			    strSQL="select " +
			           "sh_no,"+
			           "sh_color,"+
			           "min(WORK_WEEK_START) OD_FGDATE_WEEK "+
                       "from FCMPS010 "+
                       "where FCMPS010.PROCID='"+PROCID+"'"+
                       "  and FCMPS010.FA_NO='"+FA_NO+"'"+
                       "  and nvl(OD_CODE,'N')='N' "+
                       "  and IS_DISABLE='N' "+ 
                       "  and FCMPS010.SH_NO IN (SELECT distinct SH_NO FROM FCMPS0022 WHERE SH_NO2 IN (select distinct SH_NO2 from FCMPS0022 WHERE SH_NO='"+key+"'))"+
                       (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+  			                   
                       "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                   				
//                       "  and FCMPS010.WORK_WEEK_START>="+getWORK_WEEK()+" "+
                       "group by sh_no,sh_color order by OD_FGDATE_WEEK";
				pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs=pstmtData.executeQuery();
			    if(rs.next()){
			    	do {
			    		boolean iExist=false;
			    		for(int iSH_COLOR=0;iSH_COLOR<ls_SH_COLOR.size();iSH_COLOR++) {
			    			if(ls_SH_COLOR.get(iSH_COLOR)[0].equals(rs.getString("SH_NO")) && ls_SH_COLOR.get(iSH_COLOR)[1].equals(rs.getString("SH_COLOR"))) {
			    				iExist=true;
			    				break;
			    			}
			    		}
			    		if(!iExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
			    	}while(rs.next());
			    }
			    rs.close();
			    pstmtData.close();			    
		    }
		    		    
		    String SH_NO2="";
		    
		    iFirst:for(int iSH_COLOR=0;iSH_COLOR<ls_SH_COLOR.size();iSH_COLOR++) {
		    	SH_NO2=ls_SH_COLOR.get(iSH_COLOR)[0];	        		
	    				    				 		    		
		    	String SH_COLOR2=ls_SH_COLOR.get(iSH_COLOR)[1];	
		    	
	    		if(!FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().isEmpty()) {
		    		for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().size();i++) {
		    			if(SH_NO2.equals(FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().get(i))) {
		    				continue iFirst;
		    			}
		    		}	
	    		}

			    strSQL="select " +
                "sum(FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)) WORK_PLAN_QTY "+
                "from FCMPS010 "+
                "where FCMPS010.PROCID='"+PROCID+"'"+
                "  and FCMPS010.FA_NO='"+FA_NO+"'"+
                "  and nvl(OD_CODE,'N')='N' "+
                "  and IS_DISABLE='N' "+
                "  and FCMPS010.SH_NO='"+SH_NO2+"'"+
                "  and FCMPS010.SH_COLOR='"+SH_COLOR2+"'"+
                "  and WORK_WEEK_END>="+getWORK_WEEK()+
                
                (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
  			                   
                "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                   
				
//                "  and FCMPS010.WORK_WEEK_START>="+getWORK_WEEK()+                  
                
	            //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
                //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
                "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
                "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
                "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
                "                      (SELECT SH_ARITCLE, PROC_SEQ "+
                "                       FROM FCPS22_1 "+
                "                       WHERE NEED_PLAN = 'Y' "+
                "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
                "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
                "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
                "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
                "                     from FCMPS007,FCMPS006 " +
                "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
                "                     and FCMPS006.IS_SURE='Y'"+
                "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
                "                     and (FCMPS006.WORK_WEEK>="+FCMPS_PUBLIC.getNext_Week(WORK_WEEK,1)+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
                "         or          "+
                "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
                "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
                "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
                "                      (SELECT SH_ARITCLE, PROC_SEQ "+
                "                       FROM FCPS22_1 "+
                "                       WHERE NEED_PLAN = 'Y' "+
                "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
                "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))) ";
	                   	      								
              //指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
              //為了避免因為後關提前導致其前關也提前
			    strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID)not in" +
                "             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
                "               where FA_NO='"+FA_NO +"'"+
                "                 and WORK_WEEK>"+getWORK_WEEK()+
                "              ) "; 
			    
				pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs3=pstmtData3.executeQuery();
		    
			    double WORK_PLAN_QTY=0;
			    if(rs3.next()){			    
			    	WORK_PLAN_QTY=rs3.getDouble("WORK_PLAN_QTY");			    	
			    }
			    rs3.close();
			    pstmtData3.close();
			    
			    if(WORK_PLAN_QTY>=SHOOT_MIN_PRODUCE_QTY) continue;
			    
			    strSQL="select " +
                "FCMPS010.OD_PONO1, "+
                "FCMPS010.STYLE_NO,"+
                "FCMPS010.SH_NO,"+
                "FCMPS010.SH_COLOR,"+
                "FCMPS010.SH_SIZE,"+
                "FCMPS010.PROC_SEQ,"+
                "FCMPS010.OD_QTY,"+
                "FCMPS010.OD_QTY-" +
                "nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0) WORK_PLAN_QTY,"+
                "FCMPS010.WORK_WEEK_START,"+
                "FCMPS010.WORK_WEEK_END,"+
                "FCMPS010.OD_SHIP,"+
                "to_char(FCMPS010.OD_SHIP,'IYIW') od_ship_week,"+
                "FCMPS010.OD_FGDATE, "+			
                "to_char(FCMPS010.OD_FGDATE,'IYIW') od_fgdate_week "+
                "from FCMPS010 "+
                "where FCMPS010.PROCID='"+PROCID+"'"+
                "  and FCMPS010.FA_NO='"+FA_NO+"'"+
                "  and nvl(OD_CODE,'N')='N' "+
                "  and IS_DISABLE='N' "+
                "  and FCMPS010.SH_NO='"+SH_NO2+"'"+
                "  and FCMPS010.SH_COLOR='"+SH_COLOR2+"'"+
                "  and WORK_WEEK_END>="+getWORK_WEEK()+
                
                (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
  			                   
                "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                   
				
//                "  and FCMPS010.WORK_WEEK_START>="+getWORK_WEEK()+                  
                
	            //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
                //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
                "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
                "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
                "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
                "                      (SELECT SH_ARITCLE, PROC_SEQ "+
                "                       FROM FCPS22_1 "+
                "                       WHERE NEED_PLAN = 'Y' "+
                "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
                "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
                "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
                "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
                "                     from FCMPS007,FCMPS006 " +
                "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
                "                     and FCMPS006.IS_SURE='Y'"+
                "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
                "                     and (FCMPS006.WORK_WEEK>="+FCMPS_PUBLIC.getNext_Week(WORK_WEEK,1)+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
                "         or          "+
                "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
                "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
                "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
                "                      (SELECT SH_ARITCLE, PROC_SEQ "+
                "                       FROM FCPS22_1 "+
                "                       WHERE NEED_PLAN = 'Y' "+
                "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
                "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))) ";
	                   	      								
              //指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
              //為了避免因為後關提前導致其前關也提前
			    strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID)not in" +
                "             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
                "               where FA_NO='"+FA_NO +"'"+
                "                 and WORK_WEEK>"+getWORK_WEEK()+
//                "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
//                "                 and PROCID=FCMPS010.PROCID" +
//                "                 and SH_NO=FCMPS010.SH_NO "+
//                "                 and SH_COLOR=FCMPS010.SH_COLOR "+
//                "                 and SH_SIZE=FCMPS010.SH_SIZE" +
                "              ) "; 
					 					
				if(getPLAN_PRIORITY_TYPE().equals("1")) {
					if(getPLAN_BY_DATE().equals("A")) {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP ";
					}else {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE ";
					}
				}
			
				if(getPLAN_PRIORITY_TYPE().equals("2")) {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.WORK_WEEK_START";
				}
				
				if(getPLAN_PRIORITY_TYPE().equals("3")) {
					if(getPLAN_BY_DATE().equals("A")) {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP,FCMPS010.WORK_WEEKS ";
					}else {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE,FCMPS010.WORK_WEEKS ";
					}
				}
				
//				System.out.println(strSQL);
				pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs3=pstmtData3.executeQuery();
			    
				rs3.setFetchDirection(ResultSet.FETCH_FORWARD);
				rs3.setFetchSize(3000);
		    
			    if(rs3.next()){
		    		FCMPS_CLS_GenerateOrderPlan_MultiThread cls_GenerateOrderPlan=new FCMPS_CLS_GenerateOrderPlan_MultiThread();
			    	cls_GenerateOrderPlan.setFA_NO(FA_NO);
			    	cls_GenerateOrderPlan.setPLAN_BY_DATE(getPLAN_BY_DATE());
			    	cls_GenerateOrderPlan.setPLAN_NO(PLAN_NO);
			    	cls_GenerateOrderPlan.setWORK_WEEK(WORK_WEEK);
			    	cls_GenerateOrderPlan.setConnection(conn);
			    	cls_GenerateOrderPlan.setIs_FORE_PLAN_WEEKS(is_FORE_PLAN_WEEKS);
			    	cls_GenerateOrderPlan.setFORE_PLAN_WEEKS(getFORE_PLAN_WEEKS());
			    	cls_GenerateOrderPlan.setUP_USER(getUP_USER());
			    	cls_GenerateOrderPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
			    	cls_GenerateOrderPlan.setConfig_xml(config_xml);
			    	cls_GenerateOrderPlan.setPER_SIZE_ALLOW_OVER_NUM(PER_SIZE_ALLOW_OVER_NUM);
			    	cls_GenerateOrderPlan.setFCMPS_RCCP_INFO_Var(FCMPS_RCCP_INFO_Var);
			    	
			    	boolean iResult=cls_GenerateOrderPlan.doGeneratePlan(rs3,PROCID,false);			    	
			    	
			    	if(!cls_GenerateOrderPlan.getMessage().isEmpty())setMessage(cls_GenerateOrderPlan.getMessage());
			    	if(!iResult) {
			    		System.out.println("計算出現錯誤退出!");
					    iRet=-1;
			    	}
 		    	
			    }
			    rs3.close();
			    pstmtData3.close();	    
			    
			    if(iRet!=0) break;

		    }
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
	}
	
	/**
	 * 計劃計算完成後,向後幾周內已排型體配色,訂單數小於516的,追加數量至本周
	 * @param conn
	 * @param PROCID
	 * @param PLAN_NO
	 * @param FA_NO
	 * @param WORK_WEEK
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @param ls_SH_KEY_SIZE
	 * @param ls_SH_COLOR_SIZE
	 * @param ls_Share_Style_Size
	 * @param SH_NO
	 * @return
	 */
	private int doGeneratePlan_Append_Less_Than_516(
    		Connection conn,
    		String PROCID,
    		String PLAN_NO,
    		String FA_NO,
    		int WORK_WEEK,
    		int FORE_PLAN_WEEKS,  
    		double MIN_SHOOT_QTY,
    		String... SH_NO
    		) {
    	int iRet=0;
    	
    	if(!is_FORE_PLAN_WEEKS || getFORE_PLAN_WEEKS()==0) return iRet;	
    	
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		try {

			ArrayList<String[]> ls_SH_COLOR=new ArrayList<String[]>();
    		
			//本周有哪些型體配色
		    strSQL="select SH_NO,SH_COLOR,sum(WORK_PLAN_QTY) WORK_PLAN_QTY2 " +
		    		"from FCMPS007 " +
		    		"where PLAN_NO='"+getPLAN_NO()+"' "+
		    		(SH_NO.length>0?" and SH_NO IN ("+SH_NO[0]+") ":" ")+
		    		"group by SH_NO,SH_COLOR ";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    if(rs.next()){
		    	 do {
		    		boolean iExist=false;
		    		if(!FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().isEmpty()) {
			    		for(int i=0;i<FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().size();i++) {
			    			if(rs.getString("SH_NO").equals(FCMPS_RCCP_INFO_Var.getLs_Finshed_SH().get(i))) {
			    				iExist=true;
			    				break;
			    			}
			    		}	
		    		}
		    		
		    		if(!iExist)ls_SH_COLOR.add(new String[] {rs.getString("SH_NO"),rs.getString("SH_COLOR")});
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();

		    String SH_NO2="";
		    
//		    int Next_Week=FCMPS_PUBLIC.getNext_Week(WORK_WEEK, FORE_PLAN_WEEKS-1);
		    
    		int NEXT_FORE_PLAN_WEEK=WORK_WEEK;

    		for(int iWeek=1;iWeek<FORE_PLAN_WEEKS;iWeek++) {
    			NEXT_FORE_PLAN_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_FORE_PLAN_WEEK, 1);
        		do {
        			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,NEXT_FORE_PLAN_WEEK,conn)==0) {
        				NEXT_FORE_PLAN_WEEK=FCMPS_PUBLIC.getNext_Week(NEXT_FORE_PLAN_WEEK,1);
        			}else {
        				break;
        			}
        			
        		}while(true);
    		}
		    
		    for(int iSH_COLOR=0;iSH_COLOR<ls_SH_COLOR.size();iSH_COLOR++) {
		    	SH_NO2=ls_SH_COLOR.get(iSH_COLOR)[0];	        		
	    				    				 		    		
		    	String SH_COLOR2=ls_SH_COLOR.get(iSH_COLOR)[1];			    			    			    	    
		    			    			    	
			    strSQL="select " +
                "sum(FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)) WORK_PLAN_QTY "+
                "from FCMPS010 "+
                "where PROCID='"+PROCID+"'"+
                "  and FA_NO='"+FA_NO+"'"+
                "  and nvl(OD_CODE,'N')='N' "+
                "  and IS_DISABLE='N' "+
                "  and SH_NO='"+SH_NO2+"'"+
                "  and SH_COLOR='"+SH_COLOR2+"'"+
                "  and WORK_WEEK_END>="+getWORK_WEEK()+
                "  and WORK_WEEK_END<="+NEXT_FORE_PLAN_WEEK+
                
                (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
  			                   
                "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                                 
   
                "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
                "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
                "       FROM FCMPS021 " +
                "      WHERE PROCID = '"+PROCID+"' " +
                "        AND FA_NO = '"+FA_NO+"' " +	                
//                "        AND SH_NO = FCMPS010.SH_NO " +
//                "        AND SH_COLOR = FCMPS010.SH_COLOR " +
//                "        AND SH_SIZE = FCMPS010.SH_SIZE " +
//                "        AND OD_PONO1 = FCMPS010.OD_PONO1 " +   
                "        AND WORK_WEEK<="+NEXT_FORE_PLAN_WEEK+") "+
                
	            //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
                //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
                "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
                "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
                "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
                "                      (SELECT SH_ARITCLE, PROC_SEQ "+
                "                       FROM FCPS22_1 "+
                "                       WHERE NEED_PLAN = 'Y' "+
                "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
                "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
                "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
                "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
                "                     from FCMPS007,FCMPS006 " +
                "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
                "                     and FCMPS006.IS_SURE='Y'"+
                "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
                "                     and (FCMPS006.WORK_WEEK>="+FCMPS_PUBLIC.getNext_Week(WORK_WEEK,1)+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
                "         or          "+
                "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
                "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
                "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
                "                      (SELECT SH_ARITCLE, PROC_SEQ "+
                "                       FROM FCPS22_1 "+
                "                       WHERE NEED_PLAN = 'Y' "+
                "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
                "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))) ";
	                   	      								
              //指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
              //為了避免因為後關提前導致其前關也提前
			    strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID)not in" +
                "             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
                "               where FA_NO='"+FA_NO+ "'"+
                "                 and WORK_WEEK>"+getWORK_WEEK()+
//                "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
//                "                 and PROCID=FCMPS010.PROCID" +
//                "                 and SH_NO=FCMPS010.SH_NO "+
//                "                 and SH_COLOR=FCMPS010.SH_COLOR "+
//                "                 and SH_SIZE=FCMPS010.SH_SIZE" +
                "              ) "; 
			    
				pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs3=pstmtData3.executeQuery();
		    
			    double WORK_PLAN_QTY=0;
			    if(rs3.next()){			    
			    	WORK_PLAN_QTY=rs3.getDouble("WORK_PLAN_QTY");			    	
			    }
			    rs3.close();
			    pstmtData3.close();
			    
			    if(WORK_PLAN_QTY>MIN_SHOOT_QTY) continue;
			    
			    strSQL="select " +
                "FCMPS010.OD_PONO1, "+
                "FCMPS010.STYLE_NO,"+
                "FCMPS010.SH_NO,"+
                "FCMPS010.SH_COLOR,"+
                "FCMPS010.SH_SIZE,"+
                "FCMPS010.PROC_SEQ,"+
                "FCMPS010.OD_QTY,"+
                "FCMPS010.OD_QTY-" +
                "nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0) WORK_PLAN_QTY,"+
                "FCMPS010.WORK_WEEK_START,"+
                "FCMPS010.WORK_WEEK_END,"+
                "FCMPS010.OD_SHIP,"+
                "to_char(FCMPS010.OD_SHIP,'IYIW') od_ship_week,"+
                "FCMPS010.OD_FGDATE, "+			
                "to_char(FCMPS010.OD_FGDATE,'IYIW') od_fgdate_week "+
                "from FCMPS010 "+
                "where PROCID='"+PROCID+"'"+
                "  and FA_NO='"+FA_NO+"'"+
                "  and nvl(OD_CODE,'N')='N' "+
                "  and IS_DISABLE='N' "+
                "  and SH_NO='"+SH_NO2+"'"+
                "  and SH_COLOR='"+SH_COLOR2+"'"+
                "  and WORK_WEEK_END>="+getWORK_WEEK()+
                "  and WORK_WEEK_END<="+NEXT_FORE_PLAN_WEEK+
                
                (getPLAN_PRIORITY_TYPE().equals("1") && getPLAN_BY_DATE().equals("B")?" and OD_FGDATE IS NOT NULL ":"")+
  			                   
                "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+	                                
   
                "  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN "+
                "      (SELECT FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID "+
                "         FROM FCMPS021 " +
                "        WHERE PROCID = '"+PROCID+"' " +
                "          AND FA_NO = '"+FA_NO+"' " +
//                "          AND SH_NO = FCMPS010.SH_NO " +
//                "          AND SH_COLOR = FCMPS010.SH_COLOR " +
//                "          AND SH_SIZE = FCMPS010.SH_SIZE " +
//                "          AND OD_PONO1 = FCMPS010.OD_PONO1 " +   
                "          AND WORK_WEEK<="+NEXT_FORE_PLAN_WEEK+") "+
                
	            //訂單相同順序其它制程,如果排在了下一周的計劃中,則訂單此制程不能提前.
                //這是為了保證配套,只要訂單有相同順序其它制程排了計劃,訂單此制程相當於已排且應和相同順序其它制程排在相同周次,所以不能排入本周了.
                "  and ((((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) IN "+
                "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
                "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
                "                      (SELECT SH_ARITCLE, PROC_SEQ "+
                "                       FROM FCPS22_1 "+
                "                       WHERE NEED_PLAN = 'Y' "+
                "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
                "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y')) "+	                   
                "                and ((FCMPS010.OD_PONO1,FCMPS010.SH_NO,FCMPS010.SH_SIZE,FCMPS010.SH_COLOR) not in" +
                "                    (select FCMPS007.OD_PONO1,FCMPS007.SH_NO,FCMPS007.SH_SIZE,FCMPS007.SH_COLOR " +
                "                     from FCMPS007,FCMPS006 " +
                "                     where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO" +
                "                     and FCMPS006.IS_SURE='Y'"+
                "                     and FCMPS006.FA_NO='"+FA_NO+"'"+
                "                     and (FCMPS006.WORK_WEEK>="+FCMPS_PUBLIC.getNext_Week(WORK_WEEK,1)+" and FCMPS007.PROCID<>'"+PROCID+"'))))" +
                "         or          "+
                "         ((FCMPS010.SH_NO,FCMPS010.PROC_SEQ) NOT IN "+
                "               (SELECT SH_ARITCLE, PROC_SEQ FROM FCPS22_1 "+
                "                WHERE (SH_ARITCLE, PROC_SEQ) IN "+
                "                      (SELECT SH_ARITCLE, PROC_SEQ "+
                "                       FROM FCPS22_1 "+
                "                       WHERE NEED_PLAN = 'Y' "+
                "                       GROUP BY SH_ARITCLE, PROC_SEQ "+
                "                       HAVING COUNT(PROC_SEQ)>1) AND PB_PTNO='"+PROCID+"' AND NEED_PLAN = 'Y'))) ";
	                   	      								
              //指定周次生產的訂單,有可能只有一個後關制程提前,比如針車 / 組底,
              //為了避免因為後關提前導致其前關也提前
			    strSQL=strSQL+"  and (FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID)not in" +
                "             (select FA_NO,OD_PONO1,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS013 " +
                "               where FA_NO='"+FA_NO +"'"+
                "                 and WORK_WEEK>"+getWORK_WEEK()+
//                "                 and OD_PONO1=FCMPS010.OD_PONO1 "+
//                "                 and PROCID=FCMPS010.PROCID" +
//                "                 and SH_NO=FCMPS010.SH_NO "+
//                "                 and SH_COLOR=FCMPS010.SH_COLOR "+
//                "                 and SH_SIZE=FCMPS010.SH_SIZE" +
                "              ) "; 
					 					
				if(getPLAN_PRIORITY_TYPE().equals("1")) {
					if(getPLAN_BY_DATE().equals("A")) {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP ";
					}else {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE ";
					}
				}
			
				if(getPLAN_PRIORITY_TYPE().equals("2")) {
					strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.WORK_WEEK_START";
				}
				
				if(getPLAN_PRIORITY_TYPE().equals("3")) {
					if(getPLAN_BY_DATE().equals("A")) {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_SHIP,FCMPS010.WORK_WEEKS ";
					}else {
						strSQL=strSQL+" Order by FCMPS010.SH_SIZE,FCMPS010.OD_FGDATE,FCMPS010.WORK_WEEKS ";
					}
				}
				
//				System.out.println(strSQL);
				pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs3=pstmtData3.executeQuery();
			    
				rs3.setFetchDirection(ResultSet.FETCH_FORWARD);
				rs3.setFetchSize(3000);
		    
			    if(rs3.next()){
			    	FCMPS_CLS_GenerateOrderPlan_MultiThread cls_GenerateOrderPlan=new FCMPS_CLS_GenerateOrderPlan_MultiThread();
			    	cls_GenerateOrderPlan.setFA_NO(FA_NO);
			    	cls_GenerateOrderPlan.setPLAN_BY_DATE(getPLAN_BY_DATE());
			    	cls_GenerateOrderPlan.setPLAN_NO(PLAN_NO);
			    	cls_GenerateOrderPlan.setWORK_WEEK(WORK_WEEK);
			    	cls_GenerateOrderPlan.setConnection(conn);
			    	cls_GenerateOrderPlan.setIs_FORE_PLAN_WEEKS(is_FORE_PLAN_WEEKS);
			    	cls_GenerateOrderPlan.setFORE_PLAN_WEEKS(getFORE_PLAN_WEEKS());
			    	cls_GenerateOrderPlan.setUP_USER(getUP_USER());
			    	cls_GenerateOrderPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
			    	cls_GenerateOrderPlan.setConfig_xml(config_xml);
			    	cls_GenerateOrderPlan.setPER_SIZE_ALLOW_OVER_NUM(PER_SIZE_ALLOW_OVER_NUM);
			    	cls_GenerateOrderPlan.setFCMPS_RCCP_INFO_Var(FCMPS_RCCP_INFO_Var);
			    	boolean iResult=cls_GenerateOrderPlan.doGeneratePlan(rs3,PROCID,false);			    	
			    	
			    	if(!cls_GenerateOrderPlan.getMessage().isEmpty())setMessage(cls_GenerateOrderPlan.getMessage());
			    	if(!iResult) {
			    		System.out.println("計算出現錯誤退出!");
					    iRet=-1;
			    	}
 		    	
			    }
			    rs3.close();
			    pstmtData3.close();	    
			    
			    if(iRet!=0) break;			    		
	    		    		    			    			    
		    }
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	iRet=-1;
	    }
		
		return iRet;
	}
	
	/**
	 * 刪除指定計劃
	 * @param PLAN_NO
	 * @return
	 */
	private boolean deletePlan(String PLAN_NO,String PROCID,Connection conn,String ...SH_NO) {
		boolean iRet=false;
		String strSQL="";
//		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		

		try{
		    
			strSQL="delete from FCMPS007 where PLAN_NO='"+PLAN_NO+"' and PROCID='"+PROCID+"'";
			if(SH_NO.length>0) {
				strSQL="delete from FCMPS007 " +
					   "where PLAN_NO='"+PLAN_NO+"' and PROCID='"+PROCID+"' and SH_NO IN ("+SH_NO[0]+")";
			}
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
		    pstmtData.close();
			
		    iRet=true;
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}
	    return iRet;
	}
	
	/**
	 * 計劃是否已發出
	 * @param PLAN_NO
	 * @param conn
	 * @return
	 */
	private boolean is_Sure(String PLAN_NO,Connection conn) {
		boolean iRet=true;
		String strSQL="";
//		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
		    
			strSQL="select * from FCMPS006 where PLAN_NO='"+PLAN_NO+"' and IS_SURE='Y'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(!rs.next()) {
			    iRet=false;
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
	 * 扣除周計劃制程已排的數量
	 * @param PLAN_NO 周計劃編號
	 */
	public void doDeductPlannedProcQty(
			String PLAN_NO,					
			String FA_NO,		
			String PROCID,
			Connection conn
			) {
		String strSQL="select WORK_WEEK,PROCID,sum(WORK_PLAN_QTY) WORK_PLAN_QTY " +
				      "from FCMPS007 " +
				      "where PLAN_NO='"+PLAN_NO+"'"+
				      "  and PROCID='"+PROCID+"' "+
				      "group by WORK_WEEK,PROCID ";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		try{
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
				PROC_WORK_QTY proc_Work_Qty=null;
				
		    	do {
		    		int WORK_WEEK_END=this.getWORK_WEEK();
		    		double WORK_PLAN_QTY=rs.getDouble("WORK_PLAN_QTY");
		    		
		    		boolean isExist=false;
		    		
		    		List<PROC_WORK_QTY> ls_PROC_WORK_QTY=FCMPS_RCCP_INFO_Var.getLs_PROC_WORK_QTY();
		    		
		    		//取制程的最大產能
		    		if(!ls_PROC_WORK_QTY.isEmpty()) {
		    			for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
		    				proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
		    				if(proc_Work_Qty.equals(FA_NO, PROCID, WORK_WEEK_END)) {
		    					proc_Work_Qty.setWORK_PLANNED_QTY(proc_Work_Qty.getWORK_PLANNED_QTY()+WORK_PLAN_QTY);
		    					isExist=true;
		    					break;
		    				}
		    			}
		    		}
		    		
		    		//制程本周的最大產量沒記錄. 增加進來
		    		if(!isExist) {
		    			proc_Work_Qty=new PROC_WORK_QTY();
		    			proc_Work_Qty.setFA_NO(FA_NO);
		    			proc_Work_Qty.setPROCID(PROCID);
		    			proc_Work_Qty.setWORK_WEEK(WORK_WEEK_END);
		    			proc_Work_Qty.setWORK_CAP_QTY(FCMPS_PUBLIC.get_PROC_Plan_QTY(FA_NO, WORK_WEEK_END,PROCID,conn)[0]);
		    			proc_Work_Qty.setWORK_PLANNED_QTY(WORK_PLAN_QTY);
		    			ls_PROC_WORK_QTY.add(proc_Work_Qty);
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

	}
	
	/**
	 * 扣除周計劃型體已排的數量
	 * @param PLAN_NO 周計劃編號
	 */
	public void doDeductPlannedSHQty(
			String PLAN_NO,					
			String FA_NO,		
			String PROCID,
			Connection conn,
			String... PLAN_SH_NO
			) {
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		try{
			
			strSQL="select WORK_WEEK,PROCID,PROC_SEQ,SH_NO,SH_SIZE,sum(WORK_PLAN_QTY) WORK_PLAN_QTY " +
		      "from FCMPS007 " +
		      "where PLAN_NO='"+PLAN_NO+"'"+
		      "  and PROCID='"+PROCID+"' "+
		      (PLAN_SH_NO.length>0?" and SH_NO='"+PLAN_SH_NO[0]+"' ":"")+
		      "group by WORK_WEEK,PROCID,PROC_SEQ,SH_NO,SH_SIZE ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		    rs.setFetchSize(3000);
		    
		    if(rs.next()){
		    	int WORK_WEEK_END=this.getWORK_WEEK();
		    	
				SH_WORK_QTY sh_Work_Qty=null;
				SH_KEY_SIZE sh_key_size=null;
				
	    		String OLD_SH_NO=rs.getString("SH_NO");
	    		String OLD_SH_SIZE=rs.getString("SH_SIZE");
	    		List<String[]> ls_STYLE_PART=null;
	    		
	    		boolean NEED_SHOOT=FCMPS_PUBLIC.getSH_Need_PROC(OLD_SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, conn);
	    		if(NEED_SHOOT) {
	    			Map<String,List<String[]>> ls_Share_Style_Size=FCMPS_RCCP_INFO_Var.getLs_Share_Style_Size();
	    			
	    			ls_STYLE_PART=ls_Share_Style_Size.get(FA_NO+OLD_SH_NO+OLD_SH_SIZE);
	    			if(ls_STYLE_PART==null) {
	    				if(!FCMPS_PUBLIC.ChokePointPart_is_SharePart(FA_NO, OLD_SH_NO, OLD_SH_SIZE, conn,WORK_WEEK_END)) {
	    					ls_STYLE_PART=new ArrayList<String[]>();
	    				}else {
	    					ls_STYLE_PART=FCMPS_PUBLIC.getSH_Share_SIZE_Max_MD_CAP(OLD_SH_NO,OLD_SH_SIZE, getFA_NO(), conn, WORK_WEEK_END, getWORK_DAYS());
//		    				ls_STYLE_PART=FCMPS_PUBLIC.getMD_Style_Share_Part(FA_NO,OLD_SH_NO,OLD_SH_SIZE,conn,WORK_WEEK_END);
//		    				ls_STYLE_PART=FCMPS_PUBLIC.getSH_Share_SIZE_Max_MD_CAP(ls_STYLE_PART, FA_NO, conn,WORK_WEEK_END, getWORK_DAYS());	    					
	    				}

	    				ls_Share_Style_Size.put(FA_NO+OLD_SH_NO+OLD_SH_SIZE, ls_STYLE_PART);
	    			}
	    			
	    		}
	    			    		
		    	do {
		    		String SH_NO=rs.getString("SH_NO");
		    		String SH_SIZE=rs.getString("SH_SIZE");
		    		double PROC_SEQ=rs.getDouble("PROC_SEQ");
		    		double WORK_PLAN_QTY=rs.getDouble("WORK_PLAN_QTY");
		    				    		
		    		if(!OLD_SH_NO.equals(SH_NO) || !OLD_SH_SIZE.equals(SH_SIZE)) {
		    			NEED_SHOOT=FCMPS_PUBLIC.getSH_Need_PROC(OLD_SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, conn);
		    			if(NEED_SHOOT) {
		    				//取得型體SIZE射出的共模部位
		    				Map<String,List<String[]>> ls_Share_Style_Size=FCMPS_RCCP_INFO_Var.getLs_Share_Style_Size();
			    			ls_STYLE_PART=ls_Share_Style_Size.get(FA_NO+SH_NO+SH_SIZE);
			    			if(ls_STYLE_PART==null) {
			    				if(!FCMPS_PUBLIC.ChokePointPart_is_SharePart(FA_NO, SH_NO, SH_SIZE, conn,WORK_WEEK_END)) {
			    					ls_STYLE_PART=new ArrayList<String[]>();
			    				}else {
//				    				ls_STYLE_PART=FCMPS_PUBLIC.getMD_Style_Share_Part(FA_NO,SH_NO,SH_SIZE,conn,WORK_WEEK_END);
//				    				ls_STYLE_PART=FCMPS_PUBLIC.getSH_Share_SIZE_Max_MD_CAP(ls_STYLE_PART, FA_NO, conn, WORK_WEEK_END,getWORK_DAYS());
			    					ls_STYLE_PART=FCMPS_PUBLIC.getSH_Share_SIZE_Max_MD_CAP(SH_NO,SH_SIZE, getFA_NO(), conn, WORK_WEEK_END, getWORK_DAYS());
			    				}

			    				ls_Share_Style_Size.put(FA_NO+SH_NO+SH_SIZE, ls_STYLE_PART);
			    			}			    						    			
		    			}
		    			OLD_SH_NO=SH_NO;
		    			OLD_SH_SIZE=SH_SIZE;
		    		}
		    		
					int SHOOT_WORK_WEEK=WORK_WEEK_END;
					
					double WORK_DAYS=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(getFA_NO(), SH_NO, PROCID,WORK_WEEK_END, conn);
					
	    			if(!PROCID.equals(FCMPS_PUBLIC.PROCID_SHOOT)) {
//	        			int Interval_Weeks=FCMPS_PUBLIC.getPROC_Interval(SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, PROCID, getConnection());
//	        			SHOOT_WORK_WEEK=FCMPS_PUBLIC.getPrevious_Week(getWORK_WEEK(),Interval_Weeks);
	    				
	    				SHOOT_WORK_WEEK=FCMPS_PUBLIC.getSHOOT_WORK_WEEK(FA_NO,SH_NO,PROCID,WORK_WEEK_END,conn);
	    			}
	    			
		    		boolean isExist=false;
		    		
		    		List<SH_WORK_QTY> ls_SH_WORK_QTY=this.FCMPS_RCCP_INFO_Var.getLs_SH_WORK_QTY();
		    		//取型體制程的最大產量
		    		if(!ls_SH_WORK_QTY.isEmpty()) {
		    			for(int i=0;i<ls_SH_WORK_QTY.size();i++) {
		    				sh_Work_Qty=ls_SH_WORK_QTY.get(i);
		    				if(sh_Work_Qty.equals(FA_NO, PROCID, SH_NO, WORK_WEEK_END)) {
		    					sh_Work_Qty.setWORK_PLANNED_QTY(sh_Work_Qty.getWORK_PLANNED_QTY()+WORK_PLAN_QTY);
		    					isExist=true;
		    					break;
		    				}
		    			}
		    		}
		    		
		    		//型體制程的本周最大產量沒記錄. 增加進來
		    		if(!isExist) {
		    			sh_Work_Qty=new SH_WORK_QTY();
		    			sh_Work_Qty.setFA_NO(FA_NO);
		    			sh_Work_Qty.setPROCID(PROCID);
		    			sh_Work_Qty.setSH_NO(SH_NO);
		    			sh_Work_Qty.setWORK_WEEK(WORK_WEEK_END);
		    			
		    			//以射出,組底和針車產能小的為限制,這是為了各製程的平準生產
//		    			sh_Work_Qty.setWORK_CAP_QTY(FCMPS_PUBLIC.get_SH_Plan_QTY(FA_NO,SH_NO,NEED_SHOOT, WORK_WEEK_END,conn));
						Double NextProc_PlanQTY=FCMPS_PUBLIC.getSH_OtherWeek_Plan_QTY(getFA_NO(), 
								PROC_SEQ, 
								PROCID, 
								SH_NO, 
								getWORK_WEEK(),
								getConnection());
						
						if(NextProc_PlanQTY!=null) {
							sh_Work_Qty.setWORK_CAP_QTY(NextProc_PlanQTY.doubleValue());
						}else {
			    			sh_Work_Qty.setWORK_CAP_QTY(FCMPS_PUBLIC.get_SH_Plan_QTY(
			    					getFA_NO(),
			    					SH_NO,
			    					PROCID, 
			    					NEED_SHOOT,
			    					WORK_WEEK_END,
			    					getConnection(),
			    					WORK_DAYS));
						}
		    			
		    			sh_Work_Qty.setWORK_PLANNED_QTY(WORK_PLAN_QTY);
		    			ls_SH_WORK_QTY.add(sh_Work_Qty);
		    		}	
		    				    		
		    		if(NEED_SHOOT) {
		    			isExist=false;
		    			
			            if(!ls_STYLE_PART.isEmpty()) {
			            	for(int n=0;n<ls_STYLE_PART.size();n++) {
			            		String part[]=ls_STYLE_PART.get(n);
			            		String share_SH_NO=part[0];
			            		String share_SH_SIZE=part[1];
			            		
			            		List<SH_KEY_SIZE> ls_SH_KEY_SIZE=FCMPS_RCCP_INFO_Var.getLs_SH_KEY_SIZE();
		    					//找到目前size的模具瓶頸產能
		    					for(int m=0;m<ls_SH_KEY_SIZE.size();m++) {
		    						sh_key_size=ls_SH_KEY_SIZE.get(m);
		    						
		    						if(sh_key_size.equals(FA_NO, PROCID, share_SH_NO, share_SH_SIZE, SHOOT_WORK_WEEK)) {
		    							sh_key_size.setWORK_PLANNED_QTY(sh_key_size.getWORK_PLANNED_QTY()+WORK_PLAN_QTY);
		    							isExist=true;
		    							break;
		    						}						
		    					}	
		    					
		    					if(isExist) break;
		    					
			            	}
			            }else {
	    					//找到目前size的模具瓶頸產能
			            	List<SH_KEY_SIZE> ls_SH_KEY_SIZE=FCMPS_RCCP_INFO_Var.getLs_SH_KEY_SIZE();
	    					for(int m=0;m<ls_SH_KEY_SIZE.size();m++) {
	    						sh_key_size=ls_SH_KEY_SIZE.get(m);
	    						if(sh_key_size.equals(FA_NO, PROCID, SH_NO, SH_SIZE, SHOOT_WORK_WEEK)) {
	    							sh_key_size.setWORK_PLANNED_QTY(sh_key_size.getWORK_PLANNED_QTY()+WORK_PLAN_QTY);
	    							isExist=true;
	    							break;
	    						}					
	    					}				            	
			            }
			            
			    		//型體size的本周最大產量沒記錄. 增加進來
			    		if(!isExist) {
		    				sh_key_size=new SH_KEY_SIZE();
		    				sh_key_size.setFA_NO(FA_NO);
		    				sh_key_size.setPROCID(PROCID);
		    				if(ls_STYLE_PART.isEmpty()) {
		    					sh_key_size.setSH_NO(SH_NO);//共模型體中產能最大的型體
		    					sh_key_size.setSH_SIZE(SH_SIZE);
//		    					sh_key_size.setWORK_CAP_QTY(FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(FA_NO,SH_NO,SH_SIZE, conn,WORK_WEEK_END));
		        				sh_key_size.setWORK_CAP_QTY(FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(            						
		                                getFA_NO(),
		                                SH_NO,
		                                SH_SIZE, 
		                                conn,
		                                SHOOT_WORK_WEEK,
		                                WORK_DAYS)); 
		    				}else {
		    					sh_key_size.setSH_NO(ls_STYLE_PART.get(0)[0]);//共模型體中產能最大的型體
		    					sh_key_size.setSH_SIZE(ls_STYLE_PART.get(0)[1]);
//		    					sh_key_size.setWORK_CAP_QTY(FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(FA_NO,ls_STYLE_PART.get(0)[0],ls_STYLE_PART.get(0)[1], conn,WORK_WEEK_END));
		        				sh_key_size.setWORK_CAP_QTY(FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(            						
		                                getFA_NO(),
		                                ls_STYLE_PART.get(0)[0],
		                                ls_STYLE_PART.get(0)[1], 
		                                conn,
		                                SHOOT_WORK_WEEK,
		                                WORK_DAYS)); 
		    				}
		    						    				
		    				sh_key_size.setWORK_WEEK(SHOOT_WORK_WEEK);			    			    			
			    			
		    				sh_key_size.setWORK_PLANNED_QTY(0);
		    				
		    				List<SH_KEY_SIZE> ls_SH_KEY_SIZE=FCMPS_RCCP_INFO_Var.getLs_SH_KEY_SIZE();		    				
			    			ls_SH_KEY_SIZE.add(sh_key_size);			    			
			    			
			    		}
			    		
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

	}

	/**
	 * 取型體下一個需要排計劃的制程
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	private Map<String,Integer> getSH_Next_PROC(String SH_NO,String PROCID) {
		Map<String,Integer> iRet=new HashMap<String,Integer>();
		String strSQL="";
		Connection conn =getConnection();
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
		    	int Interval=0;
		    	do {
		    		Interval++;
					strSQL="select PB_PTNO from fcps22_1 " +
					       "where SH_ARITCLE='"+SH_NO+"'"+
					       "  and PROC_SEQ="+rs.getDouble("PROC_SEQ")+
					       "  and NEED_PLAN='Y' "+
					       "order by PB_PTNO DESC";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){
				    	do {
				    		iRet.put(rs2.getString("PB_PTNO"), Interval);
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
		
		return iRet;
	}
	

	
	/**
	 * 取當型體前一個需要排計劃的制程
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	private Map<String,Integer> getSH_Prev_PROC(String SH_NO,String PROCID) {
		Map<String,Integer> iRet=new HashMap<String,Integer>();
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		try{

			strSQL="select distinct PROC_SEQ from fcps22_1 " +
				   "where SH_ARITCLE='"+SH_NO+"'"+
				   "  and PROC_SEQ<(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+PROCID+"') "+
				   "order by PROC_SEQ DESC";
				   
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	int Interval=0;
		    	do {		
		    		Interval++;
					strSQL="select PB_PTNO from fcps22_1 " +
					       "where SH_ARITCLE='"+SH_NO+"'"+
					       "  and PROC_SEQ="+rs.getDouble("PROC_SEQ")+
					       "  and NEED_PLAN='Y' "+
					       "order by PB_PTNO DESC";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){
				    	do {
				    		iRet.put(rs2.getString("PB_PTNO"), Interval);
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
		
		return iRet;
	}
	
	/**
	 * 型體的最大制程
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	private boolean isMax_PROC_SEQ(String SH_NO,String PROCID) {
		boolean iRet=false;
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select PROC_SEQ from fcps22_1 " +
				   "where SH_ARITCLE='"+SH_NO+"'"+
				   "  and PB_PTNO='"+PROCID+"'"+
				   "  and PROC_SEQ=(select max(PROC_SEQ) from FCPS22_1 " +
				   "                where SH_ARITCLE='"+SH_NO+"' and nvl(NEED_PLAN,'N')='Y') ";
				   
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
	 * 型體的相同制程的計劃是否已排定
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	private boolean isPlanned_Same_PROC_SEQ(String FA_NO,String SH_NO,String SH_COLOR,String PROCID,int WORK_WEEK) {
		boolean iRet=false;
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			String PLAN_NO="";
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
		    	PLAN_NO=rs.getString("plan_no");
		    }
			rs.close();
			pstmtData.close();

			if(PLAN_NO.equals("")) return iRet;
			
			strSQL="select count(*) iCount from FCMPS007 where PLAN_NO='"+PLAN_NO+"' and SH_NO='"+SH_NO+"' and SH_COLOR='"+SH_COLOR+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(rs.getInt("iCount")>0) iRet=true;
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
	 * 型體的後關制程的計劃是否已排定
	 * @param SH_NO
	 * @param PROCID
	 * @return
	 */
	private boolean isPlanned_Next_PROC_SEQ(String SH_NO,String SH_COLOR,String PROCID,int WORK_WEEK) {
		boolean iRet=false;
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		try{
			String PLAN_NO="";
			
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
						    	PLAN_NO=rs3.getString("plan_no");
						    }
							rs3.close();
							pstmtData3.close();		
							
							if(!PLAN_NO.equals("")) break;
				    	}while(rs2.next());				    				    	
				    }
		    		rs2.close();
		    		pstmtData2.close();
		    		
		    		if(!PLAN_NO.equals("")) break;
		    		
		    		Week_Interval++;
		    		
		    		
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();	

			if(PLAN_NO.equals("")) return iRet;
			
			strSQL="select count(*) iCount from FCMPS007 where PLAN_NO='"+PLAN_NO+"' and SH_NO='"+SH_NO+"' and SH_COLOR='"+SH_COLOR+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(rs.getInt("iCount")>0) iRet=true;
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
	private boolean isPlanned_Prev_PROC_SEQ(String SH_NO,String SH_COLOR,String PROCID,int WORK_WEEK) {
		boolean iRet=false;
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		try{
			String PLAN_NO="";
			
			//取此制程的後關制程
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
						    	PLAN_NO=rs3.getString("plan_no");
						    }
							rs3.close();
							pstmtData3.close();	
							
							if(!PLAN_NO.equals("")) break;
							
				    	}while(rs2.next());				    				    	
				    }
		    		rs2.close();
		    		pstmtData2.close();
		    		
		    		if(!PLAN_NO.equals("")) break;
		    		
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

			if(PLAN_NO.equals("")) return iRet;
			
			strSQL="select count(*) iCount from FCMPS007 where PLAN_NO='"+PLAN_NO+"' and SH_NO='"+SH_NO+"' and SH_COLOR='"+SH_COLOR+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(rs.getInt("iCount")>0) iRet=true;
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
	 * 取型體的可用SIZE
	 * @param SH_ANO
	 * @param STYLE_NO
	 * @return
	 */
	private ArrayList<String[]> getSH_SIZE(String SH_NO) {
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";

		Connection conn =getConnection();
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		String SIZE_FIELD="";
		for(int i=1;i<=40;i++) {
			if(!SIZE_FIELD.equals("")) SIZE_FIELD=SIZE_FIELD+",";
			SIZE_FIELD=SIZE_FIELD+"T"+i+",U"+i;
		}
		
		try{

			strSQL="select "+SIZE_FIELD+" from DSSH05 where SH_NO='"+SH_NO+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	for(int i=1;i<=40;i++) {
		    		if(FCMPS_PUBLIC.getValue(rs.getString("U"+i)).equals("T")) {
		    			iRet.add(new String[] {rs.getString("T"+i),String.valueOf(i)});
		    		}
		    	}
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
	 * 取得型體size的可排數和已排數
	 * @param SH_NO
	 * @param SH_SIZE
	 * @param PROCID
	 * @param PROC_SEQ
	 * @param ls_Share_Style_Size
	 * @param ls_SH_KEY_SIZE
	 * @return String,Integer[] String: SH_SIZE,Integer[0]: 可排數 Integer[1]:已排數
	 */
	private SH_KEY_SIZE getSH_SIZE_Allow_Plan_QTY(
			String SH_NO,
			String SH_SIZE,
			String PROCID,
			double PROC_SEQ,
			Map<String,List<String[]>> ls_Share_Style_Size,
			List<SH_KEY_SIZE> ls_SH_KEY_SIZE
			){
		
		List<String[]> ls_STYLE_SIZE=null;
		
		ls_STYLE_SIZE=ls_Share_Style_Size.get(getFA_NO()+SH_NO+SH_SIZE);
		if(ls_STYLE_SIZE==null) {
			if(!FCMPS_PUBLIC.ChokePointPart_is_SharePart(getFA_NO(), SH_NO, SH_SIZE, getConnection(),getWORK_WEEK())) {
				ls_STYLE_SIZE=new ArrayList<String[]>();
			}else {
//				Double NextProc_PlanQTY=FCMPS_PUBLIC.getSH_SIZE_OtherWeek_Plan_QTY(getFA_NO(), PROC_SEQ, PROCID, SH_NO,SH_SIZE,getWORK_WEEK(),getConnection());
//				if(NextProc_PlanQTY!=null) {
//					if(ls_STYLE_SIZE==null)ls_STYLE_SIZE=new ArrayList<String[]>();
//					ls_STYLE_SIZE.add(new String[] {SH_NO,SH_SIZE});
//				}else {
//					ls_STYLE_SIZE=FCMPS_PUBLIC.getSH_Share_SIZE_Max_MD_CAP(SH_NO, SH_SIZE, getFA_NO(), getConnection(), getWORK_WEEK(), getWORK_DAYS());
//				}
				ls_STYLE_SIZE=FCMPS_PUBLIC.getSH_Share_SIZE_Max_MD_CAP(SH_NO, SH_SIZE, getFA_NO(), getConnection(), getWORK_WEEK(), getWORK_DAYS());
			}

			ls_Share_Style_Size.put(getFA_NO()+SH_NO+SH_SIZE, ls_STYLE_SIZE);
		}	    		
		
		SH_KEY_SIZE sh_key_size=null;
		boolean isExist=false;
		
		String share_SH_NO="";
		String share_SH_SIZE="";
		
		int SHOOT_WORK_WEEK=getWORK_WEEK();
		if(!PROCID.equals(FCMPS_PUBLIC.PROCID_SHOOT)) {
//			int Interval_Weeks=FCMPS_PUBLIC.getPROC_Interval(SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, PROCID, getConnection());
//			SHOOT_WORK_WEEK=FCMPS_PUBLIC.getPrevious_Week(getWORK_WEEK(),Interval_Weeks);
			SHOOT_WORK_WEEK=FCMPS_PUBLIC.getSHOOT_WORK_WEEK(getFA_NO(),SH_NO,PROCID,getWORK_WEEK(),getConnection());
		}
		

		
		synchronized(ls_SH_KEY_SIZE) {
	        if(!ls_STYLE_SIZE.isEmpty()) {
	        	for(int n=0;n<ls_STYLE_SIZE.size();n++) {
	        		String part[]=ls_STYLE_SIZE.get(n);
	        		share_SH_NO=part[0];
	        		share_SH_SIZE=part[1];
	        		
					//找到目前size的模具瓶頸產能
	        		Iterator<SH_KEY_SIZE> it=ls_SH_KEY_SIZE.iterator();
	        		while(it.hasNext()) {
	        			sh_key_size=it.next();
						if(sh_key_size.getFA_NO().equals(getFA_NO())&& 
	 					   sh_key_size.getPROCID().equals(PROCID)&&
	 					   sh_key_size.getSH_NO().equals(share_SH_NO)&&
	 					   sh_key_size.getSH_SIZE().equals(share_SH_SIZE)&&
	 					   sh_key_size.getWORK_WEEK()==SHOOT_WORK_WEEK) {
	 					    isExist=true;
	 						break;
	 					}
	        		}
					
					if(isExist) break;
					
	        	}
	        	if(!isExist) {
	        		share_SH_NO="";
	        		share_SH_SIZE="";
	        	}
	        }else {
	    		Iterator<SH_KEY_SIZE> it=ls_SH_KEY_SIZE.iterator();
	    		while(it.hasNext()) {
	    			sh_key_size=it.next();
					if(sh_key_size.getFA_NO().equals(getFA_NO())&& 
					   sh_key_size.getPROCID().equals(PROCID)&&
					   sh_key_size.getSH_NO().equals(SH_NO)&&
					   sh_key_size.getSH_SIZE().equals(SH_SIZE)&&
					   sh_key_size.getWORK_WEEK()==SHOOT_WORK_WEEK) {
						    isExist=true;
							break;
					}
	    		}	            	
	        }
	        
			//型體size的本周最大產量沒記錄. 增加進來
			if(!isExist) {
				sh_key_size=new SH_KEY_SIZE();
				sh_key_size.setFA_NO(getFA_NO());
				sh_key_size.setPROCID(PROCID);    				
				
				sh_key_size.setWORK_WEEK(SHOOT_WORK_WEEK);
				    				
				if(!ls_STYLE_SIZE.isEmpty()) { //共模型體
					share_SH_NO=ls_STYLE_SIZE.get(0)[0];
					share_SH_SIZE=ls_STYLE_SIZE.get(0)[1];
					
					double WORK_DAYS=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(
							getFA_NO(), 
							share_SH_NO, 
							FCMPS_PUBLIC.PROCID_SHOOT,
							SHOOT_WORK_WEEK, 
							getConnection());
					
					sh_key_size.setWORK_DAYS(WORK_DAYS);
					
					sh_key_size.setSH_NO(share_SH_NO); //共模型體中產能最大的型體
					sh_key_size.setSH_SIZE(share_SH_SIZE);
					sh_key_size.setWORK_CAP_QTY(
							FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(
									getFA_NO(),
									share_SH_NO,
									share_SH_SIZE, 
							        getConnection(),
							        SHOOT_WORK_WEEK,
							        WORK_DAYS));

				}else {
					double WORK_DAYS=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(
							getFA_NO(), 
							SH_NO, 
							FCMPS_PUBLIC.PROCID_SHOOT,
							SHOOT_WORK_WEEK, 
							getConnection());
					
					sh_key_size.setWORK_DAYS(WORK_DAYS);
					
					sh_key_size.setSH_NO(SH_NO); 
					sh_key_size.setSH_SIZE(SH_SIZE);
					sh_key_size.setWORK_CAP_QTY(
							FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(
									getFA_NO(),
									SH_NO,
									SH_SIZE, 
									getConnection(),
									SHOOT_WORK_WEEK,
									WORK_DAYS));
				}
					    			
				sh_key_size.setWORK_PLANNED_QTY(0);
				ls_SH_KEY_SIZE.add(sh_key_size);	    			
				System.out.println(
						SH_NO+
						" SIZE:"+sh_key_size.getSH_SIZE()+
						" CAP:"+sh_key_size.getWORK_CAP_QTY()+
						" SHOOT_WEEK:"+SHOOT_WORK_WEEK+
						" WORK_DAYS:"+WORK_DAYS+
						" WORK_WEEK:"+getWORK_WEEK()+
						" 2B");	
			}
		}

		
		return sh_key_size;
	}
	
	private PROC_WORK_QTY getCurrent_PROC_WORK_QTY(
			String FA_NO,
			int WORK_WEEK,
			String PROCID,
			List<PROC_WORK_QTY> ls_PROC_WORK_QTY) {
		
		boolean isExist=false;
		PROC_WORK_QTY proc_Work_Qty=null;
		//取制程的最大產能
		if(!ls_PROC_WORK_QTY.isEmpty()) {
			for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
				proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
				if(proc_Work_Qty.getFA_NO().equals(FA_NO)&& 
				   proc_Work_Qty.getPROCID().equals(PROCID)&&
				   proc_Work_Qty.getWORK_WEEK()==WORK_WEEK) {
					isExist=true;
					break;
				}
			}
		}
		
		//制程本周的最大產量沒記錄. 增加進來
		if(!isExist) {
			proc_Work_Qty=new PROC_WORK_QTY();
			proc_Work_Qty.setFA_NO(FA_NO);
			proc_Work_Qty.setPROCID(PROCID);
			proc_Work_Qty.setWORK_WEEK(WORK_WEEK);
			proc_Work_Qty.setWORK_CAP_QTY(FCMPS_PUBLIC.get_PROC_Plan_QTY(FA_NO, WORK_WEEK,PROCID,getConnection())[0]);
			proc_Work_Qty.setWORK_PLANNED_QTY(0);
			
			ls_PROC_WORK_QTY.add(proc_Work_Qty);
		}	
		
		return proc_Work_Qty;
	}
	
	private SH_WORK_QTY getSH_WORK_PLAN_QTY(
			String FA_NO,
			String SH_NO,
			String PROCID,
			double PROC_SEQ,
			int WORK_WEEK,			
			List<SH_WORK_QTY> ls_SH_WORK_QTY,
			double... WORK_DAYS
			) {
		SH_WORK_QTY sh_Work_Qty=null;
		boolean isExist=false;
		
		//取型體制程的最大產量
		if(!ls_SH_WORK_QTY.isEmpty()) {
			for(int i=0;i<ls_SH_WORK_QTY.size();i++) {
				sh_Work_Qty=ls_SH_WORK_QTY.get(i);
				if(sh_Work_Qty.getFA_NO().equals(FA_NO)&& 
				   sh_Work_Qty.getPROCID().equals(PROCID)&&
				   sh_Work_Qty.getSH_NO().equals(SH_NO)&&
				   sh_Work_Qty.getWORK_WEEK()==WORK_WEEK) {
					isExist=true;
					break;
				}
			}
		}
		
		//型體制程的本周最大產量沒記錄. 增加進來
		if(!isExist) {
			sh_Work_Qty=new SH_WORK_QTY();
			sh_Work_Qty.setFA_NO(FA_NO);
			sh_Work_Qty.setPROCID(PROCID);
			sh_Work_Qty.setSH_NO(SH_NO);
			sh_Work_Qty.setWORK_WEEK(WORK_WEEK);
			
			Double NextProc_PlanQTY=FCMPS_PUBLIC.getSH_OtherWeek_Plan_QTY(FA_NO, PROC_SEQ, PROCID, SH_NO, WORK_WEEK,getConnection());
			if(NextProc_PlanQTY!=null) {
				sh_Work_Qty.setWORK_CAP_QTY(NextProc_PlanQTY.doubleValue());
			}else {
				
    			sh_Work_Qty.setWORK_CAP_QTY(FCMPS_PUBLIC.get_SH_Plan_QTY(
    					FA_NO,
    					SH_NO,
    					WORK_WEEK,
    					getConnection(),
    					FCMPS_PUBLIC.getSH_WorkDaysOfWeek(FA_NO, SH_NO, PROCID,getWORK_WEEK(), getConnection())));
			}
			
			sh_Work_Qty.setWORK_PLANNED_QTY(0);
			ls_SH_WORK_QTY.add(sh_Work_Qty);
			
		}			
		
		return sh_Work_Qty;
		
	}

	/**
	 * 取各制程允許的最低排產量
	 * @param FA_NO
	 * @param SH_NO
	 * @param PROCID
	 * @param WORK_WEEK
	 * @param ls_PROC_WORK_QTY
	 * @param ls_SH_WORK_QTY
	 * @return
	 */
	private double getOther_PROC_Allow_Plan_QTY(
			String FA_NO,
			String SH_NO,
			String PROCID,
			int WORK_WEEK,
			List<PROC_WORK_QTY> ls_PROC_WORK_QTY,
			List<SH_WORK_QTY> ls_SH_WORK_QTY) {
		
		double iRet=0;
		
		double cur_WORK_CAP_QTY=0;
		
		PROC_WORK_QTY proc_Work_Qty=getCurrent_PROC_WORK_QTY(FA_NO, WORK_WEEK, PROCID, ls_PROC_WORK_QTY);
		cur_WORK_CAP_QTY=proc_Work_Qty.getWORK_CAP_QTY();
		
		iRet=proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY();
		
		//取此制程的前關制程
		String strSQL="select FCPS22_1.PB_PTNO," +
				      "       FCPS22_1.PROC_SEQ," +
				      "       FCPS22_2.PB_PTNA," +
				      "       nvl(FCPS22_1.NEED_PLAN,'N') NEED_PLAN " +
				      "from FCPS22_1,FCPS22_2 " +
		              "where FCPS22_1.PB_PTNO=FCPS22_2.PB_PTNO" +
		              "  and FCPS22_1.SH_ARITCLE='"+SH_NO+"' " +
		              "  and FCPS22_1.PROC_SEQ<=(select PROC_SEQ from FCPS22_1 where SH_ARITCLE='"+SH_NO+"' and PB_PTNO='"+PROCID+"') "+
		              "  and FCPS22_1.PB_PTNO<>'"+PROCID+"' "+
//		              "  and FCPS22_1.NEED_PLAN='Y' " +
		              "ORDER BY PROC_SEQ DESC";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;

		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;

		Connection conn =getConnection();
		
		try{

			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs=pstmtData.executeQuery();

			if(rs.next()){
				int iWeek=0;
				double PROC_SEQ=rs.getDouble("PROC_SEQ");
				double CUR_PROC_SEQ=FCMPS_PUBLIC.getPROC_SEQ(SH_NO, PROCID,getConnection());
				if(CUR_PROC_SEQ>PROC_SEQ && rs.getString("NEED_PLAN").equals("Y")) iWeek++;
				
				do {
					
					if(rs.getString("NEED_PLAN").equals("N")) { 
						iWeek++;
						continue;
					}
					
					if(PROC_SEQ!=rs.getDouble("PROC_SEQ")) {
						iWeek++;
						PROC_SEQ=rs.getDouble("PROC_SEQ");
					}
					
					String PB_PTNO=rs.getString("PB_PTNO");
					String PB_PTNA=rs.getString("PB_PTNA");
					
					double WORK_CAP_QTY=0;
					
					//取前關制程的產能數
					strSQL="select WORK_CAP_QTY from FCMPS008 " +
				           "where FA_NO='"+FA_NO+"' and PROCID='"+PB_PTNO+"' and WORK_WEEK="+FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, iWeek);
					strSQL=strSQL+" UNION ALL ";
					strSQL=strSQL+"select WORK_CAP_QTY from FCMPS009 " +
					              "where FA_NO='"+FA_NO+"' and PROCID='"+PB_PTNO+"'";

				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){
			    		WORK_CAP_QTY=rs2.getDouble("WORK_CAP_QTY");   		
				    }
					rs2.close();
					pstmtData2.close();

					if(WORK_CAP_QTY==0)continue;					
		    		
		    		if(!ls_SH_WORK_QTY.isEmpty()) {
		    			for(int i=0;i<ls_SH_WORK_QTY.size();i++) {
		    				SH_WORK_QTY sh_WORK_QTY=ls_SH_WORK_QTY.get(i);
		    				
		       				if(sh_WORK_QTY.getFA_NO().equals(getFA_NO())&&
			   		    	   sh_WORK_QTY.getWORK_WEEK()==WORK_WEEK) {
		       					
		       					//減去已排型體中需要此制程的數量
		       					if(FCMPS_PUBLIC.is_Same_PROC_Weeks(SH_NO, sh_WORK_QTY.getSH_NO(), PROCID, PB_PTNO,getConnection())) {
		       						WORK_CAP_QTY=WORK_CAP_QTY-sh_WORK_QTY.getWORK_PLANNED_QTY();
		       					}

		       					if(WORK_CAP_QTY<=0) break;
		       					
		       				}	
		    	        }
		    		}
		    		
		    		if(WORK_CAP_QTY<iRet) iRet=WORK_CAP_QTY;
		    		
		    		if(iRet<=0) break;
		    		
				}while(rs.next());
				
			}
			rs.close();
			pstmtData.close();			
					
		}catch(Exception sqlex){
			sqlex.printStackTrace();
		}finally{	    	
//		Application.getApp().closeConnection(conn);
		}
		
		return iRet;		
	}

	/**
	 * 取得允許排產的型體陪位個數
	 * @param FA_NO
	 * @param STYLE_NO
	 * @param SH_NO
	 * @param SH_SIZE
	 * @param ls_Share_Style
	 * @param WORK_WEEK
	 * @return
	 */
	private int getPart_Allow_Count(String FA_NO,Connection conn) {
		
		int iRet=0;
		
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;		
		
		try{
			
			strSQL="select count(*) iCount from FCMPS026 where FA_NO='"+FA_NO+"' and  MH_STATE='Y'";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=rs.getInt("iCount")*FCMPS_PUBLIC.WEEK_MH_MAX_PRODUCE_PART_COUNT;
		    }
		    rs.close();
		    pstmtData.close();	
		    
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
	    return iRet;
	}
	
}
