package com.cxytiandi.job.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 钉钉机器人发送消息
 * https://open-doc.dingtalk.com/docs/doc.htm?spm=a219a.7629140.0.0.karFPe&treeId=257&articleId=105735&docType=1
 * @author yinjihuan
 *
 */
public class DingDingMessageUtil {

	public static String access_token = "填写你自己申请的token";

	public static void sendTextMessage(String msg) {
		try {
			Message message = new Message();
			message.setMsgtype("text");
			message.setText(new MessageInfo(msg));
			URL url = new URL("https://oapi.dingtalk.com/robot/send?access_token=" + access_token);
			// 建立http连接
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Type", "application/Json; charset=UTF-8");
			conn.connect();
			OutputStream out = conn.getOutputStream();
			String textMessage = JsonUtils.toJson(message);
			byte[] data = textMessage.getBytes();
			out.write(data);
			out.flush();
			out.close();
			System.out.println(conn.getResponseCode());
			InputStream in = conn.getInputStream();
			byte[] data1 = new byte[in.available()];
			in.read(data1);
			System.out.println(new String(data1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		DingDingMessageUtil.sendTextMessage("hello");
	}
}

class Message {
	private String msgtype;
	private MessageInfo text;
	public String getMsgtype() {
		return msgtype;
	}
	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}
	public MessageInfo getText() {
		return text;
	}
	public void setText(MessageInfo text) {
		this.text = text;
	}
}

class MessageInfo {
	private String content;
	public MessageInfo(String content) {
		this.content = content;
	}
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}