import React from 'react';
import logo from './logo.svg';
import './App.css';
import {IdentitySerializer, JsonSerializer, RSocketClient} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {Flowable} from 'rsocket-flowable';

const address = {host: 'localhost', port: 7000};

function getClientTransport(host: string, port: number) {
    return new RSocketWebSocketClient({
        url: `ws://${host}:${port}`
    });
}

const maxRSocketRequestN = 2147483647;
let client = new RSocketClient({
    serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer
    },
    setup: {
        keepAlive: 60000,
        lifetime: 180000,
        dataMimeType: 'application/json',
        metadataMimeType: 'message/x.rsocket.routing.v0',
    },
    transport: getClientTransport(address.host, address.port),
});

client.connect().subscribe({
    onComplete: socket => {
        console.log('RSocket completed');

        // Request Stream
        socket
            .requestStream({
                data: 'data from front-end',
                metadata: String.fromCharCode('requestStream'.length) + 'requestStream',
            })
            .subscribe({
                onComplete: () => console.log('request stream complete'),
                onError: error => console.log(error),
                onNext: payload => console.log("onNext stream: %s", JSON.stringify(payload.data)),
                onSubscribe: subscription => subscription.request(maxRSocketRequestN),
            });

        // Request channel
        socket
            .requestChannel(Flowable.just({
                data: {
                    'author': 'carl',
                    'content': 'my tweet from the ui'
                },
                metadata: String.fromCharCode('channelOfTweet'.length) + 'channelOfTweet',
            }))
            .subscribe({
                onComplete: () => console.log('Request channel complete'),
                onError: error => console.log(error),
                onNext: payload => console.log("onNext channel: %s", JSON.stringify(payload.data)),
                onSubscribe: subscription => subscription.request(maxRSocketRequestN),
            });

        // Request response
        socket
            .requestResponse({
                data: {
                    'author': 'carl',
                    'content': 'add tweet'
                },
                metadata: String.fromCharCode('addTweet'.length) + 'addTweet',
            })
            .subscribe({
                onComplete: payload => console.log("Complete request response with %s", JSON.stringify(payload.data)),
                onError: error => console.log(error),
            });

        socket.connectionStatus().subscribe(status => {
            console.log('Connection status:', status);
        });
    },
    onError: error => {
        console.log(error);
    }
});

function App() {
    return (
        <div className="App">
            <header className="App-header">
                <img src={logo} className="App-logo" alt="logo"/>
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
