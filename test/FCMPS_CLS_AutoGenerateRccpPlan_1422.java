package fcmps.test;

import java.sql.Connection;
import java.util.Date;

import fcmps.ui.FCMPS_CLS_GenerateRccpPlan;

public class FCMPS_CLS_AutoGenerateRccpPlan_1422 {	
	public void do_Rccp(
			String FA_NO,
			String config_xml,
			int FORE_PLAN_WEEKS,
			Connection conn,
			int SHOOT_MIN_PRODUCE_QTY) {

		FCMPS_CLS_GenerateRccpPlan cls_GenerateRccpPlan=null;
		
		System.out.println("======================================開始排入1422ZD================================");
		System.out.println(new Date());
		
		cls_GenerateRccpPlan=new FCMPS_CLS_GenerateRccpPlan();
    	cls_GenerateRccpPlan.setFA_NO("FIC");
    	cls_GenerateRccpPlan.setPLAN_BY_DATE("B");
    	cls_GenerateRccpPlan.setPLAN_NO("1422ZD");
    	cls_GenerateRccpPlan.setWORK_DAYS(5);
    	cls_GenerateRccpPlan.setWORK_WEEK(1422);
    	cls_GenerateRccpPlan.setPLAN_PRIORITY_TYPE("1");
    	cls_GenerateRccpPlan.set_ReGenerateRccpPlan(true);
    	cls_GenerateRccpPlan.setWEEK_MAX_CAP_QTY(343440);
    	cls_GenerateRccpPlan.setPROCID("300");
    	cls_GenerateRccpPlan.setIs_FORE_PLAN_WEEKS(true);
    	cls_GenerateRccpPlan.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
    	cls_GenerateRccpPlan.setConfig_xml(config_xml);
    	cls_GenerateRccpPlan.setConnection(conn);
    	cls_GenerateRccpPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
    	
//    	cls_GenerateRccpPlan.setSH_NO("CBSTRWRSTRPRCLG");
    	
    	boolean iRet=cls_GenerateRccpPlan.doGeneratePlan();
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPrint(cls_GenerateRccpPlan.getMessage(), "1422ZD周計劃Log");
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish("1422ZD","Y",conn);
   	
  			
		System.out.println("======================================開始排入1421ZC================================");
		System.out.println(new Date());
		
    	cls_GenerateRccpPlan=new FCMPS_CLS_GenerateRccpPlan();
    	cls_GenerateRccpPlan.setFA_NO("FIC");
    	cls_GenerateRccpPlan.setPLAN_BY_DATE("B");
    	cls_GenerateRccpPlan.setPLAN_NO("1421ZC");
    	cls_GenerateRccpPlan.setWORK_DAYS(6);
    	cls_GenerateRccpPlan.setWORK_WEEK(1421);
    	cls_GenerateRccpPlan.setPLAN_PRIORITY_TYPE("1");
    	cls_GenerateRccpPlan.set_ReGenerateRccpPlan(true);
    	cls_GenerateRccpPlan.setWEEK_MAX_CAP_QTY(120000);
    	cls_GenerateRccpPlan.setPROCID("200");
    	cls_GenerateRccpPlan.setIs_FORE_PLAN_WEEKS(true);
    	cls_GenerateRccpPlan.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
    	cls_GenerateRccpPlan.setConnection(conn);
    	cls_GenerateRccpPlan.setConfig_xml(config_xml);
    	cls_GenerateRccpPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
    	
//    	cls_GenerateRccpPlan.setSH_NO("BEACHLINESLPONM");
    	
    	iRet=cls_GenerateRccpPlan.doGeneratePlan();	
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPrint(cls_GenerateRccpPlan.getMessage(), "1421ZC周計劃Log");
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish("1421ZC","Y",conn);
	
    	System.out.println("======================================開始排入1421SC================================");
    	System.out.println(new Date());
    	
    	cls_GenerateRccpPlan=new FCMPS_CLS_GenerateRccpPlan();
    	cls_GenerateRccpPlan.setFA_NO("FIC");
    	cls_GenerateRccpPlan.setPLAN_BY_DATE("B");
    	cls_GenerateRccpPlan.setPLAN_NO("1421SC");
    	cls_GenerateRccpPlan.setWORK_DAYS(6);
    	cls_GenerateRccpPlan.setWORK_WEEK(1421);
    	cls_GenerateRccpPlan.setPLAN_PRIORITY_TYPE("1");
    	cls_GenerateRccpPlan.set_ReGenerateRccpPlan(true);
    	cls_GenerateRccpPlan.setWEEK_MAX_CAP_QTY(450000);
    	cls_GenerateRccpPlan.setPROCID("100");
    	cls_GenerateRccpPlan.setIs_FORE_PLAN_WEEKS(true);
    	cls_GenerateRccpPlan.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);    	    	    	
    	cls_GenerateRccpPlan.setConnection(conn);
    	cls_GenerateRccpPlan.setConfig_xml(config_xml);
    	cls_GenerateRccpPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
    	
//    	cls_GenerateRccpPlan.setSH_NO("CBSTRWRSTRPRCLG");
    	
    	iRet=cls_GenerateRccpPlan.doGeneratePlan();	
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPrint(cls_GenerateRccpPlan.getMessage(), "1421SC周計劃Log");
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish("1421SC","Y",conn);
    	
    	System.out.println(new Date());
    
	}

}
