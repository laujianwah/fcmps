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
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import dsc.util.function.UUID;
import fcmps.domain.FCMPS0101_BEAN;
import fcmps.domain.FCMPS010_BEAN;

public class FCMPS_CLS_Compare_FOS_ERP_Order extends TestCase{
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

	private Map<String,ArrayList<String[]>> ls_SH_SIZE=new HashMap<String,ArrayList<String[]>>();
	
	private SessionFactory sessionFactory=null;
    private String config_xml="";
    
    private Connection conn=null;
    
    private String output=".";
        
	private String UP_USER="DEV";	
	
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
    	String FOS_File="F:/臨時文件/2014/20140723/FOS 101-0723.xls";
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
			conn=session.connection();
			
		}catch(Exception ex) {
			ex.printStackTrace();
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

			List<ORDER_DATA> data_set=new ArrayList<ORDER_DATA>();
			
			if(sheet!=null) {
				if(!readSheet(data_set,sheet, conn)){
					return;
				}
			}
			
			FOS_STATUS="Pending";
			sheet = wb.getSheet(FOS_STATUS);			 			

			if(sheet!=null) {
				if(!readSheet(data_set,sheet, conn)){
					return;
				}
			}
		
			FOS_STATUS="Closed";
			sheet = wb.getSheet(FOS_STATUS);			 			

			if(sheet!=null) {
				if(!readSheet(data_set,sheet, conn)){
					return;
				}
			}
			
		    fileIn.close();
		    wb=null;   
			
		    this.doPrint(data_set);
		    
		}catch(Exception ex){
			ex.printStackTrace();
		}

    }
    
    private boolean readSheet(List<ORDER_DATA> data_set,HSSFSheet sheet,Connection conn){
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
			
			//FOS中廠別是FIC ,Branch Code 為 CHN 的訂單為內銷訂單, 其它均為外銷
			//FVI的FOS中也有Branch Code 為 CHN 的訂單,但FVI不分內外銷
			if(FA_NO.equals("FIC") && Branch_Code.toUpperCase().equals("CHN")) {//FIC內銷訂單
	            if(!exist_OD_IN_ERP(OD_PONO1, FCC_conn)) {
	            	 if(!exist_OD_IN_ERP(PO, FCC_conn)) {
	            		System.out.println("Row:"+(iRow+1)+" "+OD_PONO1+" 訂單系統無此訂單!");	            		
		    			iRow++;
		    			
		    			continue;
	            	 }else {
	            		OD_PONO1=PO;
	            	 }	    			
	            }
			}else {
	            if(!exist_OD_IN_ERP(OD_PONO1, conn)) {
	            	 if(!exist_OD_IN_ERP(PO, conn)) {
	            		 System.out.println("Row:"+(iRow+1)+" "+OD_PONO1+" 訂單系統無此訂單!");	    

		    			iRow++;
		    			
		    			continue;
	            	 }else {
	            		OD_PONO1=PO;	            		 
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
				System.out.println("Row:"+(iRow+1)+" "+STYLE_NO+" "+SH_NO+" 沒有對應的內部型體名稱!");    			
    			continue;
			}			
						
		    ArrayList<String[]> SH_SIZE=ls_SH_SIZE.get(MODEL_CNA);
		    
			if(SH_SIZE==null) {
				SH_SIZE=getSH_SIZE(MODEL_CNA, conn);
				ls_SH_SIZE.put(MODEL_CNA, SH_SIZE);
			}	

			if(FA_NO.equals("FIC") && Branch_Code.toUpperCase().equals("CHN")) {//FIC內銷訂單
				this.doCompare(FA_NO, OD_PONO1, STYLE_NO, MODEL_CNA, OD_QTY, SH_SIZE, Status, data_set, FCC_conn);
			}else {
				this.doCompare(FA_NO, OD_PONO1, STYLE_NO, MODEL_CNA, OD_QTY, SH_SIZE, Status, data_set, conn);
			}

			iRow++;
		}    	
		
		if(FCC_conn!=null) closeConnection(FCC_conn);
		
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

	public String doCompare(
			String FA_NO,
			String OD_PONO1,
			String STYLE_NO,
			String SH_NO,
			double OD_QTY,
			ArrayList<String[]> ls_SH_SIZE,
			String FOS_STATUS,
            List<ORDER_DATA> data_set,
			Connection conn) {
		String iRet="";
		String strSQL="";
				
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;

		try{
			    		    		
    		//因為fos中沒有顏色, 需要先找出PO# 型體的配色
			strSQL="select " +
               "sh_aritcleno,"+                   
               "sh_color,"+
               "sum(od_qty) od_qty "+
               "from dsod00 "+
               "where od_Pono1='"+OD_PONO1+"' "+
               "  and sh_aritcleno='"+SH_NO+"' "+
               "group by sh_aritcleno,sh_color";
	
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	
	    		String SIZE_FIELD="";
	    		for(int i=1;i<=40;i++) {
	    			if(!SIZE_FIELD.equals("")) SIZE_FIELD=SIZE_FIELD+",";
	    			SIZE_FIELD=SIZE_FIELD+"T"+i+",U"+i;
	    		}
	    		
		    	do {
		    		
		    		String SH_COLOR=FCMPS_PUBLIC.getValue(rs.getString("SH_COLOR"));
		    		
		    		for(int i=0;i<ls_SH_SIZE.size();i++) {
		    			String SH_SIZE=ls_SH_SIZE.get(i)[0];
			    		
			    		double SIZE_OD_QTY=0;
			    		
			    		//找出PO#,型體,顏色對應的SIZE數量
			    		strSQL="select sum(S"+ls_SH_SIZE.get(i)[1]+") OD_QTY from DSOD_03 " +
			    			   "where OD_NO IN (select od_no from DSOD00 " +
			    			   "           where sh_aritcleno='"+SH_NO+"' " +
			    			   "             and SH_COLOR='"+SH_COLOR+"' " +
			    			   "             and OD_PONO1='"+OD_PONO1+"')";
					    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
					    rs3=pstmtData3.executeQuery();
					    
					    if(rs3.next()){
					    	SIZE_OD_QTY=FCMPS_PUBLIC.getDouble(rs3.getDouble("OD_QTY"));
					    }
			    		rs3.close();
			    		pstmtData3.close();
			    		
			    		if(SIZE_OD_QTY==0) continue; //SIZE沒有訂單量
			    					    					    		
			    		ORDER_DATA data=new ORDER_DATA();
			    		data.setFA_NO(FA_NO);
			    		data.setOD_CODE(FOS_STATUS);
			    		data.setOD_PONO1(OD_PONO1);
			    		data.setOD_QTY(OD_QTY);
			    		data.setSH_COLOR(SH_COLOR);
			    		data.setSH_NO(SH_NO);
			    		data.setSH_SIZE(SH_SIZE);
			    		data.setSIZE_QTY(SIZE_OD_QTY);
			    		data.setSTYLE_NO(SH_NO);
			    		
			    		data_set.add(data);
			    		
		    		}

		    	}while(rs.next());
		    }else {
		    	System.out.println(OD_PONO1+" 訂單系統無此訂單!");
		    	iRet=OD_PONO1+" 訂單系統無此訂單!";
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
	
	private void doPrint(List<ORDER_DATA> data_set) {	
		if(data_set.isEmpty()) return;
		JExportToExcel JETE=new JExportToExcel();
		HSSFWorkbook wb=JETE.getWorkbook();
		HSSFSheet sheet=wb.createSheet();
		
		String items[]=new String[] {
				"Factory",
        		"PO#",
        		"Style Code",
        		"Style",
        		"Color",
        		"Size",
        		"PO QTY",
        		"Size QTY",
        		"Status"};
		
		try {
			
			int iRow=0;
			
	    	HSSFRow row = sheet.createRow(iRow);
	    	HSSFCell cell = row.createCell((short)0);
	    	
			for(int iCol=0;iCol<items.length;iCol++) {
				row = sheet.createRow(iRow);
		    	cell = row.createCell((short)iCol);
		    	JETE.setCellValue(wb, cell, items[iCol]);  
			}
			
			int recNo=0;
			do {
				ORDER_DATA data=data_set.get(recNo);
				
				row = sheet.createRow(iRow+1);
				
				short iCol=0;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, data.getFA_NO());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, data.getOD_PONO1());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, data.getSTYLE_NO());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, data.getSH_NO());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, data.getSH_COLOR());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, data.getSH_SIZE());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, data.getOD_QTY());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, data.getSIZE_QTY());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, data.getOD_CODE());
	        	
	        	iRow++;			
	        	
	    		if(iRow==65535) {
	    			sheet=wb.createSheet();
	    			iRow=0;		
	    			for(int i=0;i<items.length;i++) {
	    				row = sheet.createRow(iRow);
	    		    	cell = row.createCell((short)i);
	    		    	JETE.setCellValue(wb, cell, items[i]);  
	    			}
	    		}
	        	
	    		recNo++;
	    		
			}while(recNo<data_set.size());
									
			FileOutputStream fileOut=null;
			String fileID=UUID.generate();	
			fileOut = new FileOutputStream(getOutput()+"/"+fileID+".xls");
			
			wb.write(fileOut);
			fileOut.close();			
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
	}

	private class ORDER_DATA {
		private String FA_NO="";
		private String OD_PONO1="";
		private String SH_NO="";
		private String STYLE_NO="";
		private String SH_COLOR="";
		private String SH_SIZE="";
		private double OD_QTY=0;
		private double SIZE_QTY=0;
		private String OD_CODE="";
		
		public String getFA_NO() {
			return FA_NO;
		}
		public void setFA_NO(String fa_no) {
			FA_NO = fa_no;
		}
		public String getOD_CODE() {
			return OD_CODE;
		}
		public void setOD_CODE(String od_code) {
			OD_CODE = od_code;
		}
		public String getOD_PONO1() {
			return OD_PONO1;
		}
		public void setOD_PONO1(String od_pono1) {
			OD_PONO1 = od_pono1;
		}
		public double getOD_QTY() {
			return OD_QTY;
		}
		public void setOD_QTY(double od_qty) {
			OD_QTY = od_qty;
		}
		public String getSH_COLOR() {
			return SH_COLOR;
		}
		public void setSH_COLOR(String sh_color) {
			SH_COLOR = sh_color;
		}
		public String getSH_NO() {
			return SH_NO;
		}
		public void setSH_NO(String sh_no) {
			SH_NO = sh_no;
		}
		public String getSH_SIZE() {
			return SH_SIZE;
		}
		public void setSH_SIZE(String sh_size) {
			SH_SIZE = sh_size;
		}
		public double getSIZE_QTY() {
			return SIZE_QTY;
		}
		public void setSIZE_QTY(double size_qty) {
			SIZE_QTY = size_qty;
		}
		public String getSTYLE_NO() {
			return STYLE_NO;
		}
		public void setSTYLE_NO(String style_no) {
			STYLE_NO = style_no;
		}
		
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
