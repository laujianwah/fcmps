package fcmps.domain.pk;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import dsc.util.hibernate.validator.NotBlank;
@Embeddable
public class FCMPS010Pk implements Serializable {
    private java.lang.String PROCID;	//制程代號
    private java.lang.String OD_PONO1;	//訂單號碼
    private java.lang.String SH_NO;	//型體
    private java.lang.String SH_SIZE;	//SIZE碼
    private java.lang.String FA_NO;	//廠別
    private java.lang.String SH_COLOR;	//顏色
    
    public FCMPS010Pk() {
        super();
    }
    public FCMPS010Pk(java.lang.String PROCID, java.lang.String OD_PONO1, java.lang.String SH_NO, java.lang.String SH_SIZE, java.lang.String FA_NO, java.lang.String SH_COLOR) {
        super();
        this.PROCID = PROCID;
        this.OD_PONO1 = OD_PONO1;
        this.SH_NO = SH_NO;
        this.SH_SIZE = SH_SIZE;
        this.FA_NO = FA_NO;
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
     * 取得廠別
     * @returnFA_NO 廠別
     */
    @NotBlank
    @Column(name = "FA_NO", length = 5)
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
        if (!(other instanceof FCMPS010Pk))
            return false;
        FCMPS010Pk castOther = (FCMPS010Pk) other;
        return new EqualsBuilder().append(PROCID, castOther.PROCID).append(OD_PONO1, castOther.OD_PONO1).append(SH_NO, castOther.SH_NO).append(SH_SIZE, castOther.SH_SIZE).append(FA_NO, castOther.FA_NO).append(SH_COLOR, castOther.SH_COLOR).isEquals();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(PROCID).append(OD_PONO1).append(SH_NO).append(SH_SIZE).append(FA_NO).append(SH_COLOR).toHashCode();
    }
}
