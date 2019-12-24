import React from 'react';
import './App.css';
import {IdentitySerializer, JsonSerializer, RSocketClient} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {Flowable} from 'rsocket-flowable';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import FormControl from '@material-ui/core/FormControl';
import DialogTitle from '@material-ui/core/DialogTitle';

//const address = {host: '192.168.1.4', port: 7000};
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

let rSocket = undefined;

function doFireAndForget() {
  if (rSocket !== undefined) {
    rSocket
      .fireAndForget({
        data: '',
        metadata: String.fromCharCode('reset'.length) + 'reset',
      });
  } else {
    console.error("Rsocket not ready");
  }
}

function doRequestResponse() {
  if (rSocket !== undefined) {
    rSocket
      .requestResponse({
        data: {
          'author': document.getElementById("author").value,
          'content': document.getElementById("tweet").value,
        },
        metadata: String.fromCharCode('addTweet'.length) + 'addTweet',
      })
      .subscribe({
        onComplete: payload => {
          console.log("Complete request response with %s", JSON.stringify(payload.data));
          document.getElementById("requestResponse").innerHTML = JSON.stringify(payload.data);
          //document.getElementById("author").value = "";
          //document.getElementById("tweet").value = "";
        },
        onError: error => console.log(error),
      });
  } else {
    console.error("RSocket not ready");
  }
}

function doRequestStream() {
  if (rSocket !== undefined) {
    rSocket
      .requestStream({
        data: '',
        metadata: String.fromCharCode('streamOfTweet'.length) + 'streamOfTweet',
      })
      .subscribe({
        onNext: payload => {
          //console.log("onNext stream: %s", JSON.stringify(payload.data))
          let newDiv = document.createElement('div');
          newDiv.innerText = JSON.stringify(payload.data);
          document.getElementById("requestStream").appendChild(newDiv);
        },
        onComplete: () => {
          console.log('request stream complete');
          let newDiv = document.createElement('div');
          newDiv.innerText = "Done!";
          document.getElementById("requestStream").appendChild(newDiv);
        },
        onError: error => console.log(error),
        onSubscribe: subscription => subscription.request(maxRSocketRequestN),
      });
  } else {
    console.error("RSocket not ready");
  }
}

function doRequestChannel() {
  if (rSocket !== undefined) {
    rSocket
      .requestChannel(Flowable.just({
        data: {
          'author': 'carl',
          'content': 'my tweet from the ui'
        },
        metadata: String.fromCharCode('channelOfTweet'.length) + 'channelOfTweet',
      }))
      .subscribe({
        onNext: payload => {
          console.log("onNext channel: %s", JSON.stringify(payload.data));
          let container = document.createElement('div');
          payload.data.forEach(function (item, index) {
            console.log(item, index);
            let element = document.createElement('div');
            let shortId = item.id.substr(1, 4);
            element.innerText = `{id: ${shortId}..., author: ${item.author}, content: ${item.content}},`;
            container.appendChild(element);
          });

          let req = document.getElementById("requestChannel");
          if (req.childElementCount === 1) {
            req.removeChild(req.children[0]);
          }
          document.getElementById("requestChannel").appendChild(container);
        },
        onComplete: () => console.log('Request channel complete'),
        onError: error => console.log(error),
        onSubscribe: subscription => subscription.request(maxRSocketRequestN),
      });
  } else {
    console.error("RSocket not ready");
  }
}

function clearRequestStream() {
  let requestStreamDiv = document.getElementById("requestStream")
  while (requestStreamDiv.hasChildNodes()) {
    requestStreamDiv.removeChild(requestStreamDiv.lastChild);
  }
}

client.connect().subscribe({
  onComplete: socket => {
    rSocket = socket;
    console.log('RSocket completed');
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
      <table border="1" width="100%">
        <tbody>
        <tr valign="top">
          <td width="10%">
            <DialogTitle>Fire and forget</DialogTitle>
            <Button variant="contained" color="primary" onClick={doFireAndForget}>Clean DB</Button>
          </td>
          <td width="30%">
            <FormControl>
              <DialogTitle>Request/Response</DialogTitle>
              <TextField id="author" label="Author" type="text" name="name"/>
              <TextField label="Tweet" id="tweet" type="text" name="tweet"/>
              <Button variant="contained" color="primary" onClick={doRequestResponse}>Submit</Button>
            </FormControl>
          </td>
          <td width="20%">
            <DialogTitle>Request/Stream</DialogTitle>
            <Button variant="contained" color="primary" onClick={doRequestStream}>Submit</Button>
            <Button variant="contained" color="primary" onClick={clearRequestStream}>Clear</Button>
          </td>
          <td width="40%">
            <DialogTitle>Request/Channel</DialogTitle>
            <Button variant="contained" color="primary" onClick={doRequestChannel}>Submit</Button>
          </td>
        </tr>
        <tr valign="top">
          <td>
            <div id="fireAndForget"/>
          </td>
          <td>
            <div id="requestResponse"/>
          </td>
          <td>
            <div id="requestStream"/>
          </td>
          <td>
            <div id="requestChannel"/>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  );
}

export default App;
