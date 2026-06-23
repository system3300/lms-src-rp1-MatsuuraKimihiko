package jp.co.sss.lms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.entity.MPlace;
import jp.co.sss.lms.mapper.MPlaceMapper;
import jp.co.sss.lms.util.Constants;

@Service
public class PlaceService {

	@Autowired
	MPlaceMapper mPlaceMapper;

	@Autowired
	LoginUserDto loginUserDto;

	public MPlace getPlace() {

		return mPlaceMapper.findByPlaceId(
				loginUserDto.getPlaceId(),
				Constants.DB_FLG_FALSE,
				Constants.DB_FLG_FALSE);
	}
}
