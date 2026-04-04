package com.mbc.mobileapp.repository;

import com.mbc.common.entity.Cust;
import com.mbc.common.repository.CustRepo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface CustRepoExtend extends CustRepo {

    @Query(value = "SELECT a.host_cust_id as cif, a.cust_id as cust_id,c.user_id as user_id,a.acct_nm as acct_name  " +
            "            FROM CUST c INNER JOIN ACCT a ON c.id=a.cust_id " +
            "            WHERE a.CUST_ID=?1 AND a.ACCT_NO=?2 AND IS_ACCS_EBANKING='Y' AND c.IS_INACTIVE='N' AND c.IS_DELETE='N' AND a.INACTIVE_STS='0' ", nativeQuery = true)
    Tuple getCustInfo(String custId, String acctNo);

    public Cust findByNmAndIdTypNoAndIsDeleteAndIsInactive(String nm, String idTypNo, String delete, String active);


    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM CUST T "
            + "WHERE T.CHANNEL_CD = 'M' "
            + "AND TRUNC(T.CREATED_DATE_USER_ID) >= TRUNC(:startDate) "
            + "AND TRUNC(T.CREATED_DATE_USER_ID) <= TRUNC(:endDate)")
    public int numberUserRegisterApp(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM CUST T, COM_RM C "
            + "WHERE C.SRVC = 'SRVC_OPEN_ONL_ACCT' "
            + "AND C.CUST_ID = T.ID "
            + "AND T.CHANNEL_CD = 'M' "
            + "AND TRUNC(T.CREATED_DATE_USER_ID) >= TRUNC(:startDate) "
            + "AND TRUNC(T.CREATED_DATE_USER_ID) <= TRUNC(:endDate)")
    public int numberRmReferUserRegisterApp(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(
            value = "SELECT PHONE_NO,HOST_CIF_ID FROM CUST WHERE IS_INACTIVE='N' AND HOST_CIF_ID IN(?1) AND IS_DELETE='N'",
            nativeQuery = true
    )
    List<Tuple> getPhonesByCifs(Collection<String> var1);

    public List<Cust> findByCorrespondentEmailIgnoreCase(String correspondentEmail);

    @Query(nativeQuery = true, value = "SELECT * FROM CUST T " +
            "WHERE T.CREATED_DATE_USER_ID >= TO_DATE( :date, 'DD/MM/YYYY') " +
            "AND T.ID_TYP_TYPE = 'PASSPORT' " +
            "AND T.IS_DELETE = 'N' " +
            "AND T.IS_INACTIVE = 'N' " +
            "ORDER BY T.CREATED_DATE_USER_ID desc")
    public List<Cust> getUserPushSmsUpdateVisa(@Param("date") String date);
}
