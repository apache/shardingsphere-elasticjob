/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.console.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.dangdang.ddframe.job.console.controller.RegistryCenterController;
import com.dangdang.ddframe.job.console.domain.RegistryCenterClient;
import com.dangdang.ddframe.job.console.util.SessionCuratorClient;

public final class CuratorSessionClientInterceptor extends HandlerInterceptorAdapter {
    
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        RegistryCenterClient client = (RegistryCenterClient) request.getSession().getAttribute(RegistryCenterController.CURATOR_CLIENT_KEY);
        if (null == client || !client.isConnected()) {
            return false;
        }
        SessionCuratorClient.setCuratorClient(client.getCuratorClient());
        return true;
    }
    
    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final ModelAndView modelAndView) throws Exception {
        SessionCuratorClient.clear();
    }
}
