package com.mbc.mobileapp.constant;

public enum SavingProductEnum {
    MATURITY("Maturity interest", "6518", "AZ000001"),
    MONTHLY("Monthly interest", "6520", "AZ000003"),
    //todo
    UPFRONT("Upfront interest", "21006", "AZ000003X"),

//    REAL_TIME("Real Time interest", "6505", "AZ000002"),
//    FLEXI_TERM("Flexi Term interest", "6011", "AC000001");
    REAL_TIME(null, "6505", "AZ000002"),
    FLEXI_TERM(null, "6011", "AC000001");

    private final String name;
    private final String category;
    private final String productCode;

    SavingProductEnum(String name, String category, String productCode) {
        this.name = name;
        this.category = category;
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getProductCode() {
        return productCode;
    }

    public static SavingProductEnum getByCategory(String category) {
        for (SavingProductEnum product : values()) {
            if (product.getCategory().equals(category)) {
                return product;
            }
        }
        throw new IllegalArgumentException("No enum constant with category " + category);
    }

    public static SavingProductEnum valueOfName(String name) {
        for (SavingProductEnum product : values()) {
            if (product.getName().equals(name)) {
                return product;
            }
        }
        throw new IllegalArgumentException("No enum constant with name " + name);
    }
}
