package com.appme.story.engine.app.listeners;

import com.appme.story.service.server.WebChatServer;

public interface OnServiceBoundListener {
    void onServiceBound(WebChatServer server);
}
