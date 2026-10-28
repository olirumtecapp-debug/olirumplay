package com.olirum.player;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.RemoteViews;

import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.olirum.player.service.OlirumPlaybackService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OlirumPlayerWidget extends AppWidgetProvider {

    public static final String ACTION_WIDGET_PLAY_PAUSE = "com.olirum.player.ACTION_WIDGET_PLAY_PAUSE";
    public static final String ACTION_WIDGET_PREV = "com.olirum.player.ACTION_WIDGET_PREV";
    public static final String ACTION_WIDGET_NEXT = "com.olirum.player.ACTION_WIDGET_NEXT";

    private static String sCurrentTitle = "OlirumPlay";
    private static String sCurrentArtist = "Toque para ouvir música";
    private static boolean sIsPlaying = false;
    private static String sArtworkUri = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidgetUI(context, appWidgetManager, appWidgetId, sCurrentTitle, sCurrentArtist, sIsPlaying, sArtworkUri);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if (ACTION_WIDGET_PLAY_PAUSE.equals(action) ||
            ACTION_WIDGET_PREV.equals(action) ||
            ACTION_WIDGET_NEXT.equals(action)) {

            SessionToken sessionToken = new SessionToken(context, new ComponentName(context, OlirumPlaybackService.class));
            ListenableFuture<MediaController> controllerFuture = new MediaController.Builder(context, sessionToken).buildAsync();

            controllerFuture.addListener(() -> {
                try {
                    MediaController controller = controllerFuture.get();
                    if (ACTION_WIDGET_PLAY_PAUSE.equals(action)) {
                        if (controller.isPlaying()) {
                            controller.pause();
                        } else {
                            controller.play();
                        }
                    } else if (ACTION_WIDGET_PREV.equals(action)) {
                        if (controller.getCurrentPosition() > 3000) {
                            controller.seekTo(0);
                        } else {
                            controller.seekToPreviousMediaItem();
                        }
                    } else if (ACTION_WIDGET_NEXT.equals(action)) {
                        controller.seekToNextMediaItem();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, MoreExecutors.directExecutor());
        }
    }

    public static void updateAllWidgets(Context context, String title, String artist, boolean isPlaying, String artworkUri) {
        sCurrentTitle = (title != null && !title.isEmpty()) ? title : "OlirumPlay";
        sCurrentArtist = (artist != null && !artist.isEmpty()) ? artist : "Toque para ouvir música";
        sIsPlaying = isPlaying;
        sArtworkUri = artworkUri;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, OlirumPlayerWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        for (int id : appWidgetIds) {
            updateWidgetUI(context, appWidgetManager, id, sCurrentTitle, sCurrentArtist, sIsPlaying, sArtworkUri);
        }
    }

    private static void updateWidgetUI(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                                      String title, String artist, boolean isPlaying, String artworkUri) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_player);

        views.setTextViewText(R.id.widget_title, title);
        views.setTextViewText(R.id.widget_artist, artist);

        int playIcon = isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        views.setImageViewResource(R.id.widget_play, playIcon);

        // Click no app principal
        Intent appIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingApp = PendingIntent.getActivity(
                context, 0, appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_cover, pendingApp);
        views.setOnClickPendingIntent(R.id.widget_title, pendingApp);
        views.setOnClickPendingIntent(R.id.widget_artist, pendingApp);

        // Click Play/Pause
        Intent playIntent = new Intent(context, OlirumPlayerWidget.class);
        playIntent.setAction(ACTION_WIDGET_PLAY_PAUSE);
        PendingIntent pendingPlay = PendingIntent.getBroadcast(
                context, 1, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_play, pendingPlay);

        // Click Anterior
        Intent prevIntent = new Intent(context, OlirumPlayerWidget.class);
        prevIntent.setAction(ACTION_WIDGET_PREV);
        PendingIntent pendingPrev = PendingIntent.getBroadcast(
                context, 2, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_prev, pendingPrev);

        // Click Próxima
        Intent nextIntent = new Intent(context, OlirumPlayerWidget.class);
        nextIntent.setAction(ACTION_WIDGET_NEXT);
        PendingIntent pendingNext = PendingIntent.getBroadcast(
                context, 3, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_next, pendingNext);

        appWidgetManager.updateAppWidget(appWidgetId, views);

        // Carrega capa assincronamente via decodeStream padrão
        if (artworkUri != null && !artworkUri.isEmpty()) {
            new Thread(() -> {
                try {
                    Uri uri = Uri.parse(artworkUri);
                    Bitmap bitmap = null;
                    if ("content".equals(uri.getScheme()) || "file".equals(uri.getScheme())) {
                        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                            if (is != null) {
                                bitmap = BitmapFactory.decodeStream(is);
                            }
                        }
                    } else if (artworkUri.startsWith("http://") || artworkUri.startsWith("https://")) {
                        URL url = new URL(artworkUri);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        try (InputStream is = conn.getInputStream()) {
                            bitmap = BitmapFactory.decodeStream(is);
                        }
                    }
                    if (bitmap != null) {
                        RemoteViews coverView = new RemoteViews(context.getPackageName(), R.layout.widget_player);
                        coverView.setImageViewBitmap(R.id.widget_cover, bitmap);
                        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, coverView);
                    }
                } catch (Exception ignored) {
                }
            }).start();
        } else {
            views.setImageViewResource(R.id.widget_cover, R.mipmap.ic_launcher);
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
        }
    }
}
