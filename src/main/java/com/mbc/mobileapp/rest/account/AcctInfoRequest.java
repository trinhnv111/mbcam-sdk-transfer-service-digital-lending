package com.mbc.mobileapp.rest.account;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcctInfoRequest extends RestRequest {
  private String phoneNo;
}
