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
        <el-form-item label="密码确认">
          <el-input show-password v-model="password"></el-input>
        </el-form-item>
        <el-form-item label="设置头像">
          <el-upload
            v-loading="upLoading"
            class="avatar-uploader"
            action="http://8.130.114.187:2233/user/upload/avatar"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload">
            <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="success" plain @click="submitForm">提交注册</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import login from "~/api/login";

export default {
  data() {
    return {
      userInfo: {
        name: '',
        password: '',
        avatar: null
      },
      password: '',
      upLoading:false,
      formOut: false
    }
  },
  created() {

  },
  methods: {
    submitForm() {
      if (this.userInfo.password == this.password) {
        login.signup(this.userInfo).then(res => {
          if (res.data.success) {
            this.$message.success({type: "success", message: '注册成功'})
            this.$router.push('/')
          }
        })
      } else {
        this.$message.error({type: "error", message: '密码不一致'})
      }
    },
    beforeAvatarUpload(file) {
      const isRightType = /\.(jpg|jpeg|png|GIF|JPG|PNG)$/.test(file.name)
      if (!isRightType) {
        this.$message.warning({type: 'warning', message: '上传头像只能是JPG或PNG格式！'})
      }
      this.upLoading=true;
      return isRightType;
    },
    handleAvatarSuccess(res, file) {
      console.log(res)
      this.upLoading=false
      this.userInfo.avatar=res.data.url
    },
    resetForm() {
      this.userInfo = {}
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
  height: 500px;
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

/deep/ .avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

/deep/ .avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>

