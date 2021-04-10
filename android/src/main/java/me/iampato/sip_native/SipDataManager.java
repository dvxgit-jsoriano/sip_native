package me.iampato.sip_native;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Handler;
import android.util.Log;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

public class SipDataManager {
    final private String TAG = "SIP Native";
    private final Handler uiThreadHandler;
    /// To use the SIP API, your application must create a SipManager object.
    /// The SipManager takes care of the following in your application:
    ///  1. Initiating SIP sessions.
    ///  2. Initiating and receiving calls.
    ///  3. Registering and unregistering with a SIP provider.
    ///  4. Verifying session connectivity.
    public SipManager sipManager;
    /// A SipProfile defines a SIP profile, including a SIP account, and domain
    /// and server information.
    public SipProfile sipProfile;
    private Context context;
    private SipAudioCall call;
    private UserProfile userProfile;

    public SipDataManager(Context context, Handler uiThreadHandler) {
        this.context = context;
        this.uiThreadHandler = uiThreadHandler;
    }

    public void initialize() throws Exception {
        Log.d(TAG, "initialize manager");
        if (sipManager == null) {
            Log.d(TAG, "sip manager is not null");
            sipManager = SipManager.newInstance(context);
        }
    }

    public boolean isVoipSupported(Context context) {
        boolean isVoipSupported = false;
        // check if Sip manager is supported
        if (SipManager.isVoipSupported(context)) {
            Log.d(TAG, "Supported!");
            isVoipSupported = true;
        } else {
            Log.d(TAG, "Not Supported");
        }
        return isVoipSupported;
    }

    public boolean isSipManagerSupported(Context context) {
        boolean isSipManagerSupported = false;
        if (SipManager.isApiSupported(context)) {
            Log.d(TAG, "Supported!");
            isSipManagerSupported = true;
        } else {
            Log.d(TAG, "NotSupported!");
        }
        return isSipManagerSupported;
    }

    public void initializeProfile() throws Exception {
        if (sipManager == null) {
            throw new Exception("Sip manager cannot be null");
        }
        try {
            SipProfile.Builder builder = new SipProfile.Builder(userProfile.getUsername(), userProfile.getDomain());
            builder.setPassword(userProfile.getPassword());
            builder.setPort(userProfile.port);
            builder.setProtocol(userProfile.Protocol);
            builder.setAutoRegistration(true);
            this.sipProfile = builder.build();
            Intent intent = new Intent();
            intent.setAction("android.SipDemo.INCOMING_CALL");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, Intent.FILL_IN_DATA);
            sipManager.open(sipProfile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupRegistrationStream(final EventChannel.EventSink events, SipRegistrationState registrationState) throws Exception {
        if (sipManager != null && this.sipProfile != null) {
            SipRegistrationListener listener = new MySipRegistrationListener(uiThreadHandler, events, registrationState);
            sipManager.setRegistrationListener(sipProfile.getUriString(), listener);
        } else {
            throw new Exception("sip manager || sip profile cannot be null");
        }
    }

    public void makeCall(MethodChannel.Result result, String address) {
        try {
            call = sipManager.makeAudioCall(sipProfile.getUriString(), address,null , 30);
            if (call != null) {
                result.success(true);
            } else {
                result.success(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.error(null, e.toString(), null);
        }
    }

    public void setupCallStateStream(final EventChannel.EventSink events, SipCallState sipCallState) throws Exception {
        if (call != null) {
            call.setListener(new MySipCallListener(uiThreadHandler, events, sipCallState));
        } else {
            throw new Exception("sip manager cannot be null");
        }
    }

    public void endCall(MethodChannel.Result result) {
        try {
            if (call != null) {
                call.endCall();
                result.success(true);
            } else {
                throw new Exception("No call is in progress");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.error(null, e.toString(), null);
        }
    }

    public void holdCall(MethodChannel.Result result) {
        try {
            if (call != null) {
                call.holdCall(0);
                result.success(true);
            } else {
                throw new Exception("No call is in progress");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.error(null, e.toString(), null);
        }
    }

    public void callSpeakerMode(MethodChannel.Result result, boolean value) {
        try {
            if (call != null) {
                call.setSpeakerMode(value);
                result.success(true);
            } else {
                throw new Exception("No call is in progress");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.error(null, e.toString(), null);
        }
    }

    public void muteCall(MethodChannel.Result result) {
        try {
            if (call != null) {
                call.toggleMute();
                result.success(true);
            } else {
                throw new Exception("No call is in progress");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.error(null, e.toString(), null);
        }
    }

    public boolean dispose() {
        if (sipManager == null || call == null) {
            return true;
        } else {
            try {
                if (sipProfile != null) {
                    sipManager.close(sipProfile.getUriString());
                }
                if (call != null) {
                    call.close();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public SipManager getSipManager() {
        return sipManager;
    }

    public void setSipManager(SipManager sipManager) {
        this.sipManager = sipManager;
    }

    public SipAudioCall getCall() {
        return call;
    }

    public Context getContext() {
        return context;
    }
}
//getters and setters