package jp.co.sss.lms.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.sss.lms.dto.AgreementConsentDto;
import jp.co.sss.lms.dto.LoginUserDto;
import jp.co.sss.lms.mapper.TAgreementConsentMapper;
import jp.co.sss.lms.util.Constants;

/**
 * 契約情報サービス
 * 
 * @author 東京ITスクール
 */
@Service
public class ContractService {

	@Autowired
	private TAgreementConsentMapper tAgreementConsentMapper;
	@Autowired
	private LoginUserDto loginUserDto;

	/**
	 * 契約同意DTOリストの取得
	 * 
	 * @return 契約同意DTOリスト
	 */
	public List<AgreementConsentDto> getAgreementConsentDtoList() {
		return tAgreementConsentMapper.getAgreementConsentDtoList(loginUserDto.getCompanyId(), null,
				Constants.CODE_VAL_CONTRACT_AGREE, Constants.DB_FLG_FALSE);
	}

	/**
	 * 未同意の契約同意DTOリストの取得
	 * 
	 * @return 契約同意DTOリスト
	 */
	public List<AgreementConsentDto> getDisagreementConsentDtoList() {
		Date today = new Date();
		return tAgreementConsentMapper.getAgreementConsentDtoList(loginUserDto.getCompanyId(),
				today, Constants.CODE_VAL_CONTRACT_DISAGREE, Constants.DB_FLG_FALSE);
	}

}
