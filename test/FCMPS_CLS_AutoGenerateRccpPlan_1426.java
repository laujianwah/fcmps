package fcmps.test;

import java.sql.Connection;
import java.util.Date;

import fcmps.ui.FCMPS_CLS_GenerateRccpPlan;

public class FCMPS_CLS_AutoGenerateRccpPlan_1426 {
	
	public void do_Rccp(
			String FA_NO,
			String config_xml,
			int FORE_PLAN_WEEKS,
			Connection conn,
			int SHOOT_MIN_PRODUCE_QTY
			) {				

		FCMPS_CLS_GenerateRccpPlan cls_GenerateRccpPlan=null;
		
		System.out.println("======================================開始排入1426ZD================================");
		System.out.println(new Date());
		
		cls_GenerateRccpPlan=new FCMPS_CLS_GenerateRccpPlan();
    	cls_GenerateRccpPlan.setFA_NO("FIC");
    	cls_GenerateRccpPlan.setPLAN_BY_DATE("B");
    	cls_GenerateRccpPlan.setPLAN_NO("1426ZD");
    	cls_GenerateRccpPlan.setWORK_DAYS(5);
    	cls_GenerateRccpPlan.setWORK_WEEK(1426);
    	cls_GenerateRccpPlan.setPLAN_PRIORITY_TYPE("1");
    	cls_GenerateRccpPlan.set_ReGenerateRccpPlan(true);
    	cls_GenerateRccpPlan.setWEEK_MAX_CAP_QTY(250000);
    	cls_GenerateRccpPlan.setPROCID("300");
    	cls_GenerateRccpPlan.setIs_FORE_PLAN_WEEKS(true);
    	cls_GenerateRccpPlan.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
    	cls_GenerateRccpPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
    	
//    	cls_GenerateRccpPlan.setSH_NO("LEIGH WEDGE");
    	
    	cls_GenerateRccpPlan.setConnection(conn);
    	cls_GenerateRccpPlan.setConfig_xml(config_xml);
    	
    	boolean iRet=cls_GenerateRccpPlan.doGeneratePlan();	
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPrint(cls_GenerateRccpPlan.getMessage(), "1426ZD周計劃Log");
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish("1426ZD","Y",conn);
    	
    	System.out.println("======================================開始排入1425ZC================================");
    	System.out.println(new Date());
    	
    	cls_GenerateRccpPlan=new FCMPS_CLS_GenerateRccpPlan();
    	cls_GenerateRccpPlan.setFA_NO("FIC");
    	cls_GenerateRccpPlan.setPLAN_BY_DATE("B");
    	cls_GenerateRccpPlan.setPLAN_NO("1425ZC");
    	cls_GenerateRccpPlan.setWORK_DAYS(5);
    	cls_GenerateRccpPlan.setWORK_WEEK(1425);
    	cls_GenerateRccpPlan.setPLAN_PRIORITY_TYPE("1");
    	cls_GenerateRccpPlan.set_ReGenerateRccpPlan(true);
    	cls_GenerateRccpPlan.setWEEK_MAX_CAP_QTY(100000);
    	cls_GenerateRccpPlan.setPROCID("200");
    	cls_GenerateRccpPlan.setIs_FORE_PLAN_WEEKS(true);
    	cls_GenerateRccpPlan.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
    	cls_GenerateRccpPlan.setConnection(conn);
    	cls_GenerateRccpPlan.setConfig_xml(config_xml);
    	cls_GenerateRccpPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
    	
    	iRet=cls_GenerateRccpPlan.doGeneratePlan();	
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPrint(cls_GenerateRccpPlan.getMessage(), "1425ZC周計劃Log");
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish("1425ZC","Y",conn);
    	
    	System.out.println("======================================開始排入1425SC================================");
    	System.out.println(new Date());
    	
    	cls_GenerateRccpPlan=new FCMPS_CLS_GenerateRccpPlan();
    	cls_GenerateRccpPlan.setFA_NO("FIC");
    	cls_GenerateRccpPlan.setPLAN_BY_DATE("B");
    	cls_GenerateRccpPlan.setPLAN_NO("1425SC");
    	cls_GenerateRccpPlan.setWORK_DAYS(5);
    	cls_GenerateRccpPlan.setWORK_WEEK(1425);
    	cls_GenerateRccpPlan.setPLAN_PRIORITY_TYPE("1");
    	cls_GenerateRccpPlan.set_ReGenerateRccpPlan(true);
    	cls_GenerateRccpPlan.setWEEK_MAX_CAP_QTY(450000);
    	cls_GenerateRccpPlan.setPROCID("100");
    	cls_GenerateRccpPlan.setIs_FORE_PLAN_WEEKS(true);
    	cls_GenerateRccpPlan.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);    	    	    	
    	cls_GenerateRccpPlan.setConnection(conn);
    	cls_GenerateRccpPlan.setConfig_xml(config_xml);
    	cls_GenerateRccpPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
    	
    	iRet=cls_GenerateRccpPlan.doGeneratePlan();	
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPrint(cls_GenerateRccpPlan.getMessage(), "1425SC周計劃Log");
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish("1425SC","Y",conn);
    	
    	System.out.println(new Date());

	}
}
