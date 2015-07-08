package fcmps.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import dsc.util.function.UUID;
import fcmps.domain.FCMPS0101_BEAN;
import fcmps.domain.FCMPS010_BEAN;

public class FCMPS_CLS_ImportOrderFromFOS extends TestCase {
	private String displayItems[][]=new String[][]{{"Brand","Brand"},
            {"Factory","Factory"},
            {"Style Code","Style_Code"},
            {"Style","Style"},
            {"Status","Status"},
            {"PO","PO"},
            {"Order Type Code","Order_Type_Code"},
            {"SD","SD"},
            {"PO Changes","PO_CHANGES"},			                                       
            {"Customer Name","Customer_Name"},
            {"Customer Country","Customer_Country"},
            {"PO Open Quantity","PO_Open_Quantity"},
            {"PO Received Date","PO_Received_Date"},
            {"Status Change Date","Status_Change_Date"},
            {"Required Ship Date","Required_Ship_Date"},
            {"Actual RSD","ACT_RSD"},			                                       
            {"E1 Promised Ship Date","E1_Promised_Ship_Date"},
            {"Factory Promised Ship Date","Factory_Promised_Ship_Date"},
            {"Previous Factory Promised Ship Date","Pre_Factory_Promised_Ship_Date"},
            {"Original Promised Ship Date","Original_Promised_Ship_Date"},
            {"Crocs Remarks","Crocs_Remarks"},
            {"Factory Remarks","Factory_Remarks"},
            {"FG Ready","FG_Ready"},
            {"Earliest Compliant RSD","RSD"},
            {"Style Remarks","Style_Remark"},
            {"Region","Region"},
            {"Branch Code","Branch_Code"},
            {"Transport Mode Code","Transport_Mode_Code"},
            {"Transit Days","Transit_Days"},
            {"Promised Delivery Date","Promised_Delivery_Date"},
            {"Customer Request Date","Customer_Request_Date"},
            {"Cancel Date","Cancel_Date"},
            {"Evaluation","Evaluation"},
            {"Evaluation(Actual RSD)","EVALUATION_ACT_RSD"},
            {"Evaluation Remarks","Evaluation_Remarks"},
            {"Late Delta (Required Ship Date)","Late_Delta"},			                                       
            {"Lead Time","Lead_Time"},
            {"Region Compliance","Region_Compliance"},
            {"Factory Compliance","Factory_Compliance"},
            {"Multiple Styles","Multiple_Styles"},			                                       
            {"Year-Month (Promised Ship Date)","YM_PSD"},
            {"Year-Week (Promised Ship Date)","YW_PSD"},
            {"Year-Week Description (Promised Ship Date)","YWD_PSD"},			                                       
            {"Year-Month (Required Ship Date)","YM_RD"},
            {"Year-Week (Required Ship Date)","YW_RD"},
            {"Year-Week Description (Required Ship Date)","YWD_RD"},
            {"Planner","Planner"},
            {"Year-Month (PO Received Date)","YM_PRD"},
            {"Year-Week (PO Received Date)","YW_PRD"},
            {"Year-Week Description (PO Received Date)","YWD_PRD"},
            {"Buyer Name","Buyer_Name"},
            {"Tradecard PO","TC_PONO"},
            {"Supplier Commit","SUPPLIER_COMMIT"},
            {"MFR Date","MFR"},
            {"KPR","KPR"},
            {"VA Code","VA_CODE"},
           };

	private Map<String,String> ItemDataType=new HashMap<String,String>();

	private String FOS_STATUS="";

	private ArrayList<CLS_RCCP_ERROR> ls_Message=new ArrayList<CLS_RCCP_ERROR>();
	private Map<String,ArrayList<String[]>> ls_SH_SIZE=new HashMap<String,ArrayList<String[]>>();

	private Map<String,Double> ls_SH_SIZE_MD_PAIR_QTY=new HashMap<String,Double>();
	private Map<String,ArrayList<String>> ls_SH_SHARE_PART=new HashMap<String,ArrayList<String>>();

	private Map<String,Double> ls_SH_MIN_CAP_QTY=new HashMap<String,Double>();	
	
	Map<String,ArrayList<String>> ls_SH_NEED_PLAN_PROC=new HashMap<String,ArrayList<String>>();
	
	private SessionFactory sessionFactory=null;
    private Transaction transaction=null;
    private String config_xml="";
    
    private Connection conn=null;
    
    private String output=".";
        
	private String UP_USER="DEV";	
	
	public void test_Import() {
		String path="";
		String package_path[]=FCMPS_CLS_ImportOrderFromFOS.class.getPackage().toString().split(" ");
		package_path=package_path[1].split("\\.");
		for(int i=0;i<package_path.length;i++) {
			if(!path.equals(""))path=path+"/";
			path=path+package_path[i];
		}
		
		File file = new File("");	  
		path=file.getAbsolutePath()+"/src/"+path;
		
    	config_xml=path+"/FTI.cfg.xml";
    	output="C:/temp/20131230";
    	String FOS_File="C:/Documents and Settings/dev17/桌面/FTI周計劃/FOS/1422周FOS.xls";
		doImport(FOS_File,config_xml);
//		doPrint();
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
				System.out.println( "The Config file " + getConfig_XML()+" does not exist!" );
				return false;
			}
			Configuration config=new Configuration().configure(fConfig);	
			config.addClass(FCMPS0101_BEAN.class);
			config.addClass(FCMPS010_BEAN.class);
			sessionFactory=config.buildSessionFactory();	
			
			Session session = sessionFactory.openSession(); 			
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
	
	private SessionFactory getSessionFactory() {
		return this.sessionFactory;
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

	public String getUP_USER() {
		return UP_USER;
	}

	public void setUP_USER(String up_user) {
		UP_USER = up_user;
	}
    
	public void doImport(String FOS_File,String config_xml){
    	this.config_xml=config_xml;
    	
    	if(!GenericSessionFactory()) {
    		return;
    	}
    	
    	try{
    		    		
    		ItemDataType.put("PO Received Date", "date");
    		ItemDataType.put("Required Ship Date", "date");
    		ItemDataType.put("E1 Promised Ship Date", "date");
    		ItemDataType.put("Factory Promised Ship Date", "date");
    		ItemDataType.put("Previous Factory Promised Ship Date", "date");
    		ItemDataType.put("Original Promised Ship Date", "date");
    		ItemDataType.put("Promised Delivery Date", "date");
    		ItemDataType.put("Customer Request Date", "date");
    		ItemDataType.put("Cancel Date", "date");
    		ItemDataType.put("Earliest Compliant RSD", "date");
    				
    		ItemDataType.put("Year-Month (Promised Ship Date)", "String");
    		ItemDataType.put("Year-Week (Promised Ship Date)", "String");
    		ItemDataType.put("Year-Week Description (Promised Ship Date)", "String");
    		ItemDataType.put("Year-Month (Required Ship Date)", "String");
    		ItemDataType.put("Year-Week (Required Ship Date)", "String");
    		ItemDataType.put("Year-Week Description (Required Ship Date)", "String");
    		ItemDataType.put("Year-Month (PO Received Date)", "String");
    		ItemDataType.put("Year-Week (PO Received Date)", "String");
    		ItemDataType.put("Year-Week Description (PO Received Date)", "String");
    		ItemDataType.put("Evaluation", "String");
    				
    		ItemDataType.put("PO Open Quantity", "Numeric");
    		ItemDataType.put("Transit Days", "Numeric");
    		ItemDataType.put("Late Delta (Required Ship Date)", "Numeric");
    		ItemDataType.put("Lead Time", "Numeric");
    		
    		ItemDataType.put("MFR Date", "date");
    		ItemDataType.put("Actual RSD", "date");
    		
    		doImport(FOS_File,conn);

    		CloseSessionFactory();
    		
    		doPrint();

    		
    	}catch(Exception ex){
    		ex.printStackTrace();
         		
    	}finally{
    		CloseSessionFactory();  
    	}
    }

    private void doImport(String FOS_File,Connection conn){
		
		try {
			
			File fos=new File(FOS_File);
			if(!fos.exists()) {
				System.out.println( "The FOS file " + fos+" does not exist!" );
				return;
			}
			
			FileInputStream fileIn = new FileInputStream(fos);
			if(fileIn==null) {
				System.out.println("無法讀取FOS:"+FOS_File);
				return;
			}
			HSSFWorkbook wb=new HSSFWorkbook(fileIn);
			
			FOS_STATUS="Open";
			HSSFSheet sheet = wb.getSheet(FOS_STATUS);			 			

			if(sheet!=null) {
				if(!readSheet(sheet, conn)){
					conn.rollback();
					return;
				}
			}
			
			FOS_STATUS="Pending";
			sheet = wb.getSheet(FOS_STATUS);			 			

			if(sheet!=null) {
				if(!readSheet(sheet, conn)){
					conn.rollback();
					return;
				}
			}
		
			FOS_STATUS="Closed";
			sheet = wb.getSheet(FOS_STATUS);			 			

			if(sheet!=null) {
				if(!readSheet(sheet, conn)){
					conn.rollback();
					return;
				}
			}
			
		    fileIn.close();
		    wb=null;   
			
		}catch(Exception ex){
			ex.printStackTrace();
		}

    }
    
    private boolean readSheet(HSSFSheet sheet,Connection conn){
    	boolean iRet=false;
		
		int iRow=0;    		
		
		HSSFRow row = null;			
		HSSFCell cell = null;			
		row=sheet.getRow(iRow);
		String sql_Fields="";			
		
		for(int i=0;i<displayItems.length;i++){
			cell = row.getCell((short)i);
			Object obj=FCMPS_PUBLIC.getCellValue(cell);
			
			if(!displayItems[i][0].toUpperCase().equals(FCMPS_PUBLIC.getValue(obj).toUpperCase())){
				System.out.println(displayItems[i][0].toUpperCase());
				System.out.println(FCMPS_PUBLIC.getValue(obj).toUpperCase());
				System.out.println("不是有效的FOS檔案格式!");
				return iRet;
			}else{
				if(!sql_Fields.equals("")) sql_Fields=sql_Fields+",";
				sql_Fields=sql_Fields+displayItems[i][1].toUpperCase();
			}
			
		}
		
		Connection FCC_conn=null;
	   
	    
		iRow++;
				
		while(iRow<=sheet.getLastRowNum()){
			row=sheet.getRow(iRow);
			if(row==null) break;
			
			cell = row.getCell((short)0);
			if(FCMPS_PUBLIC.getCellValue(cell)==null) break;
			
			String OD_PONO1="";
			String FA_NO="";
			String STYLE_NO="";
			double OD_QTY=0;
			String SH_NO="";
			String KPR="";
			Date OD_SHIP=null;
			Date OD_FGDATE=null;
			
			String Branch_Code="";
			String PO="";
			String Status="";
			
			for(int i=0;i<displayItems.length;i++){
				cell = row.getCell((short)i);
				if(cell==null) continue;		
				
				String datatype=ItemDataType.get(displayItems[i][0]);
				
				if(FCMPS_PUBLIC.getValue(datatype).equals("String")){
					String obj="";
					if(cell!=null){
						obj=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,2));
					}					
					
					if(displayItems[i][0].equals("Factory")) {
						FA_NO=FCMPS_PUBLIC.getValue(obj);
					}
					if(displayItems[i][0].equals("Tradecard PO")) {
						OD_PONO1=FCMPS_PUBLIC.getValue(obj);
					}
					if(displayItems[i][0].equals("Branch Code")) {
						Branch_Code=FCMPS_PUBLIC.getValue(obj);
					}
					
					if(displayItems[i][0].equals("Status")) {
						Status=FCMPS_PUBLIC.getValue(obj);
					}
					
					if(displayItems[i][0].equals("PO")) {
						PO=FCMPS_PUBLIC.getValue(obj);
					}
					if(displayItems[i][0].equals("Style Code")) {
						STYLE_NO=FCMPS_PUBLIC.getValue(obj);
					}
					if(displayItems[i][0].equals("Style")) {
						SH_NO=FCMPS_PUBLIC.getValue(obj).toUpperCase();
					}
					if(displayItems[i][0].equals("KPR")) {
						KPR=FCMPS_PUBLIC.getValue(obj).toUpperCase();
						if(KPR.equals("YES")) {
							KPR="Y";							
						}else {
							KPR="N";
						}
					}
					
				}else if(FCMPS_PUBLIC.getValue(datatype).equals("date")){
					Object obj=FCMPS_PUBLIC.getCellValue(cell);	
					if(!FCMPS_PUBLIC.getValue(obj).equals("")&&!FCMPS_PUBLIC.getValue(obj).equals("NaN")){
						Calendar cal=Calendar.getInstance();
						cal.clear();
						if(FCMPS_PUBLIC.getDouble(obj)!=0){
							long l_date=Long.valueOf((long)FCMPS_PUBLIC.getDouble(obj));
							l_date=l_date-25569+1; //因為Excel中的時間是從1900/01/01 00:00:00 開始						
							cal.set(Calendar.DAY_OF_YEAR, Long.valueOf(l_date).intValue());						
							
							if(displayItems[i][0].equals("Required Ship Date")) {
								OD_SHIP=cal.getTime();
							}
							if(displayItems[i][0].equals("Factory Promised Ship Date")) {
								OD_FGDATE=cal.getTime();
							}
						}

					}
					
				}else if(FCMPS_PUBLIC.getValue(datatype).equals("Numeric")){
					Object obj=FCMPS_PUBLIC.getCellValue(cell,2);	
					if(!FCMPS_PUBLIC.getValue(obj).equals("")&&!FCMPS_PUBLIC.getValue(obj).equals("NaN")){
						if(displayItems[i][0].equals("PO Open Quantity")) {
							OD_QTY=FCMPS_PUBLIC.getDouble(obj);
						}	
					}
				}else{
					Object obj=FCMPS_PUBLIC.getCellValue(cell,2);
					
					if(displayItems[i][0].equals("Factory")) {
						FA_NO=FCMPS_PUBLIC.getValue(obj);
					}
					if(displayItems[i][0].equals("Tradecard PO")) {
						OD_PONO1=FCMPS_PUBLIC.getValue(obj);
					}
					if(displayItems[i][0].equals("Branch Code")) {
						Branch_Code=FCMPS_PUBLIC.getValue(obj);
					}
					
					if(displayItems[i][0].equals("Status")) {
						Status=FCMPS_PUBLIC.getValue(obj);
					}
					
					if(displayItems[i][0].equals("PO")) {
						PO=FCMPS_PUBLIC.getValue(obj);
					}
					if(displayItems[i][0].equals("Style Code")) {
						STYLE_NO=FCMPS_PUBLIC.getValue(obj);
					}
					if(displayItems[i][0].equals("Style")) {
						SH_NO=FCMPS_PUBLIC.getValue(obj).toUpperCase();
					}
					if(displayItems[i][0].equals("KPR")) {
						KPR=FCMPS_PUBLIC.getValue(obj).toUpperCase();
						if(KPR.equals("YES")) {
							KPR="Y";							
						}else {
							KPR="N";
						}
					}
				}	

			}

			 if(FA_NO.equals("FIC")) {
				 if(FCC_conn==null) FCC_conn=getConnection("FCC",conn);			 
			 }
			
			PO=PO.replace(" ", "");
			System.out.println("Row:"+iRow+" "+new Date());
			
			if(OD_SHIP==null) {
				CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setOD_PONO1(OD_PONO1);
				cls_message.setSTYLE_NO(STYLE_NO);
				cls_message.setSH_NO(SH_NO);
				cls_message.setOD_QTY(OD_QTY);
				
				cls_message.setERROR("Row:"+(iRow+1)+" 沒有訂單交期!");
				
				ls_Message.add(cls_message);				
    			
    			iRow++;
    			
    			continue;
				
			}
			
			if(OD_FGDATE==null) {
				CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setOD_PONO1(OD_PONO1);
				cls_message.setSTYLE_NO(STYLE_NO);
				cls_message.setSH_NO(SH_NO);
				cls_message.setOD_QTY(OD_QTY);
				
				cls_message.setERROR("Row:"+(iRow+1)+" 沒有FG Date!");
				
				ls_Message.add(cls_message);				
    			
    			iRow++;
    			
    			continue;
				
			}
			
			//FOS中廠別是FIC ,Branch Code 為 CHN 的訂單為內銷訂單, 其它均為外銷
			//FVI的FOS中也有Branch Code 為 CHN 的訂單,但FVI不分內外銷
			if(FA_NO.equals("FIC") && Branch_Code.toUpperCase().equals("CHN")) {//FIC內銷訂單
	            if(!exist_OD_IN_ERP(OD_PONO1, FCC_conn)) {
	            	 if(!exist_OD_IN_ERP(PO, FCC_conn)) {
	 	    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
						cls_message.setOD_PONO1(OD_PONO1);
						cls_message.setSTYLE_NO(STYLE_NO);
						cls_message.setSH_NO(SH_NO);
						cls_message.setOD_QTY(OD_QTY);
						if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
						cls_message.setERROR("Row:"+(iRow+1)+" "+OD_PONO1+" 訂單系統無此訂單!");
						
						ls_Message.add(cls_message);

		    			iRow++;
		    			
		    			continue;
	            	 }else {
	            		OD_PONO1=PO;
	             		if(FOS_STATUS.equals("Open")) {
	            			String PO_CancelDate=getPO_CancelDate(OD_PONO1, FCC_conn);
	            			if(!PO_CancelDate.equals("")){
	    		    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
	    						cls_message.setOD_PONO1(OD_PONO1);
	    						cls_message.setSTYLE_NO(STYLE_NO);
	    						cls_message.setSH_NO(SH_NO);
	    						cls_message.setOD_QTY(OD_QTY);
	    						if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
	    						cls_message.setERROR("Row:"+(iRow+1)+" "+OD_PONO1+" 訂單已取消,取消日期:"+PO_CancelDate);
	    						
	    						ls_Message.add(cls_message);
	    						
	    		    			iRow++;
	    		    			
	    		    			continue;

	            			}
	            		}
	            	 }	    			
	            }else {
            		if(FOS_STATUS.equals("Open")) {
            			String PO_CancelDate=getPO_CancelDate(OD_PONO1, FCC_conn);
            			if(!PO_CancelDate.equals("")){
    		    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
    						cls_message.setOD_PONO1(OD_PONO1);
    						cls_message.setSTYLE_NO(STYLE_NO);
    						cls_message.setSH_NO(SH_NO);
    						cls_message.setOD_QTY(OD_QTY);
    						if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
    						cls_message.setERROR("Row:"+(iRow+1)+" "+OD_PONO1+" 訂單已取消,取消日期:"+PO_CancelDate);
    						
    						ls_Message.add(cls_message);
    						
    		    			iRow++;
    		    			
    		    			continue;

            			}
            		}	            	
	            }
			}else {
	            if(!exist_OD_IN_ERP(OD_PONO1, conn)) {
	            	 if(!exist_OD_IN_ERP(PO, conn)) {
	 	    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
						cls_message.setOD_PONO1(OD_PONO1);
						cls_message.setSTYLE_NO(STYLE_NO);
						cls_message.setSH_NO(SH_NO);
						cls_message.setOD_QTY(OD_QTY);
						if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
						cls_message.setERROR("Row:"+(iRow+1)+" "+OD_PONO1+" 訂單系統無此訂單!");
						
						ls_Message.add(cls_message);

		    			iRow++;
		    			
		    			continue;
	            	 }else {
	            		OD_PONO1=PO;
	             		if(FOS_STATUS.equals("Open")) {
	            			String PO_CancelDate=getPO_CancelDate(OD_PONO1, conn);
	            			if(!PO_CancelDate.equals("")){
	    		    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
	    						cls_message.setOD_PONO1(OD_PONO1);
	    						cls_message.setSTYLE_NO(STYLE_NO);
	    						cls_message.setSH_NO(SH_NO);
	    						cls_message.setOD_QTY(OD_QTY);
	    						if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
	    						cls_message.setERROR("Row:"+(iRow+1)+" "+OD_PONO1+" 訂單已取消,取消日期:"+PO_CancelDate);
	    						
	    						ls_Message.add(cls_message);
	    						
	    		    			iRow++;
	    		    			
	    		    			continue;

	            			}
	            		}	            		 
	            	 }	    			
	            }else {
            		if(FOS_STATUS.equals("Open")) {
            			String PO_CancelDate=getPO_CancelDate(OD_PONO1, conn);
            			if(!PO_CancelDate.equals("")){
    		    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
    						cls_message.setOD_PONO1(OD_PONO1);
    						cls_message.setSTYLE_NO(STYLE_NO);
    						cls_message.setSH_NO(SH_NO);
    						cls_message.setOD_QTY(OD_QTY);
    						if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
    						cls_message.setERROR("Row:"+(iRow+1)+" "+OD_PONO1+" 訂單已取消,取消日期:"+PO_CancelDate);
    						
    						ls_Message.add(cls_message);
    						
    		    			iRow++;
    		    			
    		    			continue;

            			}
            		}	            	
	            }

			}            	
			
			String MODEL_CNA="";
			if(FA_NO.equals("FIC") && Branch_Code.toUpperCase().equals("CHN")) {//FIC內銷訂單
				MODEL_CNA=getSH_NO(SH_NO, STYLE_NO, FCC_conn);
			}else {
				MODEL_CNA=getSH_NO(SH_NO, STYLE_NO, conn);
			}
						
			if(MODEL_CNA.equals("")) {
				CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setOD_PONO1(OD_PONO1);
				cls_message.setSTYLE_NO(STYLE_NO);
				cls_message.setSH_NO(SH_NO);
				cls_message.setOD_QTY(OD_QTY);
				if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
				cls_message.setERROR("Row:"+(iRow+1)+" "+STYLE_NO+" "+SH_NO+" 沒有對應的內部型體名稱!");
				
				ls_Message.add(cls_message);
				
    			iRow++;
    			
    			continue;
			}			
						
			ArrayList<String[]> SH_SIZE2=new ArrayList<String[]>();
		    ArrayList<String[]> SH_SIZE=ls_SH_SIZE.get(MODEL_CNA);
		    
			if(SH_SIZE==null) {
				SH_SIZE=getSH_SIZE(MODEL_CNA, conn);
				ls_SH_SIZE.put(MODEL_CNA, SH_SIZE);
			}	
			
			int WORK_WEEK=Integer.valueOf(WeekUtil.getWeekOfYear(new Date(),true));
			
    		for(int i=0;i<SH_SIZE.size();i++) {
    			String SIZE=SH_SIZE.get(i)[0];
    			
    			boolean iExit=false;
    			if(FA_NO.equals("FIC") && Branch_Code.toUpperCase().equals("CHN")) {//FIC內銷訂單
    				iExit=have_OD_QTY(OD_PONO1,Integer.valueOf(SH_SIZE.get(i)[1]),FCC_conn);
    			}else {
    				iExit=have_OD_QTY(OD_PONO1,Integer.valueOf(SH_SIZE.get(i)[1]),conn);
    			}
    			
    			if(iExit) {//有訂單數量
    	    		Double MD_PAIR_QTY=ls_SH_SIZE_MD_PAIR_QTY.get(FA_NO+MODEL_CNA+SIZE);			    		
    	    		if(MD_PAIR_QTY==null) {
    	    			MD_PAIR_QTY=FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(FA_NO, MODEL_CNA, SIZE, conn,WORK_WEEK);
    	    			ls_SH_SIZE_MD_PAIR_QTY.put(FA_NO+MODEL_CNA+SIZE, MD_PAIR_QTY);
    	    		}
    	    		
    	    		if(MD_PAIR_QTY.doubleValue()==0) {
    	    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
    					cls_message.setOD_PONO1(OD_PONO1);
    					cls_message.setSTYLE_NO(STYLE_NO);
    					cls_message.setSH_NO(MODEL_CNA);
    					cls_message.setOD_QTY(OD_QTY);
    					if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
    					cls_message.setERROR("Row:"+(iRow+1)+" 型體:"+MODEL_CNA+" SIZE:"+SIZE+" 沒有建立模具資料!");
    					
    					ls_Message.add(cls_message);  
    					
    					SH_SIZE2.add(new String[] {SH_SIZE.get(i)[0],SH_SIZE.get(i)[1]});
    	    		}
    			}
	    		
    		}
    		
    		//去除有訂單數量,卻沒有模具的size
    		if(!SH_SIZE2.isEmpty()) {
    			for(int i=0;i<SH_SIZE2.size();i++) {
    				String SIZE=SH_SIZE2.get(i)[0];
    				for(int n=0;n<SH_SIZE.size();n++) {
    					if(SIZE.equals(SH_SIZE.get(n)[0])) {
    						SH_SIZE.remove(n);
    						break;
    					}
    				}
    			}
    		}
    		
    		SH_SIZE2.clear();
    		
    		ArrayList<String[]> ls_PROC_SEQ=getPROC_SEQ(MODEL_CNA,conn);
    		
    		if(ls_PROC_SEQ.isEmpty()) {
    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setOD_PONO1(OD_PONO1);
				cls_message.setSTYLE_NO(STYLE_NO);
				cls_message.setSH_NO(MODEL_CNA);
				cls_message.setOD_QTY(OD_QTY);
				if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
				cls_message.setERROR("Row:"+(iRow+1)+" 沒有建立型體:"+MODEL_CNA+" 的制程或是沒有設定需要排計劃的制程!");
				
				ls_Message.add(cls_message);

    			iRow++;
    			
    			continue;
    			
    		}
			
    		Double MIN_WEEK_CAP_QTY=ls_SH_MIN_CAP_QTY.get(MODEL_CNA);
    		if(MIN_WEEK_CAP_QTY==null) {
    			MIN_WEEK_CAP_QTY=FCMPS_PUBLIC.getSH_Min_Week_Cap_QTY(FA_NO, MODEL_CNA,WORK_WEEK, conn);
    			ls_SH_MIN_CAP_QTY.put(MODEL_CNA, MIN_WEEK_CAP_QTY);
    		}
    		
    		if(MIN_WEEK_CAP_QTY.doubleValue()==0) {
    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setOD_PONO1(OD_PONO1);
				cls_message.setSTYLE_NO(STYLE_NO);
				cls_message.setSH_NO(MODEL_CNA);
				cls_message.setOD_QTY(OD_QTY);
				if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
				cls_message.setERROR("Row:"+(iRow+1)+" 沒有建立型體:"+MODEL_CNA+" 的周產能!");
				
				ls_Message.add(cls_message);
				
    			iRow++;
    			
    			continue;
    		}
    		
			if(FA_NO.equals("FIC") && Branch_Code.toUpperCase().equals("CHN")) {//FIC內銷訂單
	            String msg=record_PO_Difference(OD_PONO1,MODEL_CNA,conn,FCC_conn);
	            if(!msg.equals("")) {
 	    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
					cls_message.setOD_PONO1(OD_PONO1);
					cls_message.setSTYLE_NO(STYLE_NO);
					cls_message.setSH_NO(SH_NO);
					cls_message.setOD_QTY(OD_QTY);
					if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
					cls_message.setERROR("Row:"+(iRow+1)+" "+msg);
					
					ls_Message.add(cls_message);
	            }
			}else {
	            String msg=record_PO_Difference(OD_PONO1,MODEL_CNA,conn,conn);
	            if(!msg.equals("")) {
 	    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
					cls_message.setOD_PONO1(OD_PONO1);
					cls_message.setSTYLE_NO(STYLE_NO);
					cls_message.setSH_NO(SH_NO);
					cls_message.setOD_QTY(OD_QTY);
					if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
					cls_message.setERROR("Row:"+(iRow+1)+" "+msg);
					
					ls_Message.add(cls_message);
	            }				
			}
    		
    		transaction.begin();
			FCMPS_CLS_AnalyseOrderFromFOS cls_AnalyseOrderFromFOS=new FCMPS_CLS_AnalyseOrderFromFOS();
			cls_AnalyseOrderFromFOS.setConnection(conn);
			cls_AnalyseOrderFromFOS.setFA_NO(FA_NO);
			cls_AnalyseOrderFromFOS.setOD_FGDATE(OD_FGDATE);
			cls_AnalyseOrderFromFOS.setOD_PONO1(OD_PONO1);
			cls_AnalyseOrderFromFOS.setE1_PO(PO);
			cls_AnalyseOrderFromFOS.setOD_QTY(OD_QTY);
			cls_AnalyseOrderFromFOS.setOD_SHIP(OD_SHIP);
			cls_AnalyseOrderFromFOS.setSH_NO(MODEL_CNA);		
			cls_AnalyseOrderFromFOS.setSH_SIZE(SH_SIZE);
			cls_AnalyseOrderFromFOS.setSTYLE_NO(STYLE_NO);
			cls_AnalyseOrderFromFOS.setKPR(KPR);
			cls_AnalyseOrderFromFOS.setBranch_Code(Branch_Code);
			cls_AnalyseOrderFromFOS.setIs_NON_Analyse_Exist_Order(true);
			cls_AnalyseOrderFromFOS.setMIN_WEEK_CAP_QTY(MIN_WEEK_CAP_QTY.doubleValue());
			cls_AnalyseOrderFromFOS.setLs_SH_SIZE_MD_PAIR_QTY(ls_SH_SIZE_MD_PAIR_QTY);			
			cls_AnalyseOrderFromFOS.setLs_SH_SHARE_PART(ls_SH_SHARE_PART);
			cls_AnalyseOrderFromFOS.setLs_SH_NEED_PLAN_PROC(ls_SH_NEED_PLAN_PROC);
			cls_AnalyseOrderFromFOS.setUP_USER(getUP_USER());
			cls_AnalyseOrderFromFOS.setIS_REPLACEMENT((Status.trim().toUpperCase().equals("Replacement".toUpperCase())?"Y":"N"));
			
			
			cls_AnalyseOrderFromFOS.setSessionFactory(getSessionFactory());
			
			if(FOS_STATUS.equals("Open")) {
				cls_AnalyseOrderFromFOS.setOD_CODE("N");
			}
			if(FOS_STATUS.equals("Pending")) {
				cls_AnalyseOrderFromFOS.setOD_CODE("P");
			}
			
			if(FOS_STATUS.equals("Closed")) {
				cls_AnalyseOrderFromFOS.setOD_CODE("Y");
			}
			
			String message="";
			if(FA_NO.equals("FIC") && Branch_Code.toUpperCase().equals("CHN")) {
				message=cls_AnalyseOrderFromFOS.doAnalyse(FCC_conn);			
			}else {
				message=cls_AnalyseOrderFromFOS.doAnalyse(conn);						
			}
			
			transaction.commit();

			if(!message.equals("")) {
				CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setOD_PONO1(OD_PONO1);
				cls_message.setSTYLE_NO(STYLE_NO);
				cls_message.setSH_NO(SH_NO);
				cls_message.setERROR(message);
				
				ls_Message.add(cls_message);
			}

			iRow++;
		}    	
		
		if(FCC_conn!=null) this.closeConnection(FCC_conn);
		
		iRet=true;
    	return iRet;
    }    
		
	/**
	 * 將客戶型體轉換為內容型體
	 * @param SH_ANO
	 * @param STYLE_NO
	 * @return
	 */
	private String getSH_NO(String SH_ANO,String STYLE_NO,Connection conn) {
		String iRet="";
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select MODEL_CNA " +
				   "from FICSKU01 " +
				   "where SKU LIKE '"+STYLE_NO+"%' and rownum=1";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=FCMPS_PUBLIC.getValue(rs.getString("MODEL_CNA"));
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
	 * 取型體的可用SIZE
	 * @param SH_ANO
	 * @param STYLE_NO
	 * @return
	 */
	private ArrayList<String[]> getSH_SIZE(String SH_NO,Connection conn) {
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
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
		    			strSQL="select count(SKU) SKU from FICSKU01 where MODEL_CNA='"+SH_NO+"' and SIZE_NA='"+rs.getString("T"+i)+"'";
		    		    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    		    rs2=pstmtData2.executeQuery();
		    		    
		    		    if(rs2.next()){ //如果型體的SIZE沒有SKU,說明可能只是在開發階段有此SIZE,而量產時沒有.
		    		    	if(rs2.getInt("SKU")>0) iRet.add(new String[] {rs.getString("T"+i),String.valueOf(i)});
		    		    }
		    			rs2.close();
		    			pstmtData2.close();		    					    		
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
	 * 取型體需要的制程順序
	 * @param SH_NO
	 * @return
	 */
	private ArrayList<String[]> getPROC_SEQ(String SH_NO,Connection conn) {
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select FCPS22_1.PB_PTNO,FCPS22_2.PB_PTNA from FCPS22_1,FCPS22_2 " +
				   "where FCPS22_1.PB_PTNO=FCPS22_2.PB_PTNO(+) " +
				   "  and FCPS22_1.SH_ARITCLE='"+SH_NO+"' " +
				   "  and FCPS22_1.NEED_PLAN='Y'";
			
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
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	/**
	 * 訂單是否存在於ERP訂單系統
	 * @param OD_PONO1
	 * @param conn
	 * @return
	 */
	private boolean exist_OD_IN_ERP(String OD_PONO1,Connection conn) {
		boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select count(*) iCount from DSOD00 where OD_PONO1='"+OD_PONO1+"' ";
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
	 * 取訂單的Cancel Date
	 * @param OD_PONO1
	 * @param conn
	 * @return
	 */
	private String getPO_CancelDate(String OD_PONO1,Connection conn) {
		String iRet="";
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select TO_CHAR(CANCEL_DATE,'YYYY/MM/DD') CANCEL_DATE from DSOD00 where OD_PONO1='"+OD_PONO1+"' and nvl(OD_CODE,'N')='C' ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=FCMPS_PUBLIC.getValue(rs.getString("CANCEL_DATE"));
		    	if(iRet.equals("")) iRet="empty";
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
	
	private String record_PO_Difference(String OD_PONO1,String SH_NO,Connection conn,Connection FCC_conn) {
		String iRet="";
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		double org_OD_QTY=0;
		String org_OD_SHIP="";
		
		double new_OD_QTY=0;
		String new_OD_SHIP="";
		
		try{

			strSQL="SELECT to_char(OD_SHIP,'yyyy/mm/dd') OD_SHIP,SUM(OD_QTY) OD_QTY " +
				   "  FROM FCMPS010 " +
				   " WHERE OD_PONO1='"+OD_PONO1+"' " +
				   "   AND SH_NO='"+SH_NO+"' "+
				   "   AND PROCID=(SELECT MAX(PROCID) FROM FCMPS010 WHERE OD_PONO1='"+OD_PONO1+"' AND SH_NO='"+SH_NO+"')"+
				   " GROUP BY OD_SHIP";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	org_OD_QTY=rs.getDouble("OD_QTY");
		    	org_OD_SHIP=rs.getString("OD_SHIP");
		    }else {
		    	iRet=OD_PONO1+" 型體:"+SH_NO+" 為新接單";
		    }
			rs.close();
			pstmtData.close();
			
			if(!iRet.equals("")) return iRet;
			
			strSQL="select SUM(od_qty) OD_QTY,to_char(OD_SHIP,'yyyy/mm/dd') OD_SHIP " +
				   "from DSOD00 " +
				   "where OD_PONO1='"+OD_PONO1+"' " +
				   "  and SH_ARITCLENO='"+SH_NO+"' "+
				   "group by OD_SHIP ";
		    pstmtData = FCC_conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	new_OD_QTY=rs.getDouble("OD_QTY");
		    	new_OD_SHIP=rs.getString("OD_SHIP");
		    }
			rs.close();
			pstmtData.close();

			if(org_OD_QTY!=new_OD_QTY) {
				iRet=OD_PONO1+" 型體:"+SH_NO+" FOS訂單數量:"+org_OD_QTY+" 訂單系統訂單數量:"+new_OD_QTY;
			}
			
			if(!org_OD_SHIP.equals(new_OD_SHIP)) {
				if(iRet.equals("")) {
					iRet=OD_PONO1+" 型體:"+SH_NO+" FOS交期:"+org_OD_SHIP+" 訂單系統交期:"+new_OD_SHIP;
				}else {
					iRet=iRet+" FOS交期:"+org_OD_SHIP+" 訂單系統交期:"+new_OD_SHIP;
				}
			}

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	/**
	 * 訂單的SIZE是否有訂單量
	 * @param OD_PONO1
	 * @param conn
	 * @return
	 */
	private boolean have_OD_QTY(String OD_PONO1,int SIZE_INDEX,Connection conn) {
		boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select nvl(sum(S"+SIZE_INDEX+"),0) OD_QTY from DSOD_03 " +
			       "where OD_NO IN (select od_no from DSOD00 where OD_PONO1='"+OD_PONO1+"')";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(rs.getInt("OD_QTY")>0) iRet=true;
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
	 * 取得訊息
	 * @return
	 */
	public ArrayList<CLS_RCCP_ERROR> getMessage() {
		return ls_Message;
	}
	
	private void doPrint() {	
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
	 * 取得FTI廠別正式數據庫連線
	 * @param COMMPANY_ID
	 * @return
	 */
	private Connection getConnection(String FA_NO,Connection conn){		
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
