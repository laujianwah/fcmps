package fcmps.test;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import junit.framework.TestCase;
import oracle.jdbc.OracleTypes;
import oracle.sql.ARRAY;
import oracle.sql.Datum;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.cfg.Configuration;

import fcmps.domain.FCMPS010_BEAN;
import fcmps.ui.JExportToExcel;

public class FCMPS_CLS_OtherTest extends TestCase {
	String PLAN_File="2015.xls";
	String FA_NO="FIC";
	
	public void test_Rccp() {
		String path="";
		String package_path[]=FCMPS_CLS_AutoGenerateRccpPlanTest.class.getPackage().toString().split(" ");
		package_path=package_path[1].split("\\.");
		for(int i=0;i<package_path.length;i++) {
			if(!path.equals(""))path=path+"/";
			path=path+package_path[i];
		}
		
		File file = new File("");	  
		path=file.getAbsolutePath()+"/src/"+path;
		
		String config_xml=path+"/FTIDB04.cfg.xml";

		Connection conn=this.getConnection(config_xml);
		
		String procedure = "{call FCMPS_Packages.Stat_Same_Of_LastWeek(?,?) }";
		
		CallableStatement cs = null;
		
		try{

            cs = conn.prepareCall(procedure);

            cs.registerOutParameter(2, OracleTypes.ARRAY, "TABLE_NUMBER".toUpperCase()); 
            cs.setString(1, "1523SC");
            cs.execute();
            
            ARRAY obj = (ARRAY) cs.getArray(2);  
            Datum[] datas = obj.getOracleArray();//获取对象  
            
            this.doPrint(datas, PLAN_File);
            
/*//          遍历对象数组  
            for(int i=0;i<datas.length;i++){  
                System.out.println("对象"+i);  
                //获取属性  
                Datum[] beanAttributes =((ARRAY)datas[i]).getOracleArray();  
                //遍历属性  
                for(int m=0;m<beanAttributes.length;m++){  
                    System.out.println("  "+beanAttributes[m].intValue());  
                }  
            }  */
            
		}catch(Exception ex) {
			ex.printStackTrace();
	    }finally{
	    	closeConnection(conn);
		}	
		
	}

	private void doPrint(Datum[] datas,String fileName) {	
		String Main_Path="F:/臨時文件/2015/20150515";
		
		JExportToExcel JETE=new JExportToExcel();
		HSSFWorkbook wb=JETE.getWorkbook();
		HSSFSheet sheet=wb.createSheet();
				
		String items[]=new String[] {
        		"周次",
        		"主型體個數",
        		"配色個數",
        		"主型體與上周相同個數",
        		"%",
        		"配色與上周相同個數",
        		"%",
        		"與上周同型體不同配色個數",
        		"%"};
		
		try {
			
			int iRow=0;
			
	    	HSSFRow row = sheet.createRow(iRow);
	    	HSSFCell cell = row.createCell((short)0);
	    	
			for(int iCol=0;iCol<items.length;iCol++) {
				row = sheet.createRow(iRow);
		    	cell = row.createCell((short)iCol);
		    	JETE.setCellValue(wb, cell, items[iCol]);  
			}
			
			double PREV_MAIN_SH_COUNT=0;
			double PREV_SH_COLOR_COUNT=0;
			
			HSSFDataFormat format = wb.createDataFormat();
			HSSFCellStyle style = wb.createCellStyle();
		    style.setDataFormat(format.getFormat("0.0%"));
			
			do {
				
				row = sheet.createRow(iRow+1);
				
				Datum[] beanAttributes =((ARRAY)datas[iRow]).getOracleArray(); 
				
				int WORK_WEEK=beanAttributes[0].intValue();
				double MAIN_SH_COUNT=beanAttributes[1].doubleValue(); //主型體個數
				double SH_COLOR_COUNT=beanAttributes[2].doubleValue(); //配色個數
				
				short iCol=0;
		    	cell = row.createCell((short)iCol);
		       	JETE.setCellValue(wb, cell, WORK_WEEK);
		       	
		       	iCol++;
		    	cell = row.createCell((short)iCol);
		       	JETE.setCellValue(wb, cell, MAIN_SH_COUNT);
		       	
		       	iCol++;
		    	cell = row.createCell((short)iCol);
		       	JETE.setCellValue(wb, cell, SH_COLOR_COUNT);
		       	
		       	if(PREV_MAIN_SH_COUNT==0) {
					PREV_MAIN_SH_COUNT=MAIN_SH_COUNT;
					PREV_SH_COLOR_COUNT=SH_COLOR_COUNT;
		       	 	iRow++;	
		       		continue;
		       	}
		       	
				double MAIN_SAME_COUNT=beanAttributes[3].doubleValue(); //主型體與上周相同個數
				double COLOR_SAME_COUNT=beanAttributes[4].doubleValue(); //配色與上周相同個數
				double COLOR_OTHER_COUNT=beanAttributes[5].doubleValue(); //與上周同型體不同配色個數
		       	
		       	iCol++;
		    	cell = row.createCell((short)iCol);
		       	JETE.setCellValue(wb, cell, MAIN_SAME_COUNT);
		       	
		       	iCol++;
		    	cell = row.createCell((short)iCol);
		    	cell.setCellStyle(style);
		       	JETE.setCellValue(wb, cell, MAIN_SAME_COUNT/PREV_MAIN_SH_COUNT);
		       	
		       	iCol++;
		    	cell = row.createCell((short)iCol);
		       	JETE.setCellValue(wb, cell, COLOR_SAME_COUNT);
		       			       	
		       	iCol++;
		    	cell = row.createCell((short)iCol);
		    	cell.setCellStyle(style);
		       	JETE.setCellValue(wb, cell, COLOR_SAME_COUNT/PREV_SH_COLOR_COUNT);
		       	
		       	iCol++;
		    	cell = row.createCell((short)iCol);
		       	JETE.setCellValue(wb, cell, COLOR_OTHER_COUNT);
		       	
		       	iCol++;
		    	cell = row.createCell((short)iCol);		   
		    	cell.setCellStyle(style);
		       	JETE.setCellValue(wb, cell, COLOR_OTHER_COUNT/PREV_SH_COLOR_COUNT);
		       	
				PREV_MAIN_SH_COUNT=MAIN_SH_COUNT;
				PREV_SH_COLOR_COUNT=SH_COLOR_COUNT;
								
	        	iRow++;			
	        	
			}while(iRow<datas.length);
									
	    	File file=new File(Main_Path+"/"+fileName+".xls");
	    	if(file.exists()) {
	    		file.delete();
	    	}else {
	    		file=new File(Main_Path+"/");
	    		if(!file.exists()) {
	    			file.mkdir();
	    		}
	    	}
	    	
			FileOutputStream fileOut=null;
			fileOut = new FileOutputStream(Main_Path+"/"+fileName+".xls");
			
			wb.write(fileOut);
			fileOut.close();			
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
	}
	
	/**
	 * 產生SessionFactory
	 *
	 */
	private Connection getConnection(String config_xml) {
		Connection iRet=null;
		try {
			File fConfig=new File(config_xml);
			if(!fConfig.exists()) {
				System.out.println( "The Config file " + config_xml+" does not exist!" );
				return iRet;
			}
			Configuration config=new Configuration().configure(fConfig);	
//			config.addClass(FCMPS0101_BEAN.class);
			config.addClass(FCMPS010_BEAN.class);
			
			String USER=config.getProperty("connection.username");
			String URL=config.getProperty("connection.url");
			String PSW=config.getProperty("connection.password");
			String DRIVER=config.getProperty("connection.driver_class");
			
    		Class.forName(DRIVER); //加載驅動程序
    		iRet=DriverManager.getConnection(URL,USER,PSW);
			
		}catch(Exception ex) {
			ex.printStackTrace();		
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
