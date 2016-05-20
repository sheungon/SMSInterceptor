package com.sheungon.smsinterceptor;

import android.os.Bundle;
import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sheungon.smsinterceptor.service.SMSInterceptorSettings;
import com.sheungon.smsinterceptor.util.FileUtil;
import com.sheungon.smsinterceptor.util.UIUtil;

import java.io.File;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.Unbinder;

/**
 * @author John
 */
public class MainFragment extends Fragment {

    private static final ButterKnife.Setter<View, Boolean> BK_ENABLE = new ButterKnife.Setter<View, Boolean>() {
        @Override public void set(@NonNull View view, Boolean enable, int index) {
            if (!enable) {
                view.clearFocus();
            }
            view.setEnabled(enable);
        }
    };

    private static final String REGEX_HTTP_PROTOCOL = "^[Hh][Tt][Tt][Pp][Ss]?://.+";
    public static final String SLASH = "/";
    public static final String REGEX_START_SLASH = "^/+";


    @BindView(R.id.base_url) EditText mBaseUrl;
    @BindView(R.id.server_api) EditText mServerAPI;
    @BindView(R.id.btn_toggle_service) ToggleButton mServiceBtn;
    @BindView(R.id.log) TextView mLogView;

    @BindViews({R.id.base_url, R.id.server_api})
    EditText[] mInputViews;

    private boolean mIgnoreToggle = false;
    private boolean mNeedUpdateLogViewOnResume = false;
    private Unbinder mUnbinder;
    private FileObserver mLogObserver;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUnbinder = ButterKnife.bind(this, view);

        mBaseUrl.setText(SMSInterceptorSettings.getServerBaseUrl());
        mServerAPI.setText(SMSInterceptorSettings.getServerApi());

        // Get and show service state
        boolean serviceRunning = SMSReceiver.isReceiverEnabled();
        mIgnoreToggle = true;
        mServiceBtn.setChecked(serviceRunning);
        mIgnoreToggle = false;
        ButterKnife.apply(mInputViews, BK_ENABLE, !serviceRunning);

        File logFile = FileUtil.getLogFile();
        if (logFile != null) {
            mLogObserver = new MyFileObserver(this,
                    logFile.getPath(),
                    FileObserver.MODIFY | FileObserver.DELETE | FileObserver.CLOSE_WRITE);
            mLogObserver.startWatching();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mNeedUpdateLogViewOnResume) {
            updateLogView();
            mNeedUpdateLogViewOnResume = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mLogObserver != null) {
            mLogObserver.stopWatching();
            mLogObserver = null;
        }

        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
    }

    @SuppressWarnings("unused")
    @OnCheckedChanged(R.id.btn_toggle_service)
    void onToggleService(@NonNull ToggleButton toggleButton, boolean isChecked) {

        if (mIgnoreToggle) {
            return;
        }

        if (isChecked) {
            String baseUrl = null;
            String serverApi = null;

            // Validate input
            FragmentActivity activity = getActivity();
            if (activity != null) {
                UIUtil.hideSoftKeyboard(activity);
            }

            boolean inputInvalid = false;

            // Empty check
            String errorMsg = null;
            for (EditText inputView : mInputViews) {
                String input = inputView.getText().toString();
                if (input.isEmpty()) {
                    if (errorMsg == null) {
                        errorMsg = getString(R.string.error_input);
                    }
                    inputView.setError(errorMsg);

                    if (!inputInvalid) {
                        // Focus to the first missing input
                        inputView.requestFocus();
                        inputInvalid = true;
                    }
                }
            }

            // Validate input
            if (!inputInvalid) {
                baseUrl = mBaseUrl.getText().toString();
                if (!baseUrl.matches(REGEX_HTTP_PROTOCOL)) {
                    mBaseUrl.setError(getString(R.string.error_base_url_invalid_format));
                    mBaseUrl.requestFocus();
                    inputInvalid = true;
                }
                if (!baseUrl.endsWith(SLASH)) {
                    baseUrl += SLASH;
                    mBaseUrl.setText(baseUrl);
                }
            }
            serverApi = mServerAPI.getText().toString();
            serverApi = serverApi.replaceAll(REGEX_START_SLASH, "");
            mServerAPI.setText(serverApi);

            if (inputInvalid) {
                toggleButton.setChecked(false);
                return;
            }

            // Save the setting
            SMSInterceptorSettings.setServerApi(serverApi);
            SMSInterceptorSettings.setServerBaseUrl(baseUrl);
        }

        // Update view state
        ButterKnife.apply(mInputViews, BK_ENABLE, !isChecked);

        // Update Receiver
        SMSReceiver.enableReceiver(isChecked);
    }

    boolean isViewReleased() {
        return mUnbinder == null;
    }

    @UiThread
    private void updateLogView() {

        if (isViewReleased()) {
            return;
        }

        if (!isResumed()) {
            // Suspend the view update to resume
            mNeedUpdateLogViewOnResume = true;
            return;
        }

        File logFile = FileUtil.getLogFile();
        if (logFile == null) {
            mLogView.setText(R.string.error_read_log);
            return;
        }

        StringBuilder logContentBuilder = new StringBuilder();
        FileUtil.readTextFile(logFile, logContentBuilder);
        mLogView.setText(logContentBuilder);
    }


    ///////////////////////////
    // Class and interface
    ///////////////////////////
    private static class MyFileObserver extends FileObserver {

        private final WeakReference<MainFragment> mFragmentRef;
        private final Runnable mUpdateLogTask = new Runnable() {
            @Override
            public void run() {
                MainFragment fragment = mFragmentRef.get();
                if (fragment == null) {
                    return;
                }
                fragment.updateLogView();
            }
        };

        public MyFileObserver(@NonNull MainFragment fragment,
                              @NonNull String path,
                              int mask) {
            super(path, mask);

            mFragmentRef = new WeakReference<>(fragment);
        }

        @Override
        public void onEvent(int event, String path) {

            MainFragment fragment = mFragmentRef.get();
            FragmentActivity activity = fragment == null ? null : fragment.getActivity();
            if (activity == null) {
                return;
            }

            switch (event) {
                case FileObserver.MODIFY:
                case FileObserver.CLOSE_WRITE:
                    activity.runOnUiThread(mUpdateLogTask);
                    break;

                case FileObserver.DELETE:
                    break;
            }
        }
    }
}
