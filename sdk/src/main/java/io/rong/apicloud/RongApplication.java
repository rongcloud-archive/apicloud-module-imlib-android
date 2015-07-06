package io.rong.apicloud;

import android.os.Process;

import com.uzmap.pkg.uzapp.UZApplication;

import io.rong.common.RLog;
import io.rong.imlib.RongIMClient;

/**
 * Created by DragonJ on 15/7/6.
 */
public class RongApplication extends UZApplication {
    @Override
    public void onCreate() {

        RLog.w(this, "RongApplication", "Uid:"+Process.myPid());
        super.onCreate();
        RongIMClient.init(this);
    }
}
