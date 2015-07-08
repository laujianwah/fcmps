package fcmps.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 訂單分析<br>
 * 訂單來自FOS<br>
 * 確定訂單的最晚開工周次和最晚完工周次,排產數量<br>
 * 因了解到射出是瓶頸, 故直接以射出模具的周產能,分析訂單的最完開工周次<br>
 * @author dev17
 *
 */
public class FCMPS_CLS_AnalyseOrderFromFOS_Ex {
	private String OD_PONO1="";
	private String FA_NO="";
	private String STYLE_NO="";
	private Date OD_SHIP=null;
	private Date OD_FGDATE=null;
	private String OD_CODE="";
	private double OD_QTY=0;
	private String SH_NO="";
	private String KPR="";
	private String UP_USER="DEV";	
	private String Branch_Code="";
	private String E1_PO="";
	private String IS_REPLACEMENT="N";
	
	private boolean is_NON_Analyse_Exist_Order=false;
	
	private Connection conn=null;
	
	private List<String[]> SH_SIZE;

	private double MIN_WEEK_CAP_QTY=0;	
 
    
	/**
	 * 取得PO#
	 * @return
	 */
	public String getOD_PONO1() {
		return OD_PONO1;
	}

	/**
	 * 設定PO#
	 * @param od_pono1
	 */
	public void setOD_PONO1(String od_pono1) {
		OD_PONO1 = od_pono1;
	}

	/**
	 * 取得廠別
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
	 * 取型體代號
	 * @return
	 */
	public String getSTYLE_NO() {
		return STYLE_NO;
	}
	
	/**
	 * 設定型體代號
	 * @param style_no
	 */

	public void setSTYLE_NO(String style_no) {
		STYLE_NO = style_no;
	}

	/**
	 * 取訂單狀態
	 * @return
	 */

	public String getOD_CODE() {
		return OD_CODE;
	}
	
	/**
	 * 設定訂單狀態
	 * @param od_code
	 */

	public void setOD_CODE(String od_code) {
		OD_CODE = od_code;
	}
	
	/**
	 * 取FG Date
	 * @return
	 */

	public Date getOD_FGDATE() {
		return OD_FGDATE;
	}
	
	/**
	 * 設定FG Date
	 * @param od_fgdate
	 */

	public void setOD_FGDATE(Date od_fgdate) {
		OD_FGDATE = od_fgdate;
	}
	
	/**
	 * 取訂單交期
	 * @return
	 */

	public Date getOD_SHIP() {
		return OD_SHIP;
	}
	
	/**
	 * 設定訂單交期
	 * @param od_ship
	 */

	public void setOD_SHIP(Date od_ship) {
		OD_SHIP = od_ship;
	}
	
	/**
	 * 取訂單數
	 * @return
	 */

	public double getOD_QTY() {
		return OD_QTY;
	}

	/**
	 * 設定訂單數
	 * @param od_qty
	 */
	public void setOD_QTY(double od_qty) {
		OD_QTY = od_qty;
	}
	
	/**
	 * 取客戶型體
	 * @return
	 */
	public String getSH_NO() {
		return SH_NO;
	}

	/**
	 * 設定型體
	 * @param sh_no
	 */
	public void setSH_NO(String sh_no) {
		SH_NO = sh_no;
	}
	
	/**
	 * 是否為KPR訂單
	 * @return
	 */
	public String getKPR() {
		return KPR;
	}
	
	/**
	 * 設定是否為KPR訂單
	 * @param kpr
	 */

	public void setKPR(String kpr) {
		KPR = kpr;
	}
		
	public String getUP_USER() {
		return UP_USER;
	}

	public void setUP_USER(String up_user) {
		UP_USER = up_user;
	}

	public String getBranch_Code() {
		return Branch_Code;
	}

	public void setBranch_Code(String branch_Code) {
		Branch_Code = branch_Code;
	}
			
	public String getE1_PO() {
		return E1_PO;
	}
	

	public void setE1_PO(String e1_po) {
		E1_PO = e1_po;
	}

	/**
	 * 是否為替代訂單
	 * @return
	 */
	public String getIS_REPLACEMENT() {
		return IS_REPLACEMENT;
	}
	
	/**
	 * 設定是否為替代訂單
	 * @param is_replacement
	 */

	public void setIS_REPLACEMENT(String is_replacement) {
		IS_REPLACEMENT = is_replacement;
	}
	

	/**
	 * 取得資料庫連線
	 * @return
	 */
	public Connection getConnection() {
		return conn;
	}

	/**
	 * 設定資料庫連線
	 * @param conn
	 */
	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 已分析的訂單,不再重新分析
	 * @return
	 */
	public boolean Is_NON_Analyse_Exist_Order() {
		return is_NON_Analyse_Exist_Order;
	}

	/**
	 * 設定已分析的訂單,不再重新分析
	 * @param is_NON_Analyse_Exist_Order
	 */
	public void setIs_NON_Analyse_Exist_Order(boolean is_NON_Analyse_Exist_Order) {
		this.is_NON_Analyse_Exist_Order = is_NON_Analyse_Exist_Order;
	}

	public List<String[]> getSH_SIZE() {
		return SH_SIZE;
	}

	public void setSH_SIZE(List<String[]> sh_size) {
		SH_SIZE = sh_size;
	}

	public double getMIN_WEEK_CAP_QTY() {
		return MIN_WEEK_CAP_QTY;
	}

	public void setMIN_WEEK_CAP_QTY(double min_week_cap_qty) {
		MIN_WEEK_CAP_QTY = min_week_cap_qty;
	}
	
	public String doAnalyse(Connection conn) {
		String iRet="";
		String strSQL="";
				
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
//		ResultSet rs2=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;

		try{
			
    		String SH_NO=getSH_NO();
    		
    		ArrayList<Double> ls_PROC_SEQ=getPROC_SEQ(SH_NO);
    		
    		if(ls_PROC_SEQ.isEmpty()) {
    			iRet="沒有建立型體:"+SH_NO+" 的制程順序!";
    			return iRet;
    		}    		
    		    		    		
    		int WORK_WEEK_END=-1;
    		
    		//最晚開工和最晚完工周次計算優先依工廠FG Date 計算
    		//沒有FG Date 再依客人的交期計算
    		if(getOD_FGDATE()!=null) {
        		WORK_WEEK_END=Integer.valueOf(WeekUtil.getWeekOfYear(getOD_FGDATE(), true));
    		}else {
    			WORK_WEEK_END=Integer.valueOf(WeekUtil.getWeekOfYear(getOD_SHIP(), true));
    		}
    		
    		//因為fos中沒有顏色, 需要先找出PO# 型體的配色
			strSQL="select " +
               "sh_aritcleno,"+                   
               "sh_color,"+
               "sum(od_qty) od_qty "+
               "from dsod00 "+
               "where od_Pono1='"+getOD_PONO1()+"' "+
               "  and sh_aritcleno='"+SH_NO+"' "+
               "group by sh_aritcleno,sh_color";
	
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){

		    	String LEAN_NO="";
		    			    	
		    	if(getFA_NO().equals("FIC") && getBranch_Code().toUpperCase().equals("CHN")) { //上海是內銷,其它都是外銷
		    		LEAN_NO="SHANGHAI";
		    	}else {
		    		LEAN_NO=getLEAN_NO(getOD_PONO1(),conn);
		    	}
	    		
		    	FCMPS_CLS_ImportOrderFromFOS_Var cls_var=FCMPS_CLS_ImportOrderFromFOS_Var.getInstance();
		    	
		    	do {
		    		
		    		String SH_COLOR=FCMPS_PUBLIC.getValue(rs.getString("SH_COLOR"));
		    		
		    		for(int i=0;i<this.getSH_SIZE().size();i++) {
		    			String SH_SIZE=getSH_SIZE().get(i)[0];
			    		
			    		double SIZE_OD_QTY=0;
			    		
			    		//找出PO#,型體,顏色對應的SIZE數量
			    		strSQL="select sum(S"+getSH_SIZE().get(i)[1]+") OD_QTY from DSOD_03 " +
			    			   "where OD_NO IN (select od_no from DSOD00 " +
			    			   "           where sh_aritcleno='"+SH_NO+"' " +
			    			   "             and SH_COLOR='"+SH_COLOR+"' " +
			    			   "             and OD_PONO1='"+getOD_PONO1()+"')";
					    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
					    rs3=pstmtData3.executeQuery();
					    
					    if(rs3.next()){
					    	SIZE_OD_QTY=FCMPS_PUBLIC.getDouble(rs3.getDouble("OD_QTY"));
					    }
			    		rs3.close();
			    		pstmtData3.close();
			    		
			    		if(SIZE_OD_QTY==0) continue; //SIZE沒有訂單量
			    					    					  
			    		Map<String,Double> ls_SH_SIZE_MD_PAIR_QTY=cls_var.get_SH_SIZE_MD_PAIR_QTY();
			    		
			    		Double MD_PAIR_QTY=null;
			    		synchronized(ls_SH_SIZE_MD_PAIR_QTY) {
				    		MD_PAIR_QTY=ls_SH_SIZE_MD_PAIR_QTY.get(getFA_NO()+SH_NO+SH_SIZE);			    		
				    		if(MD_PAIR_QTY==null) {
				    			MD_PAIR_QTY=FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(getFA_NO(), SH_NO, SH_SIZE, getConnection(),WORK_WEEK_END);

				    			cls_var.add_SH_SIZE_MD_PAIR_QTY(getFA_NO()+SH_NO+SH_SIZE, MD_PAIR_QTY);
				    		}
			    		}
			    		
			    		double WORK_WEEKS=(int)(SIZE_OD_QTY/MD_PAIR_QTY);
			    		double WORK_DAYS=(int)(SIZE_OD_QTY/(MD_PAIR_QTY/5));
			    		
			    		if(SIZE_OD_QTY % MD_PAIR_QTY>0) WORK_WEEKS++;
			    		if(SIZE_OD_QTY % (MD_PAIR_QTY/5)>0) WORK_DAYS++;
			    		
			    		int WORK_WEEK_LAST=WORK_WEEK_END;
			    		
			    		for(int iPROC=0;iPROC<ls_PROC_SEQ.size();iPROC++) {
			    			
			    			WORK_WEEK_LAST=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_LAST, 1);
			    			
			    			Map<String,List<String>> ls_SH_NEED_PLAN_PROC=cls_var.get_SH_NEED_PLAN_PROC();
			    			
			    			List<String> ls_PROCID=null;
			    			synchronized(ls_SH_NEED_PLAN_PROC) {
				    			ls_PROCID=ls_SH_NEED_PLAN_PROC.get(SH_NO+ls_PROC_SEQ.get(iPROC));
				    			if(ls_PROCID==null) {
				    				ls_PROCID=getNeed_Plan_PROC(SH_NO, ls_PROC_SEQ.get(iPROC));
				    				cls_var.add_SH_NEED_PLAN_PROC(SH_NO+ls_PROC_SEQ.get(iPROC), ls_PROCID);
				    			}
			    			}
			    			
			    			if(!ls_PROCID.isEmpty()) { //需要排周計劃
			    				int WORK_WEEK_BEGIN=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_LAST, (int)(WORK_WEEKS-1));
			    				for(String PROCID:ls_PROCID) {

			    					strSQL="select OD_PONO1 from FCMPS010 " +
			    						   "where FA_NO='"+getFA_NO()+"' " +
			    						   "  and PROCID='"+PROCID+"' " +
			    						   "  and OD_PONO1='"+getOD_PONO1()+"' " +
			    						   "  and SH_NO='"+SH_NO+"' " +
			    						   "  and SH_COLOR='"+SH_COLOR+"' " +
			    						   "  and SH_SIZE='"+SH_SIZE+"' ";
								    pstmtData3 = getConnection().prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
								    rs3=pstmtData3.executeQuery();
								    
								    if(rs3.next()){
										if(!Is_NON_Analyse_Exist_Order()) {

											strSQL="update FCMPS010 set OD_QTY="+SIZE_OD_QTY;
											strSQL=strSQL+",OD_SHIP=TO_DATE('"+FCMPS_PUBLIC.getDate(getOD_SHIP(), "yyyy/MM/dd")+"','YYYY/MM/DD')";
											strSQL=strSQL+",OD_FGDATE=TO_DATE('"+FCMPS_PUBLIC.getDate(getOD_FGDATE(), "yyyy/MM/dd")+"','YYYY/MM/DD')";
											strSQL=strSQL+",OD_CODE='"+getOD_CODE()+"'";
											strSQL=strSQL+",UP_DATE=sysdate,";
											strSQL=strSQL+",UP_USER='"+getUP_USER()+"'";
											strSQL=strSQL+",WORK_WEEK_END="+WORK_WEEK_LAST;
											strSQL=strSQL+",WORK_WEEK_START="+WORK_WEEK_BEGIN;
											strSQL=strSQL+",WORK_WEEKS="+WORK_WEEKS;
											strSQL=strSQL+",WORK_WEEKS="+WORK_WEEKS;
											strSQL=strSQL+",WORK_DAYS="+WORK_DAYS;
											strSQL=strSQL+",MD_PAIR_QTY="+MD_PAIR_QTY;
											strSQL=strSQL+",PROC_SEQ="+ls_PROC_SEQ.get(iPROC);
											strSQL=strSQL+",KPR='"+getKPR()+"' ";
											strSQL=strSQL+"where FA_NO='"+getFA_NO()+"' " +
				    						              "  and PROCID='"+PROCID+"' " +
				    						              "  and OD_PONO1='"+getOD_PONO1()+"' " +
				    						              "  and SH_NO='"+SH_NO+"' " +
				    						              "  and SH_COLOR='"+SH_COLOR+"' " +
				    						              "  and SH_SIZE='"+SH_SIZE+"' ";
										    pstmtData2 = getConnection().prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
										    pstmtData2.execute();
										    pstmtData2.close();
										

										}
								    }else {


								    	strSQL="insert into FCMPS010 (procid, od_pono1, sh_no, sh_size, work_week_end, work_week_start, work_weeks, " +
							    		   "                      od_qty, od_ship, work_plan_qty, fa_no, sh_color, up_user, up_date, style_no, " +
							    		   "                      md_pair_qty, work_days, od_fgdate, is_disable, od_code, lean_no, proc_seq, kpr, " +
							    		   "                      expect_plan_qty, is_replacement, replaced_qty, e1_po) values (";
								    	strSQL=strSQL+"'"+PROCID+"'";
								    	strSQL=strSQL+",'"+getOD_PONO1()+"'";
								    	strSQL=strSQL+",'"+SH_NO+"'";
								    	strSQL=strSQL+",'"+SH_SIZE+"'";
								    	strSQL=strSQL+",'"+WORK_WEEK_LAST+"'";
								    	strSQL=strSQL+",'"+WORK_WEEK_BEGIN+"'";
								    	strSQL=strSQL+",'"+WORK_WEEKS+"'";
								    	strSQL=strSQL+",'"+SIZE_OD_QTY+"'";
								    	strSQL=strSQL+",TO_DATE('"+FCMPS_PUBLIC.getDate(getOD_SHIP(), "yyyy/MM/dd")+"','YYYY/MM/DD')";
								    	strSQL=strSQL+",0";
								    	strSQL=strSQL+",'"+getFA_NO()+"'";
								    	strSQL=strSQL+",'"+SH_COLOR+"'";
								    	strSQL=strSQL+",'"+getUP_USER()+"'";
								    	strSQL=strSQL+",sysdate";
								    	strSQL=strSQL+",'"+getSTYLE_NO()+"'";
								    	strSQL=strSQL+",'"+MD_PAIR_QTY+"'";
								    	strSQL=strSQL+",'"+WORK_DAYS+"'";
								    	strSQL=strSQL+",TO_DATE('"+FCMPS_PUBLIC.getDate(getOD_FGDATE(), "yyyy/MM/dd")+"','YYYY/MM/DD')";
								    	if(getIS_REPLACEMENT().equals("Y")) {
								    		strSQL=strSQL+",'Y'";
								    	}else {
								    		strSQL=strSQL+",'N'";
								    	}
								    	strSQL=strSQL+",'"+getOD_CODE()+"'";
								    	strSQL=strSQL+",'"+LEAN_NO+"'";
								    	strSQL=strSQL+",'"+ls_PROC_SEQ.get(iPROC)+"'";
								    	strSQL=strSQL+",'"+getKPR()+"'";
								    	strSQL=strSQL+",0";
								    	strSQL=strSQL+",'"+getIS_REPLACEMENT()+"'";
								    	strSQL=strSQL+",0";
								    	strSQL=strSQL+",'"+getE1_PO()+"'";
								    	strSQL=strSQL+")";
								    	
									    pstmtData2 = getConnection().prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
									    pstmtData2.execute();
									    pstmtData2.close();								    		
							    									
								    }
						    		rs3.close();
						    		pstmtData3.close();
				    				
			    				}

			    			}
			    		}				    			
		    		}

		    	}while(rs.next());
		    	
		    }else {
		    	System.out.println(getOD_PONO1()+" 訂單系統無此訂單!");
		    	iRet=getOD_PONO1()+" 訂單系統無此訂單!";
		    }
		    rs.close();
		    pstmtData.close();		    		
		   
		}catch(Exception sqlex){
	    	System.out.println(getOD_PONO1()+" "+sqlex.getMessage());
	    	iRet=getOD_PONO1()+" "+sqlex.getMessage();
	    }finally{	    	

		}			
		return iRet;
	}
	
	/**
	 * 取型體需要的制程順序
	 * @param SH_NO
	 * @return
	 */
	private ArrayList<Double> getPROC_SEQ(String SH_NO) {
		ArrayList<Double> iRet=new ArrayList<Double>();
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select distinct PROC_SEQ from fcps22_1 where SH_ARITCLE='"+SH_NO+"' Order By PROC_SEQ DESC";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		if(rs.getObject("PROC_SEQ")!=null) iRet.add(rs.getDouble("PROC_SEQ"));
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
	 * 型體制程是否需要排計劃<br>
	 * 因為有射出和針車,針車和組底,針車和轉印在同一周生產,且都要排計劃,故返回ArrayList <br>
	 * @param SH_NO
	 * @param PROC_SEQ 制程順序
	 * @return String 需要排計劃則返回制程代號, 否則返回空
	 */
	private ArrayList<String> getNeed_Plan_PROC(String SH_NO,double PROC_SEQ) {
		ArrayList<String> iRet=new ArrayList<String>();
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select PB_PTNO from fcps22_1 " +
				   "where SH_ARITCLE='"+SH_NO+"' " +
				   "  and NEED_PLAN='Y'"+
				   "  and PROC_SEQ="+PROC_SEQ;
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
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}	

	/**
	 * 是哪個線別的訂單
	 * @param OD_PONO1
	 * @return
	 */
	private String getLEAN_NO(String OD_PONO1,Connection conn) {
		String iRet="";
		String strSQL="";
//		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select nvl(lean_no, '無') lean_no "+
                   "from dsod00, fcpb07 "+
                   "where dsod00.cu_dest = fcpb07.cu_dest(+) "+
                   "  and dsod00.od_pono1 = '"+OD_PONO1+"'"+
                   "  and rownum=1";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=FCMPS_PUBLIC.getValue(rs.getString("lean_no"));
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
	
}
