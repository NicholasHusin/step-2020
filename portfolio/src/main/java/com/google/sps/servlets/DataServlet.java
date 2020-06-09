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

package com.google.sps.servlets;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Cursor;
import com.google.sps.utils.Parse;
import com.google.sps.utils.Language;


/**
 * This is a class intended to be inherited by other classes that wants to make use of Datastore.
 * This class provides general usage of Datastore that will be overloaded by classes that inherit it.
 **/
public class DataServlet extends HttpServlet {
  protected static final String REDIRECT_URL_HOME           = "/";
  protected static final String RESPONSE_CONTENT_TYPE_JSON  = "application/json; charset=UTF-8";
  protected static final String ENTITY_TIMESTAMP_PARAMETER  = "timestamp";
  protected static final String ENTITY_KIND_PARAMETER       = "kind";
  protected static final String PREV_CURSOR_PARAMETER       = "prev-cursor";
  protected static final String NEXT_CURSOR_PARAMETER       = "next-cursor";
  protected static final String CURSOR_PARAMETER            = "cursor";
  protected static final String QUERY_RESULT_PARAMETER      = "result";
  protected static final String LANGUAGE_CODE_PARAMETER     = "language";
  protected static final String UNDEFINED_STRING            = "undefined";
  private static DatastoreService datastore;

  public DataServlet() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Function that implements a general usage of storing entity in Datastore.
   * Intended to be overloaded by child of DataServlet class.
   **/
  protected void doPost(HttpServletRequest request, HttpServletResponse response, String entityKind, 
      HashMap<String, String> extraParameters) throws IOException {

    Entity newEntity = new Entity(entityKind);
    setTimestamp(newEntity);
    setRequestParameters(newEntity, request);
    setExtraParameters(newEntity, extraParameters);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(newEntity);
  }

  /**
   * Function that implements a general usage of retrieving entity in Datastore.
   * Intended to be overloaded by child of DataServlet class.
   * Takes in prevCursorMap that acts as a cursor cache to keep track of previous cursors.
   * Also takes in translationMap that specifies which property of the entities fetched should be translated
   * and to what languagei (leave as null if no translation is needed). 
   **/
  protected void doGet(HttpServletRequest request, HttpServletResponse response, String entityKind, 
      String sortKey, int entityLimit, HashMap<String, String> prevCursorMap, HashMap<String, String> translationMap) throws IOException {

    FetchOptions fetchOptions   = FetchOptions.Builder.withLimit(entityLimit);
    String currentCursor        = request.getParameter(CURSOR_PARAMETER);

    if (currentCursor != null && !currentCursor.equals(UNDEFINED_STRING)) {
      fetchOptions.startCursor(Cursor.fromWebSafeString(currentCursor));
    }

    Query query                 = new Query(entityKind).addSort(sortKey, SortDirection.DESCENDING);
    PreparedQuery preparedQuery = datastore.prepare(query);

    QueryResultList<Entity> entities    = preparedQuery.asQueryResultList(fetchOptions);
    String nextCursorString             = entities.getCursor().toWebSafeString();
    String prevCursorString             = prevCursorMap.get(currentCursor);

    if (translationMap != null) {
      translateQueryResult(entities, translationMap);
    }

    if (currentCursor != null && !currentCursor.equals(UNDEFINED_STRING)) {
      prevCursorMap.put(nextCursorString, currentCursor);
    }

    HashMap<String, Object> resultMap   = new HashMap<String, Object>();
    resultMap.put(PREV_CURSOR_PARAMETER, prevCursorString);
    resultMap.put(NEXT_CURSOR_PARAMETER, nextCursorString);
    resultMap.put(QUERY_RESULT_PARAMETER, entities);

    String json = Parse.toJson(resultMap);

    response.setContentType(RESPONSE_CONTENT_TYPE_JSON);
    response.getWriter().println(json);
  }

  /**
   * Utility function to delete all entity belonging to certain kind.
   **/
  protected void deleteAll(String entityKind) {
    Query query = new Query(entityKind);

    DatastoreService datastore  = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results       = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      datastore.delete(entity.getKey());
    }
  }

  /**
   * Utility function to parse parameters that are meant to be integer.
   * Returns -1 if the parameter value is invalid.
   **/
  protected int parseIntParameter(HttpServletRequest request, String parameterName) {
    String parameterValueString = request.getParameter(parameterName);

    int parameterValue;
    try {
      parameterValue = Integer.parseInt(parameterValueString);
    } catch (NumberFormatException e) {
      return -1;
    }

    return parameterValue;
  }

  /**
   * Utility function to set the timestamp of entity that are to be stored.
   **/
  private void setTimestamp(Entity newEntity) {
    long timestamp  = System.currentTimeMillis();
    newEntity.setProperty(ENTITY_TIMESTAMP_PARAMETER, timestamp);
  }

  /**
   * Utility function to set the parameters of entity that are to be stored.
   * Parameters added are the ones attached to the request.
   * Note that parameters value are joined with ",".
   * This is to account for input types such as checkboxes that has multiple value.
   **/
  private void setRequestParameters(Entity newEntity, HttpServletRequest request) {
    Enumeration<String> parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String parameterName          = parameterNames.nextElement();
      String[] parameterValues      = request.getParameterValues(parameterName);
      String joinedParameterValue   = String.join(",", parameterValues);
      newEntity.setProperty(parameterName, joinedParameterValue);
    }
  }

  /**
   * Utility function to set extra parameters of entity that are to be stored.
   * This is to account for cases when a child class that inherits DataServlet 
   * would want to attach extra parameters.
   **/
  private void setExtraParameters(Entity newEntity, HashMap<String, String> extraParameters) {
    if (extraParameters == null) {
      return;
    }

    for (Map.Entry<String, String> entry : extraParameters.entrySet()) {
      String extraParameter  = entry.getKey();
      String extraValue      = entry.getValue();
      newEntity.setProperty(extraParameter, extraValue);
    }
  }

  /**
   * Utility function to translate properties that are attached to entities.
   * Only properties that are listed in the translationMap as keys will be translated.
   * The language each property will be translated to depends on the value in the translationMap.
   **/
  private void translateQueryResult(QueryResultList<Entity> entities, HashMap<String, String> translationMap) {
    for (Entity entity : entities) {
      for (Map.Entry<String, String> entry : translationMap.entrySet()) {
        String property     = entry.getKey();
        String languageCode = entry.getValue();

        String originalPropertyValue    = (String) entity.getProperty(property);
        String translatedPropertyValue  = Language.translate(originalPropertyValue, languageCode);

        entity.setProperty(property, translatedPropertyValue);
      }
    }
  }
}
