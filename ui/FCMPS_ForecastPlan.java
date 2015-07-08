package fcmps.ui;

import java.io.File;

public class FCMPS_ForecastPlan {
	  public static void main(String args[])
	  {
		  if(args.length<2) {
			  System.out.println("請輸入正確指令: FCMPS_ForecastPlan \"廠別代號\" \"當前排產周次\" ");
			  return;
		  }
		  
		  File file = new File("");	  
		  
		  if(args[0].toUpperCase().equals("FTI")) {
			 String config_xml="FTI.cfg.xml";
			 
			 FCMPS_CLS_ForeGenerateRccpPlan_MultiThread cls_ForeGenerateRccpPlan=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread();
			 cls_ForeGenerateRccpPlan.setFA_NO("FIC");
			 cls_ForeGenerateRccpPlan.setOutput(file.getAbsolutePath());
			 if(args.length==2) cls_ForeGenerateRccpPlan.doGeneratePlan(config_xml,Integer.valueOf(args[1]));
			 if(args.length==3) cls_ForeGenerateRccpPlan.doGeneratePlan(config_xml,args[1],Integer.valueOf(args[2]));
			 
			 cls_ForeGenerateRccpPlan.doPrint();			 
		  }
		  
		  if(args[0].toUpperCase().equals("FVI")) {
				 String config_xml="FVI.cfg.xml";
				 
				 FCMPS_CLS_ForeGenerateRccpPlan_MultiThread cls_ForeGenerateRccpPlan=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread();
				 cls_ForeGenerateRccpPlan.setFA_NO("FVI");
				 cls_ForeGenerateRccpPlan.setOutput(file.getAbsolutePath());
				 if(args.length==2) cls_ForeGenerateRccpPlan.doGeneratePlan(config_xml,Integer.valueOf(args[1]));
				 if(args.length==3) cls_ForeGenerateRccpPlan.doGeneratePlan(config_xml,args[1],Integer.valueOf(args[2]));
				 cls_ForeGenerateRccpPlan.doPrint();
	      }
		  
	  }
	  
	  
}
