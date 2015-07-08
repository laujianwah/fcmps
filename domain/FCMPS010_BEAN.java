package fcmps.domain;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.Length;

import fcmps.domain.pk.FCMPS010Pk;
import dsc.echo2app.program.Config;
import dsc.util.hibernate.validator.NotBlank;
/**
* 新接訂單分析
**/
public class FCMPS010_BEAN {
    private java.lang.String PROCID;	//制程代號
    private java.lang.String OD_PONO1;	//訂單號碼
    private java.lang.String SH_NO;	//型體
    private java.lang.String SH_SIZE;	//SIZE碼
    private java.lang.Integer WORK_WEEK_END;	//最晚完工周次
    private java.lang.Integer WORK_WEEK_START;	//最晚開工周次
    private java.lang.Double WORK_WEEKS;	//生產周期
    private java.lang.Double OD_QTY;	//訂單數量
    private java.util.Date OD_SHIP;	//訂單交期
    private java.lang.Double WORK_PLAN_QTY;	//計劃數量
    private java.lang.Double EXPECT_PLAN_QTY;	//預排數量
    private java.lang.Double REPLACED_QTY;	//替代數量
    private java.lang.String FA_NO;	//廠別
    private java.lang.String SH_COLOR;	//顏色
    private java.lang.String UP_USER;	//異動人員
    private java.util.Date UP_DATE;	//異動日期
    private java.lang.String STYLE_NO;	//型體代號
    private java.lang.Double MD_PAIR_QTY;	//模具雙數
    private java.lang.Double WORK_DAYS;	//生產天數
    private java.util.Date OD_FGDATE;	//預計交貨日期
    private java.lang.String IS_DISABLE;	//禁止排產
    private java.lang.String OD_CODE;	//訂單狀況
    private java.lang.String LEAN_NO;	//線別
    private java.lang.Double PROC_SEQ;	//制程順序
    private java.lang.String KPR;	//是否為KPR訂單
    private java.lang.String E1_PO;	//E1 PO#
    private java.lang.String IS_REPLACEMENT;	//E1 PO#
    private java.lang.String PO_TYPE;	//PO TYPE
    
    private FCMPS010Pk pk;
    
    /**
     * 取得制程代號
     * @return PROCID 制程代號
     */
    @Id
    @NotBlank
    @Column(name = "PROCID")
    @Config(key = "FCMPS010.PROCID")
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
     * 取得訂單號碼
     * @return OD_PONO1 訂單號碼
     */
    @Id
    @NotBlank
    @Column(name = "OD_PONO1")
    @Config(key = "FCMPS010.OD_PONO1")
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
    @Config(key = "FCMPS010.SH_NO")
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
    @Config(key = "FCMPS010.SH_SIZE")
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
     * 取得最晚完工周次
     * @return WORK_WEEK_END 最晚完工周次
     */
    @NotBlank
    @Column(name = "WORK_WEEK_END")
    @Config(key = "FCMPS010.WORK_WEEK_END")
    public java.lang.Integer getWORK_WEEK_END() {
        return WORK_WEEK_END;
    }
    /**
     * 設定最晚完工周次
     * @param WORK_WEEK_END 最晚完工周次
     */
    public void setWORK_WEEK_END(java.lang.Integer WORK_WEEK_END) {
        this.WORK_WEEK_END = WORK_WEEK_END;
    }
    /**
     * 取得最晚開工周次
     * @return WORK_WEEK_START 最晚開工周次
     */
    @NotBlank
    @Column(name = "WORK_WEEK_START")
    @Config(key = "FCMPS010.WORK_WEEK_START")
    public java.lang.Integer getWORK_WEEK_START() {
        return WORK_WEEK_START;
    }
    /**
     * 設定最晚開工周次
     * @param WORK_WEEK_START 最晚開工周次
     */
    public void setWORK_WEEK_START(java.lang.Integer WORK_WEEK_START) {
        this.WORK_WEEK_START = WORK_WEEK_START;
    }
    /**
     * 取得生產周期
     * @return WORK_WEEKS 生產周期
     */
    @NotBlank
    @Column(name = "WORK_WEEKS")
    @Config(key = "FCMPS010.WORK_WEEKS")
    public java.lang.Double getWORK_WEEKS() {
        return WORK_WEEKS;
    }
    /**
     * 設定生產周期
     * @param WORK_WEEKS 生產周期
     */
    public void setWORK_WEEKS(java.lang.Double WORK_WEEKS) {
        this.WORK_WEEKS = WORK_WEEKS;
    }
    /**
     * 取得訂單數量
     * @return OD_QTY 訂單數量
     */
    @NotBlank
    @Column(name = "OD_QTY")
    @Config(key = "FCMPS010.OD_QTY")
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
     * 取得訂單交期
     * @return OD_SHIP 訂單交期
     */
    @NotBlank
    @Temporal(TemporalType.DATE)
    @Column(name = "OD_SHIP")
    @Config(key = "FCMPS010.OD_SHIP")
    public java.util.Date getOD_SHIP() {
        return OD_SHIP;
    }
    /**
     * 設定訂單交期
     * @param OD_SHIP 訂單交期
     */
    public void setOD_SHIP(java.util.Date OD_SHIP) {
        this.OD_SHIP = OD_SHIP;
    }
    /**
     * 取得計劃數量
     * @return WORK_PLAN_QTY 計劃數量
     */
    @NotBlank
    @Column(name = "WORK_PLAN_QTY")
    @Config(key = "FCMPS010.WORK_PLAN_QTY")
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
     * 取得廠別
     * @return FA_NO 廠別
     */
    @Id
    @NotBlank
    @Column(name = "FA_NO")
    @Config(key = "FCMPS010.FA_NO")
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
     * 取得顏色
     * @return SH_COLOR 顏色
     */
    @Id
    @NotBlank
    @Column(name = "SH_COLOR")
    @Config(key = "FCMPS010.SH_COLOR")
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
     * 取得異動人員
     * @return UP_USER 異動人員
     */
    @Length(max = 10)
    @Column(name = "UP_USER")
    @Config(key = "FCMPS010.UP_USER")
    public java.lang.String getUP_USER() {
        return UP_USER;
    }
    /**
     * 設定異動人員
     * @param UP_USER 異動人員
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
    @Config(key = "FCMPS010.UP_DATE")
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
    @Config(key = "FCMPS010.STYLE_NO")
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
     * 取得模具雙數
     * @return MD_PAIR_QTY 模具雙數
     */
    @Column(name = "MD_PAIR_QTY")
    @Config(key = "FCMPS010.MD_PAIR_QTY")
    public java.lang.Double getMD_PAIR_QTY() {
        return MD_PAIR_QTY;
    }
    /**
     * 設定模具雙數
     * @param MD_PAIR_QTY 模具雙數
     */
    public void setMD_PAIR_QTY(java.lang.Double MD_PAIR_QTY) {
        this.MD_PAIR_QTY = MD_PAIR_QTY;
    }
    /**
     * 取得生產天數
     * @return WORK_DAYS 生產天數
     */
    @Column(name = "WORK_DAYS")
    @Config(key = "FCMPS010.WORK_DAYS")
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
     * 取得預計交貨日期
     * @return OD_FGDATE 預計交貨日期
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "OD_FGDATE")
    @Config(key = "FCMPS010.OD_FGDATE")
    public java.util.Date getOD_FGDATE() {
        return OD_FGDATE;
    }
    /**
     * 設定預計交貨日期
     * @param OD_FGDATE 預計交貨日期
     */
    public void setOD_FGDATE(java.util.Date OD_FGDATE) {
        this.OD_FGDATE = OD_FGDATE;
    }
    /**
     * 取得禁止排產
     * @return IS_DISABLE 禁止排產
     */
    @Length(max = 1)
    @Column(name = "IS_DISABLE")
    @Config(key = "FCMPS010.IS_DISABLE")
    public java.lang.String getIS_DISABLE() {
        return IS_DISABLE;
    }
    /**
     * 設定禁止排產
     * @param IS_DISABLE 禁止排產
     */
    public void setIS_DISABLE(java.lang.String IS_DISABLE) {
        this.IS_DISABLE = IS_DISABLE;
    }
    /**
     * 取得訂單狀況
     * @return OD_CODE 訂單狀況
     */
    @Length(max = 1)
    @Column(name = "OD_CODE")
    @Config(key = "FCMPS010.OD_CODE")
    public java.lang.String getOD_CODE() {
        return OD_CODE;
    }
    /**
     * 設定訂單狀況
     * @param OD_CODE 訂單狀況
     */
    public void setOD_CODE(java.lang.String OD_CODE) {
        this.OD_CODE = OD_CODE;
    }
    /**
     * 取得線別
     * @return LEAN_NO 線別
     */
    @Length(max = 12)
    @Column(name = "LEAN_NO")
    @Config(key = "FCMPS010.LEAN_NO")
    public java.lang.String getLEAN_NO() {
        return LEAN_NO;
    }
    /**
     * 設定線別
     * @param LEAN_NO 線別
     */
    public void setLEAN_NO(java.lang.String LEAN_NO) {
        this.LEAN_NO = LEAN_NO;
    }
    
    /**
     * 取得制程順序
     * @return PROC_SEQ 制程順序
     */
    @NotBlank
    @Column(name = "PROC_SEQ")
    @Config(key = "FCPS22_1.PROC_SEQ")
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
     * 是否為KPR訂單
     * @return
     */
    @Column(name = "KPR")
    @Config(key = "FCMPS010.KPR")
	public java.lang.String getKPR() {
		return KPR;
	}
    
    /**
     * 設定是否為KPR訂單 
     * @param kpr
     */
	public void setKPR(java.lang.String kpr) {
		KPR = kpr;
	}
	public FCMPS010Pk getFCMPS010Pk() {
		return pk;
	}
	public void setFCMPS010Pk(FCMPS010Pk pk) {
		this.pk = pk;
	}
	
    @NotBlank
    @Column(name = "EXPECT_PLAN_QTY")
    @Config(key = "FCMPS010.EXPECT_PLAN_QTY")
    /**
     * 取得預排數
     */
	public java.lang.Double getEXPECT_PLAN_QTY() {
		return EXPECT_PLAN_QTY;
	}
    
    /**
     * 設定預排數
     * @param expect_plan_qty
     */
	public void setEXPECT_PLAN_QTY(java.lang.Double expect_plan_qty) {
		EXPECT_PLAN_QTY = expect_plan_qty;
	}
	
    @NotBlank
    @Column(name = "REPLACED_QTY")
    @Config(key = "FCMPS010.REPLACED_QTY")
    /**
     * 取得替代數量
     */
	public java.lang.Double getREPLACED_QTY() {
		return REPLACED_QTY;
	}
    
    /**
     * 設定替代數量
     * @param expect_plan_qty
     */
	public void setREPLACED_QTY(java.lang.Double replaced_qty) {
		REPLACED_QTY = replaced_qty;
	}
	
	/**
	 * 取E1 PO#
	 * @return
	 */
    @Column(name = "E1_PO")
    @Config(key = "FCMPS010.E1_PO")
	public java.lang.String getE1_PO() {
		return E1_PO;
	}
	
	/**
	 * 設定E1 PO#
	 * @param e1_po
	 */
	public void setE1_PO(java.lang.String e1_po) {
		E1_PO = e1_po;
	}
	
	/**
	 * 是否替代訂單
	 * @return
	 */
    @Column(name = "IS_REPLACEMENT")
    @Config(key = "FCMPS010.IS_REPLACEMENT")
	public java.lang.String getIS_REPLACEMENT() {
		return IS_REPLACEMENT;
	}
	
	/**
	 * 設定是否替代訂單
	 * @param is_replacement
	 */
	public void setIS_REPLACEMENT(java.lang.String is_replacement) {
		IS_REPLACEMENT = is_replacement;
	}
	
    @Column(name = "PO_TYPE")
    @Config(key = "DSOD00.PO_TYPE")
	public java.lang.String getPO_TYPE() {
		return PO_TYPE;
	}
	public void setPO_TYPE(java.lang.String po_type) {
		PO_TYPE = po_type;
	}
    
    
    
}
