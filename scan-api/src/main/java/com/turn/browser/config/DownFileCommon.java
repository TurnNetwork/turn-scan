package com.turn.browser.config;

import com.turn.browser.bean.CommonConstant;
import com.turn.browser.response.account.AccountDownload;
import com.turn.browser.utils.CommonUtil;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Download file unified package class
 */
@Slf4j
@Component
public class DownFileCommon {

    /**
     * Download method
     *
     * @throws Exception
     * @method download
     */
    public void download(HttpServletResponse response, String filename, long length, byte[] data) throws IOException {
        /** Return setting header and type*/
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        response.setHeader(CommonConstant.TRACE_ID, CommonUtil.ofNullable(() -> CommonUtil.getTraceId()).orElse(""));
        response.setContentType("application/octet-stream");
        response.setContentLengthLong(length);
        response.getOutputStream().write(data);
    }


    public AccountDownload writeDate(String filename, List<Object[]> rows, String... headers) {
        AccountDownload accountDownload = new AccountDownload();
        /** Initialize the output stream object */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            /** Set the exported csv header to prevent garbled characters */
            byteArrayOutputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        } catch (Exception e) {
            log.error("Output data error:", e);
            return accountDownload;
        }
        Writer outputWriter = new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8);
        CsvWriter writer = new CsvWriter(outputWriter, new CsvWriterSettings());
        /** Set the header of the export table */
        writer.writeHeaders(headers);
        writer.writeRowsAndClose(rows);
        /** Set return object */
        accountDownload.setData(byteArrayOutputStream.toByteArray());
        accountDownload.setFilename(filename);
        accountDownload.setLength(byteArrayOutputStream.size());
        return accountDownload;
    }

}
