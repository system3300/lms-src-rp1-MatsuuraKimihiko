package jp.co.sss.lms.controller;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import jp.co.sss.lms.form.DailyReportDownloadForm;
import jp.co.sss.lms.form.DailyReportSubmitForm;
import jp.co.sss.lms.service.ReportService;

/**
 * レポートコントローラー
 * 
 * @author 東京ITスクール
 */
@Controller
@RequestMapping("/report")
public class ReportController {

	@Autowired
	private ReportService reportService;

	/**
	 * レポート登録画面 初期表示
	 * 
	 * @param dailyReportSubmitForm
	 * @return レポート登録画面
	 * @throws ParseException
	 */
	@RequestMapping(path = "/regist", method = RequestMethod.POST)
	public String submit(@ModelAttribute DailyReportSubmitForm dailyReportSubmitForm)
			throws ParseException {

		// レポート情報取得
		reportService.getDailyReport(dailyReportSubmitForm);

		return "report/regist";
	}

	/**
	 * レポート登録画面 『提出する』ボタン押下
	 * 
	 * @param dailyReportSubmitForm
	 * @return セクション詳細画面
	 * @throws ParseException
	 */
	@RequestMapping(path = "/complete", method = RequestMethod.POST)
	public String complete(DailyReportSubmitForm dailyReportSubmitForm, BindingResult result)
			throws ParseException {

		// レポート登録/更新
		reportService.submit(dailyReportSubmitForm);

		return "redirect:/section/detail?sectionId=" + dailyReportSubmitForm.getSectionId();
	}

	/**
	 * レポート詳細画面の初期表示
	 * 
	 * @return レポート詳細画面
	 */
	@RequestMapping(path = "/detail")
	public String detail() {

		return "/report/detail";
	}

	/**
	 * ユーザー詳細画面 『ダウンロード（レポート）』ボタン押下
	 * 
	 * @param reportDownloadForm
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(path = "/download", method = RequestMethod.POST)
	@ResponseBody
	public void download(DailyReportDownloadForm dailyReportDownloadForm,
			HttpServletResponse response) throws IOException {

		// レポートダウンロード
		reportService.download(dailyReportDownloadForm.getDailyReportId(),
				dailyReportDownloadForm.getDailyReportSubmitId(), response);
	}

}
