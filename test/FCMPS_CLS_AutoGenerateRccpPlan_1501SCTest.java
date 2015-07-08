package fcmps.test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.hibernate.cfg.Configuration;

import fcmps.domain.FCMPS010_BEAN;

public class FCMPS_CLS_AutoGenerateRccpPlan_1501SCTest extends TestCase {
	String FOS_File="";
	String config_xml="";
	String FA_NO="FIC";
	int maxColorCount=5;  //型體每周的配色個數
	int FORE_PLAN_WEEKS=12; //可提前周數
	int SHOOT_MIN_PRODUCE_QTY=1500; //射出最小排產量

	public void test_1422() {
		String path="";
		String package_path[]=FCMPS_CLS_AutoGenerateRccpPlanTest.class.getPackage().toString().split(" ");
		package_path=package_path[1].split("\\.");
		for(int i=0;i<package_path.length;i++) {
			if(!path.equals(""))path=path+"/";
			path=path+package_path[i];
		}
		
		File file = new File("");	  
		path=file.getAbsolutePath()+"/src/"+path;
		
    	config_xml=path+"/FTI.cfg.xml";
    	
    	Connection conn=getConnection(config_xml);
    	
//    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish("1422ZD", "N", conn);
//    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish("1424ZC", "N", conn);
//    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish("1421SC", "N", conn);
    	try {
    		FCMPS_CLS_AutoGenerateRccpPlan_1501SC rccp1452=new FCMPS_CLS_AutoGenerateRccpPlan_1501SC();    	
        	rccp1452.do_Rccp(FA_NO, config_xml, FORE_PLAN_WEEKS, conn,SHOOT_MIN_PRODUCE_QTY);  
    	}finally {
        	closeConnection(conn);
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
