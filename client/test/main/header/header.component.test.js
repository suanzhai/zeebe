import {expect} from 'chai';
import {mountTemplate} from 'testHelpers/mountTemplate';
import {jsx} from 'view-utils';
import {Header} from 'main/header/header.component';

describe('<Header>', () => {
  let node;

  beforeEach(() => {
    ({node} = mountTemplate(<Header/>));
  });

  it('should contain footer text', () => {
    expect(node).to.contain.text('Camunda Optimize');
  });

  it('should have footer class', () => {
    expect(node.querySelector('header')).to.have.class('cam-brand-header');
  });
});
