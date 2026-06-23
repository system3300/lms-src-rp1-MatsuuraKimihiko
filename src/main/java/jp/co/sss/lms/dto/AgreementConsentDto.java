package jp.co.sss.lms.dto;

import java.util.Date;

import lombok.Data;

/**
 * 契約同意DTO
 * 
 * @author 東京ITスクール
 */
@Data
public class AgreementConsentDto {

	/** 契約同意ID */
	private Integer agreementConsentId;
	/** 契約日 */
	private Date contractDate;
	/** コース名 */
	private String courseName;
	/** 契約開始日 */
	private Date contractStartDate;
	/** 契約終了日 */
	private Date contractEndDate;

}
