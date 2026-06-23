package jp.co.sss.lms.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonParseException;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.lms.dto.InfoDto;
import jp.co.sss.lms.service.InfoService;
import net.arnx.jsonic.JSON;

/**
 * お知らせコントローラー
 * 
 * @author 東京ITスクール
 */
@Controller
@RequestMapping("/info")
public class InfoController {

	@Autowired
	private InfoService infoService;
	@Autowired
	private HttpSession session;

	/**
	 * お知らせ情報取得
	 * 
	 * @return お知らせ情報
	 * @throws JsonParseException
	 * @throws IOException
	 */
	@RequestMapping(path = "")
	@ResponseBody
	public String index() throws JsonParseException, IOException {

		// お知らせ情報を取得
		InfoDto infoDto = infoService.getInfo();
		// セッションに格納
		session.setAttribute("infoDto", infoDto);
		// JSON形式で返す
		return JSON.encode(infoDto);

	}

}
