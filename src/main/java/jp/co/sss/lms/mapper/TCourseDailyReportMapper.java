package jp.co.sss.lms.mapper;

import java.util.Date;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.sss.lms.dto.DailyReportDto;

/**
 * コース・日報紐付けテーブルマッパー
 * 
 * @author 東京ITスクール
 */
@Mapper
public interface TCourseDailyReportMapper {

	/**
	 * レポートDTO取得
	 * 
	 * @param dailyReportId
	 * @param courseId
	 * @param accountId
	 * @param lmsUserId
	 * @param deleteFlg
	 * @param date
	 * @return レポートDTO
	 */
	DailyReportDto getDailyReportDto(@Param("dailyReportId") Integer dailyReportId,
			@Param("courseId") Integer courseId, @Param("accountId") Integer accountId,
			@Param("lmsUserId") Integer lmsUserId, @Param("deleteFlg") Short deleteFlg,
			@Param("date") Date date);

}
