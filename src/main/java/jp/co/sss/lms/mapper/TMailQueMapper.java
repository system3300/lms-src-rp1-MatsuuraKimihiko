package jp.co.sss.lms.mapper;

import org.apache.ibatis.annotations.Mapper;

import jp.co.sss.lms.entity.TMailQue;

/**
 * メール送信キューテーブルマッパー
 * 
 * @author 東京ITスクール
 */
@Mapper
public interface TMailQueMapper {

	/**
	 * メール送信キュー登録
	 * 
	 * @param tMailQue
	 * @return 登録結果
	 */
	Boolean insert(TMailQue tMailQue);

}
