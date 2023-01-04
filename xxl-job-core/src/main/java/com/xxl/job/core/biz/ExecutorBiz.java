package com.xxl.job.core.biz;

import com.xxl.job.core.biz.model.*;

/**
 * Created by xuxueli on 17/3/1.
 */
public interface ExecutorBiz {

    /**
     * beat
     * @return
     */
    JobResponseEntity beat();

    /**
     * idle beat
     *
     * @param idleBeatParam
     * @return
     */
    JobResponseEntity idleBeat(IdleBeatParam idleBeatParam);

    /**
     * run
     * @param triggerParam
     * @return
     */
    JobResponseEntity run(TriggerParam triggerParam);

    /**
     * kill
     * @param killParam
     * @return
     */
    JobResponseEntity kill(KillParam killParam);

    /**
     * log
     * @param logParam
     * @return
     */
    JobResponseEntity log(LogParam logParam);

}
