package com.zykj.file.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zykj.common.api.model.file.FileRecord;
import com.zykj.file.core.mapper.FileRecordMapper;
import com.zykj.file.core.service.FileRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class FileRecordServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileRecordService  {

}
