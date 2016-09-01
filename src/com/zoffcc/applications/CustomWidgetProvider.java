package com.zoffcc.applications;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RemoteViews;

import org.thoughtcrime.securesms.R;

/**
 * Created on 01.09.2016.
 */
public class CustomWidgetProvider extends AppWidgetProvider
{

    public static final int MAX_COUNT = 20;

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        // ----------- DEBUG ------------
        //        final Intent intent2 = new Intent();
        //        intent2.setAction("com.zoffcc.applications.intent.action.CHANGE_BADGE");
        //
        //        final Handler handler = new Handler();
        //        handler.postDelayed(new Runnable()
        //        {
        //            @Override
        //            public void run()
        //            {
        //                context.sendBroadcast(intent2);
        //            }
        //        }, 3000);
        // ----------- DEBUG ------------

        for (int widgetId : appWidgetIds)
        {
            Intent intent = new Intent(context, org.thoughtcrime.securesms.ConversationListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.badge_widget);

            int unreadCount = BadgeWidgetIntentReceiver.unreadCount;
            if (unreadCount <= 0)
            {
                // Hide TextView for unread count
                remoteViews.setViewVisibility(R.id.widget_number, View.GONE);
            }
            else
            {
                remoteViews.setViewVisibility(R.id.widget_number, View.VISIBLE);

                String displayCount = (unreadCount <= MAX_COUNT) ? String.valueOf(unreadCount) : String.valueOf(MAX_COUNT) + "+";
                remoteViews.setTextViewText(R.id.widget_number, displayCount);
            }

            remoteViews.setOnClickPendingIntent(R.id.widget_icon, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
