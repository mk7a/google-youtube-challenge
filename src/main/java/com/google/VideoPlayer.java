package com.google;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VideoPlayer {

  private final VideoLibrary videoLibrary;

  private String playingVideoId;
  private final Random random = new Random();
  private boolean videoIsPlaying = false;

  private final Map<String, VideoPlaylist> playlistCollection = new LinkedHashMap<>();
  private final Map<String, Optional<String>> flaggedVideos = new HashMap<>();

  public VideoPlayer() {
    this.videoLibrary = new VideoLibrary();
  }

  private String getPlayingVideoTitle() {
    return videoLibrary.getVideo(playingVideoId).getTitle();
  }

  private String formatVideoDetails(Video v) {
    String details = String.format("%s (%s) [%s]", v.getTitle(), v.getVideoId(), String.join(" ", v.getTags()));
    if (flaggedVideos.containsKey(v.getVideoId())) {
      details += String.format(" - FLAGGED (reason: %s)", flaggedVideos.get(v.getVideoId()).orElse("Not supplied"));
    }
    return details;
  }

  private List<Video> getNonFlaggedVideos() {
    return videoLibrary.getVideos().stream()
            .filter(video -> !flaggedVideos.containsKey(video.getVideoId()))
            .collect(Collectors.toList());
  }

  private String getFlagReason(String videoId) {
    return flaggedVideos.get(videoId).orElse("Not supplied");
  }

  public void numberOfVideos() {
    System.out.printf("%s videos in the library%n", videoLibrary.getVideos().size());
  }

  public void showAllVideos() {
    var video_list = videoLibrary.getVideos().stream()
            .sorted(Comparator.comparing(Video::getTitle))
            .map(this::formatVideoDetails)
            .collect(Collectors.joining("\n"));
    System.out.println("Here's a list of all available videos:\n" + video_list);
  }

  public void playVideo(String videoId) {
    if (videoLibrary.getVideo(videoId) == null) {
      System.out.println("Cannot play video: Video does not exist");
    } else if (flaggedVideos.containsKey(videoId)) {
      System.out.printf("Cannot play video: Video is currently flagged (reason: %s)%n", getFlagReason(videoId));
    } else {
      if (playingVideoId != null) {
        stopVideo();
      }
      videoIsPlaying = true;
      playingVideoId = videoId;
      System.out.println("Playing video: " + getPlayingVideoTitle());
    }
  }

  public void stopVideo() {
    if (playingVideoId == null) {
      System.out.println("Cannot stop video: No video is currently playing");
    } else {
      videoIsPlaying = false;
      System.out.println("Stopping video: " + getPlayingVideoTitle());
      playingVideoId = null;
    }
  }

  public void playRandomVideo() {
    var videos = getNonFlaggedVideos();
    int numOfVideos = videos.size();
    if (numOfVideos == 0) {
      System.out.println("No videos available");
    } else {
      String randomVideoId = videos.get(random.nextInt(numOfVideos)).getVideoId();
      playVideo(randomVideoId);
    }
  }

  public void pauseVideo() {
    if (videoIsPlaying) {
      videoIsPlaying = false;
      System.out.println("Pausing video: " + getPlayingVideoTitle());
    } else {
      if (playingVideoId == null) {
        System.out.println("Cannot pause video: No video is currently playing");
      } else {
        System.out.println("Video already paused: " + getPlayingVideoTitle());
      }
    }
  }

  public void continueVideo() {
    if (playingVideoId == null) {
      System.out.println("Cannot continue video: No video is currently playing");
    } else {
      if (videoIsPlaying) {
        System.out.println("Cannot continue video: Video is not paused");
      } else {
        System.out.println("Continuing video: " + getPlayingVideoTitle());
      }
    }
  }

  public void showPlaying() {
    if (playingVideoId != null) {
      String videoDetails = formatVideoDetails(videoLibrary.getVideo(playingVideoId));
      if (!videoIsPlaying) {
        videoDetails += " - PAUSED";
      }
      System.out.println("Currently playing: " + videoDetails);
    } else {
      System.out.println("No video is currently playing");
    }

  }

  public void createPlaylist(String playlistName) {
    String playlistId = playlistName.toLowerCase();
    if (playlistCollection.containsKey(playlistId)) {
      System.out.println("Cannot create playlist: A playlist with the same name already exists");
    } else {
      playlistCollection.put(playlistId, new VideoPlaylist(playlistName));
      System.out.println("Successfully created new playlist: " + playlistName);
    }
  }

  public void addVideoToPlaylist(String playlistName, String videoId) {
    String playlistId = playlistName.toLowerCase();
    var playlist = playlistCollection.get(playlistId);
    if (playlist == null) {
      System.out.printf("Cannot add video to %s: Playlist does not exist%n", playlistName);
    } else if (flaggedVideos.containsKey(videoId)) {
      System.out.printf("Cannot add video to %s: Video is currently flagged (reason: %s)%n",
              playlistName, getFlagReason(videoId));
    } else if (videoLibrary.getVideo(videoId) == null) {
        System.out.printf("Cannot add video to %s: Video does not exist%n", playlistName);
    } else if (playlist.containsVideo(videoId)) {
        System.out.printf("Cannot add video to %s: Video already added%n", playlistName);
    } else {
      playlist.addVideo(videoId);
      System.out.printf("Added video to %s: Amazing Cats%n", playlistName);
    }

  }

  public void showAllPlaylists() {
    if (playlistCollection.isEmpty()) {
      System.out.println("No playlists exist yet");
    } else {
      System.out.println("Showing all playlists:");
      var playlists = new ArrayList<>(playlistCollection.keySet());
      Collections.reverse(playlists);
      playlists.forEach(System.out::println);
    }
  }

  public void showPlaylist(String playlistName) {
    String playlistId = playlistName.toLowerCase();
    if (!playlistCollection.containsKey(playlistId)) {
      System.out.printf("Cannot show playlist %s: Playlist does not exist%n", playlistName);
    } else {
      System.out.println("Showing playlist: " + playlistName);
      var playlist = playlistCollection.get(playlistId);
      if (playlist.isEmpty()) {
        System.out.println("   No videos here yet");
      } else {
        playlist.getPlaylistVideos().stream()
                .map(videoLibrary::getVideo)
                .forEach(v -> System.out.println("   " + formatVideoDetails(v)));
      }
    }
  }

  public void removeFromPlaylist(String playlistName, String videoId) {
    String playlistId = playlistName.toLowerCase();
    if (!playlistCollection.containsKey(playlistId)) {
      System.out.printf("Cannot remove video from %s: Playlist does not exist%n", playlistName);
    } else if (videoLibrary.getVideo(videoId) == null) {
      System.out.printf("Cannot remove video from %s: Video does not exist%n", playlistName);
    } else {
      var playlist = playlistCollection.get(playlistId);
      if (!playlist.containsVideo(videoId)) {
        System.out.printf("Cannot remove video from %s: Video is not in playlist%n", playlistName);
      } else {
        playlist.removeVideo(videoId);
        System.out.printf("Removed video from %s: %s%n", playlistName, videoLibrary.getVideo(videoId).getTitle());
      }
    }
  }

  public void clearPlaylist(String playlistName) {
    String playlistId = playlistName.toLowerCase();
    if (!playlistCollection.containsKey(playlistId)) {
      System.out.printf("Cannot clear playlist %s: Playlist does not exist%n", playlistName);
    } else {
      var playlist = playlistCollection.get(playlistId);
      playlist.clearPlaylist();
      System.out.println("Successfully removed all videos from " + playlistName);
    }
  }

  public void deletePlaylist(String playlistName) {
    String playlistId = playlistName.toLowerCase();
    if (!playlistCollection.containsKey(playlistId)) {
      System.out.printf("Cannot delete playlist %s: Playlist does not exist%n", playlistName);
    } else {
      playlistCollection.remove(playlistId);
      System.out.println("Deleted playlist: " + playlistName);
    }
  }

  private void search(String searchTerm, Function<Video, String> searchFilter) {
    List<Video> results = new ArrayList<>();
    getNonFlaggedVideos().forEach(video -> {
      if (searchFilter.apply(video).toLowerCase().contains(searchTerm.toLowerCase())) {
        results.add(video);
      }
    });
    if (results.size() == 0) {
      System.out.println("No search results for " + searchTerm);
      return;
    }
    results.sort(Comparator.comparing(Video::getTitle));
    System.out.printf("Here are the results for %s:%n", searchTerm);
    for (int n = 0; n < results.size(); n++) {
      System.out.printf("  %d) %s%n", n + 1, formatVideoDetails(results.get(n)));
    }
    System.out.println("Would you like to play any of the above? If yes, specify the number of the video." +
            "\nIf your answer is not a valid number, we will assume it's a no.");
    var scanner = new Scanner(System.in);
    try {
      String line = scanner.nextLine();
      int selection = Integer.parseInt(line);
      selection -= 1;
      if (selection >= 0 && selection < results.size()) {
        playVideo(results.get(selection).getVideoId());
      }
    } catch (Exception e) {
      // "Do nothing"
    }
  }

  public void searchVideos(String searchTerm) {
    search(searchTerm, Video::getTitle);
  }


  public void searchVideosWithTag(String videoTag) {
    search(videoTag, v -> String.join(" ", v.getTags()));
  }

  public void flagVideo(String videoId) {
    flagVideo(videoId, "");
  }

  public void flagVideo(String videoId, String reason) {
    if (videoLibrary.getVideo(videoId) == null) {
      System.out.println("Cannot flag video: Video does not exist");
    } else if (flaggedVideos.containsKey(videoId)) {
      System.out.println("Cannot flag video: Video is already flagged");
    } else {
      flaggedVideos.put(videoId, reason.equals("") ? Optional.empty() : Optional.of(reason));
      if (playingVideoId != null && playingVideoId.equals(videoId)) {
        stopVideo();
      }
      System.out.printf("Successfully flagged video: %s (reason: %s)%n", videoLibrary.getVideo(videoId).getTitle(),
              reason.equals("") ? "Not supplied" : reason);
    }

  }

  public void allowVideo(String videoId) {
    if (videoLibrary.getVideo(videoId) == null) {
      System.out.println("Cannot remove flag from video: Video does not exist");
    } else if (!flaggedVideos.containsKey(videoId)) {
      System.out.println("Cannot remove flag from video: Video is not flagged");
    } else {
      flaggedVideos.remove(videoId);
      System.out.println("Successfully removed flag from video: " + videoLibrary.getVideo(videoId).getTitle());
    }
  }


}