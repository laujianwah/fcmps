package fcmps.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.cfg.Configuration;

import dsc.util.function.UUID;
import fcmps.domain.FCMPS010_BEAN;
import fcmps.domain.FCMPS021_BEAN;

/**
 * 在導入FOS後,按照系統設定的制程標準產能,型體標準產能,依FG Date預排各訂單應分佈在哪些周次生產較合理
 * @author dev17
 *
 */
public class FCMPS_CLS_ForeGenerateRccpPlan_MultiThread extends TestCase{
	private String FA_NO="";
	private Connection conn=null;
	
//	private ArrayList<CLS_RCCP_ERROR> ls_Message=new ArrayList<CLS_RCCP_ERROR>();
	

    private String config_xml="";
    
    private String UP_USER="DEV";
    private String output="";
    
    private static Log log = LogFactory.getLog( FCMPS_CLS_ForeGenerateRccpPlan_MultiThread.class );
    private FCMPS_CLS_ForeGenerateRccpPlan_Var cls_var=null;
    
    private ExecutorService pool=null;
    /**
     * 同時計算幾個型體配色
     */
    private int Parallel_Calcu_Colors=Runtime.getRuntime().availableProcessors(); 
    
    private List<Future<Map<String,Integer[]>>> resultList;
    private List<FCMPS_CLS_ForeGenerateRccpPlan_MultiThread_Info> listThreads=new ArrayList<FCMPS_CLS_ForeGenerateRccpPlan_MultiThread_Info>();
    
    private boolean isStopNow=false;
    
    private int SHOOT_MIN_PRODUCE_QTY=516; //射出最小排產量
    
    private boolean is_Finished=false;
    
    private int Max_Week=0;  //結止周次
    private int Min_Week=0;  //起始周次
    private int Cur_Week=0;  //當前周次  
    
    public void test_ForeGenerateRccpPlan() {
    	config_xml="C:\\temp\\20130121\\FTI.cfg.xml";
    	output="F:/臨時文件/2014/20140603";
    	int CURRENT_PLAN_WEEK=1422;
    	GenericSessionFactory();
    	this.setFA_NO("FIC");    	
    	if(!deleteFCMPS021()) {
    		return;
    	}
//    	ls_SH_COLOR_ALLOW_COUNT=getColor_Allow_Count(getFA_NO(),getConnection());

    	doGeneratePlan(CURRENT_PLAN_WEEK);
 
    	doPrint();
    }

    /**
     * 執行完成
     * @return
     */
	public boolean Is_Finished() {
		return is_Finished;
	}
	
	/**
	 * 當前周次
	 * @return
	 */




	public int getCur_Week() {
		return Cur_Week;
	}
	
	/**
	 * 結止周次
	 * @return
	 */

	public int getMax_Week() {
		return Max_Week;
	}
	
	
	/**
	 * 起始周次
	 * @return
	 */

	public int getMin_Week() {
		return Min_Week;
	}

	/**
	 * 產生SessionFactory
	 *
	 */
	private Connection GenericSessionFactory() {
		Connection conn=null;
		try {
			File fConfig=new File(getConfig_XML());
			if(!fConfig.exists()) {
				log.warn( "The Config file " + getConfig_XML()+" does not exist!" );
				return null;
			}
			Configuration config=new Configuration().configure(fConfig);	
			config.addClass(FCMPS021_BEAN.class);
			config.addClass(FCMPS010_BEAN.class);

			String USER=config.getProperty("connection.username");
			String URL=config.getProperty("connection.url");
			String PSW=config.getProperty("connection.password");
			String DRIVER=config.getProperty("connection.driver_class");
			
    		Class.forName(DRIVER); //加載驅動程序
    		conn=DriverManager.getConnection(URL,USER,PSW);
			
		}catch(Exception ex) {
			ex.printStackTrace();		
		}
		return conn;
	}
	
	public String getConfig_XML() {
		return config_xml;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
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
		
	public String getUP_USER() {
		return UP_USER;
	}

	public void setUP_USER(String up_user) {
		UP_USER = up_user;
	}

	public void stopNow(boolean isstopNow) {
		this.isStopNow=isstopNow;
		if(pool!=null) pool.shutdownNow();
	}
	
	/**
	 * 取得資料庫連線
	 * @return
	 */
	public Connection getConnection() {
		return conn;
	}
	
	/**
	 * 取得訊息
	 * @return
	 */
	public List<CLS_RCCP_ERROR> getMessage() {
		return cls_var.getLs_Message();
	}
	
	private void setMessage(CLS_RCCP_ERROR message) {
		cls_var.addLs_Message(message);
	}	

	public List<FCMPS_CLS_ForeGenerateRccpPlan_MultiThread_Info> getListThreads() {
		return listThreads;
	}

	/**
	 * 設定同時計算幾周
	 * @param parallel_Calcu_Weeks
	 */
	public void setParallel_Calcu_Colors(int Parallel_Calcu_Colors) {
		this.Parallel_Calcu_Colors = Parallel_Calcu_Colors;
	}
	
	public void doGeneratePlan(String config_xml,int CURRENT_PLAN_WEEK,String...SH_NO) {
    	this.config_xml=config_xml;
    	conn=GenericSessionFactory();
    	try {
        	if(!deleteFCMPS021(SH_NO)) {
        		return;
        	}
        	
        	cls_var=FCMPS_CLS_ForeGenerateRccpPlan_Var.getInstance(getFA_NO(),getConnection());

        	doGeneratePlan(CURRENT_PLAN_WEEK,SH_NO);
    	}finally {
        	closeConnection(getConnection());
    	}

    }
	
    public void doGeneratePlan(String config_xml,String isReCalcu,int CURRENT_PLAN_WEEK,String...SH_NO) {
    	this.config_xml=config_xml;
    	conn=GenericSessionFactory();

    	try {
        	cls_var=FCMPS_CLS_ForeGenerateRccpPlan_Var.getInstance(getFA_NO(),getConnection());
        	
        	if(isReCalcu.toUpperCase().equals("Y")) {
        		doReCalcuPlan();
        	}else {
            	if(!deleteFCMPS021(SH_NO)) {
            		return;
            	}
        	}

        	doGeneratePlan(CURRENT_PLAN_WEEK,SH_NO);
    	}finally {
    		closeConnection(getConnection());
    	}    	
    	
    }
    
	private boolean deleteFCMPS021(String ...SH_NO) {
        boolean iRet=false;
		
		String strSQL="";
		
		Connection conn=getConnection();
		PreparedStatement pstmtData = null;		
		
		try{
			
			strSQL="TRUNCATE TABLE FCMPS021";
			
			if(SH_NO.length>0) {
				strSQL="delete from FCMPS021 where SH_NO IN ("+SH_NO[0]+")";
			}
			
//			strSQL="delete from FCMPS021 where STYLE_NO in ('16013','15991','14461') ";
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    pstmtData.execute();
		    pstmtData.close();
		    
			strSQL="update FCMPS010 set EXPECT_PLAN_QTY=0 " +
				   "where OD_QTY-nvl(WORK_PLAN_QTY,0)>0 "+
                   "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
                   "  and FCMPS010.IS_DISABLE='N' "+
                   "  and FCMPS010.OD_FGDATE is not null "+ 
                   (SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SH_NO[0]+") ":"")+
//                   "  and STYLE_NO='14268'"+
//                   "  and SH_NO='CBDVADERLNDCLG'"+
                   "  and FCMPS010.FA_NO='"+getFA_NO()+"' ";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    pstmtData.execute();
		    pstmtData.close();
		    
		    iRet=true;
		    
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
	    return iRet;
	}
    
    private boolean doReCalcuPlan() {
    	boolean iRet=false;
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		try {
			strSQL="select PROCID,WORK_WEEK,sum(WORK_PLAN_QTY) WORK_PLAN_QTY " +
				   "from FCMPS021 "+
				   "group by PROCID,WORK_WEEK";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
	        rs=pstmtData.executeQuery();
	 
	        if(rs.next()){
	        	do {
	        		
	        		String PROCID=rs.getString("PROCID");
	        		int WORK_WEEK_END=rs.getInt("WORK_WEEK");
	        		double WORK_PLAN_QTY=rs.getDouble("WORK_PLAN_QTY");
	        		
		        	PROC_WORK_QTY proc_Work_Qty=new PROC_WORK_QTY();
	    			proc_Work_Qty.setFA_NO(getFA_NO());
	    			proc_Work_Qty.setPROCID(PROCID);
	    			proc_Work_Qty.setWORK_WEEK(WORK_WEEK_END);
	    			double[] PROC_CAP_QTY=FCMPS_PUBLIC.get_PROC_Plan_QTY(getFA_NO(), WORK_WEEK_END,PROCID,getConnection());
	    			proc_Work_Qty.setWORK_CAP_QTY(PROC_CAP_QTY[0]);
	    			proc_Work_Qty.setWORK_MAX_CAP_QTY(PROC_CAP_QTY[1]);
	    			proc_Work_Qty.setWORK_PLANNED_QTY(WORK_PLAN_QTY);
	    			
	    			cls_var.addLs_PROC_WORK_QTY(proc_Work_Qty);
	    			
//	    			ls_PROC_WORK_QTY.add(proc_Work_Qty);
	    			
	    			strSQL="select SH_NO,share_SH_NO,SH_CAP_QTY,sum(work_plan_Qty) WORK_PLAN_QTY "+
                           "from fcmps021 "+
                           "where PROCID='"+PROCID+"' AND WORK_WEEK="+WORK_WEEK_END+" "+
                           "group by SH_NO,share_SH_NO,SH_CAP_QTY";
	    		    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
	    	        rs2=pstmtData2.executeQuery();
	    	 
	    	        if(rs2.next()){
	    	        	do {
	    	        		String SH_NO=rs2.getString("SH_NO");
	    	        		String share_SH_NO=FCMPS_PUBLIC.getValue(rs2.getString("share_SH_NO"));
	    	        		double SH_CAP_QTY=rs2.getDouble("SH_CAP_QTY");
	    	        		WORK_PLAN_QTY=rs2.getDouble("WORK_PLAN_QTY");	    	        			    	    			
	    	    			
	    	    			strSQL="select OD_FGDATE_WEEK,SH_COLOR,sum(work_plan_Qty) WORK_PLAN_QTY "+
	                           "from fcmps021 "+
	                           "where PROCID='"+PROCID+"' " +
	                           "  and WORK_WEEK="+WORK_WEEK_END+" "+
	                           "  and SH_NO='"+SH_NO+"'"+
	                           (!share_SH_NO.equals("")?" and share_SH_NO='"+share_SH_NO+"' ":" ")+	                           
	                           "group by OD_FGDATE_WEEK,SH_COLOR";
	    	    		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
	    	    	        rs3=pstmtData3.executeQuery();
	    	    	 
	    	    	        if(rs3.next()){
	    	    	        	do {
	    	    	        		
	    	    	        		String SH_COLOR=rs3.getString("SH_COLOR");
	    	    	        		double SH_COLOR_QTY=rs3.getDouble("WORK_PLAN_QTY");
	    	    	        		int OD_FGDATE_WEEK=rs3.getInt("OD_FGDATE_WEEK");
	    	    	        		
	    	    	        		Map<String,List<SH_COLOR_SIZE>> ls_SH_COLOR_SIZE =cls_var.getLs_SH_COLOR_SIZE(); 
	    	    	        		synchronized(ls_SH_COLOR_SIZE) {
		    	    		    		List<SH_COLOR_SIZE> al_SH_COLOR_SIZE=ls_SH_COLOR_SIZE.get(getFA_NO()+PROCID+OD_FGDATE_WEEK+SH_NO+SH_COLOR);

		    	    		    		if(al_SH_COLOR_SIZE==null) {
		    	    		    			al_SH_COLOR_SIZE=new ArrayList<SH_COLOR_SIZE>();
		    	    		    			cls_var.putLs_SH_COLOR_SIZE(getFA_NO()+PROCID+OD_FGDATE_WEEK+SH_NO+SH_COLOR, al_SH_COLOR_SIZE);
		    	    		    		}
		    	    		    		
		    	    	        		SH_COLOR_SIZE sh_color_size=new SH_COLOR_SIZE();
		    	    	        		sh_color_size.setFA_NO(getFA_NO());
		    	    	        		sh_color_size.setPROCID(PROCID);
		    	    	        		sh_color_size.setSH_COLOR(SH_COLOR);
		    	    	        		sh_color_size.setSH_NO(SH_NO);
		    	    	        		if(!share_SH_NO.equals(""))sh_color_size.setSH_NO(share_SH_NO);
		    	    	        		sh_color_size.setWORK_WEEK(WORK_WEEK_END);
		    	    	        		sh_color_size.setWORK_PLANNED_QTY(SH_COLOR_QTY);
		    	    	        		
		    	    	        		al_SH_COLOR_SIZE.add(sh_color_size);
	    	    	        		}

	    	    	        			    	    	        		
	    	    	        	}while(rs3.next());
	    	    	        }
	    	    	        rs3.close();
	    	    	        pstmtData3.close();
	    	    	        
	    	    	        
	    	    			strSQL="select share_SIZE,SH_SIZE,SIZE_CAP_QTY,sum(work_plan_Qty) WORK_PLAN_QTY "+
	                               "from fcmps021 "+
	                               "where PROCID='"+PROCID+"' " +
	                               "  and WORK_WEEK="+WORK_WEEK_END+" "+
	                               "  and SH_NO='"+SH_NO+"'"+
	                               (!share_SH_NO.equals("")?" and share_SH_NO='"+share_SH_NO+"' ":" ")+	                               
	                               "group by share_SIZE,SH_SIZE,SIZE_CAP_QTY";
	    	    		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
	    	    	        rs3=pstmtData3.executeQuery();
	    	    	 
	    	    	        if(rs3.next()){
	    	    	        	do {
	    	    	        		String SH_SIZE=rs3.getString("SH_SIZE");	    	    	        			    	    	        		
	    	    	        		
	    	    	        		String share_SIZE=rs3.getString("share_SIZE");
	    	    	        		double SH_SIZE_CAP_QTY=rs3.getDouble("SIZE_CAP_QTY");
	    	    	        		double SH_SIZE_QTY=rs3.getDouble("WORK_PLAN_QTY");
	    	    	        		
	    	    		    		SH_KEY_SIZE sh_size_cap=new SH_KEY_SIZE();
	    	    		    		sh_size_cap.setFA_NO(getFA_NO());
	    	    		    		sh_size_cap.setPROCID(PROCID);	    					    	    		    		
	    	    		    		sh_size_cap.setWORK_WEEK(WORK_WEEK_END);	
	    	    		    		if(!share_SH_NO.equals("")) {
		    	    	    			sh_size_cap.setSH_NO(share_SH_NO);
		    	    	    			sh_size_cap.setSH_SIZE(share_SIZE);
	    	    		    		}else {
		    	    	    			sh_size_cap.setSH_NO(SH_NO);
		    	    	    			sh_size_cap.setSH_SIZE(SH_SIZE);	    	    		    			
	    	    		    		}

	    	    	    			sh_size_cap.setWORK_CAP_QTY(SH_SIZE_CAP_QTY);
	    	    	    			
	    	    	    			Map<String,List<SH_KEY_SIZE>> ls_SH_SIZE_CAP=cls_var.getLs_SH_SIZE_CAP();
	    	    	    			
	    	    	    			synchronized(ls_SH_SIZE_CAP) {
		    	    	    			List<SH_KEY_SIZE> al_SH_SIZE_CAP=ls_SH_SIZE_CAP.get(getFA_NO()+PROCID+SH_NO+SH_SIZE);
		    	    	    			if(al_SH_SIZE_CAP==null) {
		    	    	    				al_SH_SIZE_CAP=new ArrayList<SH_KEY_SIZE>();
		    	    	    				al_SH_SIZE_CAP.add(sh_size_cap);
		    	    	    				cls_var.putLs_SH_SIZE_CAP(getFA_NO()+PROCID+SH_NO+SH_SIZE,al_SH_SIZE_CAP);
		    	    	    			}else {
		    	    	    				al_SH_SIZE_CAP.add(sh_size_cap);
		    	    	    			}
	    	    	    			}

	    	    	    			Map<String,SH_KEY_SIZE> ls_SH_KEY_SIZE=cls_var.getLs_SH_KEY_SIZE();
	    	    	    			
	    	    	    			synchronized(ls_SH_KEY_SIZE) {
		    	    	    			SH_KEY_SIZE sh_key_size=new SH_KEY_SIZE();
		    		    				sh_key_size.setFA_NO(getFA_NO());
		    		    				sh_key_size.setPROCID(PROCID);	    				
		    		    				
		    		    				sh_key_size.setWORK_WEEK(WORK_WEEK_END);
		    			    			
		    		    				if(!share_SH_NO.equals("")) {		            		
		    			    				sh_key_size.setSH_NO(share_SH_NO);
		    			    				sh_key_size.setSH_SIZE(share_SIZE);
		    			    				sh_key_size.setWORK_CAP_QTY(sh_size_cap.getWORK_CAP_QTY());
			    		    				sh_key_size.setWORK_PLANNED_QTY(SH_SIZE_QTY);
			    		    				cls_var.putLs_SH_KEY_SIZE(getFA_NO()+PROCID+share_SH_NO+share_SIZE+WORK_WEEK_END,sh_key_size);
		    		    				}else {
		    			    				sh_key_size.setSH_NO(SH_NO);
		    			    				sh_key_size.setSH_SIZE(SH_SIZE);
		    			    				sh_key_size.setWORK_CAP_QTY(sh_size_cap.getWORK_CAP_QTY());
			    		    				sh_key_size.setWORK_PLANNED_QTY(SH_SIZE_QTY);
			    		    				cls_var.putLs_SH_KEY_SIZE(getFA_NO()+PROCID+SH_NO+SH_SIZE+WORK_WEEK_END,sh_key_size);
		    		    				} 
	    	    	    			}
   	    			
	    	    	        	}while(rs3.next());
	    	    	        }
	    	    	        rs3.close();
	    	    	        pstmtData3.close();
	    	    	        
	    	        	}while(rs2.next());
	    	        }
	    	        rs2.close();
	    	        pstmtData2.close();
	    				    			
	        	}while(rs.next());
	        }
	        rs.close();
	        pstmtData.close();
	        
	        iRet=true;
	        
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return iRet;

    }
    
    
    /**
     * 計算訂單的排產周次
     * @param is_Allow_LessThan_516_Plan 是否允許小於516的數量排入
     * @param is_Compel  是否強制排入
     * @param WORK_DAYS  工作天數
     * @param WORK_WEEK_START 開始周次
     * @return
     */
	private boolean doGeneratePlan(int CURRENT_PLAN_WEEK,String...SPEC_SH_NO) {
		boolean iRet=false;
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;		
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;	
		
		Stack<Connection> conn_stack=new Stack<Connection>();
		
		try {	    
			
			strSQL="select distinct " +
			       "to_char(FCMPS010.OD_FGDATE,'IYIW') OD_FGDATE_WEEK "+
                   "from FCMPS010 "+
                   "where OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY>0 "+  
                   "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
                   "  and FCMPS010.IS_DISABLE='N' "+
                   (SPEC_SH_NO.length>0?" and FCMPS010.SH_NO IN ("+SPEC_SH_NO[0]+") ":"")+
//                   "  and STYLE_NO in ('16013','15991','14461') "+
//                   "  and SH_NO='CBDVADERLNDCLG'"+
//                   "  and to_char(FCMPS010.OD_FGDATE,'IYIW')<='1436'"+                    
                   "  and FCMPS010.OD_FGDATE is not null "+                   
                   "  and FCMPS010.FA_NO='"+getFA_NO()+"' "+
                   "order by to_char(FCMPS010.OD_FGDATE,'IYIW') DESC";
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.last()) {
		    	Min_Week=rs.getInt("OD_FGDATE_WEEK");
		    }
		    
		    rs.beforeFirst();
		    
		    if(rs.next()){
		    			    
		    	String OD_FGDATE_WEEK="";
		    	String PRE_OD_FGDATE_WEEK="";
		    	
		    	Max_Week=rs.getInt("OD_FGDATE_WEEK");
		    	
		    	for(int i=0;i<Parallel_Calcu_Colors;i++) {
		    		conn_stack.push(GenericSessionFactory());
		    	}
		    	
		    	do {
		    		
			    	pool=Executors.newFixedThreadPool(Parallel_Calcu_Colors);
			    	resultList = new ArrayList<Future<Map<String,Integer[]>>>();
			    	
		    		OD_FGDATE_WEEK=rs.getString("OD_FGDATE_WEEK");
			    	
		    		Cur_Week=rs.getInt("OD_FGDATE_WEEK");
		    		
		    		Date stDate=new Date();
   				    
		    		Map<String,Integer> ls_SH_BY_WEEKS=new LinkedHashMap<String,Integer>();
		    		
		    		//後一周已排的型體
		    		getSH_BY_WEEK(OD_FGDATE_WEEK,PRE_OD_FGDATE_WEEK,2,getConnection(),ls_SH_BY_WEEKS,SPEC_SH_NO);
		    		//優先排產的型體
		    		getSH_BY_WEEK(OD_FGDATE_WEEK,PRE_OD_FGDATE_WEEK,1,getConnection(),ls_SH_BY_WEEKS,SPEC_SH_NO);
		    		//本周需要排的型體
		    		getSH_BY_WEEK(OD_FGDATE_WEEK,PRE_OD_FGDATE_WEEK,0,getConnection(),ls_SH_BY_WEEKS,SPEC_SH_NO);
		    		
		    		int iRow=0;
		    		 
		    		Iterator<String> it_SH_NO= ls_SH_BY_WEEKS.keySet().iterator();
		    		while(it_SH_NO.hasNext()) {
		    			String SH_NO=it_SH_NO.next();

	        			Boolean NEED_SHOOT=null;
	        			Map<String,Boolean> ls_SH_NEED_SHOOT=cls_var.getLs_SH_NEED_SHOOT();
	        			synchronized(ls_SH_NEED_SHOOT) {
	        	    		NEED_SHOOT=ls_SH_NEED_SHOOT.get(SH_NO);

	        	    		if(NEED_SHOOT==null) {
	        	    			NEED_SHOOT=FCMPS_PUBLIC.getSH_Need_PROC(SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, getConnection());
	        	    			cls_var.putLs_SH_NEED_SHOOT(SH_NO, NEED_SHOOT);
	        	    		}
	        			}
	    	    		
	        			List<Double> ls_PROC_SEQ=null;
	    	    		Map<String,List<Double>> ls_SH_PROC_SEQ=cls_var.getLs_SH_PROC_SEQ();
	    	    		synchronized(ls_SH_PROC_SEQ) {
	    	    			ls_PROC_SEQ=ls_SH_PROC_SEQ.get(SH_NO);
	    					if(ls_PROC_SEQ==null) {
	    						ls_PROC_SEQ=getPROC_SEQ(SH_NO);
	    						ls_SH_PROC_SEQ.put(SH_NO, ls_PROC_SEQ);
	    					}
	    					
	    		    		if(ls_PROC_SEQ.isEmpty()) {
	    		    			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
	    		    			message.setSH_NO(SH_NO);
	    		    			message.setERROR("沒有建立型體:"+SH_NO+" 的制程順序!");
	    		    			setMessage(message);

	    		    			System.out.println("沒有建立型體:"+SH_NO+" 的制程順序!");
	    		    			continue;
	    		    		}
	    	    		}
			    		
		    			int ALLOW_PLAN_WEEKS=ls_SH_BY_WEEKS.get(SH_NO);
		    			
		    			ALLOW_PLAN_WEEKS=0; //預排先不提前
		    			
		    			int OD_FGDATE_WEEK_END=FCMPS_PUBLIC.getNext_Week(Integer.valueOf(OD_FGDATE_WEEK), ALLOW_PLAN_WEEKS);
		    			
/*					    strSQL="select STYLE_NO,SH_NO,SH_COLOR,max(OD_QTY) OD_QTY FROM ( "+
					    	   " select STYLE_NO,SH_NO,SH_COLOR,PROCID, sum(OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY) od_qty "+
	                           " from FCMPS010 "+
	                           " where to_char(FCMPS010.OD_FGDATE, 'IYIW') <= '"+OD_FGDATE_WEEK_END+"'"+   
	                           "   and to_char(FCMPS010.OD_FGDATE, 'IYIW') >= '"+OD_FGDATE_WEEK+"'"+
	                           "   and SH_NO='"+SH_NO+"'"+
	    	                   "   and OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY>0 "+                            
	    	                   "   and nvl(OD_CODE,'N')='N' "+
	    	                   "   and IS_DISABLE='N' "+
			                   "   and FA_NO='"+getFA_NO()+"' "+
	                           " group by STYLE_NO,SH_NO,SH_COLOR,PROCID)A "+
	                           "group by STYLE_NO,SH_NO,SH_COLOR "+
	                           "order by OD_QTY ";*/
					    
					    strSQL="select STYLE_NO,SH_COLOR,sum(OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY) od_qty2 "+
                               " from FCMPS010 "+
                               " where to_char(FCMPS010.OD_FGDATE, 'IYIW') <= '"+OD_FGDATE_WEEK_END+"'"+   
                               "   and to_char(FCMPS010.OD_FGDATE, 'IYIW') >= '"+OD_FGDATE_WEEK+"'"+
                               "   and SH_NO='"+SH_NO+"'"+
                               "   and PROCID='"+FCMPS_PUBLIC.PROCID_SHOOT+"'"+
 	                           "   and OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY>0 "+                            
 	                           "   and nvl(OD_CODE,'N')='N' "+
 	                           "   and IS_DISABLE='N' "+
		                       "   and FA_NO='"+getFA_NO()+"' "+
                               " group by STYLE_NO,SH_COLOR order by od_qty2 ";
					    
						pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
					    rs2=pstmtData2.executeQuery();
					    
					    rs2.setFetchDirection(ResultSet.FETCH_FORWARD);
					    rs2.setFetchSize(3000);

					    if(rs2.next()){	

					    	do {
					    		
					    		iRow++;			    		
					    		String STYLE_NO=rs2.getString("STYLE_NO");
					    		String SH_COLOR=rs2.getString("SH_COLOR");
					    		
					    		System.out.println(OD_FGDATE_WEEK+"周 型體:"+SH_NO+"("+STYLE_NO+") 配色:"+SH_COLOR);
					    		
					    		FCMPS_CLS_ForeGenerateRccpPlan_sub sub=new FCMPS_CLS_ForeGenerateRccpPlan_sub(
					    				OD_FGDATE_WEEK,
					    				OD_FGDATE_WEEK_END,
					    				CURRENT_PLAN_WEEK,
					    				STYLE_NO,
					    				SH_NO,
					    				SH_COLOR,
					    				ls_PROC_SEQ,
					    				NEED_SHOOT,
					    				conn_stack
					    				);
					    						    
					    		Future<Map<String,Integer[]>> result=pool.submit(sub);
					    		resultList.add(result);

					    	}while(rs2.next());
					    	
					    }
					    rs2.close();
					    pstmtData2.close();	    			    	

		    		}
		    		
		    		pool.shutdown();
		    		
				    PRE_OD_FGDATE_WEEK=OD_FGDATE_WEEK;
				    
				    int ERROR_CASE=0;
				    int FINISH_CASE=0;
				    int CANCEL_CASE=0;
				    
			        for (Future<Map<String,Integer[]>> fs : resultList) {  

			            try {  
			            	Map<String,Integer[]> result=fs.get();
			            	Iterator<String> it=result.keySet().iterator();
			            	while(it.hasNext()) {
			            		String key=it.next();
			            		Integer[] status=result.get(key);
			            		if(status[0]==FCMPS_CLS_ForeGenerateRccpPlan_sub.STATUS_CANCEL) {
			            			CANCEL_CASE++;
//			            			System.out.println(OD_FGDATE_WEEK+" 周,型體配色:"+key+" 預排已取消 "+new Date()); 
			            		}
			            		    
			            		if(status[0]==FCMPS_CLS_ForeGenerateRccpPlan_sub.STATUS_COMPLETE) {
			            			FINISH_CASE++;
//			            			System.out.println(OD_FGDATE_WEEK+" 周,型體配色:"+key+" 預排已完成,耗時:"+status[1]+" 毫秒 "+new Date());
			            		}
			            			
			            		if(status[0]==FCMPS_CLS_ForeGenerateRccpPlan_sub.STATUS_ERROR) {
			            			ERROR_CASE++;
//			            			System.out.println(OD_FGDATE_WEEK+" 周,型體配色:"+key+" 預排發生錯誤 "+new Date());
			            		}

			            	}
			                
			            } catch (InterruptedException e) {  
			                e.printStackTrace();  
			            } catch (ExecutionException e) {  
			            	pool.shutdownNow();
			                e.printStackTrace();  
			                break;  
			            }		    
			        }

				    resultList.clear();
				    resultList=null;
				    
		    		System.gc();
		    		
		    		Date edDate=new Date();
		    		
		    		FCMPS_CLS_ForeGenerateRccpPlan_MultiThread_Info info=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread_Info();
		    		info.setStDate(stDate);
		    		info.setEdDate(edDate);
		    		info.setOD_FGDATE_WEEK(OD_FGDATE_WEEK);
		    		info.setSH_NO_CASE(ls_SH_BY_WEEKS.size());
		    		info.setCOLOR_CASE(iRow);
		    		info.setERROR_CASE(ERROR_CASE);
		    		info.setFINISH_CASE(FINISH_CASE);
		    		info.setCANCEL_CASE(CANCEL_CASE);
		    		listThreads.add(info);
		    		
		    		System.out.println(edDate+" " +OD_FGDATE_WEEK+"/("+Min_Week+"-"+Max_Week+")周結束," +
		    				           " 型體數:"+ls_SH_BY_WEEKS.size()+
		    				           " 配色數:"+iRow+
		    				           " 完成數:"+FINISH_CASE+
		    				           " 取消數:"+CANCEL_CASE+
		    				           " 錯誤數:"+ERROR_CASE+
		    				           " 耗時:"+(edDate.getTime()-stDate.getTime())+" 毫秒");	
		    		
		    		if(isStopNow) break;
		    		
		    	}while(rs.next());

		    }
		    rs.close();
		    pstmtData.close();

		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
	    	for(int i=0;i<Parallel_Calcu_Colors;i++) {
	    		Connection connb=conn_stack.pop();
	    		this.closeConnection(connb);
	    	}
	    	conn_stack.clear();
	    	conn_stack=null;
		}
    	
		is_Finished=true;
		
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

			strSQL="select distinct PROC_SEQ from fcps22_1 " +
				   "where SH_ARITCLE='"+SH_NO+"' " +
				   "Order By PROC_SEQ DESC";
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
	 * 取周次有多少型體的訂單以及型體可排周數
	 * @param OD_FGDATE_WEEK
	 * @return
	 */
	private void getSH_BY_WEEK(
			String OD_FGDATE_WEEK,
			String PRE_OD_FGDATE_WEEK,
			int Style,
			Connection conn,
			Map<String,Integer> ls_SH_BY_WEEKS,
			String... SH_NO) {
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;		
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;		
		
		try{
			
		    //後一個出貨周次出現的型體/顏色優先排,但只有型體顏色大於516才排入 
		    strSQL="select distinct SH_NO "+
                   "  from FCMPS010 "+
                   " where to_char(FCMPS010.OD_FGDATE, 'IYIW') = '"+OD_FGDATE_WEEK+"'"+      
                   "   and OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY>0 "+                            
                   "   and nvl(OD_CODE,'N')='N' "+
                   "   and IS_DISABLE='N' "+
                   (SH_NO.length>0?" and SH_NO IN ("+SH_NO[0]+") ":"")+
//                   "  and STYLE_NO in ('16013','15991','14461') "+
//                   "   and SH_NO='CBDVADERLNDCLG'"+
                   "   and FA_NO='"+getFA_NO()+"' ";
			
		    //後一周已排的型體
		    if(Style==1) {
		    	strSQL="select * from ("+strSQL+") a where SH_NO IN " +
                              "       (SELECT SH_NO FROM FCMPS010 " +
                              "         WHERE SH_NO=a.SH_NO " +
                              "           and to_char(OD_FGDATE, 'IYIW') = '"+PRE_OD_FGDATE_WEEK+"'" +
                              "           and FA_NO='"+getFA_NO()+"') ";
		    }
		    
		    //優先排產的型體
		    if(Style==2) {
		    	strSQL=strSQL+" and SH_NO IN " +
	    	                  "       (select SH_NO from fcmps018 where FA_NO='"+getFA_NO()+"' "+
	                          "        UNION ALL "+
	                          "        select SH_NO from fcmps019 WHERE FA_NO='"+getFA_NO()+"' and WORK_WEEK="+OD_FGDATE_WEEK+") ";
		    }
		    		    		    
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		String SH_NO2=rs.getString("SH_NO");
		    		
		    		strSQL="select ALLOW_MOVE_UP_WEEK from fcmps022 where SH_NO='"+SH_NO2+"' and FA_NO='"+FA_NO+"'";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){
				    	ls_SH_BY_WEEKS.put(SH_NO2, rs2.getInt("ALLOW_MOVE_UP_WEEK"));
				    }else {
				    	ls_SH_BY_WEEKS.put(SH_NO2, 8);
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

	}

	public void doPrint() {	
		if(getMessage().isEmpty()) return;
		
		JExportToExcel JETE=new JExportToExcel();
		HSSFWorkbook wb=JETE.getWorkbook();
		HSSFSheet sheet=wb.createSheet();
				
		String items[]=new String[] {
        		"PO#",
        		"Style Code",
        		"Style",
        		"Color",
        		"Size",
        		"QTY",
        		"Week (Factory Promised Ship Date)",
        		"Message"};
		
		try {
			
			int iRow=0;
			
	    	HSSFRow row = sheet.createRow(iRow);
	    	HSSFCell cell = row.createCell((short)0);
	    	
			for(int iCol=0;iCol<items.length;iCol++) {
				row = sheet.createRow(iRow);
		    	cell = row.createCell((short)iCol);
		    	JETE.setCellValue(wb, cell, items[iCol]);  
			}
			
			do {
				CLS_RCCP_ERROR error=getMessage().get(iRow);
				
				row = sheet.createRow(iRow+1);
				
				short iCol=0;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getOD_PONO1());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getSTYLE_NO());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getSH_NO());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getSH_COLOR());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getSH_SIZE());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getOD_QTY());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getOD_FGDATE_WEEK());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getERROR());
	        	
	        	iRow++;			
	        	
	        	if(iRow==65535) {
	        		iRow=0;
	        		sheet=wb.createSheet();
	    			for(iCol=0;iCol<items.length;iCol++) {
	    				row = sheet.createRow(iRow);
	    		    	cell = row.createCell((short)iCol);
	    		    	JETE.setCellValue(wb, cell, items[iCol]);  
	    			}	        		
	        	}
	        	
			}while(iRow<getMessage().size());
									
			FileOutputStream fileOut=null;
			String fileID=UUID.generate();	
			fileOut = new FileOutputStream(getOutput()+"/"+fileID+".xls");
			
			wb.write(fileOut);
			fileOut.close();			
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
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
	
	public class FCMPS_CLS_ForeGenerateRccpPlan_sub implements Callable{
		private String OD_FGDATE_WEEK;
		private int OD_FGDATE_WEEK_END;
		private int CURRENT_PLAN_WEEK;
		private String STYLE_NO="";
		private String SH_NO="";
		private String SH_COLOR="";		
		
		private List<Double> ls_PROC_SEQ;
		private Boolean NEED_SHOOT;
	    private Connection conn=null;
	    private Stack<Connection> conn_stack=null;
	    
	    public static final int STATUS_CANCEL=0;
	    public static final int STATUS_RUNNING=1;
	    public static final int STATUS_ERROR=2;
	    public static final int STATUS_COMPLETE=3;
	    public static final int STATUS_WAIT=4;
	    
	    private int STATUS=STATUS_WAIT;

		public FCMPS_CLS_ForeGenerateRccpPlan_sub(
				String OD_FGDATE_WEEK,
				int OD_FGDATE_WEEK_END,
				int CURRENT_PLAN_WEEK,
				String STYLE_NO,
				String SH_NO,
				String SH_COLOR,
				List<Double> ls_PROC_SEQ,
				Boolean NEED_SHOOT,
				Stack<Connection> conn_stack
				) {
			this.OD_FGDATE_WEEK=OD_FGDATE_WEEK;
			this.OD_FGDATE_WEEK_END=OD_FGDATE_WEEK_END;
			this.CURRENT_PLAN_WEEK=CURRENT_PLAN_WEEK;
			this.STYLE_NO=STYLE_NO;
			this.SH_NO=SH_NO;
			this.SH_COLOR=SH_COLOR;
			this.ls_PROC_SEQ=ls_PROC_SEQ;
			this.NEED_SHOOT=NEED_SHOOT;
			this.conn_stack=conn_stack;
		}
						
		public int getSTATUS() {
			return STATUS;
		}

		public String getOD_FGDATE_WEEK() {
			return OD_FGDATE_WEEK;
		}
				
		private Connection getConnection() {
			return conn;
		}

		public Map<String,Integer[]> call() {
			STATUS=STATUS_RUNNING;
			int intTimes=0;
			try {				
				
				conn=conn_stack.pop();
				if(conn==null) {
					STATUS=STATUS_CANCEL;
					Map<String,Integer[]> result=new HashMap<String,Integer[]>();
					result.put(SH_NO+":"+SH_COLOR,new Integer[] {STATUS,intTimes});
					return result;
				}
				
				intTimes=doProcess();

				STATUS=STATUS_COMPLETE;
			}catch(Exception ex) {
				ex.printStackTrace();
				CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
				message.setOD_FGDATE_WEEK(Integer.valueOf(OD_FGDATE_WEEK));
				message.setSH_NO(SH_NO);
				message.setSH_COLOR(SH_COLOR);
				message.setERROR(ex.getMessage());
				setMessage(message);
				
				STATUS=STATUS_ERROR;
			}finally {
				conn_stack.push(conn);		
			}
			
			Map<String,Integer[]> result=new HashMap<String,Integer[]>();
			result.put(SH_NO+":"+SH_COLOR,new Integer[] {STATUS,intTimes});
			return result;
		};
		
		private int doProcess() throws Exception{
    		Date st_date=new Date();
    		
    		int WORK_WEEK_END=FCMPS_PUBLIC.getInt(OD_FGDATE_WEEK);	
    		
	    	boolean iRet=doGeneratePlan(
	    			OD_FGDATE_WEEK,
	    			OD_FGDATE_WEEK_END,
	    			WORK_WEEK_END,
	    			STYLE_NO,
	    			SH_NO,
	    			SH_COLOR,
	    			ls_PROC_SEQ,
	    			NEED_SHOOT);	

			Date ed_date=new Date();
			
//			System.out.println(OD_FGDATE_WEEK+"周 型體:"+SH_NO+"顏色:"+SH_COLOR+" flush time:"+(ed_date.getTime()-st_date.getTime()));
			return Integer.valueOf(String.valueOf(ed_date.getTime()-st_date.getTime()));
		}
		
		/**
		 * 計算訂單的排產周次
		 * @param OD_FGDATE_WEEK
		 * @param WORK_WEEK_START
		 * @param STYLE_NO
		 * @param SH_NO
		 * @param SH_COLOR
		 * @param ls_PROC_WORK_QTY
		 * @param ls_SH_WORK_QTY
		 * @param ls_SH_CAP_QTY
		 * @param ls_SH_KEY_SIZE
		 * @param ls_SH_SIZE_CAP
		 * @param ls_SH_COLOR_SIZE
		 * @param ls_PROC_SEQ
		 * @param ls_SH_NEED_PLAN_PROC
		 * @param ls_Share_Style
		 * @param NEED_SHOOT
		 * @param is_Allow_LessThan_516_Plan
		 * @param total_SH_COLOR_QTY
		 * @param ls_SH_SIZE_ALLOW_PLAN_QTY
		 * @return
		 */
		private boolean doGeneratePlan(		
				String OD_FGDATE_WEEK,
				int OD_FGDATE_WEEK_END,
				int WORK_WEEK_START,
				String STYLE_NO,
				String SH_NO,
				String SH_COLOR,			
				List<Double> ls_PROC_SEQ,
				boolean NEED_SHOOT
				) throws Exception {
			boolean iRet=false;
					
			Connection conn =getConnection();
			PreparedStatement pstmtData = null;		
			ResultSet rs=null;

    		for(int iWeek=0;iWeek<ls_PROC_SEQ.size();iWeek++) {
    			WORK_WEEK_START=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_START, 1);
    			
    			Map<String, List<String[]>> ls_SH_NEED_PLAN_PROC=cls_var.getLs_SH_NEED_PLAN_PROC();
    			
    			List<String[]> ls_PB_PTNO=null;
    			synchronized(ls_SH_NEED_PLAN_PROC) {
	    			ls_PB_PTNO=ls_SH_NEED_PLAN_PROC.get(SH_NO+ls_PROC_SEQ.get(iWeek));
	    			if(ls_PB_PTNO==null) {
	    				ls_PB_PTNO=getNeed_Plan_PROC(SH_NO, ls_PROC_SEQ.get(iWeek));
	    				ls_SH_NEED_PLAN_PROC.put(SH_NO+ls_PROC_SEQ.get(iWeek), ls_PB_PTNO);
	    			}
    			}
    				    			    	    			    		
    			for(String[] PB_PTNO:ls_PB_PTNO) {
    				if(!PB_PTNO[0].equals(FCMPS_PUBLIC.PROCID_SHOOT)) continue;
    				
    				String USE_CAP=PB_PTNO[2];
    				
    				int WORK_WEEK_END=WORK_WEEK_START; 
	    	
	    			double work_week_days=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(
	    					getFA_NO(),
	    					SH_NO, 
	    					WORK_WEEK_END, 
	    					getConnection());
	    			
	    			if(work_week_days==0) work_week_days=5;
	    			
    	    		PROC_WORK_QTY proc_Work_Qty=null;
    	    		SH_WORK_QTY sh_Work_Qty=null;
    	    		
    	    		sh_Work_Qty=doInitSH_CAP(SH_NO,PB_PTNO[0], WORK_WEEK_END, NEED_SHOOT, work_week_days);	    			
    	    		
	    			proc_Work_Qty=doInitPROC_CAP(
    						PB_PTNO[0], 
    						WORK_WEEK_END,
    						work_week_days);
    	    		
					//有些型體制程有設定要排計劃, 卻沒有設定產能,故退出
					if(proc_Work_Qty.getWORK_CAP_QTY()==0) {
						throw new Exception(" 制程:"+PB_PTNO[1]+proc_Work_Qty.getWORK_WEEK()+"周 沒有設定周產能!");
					}
					
/*					List<PROC_WORK_QTY> ls_PROC_WORK_QTY=cls_var.getLs_PROC_WORK_QTY();
    	    		synchronized(ls_PROC_WORK_QTY) {
	    				if(!ls_PROC_WORK_QTY.isEmpty()) {    					
	    					for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
	    						proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
	    						if(proc_Work_Qty.getFA_NO().equals(getFA_NO())&& 
	    						   proc_Work_Qty.getPROCID().equals(PB_PTNO[0])&&
	    						   proc_Work_Qty.getWORK_WEEK()==WORK_WEEK_END) {
	    							//有些型體制程有設定要排計劃, 卻沒有設定產能,故退出
	    							if(proc_Work_Qty.getWORK_CAP_QTY()==0) {
	    								throw new Exception(" 制程:"+PB_PTNO[1]+proc_Work_Qty.getWORK_WEEK()+"周 沒有設定周產能!");
	    							}
	    							break;
	    						}
	    					}    	    			
	    				}
	    				
    	    		}*/
    									
//    				sh_Work_Qty=getSH_WORK_QTY(SH_NO,PB_PTNO[0], WORK_WEEK_END);
    				if(sh_Work_Qty==null) {
    					throw new Exception("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
    				}
    				
				    String strSQL="select SH_SIZE,sum(OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY) WORK_PLAN_QTY "+
                                  " from FCMPS010 "+
                                  " where FCMPS010.SH_NO='"+SH_NO+"'"+
                                  "   and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+	
                                  "   and FCMPS010.PROCID='"+PB_PTNO[0]+"'"+
                                  "   and FA_NO='"+getFA_NO()+"' "+
	                              "   and OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY>0 "+                            
	                              "   and nvl(OD_CODE,'N')='N' "+
	                              "   and IS_DISABLE='N' "+					                   	                              
	                              "   and to_char(FCMPS010.OD_FGDATE, 'IYIW') >= '"+OD_FGDATE_WEEK+"'"+  
                                  "   and to_char(FCMPS010.OD_FGDATE, 'IYIW') <= '"+OD_FGDATE_WEEK_END+"' "+
                                  " group by SH_SIZE";
					pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
				    rs=pstmtData.executeQuery();
				    
				    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
				    rs.setFetchSize(3000);				    
				    
				    List<FCMPS010_BEAN> ls_FCMPS010=new ArrayList<FCMPS010_BEAN>();

				    double curOD_QTY=0;
				    if(rs.next()) {				    					    					    	
				    	do {
				    		FCMPS010_BEAN data=new FCMPS010_BEAN();
				    		data.setSH_SIZE(rs.getString("SH_SIZE"));
				    		data.setWORK_PLAN_QTY(rs.getDouble("WORK_PLAN_QTY"));				    		
				    		ls_FCMPS010.add(data);
				    		curOD_QTY=curOD_QTY+rs.getDouble("WORK_PLAN_QTY");

				    	}while(rs.next());		
				    }
				    rs.close();
				    pstmtData.close();				    

				    if(curOD_QTY==0) continue;

//				    System.out.println("型體:"+SH_NO+" 顏色:"+SH_COLOR+" 制程:"+PB_PTNO[1]+" 出貨周次:"+OD_FGDATE_WEEK+" 排產周次:"+WORK_WEEK_END+" 需要排產數:"+curOD_QTY);
				    
                    for(int iRow=0;iRow<ls_FCMPS010.size();iRow++) {
                    	FCMPS010_BEAN data=ls_FCMPS010.get(iRow);

    		    		String SH_SIZE="";
	    		
    		    		SH_SIZE=data.getSH_SIZE();   		    		
        				double WORK_PLAN_QTY=data.getWORK_PLAN_QTY();
        				
        				if(WORK_PLAN_QTY<=0) continue;
        				
        				if(NEED_SHOOT) {
        					if(!doInitSize(
        							STYLE_NO, 
        							SH_NO, 
        							SH_COLOR, 
        							SH_SIZE,
        							PB_PTNO[0], 
        							WORK_WEEK_END, 
        							work_week_days)) {
        						return iRet;
        					}
        				}
        				
        				double planed_qty=calc_Order_Week(
			    				STYLE_NO,
			    				SH_NO, 
			    				SH_COLOR, 
			    				SH_SIZE, 
			    				ls_PROC_SEQ.get(iWeek),
			    				WORK_PLAN_QTY, 
			    				PB_PTNO[0], 
			    				WORK_WEEK_END,
			    				work_week_days,
			    				OD_FGDATE_WEEK,
			    				OD_FGDATE_WEEK_END,
			    				proc_Work_Qty, 
			    				sh_Work_Qty,
			    				NEED_SHOOT,
			    				USE_CAP
			    				);
        				if(planed_qty==-1) {
//        	    			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
//        	    			message.setSH_COLOR(SH_COLOR);
//        	    			message.setSH_NO(SH_NO);
//        	    			message.setSH_SIZE(SH_SIZE);
//        	    			message.setSTYLE_NO(STYLE_NO);
//        	    			message.setOD_FGDATE_WEEK(Integer.valueOf(OD_FGDATE_WEEK));
//        	    			message.setERROR("計算制程:"+PB_PTNO[1]+" 的排產周次時發生錯誤!");
//        	    			setMessage(message);
        	    			throw new Exception("周次"+OD_FGDATE_WEEK+" 型體:"+SH_NO+" 配色:"+SH_COLOR+" 計算制程:"+PB_PTNO[1]+" 的排產周次時發生錯誤!");        				
        				}
        				
        				data.setWORK_PLAN_QTY(data.getWORK_PLAN_QTY()-planed_qty);
        				
        	    		//如果已達到本周制程的最大產量, 則往前排一周
        	    		if(proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY()<=0) {
        	    			WORK_WEEK_END=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_END, 1);
        	    			work_week_days=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(
        	    					getFA_NO(),
        	    					SH_NO, 
        	    					WORK_WEEK_END, 
        	    					getConnection());
        	    			
        	    			if(work_week_days==0) work_week_days=5;
        	    			
        	    			sh_Work_Qty=doInitSH_CAP(SH_NO,PB_PTNO[0], WORK_WEEK_END, NEED_SHOOT, work_week_days);
        	    			
        	    			proc_Work_Qty=doInitPROC_CAP(
            						PB_PTNO[0], 
            						WORK_WEEK_END,
            						work_week_days);

            				if(sh_Work_Qty==null) {
            					throw new Exception("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
            				}
            				
            				iRow=0;
            				continue;
        	    		}
        	    		
        	    		//如果已達到型體的制程的最大產量, 則往前排一周
        	    		if(sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY()<=0) {
        	    			WORK_WEEK_END=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_END, 1);
        	    			
        	    			work_week_days=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(
        	    					getFA_NO(),
        	    					SH_NO, 
        	    					WORK_WEEK_END, 
        	    					getConnection());
        	    			
        	    			if(work_week_days==0) work_week_days=5;
        	    			
        	    			sh_Work_Qty=doInitSH_CAP(SH_NO,PB_PTNO[0], WORK_WEEK_END, NEED_SHOOT, work_week_days);
        	    			
        	    			proc_Work_Qty=doInitPROC_CAP(
            						PB_PTNO[0], 
            						WORK_WEEK_END,
            						work_week_days);

            				if(sh_Work_Qty==null) {
            					throw new Exception("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
            				}
            				
            				iRow=0;
            				continue;
        	    		}	        				
        				
			       }				    	
                   
                   curOD_QTY=0;

                    for(int iRow=0;iRow<ls_FCMPS010.size();iRow++) {
                    	FCMPS010_BEAN data=ls_FCMPS010.get(iRow);
                    	curOD_QTY=curOD_QTY+data.getWORK_PLAN_QTY();
                    }
                    
                    if(curOD_QTY>0) {
    	    			WORK_WEEK_END=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_END, 1);
    	    			
    	    			work_week_days=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(
    	    					getFA_NO(),
    	    					SH_NO, 
    	    					WORK_WEEK_END, 
    	    					getConnection());
    	    			
    	    			if(work_week_days==0) work_week_days=5;
    	    			
    	    			sh_Work_Qty=doInitSH_CAP(SH_NO,PB_PTNO[0], WORK_WEEK_END, NEED_SHOOT, work_week_days);
    	    			
    	    			proc_Work_Qty=doInitPROC_CAP(
        						PB_PTNO[0], 
        						WORK_WEEK_END,
        						work_week_days);        				        	    		
        				
        				sh_Work_Qty=getSH_WORK_QTY(SH_NO,PB_PTNO[0], WORK_WEEK_END);
        				if(sh_Work_Qty==null) {
        					throw new Exception("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
        				}
        				
 				    	iRet=doGeneratePlan(
				    			OD_FGDATE_WEEK,
				    			OD_FGDATE_WEEK_END,
				    			WORK_WEEK_END,
				    			STYLE_NO,
				    			SH_NO,
				    			SH_COLOR,
				    			PB_PTNO[0], 
				    			PB_PTNO[1], 
				    			ls_PROC_SEQ.get(iWeek),
				    			NEED_SHOOT,
				    			USE_CAP,
				    			work_week_days);	
        				
    	    		}           
                    
                    ls_FCMPS010.clear();
    			}	
    				    			
    		}
    		
		    iRet=true;

			return iRet;
		}
		
		/**
		 * 計算訂單的排產周次
		 * @param OD_FGDATE_WEEK
		 * @param WORK_WEEK_START
		 * @param STYLE_NO
		 * @param SH_NO
		 * @param SH_COLOR
		 * @param PROCID
		 * @param PB_PTNA
		 * @param ls_PROC_WORK_QTY
		 * @param ls_SH_WORK_QTY
		 * @param ls_SH_CAP_QTY
		 * @param ls_SH_KEY_SIZE
		 * @param ls_SH_SIZE_CAP
		 * @param ls_SH_COLOR_SIZE
		 * @param ls_PROC_SEQ
		 * @param ls_SH_NEED_PLAN_PROC
		 * @param ls_Share_Style
		 * @param NEED_SHOOT
		 * @param is_Allow_LessThan_516_Plan
		 * @param total_SH_COLOR_QTY
		 * @param ls_SH_SIZE_ALLOW_PLAN_QTY
		 * @return
		 */
		private boolean doGeneratePlan(		
				String OD_FGDATE_WEEK,
				int OD_FGDATE_WEEK_END,
				int WORK_WEEK_END,
				String STYLE_NO,
				String SH_NO,
				String SH_COLOR,
				String PROCID,
				String PB_PTNA,
				double PROC_SEQ,
				boolean NEED_SHOOT,
				String USE_CAP,
				double work_week_days
				) throws Exception {
			boolean iRet=false;

			Connection conn =getConnection();
			PreparedStatement pstmtData = null;		
			ResultSet rs=null;

//			double work_week_days=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(
//					getFA_NO(),
//					SH_NO, 
//					WORK_WEEK_END, 
//					getConnection());
			
			PROC_WORK_QTY proc_Work_Qty=null;
			SH_WORK_QTY sh_Work_Qty=null;
			
			sh_Work_Qty=doInitSH_CAP(SH_NO,PROCID, WORK_WEEK_END, NEED_SHOOT, work_week_days);
			
			proc_Work_Qty=doInitPROC_CAP(
					PROCID, 
					WORK_WEEK_END,
					work_week_days);

			if(sh_Work_Qty==null) {
				throw new Exception("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
			}
			
			String strSQL="select SH_SIZE,sum(OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY) WORK_PLAN_QTY "+
	           "from FCMPS010 "+
	           "where FCMPS010.SH_NO='"+SH_NO+"'"+
	           "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
	           "  and FCMPS010.PROCID='"+PROCID+"'"+	
	           "  and FCMPS010.FA_NO='"+getFA_NO()+"' "+
	           "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+ 				                   
	           "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
	           "  and FCMPS010.IS_DISABLE='N' "+	           
	           "  and to_char(FCMPS010.OD_FGDATE,'IYIW')>='"+OD_FGDATE_WEEK+"'"+
		       "  and to_char(FCMPS010.OD_FGDATE,'IYIW')<='"+OD_FGDATE_WEEK_END+"' "+
	           "group by FCMPS010.SH_SIZE ";
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		    rs.setFetchSize(3000);				    
		    
		    List<FCMPS010_BEAN> ls_FCMPS010=new ArrayList<FCMPS010_BEAN>();

		    double curOD_QTY=0;
		    if(rs.next()) {				    					    					    	
		    	do {
		    		FCMPS010_BEAN data=new FCMPS010_BEAN();
		    		data.setSH_SIZE(rs.getString("SH_SIZE"));
		    		data.setWORK_PLAN_QTY(rs.getDouble("WORK_PLAN_QTY"));				    		
		    		ls_FCMPS010.add(data);
		    		curOD_QTY=curOD_QTY+rs.getDouble("WORK_PLAN_QTY");
		    	}while(rs.next());		
		    }
		    rs.close();
		    pstmtData.close();				    
		    
	        for(int iRow=0;iRow<ls_FCMPS010.size();iRow++) {
	        	FCMPS010_BEAN data=ls_FCMPS010.get(iRow);

	    		String SH_SIZE="";
	    		   		
	    		SH_SIZE=data.getSH_SIZE();		    		
				double WORK_PLAN_QTY=data.getWORK_PLAN_QTY();
				
				if(WORK_PLAN_QTY<=0) continue;
				
				if(NEED_SHOOT) {
					if(!doInitSize(
							STYLE_NO, 
							SH_NO, 
							SH_COLOR, 
							SH_SIZE,
							PROCID, 
							WORK_WEEK_END, 
							work_week_days)) {
						return iRet;
					}
				}
				
				double planed_qty=calc_Order_Week(
	    				STYLE_NO,
	    				SH_NO, 
	    				SH_COLOR, 
	    				SH_SIZE, 
	    				PROC_SEQ,
	    				WORK_PLAN_QTY, 
	    				PROCID, 
	    				WORK_WEEK_END,
	    				work_week_days,
	    				OD_FGDATE_WEEK,
	    				OD_FGDATE_WEEK_END,
	    				proc_Work_Qty, 
	    				sh_Work_Qty,
	    				NEED_SHOOT,
	    				USE_CAP
	    				);
				
				if(planed_qty==-1) {
//	    			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
//	    			message.setSH_COLOR(SH_COLOR);
//	    			message.setSH_NO(SH_NO);
//	    			message.setSH_SIZE(SH_SIZE);
//	    			message.setSTYLE_NO(STYLE_NO);
//	    			message.setOD_FGDATE_WEEK(Integer.valueOf(OD_FGDATE_WEEK));
//	    			message.setERROR("計算制程:"+PB_PTNA+" 的排產周次時發生錯誤!");
//	    			setMessage(message);
	    			throw new Exception("周次"+OD_FGDATE_WEEK+" 型體:"+SH_NO+" 配色:"+SH_COLOR+" 計算制程:"+PB_PTNA+" 的排產周次時發生錯誤!");				
				}
												
				data.setWORK_PLAN_QTY(data.getWORK_PLAN_QTY()-planed_qty);
		
	    		//如果已達到本周制程的最大產量, 則往前排一周
	    		if(proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY()<=0) {
	    			WORK_WEEK_END=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_END, 1);
	    			
	    			work_week_days=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(
	    					getFA_NO(),
	    					SH_NO, 
	    					WORK_WEEK_END, 
	    					getConnection());
	    			
	    			if(work_week_days==0) work_week_days=5;
	    			
	    			sh_Work_Qty=doInitSH_CAP(SH_NO,PROCID, WORK_WEEK_END, NEED_SHOOT, work_week_days);
	    			
	    			proc_Work_Qty=doInitPROC_CAP(
							PROCID, 
							WORK_WEEK_END,
							work_week_days);

    				if(sh_Work_Qty==null) {
    					throw new Exception("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
    				}
					
					iRow=0;
					continue;
	    		}
	    		
	    		//如果已達到型體的制程的最大產量, 則往前排一周
	    		if(sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY()<=0) {
	    			WORK_WEEK_END=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_END, 1);
	    			
	    			work_week_days=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(
	    					getFA_NO(),
	    					SH_NO, 
	    					WORK_WEEK_END, 
	    					getConnection());
	    			
	    			if(work_week_days==0) work_week_days=5;
	    			
	    			sh_Work_Qty=doInitSH_CAP(SH_NO,PROCID, WORK_WEEK_END, NEED_SHOOT, work_week_days);
	    			
	    			proc_Work_Qty=doInitPROC_CAP(
							PROCID, 
							WORK_WEEK_END,
							work_week_days);

    				if(sh_Work_Qty==null) {
    					throw new Exception("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
    				}
					
					iRow=0;
					continue;
	    		}	        				
				
	       }				    	
	        
	       iRet=true;
	       
	       curOD_QTY=0;

	        for(int iRow=0;iRow<ls_FCMPS010.size();iRow++) {
	        	FCMPS010_BEAN data=ls_FCMPS010.get(iRow);
	        	curOD_QTY=curOD_QTY+data.getWORK_PLAN_QTY();
	        }
	        
	        if(curOD_QTY>0) {
    			WORK_WEEK_END=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_END, 1);
    			
    			work_week_days=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(
    					getFA_NO(),
    					SH_NO, 
    					WORK_WEEK_END, 
    					getConnection());
    			
    			if(work_week_days==0) work_week_days=5;
    			
    			sh_Work_Qty=doInitSH_CAP(SH_NO,PROCID, WORK_WEEK_END, NEED_SHOOT, work_week_days);
    			
    			proc_Work_Qty=doInitPROC_CAP(
						PROCID, 
						WORK_WEEK_END,
						work_week_days);

				if(sh_Work_Qty==null) {
					throw new Exception("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
				}
				
		    	iRet=doGeneratePlan(
		    			OD_FGDATE_WEEK,
		    			OD_FGDATE_WEEK_END,
		    			WORK_WEEK_END,
		    			STYLE_NO,
		    			SH_NO,
		    			SH_COLOR,
		    			PROCID,
		    			PB_PTNA,
		    			PROC_SEQ,
		    			NEED_SHOOT,
		    			USE_CAP,
		    			work_week_days);	
		    	
    		}
	        	        
	        ls_FCMPS010.clear();

			return iRet;
		}
		
		/**
		 * 計算制程和型體的產能
		 * @param STYLE_NO
		 * @param SH_NO
		 * @param SH_COLOR
		 * @param PROCID
		 * @param PB_PTNA
		 * @param WORK_WEEK_END
		 * @param NEED_SHOOT
		 * @param ls_PROC_WORK_QTY
		 * @param ls_SH_WORK_QTY
		 * @param ls_SH_CAP_QTY
		 * @return
		 */
		private PROC_WORK_QTY doInitPROC_CAP(
				String PROCID,
				int WORK_WEEK_END,
				double Week_Plan_Days) {
			 
		        boolean isExist=false;
		        
				PROC_WORK_QTY proc_Work_Qty=null;

				List<PROC_WORK_QTY> ls_PROC_WORK_QTY=cls_var.getLs_PROC_WORK_QTY();
				
				synchronized(ls_PROC_WORK_QTY) {
					//取制程的最大產能
					if(!ls_PROC_WORK_QTY.isEmpty()) {
						
						for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
							proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
							if(proc_Work_Qty.getFA_NO().equals(getFA_NO())&& 
							   proc_Work_Qty.getPROCID().equals(PROCID)&&
							   proc_Work_Qty.getWORK_WEEK()==WORK_WEEK_END) {
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
						proc_Work_Qty.setWORK_WEEK(WORK_WEEK_END);
						double[] PROC_CAP_QTY=FCMPS_PUBLIC.get_PROC_Plan_QTY(getFA_NO(), WORK_WEEK_END,PROCID,getConnection());
						proc_Work_Qty.setWORK_CAP_QTY(PROC_CAP_QTY[0]/5*Week_Plan_Days);
						proc_Work_Qty.setWORK_MAX_CAP_QTY(PROC_CAP_QTY[1]/5*Week_Plan_Days);
						if(proc_Work_Qty.getWORK_MAX_CAP_QTY()==0) proc_Work_Qty.setWORK_MAX_CAP_QTY(proc_Work_Qty.getWORK_CAP_QTY());
						proc_Work_Qty.setWORK_PLANNED_QTY(0);
						
						cls_var.addLs_PROC_WORK_QTY(proc_Work_Qty);
					}
  					
				}

			 return proc_Work_Qty;
		}
		
		/**
		 * 初始各周型體產能
		 * @param SH_NO
		 * @param WORK_WEEK_END
		 * @param NEED_SHOOT
		 * @param ls_SH_CAP_QTY
		 * @return
		 */
		private SH_WORK_QTY doInitSH_CAP(
				String SH_NO,
				String PROCID,
				int WORK_WEEK_END,
				boolean NEED_SHOOT,
				double week_plan_days) {

				SH_WORK_QTY sh_cap_Qty=null;  	    		
				
				try {
					Map<String,List<SH_WORK_QTY>> ls_SH_CAP_QTY=cls_var.getLs_SH_CAP_QTY();
					
					synchronized(ls_SH_CAP_QTY) {
						List<SH_WORK_QTY> al_SH_CAP_QTY=ls_SH_CAP_QTY.get(getFA_NO()+SH_NO);	 
						if(al_SH_CAP_QTY==null) {
							al_SH_CAP_QTY=new ArrayList<SH_WORK_QTY>();
							cls_var.putLs_SH_CAP_QTY(getFA_NO()+SH_NO, al_SH_CAP_QTY);
							
							sh_cap_Qty=new SH_WORK_QTY();
							sh_cap_Qty.setFA_NO(getFA_NO());
							sh_cap_Qty.setSH_NO(SH_NO);
							sh_cap_Qty.setPROCID(PROCID);
							sh_cap_Qty.setWORK_WEEK(WORK_WEEK_END);
							
							//以射出,組底和針車產能小的為限制,這是為了各製程的平準生產
							sh_cap_Qty.setWORK_CAP_QTY(FCMPS_PUBLIC.get_SH_Plan_QTY(
									getFA_NO(),
									SH_NO,
									NEED_SHOOT,
									WORK_WEEK_END,
									getConnection(),
									week_plan_days));
							
							al_SH_CAP_QTY.add(sh_cap_Qty);  						
						}else {
							boolean isExist=false;
			    			for(int i=0;i<al_SH_CAP_QTY.size();i++) {
			    				sh_cap_Qty=al_SH_CAP_QTY.get(i);
			    				if(sh_cap_Qty.getFA_NO().equals(getFA_NO()) &&
			    				   sh_cap_Qty.getPROCID().equals(PROCID) &&
			    				   sh_cap_Qty.getSH_NO().equals(SH_NO) &&
			    				   sh_cap_Qty.getWORK_WEEK()==WORK_WEEK_END) {
			    					isExist=true;
			    					break;
			    				}
			    			}
			    			   			
			    			if(!isExist) {
								sh_cap_Qty=new SH_WORK_QTY();
								sh_cap_Qty.setFA_NO(getFA_NO());
								sh_cap_Qty.setSH_NO(SH_NO);
								sh_cap_Qty.setPROCID(PROCID);
								sh_cap_Qty.setWORK_WEEK(WORK_WEEK_END);
								
								//以射出,組底和針車產能小的為限制,這是為了各製程的平準生產
								sh_cap_Qty.setWORK_CAP_QTY(FCMPS_PUBLIC.get_SH_Plan_QTY(
										getFA_NO(),
										SH_NO,
										NEED_SHOOT,
										WORK_WEEK_END,
										getConnection(),
										week_plan_days));
								
								al_SH_CAP_QTY.add(sh_cap_Qty); 
			    			}					
						}
					}
				
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			 			 
			 return sh_cap_Qty;
		}
		
		/**
		 * 計算型體需要排產的size的產能
		 * @param OD_FGDATE_WEEK
		 * @param STYLE_NO
		 * @param SH_NO
		 * @param SH_COLOR
		 * @param PROCID
		 * @param PB_PTNA
		 * @param WORK_WEEK_END
		 * @param NEED_SHOOT
		 * @param ls_PROC_WORK_QTY
		 * @param ls_SH_WORK_QTY
		 * @param ls_SH_CAP_QTY
		 * @param ls_SH_KEY_SIZE
		 * @param ls_SH_SIZE_CAP
		 * @param ls_Share_Style
		 * @param ls_SH_SIZE_ALLOW_PLAN_QTY
		 * @return
		 */
		private boolean doInitSize(
				String STYLE_NO,
				String SH_NO,
				String SH_COLOR,
				String SH_SIZE,
				String PROCID,
				int WORK_WEEK_END,
				double Week_Plan_Days) throws Exception {

	        boolean iRet=false;
	        
	        boolean isExist=false;
	        		
			List<String[]> ls_STYLE=null;
			
			String share_SH_NO="";
			String share_SH_SIZE="";
			
			SH_KEY_SIZE sh_key_size=null;
			SH_KEY_SIZE sh_size_cap=null;
			
			
			Map<String,List<String[]>> ls_Share_Style=cls_var.getLs_Share_Style();
			
			synchronized(ls_Share_Style) {
				Object SHARE_SIZE[]=ls_Share_Style.keySet().toArray();
				
				for(int i=SHARE_SIZE.length-1;i>=0;i--) {
					String strSHARE_SIZE=FCMPS_PUBLIC.getValue(SHARE_SIZE[i]);
					if(strSHARE_SIZE.startsWith(getFA_NO()+STYLE_NO+SH_NO+SH_SIZE)) {
						if(Integer.valueOf(strSHARE_SIZE.substring(String.valueOf(getFA_NO()+STYLE_NO+SH_NO+SH_SIZE).length()))<=WORK_WEEK_END) {
							ls_STYLE=ls_Share_Style.get(strSHARE_SIZE);
							break;
						}
					}
				}
				
				if(ls_STYLE==null) {
					ls_STYLE=getMD_Style_Share(getFA_NO(),STYLE_NO, SH_NO, SH_SIZE, WORK_WEEK_END);    					    					
					SHARE_SIZE=ls_Share_Style.keySet().toArray(new String[0]);
//					Arrays.sort(SHARE_SIZE);       				
				}
				
				if(ls_STYLE==null) {				
					System.out.println("型體:"+SH_NO+" SIZE:"+SH_SIZE+" 排產周次:"+WORK_WEEK_END+" 沒有模具資料!");
					throw new Exception("型體:"+SH_NO+" SIZE:"+SH_SIZE+" 排產周次:"+WORK_WEEK_END+" 沒有模具資料!");
				}
												
			}

			isExist=false;
			
			List<SH_KEY_SIZE> al_SH_SIZE_CAP=null;
			Map<String,List<SH_KEY_SIZE>> ls_SH_SIZE_CAP=cls_var.getLs_SH_SIZE_CAP();
			synchronized(ls_SH_SIZE_CAP) {
				al_SH_SIZE_CAP=ls_SH_SIZE_CAP.get(getFA_NO()+PROCID+SH_NO+SH_SIZE);
				if(al_SH_SIZE_CAP==null) {
					al_SH_SIZE_CAP=new ArrayList<SH_KEY_SIZE>();
					ls_SH_SIZE_CAP.put(getFA_NO()+PROCID+SH_NO+SH_SIZE, al_SH_SIZE_CAP);
				}
			}
	       	
			synchronized(al_SH_SIZE_CAP) {
				//首先查找是否已計算此型體size的產能
		        if(!ls_STYLE.isEmpty()) {
		        	for(int n=0;n<ls_STYLE.size();n++) {
		        		String part[]=ls_STYLE.get(n);
		        		share_SH_NO=part[0];
		        		share_SH_SIZE=part[1];
		        		
						//找到目前size的模具瓶頸產能
								            		            		
		        		for(int i=0;i<al_SH_SIZE_CAP.size();i++) {
		        			sh_size_cap=al_SH_SIZE_CAP.get(i);
							if(sh_size_cap.getFA_NO().equals(getFA_NO())&& 
		 					   sh_size_cap.getPROCID().equals(PROCID)&&
		 					   sh_size_cap.getSH_NO().equals(share_SH_NO)&&
		 					   sh_size_cap.getSH_SIZE().equals(share_SH_SIZE)&&
		 					   sh_size_cap.getWORK_WEEK()<=WORK_WEEK_END) {								
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
					//找到目前size的模具瓶頸產能		            	
		        	for(int i=0;i<al_SH_SIZE_CAP.size();i++) {
		        		sh_size_cap=al_SH_SIZE_CAP.get(i);
						if(sh_size_cap.getFA_NO().equals(getFA_NO())&& 
						   sh_size_cap.getPROCID().equals(PROCID)&&
			    		   sh_size_cap.getSH_NO().equals(SH_NO)&&
			    		   sh_size_cap.getSH_SIZE().equals(SH_SIZE) &&
			    		   sh_size_cap.getWORK_WEEK()<=WORK_WEEK_END) {
			    			isExist=true;
			    			break;
			    		}		            		
		        	}
		        }
		        
		        //沒有計算型體size的周最大產量. 增加進來
		        if(!isExist) {
		        	if(!ls_STYLE.isEmpty()) {
		        		share_SH_NO=ls_STYLE.get(0)[0];
		        		share_SH_SIZE=ls_STYLE.get(0)[1];
		        		sh_size_cap=getMD_Min_Week_Cap_QTY(
		        				getFA_NO(), 
		        				share_SH_NO, 
		        				share_SH_SIZE, 
		        				PROCID, 
		        				al_SH_SIZE_CAP,
		        				WORK_WEEK_END,
		        				Week_Plan_Days);
		            	
		        	}else {
		        		sh_size_cap=getMD_Min_Week_Cap_QTY(
		        				getFA_NO(), 
		        				SH_NO, 
		        				SH_SIZE, 
		        				PROCID, 
		        				al_SH_SIZE_CAP,
		        				WORK_WEEK_END,
		        				Week_Plan_Days);
		        	}
		        }
				
		        if(sh_size_cap==null) {
		        	throw new Exception("型體:"+SH_NO+" SIZE:"+SH_SIZE+" 沒有建立模具資料!");
		        }
		        
				if(sh_size_cap.getWORK_CAP_QTY()==0) {
					if(!ls_STYLE.isEmpty()) {
						System.out.println("共模型體:"+ls_STYLE.get(0)[0]+" size:"+ls_STYLE.get(0)[1]+" 工作天數:"+Week_Plan_Days);
					}
					
					throw new Exception("型體:"+SH_NO+" SIZE:"+SH_SIZE+" 沒有"+sh_size_cap.getWORK_WEEK()+"周模具資料!");		    			
				}
			}
			
			Map<String,SH_KEY_SIZE> ls_SH_KEY_SIZE=cls_var.getLs_SH_KEY_SIZE();
			synchronized(ls_SH_KEY_SIZE) {
				//再查找是否已記錄此型體本周的產能和排產數, 沒有則增加進來
		        if(!ls_STYLE.isEmpty()) {
		        	sh_key_size=ls_SH_KEY_SIZE.get(getFA_NO()+PROCID+share_SH_NO+share_SH_SIZE+WORK_WEEK_END);
		        }else {
		        	sh_key_size=ls_SH_KEY_SIZE.get(getFA_NO()+PROCID+SH_NO+SH_SIZE+WORK_WEEK_END);		            	
		        }
		        
				//型體size的本周產能和排產數沒記錄. 增加進來
				if(sh_key_size==null) {
					sh_key_size=new SH_KEY_SIZE();
					sh_key_size.setFA_NO(getFA_NO());
					sh_key_size.setPROCID(PROCID);	    					    				
					sh_key_size.setWORK_WEEK(WORK_WEEK_END);		    			
					sh_key_size.setWORK_CAP_QTY(sh_size_cap.getWORK_CAP_QTY());
					sh_key_size.setWORK_PLANNED_QTY(0);
					
					if(!ls_STYLE.isEmpty()) {	
		        		share_SH_NO=ls_STYLE.get(0)[0];
		        		share_SH_SIZE=ls_STYLE.get(0)[1];
						sh_key_size.setSH_NO(share_SH_NO);
						sh_key_size.setSH_SIZE(share_SH_SIZE);
						ls_SH_KEY_SIZE.put(getFA_NO()+PROCID+share_SH_NO+share_SH_SIZE+WORK_WEEK_END,sh_key_size);
					}else {
						sh_key_size.setSH_NO(SH_NO);
						sh_key_size.setSH_SIZE(SH_SIZE);
						ls_SH_KEY_SIZE.put(getFA_NO()+PROCID+SH_NO+SH_SIZE+WORK_WEEK_END,sh_key_size);
					}    					    			
				}	 
			}
   	            		
	        iRet=true;
	        
	        return iRet;
				
		}
		
		/**
		 * 計算訂單的排產周次<br>
		 * @param OD_PONO1      PO#
		 * @param STYLE_NO      型體代號
		 * @param SH_NO         型體
		 * @param SH_COLOR      顏色
		 * @param SH_SIZE       size 
		 * @param OD_QTY        訂單數
		 * @param PROC_SEQ      制程序號
		 * @param WORK_PLAN_QTY 待排產數量
		 * @param PROCID        制程
		 * @param WORK_WEEK_END 開始周次
		 * @param OD_FGDATE_WEEK 工廠交期周次
		 * @param proc_Work_Qty 制程排產數
		 * @param sh_Work_Qty 型體排產數
		 * @param ls_SH_KEY_SIZE size排產數
		 * @param ls_SH_COLOR_SIZE 配色排產數
		 * @param ls_Share_Style 共模型體,SIZE,部位
		 * @param NEED_SHOOT 需要射出否
		 * @param is_Allow_LessThan_516_Plan 允許排入小於516的型體配色否
		 * @param total_SH_COLOR_QTY 型體配色總數
		 * @param ls_SH_SIZE_ALLOW_PLAN_QTY size允許排產數
		 * @return int 最晚完工周次
		 */
		private double calc_Order_Week(
				String STYLE_NO,
				String SH_NO,
				String SH_COLOR,
				String SH_SIZE,
				double PROC_SEQ,
				double WORK_PLAN_QTY,
				String PROCID,
				int WORK_WEEK_END,
				double Week_Plan_Days,
				String OD_FGDATE_WEEK,
				int OD_FGDATE_WEEK_END,
				PROC_WORK_QTY proc_Work_Qty,
				SH_WORK_QTY sh_Work_Qty,				
				boolean NEED_SHOOT,
				String USE_CAP
				) throws Exception {
			
			double iRet=-1;						

			if(WORK_PLAN_QTY==0) return WORK_PLAN_QTY;
													
			String share_SH_NO="";
			String share_SH_SIZE="";
			
			Connection conn =getConnection();
			PreparedStatement pstmtData = null;		
			ResultSet rs=null;
			
			PreparedStatement pstmtData2 = null;
			
			//開始計算

    		SH_KEY_SIZE sh_key_size=null;
    		    		
    		List<String[]> ls_STYLE=null;
    		
    		Map<String,List<String[]>> ls_Share_Style=cls_var.getLs_Share_Style();
    		
    		synchronized(ls_Share_Style) {
	    		Object SHARE_SIZE[]=ls_Share_Style.keySet().toArray();
	    		
				if(NEED_SHOOT) {    				
					for(int i=SHARE_SIZE.length-1;i>=0;i--) {
						String strSHARE_SIZE=FCMPS_PUBLIC.getValue(SHARE_SIZE[i]);
						if(strSHARE_SIZE.startsWith(getFA_NO()+STYLE_NO+SH_NO+SH_SIZE)) {
							if(Integer.valueOf(strSHARE_SIZE.substring(String.valueOf(getFA_NO()+STYLE_NO+SH_NO+SH_SIZE).length()))<=WORK_WEEK_END) {
								ls_STYLE=ls_Share_Style.get(strSHARE_SIZE);
								break;
							}
						}
					}
					
					if(ls_STYLE==null) {
						ls_STYLE=getMD_Style_Share(getFA_NO(),STYLE_NO, SH_NO, SH_SIZE, WORK_WEEK_END);    					    					
	    				SHARE_SIZE=ls_Share_Style.keySet().toArray(new String[0]);     				
					}	
					
					if(ls_STYLE==null) {
						throw new Exception("型體:"+STYLE_NO+" "+SH_NO+"顏色:"+SH_COLOR+" 沒有找到共模關系");
					}			
		    		
		    		//再查找是否已記錄此型體本周的產能和排產數, 沒有則增加進來
		            if(!ls_STYLE.isEmpty()) {
			    		share_SH_NO=ls_STYLE.get(0)[0];
			    		share_SH_SIZE=ls_STYLE.get(0)[1];
			    		Map<String,SH_KEY_SIZE> ls_SH_KEY_SIZE=cls_var.getLs_SH_KEY_SIZE();
			    		synchronized(ls_SH_KEY_SIZE) {
			    			sh_key_size=ls_SH_KEY_SIZE.get(getFA_NO()+PROCID+share_SH_NO+share_SH_SIZE+WORK_WEEK_END);
			    		}
		            	
		            }else {
		            	Map<String,SH_KEY_SIZE> ls_SH_KEY_SIZE=cls_var.getLs_SH_KEY_SIZE();
		            	synchronized(ls_SH_KEY_SIZE) {
		            		sh_key_size=ls_SH_KEY_SIZE.get(getFA_NO()+PROCID+SH_NO+SH_SIZE+WORK_WEEK_END);
		            	}			            			            
		            }
		            
		            if(sh_key_size==null) {
		            	System.out.println(sh_key_size);
		            }
		            
		    		//如果已達到size的模具產能, 則往前排一周
		    		if(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY()<=0) {
		    			return 0;
		    		}
		    		
				}
    		}
    			    	
    		boolean color_exist=false;
    		Integer color_allow_count=null;
    		
			Map<String,Integer> ls_SH_COLOR_ALLOW_COUNT=cls_var.getLs_SH_COLOR_ALLOW_COUNT();
			
			synchronized(ls_SH_COLOR_ALLOW_COUNT) {
				
				//控制配色個數
				color_allow_count=ls_SH_COLOR_ALLOW_COUNT.get(sh_key_size.getSH_NO());

				if(color_allow_count==null) {
					color_allow_count=5;
					ls_SH_COLOR_ALLOW_COUNT.put(sh_key_size.getSH_NO(), color_allow_count);									
				}

			}
			
			Map<String,List<SH_COLOR_SIZE>> ls_SH_COLOR_SIZE=cls_var.getLs_SH_COLOR_SIZE();
			synchronized(ls_SH_COLOR_SIZE) {
				if(!ls_SH_COLOR_SIZE.isEmpty()) {
					int color_count=0;
					List<SH_COLOR_SIZE> al_SH_COLOR_SIZE =ls_SH_COLOR_SIZE.get(getFA_NO()+PROCID+sh_key_size.getSH_NO());
					if(al_SH_COLOR_SIZE!=null) {							
			            if(!al_SH_COLOR_SIZE.isEmpty()) {
			            	for(SH_COLOR_SIZE sh_color_size:al_SH_COLOR_SIZE) {			            		
			            		if(sh_color_size.getFA_NO().equals(getFA_NO())&&
			    	               sh_color_size.getPROCID().equals(PROCID) &&
			    	               sh_color_size.getWORK_WEEK()==WORK_WEEK_END) {
			            			color_count++;
			            			if(sh_color_size.getSH_NO().equals(SH_NO) && sh_color_size.getSH_COLOR().equals(SH_COLOR)) {
			            				color_exist=true;
			            				break;		
			            			}		            			
			            		}
			            	}
			            }
					}
					
					//配色沒有排過,此已排配色大於允許的個數
					if(color_allow_count<=color_count && color_exist==false) return 0;
					
				}	
			}
			
    		double RESIDUE_QTY=0;
    		synchronized(proc_Work_Qty) {
    			
    			RESIDUE_QTY=proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY(); //餘數
    			
        		if(RESIDUE_QTY>(sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY())) 
        			RESIDUE_QTY=sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY();

        		//如果配色第一次出現,可排量小於最小排產量,則不排
        		if(!color_exist && RESIDUE_QTY<SHOOT_MIN_PRODUCE_QTY) return 0;
        		
        		if(NEED_SHOOT) {
    	    		if(RESIDUE_QTY>(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY())) 
    	    			RESIDUE_QTY=sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY();
        		}

        		if(RESIDUE_QTY>=WORK_PLAN_QTY) RESIDUE_QTY=WORK_PLAN_QTY;
        		
        		if(RESIDUE_QTY<=0) return 0;	
    		}

			String strSQL="select " +
	                      "FCMPS010.OD_PONO1, "+
                          "FCMPS010.SH_SIZE,"+
                          "FCMPS010.OD_QTY,"+
                          "FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)- nvl(EXPECT_PLAN_QTY,0) WORK_PLAN_QTY "+
                          "from FCMPS010 "+
                          "where FCMPS010.SH_NO='"+SH_NO+"'"+
                          "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
                          "  and FCMPS010.SH_SIZE='"+SH_SIZE+"'"+
                          "  and FCMPS010.PROCID='"+PROCID+"'"+ 
                          "  and FCMPS010.FA_NO='"+getFA_NO()+"' "+
                          "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+ 				                   
                          "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
                          "  and FCMPS010.IS_DISABLE='N' "+                          
                          "  and to_char(FCMPS010.OD_FGDATE,'IYIW')>='"+OD_FGDATE_WEEK+"'"+
                          "  and to_char(FCMPS010.OD_FGDATE,'IYIW')<='"+OD_FGDATE_WEEK_END+"' "+
                          "order by OD_FGDATE DESC";
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();		  
		    
		    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		    rs.setFetchSize(3000);	
		    
		    List<FCMPS010_BEAN> ls_FCMPS010=new ArrayList<FCMPS010_BEAN>();
		    
		    double curOD_QTY=0;
		    
		    if(rs.next()) {			    	 
			    			    
		    	do {
		    		FCMPS010_BEAN data=new FCMPS010_BEAN();
		    		ls_FCMPS010.add(data);
		    		
		    		data.setOD_PONO1(rs.getString("OD_PONO1"));
		    		data.setFA_NO(getFA_NO());
		    		data.setSH_NO(SH_NO);
		    		data.setSH_COLOR(SH_COLOR);		    		
		    		data.setSH_SIZE(rs.getString("SH_SIZE"));
		    		data.setPROCID(PROCID);		  
		    		data.setOD_QTY(rs.getDouble("OD_QTY"));
		    		
		    		if(RESIDUE_QTY-curOD_QTY>rs.getDouble("WORK_PLAN_QTY")) {
		    			data.setWORK_PLAN_QTY(rs.getDouble("WORK_PLAN_QTY"));		    			
		    		}else {
		    			data.setWORK_PLAN_QTY(RESIDUE_QTY-curOD_QTY);
		    			break;
		    		}
		    		
		    		curOD_QTY=curOD_QTY+rs.getDouble("WORK_PLAN_QTY");
		    		
		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();
			
			String subSQL="";
			
			if(!ls_FCMPS010.isEmpty()) {
				
				for(int iRow=0;iRow<ls_FCMPS010.size();iRow++) {
					FCMPS010_BEAN data=ls_FCMPS010.get(iRow);
					if(!subSQL.equals("")) subSQL=subSQL+",";
					subSQL=subSQL+"('"+PROCID+"',"+
					                   WORK_WEEK_END+",'"+
					                   data.getOD_PONO1()+"','"+
					                   data.getFA_NO()+"','"+
					                   data.getSH_NO()+"','"+
					                   data.getSH_COLOR()+"','"+
					                   data.getSH_SIZE()+"')";
					
				}
				
	    		strSQL="select OD_PONO1,FA_NO,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS021 " +
	    			   "where (PROCID,WORK_WEEK,OD_PONO1,FA_NO,SH_NO,SH_COLOR,SH_SIZE) IN ("+subSQL+")";

				pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs=pstmtData.executeQuery();	
			    if(rs.next()) {	
			    	do {
			    		for(int iRow=0;iRow<ls_FCMPS010.size();iRow++) {
			    			FCMPS010_BEAN data=ls_FCMPS010.get(iRow);
			    			if(data.getFA_NO().equals(rs.getString("FA_NO")) &&
			    			   data.getOD_PONO1().equals(rs.getString("OD_PONO1")) &&
			    			   data.getSH_NO().equals(rs.getString("SH_NO")) &&
			    			   data.getSH_COLOR().equals(rs.getString("SH_COLOR")) &&
			    			   data.getSH_SIZE().equals(rs.getString("SH_SIZE")) &&
			    			   data.getPROCID().equals(rs.getString("PROCID"))
			    				) {
			    		    	strSQL="update FCMPS021 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)+"+data.getWORK_PLAN_QTY()+
						               "where PROCID='"+PROCID+"'"+
						               "  and WORK_WEEK='"+WORK_WEEK_END+"'"+
						               "  and OD_PONO1='"+data.getOD_PONO1()+"'"+
						               "  and SH_NO='"+SH_NO+"'"+
						               "  and SH_COLOR='"+SH_COLOR+"'"+
						               "  and SH_SIZE='"+SH_SIZE+"'";
			    		    	
								pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
							    pstmtData2.executeQuery();	
							    pstmtData2.close();
							    ls_FCMPS010.remove(data);
							    break;
			    			}
			    		}
			    		
			    	}while(rs.next());
			    }
				rs.close();
				pstmtData.close();
				
		    	strSQL="insert into FCMPS021(procid, work_week, od_pono1,sh_no, sh_size, sh_color, " +
		               "                     proc_seq, od_qty, work_plan_qty, up_user, up_date, style_no, " +
		               "                     fa_no, size_cap_qty, sh_cap_qty, need_shoot, share_sh_no, " +
		               "                     share_size,od_fgdate_week,IS_USE_CAP,WEEK_PLAN_DAYS)";
		    	
		    	for(int iRow=0;iRow<ls_FCMPS010.size();iRow++) {
		    		FCMPS010_BEAN data=ls_FCMPS010.get(iRow);
		    		if(iRow!=0)strSQL=strSQL+" union all ";
		    		strSQL=strSQL+"select ";
			    	strSQL=strSQL+"'"+PROCID+"'";
			    	strSQL=strSQL+","+WORK_WEEK_END;
			    	strSQL=strSQL+",'"+data.getOD_PONO1()+"'";
			    	strSQL=strSQL+",'"+SH_NO+"'";
			    	strSQL=strSQL+",'"+SH_SIZE+"'";
			    	strSQL=strSQL+",'"+SH_COLOR+"'";
			    	strSQL=strSQL+",'"+PROC_SEQ+"'";
			    	strSQL=strSQL+","+data.getOD_QTY();
			    	strSQL=strSQL+","+data.getWORK_PLAN_QTY();
			    	strSQL=strSQL+",'"+getUP_USER()+"'";
			    	strSQL=strSQL+",sysdate";
			    	strSQL=strSQL+",'"+STYLE_NO+"'";
			    	strSQL=strSQL+",'"+FA_NO+"'";
			    	if(NEED_SHOOT) {
			    		strSQL=strSQL+","+sh_key_size.getWORK_CAP_QTY();
			    	}else {
			    		strSQL=strSQL+",null";
			    	}		    	
			    	strSQL=strSQL+","+sh_Work_Qty.getWORK_CAP_QTY();
			    	strSQL=strSQL+",'"+(NEED_SHOOT?"Y":"N")+"'";
			    	strSQL=strSQL+",'"+share_SH_NO+"'";
			    	strSQL=strSQL+",'"+share_SH_SIZE+"'";
			    	strSQL=strSQL+","+OD_FGDATE_WEEK;
			    	strSQL=strSQL+",'"+USE_CAP+"'";
			    	strSQL=strSQL+","+Week_Plan_Days+" from dual ";
		    	}

				pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    pstmtData2.executeQuery();	
			    pstmtData2.close();	
			    
				subSQL="";
				for(int iRow=0;iRow<ls_FCMPS010.size();iRow++) {
					FCMPS010_BEAN data=ls_FCMPS010.get(iRow);
					if(!subSQL.equals("")) subSQL=subSQL+",";
					subSQL=subSQL+"('"+data.getOD_PONO1()+"','"+
					                   data.getFA_NO()+"','"+
					                   data.getSH_NO()+"','"+
					                   data.getSH_COLOR()+"','"+
					                   data.getSH_SIZE()+"','"+
					                   PROCID+"')";
					
				}
				
	    		//更新訂單的預排數量
	    		strSQL="update FCMPS010 " +
	    			   "set EXPECT_PLAN_QTY=(select nvl(sum(work_plan_Qty),0) from FCMPS021 " +
	    			   "  where PROCID=FCMPS010.PROCID" +
	    			   "    and OD_PONO1=FCMPS010.OD_PONO1" +    			   
	    			   "    and SH_NO=FCMPS010.SH_NO" +
	    			   "    and SH_COLOR=FCMPS010.SH_COLOR" +
	    			   "    and SH_SIZE=FCMPS010.SH_SIZE" +
	    			   "    and FA_NO=FCMPS010.FA_NO" +
	    			   ") where (OD_PONO1,FA_NO,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN ("+subSQL+")";
//	    		System.out.println(strSQL);
				pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    pstmtData2.executeQuery();	
			    pstmtData2.close();	
			    
			}

			synchronized(proc_Work_Qty) {
	    		if(USE_CAP.equals("Y")) { //占用產能
	    			sh_Work_Qty.setWORK_PLANNED_QTY(sh_Work_Qty.getWORK_PLANNED_QTY()+RESIDUE_QTY);
	    			proc_Work_Qty.setWORK_PLANNED_QTY(proc_Work_Qty.getWORK_PLANNED_QTY()+RESIDUE_QTY);	
	    				    			
	        		if(NEED_SHOOT) {
	        			sh_key_size.setWORK_PLANNED_QTY(sh_key_size.getWORK_PLANNED_QTY()+RESIDUE_QTY);
	        		}
	    		}
			}
			
			//記錄本周型體配色排了多少數量
			if(NEED_SHOOT) {
				
				ls_SH_COLOR_SIZE=cls_var.getLs_SH_COLOR_SIZE();
				synchronized(ls_SH_COLOR_SIZE) {
					List<SH_COLOR_SIZE> al_SH_COLOR_SIZE=ls_SH_COLOR_SIZE.get(getFA_NO()+PROCID+sh_key_size.getSH_NO());
		            if(al_SH_COLOR_SIZE==null) {
		            	al_SH_COLOR_SIZE=new ArrayList<SH_COLOR_SIZE>();
		            	cls_var.putLs_SH_COLOR_SIZE(getFA_NO()+PROCID+sh_key_size.getSH_NO(), al_SH_COLOR_SIZE);
		            }
		            
		            if(!al_SH_COLOR_SIZE.isEmpty()) {
		            	boolean iExist=false;
		            	for(SH_COLOR_SIZE sh_color_size:al_SH_COLOR_SIZE) {
		            		if(sh_color_size.getFA_NO().equals(getFA_NO())&&
		            		   sh_color_size.getPROCID().equals(PROCID) &&
		            		   sh_color_size.getSH_NO().equals(SH_NO) &&
		            		   sh_color_size.getSH_COLOR().equals(SH_COLOR) &&
		            		   sh_color_size.getWORK_WEEK()==WORK_WEEK_END) {
		            			sh_color_size.setWORK_PLANNED_QTY(sh_color_size.getWORK_PLANNED_QTY()+RESIDUE_QTY);
		            			iExist=true;
		            			break;
		            		}
		            	}
		            	if(!iExist) {
	    	            	SH_COLOR_SIZE sh_color_size=new SH_COLOR_SIZE();
	    	        		sh_color_size.setFA_NO(getFA_NO());
	    	        		sh_color_size.setPROCID(PROCID);
	    	        		sh_color_size.setSH_COLOR(SH_COLOR);
	    	        		sh_color_size.setSH_NO(SH_NO);
	    	        		sh_color_size.setWORK_WEEK(WORK_WEEK_END);
	    	        		sh_color_size.setWORK_PLANNED_QTY(RESIDUE_QTY);
	    	        		
	    	            	al_SH_COLOR_SIZE.add(sh_color_size);
		            	}
		            }else {
		            	SH_COLOR_SIZE sh_color_size=new SH_COLOR_SIZE();
		        		sh_color_size.setFA_NO(getFA_NO());
		        		sh_color_size.setPROCID(PROCID);
		        		sh_color_size.setSH_COLOR(SH_COLOR);
		        		sh_color_size.setSH_NO(SH_NO);
		        		sh_color_size.setWORK_WEEK(WORK_WEEK_END);
		        		sh_color_size.setWORK_PLANNED_QTY(RESIDUE_QTY);
		        		
		            	al_SH_COLOR_SIZE.add(sh_color_size);
		            	
		            }    	
				}
            	          
			}
			
			iRet=RESIDUE_QTY;			

			return iRet;
			
		}
		
		
		/**
		 * 型體制程是否需要排計劃<br>
		 * 因為有射出和針車,針車和組底,針車和轉印在同一周生產,且都要排計劃,故返回ArrayList <br>
		 * @param SH_NO
		 * @param PROC_SEQ 制程順序
		 * @return String 需要排計劃則返回制程代號, 否則返回空
		 */
		private ArrayList<String[]> getNeed_Plan_PROC(String SH_NO,double PROC_SEQ) {
			ArrayList<String[]> iRet=new ArrayList<String[]>();
			String strSQL="";
			Connection conn =getConnection();
			PreparedStatement pstmtData = null;		
			ResultSet rs=null;
			
			try{

				strSQL="select fcps22_1.PB_PTNO,fcps22_2.PB_PTNA,nvl(fcps22_1.IS_USE_CAP,'N') IS_USE_CAP " +
					   "from fcps22_1,fcps22_2 " +
					   "where fcps22_1.SH_ARITCLE='"+SH_NO+"' " +
					   "  and fcps22_1.PB_PTNO=fcps22_2.PB_PTNO(+)" +
					   "  and fcps22_1.NEED_PLAN='Y'"+
					   "  and fcps22_1.PROC_SEQ="+PROC_SEQ;
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    if(rs.next()){
			    	do {
			    		iRet.add(new String[] {rs.getString("PB_PTNO"),rs.getString("PB_PTNA"),rs.getString("IS_USE_CAP")});
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
		
		private List<String[]> getMD_Style_Share(
				String FA_NO,
				String STYLE_NO,
				String SH_NO,
				String SH_SIZE,
				int WORK_WEEK) {
			List<String[]> iRet=null;
			
			String strSQL="";
			
			PreparedStatement pstmtData = null;		
			ResultSet rs=null;		
			
			Connection conn=getConnection();
			
			try{
				
				strSQL="select DISTINCT EFFECTIVE_WEEK " +
				       "from FCMPS0022 A " +
				       "where SH_NO='" +SH_NO+"'"+
				       "  and FA_NO='"+FA_NO+"'" +
				       "  and SH_SIZE='"+SH_SIZE+"' "+
				       "order by EFFECTIVE_WEEK DESC";
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    if(rs.next()){
			    	do {
			    		int EFFECTIVE_WEEK=rs.getInt("EFFECTIVE_WEEK");
			    		Map<String,List<String[]>> ls_Share_Style=cls_var.getLs_Share_Style();
			    		synchronized(ls_Share_Style) {
				    		if(!FCMPS_PUBLIC.ChokePointPart_is_SharePart(FA_NO, SH_NO, SH_SIZE, getConnection(),EFFECTIVE_WEEK)) {
				    			ArrayList<String[]> ls_STYLE=new ArrayList<String[]>();
				    			ls_Share_Style.put(getFA_NO()+STYLE_NO+SH_NO+SH_SIZE+EFFECTIVE_WEEK,ls_STYLE);
				    			if(EFFECTIVE_WEEK<=WORK_WEEK && iRet==null) iRet=ls_STYLE;
				    					    			
				    		}else {												
				    			ArrayList<String[]> ls_STYLE_B=FCMPS_PUBLIC.getSH_Share_SIZE_Max_MD_CAP(SH_NO,SH_SIZE, FA_NO, getConnection(),EFFECTIVE_WEEK);
				    			ls_Share_Style.put(FA_NO+STYLE_NO+SH_NO+SH_SIZE+EFFECTIVE_WEEK,ls_STYLE_B);
				    			if(EFFECTIVE_WEEK<=WORK_WEEK && iRet==null) iRet=ls_Share_Style.get(FA_NO+STYLE_NO+SH_NO+SH_SIZE+EFFECTIVE_WEEK);
				    		}
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
		
		

		
		private SH_KEY_SIZE getMD_Min_Week_Cap_QTY(
				String FA_NO,
				String SH_NO,
				String SH_SIZE,
				String PROCID,
				List<SH_KEY_SIZE> ls_SH_SIZE_CAP,
				int WORK_WEEK,
				double Week_Plan_Days) {
			
			SH_KEY_SIZE sh_size_cap=new SH_KEY_SIZE();
			sh_size_cap.setFA_NO(getFA_NO());
			sh_size_cap.setPROCID(PROCID);	    				
			sh_size_cap.setSH_SIZE(SH_SIZE);
			sh_size_cap.setWORK_WEEK(WORK_WEEK);		    		
			sh_size_cap.setSH_NO(SH_NO);
			sh_size_cap.setWORK_CAP_QTY(FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(FA_NO,SH_NO,SH_SIZE, getConnection(),WORK_WEEK,Week_Plan_Days));
			
			ls_SH_SIZE_CAP.add(sh_size_cap);	
				    
		    return sh_size_cap;		
		}
		
		private SH_WORK_QTY getSH_WORK_QTY(
				String SH_NO,
				String PROCID,
				int WORK_WEEK_END) {
			    
				SH_WORK_QTY sh_cap_Qty=null;
				
				Map<String,List<SH_WORK_QTY>> ls_SH_CAP_QTY=cls_var.getLs_SH_CAP_QTY();
				
				synchronized(ls_SH_CAP_QTY) {
					List<SH_WORK_QTY> al_SH_CAP_QTY=ls_SH_CAP_QTY.get(getFA_NO()+SH_NO);	 
					
					for(int i=0;i<al_SH_CAP_QTY.size();i++) {
						sh_cap_Qty=al_SH_CAP_QTY.get(i);
						if(sh_cap_Qty.getFA_NO().equals(getFA_NO()) &&
						   sh_cap_Qty.getSH_NO().equals(SH_NO) &&
						   sh_cap_Qty.getPROCID().equals(PROCID) &&
						   sh_cap_Qty.getWORK_WEEK()==WORK_WEEK_END) {
							break;
						}
					} 
				}
			    		    
			    return sh_cap_Qty;
		}
	}
	
	public class FCMPS_CLS_ForeGenerateRccpPlan_MultiThread_Info{
		private Date stDate;
		private Date edDate;
		private String OD_FGDATE_WEEK;
		private int SH_NO_CASE;
		private int COLOR_CASE;
		private int FINISH_CASE;
		private int ERROR_CASE;
		private int CANCEL_CASE;
		
		public int getCOLOR_CASE() {
			return COLOR_CASE;
		}
		public void setCOLOR_CASE(int color_case) {
			COLOR_CASE = color_case;
		}
		public Date getEdDate() {
			return edDate;
		}
		public void setEdDate(Date edDate) {
			this.edDate = edDate;
		}
		public int getERROR_CASE() {
			return ERROR_CASE;
		}
		public void setERROR_CASE(int error_case) {
			ERROR_CASE = error_case;
		}
		public int getFINISH_CASE() {
			return FINISH_CASE;
		}
		public void setFINISH_CASE(int finish_case) {
			FINISH_CASE = finish_case;
		}
		public String getOD_FGDATE_WEEK() {
			return OD_FGDATE_WEEK;
		}
		public void setOD_FGDATE_WEEK(String od_fgdate_week) {
			OD_FGDATE_WEEK = od_fgdate_week;
		}
		public int getSH_NO_CASE() {
			return SH_NO_CASE;
		}
		public void setSH_NO_CASE(int sh_no_case) {
			SH_NO_CASE = sh_no_case;
		}
		public Date getStDate() {
			return stDate;
		}
		public void setStDate(Date stDate) {
			this.stDate = stDate;
		}

		public int getCANCEL_CASE() {
			return CANCEL_CASE;
		}
		public void setCANCEL_CASE(int cancel_case) {
			CANCEL_CASE = cancel_case;
		}
		
		
		
	}
	
	
	
}
