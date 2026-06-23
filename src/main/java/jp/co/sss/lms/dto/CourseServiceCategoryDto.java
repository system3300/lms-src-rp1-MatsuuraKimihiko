package jp.co.sss.lms.dto;

import java.util.List;

import lombok.Data;

/**
 * コース情報サービス カテゴリDTO
 * 
 * @author 東京ITスクール
 *
 */
@Data
public class CourseServiceCategoryDto {

	/** カテゴリ名 */
	private String categoryName;
	/** コース情報サービス セクションDTOリスト */
	private List<CourseServiceSectionDto> courseServiceSectionDtoList;

}
