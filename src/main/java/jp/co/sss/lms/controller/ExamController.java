package jp.co.sss.lms.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jp.co.sss.lms.dto.ExamDetailDto;
import jp.co.sss.lms.dto.ExamResultDetailDto;
import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.form.ExamQuestionForm;
import jp.co.sss.lms.service.ExamService;

/**
 * 試験コントローラー
 * 
 * @author 東京ITスクール
 */
@Controller
@RequestMapping("/exam")
public class ExamController {

	@Autowired
	private ExamService examService;
	@Autowired
	private LoginUserDto loginUserDto;

	/**
	 * 試験詳細画面 初期表示
	 * 
	 * @return 試験詳細画面
	 */
	@RequestMapping(path = "/detail", method = RequestMethod.POST)
	public String detail() {

		// 試験詳細画面に遷移
		return "exam/detail";
	}

	/**
	 * 試験結果詳細画面 初期表示
	 * 
	 * @param examResultId
	 * @param model
	 * @return 試験結果詳細画面
	 */
	@RequestMapping(path = "/resultDetail", method = RequestMethod.POST)
	public String resultDetail(Integer examResultId, Model model) {

		// 試験結果詳細の取得
		ExamResultDetailDto examResultDetailDto = examService.getExamResultDetail(examResultId);
		model.addAttribute("examResultDetailDto", examResultDetailDto);

		return "exam/resultDetail";
	}

	/**
	 * 試験開始画面 初期表示
	 * 
	 * @param examSectionId
	 * @param model
	 * @return 試験開始画面
	 */
	@RequestMapping(path = "/start", method = RequestMethod.POST)
	public String start(Integer examSectionId, Model model) {

		// 試験詳細DTOの取得
		ExamDetailDto examDetailDto = examService.getExamDetail(examSectionId,
				loginUserDto.getLmsUserId());

		// 公開前の試験ならエラー画面へ遷移
		Date now = new Date();
		if (examDetailDto != null && examDetailDto.getPublicDate().compareTo(now) >= 0) {
			return "illegal";
		}

		model.addAttribute("examDetailDto", examDetailDto);
		return "exam/start";
	}

	/**
	 * 試験開始画面 『試験を開始する』ボタン押下
	 * 
	 * @param examQuestionForm
	 * @return 試験問題画面
	 */
	@RequestMapping(path = "/question", method = RequestMethod.POST)
	public String question(@ModelAttribute ExamQuestionForm examQuestionForm) {

		// 試験問題画面フォームの設定
		examService.setExamQuestionForm(examQuestionForm);

		return "exam/question";
	}

	/**
	 * 試験問題画面 『確認画面へ進む』ボタン押下
	 * 
	 * @param examQuestionForm
	 * @param model
	 * @return 試験問題確認画面
	 */
	@RequestMapping(path = "/answerCheck", method = RequestMethod.POST)
	public String answerCheck(ExamQuestionForm examQuestionForm, Model model) {

		// 試験問題画面フォームの設定
		examService.setExamQuestionForm(examQuestionForm);

		return "exam/answerCheck";
	}

	/**
	 * 試験問題確認画面 『回答を送信する』ボタン押下
	 * 
	 * @param examQuestionForm
	 * @param model
	 * @return 試験結果画面
	 */
	@RequestMapping(path = "/result", method = RequestMethod.POST)
	public String complete(ExamQuestionForm examQuestionForm, Model model) {

		// 試験結果登録
		Integer examResultId = examService.insert(examQuestionForm);

		// 試験結果詳細の取得
		ExamResultDetailDto examResultDetailDto = examService.getExamResultDetail(examResultId);
		model.addAttribute("examResultDetailDto", examResultDetailDto);

		return "exam/result";
	}

}
