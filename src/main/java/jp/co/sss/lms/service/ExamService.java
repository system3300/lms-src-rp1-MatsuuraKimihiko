package jp.co.sss.lms.service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.sss.lms.dto.ExamDetailDto;
import jp.co.sss.lms.dto.ExamQuestionDto;
import jp.co.sss.lms.dto.ExamResultDetailDto;
import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.dto.QuestionDto;
import jp.co.sss.lms.entity.MQuestion;
import jp.co.sss.lms.entity.TExamResult;
import jp.co.sss.lms.entity.TExamResultDetail;
import jp.co.sss.lms.form.ExamQuestionForm;
import jp.co.sss.lms.mapper.MExamMapper;
import jp.co.sss.lms.mapper.MQuestionMapper;
import jp.co.sss.lms.mapper.TExamResultDetailMapper;
import jp.co.sss.lms.mapper.TExamResultMapper;
import jp.co.sss.lms.util.Constants;

/**
 * 試験情報サービス
 * 
 * @author 東京ITスクール
 */
@Service
public class ExamService {

	@Autowired
	private TExamResultMapper tExamResultMapper;
	@Autowired
	private MExamMapper mExamMapper;
	@Autowired
	private TExamResultDetailMapper tExamResultDetailMapper;
	@Autowired
	private MQuestionMapper mQuestionMapper;
	@Autowired
	private LoginUserDto loginUserDto;

	/**
	 * 試験詳細情報を取得
	 * 
	 * @param examSectionId
	 * @param lmsUserId
	 * @return 試験詳細DTO
	 */
	public ExamDetailDto getExamDetail(Integer examSectionId, Integer lmsUserId) {

		// 試験詳細DTOを取得
		ExamDetailDto examDetailDto = tExamResultMapper.getExamDetailDto(examSectionId, lmsUserId,
				Constants.DB_FLG_FALSE);

		return examDetailDto;
	}

	/**
	 * 試験結果詳細情報取得
	 * 
	 * @param examResultId
	 * @return 試験結果詳細情報
	 */
	public ExamResultDetailDto getExamResultDetail(Integer examResultId) {

		// 試験結果詳細を取得
		ExamResultDetailDto examResultDetailDto = tExamResultMapper
				.getExamResultDetail(examResultId, Constants.DB_FLG_FALSE);

		// 試験結果詳細を基に試験結果詳細DTO．問題DTOリスト．回答リストを設定
		for (QuestionDto questionDto : examResultDetailDto.getQuestionDtoList()) {
			List<String> answerList = new LinkedList<>();
			answerList.add(questionDto.getChoice1());
			answerList.add(questionDto.getChoice2());
			answerList.add(questionDto.getChoice3());
			answerList.add(questionDto.getChoice4());
			questionDto.setAnswerList(answerList);
		}

		return examResultDetailDto;

	}

	/**
	 * 試験問題画面フォームの設定
	 * 
	 * @param examQuestionForm
	 */
	public void setExamQuestionForm(ExamQuestionForm examQuestionForm) {
		// 試験問題情報を取得
		ExamQuestionDto examQuestionDto = mExamMapper.getExamQuestion(examQuestionForm.getExamId(),
				Constants.DB_FLG_FALSE);
		BeanUtils.copyProperties(examQuestionDto, examQuestionForm);
		// 試験問題情報を基に試験問題DTO．問題DTOリスト．回答リストを設定
		for (QuestionDto questionDto : examQuestionForm.getQuestionDtoList()) {
			List<String> answerList = new LinkedList<>();
			answerList.add(questionDto.getChoice1());
			answerList.add(questionDto.getChoice2());
			answerList.add(questionDto.getChoice3());
			answerList.add(questionDto.getChoice4());
			questionDto.setAnswerList(answerList);
		}
		// 経過時間の初期化
		if (examQuestionForm.getTime() == null) {
			examQuestionForm.setTime(0);
		}
		// 回答の再設定
		int questionCount = examQuestionDto.getQuestionDtoList().size();
		Short[] answerArray = new Short[questionCount];
		Short[] userAnswer = examQuestionForm.getAnswer();
		if (userAnswer != null) {
			for (int i = 0; i < userAnswer.length; i++) {
				answerArray[i] = userAnswer[i];
			}
		}
		examQuestionForm.setAnswer(answerArray);
	}

	/**
	 * 試験結果登録
	 * 
	 * @param examQuestionForm
	 * @return examResultId
	 */
	public Integer insert(ExamQuestionForm examQuestionForm) {

		// 試験IDに紐づく試験問題リストを取得
		List<MQuestion> mQuestionList = mQuestionMapper.findByExamId(examQuestionForm.getExamId(),
				Constants.DB_FLG_FALSE);

		// 現在日時情報
		Date now = new Date();

		// 試験結果エンティティを生成
		TExamResult tExamResult = new TExamResult();

		// 得点設定
		Short score = 0;
		Short[] answerArray = examQuestionForm.getAnswer();
		for (int i = 0; i < answerArray.length; i++) {
			if (answerArray[i] != null && answerArray[i] == mQuestionList.get(i).getAnswerNum()) {
				score++;
			}
		}

		// 試験結果件数取得
		Integer examCount = tExamResultMapper.getExamCount(examQuestionForm.getExamSectionId(),
				loginUserDto.getLmsUserId(), Constants.DB_FLG_FALSE);

		// 試験結果の設定
		tExamResult.setExamSectionId(examQuestionForm.getExamSectionId());
		tExamResult.setLmsUserId(loginUserDto.getLmsUserId());
		tExamResult.setScore(score);
		tExamResult.setTime(examQuestionForm.getTime());
		if (examCount == 0) {
			tExamResult.setMarkFlg(Constants.DB_FLG_TRUE);
		} else {
			tExamResult.setMarkFlg(Constants.DB_FLG_FALSE);
		}
		tExamResult.setAccountId(loginUserDto.getAccountId());
		tExamResult.setDeleteFlg(Constants.DB_FLG_FALSE);
		tExamResult.setFirstCreateUser(loginUserDto.getLmsUserId());
		tExamResult.setFirstCreateDate(now);
		tExamResult.setLastModifiedUser(loginUserDto.getLmsUserId());
		tExamResult.setLastModifiedDate(now);

		// 試験結果へ登録
		tExamResultMapper.insert(tExamResult);

		// 試験結果詳細登録
		for (int j = 0; j < mQuestionList.size(); j++) {
			// 試験結果詳細を生成
			TExamResultDetail tExamResultDetail = new TExamResultDetail();
			tExamResultDetail.setExamResultId(tExamResult.getExamResultId());
			tExamResultDetail.setLmsUserId(loginUserDto.getLmsUserId());
			tExamResultDetail.setQuestionId(mQuestionList.get(j).getQuestionId());
			Short reply = 0;
			try {
				reply = examQuestionForm.getAnswer()[j];
			} catch (Exception e) {
				reply = 0;
			}
			tExamResultDetail.setReply(reply);
			tExamResultDetail.setAccountId(loginUserDto.getAccountId());
			tExamResultDetail.setDeleteFlg(Constants.DB_FLG_FALSE);
			tExamResultDetail.setFirstCreateUser(loginUserDto.getLmsUserId());
			tExamResultDetail.setFirstCreateDate(now);
			tExamResultDetail.setLastModifiedUser(loginUserDto.getLmsUserId());
			tExamResultDetail.setLastModifiedDate(now);
			// 試験結果詳細へ登録
			tExamResultDetailMapper.insert(tExamResultDetail);
		}

		return tExamResult.getExamResultId();
	}

}