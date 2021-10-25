package com.example.server.utils.auth;

import com.alibaba.fastjson.JSON;
import com.example.imlib.utils.L;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import user.BaseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author Lehr
 * @create: 2020-02-03
 */
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
        // 从请求头中取出 token  这里需要和前端约定好把jwt放到请求头一个叫token的地方
        String token = request.getHeader("token");
        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod))
            return true;
        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod();
        //检查是否有passtoken注释，有则跳过认证
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                return true;
            }
        }
        //默认全部检查
        else {
            System.out.println("被jwt拦截需要验证 token==>" + token);
            // 执行认证
            if (StringUtils.isEmpty(token)) {
                L.p("preHandle token为null");
                //这里其实是登录失效,没token了   这个错误也是我自定义的，读者需要自己修改
                response(response);
                return false;
            }

            // 获取 token 中的 user Name
//            String userId = AuthUtil.getAudience(token);

            //找找看是否有这个user   因为我们需要检查用户是否存在，读者可以自行修改逻辑

            // 验证 token
            boolean res = AuthUtil.verifyToken(token);
            L.p("preHandle res==>" + res);
            if (!res) {
                response(response);
                return false;
            }

            //获取载荷内容
            String uuid = AuthUtil.getClaimByName(token, "uuid").asString();

            //放入attribute以便后面调用
            request.setAttribute("uuid", uuid);
            return true;

        }
        return true;
    }

    private void response(HttpServletResponse response) {
        response.setContentType("Content-Type: application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        BaseEntity res = BaseEntity.fail(BaseEntity.FAIL_TOKEN);
        try {
            response.getWriter().write(JSON.toJSONString(res));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {
    }
}

