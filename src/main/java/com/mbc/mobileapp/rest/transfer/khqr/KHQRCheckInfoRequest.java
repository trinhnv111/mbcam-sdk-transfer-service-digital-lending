package com.mbc.mobileapp.rest.transfer.khqr;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KHQRCheckInfoRequest extends RestRequest {

    @NotNull
    @NotBlank
    private String payloadQr;
}
