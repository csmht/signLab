package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ClassExperimentMapper;
import com.example.demo.pojo.entity.ClassExperiment;
import com.example.demo.pojo.vo.TeacherQrVO;
import com.example.demo.util.CryptoUtil;
import com.example.demo.util.SecurityUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class QrService {

    @Value("${slz.qr.duration:10}")
    private Long duration;

    private final CryptoUtil cryptoUtil;
    private final ClassExperimentMapper classExperimentMapper;

    /**
     * 根据班级实验ID获取签到二维码
     *
     * @param classExperimentId 班级实验ID
     * @return 二维码数据
     */
    public TeacherQrVO getTeacherQrVO(Long classExperimentId) {
        ClassExperiment classExperiment = classExperimentMapper.selectById(classExperimentId);
        if (classExperiment == null) {
            throw new BusinessException(404, "班级实验不存在");
        }

        return generateQrVO(classExperiment);
    }

    /**
     * 根据班级代码和实验ID获取签到二维码
     *
     * @param classCode 班级代码
     * @param experimentId 实验ID
     * @return 二维码数据
     */
    public TeacherQrVO getTeacherQrVOByClassAndExperiment(String classCode, String experimentId) {
        QueryWrapper<ClassExperiment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_code", classCode)
                .eq("experiment_id", experimentId);
        ClassExperiment classExperiment = classExperimentMapper.selectOne(queryWrapper);

        if (classExperiment == null) {
            throw new BusinessException(404, "班级实验不存在");
        }

        return generateQrVO(classExperiment);
    }

    /**
     * 生成二维码数据
     *
     * @param classExperiment 班级实验
     * @return 二维码数据
     */
    private TeacherQrVO generateQrVO(ClassExperiment classExperiment) {
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        TeacherQr teacherQr = new TeacherQr();
        currentUsername.ifPresent(teacherQr::setTeacherName);
        teacherQr.setEndTime(LocalDateTime.now().plusSeconds(duration + 2));
        teacherQr.setClassCode(classExperiment.getClassCode());
        teacherQr.setExperimentCode(classExperiment.getExperimentId());

        TeacherQrVO teacherQrVO = new TeacherQrVO();
        teacherQrVO.setSeconds(duration);
        teacherQrVO.setFileKey(cryptoUtil.encrypt(teacherQr.toString()));

        return teacherQrVO;
    }

}

@Data
class TeacherQr{
    private String teacherName;
    private String classCode;
    private String experimentCode;
    private LocalDateTime endTime;
    private final String key ;

    public TeacherQr() {
        this.key = UUID.randomUUID().toString();
    }

    public TeacherQr(String key) {
        String[] split = key.split("\\|");
        this.teacherName = split[0];
        this.classCode = split[1];
        this.experimentCode = split[2];
        this.endTime = LocalDateTime.parse(split[3]);
        this.key = split[4];
    }

    public String toString(){
        return teacherName + "|" + this.classCode + "|" + this.experimentCode + "|" + endTime  + "|" + key;
    }
}