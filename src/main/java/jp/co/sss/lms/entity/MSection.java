package jp.co.sss.lms.entity;

import java.util.Date;

import lombok.Data;

/**
 * セクションマスタエンティティ
 * 
 * @author 東京ITスクール
 */
@Data
public class MSection {

	/** セクションID */
	private Integer sectionId;
	/** セクション名 */
	private String sectionName;
	/** 概要 */
	private String sectionDescription;
	/** コースID */
	private Integer courseId;
	/** カテゴリID */
	private Integer categoryId;
	/** 日付 */
	private Date date;
	/** 企業アカウントID */
	private Integer accountId;
	/** 削除フラグ */
	private Short deleteFlg;
	/** 初回作成者 */
	private Integer firstCreateUser;
	/** 初回作成日時 */
	private Date firstCreateDate;
	/** 最終更新者 */
	private Integer lastModifiedUser;
	/** 最終更新日時 */
	private Date lastModifiedDate;

}