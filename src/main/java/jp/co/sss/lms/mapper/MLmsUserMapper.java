package jp.co.sss.lms.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.sss.lms.dto.AttendanceStudentDto;
import jp.co.sss.lms.dto.UserDetailDto;

/**
 * LMSユーザーマスタマッパー
 *
 * @author 東京ITスクール
 */
@Mapper
public interface MLmsUserMapper {

	/**
	 * ユーザー基本情報取得
	 *
	 * @param lmsUserId
	 * @param deleteFlg
	 * @return ユーザー基本情報DTO
	 */
	UserDetailDto getUserDetail(
			@Param("lmsUserId") Integer lmsUserId,
			@Param("deleteFlg") Short deleteFlg);

	/**
	 * Task.57 勤怠情報確認（受講生一覧）取得
	 *
	 * @param courseId
	 * @param companyId
	 * @param userName
	 * @param placeId
	 * @param deleteFlg
	 * @return 受講生一覧
	 */
	List<AttendanceStudentDto> getAttendanceStudentList(
			@Param("courseId") Integer courseId,
			@Param("companyId") Integer companyId,
			@Param("userName") String userName,
			@Param("placeId") Integer placeId,
			@Param("deleteFlg") Short deleteFlg);

	List<AttendanceStudentDto> getAttendanceStudentListCompany(
			@Param("companyId") Integer companyId,
			@Param("userName") String userName,
			@Param("pastFlg") Integer pastFlg,
			@Param("limitDate") Date limitDate);

	List<AttendanceStudentDto> getAttendanceStudentListCompanyAll(
			@Param("companyId") Integer companyId,
			@Param("userName") String userName,
			@Param("pastFlg") Integer pastFlg);

	List<AttendanceStudentDto> getAttendanceStudentListAdmin(
			@Param("courseId") Integer courseId,
			@Param("companyId") Integer companyId,
			@Param("userName") String userName,
			@Param("limitDate") Date limitDate);

	List<AttendanceStudentDto> getAttendanceStudentListAdminAll(
			@Param("courseId") Integer courseId,
			@Param("companyId") Integer companyId,
			@Param("userName") String userName);
}
