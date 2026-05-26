package com.mbc.mobileapp.repository;

import com.mbc.common.entity.linkAccount.ComLinkAccount;
import com.mbc.common.repository.linkAccount.ComLinkAccountRepo;

import java.util.Optional;

public interface ComLinkAccountRepository extends ComLinkAccountRepo {
    Optional<ComLinkAccount> findByCustomerCodeAndPartnerCode(String customerCode, String partnerCode);
}
