package fcmps.ui;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import fcmps.domain.FCMPS010_BEAN;

/**
 * 已排訂單或是已抵扣庫存訂單交期大於用新接訂單交期, 則用新接訂單替代
 * @author dev17
 *
 */
public class FCMPS_CLS_ReplacePlannedOrder {
	private SessionFactory sessionFactory=null;
	private Session session=null; 
    private Transaction transaction=null;
    private String config_xml="";
    private Connection conn=null;
    private String FA_NO="";
    
    private static Log log = LogFactory.getLog( FCMPS_CLS_ReplacePlannedOrder.class );
    
    public FCMPS_CLS_ReplacePlannedOrder(String FA_NO,String config_xml) {
    	this.config_xml=config_xml;
    	this.FA_NO=FA_NO;

    }
    
    public boolean doReplace() {
    	boolean iRet=false;
    	
    	if(GenericSessionFactory()) {
    		if(!doReplacePlannedOrder()) {    			
    			CloseSessionFactory();
    			return iRet;
    		}
    		if(!doReplaceNeutralizedOrder()) { 
    			CloseSessionFactory();
    			return iRet;
    		}
    		CloseSessionFactory();
    	}else {
    		return iRet;
    	}
    	
    	iRet=true;
    	
    	return iRet;
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
	
	private String getConfig_XML() {
		return config_xml;
	}
	
	private void CloseSessionFactory() {
		sessionFactory.close();
	}
	
	/**
	 * 取得資料庫連線
	 * @return
	 */
	private Connection getConnection() {
		return conn;
	}
	
	/**
	 * 取得廠別
	 * @return
	 */
	private String getFA_NO() {
		return FA_NO;
	}
	
	/**
	 * 存在沒有排的訂單比已排訂單的交期早,則要替換<br>
	 * 注意為了不影響配套,沒有排的訂單,其任何制程都應該沒有排過計劃.
	 * @return
	 */
	private boolean doReplacePlannedOrder() {
		boolean iRet=true;
		
		String FCMPS010="select min(FCMPS010.OD_FGDATE) OD_FGDATE, "+
                        "       FCMPS010.SH_NO, "+
                        "       FCMPS010.SH_COLOR, "+
                        "       FCMPS010.SH_SIZE "+
                        "from FCMPS010 "+
                        "where nvl(FCMPS010.OD_CODE, 'N') = 'N' "+
                        "  and FCMPS010.IS_DISABLE = 'N' "+
                        "  and FCMPS010.OD_FGDATE is not null "+
                        "  and FCMPS010.FA_NO ='"+getFA_NO()+"' "+
 	                    "  and not exists(select * from FCMPS013 " +//已指定周次排產的訂單不進行抵扣
	                    "                 where FA_NO=FCMPS010.FA_NO " +
	                    "                   and OD_PONO1=FCMPS010.OD_PONO1 " +
	                    "                   and SH_NO=FCMPS010.SH_NO " +
	                    "                   and SH_COLOR=FCMPS010.SH_COLOR " +
	                    "                   and SH_SIZE=FCMPS010.SH_SIZE) "+	  
                        "group by FCMPS010.SH_NO,FCMPS010.SH_COLOR,FCMPS010.SH_SIZE,FCMPS010.PROCID";
		
          
		String FCMPS007="select max(fcmps010.OD_FGDATE) OD_FGDATE," +
                         "       fcmps007.SH_NO, "+
                         "       fcmps007.SH_COLOR, "+
                         "       fcmps007.SH_SIZE "+
	 		             "from fcmps006,fcmps007,fcmps010 "+
                         "where fcmps006.plan_no=fcmps007.plan_no "+
                         "  and fcmps007.OD_PONO1=fcmps010.OD_PONO1(+)"+
                         "  and fcmps007.SH_NO=fcmps010.SH_NO(+)"+
                         "  and fcmps007.SH_COLOR=fcmps010.SH_COLOR(+)"+
                         "  and fcmps007.SH_SIZE=fcmps010.SH_SIZE(+)"+
                         "  and fcmps007.PROCID=fcmps010.PROCID(+)"+                            
                         "  and fcmps006.is_sure='Y' "+
                         "  and fcmps006.fa_no='"+getFA_NO()+"' "+
                         "group by fcmps007.SH_NO,fcmps007.SH_COLOR,fcmps007.SH_SIZE,fcmps007.PROCID";
        
		String strSQL="select A.*,B.OD_FGDATE MAX_OD_FGDATE from ("+FCMPS010+") A,("+FCMPS007+") B "+
		              "where A.SH_NO=B.SH_NO(+)"+
                      "  and A.SH_COLOR=B.SH_COLOR(+)"+
                      "  and A.SH_SIZE=B.SH_SIZE(+)"+
                      "  and B.OD_FGDATE>A.OD_FGDATE";

		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		try {
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){		    	
		    	do {
		    		 String SH_NO=rs.getString("SH_NO");
		    		 String SH_COLOR=rs.getString("SH_COLOR");
		    		 String SH_SIZE=rs.getString("SH_SIZE");
		    		 String OD_FGDATE=FCMPS_PUBLIC.getDate(rs.getDate("MAX_OD_FGDATE"), "yyyy/MM/dd");
		    		 
		    		 if(!doReplacePlannedOrder(SH_NO, SH_COLOR, SH_SIZE, OD_FGDATE)) {
		    			 iRet=false;
		    			 break;
		    		 }
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();

//		    conn.commit();
//		    conn.rollback();
		}catch(Exception ex) {
			ex.printStackTrace();
			 iRet=false;
			 try {
				 conn.rollback();
			 }catch(Exception sqlex) {
				 sqlex.printStackTrace();
			 }
		}finally {
			
		}
		return iRet;
	}
	
	/**
	 * 存在沒有抵扣的訂單比已抵扣的訂單交期早, 則要替換
	 * @return
	 */
	private boolean doReplaceNeutralizedOrder() {
		boolean iRet=true;
		
		String FCMPS010="select min(FCMPS010.OD_FGDATE) OD_FGDATE, "+
                        "       FCMPS010.SH_NO, "+
                        "       FCMPS010.SH_COLOR, "+
                        "       FCMPS010.SH_SIZE, "+
                        "       (CASE WHEN LEAN_NO='SHANGHAI' THEN LEAN_NO ELSE '無' END) LEAN_NO "+	
                        "from FCMPS010 "+
                        "where FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY, 0) > 0 "+
                        "  and nvl(FCMPS010.OD_CODE, 'N') = 'N' "+
                        "  and FCMPS010.IS_DISABLE = 'N' "+
                        "  and FCMPS010.OD_FGDATE is not null "+
                        "  and FCMPS010.FA_NO = '"+getFA_NO()+"' "+
 	                    "  and not exists(select * from FCMPS013 " +//已指定周次排產的訂單不進行抵扣
	                    "                 where FA_NO=FCMPS010.FA_NO " +
	                    "                   and OD_PONO1=FCMPS010.OD_PONO1 " +
	                    "                   and SH_NO=FCMPS010.SH_NO " +
	                    "                   and SH_COLOR=FCMPS010.SH_COLOR " +
	                    "                   and SH_SIZE=FCMPS010.SH_SIZE) "+
                        "group by FCMPS010.SH_NO,FCMPS010.SH_COLOR,FCMPS010.SH_SIZE,(CASE WHEN LEAN_NO='SHANGHAI' THEN LEAN_NO ELSE '無' END)";

		String FCMPS017="select max(fcmps017.OD_FGDATE) OD_FGDATE, "+
                         "       fcmps017.SH_ARITCLE, "+
                         "       fcmps017.SH_COLOR, "+
                         "       fcmps017.SH_SIZE, "+
                         "       (CASE WHEN LEAN_NO='SHANGHAI' THEN LEAN_NO ELSE '無' END) LEAN_NO "+	
                         "from fcmps017 "+
                         "where fcmps017.fa_no='"+getFA_NO()+"' "+	 
                         "group by fcmps017.SH_ARITCLE,fcmps017.SH_COLOR,fcmps017.SH_SIZE,(CASE WHEN LEAN_NO='SHANGHAI' THEN LEAN_NO ELSE '無' END)";
		 
		String strSQL="select A.*,B.OD_FGDATE MAX_OD_FGDATE from ("+FCMPS010+") A,("+FCMPS017+") B "+
                      "where A.SH_NO=B.SH_ARITCLE(+)"+
                      "  and A.SH_COLOR=B.SH_COLOR(+)"+
                      "  and A.SH_SIZE=B.SH_SIZE(+)"+
                      "  and A.LEAN_NO=B.LEAN_NO(+)"+
                      "  and B.OD_FGDATE>A.OD_FGDATE";
			
		Connection conn =getConnection();
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try {
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
		    rs=pstmtData.executeQuery();
		    
		    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		    rs.setFetchSize(3000);
		    
		    if(rs.next()){
		    	do {
		    		 String SH_NO=rs.getString("SH_NO");
		    		 String SH_COLOR=rs.getString("SH_COLOR");
		    		 String SH_SIZE=rs.getString("SH_SIZE");
		    		 String LEAN_NO=rs.getString("LEAN_NO");
                     String OD_FGDATE=FCMPS_PUBLIC.getDate(rs.getDate("MAX_OD_FGDATE"), "yyyy/MM/dd");
                     
		    		 if(!doReplaceNeutralizedOrder(SH_NO, SH_COLOR, SH_SIZE,LEAN_NO,OD_FGDATE)) {
		    			 iRet=false;
		    			 break;
		    		 }
				    
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();

//		    conn.commit();
//		    conn.rollback();
		}catch(Exception ex) {
			ex.printStackTrace();
			iRet=false;
			try {
				conn.rollback();
			}catch(Exception sqlex) {
				sqlex.printStackTrace();
			}
		}finally {
			
		}
		return iRet;
	}
	
	/**
	 * 替代已排產訂單
	 * @return
	 */
	private boolean doReplacePlannedOrder(
			String SH_NO,
			String SH_COLOR,
			String SH_SIZE,
			String MAX_OD_FGDATE) throws Exception{
		boolean iRet=false;
		
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		PreparedStatement pstmtData3 = null;
		ResultSet rs3=null;
		
		PreparedStatement pstmtData4 = null;
		
		List<String> ls_PROCID=FCMPS_PUBLIC.getSH_PROC(SH_NO, conn);
		
		
		
		strSQL="select " +
	           "to_char(FCMPS010.OD_FGDATE,'YYYY/MM/DD') OD_FGDATE, "+
	           "FCMPS010.OD_PONO1,"+
	           "to_char(FCMPS010.OD_SHIP,'YYYY/MM/DD') OD_SHIP,"+
	           "FCMPS010.OD_QTY,"+	
	           "min(OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)) WORK_PLAN_QTY "+	
               "from FCMPS010 "+
               "where FCMPS010.SH_NO='"+SH_NO+"'"+
               "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
               "  and FCMPS010.SH_SIZE='"+SH_SIZE+"'"+
               "  and to_char(fcmps010.OD_FGDATE,'YYYY/MM/DD')<'"+MAX_OD_FGDATE+"'"+
               "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
               "  and FCMPS010.IS_DISABLE='N' "+                    
               "  and FCMPS010.OD_FGDATE is not null "+                        
               "  and FCMPS010.FA_NO='"+getFA_NO()+"' "+   
               "  and FCMPS010.PROCID=(select min(PB_PTNO) from fcps22_1 where PB_PTNO is not null and SH_ARITCLE=FCMPS010.SH_NO) "+
               "  and not exists(select * from FCMPS013 " +//已指定周次排產的訂單不進行抵扣
               "                 where FA_NO=FCMPS010.FA_NO " +
               "                   and OD_PONO1=FCMPS010.OD_PONO1 " +
               "                   and SH_NO=FCMPS010.SH_NO " +
               "                   and SH_COLOR=FCMPS010.SH_COLOR " +
               "                   and SH_SIZE=FCMPS010.SH_SIZE) "+
               "group by to_char(FCMPS010.OD_FGDATE,'YYYY/MM/DD'),FCMPS010.OD_PONO1,to_char(FCMPS010.OD_SHIP,'YYYY/MM/DD'),FCMPS010.OD_QTY "+
		       "order by to_char(FCMPS010.OD_FGDATE,'YYYY/MM/DD') ASC,to_char(FCMPS010.OD_SHIP,'YYYY/MM/DD') ASC,FCMPS010.OD_PONO1 ";
	
	    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
	    rs=pstmtData.executeQuery();
	    
	    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
	    rs.setFetchSize(3000);
	    
	    if(rs.next()){
	         conn.setAutoCommit(false);
	    	 do {
	    		 String OD_PONO1=rs.getString("OD_PONO1");
	    		 String OD_FGDATE=rs.getString("OD_FGDATE");
	    		 double WORK_PLAN_QTY=rs.getDouble("WORK_PLAN_QTY");		    		 
	    		 
	    		 if(WORK_PLAN_QTY==0) continue;
	    		 
	    		 double OD_QTY=rs.getDouble("OD_QTY");
	    		 
	    		 for(String PROCID:ls_PROCID) {
	    			 
	    			 WORK_PLAN_QTY=rs.getDouble("WORK_PLAN_QTY");
	    			 
		    		 strSQL="select to_char(fcmps010.OD_FGDATE,'YYYY/MM/DD') OD_FGDATE," +
		    		        "       to_char(fcmps010.OD_SHIP,'YYYY/MM/DD') OD_SHIP,"+
	    		            "       fcmps007.PLAN_NO,"+
	    		 		    "       fcmps007.OD_PONO1," +
	    		 		    "       fcmps007.WORK_PLAN_QTY " +
	    		 		    "from fcmps006,fcmps007,fcmps010 "+
                            "where fcmps006.plan_no=fcmps007.plan_no "+
                            "  and fcmps007.OD_PONO1=fcmps010.OD_PONO1(+)"+
                            "  and fcmps007.SH_NO=fcmps010.SH_NO(+)"+
                            "  and fcmps007.SH_COLOR=fcmps010.SH_COLOR(+)"+
                            "  and fcmps007.SH_SIZE=fcmps010.SH_SIZE(+)"+
                            "  and fcmps007.PROCID=fcmps010.PROCID(+)"+                            
                            "  and fcmps006.is_sure='Y' "+
                            "  and fcmps006.fa_no='"+getFA_NO()+"' "+
                            "  and to_char(fcmps010.OD_FGDATE,'YYYY/MM/DD')>'"+OD_FGDATE+"'"+
                            "  and fcmps007.SH_NO='"+SH_NO+"'"+
                            "  and fcmps007.SH_COLOR='"+SH_COLOR+"'"+
                            "  and fcmps007.SH_SIZE='"+SH_SIZE+"'"+
                            "  and fcmps007.PROCID='"+PROCID+"' "+
                            "  and not exists(select * from FCMPS013 " +//已指定周次排產的訂單不進行抵扣
                            "                 where FA_NO=FCMPS006.FA_NO " +
                            "                   and OD_PONO1=FCMPS007.OD_PONO1 " +
                            "                   and SH_NO=FCMPS007.SH_NO " +
                            "                   and SH_COLOR=FCMPS007.SH_COLOR " +
                            "                   and SH_SIZE=FCMPS007.SH_SIZE) "+
                            "order by to_char(fcmps010.OD_FGDATE,'YYYY/MM/DD') DESC,to_char(fcmps010.OD_SHIP,'YYYY/MM/DD') DESC,fcmps007.OD_PONO1 ";
			 		    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
					    rs2=pstmtData2.executeQuery();
					    
					    if(rs2.next()){
					    	do {
					    		
					    		if(WORK_PLAN_QTY<=0) break;
					    		
					    		System.out.println("型體:"+SH_NO+" 配色:"+SH_COLOR+" SIZE:"+SH_SIZE+" 制程:"+PROCID+" 新接單:"+OD_PONO1+" 被替代訂單:"+rs2.getString("OD_PONO1"));
					    		
					    		if(rs2.getDouble("WORK_PLAN_QTY")>WORK_PLAN_QTY) {
					    			//更新新訂單的排產量
					    			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)+"+WORK_PLAN_QTY+" "+
					    			       "where OD_PONO1='" +OD_PONO1+"'"+
					    			       "  and SH_NO='"+SH_NO+"'"+
		                                   "  and SH_COLOR='"+SH_COLOR+"'"+
		                                   "  and SH_SIZE='"+SH_SIZE+"'"+
		                                   "  and PROCID='"+PROCID+"'"+
		                                   "  and FA_NO='"+getFA_NO()+"' ";
						 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
								    pstmtData3.execute();
								    pstmtData3.close();
								    
								    //扣除被替代訂單的排產量
					    			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)-"+WORK_PLAN_QTY+" "+
				    			           "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
				    			           "  and SH_NO='"+SH_NO+"'"+
		                                   "  and SH_COLOR='"+SH_COLOR+"'"+
		                                   "  and SH_SIZE='"+SH_SIZE+"'"+
		                                   "  and PROCID='"+PROCID+"'"+
		                                   "  and FA_NO='"+getFA_NO()+"' ";
						 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
								    pstmtData3.execute();
								    pstmtData3.close();
								    						   							   
								    //新增新訂單到排產計劃中
								    strSQL="select PLAN_NO from FCMPS007 "+
		 			                       "where OD_PONO1='" +OD_PONO1+"'"+		    			               
		 			                       "  and SH_NO='"+SH_NO+"'"+
		                                   "  and SH_COLOR='"+SH_COLOR+"'"+
		                                   "  and SH_SIZE='"+SH_SIZE+"'"+
		                                   "  and PROCID='"+PROCID+"'"+
		                                   "  and PLAN_NO='"+rs2.getString("PLAN_NO")+"'";
						 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
								    rs3=pstmtData3.executeQuery();
								    if(!rs3.next()) {
						    			strSQL="insert into fcmps007 "+
		                                    "(plan_no, procid, od_pono1, sh_no, sh_size, od_qty, work_plan_qty, " +
		                                    " sh_color, work_week, up_date, up_user, style_no, proc_seq, size_cap_qty, " +
		                                    " sh_cap_qty, need_shoot, share_sh_no, share_size, is_use_cap) "+
		                                    " select plan_no, procid, '"+OD_PONO1+"', sh_no, sh_size, "+OD_QTY+", "+WORK_PLAN_QTY+", " +
		                                    " sh_color, work_week, up_date, up_user, style_no, proc_seq, size_cap_qty, " +
		                                    " sh_cap_qty, need_shoot, share_sh_no, share_size, is_use_cap from FCMPS007 "+
			    			                   "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
			    			                   "  and SH_NO='"+SH_NO+"'"+
		                                    "  and SH_COLOR='"+SH_COLOR+"'"+
		                                    "  and SH_SIZE='"+SH_SIZE+"'"+
		                                    "  and PROCID='"+PROCID+"'"+
		                                    "  and PLAN_NO='"+rs2.getString("PLAN_NO")+"'";
							 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
									    pstmtData4.execute();
									    pstmtData4.close();							    	
								    }else {
						    			strSQL="update FCMPS007 set WORK_PLAN_QTY=WORK_PLAN_QTY+"+WORK_PLAN_QTY+" "+
			    			                   "where OD_PONO1='" +OD_PONO1+"'"+		    			               
			    			                   "  and SH_NO='"+SH_NO+"'"+
		                                       "  and SH_COLOR='"+SH_COLOR+"'"+
		                                       "  and SH_SIZE='"+SH_SIZE+"'"+
		                                       "  and PROCID='"+PROCID+"'"+
		                                       "  and PLAN_NO='"+rs2.getString("PLAN_NO")+"'";
		                             
							 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
									    pstmtData4.execute();
									    pstmtData4.close();							    	
								    }
								    rs3.close();
								    pstmtData3.close();	

								    //扣除被替代訂單的部份排產量
					    			strSQL="update FCMPS007 set WORK_PLAN_QTY=WORK_PLAN_QTY-"+WORK_PLAN_QTY+" "+
			    			               "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+		    			               
			    			               "  and SH_NO='"+SH_NO+"'"+
		                                   "  and SH_COLOR='"+SH_COLOR+"'"+
		                                   "  and SH_SIZE='"+SH_SIZE+"'"+
		                                   "  and PROCID='"+PROCID+"'"+
		                                   "  and PLAN_NO='"+rs2.getString("PLAN_NO")+"'";
		                                
						 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
								    pstmtData3.execute();
								    pstmtData3.close();							    
								    
								    WORK_PLAN_QTY=0;
					    		}
					    		
					    		if(rs2.getDouble("WORK_PLAN_QTY")==WORK_PLAN_QTY) {
					    			//更新新訂單的排產量
					    			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)+"+WORK_PLAN_QTY+" "+
					    			       "where OD_PONO1='" +OD_PONO1+"'"+
					    			       "  and SH_NO='"+SH_NO+"'"+
		                                   "  and SH_COLOR='"+SH_COLOR+"'"+
		                                   "  and SH_SIZE='"+SH_SIZE+"'"+
		                                   "  and PROCID='"+PROCID+"'"+
		                                   "  and FA_NO='"+getFA_NO()+"' ";
						 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
								    pstmtData3.execute();
								    pstmtData3.close();
								    
								    //扣除被替代訂單的排產量
					    			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)-"+WORK_PLAN_QTY+" "+
				    			           "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
				    			           "  and SH_NO='"+SH_NO+"'"+
		                                   "  and SH_COLOR='"+SH_COLOR+"'"+
		                                   "  and SH_SIZE='"+SH_SIZE+"'"+
		                                   "  and PROCID='"+PROCID+"'"+
		                                   "  and FA_NO='"+getFA_NO()+"' ";
						 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
								    pstmtData3.execute();
								    pstmtData3.close();							    
							    
								    //將新接訂單替代原訂單號					    							    
								    strSQL="select PLAN_NO from FCMPS007 "+
					                       "where OD_PONO1='" +OD_PONO1+"'"+		    			               
					                       "  and SH_NO='"+SH_NO+"'"+
		                                   "  and SH_COLOR='"+SH_COLOR+"'"+
		                                   "  and SH_SIZE='"+SH_SIZE+"'"+
		                                   "  and PROCID='"+PROCID+"'"+
		                                   "  and PLAN_NO='"+rs2.getString("PLAN_NO")+"'";
						 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
								    rs3=pstmtData3.executeQuery();
								    if(!rs3.next()) {//新接訂單沒有在此計劃中, 直接將被替代訂單號改為新接訂單號
						    			strSQL="update FCMPS007 set OD_PONO1='"+OD_PONO1+"',OD_QTY="+OD_QTY+" "+
			    			                   "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
			    			                   "  and SH_NO='"+SH_NO+"'"+
		                                       "  and SH_COLOR='"+SH_COLOR+"'"+
		                                       "  and SH_SIZE='"+SH_SIZE+"'"+
		                                       "  and PROCID='"+PROCID+"'"+
		                                       "  and PLAN_NO='"+rs2.getString("PLAN_NO")+"'";
							 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
									    pstmtData4.execute();
									    pstmtData4.close();									    	
								    }else {
						    			strSQL="update FCMPS007 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)+"+WORK_PLAN_QTY+" "+
				    			               "where OD_PONO1='" +OD_PONO1+"'"+
				    			               "  and SH_NO='"+SH_NO+"'"+
		                                       "  and SH_COLOR='"+SH_COLOR+"'"+
		                                       "  and SH_SIZE='"+SH_SIZE+"'"+
		                                       "  and PROCID='"+PROCID+"'"+
		                                       "  and PLAN_NO='"+rs2.getString("PLAN_NO")+"'";
							 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
									    pstmtData4.execute();
									    pstmtData4.close();	
									    			
									    //在計劃中刪除被替代訂單
						    			strSQL="delete from FCMPS007 "+
					                           "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
					                           "  and SH_NO='"+SH_NO+"'"+
		                                       "  and SH_COLOR='"+SH_COLOR+"'"+
		                                       "  and SH_SIZE='"+SH_SIZE+"'"+
		                                       "  and PROCID='"+PROCID+"'"+
		                                       "  and PLAN_NO='"+rs2.getString("PLAN_NO")+"'";
							 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
									    pstmtData4.execute();
									    pstmtData4.close();
									    
								    }
								    rs3.close();
								    pstmtData3.close();

								    WORK_PLAN_QTY=0;
					    		}
					    		
					    		if(rs2.getDouble("WORK_PLAN_QTY")<WORK_PLAN_QTY) {
					    			//更新新訂單的排產量
					    			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)+"+rs2.getDouble("WORK_PLAN_QTY")+" "+
					    			       "where OD_PONO1='" +OD_PONO1+"'"+
					    			       "  and SH_NO='"+SH_NO+"'"+
		                                   "  and SH_COLOR='"+SH_COLOR+"'"+
		                                   "  and SH_SIZE='"+SH_SIZE+"'"+
		                                   "  and PROCID='"+PROCID+"'"+
		                                   "  and FA_NO='"+getFA_NO()+"' ";
						 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
								    pstmtData3.execute();
								    pstmtData3.close();
								    
								    //扣除被替代訂單的排產量
					    			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)-"+rs2.getDouble("WORK_PLAN_QTY")+" "+
				    			           "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
				    			           "  and SH_NO='"+SH_NO+"'"+
		                                   "  and SH_COLOR='"+SH_COLOR+"'"+
		                                   "  and SH_SIZE='"+SH_SIZE+"'"+
		                                   "  and PROCID='"+PROCID+"'"+
		                                   "  and FA_NO='"+getFA_NO()+"' ";
						 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
								    pstmtData3.execute();
								    pstmtData3.close();
								    
								    //將新接訂單替代原訂單號
								    strSQL="select PLAN_NO from FCMPS007 "+
			                               "where OD_PONO1='" +OD_PONO1+"'"+		    			               
			                               "  and SH_NO='"+SH_NO+"'"+
		                                   "  and SH_COLOR='"+SH_COLOR+"'"+
		                                   "  and SH_SIZE='"+SH_SIZE+"'"+
		                                   "  and PROCID='"+PROCID+"'"+
		                                   "  and PLAN_NO='"+rs2.getString("PLAN_NO")+"'";
						 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
								    rs3=pstmtData3.executeQuery();
								    if(!rs3.next()) {//新接訂單沒有在此計劃中, 直接將被替代訂單號改為新接訂單號
						    			strSQL="update FCMPS007 set OD_PONO1='"+OD_PONO1+"',OD_QTY="+OD_QTY+" "+
			    			                   "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
			    			                   "  and SH_NO='"+SH_NO+"'"+
		                                       "  and SH_COLOR='"+SH_COLOR+"'"+
		                                       "  and SH_SIZE='"+SH_SIZE+"'"+
		                                       "  and PROCID='"+PROCID+"'"+
		                                       "  and PLAN_NO='"+rs2.getString("PLAN_NO")+"'";
							 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
									    pstmtData4.execute();
									    pstmtData4.close();							    	
								    }else {
						    			strSQL="update FCMPS007 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)+"+rs2.getDouble("WORK_PLAN_QTY")+" "+
				    			               "where OD_PONO1='" +OD_PONO1+"'"+
				    			               "  and SH_NO='"+SH_NO+"'"+
		                                       "  and SH_COLOR='"+SH_COLOR+"'"+
		                                       "  and SH_SIZE='"+SH_SIZE+"'"+
		                                       "  and PROCID='"+PROCID+"'"+
		                                       "  and PLAN_NO='"+rs2.getString("PLAN_NO")+"'";
							 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
									    pstmtData4.execute();
									    pstmtData4.close();		
									    		
						    			strSQL="delete from FCMPS007 "+
		                                    "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
		                                    "  and SH_NO='"+SH_NO+"'"+
		                                    "  and SH_COLOR='"+SH_COLOR+"'"+
		                                    "  and SH_SIZE='"+SH_SIZE+"'"+
		                                    "  and PROCID='"+PROCID+"'"+
		                                    "  and PLAN_NO='"+rs2.getString("PLAN_NO")+"'";
							 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
									    pstmtData4.execute();
									    pstmtData4.close();
								    
								    }
								    rs3.close();
								    pstmtData3.close();						    
								    							    
								    WORK_PLAN_QTY=WORK_PLAN_QTY-rs2.getDouble("WORK_PLAN_QTY");
					    		}
					    		
					    	}while(rs2.next());
					    	
						    rs2.close();
						    pstmtData2.close();
					    }else {
						    rs2.close();
						    pstmtData2.close();
						    break;
					    }	    			 
	    		 }
	    		 

			    
	    	 }while(rs.next());
	    	 
	    	 conn.commit();
	    }
	    rs.close();
	    pstmtData.close();
	    
	    iRet=true;
	    	
		
		return iRet;
	}
	
	/**
	 * 替代已抵扣訂單
	 * @return
	 */
	private boolean doReplaceNeutralizedOrder(
			String SH_NO,
			String SH_COLOR,
			String SH_SIZE,
			String LEAN_NO,
			String MAX_OD_FGDATE) throws Exception {
		boolean iRet=false;
		
		String strSQL="";
		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		PreparedStatement pstmtData3 = null;
		ResultSet rs3=null;
		
		PreparedStatement pstmtData4 = null;		
		

		strSQL="select " +
	           "to_char(FCMPS010.OD_FGDATE,'YYYY/MM/DD') OD_FGDATE, "+
	           "FCMPS010.OD_PONO1,"+
	           "to_char(FCMPS010.OD_SHIP,'YYYY/MM/DD') OD_SHIP,"+
	           "FCMPS010.SH_NO,"+
	           "FCMPS010.SH_COLOR,"+
	           "FCMPS010.SH_SIZE,"+	
	           "FCMPS010.LEAN_NO,"+	
	           "min(FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)) WORK_PLAN_QTY "+	
               "from FCMPS010 "+
               "where FCMPS010.SH_NO='"+SH_NO+"'"+
               "  and FCMPS010.SH_COLOR='"+SH_COLOR+"'"+
               "  and FCMPS010.SH_SIZE='"+SH_SIZE+"'"+
               "  and (CASE WHEN LEAN_NO='SHANGHAI' THEN LEAN_NO ELSE '無' END)='"+LEAN_NO+"'"+	
               "  and nvl(FCMPS010.OD_CODE,'N')='N' "+
               "  and FCMPS010.IS_DISABLE='N' "+  
               "  and to_char(fcmps010.OD_FGDATE,'YYYY/MM/DD')<'"+MAX_OD_FGDATE+"'"+
               "  and FCMPS010.FA_NO='"+getFA_NO()+"' "+
               "  and not exists(select * from FCMPS013 " +//已指定周次排產的訂單不進行抵扣
               "                 where FA_NO=FCMPS010.FA_NO " +
               "                   and OD_PONO1=FCMPS010.OD_PONO1 " +
               "                   and SH_NO=FCMPS010.SH_NO " +
               "                   and SH_COLOR=FCMPS010.SH_COLOR " +
               "                   and SH_SIZE=FCMPS010.SH_SIZE) "+
               "group by to_char(FCMPS010.OD_FGDATE,'YYYY/MM/DD')," +
               "         FCMPS010.OD_PONO1," +
               "         to_char(FCMPS010.OD_SHIP,'YYYY/MM/DD'),"+
               "         FCMPS010.SH_NO," +
               "         FCMPS010.SH_COLOR," +
               "         FCMPS010.SH_SIZE, "+
               "         FCMPS010.LEAN_NO "+
               "order by to_char(FCMPS010.OD_FGDATE,'YYYY/MM/DD') ASC,to_char(FCMPS010.OD_SHIP,'YYYY/MM/DD') ASC,FCMPS010.OD_PONO1 ";
	
	    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
	    rs=pstmtData.executeQuery();
	    
	    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
	    rs.setFetchSize(3000);
	    
	    if(rs.next()){
	    	 conn.setAutoCommit(false);
	    	 do {
	    		 String OD_PONO1=rs.getString("OD_PONO1");
	    		 String OD_FGDATE=rs.getString("OD_FGDATE");
	    		 String LEAN_NO_MPS010=rs.getString("LEAN_NO");
	    		 double WORK_PLAN_QTY=rs.getDouble("WORK_PLAN_QTY");
	    		 
	    		 if(WORK_PLAN_QTY==0) continue;
	    		 
	    		 strSQL="select distinct " +
	    		        "       fcmps017.PR_NO,"+
	    		 		"       to_char(fcmps010.OD_FGDATE,'YYYY/MM/DD') OD_FGDATE," +
	    		 		"       fcmps017.OD_PONO1," +
	    		 		"       to_char(fcmps010.OD_SHIP,'YYYY/MM/DD') OD_SHIP,"+
	    		 		"       fcmps017.USE_TYPE,"+
	    		 		"       fcmps017.USE_QTY " +
	    		 		"from fcmps017,fcmps010 "+
                        "where fcmps017.OD_PONO1=fcmps010.OD_PONO1(+)"+
                        "  and fcmps017.SH_ARITCLE=fcmps010.SH_NO(+)"+
                        "  and fcmps017.SH_COLOR=fcmps010.SH_COLOR(+)"+
                        "  and fcmps017.SH_SIZE=fcmps010.SH_SIZE(+)"+        
                        "  and fcmps017.FA_NO=fcmps010.FA_NO(+)"+ 
                        "  and fcmps017.SH_ARITCLE='"+SH_NO+"'"+
                        "  and fcmps017.SH_COLOR='"+SH_COLOR+"'"+
                        "  and fcmps017.SH_SIZE='"+SH_SIZE+"'"+
                        "  and fcmps017.fa_no='"+getFA_NO()+"' "+                        
                        "  and to_char(fcmps010.OD_FGDATE,'YYYY/MM/DD')>'"+OD_FGDATE+"'"+
                        "  and (CASE WHEN fcmps017.LEAN_NO='SHANGHAI' THEN fcmps017.LEAN_NO ELSE '無' END)='"+LEAN_NO+"' "+
                        "  and not exists(select * from FCMPS013 " +//已指定周次排產的訂單不進行抵扣
                        "                 where FA_NO=FCMPS017.FA_NO " +
                        "                   and OD_PONO1=FCMPS017.OD_PONO1 " +
                        "                   and SH_NO=FCMPS017.SH_ARITCLE " +
                        "                   and SH_COLOR=FCMPS017.SH_COLOR " +
                        "                   and SH_SIZE=FCMPS017.SH_SIZE) "+
                        "order by to_char(fcmps010.OD_FGDATE,'YYYY/MM/DD') DESC,to_char(fcmps010.OD_SHIP,'YYYY/MM/DD') DESC,fcmps017.OD_PONO1 ";
	 		    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
			    rs2=pstmtData2.executeQuery();
			    
			    if(rs2.next()){
			    	do {
			    		if(WORK_PLAN_QTY==0) break;
			    		
			    		if(rs2.getDouble("USE_QTY")>WORK_PLAN_QTY) {
			    			//更新新訂單的排產量
			    			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)+"+WORK_PLAN_QTY+" "+
			    			       "where OD_PONO1='" +OD_PONO1+"'"+
			    			       "  and SH_NO='"+SH_NO+"'"+
                                   "  and SH_COLOR='"+SH_COLOR+"'"+
                                   "  and SH_SIZE='"+SH_SIZE+"'"+
                                   "  and FA_NO='"+getFA_NO()+"' ";
				 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
						    pstmtData3.execute();
						    pstmtData3.close();
						    
						    //扣除被替代訂單的排產量
			    			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)-"+WORK_PLAN_QTY+" "+
		    			           "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
		    			           "  and SH_NO='"+SH_NO+"'"+
                                   "  and SH_COLOR='"+SH_COLOR+"'"+
                                   "  and SH_SIZE='"+SH_SIZE+"'"+
                                   "  and FA_NO='"+getFA_NO()+"' ";
				 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
						    pstmtData3.execute();
						    pstmtData3.close();
						    				   							    
						    //新增新訂單到抵扣記錄中
						    
						    strSQL="select OD_PONO1 from FCMPS017 "+
    			                   "where OD_PONO1='" +OD_PONO1+"'"+
    			                   "  and USE_TYPE='"+rs2.getString("USE_TYPE")+"'"+
    			                   "  and PR_NO='"+rs2.getString("PR_NO")+"'"+
    			                   "  and SH_ARITCLE='"+SH_NO+"'"+
                                   "  and SH_COLOR='"+SH_COLOR+"'"+
                                   "  and SH_SIZE='"+SH_SIZE+"'"+
                                   "  and FA_NO='"+getFA_NO()+"' ";
				 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
						    rs3=pstmtData3.executeQuery();
						    if(!rs3.next()) {
				    			strSQL="insert into fcmps017 " +
				    				   "(fa_no,pr_no, od_pono1, sh_aritcle, sh_color, sh_size, use_qty, " +
				    				   " lean_no,up_date,use_type,od_fgdate) "+
			    			           "select fa_no,pr_no, '"+OD_PONO1+"', sh_aritcle, sh_color, sh_size, "+WORK_PLAN_QTY+", " +
			    			           "'"+LEAN_NO_MPS010+"',up_date,use_type,to_date('"+OD_FGDATE+"','YYYY/MM/DD') from fcmps017 "+
	    			                   "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
	    			                   "  and USE_TYPE='"+rs2.getString("USE_TYPE")+"'"+
	    			                   "  and PR_NO='"+rs2.getString("PR_NO")+"'"+
	    			                   "  and SH_ARITCLE='"+SH_NO+"'"+
                                       "  and SH_COLOR='"+SH_COLOR+"'"+
                                       "  and SH_SIZE='"+SH_SIZE+"'"+
                                       "  and FA_NO='"+getFA_NO()+"' ";
			    			
					 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
							    pstmtData4.execute();
							    pstmtData4.close();	
						    }else {
				    			strSQL="update FCMPS017 set USE_QTY=USE_QTY+"+WORK_PLAN_QTY+" "+
	    			                   "where OD_PONO1='" +OD_PONO1+"'"+
	    			                   "  and USE_TYPE='"+rs2.getString("USE_TYPE")+"'"+
	    			                   "  and PR_NO='"+rs2.getString("PR_NO")+"'"+
	    			                   "  and SH_ARITCLE='"+SH_NO+"'"+
                                       "  and SH_COLOR='"+SH_COLOR+"'"+
                                       "  and SH_SIZE='"+SH_SIZE+"'"+
                                       "  and FA_NO='"+getFA_NO()+"' ";
					 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
							    pstmtData4.execute();
							    pstmtData4.close();							    	
						    }
						    rs3.close();
						    pstmtData3.close();		
						    			
						    //扣除被替代訂單的部份抵扣量
			    			strSQL="update FCMPS017 set USE_QTY=USE_QTY-"+WORK_PLAN_QTY+" "+
	    			               "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
	    			               "  and USE_TYPE='"+rs2.getString("USE_TYPE")+"'"+
	    			               "  and PR_NO='"+rs2.getString("PR_NO")+"'"+
	    			               "  and SH_ARITCLE='"+SH_NO+"'"+
                                   "  and SH_COLOR='"+SH_COLOR+"'"+
                                   "  and SH_SIZE='"+SH_SIZE+"'"+
                                   "  and FA_NO='"+getFA_NO()+"' ";
				 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
						    pstmtData3.execute();
						    pstmtData3.close();		
						    
						    WORK_PLAN_QTY=0;
			    		}
			    		
			    		if(rs2.getDouble("USE_QTY")==WORK_PLAN_QTY) {
			    			//更新新訂單的排產量
			    			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)+"+WORK_PLAN_QTY+" "+
			    			       "where OD_PONO1='" +OD_PONO1+"'"+
			    			       "  and SH_NO='"+SH_NO+"'"+
                                   "  and SH_COLOR='"+SH_COLOR+"'"+
                                   "  and SH_SIZE='"+SH_SIZE+"'"+
                                   "  and FA_NO='"+getFA_NO()+"' ";
				 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
						    pstmtData3.execute();
						    pstmtData3.close();
						    
						    //扣除被替代訂單的排產量
			    			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)-"+WORK_PLAN_QTY+" "+
		    			           "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
		    			           "  and SH_NO='"+SH_NO+"'"+
                                   "  and SH_COLOR='"+SH_COLOR+"'"+
                                   "  and SH_SIZE='"+SH_SIZE+"'"+
                                   "  and FA_NO='"+getFA_NO()+"' ";
				 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
						    pstmtData3.execute();
						    pstmtData3.close();
						    			
						    //將新接訂單替代原訂單號
						    strSQL="select OD_PONO1 from FCMPS017 "+
    			                   "where OD_PONO1='" +OD_PONO1+"'"+
    			                   "  and USE_TYPE='"+rs2.getString("USE_TYPE")+"'"+
    			                   "  and PR_NO='"+rs2.getString("PR_NO")+"'"+
    			                   "  and SH_ARITCLE='"+SH_NO+"'"+
                                   "  and SH_COLOR='"+SH_COLOR+"'"+
                                   "  and SH_SIZE='"+SH_SIZE+"'"+
                                   "  and FA_NO='"+getFA_NO()+"' ";
				 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
						    rs3=pstmtData3.executeQuery();
						    if(!rs3.next()) {								    
				    			strSQL="update FCMPS017 set OD_PONO1='"+OD_PONO1+"'," +
				    				   "LEAN_NO='"+LEAN_NO_MPS010+"',"+
				    				   "OD_FGDATE=TO_DATE('"+OD_FGDATE+"','YYYY/MM/DD') "+
		    			               "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
		    			               "  and USE_TYPE='"+rs2.getString("USE_TYPE")+"'"+
		    			               "  and PR_NO='"+rs2.getString("PR_NO")+"'"+
		    			               "  and SH_ARITCLE='"+SH_NO+"'"+
                                       "  and SH_COLOR='"+SH_COLOR+"'"+
                                       "  and SH_SIZE='"+SH_SIZE+"'"+
                                       "  and FA_NO='"+getFA_NO()+"' ";
					 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
							    pstmtData4.executeUpdate();
							    pstmtData4.close();	
						    }else {
				    			strSQL="update FCMPS017 set USE_QTY=USE_QTY+"+WORK_PLAN_QTY+" "+
    			                       "where OD_PONO1='" +OD_PONO1+"'"+
    			                       "  and USE_TYPE='"+rs2.getString("USE_TYPE")+"'"+
    			                       "  and PR_NO='"+rs2.getString("PR_NO")+"'"+
    			                       "  and SH_ARITCLE='"+SH_NO+"'"+
                                       "  and SH_COLOR='"+SH_COLOR+"'"+
                                       "  and SH_SIZE='"+SH_SIZE+"'"+
                                       "  and FA_NO='"+getFA_NO()+"' ";
					 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
							    pstmtData4.executeUpdate();
							    pstmtData4.close();	

							    //刪除被替代訂單
				    			strSQL="delete from FCMPS017 "+
                                       "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
                                       "  and USE_TYPE='"+rs2.getString("USE_TYPE")+"'"+
                                       "  and PR_NO='"+rs2.getString("PR_NO")+"'"+
                                       "  and SH_ARITCLE='"+SH_NO+"'"+
                                       "  and SH_COLOR='"+SH_COLOR+"'"+
                                       "  and SH_SIZE='"+SH_SIZE+"'"+
                                       "  and FA_NO='"+getFA_NO()+"' ";
					 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
							    pstmtData4.executeUpdate();
							    pstmtData4.close();	
							    
						    }
						    rs3.close();
						    pstmtData3.close();		

						    WORK_PLAN_QTY=0;
			    		}
			    		
			    		if(rs2.getDouble("USE_QTY")<WORK_PLAN_QTY) {
			    			//更新新訂單的排產量
			    			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)+"+rs2.getDouble("USE_QTY")+" "+
			    			       "where OD_PONO1='" +OD_PONO1+"'"+
			    			       "  and SH_NO='"+SH_NO+"'"+
                                   "  and SH_COLOR='"+SH_COLOR+"'"+
                                   "  and SH_SIZE='"+SH_SIZE+"'"+
                                   "  and FA_NO='"+getFA_NO()+"' ";
				 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
						    pstmtData3.execute();
						    pstmtData3.close();
						    
						    //扣除被替代訂單的排產量
			    			strSQL="update FCMPS010 set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)-"+rs2.getDouble("USE_QTY")+" "+
		    			           "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
		    			           "  and SH_NO='"+SH_NO+"'"+
                                   "  and SH_COLOR='"+SH_COLOR+"'"+
                                   "  and SH_SIZE='"+SH_SIZE+"'"+
                                   "  and FA_NO='"+getFA_NO()+"' ";
				 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
						    pstmtData3.execute();
						    pstmtData3.close();
						    
						    //將新接訂單替代原訂單號
						    strSQL="select OD_PONO1 from FCMPS017 "+
			                       "where OD_PONO1='" +OD_PONO1+"'"+
	    			               "  and USE_TYPE='"+rs2.getString("USE_TYPE")+"'"+
	    			               "  and PR_NO='"+rs2.getString("PR_NO")+"'"+
			                       "  and SH_ARITCLE='"+SH_NO+"'"+
                                   "  and SH_COLOR='"+SH_COLOR+"'"+
                                   "  and SH_SIZE='"+SH_SIZE+"'"+
                                   "  and FA_NO='"+getFA_NO()+"' ";
				 		    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
						    rs3=pstmtData3.executeQuery();
						    if(!rs3.next()) {								    
				    			strSQL="update FCMPS017 set OD_PONO1='"+OD_PONO1+"', "+
			    				       "LEAN_NO='"+LEAN_NO_MPS010+"',"+
			    				       "OD_FGDATE=TO_DATE('"+OD_FGDATE+"','YYYY/MM/DD') "+
		    			               "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
		    			               "  and USE_TYPE='"+rs2.getString("USE_TYPE")+"'"+
		    			               "  and PR_NO='"+rs2.getString("PR_NO")+"'"+
		    			               "  and SH_ARITCLE='"+SH_NO+"'"+
                                       "  and SH_COLOR='"+SH_COLOR+"'"+
                                       "  and SH_SIZE='"+SH_SIZE+"'"+
                                       "  and FA_NO='"+getFA_NO()+"' ";
					 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
							    pstmtData4.execute();
							    pstmtData4.close();	
						    }else {
				    			strSQL="update FCMPS017 set USE_QTY=USE_QTY+"+rs2.getDouble("USE_QTY")+" "+
 			                           "where OD_PONO1='" +OD_PONO1+"'"+
 			                           "  and USE_TYPE='"+rs2.getString("USE_TYPE")+"'"+
 			                           "  and PR_NO='"+rs2.getString("PR_NO")+"'"+
 			                           "  and SH_ARITCLE='"+SH_NO+"'"+
                                       "  and SH_COLOR='"+SH_COLOR+"'"+
                                       "  and SH_SIZE='"+SH_SIZE+"'"+
                                       "  and FA_NO='"+getFA_NO()+"' ";
					 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
							    pstmtData4.execute();
							    pstmtData4.close();		
							    
				    			strSQL="delete from FCMPS017 "+
		                               "where OD_PONO1='" +rs2.getString("OD_PONO1")+"'"+
		                               "  and USE_TYPE='"+rs2.getString("USE_TYPE")+"'"+
		                               "  and PR_NO='"+rs2.getString("PR_NO")+"'"+
		                               "  and SH_ARITCLE='"+SH_NO+"'"+
                                       "  and SH_COLOR='"+SH_COLOR+"'"+
                                       "  and SH_SIZE='"+SH_SIZE+"'"+
                                       "  and FA_NO='"+getFA_NO()+"' ";
					 		    pstmtData4 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);	
							    pstmtData4.execute();
							    pstmtData4.close();	
							    
						    }
						    rs3.close();
						    pstmtData3.close();							    
						    
						    WORK_PLAN_QTY=WORK_PLAN_QTY-rs2.getDouble("USE_QTY");
			    		}
			    		
			    	}while(rs2.next());
			    	
				    rs2.close();
				    pstmtData2.close();
				    
			    }else {
				    rs2.close();
				    pstmtData2.close();
				    break;
			    }
			    
	    	 }while(rs.next());
	    	 
	    	 conn.commit();
	    }
	    rs.close();
	    pstmtData.close();
	    
	    iRet=true;	    	
		
		return iRet;
	}
	
	
}
