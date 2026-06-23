package jp.co.sss.lms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.sss.lms.dto.ExamQuestionDto;

/**
 * 試験マスタマッパー
 * 
 * @author 東京ITスクール
 */
@Mapper
public interface MExamMapper {

	/**
	 * 試験問題DTO取得
	 * 
	 * @param examId
	 * @param deleteFlg
	 * @return 試験問題DTO
	 */
	ExamQuestionDto getExamQuestion(@Param("examId") Integer examId,
			@Param("deleteFlg") Short deleteFlg);

}
