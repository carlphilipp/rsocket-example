import React from 'react';
import logo from './logo.svg';
import './App.css';
import {IdentitySerializer, JsonSerializer, RSocketClient} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';

const address = {host: 'localhost', port: 7000};

function getClientTransport(host: string, port: number) {
    return new RSocketWebSocketClient({
        url: `ws://${host}:${port}`
    });
}

let client = undefined;

function main() {

    if (client !== undefined) {
        client.close();
    }

    client = new RSocketClient({
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

            socket
                .requestStream({
                    data: 'data from front-end',
                    metadata: String.fromCharCode('requestStream'.length) + 'requestStream',
                })
                .subscribe({
                    onComplete: () => console.log('end stream'),
                    onError: error => {
                        console.log(error);
                        //addErrorMessage("Connection has been closed due to ", error);
                    },
                    onNext: payload => {
                        console.log(payload.data);
                    },
                    onSubscribe: subscription => {
                        subscription.request(2147483647);
                    },
                });

            socket.connectionStatus().subscribe(status => {
                console.log('Connection status:', status);
            });
        },
        onError: error => {
            console.log(error);
        }
    });
}

document.addEventListener('DOMContentLoaded', main);

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
