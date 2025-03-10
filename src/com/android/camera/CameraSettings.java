/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.util.FloatMath;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 *  Provides utilities and keys for Camera settings.
 */
public class CameraSettings {
    private static final int NOT_FOUND = -1;

    public static final String KEY_VERSION = "pref_version_key";
    public static final String KEY_LOCAL_VERSION = "pref_local_version_key";
    public static final String KEY_RECORD_LOCATION = RecordLocationPreference.KEY;
    public static final String KEY_VIDEO_QUALITY = "pref_video_quality_key";
    public static final String KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL = "pref_video_time_lapse_frame_interval_key";
    public static final String KEY_PICTURE_SIZE = "pref_camera_picturesize_key";
    public static final String KEY_JPEG_QUALITY = "pref_camera_jpegquality_key";
    public static final String KEY_FOCUS_MODE = "pref_camera_focusmode_key";
    public static final String KEY_FLASH_MODE = "pref_camera_flashmode_key";
    public static final String KEY_VIDEOCAMERA_FLASH_MODE = "pref_camera_video_flashmode_key";
    public static final String KEY_WHITE_BALANCE = "pref_camera_whitebalance_key";
    public static final String KEY_SCENE_MODE = "pref_camera_scenemode_key";
    public static final String KEY_EXPOSURE = "pref_camera_exposure_key";
    public static final String KEY_VIDEO_EFFECT = "pref_video_effect_key";
    public static final String KEY_CAMERA_ID = "pref_camera_id_key";
    public static final String KEY_CAMERA_FIRST_USE_HINT_SHOWN = "pref_camera_first_use_hint_shown_key";
    public static final String KEY_VIDEO_FIRST_USE_HINT_SHOWN = "pref_video_first_use_hint_shown_key";
    // Add ISO setting menu by wk.m
    public static final String KEY_ISO_VALUE = "pref_camera_isovalue_key";
    //--------------------------------------------------------------------
    // Add Effect setting menu by wk.m
    public static final String KEY_EFFECT = "pref_camera_effect_key";
    //--------------------------------------------------------------------
    // Add Brightness setting menu by wk.m
    public static final String KEY_BRIGHTNESS = "pref_camera_brightness_key";
    //--------------------------------------------------------------------
    // Add Metering setting menu by wk.m
    public static final String KEY_METERING = "pref_camera_metering_key";
    //--------------------------------------------------------------------
    // Add Anti-banding setting menu by wk.m
    public static final String KEY_ANTIBANDING = "pref_camera_antibanding_key";
    //--------------------------------------------------------------------
    // Add Contrast setting menu by wk.m
    public static final String KEY_CONTRAST = "pref_camera_contrast_key";
    //--------------------------------------------------------------------
    // Add Saturation setting menu by wk.m
    public static final String KEY_SATURATION = "pref_camera_saturation_key";
    //--------------------------------------------------------------------
    // Add Sharpness setting menu by wk.m
    public static final String KEY_SHARPNESS = "pref_camera_sharpness_key";
    //--------------------------------------------------------------------
    // Add Hue setting menu by wk.m
    public static final String KEY_HUE = "pref_camera_hue_key";
    //--------------------------------------------------------------------
    // Add Face Detection setting menu by wk.m
    public static final String KEY_WDR = "pref_camera_wdr_key";
    //--------------------------------------------------------------------
    // Add Jpeg Quality setting menu by wk.m
    public static final String KEY_JPEG_QUAL = "pref_camera_jpeg_qual_key";
    //--------------------------------------------------------------------
    public static final String KEY_AE_LOCK = "pref_camera_ae_lock_key";
    public static final String KEY_AWB_LOCK = "pref_camera_awb_lock_key";

    public static final String EXPOSURE_DEFAULT_VALUE = "0";
    // by Brightness wk.m
    public static final String BRIGHTNESS_DEFAULT_VALUE = "0";
    // by Saturation wk.m
    public static final String SATURATION_DEFAULT_VALUE = "0";
    // by Sharpness wk.m
    public static final String SHARPNESS_DEFAULT_VALUE = "0";
    // by Hue wk.m
    public static final String HUE_DEFAULT_VALUE = "0";
    // by WDR wk.m
    public static final String WDR_DEFAULT_VALUE = "0";
    // by Jpeg Quality wk.m
    public static final String JPEG_QUAL_DEFAULT_VALUE = "100";

    // Auto Exposure and WhiteBalance Lock by hm choi
    public static final String AE_LOCK_DEFAULT_VALUE = "0";
    public static final String AWB_LOCK_DEFAULT_VALUE = "0";

    public static final int CURRENT_VERSION = 5;
    public static final int CURRENT_LOCAL_VERSION = 2;

    public static final int DEFAULT_VIDEO_DURATION = 0; // no limit

    private static final String TAG = "CameraSettings";

    private final Context mContext;
    private final Parameters mParameters;
    private final CameraInfo[] mCameraInfo;
    private final int mCameraId;

    public CameraSettings(Activity activity, Parameters parameters,
                          int cameraId, CameraInfo[] cameraInfo) {
        mContext = activity;
        mParameters = parameters;
        mCameraId = cameraId;
        mCameraInfo = cameraInfo;
    }

    public PreferenceGroup getPreferenceGroup(int preferenceRes) {
        PreferenceInflater inflater = new PreferenceInflater(mContext);
        PreferenceGroup group =
                (PreferenceGroup) inflater.inflate(preferenceRes);
        initPreference(group);
        return group;
    }

    public static String getDefaultVideoQuality(int cameraId,
            String defaultQuality) {
        int quality = Integer.valueOf(defaultQuality);
        if (CamcorderProfile.hasProfile(cameraId, quality)) {
            return defaultQuality;
        }
        return Integer.toString(CamcorderProfile.QUALITY_HIGH);
    }

    public static void initialCameraPictureSize(
            Context context, Parameters parameters) {
        // When launching the camera app first time, we will set the picture
        // size to the first one in the list defined in "arrays.xml" and is also
        // supported by the driver.
        List<Size> supported = parameters.getSupportedPictureSizes();
        if (supported == null) return;
        for (String candidate : context.getResources().getStringArray(
                R.array.pref_camera_picturesize_entryvalues)) {
            if (setCameraPictureSize(candidate, supported, parameters)) {
                SharedPreferences.Editor editor = ComboPreferences
                        .get(context).edit();
                editor.putString(KEY_PICTURE_SIZE, candidate);
                editor.apply();
                return;
            }
        }
        Log.e(TAG, "No supported picture size found");
    }

    public static void removePreferenceFromScreen(
            PreferenceGroup group, String key) {
        removePreference(group, key);
    }

    public static boolean setCameraPictureSize(
            String candidate, List<Size> supported, Parameters parameters) {
        int index = candidate.indexOf('x');
        if (index == NOT_FOUND) return false;
        int width = Integer.parseInt(candidate.substring(0, index));
        int height = Integer.parseInt(candidate.substring(index + 1));
        for (Size size : supported) {
            if (size.width == width && size.height == height) {
                parameters.setPictureSize(width, height);
                return true;
            }
        }
        return false;
    }

    private void initPreference(PreferenceGroup group) {
        ListPreference videoQuality = group.findPreference(KEY_VIDEO_QUALITY);
        ListPreference timeLapseInterval = group.findPreference(KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL);
        ListPreference pictureSize = group.findPreference(KEY_PICTURE_SIZE);
        ListPreference whiteBalance =  group.findPreference(KEY_WHITE_BALANCE);
        ListPreference sceneMode = group.findPreference(KEY_SCENE_MODE);
        ListPreference flashMode = group.findPreference(KEY_FLASH_MODE);
        ListPreference focusMode = group.findPreference(KEY_FOCUS_MODE);
        ListPreference exposure = group.findPreference(KEY_EXPOSURE);
        IconListPreference cameraIdPref =
                (IconListPreference) group.findPreference(KEY_CAMERA_ID);
        ListPreference videoFlashMode =
                group.findPreference(KEY_VIDEOCAMERA_FLASH_MODE);
        ListPreference videoEffect = group.findPreference(KEY_VIDEO_EFFECT);

        // Since the screen could be loaded from different resources, we need
        // to check if the preference is available here
        if (videoQuality != null) {
            filterUnsupportedOptions(group, videoQuality, getSupportedVideoQuality());
        }

        if (pictureSize != null) {
            filterUnsupportedOptions(group, pictureSize, sizeListToStringList(
                    mParameters.getSupportedPictureSizes()));
        }
        if (whiteBalance != null) {
            filterUnsupportedOptions(group,
                    whiteBalance, mParameters.getSupportedWhiteBalance());
        }
        if (sceneMode != null) {
            filterUnsupportedOptions(group,
                    sceneMode, mParameters.getSupportedSceneModes());
        }
        if (flashMode != null) {
            filterUnsupportedOptions(group,
                    flashMode, mParameters.getSupportedFlashModes());
        }
        if (focusMode != null) {
            if (mParameters.getMaxNumFocusAreas() == 0) {
                filterUnsupportedOptions(group,
                        focusMode, mParameters.getSupportedFocusModes());
            } else {
                // Remove the focus mode if we can use tap-to-focus.
                removePreference(group, focusMode.getKey());
            }
        }
        if (videoFlashMode != null) {
            filterUnsupportedOptions(group,
                    videoFlashMode, mParameters.getSupportedFlashModes());
        }
        if (exposure != null) buildExposureCompensation(group, exposure);
        if (cameraIdPref != null) buildCameraId(group, cameraIdPref);

        if (timeLapseInterval != null) resetIfInvalid(timeLapseInterval);
        if (videoEffect != null) {
            initVideoEffect(group, videoEffect);
            resetIfInvalid(videoEffect);
        }

        //jmq.add. UI setting of "iso"
        ListPreference curPref = group.findPreference(KEY_ISO_VALUE);
		Log.e(TAG," mParameters.get ISO "+mParameters.get("iso"));//add_lzy
	 if(curPref != null && mParameters.get("iso")==null)
	 {
	     Log.i(TAG,"Can't get iso setting");
	     removePreference(group, curPref.getKey());
	 }
	 // jmq.add. UI setting of "Effect"
	 curPref = group.findPreference(KEY_EFFECT);
	 if(curPref != null && mParameters.getColorEffect()==null)
	 {
	     Log.i(TAG,"Can't get effect setting");
	     removePreference(group, curPref.getKey());
	 }
        //jmq.add. UI setting of "Brightness"
	 curPref = group.findPreference(KEY_BRIGHTNESS);
	 if(curPref != null && mParameters.get("brightness")==null)
	 {
	     Log.i(TAG,"Can't get brightness setting");
	     removePreference(group, curPref.getKey());
	 }else
	     Log.e(TAG,"brightness setting"+mParameters.get("brightness")
	     +" saturation:"+mParameters.get("saturation")
	     +" sharpness:"+mParameters.get("sharpness")
	     +" hue:"+mParameters.get("hue"));
        // jmq.add. UI setting of "Metering"
	 curPref = group.findPreference(KEY_METERING);
	 if(curPref != null && mParameters.get("metering")==null)
	 {
	     Log.i(TAG,"Can't get metering setting");
	     removePreference(group, curPref.getKey());
	 }
        // jmq.add. UI setting of "AFC"
	 curPref = group.findPreference(KEY_ANTIBANDING);
	 if(curPref != null && mParameters.getAntibanding()==null)
	 {
	     Log.i(TAG,"Can't get Antibanding setting");
	     removePreference(group, curPref.getKey());
	 }
        // jmq.add. UI setting of "Saturation"
	 curPref = group.findPreference(KEY_SATURATION);
	 if(curPref != null && mParameters.get("saturation")==null)
	 {
	     Log.i(TAG,"Can't get saturation setting");
	     removePreference(group, curPref.getKey());
	 }
        // jmq.add. UI setting of "Sharpness"
	 curPref = group.findPreference(KEY_SHARPNESS);
	 if(curPref != null && mParameters.get("sharpness")==null)
	 {
	     Log.i(TAG,"Can't get sharpness setting");
	     removePreference(group, curPref.getKey());
	 }
        // jmq.add. UI setting of "Hue"
	 curPref = group.findPreference(KEY_HUE);
	 if(curPref != null && mParameters.get("hue")==null)
	 {
	     Log.i(TAG,"Can't get hue setting");
	     removePreference(group, curPref.getKey());
	 }
        // jmq.add. UI setting of "Contrast"
	 curPref = group.findPreference(KEY_CONTRAST);
	 if(curPref != null && mParameters.get("contrast")==null)
	 {
	     Log.i(TAG,"Can't get contrast setting");
	     removePreference(group, curPref.getKey());
	 }
        // jmq.add. UI setting of "WDR"
	 curPref = group.findPreference(KEY_WDR);
	 if(curPref != null && mParameters.get("wdr")==null)
	 {
	     Log.i(TAG,"Can't get wdr setting");
	     removePreference(group, curPref.getKey());
	 }
        // jmq.add. UI setting of "Jpeg Quality"
	 curPref = group.findPreference(KEY_JPEG_QUAL);
	 if(curPref != null && mParameters.get("jpeg-quality")==null)//jmq.add.Parameters.KEY_JPEG_QUALITY is private
	 {
	     Log.i(TAG,"Can't get jpeg quality setting");
	     removePreference(group, curPref.getKey());
	 }
	 // jmq.add. UI setting of "AE Lock"
	 curPref = group.findPreference(KEY_AE_LOCK);
	 if(curPref != null && mParameters.isAutoExposureLockSupported()==false)
	 {
	     Log.i(TAG,"Can't get AE Lock setting");
	     removePreference(group, curPref.getKey());
	 }
	         // jmq.add. UI setting of "AWB Lock"
	 curPref = group.findPreference(KEY_AWB_LOCK);
	 if(curPref != null && mParameters.isAutoWhiteBalanceLockSupported()==false)
	 {
	     Log.i(TAG,"Can't get AWB Lock setting");
	     removePreference(group, curPref.getKey());
	 }
    }

    private void buildExposureCompensation(
            PreferenceGroup group, ListPreference exposure) {
        int max = mParameters.getMaxExposureCompensation();
        int min = mParameters.getMinExposureCompensation();
        if (max == 0 && min == 0) {
            removePreference(group, exposure.getKey());
            return;
        }
        float step = mParameters.getExposureCompensationStep();

        // show only integer values for exposure compensation
        int maxValue = (int) FloatMath.floor(max * step);
        int minValue = (int) FloatMath.ceil(min * step);
        CharSequence entries[] = new CharSequence[maxValue - minValue + 1];
        CharSequence entryValues[] = new CharSequence[maxValue - minValue + 1];
        for (int i = minValue; i <= maxValue; ++i) {
            entryValues[maxValue - i] = Integer.toString(Math.round(i / step));
            StringBuilder builder = new StringBuilder();
            if (i > 0) builder.append('+');
            entries[maxValue - i] = builder.append(i).toString();
        }
        exposure.setEntries(entries);
        exposure.setEntryValues(entryValues);
    }

    private void buildCameraId(
            PreferenceGroup group, IconListPreference preference) {
        int numOfCameras = mCameraInfo.length;
        if (numOfCameras < 2) {
            removePreference(group, preference.getKey());
            return;
        }

        CharSequence[] entryValues = new CharSequence[2];
        for (int i = 0; i < mCameraInfo.length; ++i) {
            int index =
                    (mCameraInfo[i].facing == CameraInfo.CAMERA_FACING_FRONT)
                    ? CameraInfo.CAMERA_FACING_FRONT
                    : CameraInfo.CAMERA_FACING_BACK;
            if (entryValues[index] == null) {
                entryValues[index] = "" + i;
                if (entryValues[((index == 1) ? 0 : 1)] != null) break;
            }
        }
        preference.setEntryValues(entryValues);
    }

    private static boolean removePreference(PreferenceGroup group, String key) {
        for (int i = 0, n = group.size(); i < n; i++) {
            CameraPreference child = group.get(i);
            if (child instanceof PreferenceGroup) {
                if (removePreference((PreferenceGroup) child, key)) {
                    return true;
                }
            }
            if (child instanceof ListPreference &&
                    ((ListPreference) child).getKey().equals(key)) {
                group.removePreference(i);
                return true;
            }
        }
        return false;
    }

    private void filterUnsupportedOptions(PreferenceGroup group,
            ListPreference pref, List<String> supported) {

        // Remove the preference if the parameter is not supported or there is
        // only one options for the settings.
        if (supported == null || supported.size() <= 1) {
            removePreference(group, pref.getKey());
            return;
        }

        pref.filterUnsupported(supported);
        if (pref.getEntries().length <= 1) {
            removePreference(group, pref.getKey());
            return;
        }

        resetIfInvalid(pref);
    }

    private void resetIfInvalid(ListPreference pref) {
        // Set the value to the first entry if it is invalid.
        String value = pref.getValue();
        if (pref.findIndexOfValue(value) == NOT_FOUND) {
            pref.setValueIndex(0);
        }
    }

    private static List<String> sizeListToStringList(List<Size> sizes) {
        ArrayList<String> list = new ArrayList<String>();
        for (Size size : sizes) {
            list.add(String.format("%dx%d", size.width, size.height));
        }
        return list;
    }

    public static void upgradeLocalPreferences(SharedPreferences pref) {
        int version;
        try {
            version = pref.getInt(KEY_LOCAL_VERSION, 0);
        } catch (Exception ex) {
            version = 0;
        }
        if (version == CURRENT_LOCAL_VERSION) return;

        SharedPreferences.Editor editor = pref.edit();
        if (version == 1) {
            // We use numbers to represent the quality now. The quality definition is identical to
            // that of CamcorderProfile.java.
            editor.remove("pref_video_quality_key");
        }
        editor.putInt(KEY_LOCAL_VERSION, CURRENT_LOCAL_VERSION);
        editor.apply();
    }

    public static void upgradeGlobalPreferences(SharedPreferences pref) {
        upgradeOldVersion(pref);
        upgradeCameraId(pref);
    }

    private static void upgradeOldVersion(SharedPreferences pref) {
        int version;
        try {
            version = pref.getInt(KEY_VERSION, 0);
        } catch (Exception ex) {
            version = 0;
        }
        if (version == CURRENT_VERSION) return;

        SharedPreferences.Editor editor = pref.edit();
        if (version == 0) {
            // We won't use the preference which change in version 1.
            // So, just upgrade to version 1 directly
            version = 1;
        }
        if (version == 1) {
            // Change jpeg quality {65,75,85} to {normal,fine,superfine}
            String quality = pref.getString(KEY_JPEG_QUALITY, "85");
            if (quality.equals("65")) {
                quality = "normal";
            } else if (quality.equals("75")) {
                quality = "fine";
            } else {
                quality = "superfine";
            }
            editor.putString(KEY_JPEG_QUALITY, quality);
            version = 2;
        }
        if (version == 2) {
            editor.putString(KEY_RECORD_LOCATION,
                    pref.getBoolean(KEY_RECORD_LOCATION, false)
                    ? RecordLocationPreference.VALUE_ON
                    : RecordLocationPreference.VALUE_NONE);
            version = 3;
        }
        if (version == 3) {
            // Just use video quality to replace it and
            // ignore the current settings.
            editor.remove("pref_camera_videoquality_key");
            editor.remove("pref_camera_video_duration_key");
        }

        editor.putInt(KEY_VERSION, CURRENT_VERSION);
        editor.apply();
    }

    private static void upgradeCameraId(SharedPreferences pref) {
        // The id stored in the preference may be out of range if we are running
        // inside the emulator and a webcam is removed.
        // Note: This method accesses the global preferences directly, not the
        // combo preferences.
        int cameraId = readPreferredCameraId(pref);
        if (cameraId == 0) return;  // fast path

        int n = CameraHolder.instance().getNumberOfCameras();
        if (cameraId < 0 || cameraId >= n) {
            writePreferredCameraId(pref, 0);
        }
    }

    public static int readPreferredCameraId(SharedPreferences pref) {
        return Integer.parseInt(pref.getString(KEY_CAMERA_ID, "0"));
    }

    public static void writePreferredCameraId(SharedPreferences pref,
            int cameraId) {
        Editor editor = pref.edit();
        editor.putString(KEY_CAMERA_ID, Integer.toString(cameraId));
        editor.apply();
    }

    public static int readExposure(ComboPreferences preferences) {
        String exposure = preferences.getString(
                CameraSettings.KEY_EXPOSURE,
                EXPOSURE_DEFAULT_VALUE);
        try {
            return Integer.parseInt(exposure);
        } catch (Exception ex) {
            Log.e(TAG, "Invalid exposure: " + exposure);
        }
        return 0;
    }
    // Add Brightness by wk.m
    public static int readBrightness(ComboPreferences preferences) {
        String brightness = preferences.getString(
                CameraSettings.KEY_BRIGHTNESS,
                BRIGHTNESS_DEFAULT_VALUE);
        try {
            return Integer.parseInt(brightness);
        } catch (Exception ex) {
            Log.e(TAG, "Invalid brightness: " + brightness);
        }
        return 0;
    }
    // Add Saturation by wk.m
    public static int readSaturation(ComboPreferences preferences) {
        String saturation = preferences.getString(
                CameraSettings.KEY_SATURATION,
                SATURATION_DEFAULT_VALUE);
        try {
            return Integer.parseInt(saturation);
        } catch (Exception ex) {
            Log.e(TAG, "Invalid saturation: " + saturation);
        }
        return 0;
    }
    // Add Sharpness by wk.m
    public static int readSharpness(ComboPreferences preferences) {
        String sharpness = preferences.getString(
                CameraSettings.KEY_SHARPNESS,
                SHARPNESS_DEFAULT_VALUE);
        try {
            return Integer.parseInt(sharpness);
        } catch (Exception ex) {
            Log.e(TAG, "Invalid sharpness: " + sharpness);
        }
        return 0;
    }
    // Add Hue by wk.m
    public static int readHue(ComboPreferences preferences) {
        String hue = preferences.getString(
                CameraSettings.KEY_HUE,
                HUE_DEFAULT_VALUE);
        try {
            return Integer.parseInt(hue);
        } catch (Exception ex) {
            Log.e(TAG, "Invalid hue: " + hue);
        }
        return 0;
    }
    // Add WDR by wk.m
    public static int readWdr(ComboPreferences preferences) {
        String wdr = preferences.getString(
                CameraSettings.KEY_WDR,
                WDR_DEFAULT_VALUE);
        try {
            return Integer.parseInt(wdr);
        } catch (Exception ex) {
            Log.e(TAG, "Invalid wdr: " + wdr);
        }
        return 0;
    }
    // Add Jpeg Quality by wk.m
    public static int readJpegQuality(ComboPreferences preferences) {
        String jpeg_qual = preferences.getString(
                CameraSettings.KEY_JPEG_QUAL,
                JPEG_QUAL_DEFAULT_VALUE);
        try {
            return Integer.parseInt(jpeg_qual);
        } catch (Exception ex) {
            Log.e(TAG, "Invalid jpeg_qual: " + jpeg_qual);
        }
        return 0;
    }
    // Add AE Lock by hm choi
    public static boolean readAELock(ComboPreferences preferences) {
        String ae_lock = preferences.getString(
                CameraSettings.KEY_AE_LOCK,
                AE_LOCK_DEFAULT_VALUE);
        try {
            return ae_lock.equals("1");
        } catch (Exception ex) {
            Log.e(TAG, "Invalid ae_lock: " + ae_lock);
        }
        return false;
    }
    // Add AWB Lock by hm choi
    public static boolean readAWBLock(ComboPreferences preferences) {
        String awb_lock = preferences.getString(
                CameraSettings.KEY_AWB_LOCK,
                AWB_LOCK_DEFAULT_VALUE);
        try {
            return awb_lock.equals("1");
        } catch (Exception ex) {
            Log.e(TAG, "Invalid awb_lock: " + awb_lock);
        }
        return false;
    }

    public static int readEffectType(SharedPreferences pref) {
        String effectSelection = pref.getString(KEY_VIDEO_EFFECT, "none");
        if (effectSelection.equals("none")) {
            return EffectsRecorder.EFFECT_NONE;
        } else if (effectSelection.startsWith("goofy_face")) {
            return EffectsRecorder.EFFECT_GOOFY_FACE;
        } else if (effectSelection.startsWith("backdropper")) {
            return EffectsRecorder.EFFECT_BACKDROPPER;
        }
        Log.e(TAG, "Invalid effect selection: " + effectSelection);
        return EffectsRecorder.EFFECT_NONE;
    }

    public static Object readEffectParameter(SharedPreferences pref) {
        String effectSelection = pref.getString(KEY_VIDEO_EFFECT, "none");
        if (effectSelection.equals("none")) {
            return null;
        }
        int separatorIndex = effectSelection.indexOf('/');
        String effectParameter =
                effectSelection.substring(separatorIndex + 1);
        if (effectSelection.startsWith("goofy_face")) {
            if (effectParameter.equals("squeeze")) {
                return EffectsRecorder.EFFECT_GF_SQUEEZE;
            } else if (effectParameter.equals("big_eyes")) {
                return EffectsRecorder.EFFECT_GF_BIG_EYES;
            } else if (effectParameter.equals("big_mouth")) {
                return EffectsRecorder.EFFECT_GF_BIG_MOUTH;
            } else if (effectParameter.equals("small_mouth")) {
                return EffectsRecorder.EFFECT_GF_SMALL_MOUTH;
            } else if (effectParameter.equals("big_nose")) {
                return EffectsRecorder.EFFECT_GF_BIG_NOSE;
            } else if (effectParameter.equals("small_eyes")) {
                return EffectsRecorder.EFFECT_GF_SMALL_EYES;
            }
        } else if (effectSelection.startsWith("backdropper")) {
            // Parameter is a string that either encodes the URI to use,
            // or specifies 'gallery'.
            return effectParameter;
        }

        Log.e(TAG, "Invalid effect selection: " + effectSelection);
        return null;
    }


    public static void restorePreferences(Context context,
            ComboPreferences preferences, Parameters parameters) {
        int currentCameraId = readPreferredCameraId(preferences);

        // Clear the preferences of both cameras.
        int backCameraId = CameraHolder.instance().getBackCameraId();
        if (backCameraId != -1) {
            preferences.setLocalId(context, backCameraId);
            Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
        }
        int frontCameraId = CameraHolder.instance().getFrontCameraId();
        if (frontCameraId != -1) {
            preferences.setLocalId(context, frontCameraId);
            Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
        }

        // Switch back to the preferences of the current camera. Otherwise,
        // we may write the preference to wrong camera later.
        preferences.setLocalId(context, currentCameraId);

        upgradeGlobalPreferences(preferences.getGlobal());
        upgradeLocalPreferences(preferences.getLocal());

        // Write back the current camera id because parameters are related to
        // the camera. Otherwise, we may switch to the front camera but the
        // initial picture size is that of the back camera.
        initialCameraPictureSize(context, parameters);
        writePreferredCameraId(preferences, currentCameraId);
    }

    private ArrayList<String> getSupportedVideoQuality() {
        ArrayList<String> supported = new ArrayList<String>();
        // Check for supported quality
        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_1080P)) {
            supported.add(Integer.toString(CamcorderProfile.QUALITY_1080P));
        }
        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_720P)) {
            supported.add(Integer.toString(CamcorderProfile.QUALITY_720P));
        }
        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_480P)) {
            supported.add(Integer.toString(CamcorderProfile.QUALITY_480P));
        }

	 //jmq.add for qcif UI
        if (CamcorderProfile.hasProfile(mCameraId, CamcorderProfile.QUALITY_QCIF)) {
            supported.add(Integer.toString(CamcorderProfile.QUALITY_QCIF));
        }
        return supported;
    }

    private void initVideoEffect(PreferenceGroup group, ListPreference videoEffect) {
        CharSequence[] values = videoEffect.getEntryValues();

        boolean goofyFaceSupported =
                EffectsRecorder.isEffectSupported(EffectsRecorder.EFFECT_GOOFY_FACE);
        boolean backdropperSupported =
                EffectsRecorder.isEffectSupported(EffectsRecorder.EFFECT_BACKDROPPER) &&
                mParameters.isAutoExposureLockSupported() &&
                mParameters.isAutoWhiteBalanceLockSupported();

        ArrayList<String> supported = new ArrayList<String>();
        for (CharSequence value : values) {
            String effectSelection = value.toString();
            if (!goofyFaceSupported && effectSelection.startsWith("goofy_face")) continue;
            if (!backdropperSupported && effectSelection.startsWith("backdropper")) continue;
            supported.add(effectSelection);
        }

        filterUnsupportedOptions(group, videoEffect, supported);
    }
}
