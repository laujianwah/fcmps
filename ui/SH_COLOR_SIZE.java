package fcmps.ui;

import org.apache.commons.lang.builder.EqualsBuilder;

public class SH_COLOR_SIZE {

	private String FA_NO="";
	private String PROCID="";
	private double WORK_CAP_QTY=0;
	private String SH_NO="";
	private String SH_COLOR="";
	private String SH_SIZE="";
	private int WORK_WEEK=-1;
	private double WORK_PLANNED_QTY=0;
		
	
	public String getFA_NO() {
		return FA_NO;
	}
	
	public void setFA_NO(String fa_no) {
		FA_NO = fa_no;
	}
	
	public String getPROCID() {
		return PROCID;
	}
	
	public void setPROCID(String procid) {
		PROCID = procid;
	}
	
	public double getWORK_CAP_QTY() {
		return WORK_CAP_QTY;
	}
	
	public void setWORK_CAP_QTY(double work_cap_qty) {
		WORK_CAP_QTY = work_cap_qty;
	}

	public String getSH_NO() {
		return SH_NO;
	}

	public void setSH_NO(String sh_no) {
		SH_NO = sh_no;
	}

	public int getWORK_WEEK() {
		return WORK_WEEK;
	}

	public void setWORK_WEEK(int work_week) {
		WORK_WEEK = work_week;
	}

	public double getWORK_PLANNED_QTY() {
		return WORK_PLANNED_QTY;
	}

	public void setWORK_PLANNED_QTY(double work_planned_qty) {
		WORK_PLANNED_QTY = work_planned_qty;
	}

	public String getSH_COLOR() {
		return SH_COLOR;
	}

	public void setSH_COLOR(String sh_color) {
		SH_COLOR = sh_color;
	}	
	
	public String getSH_SIZE() {
		return SH_SIZE;
	}

	public void setSH_SIZE(String sh_size) {
		SH_SIZE = sh_size;
	}
			
	public boolean equals(String FA_NO,String PROCID,String SH_NO,String SH_COLOR) {
		return new EqualsBuilder().append(this.FA_NO, FA_NO).append(this.PROCID, PROCID).append(this.SH_NO, SH_NO).append(this.SH_COLOR, SH_COLOR).isEquals();
	}
	
}
