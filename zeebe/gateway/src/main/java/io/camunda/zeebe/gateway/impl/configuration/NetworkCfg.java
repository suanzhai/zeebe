/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.gateway.impl.configuration;

import static io.camunda.zeebe.gateway.impl.configuration.ConfigurationDefaults.DEFAULT_GATEWAY_SO_RCVBUF;
import static io.camunda.zeebe.gateway.impl.configuration.ConfigurationDefaults.DEFAULT_GATEWAY_SO_SNDBUF;
import static io.camunda.zeebe.gateway.impl.configuration.ConfigurationDefaults.DEFAULT_PORT;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Objects;
import org.springframework.util.unit.DataSize;

public final class NetworkCfg {

  private String host;
  private int port = DEFAULT_PORT;
  private Duration minKeepAliveInterval = Duration.ofSeconds(30);
  private DataSize maxMessageSize = DataSize.ofMegabytes(4);
  private int so_sndbuf = DEFAULT_GATEWAY_SO_SNDBUF;
  private int so_rcvbuf = DEFAULT_GATEWAY_SO_RCVBUF;

  public void init(final String defaultHost) {
    if (host == null) {
      host = defaultHost;
    }
  }

  public String getHost() {
    return host;
  }

  public NetworkCfg setHost(final String host) {
    this.host = host;
    return this;
  }

  public int getPort() {
    return port;
  }

  public NetworkCfg setPort(final int port) {
    this.port = port;
    return this;
  }

  public Duration getMinKeepAliveInterval() {
    return minKeepAliveInterval;
  }

  public NetworkCfg setMinKeepAliveInterval(final Duration keepAlive) {
    minKeepAliveInterval = keepAlive;
    return this;
  }

  public DataSize getMaxMessageSize() {
    return maxMessageSize;
  }

  public NetworkCfg setMaxMessageSize(final DataSize maxMessageSize) {
    this.maxMessageSize = maxMessageSize;
    return this;
  }

  public int getSoSndbuf() {
    return so_sndbuf;
  }

  public NetworkCfg setSoSndbuf(final int soSndbuf) {
    so_sndbuf = soSndbuf;
    return this;
  }

  public int getSoRcvbuf() {
    return so_rcvbuf;
  }

  public NetworkCfg setSoRcvbuf(final int soRcvbuf) {
    so_rcvbuf = soRcvbuf;
    return this;
  }

  public InetSocketAddress toSocketAddress() {
    return new InetSocketAddress(host, port);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, port);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final NetworkCfg that = (NetworkCfg) o;
    return port == that.port && Objects.equals(host, that.host);
  }

  @Override
  public String toString() {
    return "NetworkCfg{"
        + "host='"
        + host
        + '\''
        + ", port="
        + port
        + ", minKeepAliveInterval="
        + minKeepAliveInterval
        + ", so_sndbuf="
        + so_sndbuf
        + ", so_rcvbuf="
        + so_rcvbuf
        + '}';
  }
}
