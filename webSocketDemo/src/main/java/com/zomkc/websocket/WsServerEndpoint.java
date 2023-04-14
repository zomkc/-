package com.zomkc.websocket;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/myWs/{name}/{vcsId}")
@Component
public class WsServerEndpoint {
    //concurrent包的线程安全Map，用来存放每个客户端对应的LoginWebsocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    // String: 用户名  WsServerEndpoint: 当前用户的连接信息
    private static ConcurrentHashMap<String,WsServerEndpoint> webSocketMap = new ConcurrentHashMap<String, WsServerEndpoint>();
    //会议用户列表
    // String: 会议室号     map-String: 会议室主人   TreeSet<String>: 当前会议室用户列表
    private static ConcurrentHashMap<String, HashMap<String,TreeSet<String>>> VCSList = new ConcurrentHashMap<String, HashMap<String,TreeSet<String>>>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    /**
     * 连接成功
     * name: 用户名
     * vcsId: 会议室号
     */
    @OnOpen
    public void onOpen(@PathParam("name") String name,@PathParam("vcsId") String vcsId,Session session) throws IOException {
        //以后利用session向其他客户端发送数据
        this.session = session;
        //将 当前用户 和 实例对象 传入map中
        name = URLDecoder.decode(name, "utf-8");
        //TODO 检查是否是重复登录    用户名不能重复

        webSocketMap.put(name,this);    //保存当前用户的websocket信息

        if(VCSList.containsKey(vcsId)){ //检测当前会议室id是否存在,没有就是新建会议室,存在就是加入此会议室
                HashMap<String, TreeSet<String>> map = VCSList.get(vcsId);
                TreeSet<String> set = map.get(map.keySet().toArray()[0]);
                System.out.println(new Date() +" "+name+"---加入了---"+map.keySet().toArray()[0].toString()+"---的会议室");
                //构造返回消息
                JSONObject message = new JSONObject();
                message.put("type","login");
                message.put("username",name);
                //根据集合包发送自己参加会议的消息
                for(String joiner:set){
                    try {
                        webSocketMap.get(joiner).sendMessage(message.toString());  //将新入会成员推送给会议列表的用户
                        //给客户端发送之前已经参加会议的人的集合包
                        JSONObject message3=new JSONObject();
                        message3.put("type","login");
                        message3.put("username",joiner);
                        this.sendMessage(message3.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                set.add(name);   //将用户加入列表
                System.out.println("当前用户列表:");
                for (String s : set) {
                    System.out.println(s+"   ");
                }
                VCSList.put(vcsId,map);
        }else {
            System.out.println(new Date() +" "+ name +"创建会议,会议号: "+vcsId);
            HashMap<String, TreeSet<String>> map = new HashMap<String,TreeSet<String>>();
            TreeSet<String> strings = new TreeSet<>();  //创建用户列表
            strings.add(name);
            map.put(name,strings);
            VCSList.put(vcsId,map);    //存储会议室id和会议室主人名字为key的用户列表
        }
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose(@PathParam("name") String name,@PathParam("vcsId") String vcsId,Session session) throws UnsupportedEncodingException {
        name = URLDecoder.decode(name, "utf-8");
        System.out.println("用户:"+name+"退出了会议");
        try {
            HashMap<String,TreeSet<String>> map = VCSList.get(vcsId); //获取当前会议室id 获取用户
            TreeSet<String> set = map.get(map.keySet().toArray()[0].toString());
            JSONObject message = new JSONObject();

            if (name.equals(map.keySet().toArray()[0].toString())){ //如果名字是map集合的建,就代表是房主
                message.put("type","text");
                message.put("text","会议室关闭");
                //TODO 给每个人发送会议室关闭的消息

                VCSList.remove(vcsId);  //删除会议室
//            for (String s : set) {
                //TODO 改成给每个用户返回房主退出信息,让客户端关闭连接,不然会报错
//                webSocketMap.remove(s); //删除当前会议室所有用户的websocket信息
//            }
            }else {   //如果不是,就代表当前用户退出会议室
                message.put("type","text");
                message.put("text",name+"离开会议");
                //根据集合包发送自己离开会议的消息
                for(String joiner:set){
                    try {
                        webSocketMap.get(joiner).sendMessage(message.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                set.remove(name); //将当前用户移除当前会议室用户列表
                webSocketMap.remove(name); //将当前用户移除会议室
            }
        }catch (Exception e){
            System.out.println("关闭连接方法报错:===="+e);
        }
    }

    /**
     * 接收到消息
     */
    @OnMessage
    public void onMsg(String message , @PathParam("name") String name,@PathParam("vcsId") String vcsId) throws IOException {
        if(VCSList.containsKey(vcsId)){ //检测当前会议室id是否存在
            //对消息进行解析，判断消息的类型
            JSONObject msgage = JSONObject.fromObject(message);
            if(msgage.getString("type").equals("msgage")){  //type是msgage代表是用户消息
                name = URLDecoder.decode(name, "utf-8");
                HashMap<String, TreeSet<String>> map = VCSList.get(vcsId);
                TreeSet<String> set = map.get(map.keySet().toArray()[0]);
                for (String s : set) {
                    if (!s.equals(name)){   //给除自己外的用户发送消息
                        System.out.println(name+"给"+s+"发送消息:"+msgage);
                        webSocketMap.get(s).sendMessage(msgage.toString());
                    }
                }
            }
        }

    }

    public void sendMessage(String message) throws IOException{
        //服务端 向客户端推送消息
        this.session.getBasicRemote().sendText(message);
    }
}
