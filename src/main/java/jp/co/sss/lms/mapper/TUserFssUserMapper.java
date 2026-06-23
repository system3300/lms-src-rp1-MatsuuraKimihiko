package jp.co.sss.lms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ユーザ・共有ユーザ紐付けテーブルマッパー
 * 
 * @author 東京ITスクール
 */
@Mapper
public interface TUserFssUserMapper {

	/**
	 * ユーザー・共有ユーザー紐付け件数取得
	 * 
	 * @param userId
	 * @param auth
	 * @param deleteFlg
	 * @return 件数
	 */
	Integer getCount(@Param("userId") Integer userId, @Param("auth") Short auth,
			@Param("deleteFlg") Short deleteFlg);

	/**
	 * 共有ユーザID取得
	 * 
	 * @param userId
	 * @param deleteFlg
	 * @return 共有ユーザID
	 */
	Integer getFssUserId(@Param("userId") Integer userId, @Param("deleteFlg") Short deleteFlg);

}
