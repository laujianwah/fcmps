package fcmps.ui;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;

import fcmps.domain.FCMPS010_BEAN;
import fcmps.ui.FCMPS_CLS_ForeGenerateRccpPlan_MultiThread.FCMPS_CLS_ForeGenerateRccpPlan_sub;

/**
 * 生成周計<BR>
 * 計算方法, 依交期向前倒推各制程的開工周次<br>
 * @author dev17
 *
 */
public class FCMPS_CLS_GenerateOrderPlan_MultiThread {
	private Connection main_conn=null;
    private List<CLS_RCCP_ERROR> ls_Message=Collections.synchronizedList(new ArrayList<CLS_RCCP_ERROR>());
    private FCMPS_RCCP_INFO FCMPS_RCCP_INFO_Var=null;
    
	private String FA_NO=""; //廠別
	private String PLAN_NO=""; //計劃編號
	private Integer WORK_WEEK=0; //計劃周次
	
	private String PLAN_BY_DATE="A";
	
	/**
	 * size允許超出產能的數量
	 */
	private double PER_SIZE_ALLOW_OVER_NUM=0;
	
	private static Log log = LogFactory.getLog(FCMPS_CLS_GenerateOrderPlan_MultiThread.class );
	
	private boolean is_UP_TO_CAP=false; //達到本周最大產能的標識
	
	/**
	 * 如果本周排不滿, 是否將後續訂單往前排.
	 */
	private boolean is_FORE_PLAN_WEEKS=false;  //如果本周排不滿, 是否將後續訂單往前排.
	
	/**
	 * 可提前周數
	 */
	private int FORE_PLAN_WEEKS=0;
	
	private String UP_USER="";
	
	/**
	 * 必須追加至本周
	 */
	private boolean is_Must_Append=false;
	
	/**
	 * 射出的最小排產量
	 */
	private int SHOOT_MIN_PRODUCE_QTY=516;
	
	private String config_xml="";
	
	/**
	 * 並行計算的SIZE數
	 */
	private int Parallel_Calcu_Color_Size=Runtime.getRuntime().availableProcessors();
	
	public void setConfig_xml(String config_xml) {
		this.config_xml = config_xml;
	}

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

	public String getUP_USER() {
		return UP_USER;
	}

	public void setUP_USER(String up_user) {
		UP_USER = up_user;
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
	 * 是否達到制程周產量
	 * @return
	 */
	public boolean is_UP_TO_CAP() {
		return is_UP_TO_CAP;
	}
	
	/**
	 * 設定是否達到制程周產量
	 * @param is_UP_TO_CAP
	 */

	private void setIs_UP_TO_CAP(boolean is_UP_TO_CAP) {
		this.is_UP_TO_CAP = is_UP_TO_CAP;
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
     * size允許超出產能的數量
     * @param per_size_allow_over_num
     */
	public void setPER_SIZE_ALLOW_OVER_NUM(double per_size_allow_over_num) {
		PER_SIZE_ALLOW_OVER_NUM = per_size_allow_over_num;
	}
	
	/**
	 * 取得訊息
	 * @return
	 */
	public List<CLS_RCCP_ERROR> getMessage() {
		return ls_Message;
	}
	
	private void setMessage(CLS_RCCP_ERROR message) {
		synchronized(ls_Message) {
			ls_Message.add(message);
		}
	}	

	private void addFinishSH(String SH_NO) {
		synchronized(getFCMPS_RCCP_INFO_Var().getLs_Finshed_SH()) {
			boolean existSH_NO=false;
			for(String orgSH_NO:getFCMPS_RCCP_INFO_Var().getLs_Finshed_SH()) {
				if(orgSH_NO.equals(SH_NO)) {
					existSH_NO=true;
					break;
				}
			}
			if(!existSH_NO)getFCMPS_RCCP_INFO_Var().getLs_Finshed_SH().add(SH_NO);
		}
	}
	
	private void addNot_Plan_PO_Size(String OD_PONO1,String SH_NO,String SH_COLOR,String SH_SIZE) {
		synchronized(getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size()) {
			boolean exist=false;
			for(String[] org:getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size()) {
				if(OD_PONO1.equals(org[0]) && SH_NO.equals(org[1]) && SH_COLOR.equals(org[2]) && SH_SIZE.equals(org[3])) {
					exist=true;
					break;
				}
			}
			if(!exist)getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().add(new String[] {OD_PONO1,SH_NO,SH_COLOR,SH_SIZE});
		}
	}
	

	/**
	 * 設定是否必須追加到本周
	 * @param is_Must_Append
	 */
	public void setIs_Must_Append(boolean is_Must_Append) {
		this.is_Must_Append = is_Must_Append;
	}
	
	/**
	 * 設定射出的最小排產量
	 * @param shoot_min_produce_qty
	 */
	public void setSHOOT_MIN_PRODUCE_QTY(int shoot_min_produce_qty) {
		SHOOT_MIN_PRODUCE_QTY = shoot_min_produce_qty;
	}
		
	/**
	 * 設定當前所用資料庫連線
	 * @param conn
	 */
    public void setConnection(Connection conn) {
    	main_conn = conn;
	}	

	private FCMPS_RCCP_INFO getFCMPS_RCCP_INFO_Var() {
		return FCMPS_RCCP_INFO_Var;
	}
	
	public void setFCMPS_RCCP_INFO_Var(FCMPS_RCCP_INFO var) {
		FCMPS_RCCP_INFO_Var = var;
	}
	


	/**
	 * 計算訂單的排產周次
	 * @param rs
	 * @param PROCID
	 * @param is_Spec_Week_Plan
	 * @return
	 */
	public boolean doGeneratePlan(
			ResultSet rs,	
			String PROCID,
			boolean is_Spec_Week_Plan) {
		boolean iRet=false;
				
		Stack<Connection> conn_stack=new Stack<Connection>();
		
		try{		

			ExecutorService pool=null;
//			List<Future<Map<FCMPS_CLS_GenerateOrderPlan_sub,Integer>>> resultList;
			CompletionService<Map<FCMPS_CLS_GenerateOrderPlan_sub,Integer>> resultList;
			
	    	pool=Executors.newFixedThreadPool(Parallel_Calcu_Color_Size);
	    		    		    	
	    	for(int i=0;i<Parallel_Calcu_Color_Size;i++) {
	    		Connection conn=GenericSessionFactory();
	    		conn_stack.push(conn);
	    	}	    	
	    	
//	    	resultList = new ArrayList<Future<Map<FCMPS_CLS_GenerateOrderPlan_sub,Integer>>>();
	    	resultList=new ExecutorCompletionService<Map<FCMPS_CLS_GenerateOrderPlan_sub,Integer>>(pool);

	    	int ThreadCounts=0;
	    	do {
	    		
	    		String OD_PONO1=FCMPS_PUBLIC.getValue(rs.getString("OD_PONO1"));
	    		String SH_NO=FCMPS_PUBLIC.getValue(rs.getString("SH_NO"));
	    		String SH_COLOR=FCMPS_PUBLIC.getValue(rs.getString("SH_COLOR"));
	    		String SH_SIZE=FCMPS_PUBLIC.getValue(rs.getString("SH_SIZE"));
	    		String STYLE_NO=FCMPS_PUBLIC.getValue(rs.getString("STYLE_NO"));    		
	    		
	    		double OD_QTY=FCMPS_PUBLIC.getDouble(rs.getDouble("OD_QTY"));
	    		
	    		//取得待排產數量
	    		double WORK_PLAN_QTY=FCMPS_PUBLIC.getDouble(rs.getDouble("WORK_PLAN_QTY"));
	    		
	    		int OD_FGDATE_WEEK=FCMPS_PUBLIC.getInt(rs.getString("od_fgdate_week"));
	    		int OD_SHIP_WEEK=FCMPS_PUBLIC.getInt(rs.getString("od_ship_week"));
	    		
	    		//最晚開工周次
	    		int WORK_WEEK_START=FCMPS_PUBLIC.getInt(rs.getString("WORK_WEEK_START"));
	    		
	    		//型體已排滿	    		
	    		synchronized(getFCMPS_RCCP_INFO_Var().getLs_Finshed_SH()) {
	    			boolean iFinshed=false;
	    			
		    		if(!getFCMPS_RCCP_INFO_Var().getLs_Finshed_SH().isEmpty()) {
			    		for(int i=0;i<getFCMPS_RCCP_INFO_Var().getLs_Finshed_SH().size();i++) {
			    			if(SH_NO.equals(getFCMPS_RCCP_INFO_Var().getLs_Finshed_SH().get(i))) {
			    				iFinshed=true;
			    				break;
			    			}
			    		}	
		    		}
		    		if(iFinshed && !is_Must_Append && !is_Spec_Week_Plan) continue;
	    		}
	    			    		
	    		//PO,型體,配色, Size 不能排入本周	    		
	    		synchronized(getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size()) {
	    			boolean iNotPlan=false;
		    		if(!getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().isEmpty()) {
			    		for(int i=0;i<getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().size();i++) {
			    			if(OD_PONO1.equals(getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().get(i)[0])&&
			    			   SH_NO.equals(getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().get(i)[1])&&
			    			   SH_COLOR.equals(getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().get(i)[2])&&
			    			   SH_SIZE.equals(getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().get(i)[3])) {
			    				iNotPlan=true;
			    				break;
			    			}
			    		}	
		    		}
		    		
		    		if(iNotPlan && !is_Must_Append && !is_Spec_Week_Plan) continue;
	    		}
	    			    			    		
	    		Boolean NEED_SHOOT=null;
	    		synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_NEED_SHOOT()) {
					NEED_SHOOT=getFCMPS_RCCP_INFO_Var().getLs_SH_NEED_SHOOT().get(SH_NO);
					if(NEED_SHOOT==null) {
						NEED_SHOOT=FCMPS_PUBLIC.getSH_Need_PROC(SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, main_conn);
						getFCMPS_RCCP_INFO_Var().getLs_SH_NEED_SHOOT().put(SH_NO, NEED_SHOOT);
					}
	    		}

	    		Boolean USE_CAP=null;
				synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_USE_CAP()) {
					USE_CAP=getFCMPS_RCCP_INFO_Var().getLs_SH_USE_CAP().get(SH_NO);
					if(USE_CAP==null) {
						USE_CAP=FCMPS_PUBLIC.get_SH_USE_CAP(SH_NO, PROCID, main_conn);
						getFCMPS_RCCP_INFO_Var().getLs_SH_USE_CAP().put(SH_NO, USE_CAP);
					}
				}
												
				FCMPS_CLS_GenerateOrderPlan_sub cls_sub=new FCMPS_CLS_GenerateOrderPlan_sub(
						OD_PONO1,
						SH_NO,
						SH_COLOR,
						SH_SIZE,
						STYLE_NO,
						OD_QTY,
						WORK_PLAN_QTY,
						OD_FGDATE_WEEK,
						PROCID,
						OD_SHIP_WEEK,
						NEED_SHOOT,
						USE_CAP.booleanValue(),
						WORK_WEEK_START,
						is_Spec_Week_Plan,
						conn_stack
						);
	    		
				
				resultList.submit(cls_sub);
				
				ThreadCounts++;	    		
	    		
	    	}while(rs.next());
		    	    	
	    	pool.shutdown();
	    	
	    	System.out.println("開始:"+ThreadCounts+" 筆訂單記錄的處理!");
	    		    	
	    	for(int i=0;i<ThreadCounts;i++) {
            	Map<FCMPS_CLS_GenerateOrderPlan_sub,Integer> result=resultList.take().get();
            	
            	Iterator<FCMPS_CLS_GenerateOrderPlan_sub> it=result.keySet().iterator();
            	while(it.hasNext()) {
            		FCMPS_CLS_GenerateOrderPlan_sub key=it.next();
            		Integer status=result.get(key);
            		if(status==FCMPS_CLS_ForeGenerateRccpPlan_sub.STATUS_CANCEL) {
            			System.out.println("無法連接資料庫!");
            		}
            		if(status==FCMPS_CLS_ForeGenerateRccpPlan_sub.STATUS_ERROR) {
            			System.out.println("發生錯誤!");
            		}
            	}
	    	}
	    	
		    resultList=null;
		    
		    System.out.println("結束:"+ThreadCounts+" 筆訂單記錄的處理!");
    			    	
		    iRet=true;
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
	    	for(int i=0;i<Parallel_Calcu_Color_Size;i++) {
	    		Connection conn=conn_stack.pop();
	    		closeConnection(conn);
	    	}	
	    	conn_stack.clear();
	    	conn_stack=null;
		}		
		
		return iRet;
	}
	
	/**
	 * 產生SessionFactory
	 *
	 */
	private Connection GenericSessionFactory() {
		Connection conn=null;
		try {
			File fConfig=new File(config_xml);
			if(!fConfig.exists()) {
				log.warn( "The Config file " + config_xml+" does not exist!" );
				return null;
			}
			Configuration config=new Configuration().configure(fConfig);	
			config.addClass(FCMPS010_BEAN.class);

			String USER=config.getProperty("connection.username");
			String URL=config.getProperty("connection.url");
			String PSW=config.getProperty("connection.password");
			String DRIVER=config.getProperty("connection.driver_class");
			
    		Class.forName(DRIVER); //加載驅動程序
    		conn=DriverManager.getConnection(URL,USER,PSW);
			
		}catch(Exception ex) {
//			ex.printStackTrace();					
		}
		return conn;
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
	
	private class FCMPS_CLS_GenerateOrderPlan_sub implements Callable{
		private String OD_PONO1;
		private String SH_NO;
		private String SH_COLOR;
		private String SH_SIZE;
		private String STYLE_NO;
		private double OD_QTY;
		private double WORK_PLAN_QTY;
		private int OD_FGDATE_WEEK;
		private String PROCID;
		private int OD_SHIP_WEEK;
		private boolean is_Spec_Week_Plan;
		private boolean NEED_SHOOT; 
		private boolean USE_CAP;
		private int WORK_WEEK_START;

		private Stack<Connection> conn_stack=null;
		
	    public static final int STATUS_CANCEL=0;
	    public static final int STATUS_RUNNING=1;
	    public static final int STATUS_ERROR=2;
	    public static final int STATUS_COMPLETE=3;
	    public static final int STATUS_WAIT=4;
	    
		public FCMPS_CLS_GenerateOrderPlan_sub(
				String OD_PONO1,
				String SH_NO,
				String SH_COLOR,
				String SH_SIZE,
				String STYLE_NO,
				double OD_QTY,
				double WORK_PLAN_QTY,
				int OD_FGDATE_WEEK,
				String PROCID,
				int OD_SHIP_WEEK,
				boolean NEED_SHOOT,
				boolean USE_CAP,
				int WORK_WEEK_START,
				boolean is_Spec_Week_Plan,
				Stack<Connection> conn_stack
				) {			
			this.OD_PONO1=OD_PONO1;
			this.SH_NO=SH_NO;
			this.SH_COLOR=SH_COLOR;
			this.SH_SIZE=SH_SIZE;
			this.STYLE_NO=STYLE_NO;
			this.OD_QTY=OD_QTY;
			this.WORK_PLAN_QTY=WORK_PLAN_QTY;
			this.OD_FGDATE_WEEK=OD_FGDATE_WEEK;
			this.PROCID=PROCID;
			this.OD_SHIP_WEEK=OD_SHIP_WEEK;
			this.is_Spec_Week_Plan=is_Spec_Week_Plan;
			this.NEED_SHOOT=NEED_SHOOT;
			this.USE_CAP=USE_CAP;
			this.WORK_WEEK_START=WORK_WEEK_START;
			this.conn_stack=conn_stack;
		}
				
		public Map<FCMPS_CLS_GenerateOrderPlan_sub,Integer> call() {
			Map<FCMPS_CLS_GenerateOrderPlan_sub,Integer> iRet=new HashMap<FCMPS_CLS_GenerateOrderPlan_sub,Integer>();
				
			Connection conn=null;
			try {
				
				conn=conn_stack.pop();
				
				if(conn==null) {
					iRet.put(this, STATUS_CANCEL);
					return iRet;
				}
				
	    		//型體已排滿	    		
	    		synchronized(getFCMPS_RCCP_INFO_Var().getLs_Finshed_SH()) {
	    			boolean iFinshed=false;
	    			
		    		if(!getFCMPS_RCCP_INFO_Var().getLs_Finshed_SH().isEmpty()) {
			    		for(int i=0;i<getFCMPS_RCCP_INFO_Var().getLs_Finshed_SH().size();i++) {
			    			if(SH_NO.equals(getFCMPS_RCCP_INFO_Var().getLs_Finshed_SH().get(i))) {
			    				iFinshed=true;
			    				break;
			    			}
			    		}	
		    		}
		    		if(iFinshed && !is_Must_Append) {
		    			iRet.put(this, STATUS_COMPLETE);
		    			return iRet;
		    		}
	    		}
				
	    		//PO,型體,配色, Size 不能排入本周	    		
	    		synchronized(getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size()) {
	    			boolean iNotPlan=false;
		    		if(!getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().isEmpty()) {
			    		for(int i=0;i<getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().size();i++) {
			    			if(OD_PONO1.equals(getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().get(i)[0])&&
			    			   SH_NO.equals(getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().get(i)[1])&&
			    			   SH_COLOR.equals(getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().get(i)[2])&&
			    			   SH_SIZE.equals(getFCMPS_RCCP_INFO_Var().getLs_Not_Plan_PO_Size().get(i)[3])) {
			    				iNotPlan=true;
			    				break;
			    			}
			    		}	
		    		}
		    		
		    		if(iNotPlan && !is_Must_Append && !is_Spec_Week_Plan) {
		    			iRet.put(this, STATUS_COMPLETE);
		    			return iRet;		    			
		    		}
	    		}
	    			    		
				if(doProcess(conn)) {
					iRet.put(this, STATUS_COMPLETE);
				}else {
					iRet.put(this, STATUS_ERROR);
				}
			}catch(Exception ex) {
				ex.printStackTrace();
				CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
				message.setOD_PONO1(OD_PONO1);
				message.setSH_COLOR(SH_COLOR);
				message.setSH_NO(SH_NO);
				message.setSH_SIZE(SH_SIZE);
				message.setSTYLE_NO(STYLE_NO);
				message.setOD_FGDATE_WEEK(OD_FGDATE_WEEK);
				message.setOD_QTY(OD_QTY);
    			message.setERROR(ex.getMessage());
    			setMessage(message);
    			
				iRet.put(this, STATUS_ERROR);
			}finally {
				conn_stack.push(conn);
			}

			return iRet;
		}
		
		private boolean doProcess(Connection conn) throws Exception{
			boolean iRet=false;

			List<String[]> ls_STYLE_SIZE=null;
			
			if(NEED_SHOOT) {				
				synchronized(getFCMPS_RCCP_INFO_Var().getLs_Share_Style_Size()) {
					ls_STYLE_SIZE=getFCMPS_RCCP_INFO_Var().getLs_Share_Style_Size().get(getFA_NO()+SH_NO+SH_SIZE);
					if(ls_STYLE_SIZE==null) {
						if(!FCMPS_PUBLIC.ChokePointPart_is_SharePart(getFA_NO(), SH_NO, SH_SIZE, conn,getWORK_WEEK())) {
							ls_STYLE_SIZE=new ArrayList<String[]>();
						}else {
							ls_STYLE_SIZE=FCMPS_PUBLIC.getSH_Share_SIZE_Max_MD_CAP(SH_NO, SH_SIZE, getFA_NO(), conn, getWORK_WEEK());
						}

						getFCMPS_RCCP_INFO_Var().getLs_Share_Style_Size().put(getFA_NO()+SH_NO+SH_SIZE, ls_STYLE_SIZE);
					}
				}
			}		    		
    		
			int SHOOT_WORK_WEEK=getWORK_WEEK();
			if(NEED_SHOOT && !PROCID.equals(FCMPS_PUBLIC.PROCID_SHOOT)) {    			
    			SHOOT_WORK_WEEK=FCMPS_PUBLIC.getSHOOT_WORK_WEEK(getFA_NO(),SH_NO,PROCID,getWORK_WEEK(),conn);
			}
			
			
			
    		if(NEED_SHOOT) {	    				    				    			
    			SH_KEY_SIZE sh_key_size=null;
    			boolean isExist=false;
	            if(!ls_STYLE_SIZE.isEmpty()) {
	            	for(int n=0;n<ls_STYLE_SIZE.size();n++) {
	            		String part[]=ls_STYLE_SIZE.get(n);
	            		String share_SH_NO=part[0];
	            		String share_SH_SIZE=part[1];
	            		
    					//找到目前size的模具瓶頸產能
	            		synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_KEY_SIZE()) {
		            		Iterator<SH_KEY_SIZE> it=getFCMPS_RCCP_INFO_Var().getLs_SH_KEY_SIZE().iterator();
		            		while(it.hasNext()) {
		            			sh_key_size=it.next();
		            			if(sh_key_size.equals(getFA_NO(), PROCID, share_SH_NO, share_SH_SIZE, SHOOT_WORK_WEEK)) {
	 	    						if(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY()<=0) {
	 	    							isExist=true;
	 	    						}
	 	    						
	 	    						break;
		            			}	            			
		            		}
	            		}
    					
    					if(isExist) break;
    					
	            	}
	            }else {
	            	synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_KEY_SIZE()) {
	            		Iterator<SH_KEY_SIZE> it=getFCMPS_RCCP_INFO_Var().getLs_SH_KEY_SIZE().iterator();
	            		while(it.hasNext()) {
	            			sh_key_size=it.next();
	            			
	            			if(sh_key_size.equals(getFA_NO(), PROCID, SH_NO, SH_SIZE, SHOOT_WORK_WEEK)) {
	    						if(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY()<=0) {
	    							isExist=true;
	    						}	    						
	    						break;	    					
	            			}
	            			
	            		}
	            	}

	            }
	            
	    		//型體size已達到模具產能
	    		if(isExist && !is_Must_Append && !is_Spec_Week_Plan) {
//	    			System.out.println("型體:"+SH_NO+" SIZE:"+SH_SIZE+" 已達到模具產能,無法排入!");
	    			addNot_Plan_PO_Size(OD_PONO1,SH_NO,SH_COLOR,SH_SIZE);
	    			return true;				    			
	    		}
    		}    		
    		
    		ArrayList<Double> ls_PROC_SEQ=getPROC_SEQ(conn,SH_NO);
    		
    		if(ls_PROC_SEQ.isEmpty()) {
    			throw new Exception("沒有建立型體:"+SH_NO+" 的制程順序!");
    		}
    		
    		int WORK_WEEK_END=OD_SHIP_WEEK;
    		
    		if(getPLAN_BY_DATE().equals("B")) {
    			if(FCMPS_PUBLIC.getValue(OD_FGDATE_WEEK).equals("")) {
	    			throw new Exception("訂單:"+OD_PONO1+" 沒有輸入FG Date");
    			}
    			WORK_WEEK_END=FCMPS_PUBLIC.getInt(OD_FGDATE_WEEK);
    		}
    		
    		int WORK_WEEK_LAST=WORK_WEEK_END;
    		
    		for(int iWeek=0;iWeek<ls_PROC_SEQ.size();iWeek++) {
    			List<String[]> ls_PB_PTNO=null;
    			synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_NEED_PLAN_PROC()) {
    				ls_PB_PTNO=getFCMPS_RCCP_INFO_Var().getLs_SH_NEED_PLAN_PROC().get(SH_NO+ls_PROC_SEQ.get(iWeek));
        			if(ls_PB_PTNO==null) {
        				ls_PB_PTNO=getNeed_Plan_PROC(conn,SH_NO, ls_PROC_SEQ.get(iWeek));
        				getFCMPS_RCCP_INFO_Var().getLs_SH_NEED_PLAN_PROC().put(SH_NO+ls_PROC_SEQ.get(iWeek), ls_PB_PTNO);
        			}	 
    			}
    			
    			for(String[] PB_PTNO:ls_PB_PTNO) {  
    				
	    			if(PB_PTNO[0].equals(PROCID)) {

		    		    iRet=calc_Order_Week(
		    		    		    conn,
				    				OD_PONO1, 
				    				STYLE_NO,
				    				SH_NO, 
				    				SH_COLOR, 
				    				SH_SIZE, 
				    				ls_PROC_SEQ,
				    				ls_PROC_SEQ.get(iWeek),
				    				OD_QTY, 
				    				WORK_PLAN_QTY, 
				    				PROCID, 
				    				PB_PTNO[1],
				    				WORK_WEEK_LAST,
				    				ls_STYLE_SIZE,
				    				NEED_SHOOT,				    				
				    				SHOOT_WORK_WEEK,
				    				USE_CAP,
				    				is_Spec_Week_Plan);
			    			
					    if(!iRet) {					    	
					    	System.out.println(STYLE_NO+" "+SH_NO+" "+OD_PONO1+" 發生錯誤!");
					    	return iRet;	
					    }
	    			}
    			}	    			
    		}
    				
    		return iRet;
		}
		
		/**
		 * 計算訂單的排產周次<br>
		 * @param OD_PONO1      PO#
		 * @param SH_NO         型體
		 * @param SH_COLOR      顏色
		 * @param SH_SIZE       size 
		 * @param OD_QTY        訂單數
		 * @param WORK_PLAN_QTY 待排產數量
		 * @param PROCID        制程
		 * @param WORK_WEEK_END 開始周次
		 * @param WORK_WEEK_LAST 最晚完工周次
		 * @param ls_PROC_WORK_QTY
		 * @param ls_SH_WORK_QTY
		 * @param ls_STYLE  //共模型體,SIZE,部位
		 * @return int 制程的開工周次
		 */
		private boolean calc_Order_Week(
				Connection conn,
				String OD_PONO1,
				String STYLE_NO,
				String SH_NO,
				String SH_COLOR,
				String SH_SIZE,
				ArrayList<Double> ls_PROC_SEQ,
				double PROC_SEQ,
				double OD_QTY,
				double WORK_PLAN_QTY,
				String PROCID,
				String PB_PTNA,
				int WORK_WEEK_LAST,
				List<String[]> ls_STYLE_SIZE,			
				boolean NEED_SHOOT,
				int SHOOT_WORK_WEEK,
				boolean USE_CAP,
				boolean is_Spec_Week_Plan
				) throws Exception{
			boolean iRet=false;					
							
    		boolean isExist=false;
    		
    		PROC_WORK_QTY proc_Work_Qty=null;
    		SH_WORK_QTY sh_Work_Qty=null;
    		SH_KEY_SIZE sh_key_size=null;
    		SH_COLOR_SIZE sh_color_size=null;
    		int MAX_OVER_NUMBER=0;
    			
    		synchronized(getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY()) {
	    		//取制程的最大產能
	    		if(!getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY().isEmpty()) {
	    			for(int i=0;i<getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY().size();i++) {
	    				proc_Work_Qty=getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY().get(i);
	    				
	    				if(proc_Work_Qty.equals(SH_NO, PROCID, getWORK_WEEK())) {
	    					isExist=true;
	    					break;
	    				}
	    			}
	    		}
	    		
	    		//制程本周的最大產量沒記錄. 增加進來
	    		if(!isExist) {
	    			proc_Work_Qty=new PROC_WORK_QTY();
	    			proc_Work_Qty.setFA_NO(getFA_NO());
	    			proc_Work_Qty.setPROCID(PROCID);
	    			proc_Work_Qty.setWORK_WEEK(getWORK_WEEK());
	    			proc_Work_Qty.setWORK_CAP_QTY(FCMPS_PUBLIC.get_PROC_Plan_QTY(getFA_NO(), getWORK_WEEK(),PROCID,conn)[0]);
	    			proc_Work_Qty.setWORK_PLANNED_QTY(0);
	    			
	    			getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY().add(proc_Work_Qty);
	    		}
	    		
	    		//如果已達到計劃周次的最大產量, 則退出
        		if(!is_Must_Append && 
        		   !is_Spec_Week_Plan &&
             	   proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY()<=0) {
 	    			  if(proc_Work_Qty.getWORK_WEEK()==getWORK_WEEK()) {
 	    				setIs_UP_TO_CAP(true);
 	    				return true;
 	    			  }	    				    			
             	   } 
    		}
    		
    		isExist=false;
    		
    		String share_SH_NO="";
    		String share_SH_SIZE="";    		
    		
    		if(NEED_SHOOT) {
    			
    			synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_KEY_SIZE()) {
	        		Iterator<SH_KEY_SIZE> it=getFCMPS_RCCP_INFO_Var().getLs_SH_KEY_SIZE().iterator();
	        		if(ls_STYLE_SIZE.isEmpty()) {
		        		while(it.hasNext()) {
		        			sh_key_size=it.next();
		        			
		        			if(sh_key_size.equals(SH_NO, PROCID, SH_NO, SH_SIZE, SHOOT_WORK_WEEK)) {
	    					    isExist=true;
	    						break;
		        			}
		        		}
	        		}else {
    	            	for(int n=0;n<ls_STYLE_SIZE.size();n++) {
    	            		String part[]=ls_STYLE_SIZE.get(n);
    	            		share_SH_NO=part[0];
    	            		share_SH_SIZE=part[1];
    	            		
        					//找到目前size的模具瓶頸產能
    	            		it=getFCMPS_RCCP_INFO_Var().getLs_SH_KEY_SIZE().iterator();
    	            		while(it.hasNext()) {
    	            			sh_key_size=it.next();
    	            			
    		        			if(sh_key_size.equals(SH_NO, PROCID, share_SH_NO, share_SH_SIZE, SHOOT_WORK_WEEK)) {
    	    					    isExist=true;
    	    						break;
    		        			}
    	            		}
        					
        					if(isExist) break;
        					
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
	    					
	    					double WORK_DAYS=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(FA_NO,share_SH_NO,SHOOT_WORK_WEEK,conn);
	    					
	    					sh_key_size.setWORK_DAYS(WORK_DAYS);
	    					
	        				sh_key_size.setSH_NO(share_SH_NO); //共模型體中產能最大的型體
	        				sh_key_size.setSH_SIZE(share_SH_SIZE);
	        					        				
	        				sh_key_size.setWORK_CAP_QTY(FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(            						
	                                getFA_NO(),
	                                share_SH_NO,
	                                share_SH_SIZE, 
	                                conn,
	                                SHOOT_WORK_WEEK,//以主型體射出周次的產能計
	                                WORK_DAYS));
	        				if(PROCID.equals(FCMPS_PUBLIC.PROCID_SHOOT)) {
	        					sh_key_size.setWORK_PLANNED_QTY(0);
	        				}else {
		        				double PREV_WEEK_QTY=FCMPS_PUBLIC.getShare_SH_Prev_Week_Plan_QTY(
		        						getFA_NO(), 
		        						share_SH_NO, 
		        						share_SH_SIZE, 
		        						PROCID, 
		        						SHOOT_WORK_WEEK,
		        						conn);
		        				
		        				sh_key_size.setWORK_PLANNED_QTY(PREV_WEEK_QTY);
	        				}

	        				
	    				}else {
	    					
	    					double WORK_DAYS=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(FA_NO,SH_NO,SHOOT_WORK_WEEK,conn);
	    					
	    					sh_key_size.setWORK_DAYS(WORK_DAYS);
	    					
	        				sh_key_size.setSH_NO(SH_NO); 
	        				sh_key_size.setSH_SIZE(SH_SIZE);
	        				
	        				sh_key_size.setWORK_CAP_QTY(FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(
	        						getFA_NO(),
	                                SH_NO,
	                                SH_SIZE,
	                                conn,
	                                SHOOT_WORK_WEEK,//以主型體射出周次的產能計
	                                WORK_DAYS));
	        				
	        				if(PROCID.equals(FCMPS_PUBLIC.PROCID_SHOOT)) {
	        					sh_key_size.setWORK_PLANNED_QTY(0);
	        				}else {
		        				double PREV_WEEK_QTY=FCMPS_PUBLIC.getShare_SH_Prev_Week_Plan_QTY(
		        						getFA_NO(), 
		        						SH_NO, 
		        						SH_SIZE, 
		        						PROCID, 
		        						SHOOT_WORK_WEEK,
		        						conn);
		        				
		        				sh_key_size.setWORK_PLANNED_QTY(PREV_WEEK_QTY);
	        				}
	        				
	    				}
	    				
	    				getFCMPS_RCCP_INFO_Var().getLs_SH_KEY_SIZE().add(sh_key_size);	    			
		    				    			
		    			System.out.println(sh_key_size.getSH_NO()+" SIZE:"+sh_key_size.getSH_SIZE()+" CAP:"+sh_key_size.getWORK_CAP_QTY());
		    		}	    
		    		
		    		if(sh_key_size.getWORK_CAP_QTY()==0) {	
		    			addFinishSH(SH_NO); 

		    			System.out.println("型體:"+SH_NO+" SIZE:"+SH_SIZE+" 沒有建立模具資料!");
		    			throw new Exception("型體:"+SH_NO+" SIZE:"+SH_SIZE+" 沒有建立模具資料!");    			
		    		}	
		    		
		    		//如果已達到共模size的模具產能,返回
		    		if(!is_Must_Append && 
		    		   !is_Spec_Week_Plan && 
		    		   sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY()<=0) {
		    			if(getWORK_WEEK()>=WORK_WEEK_START) {//如果本周已大於等於訂單最晚開工周次,則提示出來
			    			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
			    			message.setOD_PONO1(OD_PONO1);
			    			message.setSH_COLOR(SH_COLOR);
			    			message.setSH_NO(SH_NO);
			    			message.setSH_SIZE(SH_SIZE);
			    			message.setSTYLE_NO(STYLE_NO);
			    			message.setOD_FGDATE_WEEK(OD_FGDATE_WEEK);
			    			message.setOD_QTY(OD_QTY);
			    			message.setERROR("計劃周次大於最晚開工周次("+WORK_WEEK_START+"),模具產能已滿,無法排入!");
			    			setMessage(message);
		    			}
		    			addNot_Plan_PO_Size(OD_PONO1,SH_NO,SH_COLOR,SH_SIZE);
		    			return true;			
		    		}
		    		
		    		MAX_OVER_NUMBER=(int)(sh_key_size.getWORK_CAP_QTY()/sh_key_size.getWORK_DAYS()/8*PER_SIZE_ALLOW_OVER_NUM);
		    		
		    		if(is_Must_Append && 
		    		  !is_Spec_Week_Plan && 
		    		  sh_key_size.getWORK_CAP_QTY()+MAX_OVER_NUMBER-sh_key_size.getWORK_PLANNED_QTY()<=0) {
		    			if(getWORK_WEEK()>=WORK_WEEK_START) {//如果本周已大於等於訂單最晚開工周次,則提示出來
			    			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
			    			message.setOD_PONO1(OD_PONO1);
			    			message.setSH_COLOR(SH_COLOR);
			    			message.setSH_NO(SH_NO);
			    			message.setSH_SIZE(SH_SIZE);
			    			message.setSTYLE_NO(STYLE_NO);
			    			message.setOD_FGDATE_WEEK(OD_FGDATE_WEEK);
			    			message.setOD_QTY(OD_QTY);
			    			message.setERROR("計劃周次大於最晚開工周次("+WORK_WEEK_START+"),模具產能已滿,無法排入!");
			    			setMessage(message);
		    			}

		    			return true;			
		    		}
		    		
    			}
    			    		
    		}    		   

    		isExist=false;
    		
    		synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_WORK_QTY()) {
	    		//取型體制程的最大產量
	    		if(!getFCMPS_RCCP_INFO_Var().getLs_SH_WORK_QTY().isEmpty()) {
	    			for(int i=0;i<getFCMPS_RCCP_INFO_Var().getLs_SH_WORK_QTY().size();i++) {
	    				sh_Work_Qty=getFCMPS_RCCP_INFO_Var().getLs_SH_WORK_QTY().get(i);
	    					    				
	    				if(sh_Work_Qty.equals(getFA_NO(), PROCID, SH_NO, getWORK_WEEK())) {
	    					isExist=true;
	    					break;
	    				}

	    			}
	    		}
	    		
	    		//型體制程的本周最大產量沒記錄. 增加進來
	    		if(!isExist) {
	    			sh_Work_Qty=new SH_WORK_QTY();
	    			sh_Work_Qty.setFA_NO(getFA_NO());
	    			sh_Work_Qty.setPROCID(PROCID);
	    			sh_Work_Qty.setSH_NO(SH_NO);
	    			sh_Work_Qty.setWORK_WEEK(getWORK_WEEK());
					
					double WORK_DAYS=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(FA_NO,SH_NO,getWORK_WEEK(),conn);
					
	    			sh_Work_Qty.setWORK_CAP_QTY(FCMPS_PUBLIC.get_SH_Plan_QTY(
	    					getFA_NO(),
	    					SH_NO,
	    					PROCID, 
	    					NEED_SHOOT,
	    					getWORK_WEEK(),
	    					conn,
	    					WORK_DAYS));
	    			System.out.println(SH_NO+"產能:"+sh_Work_Qty.getWORK_CAP_QTY()+" 天數:"+WORK_DAYS);
	    			
	    			sh_Work_Qty.setWORK_PLANNED_QTY(0);
	    			getFCMPS_RCCP_INFO_Var().getLs_SH_WORK_QTY().add(sh_Work_Qty);
	    			
	    		}	
	    		
				if(sh_Work_Qty.getWORK_CAP_QTY()<=0) {//因為在減去工令欠數後,產能有可能為負數,所以應為小於等於0
					addFinishSH(SH_NO); 

					System.out.println("型體:"+SH_NO+" 制程:"+PB_PTNA+" 沒有設定型體產能!");
					throw new Exception("型體:"+SH_NO+" 制程:"+PB_PTNA+" 沒有設定型體產能!");
				}	
				
	    		//如果已達到型體的制程的最大產量,返回
				//但如果是指定周次排產,則可以大於產能限制
	    		if(!is_Must_Append && 
	    	       !is_Spec_Week_Plan && 			
	    	       sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY()<=0) {//型體已排滿,無法排入!		
	    			if(getWORK_WEEK()>=WORK_WEEK_START) {//如果本周已大於等於訂單最晚開工周次,則提示出來
		    			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
		    			message.setOD_PONO1(OD_PONO1);
		    			message.setSH_COLOR(SH_COLOR);
		    			message.setSH_NO(SH_NO);
		    			message.setSH_SIZE(SH_SIZE);
		    			message.setSTYLE_NO(STYLE_NO);
		    			message.setOD_FGDATE_WEEK(OD_FGDATE_WEEK);
		    			message.setOD_QTY(OD_QTY);
		    			message.setERROR("計劃周次大於最晚開工周次("+WORK_WEEK_START+"),型體已排滿,無法排入!");
		    			setMessage(message);
	    			}
					return true;
	    	    }	
	    		
	    		//需射出的型體沒有排過, 但制程可排產的餘數小於 516
	    		//但如果是指定周次排產,則不受限
	    		if(!is_Must_Append &&
	    		   !is_Spec_Week_Plan &&
	    		   NEED_SHOOT &&
	    		   sh_Work_Qty.getWORK_PLANNED_QTY()<=0 && 
	    		   proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY()<SHOOT_MIN_PRODUCE_QTY) {

	    			addFinishSH(SH_NO); 
	    			System.out.println("計劃周次大於最晚開工周次("+WORK_WEEK_START+"),制程可排產的餘數小於"+SHOOT_MIN_PRODUCE_QTY);
	    			
	    			if(getWORK_WEEK()>=WORK_WEEK_START) {//如果本周已大於等於訂單最晚開工周次,則提示出來
		    			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
		    			message.setOD_PONO1(OD_PONO1);
		    			message.setSH_COLOR(SH_COLOR);
		    			message.setSH_NO(SH_NO);
		    			message.setSH_SIZE(SH_SIZE);
		    			message.setSTYLE_NO(STYLE_NO);
		    			message.setOD_FGDATE_WEEK(OD_FGDATE_WEEK);
		    			message.setOD_QTY(OD_QTY);
		    			message.setERROR("計劃周次大於最晚開工周次("+WORK_WEEK_START+"),制程可排產的餘數小於"+SHOOT_MIN_PRODUCE_QTY);
		    			setMessage(message);
	    			}
					return true;
	    		}	
	    		
			
    		}		    			    		    		  		
    		
    		//需射出的型體配色沒有排過, 但型體可排產的餘數小於 516
    		//但如果是指定周次排產,則不受限
    		synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_COLOR_QTY()) {
    			
    			boolean color_exist=false;
    			
				List<SH_COLOR_SIZE> SH_COLOR_QTY=getFCMPS_RCCP_INFO_Var().getLs_SH_COLOR_QTY().get(sh_key_size.getSH_NO()+PROCID);
				if(SH_COLOR_QTY!=null) {
					for(SH_COLOR_SIZE sh_color_qty:SH_COLOR_QTY) {
						if(sh_color_qty.equals(getFA_NO(), PROCID, SH_NO, SH_COLOR)) {
            				color_exist=true;
            				break;
						}
					}
				}
				
	    		if(!color_exist) {
	        		if(!is_Must_Append &&
	        	       NEED_SHOOT &&
	        	       !is_Spec_Week_Plan &&
	        	       sh_Work_Qty.getWORK_CAP_QTY()>=SHOOT_MIN_PRODUCE_QTY &&
	        	       sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY()<SHOOT_MIN_PRODUCE_QTY) { 		
	            			if(getWORK_WEEK()>=WORK_WEEK_START) {//如果本周已大於等於訂單最晚開工周次,則提示出來
	        	    			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
	        	    			message.setOD_PONO1(OD_PONO1);
	        	    			message.setSH_COLOR(SH_COLOR);
	        	    			message.setSH_NO(SH_NO);
	        	    			message.setSH_SIZE(SH_SIZE);
	        	    			message.setSTYLE_NO(STYLE_NO);
	        	    			message.setOD_FGDATE_WEEK(OD_FGDATE_WEEK);
	        	    			message.setOD_QTY(OD_QTY);
	        	    			message.setERROR("計劃周次大於最晚開工周次("+WORK_WEEK_START+"),型體可排產的餘數小於"+SHOOT_MIN_PRODUCE_QTY);
	        	    			setMessage(message);
	            			}
	        				return true;
	        	       } 
	        		
	        		if(!is_Must_Append &&
	             	   NEED_SHOOT &&
	             	   !is_Spec_Week_Plan &&
	             	   proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY()<SHOOT_MIN_PRODUCE_QTY) { 
		            			if(getWORK_WEEK()>=WORK_WEEK_START) {//如果本周已大於等於訂單最晚開工周次,則提示出來
		        	    			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
		        	    			message.setOD_PONO1(OD_PONO1);
		        	    			message.setSH_COLOR(SH_COLOR);
		        	    			message.setSH_NO(SH_NO);
		        	    			message.setSH_SIZE(SH_SIZE);
		        	    			message.setSTYLE_NO(STYLE_NO);
		        	    			message.setOD_FGDATE_WEEK(OD_FGDATE_WEEK);
		        	    			message.setOD_QTY(OD_QTY);
		        	    			message.setERROR("計劃周次大於最晚開工周次("+WORK_WEEK_START+"),制程可排產的餘數小於"+SHOOT_MIN_PRODUCE_QTY);
		        	    			setMessage(message);
		            			}
	             				return true;
	             	   }          		
	    		}
    		}
    		

			//控制配色個數
			if(!is_Spec_Week_Plan && !is_Must_Append && NEED_SHOOT) {
				synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_COLOR_ALLOW_COUNT()) {
					Integer color_allow_count=getFCMPS_RCCP_INFO_Var().getLs_SH_COLOR_ALLOW_COUNT().get(sh_key_size.getSH_NO());
					if(color_allow_count==null) {
						color_allow_count=5;
						getFCMPS_RCCP_INFO_Var().getLs_SH_COLOR_ALLOW_COUNT().put(sh_key_size.getSH_NO(), color_allow_count);						
					}
					synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_COLOR_QTY()) {
						if(!getFCMPS_RCCP_INFO_Var().getLs_SH_COLOR_QTY().isEmpty()) {							
							boolean color_exist=false;
							List<SH_COLOR_SIZE> SH_COLOR_QTY=getFCMPS_RCCP_INFO_Var().getLs_SH_COLOR_QTY().get(sh_key_size.getSH_NO()+PROCID);
							int color_count=0;
							if(SH_COLOR_QTY!=null) {
								color_count=SH_COLOR_QTY.size();
								for(SH_COLOR_SIZE sh_color_qty:SH_COLOR_QTY) {
									if(sh_color_qty.equals(getFA_NO(), PROCID, SH_NO, SH_COLOR)) {
			            				color_exist=true;
			            				break;
									}									
								}
							}
							
							//配色沒有排過,此已排配色大於允許的個數
							if(color_allow_count<=color_count && color_exist==false) return true;
							
						}						
					}
				}
			}
    		    		    
			double RESIDUE_QTY=0;
			
    		synchronized(proc_Work_Qty) {
    			    			
        		RESIDUE_QTY=proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY(); //餘數    		

        		int WORK_WEEK_START=getWork_Week_Start(conn, SH_NO, PROCID, getWORK_WEEK());
        		
        		for(int iWeek=ls_PROC_SEQ.size()-1;iWeek>=0;iWeek--) {
        			List<String[]> ls_PB_PTNO=null;
        			synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_NEED_PLAN_PROC()) {
        				ls_PB_PTNO=getFCMPS_RCCP_INFO_Var().getLs_SH_NEED_PLAN_PROC().get(SH_NO+ls_PROC_SEQ.get(iWeek));
            			if(ls_PB_PTNO==null) {
            				ls_PB_PTNO=getNeed_Plan_PROC(conn,SH_NO, ls_PROC_SEQ.get(iWeek));
            				getFCMPS_RCCP_INFO_Var().getLs_SH_NEED_PLAN_PROC().put(SH_NO+ls_PROC_SEQ.get(iWeek), ls_PB_PTNO);
            			}	 
        			}
       			        			        						        			
        			do {
            			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,WORK_WEEK_START,conn)==0) {
            				WORK_WEEK_START=FCMPS_PUBLIC.getNext_Week(WORK_WEEK_START, 1);	
            			}else {
            				break;
            			}
        			}while(true);
        			        			
        			for(String[] PB_PTNO:ls_PB_PTNO) {
        				
        	    		synchronized(getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY()) {
        	    			boolean Exists=false;
        	    			
        	    			PROC_WORK_QTY other_proc_Work_Qty=null;
        	    			
        		    		//取制程的最大產能
        		    		if(!getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY().isEmpty()) {
        		    			for(int i=0;i<getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY().size();i++) {
        		    				other_proc_Work_Qty=getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY().get(i);
        		    				
        		    				if(other_proc_Work_Qty.equals(getFA_NO(), PB_PTNO[0], WORK_WEEK_START)) {
        		    					Exists=true;
        		    					break;
        		    				}

        		    			}
        		    		}
        		    		
        		    		//制程本周的最大產量沒記錄. 增加進來
        		    		if(!Exists) {
        		    			other_proc_Work_Qty=new PROC_WORK_QTY();
        		    			other_proc_Work_Qty.setFA_NO(getFA_NO());
        		    			other_proc_Work_Qty.setPROCID(PB_PTNO[0]);
        		    			other_proc_Work_Qty.setWORK_WEEK(WORK_WEEK_START);
        		    			other_proc_Work_Qty.setWORK_CAP_QTY(FCMPS_PUBLIC.get_PROC_Plan_QTY(getFA_NO(), WORK_WEEK_START,PB_PTNO[0],conn)[0]);
        		    			other_proc_Work_Qty.setWORK_PLANNED_QTY(FCMPS_PUBLIC.get_PROC_Planed_QTY(getFA_NO(), getWORK_WEEK(), PB_PTNO[0], WORK_WEEK_START, conn));
        		    			        		    			        		    			
        		    			getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY().add(other_proc_Work_Qty);
        		    		}
        		    	    
        		    		if(PB_PTNO[0].equals("300") && WORK_WEEK_START==1521) {
        		    			System.out.println("1521 組底最大產能:"+other_proc_Work_Qty.getWORK_CAP_QTY()+" 當前排入:"+other_proc_Work_Qty.getWORK_PLANNED_QTY());
        		    		}
        		    		
        		    		if(other_proc_Work_Qty.getWORK_CAP_QTY()<=other_proc_Work_Qty.getWORK_PLANNED_QTY()) {
        		    			addFinishSH(SH_NO); 
        		    			return true;
        		    		}
        		    		
        		    		if(RESIDUE_QTY>other_proc_Work_Qty.getWORK_CAP_QTY()-other_proc_Work_Qty.getWORK_PLANNED_QTY()) {//其它制程餘數 
        		    			RESIDUE_QTY=other_proc_Work_Qty.getWORK_CAP_QTY()-other_proc_Work_Qty.getWORK_PLANNED_QTY(); 	
        		    		}
        		    		
        	    		}
        			}	  
        			
        			WORK_WEEK_START=FCMPS_PUBLIC.getNext_Week(WORK_WEEK_START, 1);	    
        		}
        		
        		if(RESIDUE_QTY>(sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY())) 
        			RESIDUE_QTY=sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY();
        		    	
        		if(is_Must_Append) RESIDUE_QTY=WORK_PLAN_QTY; //指定要排,可以大於制程產能,也可以大於型體產能,但不能大於SIZE模具產能
        		
        		if(NEED_SHOOT) {
    	    		if(RESIDUE_QTY>(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY())) 
    	    			RESIDUE_QTY=sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY();    	    			   		
        		}
        		
        		if(sh_color_size!=null) {
    	    		if(RESIDUE_QTY>(sh_color_size.getWORK_CAP_QTY()-sh_color_size.getWORK_PLANNED_QTY())) 
    	    			RESIDUE_QTY=sh_color_size.getWORK_CAP_QTY()-sh_color_size.getWORK_PLANNED_QTY();	    		
        		}
        		
        		if(RESIDUE_QTY>WORK_PLAN_QTY) RESIDUE_QTY=WORK_PLAN_QTY;  
        		
        		if(RESIDUE_QTY<=0) return true;  
        		
        		
    			if(USE_CAP) {	
				    				
    				WORK_WEEK_START=getWork_Week_Start(conn, SH_NO, PROCID, getWORK_WEEK());
    				
	        		for(int iWeek=ls_PROC_SEQ.size()-1;iWeek>=0;iWeek--) {
	        			List<String[]> ls_PB_PTNO=null;
	        			synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_NEED_PLAN_PROC()) {
	        				ls_PB_PTNO=getFCMPS_RCCP_INFO_Var().getLs_SH_NEED_PLAN_PROC().get(SH_NO+ls_PROC_SEQ.get(iWeek));
	            			if(ls_PB_PTNO==null) {
	            				ls_PB_PTNO=getNeed_Plan_PROC(conn,SH_NO, ls_PROC_SEQ.get(iWeek));
	            				getFCMPS_RCCP_INFO_Var().getLs_SH_NEED_PLAN_PROC().put(SH_NO+ls_PROC_SEQ.get(iWeek), ls_PB_PTNO);
	            			}	 
	        			}
	       			        				        			    				        			
	        			do {
	            			if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,WORK_WEEK_START,conn)==0) {
	            				WORK_WEEK_START=FCMPS_PUBLIC.getNext_Week(WORK_WEEK_START, 1);	
	            			}else {
	            				break;
	            			}
	        			}while(true);
	        			
	        			for(String[] PB_PTNO:ls_PB_PTNO) {
	        					        				
	        	    		synchronized(getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY()) {
		        				PROC_WORK_QTY other_proc_Work_Qty=null;
		        				
		        				boolean Exists=false;
		        				
	        		    		//取制程的最大產能
	        		    		if(!getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY().isEmpty()) {
	        		    			for(int i=0;i<getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY().size();i++) {
	        		    				other_proc_Work_Qty=getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY().get(i);
	        		    				
	        		    				if(other_proc_Work_Qty.equals(getFA_NO(), PB_PTNO[0], WORK_WEEK_START)) {
	        		    					Exists=true;
	        		    					break;
	        		    				}

	        		    			}
	        		    		}
	        		    		
	        		    		//制程本周的最大產量沒記錄. 增加進來
	        		    		if(!Exists) {
	        		    			other_proc_Work_Qty=new PROC_WORK_QTY();
	        		    			other_proc_Work_Qty.setFA_NO(getFA_NO());
	        		    			other_proc_Work_Qty.setPROCID(PB_PTNO[0]);
	        		    			other_proc_Work_Qty.setWORK_WEEK(WORK_WEEK_START);
	        		    			other_proc_Work_Qty.setWORK_CAP_QTY(FCMPS_PUBLIC.get_PROC_Plan_QTY(getFA_NO(), WORK_WEEK_START,PB_PTNO[0],conn)[0]);
	        		    			other_proc_Work_Qty.setWORK_PLANNED_QTY(FCMPS_PUBLIC.get_PROC_Planed_QTY(getFA_NO(), getWORK_WEEK(), PB_PTNO[0], WORK_WEEK_START, conn));
	   	        		    				        		    			
	        		    			getFCMPS_RCCP_INFO_Var().getLs_PROC_WORK_QTY().add(other_proc_Work_Qty);
	        		    		}
	        		    			        		    			        		    			        		    		
        		    			boolean PROC_USE_CAP=FCMPS_PUBLIC.get_SH_USE_CAP(SH_NO, PB_PTNO[0], conn);
        		    			if(PROC_USE_CAP) {
        		    				other_proc_Work_Qty.setWORK_PLANNED_QTY(other_proc_Work_Qty.getWORK_PLANNED_QTY()+RESIDUE_QTY);       		    					        		    			
        		    			}
	        		    				        		    	        		    		
	        	    		}        	    		
	        	    		
	        			}	  
	        			
	        			WORK_WEEK_START=FCMPS_PUBLIC.getNext_Week(WORK_WEEK_START, 1);	
	        		}
										
					sh_Work_Qty.setWORK_PLANNED_QTY(sh_Work_Qty.getWORK_PLANNED_QTY()+RESIDUE_QTY);
    			}
    			
    			if(NEED_SHOOT) sh_key_size.setWORK_PLANNED_QTY(sh_key_size.getWORK_PLANNED_QTY()+RESIDUE_QTY);
    			
				if(sh_color_size!=null) sh_color_size.setWORK_PLANNED_QTY(sh_color_size.getWORK_PLANNED_QTY()+RESIDUE_QTY);
				
	    		//如果已達到計劃周次的最大產量, 則退出
	    		if(!is_Spec_Week_Plan && !is_Must_Append && proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY()<=0) {
					setIs_UP_TO_CAP(true);			    			
	    		}

    		}
    		
    		doInsertDB(
    				conn,
    				getPLAN_NO(), 
    				OD_PONO1, 
    				SH_NO, 
    				SH_COLOR, 
    				SH_SIZE,
    				STYLE_NO,
    				PROCID,
    				PROC_SEQ,
    				share_SH_NO,
    				share_SH_SIZE,
    				sh_Work_Qty.getWORK_CAP_QTY(),
    				sh_key_size.getWORK_CAP_QTY(), 
    				WORK_WEEK_LAST,//客戶需求日,或是FG Date
    				OD_QTY,
    				RESIDUE_QTY,
    				NEED_SHOOT,
    				SHOOT_WORK_WEEK,
    				sh_key_size.getWORK_DAYS(),
    				USE_CAP,
    				UP_USER);

			//但因要配套, 所以顏色size要與其它制程一致.
											
			synchronized(getFCMPS_RCCP_INFO_Var().getLs_SH_COLOR_QTY()) {
				if(NEED_SHOOT) {										
				    //配色計數
					List<SH_COLOR_SIZE> SH_COLOR_QTY=getFCMPS_RCCP_INFO_Var().getLs_SH_COLOR_QTY().get(sh_key_size.getSH_NO()+PROCID);
					if(SH_COLOR_QTY==null) {
						SH_COLOR_QTY=new ArrayList<SH_COLOR_SIZE>();
						getFCMPS_RCCP_INFO_Var().getLs_SH_COLOR_QTY().put(sh_key_size.getSH_NO()+PROCID, SH_COLOR_QTY);
					}else {
						boolean color_exist=false;
						for(SH_COLOR_SIZE sh_color_qty:SH_COLOR_QTY) {
							if(sh_color_qty.equals(getFA_NO(), PROCID, SH_NO, SH_COLOR)) {
	            				color_exist=true;
	            				break;
							}
           	
						}
						
						if(!color_exist) {
							SH_COLOR_SIZE sh_color_qty=new SH_COLOR_SIZE();
							sh_color_qty.setFA_NO(getFA_NO());
							sh_color_qty.setPROCID(PROCID);
							sh_color_qty.setSH_COLOR(SH_COLOR);
							sh_color_qty.setSH_NO(SH_NO);
							SH_COLOR_QTY.add(sh_color_qty);
						}
					}										
				}						
				
			}					

			iRet=true;

			return iRet;
			
		}
		
		private void doInsertDB(
				Connection conn,
				String PLAN_NO,
				String OD_PONO1,
				String SH_NO,
				String SH_COLOR,
				String SH_SIZE,
				String STYLE_NO,
				String PROCID,
				double PROC_SEQ,
				String share_SH_NO,
				String share_SH_SIZE,
				double SH_CAP_QTY,
				double SIZE_CAP_QTY,
				int WORK_WEEK_END,
				double OD_QTY,
				double RESIDUE_QTY,
				boolean NEED_SHOOT,
				int SHOOT_WORK_WEEK,
				double WORK_DAYS,
				boolean USE_CAP,
				String UP_USER
				) throws Exception{
			PreparedStatement pstmtData = null;		
			ResultSet rs=null;

			PreparedStatement pstmtData2 = null;	

			String strSQL="select WORK_PLAN_QTY from FCMPS007 " +
            "where PLAN_NO='"+PLAN_NO+"'"+
            "  and OD_PONO1='"+OD_PONO1+"'"+
            "  and SH_NO='"+SH_NO+"'"+
            "  and SH_SIZE='"+SH_SIZE+"'"+
            "  and SH_COLOR='"+SH_COLOR+"'"+
            "  and PROCID='"+PROCID+"'"+
            "  and WORK_WEEK="+WORK_WEEK_END;

			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs=pstmtData.executeQuery();

			if(rs.next()){		
				strSQL="update FCMPS007 set WORK_PLAN_QTY=WORK_PLAN_QTY+"+RESIDUE_QTY+
	             "where PLAN_NO='"+getPLAN_NO()+"'"+			           
	             "  and OD_PONO1='"+OD_PONO1+"'"+
	             "  and SH_NO='"+SH_NO+"'"+
	             "  and SH_SIZE='"+SH_SIZE+"'"+
	             "  and SH_COLOR='"+SH_COLOR+"'"+
	             "  and PROCID='"+PROCID+"'"+
	             "  and WORK_WEEK="+WORK_WEEK_END;
				pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				pstmtData2.executeUpdate();
				pstmtData2.close();
								
			}else {
				strSQL="insert into FCMPS007 (OD_PONO1,OD_QTY,PLAN_NO,PROCID,PROC_SEQ,SH_COLOR,SH_NO,STYLE_NO," +
						"                     SH_SIZE,NEED_SHOOT,SHARE_SH_NO,SHARE_SIZE,WORK_WEEK,SH_CAP_QTY," +
						"                     SIZE_CAP_QTY,UP_DATE,UP_USER,WORK_PLAN_QTY,IS_USE_CAP,SHOOT_WORK_WEEK,WORK_DAYS)";
				strSQL=strSQL+" values (";
				strSQL=strSQL+"'"+OD_PONO1+"'";
				strSQL=strSQL+",'"+OD_QTY+"'";
				strSQL=strSQL+",'"+PLAN_NO+"'";
				strSQL=strSQL+",'"+PROCID+"'";
				strSQL=strSQL+",'"+PROC_SEQ+"'";
				strSQL=strSQL+",'"+SH_COLOR+"'";
				strSQL=strSQL+",'"+SH_NO+"'";
				strSQL=strSQL+",'"+STYLE_NO+"'";
				strSQL=strSQL+",'"+SH_SIZE+"'";
				strSQL=strSQL+",'"+(NEED_SHOOT?"Y":"N")+"'";
				strSQL=strSQL+",'"+share_SH_NO+"'";
				strSQL=strSQL+",'"+share_SH_SIZE+"'";
				strSQL=strSQL+",'"+WORK_WEEK_END+"'";
				strSQL=strSQL+",'"+SH_CAP_QTY+"'";
				strSQL=strSQL+","+(NEED_SHOOT?SIZE_CAP_QTY:"null");
				strSQL=strSQL+",sysdate";
				strSQL=strSQL+",'"+UP_USER+"'";
				strSQL=strSQL+",'"+RESIDUE_QTY+"'";
				strSQL=strSQL+(USE_CAP?",'Y'":",'N'");
				strSQL=strSQL+","+SHOOT_WORK_WEEK;
				strSQL=strSQL+","+WORK_DAYS;
				strSQL=strSQL+")";
				
				pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				pstmtData2.execute();
				pstmtData2.close();				
			}
			rs.close();
			pstmtData.close();
			
			//更新訂單的預排數量
			strSQL="update FCMPS010 " +
				   "set EXPECT_PLAN_QTY=(select nvl(sum(work_plan_Qty),0) from FCMPS007 " +
				   "  where OD_PONO1=FCMPS010.OD_PONO1" +
				   "    and FA_NO=FCMPS010.FA_NO" +
				   "    and SH_NO=FCMPS010.SH_NO" +
				   "    and SH_COLOR=FCMPS010.SH_COLOR" +
				   "    and SH_SIZE=FCMPS010.SH_SIZE" +
				   "    and PROCID=FCMPS010.PROCID" +
				   "    and PLAN_NO='"+getPLAN_NO()+"'"+
				   ") where OD_PONO1='"+OD_PONO1+"'"+
				   "    and SH_NO='"+SH_NO+"'"+
				   "    and SH_SIZE='"+SH_SIZE+"'"+
				   "    and SH_COLOR='"+SH_COLOR+"'"+
				   "    and PROCID='"+PROCID+"'"+
				   "    and FA_NO='"+getFA_NO()+"'";
			
			pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    pstmtData2.executeQuery();	
		    pstmtData2.close();	

		}		
		
		private int getWork_Week_Start(				
				Connection conn,
				String SH_NO,
				String PROCID,
				int WORK_WEEK) {
			PreparedStatement pstmtData = null;		
			ResultSet rs=null;
			int Work_Week_Start=WORK_WEEK;
			
			try{
												
				String strSQL="select distinct PROC_SEQ  "+
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
				
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			
			return Work_Week_Start;
		}
	
		
		/**
		 * 取型體需要的制程順序
		 * @param SH_NO
		 * @return
		 */
		private ArrayList<Double> getPROC_SEQ(Connection conn,String SH_NO) {
			ArrayList<Double> iRet=new ArrayList<Double>();
			String strSQL="";

			PreparedStatement pstmtData = null;		
			ResultSet rs=null;
			
			try{

				strSQL="select distinct PROC_SEQ from fcps22_1 " +
					   " where SH_ARITCLE='"+SH_NO+"' " +
					   " Order By PROC_SEQ DESC";
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
//				Application.getApp().closeConnection(conn);
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
		private ArrayList<String[]> getNeed_Plan_PROC(Connection conn,String SH_NO,double PROC_SEQ) {
			ArrayList<String[]> iRet=new ArrayList<String[]>();
			String strSQL="";

			PreparedStatement pstmtData = null;		
			ResultSet rs=null;
			
			try{

				strSQL="select fcps22_1.PB_PTNO,fcps22_2.PB_PTNA from fcps22_1,fcps22_2 " +
					   "where fcps22_1.PB_PTNO=fcps22_2.PB_PTNO" +
					   "  and fcps22_1.SH_ARITCLE='"+SH_NO+"' " +
					   "  and fcps22_1.NEED_PLAN='Y'"+
					   "  and fcps22_1.PROC_SEQ="+PROC_SEQ+" "+
					   "order by fcps22_1.PB_PTNO desc ";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    if(rs.next()){
			    	do {
			    		iRet.add(new String[] {rs.getString("PB_PTNO"),rs.getString("PB_PTNA")});
			    	}while(rs.next());		    	
			    }
				rs.close();
				pstmtData.close();

			}catch(Exception sqlex){
		    	sqlex.printStackTrace();
		    }finally{	    	
//				Application.getApp().closeConnection(conn);
			}		
			
			return iRet;
		}
	}
}
