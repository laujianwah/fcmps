package fcmps.test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import junit.framework.TestCase;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import fcmps.domain.FCMPS0101_BEAN;
import fcmps.domain.FCMPS010_BEAN;
import fcmps.ui.FCMPS_CLS_ImportProducePlan;
import fcmps.ui.FCMPS_CLS_ImportProducePlan.Import_Produce_Plan_Connection_Wrap;
import fcmps.ui.FCMPS_CLS_ImportProducePlan.Style_WorkBook_Info;

public class FCMPS_CLS_ImportPlanTest extends TestCase {
	String PLAN_File="F:/臨時文件/2015/20150310/KADEE -11215.xls";
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
		
		String config_xml=path+"/FTI.cfg.xml";
		
		Session session=null;
		SessionFactory sessionFactory=null;
		Connection conn=null;
		
		Stack<Import_Produce_Plan_Connection_Wrap> conn_stack=null;
		
		try {
			
			File fConfig=new File(config_xml);
			if(!fConfig.exists()) {
				System.out.println( "The Config file " + config_xml+" does not exist!" );
				return;
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
			
			conn_stack=new Stack<Import_Produce_Plan_Connection_Wrap>();
			FCMPS_CLS_ImportProducePlan.Import_Produce_Plan_Connection_Wrap conn_wrap=new FCMPS_CLS_ImportProducePlan.Import_Produce_Plan_Connection_Wrap();
			conn_wrap.setConnection(conn);
			conn_wrap.setSession(session);
			conn_wrap.setSessionFactory(sessionFactory);
			
			conn_stack.push(conn_wrap);
			
	    	Import(PLAN_File,conn_stack);
			
		}catch(Exception ex) {
			ex.printStackTrace();
			return;
		}finally {
			if(conn_stack!=null) {
				FCMPS_CLS_ImportProducePlan.Import_Produce_Plan_Connection_Wrap conn_wrap=conn_stack.pop();
				if(conn_wrap!=null) {
					session=conn_wrap.getSession();
					sessionFactory=conn_wrap.getSessionFactory();
					conn=conn_wrap.getConnection();
					
					closeConnection(conn);
					if(session!=null)session.close();
					if(sessionFactory!=null)sessionFactory.close();
					sessionFactory=null;
					
				}
			}
		}
	}
	
	/**
	 * 依據列的順序取得Excel中Col的Title
	 * @param iCol 列的序號,從0開始
	 * @return
	 */
	private String getExcelCellTitle(int iCol){
		String iRet="";
		if(iCol<0){
			return "";
		}
		String alpha[]=new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		if(iCol<26){
			iRet=alpha[iCol];
		}else{
			iRet=alpha[(iCol+1)/26-1];			
			if((iCol+1)%26==0){
				iRet=alpha[(iCol+1)/26-2];
				iRet=iRet+ alpha[alpha.length-1];;
			}else{
				iRet=iRet+alpha[(iCol+1)%26-1];
			}
			
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
	
	private void Import(String PLAN_File,Stack<Import_Produce_Plan_Connection_Wrap> conn_stack) {
		
		List<Style_WorkBook_Info> list_work_book_info=new ArrayList<Style_WorkBook_Info>();
		
		FCMPS_CLS_ImportProducePlan cls_Import=new FCMPS_CLS_ImportProducePlan();
		cls_Import.setFA_NO(FA_NO);
		cls_Import.setUP_USER("DEV");
		cls_Import.setLEAN_NO("無");
		cls_Import.setConnection_stack(conn_stack);
		cls_Import.Cover_Old_Data(false);
		
		cls_Import.doImport(PLAN_File,list_work_book_info);
		
		for(Style_WorkBook_Info info:list_work_book_info) {
			System.out.println(info.getFile_Name()+
					" Style:"+info.getSH_NO()+
					" Sheet:"+info.getSheet_Name()+
					" Inventorty:"+info.getInventory()+
					" total col:"+(info.getTotal_Columns()+1)+
					" Last col:"+getExcelCellTitle(info.getTotal_Columns())+
					" total row:"+(info.getTotal_Rows()+1)+
					" Order Qty:"+info.getOD_QTY());
			
			for(String msg:info.getMessage()) {
				System.out.println(msg);
			}
			
		}		
		
	}

}
