package org.thoughtcrime.securesms.components.webrtc;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;

import org.thoughtcrime.redphone.util.AudioUtils;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.ViewUtil;

public class WebRtcCallControls extends LinearLayout {

  private CompoundButton          audioMuteButton;
  private CompoundButton          videoMuteButton;
  private WebRtcInCallAudioButton audioButton;

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public WebRtcCallControls(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize();
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public WebRtcCallControls(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  public WebRtcCallControls(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public WebRtcCallControls(Context context) {
    super(context);
    initialize();
  }

  private void initialize() {
    LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.webrtc_call_controls, this, true);

    this.audioMuteButton = (CompoundButton) findViewById(R.id.muteButton);
    this.videoMuteButton = ViewUtil.findById(this, R.id.video_mute_button);
    this.audioButton     = new WebRtcInCallAudioButton((CompoundButton) findViewById(R.id.audioButton));

    updateAudioButton();
  }

  public void updateAudioButton() {
    audioButton.setAudioMode(AudioUtils.getCurrentAudioMode(getContext()));

    IntentFilter filter = new IntentFilter();
    filter.addAction(AudioUtils.getScoUpdateAction());
    handleBluetoothIntent(getContext().registerReceiver(null, filter));
  }


  private void handleBluetoothIntent(Intent intent) {
    if (intent == null) {
      return;
    }

    if (!intent.getAction().equals(AudioUtils.getScoUpdateAction())) {
      return;
    }

    Integer state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
    if (state.equals(AudioManager.SCO_AUDIO_STATE_CONNECTED)) {
      audioButton.setHeadsetAvailable(true);
    } else if (state.equals(AudioManager.SCO_AUDIO_STATE_DISCONNECTED)) {
      audioButton.setHeadsetAvailable(false);
    }
  }

  public void setAudioMuteButtonListener(final MuteButtonListener listener) {
    audioMuteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        listener.onToggle(b);
      }
    });
  }

  public void setVideoMuteButtonListener(final MuteButtonListener listener) {
    videoMuteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        listener.onToggle(!isChecked);
      }
    });
  }

  public void setAudioButtonListener(final AudioButtonListener listener) {
    audioButton.setListener(listener);
  }

  public boolean isVideoEnabled() {
    return videoMuteButton.isChecked();
  }

  public void setVideoEnabled(boolean enabled) {
    videoMuteButton.setChecked(enabled);
  }

  public void displayVideoTooltip(ViewGroup viewGroup) {
    if (Build.VERSION.SDK_INT > 15) {
      final ToolTipsManager toolTipsManager = new ToolTipsManager();

      ToolTip toolTip = new ToolTip.Builder(getContext(), videoMuteButton, viewGroup,
                                            getContext().getString(R.string.WebRtcCallControls_tap_to_enable_your_video),
                                            ToolTip.POSITION_BELOW).build();
      toolTipsManager.show(toolTip);

      videoMuteButton.postDelayed(new Runnable() {
        @Override
        public void run() {
          toolTipsManager.findAndDismiss(videoMuteButton);
        }
      }, 4000);
    }
  }

  public void reset() {
    updateAudioButton();
    audioMuteButton.setChecked(false);
    videoMuteButton.setChecked(false);
  }

  public static interface MuteButtonListener {
    public void onToggle(boolean isMuted);
  }

  public static interface AudioButtonListener {
    public void onAudioChange(AudioUtils.AudioMode mode);
  }





}
