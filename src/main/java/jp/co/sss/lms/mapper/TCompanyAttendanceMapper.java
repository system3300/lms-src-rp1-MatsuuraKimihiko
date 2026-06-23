package jp.co.sss.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.sss.lms.entity.TCompanyAttendance;

@Mapper
public interface TCompanyAttendanceMapper {

	TCompanyAttendance findByCompanyAttendanceId(
			@Param("companyAttendanceId") Integer companyAttendanceId,
			@Param("deleteFlg") Short deleteFlg);

	int insert(TCompanyAttendance entity);

	int update(TCompanyAttendance entity);

	List<TCompanyAttendance> findByLmsUserId(
			@Param("lmsUserId") Integer lmsUserId,
			@Param("deleteFlg") Short deleteFlg);
}