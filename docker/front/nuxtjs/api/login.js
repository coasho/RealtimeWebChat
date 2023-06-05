import request from '@/utils/request'

export default {
  login(userInfo) {
    return request({
      url: `/user/login`,
      method: 'post',
      data: userInfo
    })
  },
  signup(userInfo) {
    return request({
      url: `/user/signup`,
      method: 'post',
      data:userInfo
    })
  },
  userInfo() {
    return request({
      url: `/user/info`,
      method: 'get'
    })
  },
  userInfoById(id) {
    return request({
      url: `/user/infoById/` + id,
      method: 'get'
    })
  }
}
