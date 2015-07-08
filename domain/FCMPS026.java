package fcmps.domain;


/**
* 機臺基本資料
**/

public class FCMPS026 {
    private java.lang.String FA_NO;	//廠別
    private java.lang.String MH_NO;	//機臺編號
    private java.lang.Integer MH_STATION_NUM;	//站位
    private java.lang.Integer MH_GUN_NUM;	//槍管數
    private java.lang.String MH_WITH_OVEN;	//有烤箱
    private java.lang.Double WORK_DAYS;	//生產天數
    private java.lang.Integer WORK_WEEK;	//射出周次
    private java.lang.Double USE_DAYS;	//占用天數
    private java.lang.String MAIN_SH_NO;	//主型體
    private java.lang.String MAIN_SH_SIZE;	//主型體SIZE碼
    
    
    /**
     * 取得廠別
     * @return FA_NO 廠別
     */
    public java.lang.String getFA_NO() {
        return FA_NO;
    }
    /**
     * 設定廠別
     * @param FA_NO 廠別
     */
    public void setFA_NO(java.lang.String FA_NO) {
        this.FA_NO = FA_NO;
    }

    /**
     * 取得機臺編號
     * @return MH_NO 機臺編號
     */
    public java.lang.String getMH_NO() {
        return MH_NO;
    }
    /**
     * 設定機臺編號
     * @param MH_NO 機臺編號
     */
    public void setMH_NO(java.lang.String MH_NO) {
        this.MH_NO = MH_NO;
    }
    /**
     * 取得站位
     * @return MH_STATION_NUM 站位
     */
    public java.lang.Integer getMH_STATION_NUM() {
        return MH_STATION_NUM;
    }
    /**
     * 設定站位
     * @param MH_STATION_NUM 站位
     */
    public void setMH_STATION_NUM(java.lang.Integer MH_STATION_NUM) {
        this.MH_STATION_NUM = MH_STATION_NUM;
    }
    /**
     * 取得槍管數
     * @return MH_GUN_NUM 槍管數
     */
    public java.lang.Integer getMH_GUN_NUM() {
        return MH_GUN_NUM;
    }
    /**
     * 設定槍管數
     * @param MH_GUN_NUM 槍管數
     */
    public void setMH_GUN_NUM(Integer MH_GUN_NUM) {
        this.MH_GUN_NUM = MH_GUN_NUM;
    }
    /**
     * 取得有烤箱
     * @return MH_WITH_OVEN 有烤箱
     */
    public java.lang.String getMH_WITH_OVEN() {
        return MH_WITH_OVEN;
    }
    /**
     * 設定有烤箱
     * @param MH_WITH_OVEN 有烤箱
     */
    public void setMH_WITH_OVEN(java.lang.String MH_WITH_OVEN) {
        this.MH_WITH_OVEN = MH_WITH_OVEN;
    }
    
    /**
     * 取得生產天數
     * @return WORK_DAYS 生產天數
     */
    public java.lang.Double getWORK_DAYS() {
        return WORK_DAYS;
    }
    /**
     * 設定生產天數
     * @param WORK_DAYS 生產天數
     */
    public void setWORK_DAYS(java.lang.Double WORK_DAYS) {
        this.WORK_DAYS = WORK_DAYS;
    }

    /**
     * 取得射出周次
     * @return WORK_WEEK 射出周次
     */
    public java.lang.Integer getWORK_WEEK() {
        return WORK_WEEK;
    }
    /**
     * 設定射出周次
     * @param WORK_WEEK 射出周次
     */
    public void setWORK_WEEK(java.lang.Integer WORK_WEEK) {
        this.WORK_WEEK = WORK_WEEK;
    }
    
    /**
     * 取得占用天數
     * @return
     */
	public java.lang.Double getUSE_DAYS() {
		return USE_DAYS;
	}
	
	/**
	 * 設定占用天數
	 * @param use_days
	 */
	public void setUSE_DAYS(java.lang.Double use_days) {
		USE_DAYS = use_days;
	}
    
    /**
     * 主型體
     * @return
     */
	public java.lang.String getMAIN_SH_NO() {
		return MAIN_SH_NO;
	}
	
	/**
	 * 設定主型體
	 * @param main_sh_no
	 */
	public void setMAIN_SH_NO(java.lang.String main_sh_no) {
		MAIN_SH_NO = main_sh_no;
	}
	
	/**
	 * 主型體SIZE
	 * @return
	 */
	public java.lang.String getMAIN_SH_SIZE() {
		return MAIN_SH_SIZE;
	}
	
	/**
	 * 設定主型體SIZE
	 * @param main_sh_size
	 */
	public void setMAIN_SH_SIZE(java.lang.String main_sh_size) {
		MAIN_SH_SIZE = main_sh_size;
	}
    
}
