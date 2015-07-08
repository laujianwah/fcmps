package fcmps.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

import fcmps.ui.CLS_RCCP_ERROR;
import fcmps.ui.FCMPS_CLS_GenerateRccpPlan;

public class FCMPS_CLS_AutoGenerateRccpPlan_1507 {	
	public void do_Rccp(
			String FA_NO,
			String config_xml,
			int FORE_PLAN_WEEKS,
			Connection conn,
			int SHOOT_MIN_PRODUCE_QTY) {

		FCMPS_CLS_GenerateRccpPlan cls_GenerateRccpPlan=null;

		String PLAN_NO="1525SC_LJH";
	    int WORK_WEEK=1526;
	    String PROCID="100";
	    
		System.out.println("======================================開始排入"+PLAN_NO+"================================");
		System.out.println(new Date());
		
		cls_GenerateRccpPlan=new FCMPS_CLS_GenerateRccpPlan();
    	cls_GenerateRccpPlan.setFA_NO("FIC");
    	cls_GenerateRccpPlan.setPLAN_BY_DATE("B");
    	cls_GenerateRccpPlan.setPLAN_NO(PLAN_NO);
    	cls_GenerateRccpPlan.setWORK_DAYS(5);
    	cls_GenerateRccpPlan.setWORK_WEEK(WORK_WEEK);
    	cls_GenerateRccpPlan.setPLAN_PRIORITY_TYPE("1");
    	cls_GenerateRccpPlan.set_ReGenerateRccpPlan(true);
    	cls_GenerateRccpPlan.setWEEK_MAX_CAP_QTY(350000);
    	cls_GenerateRccpPlan.setPROCID(PROCID);
    	cls_GenerateRccpPlan.setIs_FORE_PLAN_WEEKS(true);
    	cls_GenerateRccpPlan.setFORE_PLAN_WEEKS(FORE_PLAN_WEEKS);
    	cls_GenerateRccpPlan.setConfig_xml(config_xml);
    	cls_GenerateRccpPlan.setConnection(conn);
    	cls_GenerateRccpPlan.setSHOOT_MIN_PRODUCE_QTY(SHOOT_MIN_PRODUCE_QTY);
//    	cls_GenerateRccpPlan.setSH_NO("'BUMPITCLOGK'");
    	cls_GenerateRccpPlan.setSH_NOT_PLAN(getSH_NOT_PLAN_LIST(FA_NO,WORK_WEEK,PROCID,conn));
    	
    	cls_GenerateRccpPlan.doGeneratePlan();
    	
    	List<CLS_RCCP_ERROR> ls_Message=cls_GenerateRccpPlan.getMessage();
    	
    	for(CLS_RCCP_ERROR log:ls_Message) {
    		System.out.println("style:"+log.getSTYLE_NO()+ " sh:"+log.getSH_NO()+" color:"+log.getSH_COLOR()+" size:"+log.getSH_SIZE()+" err:"+log.getERROR());
    	}
    	
    	System.out.println(new Date());

/*   	
    	FCMPS_CLS_AutoGenerateRccpPlanTest.doPublish(PLAN_NO,"Y",conn);    	    	   	
*/

	}
	
	
	private String getSH_NOT_PLAN_LIST(String FA_NO,int WORK_WEEK,String PROCID,Connection conn) {
		String iRet="";
		
		String strSQL="";
		
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;		
		
		try{
			
			strSQL="select SH_NO from FCMPS029 where FA_NO='"+FA_NO+"' and  PROCID='"+PROCID+"' and WORK_WEEK="+WORK_WEEK;
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		if(!iRet.equals("")) iRet=iRet+",";
		    		iRet=iRet+rs.getString("SH_NO");
		    	}while(rs.next());
		    }
		    rs.close();
		    pstmtData.close();	
		    
		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }
		
	    return iRet;
	}

}
