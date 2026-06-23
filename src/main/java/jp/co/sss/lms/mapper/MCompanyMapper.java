package jp.co.sss.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import jp.co.sss.lms.dto.CompanyDto;

@Mapper
public interface MCompanyMapper {
	List<CompanyDto> getCompanyDto();
}
