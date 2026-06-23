package jp.co.sss.lms.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import jp.co.sss.lms.service.MeetingService;

@Controller
@RequestMapping("/meeting")
public class MeetingController {

	@Autowired
	private MeetingService meetingService;

	/**
	 * ユーザー詳細画面 『ダウンロード（面談記録）』ボタン押下
	 * 
	 * @param meetingId
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(path = "/download", method = RequestMethod.POST)
	@ResponseBody
	public void download(Integer meetingId, HttpServletResponse response) throws IOException {

		// 面談ファイルのダウンロード
		meetingService.downloadMeetingFile(meetingId, response);
	}

}
