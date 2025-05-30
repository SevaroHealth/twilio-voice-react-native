package com.twiliovoicereactnative;

import static com.twiliovoicereactnative.VoiceApplicationProxy.getCallRecordDatabase;
import static com.twiliovoicereactnative.VoiceApplicationProxy.getVoiceServiceApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.twilio.voice.CallInvite;
import com.twiliovoicereactnative.CallRecordDatabase.CallRecord;

import java.util.Objects;
import java.util.UUID;
import java.util.Map;
import android.util.Log;

public class VoiceBroadcastReceiver {
    private static final String TAG = "VoiceBroadcastReceiver";

    // Action constants
    public static final String ACTION_ACCEPT_INCOMING_CALL = "com.twiliovoicereactnative.ACCEPT_INCOMING_CALL";
    public static final String ACTION_REJECT_INCOMING_CALL = "com.twiliovoicereactnative.REJECT_INCOMING_CALL";
    public static final String ACTION_END_CALL = "com.twiliovoicereactnative.END_CALL";
    public static final String ACTION_HOLD_CALL = "com.twiliovoicereactnative.HOLD_CALL";
    public static final String ACTION_MUTE_CALL = "com.twiliovoicereactnative.MUTE_CALL";

    public static final String TRIAGE_ACTION_START_INCOMING_CALL = "com.sevaro.twilio.START_INCOMING_CALL";
    public static final String TRIAGE_ACTION_REPORT_END_CALL = "com.sevaro.twilio.END_CALL";

    private final Context context;
    private final LocalBroadcastManager broadcastManager;
    private final BroadcastReceiver receiver;

    public VoiceBroadcastReceiver(Context context) {
        this.context = context.getApplicationContext();
        this.broadcastManager = LocalBroadcastManager.getInstance(context);
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleIntent(intent);
            }
        };
    }

    public void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ACCEPT_INCOMING_CALL);
        filter.addAction(ACTION_REJECT_INCOMING_CALL);
        filter.addAction(ACTION_END_CALL);
        filter.addAction(ACTION_HOLD_CALL);
        filter.addAction(ACTION_MUTE_CALL);
        broadcastManager.registerReceiver(receiver, filter);
        Log.d(TAG, "VoiceBroadcastReceiver registered");
    }

    public void unregister() {
        try {
            broadcastManager.unregisterReceiver(receiver);
            Log.d(TAG, "VoiceBroadcastReceiver unregistered");
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
        }
    }

    private static CallRecordDatabase.CallRecord getCallRecord(final UUID uuid) {
        return Objects.requireNonNull(getCallRecordDatabase().get(new CallRecordDatabase.CallRecord(uuid)));
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            Log.e(TAG, "Received intent with null action");
            return;
        }

        // Get call UUID from intent
        String callUuidStr = intent.getStringExtra("callUUID");
        if (callUuidStr == null) {
            Log.e(TAG, "No callUUID provided in intent");
            return;
        }

        UUID callUuid;
        try {
            callUuid = UUID.fromString(callUuidStr);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid callUUID format: " + callUuidStr);
            return;
        }

        // Get call record from UUID
        CallRecordDatabase.CallRecord callRecord = this.getCallRecord(callUuid);
        if (callRecord == null) {
            Log.e(TAG, "No call record found for UUID: " + callUuid);
            return;
        }

        // Handle different actions
        switch (action) {
            case ACTION_ACCEPT_INCOMING_CALL:
                Log.d(TAG, "Accepting incoming call: " + callUuid);
                getVoiceServiceApi().acceptCall(callRecord);
                break;

            case ACTION_REJECT_INCOMING_CALL:
                Log.d(TAG, "Rejecting incoming call: " + callUuid);
                getVoiceServiceApi().rejectCall(callRecord);
                break;
            case ACTION_END_CALL:
                Log.d(TAG, "Disconnecing ongoing call: " + callUuid);
                getVoiceServiceApi().disconnect(callRecord);
                break;
            default:
                Log.w(TAG, "Unknown action received: " + action);
                break;
        }
    }

    public static String getName(CallRecordDatabase.CallRecord callRecord) {
      if (callRecord.getDirection() == CallRecord.Direction.INCOMING) {
        final String template = ConfigurationProperties.getIncomingCallContactHandleTemplate();
        if (template != null) {
          final String processedTemplate =
            templateDisplayName(template, callRecord.getCustomParameters());
          if (!processedTemplate.isEmpty()) {
            return processedTemplate;
          }
        }

        final CallInvite callInvite = callRecord.getCallInvite();
        final String from = null != callInvite ? getDisplayName(callInvite) : "";
        return from;
      }

      // this.callRecord.Direction == CallRecord.Direction.OUTGOING
      final String notificationDisplayName = callRecord.getNotificationDisplayName();
      if (notificationDisplayName != null && !notificationDisplayName.isEmpty()) {
        return notificationDisplayName;
      }

      final String to = callRecord.getCallRecipient();
      return to;
    }

    private static String getDisplayName(@NonNull CallInvite callInvite) {
      final String title = callInvite.getFrom();
      if (title.startsWith("client:")) {
        return title.replaceFirst("client:", "");
      }
      return title;
    }
    
    private static String templateDisplayName(final String template, final Map<String, String> twimlParams) {
      String processedTemplate = template;

      for (Map.Entry<String, String> e : twimlParams.entrySet()) {
        String paramKey = e.getKey();
        String paramValue = e.getValue();
        processedTemplate = processedTemplate.replaceAll(
          String.format("\\$\\{%s\\}", paramKey),
          paramValue);
      }

      return processedTemplate;
    }

    public static void broadCastIntent(@NonNull Context context, @NonNull String action, @NonNull Bundle extras) {
        Intent intent = new Intent(action);
        intent.putExtra("callData", extras);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static boolean isPhoneAccountRegistered(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences("sevaro_voice_prefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("phone_account_registered", false);
    }
} 