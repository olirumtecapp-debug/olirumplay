package com.olirum.player;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            WebView webView = this.bridge.getWebView();
            if (webView != null) {
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setMediaPlaybackRequiresUserGesture(false);
                settings.setAllowFileAccess(true);
                settings.setAllowContentAccess(true);
                webView.addJavascriptInterface(new NativeMusicBridge(), "AndroidNativeMusic");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class NativeMusicBridge {

        @JavascriptInterface
        public String isNativeReady() {
            return "true";
        }

        @JavascriptInterface
        public String scanDeviceAudio() {
            JSONArray list = new JSONArray();
            try {
                ContentResolver cr = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.SIZE
                };

                String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
                Cursor cursor = cr.query(uri, projection, selection, null, MediaStore.Audio.Media.TITLE + " ASC");

                if (cursor != null) {
                    int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                    int artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                    int albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                    int durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                    int dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                    int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idCol);
                        String title = cursor.getString(titleCol);
                        String artist = cursor.getString(artistCol);
                        String album = cursor.getString(albumCol);
                        long dur = cursor.getLong(durCol);
                        String path = cursor.getString(dataCol);
                        long size = cursor.getLong(sizeCol);

                        JSONObject item = new JSONObject();
                        item.put("id", "native-" + id);
                        item.put("title", title != null ? title : "Faixa Sem Título");
                        item.put("artist", (artist != null && !artist.equals("<unknown>")) ? artist : "Artista do Aparelho");
                        item.put("album", (album != null && !album.equals("<unknown>")) ? album : "Música Local");
                        item.put("duration", dur > 0 ? (dur / 1000) : 210);
                        item.put("audioUrl", "content://media/external/audio/media/" + id);
                        item.put("filePath", path != null ? path : "");
                        item.put("fileSize", size);
                        item.put("source", "local-device");
                        list.put(item);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list.toString();
        }
    }
}
