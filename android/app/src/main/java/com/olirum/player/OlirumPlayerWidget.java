package com.olirum.player;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.media3.exoplayer.ExoPlayer;

import com.olirum.player.service.OlirumPlaybackService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OlirumPlayerWidget extends AppWidgetProvider {

    public static final String ACTION_WIDGET_PLAY_PAUSE = "com.olirum.player.ACTION_WIDGET_PLAY_PAUSE";
    public static final String ACTION_WIDGET_PREV = "com.olirum.player.ACTION_WIDGET_PREV";
    public static final String ACTION_WIDGET_NEXT = "com.olirum.player.ACTION_WIDGET_NEXT";

    private static final String PREFS_NAME = "olirum_widget_prefs";
    private static final String KEY_TITLE = "widget_title";
    private static final String KEY_ARTIST = "widget_artist";
    private static final String KEY_IS_PLAYING = "widget_is_playing";
    private static final String KEY_ARTWORK = "widget_artwork";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String title = sp.getString(KEY_TITLE, "OlirumPlay");
        String artist = sp.getString(KEY_ARTIST, "Toque para ouvir música");
        boolean isPlaying = sp.getBoolean(KEY_IS_PLAYING, false);
        String artworkUri = sp.getString(KEY_ARTWORK, null);

        for (int appWidgetId : appWidgetIds) {
            updateWidgetUI(context, appWidgetManager, appWidgetId, title, artist, isPlaying, artworkUri);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent != null ? intent.getAction() : null;
        if (ACTION_WIDGET_PLAY_PAUSE.equals(action) ||
            ACTION_WIDGET_PREV.equals(action) ||
            ACTION_WIDGET_NEXT.equals(action)) {

            // 1. EXECUÇÃO IMEDIATA: Se o Player já está instanciado na memória, opera sem atraso de IPC!
            ExoPlayer player = OlirumPlaybackService.Companion.getCurrentPlayer();
            if (player != null) {
                if (ACTION_WIDGET_PLAY_PAUSE.equals(action)) {
                    if (player.isPlaying()) {
                        player.pause();
                    } else if (player.getMediaItemCount() > 0) {
                        player.play();
                    } else {
                        triggerServiceAction(context, action);
                    }
                } else if (ACTION_WIDGET_PREV.equals(action)) {
                    if (player.getCurrentPosition() > 3000) {
                        player.seekTo(0);
                    } else if (player.hasPreviousMediaItem()) {
                        player.seekToPreviousMediaItem();
                    }
                } else if (ACTION_WIDGET_NEXT.equals(action)) {
                    if (player.hasNextMediaItem()) {
                        player.seekToNextMediaItem();
                    }
                }
                return;
            }

            // 2. Se o serviço estiver dormindo, acorda o OlirumPlaybackService para iniciar a reprodução
            triggerServiceAction(context, action);
        }
    }

    private static void triggerServiceAction(Context context, String action) {
        try {
            Intent serviceIntent = new Intent(context, OlirumPlaybackService.class);
            serviceIntent.setAction(action);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Intent openIntent = new Intent(context, MainActivity.class);
                openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(openIntent);
            } catch (Exception ignored) {}
        }
    }

    public static void updateAllWidgets(Context context, String title, String artist, boolean isPlaying, String artworkUri) {
        String finalTitle = (title != null && !title.trim().isEmpty()) ? title : "OlirumPlay";
        String finalArtist = (artist != null && !artist.trim().isEmpty()) ? artist : "Toque para ouvir música";

        try {
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            sp.edit()
                .putString(KEY_TITLE, finalTitle)
                .putString(KEY_ARTIST, finalArtist)
                .putBoolean(KEY_IS_PLAYING, isPlaying)
                .putString(KEY_ARTWORK, artworkUri)
                .apply();
        } catch (Exception ignored) {}

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, OlirumPlayerWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        if (appWidgetIds != null) {
            for (int id : appWidgetIds) {
                updateWidgetUI(context, appWidgetManager, id, finalTitle, finalArtist, isPlaying, artworkUri);
            }
        }
    }

    private static void updateWidgetUI(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                                      String title, String artist, boolean isPlaying, String artworkUri) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_player);

        views.setTextViewText(R.id.widget_title, title);
        views.setTextViewText(R.id.widget_artist, artist);

        int playIcon = isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        views.setImageViewResource(R.id.widget_play, playIcon);

        // Click no card inteiro ou no texto abre o app OlirumPlay
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingApp = PendingIntent.getActivity(
                context, 0, appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_root, pendingApp);
        views.setOnClickPendingIntent(R.id.widget_cover, pendingApp);
        views.setOnClickPendingIntent(R.id.widget_title, pendingApp);
        views.setOnClickPendingIntent(R.id.widget_artist, pendingApp);

        // Click Play/Pause
        Intent playIntent = new Intent(context, OlirumPlayerWidget.class);
        playIntent.setAction(ACTION_WIDGET_PLAY_PAUSE);
        PendingIntent pendingPlay = PendingIntent.getBroadcast(
                context, 101, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_play, pendingPlay);

        // Click Anterior
        Intent prevIntent = new Intent(context, OlirumPlayerWidget.class);
        prevIntent.setAction(ACTION_WIDGET_PREV);
        PendingIntent pendingPrev = PendingIntent.getBroadcast(
                context, 102, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_prev, pendingPrev);

        // Click Próxima
        Intent nextIntent = new Intent(context, OlirumPlayerWidget.class);
        nextIntent.setAction(ACTION_WIDGET_NEXT);
        PendingIntent pendingNext = PendingIntent.getBroadcast(
                context, 103, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_next, pendingNext);

        appWidgetManager.updateAppWidget(appWidgetId, views);

        // Carrega capa assincronamente
        if (artworkUri != null && !artworkUri.trim().isEmpty()) {
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
                        conn.setConnectTimeout(3000);
                        conn.setReadTimeout(3000);
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
