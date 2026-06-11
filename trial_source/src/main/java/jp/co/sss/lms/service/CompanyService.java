package jp.co.sss.lms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.sss.lms.dto.CompanyDto;
import jp.co.sss.lms.mapper.MCompanyMapper;

@Service
public class CompanyService {

	@Autowired
	private MCompanyMapper mCompanyMapper;

	//Task.57
	public List<CompanyDto> getCompanyDto() {
		return mCompanyMapper.getCompanyDto();
	}
}
