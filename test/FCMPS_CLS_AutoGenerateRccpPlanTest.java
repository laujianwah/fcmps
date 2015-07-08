package fcmps.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.cfg.Configuration;

import fcmps.domain.FCMPS010_BEAN;
import fcmps.ui.CLS_RCCP_ERROR;
import fcmps.ui.FCMPS_CLS_ForeGenerateRccpPlan_MultiThread;
import fcmps.ui.FCMPS_CLS_ForeGenerateRccpPlan_MultiThread20141125;
import fcmps.ui.FCMPS_CLS_ForeGenerateRccpPlan_Var;
import fcmps.ui.FCMPS_CLS_ImportOrderFromFOS_Ex;
import fcmps.ui.FCMPS_CLS_ImportOrderFromFOS_Var;
import fcmps.ui.FCMPS_CLS_PR_Analyse;
import fcmps.ui.FCMPS_CLS_ReplacePlannedOrder;
import fcmps.ui.FCMPS_CLS_Ship_Analyse;
import fcmps.ui.FCMPS_PUBLIC;
import fcmps.ui.JExportToExcel;
import fcmps.ui.WeekUtil;

public class FCMPS_CLS_AutoGenerateRccpPlanTest extends TestCase {
	String FOS_File="";
	String config_xml="";
	String FA_NO="FIC";
	int maxColorCount=5;  //型體每周的配色個數
	int FORE_PLAN_WEEKS=4; //可提前周數
	int SHOOT_MIN_PRODUCE_QTY=516; //射出最小排產量
	private static String Main_Path="F:/FTI周計劃/測試分析";
	private static String TEST_CODE="0001"; //測試編號

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
		
    	config_xml=path+"/FTDB06.cfg.xml";
    	
    	Connection conn=getConnection(config_xml);
    	
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;
		
		try{

			strSQL="select " +
					"test_code, " +
					"shoot_min_qty, " +
					"shoot_week_cap, " +
				    "pre_weeks, " +
				    "sh_week_colors, " +
				    "t_weeks_qty, " +
				    "f_weeks_qty " +
				    "from fcmps_test where test_code='0031' order by test_code ";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		
		    		strSQL="update fcmps_test set START_TEST=sysdate where test_code='"+rs.getString("test_code")+"'";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.executeUpdate();
				    pstmtData2.close();
		    		
		    		Date stDate=new Date();		    		
		    		TEST_CODE=rs.getString("test_code");
		    		SHOOT_MIN_PRODUCE_QTY=rs.getInt("shoot_min_qty");
		    		FORE_PLAN_WEEKS=rs.getInt("pre_weeks");
		    		maxColorCount=rs.getInt("sh_week_colors");
		    		Rccp(conn,config_xml);		
		    		
		    		strSQL="update fcmps_test set END_TEST=sysdate where test_code='"+rs.getString("test_code")+"'";
				    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData2.executeUpdate();
				    pstmtData2.close();
				    
		    		stDate=new Date();	
		    		this.doStatResult(conn,stDate);

		    	}while(rs.next());
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
			closeConnection(conn);
		}	
	        			
	}
	
	public void Rccp(Connection conn,String config_xml) throws Exception  {
    	
		System.out.println("開始計算計劃號:"+TEST_CODE+" "+new Date().toString());
/*	
    	this.doBeforeAction(conn);
    	
//===============================1422周================================================================
  	
    	FOS_File="F:/FTI周計劃/FOS/1422周FOS.xls";
    	
    	System.out.println("導入1422FOS:"+new Date().toString());
    	
    	Import(FOS_File, config_xml);
    	Runtime.getRuntime().gc();  

    	System.out.println("抵扣開始:"+new Date().toString());
    	doQuitStock(FA_NO,false,conn,config_xml);
    	System.out.println("抵扣完成:"+new Date().toString());
    	Runtime.getRuntime().gc();
    	
    	Thread.sleep(30000);
    	
    	doInsertMPS022(FA_NO,maxColorCount,conn);
    	Runtime.getRuntime().gc();
    	
    	do1422BeforeAction(conn);
  	
//    	if(1==1) return;
*/    	
    	System.out.println("開始1422預排"+new Date().toString());
    	
    	FCMPS_CLS_ForeGenerateRccpPlan_Var cls_ForeGenerateRccpPlan_Var=FCMPS_CLS_ForeGenerateRccpPlan_Var.getInstance(FA_NO, conn);
    	cls_ForeGenerateRccpPlan_Var.init(FA_NO, conn);
    	
    	FCMPS_CLS_ForeGenerateRccpPlan_MultiThread20141125 cls_ForeGenerateRccpPlan1422=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread20141125();
		cls_ForeGenerateRccpPlan1422.setFA_NO(FA_NO);
		cls_ForeGenerateRccpPlan1422.setUP_USER("DEV17");
		cls_ForeGenerateRccpPlan1422.doGeneratePlan(
				config_xml,
				1422,"'BLITZEN2CLOG','BLTZN2ANMLCLG','BLTZN2PLAIDCLG'");
		
		List<CLS_RCCP_ERROR> ls_Message=cls_ForeGenerateRccpPlan_Var.getLs_Message();
		doPrint(ls_Message, "1422周預排Log");
		
//    	doExportNonPlanColor_SUM(conn,"1422周未排訂單");
//    	Runtime.getRuntime().gc();
    	
		doExportForecastColor_SUM(conn,"1422周預排訂單");
    	Runtime.getRuntime().gc();
    	if(1==1) return;
    	
    	FCMPS_CLS_AutoGenerateRccpPlan_1422 rccp1422=new FCMPS_CLS_AutoGenerateRccpPlan_1422();    	
    	rccp1422.do_Rccp(FA_NO, config_xml, FORE_PLAN_WEEKS, conn,SHOOT_MIN_PRODUCE_QTY);    	
    	Runtime.getRuntime().gc();
    	
    	doExportNonPlanColor_SUM(conn,"1422周排定後的未排訂單");
    	Runtime.getRuntime().gc();
    	
//===============================1423周================================================================
   	
    	FOS_File="F:/FTI周計劃/FOS/1423周FOS.xls";
    	
    	Import(FOS_File, config_xml);
    	Runtime.getRuntime().gc();
    	doQuitStock(FA_NO,false,conn,config_xml);
    	Runtime.getRuntime().gc();

    	Thread.sleep(30000);
    	
    	doInsertMPS022(FA_NO,maxColorCount,conn);
    	Runtime.getRuntime().gc();
    	
    	cls_ForeGenerateRccpPlan_Var=FCMPS_CLS_ForeGenerateRccpPlan_Var.getInstance(FA_NO, conn);
    	cls_ForeGenerateRccpPlan_Var.init(FA_NO, conn);
    	
		FCMPS_CLS_ForeGenerateRccpPlan_MultiThread cls_ForeGenerateRccpPlan1423=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread();
		cls_ForeGenerateRccpPlan1423.setFA_NO(FA_NO);
		cls_ForeGenerateRccpPlan1423.setUP_USER("DEV17");
//		cls_ForeGenerateRccpPlan1423.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
//		cls_ForeGenerateRccpPlan1423.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
		
		cls_ForeGenerateRccpPlan1423.doGeneratePlan(
				config_xml,
				1423,
				"'BLITZEN2CLOG','BLTZN2PLAIDCLG','BLTZN2ANMLCLG'");
		
		ls_Message=cls_ForeGenerateRccpPlan_Var.getLs_Message();
		doPrint(ls_Message, "1423周預排Log");
		
    	doExportNonPlanColor_SUM(conn,"1423周未排訂單");
    	Runtime.getRuntime().gc();
    	
		doExportForecastColor_SUM(conn,"1423周預排訂單");
    	Runtime.getRuntime().gc();
    	
    	FCMPS_CLS_AutoGenerateRccpPlan_1423 rccp1423=new FCMPS_CLS_AutoGenerateRccpPlan_1423();    	
    	rccp1423.do_Rccp(FA_NO, config_xml,FORE_PLAN_WEEKS, conn,SHOOT_MIN_PRODUCE_QTY);
    	Runtime.getRuntime().gc();    	

    	doExportNonPlanColor_SUM(conn,"1423周排定後的未排訂單");
    	Runtime.getRuntime().gc();
    	
//===============================1424周================================================================
    	
    	FOS_File="F:/FTI周計劃/FOS/1424周FOS.xls";
    	
    	Import(FOS_File, config_xml);    	
    	Runtime.getRuntime().gc();
    	doQuitStock(FA_NO,false,conn,config_xml);	
    	Runtime.getRuntime().gc();

    	Thread.sleep(30000);
    	
    	doInsertMPS022(FA_NO,maxColorCount,conn);
    	Runtime.getRuntime().gc();
    	
    	cls_ForeGenerateRccpPlan_Var=FCMPS_CLS_ForeGenerateRccpPlan_Var.getInstance(FA_NO, conn);
    	cls_ForeGenerateRccpPlan_Var.init(FA_NO, conn);
    	
		FCMPS_CLS_ForeGenerateRccpPlan_MultiThread cls_ForeGenerateRccpPlan1424=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread();
		cls_ForeGenerateRccpPlan1424.setFA_NO(FA_NO);
		cls_ForeGenerateRccpPlan1424.setUP_USER("DEV17");
//		cls_ForeGenerateRccpPlan1424.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
//		cls_ForeGenerateRccpPlan1424.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
		
		cls_ForeGenerateRccpPlan1424.doGeneratePlan(
				config_xml,
				1424);
		
		ls_Message=cls_ForeGenerateRccpPlan_Var.getLs_Message();
		doPrint(ls_Message, "1424周預排Log");
		
    	doExportNonPlanColor_SUM(conn,"1424周未排訂單");
    	Runtime.getRuntime().gc();
    	
		doExportForecastColor_SUM(conn,"1424周預排訂單");
    	Runtime.getRuntime().gc();
   	
    	FCMPS_CLS_AutoGenerateRccpPlan_1424 rccp1424=new FCMPS_CLS_AutoGenerateRccpPlan_1424();    	
    	rccp1424.do_Rccp(FA_NO, config_xml, FORE_PLAN_WEEKS,conn,SHOOT_MIN_PRODUCE_QTY);    	
    	Runtime.getRuntime().gc();
    	
    	doExportNonPlanColor_SUM(conn,"1424周排定後的未排訂單");
    	Runtime.getRuntime().gc();
    	
//===============================1425周================================================================
    	
    	FOS_File="F:/FTI周計劃/FOS/1425周FOS.xls";
    	
    	Import(FOS_File, config_xml);    
    	Runtime.getRuntime().gc();
    	doQuitStock(FA_NO,false,conn,config_xml);
    	Runtime.getRuntime().gc();

    	Thread.sleep(30000);
    	
    	doInsertMPS022(FA_NO,maxColorCount,conn);
    	Runtime.getRuntime().gc();
    	
		cls_ForeGenerateRccpPlan_Var=FCMPS_CLS_ForeGenerateRccpPlan_Var.getInstance(FA_NO, conn);
    	cls_ForeGenerateRccpPlan_Var.init(FA_NO, conn);
    	
		FCMPS_CLS_ForeGenerateRccpPlan_MultiThread cls_ForeGenerateRccpPlan1425=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread();
		cls_ForeGenerateRccpPlan1425.setFA_NO(FA_NO);
		cls_ForeGenerateRccpPlan1425.setUP_USER("DEV17");
//		cls_ForeGenerateRccpPlan1425.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
//		cls_ForeGenerateRccpPlan1425.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
		
		cls_ForeGenerateRccpPlan1425.doGeneratePlan(
				config_xml,
				1425);
		
		ls_Message=cls_ForeGenerateRccpPlan_Var.getLs_Message();
		doPrint(ls_Message, "1425周預排Log");
		
    	doExportNonPlanColor_SUM(conn,"1425周未排訂單");
    	Runtime.getRuntime().gc();
    	
		doExportForecastColor_SUM(conn,"1425周預排訂單");
    	Runtime.getRuntime().gc();
    	
//    	if(1==1)return;
    	
    	FCMPS_CLS_AutoGenerateRccpPlan_1425 rccp1425=new FCMPS_CLS_AutoGenerateRccpPlan_1425();    	
    	rccp1425.do_Rccp(FA_NO, config_xml, FORE_PLAN_WEEKS,conn,SHOOT_MIN_PRODUCE_QTY);    	
    	Runtime.getRuntime().gc();    	    	
    	
    	doExportNonPlanColor_SUM(conn,"1425周排定後的未排訂單");
    	Runtime.getRuntime().gc();
    	
//===============================1426周================================================================
    	
    	FOS_File="F:/FTI周計劃/FOS/1426周FOS.xls";
    	
    	Import(FOS_File, config_xml);   
    	Runtime.getRuntime().gc();
    	doQuitStock(FA_NO,false,conn,config_xml);
    	Runtime.getRuntime().gc();

    	Thread.sleep(30000);
    	
    	doInsertMPS022(FA_NO,maxColorCount,conn);
    	Runtime.getRuntime().gc();
    	
    	cls_ForeGenerateRccpPlan_Var=FCMPS_CLS_ForeGenerateRccpPlan_Var.getInstance(FA_NO, conn);
    	cls_ForeGenerateRccpPlan_Var.init(FA_NO, conn);
    	
		FCMPS_CLS_ForeGenerateRccpPlan_MultiThread cls_ForeGenerateRccpPlan1426=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread();
		cls_ForeGenerateRccpPlan1426.setFA_NO(FA_NO);
		cls_ForeGenerateRccpPlan1426.setUP_USER("DEV17");
//		cls_ForeGenerateRccpPlan1426.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
//		cls_ForeGenerateRccpPlan1426.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
		
		cls_ForeGenerateRccpPlan1426.doGeneratePlan(
				config_xml,
				1426);
		
		ls_Message=cls_ForeGenerateRccpPlan_Var.getLs_Message();
		doPrint(ls_Message, "1426周預排Log");
		
    	doExportNonPlanColor_SUM(conn,"1426周未排訂單");
    	Runtime.getRuntime().gc();
    	
		doExportForecastColor_SUM(conn,"1426周預排訂單");
    	Runtime.getRuntime().gc();
  	
    	FCMPS_CLS_AutoGenerateRccpPlan_1426 rccp1426=new FCMPS_CLS_AutoGenerateRccpPlan_1426();    	
    	rccp1426.do_Rccp(FA_NO, config_xml, FORE_PLAN_WEEKS,conn,SHOOT_MIN_PRODUCE_QTY);      	
    	Runtime.getRuntime().gc();
    	
    	doExportNonPlanColor_SUM(conn,"1426周排定後的未排訂單");
    	Runtime.getRuntime().gc();
    	
//===============================1427周================================================================
    	
    	FOS_File="F:/FTI周計劃/FOS/1427周FOS.xls";
   	
    	Import(FOS_File, config_xml);   
    	Runtime.getRuntime().gc();
    	doQuitStock(FA_NO,false,conn,config_xml);
    	Runtime.getRuntime().gc();

    	Thread.sleep(30000);
    	
    	doInsertMPS022(FA_NO,maxColorCount,conn);
    	Runtime.getRuntime().gc();
    	
    	cls_ForeGenerateRccpPlan_Var=FCMPS_CLS_ForeGenerateRccpPlan_Var.getInstance(FA_NO, conn);
    	cls_ForeGenerateRccpPlan_Var.init(FA_NO, conn);
    	
		FCMPS_CLS_ForeGenerateRccpPlan_MultiThread cls_ForeGenerateRccpPlan1427=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread();
		cls_ForeGenerateRccpPlan1427.setFA_NO(FA_NO);
		cls_ForeGenerateRccpPlan1427.setUP_USER("DEV17");
//		cls_ForeGenerateRccpPlan1427.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
//		cls_ForeGenerateRccpPlan1427.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
		
		cls_ForeGenerateRccpPlan1427.doGeneratePlan(
				config_xml,
				1427);
		
		ls_Message=cls_ForeGenerateRccpPlan_Var.getLs_Message();
		doPrint(ls_Message, "1427周預排Log");
		
    	doExportNonPlanColor_SUM(conn,"1427周未排訂單");
    	Runtime.getRuntime().gc();
    	
		doExportForecastColor_SUM(conn,"1427周預排訂單");
    	Runtime.getRuntime().gc();
    	
    	FCMPS_CLS_AutoGenerateRccpPlan_1427 rccp1427=new FCMPS_CLS_AutoGenerateRccpPlan_1427();    	
    	rccp1427.do_Rccp(FA_NO, config_xml, FORE_PLAN_WEEKS, conn,SHOOT_MIN_PRODUCE_QTY);      	
    	Runtime.getRuntime().gc();    	
    	
    	doExportNonPlanColor_SUM(conn,"1427周排定後的未排訂單");
    	Runtime.getRuntime().gc();
    	
//===============================1428周================================================================
    	
    	FOS_File="F:/FTI周計劃/FOS/1428周FOS.xls";
   	
    	Import(FOS_File, config_xml);   
    	Runtime.getRuntime().gc();
    	doQuitStock(FA_NO,false,conn,config_xml);
    	Runtime.getRuntime().gc();

    	Thread.sleep(30000);
    	
    	doInsertMPS022(FA_NO,maxColorCount,conn);
    	Runtime.getRuntime().gc();
    	
    	cls_ForeGenerateRccpPlan_Var=FCMPS_CLS_ForeGenerateRccpPlan_Var.getInstance(FA_NO, conn);
    	cls_ForeGenerateRccpPlan_Var.init(FA_NO, conn);
    	
		FCMPS_CLS_ForeGenerateRccpPlan_MultiThread cls_ForeGenerateRccpPlan1428=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread();
		cls_ForeGenerateRccpPlan1428.setFA_NO(FA_NO);
		cls_ForeGenerateRccpPlan1428.setUP_USER("DEV17");
//		cls_ForeGenerateRccpPlan1428.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
//		cls_ForeGenerateRccpPlan1428.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
		
//		cls_ForeGenerateRccpPlan.setConnection(conn);
		cls_ForeGenerateRccpPlan1428.doGeneratePlan(
				config_xml,
				1428);
		
		ls_Message=cls_ForeGenerateRccpPlan_Var.getLs_Message();
		doPrint(ls_Message, "1428周預排Log");
		
    	doExportNonPlanColor_SUM(conn,"1428周未排訂單");
    	Runtime.getRuntime().gc();
    	
		doExportForecastColor_SUM(conn,"1428周預排訂單");
    	Runtime.getRuntime().gc();
    	
    	FCMPS_CLS_AutoGenerateRccpPlan_1428 rccp1428=new FCMPS_CLS_AutoGenerateRccpPlan_1428();    	
    	rccp1428.do_Rccp(FA_NO, config_xml, FORE_PLAN_WEEKS, conn,SHOOT_MIN_PRODUCE_QTY);      	
    	Runtime.getRuntime().gc();    	
    	
    	doExportNonPlanColor_SUM(conn,"1428周排定後的未排訂單");
    	Runtime.getRuntime().gc();
    	
//===============================1429周================================================================
    	
    	FOS_File="F:/FTI周計劃/FOS/1429周FOS.xls";
   	
    	Import(FOS_File, config_xml);   
    	Runtime.getRuntime().gc();
    	doQuitStock(FA_NO,false,conn,config_xml);
    	Runtime.getRuntime().gc();

    	Thread.sleep(30000);
    	
    	doInsertMPS022(FA_NO,maxColorCount,conn);
    	Runtime.getRuntime().gc();
    	
    	cls_ForeGenerateRccpPlan_Var=FCMPS_CLS_ForeGenerateRccpPlan_Var.getInstance(FA_NO, conn);
    	cls_ForeGenerateRccpPlan_Var.init(FA_NO, conn);
    	
		FCMPS_CLS_ForeGenerateRccpPlan_MultiThread cls_ForeGenerateRccpPlan1429=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread();
		cls_ForeGenerateRccpPlan1429.setFA_NO(FA_NO);
		cls_ForeGenerateRccpPlan1429.setUP_USER("DEV17");
//		cls_ForeGenerateRccpPlan1429.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
//		cls_ForeGenerateRccpPlan1429.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
		
//		cls_ForeGenerateRccpPlan.setConnection(conn);
		cls_ForeGenerateRccpPlan1429.doGeneratePlan(
				config_xml,
				1429);
		
		ls_Message=cls_ForeGenerateRccpPlan_Var.getLs_Message();
		doPrint(ls_Message, "1429周預排Log");
		
    	doExportNonPlanColor_SUM(conn,"1429周未排訂單");
    	Runtime.getRuntime().gc();
    	
		doExportForecastColor_SUM(conn,"1429周預排訂單");
    	Runtime.getRuntime().gc();
    	
    	FCMPS_CLS_AutoGenerateRccpPlan_1429 rccp1429=new FCMPS_CLS_AutoGenerateRccpPlan_1429();    	
    	rccp1429.do_Rccp(FA_NO, config_xml, FORE_PLAN_WEEKS, conn,SHOOT_MIN_PRODUCE_QTY);      	
    	Runtime.getRuntime().gc();  
    	
    	doExportNonPlanColor_SUM(conn,"1429周排定後的未排訂單");
    	Runtime.getRuntime().gc();
    	
    	doExportRCCP_COLOR_SUM(conn,"周計劃");
    	Runtime.getRuntime().gc();
    	    	
	}
	
	private void Import(String FOS_File,String config_xml) {   
		
		FCMPS_CLS_ImportOrderFromFOS_Var cls_var=FCMPS_CLS_ImportOrderFromFOS_Var.getInstance();
		cls_var.init();
		
		FCMPS_CLS_ImportOrderFromFOS_Ex cls_Import=new FCMPS_CLS_ImportOrderFromFOS_Ex();  
		cls_Import.setUP_USER("DEV17");
		cls_Import.setNeed_Self_Monitor(true);
		cls_Import.doImport(FOS_File, config_xml,50);
		
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
		
		if(!replace) return;
		
		FCMPS_CLS_ReplacePlannedOrder cls_Replace=new FCMPS_CLS_ReplacePlannedOrder(FA_NO,config_xml);
		if(!cls_Replace.doReplace()) {
			System.out.println("訂單替代執行失敗!");
			return;
		}else {
			System.out.println("執行成功!");
		}		
	}
	
	/**
	 * 發出周計劃與否
	 * @param PLAN_NO
	 * @return
	 */
	public static boolean doPublish(String PLAN_NO,String IS_SURE,Connection conn) {
		boolean iRet=false;
		String strSQL="";
//		Connection conn=Application.getApp().getConnection();
		PreparedStatement pstmtData = null;			
		ResultSet rs=null;
		
		try{

			Thread.sleep(30000);
									
			strSQL="select * from FCMPS006 where PLAN_NO='"+PLAN_NO+"' and IS_SURE='"+IS_SURE+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    if(rs.next()) {
		    	return true;
		    }
		    rs.close();
		    pstmtData.close();	
			
		    conn.setAutoCommit(false);	
		    
			//更新FCMPS006. IS_SURE為:Y
			strSQL="update FCMPS006 set IS_SURE='"+IS_SURE+"' where PLAN_NO='"+PLAN_NO+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
		    pstmtData.close();	    															    
		    
		    //將總排產數更新FCMPS010.WORK_PLAN_QTY	
		    
		    if(IS_SURE.equals("Y")) {
				   strSQL="update FCMPS010 " +
				          "set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)+(select sum(FCMPS007.WORK_PLAN_QTY) " +
				          "                   from FCMPS007,FCMPS006 " +
				          "                   where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO " +
				          "                     and FCMPS006.IS_SURE='"+IS_SURE+"' " +
				          "                     and FCMPS006.PLAN_NO='"+PLAN_NO+"' " +
				          "                     and FCMPS007.OD_PONO1=FCMPS010.OD_PONO1 " +
				          "                     and FCMPS007.SH_NO=FCMPS010.SH_NO " +
				          "                     and FCMPS007.SH_SIZE=FCMPS010.SH_SIZE " +
				          "                     and FCMPS007.SH_COLOR=FCMPS010.SH_COLOR " +
				          "                     and FCMPS006.FA_NO=FCMPS010.FA_NO) " +
				          "where (OD_PONO1,SH_NO,SH_SIZE,SH_COLOR,FA_NO) in " +
				          "(select OD_PONO1,SH_NO,SH_SIZE,SH_COLOR,FA_NO from FCMPS007,FCMPS006 " +
				          " where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO and FCMPS007.PLAN_NO='"+PLAN_NO+"')";
				    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				    pstmtData.execute();
				    pstmtData.close();	  
		    }
		    		    	    
		    if(IS_SURE.equals("N")) {
				strSQL="update FCMPS010 " +
				       "set WORK_PLAN_QTY=nvl(WORK_PLAN_QTY,0)-(select sum(FCMPS007.WORK_PLAN_QTY) " +
				       "                   from FCMPS007,FCMPS006 " +
				       "                   where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO " +
				       "                     and FCMPS006.IS_SURE='"+IS_SURE+"' " +
				       "                     and FCMPS006.PLAN_NO='"+PLAN_NO+"' " +
				       "                     and FCMPS007.OD_PONO1=FCMPS010.OD_PONO1 " +
				       "                     and FCMPS007.SH_NO=FCMPS010.SH_NO " +
				       "                     and FCMPS007.SH_SIZE=FCMPS010.SH_SIZE " +
				       "                     and FCMPS007.SH_COLOR=FCMPS010.SH_COLOR " +
				       "                     and FCMPS006.FA_NO=FCMPS010.FA_NO) " +
				       "where (OD_PONO1,SH_NO,SH_SIZE,SH_COLOR,FA_NO) in " +
				       "(select OD_PONO1,SH_NO,SH_SIZE,SH_COLOR,FA_NO from FCMPS007,FCMPS006 " +
				       " where FCMPS007.PLAN_NO=FCMPS006.PLAN_NO and FCMPS007.PLAN_NO='"+PLAN_NO+"')";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData.execute();
			    pstmtData.close();	  		    	
		    }
		    		    
		    conn.commit();

		    iRet=true;
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	try {
	    		conn.rollback();
	    	}catch(Exception ex) {
	    		ex.printStackTrace();
	    	}
	    }finally{	    	

		}	
		return iRet;
		
	}
	
	/**
	 * 設定工廠lead time
	 * @param FA_NO
	 * @param conn
	 */
	private void doInsertMPS022(String FA_NO,int maxColorCount,Connection conn) {		
		ResultSet rs = null;
		PreparedStatement pstmtData = null;
		PreparedStatement pstmtData2 = null;
		try	{
			conn.setAutoCommit(false);
			String strSQL="delete from FCMPS022";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();
			
			strSQL="SELECT FA_NO," +
		           "STYLE_NO," +
		           "SH_NO," +
		           "SUM(FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)) OD_QTY "+         
                   "FROM FCMPS010 "+
                   "WHERE OD_QTY-nvl(WORK_PLAN_QTY,0)>0 " +
                   "  and PROCID='"+FCMPS_PUBLIC.PROCID_SHOOT+"'"+
                   "  and nvl(IS_DISABLE,'N')='N' " +
                   "  and nvl(OD_CODE,'N')='N' "+
                   "  and FA_NO = '"+FA_NO+"' "+
                   "group by FA_NO,STYLE_NO,SH_NO";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
			if(rs.next()){
				
				int LeadTime=getLead_Time(FA_NO, FCMPS_PUBLIC.PROCID_SHOOT, conn);
				if(LeadTime<=8) {
					LeadTime=8;
				}else if(LeadTime>8) {
					LeadTime=12;
				}
				
				do {
					String STYLE_NO=rs.getString("STYLE_NO");
					String SH_NO=rs.getString("SH_NO");
					double total_OD_QTY=rs.getDouble("OD_QTY");
					
					int COLOR_COUNT=getColor_Count(FA_NO, SH_NO, conn);
										
					int CUR_WORK_WEEK=Integer.valueOf(WeekUtil.getWeekOfYear(new Date(),true));										
										
					double MD_MIN_CAP_QTY=FCMPS_PUBLIC.getSH_Min_Week_Cap_QTY(FA_NO, SH_NO, CUR_WORK_WEEK,conn);
					
					int WORK_WEEK_END=FCMPS_PUBLIC.getNext_Week(CUR_WORK_WEEK, LeadTime);
					
					double OD_QTY=getOD_QTY(FA_NO, SH_NO, FCMPS_PUBLIC.PROCID_SHOOT, CUR_WORK_WEEK, WORK_WEEK_END, conn);
					
					int OD_PERCENT=0;
					if(MD_MIN_CAP_QTY>0) {
						OD_PERCENT=(int)((OD_QTY/LeadTime)/MD_MIN_CAP_QTY*100);
						if((OD_QTY/LeadTime)%MD_MIN_CAP_QTY>0) OD_PERCENT++;
					}
										
					int lt_COLOR_COUNT=getColor_Count(FA_NO, SH_NO, CUR_WORK_WEEK, WORK_WEEK_END, conn);

					int ALLOW_COLOR=COLOR_COUNT;
					if(COLOR_COUNT>maxColorCount) ALLOW_COLOR=maxColorCount;
					
					strSQL="insert into fcmps022 (sh_no, style_no, fa_no, od_qty, total_color, sh_cap_qty, " +
					   "                      lt_weeks, lt_od_qty, lt_percent, lt_color, allow_color,allow_move_up_week, " +
					   "                      up_user,up_date)"+
                    " values ("+
                    "'"+SH_NO+"'"+
                    ",'"+STYLE_NO+"'"+
                    ",'"+FA_NO+"'"+
                    ",'"+total_OD_QTY+"'"+
                    ",'"+COLOR_COUNT+"'"+
                    ",'"+MD_MIN_CAP_QTY+"'"+
                    ",'"+LeadTime+"'"+
                    ",'"+OD_QTY+"'"+
                    ",'"+OD_PERCENT+"'"+
                    ",'"+lt_COLOR_COUNT+"'"+
                    ",'"+ALLOW_COLOR+"'"+
                    ","+LeadTime+
                    ",'DEV17'"+
                    ",Sysdate"+
                    ")";
			    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    pstmtData2.execute();
				pstmtData2.close();

				}while(rs.next());
			}
			rs.close();
			pstmtData.close();
			
			conn.commit();
			
		}catch (Exception err) {
			err.printStackTrace();
	    	try {
	    		conn.rollback();
	    	}catch(Exception ex) {
	    		ex.printStackTrace();
	    	}
		}finally {
			
		}	
			
	}
	
	/**
	 * 取得 Lead Time
	 * @param FA_NO
	 * @param SH_NO
	 * @param PROCID
	 * @param conn
	 * @return
	 */
	private int getLead_Time(
			String FA_NO,
			String PROCID,
			Connection conn) {
		int iRet=0;
		String strSQL="";
//		Connection conn =Application.getApp().getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select SUM(FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)) OD_QTY " +
				   "from FCMPS010 " +
                   "WHERE FA_NO='"+FA_NO+"'"+
                   "  and PROCID='"+PROCID+"'"+              
                   "  and OD_QTY-nvl(WORK_PLAN_QTY,0)>0 "+
                   "  and nvl(IS_DISABLE,'N')='N' " +
                   "  and nvl(OD_CODE,'N')='N' ";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	double OD_QTY=FCMPS_PUBLIC.getDouble(rs.getDouble("OD_QTY"));
		    	
				int CUR_WORK_WEEK=Integer.valueOf(WeekUtil.getWeekOfYear(new Date(),true));									

				do {
					if(iRet>12) break;
					double PROC_Plan_QTY=FCMPS_PUBLIC.get_PROC_Plan_QTY(FA_NO, CUR_WORK_WEEK, PROCID, conn)[0];
					OD_QTY=OD_QTY-PROC_Plan_QTY;
					
					CUR_WORK_WEEK=FCMPS_PUBLIC.getNext_Week(CUR_WORK_WEEK, 1);
					
					iRet++;
					
				}while(OD_QTY>0);
		    	
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	private int getColor_Count(
			String FA_NO,
			String SH_NO,
			Connection conn) {
		int iRet=0;
		String strSQL="";
//		Connection conn =Application.getApp().getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select FA_NO " +
		           "STYLE_NO," +
		           "SH_NO," +
		           "SH_COLOR "+    
				   "from FCMPS010 " +
                   "WHERE FA_NO='"+FA_NO+"'"+
                   "  and SH_NO='"+SH_NO+"'"+            
                   "  and OD_QTY-nvl(WORK_PLAN_QTY,0)>0 "+
                   "  and nvl(IS_DISABLE,'N')='N' " +
                   "  and nvl(OD_CODE,'N')='N' "+
                   "group by FA_NO,STYLE_NO,SH_NO,SH_COLOR";
			
			strSQL="select Count(*) COLOR_COUNT from ("+strSQL+")";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=rs.getInt("COLOR_COUNT");
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	/**
	 * 取階段配色個數
	 * @param FA_NO
	 * @param SH_NO
	 * @param PROCID
	 * @param WORK_WEEK_START
	 * @param WORK_WEEK_END
	 * @param conn
	 * @return
	 */
	private int getColor_Count(
			String FA_NO,
			String SH_NO,
			int WORK_WEEK_START,
			int WORK_WEEK_END,
			Connection conn) {
		int iRet=0;
		String strSQL="";
//		Connection conn =Application.getApp().getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select FA_NO " +
		           "STYLE_NO," +
		           "SH_NO," +
		           "SH_COLOR "+
				   "from FCMPS010 " +
                   "WHERE to_char(OD_FGDATE,'YYWW')>='"+WORK_WEEK_START+"'"+
                   "  and to_char(OD_FGDATE,'YYWW')<='"+WORK_WEEK_END+"'"+ 
                   "  and SH_NO='"+SH_NO+"'"+            
                   "  and OD_QTY-nvl(WORK_PLAN_QTY,0)>0 "+
                   "  and nvl(IS_DISABLE,'N')='N' " +
                   "  and nvl(OD_CODE,'N')='N' "+
                   "  and FA_NO='"+FA_NO+"' "+
                   "group by FA_NO,STYLE_NO,SH_NO,SH_COLOR";
			
			strSQL="select Count(*) COLOR_COUNT from ("+strSQL+")";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=rs.getInt("COLOR_COUNT");
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	/**
	 * 取階段訂單數
	 * @param FA_NO
	 * @param SH_NO
	 * @param PROCID
	 * @param WORK_WEEK_START
	 * @param WORK_WEEK_END
	 * @param conn
	 * @return
	 */
	private double getOD_QTY(
			String FA_NO,
			String SH_NO,
			String PROCID,
			int WORK_WEEK_START,
			int WORK_WEEK_END,
			Connection conn) {
		double iRet=0;
		String strSQL="";
//		Connection conn =Application.getApp().getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select SUM(FCMPS010.OD_QTY-nvl(FCMPS010.WORK_PLAN_QTY,0)) OD_QTY " +
				   "from FCMPS010 " +
                   "WHERE to_char(OD_FGDATE,'YYWW')>='"+WORK_WEEK_START+"'"+
                   "  and to_char(OD_FGDATE,'YYWW')<='"+WORK_WEEK_END+"'"+ 
                   "  and FA_NO='"+FA_NO+"'"+
                   "  and SH_NO='"+SH_NO+"'"+
                   "  and PROCID='"+PROCID+"'"+              
                   "  and OD_QTY-nvl(WORK_PLAN_QTY,0)>0 "+
                   "  and nvl(IS_DISABLE,'N')='N' " +
                   "  and nvl(OD_CODE,'N')='N' ";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=FCMPS_PUBLIC.getDouble(rs.getDouble("OD_QTY"));
		    }
			rs.close();
			pstmtData.close();

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	/**
	 * 匯出型體匯總明細表
	 *
	 */
	private void doExportRCCP_COLOR_SUM(Connection conn,String fileName){
	
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			strSQL="select "+
            "FCMPS006.FA_NO, "+
            "FCMPS006.PLAN_NO,"+
            "FCMPS006.PLAN_NAME,"+
            "FCPS22_2.PB_PTNA,"+
            "FCMPS006.WORK_WEEK, "+
            "FCMPS006.WEEK_MAX_CAP_QTY,"+                        
            "FCMPS006.WORK_DAYS,"+
            "(case when FCMPS006.IS_SURE='Y' then '是'" +
            "      when FCMPS006.IS_SURE='N' then '否' end)IS_SURE, "+
            "FCMPS007.STYLE_NO,"+
            "FCMPS007.SH_NO,"+
            "FCMPS007.SH_COLOR,"+
            "FCMPS007.SH_SIZE,"+
            "FCMPS007.SIZE_CAP_QTY,"+
            "(CASE WHEN FCMPS007.SHARE_SH_NO IS NULL THEN FCMPS007.SH_NO ELSE FCMPS007.SHARE_SH_NO END)SHARE_SH_NO,"+
            "(CASE WHEN FCMPS007.SHARE_SIZE IS NULL THEN FCMPS007.SH_SIZE ELSE FCMPS007.SHARE_SIZE END)SHARE_SIZE,"+
            "sum(FCMPS007.WORK_PLAN_QTY) WORK_PLAN_QTY,"+  
            "FCMPS007.IS_USE_CAP "+        
            "from FCMPS006,FCMPS007,FCPS22_2 " +
            "where FCMPS006.IS_SURE='Y' "+
            "  and FCMPS006.PLAN_NO=FCMPS007.PLAN_NO " +
            "  and FCMPS006.PROCID=FCPS22_2.PB_PTNO(+) "+    
            "group by FCMPS006.FA_NO, "+
            "         FCMPS006.PLAN_NO,"+
            "         FCMPS006.PLAN_NAME,"+
            "         FCPS22_2.PB_PTNA,"+
            "         FCMPS006.WORK_WEEK, "+
            "         FCMPS006.WEEK_MAX_CAP_QTY,"+
            "         FCMPS006.PLAN_PRIORITY_TYPE,"+
            "         FCMPS006.PLAN_BY_DATE,"+
            "         FCMPS006.WORK_DAYS,"+
            "         FCMPS006.IS_SURE,"+
            "         FCMPS007.STYLE_NO,"+
            "         FCMPS007.SH_NO,"+
            "         FCMPS007.SH_COLOR,"+
            "         FCMPS007.SH_SIZE,"+
            "         FCMPS007.SIZE_CAP_QTY,"+
            "         (CASE WHEN FCMPS007.SHARE_SH_NO IS NULL THEN FCMPS007.SH_NO ELSE FCMPS007.SHARE_SH_NO END),"+
            "         (CASE WHEN FCMPS007.SHARE_SIZE IS NULL THEN FCMPS007.SH_SIZE ELSE FCMPS007.SHARE_SIZE END),"+
            "         FCMPS007.IS_USE_CAP "+            
            "order by FCMPS006.PLAN_NO,(CASE WHEN FCMPS007.SHARE_SH_NO IS NULL THEN FCMPS007.SH_NO ELSE FCMPS007.SHARE_SH_NO END),FCMPS007.STYLE_NO";
		
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	JExportToExcel JETE=new JExportToExcel();

		    	String ls_Items[]=new String[] {
		    			"廠別",
		    			"計劃號",
		    			"計劃名稱",
		    			"制程",
		    			"周次",
		    			"最大排產量",		
		    			"工作天數",
		    			"已發放",
		    			"型體代號",
		    			"型體",
		    			"配色",
		    			"Size",
		    			"Size產能",
		    			"主型體",
		    			"主Size",
		    			"排產數",
		    			"占用產能"
		    			};
		    	
		    	HSSFWorkbook wb=JETE.getWorkbook();
		    	HSSFSheet sheet =wb.createSheet();
		    	
				int iRow=0;
		    	HSSFRow row = sheet.createRow(iRow);
		    	HSSFCell cell = null;
		    	
		    	for(int i=0;i<ls_Items.length;i++) {			    	
			    	cell = row.createCell((short)i);
			    	JETE.setCellValue(wb, cell,ls_Items[i]);  			    	
		    	}
		    	
		    	do {		    		
		    		iRow++;
		    		row = sheet.createRow(iRow);
		    		
			    	for(int i=0;i<ls_Items.length;i++) {			    	
				    	cell = row.createCell((short)i);
				    	JETE.setCellValue(wb, cell,rs.getObject(i+1));  			    	
			    	}
			    	
		    		if(iRow==65535) {
		    			sheet=wb.createSheet();		    			
		    			iRow=0;			
		    			
		    			row = sheet.createRow(iRow);
				    	for(int i=0;i<ls_Items.length;i++) {			    	
					    	cell = row.createCell((short)i);
					    	JETE.setCellValue(wb, cell,ls_Items[i]);  			    	
				    	}
		    		}
		    		
		    	}while(rs.next());
		    	
		    	File file=new File(Main_Path+"/"+TEST_CODE+"/"+fileName+".xls");
		    	if(file.exists()) {
		    		file.delete();
		    	}else {
		    		file=new File(Main_Path+"/"+TEST_CODE);
		    		if(!file.exists()) {
		    			file.mkdir();
		    		}
		    	}
		    	
				FileOutputStream fileOut=null;
				fileOut = new FileOutputStream(Main_Path+"/"+TEST_CODE+"/"+fileName+".xls");
				
				wb.write(fileOut);
				fileOut.close();
				
		    }
		    rs.close();
		    pstmtData.close();
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	

		}
	    
	}
	
	/**
	 * 匯出未排訂單配色匯總明細表
	 *
	 */
	private void doExportNonPlanColor_SUM(Connection conn,String fileName){	
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			strSQL="SELECT FA_NO,STYLE_NO,SH_NO,SH_COLOR,SH_SIZE,PB_PTNA,to_char(OD_FGDATE,'IYIW')WORK_WEEK,LEAN_NO,SUM(OD_QTY-WORK_PLAN_QTY) WORK_PLAN_QTY "+
                   "FROM FCMPS010,FCPS22_2 "+
                   "WHERE OD_QTY-nvl(WORK_PLAN_QTY,0)>0" +
                   "  AND IS_DISABLE='N'"+
                   "  AND nvl(OD_CODE,'N')='N' "+
                   "  AND PROCID=FCPS22_2.PB_PTNO(+) "+
                   "GROUP BY FA_NO,STYLE_NO,SH_NO,SH_COLOR,SH_SIZE,PB_PTNA,to_char(OD_FGDATE,'IYIW'),LEAN_NO ";
		
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		    rs.setFetchSize(3000);
		    
		    if(rs.next()){
		    	JExportToExcel JETE=new JExportToExcel();

		    	String ls_Items[]=new String[] {
		    			"廠別",
		    			"型體代號",
		    			"型體",
		    			"配色",
		    			"Size",
		    			"制程",
		    			"周次",
		    			"線別",
		    			"訂單數量"
		    			};
		    	
		    	HSSFWorkbook wb=JETE.getWorkbook();
		    	HSSFSheet sheet =wb.createSheet();
		    	
				int iRow=0;
		    	HSSFRow row = sheet.createRow(iRow);
		    	HSSFCell cell = null;
		    	
		    	for(int i=0;i<ls_Items.length;i++) {			    	
			    	cell = row.createCell((short)i);
			    	JETE.setCellValue(wb, cell,ls_Items[i]);  			    	
		    	}
		    	
		    	do {		    		
		    		iRow++;
		    		row = sheet.createRow(iRow);
		    		
			    	for(int i=0;i<ls_Items.length;i++) {			    	
				    	cell = row.createCell((short)i);
				    	JETE.setCellValue(wb, cell,rs.getObject(i+1));  			    	
			    	}
			    	
		    		if(iRow==65535) {
		    			sheet=wb.createSheet();		    			
		    			iRow=0;			
		    			
		    			row = sheet.createRow(iRow);
				    	for(int i=0;i<ls_Items.length;i++) {			    	
					    	cell = row.createCell((short)i);
					    	JETE.setCellValue(wb, cell,ls_Items[i]);  			    	
				    	}
		    		}
		    		
		    	}while(rs.next());
		    	
		    	File file=new File(Main_Path+"/"+TEST_CODE+"/"+fileName+".xls");
		    	if(file.exists()) {
		    		file.delete();
		    	}else {
		    		file=new File(Main_Path+"/"+TEST_CODE);
		    		if(!file.exists()) {
		    			file.mkdir();
		    		}
		    	}
		    	
				FileOutputStream fileOut=null;
				fileOut = new FileOutputStream(Main_Path+"/"+TEST_CODE+"/"+fileName+".xls");
				
				wb.write(fileOut);
				fileOut.close();
				
		    }
		    rs.close();
		    pstmtData.close();
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{
	    	
	    }
	    
	}
	
	/**
	 * 匯出預排型體配色匯總明細表
	 *
	 */
	private void doExportForecastColor_SUM(Connection conn,String fileName){	
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{
			strSQL="SELECT " +
					"FA_NO," +
					"(case when SHARE_SH_NO is null then SH_NO else SHARE_SH_NO end) SHARE_SH_NO,"+
					"(case when SHARE_SIZE is null then SH_SIZE else SHARE_SIZE end) SHARE_SIZE,"+
					"SH_CAP_QTY,"+
					"SIZE_CAP_QTY,"+
					"STYLE_NO," +
					"SH_NO," +
					"SH_COLOR," +
					"SH_SIZE," +
					"PROCID," +
					"WORK_WEEK," +
					"SUM(WORK_PLAN_QTY) WORK_PLAN_QTY "+
                   "FROM FCMPS021 "+
                   "GROUP BY FA_NO,SHARE_SH_NO,SHARE_SIZE,SH_CAP_QTY,SIZE_CAP_QTY,STYLE_NO,SH_NO,SH_COLOR,SH_SIZE,PROCID,WORK_WEEK ";
		
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		    rs.setFetchSize(3000);
		    
		    if(rs.next()){
		    	JExportToExcel JETE=new JExportToExcel();

		    	String ls_Items[]=new String[] {
		    			"廠別",
		    			"主型體",
		    			"主Size",
		    			"型體產能",
		    			"Size產能",
		    			"型體代號",
		    			"型體",
		    			"配色",
		    			"Size",
		    			"制程",
		    			"周次",
		    			"排產數量"
		    			};
		    	
		    	HSSFWorkbook wb=JETE.getWorkbook();
		    	HSSFSheet sheet =wb.createSheet();
		    	
				int iRow=0;
		    	HSSFRow row = sheet.createRow(iRow);
		    	HSSFCell cell = null;
		    	
		    	for(int i=0;i<ls_Items.length;i++) {			    	
			    	cell = row.createCell((short)i);
			    	JETE.setCellValue(wb, cell,ls_Items[i]);  			    	
		    	}
		    	
		    	do {		    		
		    		iRow++;
		    		row = sheet.createRow(iRow);
		    		
			    	for(int i=0;i<ls_Items.length;i++) {			    	
				    	cell = row.createCell((short)i);
				    	JETE.setCellValue(wb, cell,rs.getObject(i+1));  			    	
			    	}
			    	
		    		if(iRow==65535) {
		    			sheet=wb.createSheet();		    			
		    			iRow=0;			
		    			
		    			row = sheet.createRow(iRow);
				    	for(int i=0;i<ls_Items.length;i++) {			    	
					    	cell = row.createCell((short)i);
					    	JETE.setCellValue(wb, cell,ls_Items[i]);  			    	
				    	}
		    		}
		    		
		    	}while(rs.next());
		    	
		    	File file=new File(Main_Path+"/"+TEST_CODE+"/"+fileName+".xls");
		    	if(file.exists()) {
		    		file.delete();
		    	}else {
		    		file=new File(Main_Path+"/"+TEST_CODE);
		    		if(!file.exists()) {
		    			file.mkdir();
		    		}
		    	}
		    	
				FileOutputStream fileOut=null;
				fileOut = new FileOutputStream(Main_Path+"/"+TEST_CODE+"/"+fileName+".xls");
				
				wb.write(fileOut);
				fileOut.close();
				
		    }
		    rs.close();
		    pstmtData.close();
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{
	    	
	    }
	    
	}

	/**
	 * 統計各周次測試結果
	 *
	 */
	public void doStatResult(Connection conn,Date... stDate){	
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		File file=null;
		try{
			
	    	JExportToExcel JETE=new JExportToExcel();
	    	
	    	file=new File(Main_Path+"/情景測試分析.xls");
	    	FileInputStream fi=new FileInputStream(file);
	    	
	    	HSSFWorkbook wb=new HSSFWorkbook(fi);
	    	JETE.setWorkbook(wb);
	    	
	    	if(wb.getSheet(TEST_CODE)!=null) {
	    		wb.removeSheetAt(wb.getSheetIndex(TEST_CODE));
	    	}

	    	HSSFSheet sheet =wb.cloneSheet(0);
	    	
	    	wb.setSheetName(wb.getNumberOfSheets()-1, TEST_CODE);
	    	
			int iRow=1;
	    	HSSFRow row = sheet.getRow(iRow);
	    	HSSFCell cell = null;
	    			    	
	    	cell = row.getCell((short)6);
	    	JETE.setCellValue(wb, cell,SHOOT_MIN_PRODUCE_QTY);  
	    	
	    	cell = row.getCell((short)7);
	    	JETE.setCellValue(wb, cell,SHOOT_MIN_PRODUCE_QTY); 
	    	
	    	iRow++;
	    	
	    	iRow++;		    	
	    	row = sheet.getRow(iRow);
	    	cell = row.getCell((short)6);
	    	JETE.setCellValue(wb, cell,FORE_PLAN_WEEKS);  
	    	cell = row.getCell((short)7);
	    	JETE.setCellValue(wb, cell,FORE_PLAN_WEEKS);  
	    	
	    	iRow++;		    	
	    	row = sheet.getRow(iRow);
	    	cell = row.getCell((short)6);
	    	JETE.setCellValue(wb, cell,maxColorCount);  
	    	cell = row.getCell((short)7);
	    	JETE.setCellValue(wb, cell,maxColorCount);  
	    	
	    	iRow++;
	    	
	    	iRow++;		    	
	    	row = sheet.getRow(iRow);
	    	cell = row.getCell((short)6);
	    	JETE.setCellValue(wb, cell,SHOOT_MIN_PRODUCE_QTY);  
	    	cell = row.getCell((short)7);
	    	JETE.setCellValue(wb, cell,SHOOT_MIN_PRODUCE_QTY);  
	    	
	    	iRow++;		    	
	    	row = sheet.getRow(iRow);
	    	cell = row.getCell((short)6);
	    	JETE.setCellValue(wb, cell,SHOOT_MIN_PRODUCE_QTY);  
	    	cell = row.getCell((short)7);
	    	JETE.setCellValue(wb, cell,SHOOT_MIN_PRODUCE_QTY); 
	    	
	    	if(stDate.length>0) {
	    		iRow=9;	
		    	row = sheet.getRow(iRow);
		    	cell = row.getCell((short)0);
		    	JETE.setCellValue(wb, cell,"測試時間:"+stDate[0].toString()+" --- "+new Date().toString());  
	    	}
	    	
	    	//----系統-配色個數
			strSQL="select plan_no,count(*) iCount from ( "+
                   "select plan_no,sh_no,sh_color,sum(work_plan_qty) " +
                   "  from fcmps007 where procid='300' "+
                   "group by plan_no,sh_no,sh_color) "+
                   "group by plan_no order by plan_no ";
		
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		    rs.setFetchSize(3000);
		    
		    if(rs.next()){
		    	iRow=15;	    	
		    	row = sheet.getRow(iRow);
		    	int iCol=0;
		    	do {
		    		iCol++;
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("iCount"));  
			    	
		    	}while(rs.next());
		    	
	    		iCol++;
		    	cell = row.getCell((short)iCol);
		    	cell.setCellFormula("AVERAGEA(B16:I16)");
		    	
	    		iCol++;
		    	cell = row.getCell((short)iCol);
		    	cell.setCellFormula("J16-J12");
		    	
		    }
		    rs.close();
		    pstmtData.close();
		    
		    //----系統-型體個數
			strSQL="select plan_no,count(*) iCount from ( "+
                   "select plan_no,sh_no,sum(work_plan_qty) " +
                   "  from fcmps007 where procid='300' "+
                   "group by plan_no,sh_no) "+
                   "group by plan_no order by plan_no ";
	
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		    rs.setFetchSize(3000);
		    
		    if(rs.next()){
		    	iRow=16;	    	
		    	row = sheet.getRow(iRow);
		    	int iCol=0;
		    	do {
		    		iCol++;
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("iCount"));  
			    	
		    	}while(rs.next());

	    		iCol++;
		    	cell = row.getCell((short)iCol);
		    	cell.setCellFormula("AVERAGEA(B17:I17)");
		    	
	    		iCol++;
		    	cell = row.getCell((short)iCol);
		    	cell.setCellFormula("J17-J13");
		    	
		    }
		    rs.close();
		    pstmtData.close();		    
		    
		    //----系統-計劃量
			strSQL="select plan_no,sum(work_plan_qty) work_plan_qty " +
                   "  from fcmps007 where procid='300' "+
                   "group by plan_no order by plan_no ";
	
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		    rs.setFetchSize(3000);
		    
		    if(rs.next()){
		    	iRow=17;	    	
		    	row = sheet.getRow(iRow);
		    	int iCol=0;
		    	do {
		    		iCol++;
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("work_plan_qty"));  
			    	
		    	}while(rs.next());

	    		iCol++;
		    	cell = row.getCell((short)iCol);
		    	cell.setCellFormula("AVERAGEA(B18:I18)");
		    	
	    		iCol++;
		    	cell = row.getCell((short)iCol);
		    	cell.setCellFormula("J18-J14");
		    	
		    }
		    rs.close();
		    pstmtData.close();		    
		    
		    //----型體配色與上周相同個數
	    	iRow=18;	    	
	    	row = sheet.getRow(iRow);
	    	int iCol=1;
	    	
		    for(int iWeek=1423;iWeek<=1429;iWeek++) {
		    	
		    	iCol++;
		    	
				strSQL="select plan_no,count(*) iCount from ( "+
                       "select plan_no,sh_no,sh_color,sum(work_plan_qty) " +
                       "  from fcmps007 where procid='300' and plan_no='"+iWeek+"ZD' "+
                       "  and (SH_NO,SH_COLOR) IN (SELECT SH_NO,SH_COLOR FROM FCMPS007 WHERE PLAN_NO='"+(iWeek-1)+"ZD') "+
                       "group by plan_no,sh_no,sh_color) "+
                       "group by plan_no";
	
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			    rs.setFetchSize(3000);
			    
			    if(rs.next()){		    		
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("iCount"));  
			    }
			    rs.close();
			    pstmtData.close();			    	
		    }		    
    		iCol++;
	    	cell = row.getCell((short)iCol);
	    	cell.setCellFormula("AVERAGEA(B19:I19)");
		    
		    //----型體與上周相同個數
	    	iRow=19;	    	
	    	row = sheet.getRow(iRow);
	    	iCol=1;
	    	
		    for(int iWeek=1423;iWeek<=1429;iWeek++) {
		    	
		    	iCol++;
		    	
				strSQL="select plan_no,count(*) iCount from ( "+
                       "select plan_no,sh_no,sum(work_plan_qty) " +
                       "  from fcmps007 where procid='300' and plan_no='"+iWeek+"ZD' "+
                       "  and SH_NO IN (SELECT SH_NO FROM FCMPS007 WHERE PLAN_NO='"+(iWeek-1)+"ZD') "+
                       "group by plan_no,sh_no) "+
                       "group by plan_no";
	
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			    rs.setFetchSize(3000);
			    
			    if(rs.next()){		    		
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("iCount"));  
			    }
			    rs.close();
			    pstmtData.close();			    	
		    }		    
    		iCol++;
	    	cell = row.getCell((short)iCol);
	    	cell.setCellFormula("AVERAGEA(B20:I20)");
	    	
		    
		    //----小於516配色個數
	    	iRow=20;	    	
	    	row = sheet.getRow(iRow);
	    	iCol=0;
	    	
		    for(int iWeek=1422;iWeek<=1429;iWeek++) {
		    	
		    	iCol++;
		    	
				strSQL="select plan_no,count(*) iCount from ( "+
                       "select plan_no,sh_no,sh_color,sum(work_plan_qty) " +
                       "  from fcmps007 where procid='300' and plan_no='"+iWeek+"ZD' "+
                       "group by plan_no,sh_no,sh_color having sum(work_plan_qty)<516) "+
                       "group by plan_no";
	
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			    rs.setFetchSize(3000);
			    
			    if(rs.next()){		    		
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("iCount"));  
			    }
			    rs.close();
			    pstmtData.close();			    	
		    }

    		iCol++;
	    	cell = row.getCell((short)iCol);
	    	cell.setCellFormula("AVERAGEA(B21:I21)");
		    
		    //----小於516型體個數
	    	iRow=21;	    	
	    	row = sheet.getRow(iRow);
	    	iCol=0;
	    	
		    for(int iWeek=1422;iWeek<=1429;iWeek++) {
		    	
		    	iCol++;
		    	
				strSQL="select plan_no,count(*) iCount from ( "+
                       "select plan_no,sh_no,sum(work_plan_qty) " +
                       "  from fcmps007 where procid='300' and plan_no='"+iWeek+"ZD' "+
                       "group by plan_no,sh_no having sum(work_plan_qty)<516) "+
                       "group by plan_no";
	
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			    rs.setFetchSize(3000);
			    
			    if(rs.next()){		    		
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("iCount"));  
			    }
			    rs.close();
			    pstmtData.close();			    	
		    }
		    
    		iCol++;
	    	cell = row.getCell((short)iCol);
	    	cell.setCellFormula("AVERAGEA(B22:I22)");
		    
		    //----小於516配色個數(與上周相同個數)
	    	iRow=22;	    	
	    	row = sheet.getRow(iRow);
	    	iCol=1;
	    	
		    for(int iWeek=1423;iWeek<=1429;iWeek++) {
		    	
		    	iCol++;
		    	
				strSQL="select plan_no,count(*) iCount from ( "+
                       "select plan_no,sh_no,sh_color,sum(work_plan_qty) " +
                       "  from fcmps007 where procid='300' and plan_no='"+iWeek+"ZD' "+
                       "  and (SH_NO,SH_COLOR) IN (SELECT SH_NO,SH_COLOR FROM FCMPS007 WHERE PLAN_NO='"+(iWeek-1)+"ZD' group by sh_no,sh_color having sum(work_plan_qty)<516) "+
                       "group by plan_no,sh_no,sh_color having sum(work_plan_qty)<516) "+
                       "group by plan_no";
	
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			    rs.setFetchSize(3000);
			    
			    if(rs.next()){		    		
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("iCount"));  
			    }
			    rs.close();
			    pstmtData.close();			    	
		    }		    
		    
    		iCol++;
	    	cell = row.getCell((short)iCol);
	    	cell.setCellFormula("AVERAGEA(B23:I23)");
		    
		    //----小於516型體個數(與上周相同個數)
	    	iRow=23;	    	
	    	row = sheet.getRow(iRow);
	    	iCol=1;
	    	
		    for(int iWeek=1423;iWeek<=1429;iWeek++) {
		    	
		    	iCol++;
		    	
				strSQL="select plan_no,count(*) iCount from ( "+
                       "select plan_no,sh_no,sum(work_plan_qty) " +
                       "  from fcmps007 where procid='300' and plan_no='"+iWeek+"ZD' "+
                       "  and SH_NO IN (SELECT SH_NO FROM FCMPS007 WHERE PLAN_NO='"+(iWeek-1)+"ZD' group by sh_no having sum(work_plan_qty)<516) "+
                       "group by plan_no,sh_no having sum(work_plan_qty)<516) "+
                       "group by plan_no";
	
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			    rs.setFetchSize(3000);
			    
			    if(rs.next()){		    		
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("iCount"));  
			    }
			    rs.close();
			    pstmtData.close();			    	
		    }		    
		    
    		iCol++;
	    	cell = row.getCell((short)iCol);
	    	cell.setCellFormula("AVERAGEA(B24:I24)");
		    
	    	//應排未排數量
	    	iRow=24;	    	
	    	row = sheet.getRow(iRow);
	    	iCol=0;
	    	
		    for(int iWeek=1422;iWeek<=1429;iWeek++) {
		    	
		    	iCol++;
		    	
				strSQL="select SH_NO,PROCID,sum(OD_QTY-( " +
					   "(select nvl(SUM(WORK_PLAN_QTY),0) "+
                       "   from fcmps006, fcmps007  " +
                       " where fcmps006.plan_no = fcmps007.plan_no " +
                       "  and fcmps006.procid = fcmps010.procid " +
                       "  and fcmps006.work_week <= fcmps010.work_week_end " +
                       "  and fcmps007.od_pono1 = fcmps010.od_pono1 " +
                       "  and fcmps007.sh_no = fcmps010.sh_no " +
                       "  and fcmps007.sh_color = fcmps010.sh_color " +
                       "  and fcmps007.sh_size = fcmps010.sh_size) + " +
                       "(SELECT nvl(sum(use_qty),0) " +
                       "   FROM FCMPS017 " +
                       "  WHERE OD_PONO1 = fcmps010.od_pono1 " +
                       "    AND SH_ARITCLE = fcmps010.sh_no " +
                       "    AND SH_SIZE = fcmps010.sh_size " +
                       "    AND SH_COLOR = fcmps010.sh_color))) iCount " +
                       "from fcmps010 " +
                       "where work_week_end = "+iWeek +" "+
                       "  and IS_DISABLE = 'N' " +
                       "  and nvl(OD_CODE, 'N') = 'N' " +
                       "  and proc_seq =(select max(proc_seq) from fcps22_1 where sh_aritcle = fcmps010.sh_no) " +
                       "  and (select nvl(SUM(WORK_PLAN_QTY),0) " +
                       "         from fcmps006, fcmps007 " +
                       "        where fcmps006.plan_no = fcmps007.plan_no " +
                       "          and fcmps006.procid = fcmps010.procid " +
                       "          and fcmps006.work_week <= fcmps010.work_week_end " +
                       "          and fcmps007.od_pono1 = fcmps010.od_pono1 " +
                       "          and fcmps007.sh_no = fcmps010.sh_no " +
                       "          and fcmps007.sh_color = fcmps010.sh_color " +
                       "          and fcmps007.sh_size = fcmps010.sh_size) + " +
                       "      (SELECT nvl(sum(use_qty),0) " +
                       "         FROM FCMPS017 " +
                       "        WHERE OD_PONO1 = fcmps010.od_pono1 " +
                       "          AND SH_ARITCLE = fcmps010.sh_no " +
                       "          AND SH_SIZE = fcmps010.sh_size " +
                       "          AND SH_COLOR = fcmps010.sh_color) < OD_QTY "+
                       "group by SH_NO,PROCID";
	
				strSQL="SELECT SUM(iCount) iCount from (select SH_NO,MAX(iCount) iCount FROM ("+strSQL+") GROUP BY SH_NO)";
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			    rs.setFetchSize(3000);
			    
			    if(rs.next()){		    		
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("iCount"));  
			    }
			    rs.close();
			    pstmtData.close();			    	
		    }	
	    	
		    //----型體與上周相同個數(共模)
	    	iRow=25;	    	
	    	row = sheet.getRow(iRow);
	    	iCol=1;
	    	
		    for(int iWeek=1423;iWeek<=1429;iWeek++) {
		    	
		    	iCol++;
		    	
				strSQL="select plan_no,count(*) iCount from ( "+
                       "select plan_no,sh_no,sum(work_plan_qty) " +
                       "  from fcmps007 where procid='300' and plan_no='"+iWeek+"ZD' "+
                       "  and (case when share_sh_no is null then sh_no else share_sh_no end) IN (SELECT (case when share_sh_no is null then sh_no else share_sh_no end) FROM FCMPS007 WHERE PLAN_NO='"+(iWeek-1)+"ZD') "+
                       "group by plan_no,sh_no) "+
                       "group by plan_no";
	
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			    rs.setFetchSize(3000);
			    
			    if(rs.next()){		    		
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("iCount"));  
			    }
			    rs.close();
			    pstmtData.close();			    	
		    }		    
    		iCol++;
	    	cell = row.getCell((short)iCol);
	    	cell.setCellFormula("AVERAGEA(B26:I26)");		    
		    
		    //----模具套數
	    	iRow=26;	    	
	    	row = sheet.getRow(iRow);
	    	iCol=0;
	    	
		    for(int iWeek=1422;iWeek<=1429;iWeek++) {
		    	
		    	iCol++;
		    	
				strSQL="select plan_no,count(*) iCount from ( "+
                       "select plan_no,(case when share_sh_no is null then sh_no else share_sh_no end),sum(work_plan_qty) " +
                       "  from fcmps007 where procid='300' and plan_no='"+iWeek+"ZD' "+                       
                       "group by plan_no,(case when share_sh_no is null then sh_no else share_sh_no end)) "+
                       "group by plan_no";
	
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			    rs.setFetchSize(3000);
			    
			    if(rs.next()){		    		
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("iCount"));  
			    }
			    rs.close();
			    pstmtData.close();			    	
		    }		    
    		iCol++;
	    	cell = row.getCell((short)iCol);
	    	cell.setCellFormula("AVERAGEA(B27:I27)");	
	    	
		    //----與上周相同模具套數
	    	iRow=27;	    	
	    	row = sheet.getRow(iRow);
	    	iCol=1;
	    	
		    for(int iWeek=1423;iWeek<=1429;iWeek++) {
		    	
		    	iCol++;
		    	
				strSQL="select plan_no,count(*) iCount from ( "+
                       "select plan_no,(case when share_sh_no is null then sh_no else share_sh_no end),sum(work_plan_qty) " +
                       "  from fcmps007 where procid='300' and plan_no='"+iWeek+"ZD' "+
                       "  and (case when share_sh_no is null then sh_no else share_sh_no end) IN (SELECT (case when share_sh_no is null then sh_no else share_sh_no end) FROM FCMPS007 WHERE PLAN_NO='"+(iWeek-1)+"ZD') "+
                       "group by plan_no,(case when share_sh_no is null then sh_no else share_sh_no end)) "+
                       "group by plan_no";
	
			    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			    rs=pstmtData.executeQuery();
			    
			    rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			    rs.setFetchSize(3000);
			    
			    if(rs.next()){		    		
			    	cell = row.getCell((short)iCol);
			    	JETE.setCellValue(wb, cell,rs.getInt("iCount"));  
			    }
			    rs.close();
			    pstmtData.close();			    	
		    }		    
    		iCol++;
	    	cell = row.getCell((short)iCol);
	    	cell.setCellFormula("AVERAGEA(B28:I28)");
	    	
			FileOutputStream fileOut=null;
			fileOut = new FileOutputStream(Main_Path+"/情景測試分析.xls");
			
			wb.write(fileOut);
			fileOut.close();
		    
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{
	    	
	    }
	    
	}
	
	public static void doPrint(List<CLS_RCCP_ERROR> message,String fileName) {	
		if(message.isEmpty()) return;
		
		JExportToExcel JETE=new JExportToExcel();
		HSSFWorkbook wb=JETE.getWorkbook();
		HSSFSheet sheet=wb.createSheet();
				
		String items[]=new String[] {
        		"PO#",
        		"Style Code",
        		"Style",
        		"Color",
        		"Size",
        		"QTY",
        		"Week (Factory Promised Ship Date)",
        		"Message"};
		
		try {
			
			int iRow=0;
			
	    	HSSFRow row = sheet.createRow(iRow);
	    	HSSFCell cell = row.createCell((short)0);
	    	
			for(int iCol=0;iCol<items.length;iCol++) {
				row = sheet.createRow(iRow);
		    	cell = row.createCell((short)iCol);
		    	JETE.setCellValue(wb, cell, items[iCol]);  
			}
			
			do {
				CLS_RCCP_ERROR error=message.get(iRow);
				
				row = sheet.createRow(iRow+1);
				
				short iCol=0;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getOD_PONO1());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getSTYLE_NO());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getSH_NO());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getSH_COLOR());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getSH_SIZE());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getOD_QTY());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getOD_FGDATE_WEEK());
	        	
	        	iCol++;
	    		cell = row.createCell(iCol);
	        	JETE.setCellValue(wb, cell, error.getERROR());
	        	
	        	iRow++;			
	        	
	        	if(iRow==65535) {
	        		iRow=0;
	        		sheet=wb.createSheet();
	    			for(iCol=0;iCol<items.length;iCol++) {
	    				row = sheet.createRow(iRow);
	    		    	cell = row.createCell((short)iCol);
	    		    	JETE.setCellValue(wb, cell, items[iCol]);  
	    			}	        		
	        	}
	        	
			}while(iRow<message.size());
									
	    	File file=new File(Main_Path+"/"+TEST_CODE+"/"+fileName+".xls");
	    	if(file.exists()) {
	    		file.delete();
	    	}else {
	    		file=new File(Main_Path+"/"+TEST_CODE);
	    		if(!file.exists()) {
	    			file.mkdir();
	    		}
	    	}
	    	
			FileOutputStream fileOut=null;
			fileOut = new FileOutputStream(Main_Path+"/"+TEST_CODE+"/"+fileName+".xls");
			
			wb.write(fileOut);
			fileOut.close();			
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
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
    		String URL="jdbc:oracle:thin:@10.2.6.201:1521:ficdb01";
    		String USER="dsod";
    		String PSW="dsod";
    		iRet=DriverManager.getConnection(URL,USER,PSW);
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}			

		return iRet;
	}
	
	/**
	 * 1422 周排完後需要將1422前的射出,針車更新為已排
	 * @param conn
	 * @return
	 */
	private int doBeforeAction(Connection conn) {
		int iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		
		try{

			conn.setAutoCommit(false);
			strSQL="update fcmps016 set mt_qty=org_qty";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();

			strSQL="update fcmps014 set size_qty=size_org_qty";
		
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();			
			
			strSQL="update fcmps014 set size_qty=0 where WORK_WEEK<1401";
	
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();				
			
			strSQL="update fcmps014 set size_qty=0 where WORK_WEEK>=1422";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();	
			
			strSQL="update fcmps006 set is_sure='N'";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();	
			
			conn.commit();
			
			strSQL="truncate table fcmps017";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();	
			
			strSQL="truncate table fcmps010";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();	
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	try {
	    		if(!conn.getAutoCommit())conn.rollback();
	    	}catch(Exception ex) {
	    		ex.printStackTrace();
	    	}
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	/**
	 * 1422 周排完後需要將1422前的射出,針車更新為已排
	 * @param conn
	 * @return
	 */
	private int do1422BeforeAction(Connection conn) {
		int iRet=0;
		String strSQL="";
		PreparedStatement pstmtData = null;		
		
		try{

			conn.setAutoCommit(false);
			strSQL="update fcmps010 "+
                   "set WORK_PLAN_QTY = nvl(WORK_PLAN_QTY, 0) + "+
                   "    (select work_plan_qty "+
                   "       from fcmps013 "+
                   "      where od_pono1 = fcmps010.od_pono1 "+
                   "        and sh_no = fcmps010.sh_no "+
                   "        and sh_color = fcmps010.sh_color "+
                   "        and sh_size = fcmps010.sh_size "+
                   "        and procid=fcmps010.procid) "+
                   "where (od_pono1, sh_no, sh_color, sh_size, procid) in "+
                   "(SELECT od_pono1, sh_no, sh_color, sh_size, procid "+
                   " FROM FCMPS013 "+
                   " WHERE PROCID = '100') ";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();

			strSQL="update fcmps010 "+
                   "set WORK_PLAN_QTY = nvl(WORK_PLAN_QTY, 0) + "+
                   "    (select work_plan_qty "+
                   "       from fcmps013 "+
                   "      where od_pono1 = fcmps010.od_pono1 "+
                   "        and sh_no = fcmps010.sh_no "+
                   "        and sh_color = fcmps010.sh_color "+
                   "        and sh_size = fcmps010.sh_size "+
                   "        and procid=fcmps010.procid) "+
                   "where (od_pono1, sh_no, sh_color, sh_size, procid) in "+
                   "(SELECT od_pono1, sh_no, sh_color, sh_size, procid "+
                   " FROM FCMPS013 "+
                   " WHERE PROCID = '200') ";
		
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();			
			
			strSQL="update fcmps010 "+
                   "set WORK_PLAN_QTY = nvl(WORK_PLAN_QTY, 0) + "+
                   "    (select work_plan_qty "+
                   "       from fcmps013 "+
                   "      where od_pono1 = fcmps010.od_pono1 "+
                   "        and sh_no = fcmps010.sh_no "+
                   "        and sh_color = fcmps010.sh_color "+
                   "        and sh_size = fcmps010.sh_size "+
                   "        and procid=fcmps010.procid) "+
                   "where (od_pono1, sh_no, sh_color, sh_size, procid) in "+
                   "(SELECT od_pono1, sh_no, sh_color, sh_size, procid "+
                   " FROM FCMPS013 "+
                   " WHERE PROCID = '300'  and WORK_WEEK<1422) ";
	
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    pstmtData.execute();
			pstmtData.close();				
			
			conn.commit();
			
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    	try {
	    		conn.rollback();
	    	}catch(Exception ex) {
	    		ex.printStackTrace();
	    	}
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
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
