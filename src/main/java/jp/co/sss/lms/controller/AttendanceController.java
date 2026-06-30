package jp.co.sss.lms.controller;

import java.text.ParseException;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jp.co.sss.lms.dto.AttendanceManagementDto;
import jp.co.sss.lms.dto.AttendanceStudentDto;
import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.form.AttendanceForm;
import jp.co.sss.lms.form.AttendanceStudentListForm;
import jp.co.sss.lms.form.BulkRegistForm;
import jp.co.sss.lms.service.CompanyService;
import jp.co.sss.lms.service.CourseService;
import jp.co.sss.lms.service.StudentAttendanceService;
import jp.co.sss.lms.util.AttendanceUtil;
import jp.co.sss.lms.util.Constants;
import jp.co.sss.lms.util.LoginUserUtil;
import jp.co.sss.lms.util.MessageUtil;

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

	@Autowired
	private MessageUtil messageUtil;

	@Value("${setting.search.pastTimeLabel}")
	private String pastTimeLabel;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.setAutoGrowCollectionLimit(1000);
	}

	/**
	 * 勤怠管理画面 初期表示
	 * 
	 * @param attendanceStudentListForm
	 * @param model
	 * @return 勤怠管理画面
	 * @throws ParseException
	 */
	@RequestMapping(path = "/detail", method = RequestMethod.GET)
	public String index(AttendanceStudentListForm attendanceStudentListForm, Model model) throws ParseException {
		// 勤怠一覧の取得
		if (loginUserUtil.isStudent()) {
			List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
					.getAttendanceManagement(loginUserDto.getCourseId(), loginUserDto.getLmsUserId());
			model.addAttribute("attendanceManagementDtoList", attendanceManagementDtoList);

			//松浦公彦 - Task.25

			boolean notEnterFlg = studentAttendanceService.notEnterCheck();
			model.addAttribute("notEnterFlg", notEnterFlg);
			model.addAttribute("isStudent", loginUserUtil.isStudent());

		} //松浦公彦 - Task.72
		else {

			Integer courceId = attendanceStudentListForm.getCourseId();
			Integer lmsUserId = attendanceStudentListForm.getLmsUserId();

			// 受講生情報取得

			List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
					.getAttendanceManagement(courceId, lmsUserId);

			System.out.println("courseId=" + attendanceStudentListForm.getCourseId());
			System.out.println("lmsUserId=" + attendanceStudentListForm.getLmsUserId());
			System.out.println("companyId=" + attendanceStudentListForm.getCompanyId());

			// modelへ設定
			model.addAttribute("attendanceManagementDtoList", attendanceManagementDtoList);
			model.addAttribute("attendanceStudentListForm", attendanceStudentListForm);
		}

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
		model.addAttribute("isStudent", loginUserUtil.isStudent());

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
		model.addAttribute("isStudent", loginUserUtil.isStudent());

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
		model.addAttribute("isStudent", loginUserUtil.isStudent());

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
		// 入力チェック
		studentAttendanceService.updateInputCheck(attendanceForm, result);
		if (result.hasErrors()) {
			// エラー時にプルダウンを再設定
			AttendanceUtil attendanceUtil = new AttendanceUtil();
			attendanceForm.setBlankTimes(attendanceUtil.setBlankTime());
			attendanceForm.setHourMap(attendanceUtil.getHourMap());
			attendanceForm.setMinuteMap(attendanceUtil.getMinuteMap());

			model.addAttribute("attendanceForm", attendanceForm);
			model.addAttribute("isStudent", loginUserUtil.isStudent());

			return "attendance/update";
		}
		// 更新
		String message = studentAttendanceService.update(attendanceForm);

		model.addAttribute("message", message);
		// 一覧の再取得
		List<AttendanceManagementDto> attendanceManagementDtoList = studentAttendanceService
				.getAttendanceManagement(loginUserDto.getCourseId(), loginUserDto.getLmsUserId());
		model.addAttribute("attendanceManagementDtoList", attendanceManagementDtoList);
		model.addAttribute("isStudent", loginUserUtil.isStudent());

		return "attendance/detail";
	}

	// Task.57
	/**
	 * 勤怠情報確認画面(受講生一覧)
	 * 
	 * @param attendanceStudentListForm
	 * @param model
	 * @return 勤怠情報確認画面(受講生一覧)
	 */
	@RequestMapping(path = "/list", method = RequestMethod.GET)
	public String list(
			AttendanceStudentListForm attendanceStudentListForm,
			Model model) {
		// 検索条件に一致する受講生一覧を取得
		List<AttendanceStudentDto> attendanceStudentDtoList = studentAttendanceService.getAttendanceStudentList(
				attendanceStudentListForm.getCourseId(),
				attendanceStudentListForm.getCompanyId(),
				attendanceStudentListForm.getUserName(),
				attendanceStudentListForm.getPastFlg());

		// Task.57 未入力チェック
		for (AttendanceStudentDto dto : attendanceStudentDtoList) {

			int count = studentAttendanceService.checkNotEnter(
					dto.getLmsUserId());

			// 未入力データが1件以上ある場合はフラグをON
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

		model.addAttribute("isAdmin", loginUserUtil.isAdmin());

		model.addAttribute(
				"studentList",
				attendanceStudentDtoList);

		model.addAttribute(
				"attendanceStudentListForm",
				attendanceStudentListForm);

		return "attendance/list";
	}

	// Task.57
	/**
	 * 勤怠情報確認画面(受講生一覧) 検索時の表示
	 * 
	 * @param attendanceStudentListForm
	 * @param model
	 * @return 勤怠情報確認画面(受講生一覧)
	 */
	@RequestMapping(path = "/list", params = "search", method = RequestMethod.POST)
	public String search(
			AttendanceStudentListForm attendanceStudentListForm,
			Model model) {

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

			// 未入力データが1件以上ある場合はフラグをON
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
		model.addAttribute("isAdmin", loginUserUtil.isAdmin());

		model.addAttribute(
				"studentList",
				attendanceStudentDtoList);

		model.addAttribute(
				"attendanceStudentListForm",
				attendanceStudentListForm);

		return "attendance/list";
	}

	/**
	 *  勤怠一括登録画面 初期表示
	 * 
	 * @param model
	 * @return 勤怠一括登録画面
	 */
	@RequestMapping(path = "/bulkRegist")
	public String bulkRegist(
			BulkRegistForm bulkRegistForm,
			Model model) {
		if (loginUserUtil.isTeacher() || loginUserUtil.isAdmin()) {
			// 初期表示処理
			studentAttendanceService.setBulkRegistForm(bulkRegistForm);

			model.addAttribute("bulkRegistForm", bulkRegistForm);
		}

		return "attendance/bulkRegist";
	}

	/**
	 *  勤怠一括登録画面 検索時の表示
	 * 
	 * @param model
	 * @return 勤怠一括登録画面
	 */
	@RequestMapping(path = "/bulkRegist/search", method = RequestMethod.POST)
	public String search(
			@ModelAttribute @Valid BulkRegistForm bulkRegistForm,
			BindingResult result,
			Model model) {
		if (loginUserUtil.isTeacher() || loginUserUtil.isAdmin()) {
			// ① 入力チェック
			studentAttendanceService.searchInputCheck(bulkRegistForm, result);
			studentAttendanceService.setBulkRegistForm(bulkRegistForm);

			if (result.hasErrors()) {

				// エラー時に画面表示用データを再設定
				studentAttendanceService.setBulkRegistForm(bulkRegistForm);

				model.addAttribute("companyList", companyService.getCompanyDto());
				model.addAttribute("courseList", courseService.getCourseDto());
				model.addAttribute("placeName", loginUserDto.getPlaceName());
				model.addAttribute("isTeacher", loginUserUtil.isTeacher());
				model.addAttribute("isCompany", loginUserUtil.isCompany());
				model.addAttribute("isAdmin", loginUserUtil.isAdmin());

				model.addAttribute("bulkRegistForm", bulkRegistForm);

				return "attendance/bulkRegist";
			}

			// ② 勤怠取得＋変換
			studentAttendanceService.getUserAttendance(bulkRegistForm);
			model.addAttribute("bulkRegistForm", bulkRegistForm);
		}

		return "attendance/bulkRegist";
	}

	//Task.58 
	/**
	 *  勤怠一括登録画面 登録完了時の表示
	 * 
	 * @param model
	 * @return 勤怠一括登録画面
	 */
	@RequestMapping(path = "/bulkRegist/complete", method = RequestMethod.POST)
	public String complete(
			@ModelAttribute @Valid BulkRegistForm bulkRegistForm,
			BindingResult result,
			Model model) throws ParseException {
		//ログインユーザーが講師の場合
		if (loginUserUtil.isTeacher() || loginUserUtil.isAdmin()) {
			// 一括更新時の入力チェック
			studentAttendanceService.bulkRegistInputCheck(bulkRegistForm, result);

			if (result.hasErrors()) {
				// エラー時は画面表示用データを再設定
				studentAttendanceService.setBulkRegistForm(bulkRegistForm);

				model.addAttribute("companyList", companyService.getCompanyDto());
				model.addAttribute("courseList", courseService.getCourseDto());
				model.addAttribute("placeName", loginUserDto.getPlaceName());
				model.addAttribute("isTeacher", loginUserUtil.isTeacher());
				model.addAttribute("isCompany", loginUserUtil.isCompany());
				model.addAttribute("isAdmin", loginUserUtil.isAdmin());

				model.addAttribute("bulkRegistForm", bulkRegistForm);

				return "attendance/bulkRegist";
			} else {
				// 勤怠情報を一括更新
				studentAttendanceService.bulkUpdate(bulkRegistForm);
				// 更新後の最新データを再取得
				studentAttendanceService.getUserAttendance(bulkRegistForm);

				model.addAttribute("companyList", companyService.getCompanyDto());
				model.addAttribute("courseList", courseService.getCourseDto());
				model.addAttribute("placeName", loginUserDto.getPlaceName());
				model.addAttribute("isTeacher", loginUserUtil.isTeacher());
				model.addAttribute("isCompany", loginUserUtil.isCompany());
				model.addAttribute("isAdmin", loginUserUtil.isAdmin());

			}
			// 完了メッセージを設定
			model.addAttribute("bulkRegistForm", bulkRegistForm);
			model.addAttribute("message", messageUtil.getMessage("regist.complete", new String[] { "勤怠情報" }));
		}

		return "attendance/bulkRegist";
	}
}
