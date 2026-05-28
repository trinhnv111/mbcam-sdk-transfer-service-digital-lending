
package com.mbc.mobileapp.command.user.login;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ImIeUser;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.Calendar;


@Service
public class CheckForceChangePassword implements Command {

    @Override
    public boolean execute(Context cntxt) throws Exception {
       
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        ImIeUser user = (ImIeUser) context.getVar("user");
      
        if (Constant.NO.equals(user.getIsPwdNeverExpire())) {

            Calendar currentDate = Calendar.getInstance();
            currentDate.set(Calendar.HOUR_OF_DAY, 0);
            currentDate.set(Calendar.MINUTE, 0);
            currentDate.set(Calendar.SECOND, 0);
            currentDate.set(Calendar.MILLISECOND, 0);
            Calendar passwordChangeDate = Calendar.getInstance();
            passwordChangeDate.setTime(user.getPasswordchangedate());
            // passwordChangeDate.add(Calendar.DATE, sysConfig.getPwdValidDay());
            passwordChangeDate.add(Calendar.DATE, 730);
            if (!passwordChangeDate.getTime().after(currentDate.getTime())) {
                result = new SimpleResult(ResponseCode.PASSWORD_IS_EXPIRED.getDesc(), false, ResponseCode.PASSWORD_IS_EXPIRED.getCode());
            }
        }

        context.setResult(result);
        return !result.isOk();
    }

}
