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
const GET_COMMENT_URL           = '/comment-get';
const POST_COMMENT_URL          = '/comment-post';
const DELETE_COMMENT_URL        = '/comment-delete';
const COMMENT_SECTION_ID        = 'comments-list';
const COMMENTS_NUMBER_ID        = 'comments-number';
const COMMENT_TEXT_ID           = 'comment-text';
const COMMENT_LDAP_ID           = 'ldap';
const COMMENT_SECTION_CHILD_TAG = 'li';
const AUTH_STATUS_URL           = '/auth-status';
const LOGIN_URL                 = '/auth-login';
const LOGOUT_URL                = '/auth-logout';
const GOOGLE_EMAIL_DOMAIN       = '@google.com';
const HIDDEN_ATTRIBUTE          = 'hidden';
const HREF_ATTRIBUTE            = 'href';
const LOGIN_LOGOUT_BUTTON_ID    = 'login-logout-button';
const LOGIN_MESSAGE             = 'Login';
const LOGOUT_MESSAGE            = 'Logout';
const GOOGLER_ELEMENT_IDS       = ['comment-controls'];
const NON_GOOGLER_ELEMENT_IDS   = ['comment-no-access-warning'];

/**
 * Functions to run when page first loads.
 **/
window.onload = function() {
  adjustDisplayToStatus();
  loadLoginLogoutButton();
  loadComments();
};


/**
 * Loads login / logout button according to the user status.
 * The button will redirect back to the page where the user click the button.
 * Clears previously loaded comments when called multiple times (done by setting innerHTML = "").
 * 'redirect' magic string is intentionally left as is.
 * This is because constants are taken literally when making objects.
 **/
async function loadLoginLogoutButton() {
  const button      = document.getElementById(LOGIN_LOGOUT_BUTTON_ID);
  const parameters  = {'redirect': window.location.pathname};

  if (await userIsLoggedIn()) {
    const logoutFetchUrl    = constructFetchQueryUrl(LOGOUT_URL, parameters);
    const logoutUrlJson     = await fetch(logoutFetchUrl);
    const logoutUrl         = await logoutUrlJson.json();
    button.setAttribute(HREF_ATTRIBUTE, logoutUrl);
    button.innerText        = LOGOUT_MESSAGE;
  } else {
    const loginFetchUrl     = constructFetchQueryUrl(LOGIN_URL, parameters);
    const loginUrlJson      = await fetch(loginFetchUrl);
    const loginUrl          = await loginUrlJson.json();
    button.setAttribute(HREF_ATTRIBUTE, loginUrl);
    button.innerText        = LOGIN_MESSAGE;
  }
}

/**
 * Adjust the elements that the user will see according to their status.
 * To add more elements, simply include it in the GOOGLER_ELEMENT_IDS or NON_GOOGLER_ELEMENT_IDS.
 **/
async function adjustDisplayToStatus() {
  if (await userIsGoogler()) {
    for (id of GOOGLER_ELEMENT_IDS) {
      let element = document.getElementById(id);
      element.removeAttribute(HIDDEN_ATTRIBUTE);
    }
  } else {
    for (id of NON_GOOGLER_ELEMENT_IDS) {
      let element = document.getElementById(id);
      element.removeAttribute(HIDDEN_ATTRIBUTE);
    }
  }
}

async function userIsGoogler() {
  return (await userIsLoggedIn()) && (await getUserEmail()).includes(GOOGLE_EMAIL_DOMAIN);
}

async function userIsLoggedIn() {
  return await getUserEmail() !== null;
}

/**
 * Gets user email. Returns null if not logged in. 
 **/
async function getUserEmail() {
  const emailJson = await fetch(AUTH_STATUS_URL);
  const email = await emailJson.json();
  return email;
}

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
 * The number of comments loaded depends of commentsNumber input. Will always load 5 at minimum.
 * Clears previously loaded comments when called multiple times (done by setting innerHTML = "").
 **/
async function loadComments() {
  // 'comments-number' magic string is intentionally left as is.
  // This is because constants are taken literally when making objects.
  // Ex: {COMMENTS_NUMBER_ID: commentsNumber} will not become {'comments-number': commentsNumber}
  // even if const COMMENTS_NUMBER_ID = 'comments-number' is declared.
  const commentsNumber  = document.getElementById(COMMENTS_NUMBER_ID).value;
  const parameters      = {'comments-number': commentsNumber};
  const fetchUrl        = constructFetchQueryUrl(GET_COMMENT_URL, parameters);

  const commentsJson    = await fetch(fetchUrl);
  const commentsObject  = await commentsJson.json();

  const commentSection      = document.getElementById(COMMENT_SECTION_ID);
  commentSection.innerHTML  = "";

  for (var i = 0; i < commentsObject.length; ++i) {
    let commentProperties   = commentsObject[i].propertyMap;
    let commentText         = commentProperties[COMMENT_TEXT_ID];
    let commentLdap         = commentProperties[COMMENT_LDAP_ID];
    let commentElement      = createCommentChild(commentLdap + ': ' + commentText);
    commentSection.prepend(commentElement);
  }
}

async function postComment() {
  const commentText = document.getElementById(COMMENT_TEXT_ID).value;
  const parameters  = {'comment-text': commentText};
  document.getElementById(COMMENT_TEXT_ID).value = '';

  const fetchUrl = constructFetchQueryUrl(POST_COMMENT_URL, parameters);
  await fetchPost(fetchUrl);
  
  loadComments();
}

async function deleteComment() {
  await fetchPost(DELETE_COMMENT_URL);
  loadComments();
}

async function fetchPost(url) {
  return await fetch(url, {method: 'POST'});
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
