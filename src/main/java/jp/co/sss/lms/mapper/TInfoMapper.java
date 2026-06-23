package jp.co.sss.lms.mapper;

import org.apache.ibatis.annotations.Mapper;

import jp.co.sss.lms.entity.TInfo;

/**
 * お知らせテーブルマッパー
 * 
 * @author 東京ITスクール
 */
@Mapper
public interface TInfoMapper {

	/**
	 * お知らせ取得（最新1件）
	 * 
	 * @param deleteFlg
	 * @return お知らせエンティティ
	 */
	TInfo findBySingleResult();

}
