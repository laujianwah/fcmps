package fcmps.test;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.cfg.Configuration;

import fcmps.domain.FCMPS010_BEAN;
import fcmps.ui.CLS_RCCP_ERROR;
import fcmps.ui.FCMPS_PUBLIC;
import fcmps.ui.JExportToExcel;

public class FCMPS_CLS_OtherTest2 extends TestCase {
	String PLAN_File="F:/臨時文件/2014/20141208/PR/2015外.xls";
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

		Connection conn=getConnection(config_xml);

		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		try{
			strSQL="SELECT distinct SH_ARITCLE,SH_COLOR FROM FCPS05 " +
					"WHERE TO_CHAR(P_ETD,'IYIW')>=1401 AND TO_CHAR(P_ETD,'IYIW')<=1453"+
					"  and pb_fano='FIC'"+
					"  and od_custno='CROCS'"+
					"  and OD_TYPE='1'"+
					"  and PR_YN<>'C'"+
				    "  and SH_ARITCLE NOT IN (SELECT SH_ARITCLE "+
				    "                           FROM FCPS22 "+
				    "                          WHERE SH_CYN = 'Y' "+
				    "                            AND SH_YN = 'Y')"+
				    "ORDER BY SH_ARITCLE,SH_COLOR";
	
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		    rs.setFetchSize(3000);
		    
		    if(rs.next()){
		    	List<CLS_RCCP_ERROR> message=new ArrayList<CLS_RCCP_ERROR>();
		    	
		    	do {
		    		String SH_NO=rs.getString("SH_ARITCLE");
		    		String SH_COLOR=rs.getString("SH_COLOR");
		    		
		    		strSQL="select * from DSSH05 where SH_NO='"+SH_NO+"'";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    if(rs2.next()) {
				    	for(int i=1;i<=40;i++) {
				    		if(FCMPS_PUBLIC.getValue(rs2.getString("U"+i)).equals("T")) {
				    			String SH_SIZE=rs2.getString("T"+i);
				    			int OD_QTY=0;
				    			strSQL="select sum(QTY"+i+") OD_QTY from FCPS05 " +
				    				   "where SH_ARITCLE='"+SH_NO+"' " +
				    				   "  and SH_COLOR='"+SH_COLOR+"'"+
				    				   "  and TO_CHAR(P_ETD,'IYIW')>=1401 AND TO_CHAR(P_ETD,'IYIW')<=1453"+
				   					   "  and pb_fano='FIC'"+
									   "  and od_custno='CROCS'"+
									   "  and OD_TYPE='1'"+
									   "  and PR_YN<>'C'";
							    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
							    rs3=pstmtData3.executeQuery();
							    if(rs3.next()) {
							    	OD_QTY=FCMPS_PUBLIC.getInt(rs3.getInt("OD_QTY"));
							    }
							    rs3.close();
							    pstmtData3.close();
							    
							    if(OD_QTY>0) {
							    	CLS_RCCP_ERROR data=new CLS_RCCP_ERROR();
							    	data.setSH_NO(SH_NO);
							    	data.setSH_COLOR(SH_COLOR);
							    	data.setSH_SIZE(SH_SIZE);
							    	data.setOD_QTY(OD_QTY);
							    	message.add(data);							    								    	
							    }							    							    
				    		}
				    	}
				    }
				    rs2.close();
				    pstmtData2.close();
		    				    		
		    	}while(rs.next());
		    	
		    	this.doPrint(message, "2014_OUT.xls");
		    	
		    }
			rs.close();
			pstmtData.close();
			
			
		}catch(Exception ex) {
			ex.printStackTrace();
	    }finally{
	    	this.closeConnection(conn);
		}	
		
	}

	
	private void doPrint(List<CLS_RCCP_ERROR> message,String fileName) {	
		String Main_Path="F:/臨時文件/2015/20150515";
		
		JExportToExcel JETE=new JExportToExcel();
		HSSFWorkbook wb=JETE.getWorkbook();
		HSSFSheet sheet=wb.createSheet();
				
		String items[]=new String[] {
        		"Style Code",
        		"Style",
        		"Color",
        		"Size",
        		"QTY"};
		
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
				CLS_RCCP_ERROR error=message.get(iRow);
				
				row = sheet.createRow(iRow+1);
				
				short iCol=0;
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
	        	
			}while(iRow<message.size());
									
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
