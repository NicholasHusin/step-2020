const YOUTUBE_EMBED_URL = 'https://www.youtube.com/embed';

/**
 * Opens a YouTube video in a new tab given its id.
 * Uses /embed/ path so the video is opened in full screen.
 **/
function playVideo(id) {
  openNewTab(YOUTUBE_EMBED_URL + id);
}

/**
 * Opens url in new tab.
 **/
function openNewTab(url) {
  window.open(url);
}

/**
 * Randomly clicks a link that has the given class name.
 **/
function clickRandomLink(className) {
  const links       = document.getElementsByClassName(className);
  const randomLink  = links[Math.floor(Math.random() * links.length)];

  randomLink.click();
}
