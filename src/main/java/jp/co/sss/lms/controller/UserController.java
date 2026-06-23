package jp.co.sss.lms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jp.co.sss.lms.dto.LmsUserDto;
import jp.co.sss.lms.service.UserService;
import jp.co.sss.lms.util.Constants;
import jp.co.sss.lms.util.MessageUtil;

/**
 * ユーザーコントローラー
 * 
 * @author 東京ITスクール
 */
@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private MessageUtil messageUtil;

	/**
	 * 利用規約画面 初期表示
	 * 
	 * @return 利用規約画面
	 */
	@RequestMapping(value = "/agreeSecurity")
	public String index() {
		return "user/agreeSecurity/index";
	}

	/**
	 * 利用規約画面 『次へ』ボタン押下
	 * 
	 * @param securityFlg
	 * @param model
	 * @return パスワード変更画面
	 */
	@RequestMapping(value = "/agreeSecurity/changePassword", method = RequestMethod.POST)
	public String agreeSecurity(Short securityFlg, Model model) {

		// 「同意します」に未チェックの場合
		if (securityFlg != Constants.CODE_VAL_SECURITY_AGREE) {
			model.addAttribute("error",
					messageUtil.getMessage(Constants.PROP_KEY_SECURITY_AGREE_FLG));
			return "user/agreeSecurity/index";
		}

		// 同意フラグを登録
		userService.updateSecurityFlg();

		return "redirect:/";
	}

	/**
	 * ユーザー詳細画面 初期表示
	 * 
	 * @param lmsUserId
	 * @param model
	 * @return ユーザー詳細画面
	 */
	@RequestMapping(path = "/detail")
	public String detail(@RequestParam(required = false) Integer lmsUserId, Model model) {

		// ユーザー詳細情報の取得
		LmsUserDto lmsUserDto = userService.getUserDetail(lmsUserId);
		model.addAttribute("lmsUserDto", lmsUserDto);

		return "user/detail";
	}

}
