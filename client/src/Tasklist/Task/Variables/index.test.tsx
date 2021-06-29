/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */

import * as React from 'react';
import {render, screen, waitFor} from '@testing-library/react';
import {mockGetCurrentUser} from 'modules/queries/get-current-user';
import {
  mockGetTaskVariables,
  mockGetTaskEmptyVariables,
  mockGetTaskVariablesTruncatedValues,
  mockGetFullVariableValue,
} from 'modules/queries/get-task-variables';
import {MockThemeProvider} from 'modules/theme/MockProvider';
import {Variables} from './index';
import {claimedTask, unclaimedTask} from 'modules/mock-schema/mocks/task';
import userEvent from '@testing-library/user-event';
import {ApolloProvider} from '@apollo/client';
import {client} from 'modules/apollo-client';
import {mockServer} from 'modules/mockServer';
import {graphql} from 'msw';

const Wrapper: React.FC = ({children}) => (
  <ApolloProvider client={client}>
    <MockThemeProvider>{children}</MockThemeProvider>
  </ApolloProvider>
);

describe('<Variables />', () => {
  beforeEach(() => {
    mockServer.use(
      graphql.query('GetCurrentUser', (_, res, ctx) => {
        return res.once(ctx.data(mockGetCurrentUser.result.data));
      }),
    );
  });

  it('should show existing variables for unassigned tasks', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    render(
      <Variables task={unclaimedTask()} onSubmit={() => Promise.resolve()} />,
      {
        wrapper: Wrapper,
      },
    );

    expect(await screen.findByTestId('variables-table')).toBeInTheDocument();
    expect(screen.getByText('myVar')).toBeInTheDocument();
    expect(screen.getByText('"0001"')).toBeInTheDocument();
    expect(screen.getByText('isCool')).toBeInTheDocument();
    expect(screen.getByText('"yes"')).toBeInTheDocument();
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
  });

  it('should show a message when the tasks has no variables', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskEmptyVariables().result.data));
      }),
    );

    render(
      <Variables task={claimedTask()} onSubmit={() => Promise.resolve()} />,
      {
        wrapper: Wrapper,
      },
    );

    expect(
      await screen.findByText('Task has no Variables'),
    ).toBeInTheDocument();
    expect(screen.queryByTestId('variables-table')).not.toBeInTheDocument();
  });

  it('should edit variable', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    render(
      <Variables task={claimedTask()} onSubmit={() => Promise.resolve()} />,
      {
        wrapper: Wrapper,
      },
    );
    const newVariableValue = '"changedValue"';

    expect(await screen.findByDisplayValue('"0001"')).toBeInTheDocument();

    userEvent.clear(screen.getByDisplayValue('"0001"'));
    userEvent.type(screen.getByLabelText('myVar'), newVariableValue);

    expect(screen.getByDisplayValue(newVariableValue)).toBeInTheDocument();
  });

  it('should add two variables and remove one', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    render(
      <Variables task={claimedTask()} onSubmit={() => Promise.resolve()} />,
      {
        wrapper: Wrapper,
      },
    );

    userEvent.click(
      await screen.findByRole('button', {
        name: /Add Variable/,
      }),
    );
    userEvent.click(
      screen.getByRole('button', {
        name: /Add Variable/,
      }),
    );

    expect(
      screen.getAllByRole('textbox', {
        name: /new variable/i,
      }),
    ).toHaveLength(4);

    expect(
      screen.getByRole('textbox', {
        name: 'New variable 0 name',
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByRole('textbox', {
        name: 'New variable 0 value',
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByRole('textbox', {
        name: 'New variable 1 name',
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByRole('textbox', {
        name: 'New variable 1 value',
      }),
    ).toBeInTheDocument();

    userEvent.click(
      screen.getByRole('button', {
        name: 'Remove new variable 1',
      }),
    );

    expect(
      screen.getAllByRole('textbox', {
        name: /new variable/i,
      }),
    ).toHaveLength(2);

    expect(
      screen.getByRole('textbox', {
        name: 'New variable 0 name',
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByRole('textbox', {
        name: 'New variable 0 value',
      }),
    ).toBeInTheDocument();

    expect(
      screen.queryByRole('textbox', {
        name: 'New variable 1 name',
      }),
    ).not.toBeInTheDocument();

    expect(
      screen.queryByRole('textbox', {
        name: 'New variable 1 value',
      }),
    ).not.toBeInTheDocument();
  });

  it('should add variable on task without variables', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    render(
      <Variables task={claimedTask()} onSubmit={() => Promise.resolve()} />,
      {
        wrapper: Wrapper,
      },
    );

    userEvent.click(
      await screen.findByRole('button', {
        name: /Add Variable/,
      }),
    );

    expect(
      screen.getByRole('textbox', {
        name: 'New variable 0 name',
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByRole('textbox', {
        name: 'New variable 0 value',
      }),
    ).toBeInTheDocument();
  });

  it('should validate an empty variable name', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    render(
      <Variables task={claimedTask()} onSubmit={() => Promise.resolve()} />,
      {
        wrapper: Wrapper,
      },
    );

    userEvent.click(
      await screen.findByRole('button', {
        name: /Add Variable/,
      }),
    );
    userEvent.type(
      screen.getByRole('textbox', {
        name: 'New variable 0 value',
      }),
      '"valid_value"',
    );

    expect(screen.getByTitle('Name has to be filled')).toBeInTheDocument();
  });

  it('should validate an empty variable value', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    render(
      <Variables task={claimedTask()} onSubmit={() => Promise.resolve()} />,
      {
        wrapper: Wrapper,
      },
    );

    userEvent.click(
      await screen.findByRole('button', {
        name: /Add Variable/,
      }),
    );
    userEvent.type(
      screen.getByRole('textbox', {
        name: 'New variable 0 name',
      }),
      'valid_name',
    );

    expect(screen.getByTitle('Value has to be JSON')).toBeInTheDocument();
  });

  it('should validate an invalid variable value', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    render(
      <Variables task={claimedTask()} onSubmit={() => Promise.resolve()} />,
      {
        wrapper: Wrapper,
      },
    );

    userEvent.click(
      await screen.findByRole('button', {
        name: /Add Variable/,
      }),
    );

    userEvent.type(
      screen.getByRole('textbox', {name: 'New variable 0 value'}),
      'invalid_value}}}',
    );

    expect(
      screen.getByTitle('Name has to be filled and Value has to be JSON'),
    ).toBeInTheDocument();
  });

  it('should not validate valid variables', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    render(
      <Variables task={claimedTask()} onSubmit={() => Promise.resolve()} />,
      {
        wrapper: Wrapper,
      },
    );

    userEvent.click(
      await screen.findByRole('button', {
        name: /Add Variable/,
      }),
    );

    userEvent.type(
      screen.getByRole('textbox', {
        name: 'New variable 0 name',
      }),
      'valid_name',
    );

    userEvent.type(
      screen.getByRole('textbox', {
        name: 'New variable 0 value',
      }),
      '"valid_value"',
    );

    expect(
      screen.queryByTitle('Name has to be filled and Value has to be JSON'),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByTitle('Name has to be filled'),
    ).not.toBeInTheDocument();
    expect(screen.queryByTitle('Value has to be JSON')).not.toBeInTheDocument();
  });

  it('should handle submission', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    const mockOnSubmit = jest.fn();
    const {rerender} = render(
      <Variables key="id_0" task={claimedTask()} onSubmit={mockOnSubmit} />,
      {
        wrapper: Wrapper,
      },
    );

    userEvent.click(
      await screen.findByRole('button', {
        name: /complete task/i,
      }),
    );

    await waitFor(() => expect(mockOnSubmit).toHaveBeenNthCalledWith(1, []));
    expect(mockOnSubmit).toHaveBeenCalledTimes(1);

    userEvent.click(
      screen.getByRole('button', {
        name: /add variable/i,
      }),
    );
    userEvent.type(
      screen.getByRole('textbox', {
        name: 'New variable 0 name',
      }),
      'var',
    );
    userEvent.type(
      screen.getByRole('textbox', {
        name: 'New variable 0 value',
      }),
      '1',
    );
    userEvent.click(
      screen.getByRole('button', {
        name: /complete task/i,
      }),
    );

    await waitFor(() =>
      expect(mockOnSubmit).toHaveBeenNthCalledWith(2, [
        {
          name: 'var',
          value: '1',
        },
      ]),
    );
    expect(mockOnSubmit).toHaveBeenCalledTimes(2);

    rerender(
      <Variables key="id_1" task={claimedTask()} onSubmit={mockOnSubmit} />,
    );

    expect(await screen.findByLabelText('myVar')).toBeInTheDocument();

    userEvent.click(
      screen.getByRole('button', {
        name: /add variable/i,
      }),
    );
    userEvent.type(
      screen.getByRole('textbox', {
        name: 'New variable 0 name',
      }),
      'name',
    );
    userEvent.type(
      screen.getByRole('textbox', {
        name: 'New variable 0 value',
      }),
      '"Jon"',
    );
    userEvent.click(
      screen.getByRole('button', {
        name: /complete task/i,
      }),
    );

    await waitFor(() =>
      expect(mockOnSubmit).toHaveBeenNthCalledWith(3, [
        {
          name: 'name',
          value: '"Jon"',
        },
      ]),
    );
    expect(mockOnSubmit).toHaveBeenCalledTimes(3);
  });

  it('should change variable and complete task', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    const mockOnSubmit = jest.fn();

    render(<Variables task={claimedTask()} onSubmit={mockOnSubmit} />, {
      wrapper: Wrapper,
    });

    userEvent.clear(await screen.findByLabelText('myVar'));
    userEvent.type(screen.getByLabelText('myVar'), '"newValue"');
    userEvent.click(
      screen.getByRole('button', {
        name: 'Complete Task',
      }),
    );

    await waitFor(() =>
      expect(mockOnSubmit).toHaveBeenCalledWith([
        {
          name: 'myVar',
          value: '"newValue"',
        },
      ]),
    );
  });

  it('should add new variable and complete task', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    const mockOnSubmit = jest.fn();

    render(<Variables task={claimedTask()} onSubmit={mockOnSubmit} />, {
      wrapper: Wrapper,
    });

    userEvent.click(await screen.findByText('Add Variable'));
    userEvent.type(
      screen.getByRole('textbox', {
        name: 'New variable 0 name',
      }),
      'newVariableName',
    );
    userEvent.type(
      screen.getByRole('textbox', {
        name: 'New variable 0 value',
      }),
      '"newVariableValue"',
    );
    userEvent.click(
      screen.getByRole('button', {
        name: 'Complete Task',
      }),
    );

    await waitFor(() =>
      expect(mockOnSubmit).toHaveBeenCalledWith([
        {
          name: 'newVariableName',
          value: '"newVariableValue"',
        },
      ]),
    );
  });

  it('should disable submit button on form errors for existing variables', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    render(
      <Variables task={claimedTask()} onSubmit={() => Promise.resolve()} />,
      {
        wrapper: Wrapper,
      },
    );

    userEvent.type(
      await screen.findByRole('textbox', {
        name: 'myVar',
      }),
      '{{ invalid value',
    );

    expect(screen.getAllByTestId(/^warning-icon/)).toHaveLength(1);
    expect(screen.getByTestId('warning-icon-myVar')).toBeInTheDocument();
    expect(screen.getByTitle('Value has to be JSON')).toBeInTheDocument();
    expect(
      screen.getByRole('button', {
        name: 'Complete Task',
      }),
    ).toBeDisabled();
  });

  it('should disable submit button on form errors for new variables', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    render(
      <Variables task={claimedTask()} onSubmit={() => Promise.resolve()} />,
      {
        wrapper: Wrapper,
      },
    );

    userEvent.click(
      await screen.findByRole('button', {
        name: /add variable/i,
      }),
    );
    userEvent.type(
      screen.getByRole('textbox', {
        name: 'New variable 0 value',
      }),
      '{{ invalid value',
    );

    expect(screen.getAllByTestId(/^warning-icon/)).toHaveLength(1);
    expect(
      screen.getByTestId('warning-icon-newVariables[0].value'),
    ).toBeInTheDocument();
    expect(
      screen.getByRole('button', {
        name: 'Complete Task',
      }),
    ).toBeDisabled();
  });

  it('should disable completion button', async () => {
    mockServer.use(
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(ctx.data(mockGetTaskVariables().result.data));
      }),
    );

    render(
      <Variables task={claimedTask()} onSubmit={() => Promise.resolve()} />,
      {
        wrapper: Wrapper,
      },
    );

    userEvent.click(
      await screen.findByRole('button', {
        name: /add variable/i,
      }),
    );

    expect(
      screen.getByRole('button', {
        name: /complete task/i,
      }),
    ).toBeDisabled();
  });

  it('should complete a task with a truncated variable', async () => {
    mockServer.use(
      graphql.query('GetCurrentUser', (_, res, ctx) => {
        return res.once(ctx.data(mockGetCurrentUser.result.data));
      }),
      graphql.query('GetTaskVariables', (_, res, ctx) => {
        return res.once(
          ctx.data(mockGetTaskVariablesTruncatedValues().result.data),
        );
      }),
      graphql.query('GetFullVariableValue', (_, res, ctx) => {
        return res.once(ctx.data(mockGetFullVariableValue().result.data));
      }),
    );
    const mockOnSubmit = jest.fn();
    render(<Variables task={claimedTask()} onSubmit={mockOnSubmit} />, {
      wrapper: Wrapper,
    });

    expect(await screen.findByDisplayValue('"000')).toBeInTheDocument();

    userEvent.click(screen.getByDisplayValue('"000'));

    expect(await screen.findByDisplayValue('"0001"')).toBeInTheDocument();

    userEvent.clear(screen.getByDisplayValue('"0001"'));
    userEvent.type(screen.getByLabelText('myVar'), '"newVariableValue"');
    userEvent.click(
      screen.getByRole('button', {
        name: /complete task/i,
      }),
    );

    await waitFor(() =>
      expect(mockOnSubmit).toHaveBeenCalledWith([
        {
          name: 'myVar',
          value: '"newVariableValue"',
        },
      ]),
    );
  });
});
