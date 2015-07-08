package fcmps.ui;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import fcmps.domain.FCMPS0101_BEAN;
import fcmps.domain.FCMPS010_BEAN;

public class FCMPS_CLS_ImportPR {
	private String config_xml="";
	private SessionFactory sessionFactory=null;
    private Session session=null;
    
	private String UP_USER="DEV";	
	private String FA_NO="";
	private String LEAN_NO="";
	
	private Connection conn;	
	
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

	private Connection getConnection() {
		return conn;
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
			
			String USER=config.getProperty("connection.username");
			String URL=config.getProperty("connection.url");
			String PSW=config.getProperty("connection.password");
			String DRIVER=config.getProperty("connection.driver_class");
			
    		Class.forName(DRIVER); //加載驅動程序
    		conn=DriverManager.getConnection(URL,USER,PSW);
			
    		session=sessionFactory.openSession(conn);

    		session.setFlushMode(FlushMode.COMMIT);
			
		}catch(Exception ex) {
			ex.printStackTrace();
			iRet=false;			
		}
		return iRet;
	}
	
	private SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}	
	
	private void CloseSessionFactory() {	
		closeConnection(getConnection());
		session.close();
		sessionFactory.close();
		sessionFactory=null;
	}
	
	private String getConfig_XML() {
		return config_xml;
	}

	public void setConfig_xml(String config_xml) {
		this.config_xml = config_xml;
	}

	public boolean doImport(String fileName) throws Exception {
		boolean iRet=false;
		File file=new File(fileName);
		if(!file.exists()) {
			System.out.println("文件:"+fileName+" 不存在!");
			throw new Exception("文件:"+fileName+" 不存在!");
		}
		iRet=doImport(file);
		return iRet;
	}
	
	public boolean doImport(File file) throws Exception {
		boolean iRet=false;
		HSSFWorkbook wb=null;
		FileInputStream fileIn=null;
		try {
			
			if(!GenericSessionFactory()) {
				throw new Exception("無法連接資料庫!");
			}
			
			fileIn = new FileInputStream(file);
			if(fileIn==null) {
				System.out.println("無法讀取:"+file.getName());
				throw new Exception("無法讀取:"+file.getName());
			}
			
			wb=new HSSFWorkbook(fileIn);

			for(int i=0;i<wb.getNumberOfSheets();i++) {
				HSSFSheet sheet = wb.getSheetAt(i);		 			

				if(sheet!=null) {
					if(!readSheet(sheet)){
						return iRet;
					}
				}
			}

			session.flush();
			
			iRet=true;
		}finally {
			CloseSessionFactory();
			if(wb!=null)wb=null;
			if(fileIn!=null) fileIn.close();
		}
		
		return iRet;
	}
	
    private boolean readSheet(HSSFSheet sheet)throws Exception{
    	boolean iRet=false;
		
		int iRow=1;    		
		
		HSSFRow row = null;			
		HSSFCell cell = null;			
				
		HSSFRow SIZE_TITLE_row=null;
		
		while(iRow<sheet.getLastRowNum()){
			iRow++;
			row=sheet.getRow(iRow);		
						
			if(row==null) continue;
			
			cell = row.getCell((short)0);
			String PR_NO=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,2));

			if(PR_NO.equals("工令號")) {
				System.out.println(iRow);
				SIZE_TITLE_row=sheet.getRow(iRow);
				
				HSSFRow PR_row=sheet.getRow(iRow+1);	
				cell = PR_row.getCell((short)0);
				PR_NO=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,2));
				
				cell = PR_row.getCell((short)1);
				int WORK_WEEK=FCMPS_PUBLIC.getInt(FCMPS_PUBLIC.getCellValue(cell,1));
				
				cell = PR_row.getCell((short)2);
				String SH_NO=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,1));
				
				cell = PR_row.getCell((short)3);
				String SH_COLOR=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,1));								
				
				HSSFRow SIZE_QTY_row=sheet.getRow(iRow+3);
				
				int iCol=6;
				
				do {					
					cell = SIZE_QTY_row.getCell((short)iCol);
					if(cell==null) break;
					int SIZE_QTY=FCMPS_PUBLIC.getInt(FCMPS_PUBLIC.getCellValue(cell,1));
					
					cell = SIZE_TITLE_row.getCell((short)iCol);
					if(cell==null) {
						throw new Exception("第"+(iRow+1)+"行,第"+(iCol+1)+"列沒有SIZE名稱!");
					}
					String SH_SIZE=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,1));	
					
					if(SH_SIZE.equals("合計")) break;
					
					if(SIZE_QTY>0) doInsertFCMPS014(FA_NO, PR_NO, SH_NO, SH_COLOR, SH_SIZE, LEAN_NO, WORK_WEEK, SIZE_QTY, getConnection());					
					
					iCol++;
				}while(true);
				
				iRow=iRow+3;
				
			}else {
				
				HSSFRow PR_row=sheet.getRow(iRow);	
				cell = PR_row.getCell((short)0);
				if(cell==null) continue;
				
				PR_NO=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,2));
				if(PR_NO.equals("")) continue;
				
				cell = PR_row.getCell((short)1);
				int WORK_WEEK=FCMPS_PUBLIC.getInt(FCMPS_PUBLIC.getCellValue(cell,1));
				
				cell = PR_row.getCell((short)2);
				String SH_NO=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,1));
				
				cell = PR_row.getCell((short)3);
				String SH_COLOR=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,1));	
				
				HSSFRow SIZE_QTY_row=sheet.getRow(iRow+2);
				
				int iCol=6;
				
				do {					
					cell = SIZE_QTY_row.getCell((short)iCol);
					if(cell==null) break;
					int SIZE_QTY=FCMPS_PUBLIC.getInt(FCMPS_PUBLIC.getCellValue(cell,1));
					
					cell = SIZE_TITLE_row.getCell((short)iCol);
					if(cell==null) {
						throw new Exception("第"+(iRow+1)+"行,第"+(iCol+1)+"列沒有SIZE名稱!");
					}
					String SH_SIZE=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,1));	
					
					if(SH_SIZE.equals("合計")) break;
					
					if(SIZE_QTY>0)doInsertFCMPS014(FA_NO, PR_NO, SH_NO, SH_COLOR, SH_SIZE, LEAN_NO, WORK_WEEK, SIZE_QTY, getConnection());					
					
					iCol++;
				}while(true);
				
			}
			
		}
		
		iRet=true;
    	return iRet;
    }  


	/**
	 * 記錄工令數
	 * 
	 * @param FA_NO
	 * @param PR_NO
	 * @param SH_NO
	 * @param SH_COLOR
	 * @param SH_SIZE
	 * @param LEAN_NO
	 * @param WORK_WEEK
	 * @param MT_QTY
	 * @param conn
	 * @return
	 */
	private boolean doInsertFCMPS014(
			String FA_NO,
			String PR_NO,
			String SH_NO,
			String SH_COLOR,
			String SH_SIZE,
			String LEAN_NO,
			int WORK_WEEK,
			double MT_QTY,
			Connection conn) throws Exception{
        boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;	
		
		strSQL="select * from FCMPS014 " +
			   "where FA_NO='"+FA_NO+"' " +
			   "  and PR_NO='"+PR_NO+"' " +
			   "  and SH_ARITCLE='"+SH_NO+"' " +
			   "  and SH_COLOR='"+SH_COLOR+"'" +
			   "  and SH_SIZE='"+SH_SIZE+"'";
	    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	    rs=pstmtData.executeQuery();
	    
	    if(rs.next()){
			strSQL="update FCMPS014 set SIZE_QTY="+MT_QTY+",SIZE_ORG_QTY="+MT_QTY+" "+
			       " where FA_NO='"+FA_NO+"' " +
			       "   and PR_NO='"+PR_NO+"' " +
			       "   and SH_ARITCLE='"+SH_NO+"' " +
			       "   and SH_COLOR='"+SH_COLOR+"'" +
			       "   and SH_SIZE='"+SH_SIZE+"'";
		    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData2.execute();
			pstmtData2.close();
			
			System.out.println("工令:"+PR_NO+" 碼數:"+SH_SIZE+" 已存在");

	    }else {
			strSQL="insert into fcmps014 "+
                   "(pr_no, fa_no, sh_aritcle, sh_color, sh_size, work_week, size_qty, up_date, lean_no, size_org_qty) "+
                   "values ("+
                   "'"+PR_NO+"'"+
                   ",'"+FA_NO+"'"+
                   ",'"+SH_NO+"'"+
                   ",'"+SH_COLOR+"'"+
                   ",'"+SH_SIZE+"'"+
                   ","+WORK_WEEK+
                   ","+MT_QTY+
                   ",sysdate"+	           
                   ",'"+LEAN_NO+"'"+
                   ","+MT_QTY+")";
		    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData2.execute();
			pstmtData2.close();
	    }
		rs.close();
		pstmtData.close();

		iRet=true;
			
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
