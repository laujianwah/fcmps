package fcmps.domain;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.Length;

import dsc.echo2app.program.Config;
import dsc.util.hibernate.validator.NotBlank;
import fcmps.domain.pk.FCMPS0101Pk;
/**
* 新接訂單射出部位明細
**/

public class FCMPS0101_BEAN {
    private java.lang.String PROCID;	//制程代號
    private java.lang.String OD_PONO1;	//訂單號碼
    private java.lang.String SH_NO;	//型體
    private java.lang.String SH_SIZE;	//SIZE碼
    private java.lang.Double OD_QTY;	//訂單數量
    private java.lang.String FA_NO;	//廠別
    private java.lang.String SH_COLOR;	//顏色
    private java.lang.String UP_USER;	//異動人員
    private java.util.Date UP_DATE;	//異動日期
    private java.lang.String STYLE_NO;	//型體代號
    private java.lang.String PART_NO;	//部位代號
    private java.lang.Double PROC_SEQ;	//制程順序
    
    private FCMPS0101Pk pk;
    
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
    @Length(max = 5)
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
     * 取得部位代號
     * @return PART_NO 部位代號
     */
    @Id
    @NotBlank
    @Column(name = "PART_NO")
    @Config(key = "FCMPS010.PART_NO")
    public java.lang.String getPART_NO() {
        return PART_NO;
    }
    /**
     * 設定部位代號
     * @param PART_NO 部位代號
     */
    public void setPART_NO(java.lang.String PART_NO) {
        this.PART_NO = PART_NO;
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
    
	public FCMPS0101Pk getFCMPS0101Pk() {
		return pk;
	}
	public void setFCMPS0101Pk(FCMPS0101Pk pk) {
		this.pk = pk;
	}
}
