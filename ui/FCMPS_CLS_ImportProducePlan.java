package fcmps.ui;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import dsc.common.hibernate.GenericHibernateDAO;
import dsc.common.hibernate.IGenericDAO;
import fcmps.domain.FCMPS010_BEAN;
import fcmps.domain.pk.FCMPS010Pk;

public class FCMPS_CLS_ImportProducePlan implements Callable {
//	private String config_xml="";
    private File file=null;
	private String UP_USER="DEV";	
	private String FA_NO="";
	private String LEAN_NO="";
	
	private boolean isDeleted=false;
	/**
	 * 先清空系統中的訂單和庫存,再導入此次的排程
	 */
	public static final int PROCESS_WAY_BEFORE_CLEAR_ALL=3;
	/**
	 * 將型體原有的訂單和庫存先刪除,再導入型體的排程
	 */
	public static final int PROCESS_WAY_COVER_OLD_DATE=1;
	/**
	 * 此次導入的排程只是追加訂單,如果訂單已存在,則更新訂單數量, 不更新庫存數量
	 */
	public static final int PROCESS_WAY_ONLY_APPEND_ORDER=2;
	
	private int PROCESS_WAY=PROCESS_WAY_ONLY_APPEND_ORDER;
	
	private Stack<Import_Produce_Plan_Connection_Wrap> conn_stack=null;
	
	public String getUP_USER() {
		return UP_USER;
	}

	public void setUP_USER(String up_user) {
		UP_USER = up_user;
	}

	public String getFA_NO() {
		return FA_NO;
	}

	public void setFA_NO(String fa_no) {
		FA_NO = fa_no;
	}

	public String getLEAN_NO() {
		return LEAN_NO;
	}

	public void setLEAN_NO(String lean_no) {
		LEAN_NO = lean_no;
	}

	/**
	 * 取處理方式
	 * @return
	 */	
	public int getPROCESS_WAY() {
		return PROCESS_WAY;
	}
	
	/**
	 * 設定處理方式, 預計為: 此次導入的排程只是追加訂單,如果訂單已存在,則更新訂單數量, 不更新庫存數量
	 * @param process_way
	 */

	public void setPROCESS_WAY(int process_way) {
		PROCESS_WAY = process_way;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public void setConnection_stack(Stack<Import_Produce_Plan_Connection_Wrap> conn_stack) {
		this.conn_stack = conn_stack;
	}

	public List<Style_WorkBook_Info> call(){
		List<Style_WorkBook_Info> list_work_book_info=new ArrayList<Style_WorkBook_Info>();
		doImport(getFile(), list_work_book_info);
		return list_work_book_info;
	}
	
	public void doImport(String file_name,List<Style_WorkBook_Info> list_work_book_info) {
		File file = new File(file_name);	 
		doImport(file,list_work_book_info);
	}
	
	private void doImport(File file,List<Style_WorkBook_Info> list_work_book_info) {
		
		Session session=null;
		SessionFactory sessionFactory=null;
		Connection conn=null;
		
		Map<String,Double> ls_SH_SIZE_MD_PAIR_QTY=new HashMap<String,Double>();
		Map<String,List<Double>> set_SH_PROC_SEQ=new HashMap<String,List<Double>>();
		List<FCMPS010_BEAN> ls_DataObjectSet=new ArrayList<FCMPS010_BEAN>();
		Map<String,ArrayList<String>> ls_SH_NEED_PLAN_PROC=new HashMap<String,ArrayList<String>>();
		
		Style_WorkBook_Info work_book_info=new Style_WorkBook_Info();
		list_work_book_info.add(work_book_info);
		
		Import_Produce_Plan_Connection_Wrap conn_wrap=conn_stack.pop();
		
		try {

			System.out.println(new Date()+" "+file.getName()+" "+"連接資料庫");

/*			try {
				
				File fConfig=new File(getConfig_XML());
				if(!fConfig.exists()) {
					work_book_info.getMessage().add( "The Config file " + getConfig_XML()+" does not exist!" );
					System.out.println( "The Config file " + getConfig_XML()+" does not exist!" );
					return;
				}
				
				Configuration config=new Configuration().configure(fConfig);	
				config.addClass(FCMPS0101_BEAN.class);
				config.addClass(FCMPS010_BEAN.class);
				sessionFactory=config.buildSessionFactory();	
				
				String USER=config.getProperty("connection.username");
				String URL=config.getProperty("connection.url");
				String PSW=config.getProperty("connection.password");
				String DRIVER=config.getProperty("connection.driver_class");
				
	    		Class.forName(DRIVER); //加載驅動程序
	    		conn=DriverManager.getConnection(URL,USER,PSW);
				
	    		session=sessionFactory.openSession(conn);

	    		session.setFlushMode(FlushMode.COMMIT);
	    		
			}catch(Exception ex) {
				work_book_info.getMessage().add(file.getName()+" 無法連接資料庫!");
				return;
			}*/
		    
			if(conn_wrap==null) {
				work_book_info.getMessage().add(file.getName()+" 無法取得資料庫連線!");
				return;
			}
			
			session=conn_wrap.getSession();
			sessionFactory=conn_wrap.getSessionFactory();
			conn=conn_wrap.getConnection();

			if(conn==null) {
				work_book_info.getMessage().add(file.getName()+" 無法取得資料庫連線!");
				return;
			}
			
			System.out.println(new Date()+" "+"開始讀取排程");
			
			FileInputStream fileIn = new FileInputStream(file);
			if(fileIn==null) {
				work_book_info.getMessage().add("無法讀取:"+file.getName());
				System.out.println("無法讀取:"+file.getName());
				return;
			}
			
			if(getPROCESS_WAY()==PROCESS_WAY_BEFORE_CLEAR_ALL) {
				if(!doDeleteOldData(getFA_NO(), getLEAN_NO(),conn,session)) {
					work_book_info.getMessage().add("系統中原有訂單沒有刪除成功!");
					return;
				}
			}
			
			HSSFWorkbook wb=new HSSFWorkbook(fileIn);

			for(int i=0;i<wb.getNumberOfSheets();i++) {
				HSSFSheet sheet = wb.getSheetAt(i);		 			
								
				if(sheet!=null) {
				
					if(i>0) {						
						work_book_info=new Style_WorkBook_Info();
						list_work_book_info.add(work_book_info);												
					}
					
					work_book_info.setFile_Name(file.getName());
					work_book_info.setSheet_Name(wb.getSheetName(i));
										
					System.out.println(new Date()+" "+"開始讀取第"+i+"頁");
					if(!readSheet(
							sheet,
							ls_SH_SIZE_MD_PAIR_QTY,
							set_SH_PROC_SEQ,
							ls_DataObjectSet,
							ls_SH_NEED_PLAN_PROC,
							work_book_info,
							conn,
							session)){
						return;
					}
					
				}
								
			}
			
			fileIn.close();
			wb=null;
			
			System.out.println(new Date()+" "+"排程讀取完畢!");
			
			if(ls_DataObjectSet.isEmpty()) return;
			
			IGenericDAO<FCMPS010_BEAN,String> dao=new GenericHibernateDAO<FCMPS010_BEAN,String>();
			dao.setSessionFactory(sessionFactory);
			
			int rec_no=0;
			for(FCMPS010_BEAN data:ls_DataObjectSet) {
				rec_no++;
				DetachedCriteria MPS010_DC=DetachedCriteria.forClass(FCMPS010_BEAN.class);
				MPS010_DC.add(Restrictions.eq("FA_NO",data.getFA_NO()));
				MPS010_DC.add(Restrictions.eq("PROCID",data.getPROCID()));
				MPS010_DC.add(Restrictions.eq("OD_PONO1",data.getOD_PONO1()));
				MPS010_DC.add(Restrictions.eq("SH_NO",data.getSH_NO()));
				MPS010_DC.add(Restrictions.eq("SH_SIZE",data.getSH_SIZE()));
				MPS010_DC.add(Restrictions.eq("SH_COLOR",data.getSH_COLOR()));
				
				List<FCMPS010_BEAN> LS_MPS010=dao.findByCriteria(1, MPS010_DC);
				if(!LS_MPS010.isEmpty()) {
					FCMPS010_BEAN data2=LS_MPS010.get(0);
					data2.setOD_FGDATE(data.getOD_FGDATE());
					data2.setOD_SHIP(data.getOD_SHIP());
					data2.setOD_QTY(data.getOD_QTY());
					data2.setWORK_WEEK_END(data.getWORK_WEEK_END());
					data2.setWORK_WEEK_START(data.getWORK_WEEK_START());
					data2.setLEAN_NO(data.getLEAN_NO());
					dao.update(data2);
//					dao.delete(data2);
				}else {
					dao.save(data);
				}
				
				if(rec_no==1000) {//每一千筆更新到資料庫.
					session.flush();
					rec_no=0;
				}
			}

			session.flush();
			System.out.println(new Date()+" "+"將排程數據寫入資料庫!");
			
		}catch(Exception ex) {
			ex.printStackTrace();
			work_book_info.getMessage().add(file.getName()+" "+ex.getMessage());
			
		}finally {
//			closeConnection(conn);
//			session.close();
//			sessionFactory.close();
//			sessionFactory=null;
			if(conn_wrap!=null) conn_stack.push(conn_wrap);
		}
		
	}
	
    private boolean readSheet(
    		HSSFSheet sheet,
    		Map<String,Double> ls_SH_SIZE_MD_PAIR_QTY,
    		Map<String,List<Double>> set_SH_PROC_SEQ,
    		List<FCMPS010_BEAN> ls_DataObjectSet,
    		Map<String,ArrayList<String>> ls_SH_NEED_PLAN_PROC,
    		Style_WorkBook_Info work_book_info,
    		Connection conn,
    		Session session){
    	boolean iRet=false;
		
		int iRow=0;    		
		
		HSSFRow row = null;			
		HSSFCell cell = null;			
		row=sheet.getRow(iRow);		
		
		int row_E1_PO=4;
		int row_Crocs_Remarks=5;
		int row_Required_Ship_Date=10;
		int row_Factory_Promised_Ship_Date=14;
		
		int row_Detail_Start=17;
		
		int col_Inventory=3;
		int col_COLOR=0;
		int col_SIZE=2;
		
//		iRow++;

		work_book_info.setTotal_Rows(sheet.getLastRowNum());
		
		while(iRow<sheet.getLastRowNum()){
			iRow++;
			row=sheet.getRow(iRow);
			if(row==null) continue;
			
			cell = row.getCell((short)0);
			if(FCMPS_PUBLIC.getCellValue(cell)==null) break;
			
			Object obj=FCMPS_PUBLIC.getCellValue(cell,2);
			
			if(obj==null) continue;
			
			if(obj.equals("SL/FB")) {
				row_Detail_Start=iRow;
				break;
			}
			
			if(obj.equals("E1 PO")) {
				row_E1_PO=iRow;
			}
			
			if(obj.equals("Required Ship Date")) {
				row_Required_Ship_Date=iRow;
			}
			
			if(obj.equals("Factory Promised Ship Date")) {
				row_Factory_Promised_Ship_Date=iRow;
			}			
			
			
		}    	
		
		row=sheet.getRow(0);
			
		cell = row.getCell((short)0);
		String SH_NO=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,2));
		
		work_book_info.setSH_NO(SH_NO);
		
		if(getPROCESS_WAY()==PROCESS_WAY_COVER_OLD_DATE && !isDeleted) {
			if(!doDeleteOldData(getFA_NO(), SH_NO,getLEAN_NO(),conn,session)) {
				work_book_info.getMessage().add("型體:"+SH_NO+" 的原有數量沒有刪除成功!");
				return false;
			}else {
				isDeleted=true;
			}
		}
			
		List<Double> ls_PROC_SEQ=set_SH_PROC_SEQ.get(SH_NO);
		if(ls_PROC_SEQ==null) {
			ls_PROC_SEQ=getPROC_SEQ(SH_NO, conn);
			set_SH_PROC_SEQ.put(SH_NO, ls_PROC_SEQ);
		}
		
		if(ls_PROC_SEQ.isEmpty()) {
			work_book_info.getMessage().add("型體:"+SH_NO+" 沒有制程資料,請先建立!");
			return false;
		}
		
		HSSFRow HSSFRow_E1_PO=sheet.getRow(row_E1_PO);
//		HSSFRow HSSFRow_Crocs_Remarks=sheet.getRow(row_Crocs_Remarks);
		HSSFRow HSSFRow_Required_Ship_Date=sheet.getRow(row_Required_Ship_Date);
		HSSFRow HSSFRow_Factory_Promised_Ship_Date=sheet.getRow(row_Factory_Promised_Ship_Date);
		
		iRow=row_Detail_Start;
		while(iRow<=sheet.getLastRowNum()){
			iRow++;
			
			System.out.println(new Date()+" "+"開始讀取第"+iRow+"行");
			
			row=sheet.getRow(iRow);
			if(row==null) break;
			
			cell = row.getCell((short)col_COLOR);
			if(FCMPS_PUBLIC.getCellValue(cell)==null) break;
			
			Object obj=FCMPS_PUBLIC.getCellValue(cell,2);
			
			if(obj.equals("Grand Total"))break;
			
			if(obj.equals("Total"))continue;
			
			String SH_COLOR=FCMPS_PUBLIC.getValue(obj).toUpperCase();
			
			cell = row.getCell((short)col_SIZE);
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			String SH_SIZE=FCMPS_PUBLIC.getValue(obj);
			
			cell = row.getCell((short)col_Inventory);
			obj=FCMPS_PUBLIC.getCellValue(cell,1);
			int MT_QTY=FCMPS_PUBLIC.getInt(obj);
						
			if(MT_QTY>0) {
				if(!doInsertFCMPS016(getFA_NO(), SH_NO, SH_COLOR, SH_SIZE, getLEAN_NO(), MT_QTY, conn)) {
					work_book_info.getMessage().add("型體:"+SH_NO+" 配色:"+SH_COLOR+" SIZE:"+SH_SIZE+" 的庫存沒有存入系統!");
					System.out.println("型體:"+SH_NO+" 配色:"+SH_COLOR+" SIZE:"+SH_SIZE+" 的庫存沒有存入系統!");
					continue;	
				}
				work_book_info.setInventory(work_book_info.getInventory()+MT_QTY);
				
			}
						
			int iCol=col_Inventory;
			do {
				
				iCol++;
				if(iCol==256) break; //最大列數 256
				
				boolean is_END=true;
				int iStartCol=iCol;
				for(int i=0;i<15;i++) {
					cell = HSSFRow_E1_PO.getCell((short)iCol);
					if(FCMPS_PUBLIC.getCellValue(cell)!=null) {
						is_END=false;
						break;
					}
					iCol++;
					if(iCol==256) break; //最大列數 256
				}
				
				if(is_END) break;

				for(int i=iStartCol;i<iCol;i++) {
					if(work_book_info.getMessage().isEmpty()) {
						work_book_info.getMessage().add("第:"+(iStartCol+1)+"列沒有PO#");	
					}else {
	    				boolean iExist=false;
	    				for(String msg:work_book_info.getMessage()) {
	    					if(msg.equals("第:"+(iStartCol+1)+"列沒有PO#")) {
	    						iExist=true;
	    						break;
	    					}
	    				}
	    				if(!iExist) work_book_info.getMessage().add("第:"+(iStartCol+1)+"列沒有PO#");
					}					
				}
				
				work_book_info.setTotal_Columns(iCol);
				
				cell = HSSFRow_E1_PO.getCell((short)iCol);				
				obj=FCMPS_PUBLIC.getCellValue(cell,2);
				String OD_PONO1=FCMPS_PUBLIC.getValue(obj);
				
				cell = row.getCell((short)iCol);
				obj=FCMPS_PUBLIC.getCellValue(cell,0);
				double SIZE_OD_QTY=FCMPS_PUBLIC.getDouble(obj);
				if(SIZE_OD_QTY==0) continue;
			
				work_book_info.setOD_QTY(work_book_info.getOD_QTY()+SIZE_OD_QTY);
				
				Date OD_SHIP=null;
				cell = HSSFRow_Required_Ship_Date.getCell((short)iCol);
				obj=FCMPS_PUBLIC.getCellValue(cell,2);
				if(!FCMPS_PUBLIC.getValue(obj).equals("")&&!FCMPS_PUBLIC.getValue(obj).equals("NaN")){
					Calendar cal=Calendar.getInstance();
					cal.clear();
					if(FCMPS_PUBLIC.getDouble(obj)!=0){
						long l_date=Long.valueOf((long)FCMPS_PUBLIC.getDouble(obj));
						l_date=l_date-25569+1; //因為Excel中的時間是從1900/01/01 00:00:00 開始						
						cal.set(Calendar.DAY_OF_YEAR, Long.valueOf(l_date).intValue());						
						
						OD_SHIP=cal.getTime();
					}

				}

				if(OD_SHIP==null) {
					work_book_info.getMessage().add("訂單號:"+OD_PONO1+" 型體:"+SH_NO+" 配色:"+SH_COLOR+" SIZE:"+SH_SIZE+" 沒有客戶需求日期!");
					continue;
				}
									
				Date OD_FGDATE=null;
				cell = HSSFRow_Factory_Promised_Ship_Date.getCell((short)iCol);
				obj=FCMPS_PUBLIC.getCellValue(cell,2);
				if(!FCMPS_PUBLIC.getValue(obj).equals("")&&!FCMPS_PUBLIC.getValue(obj).equals("NaN")){
					Calendar cal=Calendar.getInstance();
					cal.clear();
					if(FCMPS_PUBLIC.getDouble(obj)!=0){
						long l_date=Long.valueOf((long)FCMPS_PUBLIC.getDouble(obj));
						l_date=l_date-25569+1; //因為Excel中的時間是從1900/01/01 00:00:00 開始						
						cal.set(Calendar.DAY_OF_YEAR, Long.valueOf(l_date).intValue());						
						
						OD_FGDATE=cal.getTime();
					}
				}
				
				if(OD_FGDATE==null) {
					work_book_info.getMessage().add("訂單號:"+OD_PONO1+" 型體:"+SH_NO+" 配色:"+SH_COLOR+" SIZE:"+SH_SIZE+" 沒有FG Date!");
					continue;
				}
				
				int WORK_WEEK_END=Integer.valueOf(WeekUtil.getWeekOfYear(OD_FGDATE, true));
				
	    		Double MD_PAIR_QTY=ls_SH_SIZE_MD_PAIR_QTY.get(getFA_NO()+SH_NO+SH_SIZE);			    		
	    		if(MD_PAIR_QTY==null) {
	    			MD_PAIR_QTY=FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(getFA_NO(), SH_NO, SH_SIZE, conn,WORK_WEEK_END);
	    			ls_SH_SIZE_MD_PAIR_QTY.put(getFA_NO()+SH_NO+SH_SIZE, MD_PAIR_QTY);
	    		}
	    		
	    		if(MD_PAIR_QTY==0) {
	    			if(work_book_info.getMessage().isEmpty()) {
	    				work_book_info.getMessage().add("型體:"+SH_NO+" 配色:"+SH_COLOR+" SIZE:"+SH_SIZE+" 沒有模具資料!");
	    			}else {
	    				boolean iExist=false;
	    				for(String msg:work_book_info.getMessage()) {
	    					if(msg.equals("型體:"+SH_NO+" 配色:"+SH_COLOR+" SIZE:"+SH_SIZE+" 沒有模具資料!")) {
	    						iExist=true;
	    						break;
	    					}
	    				}
	    				if(!iExist) work_book_info.getMessage().add("型體:"+SH_NO+" 配色:"+SH_COLOR+" SIZE:"+SH_SIZE+" 沒有模具資料!");
	    			}
	    			System.out.println("row:"+iRow+" col:"+iCol+" 型體:"+SH_NO+" 配色:"+SH_COLOR+" SIZE:"+SH_SIZE+" 沒有模具資料!");
	    			continue;
	    		}
	    		
	    		double WORK_WEEKS=(int)(SIZE_OD_QTY/MD_PAIR_QTY);
	    		double WORK_DAYS=(int)(SIZE_OD_QTY/(MD_PAIR_QTY/5));
	    		
	    		if(SIZE_OD_QTY % MD_PAIR_QTY>0) WORK_WEEKS++;
	    		if(SIZE_OD_QTY % (MD_PAIR_QTY/5)>0) WORK_DAYS++;
	    		
	    		int WORK_WEEK_LAST=WORK_WEEK_END;
	    		
				for(int iPROC=0;iPROC<ls_PROC_SEQ.size();iPROC++) {
					WORK_WEEK_LAST=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_LAST, 1);
					
					do {
						if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(getFA_NO(), WORK_WEEK_LAST, conn)==0) {
							WORK_WEEK_LAST=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_LAST, 1);
						}else {
							break;
						}
					}while(true);
					
	    			ArrayList<String> ls_PROCID=ls_SH_NEED_PLAN_PROC.get(SH_NO+ls_PROC_SEQ.get(iPROC));
	    			if(ls_PROCID==null) {
	    				ls_PROCID=getNeed_Plan_PROC(SH_NO, ls_PROC_SEQ.get(iPROC),conn);
	    				ls_SH_NEED_PLAN_PROC.put(SH_NO+ls_PROC_SEQ.get(iPROC), ls_PROCID);
	    			}
	    			
	    			if(!ls_PROCID.isEmpty()) { //需要排周計劃
	    				int WORK_WEEK_BEGIN=WORK_WEEK_LAST;
	    				for(int iweek=0;iweek<(int)WORK_WEEKS-1;iweek++) {
	    					do {
	    						WORK_WEEK_BEGIN=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_BEGIN, 1);
	    						if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(getFA_NO(), WORK_WEEK_BEGIN, conn)==0) {
	    							WORK_WEEK_BEGIN=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_BEGIN, 1);
	    						}else {
	    							break;
	    						}
	    					}while(true);
	    					
	    				}
//	    				int WORK_WEEK_BEGIN=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_LAST, (int)(WORK_WEEKS-1));
	    				for(String PROCID:ls_PROCID) {
	    					FCMPS010_BEAN data=new FCMPS010_BEAN();
	    		    		data.setSTYLE_NO(getSTYLE_NO(SH_NO, conn));
	    		    		data.setFA_NO(getFA_NO());
	    		    		data.setLEAN_NO(getLEAN_NO());
	    		    		data.setIS_DISABLE("N");
	    		    		data.setOD_PONO1(OD_PONO1);
//	    		    		data.setE1_PO(E1_PO);
	    		    		data.setOD_QTY(SIZE_OD_QTY);
	    		    		data.setOD_SHIP(OD_SHIP);
	    		    		data.setOD_FGDATE(OD_FGDATE);
	    		    		data.setOD_CODE("N");
	    		    		data.setPROCID(PROCID);
	    		    		data.setSH_NO(SH_NO);
	    		    		data.setSH_COLOR(SH_COLOR);
	    		    		data.setSH_SIZE(SH_SIZE);
	    		    		data.setUP_DATE(new java.sql.Timestamp(new Date().getTime()));
	    		    		data.setUP_USER(getUP_USER());
	    		    		data.setWORK_PLAN_QTY(0.0);
	    		    		data.setEXPECT_PLAN_QTY(0.0);
	    		    		data.setREPLACED_QTY(0.0);
	    		    		data.setWORK_WEEK_END(WORK_WEEK_LAST);
	    		    		data.setWORK_WEEK_START(WORK_WEEK_BEGIN);
	    		    		data.setWORK_WEEKS(WORK_WEEKS);
	    		    		data.setWORK_DAYS(WORK_DAYS);
	    		    		data.setMD_PAIR_QTY(MD_PAIR_QTY);
	    		    		data.setPROC_SEQ(ls_PROC_SEQ.get(iPROC));
	    		    		data.setKPR("N");
	    		    		data.setIS_REPLACEMENT("N");
	    		    		if(data.getIS_REPLACEMENT().equals("Y")) {
	    		    			data.setIS_DISABLE("Y");
	    		    		}
	    		    		
	    		    		FCMPS010Pk pk=new FCMPS010Pk();
	    		    		pk.setFA_NO(getFA_NO());
	    		    		pk.setOD_PONO1(OD_PONO1);
	    		    		pk.setPROCID(PROCID);
	    		    		pk.setSH_COLOR(SH_COLOR);
	    		    		pk.setSH_NO(SH_NO);
	    		    		pk.setSH_SIZE(SH_SIZE);
	    		    		
	    		    		data.setFCMPS010Pk(pk);							    		
	    		    		
	    		    		boolean iExist=false;
	    		    		for(FCMPS010_BEAN dataobj:ls_DataObjectSet) {
	    		    			if(dataobj.getFCMPS010Pk().equals(data.getFCMPS010Pk())) {
	    		    				iExist=true;
	    		    				break;
	    		    			}
	    		    		}
	    		    		
	    		    		if(iExist) {
	    	    				iExist=false;
	    	    				for(String msg:work_book_info.getMessage()) {
	    	    					if(msg.equals("訂單:"+OD_PONO1+" 型體:"+SH_NO+" 配色:"+SH_COLOR+" SIZE:"+SH_SIZE+" 已存在!")) {
	    	    						iExist=true;
	    	    						break;
	    	    					}
	    	    				}
	    	    				if(!iExist) {
	    	    					work_book_info.getMessage().add("訂單:"+OD_PONO1+" 型體:"+SH_NO+" 配色:"+SH_COLOR+" SIZE:"+SH_SIZE+" 已存在!");
	    	    					System.out.println("row:"+iRow+" col:"+iCol+" 訂單:"+OD_PONO1+" 型體:"+SH_NO+" 配色:"+SH_COLOR+" SIZE:"+SH_SIZE+" 已存在!");
	    	    				}
	    		    		}else {
	    		    			ls_DataObjectSet.add(data);	    		    			
	    		    		}
	    		    		
	    				}
	    			}	    			
				}

			}while(true);
			
		}
		
		iRet=true;
    	return iRet;
    }  
	
	/**
	 * 取得型體
	 * @return
	 */
	private String getSTYLE_NO(String SH_NO,Connection conn){
		String iRet="無";

		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			strSQL="select SKU from ficsku01 where MODEL_CNA ='"+SH_NO+"'";
		
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(FCMPS_PUBLIC.getValue(rs.getString("SKU")).split("-").length>0)
		    	  iRet=FCMPS_PUBLIC.getValue(rs.getString("SKU")).split("-")[0];
		    }
		    rs.close();
		    pstmtData.close();
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	

		}
	    
	    return iRet;
	}
	
	/**
	 * 取型體需要的制程順序
	 * @param SH_NO
	 * @return
	 */
	private List<Double> getPROC_SEQ(String SH_NO,Connection conn) {
		ArrayList<Double> iRet=new ArrayList<Double>();
		String strSQL="";

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
	
	/**
	 * 型體制程是否需要排計劃<br>
	 * 因為有射出和針車,針車和組底,針車和轉印在同一周生產,且都要排計劃,故返回ArrayList <br>
	 * @param SH_NO
	 * @param PROC_SEQ 制程順序
	 * @return String 需要排計劃則返回制程代號, 否則返回空
	 */
	private ArrayList<String> getNeed_Plan_PROC(String SH_NO,double PROC_SEQ,Connection conn) {
		ArrayList<String> iRet=new ArrayList<String>();
		String strSQL="";

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
	
	private boolean doDeleteOldData(String FA_NO,String SH_NO,Connection conn) {
        boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		
		try{

			strSQL="delete from FCMPS016 " +
		           " where FA_NO='"+FA_NO+"' " +
		           "   and SH_ARITCLE='"+SH_NO+"' ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();

			strSQL="delete from FCMPS010 " +
		           " where FA_NO='"+FA_NO+"' " +
		           "   and SH_NO='"+SH_NO+"' ";
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

	private boolean doDeleteOldData(String FA_NO,String LEAN_NO,Connection conn,Session session) {
        boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		
		try{

			strSQL="delete from FCMPS016 " +
		           " where FA_NO='"+FA_NO+"' " +
		           "   and (case when LEAN_NO='SHANGHAI' then 'SHANGHAI' else '無' end)='"+LEAN_NO+"' ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();

			strSQL="delete from FCMPS017 " +
	               " where FA_NO='"+FA_NO+"' " +
	               "   and USE_TYPE='ST' "+
	               "   and (case when LEAN_NO='SHANGHAI' then 'SHANGHAI' else '無' end)='"+LEAN_NO+"' ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();
		
			strSQL="delete from FCMPS010 " +
		           " where FA_NO='"+FA_NO+"' " +
		           "   and (case when LEAN_NO='SHANGHAI' then 'SHANGHAI' else '無' end)='"+LEAN_NO+"' ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();

			session.flush();
			iRet=true;
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	private boolean doDeleteOldData(String FA_NO,String SH_NO,String LEAN_NO,Connection conn,Session session) {
        boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		
		try{

			strSQL="delete from FCMPS016 " +
		           " where FA_NO='"+FA_NO+"' " +
		           "   and SH_ARITCLE='"+SH_NO+"' "+
		           "   and (case when LEAN_NO='SHANGHAI' then 'SHANGHAI' else '無' end)='"+LEAN_NO+"' ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();

			strSQL="delete from FCMPS017 " +
	               " where FA_NO='"+FA_NO+"' " +
	               "   and SH_ARITCLE='"+SH_NO+"' "+
	               "   and USE_TYPE='ST' "+
	               "   and (case when LEAN_NO='SHANGHAI' then 'SHANGHAI' else '無' end)='"+LEAN_NO+"' ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();
		
			strSQL="delete from FCMPS010 " +
		           " where FA_NO='"+FA_NO+"' " +
		           "   and SH_NO='"+SH_NO+"' "+
		           "   and (case when LEAN_NO='SHANGHAI' then 'SHANGHAI' else '無' end)='"+LEAN_NO+"' ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();

			session.flush();
			iRet=true;
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	/**
	 * 記錄庫存數
	 */
	private boolean doInsertFCMPS016(
			String FA_NO,
			String SH_NO,
			String SH_COLOR,
			String SH_SIZE,
			String LEAN_NO,
			double MT_QTY,
			Connection conn) {
        boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;	
		
		try{

			strSQL="select * from FCMPS016 " +
				   "where FA_NO='"+FA_NO+"' " +
				   "  and SH_ARITCLE='"+SH_NO+"' " +
				   "  and SH_COLOR='"+SH_COLOR+"'" +
				   "  and SH_SIZE='"+SH_SIZE+"'" +
				   "  and LEAN_NO='"+LEAN_NO+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(getPROCESS_WAY()==PROCESS_WAY_COVER_OLD_DATE || getPROCESS_WAY()==PROCESS_WAY_BEFORE_CLEAR_ALL) {
					strSQL="delete from FCMPS016 " +
				       " where FA_NO='"+FA_NO+"' " +
				       "   and SH_ARITCLE='"+SH_NO+"' " +
				       "   and SH_COLOR='"+SH_COLOR+"'" +
				       "   and SH_SIZE='"+SH_SIZE+"'" +
				       "   and LEAN_NO='"+LEAN_NO+"'";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.execute();
					pstmtData2.close();
					
					strSQL="insert into FCMPS016 (sh_aritcle, sh_color, sh_size, mt_qty, up_date, fa_no, lean_no, org_qty) "+
		               "values ("+
		               "'"+SH_NO+"'"+
		               ",'"+SH_COLOR+"'"+
		               ",'"+SH_SIZE+"'"+
		               ","+MT_QTY+
		               ",sysdate"+
		               ",'"+FA_NO+"'"+
		               ",'"+LEAN_NO+"'"+
		               ","+MT_QTY+")";
					
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.execute();
					pstmtData2.close();
										
		    	}

		    }else {
		    	if(getPROCESS_WAY()==PROCESS_WAY_COVER_OLD_DATE || getPROCESS_WAY()==PROCESS_WAY_BEFORE_CLEAR_ALL) {
					strSQL="insert into FCMPS016 (sh_aritcle, sh_color, sh_size, mt_qty, up_date, fa_no, lean_no, org_qty) "+
		                   "values ("+
		                   "'"+SH_NO+"'"+
		                   ",'"+SH_COLOR+"'"+
		                   ",'"+SH_SIZE+"'"+
		                   ","+MT_QTY+
		                   ",sysdate"+
		                   ",'"+FA_NO+"'"+
		                   ",'"+LEAN_NO+"'"+
		                   ","+MT_QTY+")";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.execute();
					pstmtData2.close();
		    	}

		    }
			rs.close();
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
	
	public class Style_WorkBook_Info{
		private String SH_NO="";
		private int Inventory=0;
		private String Sheet_Name="";
		private int total_Columns=0;
		private int total_Rows=0;
		private double OD_QTY=0;
		private List<String> ls_Message=new ArrayList<String>();
		private String File_Name="";
		
		public int getInventory() {
			return Inventory;
		}
		public void setInventory(int inventory) {
			Inventory = inventory;
		}
		public double getOD_QTY() {
			return OD_QTY;
		}
		public void setOD_QTY(double od_qty) {
			OD_QTY = od_qty;
		}
		public String getSH_NO() {
			return SH_NO;
		}
		public void setSH_NO(String sh_no) {
			SH_NO = sh_no;
		}
		public String getSheet_Name() {
			return Sheet_Name;
		}
		public void setSheet_Name(String sheet_Name) {
			Sheet_Name = sheet_Name;
		}
		public int getTotal_Columns() {
			return total_Columns;
		}
		public void setTotal_Columns(int total_Columns) {
			this.total_Columns = total_Columns;
		}
		public int getTotal_Rows() {
			return total_Rows;
		}
		public void setTotal_Rows(int total_Rows) {
			this.total_Rows = total_Rows;
		}
		
		public List<String> getMessage() {
			return ls_Message;
		}
		
		public void setMessage(List<String> ls_Message) {
			this.ls_Message = ls_Message;
		}
		
		public String getFile_Name() {
			return File_Name;
		}
		
		public void setFile_Name(String file_Name) {
			File_Name = file_Name;
		}
		
	}
	
	public static class Import_Produce_Plan_Connection_Wrap{
		private Session session=null;
		private SessionFactory sessionFactory=null;
		private Connection conn=null;
		
		public Connection getConnection() {
			return conn;
		}
		public void setConnection(Connection conn) {
			this.conn = conn;
		}
		public Session getSession() {
			return session;
		}
		public void setSession(Session session) {
			this.session = session;
		}
		public SessionFactory getSessionFactory() {
			return sessionFactory;
		}
		public void setSessionFactory(SessionFactory sessionFactory) {
			this.sessionFactory = sessionFactory;
		}

	}
	
}
