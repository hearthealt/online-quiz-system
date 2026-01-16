package com.quiz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 在线答题系统主启动类
 *
 * @author Quiz System
 * @since 2024-09-26
 */
@SpringBootApplication
@MapperScan("com.quiz.mapper")
public class QuizApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizApplication.class, args);
        System.out.println("=================================");
        System.out.println("在线答题系统启动成功！");
        System.out.println("访问地址: http://localhost:9090/api");
        System.out.println("=================================");
    }
}
