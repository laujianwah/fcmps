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

public class FCMPS_CLS_Import_Style_Part_Color {
	private String config_xml="";
    
	private String UP_USER="DEV";	
	private String FA_NO="";
	
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
		
		int iRow=1;    		
		
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
		
			cell = row.getCell((short)3);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			String PLANNER=FCMPS_PUBLIC.getValue(obj).trim();
			
			cell = row.getCell((short)4);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			String SH_COLOR=FCMPS_PUBLIC.getValue(obj).trim();
			
			String PART_NO="01"; //大底
			cell = row.getCell((short)5);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			String PART_COLOR=FCMPS_PUBLIC.getValue(obj).trim();
			
			if(!PART_COLOR.equals("")) {
				String strSQL="delete from FCMPS030 " +
						      "where FA_NO='"+getFA_NO()+"' " +
						      "  and SH_NO='"+SH_NO+"' " +
						      "  and SH_COLOR='"+SH_COLOR+"' " +
						      "  and PART_NO='"+PART_NO+"' " +
						      "  and PART_COLOR='"+PART_COLOR+"'";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps030 (fa_no, style_no, sh_no,planner, sh_color, part_no, part_color, up_user, up_date) "+
                       " values ("+
                       "'"+getFA_NO()+"'"+
                       ",'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PLANNER+"'"+
                       ",'"+SH_COLOR+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PART_COLOR+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}
			
			PART_NO="27"; //鞋墊
			cell = row.getCell((short)6);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			PART_COLOR=FCMPS_PUBLIC.getValue(obj).trim();
			
			if(!PART_COLOR.equals("")) {
				String strSQL="delete from FCMPS030 " +
						      "where FA_NO='"+getFA_NO()+"' " +
						      "  and SH_NO='"+SH_NO+"' " +
						      "  and SH_COLOR='"+SH_COLOR+"' " +
						      "  and PART_NO='"+PART_NO+"' " +
						      "  and PART_COLOR='"+PART_COLOR+"'";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps030 (fa_no, style_no, sh_no,planner, sh_color, part_no, part_color, up_user, up_date) "+
                       " values ("+
                       "'"+getFA_NO()+"'"+
                       ",'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PLANNER+"'"+
                       ",'"+SH_COLOR+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PART_COLOR+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}
			
			PART_NO="03"; //鞋面
			cell = row.getCell((short)7);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			PART_COLOR=FCMPS_PUBLIC.getValue(obj).trim();
			
			if(!PART_COLOR.equals("")) {
				String strSQL="delete from FCMPS030 " +
						      "where FA_NO='"+getFA_NO()+"' " +
						      "  and SH_NO='"+SH_NO+"' " +
						      "  and SH_COLOR='"+SH_COLOR+"' " +
						      "  and PART_NO='"+PART_NO+"' " +
						      "  and PART_COLOR='"+PART_COLOR+"'";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps030 (fa_no, style_no, sh_no,planner, sh_color, part_no, part_color, up_user, up_date) "+
                       " values ("+
                       "'"+getFA_NO()+"'"+
                       ",'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PLANNER+"'"+
                       ",'"+SH_COLOR+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PART_COLOR+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}
			
			PART_NO="06"; //鞋帶
			cell = row.getCell((short)8);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			PART_COLOR=FCMPS_PUBLIC.getValue(obj).trim();
			
			if(!PART_COLOR.equals("")) {
				String strSQL="delete from FCMPS030 " +
						      "where FA_NO='"+getFA_NO()+"' " +
						      "  and SH_NO='"+SH_NO+"' " +
						      "  and SH_COLOR='"+SH_COLOR+"' " +
						      "  and PART_NO='"+PART_NO+"' " +
						      "  and PART_COLOR='"+PART_COLOR+"'";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps030 (fa_no, style_no, sh_no,planner, sh_color, part_no, part_color, up_user, up_date) "+
                       " values ("+
                       "'"+getFA_NO()+"'"+
                       ",'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PLANNER+"'"+
                       ",'"+SH_COLOR+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PART_COLOR+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}
			
			PART_NO="53"; //鉓片
			cell = row.getCell((short)9);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			PART_COLOR=FCMPS_PUBLIC.getValue(obj).trim();
			
			if(!PART_COLOR.equals("")) {
				String strSQL="delete from FCMPS030 " +
						      "where FA_NO='"+getFA_NO()+"' " +
						      "  and SH_NO='"+SH_NO+"' " +
						      "  and SH_COLOR='"+SH_COLOR+"' " +
						      "  and PART_NO='"+PART_NO+"' " +
						      "  and PART_COLOR='"+PART_COLOR+"'";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps030 (fa_no, style_no, sh_no,planner, sh_color, part_no, part_color, up_user, up_date) "+
                       " values ("+
                       "'"+getFA_NO()+"'"+
                       ",'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PLANNER+"'"+
                       ",'"+SH_COLOR+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PART_COLOR+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}
			
			PART_NO="54"; //領口鉓片
			cell = row.getCell((short)10);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			PART_COLOR=FCMPS_PUBLIC.getValue(obj).trim();
			
			if(!PART_COLOR.equals("")) {
				String strSQL="delete from FCMPS030 " +
						      "where FA_NO='"+getFA_NO()+"' " +
						      "  and SH_NO='"+SH_NO+"' " +
						      "  and SH_COLOR='"+SH_COLOR+"' " +
						      "  and PART_NO='"+PART_NO+"' " +
						      "  and PART_COLOR='"+PART_COLOR+"'";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps030 (fa_no, style_no, sh_no,planner, sh_color, part_no, part_color, up_user, up_date) "+
                       " values ("+
                       "'"+getFA_NO()+"'"+
                       ",'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PLANNER+"'"+
                       ",'"+SH_COLOR+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PART_COLOR+"'"+
                       ",'"+getUP_USER()+"'"+
                       ",SYSDATE)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			}
			
			PART_NO="05"; //後跟
			cell = row.getCell((short)11);			
			obj=FCMPS_PUBLIC.getCellValue(cell,2);
			PART_COLOR=FCMPS_PUBLIC.getValue(obj).trim();
			
			if(!PART_COLOR.equals("")) {
				String strSQL="delete from FCMPS030 " +
						      "where FA_NO='"+getFA_NO()+"' " +
						      "  and SH_NO='"+SH_NO+"' " +
						      "  and SH_COLOR='"+SH_COLOR+"' " +
						      "  and PART_NO='"+PART_NO+"' " +
						      "  and PART_COLOR='"+PART_COLOR+"'";
				
				PreparedStatement pstmtData = null;						
				
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();
			    
			    strSQL="insert into fcmps030 (fa_no, style_no, sh_no,planner, sh_color, part_no, part_color, up_user, up_date) "+
                       " values ("+
                       "'"+getFA_NO()+"'"+
                       ",'"+STYLE_CODE+"'"+
                       ",'"+SH_NO+"'"+
                       ",'"+PLANNER+"'"+
                       ",'"+SH_COLOR+"'"+
                       ",'"+PART_NO+"'"+
                       ",'"+PART_COLOR+"'"+
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
