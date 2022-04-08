/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */

import React, {useEffect, useRef} from 'react';
import Viewer from 'dmn-js';
import {migrateDiagram} from '@bpmn-io/dmn-migrate';

import {withErrorHandling} from 'HOC';
import {showError} from 'notifications';

import 'dmn-js//dist/assets/dmn-js-shared.css';
import 'dmn-js/dist/assets/dmn-js-decision-table.css';
import './DMNDiagram.scss';

export function DMNDiagram({
  xml,
  decisionDefinitionKey,
  additionalModules = [],
  onLoad = () => {},
  mightFail,
  children,
}) {
  const container = useRef(null);
  const viewer = useRef(null);

  useEffect(() => {
    if (viewer.current) {
      viewer.current.destroy();
    }

    viewer.current = new Viewer({
      container: container.current,
      decisionTable: {additionalModules},
    });

    mightFail(
      migrateDiagram(xml),
      async (dmn13Xml) => {
        if (dmn13Xml) {
          await viewer.current.importXML(dmn13Xml);
          await viewer.current.open(
            viewer.current
              .getViews()
              .find(
                ({type, element: {id}}) => type === 'decisionTable' && id === decisionDefinitionKey
              )
          );
          onLoad();
        }
      },
      showError
    );
  }, [xml, decisionDefinitionKey, mightFail, additionalModules, onLoad]);

  return (
    <div ref={container} className="DMNDiagram">
      {children}
    </div>
  );
}

export default withErrorHandling(DMNDiagram);
