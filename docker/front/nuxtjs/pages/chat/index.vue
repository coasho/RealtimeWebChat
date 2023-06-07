<template>
  <div class="app">
    <div class="chat-container">
      <el-tag v-if="isOnline" class="online" type="success">在线人数：{{ onlineCount }}</el-tag>
      <el-tag size="mini" class="user-list-tag">在线好友列表</el-tag>


      <div class="user-list-container">
        <el-scrollbar class="user-scrollbar">
          <div v-for="(onlineUser,index) in onlineUserItems" @mousedown="selectChatId(onlineUser.id)"
               :class="{'user-row':true,'user-row-selected':currentChatId==onlineUser.id}" :key="index">
            <el-avatar :fit="'fill'" class="avatar" shape="square" :size="'medium'" :src="onlineUser.avatar"/>
          </div>
        </el-scrollbar>
      </div>

      <el-button class="logout" type="danger" plain size="small" @click="logout">退出登录</el-button>


      <!-- TODO  chatMessage  -->
      <div class="chat-list-container">
        <span :class="{'now-chat-name':true,'open-chat-name':currentChatId=='000000'}">{{ currentChatInfo.name }}</span>
        <el-scrollbar ref="chat-scrollbar" class="chat-scrollbar">
          <div class="chat-row" v-for="(chatItem,index) in chatList[currentChatIndex].list" :key="index">
            <el-avatar :class="{'avatar-self':id==chatItem.fromUserId,'avatar-other':id!=chatItem.fromUserId}"
                       shape="square" :size="'large'" :src="chatItem.userInfo.avatar"/>
            <span v-if="chatItem.isDatePoint" class="chat-row-date">{{ chatItem.date }}</span>
            <span v-if="currentChatId=='000000'"
                  :class="{'name-self':id==chatItem.fromUserId,'name-other':id!=chatItem.fromUserId}">
              {{ chatItem.userInfo.name }}
            </span>
            <el-tag v-if="id==chatItem.fromUserId" size="small" class="chat-item-self">
              {{ chatItem.content }}
            </el-tag>
            <el-tag v-else class="chat-item-other" size="small" type="info">{{ chatItem.content }}</el-tag>
          </div>
        </el-scrollbar>
      </div>

      <el-form :inline="true" :model="sendMessage" :class="{'demo-form-inline':true,'send':true}">
        <el-form-item>
          <el-input maxlength="200" v-model="sendMessage.content" placeholder="发送消息，畅所欲言"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button :disabled="sendMessage.content==null" @click="sendMsg">发送</el-button>
        </el-form-item>
      </el-form>
      <div class="back-png"><img src="../../static/background.png"></div>
    </div>
  </div>
</template>

<script>
import cookie from "js-cookie";
import wscon from "~/api/wscon";
import user from "~/api/user";
import "@/assets/chat.css"

export default {
  data() {
    return {
      id: '',
      websocket: null,
      sendMessage: {
        token: '',
        toUserId: null,
        content: null,
        isDatePoint: false
      },
      onlineUserItems: [],
      chatList: [
        {
          id: '000000',
          list: []
        }],
      onlineCount: 0,
      isOnline: false,
      openGroup: {
        avatar: "https://wschat.oss-cn-beijing.aliyuncs.com/avatar/xwlcz.png",
        id: "000000",
        name: "开放群聊"
      },
      currentChatId: '',
      currentChatIndex: 0,
      currentChatInfo: {
        name: "开放群聊"
      },
      preGetDate: 0
    }
  },
  created() {
  },
  mounted() {
    this.wsInit()
  },
  updated() {
    this.scrollDown()
  },
  destroyed() {
    console.log('已离开聊天室')
    this.websocket.close()
  },
  methods: {
    sendMsg() {
      this.sendMessage.token = cookie.get('token')
      if ((Date.parse(new Date()) - this.preGetDate) > 1000 * 60 * 5) {
        this.sendMessage.isDatePoint = true
      } else {
        this.sendMessage.isDatePoint = false
      }
      this.preGetDate = (Date.parse(new Date()))
      if (this.currentChatId == "000000") {
        this.sendMessage.toUserId = null
      } else {
        this.sendMessage.toUserId = this.currentChatId
      }
      this.websocket.send(JSON.stringify(this.sendMessage))
      this.sendMessage.content = null
    },
    wsInit() {
      user.userInfo().then(res => {
        if (res == undefined) {
          this.$router.push('/')
        } else {
          this.id = res.data.data.info.id;
          this.websocket = wscon.create(this.id);
          this.websocket.onopen = this.wsOnOpen
          this.websocket.onmessage = this.wsOnMessage
          this.websocket.onclose = this.wsOnClose
          this.websocket.onerror = this.wsOnError
          this.selectChatId('000000')
        }
      })
    },
    wsOnOpen() {
      console.log('连接服务端成功')
      this.isOnline = true
    },
    wsOnClose() {
      console.log('服务端连接已断开')
      this.isOnline = false
    },
    selectChatId(id) {
      if (this.currentChatId == id) {
        return
      }
      this.currentChatId = id
      for (let i = 0; i < this.onlineUserItems.length; i++) {
        if (this.onlineUserItems[i].id == this.currentChatId) {
          this.currentChatInfo = this.onlineUserItems[i]
        }
      }
      for (let i = 0; i < this.chatList.length; i++) {
        if (this.chatList[i].id == this.currentChatId) {
          this.currentChatIndex = i
        }
      }
      //TODO 获取聊天消息缓存
      user.getCache(this.id, this.currentChatId).then(res => {
        let list = [];
        res.data.data.list.forEach(item => {
          list.push(JSON.parse(item))
        })
        this.chatList[this.currentChatIndex].list = list
      })
    },
    wsOnMessage(event) {
      let res = JSON.parse(event.data)
      // console.log(event.data)
      if (res.message == "token-fail") {
        this.$router.push('/')
      }
      if (res.isSystem) {
        this.onlineCount = res.onlineCount
        for (let i = 0; i < res.onlineUserItems.length; i++) {
          let isContain = false
          for (let j = 0; j < this.chatList.length; j++) {
            if ((this.chatList[j].id == res.onlineUserItems[i].id) || (res.onlineUserItems[i].id == this.id)) {
              isContain = true
            }
          }
          if (!isContain) {
            let newChat = {id: res.onlineUserItems[i].id, list: []}
            this.chatList.push(newChat)
          }
        }
        for (let i = 0; i < res.onlineUserItems.length; i++) {
          if (res.onlineUserItems[i].id == this.id) {
            res.onlineUserItems.splice(i, 1);
          }
        }
        res.onlineUserItems.unshift(this.openGroup)
        this.onlineUserItems = res.onlineUserItems
      } else {
        user.userInfoById(res.fromUserId).then(uInfo => {
          //插入用户数据
          res.userInfo = uInfo.data.data.info
          //日期显示处理
          this.preGetDate = Date.parse(new Date())
          //指定用户聊天列表插入数据
          if (res.isOpen) {
            this.chatList[0].list.push(res)
            this.selectChatId("000000")
          } else {
            if (res.fromUserId == this.id) {
              this.chatList[this.currentChatIndex].list.push(res)
            }else {
              this.selectChatId(res.fromUserId)
            }
            for (let i = 0; i < this.chatList.length; i++) {
              if (this.chatList[i].id == res.fromUserId) {
                this.chatList[i].list.push(res)
              }
            }
          }
        })
      }
    },
    wsOnError() {
      console.log('服务端连接错误')
      this.isOnline = false
      this.wsInit()
    },
    logout() {
      cookie.remove('token')
      this.$router.push('/')
    },
    scrollDown() {
      this.$refs['chat-scrollbar'].wrap.scrollTop = this.$refs['chat-scrollbar'].wrap.scrollHeight
    }
  }
}
</script>

<style scoped>
/deep/ .el-avatar > img {
  width: 100%;
  border: 1px solid #cbbbb8;
  border-radius: 4px;
  box-sizing: border-box;
}

/deep/ .chat-scrollbar {
  display: inline-block;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}

/deep/ .chat-scrollbar .el-scrollbar__wrap {
  overflow-x: hidden;
}

/deep/ .user-scrollbar {
  display: inline-block;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}

/deep/ .user-scrollbar .el-scrollbar__wrap {
  overflow-x: hidden;
}
</style>
