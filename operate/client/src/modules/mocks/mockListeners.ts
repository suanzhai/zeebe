/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

const mockListeners: ListenerEntity[] = [
  {
    listenerType: 'EXECUTION',
    listenerKey: 2344567895437401,
    state: 'ACTIVE',
    jobType: 'START',
    event: 'EVENT',
    time: '2024-01-02 10:51:16',
    sortValues: ['1'],
  },
  {
    listenerType: 'USER_TASK',
    listenerKey: 2344567895437402,
    state: 'ACTIVE',
    jobType: 'END',
    event: 'EVENT',
    time: '2024-01-02 11:11:12',
    sortValues: ['2'],
  },
  {
    listenerType: 'EXECUTION',
    listenerKey: 2344567895437403,
    state: 'ACTIVE',
    jobType: 'START',
    event: 'EVENT',
    time: '2024-01-02 12:51:16',
    sortValues: ['3'],
  },
  {
    listenerType: 'USER_TASK',
    listenerKey: 2344567895437404,
    state: 'ACTIVE',
    jobType: 'END',
    event: 'EVENT',
    time: '2024-01-02 13:11:12',
    sortValues: ['4'],
  },
  {
    listenerType: 'EXECUTION',
    listenerKey: 2344567895437405,
    state: 'ACTIVE',
    jobType: 'START',
    event: 'EVENT',
    time: '2024-01-02 14:51:16',
    sortValues: ['5'],
  },
  {
    listenerType: 'USER_TASK',
    listenerKey: 2344567895437406,
    state: 'ACTIVE',
    jobType: 'END',
    event: 'EVENT',
    time: '2024-01-02 15:11:12',
    sortValues: ['6'],
  },
  {
    listenerType: 'EXECUTION',
    listenerKey: 2344567895437407,
    state: 'FAILED',
    jobType: 'START',
    event: 'EVENT',
    time: '2024-01-02 16:51:16',
    sortValues: ['7'],
  },
  {
    listenerType: 'USER_TASK',
    listenerKey: 2344567895437408,
    state: 'ACTIVE',
    jobType: 'END',
    event: 'EVENT',
    time: '2024-01-02 17:11:12',
    sortValues: ['8'],
  },
  {
    listenerType: 'EXECUTION',
    listenerKey: 2344567895437409,
    state: 'ACTIVE',
    jobType: 'START',
    event: 'EVENT',
    time: '2024-01-02 18:51:16',
    sortValues: ['9'],
  },
  {
    listenerType: 'USER_TASK',
    listenerKey: 2344567895437410,
    state: 'ACTIVE',
    jobType: 'END',
    event: 'EVENT',
    time: '2024-01-02 19:11:12',
    sortValues: ['10'],
  },
  {
    listenerType: 'EXECUTION',
    listenerKey: 2344567895437411,
    state: 'ACTIVE',
    jobType: 'START',
    event: 'EVENT',
    time: '2024-01-02 19:51:16',
    sortValues: ['11'],
  },
  {
    listenerType: 'USER_TASK',
    listenerKey: 2344567895437412,
    state: 'ACTIVE',
    jobType: 'END',
    event: 'EVENT',
    time: '2024-01-02 20:11:12',
    sortValues: ['12'],
  },
  {
    listenerType: 'USER_TASK',
    listenerKey: 2344567895437413,
    state: 'ACTIVE',
    jobType: 'END',
    event: 'EVENT',
    time: '2024-01-02 21:11:12',
    sortValues: ['13'],
  },
  {
    listenerType: 'EXECUTION',
    listenerKey: 2344567895437414,
    state: 'ACTIVE',
    jobType: 'END',
    event: 'EVENT',
    time: '2024-01-02 22:11:12',
    sortValues: ['14'],
  },
  {
    listenerType: 'USER_TASK',
    listenerKey: 2344567895437415,
    state: 'ACTIVE',
    jobType: 'END',
    event: 'EVENT',
    time: '2024-01-02 23:11:12',
    sortValues: ['15'],
  },
  {
    listenerType: 'EXECUTION',
    listenerKey: 2344567895437416,
    state: 'ACTIVE',
    jobType: 'END',
    event: 'EVENT',
    time: '2024-01-02 23:21:12',
    sortValues: ['16'],
  },
  {
    listenerType: 'EXECUTION',
    listenerKey: 2344567895437417,
    state: 'ACTIVE',
    jobType: 'END',
    event: 'EVENT',
    time: '2024-01-02 23:23:12',
    sortValues: ['17'],
  },
  {
    listenerType: 'EXECUTION',
    listenerKey: 2344567895437418,
    state: 'ACTIVE',
    jobType: 'END',
    event: 'EVENT',
    time: '2024-01-02 23:25:12',
    sortValues: ['18'],
  },
];

export {mockListeners};
