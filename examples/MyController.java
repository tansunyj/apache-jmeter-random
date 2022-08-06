package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName MyController
 * @Description 此代码可用来验证脚本中执行的结果是否符合预期的随机数分布占比
 * @Author 杨杰
 * @Date 2022/1/21 20:49
 * @Version 1.0
 */
@RestController
@RequestMapping("my")
public class MyController {

    Map<String, AtomicLong> MAP=new ConcurrentHashMap<>();

    @GetMapping("test")
    public ResponseEntity test(@RequestParam double random){
        String key;
        if (random>=1.0d && random<=10.0d){
            key="LOW";
        }else if (random>=40.0d && random<=70.0d){
            key="MIDDLE";
        }else {
            key="HIGH";
        }
        handler(key);
        return ResponseEntity.ok("success");
    }

    @GetMapping("result")
    public ResponseEntity result(){
        System.out.println(MAP.toString());
        final long sum = MAP.values().stream().mapToLong(t->t.get()).sum();
        System.out.println(sum);
        MAP.forEach((key,value)->{
            System.out.println("key="+key+",占比"+(value.get()* 1.0d/sum));
        });
        return ResponseEntity.ok("success");
    }

    private void handler(String key){
        if (MAP.containsKey(key)){
            AtomicLong atomicLong = MAP.get(key);
            atomicLong.addAndGet(1);
            MAP.put(key,atomicLong);
        }else {
            MAP.put(key,new AtomicLong(1));
        }
    }
}
