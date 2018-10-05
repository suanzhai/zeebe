import React from 'react';
import {mount} from 'enzyme';

import LabeledInput from './LabeledInput';

jest.mock('components', () => ({
  Labeled: props => (
    <div>
      <label id={props.id}>{props.label}</label>
      {props.children}
    </div>
  ),
  Input: props => <input {...props} />
}));

it('should create a label with the provided id', () => {
  const node = mount(<LabeledInput id="someId" />);

  expect(node.find('Labeled')).toHaveProp('id', 'someId');
});

it('should include the child content', () => {
  const node = mount(<LabeledInput>some child content</LabeledInput>);

  expect(node).toIncludeText('some child content');
});
