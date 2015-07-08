package io.rong.apicloud;

import android.content.Context;

import com.uzmap.pkg.uzcore.uzmodule.AppInfo;
import com.uzmap.pkg.uzcore.uzmodule.ApplicationDelegate;

import io.rong.imlib.RongIMClient;

/**
 * Created by DragonJ on 15/7/8.
 */
public class RongIMClientModuleDelegate extends ApplicationDelegate {

    @Override
    public void onApplicationCreate(Context context, AppInfo info) {
        RongIMClient.init(context);
        super.onApplicationCreate(context, info);

    }
}
