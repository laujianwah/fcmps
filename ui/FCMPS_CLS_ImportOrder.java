package fcmps.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;

public class FCMPS_CLS_ImportOrder implements Callable{
	
    public static final int STATUS_COMPLETED=0;
    public static final int STATUS_RUNNING=1;
    public static final int STATUS_CANCEL=2;
    public static final int STATUS_ERROR=3;
    public static final int STATUS_NOT_CONNECT_DB=4;
    public static final int STATUS_IMPORT_SUCCESS=5;
    
	private int STATUS=STATUS_RUNNING;
	
	private String OD_PONO1="";
	private String FA_NO="";
	private String STYLE_NO="";
	private double OD_QTY=0;
	private String SH_NO="";
	private String KPR="";
	private Date OD_SHIP=null;
	private Date OD_FGDATE=null;
	
	private String Branch_Code="";
	private String PO="";
	private String Status="";
	private int iRow=0;
	
	private Stack<Connection> FIC_Conn_Stack=null;
	private Stack<Connection> FCC_Conn_Stack=null;
	private Stack<Connection> FCMPS_Conn_Stack=null;
	
    private String FOS_STATUS="";
    private String UP_USER="DEV";
    
	public int getSTATUS() {	
		return STATUS;		
	}

	public void setSTATUS(int status) {
		STATUS = status;
	}
	
	public String getBranch_Code() {
		return Branch_Code;
	}

	public void setBranch_Code(String branch_Code) {
		Branch_Code = branch_Code;
	}

	public String getFA_NO() {
		return FA_NO;
	}

	public void setFA_NO(String fa_no) {
		FA_NO = fa_no;
	}


	public String getKPR() {
		return KPR;
	}

	public void setKPR(String kpr) {
		KPR = kpr;
	}


	public Date getOD_FGDATE() {
		return OD_FGDATE;
	}

	public void setOD_FGDATE(Date od_fgdate) {
		OD_FGDATE = od_fgdate;
	}

	public String getOD_PONO1() {
		return OD_PONO1;
	}


	public void setOD_PONO1(String od_pono1) {
		OD_PONO1 = od_pono1;
	}

	public double getOD_QTY() {
		return OD_QTY;
	}

	public void setOD_QTY(double od_qty) {
		OD_QTY = od_qty;
	}

	public Date getOD_SHIP() {
		return OD_SHIP;
	}

	public void setOD_SHIP(Date od_ship) {
		OD_SHIP = od_ship;
	}

	public String getPO() {
		return PO;
	}

	public void setPO(String po) {
		PO = po;
	}

	public String getSH_NO() {
		return SH_NO;
	}

	public void setSH_NO(String sh_no) {
		SH_NO = sh_no;
	}

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	public String getSTYLE_NO() {
		return STYLE_NO;
	}

	public void setSTYLE_NO(String style_no) {
		STYLE_NO = style_no;
	}

	public String getUP_USER() {
		return UP_USER;
	}

	public void setUP_USER(String up_user) {
		UP_USER = up_user;
	}
	
	public int getIRow() {
		return iRow;
	}

	public void setIRow(int row) {
		iRow = row;
	}

	public void setFOS_STATUS(String fos_status) {
		FOS_STATUS = fos_status;
	}
		
	public void setFIC_Conn_Stack(Stack<Connection> conn_Stack) {
		FIC_Conn_Stack = conn_Stack;
	}
	
	public void setFCC_Conn_Stack(Stack<Connection> conn_Stack) {
		FCC_Conn_Stack = conn_Stack;
	}
			
	public void setFCMPS_Conn_Stack(Stack<Connection> conn_Stack) {
		FCMPS_Conn_Stack = conn_Stack;
	}

	public Map<FCMPS_CLS_ImportOrder,int[]> call() {
		Map<FCMPS_CLS_ImportOrder,int[]> result=new HashMap<FCMPS_CLS_ImportOrder,int[]>();
		
		FCMPS_CLS_ImportOrderFromFOS_Var cls_var=FCMPS_CLS_ImportOrderFromFOS_Var.getInstance();
		
		String strSQL="";
		
		Connection conn=null;
		Connection FCC_conn=null;
		Connection FIC_conn=null;
		
		try {
			
			conn=FCMPS_Conn_Stack.pop();
			
			if(conn==null) {
				setSTATUS(STATUS_NOT_CONNECT_DB);	
				result.put(this, new int[] {getIRow(),getSTATUS()});
				
	    		CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setOD_PONO1(OD_PONO1);
				cls_message.setSTYLE_NO(STYLE_NO);
				cls_message.setSH_NO(SH_NO);
				cls_message.setOD_QTY(OD_QTY);
				cls_message.setRow(iRow+1);
					
				if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
				cls_message.setERROR("無法取得資料庫連線,重新導入!");
										
				cls_var.add_Message(cls_message);
				System.out.println("無法取得資料庫連線,重新導入!");
				return result;
			}
			
			if(FA_NO.equals("FIC") && Branch_Code.toUpperCase().equals("CHN")) {//FIC內銷訂單
				FCC_conn=FCC_Conn_Stack.pop();
				if(FCC_conn==null) {
					setSTATUS(STATUS_NOT_CONNECT_DB);	
					result.put(this, new int[] {getIRow(),getSTATUS()});
		    		CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
					cls_message.setOD_PONO1(OD_PONO1);
					cls_message.setSTYLE_NO(STYLE_NO);
					cls_message.setSH_NO(SH_NO);
					cls_message.setOD_QTY(OD_QTY);
					cls_message.setRow(iRow+1);
						
					if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
					cls_message.setERROR("FCC 無法取得資料庫連線,重新導入!");
											
					cls_var.add_Message(cls_message);
					System.out.println("fcc 無法取得資料庫連線,重新導入!");
					return result;
				}
			}else {
				FIC_conn=FIC_Conn_Stack.pop();
				if(FIC_conn==null) {
					setSTATUS(STATUS_NOT_CONNECT_DB);	
					result.put(this, new int[] {getIRow(),getSTATUS()});
					
		    		CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
					cls_message.setOD_PONO1(OD_PONO1);
					cls_message.setSTYLE_NO(STYLE_NO);
					cls_message.setSH_NO(SH_NO);
					cls_message.setOD_QTY(OD_QTY);
					cls_message.setRow(iRow+1);
						
					if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
					cls_message.setERROR("FIC 無法取得資料庫連線,重新導入!");
											
					cls_var.add_Message(cls_message);
					System.out.println("FIC無法取得資料庫連線,重新導入!");
					return result;
				}
			}

			
			Date stDate=new Date();
			
			PO=PO.replace(" ", "");
			 
			//FOS中廠別是FIC ,Branch Code 為 CHN 的訂單為內銷訂單, 其它均為外銷
			//FVI的FOS中也有Branch Code 為 CHN 的訂單,但FVI不分內外銷
			if(FA_NO.equals("FIC") && Branch_Code.toUpperCase().equals("CHN")) {//FIC內銷訂單
	            if(!exist_OD_IN_ERP(OD_PONO1, FCC_conn)) {
	            	 if(!exist_OD_IN_ERP(PO, FCC_conn)) {
	 	    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
						cls_message.setOD_PONO1(OD_PONO1);
						cls_message.setSTYLE_NO(STYLE_NO);
						cls_message.setSH_NO(SH_NO);
						cls_message.setOD_QTY(OD_QTY);
						cls_message.setRow(iRow+1);
						
						if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
						cls_message.setERROR("訂單系統沒有此訂單!");
												
						cls_var.add_Message(cls_message);
						
						this.setSTATUS(STATUS_COMPLETED);
						
						result.put(this, new int[] {getIRow(),getSTATUS()});
						return result;


	            	 }else {
	            		OD_PONO1=PO;
	             		if(FOS_STATUS.equals("Open")) {
	            			String PO_CancelDate=getPO_CancelDate(OD_PONO1, FCC_conn);
	            			if(!PO_CancelDate.equals("")){
	    		    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
	    						cls_message.setOD_PONO1(OD_PONO1);
	    						cls_message.setSTYLE_NO(STYLE_NO);
	    						cls_message.setSH_NO(SH_NO);
	    						cls_message.setOD_QTY(OD_QTY);
	    						cls_message.setRow(iRow+1);
	    						if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
	    						cls_message.setERROR("訂單已取消,取消日期:"+PO_CancelDate);

	    						cls_var.add_Message(cls_message);
	    						
	    						this.setSTATUS(STATUS_COMPLETED);
	    						
	    						result.put(this, new int[] {getIRow(),getSTATUS()});
	    						return result;

	            			}
	            		}
	            	 }	    			
	            }else {
	        		if(FOS_STATUS.equals("Open")) {
	        			String PO_CancelDate=getPO_CancelDate(OD_PONO1, FCC_conn);
	        			if(!PO_CancelDate.equals("")){
			    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
							cls_message.setOD_PONO1(OD_PONO1);
							cls_message.setSTYLE_NO(STYLE_NO);
							cls_message.setSH_NO(SH_NO);
							cls_message.setOD_QTY(OD_QTY);
							cls_message.setRow(iRow+1);
							if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
							cls_message.setERROR("訂單已取消,取消日期:"+PO_CancelDate);
							
							cls_var.add_Message(cls_message);
							
							this.setSTATUS(STATUS_COMPLETED);
							
							result.put(this, new int[] {getIRow(),getSTATUS()});
							return result;

	        			}
	        		}	            	
	            }
			}else {
	            if(!exist_OD_IN_ERP(OD_PONO1, FIC_conn)) {
	            	 if(!exist_OD_IN_ERP(PO, FIC_conn)) {
	 	    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
						cls_message.setOD_PONO1(OD_PONO1);
						cls_message.setSTYLE_NO(STYLE_NO);
						cls_message.setSH_NO(SH_NO);
						cls_message.setOD_QTY(OD_QTY);
						cls_message.setRow(iRow+1);
						if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
						cls_message.setERROR("訂單系統沒有此訂單!");
						
						cls_var.add_Message(cls_message);
						
						this.setSTATUS(STATUS_COMPLETED);
						
						result.put(this, new int[] {getIRow(),getSTATUS()});
						return result;
	            	 }else {
	            		OD_PONO1=PO;
	             		if(FOS_STATUS.equals("Open")) {
	            			String PO_CancelDate=getPO_CancelDate(OD_PONO1, FIC_conn);
	            			if(!PO_CancelDate.equals("")){
	    		    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
	    						cls_message.setOD_PONO1(OD_PONO1);
	    						cls_message.setSTYLE_NO(STYLE_NO);
	    						cls_message.setSH_NO(SH_NO);
	    						cls_message.setOD_QTY(OD_QTY);
	    						cls_message.setRow(iRow+1);
	    						if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
	    						cls_message.setERROR("訂單已取消,取消日期:"+PO_CancelDate);
	    						
	    						cls_var.add_Message(cls_message);
	    						
	    						this.setSTATUS(STATUS_COMPLETED);
	    						
	    						result.put(this, new int[] {getIRow(),getSTATUS()});
	    						return result;

	            			}
	            		}	            		 
	            	 }	    			
	            }else {
	        		if(FOS_STATUS.equals("Open")) {
	        			String PO_CancelDate=getPO_CancelDate(OD_PONO1, FIC_conn);
	        			if(!PO_CancelDate.equals("")){
			    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
							cls_message.setOD_PONO1(OD_PONO1);
							cls_message.setSTYLE_NO(STYLE_NO);
							cls_message.setSH_NO(SH_NO);
							cls_message.setOD_QTY(OD_QTY);
							cls_message.setRow(iRow+1);
							if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
							cls_message.setERROR("訂單已取消,取消日期:"+PO_CancelDate);
							
							cls_var.add_Message(cls_message);
							
							this.setSTATUS(STATUS_COMPLETED);
							
							result.put(this, new int[] {getIRow(),getSTATUS()});
							return result;

	        			}
	        		}	            	
	            }

			}            	
			
//			System.out.println("第一步:"+(new Date().getTime()-stDate.getTime()));
//			stDate=new Date();
			
			String MODEL_CNA="";
			if(FA_NO.equals("FIC") && Branch_Code.toUpperCase().equals("CHN")) {//FIC內銷訂單
				MODEL_CNA=getSH_NO(SH_NO, STYLE_NO, FCC_conn);
			}else {
				MODEL_CNA=getSH_NO(SH_NO, STYLE_NO, FIC_conn);
			}
						
			if(MODEL_CNA.equals("")) {
				CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setOD_PONO1(OD_PONO1);
				cls_message.setSTYLE_NO(STYLE_NO);
				cls_message.setSH_NO(SH_NO);
				cls_message.setOD_QTY(OD_QTY);
				cls_message.setRow(iRow+1);
				if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
				cls_message.setERROR("沒有對應的內部型體名稱!");
				
				cls_var.add_Message(cls_message);
				
				this.setSTATUS(STATUS_COMPLETED);
				
				result.put(this, new int[] {getIRow(),getSTATUS()});
				return result;
			}			
						
//			System.out.println("第二步:"+(new Date().getTime()-stDate.getTime()));
//			stDate=new Date();
			
			int WORK_WEEK=Integer.valueOf(WeekUtil.getWeekOfYear(new Date(),true));																		

//			System.out.println("第三步:"+(new Date().getTime()-stDate.getTime()));
//			stDate=new Date();
			
			ArrayList<String[]> ls_PROC_SEQ=getPROC_ID(MODEL_CNA,conn);
			
			if(ls_PROC_SEQ.isEmpty()) {
				CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setOD_PONO1(OD_PONO1);
				cls_message.setSTYLE_NO(STYLE_NO);
				cls_message.setSH_NO(MODEL_CNA);
				cls_message.setOD_QTY(OD_QTY);
				cls_message.setRow(iRow+1);
				if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
				cls_message.setERROR("沒有建立型體的制程或是沒有設定需要排計劃的制程!");
				
				cls_var.add_Message(cls_message);
				
				this.setSTATUS(STATUS_COMPLETED);
				
				result.put(this, new int[] {getIRow(),getSTATUS()});
				return result;
				
			}
			
//			System.out.println("第四步:"+(new Date().getTime()-stDate.getTime()));
//			stDate=new Date();			
			
			Double MIN_WEEK_CAP_QTY=FCMPS_PUBLIC.getSH_Min_Week_Cap_QTY(FA_NO, MODEL_CNA,WORK_WEEK, conn);
			
			if(MIN_WEEK_CAP_QTY.doubleValue()==0) {
				CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
				cls_message.setOD_PONO1(OD_PONO1);
				cls_message.setSTYLE_NO(STYLE_NO);
				cls_message.setSH_NO(MODEL_CNA);
				cls_message.setOD_QTY(OD_QTY);
				cls_message.setRow(iRow+1);
				if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
				cls_message.setERROR("沒有建立型體的周產能!");
				
				cls_var.add_Message(cls_message);
				
				setSTATUS(STATUS_COMPLETED);
				
				result.put(this, new int[] {getIRow(),getSTATUS()});
				return result;
			}

//			System.out.println("第五步:"+(new Date().getTime()-stDate.getTime()));
//			stDate=new Date();
			
			if(FA_NO.equals("FIC") && Branch_Code.toUpperCase().equals("CHN")) {//FIC內銷訂單
	            String msg=record_PO_Difference(OD_PONO1,MODEL_CNA,conn,FCC_conn);
	            if(!msg.equals("")) {
		    		CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
					cls_message.setOD_PONO1(OD_PONO1);
					cls_message.setSTYLE_NO(STYLE_NO);
					cls_message.setSH_NO(SH_NO);
					cls_message.setOD_QTY(OD_QTY);
					cls_message.setRow(iRow+1);
					if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
					cls_message.setERROR(msg);
					
					cls_var.add_Message(cls_message);
	            }
			}else {
	            String msg=record_PO_Difference(OD_PONO1,MODEL_CNA,conn,FIC_conn);
	            if(!msg.equals("")) {
		    		CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
					cls_message.setOD_PONO1(OD_PONO1);
					cls_message.setSTYLE_NO(STYLE_NO);
					cls_message.setSH_NO(SH_NO);
					cls_message.setOD_QTY(OD_QTY);
					cls_message.setRow(iRow+1);
					if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
					cls_message.setERROR(msg);
					
					cls_var.add_Message(cls_message);
	            }				
			}
			
//			System.out.println("第六步:"+(new Date().getTime()-stDate.getTime()));
//			stDate=new Date();
			
//			Date edDate=new Date();
//			String chk=iRow+"檢查發費:"+(edDate.getTime()-stDate.getTime());
			
			stDate=new Date();
	
			String OD_CODE="";
			if(FOS_STATUS.equals("Open")) {
				OD_CODE="N";
			}
			if(FOS_STATUS.equals("Pending")) {
				OD_CODE="P";
			}
			
			if(FOS_STATUS.equals("Closed")) {
				OD_CODE="Y";
			}
			
			String IS_REPLACEMENT=(Status.trim().toUpperCase().equals("Replacement".toUpperCase())?"Y":"N");

			PreparedStatement pstmtData = null;		
			ResultSet rs=null;
			
			PreparedStatement pstmtData2 = null;		
//			ResultSet rs2=null;
			
			PreparedStatement pstmtData3 = null;		
			ResultSet rs3=null;
						
    		String SH_NO=MODEL_CNA;    		
    		    		    		
    		ArrayList<Double> ls_PROC_SEQ2=getPROC_SEQ(SH_NO,conn);
    		
    		int WORK_WEEK_END=-1;
    		
    		//最晚開工和最晚完工周次計算優先依工廠FG Date 計算
    		//沒有FG Date 再依客人的交期計算
    		if(getOD_FGDATE()!=null) {
        		WORK_WEEK_END=Integer.valueOf(WeekUtil.getWeekOfYear(getOD_FGDATE(), true));
    		}else {
    			WORK_WEEK_END=Integer.valueOf(WeekUtil.getWeekOfYear(getOD_SHIP(), true));
    		}
	
			strSQL="select c.color_cna ,c.size_na ,b.po_type,sum(a.od_qty) od_qty "+
                   "  from tc_sap_detail a, tc_sap_main b, ficsku01 c "+
                   " where a.sku = c.sku_n(+) "+
                   "   and a.od_pono1 = b.od_pono1 "+
                   "   and b.od_pono2 = '"+getOD_PONO1()+"'"+
                   "   and c.SKU LIKE '"+STYLE_NO+"%' "+
                   "   and c.Model_Cna = '"+SH_NO+"'"+
                   " group by c.color_cna,c.size_na,b.po_type";
			
			if(getFA_NO().equals("FIC") && getBranch_Code().toUpperCase().equals("CHN")) { 
				pstmtData = FCC_conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			}else {
				pstmtData = FIC_conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			}
		    
		    rs=pstmtData.executeQuery();
		    
			if(!rs.next()) {
				rs.close();
				pstmtData.close();
				
				strSQL="select c.color_cna,c.size_na,b.po_type,sum(a.od_qty) od_qty "+
                       "  from tc_edi_detail a, tc_edi_main b, ficsku01 c "+
                       " where a.sku = c.sku(+) "+
                       "   and a.od_pono1 = b.od_pono1 "+
                       "   and b.od_pono1 = '"+getOD_PONO1()+"'"+
                       "   and c.SKU LIKE '"+STYLE_NO+"%' "+
                       "   and c.Model_Cna = '"+SH_NO+"'"+
                       " group by c.color_cna,c.size_na,b.po_type";
				if(getFA_NO().equals("FIC") && getBranch_Code().toUpperCase().equals("CHN")) { 
					pstmtData = FCC_conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				}else {
					pstmtData = FIC_conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				}
				
				 rs=pstmtData.executeQuery();
				
			}else {
				rs.beforeFirst();
			}
		    		    		    
		    if(rs.next()){

		    	String LEAN_NO="";
		    			    	
		    	if(getFA_NO().equals("FIC") && getBranch_Code().toUpperCase().equals("CHN")) { //上海是內銷,其它都是外銷
		    		LEAN_NO="SHANGHAI";
		    	}else {
		    		LEAN_NO=getLEAN_NO(getOD_PONO1(),FIC_conn);
		    	}				
				
		    	do {
		    		
		    		String SH_COLOR=FCMPS_PUBLIC.getValue(rs.getString("color_cna"));
		    		String PO_TYPE=FCMPS_PUBLIC.getValue(rs.getString("PO_TYPE"));
					String SH_SIZE=FCMPS_PUBLIC.getValue(rs.getString("size_na"));

		    		double SIZE_OD_QTY=FCMPS_PUBLIC.getDouble(rs.getDouble("OD_QTY"));		    		
		    		
		    		Double MD_PAIR_QTY=FCMPS_PUBLIC.getMD_Min_Week_Cap_QTY(getFA_NO(), SH_NO, SH_SIZE, conn,WORK_WEEK_END);
		    		if(MD_PAIR_QTY.doubleValue()==0) {
		    			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
						cls_message.setOD_PONO1(OD_PONO1);
						cls_message.setSTYLE_NO(STYLE_NO);
						cls_message.setSH_NO(MODEL_CNA);
						cls_message.setOD_QTY(OD_QTY);
						cls_message.setSH_SIZE(SH_SIZE);
						cls_message.setRow(iRow+1);
						if(OD_SHIP!=null)cls_message.setOD_FGDATE_WEEK(Integer.valueOf(WeekUtil.getWeekOfYear(OD_SHIP,true)));
						cls_message.setERROR("沒有建立模具資料!");

						cls_var.add_Message(cls_message);
					    
		    		}
		    		
		    		if(MD_PAIR_QTY.doubleValue()==0) continue;
		    		
		    		double WORK_WEEKS=(int)(SIZE_OD_QTY/MD_PAIR_QTY);
		    		double WORK_DAYS=(int)(SIZE_OD_QTY/(MD_PAIR_QTY/5));
		    		
		    		if(SIZE_OD_QTY % MD_PAIR_QTY>0) WORK_WEEKS++;
		    		if(SIZE_OD_QTY % (MD_PAIR_QTY/5)>0) WORK_DAYS++;
		    		
		    		int WORK_WEEK_LAST=WORK_WEEK_END;
		    		
		    		for(int iPROC=0;iPROC<ls_PROC_SEQ2.size();iPROC++) {
		    			
		    			WORK_WEEK_LAST=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_LAST, 1);
		    					    			
						do {
							if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(getFA_NO(), WORK_WEEK_LAST, conn)==0) {
								WORK_WEEK_LAST=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_LAST, 1);
							}else {
								break;
							}
						}while(true);
		    			
		    			List<String> ls_PROCID=getNeed_Plan_PROC(SH_NO, ls_PROC_SEQ2.get(iPROC),conn);
																				
		    			if(!ls_PROCID.isEmpty()) { //需要排周計劃
		    				
		    				int WORK_WEEK_BEGIN=WORK_WEEK_LAST;
		    				for(int iweek=0;iweek<(int)WORK_WEEKS-1;iweek++) {
		    					do {
		    						WORK_WEEK_BEGIN=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_BEGIN, 1);
		    						if(FCMPS_PUBLIC.getSys_WorkDaysOfWeek(getFA_NO(), WORK_WEEK_BEGIN, conn)==0) {
		    							WORK_WEEK_BEGIN=FCMPS_PUBLIC.getPrevious_Week(WORK_WEEK_BEGIN, 1);
		    						}else {
		    							break;
		    						}
		    					}while(true);
		    					
		    				}
		    				
		    				for(String PROCID:ls_PROCID) {
		    								    					
		    					strSQL="select OD_PONO1 from FCMPS010 " +
	    						       "where OD_PONO1='"+getOD_PONO1()+"' " +
	    						       "  and SH_NO='"+SH_NO+"' " +
	    						       "  and SH_COLOR='"+SH_COLOR+"' " +
	    						       "  and SH_SIZE='"+SH_SIZE+"' "+
	    						       "  and PROCID='"+PROCID+"' " +
	    						       "  and FA_NO='"+getFA_NO()+"' ";
	    						   
							    pstmtData3 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
							    rs3=pstmtData3.executeQuery();
								
							    if(rs3.next()){
							    	
									strSQL="update FCMPS010 set OD_QTY="+SIZE_OD_QTY;
									strSQL=strSQL+",OD_SHIP=TO_DATE('"+FCMPS_PUBLIC.getDate(getOD_SHIP(), "yyyy/MM/dd")+"','YYYY/MM/DD')";
									strSQL=strSQL+",OD_FGDATE=TO_DATE('"+FCMPS_PUBLIC.getDate(getOD_FGDATE(), "yyyy/MM/dd")+"','YYYY/MM/DD')";
									strSQL=strSQL+",OD_CODE='"+OD_CODE+"'";
									strSQL=strSQL+",UP_DATE=sysdate";
									strSQL=strSQL+",UP_USER='"+getUP_USER()+"'";
									strSQL=strSQL+",WORK_WEEK_END="+WORK_WEEK_LAST;
									strSQL=strSQL+",WORK_WEEK_START="+WORK_WEEK_BEGIN;
									strSQL=strSQL+",WORK_WEEKS="+WORK_WEEKS;
									strSQL=strSQL+",WORK_DAYS="+WORK_DAYS;
									strSQL=strSQL+",MD_PAIR_QTY="+MD_PAIR_QTY;
									strSQL=strSQL+",PROC_SEQ="+ls_PROC_SEQ2.get(iPROC);
									strSQL=strSQL+",KPR='"+getKPR()+"' ";
									strSQL=strSQL+"where OD_PONO1='"+getOD_PONO1()+"' " +
		    						              "  and SH_NO='"+SH_NO+"' " +
		    						              "  and SH_COLOR='"+SH_COLOR+"' " +
		    						              "  and SH_SIZE='"+SH_SIZE+"' "+
		    						              "  and PROCID='"+PROCID+"' " +
		    						              "  and FA_NO='"+getFA_NO()+"' " ;
								    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
								    pstmtData2.execute();
								    pstmtData2.close();
							    }else {

							    	strSQL="insert into FCMPS010 (procid, od_pono1, sh_no, sh_size, po_type,work_week_end, work_week_start, work_weeks, " +
						    		       "                      od_qty, od_ship, work_plan_qty, fa_no, sh_color, up_user, up_date, style_no, " +
						    		       "                      md_pair_qty, work_days, od_fgdate, is_disable, od_code, lean_no, proc_seq, kpr, " +
						    	     	   "                      expect_plan_qty, is_replacement, replaced_qty, e1_po) values (";
							    	strSQL=strSQL+"'"+PROCID+"'";
							    	strSQL=strSQL+",'"+getOD_PONO1()+"'";
							    	strSQL=strSQL+",'"+SH_NO+"'";
							    	strSQL=strSQL+",'"+SH_SIZE+"'";
							    	strSQL=strSQL+",'"+PO_TYPE+"'";
							    	strSQL=strSQL+",'"+WORK_WEEK_LAST+"'";
							    	strSQL=strSQL+",'"+WORK_WEEK_BEGIN+"'";
							    	strSQL=strSQL+",'"+WORK_WEEKS+"'";
							    	strSQL=strSQL+",'"+SIZE_OD_QTY+"'";
							    	strSQL=strSQL+",TO_DATE('"+FCMPS_PUBLIC.getDate(getOD_SHIP(), "yyyy/MM/dd")+"','YYYY/MM/DD')";
							    	strSQL=strSQL+",0";
							    	strSQL=strSQL+",'"+getFA_NO()+"'";
							    	strSQL=strSQL+",'"+SH_COLOR+"'";
							    	strSQL=strSQL+",'"+getUP_USER()+"'";
							    	strSQL=strSQL+",sysdate";
							    	strSQL=strSQL+",'"+getSTYLE_NO()+"'";
							    	strSQL=strSQL+",'"+MD_PAIR_QTY+"'";
							    	strSQL=strSQL+",'"+WORK_DAYS+"'";
							    	strSQL=strSQL+",TO_DATE('"+FCMPS_PUBLIC.getDate(getOD_FGDATE(), "yyyy/MM/dd")+"','YYYY/MM/DD')";
							    	if(IS_REPLACEMENT.equals("Y")) {
							    		strSQL=strSQL+",'Y'";
							    	}else {
							    		strSQL=strSQL+",'N'";
							    	}
							    	strSQL=strSQL+",'"+OD_CODE+"'";
							    	strSQL=strSQL+",'"+LEAN_NO+"'";
							    	strSQL=strSQL+",'"+ls_PROC_SEQ2.get(iPROC)+"'";
							    	strSQL=strSQL+",'"+getKPR()+"'";
							    	strSQL=strSQL+",0";
							    	strSQL=strSQL+",'"+IS_REPLACEMENT+"'";
							    	strSQL=strSQL+",0";
							    	strSQL=strSQL+",'"+PO+"'";
							    	strSQL=strSQL+")";
							    	
								    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
								    pstmtData2.execute();
								    pstmtData2.close();								    		

							    }
					    		rs3.close();
					    		pstmtData3.close();
	    								    				
	    				    }
	    				
		    			}
		    		}				    			
	    			    		
		    	}while(rs.next());
		    	
		    }
		    rs.close();
		    pstmtData.close();    					
		    		
//			edDate=new Date();
//			System.out.println(chk+" 新增發費:"+(edDate.getTime()-stDate.getTime()));
			
			setSTATUS(STATUS_IMPORT_SUCCESS);
			
		}catch(Exception ex) {
			ex.printStackTrace();
			this.setSTATUS(STATUS_ERROR);
			
			CLS_RCCP_ERROR cls_message=new CLS_RCCP_ERROR();
			cls_message.setOD_PONO1(OD_PONO1);
			cls_message.setSTYLE_NO(STYLE_NO);
			cls_message.setSH_NO(SH_NO);
			cls_message.setRow(iRow+1);
			cls_message.setERROR(ex.getMessage());

			cls_var.add_Message(cls_message);		
			
			System.out.println(strSQL);
			
		}finally {
			if(conn!=null)FCMPS_Conn_Stack.push(conn);
			if(FIC_conn!=null)FIC_Conn_Stack.push(FIC_conn);
			if(FCC_conn!=null)FCC_Conn_Stack.push(FCC_conn);
		}
		
		result.put(this, new int[] {getIRow(),getSTATUS()});
		return result;
	}
	
	/**
	 * 將客戶型體轉換為內容型體
	 * @param SH_ANO
	 * @param STYLE_NO
	 * @return
	 */
	private String getSH_NO(String SH_ANO,String STYLE_NO,Connection conn) {
		String iRet="";
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select MODEL_CNA " +
				   "from FICSKU01 " +
				   "where SKU LIKE '"+STYLE_NO+"%' and rownum=1";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=FCMPS_PUBLIC.getValue(rs.getString("MODEL_CNA"));
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
	 * 取型體的可用SIZE
	 * @param SH_ANO
	 * @param STYLE_NO
	 * @return
	 */
	private ArrayList<String[]> getSH_SIZE(String SH_NO,Connection conn) {
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		PreparedStatement pstmtData2 = null;		
		ResultSet rs2=null;
		
		String SIZE_FIELD="";
		for(int i=1;i<=40;i++) {
			if(!SIZE_FIELD.equals("")) SIZE_FIELD=SIZE_FIELD+",";
			SIZE_FIELD=SIZE_FIELD+"T"+i+",U"+i;
		}
		
		try{

			strSQL="select "+SIZE_FIELD+" from DSSH05 where SH_NO='"+SH_NO+"'";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	for(int i=1;i<=40;i++) {
		    		if(FCMPS_PUBLIC.getValue(rs.getString("U"+i)).equals("T")) {
		    			strSQL="select count(SKU) SKU from FICSKU01 where MODEL_CNA='"+SH_NO+"' and SIZE_NA='"+rs.getString("T"+i)+"'";
		    		    pstmtData2 = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    		    rs2=pstmtData2.executeQuery();
		    		    
		    		    if(rs2.next()){ //如果型體的SIZE沒有SKU,說明可能只是在開發階段有此SIZE,而量產時沒有.
		    		    	if(rs2.getInt("SKU")>0) iRet.add(new String[] {rs.getString("T"+i),String.valueOf(i)});
		    		    }
		    			rs2.close();
		    			pstmtData2.close();		    					    		
		    		}
		    	}
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
	 * 取型體需要的制程順序
	 * @param SH_NO
	 * @return
	 */
	private ArrayList<String[]> getPROC_ID(String SH_NO,Connection conn) {
		ArrayList<String[]> iRet=new ArrayList<String[]>();
		String strSQL="";
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select FCPS22_1.PB_PTNO,FCPS22_2.PB_PTNA from FCPS22_1,FCPS22_2 " +
				   "where FCPS22_1.PB_PTNO=FCPS22_2.PB_PTNO(+) " +
				   "  and FCPS22_1.SH_ARITCLE='"+SH_NO+"' " +
				   "  and FCPS22_1.NEED_PLAN='Y'";
			
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		iRet.add(new String[] {rs.getString("PB_PTNO"),rs.getString("PB_PTNA")});
		    	}while(rs.next());		    	
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
	 * 訂單是否存在於ERP訂單系統
	 * @param OD_PONO1
	 * @param conn
	 * @return
	 */
	private boolean exist_OD_IN_ERP(String OD_PONO1,Connection conn) {
		boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select count(*) iCount from DSOD00 where OD_PONO1='"+OD_PONO1+"' ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(rs.getInt("iCount")>0) iRet=true;
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
	 * 取訂單的Cancel Date
	 * @param OD_PONO1
	 * @param conn
	 * @return
	 */
	private String getPO_CancelDate(String OD_PONO1,Connection conn) {
		String iRet="";
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select TO_CHAR(CANCEL_DATE,'YYYY/MM/DD') CANCEL_DATE " +
				   "from DSOD00 where OD_PONO1='"+OD_PONO1+"' and nvl(OD_CODE,'N')='C' ";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=FCMPS_PUBLIC.getValue(rs.getString("CANCEL_DATE"));
		    	if(iRet.equals("")) iRet="empty";
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
	
	private String record_PO_Difference(String OD_PONO1,String SH_NO,Connection conn,Connection FA_conn) {
		String iRet="";
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		double org_OD_QTY=0;
		String org_OD_SHIP="";
		
		double new_OD_QTY=0;
		String new_OD_SHIP="";
		
		try{

			strSQL="SELECT to_char(OD_SHIP,'yyyy/mm/dd') OD_SHIP,SUM(OD_QTY) OD_QTY " +
				   "  FROM FCMPS010 " +
				   " WHERE OD_PONO1='"+OD_PONO1+"' " +
				   "   AND SH_NO='"+SH_NO+"' "+
				   "   AND PROCID=(SELECT MAX(PROCID) FROM FCMPS010 WHERE OD_PONO1='"+OD_PONO1+"' AND SH_NO='"+SH_NO+"')"+
				   " GROUP BY OD_SHIP";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	org_OD_QTY=rs.getDouble("OD_QTY");
		    	org_OD_SHIP=rs.getString("OD_SHIP");
		    }else {
		    	iRet=OD_PONO1+" 型體:"+SH_NO+" 為新接單";
		    }
			rs.close();
			pstmtData.close();
			
			if(!iRet.equals("")) return iRet;
			
			strSQL="select SUM(od_qty) OD_QTY,to_char(OD_SHIP,'yyyy/mm/dd') OD_SHIP " +
				   "from DSOD00 " +
				   "where OD_PONO1='"+OD_PONO1+"' " +
				   "  and SH_ARITCLENO='"+SH_NO+"' "+
				   "group by OD_SHIP ";
		    pstmtData = FA_conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	new_OD_QTY=rs.getDouble("OD_QTY");
		    	new_OD_SHIP=rs.getString("OD_SHIP");
		    }
			rs.close();
			pstmtData.close();

			if(org_OD_QTY!=new_OD_QTY) {
				iRet=OD_PONO1+" 型體:"+SH_NO+" FOS訂單數量:"+org_OD_QTY+" 訂單系統訂單數量:"+new_OD_QTY;
			}
			
			if(!org_OD_SHIP.equals(new_OD_SHIP)) {
				if(iRet.equals("")) {
					iRet=OD_PONO1+" 型體:"+SH_NO+" FOS交期:"+org_OD_SHIP+" 訂單系統交期:"+new_OD_SHIP;
				}else {
					iRet=iRet+" FOS交期:"+org_OD_SHIP+" 訂單系統交期:"+new_OD_SHIP;
				}
			}

		}catch(Exception sqlex){
	    	sqlex.printStackTrace();
	    }finally{	    	
//			Application.getApp().closeConnection(conn);
		}		
		
		return iRet;
	}
	
	/**
	 * 訂單的SIZE是否有訂單量
	 * @param OD_PONO1
	 * @param conn
	 * @return
	 */
	private boolean have_OD_QTY(String OD_PONO1,int SIZE_INDEX,Connection conn) {
		boolean iRet=false;
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select nvl(sum(S"+SIZE_INDEX+"),0) OD_QTY from DSOD_03 " +
			       "where OD_NO IN (select od_no from DSOD00 where OD_PONO1='"+OD_PONO1+"')";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	if(rs.getInt("OD_QTY")>0) iRet=true;
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
	 * 取型體需要的制程順序
	 * @param SH_NO
	 * @return
	 */
	private ArrayList<Double> getPROC_SEQ(String SH_NO,Connection conn) {
		ArrayList<Double> iRet=new ArrayList<Double>();
		String strSQL="";

		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select distinct PROC_SEQ from fcps22_1 where SH_ARITCLE='"+SH_NO+"' Order By PROC_SEQ DESC";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		if(rs.getObject("PROC_SEQ")!=null) iRet.add(rs.getDouble("PROC_SEQ"));
		    	}while(rs.next());		    	
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
	 * 型體制程是否需要排計劃<br>
	 * 因為有射出和針車,針車和組底,針車和轉印在同一周生產,且都要排計劃,故返回ArrayList <br>
	 * @param SH_NO
	 * @param PROC_SEQ 制程順序
	 * @return String 需要排計劃則返回制程代號, 否則返回空
	 */
	private ArrayList<String> getNeed_Plan_PROC(String SH_NO,double PROC_SEQ,Connection conn) {
		ArrayList<String> iRet=new ArrayList<String>();
		String strSQL="";
//		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select PB_PTNO from fcps22_1 " +
				   "where SH_ARITCLE='"+SH_NO+"' " +
				   "  and NEED_PLAN='Y'"+
				   "  and PROC_SEQ="+PROC_SEQ;
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	do {
		    		iRet.add(rs.getString("PB_PTNO"));
		    	}while(rs.next());
		    	
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
	 * 是哪個線別的訂單
	 * @param OD_PONO1
	 * @return
	 */
	private String getLEAN_NO(String OD_PONO1,Connection conn) {
		String iRet="";
		String strSQL="";
//		Connection conn =getConnection();
		PreparedStatement pstmtData = null;		
		ResultSet rs=null;
		
		try{

			strSQL="select nvl(lean_no, '無') lean_no "+
                   "from dsod00, fcpb07 "+
                   "where dsod00.od_pono1 = '"+OD_PONO1+"'"+
                   "  and dsod00.cu_dest = fcpb07.cu_dest(+) "+
                   "  and rownum=1";
		    pstmtData = conn.prepareStatement(strSQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		    rs=pstmtData.executeQuery();
		    
		    if(rs.next()){
		    	iRet=FCMPS_PUBLIC.getValue(rs.getString("lean_no"));
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
