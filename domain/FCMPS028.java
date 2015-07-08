package fcmps.domain;
import javax.persistence.Column;
import javax.persistence.Id;

import org.hibernate.validator.Length;

import dsc.echo2app.program.Config;
import dsc.util.hibernate.validator.NotBlank;
/**
* 周計劃機臺安排
**/

public class FCMPS028 {
    private java.lang.String FA_NO;	//廠別
    private java.lang.String MAIN_SH_NO;	//主型體
    private java.lang.String SH_NO;	//型體
    private java.lang.String STYLE_NO;	//型體代號
    private java.lang.String PART_NO;	//部位
    private java.lang.String MH_NO;	//機臺編號
    private java.lang.Integer WORK_WEEK;	//射出周次
    private java.lang.String SH_SIZE;	//SIZE碼
    private java.lang.String MAIN_SH_SIZE;	//主型體SIZE碼
    private java.lang.String SH_COLOR;	//顏色
    private java.lang.Integer WORK_PLAN_QTY;	//計劃數量
    private java.lang.Integer MD_NUM;	//模具數量
    private java.lang.Double MD_PER_QTY;	//模具雙數
    private java.lang.Double WORK_DAYS;	//生產天數
    private java.lang.Integer MH_STATION_NUM;	//站位
    private java.lang.Integer MH_GUN_NUM;	//槍管數
    private java.lang.String MH_WITH_OVEN;	//有烤箱

    /**
     * 取得廠別
     * @return FA_NO 廠別
     */
    @NotBlank
    @Length(max = 5)
    @Column(name = "FA_NO")
    @Config(key = "FCMPS028.FA_NO")
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
     * 取得型體
     * @return SH_NO 型體
     */
    @Id
    @NotBlank
    @Column(name = "SH_NO")
    @Config(key = "FCMPS028.SH_NO")
    public java.lang.String getSH_NO() {
        return SH_NO;
    }
    /**
     * 設定型體
     * @param SH_NO 型體
     */
    public void setSH_NO(java.lang.String SH_NO) {
        this.SH_NO = SH_NO;
    }
    /**
     * 取得型體代號
     * @return STYLE_NO 型體代號
     */
    @NotBlank
    @Length(max = 5)
    @Column(name = "STYLE_NO")
    @Config(key = "FCMPS028.STYLE_NO")
    public java.lang.String getSTYLE_NO() {
        return STYLE_NO;
    }
    /**
     * 設定型體代號
     * @param STYLE_NO 型體代號
     */
    public void setSTYLE_NO(java.lang.String STYLE_NO) {
        this.STYLE_NO = STYLE_NO;
    }
    /**
     * 取得部位
     * @return PART_NO 部位
     */
    @Id
    @NotBlank
    @Column(name = "PART_NO")
    @Config(key = "FCMPS028.PART_NO")
    public java.lang.String getPART_NO() {
        return PART_NO;
    }
    /**
     * 設定部位
     * @param PART_NO 部位
     */
    public void setPART_NO(java.lang.String PART_NO) {
        this.PART_NO = PART_NO;
    }
    /**
     * 取得機臺編號
     * @return MH_NO 機臺編號
     */
    @Id
    @NotBlank
    @Column(name = "MH_NO")
    @Config(key = "FCMPS028.MH_NO")
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
     * 取得射出周次
     * @return WORK_WEEK 射出周次
     */
    @Id
    @NotBlank
    @Column(name = "WORK_WEEK")
    @Config(key = "FCMPS028.WORK_WEEK")
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
     * 取得SIZE碼
     * @return SH_SIZE SIZE碼
     */
    @Id
    @NotBlank
    @Column(name = "SH_SIZE")
    @Config(key = "FCMPS028.SH_SIZE")
    public java.lang.String getSH_SIZE() {
        return SH_SIZE;
    }
    /**
     * 設定SIZE碼
     * @param SH_SIZE SIZE碼
     */
    public void setSH_SIZE(java.lang.String SH_SIZE) {
        this.SH_SIZE = SH_SIZE;
    }
    /**
     * 取得顏色
     * @return SH_COLOR 顏色
     */
    @Id
    @NotBlank
    @Column(name = "SH_COLOR")
    @Config(key = "FCMPS028.SH_COLOR")
    public java.lang.String getSH_COLOR() {
        return SH_COLOR;
    }
    /**
     * 設定顏色
     * @param SH_COLOR 顏色
     */
    public void setSH_COLOR(java.lang.String SH_COLOR) {
        this.SH_COLOR = SH_COLOR;
    }
    /**
     * 取得計劃數量
     * @return WORK_PLAN_QTY 計劃數量
     */
    @NotBlank
    @Column(name = "WORK_PLAN_QTY")
    @Config(key = "FCMPS028.WORK_PLAN_QTY")
    public java.lang.Integer getWORK_PLAN_QTY() {
        return WORK_PLAN_QTY;
    }
    /**
     * 設定計劃數量
     * @param WORK_PLAN_QTY 計劃數量
     */
    public void setWORK_PLAN_QTY(java.lang.Integer WORK_PLAN_QTY) {
        this.WORK_PLAN_QTY = WORK_PLAN_QTY;
    }
    /**
     * 取得模具數量
     * @return MD_NUM 模具數量
     */
    @NotBlank
    @Column(name = "MD_NUM")
    @Config(key = "FCMPS028.MD_NUM")
    public java.lang.Integer getMD_NUM() {
        return MD_NUM;
    }
    /**
     * 設定模具數量
     * @param MD_NUM 模具數量
     */
    public void setMD_NUM(java.lang.Integer MD_NUM) {
        this.MD_NUM = MD_NUM;
    }
    /**
     * 取得模具雙數
     * @return MD_PER_QTY 模具雙數
     */
    @NotBlank
    @Column(name = "MD_PER_QTY")
    @Config(key = "FCMPS028.MD_PER_QTY")
    public java.lang.Double getMD_PER_QTY() {
        return MD_PER_QTY;
    }
    /**
     * 設定模具雙數
     * @param MD_PER_QTY 模具雙數
     */
    public void setMD_PER_QTY(java.lang.Double MD_PER_QTY) {
        this.MD_PER_QTY = MD_PER_QTY;
    }

    /**
     * 取得生產天數
     * @return WORK_DAYS 生產天數
     */
    @NotBlank
    @Column(name = "WORK_DAYS")
    @Config(key = "FCMPS028.WORK_DAYS")
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
     * 取得站位
     * @return MH_STATION_NUM 站位
     */
    @NotBlank
    @Column(name = "MH_STATION_NUM")
    @Config(key = "FCMPS028.MH_STATION_NUM")
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
    @NotBlank
    @Column(name = "MH_GUN_NUM")
    @Config(key = "FCMPS028.MH_GUN_NUM")
    public java.lang.Integer getMH_GUN_NUM() {
        return MH_GUN_NUM;
    }
    /**
     * 設定槍管數
     * @param MH_GUN_NUM 槍管數
     */
    public void setMH_GUN_NUM(java.lang.Integer MH_GUN_NUM) {
        this.MH_GUN_NUM = MH_GUN_NUM;
    }
    /**
     * 取得有烤箱
     * @return MH_WITH_OVEN 有烤箱
     */
    @NotBlank
    @Length(max = 1)
    @Column(name = "MH_WITH_OVEN")
    @Config(key = "FCMPS028.MH_WITH_OVEN")
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
