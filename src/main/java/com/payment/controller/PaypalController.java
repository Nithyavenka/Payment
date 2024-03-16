package com.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.payment.entity.OrderPayment;
import com.payment.service.PaypalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

@Controller
public class PaypalController {

	@Autowired
	private PaypalService paypalService;

	public static final String successUrl = "http://localhost:8081/pay/success";
	public static final String cancelUrl = "http://localhost:8081/pay/cancel";

	@PostMapping("/pay")
	public String payment(@ModelAttribute("order") OrderPayment theOrder) throws PayPalRESTException {
		try {
			Payment thePayment = paypalService.createPayment(theOrder.getPrice(), theOrder.getCurrency(),
					theOrder.getMethod(), theOrder.getIntent(), theOrder.getDescription(), cancelUrl, successUrl);
			for (Links links : thePayment.getLinks()) {
				if (links.getRel().equals("approval_url")) {
					System.out.println(links.getHref());
					return "redirect:" + links.getHref();
				}
			}
		} catch (PayPalRESTException payPalRESTException) {
			payPalRESTException.printStackTrace();
		}
		return "redirect:/";
	}

	@GetMapping("/success")
	public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
		try {
			Payment payment = paypalService.executePayment(paymentId, payerId);
			System.out.println(payment.toJSON());
			if (payment.getState().equals("approved")) {
				return "success";
			}
		} catch (PayPalRESTException e) {
			System.out.println(e.getMessage());
		}
		return "redirect:/";
	}

}
