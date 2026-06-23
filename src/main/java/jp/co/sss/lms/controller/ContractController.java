package jp.co.sss.lms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jp.co.sss.lms.dto.AgreementConsentDto;
import jp.co.sss.lms.service.ContractService;

/**
 * 契約情報コントローラー
 * 
 * @author 東京ITスクール
 */
@Controller
@RequestMapping("/contract")
public class ContractController {

	@Autowired
	private ContractService contractService;

	/**
	 * 過去契約一覧画面 初期表示
	 * 
	 * @param model
	 * @return 過去契約一覧画面
	 */
	@RequestMapping(path = "/history/list")
	public String historyList(Model model) {

		// 契約同意情報の取得
		List<AgreementConsentDto> agreementConsentDtoList = contractService
				.getAgreementConsentDtoList();
		model.addAttribute("agreementConsentDtoList", agreementConsentDtoList);

		return "contract/history/list";
	}

}
