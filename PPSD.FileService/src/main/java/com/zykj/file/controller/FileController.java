package com.zykj.file.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.extension.exceptions.ApiException;
import com.zykj.common.api.model.file.FileRecord;
import com.zykj.common.core.constant.CommonConstants;
import com.zykj.common.core.util.R;
import com.zykj.common.core.util.ValidateUtils;
import com.zykj.common.minio.service.MinioTemplate;
import com.zykj.file.core.service.FileRecordService;
import com.zykj.file.utils.ImageUtil;
import com.zykj.common.security.annotation.Inner;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author junyang
 * @date 2019/8/26
 */


@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/file")
@Api(value = "file", tags = "文件管理模块")
public class FileController {
    private final MinioTemplate minioTemplate;

    private final FileRecordService fileRecordService;

    /**
     * 上传文件
     * 文件名采用uuid,避免原始文件名中带"-"符号导致下载的时候解析出现异常
     *
     * @param file 资源
     * @return R(bucketName, filename)
     */
    @PostMapping("/upload")
    public R upload(@RequestParam("file") MultipartFile file, @RequestParam("bucketname") String bucketname) {
        String fileId = IdUtil.simpleUUID();
        String fileName = fileId + StrUtil.DOT + FileUtil.extName(file.getOriginalFilename());
        Map<String, String> resultMap = this.ExistFileName(fileName, bucketname);
        try {
            minioTemplate.putObject(bucketname, fileName, file.getInputStream());
            //保存文件记录
            saveFileRecord(fileId, bucketname, fileName, file);
        } catch (Exception e) {
            log.error("上传失败", e);
            return R.builder().code(CommonConstants.FAIL)
                    .msg(e.getLocalizedMessage()).build();
        }
        return R.builder().data(resultMap).build();
    }

    /**
     * 上传文件
     * 文件名采用uuid,避免原始文件名中带"-"符号导致下载的时候解析出现异常
     *
     * @param file 资源
     * @return R(bucketName, filename)
     */
    @PostMapping(value = "/inner/upload/{bucketName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Inner
    public R uploadInner(@RequestPart(value = "file") MultipartFile file, @PathVariable String bucketName) {
        String fileId = IdUtil.simpleUUID();
        String fileName = fileId + StrUtil.DOT + FileUtil.extName(file.getOriginalFilename());
        this.ExistFileName(fileName, bucketName);
        FileRecord fileRecord = new FileRecord();
        try {
            minioTemplate.putObject(bucketName, fileName, file.getInputStream());
            //保存文件记录
            fileRecord.setId(fileId);
            fileRecord.setBucketName(bucketName);
            fileRecord.setName(file.getOriginalFilename());
            fileRecord.setSize(file.getSize());
            fileRecord.setPath(fileName);
            fileRecordService.saveOrUpdate(fileRecord);
        } catch (Exception e) {
            log.error("上传失败", e);
            return R.builder().code(CommonConstants.FAIL)
                    .msg(e.getLocalizedMessage()).build();
        }
        return R.builder().data(fileRecord).build();
    }

    /**
     * 上传文件带时间水印
     * 文件名采用uuid,避免原始文件名中带"-"符号导致下载的时候解析出现异常
     *
     * @param file 资源
     * @return R(bucketName, filename)
     */
    @PostMapping("/uploadmark")
    public R uploadWithMark(@RequestParam("file") MultipartFile file, @RequestParam("bucketname") String bucketname) {
        String fileId = IdUtil.simpleUUID();
        String fileName = fileId + StrUtil.DOT + FileUtil.extName(file.getOriginalFilename());
        Map<String, String> resultMap = this.ExistFileName(fileName, bucketname);
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //添加水印
            BufferedImage bufferedImage = ImageUtil.markImageWithDate(file.getInputStream(), 5, 5, 250, 50);
            ImageIO.write(bufferedImage, fileType, byteArrayOutputStream);
            //保存文件记录
            saveFileRecord(fileId, bucketname, fileName, file);
            minioTemplate.putObject(bucketname, fileName, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        } catch (Exception e) {
            log.error("上传失败", e);
            return R.builder().code(CommonConstants.FAIL)
                    .msg(e.getLocalizedMessage()).build();
        }

        return R.builder().data(resultMap).build();
    }

    /**
     * 获取文件
     *
     * @param fileName 文件空间/名称
     * @param response
     * @return
     */
    @GetMapping("/{fileName}")
    public void file(@PathVariable String fileName, HttpServletResponse response) {
        String[] nameArray = StrUtil.split(fileName, StrUtil.DASHED);

        try (InputStream inputStream = minioTemplate.getObject(nameArray[0], nameArray[1])) {
            response.setContentType("application/octet-stream; charset=UTF-8");
            IoUtil.copy(inputStream, response.getOutputStream());
        } catch (Exception e) {
            log.error("文件读取异常", e);
        }
    }

    /**
     * 获取文件
     *
     * @param fileName 文件空间/名称
     * @param response
     * @return
     */
    @GetMapping("/preview/{name}")
    public void preview(@PathVariable String name, @RequestParam("filename") String fileName, @RequestParam("contenttype") String contenttype,
                        @RequestParam("token") String token, HttpServletResponse response) {

        if (!ValidateUtils.checkToken(token)) {
            response.setStatus(403);
            return;
        }
        String[] nameArray = StrUtil.split(fileName, StrUtil.DASHED);

        try (InputStream inputStream = minioTemplate.getObject(nameArray[0], nameArray[1])) {
            String _contentType = URLDecoder.decode(contenttype, "UTF-8");
            response.setContentType(_contentType);
            IoUtil.copy(inputStream, response.getOutputStream());
        } catch (Exception e) {
            log.error("文件读取异常", e);
        }
    }

    @ApiOperation("根据文件在对象存储中的位置下载文件")
    @GetMapping("/download/{objName}")
    public void download(@PathVariable String objName, @RequestParam("name") String name,
                         @RequestParam("token") String token, HttpServletResponse response) {
        if (!ValidateUtils.checkToken(token)) {
            response.setStatus(403);
            return;
        }
        String[] nameArray = StrUtil.split(objName, StrUtil.DASHED);
        try (InputStream inputStream = minioTemplate.getObject(nameArray[0], nameArray[1])) {
            response.setContentType("application/octet-stream");
            String contentDisposition = "attachment; filename=" + java.net.URLEncoder.encode(name, "UTF-8");
            response.setHeader("Content-Disposition", contentDisposition);
            IoUtil.copy(inputStream, response.getOutputStream());
        } catch (Exception e) {
            log.error("文件读取异常", e);
        }
    }

    /**
     * 获取文件
     *
     * @param fileName 文件空间/名称
     * @param response
     * @return
     */
    @GetMapping(value = "/inner/{fileName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Inner
    public void innerFile(@PathVariable String fileName, HttpServletResponse response) {
        String[] nameArray = StrUtil.split(fileName, StrUtil.DASHED);
        InputStream in = minioTemplate.getObject(nameArray[0], nameArray[1]);
        String fileId = nameArray[1].substring(0, nameArray[1].lastIndexOf(StrUtil.DOT));
        FileRecord fileRecord = fileRecordService.getById(fileId);
        try {
            if (fileRecord != null) {
                response.setHeader("filename", URLEncoder.encode(fileRecord.getName(), "utf-8"));
                response.setHeader("filesize", fileRecord.getSize().toString());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            OutputStream out = response.getOutputStream();
            byte buffer[] = new byte[1024];
            int length = 0;
            while ((length = in.read(buffer)) >= 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpStatus.HTTP_BAD_GATEWAY);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @GetMapping("/link/{fileName}")
    public R link(@PathVariable("fileName") String fileName,
                  @RequestParam(name = "expires", required = false) Integer expires) {
        String[] split = fileName.split("-");
        if (split.length != 2)
            throw new ApiException("请求的文件信息异常！");
        if (expires != null && expires > CommonConstants.File.SHARE_URL_EXPIRES_MAX)
            expires = CommonConstants.File.SHARE_URL_EXPIRES_MAX;
        String objectURL = minioTemplate.getObjectURL(split[0], split[1],
                expires == null ? CommonConstants.File.SHARE_URL_EXPIRES_MAX : expires);
        return R.builder().data(objectURL).build();
    }

    @PostMapping("/linklist")
    public R linkList(@RequestBody List<String> filtList) {
        List<String> list = new LinkedList<>();

        for (String fileName : filtList) {
            String[] split = fileName.split("-");
            if (split.length != 2)
                throw new ApiException("请求的文件信息异常！");
            String objectURL = minioTemplate.getObjectURL(split[0], split[1], CommonConstants.File.SHARE_URL_EXPIRES_MIN);
            list.add(objectURL);
        }
        return R.builder().data(list).build();
    }

    @PostMapping("/linklistInner")
    @Inner
    public R linkListInner(@RequestBody List<String> fileList) {
        List<String> list = new LinkedList<>();
        for (String fileName : fileList) {
            String[] splitfileName = fileName.split(",");
            for (String itemFileName : splitfileName) {
                String[] split = itemFileName.split("-");
                if (split.length != 2)
                    continue;
                String objectURL = minioTemplate.getObjectURL(split[0], split[1], CommonConstants.File.SHARE_URL_EXPIRES_MAX);
                list.add(objectURL);
            }
        }
        return R.builder().data(list).build();
    }

    /**
     * 获取文件记录
     *
     * @param ids
     * @return
     */
    @PostMapping("/record")
    @Inner
    public R fileRecord(@RequestBody List<String> ids) {
        List<FileRecord> fileRecords = new ArrayList<>(fileRecordService.listByIds(ids));
        return R.builder().data(fileRecords).build();
    }

    /**
     * 判断文件是否存在
     *
     * @param fileName
     * @param bucketName
     * @return
     */
    private Map<String, String> ExistFileName(String fileName, String bucketName) {
        Map<String, String> resultMap = new HashMap<>(4);
        //替换-字符，防止下载的时候解析出错
        bucketName = StrUtil.replace(bucketName, StrUtil.DASHED, "");
        //不存在bucketname的桶，则创建一个新的存储空间
        minioTemplate.createBucket(bucketName);
        resultMap.put("bucketName", bucketName);
        resultMap.put("fileName", fileName);
        return resultMap;
    }

    /**
     * 保存文件记录
     *
     * @param fileId
     * @param bucketname
     * @param fileName
     * @param file
     */
    private void saveFileRecord(String fileId, String bucketname, String fileName, MultipartFile file) {
        FileRecord fileRecord = new FileRecord();
        fileRecord.setId(fileId);
        fileRecord.setBucketName(bucketname);
        fileRecord.setName(file.getOriginalFilename());
        fileRecord.setSize(file.getSize());
        fileRecord.setPath(fileName);
        fileRecordService.saveOrUpdate(fileRecord);
    }
}
