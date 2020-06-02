package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;


@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {
  private final String REDIRECT_URL_HOME    = "/";
  private final String COMMENT_ENTITY_KIND  = "Comment";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(COMMENT_ENTITY_KIND);

    DatastoreService datastore  = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results       = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      datastore.delete(entity.getKey());
    }

    response.sendRedirect(REDIRECT_URL_HOME);
  }
}

