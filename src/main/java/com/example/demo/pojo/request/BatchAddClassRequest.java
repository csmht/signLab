ackage com.example.demo.pojo.request;

import lombok.Data;

import java.util.List;

/**
 * 批量添加班级请求
 */
@Data
public class BatchAddClassRequest {

    /**
     * 班级列表
     */
    private List<ClassInfo> classes;

    /**
     * 班级信息内部类
     */
    @Data
    public static class ClassInfo {
        /**
         * 班级编号
         */
        private String classCode;

        /**
         * 班级名称
         */
        private String className;
    }

}