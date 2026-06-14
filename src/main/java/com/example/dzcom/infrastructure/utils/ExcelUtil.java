package com.example.dzcom.infrastructure.utils;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel工具类
 */
public class ExcelUtil {
    
    /**
     * 导出Excel
     *
     * @param response HTTP 响应对象
     * @param fileName fileName 参数
     * @param data data 参数
     * @param clazz 目标数据类型
     * @throws IOException 方法执行失败时抛出
     * @author dz
     * @date 2026-06-14
     */
    public static void exportExcel(HttpServletResponse response, 
                                    String fileName, 
                                    List<?> data, 
                                    Class<?> clazz) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", 
            "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream(), clazz)
            .sheet("Sheet1")
            .doWrite(data);
    }
    
    /**
     * 导入Excel
     *
     * @param inputStream 输入数据流
     * @param clazz 目标数据类型
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public static <T> List<T> importExcel(InputStream inputStream, Class<T> clazz) {
        return EasyExcel.read(inputStream, clazz, null)
            .sheet()
            .doReadSync();
    }
    
    /**
     * 创建并初始化 ExcelUtil 对象。
     *
     * @author dz
     * @date 2026-06-14
     */
    private ExcelUtil() {
        // 防止实例化
    }
}
