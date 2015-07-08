package fcmps.test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

import junit.framework.TestCase;

import org.hibernate.cfg.Configuration;

import fcmps.domain.FCMPS010_BEAN;
import fcmps.ui.FCMPS_CLS_PR_Analyse;
import fcmps.ui.FCMPS_CLS_Ship_Analyse;

public class FCMPS_CLS_OrderReplaceTest extends TestCase {
	String config_xml="";
	String FA_NO="FIC";
    
    public void test_Analyse() {
		String path="";
		String package_path[]=FCMPS_CLS_AutoGenerateRccpPlanTest.class.getPackage().toString().split(" ");
		package_path=package_path[1].split("\\.");
		for(int i=0;i<package_path.length;i++) {
			if(!path.equals(""))path=path+"/";
			path=path+package_path[i];
		}
		
		File file = new File("");	  
		path=file.getAbsolutePath()+"/src/"+path;
		
    	config_xml=path+"/FTIDB04.cfg.xml";
    	
    	Connection conn=getConnection(config_xml);

    	try {
    		System.out.println(new Date());
    		doQuitStock(FA_NO,false,conn,config_xml);
    		System.out.println(new Date());
    	}finally {
        	closeConnection(conn);
    	}
    	

    }
	
	/**
	 * 抵扣庫存
	 * @param FA_NO
	 * @param conn
	 */
	private void doQuitStock(String FA_NO,boolean replace,Connection conn,String config_xml) {
	
		FCMPS_CLS_Ship_Analyse ship_Analyse=new FCMPS_CLS_Ship_Analyse();
		ship_Analyse.setFA_NO(FA_NO);
		ship_Analyse.setConnection(conn);
		ship_Analyse.setConfig_XML(config_xml);
		if(!ship_Analyse.doAnalyse()) {
			System.out.println("成品庫存抵扣執行失敗!");
			return;
		}
		
		FCMPS_CLS_PR_Analyse pr_Analyse=new FCMPS_CLS_PR_Analyse();
		pr_Analyse.setFA_NO(FA_NO);
		pr_Analyse.setConnection(conn);
		pr_Analyse.setConfig_XML(config_xml);
		
		if(!pr_Analyse.doAnalyse()) {
			System.out.println("工令抵扣執行失敗!");
			return;
		}  	
		
		System.out.println("執行成功!");
		
/*		if(!replace) return;
		
		FCMPS_CLS_ReplacePlannedOrder cls_Replace=new FCMPS_CLS_ReplacePlannedOrder(FA_NO,config_xml);
		if(!cls_Replace.doReplace()) {
			System.out.println("訂單替代執行失敗!");
			return;
		}else {
			System.out.println("執行成功!");
		}	*/	
		
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
	public void closeConnection(Connection conn) {
		try {
			if (conn != null && !conn.isClosed())
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
