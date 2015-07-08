package fcmps.test;

import java.io.File;

import junit.framework.TestCase;

public class FCMPS_CLS_ImportStylePartRemarkTest extends TestCase {
	String PLAN_File="F:/臨時文件/2015/20150421/FIC型体配色印.xls";
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
		
		String config_xml=path+"/FTIDB04.cfg.xml";
		
    	Import(PLAN_File,config_xml);
	        			
	}
	
	private void Import(String PLAN_File,String config_xml) {   
		FCMPS_CLS_Import_Style_Part_Remark cls_Import=new FCMPS_CLS_Import_Style_Part_Remark();
		cls_Import.setUP_USER("DEV");
		cls_Import.setConfig_xml(config_xml);
		cls_Import.doImport(PLAN_File);
	}

}
