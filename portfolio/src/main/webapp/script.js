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
    openNewTab('https://www.youtube.com/embed/' + id);
}

/**
* Randomly clicks a link that has the given class name.
**/
function clickRandomLink(className) {
  var links = document.getElementsByClassName(className);
  var randomLink = links[Math.floor(Math.random() * links.length)];

  randomLink.click();
}

async function loadComments() {
  const commentsJson = await fetch('/data');
  const comments = await commentsJson.json();
  const commentSection = document.getElementById('home-comments');

  for (var i = 0; i < comments.length; ++i) {
    var commentElement = createListElement(comments[i]);
    commentSection.appendChild(commentElement);
  }
}

function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}
