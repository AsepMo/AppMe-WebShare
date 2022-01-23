package com.appme.filepicker.thread.event;

import com.appme.filepicker.data.FileInfo;

public interface ScanListener {
    void onScanComplete(FileInfo[] fileInfos);
}
