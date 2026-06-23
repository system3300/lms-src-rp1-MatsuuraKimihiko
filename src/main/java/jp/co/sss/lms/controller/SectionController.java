package jp.co.sss.lms.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import jp.co.sss.lms.form.SectionDetailForm;
import jp.co.sss.lms.service.SectionService;

/**
 * セクションコントローラー
 * 
 * @author 東京ITスクール
 */
@Controller
@RequestMapping("/section")
public class SectionController {

	@Autowired
	private SectionService sectionService;

	/**
	 * セクション詳細画面 初期表示
	 * 
	 * @param sectionId セクションID
	 * @param model     モデル
	 * @return セクション詳細画面
	 * @throws ParseException
	 */
	@RequestMapping("/detail")
	public String detail(@ModelAttribute SectionDetailForm sectionDetailForm)
			throws ParseException {

		// パラメータチェック
		String message = sectionService.checkSectionId(sectionDetailForm.getSectionId());
		if (!message.isEmpty()) {
			return "illegal";
		}
		// セクション詳細情報の取得
		sectionService.getSectionDetail(sectionDetailForm);

		return "section/detail";
	}

}
