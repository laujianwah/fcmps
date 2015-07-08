package fcmps.ui;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
/**
 * 訂單替代<br>
 * 由使用者提供替代訂單號和被替代訂單號,系統自動從訂單系統抓取替代訂單的型體,配色,size訂單數量<br>
 * 替代排產系統中的被替代訂單的型體,配色,size數量<br>
 * @author dev17
 *
 */
public class FCMPS_CLS_OrderReplace {
	String FA_NO="";
	String UP_USER="";

    public String getFA_NO() {
		return FA_NO;
	}

	public void setFA_NO(String fa_no) {
		FA_NO = fa_no;
	}

	public String getUP_USER() {
		return UP_USER;
	}

	public void setUP_USER(String up_user) {
		UP_USER = up_user;
	}

	public void doImport(String file,Connection conn){
		
		try {
			
			File excel=new File(file);
			if(!excel.exists()) {
				System.out.println( "The file " + file+" does not exist!" );
				return;
			}
			
			FileInputStream fileIn = new FileInputStream(excel);
			if(fileIn==null) {
				System.out.println("無法讀取:"+file);
				return;
			}
			HSSFWorkbook wb=new HSSFWorkbook(fileIn);

			HSSFSheet sheet = wb.getSheetAt(0);		 			

			if(sheet==null) return;
			
			conn.setAutoCommit(false);
			
			readSheet(sheet, conn);
			
			conn.commit();
			
		    fileIn.close();
		    wb=null;   
			
		}catch(Exception ex){
			ex.printStackTrace();
			try {
				conn.rollback();
			}catch(Exception sqlex) {
				sqlex.printStackTrace();
			}
		}

    }
    
    private boolean readSheet(HSSFSheet sheet,Connection conn) throws Exception{
    	boolean iRet=false;
		
		int iRow=0;    		
		
		HSSFRow row = null;			
		HSSFCell cell = null;		
		
		row=sheet.getRow(iRow);	

		iRow++;
				
		while(iRow<=sheet.getLastRowNum()){
			row=sheet.getRow(iRow);
			if(row==null) break;
			
			cell = row.getCell((short)0);
			if(FCMPS_PUBLIC.getCellValue(cell)==null) break;			
			String OD_PONO1_REPLACE=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,2)).trim(); //替代訂單號
			
			if(OD_PONO1_REPLACE.equals("")) break; 
			
			cell = row.getCell((short)1);
			if(FCMPS_PUBLIC.getCellValue(cell)==null) break;			
			String OD_PONO1_ORIGINAL=FCMPS_PUBLIC.getValue(FCMPS_PUBLIC.getCellValue(cell,2)).trim(); //被替代訂單號
			
			if(OD_PONO1_ORIGINAL.equals("")) break;
			
			insertMPS025(OD_PONO1_REPLACE, OD_PONO1_ORIGINAL, conn);
			
			replaceOrder(OD_PONO1_REPLACE, OD_PONO1_ORIGINAL, conn);
			iRow++;
		}

		iRet=true;
    	return iRet;
    } 
    
    /**
     * 新增訂單替代記錄檔
     * @param OD_PONO1_REPLACE 替代訂單號
     * @param OD_PONO1_ORIGINAL 被替代訂單號
     * @param conn
     * @return
     * @throws Exception
     */
    private boolean insertMPS025(String OD_PONO1_REPLACE,String OD_PONO1_ORIGINAL,Connection conn) throws Exception {
    	boolean iRet=false;
    	
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
    	
		strSQL="select OD_PONO1 from FCMPS010 where OD_PONO1='"+OD_PONO1_REPLACE+"' and nvl(IS_REPLACEMENT,'N')='Y' ";
	    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	    rs=pstmtData.executeQuery();
	    
	    if(!rs.next()){
	    	throw new Exception(OD_PONO1_REPLACE+"--不是替代訂單!");
	    }
		rs.close();
		pstmtData.close();
		
		strSQL="select OD_PONO1 from FCMPS010 where OD_PONO1='"+OD_PONO1_REPLACE+"' and rownum=1";
	    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	    rs=pstmtData.executeQuery();
	    
	    if(!rs.next()){
	    	throw new Exception("替代訂單號:"+OD_PONO1_REPLACE+"--周計劃系統中沒有此訂單號!");
	    }
		rs.close();
		pstmtData.close();
		
		strSQL="select OD_PONO1 from FCMPS010 where OD_PONO1='"+OD_PONO1_ORIGINAL+"' and rownum=1";
	    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	    rs=pstmtData.executeQuery();
	    
	    if(!rs.next()){
	    	throw new Exception("被替代訂單號:"+OD_PONO1_ORIGINAL+"--周計劃系統中沒有此訂單號!");
	    }
		rs.close();
		pstmtData.close();
				
		strSQL="select OD_PONO1 from FCMPS010 " +
			   "where OD_PONO1='"+OD_PONO1_REPLACE+"' " +
			   "  and exists(select * from FCMPS010 A " +
			   "              where OD_PONO1='"+OD_PONO1_ORIGINAL+"' " +
			   "                and SH_NO=FCMPS010.SH_NO " +
			   "                and SH_COLOR=FCMPS010.SH_COLOR" +
			   "                and SH_SIZE=FCMPS010.SH_SIZE" +
			   " )";
	    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	    rs=pstmtData.executeQuery();
	    
	    if(!rs.next()){
	    	throw new Exception("替代訂單號:"+OD_PONO1_REPLACE+" 不包含被替代訂單:"+OD_PONO1_ORIGINAL+"中的型體配色SIZE!");
	    }
		rs.close();
		pstmtData.close();
				
		strSQL="select OD_PONO1_REPLACE from FCMPS025 " +
			   "where OD_PONO1_ORIGINAL='"+OD_PONO1_ORIGINAL+"' and OD_PONO1_REPLACE='"+OD_PONO1_REPLACE+"'";
	    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	    rs=pstmtData.executeQuery();
	    
	    if(rs.next()){
	    	throw new Exception("替代訂單號:"+OD_PONO1_REPLACE+" 已替代:"+OD_PONO1_ORIGINAL+" 這張訂單,不可重復替代!");
	    }
		rs.close();
		pstmtData.close();
		
		strSQL="select OD_PONO1_REPLACE from FCMPS025 " +
		       "where OD_PONO1_ORIGINAL='"+OD_PONO1_REPLACE+"' and OD_PONO1_REPLACE='"+OD_PONO1_ORIGINAL+"'";
		pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		rs=pstmtData.executeQuery();
		 
		if(rs.next()){
		   throw new Exception("訂單號:"+OD_PONO1_ORIGINAL+" 已替代:"+OD_PONO1_REPLACE+" 這張訂單,不可回復替代!");
		}
		rs.close();
		pstmtData.close();
	
		strSQL="insert into fcmps025 (fa_no, od_pono1_original, od_pono1_replace, is_replaced, up_user, up_date) "+
               " values ( "+
               "'"+getFA_NO()+"',"+
               "'"+OD_PONO1_ORIGINAL+"',"+
               "'"+OD_PONO1_REPLACE+"',"+
               "'N'," +
               "'"+ getUP_USER()+"'," +
               "sysdate)";
		pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		pstmtData.execute();
		pstmtData.close();
		
		iRet=true;
		
    	return iRet;
    }
    
    /**
     * 訂單替代
     * @param OD_PONO1_REPLACE  替代訂單號
     * @param OD_PONO1_ORIGINAL 被替代訂單號
     * @param conn
     * @return
     * @throws Exception
     */
    private boolean replaceOrder(String OD_PONO1_REPLACE,String OD_PONO1_ORIGINAL,Connection conn) throws Exception {
    	boolean iRet=false;
    	
    	String strSQL="select distinct SH_NO,SH_COLOR,SH_SIZE,PROCID from FCMPS010 where OD_PONO1='"+OD_PONO1_ORIGINAL+"'";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;

		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
	    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	    rs=pstmtData.executeQuery();
	    
	    if(rs.next()){

	    	do {

	    		String SH_NO=rs.getString("SH_NO");
	    		String SH_COLOR=rs.getString("SH_COLOR");
	    		String SH_SIZE=rs.getString("SH_SIZE");
	    		String PROCID=rs.getString("PROCID");
	    		
    			double SIZE_OD_QTY_ORIGINAL=0;
    			
	    		strSQL="select OD_QTY from FCMPS010 " +
	 			       "where OD_PONO1='"+OD_PONO1_ORIGINAL+"'"+
	 			       "  and SH_NO='"+SH_NO+"' " +
	 			       "  and SH_COLOR='"+SH_COLOR+"' " +
	 			       "  and SH_SIZE='"+SH_SIZE+"' " +
	 			       "  and PROCID='"+PROCID+"'";
			    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    rs2=pstmtData2.executeQuery();
			    
			    if(rs2.next()){
			    	SIZE_OD_QTY_ORIGINAL=FCMPS_PUBLIC.getDouble(rs2.getDouble("OD_QTY"));
			    }
		 		rs2.close();
		 		pstmtData2.close();
		 		
    			double SIZE_OD_QTY_REPLACE=0;
    			
	    		strSQL="select OD_QTY-nvl(REPLACED_QTY,0) OD_QTY from FCMPS010 " +
 			           "where OD_PONO1='"+OD_PONO1_REPLACE+"'"+
 			           "  and SH_NO='"+SH_NO+"' " +
 			           "  and SH_COLOR='"+SH_COLOR+"' " +
 			           "  and SH_SIZE='"+SH_SIZE+"' " +
 			           "  and PROCID='"+PROCID+"'";
			    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    rs2=pstmtData2.executeQuery();
			    
			    if(rs2.next()){
			    	SIZE_OD_QTY_REPLACE=FCMPS_PUBLIC.getDouble(rs2.getDouble("OD_QTY"));
			    }
		 		rs2.close();
		 		pstmtData2.close();
		 				 		
		 		if(SIZE_OD_QTY_REPLACE==0) continue;
		 		
		 		//替代訂單size訂單數量大於等被替代訂單數量
		 		//屬於 A==>B, A==>B+C
		 		if(SIZE_OD_QTY_REPLACE>=SIZE_OD_QTY_ORIGINAL) {
		 			
		 			//將被替代訂單的排產數移到替代訂單上
			 		strSQL="update FCMPS010 set WORK_PLAN_QTY=WORK_PLAN_QTY+" +
		 			       "nvl((select WORK_PLAN_QTY from FCMPS010 a where OD_PONO1='"+OD_PONO1_ORIGINAL+"'" +
		 			       "     and SH_NO='"+SH_NO+"' " +
	 			           "     and SH_COLOR='"+SH_COLOR+"' " +
	 			           "     and SH_SIZE='"+SH_SIZE+"' " +
	 			           "     and PROCID='"+PROCID+"'),0) " +
	 			           "where OD_PONO1='"+OD_PONO1_REPLACE+"'"+
	 			           "  and SH_NO='"+SH_NO+"' " +
	 			           "  and SH_COLOR='"+SH_COLOR+"' " +
	 			           "  and SH_SIZE='"+SH_SIZE+"' " +
	 			           "  and PROCID='"+PROCID+"'";
			 		
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.execute();
				    pstmtData2.close();		
				    
				    //將被替代訂單設為禁止排產
			 		strSQL="update FCMPS010 set IS_DISABLE='Y' " +
			               "where OD_PONO1='"+OD_PONO1_ORIGINAL+"'"+
			               "  and SH_NO='"+SH_NO+"' " +
			               "  and SH_COLOR='"+SH_COLOR+"' " +
			               "  and SH_SIZE='"+SH_SIZE+"' " +
			               "  and PROCID='"+PROCID+"'";
			 		
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.execute();
				    pstmtData2.close();	
				    
				    //更新替代訂單的累計替代數量
			 		strSQL="update FCMPS010 set REPLACED_QTY=nvl(REPLACED_QTY,0) + " +SIZE_OD_QTY_ORIGINAL+" "+
		                   "where OD_PONO1='"+OD_PONO1_REPLACE+"'"+
		                   "  and SH_NO='"+SH_NO+"' " +
		                   "  and SH_COLOR='"+SH_COLOR+"' " +
		                   "  and SH_SIZE='"+SH_SIZE+"' " +
		                   "  and PROCID='"+PROCID+"'";
		 		
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.execute();
				    pstmtData2.close();	
				    
				    //當替代訂單的累計替代數與訂單數一致時, 將IS_DISABLE='N',讓其可以繼續排產
			 		strSQL="update FCMPS010 set IS_DISABLE='N' "+
	                       "where OD_PONO1='"+OD_PONO1_REPLACE+"'"+
	                      "  and SH_NO='"+SH_NO+"' " +
	                      "  and SH_COLOR='"+SH_COLOR+"' " +
	                      "  and SH_SIZE='"+SH_SIZE+"' " +
	                      "  and PROCID='"+PROCID+"'"+
	                      "  and OD_QTY=REPLACED_QTY";
	 		
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.execute();
				    pstmtData2.close();	
				    
				    
		 		}
		 		
		 		//替代訂單size訂單數量小於被替代訂單數量
		 		//屬於B+C==>A
		 		//先將被替代訂單的排產數移到替代訂單上.但最多只能移訂單數量
		 		//更新替代訂單的is_disable='N',REPLACED_QTY等於訂單數量
		 		//更新被替代訂單的OD_QTY,和WORK_PLAN_QTY,扣除替代訂單的訂單數量和移到替代訂單上的排產數
		 		if(SIZE_OD_QTY_REPLACE<SIZE_OD_QTY_ORIGINAL) {
		 			
		 			int WORK_PLAN_QTY_REPLACE=0;
		 			
		 			strSQL="select OD_QTY-WORK_PLAN_QTY WORK_PLAN_QTY from FCMPS010 "+
			               "where OD_PONO1='"+OD_PONO1_REPLACE+"'"+
 			               "  and SH_NO='"+SH_NO+"' " +
 			               "  and SH_COLOR='"+SH_COLOR+"' " +
 			               "  and SH_SIZE='"+SH_SIZE+"' " +
 			               "  and PROCID='"+PROCID+"'";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    if(rs2.next()) {
				    	WORK_PLAN_QTY_REPLACE=rs2.getInt("WORK_PLAN_QTY");
				    }
				    rs2.close();
				    pstmtData2.close();
				    
		 			strSQL="select WORK_PLAN_QTY from FCMPS010 "+
		                   "where OD_PONO1='"+OD_PONO1_ORIGINAL+"'"+
		                   "  and SH_NO='"+SH_NO+"' " +
		                   "  and SH_COLOR='"+SH_COLOR+"' " +
		                   "  and SH_SIZE='"+SH_SIZE+"' " +
		                   "  and PROCID='"+PROCID+"'";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
				    if(rs2.next()) {
				    	if(rs2.getInt("WORK_PLAN_QTY")<WORK_PLAN_QTY_REPLACE) WORK_PLAN_QTY_REPLACE=rs2.getInt("WORK_PLAN_QTY");
				    }
				    rs2.close();
				    pstmtData2.close();
			    
				    
		 			//將被替代訂單的排產數移到替代訂單上
			 		strSQL="update FCMPS010 set WORK_PLAN_QTY=WORK_PLAN_QTY+" +
		 			       "nvl((select WORK_PLAN_QTY from FCMPS010 a where OD_PONO1='"+OD_PONO1_ORIGINAL+"'" +
		 			       "     and SH_NO='"+SH_NO+"' " +
	 			           "     and SH_COLOR='"+SH_COLOR+"' " +
	 			           "     and SH_SIZE='"+SH_SIZE+"' " +
	 			           "     and PROCID='"+PROCID+"'),0) " +
	 			           "where OD_PONO1='"+OD_PONO1_REPLACE+"'"+
	 			           "  and SH_NO='"+SH_NO+"' " +
	 			           "  and SH_COLOR='"+SH_COLOR+"' " +
	 			           "  and SH_SIZE='"+SH_SIZE+"' " +
	 			           "  and PROCID='"+PROCID+"'";
			 		
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.execute();
				    pstmtData2.close();	
				    
		 			//被替代訂單的排產數移到替代訂單上.但最多只能移訂單數量
			 		strSQL="update FCMPS010 set WORK_PLAN_QTY=OD_QTY "+
	 			           "where OD_PONO1='"+OD_PONO1_REPLACE+"'"+
	 			           "  and SH_NO='"+SH_NO+"' " +
	 			           "  and SH_COLOR='"+SH_COLOR+"' " +
	 			           "  and SH_SIZE='"+SH_SIZE+"' " +
	 			           "  and PROCID='"+PROCID+"'"+
	 			           "  and WORK_PLAN_QTY>OD_QTY";
	 			           			 		
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.execute();
				    pstmtData2.close();
				    
				    
				    //將被替代訂單的訂單數,排產數扣除替代訂單的訂單數和排產數
			 		strSQL="update FCMPS010 set WORK_PLAN_QTY=WORK_PLAN_QTY+(select OD_QTY from FCMPS010 a where OD_PONO1='"+OD_PONO1_REPLACE+"'" +
		 			       "     and SH_NO='"+SH_NO+"' " +
	 			           "     and SH_COLOR='"+SH_COLOR+"' " +
	 			           "     and SH_SIZE='"+SH_SIZE+"' " +
	 			           "     and PROCID='"+PROCID+"') " +
			               "where OD_PONO1='"+OD_PONO1_ORIGINAL+"'"+
			               "  and SH_NO='"+SH_NO+"' " +
			               "  and SH_COLOR='"+SH_COLOR+"' " +
			               "  and SH_SIZE='"+SH_SIZE+"' " +
			               "  and PROCID='"+PROCID+"'";
			 		
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.execute();
				    pstmtData2.close();	
				    				    
				    //更新替代訂單的累計替代數量
			 		strSQL="update FCMPS010 set REPLACED_QTY=nvl(REPLACED_QTY,0) + " +SIZE_OD_QTY_REPLACE+" "+
		                   "where OD_PONO1='"+OD_PONO1_REPLACE+"'"+
		                   "  and SH_NO='"+SH_NO+"' " +
		                   "  and SH_COLOR='"+SH_COLOR+"' " +
		                   "  and SH_SIZE='"+SH_SIZE+"' " +
		                   "  and PROCID='"+PROCID+"'";
		 		
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.execute();
				    pstmtData2.close();	
				    
				    //當替代訂單的累計替代數與訂單數一致時, 將IS_DISABLE='N',讓其可以繼續排產
			 		strSQL="update FCMPS010 set IS_DISABLE='N' "+
	                       "where OD_PONO1='"+OD_PONO1_REPLACE+"'"+
	                      "  and SH_NO='"+SH_NO+"' " +
	                      "  and SH_COLOR='"+SH_COLOR+"' " +
	                      "  and SH_SIZE='"+SH_SIZE+"' " +
	                      "  and PROCID='"+PROCID+"'"+
	                      "  and OD_QTY=REPLACED_QTY";
	 		
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.execute();
				    pstmtData2.close();	
				    
		 		}

	    	}while(rs.next());
	    }
		rs.close();
		pstmtData.close();
		
    	return iRet;
    }

}
