// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless min by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> busyMandatoryTimes = calculateBusyMandatoryTimes(events, request);
    ArrayList<TimeRange> busyOptionalTimes  = calculateBusyOptionalTimes(events, request);
    ArrayList<TimeRange> busyAllTimes       = new ArrayList<TimeRange>();

    busyAllTimes.addAll(busyMandatoryTimes);
    busyAllTimes.addAll(busyOptionalTimes);

    ArrayList<TimeRange> availableAllTimes  = calculateAvailableTimes(busyAllTimes, request);

    if (availableAllTimes.size() > 0) {
      return availableAllTimes; 
    }

    if (request.getAttendees().size() > 0) {
      return calculateAvailableTimes(busyMandatoryTimes, request);
    }

    return new ArrayList<TimeRange>();
  }

  /**
   * Calculates times where mandatory attendee of a new event is busy due to another existing meeting.
   **/
  private ArrayList<TimeRange> calculateBusyMandatoryTimes(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> busyTimes = new ArrayList<TimeRange>();

    for (Event event : events) {
      TimeRange eventTimeRange      = event.getWhen();
      Set<String> eventAttendees    = event.getAttendees();

      for (String attendee : request.getAttendees()) {
        if (eventAttendees.contains(attendee)) {
          busyTimes.add(eventTimeRange);
          break;
        }
      }
    }

    return busyTimes;
  }

  /**
   * Calculates times where optional attendee of a new event is busy due to another existing meeting.
   **/
  private ArrayList<TimeRange> calculateBusyOptionalTimes(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> busyTimes = new ArrayList<TimeRange>();

    for (Event event : events) {
      TimeRange eventTimeRange      = event.getWhen();
      Set<String> eventAttendees    = event.getAttendees();

      for (String attendee : request.getOptionalAttendees()) {
        if (eventAttendees.contains(attendee)) {
          busyTimes.add(eventTimeRange);
          break;
        }
      }
    }

    return busyTimes;
  }

  /**
   * Calculates the available times for a given new meeting request and a list 
   * of times in which at least one mandatory attendee is busy.
   **/
  private ArrayList<TimeRange> calculateAvailableTimes(ArrayList<TimeRange> busyTimes, MeetingRequest request) {
    ArrayList<TimeRange> availableTimes = new ArrayList<TimeRange>();
    int minDuration                     = (int) request.getDuration();

    Collections.sort(busyTimes, TimeRange.ORDER_BY_START);
    removeNestedEvents(busyTimes);

    if (busyTimes.size() == 0) {
      addIfPossible(availableTimes, TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, minDuration, true);
      return availableTimes;
    }

    addIfPossible(availableTimes, TimeRange.START_OF_DAY, busyTimes.get(0).start(), minDuration, false);

    for (int i = 0; i < busyTimes.size() - 1; ++i) {
      int availableStart    = busyTimes.get(i).end();
      int availableEnd      = busyTimes.get(i + 1).start();

      addIfPossible(availableTimes, availableStart, availableEnd, minDuration, false);
    }

    addIfPossible(availableTimes, busyTimes.get(busyTimes.size() - 1).end(), TimeRange.END_OF_DAY, minDuration, true);

    return availableTimes;
  }

  /**
   * Remove nested events from list of sorted events.
   **/
  private void removeNestedEvents(ArrayList<TimeRange> times) {
    Iterator<TimeRange> timesItr    = times.iterator();
    TimeRange prevTimeRange         = null;

    while (timesItr.hasNext()) {
      TimeRange currTimeRange = timesItr.next();

      if (prevTimeRange != null && prevTimeRange.contains(currTimeRange)) {
        timesItr.remove(); 
      } else {
        prevTimeRange = currTimeRange;
      }
    }
  }

  /**
   * Add an event to a list only when it fulfills the minimum criteria:
   * - The new event start time is before its end time.
   * - The new event is longer than the minimum duration required.
   **/
  private void addIfPossible(ArrayList<TimeRange> times, int startTime, int endTime, int minDuration, boolean isInclusive) {
    if (startTime >= endTime || endTime - startTime < minDuration) {
      return;
    }

    times.add(TimeRange.fromStartEnd(startTime, endTime, isInclusive));
  }
}
