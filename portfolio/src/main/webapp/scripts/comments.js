const GET_COMMENT_URL           = '/comment-get';
const POST_COMMENT_URL          = '/comment-post';
const DELETE_COMMENT_URL        = '/comment-delete';
const COMMENT_SECTION_ID        = 'comments-list';
const COMMENTS_NUMBER_ID        = 'comments-number';
const COMMENT_TEXT_ID           = 'comment-text';
const COMMENT_LDAP_ID           = 'ldap';
const RESPONSE_RESULT_ID        = 'result';
const RESPONSE_NEXT_CURSOR_ID   = 'next-cursor';
const RESPONSE_PREV_CURSOR_ID   = 'prev-cursor';
const COMMENT_SECTION_CHILD_TAG = 'li';
const NEXT_COMMENT_ID           = 'next-comment';
const PREV_COMMENT_ID           = 'prev-comment';
const CURR_COMMENT_ID           = 'curr-comment';
const COMMENT_LANGUAGE_ID       = 'comment-language';
const UNDEFINED_STRING          = 'undefined';

/**
 * Functions to run when page first loads.
 **/
window.addEventListener('load', function() {
  loadComments();
  addElementListeners();
});

/**
 * Functions to add listeners to elements in the page
 **/
function addElementListeners() {
  document.getElementById(COMMENTS_NUMBER_ID).addEventListener('change', function () {
    const currCursorString = document.getElementById(CURR_COMMENT_ID).value;
    loadComments(currCursorString);
  });

  document.getElementById(COMMENT_LANGUAGE_ID).addEventListener('change', function() {
    const currCursorString = document.getElementById(CURR_COMMENT_ID).value;
    loadComments(currCursorString);
  });
}

/**
 * Helper function to update the comment navigation buttons
 * Will hide buttons that are not usable (prev button on first page and next button on last page).
 * The current cursor value tracked in the hidden element CURR_COMMENT_ID will also be updated.
 **/
async function updateCommentNav(currCursorString, prevCursorString, nextCursorString, currentResponse) {
  const currCursor = document.getElementById(CURR_COMMENT_ID);
  const prevButton = document.getElementById(PREV_COMMENT_ID);
  const nextButton = document.getElementById(NEXT_COMMENT_ID);

  const prevPageResponse = await getCommentResponse(prevCursorString);
  const nextPageResponse = await getCommentResponse(nextCursorString);

  if (JSON.stringify(prevPageResponse) === JSON.stringify(currentResponse)) {
    prevButton.setAttribute(HIDDEN_ATTRIBUTE, true);
  } else {
    prevButton.removeAttribute(HIDDEN_ATTRIBUTE);
  }

  if (nextPageResponse[RESPONSE_RESULT_ID].length === 0) {
    nextButton.setAttribute(HIDDEN_ATTRIBUTE, true);
  } else {
    nextButton.removeAttribute(HIDDEN_ATTRIBUTE);
  }

  currCursor.value = currCursorString;
  prevButton.value = prevCursorString;
  nextButton.value = nextCursorString;
}

/**
 * load comments on homepage and updates the navigation button.
 * The number of comments loaded depends of commentsNumber input. Will always load 5 at minimum.
 * Clears previously loaded comments when called multiple times (done by setting innerHTML = '').
 * Load comments in the language requested. If no translation is requested, original comments are displayed.
 **/
async function loadComments(cursorString) {
  const responseObject      = await getCommentResponse(cursorString);

  const commentsObject      = responseObject[RESPONSE_RESULT_ID];
  const prevCursorString    = responseObject[RESPONSE_PREV_CURSOR_ID];
  const nextCursorString    = responseObject[RESPONSE_NEXT_CURSOR_ID];

  await updateCommentNav(cursorString, prevCursorString, nextCursorString, responseObject);

  let commentsLanguage      = document.getElementById(COMMENT_LANGUAGE_ID).value;
  if (commentsLanguage === UNDEFINED_STRING) {
    commentsLanguage        = COMMENT_TEXT_ID;
  }

  const commentSection      = document.getElementById(COMMENT_SECTION_ID);
  commentSection.innerHTML  = '';

  for (var i = 0; i < commentsObject.length; ++i) {
    let commentProperties   = commentsObject[i].propertyMap;
    let commentText         = commentProperties[commentsLanguage];
    let commentLdap         = commentProperties[COMMENT_LDAP_ID];
    let commentElement      = createCommentChild(commentLdap + ': ' + commentText);
    commentSection.prepend(commentElement);
  }
}

/**
 * Helper function to get response from GET_COMMENT_URL.
 * 'comments-number' and 'cursor' magic string is intentionally left as is.
 * This is because constants are taken literally when making objects.
 **/
async function getCommentResponse(cursorString) {
  const commentsNumber      = document.getElementById(COMMENTS_NUMBER_ID).value;

  const parameters      = {'comments-number': commentsNumber, 'cursor': cursorString};
  const fetchUrl        = constructFetchQueryUrl(GET_COMMENT_URL, parameters);

  const responseJson    = await fetch(fetchUrl);
  const responseObject  = await responseJson.json();

  return responseObject;
}

/**
 * Function used to post a new comment typed in COMMENT_TEXT_ID element.
 * Clears out the typed comment and reloads the comments section.
 * 'comments-number' magic string is intentionally left as is.
 * This is because constants are taken literally when making objects.
 **/
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
