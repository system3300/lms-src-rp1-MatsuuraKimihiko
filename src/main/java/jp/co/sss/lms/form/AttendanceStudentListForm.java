package jp.co.sss.lms.form;

import lombok.Data;

@Data
public class AttendanceStudentListForm {
	/** コースID */
	private Integer courseId;
	/** コース名 */
	private String courseName;
	/** 企業ID */
	private Integer companyId;
	/** 企業名 */
	private String companyName;
	/** ユーザー名 */
	private String userName;
	/** 過去フラグ */
	private Integer pastFlg;
	/** LMSユーザーID */
	private Integer lmsUserId;
	/** 会場名 */
	private String placeName;
}