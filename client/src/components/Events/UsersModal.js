/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */

import React from 'react';

import {Modal, Button, UserTypeahead, Labeled} from 'components';
import {showError} from 'notifications';
import {withErrorHandling} from 'HOC';
import {t} from 'translation';

import {getUsers, updateUsers} from './service';

import './UsersModal.scss';

export class UsersModal extends React.Component {
  state = {
    loading: false,
    users: null,
    deleting: null,
  };

  componentDidMount() {
    this.props.mightFail(getUsers(this.props.id), (users) => this.setState({users}), showError);
  }

  onConfirm = () => {
    this.setState({loading: true});
    this.props.mightFail(
      updateUsers(this.props.id, this.state.users),
      () => {
        this.setState({loading: false});
        this.props.onClose(this.state.users);
      },
      (error) => {
        showError(error);
        this.setState({loading: false});
      }
    );
  };

  close = () => this.props.onClose();

  render() {
    const {id} = this.props;
    const {loading, users} = this.state;
    const isValid = users && users.length > 0;

    return (
      <Modal open={id} onClose={this.close} onConfirm={this.onConfirm} className="UsersModal">
        <Modal.Header>{t('common.editAccess')}</Modal.Header>
        <Modal.Content>
          <p className="description">{t('events.permissions.description')}</p>
          <Labeled className="userTypeahead" label={t('home.userTitle')}>
            {users && <UserTypeahead users={users} onChange={(users) => this.setState({users})} />}
          </Labeled>
        </Modal.Content>
        <Modal.Actions>
          <Button main disabled={loading} onClick={this.close}>
            {t('common.cancel')}
          </Button>
          <Button main disabled={loading || !isValid} primary onClick={this.onConfirm}>
            {t('common.save')}
          </Button>
        </Modal.Actions>
      </Modal>
    );
  }
}

export default withErrorHandling(UsersModal);
