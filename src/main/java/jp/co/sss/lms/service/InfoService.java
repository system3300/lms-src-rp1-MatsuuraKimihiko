package jp.co.sss.lms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.sss.lms.dto.InfoDto;
import jp.co.sss.lms.entity.TInfo;
import jp.co.sss.lms.mapper.TInfoMapper;

/**
 * お知らせ情報サービス
 * 
 * @author 東京ITスクール
 */
@Service
public class InfoService {

	@Autowired
	private TInfoMapper tInfoMapper;

	/**
	 * お知らせ情報取得
	 * 
	 * @return お知らせ情報
	 */
	public InfoDto getInfo() {

		// 最新のお知らせを取得
		TInfo tInfo = tInfoMapper.findBySingleResult();

		InfoDto infoDto = new InfoDto();
		if (tInfo != null) {
			infoDto.setInfoId(tInfo.getInfoId());
			infoDto.setContent(tInfo.getContent());
			infoDto.setLastModifiedDate(tInfo.getLastModifiedDate());
		}

		return infoDto;
	}

}
