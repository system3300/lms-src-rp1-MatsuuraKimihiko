package jp.co.sss.lms.dto;

import lombok.Data;

// Task.57
@Data
public class AttendanceStudentDto {

	private Integer lmsUserId;

	private Integer userId;

	private String userName;

	private String courseName;

	private String companyName;

	private String placeName;
	private boolean notEnterFlg;

	private Integer notEnterCount;

	public boolean isNotEnterFlg() {
		return notEnterFlg;
	}

	public void setNotEnterFlg(boolean notEnterFlg) {
		this.notEnterFlg = notEnterFlg;
	}
}