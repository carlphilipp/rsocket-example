import React from 'react';
import logo from './logo.svg';
import './App.css';
//import type {Payload, Responder} from 'rsocket-types';
import {RSocketClient} from 'rsocket-core';
//import {every, Flowable, Single} from 'rsocket-flowable';
import RSocketWebSocketClient from 'rsocket-websocket-client';

const address = {host: 'localhost', port: 7000};

function getClientTransport(host: string, port: number) {
    return new RSocketWebSocketClient({
        url: `ws://${host}:${port}`
    });
}

const client = new RSocketClient({
    setup: {
        keepAlive: 1000000,
        lifetime: 100000,
        dataMimeType: 'application/json',
        metadataMimeType: 'message/x.rsocket.routing.v0',
    },
    transport: getClientTransport(address.host, address.port),
});

client.connect().subscribe({
    onComplete: rSocket => {
        console.log('RSocket completed');
        /*        rSocket
                    .requestResponse({
                        metadata: String.fromCharCode('requestResponse'.length) + 'requestResponse',
                    })
                    .subscribe({
                        onComplete: response => {
                            const data = response.data;
                            if (data) {
                                console.log(`Request/response: ${data}`);
                            }
                        },
                        onError: error => {
                            console.log(error);
                            console.log(`Request/response error: ${error.message}`)
                        }
                    });*/
        rSocket
            .requestStream({
                data: 'DERP',
                metadata: String.fromCharCode('requestStream'.length) + 'requestStream',
            })
            .subscribe({
                onComplete: complete => {
                    console.log(complete);
                },
                onError: error => console.error(error),
            });

        rSocket.fireAndForget({
            data: 'some data to log on server',
            metadata: String.fromCharCode('fireAndForget'.length) + 'fireAndForget',
        });

        /*        rSocket
                    .requestStream({
                        data: 'DERP',
                        metadata: String.fromCharCode('requestStream'.length) + 'requestStream',
                    })
                    .subscribe({
                        onNext(payload) {
                            console.log("next", payload.data);
                        },
                        onError: error => {
                            console.log(error);
                            console.log(`Request/Stream error: ${error.message}`)
                        }
                    });*/

        //.subscribe();
        /*.subscribe({
            onError: error => {
                console.log(error);
                console.log(`Request/Stream error: ${error.message}`)
            }
        });*/


        rSocket.connectionStatus().subscribe(status => {
            console.log('Connection status:', status);
        });
    },
    onError: error => {
        console.log(error);
        //console.log(`RSocket error: ${error}`)
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
