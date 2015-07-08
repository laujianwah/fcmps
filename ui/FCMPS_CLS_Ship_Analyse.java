package fcmps.ui;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.hibernate.cfg.Configuration;

import fcmps.domain.FCMPS010_BEAN;


/**
 * 訂單出貨分析<br>
 * 目的:只用於周計劃系統消耗AOH<br>
 * FTI 目前每年大約在 4月和10月 會盤點兩次,盤點的成品庫存會利用後續的訂單消耗掉<br>
 * 按交期先後排序訂單,用成品庫存數逐一扣減訂單數量,滿足訂單量的,則訂單可以不排入周計劃<br>
 * 並且記錄此訂單抵銷的數量<br>
 * @author dev17
 *
 */
public class FCMPS_CLS_Ship_Analyse extends TestCase{
    private String FA_NO="FIC";
    private Connection conn=null;
    private String config_xml="";
    
    public void test_Analyse() {
    	conn=getConnectionFTI();
    	doAnalyse();
    }
    
	/**
	 * 設定廠別
	 * @param fa_no
	 */
	public void setFA_NO(String fa_no) {
		FA_NO = fa_no;
	}

	/**
	 * 取資料庫連線
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

	public String getConfig_XML() {
		return config_xml;
	}

	public void setConfig_XML(String config_xml) {
		this.config_xml = config_xml;
	}
	
	public boolean doAnalyse() {
		boolean iRet=false;
		String strSQL="";
		Connection conn=getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
						
		try {

			strSQL="select " +
					"FA_NO," +
					"SH_ARITCLE," +
					"SH_COLOR," +
					"SH_SIZE," +
					"(CASE WHEN LEAN_NO='SHANGHAI' THEN LEAN_NO ELSE '無' END) LEAN_NO," +
					"MT_QTY " +
					"from FCMPS016 " +
			        "where nvl(MT_QTY,0)>0 "+
			        "  and FA_NO='"+FA_NO+"'"+
			        "  and exists (select sh_no from fcmps010 "+
                    "               where FA_NO=FCMPS016.FA_NO " +
                    "                 and SH_NO = FCMPS016.SH_ARITCLE "+
                    "                 and SH_COLOR = FCMPS016.SH_COLOR "+
                    "                 and SH_SIZE = FCMPS016.SH_SIZE "+
                    "                 and (CASE WHEN LEAN_NO='SHANGHAI' THEN LEAN_NO ELSE '無' END)=(CASE WHEN FCMPS016.LEAN_NO='SHANGHAI' THEN FCMPS016.LEAN_NO ELSE '無' END) "+
                    "                 and rownum=1)";
			
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
			rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			rs.setFetchSize(1000);
			
			if(rs.next()){	

				ExecutorService pool=null; 
				
				List<Future<Map<FCMPS_CLS_Ship_Analyse_sub,Integer>>> resultList;
				
				pool=Executors.newFixedThreadPool(30);
		    	resultList = new ArrayList<Future<Map<FCMPS_CLS_Ship_Analyse_sub,Integer>>>();
		    	
				do {

					String FA_NO=rs.getString("FA_NO");
					String SH_NO=rs.getString("SH_ARITCLE");
					String SH_SIZE=rs.getString("SH_SIZE");
					String SH_COLOR=rs.getString("SH_COLOR");
					String LEAN_NO=rs.getString("LEAN_NO");
										
					double MT_QTY=rs.getDouble("MT_QTY");
					
					FCMPS_CLS_Ship_Analyse_sub cls_sub=new FCMPS_CLS_Ship_Analyse_sub(FA_NO,SH_NO,SH_COLOR,SH_SIZE,LEAN_NO,MT_QTY);
		    		Future<Map<FCMPS_CLS_Ship_Analyse_sub,Integer>> result=pool.submit(cls_sub);
		    		resultList.add(result);
															
				}while(rs.next());	
				
				pool.shutdown();
						    	
		    	List<FCMPS_CLS_Ship_Analyse_sub> cls_redo=new ArrayList<FCMPS_CLS_Ship_Analyse_sub>();
		    	
		    	do {
			        for (Future<Map<FCMPS_CLS_Ship_Analyse_sub,Integer>> fs : resultList) {  

			            try {  
			            	Map<FCMPS_CLS_Ship_Analyse_sub,Integer> result=fs.get();
			            	
			            	Iterator<FCMPS_CLS_Ship_Analyse_sub> it=result.keySet().iterator();
			            	while(it.hasNext()) {
			            		FCMPS_CLS_Ship_Analyse_sub key=it.next();
			            		Integer status=result.get(key);
			            		if(status==FCMPS_CLS_Ship_Analyse_sub.STATUS_CANCEL) {
			            			cls_redo.add(key);
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
			        
				    if(!cls_redo.isEmpty()) {
				    	pool.shutdownNow();
				    	pool=null;
				    	pool=Executors.newFixedThreadPool(30);
				    	for(FCMPS_CLS_Ship_Analyse_sub cls:cls_redo) {
				    		Future<Map<FCMPS_CLS_Ship_Analyse_sub,Integer>> result=pool.submit(cls);
		            		resultList.add(result);
				    	}
				    	cls_redo.clear();    			    	
				    }else {
				    	break;
				    }
		    		
		    	}while(true);

			    resultList=null;
			    
	    		System.gc();

			}
			rs.close();
			pstmtData.close();

			iRet=true;
		}catch(Exception ex) {
			ex.printStackTrace();
			try {
				if(!conn.getAutoCommit())conn.rollback();
			}catch(Exception ex2) {
				ex2.printStackTrace();
			}
		}finally {
//			closeConnection(conn);
		}
		
		return iRet;
	}
	

	private class FCMPS_CLS_Ship_Analyse_sub implements Callable{
		private String FA_NO="";
		private String SH_NO="";
		private String SH_SIZE="";
		private String SH_COLOR="";
		private String LEAN_NO="";
		private double MT_QTY;
		
	    public static final int STATUS_CANCEL=0;
	    public static final int STATUS_RUNNING=1;
	    public static final int STATUS_ERROR=2;
	    public static final int STATUS_COMPLETE=3;
	    public static final int STATUS_WAIT=4;
	    
		public FCMPS_CLS_Ship_Analyse_sub(String FA_NO,String SH_NO,String SH_COLOR,String SH_SIZE,String LEAN_NO,double MT_QTY) {
			this.FA_NO=FA_NO;
			this.SH_NO=SH_NO;
			this.SH_COLOR=SH_COLOR;
			this.SH_SIZE=SH_SIZE;
			this.LEAN_NO=LEAN_NO;
			this.MT_QTY=MT_QTY;
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
					System.out.println( "The Config file " + getConfig_XML()+" does not exist!" );
					return null;
				}
				Configuration config=new Configuration().configure(fConfig);	
//				config.addClass(FCMPS0101_BEAN.class);
				config.addClass(FCMPS010_BEAN.class);
				
				String USER=config.getProperty("connection.username");
				String URL=config.getProperty("connection.url");
				String PSW=config.getProperty("connection.password");
				String DRIVER=config.getProperty("connection.driver_class");
				
	    		Class.forName(DRIVER); //加載驅動程序
	    		conn=DriverManager.getConnection(URL,USER,PSW);
				
			}catch(Exception ex) {
//				ex.printStackTrace();					
			}
			return conn;
		}
		
		
		public Map<FCMPS_CLS_Ship_Analyse_sub,Integer> call() {
			Map<FCMPS_CLS_Ship_Analyse_sub,Integer> iRet=new HashMap<FCMPS_CLS_Ship_Analyse_sub,Integer>();
			
			Connection conn=null;
						
			try {
				conn=GenericSessionFactory();
				
				if(conn==null) {
					iRet.put(this, STATUS_CANCEL);
					return iRet;
				}
				
				Date st=new Date();
				conn.setAutoCommit(false);
				doProcess(conn);
				iRet.put(this, STATUS_COMPLETE);
				conn.commit();
				
//				System.out.println("庫存---SH_NO:"+SH_NO+" Color:"+SH_COLOR+" Size:"+SH_SIZE+" 耗時:"+(new Date().getTime()-st.getTime()));
				
			}catch(Exception ex) {
				ex.printStackTrace();
				try {
					conn.rollback();
				}catch(Exception sqlex) {
					sqlex.printStackTrace();
				}
				
				iRet.put(this, STATUS_ERROR);
			}finally {
				closeConnection(conn);
			}
			
			return iRet;
		}
		
		private synchronized boolean doProcess(Connection conn) throws Exception {
			
			boolean iRet=false;
			
			PreparedStatement pstmtData2 = null;		
			ResultSet rs2=null;
			

			//可能排過某個制程,故只能抵扣各制程數間的最小可排數
			String strSQL="select " +
               "OD_PONO1, "+
               "OD_SHIP,"+
               "OD_FGDATE,"+
               "OD_QTY-nvl(WORK_PLAN_QTY,0) WORK_PLAN_QTY "+
               "from FCMPS010 "+
               "where SH_NO='"+SH_NO+"'"+
		       "  and SH_COLOR='"+SH_COLOR+"' " +
		       "  and SH_SIZE='"+SH_SIZE+"'"+
		       "  and (CASE WHEN LEAN_NO='SHANGHAI' THEN LEAN_NO ELSE '無' END)='"+LEAN_NO+"'"+	
		       "  and FA_NO='"+FA_NO+"'"+
               "  and nvl(OD_CODE,'N')='N'"+
               "  and IS_DISABLE='N' "+
               "  and OD_FGDATE is not null "+
//               "  and OD_QTY-nvl(WORK_PLAN_QTY,0)>0 "+
               "  and not exists(select * from FCMPS013 " +//已指定周次排產的訂單不進行抵扣
               "                 where FA_NO=FCMPS010.FA_NO " +
               "                   and OD_PONO1=FCMPS010.OD_PONO1 " +
               "                   and SH_NO=FCMPS010.SH_NO " +
               "                   and SH_COLOR=FCMPS010.SH_COLOR " +
               "                   and SH_SIZE=FCMPS010.SH_SIZE) "+
               "  and PROCID=(SELECT min(PB_PTNO) FROM FCPS22_1 WHERE SH_ARITCLE=FCMPS010.SH_NO and PROC_SEQ is not null)" +           
               "Order by OD_FGDATE,OD_SHIP,OD_PONO1";
			
			pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs2=pstmtData2.executeQuery();
		    
			rs2.setFetchDirection(ResultSet.FETCH_FORWARD);
			rs2.setFetchSize(1000);
		    
			if(rs2.next()) {
				
				do {

					double WORK_PLAN_QTY=rs2.getDouble("WORK_PLAN_QTY");
					if(WORK_PLAN_QTY==0) continue;
					
					String OD_PONO1=rs2.getString("OD_PONO1");
					String OD_FGDATE=FCMPS_PUBLIC.getDate(rs2.getDate("OD_FGDATE"), "yyyy/MM/dd");
										
					if(MT_QTY>=WORK_PLAN_QTY) {//庫存能滿足訂單
						MT_QTY=MT_QTY-WORK_PLAN_QTY;
						doAnalyse(
								FA_NO, 
								OD_PONO1, 
								SH_NO, 
								SH_COLOR, 
								SH_SIZE, 
								WORK_PLAN_QTY,
								"Y",
								OD_FGDATE,
								LEAN_NO,
								conn);
					}else {								
						doAnalyse(
								FA_NO, 
								OD_PONO1, 
								SH_NO, 
								SH_COLOR, 
								SH_SIZE, 
								MT_QTY,
								"N",
								OD_FGDATE,
								LEAN_NO,
								conn);	
						MT_QTY=0;
					}
					
					if(MT_QTY==0) break;
					
				}while(rs2.next());						
			}
			rs2.close();
			pstmtData2.close();
			
			rs2=null;
			pstmtData2=null;
			
			strSQL="update FCMPS016 set MT_QTY="+MT_QTY+" " +
			       "where FA_NO='"+FA_NO+"'"+						
			       "  and SH_ARITCLE='"+SH_NO+"'"+
			       "  and SH_COLOR='"+SH_COLOR+"'"+
			       "  and SH_SIZE='"+SH_SIZE+"'"+
			       "  and LEAN_NO='"+LEAN_NO+"'";
	
			pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			pstmtData2.execute();
			pstmtData2.close();		
			
			pstmtData2=null;

			iRet=true;
			
			return iRet;
			
		}
		
		private synchronized boolean doAnalyse(
				String FA_NO,
				String OD_PONO1,
				String SH_NO,
				String SH_COLOR,
				String SH_SIZE,
				double USE_QTY,
				String IS_DISABLE,
				String OD_FGDATE,
				String LEAN_NO,
				Connection conn) throws Exception {
			boolean iRet=false;
			
			String strSQL="";
			
			PreparedStatement pstmtData3 = null;
			
			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)+"+USE_QTY+" " +
		           "where OD_PONO1='"+OD_PONO1+"' " +	           
		           "  and SH_NO='"+SH_NO+"' " +
		           "  and SH_COLOR='"+SH_COLOR+"' " +
		           "  and SH_SIZE='"+SH_SIZE+"'"+
		           "  and FA_NO='" +FA_NO+"'";

			pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			pstmtData3.execute();
			pstmtData3.close();

			if(IS_DISABLE.equals("Y")) {
				strSQL="update FCMPS010 set IS_DISABLE='Y' " +
				       "where OD_PONO1='"+OD_PONO1+"' " +			       
				       "  and SH_NO='"+SH_NO+"' " +
				       "  and SH_COLOR='"+SH_COLOR+"' " +
				       "  and SH_SIZE='"+SH_SIZE+"'"+
				       "  and FA_NO='" +FA_NO+"'"+
				       "  and OD_QTY-nvl(WORK_PLAN_QTY,0)=0";
				pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				pstmtData3.execute();
				pstmtData3.close();	

			}
			
			if(!iExist(FA_NO, OD_PONO1, SH_NO, SH_COLOR, SH_SIZE,conn)) {
				strSQL="insert into FCMPS017(FA_NO, PR_NO,OD_PONO1, SH_ARITCLE, SH_COLOR, SH_SIZE,USE_QTY,OD_FGDATE,LEAN_NO,UP_DATE,USE_TYPE) values(";
				strSQL=strSQL+"'"+FA_NO+"'";
				strSQL=strSQL+",'無'";
				strSQL=strSQL+",'"+OD_PONO1+"'";
				strSQL=strSQL+",'"+SH_NO+"'";
				strSQL=strSQL+",'"+SH_COLOR+"'";
				strSQL=strSQL+",'"+SH_SIZE+"'";
				strSQL=strSQL+","+USE_QTY;
				strSQL=strSQL+",TO_DATE('"+OD_FGDATE+"','YYYY/MM/DD')";
				strSQL=strSQL+",'"+LEAN_NO+"'";
				strSQL=strSQL+",sysdate,'ST')";
			}else {
				strSQL="update FCMPS017 set USE_QTY="+USE_QTY+",UP_DATE=sysdate " +
				       "where OD_PONO1='"+OD_PONO1+"' " +			       
				       "  and SH_ARITCLE='"+SH_NO+"' " +
				       "  and SH_COLOR='"+SH_COLOR+"' " +
				       "  and SH_SIZE='"+SH_SIZE+"'"+
				       "  and FA_NO='" +FA_NO+"'"+
				       "  and USE_TYPE='ST'";
			}
			pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			pstmtData3.execute();
			pstmtData3.close();			
			
			iRet=true;
			
			return iRet;
		}
		
		private boolean iExist(String FA_NO,String OD_PONO1,String SH_NO,String SH_COLOR,String SH_SIZE,Connection conn) {
			boolean iRet=false;
			
			String strSQL="";
//			Connection conn =getConnection();
			PreparedStatement pstmtData = null;		
			ResultSet rs=null;
			
			try{

				strSQL="select count(*) iCount from FCMPS017 " +
					   "where SH_ARITCLE='"+SH_NO+"' " +
					   "  and SH_COLOR='"+SH_COLOR+"'"+
					   "  and SH_SIZE='"+SH_SIZE+"'"+
					   "  and OD_PONO1='"+OD_PONO1+"'"+
					   "  and FA_NO='"+FA_NO+"'"+
					   "  and USE_TYPE='ST'";
					   
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
//		    	closeConnection(conn);
			}	
			
			return iRet;
		}
		
	}
	
	
	
	/**
	 * 取得FTI廠別正式數據庫連線
	 * @param COMMPANY_ID
	 * @return
	 */
	private Connection getConnectionFTI(){		
		Connection iRet=null;
		try{				

    		Class.forName("oracle.jdbc.driver.OracleDriver"); //加載驅動程序
    		String URL="jdbc:oracle:thin:@10.2.13.5:1521:ficdb02";
//    		String URL="jdbc:oracle:thin:@10.2.6.201:1521:ficdb01";
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
