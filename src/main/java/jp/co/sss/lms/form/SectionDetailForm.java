package jp.co.sss.lms.form;

import java.util.Date;
import java.util.List;

import jp.co.sss.lms.dto.SectionServiceDailyReportDto;
import jp.co.sss.lms.dto.SectionServiceDeliverablesSectionDto;
import jp.co.sss.lms.dto.SectionServiceExamDto;
import jp.co.sss.lms.dto.SectionServiceFileDto;
import lombok.Data;

/**
 * セクション詳細フォーム
 * 
 * @author 東京ITスクール
 */
@Data
public class SectionDetailForm {

	/** セクションID */
	private Integer sectionId;
	/** セクション名 */
	private String sectionName;
	/** 概要 */
	private String sectionDescription;
	/** コースID */
	private Integer courseId;
	/** カテゴリーID */
	private Integer categoryId;
	/** ファイルサイズ */
	private String maxFileSize;
	/** 日付 */
	private Date date;
	/** セクションファイルDTOリスト */
	private List<SectionServiceFileDto> fileDtoList;
	/** セクションサービス試験DTOリスト */
	private List<SectionServiceExamDto> examDtoList;
	/** セクションサービスレポートDTOリスト */
	private List<SectionServiceDailyReportDto> reportDtoList;
	/** セクションサービス成果物セクションDTOリスト */
	private List<SectionServiceDeliverablesSectionDto> deliverablesDtoList;

}
