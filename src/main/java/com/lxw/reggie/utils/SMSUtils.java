package com.lxw.reggie.utils;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.google.gson.Gson;
import darabonba.core.client.ClientOverrideConfiguration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 短信发送工具类
 */
public class SMSUtils {

	public static void sendMessage(String phone,String code)  {
		StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
				//accessKeyId、accessKeySecret在阿里云申请子用户
				.accessKeyId("LTAI5tQ1yGyPcm88yCUkE7fh")
				.accessKeySecret("yN7vx9VesHYnVCDHO029xFZZuJnQh2")
				.build());

		AsyncClient client = AsyncClient.builder()
				.region("cn-hangzhou") // Region ID
				.credentialsProvider(provider)
				.overrideConfiguration(
						ClientOverrideConfiguration.create()
								.setEndpointOverride("dysmsapi.aliyuncs.com")
				)
				.build();

		SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
				.signName("阿里云短信测试")
				.templateCode("SMS_154950909")
				.phoneNumbers(phone)
				.templateParam("{\"code\":\""+code+"\"}")
				.build();

		// Asynchronously get the return value of the API request
		CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
		// Synchronously get the return value of the API request
		SendSmsResponse resp = null;
		try {
			resp = response.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		System.out.println(new Gson().toJson(resp));
		client.close();

	}

}
