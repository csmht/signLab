-- 课堂小测功能数据库表

-- 1. 课堂小测表
CREATE TABLE `classroom_quiz` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `class_experiment_id` bigint(20) NOT NULL COMMENT '班级实验ID',
  `procedure_topic_id` bigint(20) NOT NULL COMMENT '题库配置ID',
  `quiz_title` varchar(255) NOT NULL COMMENT '小测标题',
  `quiz_description` text COMMENT '小测描述',
  `quiz_time_limit` int(11) DEFAULT NULL COMMENT '答题时间限制（分钟）',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态:0-未开始,1-进行中,2-已结束',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `created_by` varchar(50) NOT NULL COMMENT '创建者(教师用户名)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_class_experiment_id` (`class_experiment_id`),
  KEY `idx_procedure_topic_id` (`procedure_topic_id`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课堂小测表';

-- 2. 课堂小测答案表
CREATE TABLE `classroom_quiz_answer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `classroom_quiz_id` bigint(20) NOT NULL COMMENT '课堂小测ID',
  `student_username` varchar(50) NOT NULL COMMENT '学生用户名',
  `class_code` varchar(20) NOT NULL COMMENT '班级编号',
  `answer` text COMMENT '答案内容(JSON格式)',
  `score` decimal(5,2) DEFAULT '0.00' COMMENT '得分',
  `is_correct` tinyint(1) DEFAULT '0' COMMENT '是否全部正确:0-否,1-是',
  `submission_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quiz_student` (`classroom_quiz_id`, `student_username`),
  KEY `idx_student_username` (`student_username`),
  KEY `idx_class_code` (`class_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课堂小测答案表';
