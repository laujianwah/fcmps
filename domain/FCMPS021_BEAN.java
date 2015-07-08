package fcmps.domain;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.Length;

import fcmps.domain.pk.FCMPS021Pk;
import dsc.echo2app.program.Config;
import dsc.util.hibernate.validator.NotBlank;
/**
* 新接單預排計劃明細
**/
public class FCMPS021_BEAN {
    private java.lang.String PROCID;	//制程代號
    private java.lang.Integer WORK_WEEK;	//周次
    private java.lang.String OD_PONO1;	//訂單號碼
    private java.lang.String SH_NO;	//型體
    private java.lang.String SH_SIZE;	//SIZE碼
    private java.lang.String SH_COLOR;	//顏色
    private java.lang.Double PROC_SEQ;	//制程順序
    private java.lang.Double OD_QTY;	//訂單數量
    private java.lang.Double WORK_PLAN_QTY;	//計劃數量
    private java.lang.String UP_USER;	//計劃員
    private java.util.Date UP_DATE;	//異動日期
    private java.lang.String STYLE_NO;	//型體代號
    private java.lang.String FA_NO;	//廠別
    private java.lang.Double SIZE_CAP_QTY;	//SIZE產能數量
    private java.lang.Double SH_CAP_QTY;	//型體產數量
    
    private java.lang.String SHARE_SH_NO;	//共模型體
    private java.lang.String SHARE_SIZE;	//共模SIZE碼
    private java.lang.String NEED_SHOOT;	//需要射出
    
    private FCMPS021Pk pk;
    
    /**
     * 取得制程代號
     * @return PROCID 制程代號
     */
    @Id
    @NotBlank
    @Column(name = "PROCID")
    @Config(key = "FCMPS021.PROCID")
    public java.lang.String getPROCID() {
        return PROCID;
    }
    /**
     * 設定制程代號
     * @param PROCID 制程代號
     */
    public void setPROCID(java.lang.String PROCID) {
        this.PROCID = PROCID;
    }
    /**
     * 取得周次
     * @return WORK_WEEK 周次
     */
    @Id
    @NotBlank
    @Column(name = "WORK_WEEK")
    @Config(key = "FCMPS021.WORK_WEEK")
    public java.lang.Integer getWORK_WEEK() {
        return WORK_WEEK;
    }
    /**
     * 設定周次
     * @param WORK_WEEK 周次
     */
    public void setWORK_WEEK(java.lang.Integer WORK_WEEK) {
        this.WORK_WEEK = WORK_WEEK;
    }
    /**
     * 取得訂單號碼
     * @return OD_PONO1 訂單號碼
     */
    @Id
    @NotBlank
    @Column(name = "OD_PONO1")
    @Config(key = "FCMPS021.OD_PONO1")
    public java.lang.String getOD_PONO1() {
        return OD_PONO1;
    }
    /**
     * 設定訂單號碼
     * @param OD_PONO1 訂單號碼
     */
    public void setOD_PONO1(java.lang.String OD_PONO1) {
        this.OD_PONO1 = OD_PONO1;
    }
    /**
     * 取得型體
     * @return SH_NO 型體
     */
    @Id
    @NotBlank
    @Column(name = "SH_NO")
    @Config(key = "FCMPS021.SH_NO")
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
     * 取得SIZE碼
     * @return SH_SIZE SIZE碼
     */
    @Id
    @NotBlank
    @Column(name = "SH_SIZE")
    @Config(key = "FCMPS021.SH_SIZE")
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
    @Config(key = "FCMPS021.SH_COLOR")
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
     * 取得制程順序
     * @return PROC_SEQ 制程順序
     */
    @NotBlank
    @Column(name = "PROC_SEQ")
    @Config(key = "FCMPS021.PROC_SEQ")
    public java.lang.Double getPROC_SEQ() {
        return PROC_SEQ;
    }
    /**
     * 設定制程順序
     * @param PROC_SEQ 制程順序
     */
    public void setPROC_SEQ(java.lang.Double PROC_SEQ) {
        this.PROC_SEQ = PROC_SEQ;
    }
    /**
     * 取得訂單數量
     * @return OD_QTY 訂單數量
     */
    @NotBlank
    @Column(name = "OD_QTY")
    @Config(key = "FCMPS021.OD_QTY")
    public java.lang.Double getOD_QTY() {
        return OD_QTY;
    }
    /**
     * 設定訂單數量
     * @param OD_QTY 訂單數量
     */
    public void setOD_QTY(java.lang.Double OD_QTY) {
        this.OD_QTY = OD_QTY;
    }
    /**
     * 取得計劃數量
     * @return WORK_PLAN_QTY 計劃數量
     */
    @NotBlank
    @Column(name = "WORK_PLAN_QTY")
    @Config(key = "FCMPS021.WORK_PLAN_QTY")
    public java.lang.Double getWORK_PLAN_QTY() {
        return WORK_PLAN_QTY;
    }
    /**
     * 設定計劃數量
     * @param WORK_PLAN_QTY 計劃數量
     */
    public void setWORK_PLAN_QTY(java.lang.Double WORK_PLAN_QTY) {
        this.WORK_PLAN_QTY = WORK_PLAN_QTY;
    }
    /**
     * 取得計劃員
     * @return UP_USER 計劃員
     */
    @Length(max = 10)
    @Column(name = "UP_USER")
    @Config(key = "FCMPS021.UP_USER")
    public java.lang.String getUP_USER() {
        return UP_USER;
    }
    /**
     * 設定計劃員
     * @param UP_USER 計劃員
     */
    public void setUP_USER(java.lang.String UP_USER) {
        this.UP_USER = UP_USER;
    }
    /**
     * 取得異動日期
     * @return UP_DATE 異動日期
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "UP_DATE")
    @Config(key = "FCMPS021.UP_DATE")
    public java.util.Date getUP_DATE() {
        return UP_DATE;
    }
    /**
     * 設定異動日期
     * @param UP_DATE 異動日期
     */
    public void setUP_DATE(java.util.Date UP_DATE) {
        this.UP_DATE = UP_DATE;
    }
    
    /**
     * 取得型體代號
     * @return STYLE_NO 型體代號
     */

    @Length(max = 7)
    @Column(name = "STYLE_NO")
    @Config(key = "FCMPS003.STYLE_NO")
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
     * 取得廠別
     * @return FA_NO 廠別
     */
    @NotBlank
    @Length(max = 5)
    @Column(name = "FA_NO")
    @Config(key = "FCMPS006.FA_NO")
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
    
	public FCMPS021Pk getFCMPS021Pk() {
		return pk;
	}
	public void setFCMPS021Pk(FCMPS021Pk pk) {
		this.pk = pk;
	}
    
    /**
     * 取得型體產能數量
     * @return
     */
    @NotBlank
    @Column(name = "SH_CAP_QTY")
    @Config(key = "FCMPS007.SH_CAP_QTY")
	public java.lang.Double getSH_CAP_QTY() {
		return SH_CAP_QTY;
	}
		
    /**
     * 設定型體產能數量
     */
	public void setSH_CAP_QTY(java.lang.Double sh_cap_qty) {
		SH_CAP_QTY = sh_cap_qty;
	}
	
	/**
	 * 取得SIZE產能數量
	 * @return
	 */
    @Column(name = "SIZE_CAP_QTY")
    @Config(key = "FCMPS007.SIZE_CAP_QTY")
	public java.lang.Double getSIZE_CAP_QTY() {
		return SIZE_CAP_QTY;
	}
	
    /**
     * 設定SIZE產能數量
     * @param size_cap_qty
     */
	public void setSIZE_CAP_QTY(java.lang.Double size_cap_qty) {
		SIZE_CAP_QTY = size_cap_qty;
	}
    
	/**
	 * 需要射出
	 * @return
	 */
    @Column(name = "NEED_SHOOT")
    @Config(key = "FCMPS007.NEED_SHOOT")
	public java.lang.String getNEED_SHOOT() {
		return NEED_SHOOT;
	}
    /**
     * 設定需要射出
     * @param need_shoot
     */
	public void setNEED_SHOOT(java.lang.String need_shoot) {
		NEED_SHOOT = need_shoot;
	}
	
	/**
	 * 取共模型體
	 * @return
	 */
    @Column(name = "SHARE_SH_NO")
    @Config(key = "FCMPS007.SHARE_SH_NO")
	public java.lang.String getSHARE_SH_NO() {
		return SHARE_SH_NO;
	}
	
	/**
	 * 設定共模型體
	 * @param share_sh_no
	 */
	public void setSHARE_SH_NO(java.lang.String share_sh_no) {
		SHARE_SH_NO = share_sh_no;
	}
	
	/**
	 * 取共模SIZE
	 * @return
	 */
    @Column(name = "SHARE_SIZE")
    @Config(key = "FCMPS007.SHARE_SIZE")
	public java.lang.String getSHARE_SIZE() {
		return SHARE_SIZE;
	}
	
    /**
     * 設定共模SIZE
     * @param share_size
     */
	public void setSHARE_SIZE(java.lang.String share_size) {
		SHARE_SIZE = share_size;
	}
}
