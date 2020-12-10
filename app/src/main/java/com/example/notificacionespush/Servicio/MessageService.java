package com.example.notificacionespush.Servicio;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.notificacionespush.Actividades.MainActivity;
import com.example.notificacionespush.App.AppConfig;
import com.example.notificacionespush.Util.NotificationsU;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageService extends FirebaseMessagingService {
    private static final String TAG = MessageService.class.getSimpleName();

    private NotificationsU notificationsU;

    @Override
    public void onNewToken(String s){
        super.onNewToken(s);
        Log.e("NEW TOKEN", s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        Log.e(TAG, "DESDE" + remoteMessage.getFrom());
        if (remoteMessage == null) return;
        if (remoteMessage.getNotification() !=null){
            Log.e(TAG, "Cuerpo de la Notificacion: " + remoteMessage.getNotification().getBody());
            manejarNotificacion(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getImageUrl().toString());
        }

        if (remoteMessage.getData().size() > 0){
            Log.e(TAG, "Carga de Datos: " + remoteMessage.getData().toString());
            try {
                JSONObject jsonObject = new JSONObject(remoteMessage.getData().toString());
                interpretarMensaje(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Excepcion " + e);
            }
        }
    }

    private void manejarNotificacion(String titulo, String mensaje, String url_imagen){
        if (!NotificationsU.isAppIsInBackground(getApplicationContext())){
            Intent pushNotification = new Intent(AppConfig.PUSH_NOTIFICATION);
            pushNotification.putExtra("titulo", titulo);
            pushNotification.putExtra("mensaje", mensaje);
            pushNotification.putExtra("url_imagen", url_imagen);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            notificationsU = new NotificationsU(getApplicationContext());
            notificationsU.playNotificationAlarm();
        }
    }

    private void interpretarMensaje(JSONObject jsonObject){
        Log.e(TAG,"push JSON: " + jsonObject.toString());

        try{
            JSONObject datos = jsonObject.getJSONObject("data");
            String titulo = datos.getString("title");
            String mensaje = datos.getString("message");
            boolean isBackground = datos.getBoolean("is_background");
            String urlImagen = datos.getString("image");
            String timeStamp = datos.getString("timestamp");
            JSONObject payload = datos.getJSONObject("payload");

            Log.e(TAG, "titulo " + titulo);
            Log.e(TAG, "mensaje " + mensaje);
            Log.e(TAG, "background " + isBackground);
            Log.e(TAG, "imagen " + urlImagen);
            Log.e(TAG, "timestamp " + timeStamp);
            Log.e(TAG, "payload " + payload.toString());

            if (!NotificationsU.isAppIsInBackground(getApplicationContext())){
                Intent pushNotification = new Intent(AppConfig.PUSH_NOTIFICATION);
                pushNotification.putExtra("titulo", titulo);
                pushNotification.putExtra("mensaje", mensaje);
                pushNotification.putExtra("url_imagen", urlImagen);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                notificationsU = new NotificationsU(getApplicationContext());
                notificationsU.playNotificationAlarm();
            } else {
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("titulo", titulo);
                resultIntent.putExtra("mensaje", mensaje);
                resultIntent.putExtra("url_imagen", urlImagen);
                if (TextUtils.isEmpty(urlImagen)){
                    notificationsU.showNotificacionMessage(titulo, mensaje, timeStamp, resultIntent);
                } else {
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    notificationsU.showNotificacionMessage(titulo, mensaje, timeStamp, resultIntent, urlImagen);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
