package jp.co.sss.lms.controller;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jp.co.sss.lms.dto.AttendanceManagementDto;
import jp.co.sss.lms.dto.AttendanceStudentDto;
import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.form.AttendanceForm;
import jp.co.sss.lms.form.AttendanceStudentListForm;
import jp.co.sss.lms.service.CompanyService;
import jp.co.sss.lms.service.CourseService;
import jp.co.sss.lms.service.StudentAttendanceService;
import jp.co.sss.lms.util.AttendanceUtil;
import jp.co.sss.lms.util.Constants;
import jp.co.sss.lms.util.LoginUserUtil;

/**
 * 勤怠管理コントローラ
 * 
 * @author 東京ITスクール
 */
@Controller
@RequestMapping("/attendance")
public class AttendanceController {

	@Autowired
	private StudentAttendanceService studentAttendanceService;
	@Autowired
	private LoginUserDto loginUserDto;

	@Autowired
	private LoginUserUtil loginUserUtil;

	@Autowired
	private CourseService courseService;

	@Autowired
	private CompanyService companyService;

	@Value("${setting.search.pastTimeLabel}")
	private String pastTimeLabel;

	/**
	 * 勤怠管理画面 初期表示
	 * 
	 * @param lmsUserId
	 * @param courseId
	 * @param model
	 * @return 勤怠管理画面
	 * @throws ParseException
	 */
	@RequestMapping(path = "/detail", method = RequestMethod.GET)
	public String index(Model model) throws ParseException {

		// 勤怠一覧の取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
				.getAttendanceManagement(loginUserDto.getCourseId(), loginUserDto.getLmsUserId());
		model.addAttribute("attendanceManagementDtoList", attendanceManagementDtoList);

		//松浦公彦 - Task.25

		boolean notEnterFlg = studentAttendanceService.notEnterCheck();
		model.addAttribute("notEnterFlg", notEnterFlg);

		return "attendance/detail";
	}

	/**
	 * 勤怠管理画面 『出勤』ボタン押下
	 * 
	 * @param model
	 * @return 勤怠管理画面
	 */
	@RequestMapping(path = "/detail", params = "punchIn", method = RequestMethod.POST)
	public String punchIn(Model model) {

		// 更新前のチェック
		String error = studentAttendanceService.punchCheck(Constants.CODE_VAL_ATWORK);
		model.addAttribute("error", error);
		// 勤怠登録
		if (error == null) {
			String message = studentAttendanceService.setPunchIn();
			model.addAttribute("message", message);
		}
		// 一覧の再取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
				.getAttendanceManagement(loginUserDto.getCourseId(), loginUserDto.getLmsUserId());
		model.addAttribute("attendanceManagementDtoList", attendanceManagementDtoList);

		return "attendance/detail";
	}

	/**
	 * 勤怠管理画面 『退勤』ボタン押下
	 * 
	 * @param model
	 * @return 勤怠管理画面
	 */
	@RequestMapping(path = "/detail", params = "punchOut", method = RequestMethod.POST)
	public String punchOut(Model model) {

		// 更新前のチェック
		String error = studentAttendanceService.punchCheck(Constants.CODE_VAL_LEAVING);
		model.addAttribute("error", error);
		// 勤怠登録
		if (error == null) {
			String message = studentAttendanceService.setPunchOut();
			model.addAttribute("message", message);
		}
		// 一覧の再取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
				.getAttendanceManagement(loginUserDto.getCourseId(), loginUserDto.getLmsUserId());
		model.addAttribute("attendanceManagementDtoList", attendanceManagementDtoList);

		return "attendance/detail";
	}

	/**
	 * 勤怠管理画面 『勤怠情報を直接編集する』リンク押下
	 * 
	 * @param model
	 * @return 勤怠情報直接変更画面
	 */
	@RequestMapping(path = "/update")
	public String update(Model model) {

		// 勤怠管理リストの取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
				.getAttendanceManagement(loginUserDto.getCourseId(), loginUserDto.getLmsUserId());
		// 勤怠フォームの生成
		AttendanceForm attendanceForm = studentAttendanceService
				.setAttendanceForm(attendanceManagementDtoList);
		model.addAttribute("attendanceForm", attendanceForm);

		return "attendance/update";
	}

	/**
	 * 勤怠情報直接変更画面 『更新』ボタン押下
	 * 
	 * @param attendanceForm
	 * @param model
	 * @param result
	 * @return 勤怠管理画面
	 * @throws ParseException
	 */
	@RequestMapping(path = "/update", params = "complete", method = RequestMethod.POST)
	public String complete(AttendanceForm attendanceForm, Model model, BindingResult result)
			throws ParseException {

		//松浦公彦 -Task.26
		studentAttendanceService.formatConversion(attendanceForm);

		//松浦公彦 -Task.27
		studentAttendanceService.updateInputCheck(attendanceForm, result);
		if (result.hasErrors()) {
			AttendanceUtil attendanceUtil = new AttendanceUtil();
			attendanceForm.setBlankTimes(attendanceUtil.setBlankTime());
			attendanceForm.setHourMap(attendanceUtil.getHourMap());
			attendanceForm.setMinuteMap(attendanceUtil.getMinuteMap());

			model.addAttribute("attendanceForm", attendanceForm);

			return "attendance/update";
		}
		// 更新
		String message = studentAttendanceService.update(attendanceForm);

		model.addAttribute("message", message);
		// 一覧の再取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
				.getAttendanceManagement(loginUserDto.getCourseId(), loginUserDto.getLmsUserId());
		model.addAttribute("attendanceManagementDtoList", attendanceManagementDtoList);

		return "attendance/detail";
	}

	// Task.57
	@RequestMapping(path = "/list", method = RequestMethod.GET)
	public String list(
			AttendanceStudentListForm attendanceStudentListForm,
			Model model) {

		List<AttendanceStudentDto> attendanceStudentDtoList = studentAttendanceService.getAttendanceStudentList(
				attendanceStudentListForm.getCourseId(),
				attendanceStudentListForm.getCompanyId(),
				attendanceStudentListForm.getUserName(),
				attendanceStudentListForm.getPastFlg());

		// Task.57 未入力チェック
		for (AttendanceStudentDto dto : attendanceStudentDtoList) {

			int count = studentAttendanceService.checkNotEnter(
					dto.getLmsUserId());

			dto.setNotEnterFlg(count > 0);
		}

		model.addAttribute("companyList",
				companyService.getCompanyDto());

		model.addAttribute("courseList",
				courseService.getCourseDto());

		model.addAttribute(
				"placeName",
				loginUserDto.getPlaceName());

		model.addAttribute("pastTimeLabel", pastTimeLabel);

		model.addAttribute("isTeacher", loginUserUtil.isTeacher());

		model.addAttribute("isCompany", loginUserUtil.isCompany());

		model.addAttribute(
				"studentList",
				attendanceStudentDtoList);

		model.addAttribute(
				"attendanceStudentListForm",
				attendanceStudentListForm);

		return "attendance/list";
	}

	// Task.57
	@RequestMapping(path = "/list", params = "search", method = RequestMethod.POST)
	public String search(
			AttendanceStudentListForm attendanceStudentListForm,
			Model model) {
		System.out.println("pastFlg=" + attendanceStudentListForm.getPastFlg());
		//検索時の処理

		List<AttendanceStudentDto> attendanceStudentDtoList = studentAttendanceService.getAttendanceStudentList(
				attendanceStudentListForm.getCourseId(),
				attendanceStudentListForm.getCompanyId(),
				attendanceStudentListForm.getUserName(),
				attendanceStudentListForm.getPastFlg());

		// Task.57 未入力チェック
		for (AttendanceStudentDto dto : attendanceStudentDtoList) {

			int count = studentAttendanceService.checkNotEnter(
					dto.getLmsUserId());

			dto.setNotEnterFlg(count > 0);
		}

		model.addAttribute("companyList",
				companyService.getCompanyDto());

		model.addAttribute("courseList",
				courseService.getCourseDto());

		model.addAttribute(
				"placeName",
				loginUserDto.getPlaceName());

		model.addAttribute("pastTimeLabel", pastTimeLabel);

		model.addAttribute("isTeacher", loginUserUtil.isTeacher());

		model.addAttribute("isCompany", loginUserUtil.isCompany());

		model.addAttribute(
				"studentList",
				attendanceStudentDtoList);

		model.addAttribute(
				"attendanceStudentListForm",
				attendanceStudentListForm);

		return "attendance/list";
	}
}