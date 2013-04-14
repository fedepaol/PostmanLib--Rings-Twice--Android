/***
 Copyright (c) 2009 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.whiterabbit.postmanlibsample.wakeful;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.whiterabbit.postman.ServerInteractionHelper;
import com.whiterabbit.postmanlibsample.WakefulAlarmManagerSample;
import com.whiterabbit.postmanlibsample.commands.TwitterScheduledGetStatusRequest;

public class WakefulAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        TwitterScheduledGetStatusRequest request = new TwitterScheduledGetStatusRequest();
        Log.d("Wake", "Performing the request");
        ServerInteractionHelper.getInstance(context).sendWakefulRequest(context, WakefulAlarmManagerSample.WAKEFUL_SCHEDULED_LATEST_TWEET, request);
    }
}