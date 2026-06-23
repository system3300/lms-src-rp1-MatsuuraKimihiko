package jp.co.sss.lms.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;

import jp.co.sss.lms.dto.AttendanceManagementDto;
import jp.co.sss.lms.dto.AttendanceStudentDto;
import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.dto.UserAttendanceDto;
import jp.co.sss.lms.entity.MPlace;
import jp.co.sss.lms.entity.TCompanyAttendance;
import jp.co.sss.lms.entity.TStudentAttendance;
import jp.co.sss.lms.enums.AttendanceStatusEnum;
import jp.co.sss.lms.form.AttendanceForm;
import jp.co.sss.lms.form.BulkRegistForm;
import jp.co.sss.lms.form.DailyAttendanceForm;
import jp.co.sss.lms.mapper.MLmsUserMapper;
import jp.co.sss.lms.mapper.MPlaceMapper;
import jp.co.sss.lms.mapper.TCompanyAttendanceMapper;
import jp.co.sss.lms.mapper.TStudentAttendanceMapper;
import jp.co.sss.lms.util.AttendanceUtil;
import jp.co.sss.lms.util.Constants;
import jp.co.sss.lms.util.DateUtil;
import jp.co.sss.lms.util.LoginUserUtil;
import jp.co.sss.lms.util.MessageUtil;
import jp.co.sss.lms.util.TrainingTime;

/**
 * 勤怠情報（受講生入力）サービス
 * 
 * @author 東京ITスクール
 */
@Service
public class StudentAttendanceService {

	@Autowired
	private DateUtil dateUtil;
	@Autowired
	private AttendanceUtil attendanceUtil;
	@Autowired
	private MessageUtil messageUtil;
	@Autowired
	private LoginUserUtil loginUserUtil;
	@Autowired
	private LoginUserDto loginUserDto;
	@Autowired
	private TStudentAttendanceMapper tStudentAttendanceMapper;

	@Autowired
	private MLmsUserMapper mLmsUserMapper;

	@Autowired
	private MPlaceMapper mPlaceMapper;

	@Autowired
	private PlaceService placeService;

	@Autowired
	private TCompanyAttendanceMapper tCompanyAttendanceMapper;

	@Value("${setting.search.pastTime}")
	private Integer pastTime;

	/**
	 * 勤怠一覧情報取得
	 * 
	 * @param courseId
	 * @param lmsUserId
	 * @return 勤怠管理画面用DTOリスト
	 */
	public List<AttendanceManagementDto> getAttendanceManagement(Integer courseId,
			Integer lmsUserId) {

		List<AttendanceManagementDto> attendanceManagementDtoList = tStudentAttendanceMapper
				.getAttendanceManagement(courseId, lmsUserId, Constants.DB_FLG_FALSE);

		for (AttendanceManagementDto dto : attendanceManagementDtoList) {

			if (dto.getBlankTime() != null) {
				TrainingTime blankTime = attendanceUtil.calcBlankTime(dto.getBlankTime());
				dto.setBlankTimeValue(String.valueOf(blankTime));
			}

			AttendanceStatusEnum statusEnum = AttendanceStatusEnum.getEnum(dto.getStatus());

			if (statusEnum != null) {
				dto.setStatusDispName(statusEnum.name);
			}
		}

		return attendanceManagementDtoList;
	}

	/**
	 * 出退勤更新前のチェック
	 * 
	 * @param attendanceType
	 * @return エラーメッセージ
	 */
	public String punchCheck(Short attendanceType) {
		Date trainingDate = attendanceUtil.getTrainingDate();
		// 権限チェック
		if (!loginUserUtil.isStudent()) {
			return messageUtil.getMessage(Constants.VALID_KEY_AUTHORIZATION);
		}
		// 研修日チェック
		if (!attendanceUtil.isWorkDay(loginUserDto.getCourseId(), trainingDate)) {
			return messageUtil.getMessage(Constants.VALID_KEY_ATTENDANCE_NOTWORKDAY);
		}
		// 登録情報チェック
		TStudentAttendance tStudentAttendance = tStudentAttendanceMapper
				.findByLmsUserIdAndTrainingDate(loginUserDto.getLmsUserId(), trainingDate,
						Constants.DB_FLG_FALSE);
		switch (attendanceType) {
		case Constants.CODE_VAL_ATWORK:
			if (tStudentAttendance != null
					&& !tStudentAttendance.getTrainingStartTime().equals("")) {
				// 本日の勤怠情報は既に入力されています。直接編集してください。
				return messageUtil.getMessage(Constants.VALID_KEY_ATTENDANCE_PUNCHALREADYEXISTS);
			}
			break;
		case Constants.CODE_VAL_LEAVING:
			if (tStudentAttendance == null
					|| tStudentAttendance.getTrainingStartTime().equals("")) {
				// 出勤情報がないため退勤情報を入力出来ません。
				return messageUtil.getMessage(Constants.VALID_KEY_ATTENDANCE_PUNCHINEMPTY);
			}
			if (!tStudentAttendance.getTrainingEndTime().equals("")) {
				// 本日の勤怠情報は既に入力されています。直接編集してください。
				return messageUtil.getMessage(Constants.VALID_KEY_ATTENDANCE_PUNCHALREADYEXISTS);
			}
			TrainingTime trainingStartTime = new TrainingTime(
					tStudentAttendance.getTrainingStartTime());
			TrainingTime trainingEndTime = new TrainingTime();
			if (trainingStartTime.compareTo(trainingEndTime) > 0) {
				// 退勤時刻は出勤時刻より後でなければいけません。
				return messageUtil.getMessage(Constants.VALID_KEY_ATTENDANCE_TRAININGTIMERANGE);
			}
			break;
		}
		return null;
	}

	/**
	 * 出勤ボタン処理
	 * 
	 * @return 完了メッセージ
	 */
	public String setPunchIn() {

		Date date = new Date();
		Date trainingDate = attendanceUtil.getTrainingDate();
		TrainingTime trainingStartTime = new TrainingTime();

		AttendanceStatusEnum attendanceStatusEnum = attendanceUtil.getStatus(trainingStartTime, null);

		TStudentAttendance tStudentAttendance = tStudentAttendanceMapper.findByLmsUserIdAndTrainingDate(
				loginUserDto.getLmsUserId(),
				trainingDate,
				Constants.DB_FLG_FALSE);

		if (tStudentAttendance == null) {

			tStudentAttendance = new TStudentAttendance();
			tStudentAttendance.setLmsUserId(loginUserDto.getLmsUserId());
			tStudentAttendance.setTrainingDate(trainingDate);
			tStudentAttendance.setTrainingStartTime(trainingStartTime.toString());
			tStudentAttendance.setTrainingEndTime("");
			tStudentAttendance.setStatus(attendanceStatusEnum.code);
			tStudentAttendance.setNote("");
			tStudentAttendance.setAccountId(loginUserDto.getAccountId());
			tStudentAttendance.setDeleteFlg(Constants.DB_FLG_FALSE);
			tStudentAttendance.setFirstCreateUser(loginUserDto.getLmsUserId());
			tStudentAttendance.setFirstCreateDate(date);
			tStudentAttendance.setLastModifiedUser(loginUserDto.getLmsUserId());
			tStudentAttendance.setLastModifiedDate(date);
			tStudentAttendance.setBlankTime(null);

			tStudentAttendanceMapper.insert(tStudentAttendance);

		} else {

			tStudentAttendance.setTrainingStartTime(trainingStartTime.toString());
			tStudentAttendance.setStatus(attendanceStatusEnum.code);
			tStudentAttendance.setLastModifiedUser(loginUserDto.getLmsUserId());
			tStudentAttendance.setLastModifiedDate(date);

			tStudentAttendanceMapper.update(tStudentAttendance);
		}

		return messageUtil.getMessage(Constants.PROP_KEY_ATTENDANCE_UPDATE_NOTICE);
	}

	/**
	 * 退勤ボタン処理
	 * 
	 * @return 完了メッセージ
	 */
	public String setPunchOut() {
		// 当日日付
		Date date = new Date();
		// 本日の研修日
		Date trainingDate = attendanceUtil.getTrainingDate();
		// 研修日の勤怠情報取得
		TStudentAttendance tStudentAttendance = tStudentAttendanceMapper
				.findByLmsUserIdAndTrainingDate(loginUserDto.getLmsUserId(), trainingDate,
						Constants.DB_FLG_FALSE);
		// 出退勤時刻
		TrainingTime trainingStartTime = new TrainingTime(
				tStudentAttendance.getTrainingStartTime());
		TrainingTime trainingEndTime = new TrainingTime();
		// 遅刻早退ステータス
		AttendanceStatusEnum attendanceStatusEnum = attendanceUtil.getStatus(trainingStartTime,
				trainingEndTime);
		// 更新処理
		tStudentAttendance.setTrainingEndTime(trainingEndTime.toString());
		tStudentAttendance.setStatus(attendanceStatusEnum.code);
		tStudentAttendance.setDeleteFlg(Constants.DB_FLG_FALSE);
		tStudentAttendance.setLastModifiedUser(loginUserDto.getLmsUserId());
		tStudentAttendance.setLastModifiedDate(date);
		tStudentAttendanceMapper.update(tStudentAttendance);
		// 完了メッセージ
		return messageUtil.getMessage(Constants.PROP_KEY_ATTENDANCE_UPDATE_NOTICE);
	}

	/**
	 * 	過去日の未入力チェック
	 * @author 松浦公彦 - Task.25
	 * @return 過去日の未入力情報
	 * @throws ParseException
	 */
	public boolean notEnterCheck() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
		Date trainingDate = attendanceUtil.getTrainingDate();
		sdf.format(trainingDate);
		Integer count = tStudentAttendanceMapper.notEnterCount(loginUserDto.getLmsUserId(), Constants.DB_FLG_FALSE,
				trainingDate);
		if (count > 0) {

			return true;
		} else {
			return false;
		}

	}

	/**
	 * 勤怠フォームへ設定
	 * 
	 * @param attendanceManagementDtoList
	 * @return 勤怠編集フォーム
	 *  出勤・退勤時間の入力方法変更
	 * @author 松浦公彦 - Task.26
	 */
	public AttendanceForm setAttendanceForm(
			List<AttendanceManagementDto> attendanceManagementDtoList) {

		AttendanceForm attendanceForm = new AttendanceForm();
		attendanceForm.setAttendanceList(new ArrayList<DailyAttendanceForm>());
		attendanceForm.setLmsUserId(loginUserDto.getLmsUserId());
		attendanceForm.setUserName(loginUserDto.getUserName());
		attendanceForm.setLeaveFlg(loginUserDto.getLeaveFlg());
		attendanceForm.setBlankTimes(attendanceUtil.setBlankTime());

		//松浦公彦 -Task.26

		attendanceForm.setHourMap(attendanceUtil.getHourMap());
		attendanceForm.setMinuteMap(attendanceUtil.getMinuteMap());

		// 途中退校している場合のみ設定
		if (loginUserDto.getLeaveDate() != null) {
			attendanceForm
					.setLeaveDate(dateUtil.dateToString(loginUserDto.getLeaveDate(), "yyyy-MM-dd"));
			attendanceForm.setDispLeaveDate(
					dateUtil.dateToString(loginUserDto.getLeaveDate(), "yyyy年M月d日"));
		}

		// 勤怠管理リストの件数分、日次の勤怠フォームに移し替え
		for (AttendanceManagementDto attendanceManagementDto : attendanceManagementDtoList) {
			DailyAttendanceForm dailyAttendanceForm = new DailyAttendanceForm();
			dailyAttendanceForm
					.setStudentAttendanceId(attendanceManagementDto.getStudentAttendanceId());
			dailyAttendanceForm
					.setTrainingDate(dateUtil.toString(attendanceManagementDto.getTrainingDate()));
			dailyAttendanceForm
					.setTrainingStartTime(attendanceManagementDto.getTrainingStartTime());
			dailyAttendanceForm.setTrainingEndTime(attendanceManagementDto.getTrainingEndTime());

			//Task.26
			dailyAttendanceForm.setTrainingStartTimeHour(
					attendanceUtil.getStartHour(attendanceManagementDto.getTrainingStartTime()));

			dailyAttendanceForm.setTrainingStartTimeMinute(
					attendanceUtil.getStartMinute(attendanceManagementDto.getTrainingStartTime()));

			dailyAttendanceForm.setTrainingEndTimeHour(
					attendanceUtil.getEndHour(attendanceManagementDto.getTrainingEndTime()));

			dailyAttendanceForm.setTrainingEndTimeMinute(
					attendanceUtil.getEndMinute(attendanceManagementDto.getTrainingEndTime()));

			if (attendanceManagementDto.getBlankTime() != null) {
				dailyAttendanceForm.setBlankTime(attendanceManagementDto.getBlankTime());
				dailyAttendanceForm.setBlankTimeValue(String.valueOf(
						attendanceUtil.calcBlankTime(attendanceManagementDto.getBlankTime())));
			}
			dailyAttendanceForm.setStatus(String.valueOf(attendanceManagementDto.getStatus()));
			dailyAttendanceForm.setNote(attendanceManagementDto.getNote());
			dailyAttendanceForm.setSectionName(attendanceManagementDto.getSectionName());
			dailyAttendanceForm.setIsToday(attendanceManagementDto.getIsToday());
			dailyAttendanceForm.setDispTrainingDate(dateUtil
					.dateToString(attendanceManagementDto.getTrainingDate(), "yyyy年M月d日(E)"));
			dailyAttendanceForm.setStatusDispName(attendanceManagementDto.getStatusDispName());

			attendanceForm.getAttendanceList().add(dailyAttendanceForm);
		}

		return attendanceForm;
	}

	/**
	 * 勤怠登録・更新処理
	 * 
	 * @param attendanceForm
	 * @return 完了メッセージ
	 * @throws ParseException
	 */
	public String update(AttendanceForm attendanceForm) throws ParseException {

		Integer lmsUserId = loginUserUtil.isStudent() ? loginUserDto.getLmsUserId()
				: attendanceForm.getLmsUserId();

		// 現在の勤怠情報（受講生入力）リストを取得
		List<TStudentAttendance> tStudentAttendanceList = tStudentAttendanceMapper
				.findByLmsUserId(lmsUserId, Constants.DB_FLG_FALSE);

		// 入力された情報を更新用のエンティティに移し替え
		Date date = new Date();
		for (DailyAttendanceForm dailyAttendanceForm : attendanceForm.getAttendanceList()) {

			// 更新用エンティティ作成
			TStudentAttendance tStudentAttendance = new TStudentAttendance();
			// 日次勤怠フォームから更新用のエンティティにコピー
			BeanUtils.copyProperties(dailyAttendanceForm, tStudentAttendance);
			// 研修日付
			tStudentAttendance
					.setTrainingDate(dateUtil.parse(dailyAttendanceForm.getTrainingDate()));
			// 現在の勤怠情報リストのうち、研修日が同じものを更新用エンティティで上書き
			for (TStudentAttendance entity : tStudentAttendanceList) {
				if (entity.getTrainingDate().equals(tStudentAttendance.getTrainingDate())) {
					tStudentAttendance = entity;
					break;
				}
			}
			tStudentAttendance.setLmsUserId(lmsUserId);
			tStudentAttendance.setAccountId(loginUserDto.getAccountId());
			// 出勤時刻整形
			TrainingTime trainingStartTime = null;
			trainingStartTime = new TrainingTime(dailyAttendanceForm.getTrainingStartTime());
			tStudentAttendance.setTrainingStartTime(trainingStartTime.getFormattedString());
			// 退勤時刻整形
			TrainingTime trainingEndTime = null;
			trainingEndTime = new TrainingTime(dailyAttendanceForm.getTrainingEndTime());
			tStudentAttendance.setTrainingEndTime(trainingEndTime.getFormattedString());
			// 中抜け時間
			tStudentAttendance.setBlankTime(dailyAttendanceForm.getBlankTime());
			// 遅刻早退ステータス
			if ((trainingStartTime != null || trainingEndTime != null)
					&& !dailyAttendanceForm.getStatusDispName().equals("欠席")) {
				AttendanceStatusEnum attendanceStatusEnum = attendanceUtil
						.getStatus(trainingStartTime, trainingEndTime);
				tStudentAttendance.setStatus(attendanceStatusEnum.code);
			}
			// 備考
			tStudentAttendance.setNote(dailyAttendanceForm.getNote());
			// 更新者と更新日時
			tStudentAttendance.setLastModifiedUser(loginUserDto.getLmsUserId());
			tStudentAttendance.setLastModifiedDate(date);
			// 削除フラグ
			tStudentAttendance.setDeleteFlg(Constants.DB_FLG_FALSE);
			// 登録用Listへ追加
			tStudentAttendanceList.add(tStudentAttendance);
		}
		// 登録・更新処理
		for (TStudentAttendance tStudentAttendance : tStudentAttendanceList) {
			if (tStudentAttendance.getStudentAttendanceId() == null) {
				tStudentAttendance.setFirstCreateUser(loginUserDto.getLmsUserId());
				tStudentAttendance.setFirstCreateDate(date);
				tStudentAttendanceMapper.insert(tStudentAttendance);
			} else {
				tStudentAttendanceMapper.update(tStudentAttendance);
			}
		}
		// 完了メッセージ
		return messageUtil.getMessage(Constants.PROP_KEY_ATTENDANCE_UPDATE_NOTICE);
	}

	// @author 松浦公彦 - Task.26
	public void formatConversion(AttendanceForm attendanceForm) {
		for (DailyAttendanceForm dailyAttendanceForm : attendanceForm.getAttendanceList()) {
			if (dailyAttendanceForm.getTrainingStartTimeHour() != null
					&& dailyAttendanceForm.getTrainingStartTimeMinute() != null) {
				String startTime = String.format("%02d:%02d",
						dailyAttendanceForm.getTrainingStartTimeHour(),
						dailyAttendanceForm.getTrainingStartTimeMinute());
				dailyAttendanceForm.setTrainingStartTime(startTime);
			}
			if (dailyAttendanceForm.getTrainingEndTimeHour() != null
					&& dailyAttendanceForm.getTrainingEndTimeMinute() != null) {
				String endTime = String.format("%02d:%02d",
						dailyAttendanceForm.getTrainingEndTimeHour(),
						dailyAttendanceForm.getTrainingEndTimeMinute());
				dailyAttendanceForm.setTrainingEndTime(endTime);
			}
		}
	}

	// @author 松浦公彦 - Task.27
	public void updateInputCheck(AttendanceForm attendanceForm, BindingResult result) {

		for (int i = 0; i < attendanceForm.getAttendanceList().size(); i++) {

			DailyAttendanceForm dailyAttendanceForm = attendanceForm.getAttendanceList().get(i);

			// a. 備考100文字超過
			if (dailyAttendanceForm.getNote() != null && dailyAttendanceForm.getNote().length() > 100) {
				result.rejectValue(
						"attendanceList[" + i + "].note",
						"maxlength",
						new Object[] { "備考", "100" },
						null);
			}

			Integer startHour = dailyAttendanceForm.getTrainingStartTimeHour();
			Integer startMin = dailyAttendanceForm.getTrainingStartTimeMinute();
			Integer endHour = dailyAttendanceForm.getTrainingEndTimeHour();
			Integer endMin = dailyAttendanceForm.getTrainingEndTimeMinute();

			boolean startHourInput = startHour != null;
			boolean startMinInput = startMin != null;
			boolean endHourInput = endHour != null;
			boolean endMinInput = endMin != null;

			// b. 出勤時間の時・分どちらかのみ入力
			if (startHourInput && !startMinInput) {
				result.rejectValue(
						"attendanceList[" + i + "].trainingStartTimeMinute",
						"input.invalid",
						new Object[] { "出勤時間" },
						null);
			}

			if (!startHourInput && startMinInput) {
				result.rejectValue(
						"attendanceList[" + i + "].trainingStartTimeHour",
						"input.invalid",
						new Object[] { "出勤時間" },
						null);
			}

			// c. 退勤時間の時・分どちらかのみ入力
			if (endHourInput && !endMinInput) {

				result.rejectValue(
						"attendanceList[" + i + "].trainingEndTimeMinute",
						"input.invalid",
						new Object[] { "退勤時間" },
						null);
			}

			if (!endHourInput && endMinInput) {

				result.rejectValue(
						"attendanceList[" + i + "].trainingEndTimeHour",
						"input.invalid",
						new Object[] { "退勤時間" },
						null);
			}
			// d. 出勤時間なし・退勤時間あり
			boolean startInput = startHourInput && startMinInput;
			boolean endInput = endHourInput && endMinInput;

			if (!startHourInput
					&& !startMinInput
					&& endInput) {

				result.rejectValue(
						"attendanceList[" + i + "].trainingStartTimeHour",
						"attendance.punchInEmpty");
			}

			// e, f は時刻が正しく入力されている場合のみ実施
			if (startInput && endInput) {

				int startTime = startHour * 60 + startMin;
				int endTime = endHour * 60 + endMin;
				// e. 出勤時間 > 退勤時間
				if (startTime > endTime) {
					result.rejectValue(
							"attendanceList[" + i + "].trainingEndTimeHour",
							"attendance.trainingTimeRange",
							new Object[] { i + 1 },
							null);
				}
				// f. 休憩時間 > 勤務時間
				if (dailyAttendanceForm.getBlankTime() != null
						&& dailyAttendanceForm.getBlankTime() > (endTime - startTime)) {

					result.rejectValue(
							"attendanceList[" + i + "].blankTime",
							"attendance.blankTimeError");
				}
			}

		}

	}

	//Task.57
	public List<AttendanceStudentDto> getAttendanceStudentList(
			Integer courseId,
			Integer companyId,
			String userName,
			Integer pastFlg) {

		// 講師以外は取得不可
		if (loginUserUtil.isTeacher()) {

			// 未指定ならログインユーザーの会場IDを利用
			Integer placeId = loginUserDto.getPlaceId();

			List<AttendanceStudentDto> list = mLmsUserMapper.getAttendanceStudentList(
					courseId,
					companyId,
					userName,
					placeId,
					Constants.DB_FLG_FALSE);

			for (AttendanceStudentDto dto : list) {
				dto.setNotEnterCount(
						checkNotEnter(dto.getLmsUserId()));
				dto.setNotEnterFlg(
						checkNotEnter(dto.getLmsUserId()) > 0);
			}

			return list;
		}

		else if (loginUserUtil.isCompany()) {

			companyId = loginUserDto.getCompanyId();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -pastTime);
			Date limitDate = cal.getTime();

			if (Integer.valueOf(1).equals(pastFlg)) {
				return mLmsUserMapper.getAttendanceStudentListCompanyAll(
						companyId,
						userName,
						pastFlg);

			} else {
				pastFlg = 0;
				return mLmsUserMapper.getAttendanceStudentListCompany(
						companyId,
						userName,
						pastFlg,
						limitDate);
			}
		} else {
			return new ArrayList<>();

		}
	}

	public void setBulkRegistForm(BulkRegistForm form) {

		// ログイン会場ID
		Integer placeId = loginUserDto.getPlaceId();

		// 会場取得（API = mapper）
		MPlace place = mPlaceMapper.findByPlaceId(
				placeId,
				Constants.DB_FLG_FALSE,
				Constants.DB_FLG_FALSE);

		// 備考から教室名抽出
		String classroom = extractClassroomName(place.getPlaceNote());

		if (classroom == null) {
			form.setPlaceName(place.getPlaceName());
		} else {
			form.setPlaceName(place.getPlaceName() + "（" + classroom + "）");
		}

		form.setPlaceId(placeId);
	}

	public void searchInputCheck(@Validated BulkRegistForm form, BindingResult result) {

		// ① すでにエラーがある場合は処理しない
		if (result.hasErrors()) {
			return;
		}

		Date from = form.getSearchPeriodFrom();
		Date to = form.getSearchPeriodTo();
		Date today = attendanceUtil.getTrainingDate();

		// nullチェック
		if (from == null || to == null) {

			return;
		}

		// ② To > システム日付（未来日チェック）
		if (to.after(today)) {
			result.addError(new FieldError(
					result.getObjectName(),
					"searchPeriodFrom",
					messageUtil.getMessage(
							Constants.VALID_KEY_SEARCHTORANGEERROR,
							new String[] { "期間(From)" })));
			return;
		}

		// ③ From > To
		if (from.after(to)) {
			result.addError(new FieldError(
					result.getObjectName(),
					"searchPeriodTo",
					messageUtil.getMessage(
							Constants.VALID_KEY_SEARCHPERIODCOMPAREERROR,
							new String[] { "期間(To)", "期間(From)" })));
			return;
		}

		// ④ 30日チェック
		long diff = (to.getTime() - from.getTime()) / (1000 * 60 * 60 * 24);

		if (diff > 30) {
			result.addError(new FieldError(
					result.getObjectName(),
					"searchPeriodTo",
					messageUtil.getMessage(
							Constants.VALID_KEY_SEARCHSETTINGOVER,
							new String[] { "期間", "30日" })));
		}
	}

	public void getUserAttendance(BulkRegistForm bulkRegistForm) {

		List<UserAttendanceDto> dtoList = mPlaceMapper.getUserAttendanceDto(
				bulkRegistForm.getPlaceId(),
				bulkRegistForm.getSearchPeriodFrom(),
				bulkRegistForm.getSearchPeriodTo(),
				Constants.DB_FLG_FALSE,
				loginUserDto.getAccountId());

		Map<Date, List<UserAttendanceDto>> dateMap = dtoList.stream()
				.collect(Collectors.groupingBy(
						UserAttendanceDto::getTrainingDate,
						TreeMap::new,
						Collectors.toList()));

		List<DailyAttendanceForm> result = new ArrayList<>();

		int index = 0;

		for (Map.Entry<Date, List<UserAttendanceDto>> entry : dateMap.entrySet()) {

			Date workDate = entry.getKey();
			List<UserAttendanceDto> users = entry.getValue();

			for (UserAttendanceDto dto : users) {
				result.add(buildDailyForm(dto, workDate, index));
			}

			index++;
		}

		bulkRegistForm.setDailyAttendanceFormList(result);
		System.out.println(
				bulkRegistForm.getDailyAttendanceFormList().size());
	}

	private DailyAttendanceForm buildDailyForm(UserAttendanceDto dto, Date workDate, int index) {

		DailyAttendanceForm f = new DailyAttendanceForm();

		String start = dto.getTrainingStartTime();
		String end = dto.getTrainingEndTime();

		// ===== 表示用（丸め）=====
		String dispStart = null;
		if (start != null && !start.isBlank()) {
			dispStart = new TrainingTime(start).roundUp().toString();
		}

		String dispEnd = null;
		if (end != null && !end.isBlank()) {
			dispEnd = new TrainingTime(end).roundDown().toString();
		}

		f.setTrainingStartTimeDisplay(dispStart);
		f.setTrainingEndTimeDisplay(dispEnd);

		// ===== 入力欄は空にする（重要）=====
		f.setTrainingStartTime(null);
		f.setTrainingEndTime(null);

		f.setTrainingDate(dateUtil.toString(workDate));
		f.setDispTrainingDate(dateUtil.dateToString(workDate, "yyyy年M月d日(E)"));

		f.setUserName(dto.getUserName());
		f.setCourseName(dto.getCourseName());

		f.setBlankTime(dto.getBlankTime());
		if (dto.getBlankTime() != null) {
			int h = dto.getBlankTime() / 60;
			int m = dto.getBlankTime() % 60;
			f.setBlankTimeValue(String.format("%02d:%02d", h, m));
		}
		f.setCompanyAttendanceId(dto.getCompanyAttendanceId());
		f.setStudentAttendanceId(dto.getStudentAttendanceId());
		f.setLmsUserId(dto.getLmsUserId());
		f.setStatus(String.valueOf(dto.getStatus()));
		f.setStatusDispName(convertStatus(dto.getStatus()));

		f.setNote(dto.getNote());

		f.setIndex(String.valueOf(index));

		return f;
	}

	private String extractClassroomName(String note) {

		if (note == null || !note.contains("$"))
			return null;

		String[] arr = note.split("\\$");

		if (arr.length < 2 || arr[1].isBlank())
			return null;

		return arr[1];
	}

	private static final Map<Short, String> STATUS_MAP = Arrays.stream(AttendanceStatusEnum.values())
			.collect(Collectors.toMap(
					e -> e.code,
					e -> e.name));

	private String convertStatus(Short status) {
		return STATUS_MAP.getOrDefault(status, "");
	}

	public void bulkRegistInputCheck(BulkRegistForm form, BindingResult result) {

		List<DailyAttendanceForm> list = form.getDailyAttendanceFormList();

		for (int i = 0; i < list.size(); i++) {

			DailyAttendanceForm row = list.get(i);
			String prefix = "dailyAttendanceFormList[" + i + "]";
			String date = row.getTrainingDate();

			boolean absent = Boolean.TRUE.equals(row.getAbsent());

			String startStr = row.getTrainingStartTime();
			String endStr = row.getTrainingEndTime();

			boolean startInput = startStr != null && !startStr.trim().isEmpty();
			boolean endInput = endStr != null && !endStr.trim().isEmpty();

			// a. 欠席なのに入力あり
			if (absent && (startInput || endInput)) {
				result.rejectValue(prefix + ".trainingStartTime",
						"absentAndTrainingTimeExistsBulk",
						new Object[] { date },
						null);
				continue;
			}

			// b. 出席なのに未入力
			if (!absent && !startInput && !endInput) {
				result.rejectValue(prefix + ".trainingStartTime",
						"requiredTrainingTimeBulk",
						new Object[] { date },
						null);
				continue;
			}

			// c. 片方だけ入力
			if ((startInput && !endInput) || (!startInput && endInput)) {
				result.rejectValue(prefix + ".trainingStartTime",
						"requiredTrainingTimeBulk",
						new Object[] { date },
						null);
				continue;
			}

			TrainingTime start;
			TrainingTime end;

			try {
				start = new TrainingTime(startStr);
				end = new TrainingTime(endStr);
			} catch (Exception e) {
				result.rejectValue(prefix + ".trainingStartTime",
						"trainingTimeBulk",
						new Object[] { date },
						null);
				continue;
			}

			// d. 24時超え
			if (isOver24(start)) {
				result.rejectValue(prefix + ".trainingStartTime",
						"maxvalBulk",
						new Object[] { date, "24:00" },
						null);
			}

			if (isOver24(end)) {
				result.rejectValue(prefix + ".trainingEndTime",
						"maxvalBulk",
						new Object[] { date, "24:00" },
						null);
			}

			// e. 開始 > 終了
			if (!isOver24(start) && !isOver24(end) && start.compareTo(end) > 0) {
				result.rejectValue(prefix + ".trainingEndTime",
						"attendance.trainingTimeRangeBulk",
						new Object[] { date },
						null);
			}
		}
	}

	public String bulkUpdate(BulkRegistForm form) throws ParseException {
		System.out.println("★★ bulkUpdate START ★★");
		Date now = new Date();

		for (DailyAttendanceForm row : form.getDailyAttendanceFormList()) {

			TCompanyAttendance attendance;

			// =========================
			// ① 入力値取得 & 安全化
			// =========================
			String startStr = row.getTrainingStartTime();
			String endStr = row.getTrainingEndTime();

			TrainingTime start = null;
			TrainingTime end = null;

			if (startStr != null && !startStr.isBlank()) {
				start = new TrainingTime(startStr);
				start.roundUp(); // ★bulk専用丸め
			}

			if (endStr != null && !endStr.isBlank()) {
				end = new TrainingTime(endStr);
				end.roundDown(); // ★bulk専用丸め
			}

			// =========================
			// ② UPDATE
			// =========================
			if (row.getCompanyAttendanceId() != null) {

				attendance = tCompanyAttendanceMapper.findByCompanyAttendanceId(
						row.getCompanyAttendanceId(),
						Constants.DB_FLG_FALSE);

				attendance.setTrainingStartTime(start == null ? null : start.toString());
				attendance.setTrainingEndTime(end == null ? null : end.toString());

				if (Boolean.TRUE.equals(row.getAbsent())) {
					attendance.setStatus(AttendanceStatusEnum.ABSENT.code);
				} else {
					attendance.setStatus(attendanceUtil.getStatus(start, end).code);
				}

				attendance.setLastModifiedUser(loginUserDto.getLmsUserId());
				attendance.setLastModifiedDate(now);

				tCompanyAttendanceMapper.update(attendance);

			}
			// =========================
			// ③ INSERT
			// =========================
			else {

				attendance = new TCompanyAttendance();

				attendance.setLmsUserId(Integer.valueOf(row.getLmsUserId()));
				attendance.setTrainingDate(dateUtil.parse(row.getTrainingDate()));

				attendance.setTrainingStartTime(start == null ? null : start.toString());
				attendance.setTrainingEndTime(end == null ? null : end.toString());

				if (Boolean.TRUE.equals(row.getAbsent())) {
					attendance.setStatus(AttendanceStatusEnum.ABSENT.code);
				} else {
					attendance.setStatus(attendanceUtil.getStatus(start, end).code);
				}

				attendance.setAccountId(loginUserDto.getAccountId());
				attendance.setDeleteFlg(Constants.DB_FLG_FALSE);
				attendance.setFirstCreateUser(loginUserDto.getLmsUserId());
				attendance.setFirstCreateDate(now);
				attendance.setLastModifiedUser(loginUserDto.getLmsUserId());
				attendance.setLastModifiedDate(now);

				tCompanyAttendanceMapper.insert(attendance);

			}
		}

		return messageUtil.getMessage("regist.complete",
				new String[] { "勤怠情報" });
	}

	private boolean isOver24(TrainingTime time) {

		if (time == null) {
			return false;
		}

		Integer hour = time.getHour();
		Integer minute = time.getMinute();

		if (hour == null) {
			return false;
		}

		if (minute == null) {
			minute = 0;
		}

		return hour > 24
				|| (hour == 24 && minute > 0);
	}

	/**
	 * 過去日の勤怠未入力件数取得
	 *
	 * @param lmsUserId LMSユーザーID
	 * @return 未入力件数
	 */
	public int checkNotEnter(Integer lmsUserId) {

		Date trainingDate = attendanceUtil.getTrainingDate();

		Integer count = tStudentAttendanceMapper.notEnterCountByLmsUserId(
				lmsUserId,
				Constants.DB_FLG_FALSE,
				trainingDate);

		return count == null ? 0 : count;
	}

}
