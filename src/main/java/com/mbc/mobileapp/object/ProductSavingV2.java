package com.mbc.mobileapp.object;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Setter
@Getter
public class ProductSavingV2 {

    private String id;

    private String name;

    private String nameKh;

    private List<ProductSavingOption> options;

}
