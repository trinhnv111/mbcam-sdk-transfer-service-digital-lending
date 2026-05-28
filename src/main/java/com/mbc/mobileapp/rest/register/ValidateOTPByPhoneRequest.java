/* ---------------------------------------------------------------------------
 *  All Rights Reserved. Copyright(C) MB Bank, Ltd.
 *  revision : 5:37:06 PM 
 *  vendor   : MB Bank, Ltd.
 *  author   : Le Van Dan OS
 *  since    : 2021-10-20 5:37:06 PM 
 *  tagId    : mbcam-mobileapp-openaccount
 * ---------------------------------------------------------------------------
 *  revision marking
 * --------------------------------------------------------------------------- */

package com.mbc.mobileapp.rest.register;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.rest.bean.PublicRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * @author danlv.os
 *
 */
@Setter
@Getter
public class ValidateOTPByPhoneRequest extends PublicRequest {

    private String phoneNo;

    private TokenOtp tokenOtp;

}
