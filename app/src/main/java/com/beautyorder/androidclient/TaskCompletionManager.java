package com.beautyorder.androidclient;

public interface TaskCompletionManager {
    // Callback function if the task is successful
    void onSuccess();

    // Callback function if the task is failing
    void onFailure();
}
