package fcmps.ui;

import java.io.File;

public class FCMPS_ImportFOS {
	  
	  public static void main(String args[])
	  {
		  if(args.length!=3) {
			  System.out.println("請輸入正確指令: FCMPS_ImportFOS \"廠別代號\"  \"FOS文件路徑\" \"當前排產周次\" ");
			  return;
		  }
		  		   			
		  File file = new File("");	  
				  
		  if(args[0].toUpperCase().equals("DEV")) {
			  
				String path="";
				String package_path[]=FCMPS_CLS_ImportOrderFromFOS_Ex.class.getPackage().toString().split(" ");
				package_path=package_path[1].split("\\.");
				for(int i=0;i<package_path.length;i++) {
					if(!path.equals(""))path=path+"/";
					path=path+package_path[i];
				}
				  
				path=file.getAbsolutePath()+"/src/"+path;
				
				String config_xml=path+"/FICDB01.cfg.xml";

				 String FOS_File=args[1];
				 FCMPS_CLS_ImportOrderFromFOS_Ex im=new FCMPS_CLS_ImportOrderFromFOS_Ex();
				 im.setNeed_Self_Monitor(true);
				 im.setOutput(file.getAbsolutePath());
				 im.doImport(FOS_File,config_xml,3);	
			        
		  }
		  
		  if(args[0].toUpperCase().equals("FTI")) {
			 String config_xml="FTI.cfg.xml";
			 String FOS_File=args[1];
			 FCMPS_CLS_ImportOrderFromFOS_Ex im=new FCMPS_CLS_ImportOrderFromFOS_Ex();
			 im.setOutput(file.getAbsolutePath());
			 im.doImport(FOS_File,config_xml,5);
			 
			 FCMPS_CLS_ForeGenerateRccpPlan_MultiThread cls_ForeGenerateRccpPlan=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread();
			 cls_ForeGenerateRccpPlan.setFA_NO("FIC");
			 cls_ForeGenerateRccpPlan.setOutput(file.getAbsolutePath());
			 cls_ForeGenerateRccpPlan.doGeneratePlan(config_xml,Integer.valueOf(args[2]));			 
			 cls_ForeGenerateRccpPlan.doPrint();			 
		  }
		  
		  if(args[0].toUpperCase().equals("FVI")) {
				 String config_xml="FVI.cfg.xml";
				 String FOS_File=args[1];
				 FCMPS_CLS_ImportOrderFromFOS_Ex im=new FCMPS_CLS_ImportOrderFromFOS_Ex();
				 im.setOutput(file.getAbsolutePath());
				 im.doImport(FOS_File,config_xml,5);
				 
				 FCMPS_CLS_ForeGenerateRccpPlan_MultiThread cls_ForeGenerateRccpPlan=new FCMPS_CLS_ForeGenerateRccpPlan_MultiThread();
				 cls_ForeGenerateRccpPlan.setFA_NO("FVI");
				 cls_ForeGenerateRccpPlan.setOutput(file.getAbsolutePath());
				 cls_ForeGenerateRccpPlan.doGeneratePlan(config_xml,Integer.valueOf(args[2]));
				 cls_ForeGenerateRccpPlan.doPrint();
	      }
		  
	  }
}
