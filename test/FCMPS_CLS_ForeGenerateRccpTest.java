package fcmps.test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.hibernate.cfg.Configuration;

import fcmps.domain.FCMPS010_BEAN;
import fcmps.ui.FCMPS_CLS_ForeGenerateRccpPlan_MultiThread20141125;
import fcmps.ui.FCMPS_CLS_ForeGenerateRccpPlan_Var;

public class FCMPS_CLS_ForeGenerateRccpTest extends TestCase {
	String config_xml="";
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
		
    	config_xml=path+"/FTI.cfg.xml";
    	
    	Connection conn=getConnection(config_xml);
		
		try{
			Import(conn);
	    }finally{	    	
			closeConnection(conn);
		}	
	        			
	}
	
	private void Import(Connection conn) {
    	FCMPS_CLS_ForeGenerateRccpPlan_Var cls_ForeGenerateRccpPlan_Var=FCMPS_CLS_ForeGenerateRccpPlan_Var.getInstance(FA_NO, conn);
    	cls_ForeGenerateRccpPlan_Var.init(FA_NO, conn);
    	
    	FCMPS_CLS_ForeGenerateRccpPlan_MultiThread20141125 cls_ForeGenerateRccpPlan1422=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread20141125();
		cls_ForeGenerateRccpPlan1422.setFA_NO(FA_NO);
		cls_ForeGenerateRccpPlan1422.setUP_USER("DEV17");
		cls_ForeGenerateRccpPlan1422.doGeneratePlan(
				config_xml,
				1514);
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
