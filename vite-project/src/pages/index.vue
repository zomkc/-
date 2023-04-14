<template>
  <div class="web">
      <div class="web-left">
        <div class="upper">
          <div class="user">
              <div class="span">
                <span>用户名</span>
              </div>
              <div class="input">
                <el-input v-model="name" placeholder="用户名" />
              </div>
          </div>
          <div class=user>
              <div class="span">
                <span>会议室id</span>
              </div>
              <div class="input">
                <el-input v-model="vcsId" placeholder="会议室id" />
              </div>
          </div>
          <div class="webbutton">
              <button @click="websocketinit">进入会议室</button>
              <button @click="websocketclose">退出会议室</button>
          </div>
          <div class="msggs">
                <el-input
                  v-model="textarea"
                  :autosize="{ minRows: 6, maxRows: 8 }"
                  resize="none"
                  type="textarea"
                  placeholder="请输入"
                />
              <div class="webmsg">
                  <button @click="sendMsg">发送消息</button>
              </div>
          </div>
        </div>
        <div class="lower">
          <div>成员列表</div>
          <div v-for="item in users">{{ item }}</div>
        </div>
      </div>

      <div class="web-right">
        <div class="neirong">
            <div v-for="item in dataMsg">
              <div :class="item.or == 1 ?'other':'oneself' " >
              <span>{{ item.name }}</span>:
              <span>{{ item.textarea }}</span>
              </div>
            </div>
        </div>
      </div>
      <div class="videoDemo">
        <div class="video">
          <video id="self" autoplay ></video>
        </div>
        <div class="video">
          <video id="self" autoplay ></video>
        </div>
        <div class="video">
          <video id="self" autoplay ></video>
        </div>
        <div class="video">
          <video id="self" autoplay ></video>
        </div>
      </div>
  </div>        
</template>
  
<script setup>
import {ref,reactive} from "vue"


const textarea = ref('')
const name =ref('')
const vcsId = ref('')
const dataMsg = reactive([])  //用户消息列表
const users = reactive([])  //用户列表,不包括自己

var websocket = null
var locatstream = ''

function websocketinit(){   //调用本地摄像头
  navigator.mediaDevices
  .getUserMedia({
    video:true,
    audio:true
  })
  .then((stream)=>{
    getUserStream(stream)
      if(name.value && vcsId.value){
      websocket = new WebSocket("ws://localhost:8989/myWs/"+name.value+"/"+vcsId.value)
      // websocket = new WebSocket("wss://zomkc.cn:8989/myWs/"+name.value+"/"+vcsId.value)
      websocket.onopen = Vonopen
      websocket.onerror = Vonerror
      websocket.onmessage = Vonmessage
      websocket.onclose = Vonclose
      }else{
        console.log('请输入用户名和会议室id');
      }
  }).catch((err)=>{
    console.log("错误:"+err);
  })
}
//获取绑定自己的媒体流到video上
function getUserStream(stream) {
        var selfVideo = document.getElementById("self");
        selfVideo.srcObject = stream;
        locatstream = stream;
};

//连接发生错误的回调方法
const Vonerror = () => {
  console.log("连接发生错误");
}
//连接成功建立的回调方法
const Vonopen = () => {
  console.log("连接成功");
  // websocket.send(VStream)
}
//接收到消息的回调方法
const Vonmessage = (event) => {
  var message = JSON.parse(event.data)
  if(message.type == "msgage"){
    dataMsg.push(message.msgage)
  }else if(message.type == "login"){
    users.push(message.username)
  }
}
//连接关闭的回调方法
const Vonclose = () => {
  console.log("连接关闭");
}
//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
window.onbeforeunload = function () {
  websocket.close()
}

function sendMsg(){
  if(textarea.value){
    dataMsg.push({name:name.value,textarea:textarea.value,or:"0"})  //or  0:代表自己的消息,1代表别人的消息
    websocket.send(JSON.stringify({
            'type':"msgage",
            'msgage':{name:name.value,textarea:textarea.value,or:"1"}
    }));
    textarea.value = null
  }
}
function websocketclose(){
  // websocket.close()
  var selfVideo = document.getElementById("self");
  selfVideo.srcObject.getTracks()[1].stop();
}

</script>
  
<style scoped>
input{
  width: 4rem;
}
.web{
  min-width: 1300px;
  min-height: 300px;
  display: flex;
  justify-content: flex-start;
}
.web .web-left .upper{
  height: 200px;
}
.user{
  display: flex;
  height: 25px;
}
.span{
  width: 30%;
}
.input{
  width: 70%;
}
.webbutton{
  display: flex;
  justify-content: space-around;
  /* margin-top: 10px; */
  height: 25px;
}
.msggs{
  height: 125px;
  display: flex;
  justify-content: space-around;
  margin-top: 5px;
}
.webmsg{
  height: 100%;
}
.web-left{
  width: 250px;
  height: 500px;
}
.web-right{
  width: 500px;
  height: 500px;
  background-color: blueviolet;
  display: flex;
  justify-content: center;
  align-items: center;
}
.neirong{
  width: 95%;
  height: 95%;
  background-color: rgb(255, 255, 255);
}
.other{
  display: flex;
}
.oneself{
  display: flex;
  justify-content:flex-end;
}
.videoDemo{
  width: 600px;
  height: 500px;
  display: flex;
  flex-wrap:wrap;
  justify-content:space-around;
}
.video ,#self{
  width: 280px;
  height: 200px;
}
</style>
  