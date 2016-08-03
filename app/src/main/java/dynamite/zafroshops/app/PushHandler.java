/*
 * Copyright 2016 Maurice Kenmeue Fonwe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dynamite.zafroshops.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.microsoft.windowsazure.notifications.NotificationsHandler;

import java.util.ArrayList;
import java.util.Random;

public class PushHandler extends NotificationsHandler {
    public static final int NOTIFICATION_ID_RANGE = 100;
    public static final String NOTIFICATION_GROUP_NEW = "new_group";
    private static final String NOTIFICATION_ID = "id";
    private static final String NOTIFICATION_NAME = "name";
    private static final String NOTIFICATION_TITLE = "title";
    private static final String NOTIFICATION_MESSAGE = "message";
    private NotificationManager notificationManager;
    Context ctx;
    public static ArrayList<String> ids;

    @Override
    public void onReceive(Context context, Bundle bundle) {
        super.onReceive(context, bundle);

        if (ids == null)
        {
            ids = new ArrayList();
        }
        ctx = context;
        sendNotification(bundle);
    }

    private void sendNotification(Bundle bundle) {
        String id = bundle.getString(NOTIFICATION_ID);
        String name = bundle.getString(NOTIFICATION_NAME);
        String title = bundle.getString(NOTIFICATION_TITLE);
        String message = bundle.getString(NOTIFICATION_MESSAGE);

        if (id != null && title != null && message != null && !ids.contains(id) && !ids.contains(name)) {
            Intent intent = new Intent(ctx, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                    new Intent(ctx, MainActivity.class).putExtra(MainActivity.EXTRA_ID, id).setData(Uri.parse("foo://" + SystemClock.elapsedRealtime())), // setData only to allow extras to be passed tp the activity
                    0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle(title)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message).setBigContentTitle(title))
                    .setContentText(message)
                    .setColor(ctx.getResources().getColor(R.color.BoxBackgroundThemeBrush))
                    .setGroup(NOTIFICATION_GROUP_NEW)
                    .setAutoCancel(true);

            builder.setContentIntent(contentIntent);
            notificationManager.notify(new Random().nextInt(NOTIFICATION_ID_RANGE), builder.build());
        }
    }
}
