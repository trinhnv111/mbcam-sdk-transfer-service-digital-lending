package com.mbc.mobileapp.filter;
//package com.mb.mobileapp.template.filter;
//
//import java.io.IOException;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//
//import lombok.Generated;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import com.mb.mobileapp.common.log.AppLog;
//
//@Component
//@Order(2)
//public class RequestResponseLoggingFilter implements Filter {
//
//	@Generated
//	@Override
//	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
//			throws IOException, ServletException {
//		HttpServletRequest req = (HttpServletRequest) request;
//		AppLog.info("Logging Request  {" + req.getMethod() + "} : {" + req.getRequestURI() + "}");
//		chain.doFilter(request, response);
//	}
//
//}
