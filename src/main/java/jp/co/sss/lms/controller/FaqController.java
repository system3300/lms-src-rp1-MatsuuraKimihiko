package jp.co.sss.lms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jp.co.sss.lms.form.FaqSearchForm;

/**
 * よくある質問コントローラー
 * 
 * @author 東京ITスクール
 */
@Controller
@RequestMapping("/faq")
public class FaqController {

	/**
	 * よくある質問画面 初期表示
	 * 
	 * @param faqSearchForm
	 * @param model
	 * @return よくある質問画面
	 */
	@RequestMapping(path = "")
	public String index(FaqSearchForm faqSearchForm, Model model) {

		return "faq/index";
	}

}
