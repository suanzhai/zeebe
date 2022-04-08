/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */

import React from 'react';

import DateFilterPreview from './DateFilterPreview';
import {shallow} from 'enzyme';

it('should create Today/Yesterday preview', () => {
  const filter = {
    type: 'relative',
    start: {
      value: 0,
      unit: 'days',
    },
  };

  const node = shallow(<DateFilterPreview filterType="instanceStartDate" filter={filter} />);

  expect(node).toMatchSnapshot();
});

it('should create correct last... with custom preview', () => {
  const filter = {
    type: 'rolling',
    start: {
      value: 5,
      unit: 'months',
    },
  };

  const node = shallow(<DateFilterPreview filterType="instanceEndDate" filter={filter} />);

  expect(node).toMatchSnapshot();
});

it('should create correct fixed date preview', () => {
  const filter = {
    type: 'fixed',
    start: '2015-01-20T00:00:00',
    end: '2019-05-11T23:59:59',
  };

  const node = shallow(<DateFilterPreview filterType="instanceStartDate" filter={filter} />);

  expect(node).toMatchSnapshot();
});

it('should include time information if fixed date filter contains time info', () => {
  const filter = {
    type: 'fixed',
    start: '2015-01-20T14:12:23',
    end: '2019-05-11T19:24:07',
  };

  const node = shallow(<DateFilterPreview filterType="instanceStartDate" filter={filter} />);

  expect(node).toMatchSnapshot();
});

it('should create variable preview', () => {
  const filter = {
    type: 'rolling',
    start: {
      value: 2,
      unit: 'days',
    },
    includeUndefined: true,
    excludeUndefined: false,
  };

  const node = shallow(<DateFilterPreview filterType="variable" filter={filter} />);

  expect(node).toMatchSnapshot();
});
