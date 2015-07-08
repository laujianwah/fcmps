package fcmps.test;

import java.sql.Connection;
import java.util.Date;

import fcmps.ui.FCMPS_CLS_GenerateRccpPlan;

public class FCMPS_CLS_AutoGenerateRccpPlan_1503 {	
	public void do_Rccp(
			String FA_NO,
			String config_xml,
			int FORE_PLAN_WEEKS,
			Connection conn,
			int SHOOT_MIN_PRODUCE_QTY) {

		FCMPS_CLS_GenerateRccpPlan cls_GenerateRccpPlan=null;

		String PLAN_NO="1503SC_12";
	
		System.out.println("======================================開始排入1503SC================================");
		System.out.println(new Date());
		
		cls_GenerateRccpPlan=new FCMPS_CLS_GenerateRccpPlan();
    	cls_GenerateRccpPlan.setFA_NO("FIC");
    	cls_GenerateRccpPlan.setPLAN_BY_DATE("B");
    	cls_GenerateRccpPlan.setPLAN_NO(PLAN_NO);
    	cls_GenerateRccpPlan.setWORK_DAYS(5);
    	cls_GenerateRccpPlan.setWORK_WEEK(1503);
    	cls_GenerateRccpPlan.setPLAN_PRIORITY_TYPE("1");
    	cls_GenerateRccpPlan.set_ReGenerateRccpPlan(true);
    	cls_GenerateRccpPlan.setWEEK_MAX_CAP_QTY(450000);
    	cls_GenerateRccpPlan.setPROCID("100");
    	cls_GenerateRccpPlan.setIs_FORE_PLAN_WEEKS(true);
    	cls_GenerateRccpPlan.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
    	cls_GenerateRccpPlan.setConfig_xml(config_xml);
    	cls_GenerateRccpPlan.setConnection(conn);
    	cls_GenerateRccpPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
    	cls_GenerateRccpPlan.setSH_NOT_PLAN("LEIGH WEDGE");
    	
    	cls_GenerateRccpPlan.doGeneratePlan();
    	
//    	if(1==1) return;
   	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish(PLAN_NO,"Y",conn);    	    	   	
    	
    	System.out.println(new Date());
   	
    	PLAN_NO="1504ZC_12";
		System.out.println("======================================開始排入1504ZC================================");
		System.out.println(new Date());
		
		cls_GenerateRccpPlan=new FCMPS_CLS_GenerateRccpPlan();
    	cls_GenerateRccpPlan.setFA_NO("FIC");
    	cls_GenerateRccpPlan.setPLAN_BY_DATE("B");
    	cls_GenerateRccpPlan.setPLAN_NO(PLAN_NO);
    	cls_GenerateRccpPlan.setWORK_DAYS(5);
    	cls_GenerateRccpPlan.setWORK_WEEK(1504);
    	cls_GenerateRccpPlan.setPLAN_PRIORITY_TYPE("1");
    	cls_GenerateRccpPlan.set_ReGenerateRccpPlan(true);
    	cls_GenerateRccpPlan.setWEEK_MAX_CAP_QTY(50000);
    	cls_GenerateRccpPlan.setPROCID("200");
    	cls_GenerateRccpPlan.setIs_FORE_PLAN_WEEKS(true);
    	cls_GenerateRccpPlan.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
    	cls_GenerateRccpPlan.setConfig_xml(config_xml);
    	cls_GenerateRccpPlan.setConnection(conn);
    	cls_GenerateRccpPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
    	
    	cls_GenerateRccpPlan.doGeneratePlan();
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish(PLAN_NO,"Y",conn);
    	System.out.println(new Date());
    	
    	
		PLAN_NO="1504ZD_12";
		
		System.out.println("======================================開始排入1504ZD================================");
		System.out.println(new Date());
		
		cls_GenerateRccpPlan=new FCMPS_CLS_GenerateRccpPlan();
    	cls_GenerateRccpPlan.setFA_NO("FIC");
    	cls_GenerateRccpPlan.setPLAN_BY_DATE("B");
    	cls_GenerateRccpPlan.setPLAN_NO(PLAN_NO);
    	cls_GenerateRccpPlan.setWORK_DAYS(5);
    	cls_GenerateRccpPlan.setWORK_WEEK(1504);
    	cls_GenerateRccpPlan.setPLAN_PRIORITY_TYPE("1");
    	cls_GenerateRccpPlan.set_ReGenerateRccpPlan(true);
    	cls_GenerateRccpPlan.setWEEK_MAX_CAP_QTY(200000);
    	cls_GenerateRccpPlan.setPROCID("300");
    	cls_GenerateRccpPlan.setIs_FORE_PLAN_WEEKS(true);
    	cls_GenerateRccpPlan.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
    	cls_GenerateRccpPlan.setConfig_xml(config_xml);
    	cls_GenerateRccpPlan.setConnection(conn);
    	cls_GenerateRccpPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
    	//cls_GenerateRccpPlan.setSH_NO("'CROCBAND SANDAL K'");
    	cls_GenerateRccpPlan.doGeneratePlan();
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish(PLAN_NO,"Y",conn);
    	System.out.println(new Date());
    	
    	PLAN_NO="1505ZD_12";
		System.out.println("======================================開始排入1505ZD================================");
		System.out.println(new Date());
		
		cls_GenerateRccpPlan=new FCMPS_CLS_GenerateRccpPlan();
    	cls_GenerateRccpPlan.setFA_NO("FIC");
    	cls_GenerateRccpPlan.setPLAN_BY_DATE("B");
    	cls_GenerateRccpPlan.setPLAN_NO(PLAN_NO);
    	cls_GenerateRccpPlan.setWORK_DAYS(5);
    	cls_GenerateRccpPlan.setWORK_WEEK(1505);
    	cls_GenerateRccpPlan.setPLAN_PRIORITY_TYPE("1");
    	cls_GenerateRccpPlan.set_ReGenerateRccpPlan(true);
    	cls_GenerateRccpPlan.setWEEK_MAX_CAP_QTY(285000);
    	cls_GenerateRccpPlan.setPROCID("300");
    	cls_GenerateRccpPlan.setIs_FORE_PLAN_WEEKS(true);
    	cls_GenerateRccpPlan.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
    	cls_GenerateRccpPlan.setConfig_xml(config_xml);
    	cls_GenerateRccpPlan.setConnection(conn);
    	cls_GenerateRccpPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
    	//cls_GenerateRccpPlan.setSH_NO("'CROCBAND SANDAL K'");
    	cls_GenerateRccpPlan.doGeneratePlan();
    	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish(PLAN_NO,"Y",conn);    	
    	System.out.println(new Date());

	}

}
