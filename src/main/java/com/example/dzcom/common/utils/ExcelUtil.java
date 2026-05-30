package com.example.dzcom.common.utils;

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
     */
    public static <T> List<T> importExcel(InputStream inputStream, Class<T> clazz) {
        return EasyExcel.read(inputStream, clazz, null)
            .sheet()
            .doReadSync();
    }
    
    private ExcelUtil() {
        // 防止实例化
    }
}
