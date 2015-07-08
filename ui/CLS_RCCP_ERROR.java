package fcmps.ui;

public class CLS_RCCP_ERROR {
	private String OD_PONO1="";
	private String SH_NO="";
	private String SH_COLOR="";
	private String SH_SIZE="";
	private String STYLE_NO="";
	private int OD_FGDATE_WEEK=-1;
	private double OD_QTY=0;
	private String ERROR="";
	private int Row=0;
	
	public String getERROR() {
		return ERROR;
	}
	public void setERROR(String error) {
		ERROR = error;
	}
	public String getOD_PONO1() {
		return OD_PONO1;
	}
	public void setOD_PONO1(String od_pono1) {
		OD_PONO1 = od_pono1;
	}
	public String getSH_COLOR() {
		return SH_COLOR;
	}
	public void setSH_COLOR(String sh_color) {
		SH_COLOR = sh_color;
	}
	public String getSH_NO() {
		return SH_NO;
	}
	public void setSH_NO(String sh_no) {
		SH_NO = sh_no;
	}
	public String getSH_SIZE() {
		return SH_SIZE;
	}
	public void setSH_SIZE(String sh_size) {
		SH_SIZE = sh_size;
	}
	public String getSTYLE_NO() {
		return STYLE_NO;
	}
	public void setSTYLE_NO(String style_no) {
		STYLE_NO = style_no;
	}
	public double getOD_QTY() {
		return OD_QTY;
	}
	public void setOD_QTY(double od_qty) {
		OD_QTY = od_qty;
	}
	public int getOD_FGDATE_WEEK() {
		return OD_FGDATE_WEEK;
	}
	public void setOD_FGDATE_WEEK(int od_fgdate_week) {
		OD_FGDATE_WEEK = od_fgdate_week;
	}
	public int getRow() {
		return Row;
	}
	public void setRow(int row) {
		Row = row;
	}
	
	
	
}
