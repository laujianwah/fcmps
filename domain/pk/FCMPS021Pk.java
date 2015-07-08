package fcmps.domain.pk;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import dsc.util.hibernate.validator.NotBlank;
@Embeddable
public class FCMPS021Pk implements Serializable {
    private java.lang.String PROCID;	//制程代號
    private java.lang.Integer WORK_WEEK;	//周次
    private java.lang.String OD_PONO1;	//訂單號碼
    private java.lang.String SH_NO;	//型體
    private java.lang.String SH_SIZE;	//SIZE碼
    private java.lang.String SH_COLOR;	//顏色
    public FCMPS021Pk() {
        super();
    }
    public FCMPS021Pk(java.lang.String PROCID, java.lang.Integer WORK_WEEK, java.lang.String OD_PONO1, java.lang.String SH_NO, java.lang.String SH_SIZE, java.lang.String SH_COLOR) {
        super();
        this.PROCID = PROCID;
        this.WORK_WEEK = WORK_WEEK;
        this.OD_PONO1 = OD_PONO1;
        this.SH_NO = SH_NO;
        this.SH_SIZE = SH_SIZE;
        this.SH_COLOR = SH_COLOR;
    }

    /**
     * 取得制程代號
     * @returnPROCID 制程代號
     */
    @NotBlank
    @Column(name = "PROCID", length = 3)
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
     * @returnWORK_WEEK 周次
     */
    @NotBlank
    @Column(name = "WORK_WEEK")
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
     * @returnOD_PONO1 訂單號碼
     */
    @NotBlank
    @Column(name = "OD_PONO1", length = 20)
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
     * @returnSH_NO 型體
     */
    @NotBlank
    @Column(name = "SH_NO", length = 25)
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
     * @returnSH_SIZE SIZE碼
     */
    @NotBlank
    @Column(name = "SH_SIZE", length = 4)
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
     * @returnSH_COLOR 顏色
     */
    @NotBlank
    @Column(name = "SH_COLOR", length = 25)
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
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof FCMPS021Pk))
            return false;
        FCMPS021Pk castOther = (FCMPS021Pk) other;
        return new EqualsBuilder().append(PROCID, castOther.PROCID).append(WORK_WEEK, castOther.WORK_WEEK).append(OD_PONO1, castOther.OD_PONO1).append(SH_NO, castOther.SH_NO).append(SH_SIZE, castOther.SH_SIZE).append(SH_COLOR, castOther.SH_COLOR).isEquals();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(PROCID).append(WORK_WEEK).append(OD_PONO1).append(SH_NO).append(SH_SIZE).append(SH_COLOR).toHashCode();
    }
}
