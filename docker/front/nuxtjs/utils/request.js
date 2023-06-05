import axios from "axios";
import cookie from "js-cookie";
import {Message} from "element-ui"
const service = axios.create({
  baseURL: "http://8.130.114.187:2233",
  timeout: 20000
})
service.interceptors.request.use(config => {
  let token = cookie.get('token');
  if(token!=undefined){
    config.headers['token']=token;
  }
  return config;
}, err => {
  return Promise.reject(err);
})
service.interceptors.response.use(res => {
  if (res.data.code == 25001) {
    Message({
      message: res.data.message || 'error',
      type: 'warning',
      duration: 3 * 1000
    })
    return
  }
  return res;

}, err => {
  return Promise.reject(err);
})
export default service
