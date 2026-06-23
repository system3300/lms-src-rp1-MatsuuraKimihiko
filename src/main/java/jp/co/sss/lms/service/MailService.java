package jp.co.sss.lms.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.sss.lms.dto.FileDownloadDto;
import jp.co.sss.lms.dto.FileDto;
import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.entity.TMailQue;
import jp.co.sss.lms.entity.TMailQueFile;
import jp.co.sss.lms.mapper.TMailQueFileMapper;
import jp.co.sss.lms.mapper.TMailQueMapper;
import jp.co.sss.lms.util.Constants;
import jp.co.sss.lms.util.FileUtil;
import jp.co.sss.lms.util.LoginUserUtil;

/**
 * メール情報サービス
 * 
 * @author 東京ITスクール
 */
@Service
public class MailService {

	@Autowired
	private TMailQueMapper tMailQueMapper;
	@Autowired
	private TMailQueFileMapper tMailQueFileMapper;
	@Autowired
	private LoginUserDto loginUserDto;
	@Autowired
	private LoginUserUtil loginUserUtil;
	@Autowired
	private FileUtil fileUtil;

	/**
	 * メールキュー登録
	 * 
	 * @param to
	 * @param subject
	 * @param body
	 * @param cc
	 * @param fileDtoList
	 */
	public void registMailQue(String to, String subject, String body, String cc,
			List<FileDto> fileDtoList) {

		Date now = new Date();
		// メール送信キューテーブル登録
		TMailQue tMailQue = new TMailQue();
		tMailQue.setMailAddressTo(to);
		tMailQue.setMailAddressCc(cc);
		tMailQue.setSubject(subject);
		tMailQue.setBody(body);
		tMailQue.setDeleteFlg(Constants.DB_FLG_FALSE);
		tMailQue.setFirstCreateDate(now);
		tMailQue.setLastModifiedDate(now);
		if (loginUserUtil.isLogin()) {
			tMailQue.setFirstCreateUser(loginUserDto.getLmsUserId());
			tMailQue.setLastModifiedUser(loginUserDto.getLmsUserId());
			tMailQueMapper.insert(tMailQue);
		} else {
			tMailQueMapper.insert(tMailQue);
		}

		// メール送信キュー・ファイルテーブル登録
		if (fileDtoList != null) {
			for (FileDto fileDto : fileDtoList) {
				TMailQueFile tMailQueFile = new TMailQueFile();
				tMailQueFile.setMailQueId(tMailQue.getMailQueId());
				tMailQueFile.setFileId(
						fileUtil.getPlaneFileId(new FileDownloadDto(), fileDto.getFileId()));
				tMailQueFile.setDeleteFlg(Constants.DB_FLG_FALSE);
				tMailQueFile.setFirstCreateDate(now);
				tMailQueFile.setLastModifiedDate(now);
				if (loginUserUtil.isLogin()) {
					tMailQueFile.setFirstCreateUser(loginUserDto.getLmsUserId());
					tMailQueFile.setLastModifiedUser(loginUserDto.getLmsUserId());
					tMailQueFileMapper.insert(tMailQueFile);
				} else {
					tMailQueFileMapper.insert(tMailQueFile);
				}
			}
		}
	}

}
