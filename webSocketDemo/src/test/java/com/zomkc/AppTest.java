package com.zomkc;

import com.zomkc.config.WebSocketConfig;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;


@SpringBootTest(classes = App.class)
public class AppTest {

    @MockBean
    WebSocketConfig webSocketConfig;    //顶替websocket配置类,使其不在测试时注入
    private static ConcurrentHashMap<String, HashMap<String,TreeSet<String>>> VCSList = new ConcurrentHashMap<String, HashMap<String,TreeSet<String>>>();

    private static ArrayList<String> usersArray = new ArrayList<>();

    @Test
    void test(){
        HashMap<String,TreeSet<String>> mmp = new HashMap<String,TreeSet<String>>();
        TreeSet<String> strings = new TreeSet<>();
        strings.add("张三");
        strings.add("王五");
        strings.add("李四");
        mmp.put("房主",strings);
        VCSList.put("1",mmp);
        HashMap<String,TreeSet<String>> map = VCSList.get("1");
        TreeSet<String> set = map.get(map.keySet().toArray()[0].toString());

        if ("房主".equals(map.keySet().toArray()[0].toString())){ //如果名字是map集合的建,就代表是房主
            System.out.println("房主!!!!!!");
        }else {

        }
    }
}
