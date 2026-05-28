package com.mbc.mobileapp.repository;

import com.mbc.common.entity.ComProductSaving;
import com.mbc.mobileapp.dto.ProductSavingDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComProductSavingRepoExtd extends JpaRepository<ComProductSaving, String> {
    @Query(value = "SELECT sg.ID AS id, sg.NAME AS name, s.NAME_EN as nameEn, s.CATEGORY AS category, s.SUB_CATEGORY AS subCategory, " +
            "s.PRODUCT_CODE AS productCode, s.SUB_PRODUCT AS subProduct, s.AMT_MIN_DEPOSIT_USD AS amtMinDepositUsd, " +
            "s.AMT_MIN_DEPOSIT_KHR AS amtMinDepositKhr, s.AMT_MIN_TOPUP_USD as amtMinTopupUsd , s.AMT_MIN_TOPUP_KHR as amtMinTopupKhr," +
            "s.AMT_MAX_NOT_FULLKYC_USD as amtMaxNotFullKycUsd, s.AMT_MAX_NOT_FULLKYC_KHR as amtMaxNotFullKycKhr," +
            "s.CURRENCY_AVAILABLE as currencyAvailable, s.GROUP_ID as groupId " +
            "FROM COM_PRODUCT_SAVING_GROUP sg JOIN COM_PRODUCT_SAVING s " +
            "ON sg.ID = s.GROUP_ID " +
            "WHERE sg.STATUS = 1 " +
            "AND s.status = 1", nativeQuery = true)
    List<ProductSavingDto> findActiveProduct();

    @Query(value = "SELECT sg.ID AS id, sg.NAME AS name, s.NAME_EN as nameEn, s.CATEGORY AS category, s.SUB_CATEGORY AS subCategory, " +
            "s.PRODUCT_CODE AS productCode, s.SUB_PRODUCT AS subProduct, s.AMT_MIN_DEPOSIT_USD AS amtMinDepositUsd, " +
            "s.AMT_MIN_DEPOSIT_KHR AS amtMinDepositKhr, s.AMT_MIN_TOPUP_USD as amtMinTopupUsd , s.AMT_MIN_TOPUP_KHR as amtMinTopupKhr," +
            "s.AMT_MAX_NOT_FULLKYC_USD as amtMaxNotFullKycUsd, s.AMT_MAX_NOT_FULLKYC_KHR as amtMaxNotFullKycKhr," +
            "s.CURRENCY_AVAILABLE as currencyAvailable, s.GROUP_ID as groupId " +
            "FROM COM_PRODUCT_SAVING_GROUP sg JOIN COM_PRODUCT_SAVING s " +
            "ON sg.ID = s.GROUP_ID " +
            "WHERE s.status = 1 " +
            "AND s.GROUP_ID = :groupId ", nativeQuery = true)
    List<ProductSavingDto> findByGroupId(@Param("groupId") String groupId);

    @Query(value = "SELECT sg.ID AS id, sg.NAME AS name, s.NAME_EN as nameEn, s.CATEGORY AS category, s.SUB_CATEGORY AS subCategory, " +
            "s.PRODUCT_CODE AS productCode, s.SUB_PRODUCT AS subProduct, s.AMT_MIN_DEPOSIT_USD AS amtMinDepositUsd, " +
            "s.AMT_MIN_DEPOSIT_KHR AS amtMinDepositKhr, s.AMT_MIN_TOPUP_USD as amtMinTopupUsd , s.AMT_MIN_TOPUP_KHR as amtMinTopupKhr," +
            "s.AMT_MAX_NOT_FULLKYC_USD as amtMaxNotFullKycUsd, s.AMT_MAX_NOT_FULLKYC_KHR as amtMaxNotFullKycKhr," +
            "s.CURRENCY_AVAILABLE as currencyAvailable, s.GROUP_ID as groupId " +
            "FROM COM_PRODUCT_SAVING_GROUP sg JOIN COM_PRODUCT_SAVING s " +
            "ON sg.ID = s.GROUP_ID " +
            "WHERE s.PRODUCT_CODE = :productCode ", nativeQuery = true)
    List<ProductSavingDto> findByProductCode(@Param("productCode") String productCode);

}

