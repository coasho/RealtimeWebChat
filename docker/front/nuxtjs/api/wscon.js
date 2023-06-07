// const WebSocket = require('ws')

export default {
  create(id) {
    let websocket = new WebSocket('ws://127.0.0.1:2233/chat/' + id);
    return websocket
  }


}
