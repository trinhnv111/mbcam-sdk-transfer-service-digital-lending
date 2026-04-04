package com.mbc.mobileapp.object;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ValidInputIncorrect {

    private String code;
    private Date dateTime;
    private int count;
}
