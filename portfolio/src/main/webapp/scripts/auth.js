const AUTH_STATUS_URL           = '/auth-status';
const LOGIN_URL                 = '/auth-login';
const LOGOUT_URL                = '/auth-logout';
const GOOGLE_EMAIL_DOMAIN       = '@google.com';
const HIDDEN_ATTRIBUTE          = 'hidden';
const HREF_ATTRIBUTE            = 'href';
const LOGIN_LOGOUT_BUTTON_ID    = 'login-logout-button';
const LOGIN_MESSAGE             = 'Login';
const LOGOUT_MESSAGE            = 'Logout';
const GOOGLER_ELEMENT_IDS       = ['comment-controls', 'comment-nav'];
const NON_GOOGLER_ELEMENT_IDS   = ['comment-no-access-warning'];

/**
 * Functions to run when page first loads.
 **/
window.addEventListener('load', function() {
  loadLoginLogoutButton();
  adjustDisplayToStatus();
});

/**
 * Loads login / logout button according to the user status.
 * The button will redirect back to the page where the user click the button.
 * Clears previously loaded comments when called multiple times (done by setting innerHTML = '').
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

  button.removeAttribute(HIDDEN_ATTRIBUTE);
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
