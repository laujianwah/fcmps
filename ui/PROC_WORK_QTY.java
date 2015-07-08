package fcmps.ui;

import org.apache.commons.lang.builder.EqualsBuilder;

public class PROC_WORK_QTY {

	private String FA_NO="";
	private String PROCID="";
	private double WORK_CAP_QTY=0;
	private double WORK_MAX_CAP_QTY=0;
	
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

	public double getWORK_MAX_CAP_QTY() {
		return WORK_MAX_CAP_QTY;
	}

	public void setWORK_MAX_CAP_QTY(double work_max_cap_qty) {
		WORK_MAX_CAP_QTY = work_max_cap_qty;
	}

	public boolean equals(String FA_NO,String PROCID,int WORK_WEEK) {
		return new EqualsBuilder().append(this.FA_NO, FA_NO).append(this.PROCID, PROCID).append(this.WORK_WEEK, WORK_WEEK).isEquals();
	}
	
}
