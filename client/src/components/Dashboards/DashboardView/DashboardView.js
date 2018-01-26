import React from 'react';

import {DashboardReport} from '../DashboardReport';
import {DashboardObject} from '../DashboardObject';

import './DashboardView.css';

const columns = 18;
const tileAspectRatio = 1;
const tileMargin = 8;

export default class DashboardView extends React.Component {
  render() {
    return (<div className='DashboardView' ref={node => this.container = node}>
      {this.state && this.props.reports.map((report, idx) =>
        <DashboardObject
          key={idx + '_' + report.id}
          tileDimensions={this.state.tileDimensions}
          {...report.position}
          {...report.dimensions}
        >
        <DashboardReport viewMode={this.props.viewMode} report={report} tileDimensions={this.state.tileDimensions} addons={this.props.reportAddons || []} />
      </DashboardObject>)}
      {this.state &&
        React.Children.map(this.props.children, child =>
          React.cloneElement(child, {
            container: this.container,
            tileDimensions: this.state.tileDimensions,
            reports: this.props.reports
          })
        )
      }
    </div>);
  }

  componentDidMount() {
    const availableWidth = this.container.clientWidth;
    const outerWidth = ~~(availableWidth / columns); // make sure we are working with round values

    const innerWidth = outerWidth - tileMargin;
    const innerHeight = innerWidth / tileAspectRatio;

    const outerHeight = innerHeight + tileMargin;

    const tileDimensions = {outerWidth, innerWidth, outerHeight, innerHeight, columns};

    this.setState({
      tileDimensions
    });
  }
}
