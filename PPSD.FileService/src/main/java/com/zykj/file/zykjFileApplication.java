/*
 *
 *      Copyright (c) 2018-2025, tony All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the mingtian-group.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: tony (117332652@qq.com)
 *
 */

package com.zykj.file;


import com.zykj.common.security.annotation.EnablezykjFeignClients;
import com.zykj.common.security.annotation.EnablezykjResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author tony
 * @date 2018年06月21日
 * 认证授权中心
 */
@ComponentScan({
		"com.zykj.common.api.config",
		"com.zykj.common.core",
		"com.zykj.file"
})
@SpringCloudApplication
@EnablezykjFeignClients
@EnablezykjResourceServer
public class zykjFileApplication {

	public static void main(String[] args) {
		SpringApplication.run(zykjFileApplication.class, args);
	}
}
