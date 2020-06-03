package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class that implements deletion of comment data.
 * Inherits from CommentDataServlet to be able to see its ENTITY_KIND.
 **/
@WebServlet("/delete-comment-data")
public class DeleteCommentDataServlet extends CommentDataServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    deleteAll(ENTITY_KIND);
    response.sendRedirect(REDIRECT_URL_HOME);
  }
}

