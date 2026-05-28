package com.mbc.mobileapp.filter;
//package com.mb.mobileapp.template.filter;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.ws.rs.core.HttpHeaders;
//
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import com.mb.mobileapp.common.bean.base.BaseResponse;
//import com.mb.mobileapp.common.bean.result.ResponseCode;
//import com.mb.mobileapp.common.bean.result.SimpleResult;
//import com.mb.mobileapp.common.constant.Constant;
//import com.mb.mobileapp.common.log.AppLog;
//import com.mb.mobileapp.common.utility.DateUtil;
//import com.mb.mobileapp.common.ws.DynamicKeyService;
//import com.mb.mobileapp.template.http.HttpRequestWrapper;
//import com.singalarity.dynamickey.DynamicKeyDecryptedResponse2;
//import com.singalarity.dynamickey.DynamicResponse;
//
//public class TransactionFilter implements Filter {
//
//	@Override
//	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
//			throws IOException, ServletException {
//		BufferedReader reader = null;
//		HttpServletResponse httpResponse;
//		boolean isError = false;
//		Gson gson = new Gson();
//		try {
//			HttpServletRequest httpRequest = (HttpServletRequest) request;
//			HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper(request);
//			String line;
//			StringBuilder sb = new StringBuilder();
//			reader = httpRequest.getReader();
//			while ((line = reader.readLine()) != null) {
//				sb.append(line);
//			}
//			JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
//			if (jsonObject != null && jsonObject.get(Constant.DATA_JSON) != null
//					&& jsonObject.get(Constant.VTAP.CONTENT_PROTECTOR_TROUBLESHOOTING_ID) != null) {
//				String tId = jsonObject.get(Constant.VTAP.CONTENT_PROTECTOR_TROUBLESHOOTING_ID).getAsString();
//				String messageId = tId + "-" + new SimpleDateFormat(DateUtil.TIME_SIMPLE_REVERSE).format(new Date());
//				if (httpRequest.getRequestURI()
//						.matches(Constant.VTAP.CONTENT_PROTECTOR_URI_REQUEST_TRANSPORT_KEY_REGEX)) {
//					DynamicKeyDecryptedResponse2 dynamicResponse = DynamicKeyService.getInstance()
//							.decryptedDataWithOtherKey(tId, messageId, tId,
//									jsonObject.get(Constant.DATA_JSON).getAsString());
//					if (Constant.VTAP.VTAP_TMS_ERROR_CODE_SUCCESS.equals(dynamicResponse.m_statusCode)) {
//						JsonObject clearJsonObject = gson.fromJson(dynamicResponse.getClearText(), JsonObject.class);
//						clearJsonObject.addProperty("transportKey", dynamicResponse.getTransportKey());
//						httpRequestWrapper
//								.resetInputStream(clearJsonObject.toString().getBytes(StandardCharsets.UTF_8.name()));
//						chain.doFilter(httpRequestWrapper, response);
//					} else {
//						isError = true;
//					}
//				} else {
//					DynamicResponse dynamicResponse = DynamicKeyService.getInstance().decryptedData(tId, messageId, tId,
//							jsonObject.get(Constant.DATA_JSON).getAsString());
//					if (Constant.VTAP.VTAP_TMS_ERROR_CODE_SUCCESS.equals(dynamicResponse.m_statusCode)) {
//						httpRequestWrapper.resetInputStream(
//								dynamicResponse.getClearText().getBytes(StandardCharsets.UTF_8.name()));
//						chain.doFilter(httpRequestWrapper, response);
//					} else {
//						isError = true;
//					}
//				}
//			} else if ((Constant.YES.equals(Constant.VTAP.CONTENT_PROTECTOR_BY_PASS)
//					|| httpRequest.getRequestURI().matches(Constant.VTAP.CONTENT_PROTECTOR_URI_REQUEST_BY_PASS_REGEX))
//					&& jsonObject != null) {
//				httpRequestWrapper.resetInputStream(jsonObject.toString().getBytes(StandardCharsets.UTF_8.name()));
//				chain.doFilter(httpRequestWrapper, response);
//			} else if (!RequestMethod.POST.toString().equals(httpRequestWrapper.getMethod())) {
//				chain.doFilter(httpRequestWrapper, response);
//			} else {
//				isError = true;
//			}
//		} catch (Exception e) {
//			AppLog.error(e);
//			isError = true;
//		} finally {
//			if (isError) {
//				BaseResponse baseResponse = new BaseResponse();
//				baseResponse.setResult(new SimpleResult("Internal Server Error. Please retry again!", false,
//						ResponseCode.DYNAMIC_KEY_DECRYPT_ERROR));
//				httpResponse = (HttpServletResponse) response;
//				httpResponse.setStatus(HttpServletResponse.SC_OK);
//				httpResponse.getOutputStream().write(gson.toJson(baseResponse).getBytes(StandardCharsets.UTF_8.name()));
//				httpResponse.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
//				httpResponse.flushBuffer();
//			}
//			if (reader != null) {
//				reader.close();
//			}
//		}
//	}
//}
