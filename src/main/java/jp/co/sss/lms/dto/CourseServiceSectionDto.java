package jp.co.sss.lms.dto;

import java.util.Date;

import lombok.Data;

/**
 * コース情報サービス セクションDTO
 * 
 * @author 東京ITスクール
 *
 */
@Data
public class CourseServiceSectionDto {

	/** セクションID */
	private Integer sectionId;
	/** セクション名 */
	private String sectionName;
	/** 日付 */
	private Date date;

}
