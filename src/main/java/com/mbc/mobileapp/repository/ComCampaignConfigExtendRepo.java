package com.mbc.mobileapp.repository;

import com.mbc.common.entity.ComCampaignConfig;
import com.mbc.common.repository.ComCampaignConfigRepo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComCampaignConfigExtendRepo extends ComCampaignConfigRepo {

    @Query(nativeQuery = true, value = "SELECT * FROM COM_CAMPAIGN_CONFIG WHERE "
            + "PRODUCT_TYPE = :productType "
            + "AND TRUNC(START_DATE) <= TRUNC(SYSDATE) "
            + "AND (TRUNC(END_DATE) >= TRUNC(SYSDATE) OR TRUNC(END_DATE) IS NULL) ")
    public List<ComCampaignConfig> getListCampaign(@Param("productType") String productType);

    @Query(nativeQuery = true, value = "SELECT * FROM COM_CAMPAIGN_CONFIG WHERE "
            + "PRODUCT_TYPE = :productType "
            + "AND CAMPAIGN_CODE = :campaignCode "
            + "AND TRUNC(START_DATE) <= TRUNC(SYSDATE) "
            + "AND (TRUNC(END_DATE) >= TRUNC(SYSDATE) OR TRUNC(END_DATE) IS NULL) ")
    public List<ComCampaignConfig> getListCampaignByCampaignCode(@Param("campaignCode") String campaignCode, @Param("productType") String productType);


    @Query(nativeQuery = true, value = "SELECT * FROM COM_CAMPAIGN_CONFIG WHERE "
            + "PRODUCT_TYPE = :productType "
            + "AND CHANNEL = :channel "
            + "AND PARTNER_CODE = :partnerCode "
            + "AND TRUNC(START_DATE) <= TRUNC(SYSDATE) "
            + "AND (TRUNC(END_DATE) >= TRUNC(SYSDATE) OR TRUNC(END_DATE) IS NULL) ")
    public List<ComCampaignConfig> getListCampaignByChannelAndPartnerCode(@Param("productType") String productType,
                                                                          @Param("channel") String channel,
                                                                          @Param("partnerCode") String partnerCode);


    @Query(nativeQuery = true, value = "SELECT * FROM COM_CAMPAIGN_CONFIG WHERE "
            + "PRODUCT_TYPE = :productType "
            + "AND CHANNEL = :channel "
            + "AND PARTNER_CODE = :partnerCode "
            + "AND CAMPAIGN_CODE = :campaignCode "
            + "AND TRUNC(START_DATE) <= TRUNC(SYSDATE) "
            + "AND (TRUNC(END_DATE) >= TRUNC(SYSDATE) OR TRUNC(END_DATE) IS NULL) ")
    public List<ComCampaignConfig> getListCampaignByCampaignCodeAndChannelAndPartnerCode(@Param("campaignCode") String campaignCode,
                                                                                         @Param("productType") String productType,
                                                                                         @Param("channel") String channel,
                                                                                         @Param("partnerCode") String partnerCode);
}
