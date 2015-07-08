package fcmps.test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.hibernate.cfg.Configuration;

import fcmps.domain.FCMPS010_BEAN;
import fcmps.ui.FCMPS_PUBLIC;

public class UPDATE_FICSC047TABLE_Test extends TestCase{
	public void test_fuck() {
		String path="";
		String config_xml="";
		
		String package_path[]=FCMPS_CLS_AutoGenerateRccpPlanTest.class.getPackage().toString().split(" ");
		package_path=package_path[1].split("\\.");
		for(int i=0;i<package_path.length;i++) {
			if(!path.equals(""))path=path+"/";
			path=path+package_path[i];
		}
		
		File file = new File("");	  
		path=file.getAbsolutePath()+"/src/"+path;
		
    	config_xml=path+"/FICDB03.cfg.xml";
    	    	    	
    	Connection conn_FICDB03=getConnection(config_xml);
    	
    	config_xml=path+"/FTI.cfg.xml";
    	
    	Connection conn_FTI=getConnection(config_xml);
    	
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;		
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;	
		
		PreparedStatement pstmtData3 = null;		
		
    	try {
    		
			strSQL="select distinct SH_NO from FICSC047";
			
		    pstmtData = conn_FICDB03.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		String SH_NO=rs.getString("SH_NO");
		    		
		    		strSQL="select * from ficsc047 where sh_no='"+SH_NO+"' order by ord";
				    pstmtData2 = conn_FICDB03.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    rs2=pstmtData2.executeQuery();
		    		if(rs2.next()) {
		    			int v_ord=0;
		    			do {
		    				v_ord++;
		    				String v_pr_type=FCMPS_PUBLIC.getValue(rs2.getString("pr_type"));
		    				String v_sh_color=FCMPS_PUBLIC.getValue(rs2.getString("sh_color"));
		    				String v_sh_col=FCMPS_PUBLIC.getValue(rs2.getString("sh_col"));
		    				String v_sh_col1=FCMPS_PUBLIC.getValue(rs2.getString("sh_col1"));
		    				String v_sh_col2=FCMPS_PUBLIC.getValue(rs2.getString("sh_col2"));
		    				
		    				strSQL="insert into ficsc047 "+
		    				       "(sh_no, pr_type, sh_color, sh_col, up_date, up_user, ord, sh_col1, sh_col2) "+
		    				       " values "+
		    				       " ('"+SH_NO+"',"+
		    				       "  '"+v_pr_type+"',"+ 
		    				       "  '"+v_sh_color+"',"+
		    				       "  '"+v_sh_col+"',"+
		    				       "  sysdate,'DEV17',"+
		    				       "  "+v_ord+","+ 
		    				       "  '"+v_sh_col1+"',"+
		    				       "  '"+v_sh_col2+"')";
						    pstmtData3 = conn_FTI.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
						    pstmtData3.execute();
						    pstmtData3.close();
						    
		    			}while(rs2.next());
		    		}
				    rs2.close();
				    pstmtData2.close();	
				    
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();	
    		
    	}catch(Exception ex) {
    		ex.printStackTrace();
    	}finally {
    		this.closeConnection(conn_FICDB03);
    		this.closeConnection(conn_FTI);
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
