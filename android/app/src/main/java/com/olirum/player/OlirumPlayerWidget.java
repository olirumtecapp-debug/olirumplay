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

import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import com.olirum.player.service.OlirumPlaybackService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OlirumPlayerWidget extends AppWidgetProvider {
    public static final String ACTION_WIDGET_PLAY_PAUSE = "com.olirum.player.ACTION_WIDGET_PLAY_PAUSE";
    public static final String ACTION_WIDGET_PREV = "com.olirum.player.ACTION_WIDGET_PREV";
    public static final String ACTION_WIDGET_NEXT = "com.olirum.player.ACTION_WIDGET_NEXT";

    private static final ExecutorService ARTWORK_EXECUTOR = Executors.newSingleThreadExecutor();
    private static String pendingArtwork;
    private static Bitmap cover;

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        refresh(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        ExoPlayer player = OlirumPlaybackService.Companion.getCurrentPlayer();
        String action = intent.getAction();

        if (ACTION_WIDGET_PLAY_PAUSE.equals(action)) {
            if (player == null || player.getMediaItemCount() == 0) {
                try {
                    openApp(context).send();
                } catch (PendingIntent.CanceledException ignored) {}
            } else if (player.getPlayWhenReady() && player.getPlaybackState() != Player.STATE_ENDED) {
                player.pause();
            } else {
                if (player.getPlaybackState() == Player.STATE_ENDED) {
                    player.seekToDefaultPosition();
                }
                if (player.getPlaybackState() == Player.STATE_IDLE) {
                    player.prepare();
                }
                player.play();
            }
        } else if (player != null && ACTION_WIDGET_PREV.equals(action)) {
            if (player.isCurrentMediaItemSeekable() && player.getCurrentPosition() > 3000) {
                player.seekTo(0);
            } else if (player.hasPreviousMediaItem()) {
                player.seekToPreviousMediaItem();
            }
        } else if (player != null && ACTION_WIDGET_NEXT.equals(action) && player.hasNextMediaItem()) {
            player.seekToNextMediaItem();
        }
    }

    public static void refresh(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] original = manager.getAppWidgetIds(new ComponentName(context, OlirumPlayerWidget.class));
        int[] compact = manager.getAppWidgetIds(new ComponentName(context, OlirumCompactWidget.class));
        int[] wide = manager.getAppWidgetIds(new ComponentName(context, OlirumWideWidget.class));
        if (original.length + compact.length + wide.length == 0) return;

        ExoPlayer player = OlirumPlaybackService.Companion.getCurrentPlayer();
        boolean ready = player != null && player.getMediaItemCount() > 0;
        boolean playing = ready && player.getPlayWhenReady() && player.getPlaybackState() != Player.STATE_ENDED;
        boolean previous = ready && (player.hasPreviousMediaItem() || player.isCurrentMediaItemSeekable());
        boolean next = ready && player.hasNextMediaItem();
        CharSequence title = ready ? player.getMediaMetadata().title : null;
        CharSequence artist = ready ? player.getMediaMetadata().artist : null;
        Uri artwork = ready ? player.getMediaMetadata().artworkUri : null;
        String artworkKey = original.length > 0 && artwork != null ? artwork.toString() : null;

        if (!java.util.Objects.equals(pendingArtwork, artworkKey)) {
            pendingArtwork = artworkKey;
            cover = null;
            if (artworkKey != null) {
                Context app = context.getApplicationContext();
                ARTWORK_EXECUTOR.execute(() -> {
                    Bitmap loaded = loadCover(app, artwork);
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        if (artworkKey.equals(pendingArtwork) && loaded != null) {
                            cover = loaded;
                            refresh(app);
                        }
                    });
                });
            }
        }

        PendingIntent app = openApp(context);
        PendingIntent play = ready ? control(context, ACTION_WIDGET_PLAY_PAUSE, 101) : app;
        RemoteViews small = new RemoteViews(context.getPackageName(), R.layout.widget_player_compact);
        bindPlay(context, small, play, ready, playing);
        if (compact.length > 0) manager.updateAppWidget(compact, small);

        for (int layout : new int[] {R.layout.widget_player, R.layout.widget_player_wide}) {
            int[] ids = layout == R.layout.widget_player ? original : wide;
            if (ids.length == 0) continue;
            RemoteViews views = new RemoteViews(context.getPackageName(), layout);
            views.setTextViewText(R.id.widget_title, title == null ? "OlirumPlay" : title);
            views.setTextViewText(R.id.widget_artist, artist == null ? "Toque para abrir" : artist);
            views.setOnClickPendingIntent(R.id.widget_root, app);
            bindPlay(context, views, play, ready, playing);
            bindSkip(context, views, R.id.widget_prev, previous, ACTION_WIDGET_PREV, 102);
            bindSkip(context, views, R.id.widget_next, next, ACTION_WIDGET_NEXT, 103);
            if (layout == R.layout.widget_player && cover != null) {
                views.setImageViewBitmap(R.id.widget_cover, cover);
            }
            manager.updateAppWidget(ids, views);
        }
    }

    private static void bindPlay(Context context, RemoteViews views, PendingIntent action, boolean ready, boolean playing) {
        views.setImageViewResource(R.id.widget_play, playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        views.setContentDescription(R.id.widget_play, context.getString(!ready
                ? R.string.widget_open_player : playing ? R.string.widget_pause : R.string.widget_play));
        views.setOnClickPendingIntent(R.id.widget_play, action);
    }

    private static void bindSkip(Context context, RemoteViews views, int id, boolean available, String action, int code) {
        views.setInt(id, "setImageAlpha", available ? 255 : 100);
        views.setOnClickPendingIntent(id, available ? control(context, action, code) : null);
    }

    private static PendingIntent openApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private static PendingIntent control(Context context, String action, int code) {
        Intent intent = new Intent(context, OlirumPlayerWidget.class).setAction(action);
        return PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private static Bitmap loadCover(Context context, Uri uri) {
        String scheme = uri.getScheme();
        HttpURLConnection connection = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        try {
            if ("content".equals(scheme) || "file".equals(scheme)) {
                try (InputStream image = context.getContentResolver().openInputStream(uri)) {
                    return image == null ? null : BitmapFactory.decodeStream(image, null, options);
                }
            }
            if ("http".equals(scheme) || "https".equals(scheme)) {
                connection = (HttpURLConnection) new URL(uri.toString()).openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                try (InputStream image = connection.getInputStream()) {
                    return BitmapFactory.decodeStream(image, null, options);
                }
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
        return null;
    }
}
