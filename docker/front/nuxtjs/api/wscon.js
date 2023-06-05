// const WebSocket = require('ws')

export default {
  create(id) {
    let websocket = new WebSocket('ws://8.130.114.187:2333/chat/' + id);
    return websocket
  }


}
