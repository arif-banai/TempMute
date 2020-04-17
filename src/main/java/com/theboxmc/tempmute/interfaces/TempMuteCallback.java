package com.theboxmc.tempmute.interfaces;

public interface TempMuteCallback<T> {
    public void onSuccess(T result);
    public void onFailure(Throwable cause);
}
