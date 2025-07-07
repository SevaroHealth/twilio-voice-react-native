package com.twiliovoicereactnative;

import android.content.Context;

class ConfigurationProperties {
  private static String incomingCallContactHandleTemplate = null;

  public static void setIncomingCallContactHandleTemplate(String template) {
    ConfigurationProperties.incomingCallContactHandleTemplate = template;
  }

  public static void setIncomingCallContactHandleTemplate(String template, Context context) {
    ConfigurationProperties.incomingCallContactHandleTemplate = template;
    // Save to persistent storage so it's available on cold start
    context.getSharedPreferences("twilio_voice_config", Context.MODE_PRIVATE)
      .edit()
      .putString("incoming_call_contact_handle_template", template)
      .apply();
  }

  public static String getIncomingCallContactHandleTemplate() {
    return ConfigurationProperties.incomingCallContactHandleTemplate;
  }

  public static String getIncomingCallContactHandleTemplate(Context context) {
    if (incomingCallContactHandleTemplate == null) {
      // Load from persistent storage if not in memory
      incomingCallContactHandleTemplate = context.getSharedPreferences("twilio_voice_config", Context.MODE_PRIVATE)
        .getString("incoming_call_contact_handle_template", null);
    }
    return incomingCallContactHandleTemplate;
  }

  /**
   * Get configuration boolean, used to determine if the built-in Firebase service should be enabled
   * or not.
   * @param context the application context
   * @return a boolean read from the application resources
   */
  public static boolean isFirebaseServiceEnabled(Context context) {
    return context.getResources()
      .getBoolean(R.bool.twiliovoicereactnative_firebasemessagingservice_enabled);
  }
}
