package com.mbc.mobileapp.api.model.salary_advance.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base response từ eMoney API
 * {
 *   "status": 0,
 *   "code": "MSG_SUCCESS",
 *   "message": "Success",
 *   "data": { ... }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmBaseResponse<T> {
    private Integer status;
    private String code;
    private String message;
    private T data;
}
