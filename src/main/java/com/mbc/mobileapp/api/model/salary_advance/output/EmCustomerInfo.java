package com.mbc.mobileapp.api.model.salary_advance.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nhóm 1 — customerInfo (Định danh & Nhân khẩu học)
 * Mapping từ response: data.customerInfo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmCustomerInfo {
    private String customerId;
    private String familyName;
    private String firstName;
    private Boolean englishName;       // true = tên tiếng Anh, false = tên Khmer
    private String idType;             // NATIONAL_ID / PASSPORT
    private String idNumber;
    private String idExpiredDate;       // YYYY-MM-DD
    private String gender;             // MALE / FEMALE / OTHER
    private String nationality;        // KH / VN / ...
    private String dateOfBirth;        // YYYY-MM-DD
    private String residential;        // Địa chỉ nơi ở (1 string)
    private String phoneNumber;        // 855XXXXXXXXX
    private String companyName;
    private String currentOccupation;
    private String work;               // Địa chỉ công ty (1 string)
}
