import React from 'react';
import {shallow} from 'enzyme';

import RelativeAbsoluteSelection from './RelativeAbsoluteSelection';

it('should match snapshot', () => {
  const node = shallow(<RelativeAbsoluteSelection configuration={{}} />);

  expect(node).toMatchSnapshot();
});

it('should call the onChange method with the correct prop and value', () => {
  const spy = jest.fn();
  const node = shallow(<RelativeAbsoluteSelection configuration={{}} onChange={spy} />);

  node
    .find('Switch')
    .at(0)
    .simulate('change', {target: {checked: false}});

  expect(spy).toHaveBeenCalledWith('hideAbsoluteValue', true);
});
