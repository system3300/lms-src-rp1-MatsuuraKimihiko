package jp.co.sss.lms.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.sss.lms.dto.DailyReportDetailDto;
import jp.co.sss.lms.dto.DailyReportDownloadDto;
import jp.co.sss.lms.dto.DailyReportDto;
import jp.co.sss.lms.dto.DailyReportFbDto;
import jp.co.sss.lms.dto.IntelligibilityDto;
import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.dto.WorkbookDto;
import jp.co.sss.lms.entity.TDailyReportSubmit;
import jp.co.sss.lms.entity.TDailyReportSubmitDetail;
import jp.co.sss.lms.entity.TIntelligibility;
import jp.co.sss.lms.form.DailyReportSubmitForm;
import jp.co.sss.lms.mapper.TCourseDailyReportMapper;
import jp.co.sss.lms.mapper.TDailyReportSubmitDetailMapper;
import jp.co.sss.lms.mapper.TDailyReportSubmitMapper;
import jp.co.sss.lms.mapper.TIntelligibilityMapper;
import jp.co.sss.lms.mapper.TSectionDailyReportMapper;
import jp.co.sss.lms.util.Constants;
import jp.co.sss.lms.util.DateUtil;
import jp.co.sss.lms.util.ExcelUtil;
import jp.co.sss.lms.util.LoginUserUtil;
import jp.co.sss.lms.util.MessageUtil;

/**
 * レポート情報サービス
 * 
 * @author 東京ITスクール
 */
@Service
public class ReportService {

	@Autowired
	private LoginUserDto loginUserDto;
	@Autowired
	private LoginUserUtil loginUserUtil;
	@Autowired
	private MessageUtil messageUtil;
	@Autowired
	private DateUtil dateUtil;
	@Autowired
	private TCourseDailyReportMapper tCourseDailyReportMapper;
	@Autowired
	private TSectionDailyReportMapper tSectionDailyReportMapper;
	@Autowired
	private TDailyReportSubmitMapper tDailyReportSubmitMapper;
	@Autowired
	private TDailyReportSubmitDetailMapper tDailyReportSubmitDetailMapper;
	@Autowired
	private TIntelligibilityMapper tIntelligibilityMapper;

	/**
	 * レポート情報取得
	 * 
	 * @param dailyReportSubmitForm
	 * @return レポートフォーム
	 * @throws ParseException
	 */
	public void getDailyReport(DailyReportSubmitForm dailyReportSubmitForm) throws ParseException {

		// コースに紐づくレポート情報を取得
		DailyReportDto dailyReportDto = tCourseDailyReportMapper.getDailyReportDto(
				dailyReportSubmitForm.getDailyReportId(), loginUserDto.getCourseId(),
				loginUserDto.getAccountId(), loginUserDto.getLmsUserId(), Constants.DB_FLG_FALSE,
				dailyReportSubmitForm.getDate());
		// 無かった場合にセクションに紐づく情報を取得
		if (dailyReportDto == null) {
			dailyReportDto = tSectionDailyReportMapper.getDailyReportDto(
					dailyReportSubmitForm.getDailyReportId(), dailyReportSubmitForm.getSectionId(),
					loginUserDto.getAccountId(), loginUserDto.getLmsUserId(),
					Constants.DB_FLG_FALSE, dailyReportSubmitForm.getDate());
		}

		// フォームに詰め替え
		dailyReportSubmitForm.setDailyReportId(dailyReportDto.getDailyReportId());
		if (dailyReportSubmitForm.getDate() == null) {
			dailyReportSubmitForm.setDate(dailyReportDto.getDate());
		}
		dailyReportSubmitForm.setReportName(dailyReportDto.getReportName());
		dailyReportSubmitForm.setIntelligibilityFlg(dailyReportDto.getIntelligibilityFlg());
		if (dailyReportDto.getIntelligibilityFlg() != null
				&& dailyReportDto.getIntelligibilityFlg() == 1) {
			dailyReportSubmitForm
					.setIntelligibilityFieldNum(dailyReportDto.getIntelligibilityFieldNum());
			dailyReportSubmitForm.setIntelligibilityNum(dailyReportDto.getIntelligibilityNum());
			Short intelligibilityDtoListSize = dailyReportDto.getIntelligibilityFieldNum();
			String[] intFieldNameArray = new String[intelligibilityDtoListSize];
			Short[] intFieldValueArray = new Short[intelligibilityDtoListSize];
			List<IntelligibilityDto> intelligibilityDtoList = dailyReportDto
					.getIntelligibilityDtoList();
			if (!(intelligibilityDtoList.size() == 1
					&& intelligibilityDtoList.get(0).getIntelligibilityId() == null)) {
				for (int i = 0; i < intelligibilityDtoList.size(); i++) {
					intFieldNameArray[i] = intelligibilityDtoList.get(i).getFieldName();
					intFieldValueArray[i] = intelligibilityDtoList.get(i).getFieldValue();
				}
			}
			dailyReportSubmitForm.setIntFieldNameArray(intFieldNameArray);
			dailyReportSubmitForm.setIntFieldValueArray(intFieldValueArray);
		}
		Integer dailyReportDetailDtoListSize = dailyReportDto.getDailyReportDetailDtoList().size();
		String[] fieldNameArray = new String[dailyReportDetailDtoListSize];
		Short[] requiredFlgArray = new Short[dailyReportDetailDtoListSize];
		Short[] inputTypeArray = new Short[dailyReportDetailDtoListSize];
		Integer[] rangeFromArray = new Integer[dailyReportDetailDtoListSize];
		Integer[] rangeToArray = new Integer[dailyReportDetailDtoListSize];
		String[] contentArray = new String[dailyReportDetailDtoListSize];
		for (int j = 0; j < dailyReportDto.getDailyReportDetailDtoList().size(); j++) {
			DailyReportDetailDto dailyReportDetailDto = dailyReportDto.getDailyReportDetailDtoList()
					.get(j);
			fieldNameArray[j] = dailyReportDetailDto.getFieldName();
			requiredFlgArray[j] = dailyReportDetailDto.getRequiredFlg();
			inputTypeArray[j] = dailyReportDetailDto.getInputType();
			rangeFromArray[j] = dailyReportDetailDto.getRangeFrom();
			rangeToArray[j] = dailyReportDetailDto.getRangeTo();
			contentArray[j] = dailyReportDetailDto.getContent();
		}
		dailyReportSubmitForm.setFieldNameArray(fieldNameArray);
		dailyReportSubmitForm.setRequiredFlgArray(requiredFlgArray);
		dailyReportSubmitForm.setInputTypeArray(inputTypeArray);
		dailyReportSubmitForm.setRangeFromArray(rangeFromArray);
		dailyReportSubmitForm.setRangeToArray(rangeToArray);
		dailyReportSubmitForm.setContentArray(contentArray);
	}

	/**
	 * レポート登録
	 * 
	 * @param form
	 * @throws ParseException
	 */
	public void submit(DailyReportSubmitForm dailyReportSubmitForm) throws ParseException {

		Date today = new Date();
		// 日報提出エンティティを生成
		TDailyReportSubmit tDailyReportSubmit = new TDailyReportSubmit();
		tDailyReportSubmit.setDailyReportId(dailyReportSubmitForm.getDailyReportId());
		tDailyReportSubmit.setDate(dailyReportSubmitForm.getDate());
		Integer lmsUserId = loginUserUtil.isStudent() ? loginUserDto.getLmsUserId()
				: dailyReportSubmitForm.getLmsUserId();
		tDailyReportSubmit.setLmsUserId(lmsUserId);
		tDailyReportSubmit.setAccountId(loginUserDto.getAccountId());
		tDailyReportSubmit.setDeleteFlg(Constants.DB_FLG_FALSE);
		tDailyReportSubmit.setLastModifiedUser(loginUserDto.getLmsUserId());
		tDailyReportSubmit.setLastModifiedDate(today);

		// 日報提出IDがnullの場合
		if (dailyReportSubmitForm.getDailyReportSubmitId() == null) {
			tDailyReportSubmit.setFirstCreateUser(loginUserDto.getLmsUserId());
			tDailyReportSubmit.setFirstCreateDate(today);
			tDailyReportSubmitMapper.insert(tDailyReportSubmit);
		} else {
			tDailyReportSubmit
					.setDailyReportSubmitId(dailyReportSubmitForm.getDailyReportSubmitId());
			tDailyReportSubmitMapper.update(tDailyReportSubmit);
			// 日報提出詳細の削除
			Integer countSubmitDetail = tDailyReportSubmitDetailMapper
					.getCountByDailyReportSubmitId(tDailyReportSubmit.getDailyReportSubmitId());
			if (countSubmitDetail > 0) {
				tDailyReportSubmitDetailMapper.delete(tDailyReportSubmit.getDailyReportSubmitId());
			}
			// 理解度の削除
			Integer countIntelligibility = tIntelligibilityMapper
					.getCountByDailyReportSubmitId(tDailyReportSubmit.getDailyReportSubmitId());
			if (countIntelligibility > 0) {
				tIntelligibilityMapper.delete(tDailyReportSubmit.getDailyReportSubmitId());
			}
		}
		// レポート詳細登録
		for (int i = 0; i < dailyReportSubmitForm.getContentArray().length; i++) {
			TDailyReportSubmitDetail tDailyReportSubmitDetail = new TDailyReportSubmitDetail();
			tDailyReportSubmitDetail
					.setDailyReportSubmitId(tDailyReportSubmit.getDailyReportSubmitId());
			tDailyReportSubmitDetail.setFieldNum(i + 1);
			tDailyReportSubmitDetail.setContent(dailyReportSubmitForm.getContentArray()[i]);
			tDailyReportSubmitDetail.setAccountId(tDailyReportSubmit.getAccountId());
			tDailyReportSubmitDetail.setDeleteFlg(Constants.DB_FLG_FALSE);
			tDailyReportSubmitDetail.setFirstCreateUser(loginUserDto.getLmsUserId());
			tDailyReportSubmitDetail.setFirstCreateDate(today);
			tDailyReportSubmitDetail.setLastModifiedUser(loginUserDto.getLmsUserId());
			tDailyReportSubmitDetail.setLastModifiedDate(today);
			tDailyReportSubmitDetailMapper.insert(tDailyReportSubmitDetail);
		}
		// 理解度登録
		if (dailyReportSubmitForm.getIntFieldNameArray() != null) {
			for (int j = 0; j < dailyReportSubmitForm.getIntFieldNameArray().length; j++) {
				TIntelligibility tIntelligibility = new TIntelligibility();
				tIntelligibility
						.setDailyReportSubmitId(tDailyReportSubmit.getDailyReportSubmitId());
				tIntelligibility.setFieldNum(j + 1);
				tIntelligibility.setFieldName(dailyReportSubmitForm.getIntFieldNameArray()[j]);
				tIntelligibility.setFieldValue(dailyReportSubmitForm.getIntFieldValueArray()[j]);
				tIntelligibility.setAccountId(tDailyReportSubmit.getAccountId());
				tIntelligibility.setDeleteFlg(Constants.DB_FLG_FALSE);
				tIntelligibility.setFirstCreateUser(loginUserDto.getLmsUserId());
				tIntelligibility.setFirstCreateDate(today);
				tIntelligibility.setLastModifiedUser(loginUserDto.getLmsUserId());
				tIntelligibility.setLastModifiedDate(today);
				tIntelligibilityMapper.insert(tIntelligibility);
			}
		}
	}

	/**
	 * レポートダウンロード
	 * 
	 * @param dailyReportId
	 * @param dailyReportSubmitId
	 * @param response
	 * @throws IOException
	 */
	public void download(Integer dailyReportId, Integer dailyReportSubmitId,
			HttpServletResponse response) throws IOException {
		WorkbookDto workbookDto = getWorkbookDto(dailyReportId, dailyReportSubmitId);
		ExcelUtil.downloadBook(workbookDto, response);
	}

	/**
	 * ワークブックDTO取得
	 * 
	 * @param dailyReportId
	 * @param dailyReportSubmitId
	 * @return ワークブックDTO
	 * @throws IOException
	 */
	private WorkbookDto getWorkbookDto(Integer dailyReportId, Integer dailyReportSubmitId)
			throws IOException {

		// レポートダウンロード情報取得
		DailyReportDownloadDto dailyReportDownloadDto = tDailyReportSubmitMapper
				.getDailyReportDownloadDto(dailyReportSubmitId, dailyReportId,
						Constants.DB_FLG_FALSE);

		// テンプレートファイルのパスを設定
		String commonFileDir = messageUtil.getMessage("setting.file.common.dir");
		String excelFilePath = commonFileDir + "/" + dailyReportDownloadDto.getFileName();

		ExcelUtil excelUtil = new ExcelUtil(excelFilePath);

		// 企業名
		excelUtil.setVal(dailyReportDownloadDto.getSheetName(),
				dailyReportDownloadDto.getRowCompany() - 1,
				dailyReportDownloadDto.getClmCompany() - 1,
				dailyReportDownloadDto.getCompanyName());

		// ユーザー名
		excelUtil.setVal(dailyReportDownloadDto.getSheetName(),
				dailyReportDownloadDto.getRowUser() - 1, dailyReportDownloadDto.getClmUser() - 1,
				dailyReportDownloadDto.getUserName());

		// 日付
		excelUtil.setVal(dailyReportDownloadDto.getSheetName(),
				dailyReportDownloadDto.getRowDate() - 1, dailyReportDownloadDto.getClmDate() - 1,
				dailyReportDownloadDto.getDate());

		// 「日報提出詳細テーブル」と「日報詳細マスタ」のデータを設定する。
		for (DailyReportDetailDto dailyReportDetailDto : dailyReportDownloadDto
				.getDailyReportDetailDtoList()) {
			excelUtil.setVal(dailyReportDownloadDto.getSheetName(),
					dailyReportDetailDto.getRow() - 1, dailyReportDetailDto.getClm() - 1,
					dailyReportDetailDto.getContent());
		}

		if (dailyReportDownloadDto.getIntelligibilityDtoList().size() > 0) {

			Integer rowIntelFld = dailyReportDownloadDto.getRowIntelFld();
			Integer rowIntel = dailyReportDownloadDto.getRowIntel();

			for (IntelligibilityDto intelligibilityDto : dailyReportDownloadDto
					.getIntelligibilityDtoList()) {
				excelUtil.setVal(dailyReportDownloadDto.getSheetName(), rowIntelFld - 1,
						dailyReportDownloadDto.getClmIntelFld() - 1,
						intelligibilityDto.getFieldName());
				excelUtil.setVal(dailyReportDownloadDto.getSheetName(), rowIntel - 1,
						dailyReportDownloadDto.getClmIntel() - 1,
						intelligibilityDto.getFieldValue());
				rowIntelFld++;
				rowIntel++;
			}
		}

		if (dailyReportDownloadDto.getDailyReportFbDtoList() != null) {
			if (dailyReportDownloadDto.getDailyReportFbDtoList().size() > 1) {
				for (int i = 1; i < dailyReportDownloadDto.getDailyReportFbDtoList().size(); i++) {
					excelUtil.sheetCopy("フィードバックコメント", 6, 8, i);
				}
			}
			for (int i = 0; i < dailyReportDownloadDto.getDailyReportFbDtoList().size(); i++) {
				DailyReportFbDto dailyReportFbDto = dailyReportDownloadDto.getDailyReportFbDtoList()
						.get(i);
				// 指定位置にフィードバックしたユーザを設定
				excelUtil.setVal("フィードバックコメント", 6 + (i * 2), 0, dailyReportFbDto.getUserName());
				// 指定位置にコメントを入力した日付を設定
				excelUtil.setVal("フィードバックコメント", 6 + (i * 2), 17, dailyReportFbDto.getDate());
				// 指定位置に入力内容を設定
				excelUtil.setVal("フィードバックコメント", 7 + (i * 2), 10, dailyReportFbDto.getContent());
			}
		}

		WorkbookDto workbookDto = new WorkbookDto();
		workbookDto.setWb(excelUtil.getWb());
		workbookDto.getWb().setForceFormulaRecalculation(true);

		String[] fileName = dailyReportDownloadDto.getFileName().split("\\.");
		String bookName = "";
		for (int i = 0; i < fileName.length - 1; i++) {
			if (i > 0) {
				bookName += ".";
			}
			bookName += fileName[i];
		}

		// 受講生名に全角スペースが使用されている場合は半角スペースに置き換える
		String userName = dailyReportDownloadDto.getUserName().replaceAll("　", " ");
		String companyName = dailyReportDownloadDto.getCompanyName().replaceAll("　", " ");

		workbookDto.setWbName(
				bookName + "_" + dateUtil.toString(dailyReportDownloadDto.getDate(), "YYYYMMdd")
						+ "_" + companyName + "_" + userName + "." + fileName[fileName.length - 1]);

		return workbookDto;
	}

}
