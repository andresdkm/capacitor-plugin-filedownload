import type { PluginListenerHandle } from '@capacitor/core';

export interface FileDownloadPlugin {
  download(options: FileDownloadOptions): Promise<FileDownloadResponse>;
  addListener(eventName: 'downloadProgress', listenerFunc: (progress: FileDownloadProgress) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  addListener(eventName: 'downloadStatus', listenerFunc: (status: FileDownloadStatus) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
}
export interface FileDownloadOptions {
  uri: string;
  fileName: string;
  title: string;
  description: string;
  objectId?: string;
}
export interface FileDownloadResponse {
  path: string;
}
export interface FileDownloadProgress {
  progress: number;
  objectId: string;
}

export interface FileDownloadStatus{
  status: string;
  objectId: string;
}