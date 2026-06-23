package jp.co.sss.lms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.sss.lms.dto.MeetingDownloadDto;

/**
 * 面談テーブルマッパー
 * 
 * @author 東京ITスクール
 */

@Mapper
public interface TMeetingMapper {

	/**
	 * 面談ダウンロードDTO取得
	 * 
	 * @param meetingId
	 * @param deleteFlg
	 * @return 面談ダウンロードDTO
	 */
	MeetingDownloadDto getMeetingDownloadDto(@Param("meetingId") Integer meetingId,
			@Param("deleteFlg") Short deleteFlg);

}
