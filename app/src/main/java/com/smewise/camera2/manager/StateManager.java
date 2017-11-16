package com.smewise.camera2.manager;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.util.Log;

/**
 * Created by wenzhe on 3/3/17.
 */

public class StateManager {
    private final String TAG = this.getClass().getSimpleName();

    public interface StateListener {
        void onCaptureCompleted(TotalCaptureResult result);

        void onSessionConfigured(CameraCaptureSession session);
    }

    private StateListener mListener;


    public StateManager(StateListener listener) {
        mListener = listener;
    }

    public CameraCaptureSession.CaptureCallback getControlCallback() {
        return controlCallback;
    }

    public CameraCaptureSession.CaptureCallback getCaptureCallback() {
        return captureCallback;
    }

    public CameraCaptureSession.StateCallback getSessionCallback() {
        return mSessionCallback;
    }

    private CameraCaptureSession.CaptureCallback controlCallback = new CameraCaptureSession
            .CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request,
                                     long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            Log.e(TAG, "onCaptureStarted");
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                        CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            Log.e(TAG, "onCaptureProgressed");
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.e(TAG, "onCaptureCompleted");
        }

        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId,
                                               long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            Log.e(TAG, "onCaptureSequenceCompleted");
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request,
                                    CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.e(TAG, "onCaptureFailed");
        }
    };

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession
            .CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            mListener.onCaptureCompleted(result);
            Log.e(TAG, "complete");
        }
    };

    private CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession
            .StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            mListener.onSessionConfigured(session);
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Log.e(TAG, "create session failed");
        }
    };
}
