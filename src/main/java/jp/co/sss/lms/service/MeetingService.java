package jp.co.sss.lms.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import jp.co.sss.lms.dto.MeetingDetailDto;
import jp.co.sss.lms.dto.MeetingDownloadDto;
import jp.co.sss.lms.dto.WorkbookDto;
import jp.co.sss.lms.mapper.TMeetingMapper;
import jp.co.sss.lms.util.Constants;
import jp.co.sss.lms.util.DateUtil;
import jp.co.sss.lms.util.ExcelUtil;
import jp.co.sss.lms.util.MessageUtil;

/**
 * 面談情報サービス
 * 
 * @author 東京ITスクール
 */
@Service
public class MeetingService {

	@Autowired
	private TMeetingMapper tMeetingMapper;
	@Autowired
	private MessageUtil messageUtil;
	@Autowired
	private DateUtil dateUtil;

	/**
	 * 面談記録ファイルのダウンロード
	 * 
	 * @param meetingId
	 * @param response
	 * @throws IOException
	 */
	public void downloadMeetingFile(Integer meetingId, HttpServletResponse response)
			throws IOException {

		// 面談ダウンロードDTOの取得
		MeetingDownloadDto meetingDownloadDto = tMeetingMapper.getMeetingDownloadDto(meetingId,
				Constants.DB_FLG_FALSE);

		// テンプレートファイルのパスを設定
		String commonFileDir = messageUtil.getMessage("setting.file.common.dir");
		String excelFilePath = commonFileDir + "/" + meetingDownloadDto.getFileName();
		ExcelUtil excelUtil = new ExcelUtil(excelFilePath);

		// 企業名
		excelUtil.setVal(meetingDownloadDto.getSheetName(), meetingDownloadDto.getRowCompany(),
				meetingDownloadDto.getClmCompany(), meetingDownloadDto.getCompanyName());
		// ユーザー名
		excelUtil.setVal(meetingDownloadDto.getSheetName(), meetingDownloadDto.getRowUser(),
				meetingDownloadDto.getClmUser(), meetingDownloadDto.getInterviewee());
		// 面談実施日
		excelUtil.setVal(meetingDownloadDto.getSheetName(), meetingDownloadDto.getRowDate(),
				meetingDownloadDto.getClmDate(), meetingDownloadDto.getInterviewDate());
		// 面談実施者
		excelUtil.setVal(meetingDownloadDto.getSheetName(), meetingDownloadDto.getRowDate(),
				meetingDownloadDto.getClmUser(), meetingDownloadDto.getInterviewer());
		// 面談内容
		int rowTechnical = meetingDownloadDto.getRowMeeting() - 1;
		int rowHuman = rowTechnical + 5;
		for (MeetingDetailDto meetingDetailDto : meetingDownloadDto.getMeetingDetailDtoList()) {
			int row = meetingDetailDto.getQuestionType() == 0 ? rowTechnical : rowHuman;
			int col = meetingDownloadDto.getClmMeeting();
			// 質問
			excelUtil.setVal(meetingDownloadDto.getSheetName(), ++row, col,
					meetingDetailDto.getQuestion());
			// 回答
			excelUtil.setVal(meetingDownloadDto.getSheetName(), row, ++col,
					meetingDetailDto.getAnswer());
			// フォロー
			excelUtil.setVal(meetingDownloadDto.getSheetName(), row, ++col,
					meetingDetailDto.getFollow());
			if (meetingDetailDto.getQuestionType() == 0) {
				rowTechnical = row;
			} else {
				rowHuman = row;
			}
		}

		WorkbookDto workbookDto = new WorkbookDto();
		workbookDto.setWb(excelUtil.getWb());
		workbookDto.getWb().setForceFormulaRecalculation(true);

		String[] fileName = meetingDownloadDto.getFileName().split("\\.");
		String bookName = "";
		for (int i = 0; i < fileName.length - 1; i++) {
			if (i > 0) {
				bookName += ".";
			}
			bookName += fileName[i];
		}

		// 受講生名に全角スペースが使用されている場合は半角スペースに置き換える
		String userName = meetingDownloadDto.getInterviewee().replaceAll("　", " ");
		String companyName = meetingDownloadDto.getCompanyName().replaceAll("　", " ");

		workbookDto.setWbName(bookName + "_"
				+ dateUtil.toString(meetingDownloadDto.getInterviewDate(), "YYYYMMdd") + "_"
				+ companyName + "_" + userName + "." + fileName[fileName.length - 1]);

		ExcelUtil.downloadBook(workbookDto, response);
	}

}
