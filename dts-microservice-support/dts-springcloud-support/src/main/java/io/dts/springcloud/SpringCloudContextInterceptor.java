/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.dts.springcloud;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * @author liushiming
 * @version SpringCloudContextInterceptor.java, v 0.0.1 2017年11月20日 下午2:16:36 liushiming
 */
public class SpringCloudContextInterceptor implements RequestInterceptor, HandlerInterceptor {

  private static final String CONTEXT_HEADER_PARENT = "x-context-";

  @Override
  public void apply(RequestTemplate template) {
    Map<String, String> contexts = SpringCloudContext.getContext().getAttachments();
    for (Map.Entry<String, String> entry : contexts.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      template.header(CONTEXT_HEADER_PARENT + key, value);
    }
  }

  @Override
  public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2,
      Exception arg3) throws Exception {

  }

  @Override
  public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2,
      ModelAndView arg3) throws Exception {

  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    Enumeration<String> headerNames = request.getHeaderNames();
    if (headerNames != null) {
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        if (StringUtils.startsWithIgnoreCase(headerName, CONTEXT_HEADER_PARENT)) {
          String value = request.getHeader(headerName);
          String key = StringUtils.replace(headerName, CONTEXT_HEADER_PARENT, "");
          SpringCloudContext.getContext().setAttachment(key, value);
        }
      }
    }
    return true;
  }

}
