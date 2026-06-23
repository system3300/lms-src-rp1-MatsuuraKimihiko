package jp.co.sss.lms.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class BulkRegistForm {

	/** 会場名 */
	private String placeName;

	/** 備考 */
	private String placeNote;

	/** 会場ID */
	private Integer placeId;
	@NotNull(message = "期間(From)は必須です")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date searchPeriodFrom;
	@NotNull(message = "期間(To)は必須です")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date searchPeriodTo;

	/** 勤怠一覧 */
	private List<DailyAttendanceForm> dailyAttendanceFormList;

	public List<DailyAttendanceForm> getDailyAttendanceFormList() {
		if (this.dailyAttendanceFormList == null) {
			this.dailyAttendanceFormList = new ArrayList<>();
		}
		return this.dailyAttendanceFormList;
	}

}