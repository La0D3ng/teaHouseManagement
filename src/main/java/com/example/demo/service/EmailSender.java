package com.example.demo.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮件发送服务
 * 用于发送验证码邮件
 */
public class EmailSender {
    
    // 邮箱配置 - 根据实际情况修改
    private static final String SMTP_HOST = "smtp.163.com"; // SMTP服务器地址（以163邮箱为例）
    private static final String SMTP_PORT = "465"; // SMTP端口
    private static final String FROM_EMAIL = "teahousemanagement@163.com"; // 发件人邮箱
    private static final String AUTH_CODE = "TGfXvCpjJAfXgsqh"; // 邮箱授权码（不是密码）
    
    // 验证码有效期（2分钟）
    private static final long CODE_VALID_TIME = 2 * 60 * 1000;
    
    // 验证码存储：key=邮箱地址，value=验证码信息
    private static final Map<String, CodeInfo> verificationCodes = new ConcurrentHashMap<>();
    
    /**
     * 验证码信息内部类
     */
    private static class CodeInfo {
        String code;
        long expireTime;
        
        CodeInfo(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }
    
    /**
     * 生成6位随机验证码
     */
    private static String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 发送验证码到指定邮箱
     * @param emailAddress 收件人邮箱地址
     * @return 是否发送成功
     */
    public static boolean sendVerificationCode(String emailAddress) {
        try {
            // 生成验证码
            String code = generateCode();
            
            // 配置邮件服务器
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            
            // SSL 配置（端口 465）
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", SMTP_PORT);
            props.put("mail.smtp.socketFactory.fallback", "false");
            
            // 设置超时时间
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.connectiontimeout", "10000");
            
            // 启用调试模式（可选，用于排查问题）
            // props.put("mail.debug", "true");
            
            // 创建认证器
            Authenticator auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, AUTH_CODE);
                }
            };
            
            // 创建会话
            Session session = Session.getInstance(props, auth);
            // session.setDebug(true); // 取消注释以启用调试输出
            
            // 创建邮件消息
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailAddress));
            message.setSubject("茶韵茶房管理系统 - 验证码：");
            
            // 邮件内容
            String content = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #333;'>验证码</h2>" +
                "<p>尊敬的用户您好！，</p>" +
                "<p>您的验证码是：</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; color: #007bff; letter-spacing: 5px;'>" +
                "%s" +
                "</div>" +
                "<p style='color: #666; margin-top: 20px;'>验证码有效期为 2 分钟，请尽快验证使用！</p>" +
                "<p style='color: #999; font-size: 12px; margin-top: 30px;'>如果这不是您的操作，请忽略此邮件。</p>" +
                "</div>" +
                "</body>" +
                "</html>",
                code
            );
            
            message.setContent(content, "text/html;charset=UTF-8");
            
            // 发送邮件
            Transport.send(message);
            
            // 存储验证码和过期时间
            long expireTime = System.currentTimeMillis() + CODE_VALID_TIME;
            verificationCodes.put(emailAddress, new CodeInfo(code, expireTime));
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("发送邮件失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证验证码是否正确
     * @param email 邮箱地址
     * @param code 用户输入的验证码
     * @return 验证码是否正确且未过期
     */
    public static boolean verifyCode(String email, String code) {
        CodeInfo codeInfo = verificationCodes.get(email);
        
        if (codeInfo == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > codeInfo.expireTime) {
            verificationCodes.remove(email);
            return false;
        }
        
        // 验证码比对
        return codeInfo.code.equals(code);
    }
    
    /**
     * 移除验证码
     * @param email 邮箱地址
     */
    public static void removeCode(String email) {
        verificationCodes.remove(email);
    }
    
    /**
     * 清理过期的验证码（可以通过定时任务调用）
     */
    public static void cleanExpiredCodes() {
        long currentTime = System.currentTimeMillis();
        verificationCodes.entrySet().removeIf(entry -> 
            currentTime > entry.getValue().expireTime
        );
    }
    
    // 保留旧方法以兼容现有代码
    @Deprecated
    public static boolean sendEmail(String emailAddress) {
        return sendVerificationCode(emailAddress);
    }
}
