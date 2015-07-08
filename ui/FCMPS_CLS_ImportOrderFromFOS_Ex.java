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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TimerTask;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.cfg.Configuration;

import dsc.util.function.UUID;
import fcmps.domain.FCMPS010_BEAN;

/**
 * 測試多線程導FOS
 * @author dev17
 *
 */
public class FCMPS_CLS_ImportOrderFromFOS_Ex extends TestCase {
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
	    
    private String output=".";
        
	private String UP_USER="DEV";	
	
	private String config_xml="";
	
	private ExecutorService pool=null;
	private Stack<Connection> FIC_Conn_Stack=null;
	private Stack<Connection> FCC_Conn_Stack=null;
	private Stack<Connection> FCMPS_Conn_Stack=null;
	
	private List<FCMPS_CLS_ImportOrder> ls_Thread=new ArrayList<FCMPS_CLS_ImportOrder>();
//	private List<Future<Map<FCMPS_CLS_ImportOrder,int[]>>> resultList;
	private CompletionService<Map<FCMPS_CLS_ImportOrder,int[]>> resultList;
	private boolean isFinished=false;
	
	private boolean Need_Self_Monitor=false;

	private int threadCount=1;
	private int Order_Count=0;
	
	public void test_Import() {
		String path="";
		String package_path[]=FCMPS_CLS_ImportOrderFromFOS_Ex.class.getPackage().toString().split(" ");
		package_path=package_path[1].split("\\.");
		for(int i=0;i<package_path.length;i++) {
			if(!path.equals(""))path=path+"/";
			path=path+package_path[i];
		}
		
		File file = new File("");	  
		path=file.getAbsolutePath()+"/src/"+path;
		
    	config_xml=path+"/FTI.cfg.xml";
    	output="C:/temp/20131230";
    	String FOS_File="F:/FTI周計劃/FOS/1422周FOS.xls";
    	
    	Need_Self_Monitor=true;
    	
		doImport(FOS_File,config_xml,10);
//		doPrint();
	}
		
	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished=isFinished;
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
	
	public int getOrder_Count() {
		return Order_Count;
	}

	private String getConfig_XML() {
		return config_xml;
	}

	public List<FCMPS_CLS_ImportOrder> getThreads() {
		return ls_Thread;
	}

	public CompletionService<Map<FCMPS_CLS_ImportOrder,int[]>> getResultList() {
		return resultList;
	}

	public boolean isNeed_Self_Monitor() {
		return Need_Self_Monitor;
	}	

	public void setNeed_Self_Monitor(boolean need_Self_Monitor) {
		Need_Self_Monitor = need_Self_Monitor;
	}
	
	public void doStop() {
		pool.shutdownNow();
	}

	public Stack<Connection> getFCC_Conn_Stack() {
		return FCC_Conn_Stack;
	}

	public Stack<Connection> getFCMPS_Conn_Stack() {
		return FCMPS_Conn_Stack;
	}

	public Stack<Connection> getFIC_Conn_Stack() {
		return FIC_Conn_Stack;
	}

	public void doImport(String FOS_File,String config_xml,int threadCount){
    	this.config_xml=config_xml;
    	    	
    	this.threadCount=threadCount;
    	
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
    		pool=Executors.newFixedThreadPool(threadCount);
    		
    		FIC_Conn_Stack=new Stack<Connection>();
    		FCC_Conn_Stack=new Stack<Connection>();
    		FCMPS_Conn_Stack=new Stack<Connection>();
    		
    		for(int i=0;i<threadCount;i++) {
    			Connection conn=GenericSessionFactory();
    			if(conn==null) {
    				System.out.println("create connection null");
    			}
    			FCMPS_Conn_Stack.push(conn);
    			
    			Connection FIC_conn=getConnection("FIC",conn);
    			if(FIC_conn==null) {
    				System.out.println("create fic connection null");
    			}
    			FIC_Conn_Stack.push(FIC_conn);
    			
    			Connection FCC_conn=getConnection("FCC",conn);
    			if(FCC_conn==null) {
    				System.out.println("create fcc connection null");
    			}
    			FCC_Conn_Stack.push(FCC_conn);
    			
    		}
    		
//    		resultList = new ArrayList<Future<Map<FCMPS_CLS_ImportOrder,int[]>>>();
    		resultList= new ExecutorCompletionService<Map<FCMPS_CLS_ImportOrder,int[]>>(pool);
    		
//    		List<FCMPS_CLS_ImportOrder> cls_redo=new ArrayList<FCMPS_CLS_ImportOrder>();
    		
    		doImport(FOS_File);
/*
    		if(isNeed_Self_Monitor()) {
    			do {
    		        for (Future<Map<FCMPS_CLS_ImportOrder,int[]>> fs : resultList) {  

    		            try {  
    		            	Map<FCMPS_CLS_ImportOrder,int[]> result=fs.get();
    		            	Iterator<FCMPS_CLS_ImportOrder> it=result.keySet().iterator();
    		            	while(it.hasNext()) {
    		            		FCMPS_CLS_ImportOrder key=it.next();
    		            		int[] status=result.get(key);
    			            	if(status[1]==FCMPS_CLS_ImportOrder.STATUS_COMPLETED)System.out.println(new Date()+" 第:"+status[0]+"行,導入完成,但資料不完整!");
    			            	if(status[1]==FCMPS_CLS_ImportOrder.STATUS_IMPORT_SUCCESS)System.out.println(new Date()+" 第:"+status[0]+"行,導入成功!");
    			            	if(status[1]==FCMPS_CLS_ImportOrder.STATUS_CANCEL)System.out.println(new Date()+" 第:"+status[0]+"行,取消!");
    			            	if(status[1]==FCMPS_CLS_ImportOrder.STATUS_ERROR)System.out.println(new Date()+" 第:"+status[0]+"行,導入失敗!");
    			            	if(status[1]==FCMPS_CLS_ImportOrder.STATUS_NOT_CONNECT_DB) {
    			            		cls_redo.add(key);   			            		
    			            		System.out.println(new Date()+" 第:"+status[0]+"行,無法取得資料庫連線,重新導入!");
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
    			    	pool=Executors.newFixedThreadPool(threadCount);
    			    	for(FCMPS_CLS_ImportOrder cls:cls_redo) {
		            		Future<Map<FCMPS_CLS_ImportOrder,int[]>> results=pool.submit(cls);
		            		resultList.add(results);
    			    	}
    			    	cls_redo.clear();    			    	
    			    }else {
    			    	break;
    			    }

    			}while(true);
    			
    			resultList=null;  			
    		}
	        */
    		
    		pool.shutdown();
    		
    		if(isNeed_Self_Monitor()) {
    			 for(int i=0;i<Order_Count;i++) {
    	            	Map<FCMPS_CLS_ImportOrder,int[]> result=resultList.take().get();
    	            	Iterator<FCMPS_CLS_ImportOrder> it=result.keySet().iterator();
    	            	while(it.hasNext()) {
    	            		FCMPS_CLS_ImportOrder key=it.next();
    	            		int[] status=result.get(key);
    		            	if(status[1]==FCMPS_CLS_ImportOrder.STATUS_COMPLETED) {
    		            		System.out.println(new Date()+" 第:"+status[0]+"行,導入完成,但資料不完整!");
    		            	}
    		            	if(status[1]==FCMPS_CLS_ImportOrder.STATUS_IMPORT_SUCCESS) {
    		            		System.out.println(new Date()+" 第:"+status[0]+"行,導入成功!");
    		            	}
    		            	if(status[1]==FCMPS_CLS_ImportOrder.STATUS_CANCEL) {
    		            		System.out.println(new Date()+" 第:"+status[0]+"行,取消!");
    		            	}
    		            	if(status[1]==FCMPS_CLS_ImportOrder.STATUS_ERROR) {
    		            		System.out.println(new Date()+" 第:"+status[0]+"行,導入失敗!");
    		            	}
    		            	if(status[1]==FCMPS_CLS_ImportOrder.STATUS_NOT_CONNECT_DB) {
    		            		System.out.println(new Date()+" 第:"+status[0]+"行,無法取得資料庫連線,重新導入!");
    		            	}
    	            	}
    			 }
    		}
    		    		    	
    	}catch(Exception ex){
    		ex.printStackTrace();
         		
    	}finally {
    		
    		if(isNeed_Self_Monitor()) {
        		if(FIC_Conn_Stack!=null) {
            		for(int i=0;i<threadCount;i++) {       			
            			Connection FIC_conn=FIC_Conn_Stack.pop();
            			closeConnection(FIC_conn);        			
            		}
        		}
        		
        		if(FCC_Conn_Stack!=null) {
            		for(int i=0;i<threadCount;i++) {       			
            			Connection FCC_conn=FCC_Conn_Stack.pop();
            			closeConnection(FCC_conn);        			
            		}
        		}
        		
        		if(FCMPS_Conn_Stack!=null) {
            		for(int i=0;i<threadCount;i++) {       			
            			Connection conn=FCMPS_Conn_Stack.pop();
            			closeConnection(conn);        			
            		}
        		}
    		}
    		    		
    	}
    }

    private void doImport(String FOS_File) throws Exception{

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
			if(!readSheet(sheet)){
				return;
			}
		}else {
			sheet = wb.getSheet(FOS_STATUS.toUpperCase());	
			if(sheet!=null) {
				if(!readSheet(sheet)){
					return;
				}
			}
		}
		
		FOS_STATUS="Pending";
		sheet = wb.getSheet(FOS_STATUS);			 			

		if(sheet!=null) {
			if(!readSheet(sheet)){
				return;
			}
		}else {
			sheet = wb.getSheet(FOS_STATUS.toUpperCase());
			if(sheet!=null) {
				if(!readSheet(sheet)){
					return;
				}
			}
		}
	
		FOS_STATUS="Closed";
		sheet = wb.getSheet(FOS_STATUS);			 			

		if(sheet!=null) {
			if(!readSheet(sheet)){
				return;
			}
		}else {
			sheet = wb.getSheet(FOS_STATUS.toUpperCase());
			if(sheet!=null) {
				if(!readSheet(sheet)){
					return;
				}
			}
		}
		
	    fileIn.close();
	    wb=null;   
			

    }
    
    private boolean readSheet(HSSFSheet sheet){
    	boolean iRet=false;
		
		int iRow=0;    		
		
		HSSFRow row = null;			
		HSSFCell cell = null;			
		row=sheet.getRow(iRow);
		String sql_Fields="";			
		
		FCMPS_CLS_ImportOrderFromFOS_Var cls_var=FCMPS_CLS_ImportOrderFromFOS_Var.getInstance();
		
		for(int i=0;i<displayItems.length;i++){
			cell = row.getCell((short)i);
			Object obj=FCMPS_PUBLIC.getCellValue(cell);
			
			if(!displayItems[i][0].toUpperCase().equals(FCMPS_PUBLIC.getValue(obj).toUpperCase())){
				System.out.println(displayItems[i][0].toUpperCase());
				System.out.println(FCMPS_PUBLIC.getValue(obj).toUpperCase());
				
				System.out.println("不是有效的FOS檔案格式!");
				CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setRow(iRow+1);				
				cls_message.setERROR("不是有效的FOS檔案格式!");								
				cls_var.add_Message(cls_message);
				
				return iRet;
			}else{
				if(!sql_Fields.equals("")) sql_Fields=sql_Fields+",";
				sql_Fields=sql_Fields+displayItems[i][1].toUpperCase();
			}
			
		}
		
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
			
			PO=PO.replace(" ", "");
//			System.out.println("Row:"+iRow+" "+new Date());
						

			if(OD_SHIP==null) {
				CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setOD_PONO1(OD_PONO1);
				cls_message.setSTYLE_NO(STYLE_NO);
				cls_message.setSH_NO(SH_NO);
				cls_message.setOD_QTY(OD_QTY);
				cls_message.setRow(iRow+1);
				
				cls_message.setERROR("沒有訂單交期!");
								
				cls_var.add_Message(cls_message);

    			iRow++;
    			
    			continue;
				
			}
			
			if(OD_FGDATE==null) {
				CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setOD_PONO1(OD_PONO1);
				cls_message.setSTYLE_NO(STYLE_NO);
				cls_message.setSH_NO(SH_NO);
				cls_message.setOD_QTY(OD_QTY);
				cls_message.setRow(iRow+1);
				cls_message.setERROR("沒有FG Date!");
				
				cls_var.add_Message(cls_message);
    			
    			iRow++;
    			
    			continue;
				
			}				
			
			boolean iExist=false;
								
			for(FCMPS_CLS_ImportOrder cls:ls_Thread) {
				if(cls.getPO().equals(PO)) {
					iExist=true;
					break;
				}
			}
						
			if(!iExist) {
				FCMPS_CLS_ImportOrder cls_ImportOrder=new FCMPS_CLS_ImportOrder();
				cls_ImportOrder.setBranch_Code(Branch_Code);
				cls_ImportOrder.setFA_NO(FA_NO);
				cls_ImportOrder.setFOS_STATUS(FOS_STATUS);
				cls_ImportOrder.setIRow(iRow);
				cls_ImportOrder.setKPR(KPR);
				cls_ImportOrder.setOD_FGDATE(OD_FGDATE);
				cls_ImportOrder.setOD_PONO1(OD_PONO1);
				cls_ImportOrder.setOD_QTY(OD_QTY);
				cls_ImportOrder.setOD_SHIP(OD_SHIP);
				cls_ImportOrder.setPO(PO);
				cls_ImportOrder.setSH_NO(SH_NO);
				cls_ImportOrder.setStatus(Status);
				cls_ImportOrder.setSTYLE_NO(STYLE_NO);
				cls_ImportOrder.setUP_USER(getUP_USER());
				
				cls_ImportOrder.setFCMPS_Conn_Stack(FCMPS_Conn_Stack);
				cls_ImportOrder.setFIC_Conn_Stack(FIC_Conn_Stack);
				cls_ImportOrder.setFCC_Conn_Stack(FCC_Conn_Stack);
				
				ls_Thread.add(cls_ImportOrder);
				
				resultList.submit(cls_ImportOrder);
				
				Order_Count++;
			}
			
//			Future<Map<FCMPS_CLS_ImportOrder,int[]>> result=pool.submit(cls_ImportOrder);
//			resultList.add(result);
			
			iRow++;
		}    	

		iRet=true;
    	return iRet;
    }    
	
	public List<CLS_RCCP_ERROR> getError_message() {
		FCMPS_CLS_ImportOrderFromFOS_Var cls_var=FCMPS_CLS_ImportOrderFromFOS_Var.getInstance();
		return cls_var.get_Messages();
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
//			config.addClass(FCMPS0101_BEAN.class);
			config.addClass(FCMPS010_BEAN.class);
			
			String USER=config.getProperty("connection.username");
			String URL=config.getProperty("connection.url");
			String PSW=config.getProperty("connection.password");
			String DRIVER=config.getProperty("connection.driver_class");
			
    		Class.forName(DRIVER); //加載驅動程序
    		conn=DriverManager.getConnection(URL,USER,PSW);
			
		}catch(Exception ex) {
			ex.printStackTrace();			
//			iRet=false;			
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
	
	private void doPrint() {	
		if(getError_message().isEmpty()) return;
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
				CLS_RCCP_ERROR error=getError_message().get(iRow);
				
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
	        	
			}while(iRow<getError_message().size());
									
			FileOutputStream fileOut=null;
			String fileID=UUID.generate();	
			fileOut = new FileOutputStream(getOutput()+"/"+fileID+".xls");
			
			wb.write(fileOut);
			fileOut.close();			
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
	}
	
	private class FCMPS_CLS_ImportOrder_Monitor extends TimerTask{
		private List<FCMPS_CLS_ImportOrder> ls_Class=null;
		private Date st_Date=null;
		private int iCompleted=0;
		private int iCancel=0;
		private int iError=0;
		
		public FCMPS_CLS_ImportOrder_Monitor(List<FCMPS_CLS_ImportOrder> ls_Class) {
			this.ls_Class=ls_Class;
			st_Date=new Date();
		}
		
		public void run() {
			if(!ls_Class.isEmpty()) {
				boolean isAllCompleted=true;
				int iRunning=0;
				int iRedo=0;
				
				List<FCMPS_CLS_ImportOrder> delete_cls=new ArrayList<FCMPS_CLS_ImportOrder>();
				List<FCMPS_CLS_ImportOrder> redo_cls=new ArrayList<FCMPS_CLS_ImportOrder>();
				
				for(FCMPS_CLS_ImportOrder cls:ls_Class) {
					if(cls.getSTATUS()==cls.STATUS_CANCEL) {
						iCancel++;
						delete_cls.add(cls);
					}				
					if(cls.getSTATUS()==cls.STATUS_RUNNING) {
						isAllCompleted=false;
						iRunning++;
					}
					if(cls.getSTATUS()==cls.STATUS_NOT_CONNECT_DB) {
						iRedo++;
						redo_cls.add(cls);
					}
					if(cls.getSTATUS()==cls.STATUS_COMPLETED) {
						iCompleted++;
						delete_cls.add(cls);					
					}
					if(cls.getSTATUS()==cls.STATUS_ERROR) {
						iError++;
						delete_cls.add(cls);					
					}
				}
				
				if(!delete_cls.isEmpty()) {
					ls_Class.removeAll(delete_cls);
				}
				
				for(FCMPS_CLS_ImportOrder cls:delete_cls) {
					cls=null;
				}
				
				delete_cls.clear();
				delete_cls=null;
				
				Date edDate=new Date();			
				Long iEscapeTime=edDate.getTime()-st_Date.getTime();
				long iForecastTime=0;
				if(iCompleted!=0) iForecastTime=iEscapeTime/iCompleted*(iRunning+iRedo)/1000/60;
								
				System.out.println(new Date().getTime()-st_Date.getTime()+"毫秒," +
						           " 完成:"+iCompleted+
						           " 取消:"+iCancel+
						           " 錯誤:"+iError+
						           " 等待:"+iRunning+
						           " 重導:"+iRedo+
						           (iForecastTime>0?" 預計還需:"+iForecastTime+" 分鐘完成":""));
				
				if(isAllCompleted) {
					if(iRedo>0) {
						ExecutorService pool=Executors.newFixedThreadPool(threadCount);
						for(FCMPS_CLS_ImportOrder cls:redo_cls) {
							cls.setSTATUS(cls.STATUS_RUNNING);
							pool.submit(cls);
						}
						pool.shutdown();
					}else {
						System.out.println(st_Date.toString()+" 到  "+new Date().toString()+" 所有線程都已完成!");	
						cancel();
					}

				}
				
			}
		}
	}

	
}
