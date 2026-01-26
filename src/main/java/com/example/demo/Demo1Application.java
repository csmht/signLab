package com.example.demo;

import com.example.demo.config.AdminInitializer;
import com.tangzc.autotable.springboot.EnableAutoTable;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableAutoTable
public class Demo1Application {

    static {
        // 在类加载时立即加载环境变量，确保在Spring Boot初始化之前完成
        loadEnvironmentVariables();
    }

    private static void loadEnvironmentVariables() {
        // 加载环境变量文件 - 在当前工作目录（jar包所在目录）寻找
        String currentDir = System.getProperty("user.dir");

        Dotenv dotenv = Dotenv.configure()
                .directory(currentDir)
                .filename("local-config.env")
                .ignoreIfMissing()
                .load();

        if (dotenv.entries().size() > 0) {
            System.out.println("✅ 找到配置文件: " + currentDir + "/local-config.env");
        } else {
            System.out.println("⚠️ 未找到配置文件，将使用默认配置");
        }

        // 将环境变量设置为系统属性，以便Spring Boot读取
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Demo1Application.class, args);
        AdminInitializer bean = context.getBean(AdminInitializer.class);
        bean.start();
    }

}
