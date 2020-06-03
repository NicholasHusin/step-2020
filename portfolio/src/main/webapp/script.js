// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

const YOUTUBE_EMBED_URL         = 'https://www.youtube.com/embed';
const COMMENT_FETCH_URL         = '/comment-data';
const COMMENT_SECTION_ID        = 'home-comments';
const COMMENT_TEXT_ID           = 'comment-text';
const COMMENT_SECTION_CHILD_TAG = 'li';

/**
* Opens url in new tab.
**/
function openNewTab(url) {
    window.open(url);
}

/**
* Opens a YouTube video in a new tab given its id.
* Uses /embed/ path so the video is opened in full screen.
**/
function playVideo(id) {
    openNewTab(YOUTUBE_EMBED_URL + id);
}

/**
* Randomly clicks a link that has the given class name.
**/
function clickRandomLink(className) {
  const links       = document.getElementsByClassName(className);
  const randomLink  = links[Math.floor(Math.random() * links.length)];

  randomLink.click();
}

/**
* load comments on homepage.
* The number of comments loaded depends of commentsNumber input.
* Clears previously loaded comments when called multiple times (done by setting innerHTML = "").
**/
async function loadComments(commentsNumber) {
  // 'comments-number' magic string is intentionally left as is.
  // This is because constants are taken literally when making objects.
  // Ex: {COMMENTS_NUMBER_ID: commentsNumber} will not become {'comments-number': commentsNumber}
  // even if const COMMENTS_NUMBER_ID = 'comments-number' is declared.
  const parameters      = {'comments-number': commentsNumber};
  const fetchUrl        = constructFetchQueryUrl(COMMENT_FETCH_URL, parameters);

  const commentsJson    = await fetch(fetchUrl);
  const commentsObject  = await commentsJson.json();

  const commentSection      = document.getElementById(COMMENT_SECTION_ID);
  commentSection.innerHTML  = "";

  for (var i = 0; i < commentsObject.length; ++i) {
    let commentProperties   = commentsObject[i].propertyMap;
    let commentText         = commentProperties[COMMENT_TEXT_ID];
    let commentElement      = createCommentChild(commentText);
    commentSection.prepend(commentElement);
  }
}

function createCommentChild(text) {
  const childElement       = document.createElement(COMMENT_SECTION_CHILD_TAG);
  childElement.innerText   = text;
  return childElement;
}

function constructFetchQueryUrl(url, parameters) {
  const query = Object.keys(parameters)
    .map(k => k + '=' + parameters[k])
    .join('&');

  return url + '?' + query;
}
