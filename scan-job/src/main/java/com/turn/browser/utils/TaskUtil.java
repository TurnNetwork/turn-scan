package com.turn.browser.utils;

import cn.hutool.core.util.StrUtil;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskUtil {

    /**
     * print log
     *
     * @param template:
     * @param params:
     * @return: void
     */
    public static void console(CharSequence template, Object... params) {
        String msg = StrUtil.format(template, params);
        XxlJobHelper.log(msg);
        log.info(msg);
    }

}
