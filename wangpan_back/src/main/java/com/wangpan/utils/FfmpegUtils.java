package com.wangpan.utils;

import com.wangpan.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author fangyixin
 * @date 2023/11/30 14:09
 */
public class FfmpegUtils {
    private static final Logger logger= LoggerFactory.getLogger(FfmpegUtils.class);

    public static String executeCommand(String cmd,Boolean outprintLog) throws BusinessException{
        if(StringTool.isEmpty(cmd)){
            logger.error("----指令执行失败，ffmpeg指令为空----");
            return null;
        }
        Runtime runtime=Runtime.getRuntime();
        Process process=null;
        try{
            process=runtime.exec(cmd);
            //执行ffmpeg指令，取出输入流和错误流信息
            //必须取出ffmpeg执行过程中产生的输出信息，如果不取的话，当输出流信息填满jvm存储输出流信息的缓冲区空间时，线程就会阻塞
            PrintStream errorStream=new PrintStream(process.getErrorStream());
            PrintStream inputStream=new PrintStream(process.getInputStream());
            errorStream.start();
            inputStream.start();
            //等待ffmpeg命令执行完
            process.waitFor();
            //获取执行结果字符串
            String result=errorStream.stringBuffer.append("\n"+inputStream.stringBuffer+"\n").toString();
            //输出执行的命令信息
            if(outprintLog){
                logger.info("执行命令:{}，已执行完毕，执行结果:{}",cmd,result);
            }else{
                logger.info("执行命令:{}，已执行完毕",cmd);
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException("视频转换失败");
        }finally {
            if(process!=null){
                ProcessKiller ffmpegKiller=new ProcessKiller(process);
                runtime.addShutdownHook(ffmpegKiller);
            }
        }
    }

    /**
     * 程序退出前结束已有的ffmpeg进程
     */
    private static class ProcessKiller extends Thread{
        private Process process;

        public ProcessKiller(Process process){this.process=process;}

        @Override
        public void run(){ this.process.destroy();}
    }

    /**
     * 取出ffmpeg线程执行过程中产生的各种输出和错误流的信息
     */
    static class PrintStream extends Thread{
        InputStream inputStream=null;
        BufferedReader bufferedReader=null;
        StringBuffer stringBuffer=new StringBuffer();

        public PrintStream(InputStream inputStream){
            this.inputStream=inputStream;
        }

        @Override
        public void run(){
            try{
                if(inputStream==null){
                    return;
                }
                bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
                String line=null;
                while((line=bufferedReader.readLine())!=null){
                    stringBuffer.append(line);
                }
            }catch (Exception e){
                logger.error("读取输入流出错，错误信息："+e.getMessage());
            }finally {
                if(bufferedReader!=null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        logger.error("bufferedReader资源关闭失败");
                    }
                }
                if(inputStream!=null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        logger.error("inputStream资源关闭失败");
                    }
                }
            }
        }
    }
}
