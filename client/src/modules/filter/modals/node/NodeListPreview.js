/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import React from 'react';

import {t} from 'translation';

import PreviewItemValue from '../../PreviewItemValue';

export default function NodeListPreview({nodes, operator, type}) {
  const previewList = [];
  const createOperator = (name) => {
    return <span className="previewItemOperator"> {name} </span>;
  };

  nodes.forEach((selectedNode, idx) => {
    previewList.push(
      <li key={idx} className="previewItem">
        <span>
          {' '}
          <PreviewItemValue>{selectedNode.name || selectedNode.id}</PreviewItemValue>
          {idx < nodes.length - 1 &&
            createOperator(
              operator === 'not in'
                ? t('common.filter.list.operators.and')
                : t('common.filter.list.operators.or')
            )}
        </span>
      </li>
    );
  });

  const executed = type === 'executedFlowNodes' && operator !== 'not in';

  return (
    <>
      <ul className="previewList">{previewList}</ul>
      {createOperator(
        ((type === 'executingFlowNodes' || executed) && t('common.filter.list.operators.is')) ||
          (type !== 'canceledFlowNodes' && nodes.length > 1
            ? t('common.filter.list.operators.were')
            : t('common.filter.list.operators.was'))
      )}
      <span className="parameterName">
        {t('common.filter.nodeModal.' + (operator === 'not in' ? 'notExecutedFlowNodes' : type))}
      </span>
    </>
  );
}
