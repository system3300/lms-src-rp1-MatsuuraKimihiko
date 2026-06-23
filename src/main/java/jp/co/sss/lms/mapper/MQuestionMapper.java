package jp.co.sss.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.co.sss.lms.entity.MQuestion;

/**
 * 試験問題マスタマッパー
 * 
 * @author 東京ITスクール
 */
@Mapper
public interface MQuestionMapper {

	/**
	 * 試験問題取得（試験ID）
	 * 
	 * @param examId
	 * @param deleteFlg
	 * @return 試験問題エンティティリスト
	 */
	List<MQuestion> findByExamId(@Param("examId") Integer examId,
			@Param("deleteFlg") Short deleteFlg);

}
