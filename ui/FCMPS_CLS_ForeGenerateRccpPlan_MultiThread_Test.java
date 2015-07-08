package fcmps.ui;

import java.io.File;

public class FCMPS_CLS_ForeGenerateRccpPlan_MultiThread_Test {
	  public static void main(String args[])
	  {
		  
		  File file = new File("");	  
		  String path="";
		  String package_path[]=FCMPS_CLS_ForeGenerateRccpPlan_MultiThread.class.getPackage().toString().split(" ");
		  package_path=package_path[1].split("\\.");
		  for(int i=0;i<package_path.length;i++) {
			 if(!path.equals(""))path=path+"/";
			 path=path+package_path[i];
		  }
			  
		  path=file.getAbsolutePath()+"/src/"+path;
			
		  String config_xml=path+"/FTI.cfg.xml";
			
		  int CURRENT_PLAN_WEEK=1502;
		 
		  FCMPS_CLS_ForeGenerateRccpPlan_MultiThread cls_ForeGenerateRccpPlan=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread();
		  cls_ForeGenerateRccpPlan.setFA_NO("FIC");
		  cls_ForeGenerateRccpPlan.doGeneratePlan(config_xml, CURRENT_PLAN_WEEK);
		 
	  }

}
