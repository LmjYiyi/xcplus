package com.xuecheng.orders.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.xuecheng.orders.config.AlipayConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 演示Java版SDK
 */
@Controller
public class PayTestController {

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    //下单支付-由二维码访问这个接口，也可以手动访问这个接口 http://10.198.196.227:63030/orders/alipay/test
    @RequestMapping("/alipay/test")
    public void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY,AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        //回调地址。如果需要使用"支付结果通知"，那么这个setReturnUrl就可以填写，表示支付成功的话跳到哪个页面，使用return_url不能保证通知到位，"通知地址"和"回调地址"不用都写，写一个就行，我们一般不用这个setReturnUrl
        //alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");//可选
        //通知地址。如果需要使用"支付结果通知"，那么这个setReturnUrl就可以填写，表示支付成功的话，会通知请求哪个接口，使用notify_url可以保证通知到位，"通知地址"和"回调地址"不用都写，写一个就行，我们一般用这个setNotifyUrl
        alipayRequest.setNotifyUrl("http://kyjs9s.natappfree.cc/orders/alipay/test/notify");
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"812420100010101903\"," +
                "    \"total_amount\":0.1," +
                "    \"subject\":\"【撒大大】商品名称\"," +
                "    \"product_code\":\"QUICK_WAP_WAY\"" +
                "  }");
        String form = alipayClient.pageExecute(alipayRequest).getBody();
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
    }

    //查询支付状态-手动访问这个接口 http://localhost:63030/orders/alipay/test/query
    @RequestMapping("/alipay/test/query")
    public void queryPayResult() throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "812420100010101903");//自定义的订单号，这行跟下面那行，任选一行写
        //bizContent.put("trade_no", "xxxxxxxxxxxxxxxxxxxx");//支付宝那边真实的订单号
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            String resultJson = response.getBody();
            //转map
            Map resultMap = JSON.parseObject(resultJson, Map.class);
            Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
            //支付宝响应回来的数据，其中trade_status字段就是支付结果
            System.out.println("调用成功！支付宝响应回来的数据: " + alipay_trade_query_response);
        } else {
            System.out.println("调用失败");
        }
    }

    //支付结果通知(也叫异步通知)-内网穿透
    @RequestMapping("/alipay/test/notify")
    public void notify(HttpServletRequest request,HttpServletResponse response) throws IOException {

        //获取支付宝响应过来的反馈信息
        Map<String,String> params = new HashMap<>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }

        //自定义订单号
        String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
        //支付宝真实的订单号
        String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
        //支付状态
        String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
        //订单的创建时间
        String gmtCreate = new String(request.getParameter("gmt_create").getBytes("ISO-8859-1"),"UTF-8");
        //谁扫了这个订单的二维码
        String buyerLogonId = new String(request.getParameter("buyer_logon_id").getBytes("ISO-8859-1"),"UTF-8");
        //这个订单是哪个商家的
        String sellerEmail = new String(request.getParameter("seller_email").getBytes("ISO-8859-1"),"UTF-8");
        //商品名，这个可以不转编码，避免奇奇怪怪的商品名导致转码之后的文字乱码
        String subjectName = request.getParameter("subject");
        //商品金额
        String totalAmount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

        if(trade_status.equals("TRADE_SUCCESS")){//用户扫码付钱了，也收到这个钱了
            System.out.println("支付结果通知(异步通知): " + (trade_status.equals("TRADE_SUCCESS") ? "支付成功" : "支付失败"));
            System.out.println("系统订单号: "+ out_trade_no);
            System.out.println("真实的支付订单号: "+ trade_no);
            System.out.println("订单的创建时间: "+ gmtCreate);
            System.out.println("谁扫了这个订单的二维码: "+ buyerLogonId);
            System.out.println("这个订单是哪个商家的: "+ sellerEmail);
            System.out.println("商品名: "+ subjectName);
            System.out.println("商品金额: "+ totalAmount);
            response.getWriter().write("success");
        } else { //用户扫码付钱了，但是没收到这个钱
            response.getWriter().write("fail");
        }
    }
}