import React from 'react';
import logo from './logo.svg';
import './App.css';
import type {Responder, Payload} from 'rsocket-types';
import {Leases, Lease, RSocketClient} from 'rsocket-core';
import {Flowable, Single, every} from 'rsocket-flowable';
import RSocketWebSocketClient from 'rsocket-websocket-client';

const address = {host: 'localhost', port: 7000};

function make(data: string, route: string): Payload<string, string> {
  return {
    data,
    metadata: String.fromCharCode(route.length) + route,
  };
}

function logRequest(type: string, payload: Payload<string, string>) {
  console.log(`Responder response to ${type}, data: ${payload.data || 'null'}`);
}

class EchoResponder implements Responder<string, string> {
  metadataPush(payload: Payload<string, string>): Single<void> {
    return Single.error(new Error('not implemented'));
  }

  fireAndForget(payload: Payload<string, string>): void {
    logRequest('fire-and-forget', payload);
  }

  requestResponse(payload: Payload<string, string>): Single<Payload<string, string>> {
    logRequest('request-response', payload);
    return Single.of(make('client response', 'requestResponse'));
  }

  requestStream(payload: Payload<string, string>): Flowable<Payload<string, string>> {
    logRequest('request-stream', payload);
    return Flowable.just(make('client stream response', 'requestStream'));
  }

  requestChannel(payloads: Flowable<Payload<string, string>>): Flowable<Payload<string, string>> {
    return Flowable.just(make('client channel response', 'requestChannel'));
  }
}

function getClientTransport(host: string, port: number) {
  return new RSocketWebSocketClient({
    url : `ws://${host}:${port}`
  });
}

const receivedLeasesLogger: (Flowable<Lease>) => void = lease =>
    lease.subscribe({
      onSubscribe: s => s.request(Number.MAX_SAFE_INTEGER),
      onNext: lease =>
          console.log(
              `Received lease - ttl: ${lease.timeToLiveMillis}, requests: ${lease.allowedRequests}`,
          ),
    });

function periodicLeaseSender(
    intervalMillis: number,
    ttl: number,
    allowedRequests: number,
): Flowable<Lease> {
  return every(intervalMillis).map(v => {
    console.log(`Sent lease - ttl: ${ttl}, requests: ${allowedRequests}`);
    return new Lease(ttl, allowedRequests);
  });
}

const client = new RSocketClient({
  setup: {
    keepAlive: 1000000,
    lifetime: 100000,
    dataMimeType: 'application/json',
    metadataMimeType: 'message/x.rsocket.routing.v0',
  },
  responder: new EchoResponder(),
/*  leases: () =>
      new Leases()
          .receiver(receivedLeasesLogger)
          .sender(stats => periodicLeaseSender(10000, 7000, 10)),*/
  transport: getClientTransport(address.host, address.port),
});

client.connect().subscribe({
  onComplete: rSocket => {
    every(1000).subscribe({
      onNext: time => {
        console.log(`Requester availability: ${rSocket.availability()}`);
        rSocket
            .requestResponse({
              //data: time.toString(),
              metadata: String.fromCharCode('requestResponse'.length) + 'requestResponse',
            })
            .subscribe({
              onComplete: response => {
                const data = response.data;
                if (data) {
                  console.log(`Requester response: ${data}`);
                }
              },
              onError: error => {
                console.log(error)
                console.log(`Requester error: ${error.message}`)
              }
            });
      },
      onSubscribe: subscription => subscription.request(Number.MAX_SAFE_INTEGER),
    });
    console.log('RSocket completed');

    rSocket.connectionStatus().subscribe(status => {
      console.log('Connection status:', status);
    });
  },
  onError: error => {
    console.log(error);
    //console.log(`RSocket error: ${error}`)
  }
});

setTimeout(() => {}, 360000);

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.js</code> and save to reload.
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
      </header>
    </div>
  );
}

export default App;
