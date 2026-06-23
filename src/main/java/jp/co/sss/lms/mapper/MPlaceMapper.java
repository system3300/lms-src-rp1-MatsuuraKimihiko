package jp.co.sss.lms.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.sss.lms.dto.UserAttendanceDto;
import jp.co.sss.lms.entity.MPlace;

@Mapper
public interface MPlaceMapper {

	List<UserAttendanceDto> getUserAttendanceDto(
			@Param("placeId") Integer placeId,
			@Param("from") Date from,
			@Param("to") Date to,
			@Param("deleteFlg") Short deleteFlg,
			@Param("accountId") Integer accountId);

	MPlace findByPlaceId(@Param("placeId") Integer placeId,
			@Param("hiddenFlg") Short hiddenFlg,
			@Param("deleteFlg") Short deleteFlg);
}
