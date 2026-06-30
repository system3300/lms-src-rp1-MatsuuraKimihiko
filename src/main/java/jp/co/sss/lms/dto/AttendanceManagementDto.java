package jp.co.sss.lms.dto;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 勤怠管理画面用DTO
 * 
 * @author 東京ITスクール
 */
@Component
@Data
@EqualsAndHashCode(callSuper = true)
public class AttendanceManagementDto extends StudentAttendanceDto {

	/** 当日フラグ */
	private Boolean isToday;
	/** 中抜け時間（文字列） */
	private String blankTimeValue;
	/** セクション名 */
	private String sectionName;
	/** 状態 */
	private Short status;
	/** 状態(表示用) */
	private String statusName;
}
