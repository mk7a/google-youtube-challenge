package com.google;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A class used to represent a Playlist */
class VideoPlaylist {

    private final String playlistName;
    private final List<String> playlistVideos = new ArrayList<>();

    public VideoPlaylist(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public List<String> getPlaylistVideos() {
        return playlistVideos;
    }

    public void addVideo(String videoId) {
        playlistVideos.add(videoId);
    }

    public void removeVideo(String videoId) {
        playlistVideos.remove(videoId);
    }

    public boolean containsVideo(String videoId) {
        return playlistVideos.contains(videoId);
    }

    public boolean isEmpty() {
        return playlistVideos.isEmpty();
    }

    public void clearPlaylist() {
        playlistVideos.clear();
    }
}
