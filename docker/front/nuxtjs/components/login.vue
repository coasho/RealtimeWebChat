<template>
  <div class="app">
    <div :class="{'login-container':true,'form-out':formOut}" @mouseout="formOut=true" @mouseover="formOut=false">
      <el-form label-position="right" label-width="80px" :model="userInfo">
        <el-form-item label="用户名">
          <el-input v-model="userInfo.name"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input show-password v-model="userInfo.password"></el-input>
        </el-form-item>
        <el-form-item class="input">
          <el-button type="success" plain @click="submitForm">登录</el-button>
          <el-button @click="signup">注册</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import user from "~/api/user";
import cookie from "js-cookie"

export default {
  name: "login",
  data() {
    return {
      userInfo: {
        name: '',
        password: ''
      },
      formOut: false
    }
  },
  created() {
    user.userInfo().then(res => {
      if (res != undefined) {
        this.$router.push('/chat')
      }
    })
  },
  methods: {
    submitForm() {
      user.login(this.userInfo).then(res => {
        if (res.data.success) {
          this.$message.success({type: "success", message: '登陆成功'})
          cookie.set('token', res.data.data.token, {domain: '8.130.114.187', expires: 7});
          this.$router.push('/chat')
        }
      })
    },
    resetForm() {
      this.userInfo = {}
    },
    signup(){
      this.$router.push('/signup')
    }
  }
}
</script>

<style scoped>
@keyframes form-hover {
  0% {
    box-shadow: none;
    border: 1px solid #e2e5e5;
  }
  100% {
    box-shadow: #bee1da 1px 0px 13px;
    border: 1px solid #b4e7f3;
  }
}

@keyframes form-out {
  0% {
    box-shadow: #bee1da 1px 0px 13px;
    border: 1px solid #b4e7f3;
  }
  100% {
    box-shadow: none;
    border: 1px solid #e2e5e5;
  }
}

.app {
  text-align: center;
  padding-top: 6%;
}

.login-container {
  display: inline-block;
  width: 500px;
  max-width: 80%;
  height: 300px;
  text-align: center;
  padding: 30px 20px 0 0;
  border: 1px solid #e2e5e5;
  border-radius: 6px;
  box-sizing: border-box;
}

.login-container:hover {
  animation: form-hover 0.3s ease-out forwards;
}

.form-out {
  animation: form-out 0.3s ease-out forwards;
}
</style>
