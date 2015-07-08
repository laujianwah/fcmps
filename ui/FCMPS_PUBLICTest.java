package fcmps.ui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;

public class FCMPS_PUBLICTest extends TestCase {

	public void test_SH() {
		Connection conn=getConnection();
		try {
			this.Achieve_Other_PROC_Planned_QTY("FIC", "LEIGHGRAPHICWDG", "100", 1507, 32);

			
		}finally {
			closeConnection(conn);
		}

		
	}

    /**
     * 檢查目前制程排入的數量是否已達到前關或是同關制程的產能<br>
     * 如果前關的產能小於目前的制程,則一定要檢查, <br>
     * 如果前關的產能大於等於目前的制程,則不需要檢查,因為目前制程怎麼樣排都不會超出其產能.<br> 
     * @param FA_NO
     * @param SH_NO
     * @param PROCID
     * @param WORK_WEEK
     * @param WORK_PLAN_QTY
     * @param ls_PROC_WORK_QTY
     * @param ls_SH_WORK_QTY
     * @return
     */
	private boolean Achieve_Other_PROC_Planned_QTY(
			String FA_NO,
			String SH_NO,
			String PROCID,
			int WORK_WEEK,
			double WORK_PLAN_QTY) {
		
		boolean iRet=false;
		
		double cur_WORK_CAP_QTY=250000;

		if(cur_WORK_CAP_QTY==0) return iRet;
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;

		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;

		PreparedStatement pstmtData3 = null;		
		ResultSet rs3=null;
		
		Connection conn =getConnection();
		
		try{

			int Work_Week_Start=WORK_WEEK;
			
			String strSQL="select distinct PROC_SEQ  "+
                   "  from fcps22_1 "+
                   " where sh_aritcle = '"+SH_NO+"' "+
                   "   and proc_seq < (select proc_seq "+
                   "                     from fcps22_1 "+
                   "                    where sh_aritcle = '"+SH_NO+"' "+
                   "                      and pb_ptno = '"+PROCID+"') "+
                   "   and proc_seq >= (select min(proc_seq) "+
                   "                      from fcps22_1 "+
                   "                     where sh_aritcle = '"+SH_NO+"') ";
			
			strSQL="select count(*) iCount from ("+strSQL+")";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {
		    	Work_Week_Start=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK, rs.getInt("iCount"));
		    }
			rs.close();
			pstmtData.close();
										
			strSQL="select distinct FCPS22_1.PROC_SEQ " +
		           "from FCPS22_1 " +
                   "where FCPS22_1.SH_ARITCLE='"+SH_NO+"' " +
                   "ORDER BY PROC_SEQ ";
			pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs=pstmtData.executeQuery();

			if(rs.next()){
				int iWeek=0;
				iFirst:do {
					
					int iwk=0;
					do {
			    		double workdays=FCMPS_PUBLIC.getSys_WorkDaysOfWeek(FA_NO,Work_Week_Start,conn);
			    		if(workdays==0) {
			    			Work_Week_Start=FCMPS_PUBLIC.getNext_Week(Work_Week_Start, 1);
			    		}else {
			    			break;
			    		}
			    		if(iwk==4)break;
			    		iwk++;
					}while(true);
					
					iWeek++;
					System.out.println(Work_Week_Start);
					double PROC_SEQ=rs.getDouble("PROC_SEQ");
					
					strSQL="select FCPS22_1.PB_PTNO," +
				           "       FCPS22_1.PROC_SEQ," +
				           "       FCPS22_2.PB_PTNA," +
				           "       nvl(FCPS22_1.NEED_PLAN,'N') NEED_PLAN " +
				           "from FCPS22_1,FCPS22_2 " +
		                   "where FCPS22_1.PB_PTNO=FCPS22_2.PB_PTNO" +
		                   "  and FCPS22_1.SH_ARITCLE='"+SH_NO+"' " +
		                   "  and FCPS22_1.PROC_SEQ="+PROC_SEQ;
					pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
					rs2=pstmtData2.executeQuery();

					if(rs2.next()){
						do {
							if(rs2.getString("NEED_PLAN").equals("N")) continue;
							if(rs2.getString("PB_PTNO").equals(PROCID)) continue;
							
							String PB_PTNO=rs2.getString("PB_PTNO");
							String PB_PTNA=rs2.getString("PB_PTNA");
															
							double WORK_CAP_QTY=0;
							
							//取前關制程的產能數
							strSQL="select WORK_CAP_QTY from FCMPS008 " +
						           "where FA_NO='"+FA_NO+"' " +
						           "  and PROCID='"+PB_PTNO+"' " +
						           "  and WORK_WEEK="+Work_Week_Start;
							strSQL=strSQL+" UNION ALL ";
							strSQL=strSQL+"select WORK_CAP_QTY from FCMPS009 " +
							              "where FA_NO='"+FA_NO+"' and PROCID='"+PB_PTNO+"'";

						    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
						    rs3=pstmtData3.executeQuery();
						    
						    if(rs3.next()){
					    		WORK_CAP_QTY=rs3.getDouble("WORK_CAP_QTY");   		
						    }
							rs3.close();
							pstmtData3.close();								

							if(WORK_CAP_QTY==0) {
						    	iRet=true;
							    rs2.close();
							    pstmtData2.close();
				    			break iFirst;
							}
							
				    		if(cur_WORK_CAP_QTY<=WORK_CAP_QTY) {//前關制程的產能大於等於目前制程的產能,不需要檢查
				    			continue;
				    		}
				    		
				    		//減去當前要排的數量
				    		WORK_CAP_QTY=WORK_CAP_QTY-WORK_PLAN_QTY;
				    		
				    		strSQL="SELECT sum(WORK_PLAN_QTY)  WORK_PLAN_QTY FROM FCMPS007 " +
				    			   "WHERE PLAN_NO = '1507SC' " +
				    			   "  and SH_NO IN (SELECT sh_aritcle FROM (" +
				    			   "     select distinct sh_aritcle, proc_Seq from fcps22_1" +
				    			   "     where SH_ARITCLE in (SELECT SH_ARITCLE FROM FCPS22_1 WHERE PB_PTNO = '"+PB_PTNO+"')) "+
                                   "     group by sh_aritcle HAVING COUNT(PROC_SEQ) = "+iWeek+") ";
				    		
						    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
						    rs3=pstmtData3.executeQuery();
						    
						    if(rs3.next()){
						    	if(rs3.getObject("WORK_PLAN_QTY")!=null) WORK_CAP_QTY=WORK_CAP_QTY-rs3.getDouble("WORK_PLAN_QTY");   		
						    }
							rs3.close();
							pstmtData3.close();	
												    		
				    		if(WORK_CAP_QTY<0) { //扣除本次需要排入的數量和已排的數量,如果小於0,則表示排不下.
				    			iRet=true;
							    rs2.close();
							    pstmtData2.close();
				    			break iFirst;
				    		}
				    							    		
						}while(rs2.next());
					}
				    rs2.close();
				    pstmtData2.close();

				    Work_Week_Start=FCMPS_PUBLIC.getNext_Week(Work_Week_Start, 1);

				}while(rs.next());
				
			}
			rs.close();
			pstmtData.close();			
					
		}catch(Exception sqlex){
			sqlex.printStackTrace();
		}finally{	    	
//		Application.getApp().closeConnection(conn);
		}
		
		return iRet;		
	}
	
	/**
	 * 取得FTI廠別正式數據庫連線
	 * @param COMMPANY_ID
	 * @return
	 */
	private Connection getConnection(){		
		Connection iRet=null;
		try{				

    		Class.forName("oracle.jdbc.driver.OracleDriver"); //加載驅動程序
    		String URL="jdbc:oracle:thin:@10.2.1.13:1521:ftdb06";
    		String USER="ftidsod";
    		String PSW="ftidsod";
    		iRet=DriverManager.getConnection(URL,USER,PSW);
			
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
	
}
