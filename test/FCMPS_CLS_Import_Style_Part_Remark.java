package fcmps.test;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.cfg.Configuration;

import fcmps.ui.FCMPS_PUBLIC;

public class FCMPS_CLS_Import_Style_Part_Remark {
	private String config_xml="";
    
	private String UP_USER="DEV";	
	
	public String getUP_USER() {
		return UP_USER;
	}

	public void setUP_USER(String up_user) {
		UP_USER = up_user;
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
				return conn;
			}
			
			Configuration config=new Configuration().configure(fConfig);	
			
			String USER=config.getProperty("connection.username");
			String URL=config.getProperty("connection.url");
			String PSW=config.getProperty("connection.password");
			String DRIVER=config.getProperty("connection.driver_class");
			
    		Class.forName(DRIVER); //加載驅動程序
    		conn=DriverManager.getConnection(URL,USER,PSW);
			
		}catch(Exception ex) {
			ex.printStackTrace();	
		}
		return conn;
	}
	
	private String getConfig_XML() {
		return config_xml;
	}

	public void setConfig_xml(String config_xml) {
		this.config_xml = config_xml;
	}

	public void doImport(String fileName) {
		File file=new File(fileName);
		if(!file.exists()) {
			System.out.println("文件:"+fileName+" 不存在!");
			return;
		}
		doImport(file);
	}
	
	public void doImport(File file) {

		try {
			
			System.out.println(new Date()+" "+"連接資料庫");
			
			Connection conn=GenericSessionFactory();
			if(conn==null) {
				System.out.println("無法連接資料庫!");
				return;
			}
			
			System.out.println(new Date()+" "+"開始讀取型體部位顏色");
			
			FileInputStream fileIn = new FileInputStream(file);
			if(fileIn==null) {
				System.out.println("無法讀取:"+file.getName());
				return;
			}
			
			HSSFWorkbook wb=new HSSFWorkbook(fileIn);

			for(int i=0;i<wb.getNumberOfSheets();i++) {
				HSSFSheet sheet = wb.getSheetAt(i);		 			

				if(sheet!=null) {
					System.out.println(new Date()+" "+"開始讀取第"+i+"頁");
					if(!readSheet(sheet,conn)){
						return;
					}
				}
			}
			
			fileIn.close();
			wb=null;
			
			System.out.println(new Date()+" "+"讀取完畢!");

			
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			
		}
		
	}
	
    private boolean readSheet(HSSFSheet sheet,Connection conn) throws Exception{
    	boolean iRet=false;
		
		int iRow=2;    		
		
		HSSFRow row = null;			
		HSSFCell cell = null;			
		row=sheet.getRow(iRow);		

		while(iRow<sheet.getLastRowNum()){
			iRow++;
			row=sheet.getRow(iRow);
			if(row==null) continue;
			
			System.out.println(new Date()+" Row:"+(iRow+1));
			
			cell = row.getCell((short)0);
			if(FCMPS_PUBLIC.getCellValue(cell)==null) break;
			
			Object obj=FCMPS_PUBLIC.getCellValue(cell,2);
			String STYLE_CODE=FCMPS_PUBLIC.getValue(obj).trim();
			
			cell = row.getCell((short)2);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			String SH_NO=FCMPS_PUBLIC.getValue(obj).trim();
			
			String PART_NO="01"; //大底
			String PROCID="100";
			
			cell = row.getCell((short)6);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			String PART_MODEL=FCMPS_PUBLIC.getValue(obj).trim();
			
			if(!PART_MODEL.equals("")) {
				String strSQL="delete from FCMPS033 " +
						      "where SH_NO='"+SH_NO+"' " +
						      "  and PROCID='"+PROCID+"' " +
						      "  and PART_NO='"+PART_NO+"' ";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps033 (style_no, sh_no,part_no, procid,remark, up_user, up_date) "+
                       " values ("+
                       "'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PROCID+"'"+
                       ",'模具代號:'||'"+PART_MODEL+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}
			
			PROCID="600";
			
			cell = row.getCell((short)7);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			String PART_ZY=FCMPS_PUBLIC.getValue(obj).trim();
			if(!PART_ZY.equals("")) {
				String strSQL="delete from FCMPS033 " +
						      "where SH_NO='"+SH_NO+"' " +
						      "  and PROCID='"+PROCID+"' " +
						      "  and PART_NO='"+PART_NO+"' ";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps033 (style_no, sh_no,part_no, procid,remark, up_user, up_date) "+
                       " values ("+
                       "'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PROCID+"'"+
                       ",'"+PART_ZY+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}

			PART_NO="27"; //鞋墊
			PROCID="100";
			cell = row.getCell((short)9);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			PART_MODEL=FCMPS_PUBLIC.getValue(obj).trim();
			
			if(!PART_MODEL.equals("")) {
				String strSQL="delete from FCMPS033 " +
						      "where SH_NO='"+SH_NO+"' " +
						      "  and PROCID='"+PROCID+"' " +
						      "  and PART_NO='"+PART_NO+"' ";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps033 (style_no, sh_no,part_no, procid,remark, up_user, up_date) "+
                       " values ("+
                       "'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PROCID+"'"+
                       ",'模具代號:'||'"+PART_MODEL+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}
			
			PROCID="600";
			
			cell = row.getCell((short)10);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			PART_ZY=FCMPS_PUBLIC.getValue(obj).trim();
			if(!PART_ZY.equals("")) {
				String strSQL="delete from FCMPS033 " +
						      "where SH_NO='"+SH_NO+"' " +
						      "  and PROCID='"+PROCID+"' " +
						      "  and PART_NO='"+PART_NO+"' ";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps033 (style_no, sh_no,part_no, procid,remark, up_user, up_date) "+
                       " values ("+
                       "'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PROCID+"'"+
                       ",'"+PART_ZY+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}

			PART_NO="03"; //鞋面
			PROCID="100";
			cell = row.getCell((short)12);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			PART_MODEL=FCMPS_PUBLIC.getValue(obj).trim();
			
			if(!PART_MODEL.equals("")) {
				String strSQL="delete from FCMPS033 " +
						      "where SH_NO='"+SH_NO+"' " +
						      "  and PROCID='"+PROCID+"' " +
						      "  and PART_NO='"+PART_NO+"' ";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps033 (style_no, sh_no,part_no, procid,remark, up_user, up_date) "+
                       " values ("+
                       "'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PROCID+"'"+
                       ",'模具代號:'||'"+PART_MODEL+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}
			
			PROCID="600";
			
			cell = row.getCell((short)13);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			PART_ZY=FCMPS_PUBLIC.getValue(obj).trim();
			if(!PART_ZY.equals("")) {
				String strSQL="delete from FCMPS033 " +
						      "where SH_NO='"+SH_NO+"' " +
						      "  and PROCID='"+PROCID+"' " +
						      "  and PART_NO='"+PART_NO+"' ";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps033 (style_no, sh_no,part_no, procid,remark, up_user, up_date) "+
                       " values ("+
                       "'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PROCID+"'"+
                       ",'"+PART_ZY+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}
			
			
			PART_NO="06"; //鞋帶
			PROCID="100";
			cell = row.getCell((short)15);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			PART_MODEL=FCMPS_PUBLIC.getValue(obj).trim();
			
			if(!PART_MODEL.equals("")) {
				String strSQL="delete from FCMPS033 " +
						      "where SH_NO='"+SH_NO+"' " +
						      "  and PROCID='"+PROCID+"' " +
						      "  and PART_NO='"+PART_NO+"' ";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps033 (style_no, sh_no,part_no, procid,remark, up_user, up_date) "+
                       " values ("+
                       "'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PROCID+"'"+
                       ",'模具代號:'||'"+PART_MODEL+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}
			
			PROCID="600";
			
			cell = row.getCell((short)16);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			PART_ZY=FCMPS_PUBLIC.getValue(obj).trim();
			if(!PART_ZY.equals("")) {
				String strSQL="delete from FCMPS033 " +
						      "where SH_NO='"+SH_NO+"' " +
						      "  and PROCID='"+PROCID+"' " +
						      "  and PART_NO='"+PART_NO+"' ";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps033 (style_no, sh_no,part_no, procid,remark, up_user, up_date) "+
                       " values ("+
                       "'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PROCID+"'"+
                       ",'"+PART_ZY+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}

		}    	
		
		iRet=true;
    	return iRet;
    }  
}
