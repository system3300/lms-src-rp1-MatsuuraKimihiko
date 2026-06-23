package jp.co.sss.lms.form;

import lombok.Data;

@Data
public class AttendanceStudentListForm {

	private Integer courseId;

	private String courseName;

	private Integer companyId;

	private String companyName;

	private String userName;

	private Integer pastFlg;
}