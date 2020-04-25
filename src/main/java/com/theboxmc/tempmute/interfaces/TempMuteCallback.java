package com.theboxmc.tempmute.interfaces;

public interface TempMuteCallback<T> {
    void onSuccess(T result);
    void onFailure(Throwable cause);
}
