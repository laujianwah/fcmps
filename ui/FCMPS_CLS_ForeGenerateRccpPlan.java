package fcmps.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import dsc.util.function.UUID;
import fcmps.domain.FCMPS010_BEAN;
import fcmps.domain.FCMPS021_BEAN;

/**
 * 在導入FOS後,按照系統設定的制程標準產能,型體標準產能,依FG Date預排各訂單應分佈在哪些周次生產較合理
 * @author dev17
 *
 */
public class FCMPS_CLS_ForeGenerateRccpPlan extends TestCase{
	private String FA_NO="";
	private Connection conn=null;
	
	private ArrayList<CLS_RCCP_ERROR> ls_Message=new ArrayList<CLS_RCCP_ERROR>();
	
	private SessionFactory sessionFactory=null;
	private Session session=null; 
    private Transaction transaction=null;
    private String config_xml="";
    
    private String UP_USER="DEV";
    private String output="";
    
    private static Log log = LogFactory.getLog( FCMPS_CLS_ForeGenerateRccpPlan.class );
    
	private ArrayList<PROC_WORK_QTY> ls_PROC_WORK_QTY=new ArrayList<PROC_WORK_QTY>();
//	private Map<String,SH_WORK_QTY> ls_SH_WORK_QTY=new TreeMap<String,SH_WORK_QTY>();	    	
	private Map<String,ArrayList<SH_WORK_QTY>> ls_SH_CAP_QTY=new TreeMap<String,ArrayList<SH_WORK_QTY>>();
	
	private Map<String,SH_KEY_SIZE> ls_SH_KEY_SIZE=new TreeMap<String,SH_KEY_SIZE>();
	private Map<String,ArrayList<SH_KEY_SIZE>> ls_SH_SIZE_CAP=new TreeMap<String,ArrayList<SH_KEY_SIZE>>();
	private Map<String,ArrayList<SH_COLOR_SIZE>> ls_SH_COLOR_SIZE=new TreeMap<String,ArrayList<SH_COLOR_SIZE>>();
	
//	private Map<String,Integer> ls_SH_COLOR_ALLOW_COUNT=getColor_Allow_Count(getFA_NO());
	private Map<String,Integer> ls_SH_COLOR_ALLOW_COUNT=null;
	
	private Map<String,ArrayList<String[]>> ls_Share_Style=new TreeMap<String,ArrayList<String[]>>();
	
	private Map<String,Boolean> ls_SH_NEED_SHOOT=new TreeMap<String,Boolean>();
    
	private Map<String,ArrayList<Double>> ls_SH_PROC_SEQ=new TreeMap<String,ArrayList<Double>>();
	private Map<String,ArrayList<String[]>> ls_SH_NEED_PLAN_PROC=new TreeMap<String,ArrayList<String[]>>();	
    
	//每周的型體部位列表
	private Map<String,List<String[]>> ls_WORK_SH_PART=new TreeMap<String,List<String[]>>();
	
	//型體的部位列表
	private Map<String,List<String[]>> set_SH_PARTS=new TreeMap<String,List<String[]>>();
	
	//每周可以生產的型體部位個數
	private int WEEK_SH_PART_COUNTS=0;
	
    public void test_ForeGenerateRccpPlan() {
    	config_xml="C:\\temp\\20130121\\FTI.cfg.xml";
    	output="F:/臨時文件/2014/20140603";
    	Date stDate=new Date();
    	int CURRENT_PLAN_WEEK=1422;
    	GenericSessionFactory();
    	this.setFA_NO("FIC");    	
    	if(!deleteFCMPS021()) {
    		return;
    	}
    	ls_SH_COLOR_ALLOW_COUNT=getColor_Allow_Count(getFA_NO(),getConnection());
    	WEEK_SH_PART_COUNTS=getPart_Allow_Count(getFA_NO(),getConnection());
    	doGeneratePlan(CURRENT_PLAN_WEEK);
    	CloseSessionFactory();  
    	Date edDate=new Date();
    	System.out.println("開始時間:"+stDate.toString()+" 結止時間:"+edDate.toString());
    	doPrint();
    }
    
	/**
	 * 產生SessionFactory
	 *
	 */
	private boolean GenericSessionFactory() {
		boolean iRet=true;
		try {
			File fConfig=new File(getConfig_XML());
			if(!fConfig.exists()) {
				log.warn( "The Config file " + getConfig_XML()+" does not exist!" );
				return false;
			}
			Configuration config=new Configuration().configure(fConfig);	
			config.addClass(FCMPS021_BEAN.class);
			config.addClass(FCMPS010_BEAN.class);
			sessionFactory=config.buildSessionFactory();	
			
			session = sessionFactory.openSession();			
			transaction = session.beginTransaction(); 
			conn=session.connection();
			
		}catch(Exception ex) {
			ex.printStackTrace();
			if(transaction!=null) transaction.rollback();
			iRet=false;			
		}
		return iRet;
	}
	
	public String getConfig_XML() {
		return config_xml;
	}
	
	private void CloseSessionFactory() {
		sessionFactory.close();
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
	 * 取得訊息
	 * @return
	 */
	public ArrayList<CLS_RCCP_ERROR> getMessage() {
		return ls_Message;
	}
	

	private void setMessage(CLS_RCCP_ERROR message) {
		ls_Message.add(message);
	}	
	
    public void doGeneratePlan(String config_xml,int CURRENT_PLAN_WEEK) {
    	this.config_xml=config_xml;
    	GenericSessionFactory();
    	
    	if(!deleteFCMPS021()) {
    		return;
    	}
    	ls_SH_COLOR_ALLOW_COUNT=getColor_Allow_Count(getFA_NO(),getConnection());
    	WEEK_SH_PART_COUNTS=getPart_Allow_Count(getFA_NO(),getConnection());
    	
    	doGeneratePlan(CURRENT_PLAN_WEEK);
    	CloseSessionFactory();    	
    }
	
    public void doGeneratePlan(String config_xml,String isReCalcu,int CURRENT_PLAN_WEEK) {
    	this.config_xml=config_xml;
    	GenericSessionFactory();

    	ls_SH_COLOR_ALLOW_COUNT=getColor_Allow_Count(getFA_NO(),getConnection());
    	WEEK_SH_PART_COUNTS=getPart_Allow_Count(getFA_NO(),getConnection());
    	
    	if(isReCalcu.toUpperCase().equals("Y")) {
    		doReCalcuPlan(
    				ls_PROC_WORK_QTY, 
    				ls_SH_KEY_SIZE,
    				ls_SH_SIZE_CAP,
    				ls_SH_COLOR_SIZE);
    	}else {
        	if(!deleteFCMPS021()) {
        		return;
        	}
    	}

    	doGeneratePlan(CURRENT_PLAN_WEEK);
    	
    	CloseSessionFactory();    	
    }
    
	private boolean deleteFCMPS021(String ...SH_NO) {
        boolean iRet=false;
		
		String strSQL="";
		
		Connection conn=getConnection();
		PreparedStatement pstmtData = null;		
		
		try{
			
			strSQL="TRUNCATE TABLE FCMPS021";
			
			if(SH_NO.length>0) {
				strSQL="delete from FCMPS021 where SH_NO='"+SH_NO[0]+"'";
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
                   (SH_NO.length>0?" and FCMPS010.SH_NO='"+SH_NO[0]+"' ":"")+
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
    
    private boolean doReCalcuPlan(
			ArrayList<PROC_WORK_QTY> ls_PROC_WORK_QTY,
			Map<String,SH_KEY_SIZE> ls_SH_KEY_SIZE,
			Map<String,ArrayList<SH_KEY_SIZE>> ls_SH_SIZE_CAP,
			Map<String,ArrayList<SH_COLOR_SIZE>> ls_SH_COLOR_SIZE    		
    		) {
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
			strSQL="select WORK_WEEK,PROCID,sum(WORK_PLAN_QTY) WORK_PLAN_QTY " +
				   "from FCMPS021 "+
				   "group by WORK_WEEK,PROCID";
			
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
	    			
	    			ls_PROC_WORK_QTY.add(proc_Work_Qty);
	    			
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
	                           "  and SH_NO='"+SH_NO+"'"+
	                           (!share_SH_NO.equals("")?" and share_SH_NO='"+share_SH_NO+"'":"")+
	                           "  and WORK_WEEK="+WORK_WEEK_END+" "+
	                           "group by OD_FGDATE_WEEK,SH_COLOR";
	    	    		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
	    	    	        rs3=pstmtData3.executeQuery();
	    	    	 
	    	    	        if(rs3.next()){
	    	    	        	do {
	    	    	        		
	    	    	        		String SH_COLOR=rs3.getString("SH_COLOR");
	    	    	        		double SH_COLOR_QTY=rs3.getDouble("WORK_PLAN_QTY");
	    	    	        		int OD_FGDATE_WEEK=rs3.getInt("OD_FGDATE_WEEK");
	    	    	        		
	    	    		    		ArrayList<SH_COLOR_SIZE> al_SH_COLOR_SIZE=ls_SH_COLOR_SIZE.get(getFA_NO()+PROCID+OD_FGDATE_WEEK+SH_NO+SH_COLOR);

	    	    		    		if(al_SH_COLOR_SIZE==null) {
	    	    		    			al_SH_COLOR_SIZE=new ArrayList<SH_COLOR_SIZE>();
	    	    		    			ls_SH_COLOR_SIZE.put(getFA_NO()+PROCID+OD_FGDATE_WEEK+SH_NO+SH_COLOR, al_SH_COLOR_SIZE);
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
	    	    	        			    	    	        		
	    	    	        	}while(rs3.next());
	    	    	        }
	    	    	        rs3.close();
	    	    	        pstmtData3.close();
	    	    	        
	    	    	        
	    	    			strSQL="select share_SIZE,SH_SIZE,SIZE_CAP_QTY,sum(work_plan_Qty) WORK_PLAN_QTY "+
	                               "from fcmps021 "+
	                               "where PROCID='"+PROCID+"' " +
	                               "  and SH_NO='"+SH_NO+"'"+
	                               (!share_SH_NO.equals("")?" and share_SH_NO='"+share_SH_NO+"'":"")+
	                               "  and WORK_WEEK="+WORK_WEEK_END+" "+
	                               "group by share_SIZE,SH_SIZE,SIZE_CAP_QTY";
	    	    		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
	    	    	        rs3=pstmtData3.executeQuery();
	    	    	 
	    	    	        if(rs3.next()){
	    	    	        	do {
	    	    	        		String SH_SIZE=rs3.getString("SH_SIZE");
	    	    	        		
	    	    	        		List<String[]> ls_SH_PARTS=ls_WORK_SH_PART.get(String.valueOf(WORK_WEEK_END)+":"+PROCID);
	    	    	        		if(ls_SH_PARTS!=null) {

	    	    	        			List<String[]> ls_SH_PARTS2=FCMPS_PUBLIC.getMD_Style_Part(getFA_NO(), SH_NO, getConnection(), WORK_WEEK_END);
	    	    	        			for(int i=0;i<ls_SH_PARTS2.size();i++) {
	    	    	        				boolean iExists=false;
	    	    	        				for(int n=0;n<ls_SH_PARTS.size();n++) {
	    	    	        					String[] SH_PARTS=ls_SH_PARTS.get(n);								
	    	    	        					if(ls_SH_PARTS2.get(i)[0].equals(SH_PARTS[0]) && ls_SH_PARTS2.get(i)[1].equals(SH_PARTS[1])) {
	    	    	        						iExists=true;
	    	    	        						break;
	    	    	        					}
	    	    	        				}
	    	    	        				
	    	    	        				if(!iExists)ls_SH_PARTS.add(new String[] {ls_SH_PARTS2.get(i)[0],ls_SH_PARTS2.get(i)[1]});
	    	    	        				
	    	    	        			}

	    	    	        		}else {
	    	    	        			ls_SH_PARTS=FCMPS_PUBLIC.getMD_Style_Part(getFA_NO(), SH_NO,  getConnection(), WORK_WEEK_END);
	    	    	        			ls_WORK_SH_PART.put(String.valueOf(WORK_WEEK_END)+":"+PROCID, ls_SH_PARTS);
	    	    	        		}
	    	    	        		
	    	    	        		
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
	    	    	    			ArrayList<SH_KEY_SIZE> al_SH_SIZE_CAP=ls_SH_SIZE_CAP.get(getFA_NO()+PROCID+SH_NO+SH_SIZE);
	    	    	    			if(al_SH_SIZE_CAP==null) {
	    	    	    				al_SH_SIZE_CAP=new ArrayList<SH_KEY_SIZE>();
	    	    	    				al_SH_SIZE_CAP.add(sh_size_cap);
	    	    	    				ls_SH_SIZE_CAP.put(getFA_NO()+PROCID+SH_NO+SH_SIZE,al_SH_SIZE_CAP);
	    	    	    			}else {
	    	    	    				al_SH_SIZE_CAP.add(sh_size_cap);
	    	    	    			}
	    	    	    			
	    	    	        		
	    	    	    			SH_KEY_SIZE sh_key_size=new SH_KEY_SIZE();
	    		    				sh_key_size.setFA_NO(getFA_NO());
	    		    				sh_key_size.setPROCID(PROCID);	    				
	    		    				
	    		    				sh_key_size.setWORK_WEEK(WORK_WEEK_END);
	    			    			
	    		    				if(!share_SH_NO.equals("")) {		            		
	    			    				sh_key_size.setSH_NO(share_SH_NO);
	    			    				sh_key_size.setSH_SIZE(share_SIZE);
	    			    				sh_key_size.setWORK_CAP_QTY(sh_size_cap.getWORK_CAP_QTY());
		    		    				sh_key_size.setWORK_PLANNED_QTY(SH_SIZE_QTY);
		    			    			ls_SH_KEY_SIZE.put(getFA_NO()+PROCID+share_SH_NO+share_SIZE+WORK_WEEK_END,sh_key_size);
	    		    				}else {
	    			    				sh_key_size.setSH_NO(SH_NO);
	    			    				sh_key_size.setSH_SIZE(SH_SIZE);
	    			    				sh_key_size.setWORK_CAP_QTY(sh_size_cap.getWORK_CAP_QTY());
		    		    				sh_key_size.setWORK_PLANNED_QTY(SH_SIZE_QTY);
		    			    			ls_SH_KEY_SIZE.put(getFA_NO()+PROCID+SH_NO+SH_SIZE+WORK_WEEK_END,sh_key_size);
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
		
		try {	    
			
			strSQL="select distinct " +
			       "to_char(FCMPS010.OD_FGDATE,'IYIW') OD_FGDATE_WEEK "+
                   "from FCMPS010 "+
                   "where OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY>0 "+  
                   "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
                   "  and FCMPS010.IS_DISABLE='N' "+
                   (SPEC_SH_NO.length>0?" and FCMPS010.SH_NO='"+SPEC_SH_NO[0]+"' ":"")+
//                   "  and STYLE_NO in ('16013','15991','14461') "+
//                   "  and SH_NO='CBDVADERLNDCLG'"+
//                   "  and to_char(FCMPS010.OD_FGDATE,'IYIW')<='1436'"+                    
                   "  and FCMPS010.OD_FGDATE is not null "+                   
                   "  and FCMPS010.FA_NO='"+getFA_NO()+"' "+
                   "order by to_char(FCMPS010.OD_FGDATE,'IYIW') DESC";
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){

		    	String OD_FGDATE_WEEK="";
		    	String PRE_OD_FGDATE_WEEK="";
		    	
		    	do {
		    		
		    		OD_FGDATE_WEEK=rs.getString("OD_FGDATE_WEEK");
			    	
		    		System.out.println(new Date()+" " +OD_FGDATE_WEEK);		    
				    
		    		Map<String,Integer> ls_SH_BY_WEEKS=new LinkedHashMap<String,Integer>();
		    		
		    		//後一周已排的型體
		    		getSH_BY_WEEK(OD_FGDATE_WEEK,PRE_OD_FGDATE_WEEK,2,getConnection(),ls_SH_BY_WEEKS,SPEC_SH_NO);
		    		//優先排產的型體
		    		getSH_BY_WEEK(OD_FGDATE_WEEK,PRE_OD_FGDATE_WEEK,1,getConnection(),ls_SH_BY_WEEKS,SPEC_SH_NO);
		    		//本周需要排的型體
		    		getSH_BY_WEEK(OD_FGDATE_WEEK,PRE_OD_FGDATE_WEEK,0,getConnection(),ls_SH_BY_WEEKS,SPEC_SH_NO);
		    				    		
		    		Iterator<String> it_SH_NO= ls_SH_BY_WEEKS.keySet().iterator();
		    		while(it_SH_NO.hasNext()) {
		    			String SH_NO=it_SH_NO.next();

			    		Boolean NEED_SHOOT=ls_SH_NEED_SHOOT.get(SH_NO);

			    		if(NEED_SHOOT==null) {
			    			NEED_SHOOT=FCMPS_PUBLIC.getSH_Need_PROC(SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, getConnection());
			    			ls_SH_NEED_SHOOT.put(SH_NO, NEED_SHOOT);
			    		}
			    		
			    		ArrayList<Double> ls_PROC_SEQ=null;
			    		
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

			    			continue;
			    		}
			    		
		    			int ALLOW_PLAN_WEEKS=ls_SH_BY_WEEKS.get(SH_NO);
		    			
		    			ALLOW_PLAN_WEEKS=0; //預排先不提前
		    			
		    			int OD_FGDATE_WEEK_END=FCMPS_PUBLIC.getNext_Week(Integer.valueOf(OD_FGDATE_WEEK), ALLOW_PLAN_WEEKS);
		    			
					    strSQL="select STYLE_NO,SH_NO,SH_COLOR,max(OD_QTY) OD_QTY FROM ( "+
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
	                           "order by OD_QTY ";
					    
						pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
					    rs2=pstmtData2.executeQuery();
					    
					    rs2.setFetchDirection(ResultSet.FETCH_FORWARD);
					    rs2.setFetchSize(3000);
					    
					    if(rs2.next()){	
   	
					    	do {
					    							    		
					    		String STYLE_NO=rs2.getString("STYLE_NO");
					    		String SH_COLOR=rs2.getString("SH_COLOR");
					    						    						    		
					    		Date st_date=new Date();
					    		
					    		int WORK_WEEK_END=FCMPS_PUBLIC.getInt(OD_FGDATE_WEEK);	
					    		
						    	iRet=doGeneratePlan(
						    			OD_FGDATE_WEEK,
						    			OD_FGDATE_WEEK_END,
						    			WORK_WEEK_END,
						    			STYLE_NO,
						    			SH_NO,
						    			SH_COLOR,
						    			ls_PROC_WORK_QTY, 
						    			ls_SH_CAP_QTY,
						    			ls_SH_BY_WEEKS,
						    			ls_SH_KEY_SIZE,
						    			ls_SH_SIZE_CAP,						    			
						    			ls_SH_COLOR_SIZE,
						    			ls_SH_COLOR_ALLOW_COUNT,
						    			ls_PROC_SEQ,
						    			ls_SH_NEED_PLAN_PROC,
						    			ls_Share_Style,
						    			NEED_SHOOT,
						    			CURRENT_PLAN_WEEK);	

								Date ed_date=new Date();
								
								System.out.println("型體:"+SH_NO+"顏色:"+SH_COLOR+" flush time:"+(ed_date.getTime()-st_date.getTime()));

					    	}while(rs2.next());
					    }
					    rs2.close();
					    pstmtData2.close();	    			    	
    			    	    			    
		    		}

		    		System.gc();
		    		
				    PRE_OD_FGDATE_WEEK=OD_FGDATE_WEEK;
				    
		    	}while(rs.next());
			    
		    }
		    rs.close();
		    pstmtData.close();

		}catch(Exception ex) {
			ex.printStackTrace();
		}
    	
		return iRet;
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
			ArrayList<PROC_WORK_QTY> ls_PROC_WORK_QTY,
//			Map<String,SH_WORK_QTY> ls_SH_WORK_QTY,
			Map<String,ArrayList<SH_WORK_QTY>> ls_SH_CAP_QTY,
			Map<String,Integer> ls_SH_BY_WEEKS,
			Map<String,SH_KEY_SIZE> ls_SH_KEY_SIZE,
			Map<String,ArrayList<SH_KEY_SIZE>> ls_SH_SIZE_CAP,
			Map<String,ArrayList<SH_COLOR_SIZE>> ls_SH_COLOR_SIZE,
			Map<String,Integer> ls_SH_COLOR_ALLOW_COUNT,
			ArrayList<Double> ls_PROC_SEQ,
			Map<String,ArrayList<String[]>> ls_SH_NEED_PLAN_PROC,
			Map<String,ArrayList<String[]>> ls_Share_Style,
			boolean NEED_SHOOT,
			int CURRENT_PLAN_WEEK
			) {
		boolean iRet=false;
				
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;

		try{		
    		    		
    		for(int iWeek=0;iWeek<ls_PROC_SEQ.size();iWeek++) {
    			WORK_WEEK_START=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_START, 1);
    			ArrayList<String[]> ls_PB_PTNO=ls_SH_NEED_PLAN_PROC.get(SH_NO+ls_PROC_SEQ.get(iWeek));
    			if(ls_PB_PTNO==null) {
    				ls_PB_PTNO=getNeed_Plan_PROC(SH_NO, ls_PROC_SEQ.get(iWeek));
    				ls_SH_NEED_PLAN_PROC.put(SH_NO+ls_PROC_SEQ.get(iWeek), ls_PB_PTNO);
    			}
    				    			    	    			    		
    			for(String[] PB_PTNO:ls_PB_PTNO) {
    				String USE_CAP=PB_PTNO[2];
    				
    				int WORK_WEEK_END=WORK_WEEK_START; 
    				
    				transaction.begin();
	    	
	    			double work_week_days=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(
	    					getFA_NO(),
	    					SH_NO, 
	    					WORK_WEEK_END, 
	    					getConnection());
	    			
	    			doInitSH_CAP(SH_NO,PB_PTNO[0], WORK_WEEK_END, NEED_SHOOT, ls_SH_CAP_QTY, work_week_days);	    			
    				
    				this.doInitPROC_CAP(
    						SH_NO, 
    						SH_COLOR,
    						PB_PTNO[0], 
    						PB_PTNO[1], 
    						WORK_WEEK_END,
    						work_week_days);
    				
    	    		PROC_WORK_QTY proc_Work_Qty=null;
    	    		SH_WORK_QTY sh_Work_Qty=null;
    	    		
    				if(!ls_PROC_WORK_QTY.isEmpty()) {    					
    					for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
    						proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
    						if(proc_Work_Qty.getFA_NO().equals(getFA_NO())&& 
    						   proc_Work_Qty.getPROCID().equals(PB_PTNO[0])&&
    						   proc_Work_Qty.getWORK_WEEK()==WORK_WEEK_END) {
    							break;
    						}
    					}    	    			
    				}
    				
    				sh_Work_Qty=getSH_WORK_QTY(SH_NO,PB_PTNO[0], WORK_WEEK_END,ls_SH_CAP_QTY);
    				if(sh_Work_Qty==null) {
    					System.out.println("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
    					CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
    					message.setSH_NO(SH_NO);
    					message.setERROR("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
    					setMessage(message);
    					return iRet;
    				}
    				
				    String strSQL="select SH_SIZE,sum(OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY) WORK_PLAN_QTY "+
                                  " from FCMPS010 "+
                                  " where to_char(FCMPS010.OD_FGDATE, 'IYIW') >= '"+OD_FGDATE_WEEK+"'"+  
                                  "   and to_char(FCMPS010.OD_FGDATE, 'IYIW') <= '"+OD_FGDATE_WEEK_END+"'"+ 
                                  "   and FCMPS010.SH_NO='"+SH_NO+"'"+
                                  "   and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+	
                                  "   and FCMPS010.PROCID='"+PB_PTNO[0]+"'"+
	                              "   and OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY>0 "+                            
	                              "   and nvl(OD_CODE,'N')='N' "+
	                              "   and IS_DISABLE='N' "+					                   
	                              "   and FA_NO='"+getFA_NO()+"' "+
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
        				
        				if(WORK_PLAN_QTY==0) continue;
        				
        				if(NEED_SHOOT) {
        					if(!doInitSize(
        							STYLE_NO, 
        							SH_NO, 
        							SH_COLOR, 
        							SH_SIZE,
        							PB_PTNO[0], 
        							WORK_WEEK_END, 
        							work_week_days,
        							ls_SH_KEY_SIZE, 
        							ls_SH_SIZE_CAP, 
        							ls_Share_Style)) {
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
			    				ls_SH_BY_WEEKS,
			    				ls_SH_KEY_SIZE,
			    				ls_SH_COLOR_SIZE,
			    				ls_SH_COLOR_ALLOW_COUNT,
			    				ls_Share_Style,
			    				NEED_SHOOT,
			    				USE_CAP
			    				);
        				if(planed_qty==-1) {
        	    			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
        	    			message.setSH_COLOR(SH_COLOR);
        	    			message.setSH_NO(SH_NO);
        	    			message.setSH_SIZE(SH_SIZE);
        	    			message.setSTYLE_NO(STYLE_NO);
        	    			message.setOD_FGDATE_WEEK(Integer.valueOf(OD_FGDATE_WEEK));
        	    			message.setERROR("計算制程:"+PB_PTNO[1]+" 的排產周次時發生錯誤!");
        	    			setMessage(message);
        	    			System.out.println("計算制程:"+PB_PTNO[1]+" 的排產周次時發生錯誤!");
        					break;
        				
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
        	    			
        	    			this.doInitSH_CAP(SH_NO,PB_PTNO[0], WORK_WEEK_END, NEED_SHOOT, ls_SH_CAP_QTY, work_week_days);
        	    			
            				this.doInitPROC_CAP(
            						SH_NO, 
            						SH_COLOR,
            						PB_PTNO[0], 
            						PB_PTNO[1], 
            						WORK_WEEK_END,
            						work_week_days);
            	    		
            				if(!ls_PROC_WORK_QTY.isEmpty()) {    					
            					for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
            						proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
            						if(proc_Work_Qty.getFA_NO().equals(getFA_NO())&& 
            						   proc_Work_Qty.getPROCID().equals(PB_PTNO[0])&&
            						   proc_Work_Qty.getWORK_WEEK()==WORK_WEEK_END) {
            							break;
            						}
            					}    	    			
            				}
            				
            				sh_Work_Qty=getSH_WORK_QTY(SH_NO,PB_PTNO[0], WORK_WEEK_END, ls_SH_CAP_QTY);
            				if(sh_Work_Qty==null) {
            					System.out.println("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
            					CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
            					message.setSH_NO(SH_NO);
            					message.setERROR("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
            					setMessage(message);
            					return iRet;
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
        	    			
        	    			this.doInitSH_CAP(SH_NO,PB_PTNO[0], WORK_WEEK_END, NEED_SHOOT, ls_SH_CAP_QTY, work_week_days);
        	    			
            				this.doInitPROC_CAP(
            						SH_NO, 
            						SH_COLOR,
            						PB_PTNO[0], 
            						PB_PTNO[1], 
            						WORK_WEEK_END,
            						work_week_days);
            	    		
            				if(!ls_PROC_WORK_QTY.isEmpty()) {    					
            					for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
            						proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
            						if(proc_Work_Qty.getFA_NO().equals(getFA_NO())&& 
            						   proc_Work_Qty.getPROCID().equals(PB_PTNO[0])&&
            						   proc_Work_Qty.getWORK_WEEK()==WORK_WEEK_END) {
            							break;
            						}
            					}    	    			
            				}
            				
            				sh_Work_Qty=getSH_WORK_QTY(SH_NO,PB_PTNO[0], WORK_WEEK_END, ls_SH_CAP_QTY);
            				if(sh_Work_Qty==null) {
            					System.out.println("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
            					CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
            					message.setSH_NO(SH_NO);
            					message.setERROR("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
            					setMessage(message);
            					return iRet;
            				}
            				
            				iRow=0;
            				continue;
        	    		}	        				
        				
			       }				    	
                    
                   transaction.commit();
                   
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
    	    			
    	    			this.doInitSH_CAP(SH_NO,PB_PTNO[0], WORK_WEEK_END, NEED_SHOOT, ls_SH_CAP_QTY, work_week_days);
    	    			
        				this.doInitPROC_CAP(
        						SH_NO, 
        						SH_COLOR,
        						PB_PTNO[0], 
        						PB_PTNO[1], 
        						WORK_WEEK_END,
        						work_week_days);        				
        	    		
        				if(!ls_PROC_WORK_QTY.isEmpty()) {    					
        					for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
        						proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
        						if(proc_Work_Qty.getFA_NO().equals(getFA_NO())&& 
        						   proc_Work_Qty.getPROCID().equals(PB_PTNO[0])&&
        						   proc_Work_Qty.getWORK_WEEK()==WORK_WEEK_END) {
        							break;
        						}
        					}    	    			
        				}
        				
        				sh_Work_Qty=getSH_WORK_QTY(SH_NO,PB_PTNO[0], WORK_WEEK_END, ls_SH_CAP_QTY);
        				if(sh_Work_Qty==null) {
        					System.out.println("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
        					CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
        					message.setSH_NO(SH_NO);
        					message.setERROR("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
        					setMessage(message);
        					return iRet;
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
				    			ls_PROC_WORK_QTY, 
				    			ls_SH_CAP_QTY,
				    			ls_SH_BY_WEEKS,
				    			ls_SH_KEY_SIZE,
				    			ls_SH_SIZE_CAP,
				    			ls_SH_COLOR_SIZE,
				    			ls_SH_COLOR_ALLOW_COUNT,
				    			ls_PROC_SEQ,
				    			ls_SH_NEED_PLAN_PROC,
				    			ls_Share_Style,
				    			NEED_SHOOT,
				    			USE_CAP);	
        				
    	    		}           
                    
                    ls_FCMPS010.clear();
    			}	
    			
    			
    		}
    		
		    iRet=true;
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
			message.setSH_COLOR(SH_COLOR);
			message.setSH_NO(SH_NO);
			message.setSTYLE_NO(STYLE_NO);
			message.setOD_FGDATE_WEEK(Integer.valueOf(OD_FGDATE_WEEK));
			message.setERROR(sqlex.getMessage());
			setMessage(message);
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}
		
		
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
			ArrayList<PROC_WORK_QTY> ls_PROC_WORK_QTY,
			Map<String,ArrayList<SH_WORK_QTY>> ls_SH_CAP_QTY,
			Map<String,Integer> ls_SH_BY_WEEKS,
			Map<String,SH_KEY_SIZE> ls_SH_KEY_SIZE,
			Map<String,ArrayList<SH_KEY_SIZE>> ls_SH_SIZE_CAP,
			Map<String,ArrayList<SH_COLOR_SIZE>> ls_SH_COLOR_SIZE,
			Map<String,Integer> ls_SH_COLOR_ALLOW_COUNT,
			ArrayList<Double> ls_PROC_SEQ,
			Map<String,ArrayList<String[]>> ls_SH_NEED_PLAN_PROC,
			Map<String,ArrayList<String[]>> ls_Share_Style,
			boolean NEED_SHOOT,
			String USE_CAP
			) {
		boolean iRet=false;

		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try {
			
			transaction.begin();

			double work_week_days=FCMPS_PUBLIC.getSH_WorkDaysOfWeek(
					getFA_NO(),
					SH_NO, 
					WORK_WEEK_END, 
					getConnection());
			
			this.doInitSH_CAP(SH_NO,PROCID, WORK_WEEK_END, NEED_SHOOT, ls_SH_CAP_QTY, work_week_days);
			
			this.doInitPROC_CAP(
					SH_NO, 
					SH_COLOR,
					PROCID, 
					PB_PTNA, 
					WORK_WEEK_END,
					work_week_days);

			PROC_WORK_QTY proc_Work_Qty=null;
			SH_WORK_QTY sh_Work_Qty=null;
			
			if(!ls_PROC_WORK_QTY.isEmpty()) {    					
				for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
					proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
					if(proc_Work_Qty.getFA_NO().equals(getFA_NO())&& 
					   proc_Work_Qty.getPROCID().equals(PROCID)&&
					   proc_Work_Qty.getWORK_WEEK()==WORK_WEEK_END) {
						break;
					}
				}    	    			
			}
			
			sh_Work_Qty=getSH_WORK_QTY(SH_NO,PROCID, WORK_WEEK_END,ls_SH_CAP_QTY);
			if(sh_Work_Qty==null) {
				System.out.println("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
				CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
				message.setSH_NO(SH_NO);
				message.setERROR("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
				setMessage(message);
				return iRet;
			}
			
			String strSQL="select SH_SIZE,sum(OD_QTY-nvl(WORK_PLAN_QTY,0)- EXPECT_PLAN_QTY) WORK_PLAN_QTY "+
	           "from FCMPS010 "+
	           "where to_char(FCMPS010.OD_FGDATE,'IYIW')>='"+OD_FGDATE_WEEK+"'"+
	           "  and to_char(FCMPS010.OD_FGDATE,'IYIW')<='"+OD_FGDATE_WEEK_END+"'"+
	           "  and FCMPS010.SH_NO='"+SH_NO+"'"+
	           "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
	           "  and FCMPS010.PROCID='"+PROCID+"'"+	           
	           "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+ 				                   
	           "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
	           "  and FCMPS010.IS_DISABLE='N' "+
	           "  and FCMPS010.FA_NO='"+getFA_NO()+"' "+
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
				
				if(WORK_PLAN_QTY==0) continue;
				
				if(NEED_SHOOT) {
					if(!doInitSize(
							STYLE_NO, 
							SH_NO, 
							SH_COLOR, 
							SH_SIZE,
							PROCID, 
							WORK_WEEK_END, 
							work_week_days,
							ls_SH_KEY_SIZE, 
							ls_SH_SIZE_CAP, 
							ls_Share_Style)) {
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
	    				ls_SH_BY_WEEKS,
	    				ls_SH_KEY_SIZE,
	    				ls_SH_COLOR_SIZE,
	    				ls_SH_COLOR_ALLOW_COUNT,
	    				ls_Share_Style,
	    				NEED_SHOOT,
	    				USE_CAP
	    				);
				if(planed_qty==-1) {
	    			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
	    			message.setSH_COLOR(SH_COLOR);
	    			message.setSH_NO(SH_NO);
	    			message.setSH_SIZE(SH_SIZE);
	    			message.setSTYLE_NO(STYLE_NO);
	    			message.setOD_FGDATE_WEEK(Integer.valueOf(OD_FGDATE_WEEK));
	    			message.setERROR("計算制程:"+PB_PTNA+" 的排產周次時發生錯誤!");
	    			setMessage(message);
	    			
					break;
				
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
	    			
	    			this.doInitSH_CAP(SH_NO,PROCID, WORK_WEEK_END, NEED_SHOOT, ls_SH_CAP_QTY, work_week_days);
	    			
					this.doInitPROC_CAP(
							SH_NO, 
							SH_COLOR,
							PROCID, 
							PB_PTNA, 
							WORK_WEEK_END,
							work_week_days);
		    		
					if(!ls_PROC_WORK_QTY.isEmpty()) {    					
						for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
							proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
							if(proc_Work_Qty.getFA_NO().equals(getFA_NO())&& 
							   proc_Work_Qty.getPROCID().equals(PROCID)&&
							   proc_Work_Qty.getWORK_WEEK()==WORK_WEEK_END) {
								break;
							}
						}    	    			
					}
					
    				sh_Work_Qty=getSH_WORK_QTY(SH_NO,PROCID, WORK_WEEK_END, ls_SH_CAP_QTY);
    				if(sh_Work_Qty==null) {
    					System.out.println("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
    					CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
    					message.setSH_NO(SH_NO);
    					message.setERROR("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
    					setMessage(message);
    					return iRet;
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
	    			
	    			this.doInitSH_CAP(SH_NO,PROCID, WORK_WEEK_END, NEED_SHOOT, ls_SH_CAP_QTY, work_week_days);
	    			
					this.doInitPROC_CAP(
							SH_NO, 
							SH_COLOR,
							PROCID, 
							PB_PTNA, 
							WORK_WEEK_END,
							work_week_days);
		    		
					if(!ls_PROC_WORK_QTY.isEmpty()) {    					
						for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
							proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
							if(proc_Work_Qty.getFA_NO().equals(getFA_NO())&& 
							   proc_Work_Qty.getPROCID().equals(PROCID)&&
							   proc_Work_Qty.getWORK_WEEK()==WORK_WEEK_END) {
								break;
							}
						}    	    			
					}
					
    				sh_Work_Qty=getSH_WORK_QTY(SH_NO,PROCID, WORK_WEEK_END,ls_SH_CAP_QTY);
    				if(sh_Work_Qty==null) {
    					System.out.println("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
    					CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
    					message.setSH_NO(SH_NO);
    					message.setERROR("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
    					setMessage(message);
    					return iRet;
    				}
					
					iRow=0;
					continue;
	    		}	        				
				
	       }				    	
	        
	       transaction.commit();
	        
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
    			
    			this.doInitSH_CAP(SH_NO,PROCID, WORK_WEEK_END, NEED_SHOOT, ls_SH_CAP_QTY, work_week_days);
    			
				this.doInitPROC_CAP(
						SH_NO, 
						SH_COLOR,
						PROCID, 
						PB_PTNA, 
						WORK_WEEK_END,
						work_week_days);
	    		
				if(!ls_PROC_WORK_QTY.isEmpty()) {    					
					for(int i=0;i<ls_PROC_WORK_QTY.size();i++) {
						proc_Work_Qty=ls_PROC_WORK_QTY.get(i);
						if(proc_Work_Qty.getFA_NO().equals(getFA_NO())&& 
						   proc_Work_Qty.getPROCID().equals(PROCID)&&
						   proc_Work_Qty.getWORK_WEEK()==WORK_WEEK_END) {
							break;
						}
					}    	    			
				}
				
				sh_Work_Qty=getSH_WORK_QTY(SH_NO,PROCID, WORK_WEEK_END,ls_SH_CAP_QTY);
				if(sh_Work_Qty==null) {
					System.out.println("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
					CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
					message.setSH_NO(SH_NO);
					message.setERROR("型體:"+SH_NO+" "+WORK_WEEK_END+"周沒有設定型體產能!");
					setMessage(message);
					return iRet;
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
		    			ls_PROC_WORK_QTY, 
		    			ls_SH_CAP_QTY,
		    			ls_SH_BY_WEEKS,
		    			ls_SH_KEY_SIZE,
		    			ls_SH_SIZE_CAP,
		    			ls_SH_COLOR_SIZE,
		    			ls_SH_COLOR_ALLOW_COUNT,
		    			ls_PROC_SEQ,
		    			ls_SH_NEED_PLAN_PROC,
		    			ls_Share_Style,
		    			NEED_SHOOT,
		    			USE_CAP);	
		    	
    		}
	        	        
	        ls_FCMPS010.clear();
	        
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
			message.setSH_COLOR(SH_COLOR);
			message.setSH_NO(SH_NO);
			message.setSTYLE_NO(STYLE_NO);
			message.setOD_FGDATE_WEEK(Integer.valueOf(OD_FGDATE_WEEK));
			message.setERROR(sqlex.getMessage());
			setMessage(message);
		}

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
	private boolean doInitPROC_CAP(
			String SH_NO,
			String SH_COLOR,
			String PROCID,
			String PB_PTNA,
			int WORK_WEEK_END,
			double Week_Plan_Days) {
		    boolean iRet=false;
		 
	        boolean isExist=false;
	        
			PROC_WORK_QTY proc_Work_Qty=null;

			
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
				
				ls_PROC_WORK_QTY.add(proc_Work_Qty);
			}

			//有些型體制程有設定要排計劃, 卻沒有設定產能,故退出
			if(proc_Work_Qty.getWORK_CAP_QTY()==0) {
				CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
				message.setSH_COLOR(SH_COLOR);
				message.setSH_NO(SH_NO);
				message.setERROR("制程:"+PB_PTNA+" 沒有設定周產能!");
				setMessage(message);

				return iRet;
			}    	    		
			
		 
		 
		 return iRet;
	}

	private SH_WORK_QTY getSH_WORK_QTY(
			String SH_NO,
			String PROCID,
			int WORK_WEEK_END,
			Map<String,ArrayList<SH_WORK_QTY>> ls_SH_CAP_QTY) {
		    
			SH_WORK_QTY sh_cap_Qty=null;
			
			ArrayList<SH_WORK_QTY> al_SH_CAP_QTY=ls_SH_CAP_QTY.get(getFA_NO()+SH_NO);	 
						
			for(int i=0;i<al_SH_CAP_QTY.size();i++) {
				sh_cap_Qty=al_SH_CAP_QTY.get(i);
				if(sh_cap_Qty.getFA_NO().equals(getFA_NO()) &&
				   sh_cap_Qty.getSH_NO().equals(SH_NO) &&
				   sh_cap_Qty.getPROCID().equals(PROCID) &&
				   sh_cap_Qty.getWORK_WEEK()==WORK_WEEK_END) {
					break;
				}
			} 	
	    		    
		    return sh_cap_Qty;
	}
	
	/**
	 * 初始各周型體產能
	 * @param SH_NO
	 * @param WORK_WEEK_END
	 * @param NEED_SHOOT
	 * @param ls_SH_CAP_QTY
	 * @return
	 */
	private boolean doInitSH_CAP(
			String SH_NO,
			String PROCID,
			int WORK_WEEK_END,
			boolean NEED_SHOOT,
			Map<String,ArrayList<SH_WORK_QTY>> ls_SH_CAP_QTY,
			double week_plan_days) {
		
		    boolean iRet=false;

			SH_WORK_QTY sh_cap_Qty=null;  	    		
			
			try {
				ArrayList<SH_WORK_QTY> al_SH_CAP_QTY=ls_SH_CAP_QTY.get(getFA_NO()+SH_NO);	 
				if(al_SH_CAP_QTY==null) {
					al_SH_CAP_QTY=new ArrayList<SH_WORK_QTY>();
					ls_SH_CAP_QTY.put(getFA_NO()+SH_NO, al_SH_CAP_QTY);
					
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
			
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		 
		 
		 return iRet;
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
			double Week_Plan_Days,
			Map<String,SH_KEY_SIZE> ls_SH_KEY_SIZE,
			Map<String,ArrayList<SH_KEY_SIZE>> ls_SH_SIZE_CAP,
			Map<String,ArrayList<String[]>> ls_Share_Style) {

        boolean iRet=false;
        

        boolean isExist=false;
        		
		ArrayList<String[]> ls_STYLE=null;
		
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
			ls_STYLE=getMD_Style_Share(getFA_NO(),STYLE_NO, SH_NO, SH_SIZE, ls_Share_Style,WORK_WEEK_END);    					    					
			SHARE_SIZE=ls_Share_Style.keySet().toArray(new String[0]);
//			Arrays.sort(SHARE_SIZE);       				
		}	
		
		if(ls_STYLE==null) {
			System.out.println("型體:"+SH_NO+" SIZE:"+SH_SIZE+" 排產周次:"+WORK_WEEK_END+" 沒有模具資料!");
			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
			message.setSH_COLOR(SH_COLOR);
			message.setSH_NO(SH_NO);
			message.setSH_SIZE(SH_SIZE);
			message.setSTYLE_NO(STYLE_NO);
			message.setOD_FGDATE_WEEK(WORK_WEEK_END);
			message.setERROR("型體:"+SH_NO+" SIZE:"+SH_SIZE+" 排產周次:"+WORK_WEEK_END+" 沒有模具資料!");
			setMessage(message);
			
			return iRet;
		}
		
		
		String share_SH_NO="";
		String share_SH_SIZE="";
		
		SH_KEY_SIZE sh_key_size=null;
		SH_KEY_SIZE sh_size_cap=null;
		
		isExist=false;
		
		ArrayList<SH_KEY_SIZE> al_SH_SIZE_CAP=ls_SH_SIZE_CAP.get(getFA_NO()+PROCID+SH_NO+SH_SIZE);
		if(al_SH_SIZE_CAP==null) {
			al_SH_SIZE_CAP=new ArrayList<SH_KEY_SIZE>();
			ls_SH_SIZE_CAP.put(getFA_NO()+PROCID+SH_NO+SH_SIZE, al_SH_SIZE_CAP);
		}
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
			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
			message.setSH_COLOR(SH_COLOR);
			message.setSH_NO(SH_NO);
			message.setSH_SIZE(SH_SIZE);
			message.setSTYLE_NO(STYLE_NO);
			message.setERROR("型體:"+SH_NO+" SIZE:"+SH_SIZE+" 沒有建立模具資料!");
			setMessage(message);
        	return iRet;
        }
        
		if(sh_size_cap.getWORK_CAP_QTY()==0) {
			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
			message.setSH_COLOR(SH_COLOR);
			message.setSH_NO(SH_NO);
			message.setSH_SIZE(SH_SIZE);
			message.setSTYLE_NO(STYLE_NO);
			message.setERROR("型體:"+SH_NO+" SIZE:"+SH_SIZE+" 沒有建立模具資料!");
			setMessage(message);
			return iRet;		    			
		}
				    			    		
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
			Map<String,Integer> ls_SH_BY_WEEKS,
			Map<String,SH_KEY_SIZE> ls_SH_KEY_SIZE,
			Map<String,ArrayList<SH_COLOR_SIZE>> ls_SH_COLOR_SIZE,
			Map<String,Integer> ls_SH_COLOR_ALLOW_COUNT,
			Map<String,ArrayList<String[]>> ls_Share_Style,
			boolean NEED_SHOOT,
			String USE_CAP
			) {
		
		double iRet=-1;						

		if(WORK_PLAN_QTY==0) return WORK_PLAN_QTY;
		
		//控制配色個數
		Integer color_allow_count=ls_SH_COLOR_ALLOW_COUNT.get(SH_NO);
		
		if(color_allow_count!=0) {//0表示不限配色個數
			if(!ls_SH_COLOR_SIZE.isEmpty()) {
				int color_count=0;
				boolean color_exist=false;
				Iterator<String> it_SH_COLOR=ls_SH_COLOR_SIZE.keySet().iterator();
				while(it_SH_COLOR.hasNext()) {
					String key=it_SH_COLOR.next();
//					if(key.indexOf(SH_NO)!=-1) {//getFA_NO()+PROCID+OD_FGDATE_WEEK+SH_NO
					if(key.indexOf(SH_NO)!=-1) {//getFA_NO()+PROCID+OD_FGDATE_WEEK+SH_NO
						ArrayList<SH_COLOR_SIZE> al_SH_COLOR_SIZE =ls_SH_COLOR_SIZE.get(key);
			            if(!al_SH_COLOR_SIZE.isEmpty()) {
			            	for(SH_COLOR_SIZE sh_color_size:al_SH_COLOR_SIZE) {
			            		if(sh_color_size.getFA_NO().equals(getFA_NO())&&
			    	               sh_color_size.getPROCID().equals(PROCID) &&
			    	               sh_color_size.getSH_NO().equals(SH_NO) &&
			    	               sh_color_size.getWORK_WEEK()==WORK_WEEK_END) {
			            			color_count++;
			            			if(sh_color_size.getSH_COLOR().equals(SH_COLOR)) {
			            				color_exist=true;
			            				break;
			            			}
			    	            }
			            	}
			            }
					}
					if(color_exist) break;
				}
				
				//配色沒有排過,此已排配色大於允許的個數
				if(color_allow_count<=color_count && color_exist==false) return 0;
				
			}			
		}
										
		String share_SH_NO="";
		String share_SH_SIZE="";
		
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;
		
		try {
			//開始計算

    		SH_KEY_SIZE sh_key_size=null;
    		    		
    		ArrayList<String[]> ls_STYLE=null;
    		
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
					ls_STYLE=getMD_Style_Share(getFA_NO(),STYLE_NO, SH_NO, SH_SIZE, ls_Share_Style,WORK_WEEK_END);    					    					
    				SHARE_SIZE=ls_Share_Style.keySet().toArray(new String[0]);     				
				}	
				
				if(ls_STYLE==null) {
					System.out.println("型體:"+STYLE_NO+" "+SH_NO+"顏色:"+SH_COLOR+" 沒有找到共模關系");
					return 0;	
				}			
	    		
	    		//再查找是否已記錄此型體本周的產能和排產數, 沒有則增加進來
	            if(!ls_STYLE.isEmpty()) {
		    		share_SH_NO=ls_STYLE.get(0)[0];
		    		share_SH_SIZE=ls_STYLE.get(0)[1];
		    		
	            	sh_key_size=ls_SH_KEY_SIZE.get(getFA_NO()+PROCID+share_SH_NO+share_SH_SIZE+WORK_WEEK_END);
	            }else {
	            	sh_key_size=ls_SH_KEY_SIZE.get(getFA_NO()+PROCID+SH_NO+SH_SIZE+WORK_WEEK_END);		            	
	            }
	            
	            if(sh_key_size==null) {
	            	System.out.println(sh_key_size);
	            }
	            
	    		//如果已達到size的模具產能, 則往前排一周
	    		if(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY()<=0) {
	    			return 0;
	    		}
	    		
			}
    			    			    	    
    		double RESIDUE_QTY=proc_Work_Qty.getWORK_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY(); //餘數
    		
    		if(RESIDUE_QTY>(sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY())) 
    			RESIDUE_QTY=sh_Work_Qty.getWORK_CAP_QTY()-sh_Work_Qty.getWORK_PLANNED_QTY();

    		if(NEED_SHOOT) {
	    		if(RESIDUE_QTY>(sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY())) 
	    			RESIDUE_QTY=sh_key_size.getWORK_CAP_QTY()-sh_key_size.getWORK_PLANNED_QTY();
    		}

    		if(RESIDUE_QTY>=WORK_PLAN_QTY) RESIDUE_QTY=WORK_PLAN_QTY;

//    		if(is_Compel) {				
//				if(proc_Work_Qty.getWORK_MAX_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY()<=WORK_PLAN_QTY) {
//					RESIDUE_QTY=proc_Work_Qty.getWORK_MAX_CAP_QTY()-proc_Work_Qty.getWORK_PLANNED_QTY(); 
//				}else {
//					RESIDUE_QTY=WORK_PLAN_QTY; 
//				}    			   	
//    		}
    		
    		if(RESIDUE_QTY==0) return RESIDUE_QTY;	

			String strSQL="select " +
	                      "FCMPS010.OD_PONO1, "+
                          "FCMPS010.SH_SIZE,"+
                          "FCMPS010.OD_QTY,"+
                          "FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)- nvl(EXPECT_PLAN_QTY,0) WORK_PLAN_QTY "+
                          "from FCMPS010 "+
                          "where to_char(FCMPS010.OD_FGDATE,'IYIW')>='"+OD_FGDATE_WEEK+"'"+
                          "  and to_char(FCMPS010.OD_FGDATE,'IYIW')<='"+OD_FGDATE_WEEK_END+"'"+
                          "  and FCMPS010.SH_NO='"+SH_NO+"'"+
                          "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
                          "  and FCMPS010.SH_SIZE='"+SH_SIZE+"'"+
                          "  and FCMPS010.PROCID='"+PROCID+"'"+                          
                          "  and FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)-nvl(EXPECT_PLAN_QTY,0)>0 "+ 				                   
                          "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
                          "  and FCMPS010.IS_DISABLE='N' "+
                          "  and FCMPS010.FA_NO='"+getFA_NO()+"' "+
                          "order by OD_FGDATE DESC";
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();		  
		    
		    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		    rs.setFetchSize(3000);	
		    
		    List<FCMPS010_BEAN> ls_FCMPS010=new ArrayList<FCMPS010_BEAN>();
		    
		    if(rs.next()) {			    	 
			    double curOD_QTY=0;
			    
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
			
    		strSQL="select OD_PONO1,FA_NO,SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS021 " +
    			   "where WORK_WEEK='"+WORK_WEEK_END+"'"+    				      
    			   "  and (OD_PONO1,FA_NO,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN ("+subSQL+")";
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
					           "where WORK_WEEK='"+WORK_WEEK_END+"'"+			           
					           "  and OD_PONO1='"+data.getOD_PONO1()+"'"+
					           "  and SH_NO='"+SH_NO+"'"+
					           "  and SH_SIZE='"+SH_SIZE+"'"+
					           "  and SH_COLOR='"+SH_COLOR+"'"+
					           "  and PROCID='"+PROCID+"'";
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
    		    		
			if(ls_FCMPS010.size()>0) {
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
		    	
			}

    		//更新訂單的預排數量
    		strSQL="update FCMPS010 " +
    			   "set EXPECT_PLAN_QTY=(select nvl(sum(work_plan_Qty),0) from FCMPS021 " +
    			   "  where PROCID=FCMPS010.PROCID" +
    			   "    and OD_PONO1=FCMPS010.OD_PONO1" +    			   
    			   "    and SH_NO=FCMPS010.SH_NO" +
    			   "    and SH_COLOR=FCMPS010.SH_COLOR" +
    			   "    and SH_SIZE=FCMPS010.SH_SIZE" +
    			   "    and FA_NO=FCMPS010.FA_NO " +
    			   ") where (OD_PONO1,FA_NO,SH_NO,SH_COLOR,SH_SIZE,PROCID) IN ("+subSQL+")";
    		
			pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    pstmtData2.executeQuery();	
		    pstmtData2.close();		
		    
    		if(USE_CAP.equals("Y")) { //占用產能
    			sh_Work_Qty.setWORK_PLANNED_QTY(sh_Work_Qty.getWORK_PLANNED_QTY()+RESIDUE_QTY);
    			proc_Work_Qty.setWORK_PLANNED_QTY(proc_Work_Qty.getWORK_PLANNED_QTY()+RESIDUE_QTY);	
    				    			
        		if(NEED_SHOOT) {
        			sh_key_size.setWORK_PLANNED_QTY(sh_key_size.getWORK_PLANNED_QTY()+RESIDUE_QTY);
        		}
    		}

			WORK_PLAN_QTY=WORK_PLAN_QTY-RESIDUE_QTY;
			
			//記錄本周型體配色排了多少數量
			if(NEED_SHOOT) {
				
				ArrayList<SH_COLOR_SIZE> al_SH_COLOR_SIZE=ls_SH_COLOR_SIZE.get(getFA_NO()+PROCID+WORK_WEEK_END+SH_NO+SH_COLOR);
	            if(al_SH_COLOR_SIZE==null) {
	            	al_SH_COLOR_SIZE=new ArrayList<SH_COLOR_SIZE>();
	            	ls_SH_COLOR_SIZE.put(getFA_NO()+PROCID+WORK_WEEK_END+SH_NO+SH_COLOR, al_SH_COLOR_SIZE);
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
			
			iRet=RESIDUE_QTY;			
			
		}catch(Exception ex) {
			ex.printStackTrace();
			System.out.println(" 型體:"+SH_NO+" 顏色:"+SH_COLOR+" Size:"+SH_SIZE+" 周次:"+WORK_WEEK_END+" 出錯!");
			CLS_RCCP_ERROR message=new CLS_RCCP_ERROR();
			message.setSH_COLOR(SH_COLOR);
			message.setSH_NO(SH_NO);
			message.setSH_SIZE(SH_SIZE);
			message.setSTYLE_NO(STYLE_NO);
			message.setOD_FGDATE_WEEK(WORK_WEEK_END);
			message.setERROR(ex.getMessage());
			setMessage(message);	
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
	private ArrayList<String[]> getNeed_Plan_PROC(String SH_NO,double PROC_SEQ) {
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select fcps22_1.PB_PTNO,fcps22_2.PB_PTNA,nvl(fcps22_1.IS_USE_CAP,'N') IS_USE_CAP " +
				   "from fcps22_1,fcps22_2 " +
				   "where fcps22_1.PB_PTNO=fcps22_2.PB_PTNO(+)" +
				   "  and fcps22_1.SH_ARITCLE='"+SH_NO+"' " +
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
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
		
	private ArrayList<String[]> getMD_Style_Share(
			String FA_NO,
			String STYLE_NO,
			String SH_NO,
			String SH_SIZE,
			Map<String,ArrayList<String[]>> ls_Share_Style,
			int WORK_WEEK) {
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
		    			ls_Share_Style.put(getFA_NO()+STYLE_NO+SH_NO+SH_SIZE+EFFECTIVE_WEEK,ls_STYLE);
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
	
	private SH_KEY_SIZE getMD_Min_Week_Cap_QTY(
			String FA_NO,
			String SH_NO,
			String SH_SIZE,
			String PROCID,
			ArrayList<SH_KEY_SIZE> ls_SH_SIZE_CAP,
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
                   (SH_NO.length>0?" and SH_NO='"+SH_NO[0]+"' ":"")+
//                   "  and STYLE_NO in ('16013','15991','14461') "+
//                   "   and SH_NO='CBDVADERLNDCLG'"+
                   "   and FA_NO='"+getFA_NO()+"' ";
			
		    //後一周已排的型體
		    if(Style==1) {
		    	strSQL="select * from ("+strSQL+") where (SH_NO) IN " +
                              "       (SELECT SH_NO FROM FCMPS010 " +
                              "         WHERE to_char(FCMPS010.OD_FGDATE, 'IYIW') = '"+PRE_OD_FGDATE_WEEK+"'" +
                              "           and FA_NO='"+getFA_NO()+"') ";
		    }
		    
		    //優先排產的型體
		    if(Style==2) {
		    	strSQL="select * from ("+strSQL+") where (SH_NO) IN " +
	    	                  "       (select SH_NO from fcmps018 where FA_NO='"+getFA_NO()+"' "+
	                          "        UNION ALL "+
	                          "        select SH_NO from fcmps019 WHERE FA_NO='"+getFA_NO()+"' and WORK_WEEK="+OD_FGDATE_WEEK+") ";
		    }
		    		    		    
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		String SH_NO2=rs.getString("SH_NO");
		    		
		    		strSQL="select ALLOW_MOVE_UP_WEEK from fcmps022 where FA_NO='"+FA_NO+"' and SH_NO='"+SH_NO2+"'";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    
				    if(rs2.next()){
				    	ls_SH_BY_WEEKS.put(SH_NO2, rs2.getInt("ALLOW_MOVE_UP_WEEK"));
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
	
}
