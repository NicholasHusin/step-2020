package com.google.sps.servlets;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.utils.AuthCheck;


/**
 * Class that implements storing and retrieving of comment data.
 * This class overloads DataServlet methods.
 * Note that deletion is implement by the child of this class: DeleteCommentDataServlet.
 **/
@WebServlet(urlPatterns={"/comment-post", "/comment-get", "/comment-delete"})
public class CommentDataServlet extends DataServlet {
  private final int MIN_COMMENTS_NUMBER             = 5;
  private final String COMMENT_NUMBER_PARAMETER     = "comments-number";
  private final String USER_LDAP_PARAMETER          = "ldap";
  private static final String POST_COMMENT_URL      = "/comment-post";
  private static final String GET_COMMENT_URL       = "/comment-get";
  private static final String DELETE_COMMENT_URL    = "/comment-delete";
  protected final String ENTITY_KIND                = "Comment";
  private HashMap<String, String> prevCursorMap;

  /**
   * Initialize a server-side cache of previous cursors that are used in datastore pagination.
   * The cache is updated whenever we access a new page of comments and cleared when comments are deleted.
   **/
  public CommentDataServlet() {
    prevCursorMap   = new HashMap<String, String>();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!hasEnoughPrivilege()) {
      return;
    }

    String requestUrl = request.getRequestURI();

    if (requestUrl.equals(POST_COMMENT_URL)) {
      postComment(request, response);
    }

    if (requestUrl.equals(DELETE_COMMENT_URL)) {
      deleteComment(request, response);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!hasEnoughPrivilege()) {
      return;
    }

    String requestUrl = request.getRequestURI();

    if (requestUrl.equals(GET_COMMENT_URL)) {
      getComment(request, response);
    }
  }

  /**
   * Function to post a new comment.
   * Attaches LDAP parameter to the comment to keep track of usernames.
   **/
  private void postComment(HttpServletRequest request, HttpServletResponse response) throws IOException {
    HashMap<String,String> extraParameters = new HashMap<String, String>();
    extraParameters.put(USER_LDAP_PARAMETER, AuthCheck.getLdap());

    doPost(request, response, ENTITY_KIND, extraParameters);
  }

  /**
   * Function to delete all comments.
   * Clears prevCursorMap cache as it will no longer be valid after the comment deletion.
   **/
  private void deleteComment(HttpServletRequest request, HttpServletResponse response) throws IOException {
    deleteAll(ENTITY_KIND);
    prevCursorMap.clear();
  }

  /**
   * Function to get a new comment.
   * prevCursorMap cache will be updated when accessing a new page.
   **/
  private void getComment(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int commentsNumber  = parseIntParameter(request, COMMENT_NUMBER_PARAMETER);
    commentsNumber      = Math.max(commentsNumber, MIN_COMMENTS_NUMBER);

    doGet(request, response, ENTITY_KIND, ENTITY_TIMESTAMP_PARAMETER, commentsNumber, prevCursorMap);
  }

  /**
   * Utility class to check if users who sent requests have enough privilege
   * to store / view / delete comments.
   * This should be re used by all functions that implements store / view / delete
   * to make sure that privilege level are synced should it be changed in the future.
   **/
  protected boolean hasEnoughPrivilege() {
    return AuthCheck.isGoogleEmail();
  }
}


