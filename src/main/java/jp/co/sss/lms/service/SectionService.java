package jp.co.sss.lms.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.dto.SectionServiceDailyReportDto;
import jp.co.sss.lms.dto.SectionServiceFileDto;
import jp.co.sss.lms.dto.SectionServiceSectionDto;
import jp.co.sss.lms.form.SectionDetailForm;
import jp.co.sss.lms.mapper.MSectionMapper;
import jp.co.sss.lms.mapper.TSectionDailyReportMapper;
import jp.co.sss.lms.util.Constants;
import jp.co.sss.lms.util.LoggingUtil;
import jp.co.sss.lms.util.MessageUtil;
import jp.co.sss.lms.util.PasswordUtil;

/**
 * セクション情報サービス
 * 
 * @author 東京ITスクール
 */
@Service
public class SectionService {

	@Autowired
	private MSectionMapper mSectionMapper;
	@Autowired
	private TSectionDailyReportMapper tSectionDailyReportMapper;
	@Autowired
	private MessageUtil messageUtil;
	@Autowired
	private LoginUserDto loginUserDto;
	@Autowired
	private PasswordUtil passwordUtil;
	@Autowired
	private LoggingUtil loggingUtil;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * パラメータチェック
	 * 
	 * @param sectionId
	 * @return エラーメッセージ
	 */
	public String checkSectionId(Integer sectionId) {
		Integer count = mSectionMapper.getSectionCount(sectionId);
		if (count == 0) {
			String message = messageUtil.getMessage(Constants.VALID_KEY_ALREADYDELETE,
					new String[] { "セクションID " + sectionId });
			StringBuffer sb = new StringBuffer(message);
			loggingUtil.appendLog(sb);
			logger.info(sb.toString());
			return message;
		}
		return "";
	}

	/**
	 * セクション詳細情報の取得
	 * 
	 * @param sectionDetailForm
	 */
	public void getSectionDetail(SectionDetailForm sectionDetailForm) {
		// セクションサービスDTOの取得
		SectionServiceSectionDto sectionServiceSectionDto = mSectionMapper.getSectionDetail(
				sectionDetailForm.getSectionId(), loginUserDto.getAccountId(),
				loginUserDto.getLmsUserId(), Constants.DB_FLG_FALSE);
		BeanUtils.copyProperties(sectionServiceSectionDto, sectionDetailForm);
		// セクション・日報紐付け情報の取得
		List<SectionServiceDailyReportDto> sectionServiceDailyReportDtoList = tSectionDailyReportMapper
				.getSectionServiceDailyReportDto(sectionDetailForm.getSectionId(),
						loginUserDto.getLmsUserId(), Constants.DB_FLG_FALSE);
		for (SectionServiceDailyReportDto sectionServiceDailyReportDto : sectionServiceDailyReportDtoList) {
			sectionServiceSectionDto.getReportDtoList().add(sectionServiceDailyReportDto);
		}
		// ファイル情報の設定
		for (SectionServiceFileDto fileDto : sectionServiceSectionDto.getFileDtoList()) {
			String hashFileId = passwordUtil.getSaltedAndStrechedPassword(fileDto.getFileId(),
					loginUserDto.getUserId().toString());
			fileDto.setFileId(hashFileId);
		}
		sectionServiceSectionDto.setMaxFileSize(Constants.DELIVERABLES_UPLOAD_MAX_SIZE);
	}

}
