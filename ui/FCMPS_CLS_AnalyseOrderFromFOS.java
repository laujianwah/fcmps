package fcmps.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import dsc.common.hibernate.GenericHibernateDAO;
import dsc.common.hibernate.IGenericDAO;
import fcmps.domain.FCMPS0101_BEAN;
import fcmps.domain.FCMPS010_BEAN;
import fcmps.domain.pk.FCMPS010Pk;

/**
 * 訂單分析<br>
 * 訂單來自FOS<br>
 * 確定訂單的最晚開工周次和最晚完工周次,排產數量<br>
 * 因了解到射出是瓶頸, 故直接以射出模具的周產能,分析訂單的最完開工周次<br>
 * @author dev17
 *
 */
public class FCMPS_CLS_AnalyseOrderFromFOS {
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
	
	private ArrayList<String[]> SH_SIZE;

	private double MIN_WEEK_CAP_QTY=0;
	
	private Map<String,Double> ls_SH_SIZE_MD_PAIR_QTY=null;
	private Map<String,ArrayList<String>> ls_SH_SHARE_PART=null;
	
	private Map<String,ArrayList<String>> ls_SH_NEED_PLAN_PROC=null;
	
	private SessionFactory sessionFactory=null;
	
    private static Log log = LogFactory.getLog(FCMPS_CLS_AnalyseOrderFromFOS.class );
    
    
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

	private SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}	
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory=sessionFactory;
	}
	
	/**
	 * 取型體SIZE共模列表
	 * @return
	 */
	public Map<String, ArrayList<String>> getLs_SH_SHARE_PART() {
		return ls_SH_SHARE_PART;
	}
	
	/**
	 * 設定型體SIZE共模列表
	 * @param ls_SH_SHARE_PART
	 */

	public void setLs_SH_SHARE_PART(Map<String, ArrayList<String>> ls_SH_SHARE_PART) {
		this.ls_SH_SHARE_PART = ls_SH_SHARE_PART;
	}
	
	/**
	 * 取型體SIZE的每模雙數列表
	 * @return
	 */

	public Map<String, Double> getLs_SH_SIZE_MD_PAIR_QTY() {
		return ls_SH_SIZE_MD_PAIR_QTY;
	}
	
	/**
	 * 設定型體SIZE每模雙數列表
	 * @param ls_SH_SIZE_MD_PAIR_QTY
	 */
	public void setLs_SH_SIZE_MD_PAIR_QTY(Map<String, Double> ls_SH_SIZE_MD_PAIR_QTY) {
		this.ls_SH_SIZE_MD_PAIR_QTY = ls_SH_SIZE_MD_PAIR_QTY;
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

	public ArrayList<String[]> getSH_SIZE() {
		return SH_SIZE;
	}

	public void setSH_SIZE(ArrayList<String[]> sh_size) {
		SH_SIZE = sh_size;
	}

	public double getMIN_WEEK_CAP_QTY() {
		return MIN_WEEK_CAP_QTY;
	}

	public void setMIN_WEEK_CAP_QTY(double min_week_cap_qty) {
		MIN_WEEK_CAP_QTY = min_week_cap_qty;
	}
	
	public Map<String, ArrayList<String>> getLs_SH_NEED_PLAN_PROC() {
		return ls_SH_NEED_PLAN_PROC;
	}

	public void setLs_SH_NEED_PLAN_PROC(Map<String, ArrayList<String>> ls_SH_NEED_PLAN_PROC) {
		this.ls_SH_NEED_PLAN_PROC = ls_SH_NEED_PLAN_PROC;
	}

	public synchronized String doAnalyse(Connection conn) {
		String iRet="";
		String strSQL="";
				
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;

		try{
			
			IGenericDAO<FCMPS010_BEAN,String> dao=new GenericHibernateDAO<FCMPS010_BEAN,String>();
			dao.setSessionFactory(getSessionFactory());
			IGenericDAO<FCMPS0101_BEAN,String> dao2=new GenericHibernateDAO<FCMPS0101_BEAN,String>();
			dao2.setSessionFactory(getSessionFactory());
			
    		String SH_NO=getSH_NO();
    		
    		ArrayList<Double> ls_PROC_SEQ=getPROC_SEQ(SH_NO);
    		
    		if(ls_PROC_SEQ.isEmpty()) {
    			iRet="沒有建立型體:"+SH_NO+" 的制程順序!";
    			return iRet;
    		}    		
    		    		    		
    		double OD_QTY=getOD_QTY();    		    		    		    		    		
    	
    		String WEEK_YEAR="";
    		int WORK_WEEK_END=-1;
    		
    		//最晚開工和最晚完工周次計算優先依工廠FG Date 計算
    		//沒有FG Date 再依客人的交期計算
    		if(getOD_FGDATE()!=null) {
        		WORK_WEEK_END=Integer.valueOf(WeekUtil.getWeekOfYear(getOD_FGDATE(), true));
//        		WEEK_YEAR=FCMPS_PUBLIC.getDate(getOD_FGDATE(),"yy");        		
//        		WORK_WEEK_END=Integer.valueOf(WEEK_YEAR+FCMPS_PUBLIC.Pad(String.valueOf(WeekUtil.getWeekOfYear(getOD_FGDATE())), "0", 2, 0));
    		}else {
    			WORK_WEEK_END=Integer.valueOf(WeekUtil.getWeekOfYear(getOD_SHIP(), true));
    			
//        		WEEK_YEAR=FCMPS_PUBLIC.getDate(getOD_SHIP(),"yy");
//        		WORK_WEEK_END=Integer.valueOf(WEEK_YEAR+FCMPS_PUBLIC.Pad(String.valueOf(WeekUtil.getWeekOfYear(getOD_SHIP())), "0", 2, 0));
    		}
    		
//    		WORK_WEEK_END=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_END, 1);//加包裝出貨準備周數    		    		
    		
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
		    	LEAN_NO=getLEAN_NO(getOD_PONO1(),conn);
		    	
		    	if(getFA_NO().equals("FIC") && getBranch_Code().toUpperCase().equals("CHN")) { //上海是內銷,其它都是外銷
//		    		LEAN_NO=getBranch_Code();
		    		LEAN_NO="SHANGHAI";
		    	}
		    	
	    		String SIZE_FIELD="";
	    		for(int i=1;i<=40;i++) {
	    			if(!SIZE_FIELD.equals("")) SIZE_FIELD=SIZE_FIELD+",";
	    			SIZE_FIELD=SIZE_FIELD+"T"+i+",U"+i;
	    		}
	    		
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
			    					    					    		
			    		Double MD_PAIR_QTY=getLs_SH_SIZE_MD_PAIR_QTY().get(getFA_NO()+SH_NO+SH_SIZE);			    		
			    		if(MD_PAIR_QTY==null) {
			    			MD_PAIR_QTY=FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(getFA_NO(), SH_NO, SH_SIZE, getConnection(),WORK_WEEK_END);
			    			getLs_SH_SIZE_MD_PAIR_QTY().put(getFA_NO()+SH_NO+SH_SIZE, MD_PAIR_QTY);
			    		}
			    		
			    		double WORK_WEEKS=(int)(SIZE_OD_QTY/MD_PAIR_QTY);
			    		double WORK_DAYS=(int)(SIZE_OD_QTY/(MD_PAIR_QTY/5));
			    		
			    		if(SIZE_OD_QTY % MD_PAIR_QTY>0) WORK_WEEKS++;
			    		if(SIZE_OD_QTY % (MD_PAIR_QTY/5)>0) WORK_DAYS++;
			    		
			    		int WORK_WEEK_LAST=WORK_WEEK_END;
			    		
			    		for(int iPROC=0;iPROC<ls_PROC_SEQ.size();iPROC++) {
			    			
			    			WORK_WEEK_LAST=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_LAST, 1);
			    			
			    			ArrayList<String> ls_PROCID=getLs_SH_NEED_PLAN_PROC().get(SH_NO+ls_PROC_SEQ.get(iPROC));
			    			if(ls_PROCID==null) {
			    				ls_PROCID=getNeed_Plan_PROC(SH_NO, ls_PROC_SEQ.get(iPROC));
			    				getLs_SH_NEED_PLAN_PROC().put(SH_NO+ls_PROC_SEQ.get(iPROC), ls_PROCID);
			    			}
			    			
			    			if(!ls_PROCID.isEmpty()) { //需要排周計劃
			    				int WORK_WEEK_BEGIN=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_LAST, (int)(WORK_WEEKS-1));
			    				for(String PROCID:ls_PROCID) {
				    										    				
			    					FCMPS010_BEAN data=null;
				    				
									DetachedCriteria MPS010_DC=DetachedCriteria.forClass(FCMPS010_BEAN.class);
									MPS010_DC.add(Restrictions.eq("FA_NO",getFA_NO()));
									MPS010_DC.add(Restrictions.eq("PROCID",PROCID));
									MPS010_DC.add(Restrictions.eq("OD_PONO1",getOD_PONO1()));
									MPS010_DC.add(Restrictions.eq("SH_NO",SH_NO));
									MPS010_DC.add(Restrictions.eq("SH_SIZE",SH_SIZE));
									MPS010_DC.add(Restrictions.eq("SH_COLOR",SH_COLOR));
									
									List<FCMPS010_BEAN> LS_MPS010=dao.findByCriteria(1, MPS010_DC);
									if(!LS_MPS010.isEmpty()) {
										if(!Is_NON_Analyse_Exist_Order()) {
											data=LS_MPS010.get(0);
											
								    		data.setOD_QTY(SIZE_OD_QTY);
								    		data.setOD_SHIP(getOD_SHIP());
								    		data.setOD_FGDATE(getOD_FGDATE());
								    		data.setOD_CODE(getOD_CODE());
								    		data.setUP_DATE(new java.sql.Timestamp(new Date().getTime()));
								    		data.setUP_USER(getUP_USER());
								    		data.setWORK_WEEK_END(WORK_WEEK_LAST);
								    		data.setWORK_WEEK_START(WORK_WEEK_BEGIN);
								    		data.setWORK_WEEKS(WORK_WEEKS);
								    		data.setWORK_DAYS(WORK_DAYS);
								    		data.setMD_PAIR_QTY(MD_PAIR_QTY);
								    		data.setPROC_SEQ(ls_PROC_SEQ.get(iPROC));
								    		data.setKPR(getKPR());
								    		
											dao.update(data);
										}

									}else {
							    		data=new FCMPS010_BEAN();
							    		data.setSTYLE_NO(getSTYLE_NO());
							    		data.setFA_NO(getFA_NO());
							    		data.setLEAN_NO(LEAN_NO);
							    		data.setIS_DISABLE("N");
							    		data.setOD_PONO1(getOD_PONO1());
							    		data.setE1_PO(getE1_PO());
							    		data.setOD_QTY(SIZE_OD_QTY);
							    		data.setOD_SHIP(getOD_SHIP());
							    		data.setOD_FGDATE(getOD_FGDATE());
							    		data.setOD_CODE(getOD_CODE());
							    		data.setPROCID(PROCID);
							    		data.setSH_NO(SH_NO);
							    		data.setSH_COLOR(SH_COLOR);
							    		data.setSH_SIZE(SH_SIZE);
							    		data.setUP_DATE(new java.sql.Timestamp(new Date().getTime()));
							    		data.setUP_USER(getUP_USER());
							    		data.setWORK_PLAN_QTY(0.0);
							    		data.setEXPECT_PLAN_QTY(0.0);
							    		data.setWORK_WEEK_END(WORK_WEEK_LAST);
							    		data.setWORK_WEEK_START(WORK_WEEK_BEGIN);
							    		data.setWORK_WEEKS(WORK_WEEKS);
							    		data.setWORK_DAYS(WORK_DAYS);
							    		data.setMD_PAIR_QTY(MD_PAIR_QTY);
							    		data.setPROC_SEQ(ls_PROC_SEQ.get(iPROC));
							    		data.setKPR(getKPR());
							    		data.setIS_REPLACEMENT(getIS_REPLACEMENT());
							    		if(data.getIS_REPLACEMENT().equals("Y")) {
							    			data.setIS_DISABLE("Y");
							    		}
							    		
							    		FCMPS010Pk pk=new FCMPS010Pk();
							    		pk.setFA_NO(getFA_NO());
							    		pk.setOD_PONO1(getOD_PONO1());
							    		pk.setPROCID(PROCID);
							    		pk.setSH_COLOR(SH_COLOR);
							    		pk.setSH_NO(SH_NO);
							    		pk.setSH_SIZE(SH_SIZE);
							    		
							    		data.setFCMPS010Pk(pk);							    		
							    		
							    		dao.save(data);
									}
/*			    							
									// 分部位在第二階段,目前暫不導入
				    				if(PROCID.equals(FCMPS_PUBLIC.PROCID_SHOOT)) { //是射出制程,需要分部位
				    					ArrayList<String> ls_PARTS=getLs_SH_SHARE_PART().get(getFA_NO()+SH_NO+SH_SIZE);
				    					
						    			if(ls_PARTS==null) {
						    				ls_PARTS=FCMPS_PUBLIC.getMD_Style_Share_Part2(getFA_NO(), SH_NO, SH_SIZE, getConnection(),WORK_WEEK_END);
						    				getLs_SH_SHARE_PART().put(getFA_NO()+SH_NO+SH_SIZE, ls_PARTS);
						    			}
						    			
						    			if(!ls_PARTS.isEmpty()) {
						    				
						    				FCMPS0101_BEAN sdata=null;
						    				
						    				for(int n=0;n<ls_PARTS.size();n++) {
						    					
						    					String PART_NO=ls_PARTS.get(n);
						    					
												DetachedCriteria MPS0101_DC=DetachedCriteria.forClass(FCMPS0101_BEAN.class);
												MPS0101_DC.add(Restrictions.eq("FA_NO",getFA_NO()));
												MPS0101_DC.add(Restrictions.eq("PROCID",FCMPS_PUBLIC.PROCID_SHOOT));
												MPS0101_DC.add(Restrictions.eq("OD_PONO1",getOD_PONO1()));
												MPS0101_DC.add(Restrictions.eq("SH_NO",SH_NO));
												MPS0101_DC.add(Restrictions.eq("SH_SIZE",SH_SIZE));
												MPS0101_DC.add(Restrictions.eq("SH_COLOR",SH_COLOR));
												MPS0101_DC.add(Restrictions.eq("PART_NO",PART_NO));
												
												List<FCMPS0101_BEAN> LS_MPS0101=dao2.findByCriteria(1, MPS0101_DC);
												if(!LS_MPS0101.isEmpty()) {														
													if(!Is_NON_Analyse_Exist_Order()) {
														sdata=LS_MPS0101.get(0);
											    		sdata.setOD_QTY(SIZE_OD_QTY);
											    		sdata.setUP_DATE(new java.sql.Timestamp(new Date().getTime()));
											    		sdata.setUP_USER(getUP_USER());
											    		sdata.setPROC_SEQ(ls_PROC_SEQ.get(iPROC));
											    		
														dao2.update(sdata);
													}
												}else {
										    		sdata=new FCMPS0101_BEAN();
										    		sdata.setSTYLE_NO(getSTYLE_NO());
										    		sdata.setFA_NO(getFA_NO());
										    		sdata.setOD_PONO1(getOD_PONO1());
										    		sdata.setOD_QTY(SIZE_OD_QTY);
										    		sdata.setPROCID(FCMPS_PUBLIC.PROCID_SHOOT);
										    		sdata.setSH_NO(SH_NO);
										    		sdata.setSH_COLOR(SH_COLOR);
										    		sdata.setSH_SIZE(SH_SIZE);
										    		sdata.setUP_DATE(new java.sql.Timestamp(new Date().getTime()));
										    		sdata.setUP_USER(getUP_USER());
										    		sdata.setPROC_SEQ(ls_PROC_SEQ.get(iPROC));
										    		sdata.setPART_NO(PART_NO);
										    		
										    		FCMPS0101Pk pk=new FCMPS0101Pk();
										    		pk.setFA_NO(getFA_NO());
										    		pk.setOD_PONO1(getOD_PONO1());
										    		pk.setPROCID(PROCID);
										    		pk.setSH_COLOR(SH_COLOR);
										    		pk.setSH_NO(SH_NO);
										    		pk.setSH_SIZE(SH_SIZE);
										    		pk.setPART_NO(PART_NO);
										    		
										    		sdata.setFCMPS0101Pk(pk);
										    		
										    		dao2.save(sdata);
												}								    		
						    				}
						    			}						    									    					
				    				}
*/				    				
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
	    	sqlex.printStackTrace();
	    	iRet=sqlex.getMessage();
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

			strSQL="select distinct nvl(lean_no, '無') lean_no "+
                   "from dsod00, fcpb07 "+
                   "where dsod00.cu_dest = fcpb07.cu_dest(+) "+
                   "  and dsod00.od_pono1 = '"+OD_PONO1+"'";
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
