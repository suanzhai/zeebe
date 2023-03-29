/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */

import {render, screen, waitFor} from 'modules/testing-library';
import {Link, MemoryRouter} from 'react-router-dom';
import {rest} from 'msw';
import {Login} from './index';
import {authenticationStore} from 'modules/stores/authentication';
import {MockThemeProvider} from 'modules/theme/MockProvider';
import {nodeMockServer} from 'modules/mockServer/nodeMockServer';
import {LocationLog} from 'modules/utils/LocationLog';

const getFullYearMock = jest.spyOn(Date.prototype, 'getFullYear');
function createWrapper(
  initialEntries: React.ComponentProps<
    typeof MemoryRouter
  >['initialEntries'] = ['/login'],
) {
  const Wrapper: React.FC<{
    children?: React.ReactNode;
  }> = ({children}) => (
    <MockThemeProvider>
      <MemoryRouter initialEntries={initialEntries}>
        {children}
        <Link
          to="/login"
          state={{
            referrer: {
              pathname: '/1',
              search: '?filter=unassigned',
            },
          }}
        >
          emulate redirection
        </Link>
        <LocationLog />
      </MemoryRouter>
    </MockThemeProvider>
  );
  return Wrapper;
}

describe('<Login />', () => {
  beforeAll(() => {
    global.IS_REACT_ACT_ENVIRONMENT = false;
  });

  beforeEach(() => {
    authenticationStore.disableSession();
  });

  afterEach(() => {
    getFullYearMock.mockClear();
    authenticationStore.reset();
  });

  afterAll(() => {
    getFullYearMock.mockRestore();
    global.IS_REACT_ACT_ENVIRONMENT = true;
  });

  it('should redirect to the initial page on success', async () => {
    nodeMockServer.use(
      rest.post('/api/login', (_, res, ctx) => res.once(ctx.text(''))),
    );

    const {user} = render(<Login />, {
      wrapper: createWrapper(),
    });

    await user.type(screen.getByLabelText(/username/i), 'demo');
    await user.type(screen.getByLabelText('Password'), 'demo');
    await user.click(screen.getByRole('button', {name: 'Login'}));

    expect(
      screen.getByRole('button', {name: 'Logging in'}),
    ).toBeInTheDocument();
    expect(
      await screen.findByRole('button', {name: 'Login'}),
    ).toBeInTheDocument();
    expect(screen.getByTestId('pathname')).toHaveTextContent(/^\/$/i);
  });

  it('should redirect to the referrer page', async () => {
    nodeMockServer.use(
      rest.post('/api/login', (_, res, ctx) => res.once(ctx.text(''))),
    );
    const {user} = render(<Login />, {
      wrapper: createWrapper(),
    });

    await user.click(screen.getByRole('link', {name: /emulate redirection/i}));

    await user.type(screen.getByLabelText(/username/i), 'demo');
    await user.type(screen.getByLabelText('Password'), 'demo');
    await user.click(screen.getByRole('button', {name: 'Login'}));

    await waitFor(() =>
      expect(screen.getByTestId('pathname')).toHaveTextContent('/1'),
    );
    expect(screen.getByTestId('search')).toHaveTextContent('filter=unassigned');
  });

  it('should show an error for wrong credentials', async () => {
    nodeMockServer.use(
      rest.post('/api/login', (_, res, ctx) =>
        res.once(ctx.status(401), ctx.text('')),
      ),
    );
    const {user} = render(<Login />, {
      wrapper: createWrapper(),
    });

    await user.type(screen.getByLabelText(/username/i), 'demo');
    await user.type(screen.getByLabelText('Password'), 'demo');
    await user.click(screen.getByRole('button', {name: 'Login'}));

    expect(
      await screen.findByText('Username and password do not match'),
    ).toBeInTheDocument();
  });

  it('should show a generic error message', async () => {
    nodeMockServer.use(
      rest.post('/api/login', (_, res, ctx) =>
        res.once(ctx.status(404), ctx.text('')),
      ),
    );
    const {user} = render(<Login />, {
      wrapper: createWrapper(),
    });

    await user.type(screen.getByLabelText(/username/i), 'demo');
    await user.type(screen.getByLabelText('Password'), 'demo');
    await user.click(screen.getByRole('button', {name: 'Login'}));

    expect(
      await screen.findByText('Credentials could not be verified'),
    ).toBeInTheDocument();

    nodeMockServer.use(
      rest.post('/api/login', (_, res) => res.networkError('A network error')),
    );

    await user.type(screen.getByLabelText(/username/i), 'demo');
    await user.type(screen.getByLabelText('Password'), 'demo');
    await user.click(screen.getByRole('button', {name: 'Login'}));

    expect(
      await screen.findByText('Credentials could not be verified'),
    ).toBeInTheDocument();
  });

  it('should show a loading state while the login form is submitting', async () => {
    nodeMockServer.use(
      rest.post('/api/login', (_, res, ctx) => res.once(ctx.text(''))),
    );

    const {user} = render(<Login />, {
      wrapper: createWrapper(),
    });

    await user.type(screen.getByLabelText(/username/i), 'demo');
    await user.type(screen.getByLabelText('Password'), 'demo');
    await user.click(screen.getByRole('button', {name: 'Login'}));

    expect(
      screen.getByRole('button', {
        name: 'Logging in',
      }),
    ).toBeDisabled();
  });

  it('should have the correct copyright notice', () => {
    const mockYear = 1984;
    getFullYearMock.mockReturnValue(mockYear);
    render(<Login />, {
      wrapper: createWrapper(),
    });

    expect(
      screen.getByText(
        `© Camunda Services GmbH ${mockYear}. All rights reserved. | 1.2.3`,
      ),
    ).toBeInTheDocument();
  });

  it('should not allow the form to be submitted with empty fields', async () => {
    nodeMockServer.use(
      rest.post('/api/login', (_, res, ctx) => res.once(ctx.text(''))),
    );

    const {user} = render(<Login />, {
      wrapper: createWrapper(),
    });

    await user.click(screen.getByRole('button', {name: /login/i}));

    expect(screen.getByLabelText(/username/i)).toHaveAccessibleDescription(
      /username is required/i,
    );
    expect(screen.getByLabelText(/username/i)).toBeInvalid();
    expect(screen.getByLabelText(/password/i)).toHaveAccessibleDescription(
      /password is required/i,
    );
    expect(screen.getByLabelText(/password/i)).toBeInvalid();
    expect(screen.getByTestId('pathname')).toHaveTextContent('/login');

    await user.type(screen.getByLabelText(/username/i), 'demo');
    await user.click(screen.getByRole('button', {name: /login/i}));

    expect(screen.getByLabelText(/password/i)).not.toHaveAccessibleDescription(
      /username is required/i,
    );
    expect(screen.getByLabelText(/username/i)).toBeValid();
    expect(screen.getByLabelText(/password/i)).toHaveAccessibleDescription(
      /password is required/i,
    );
    expect(screen.getByLabelText(/password/i)).toBeInvalid();
    expect(screen.getByTestId('pathname')).toHaveTextContent('/login');

    await user.clear(screen.getByLabelText(/username/i));
    await user.type(screen.getByLabelText(/password/i), 'demo');
    await user.click(screen.getByRole('button', {name: /login/i}));

    expect(screen.getByLabelText(/password/i)).not.toHaveAccessibleDescription(
      /password is required/i,
    );
    expect(screen.getByLabelText(/password/i)).toBeValid();
    expect(screen.getByLabelText(/username/i)).toHaveAccessibleDescription(
      /username is required/i,
    );
    expect(screen.getByLabelText(/username/i)).toBeInvalid();
    expect(screen.getByTestId('pathname')).toHaveTextContent('/login');

    await user.type(screen.getByLabelText(/username/i), 'demo');
    await user.click(screen.getByRole('button', {name: /login/i}));

    expect(screen.getByTestId('pathname')).toHaveTextContent('/login');
    await waitFor(() =>
      expect(screen.getByTestId('pathname')).toHaveTextContent(/^\/$/i),
    );
  });
});
