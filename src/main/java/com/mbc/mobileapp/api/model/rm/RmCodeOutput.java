/* ---------------------------------------------------------------------------
 *  All Rights Reserved. Copyright(C) MB Bank, Ltd.
 *  revision : 10:54:42 AM 
 *  vendor   : MB Bank, Ltd.
 *  author   : Le Van Dan OS
 *  since    : 2021-11-23 10:54:42 AM 
 *  tagId    : mbcam-mobileapp-openaccount
 * ---------------------------------------------------------------------------
 *  revision marking
 * --------------------------------------------------------------------------- */

package com.mbc.mobileapp.api.model.rm;


import lombok.Getter;
import lombok.Setter;

/**
 * @author danlv.os
 *
 */
@Getter
@Setter
public class RmCodeOutput {
    
    private String rmCode;
    private String area;
    private String name;
    private String deliveryPoint;
    private String branch;
    private String rmMobile;
    private String mnemonic;
    private String rmCif;
    

//    @JsonAlias("AREA")
//    @JsonProperty("area")
//    private String area;
//
//    @JsonAlias("t24VersionId")
//    @JsonProperty("t24VersionId")
//    private String t24VersionId;

//    @JsonAlias("CO.CODE")
//    @JsonProperty("coCode")
//    private String coCode;

//    @JsonAlias("VALUE.LIST")
//    @JsonProperty("valueList")
//    private List<String> valueList;
//
//    @JsonAlias("LOCAL.REF.LIST")
//    @JsonProperty("localRefList")
//    private List<String> localRefList;
//
//    @JsonAlias("MV.ALERT.RES5.LIST")
//    @JsonProperty("mvAlertRes5List")
//    private List<String> mvAlertRes5List;
//
//    @JsonAlias("MV.ALERT.RES2.LIST")
//    @JsonProperty("mvAlertRes2List")
//    private List<String> mvAlertRes2List;
//
//    @JsonAlias("CURR.NO")
//    @JsonProperty("currNo")
//    private String currNo;
//
//    @JsonAlias("OPERAND.LIST")
//    @JsonProperty("operandList")
//    private List<String> operandList;
//
//    @JsonAlias("MV.ALERT.RES3.LIST")
//    @JsonProperty("mvAlertRes3List")
//    private List<String> mvAlertRes3List;
//
//    @JsonAlias("DATE.TIME")
//    @JsonProperty("dateTime")
//    private String dateTime;
//
//    @JsonAlias("MV.ALERT.RES6.LIST")
//    @JsonProperty("mvAlertRes6List")
//    private List<String> mvAlertRes6List;
//
//    @JsonAlias("INPUTTER")
//    @JsonProperty("inputter")
//    private String inputter;
//
//    @JsonAlias("DEPT.CODE")
//    @JsonProperty("deptCode")
//    private String deptCode;
//
//    @JsonAlias(".LIST")
//    @JsonProperty("list")
//    private List<String> list;
//
//    @JsonAlias("REQUEST.ID.LIST")
//    @JsonProperty("requestIdList")
//    private List<String> requestIdList;
//
//    @JsonAlias("FIELD.LIST")
//    @JsonProperty("fieldList")
//    private List<String> fieldList;
//
//    @JsonAlias("PROHIBIT.COMPS.LIST")
//    @JsonProperty("prohibitCompsList")
//    private List<String> prohibitCompsList;
//
//    @JsonAlias("NAME")
//    @JsonProperty("name")
//    private String name;
//
//    @JsonAlias("EVENT.LIST")
//    @JsonProperty("eventList")
//    private List<String> eventList;
//
//    @JsonAlias("MV.ALERT.RES1.LIST")
//    @JsonProperty("mvAlertRes1List")
//    private List<String> mvAlertRes1List;
//
//    @JsonAlias("MV.ALERT.RES4.LIST")
//    @JsonProperty("mvAlertRes4List")
//    private List<String> mvAlertRes4List;
//
//    @JsonAlias("DELIVERY.POINT")
//    @JsonProperty("deliveryPoint")
//    private String deliveryPoint;
//
//    @JsonAlias("RECORD.STATUS")
//    @JsonProperty("recordStatus")
//    private String recordStatus;
//
//    @JsonAlias("RESTR.TO.COMPS.LIST")
//    @JsonProperty("restrToCompsList")
//    private List<RestsToComps> restrToCompsList;
}
