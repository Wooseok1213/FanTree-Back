package com.example.fantreehouse.domain.product.kakaopay.service;

import com.example.fantreehouse.common.config.KakaoPayConfig;
import com.example.fantreehouse.common.enums.ErrorType;
import com.example.fantreehouse.common.exception.CustomException;
import com.example.fantreehouse.domain.product.kakaopay.dto.KakaoPayApproveResponseDto;
import com.example.fantreehouse.domain.product.kakaopay.dto.KakaoPayCancelResponseDto;
import com.example.fantreehouse.domain.product.kakaopay.dto.KakaoPayReadyResponseDto;
import com.example.fantreehouse.domain.product.kakaopay.dto.KakaoPayRefundRequestDto;
import com.example.fantreehouse.domain.product.order.entity.Order;
import com.example.fantreehouse.domain.product.order.repository.OrdersRepository;
import com.example.fantreehouse.domain.user.entity.User;
import com.example.fantreehouse.domain.user.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KakaoPayService {

  private final KakaoPayConfig kakaoPayConfig;
  private final UserRepository userRepository;
  private final OrdersRepository ordersRepository;

  //카카오페이 요청 양식
  public KakaoPayReadyResponseDto kakaoPayReady(User user, Long orderId) {
    Order order = ordersRepository.findById(orderId).orElseThrow(
        ()-> new CustomException(ErrorType.NOT_FOUND_ORDER));
    Map<String, String> parameters = new HashMap<>();
    parameters.put("cid", kakaoPayConfig.cid);
    parameters.put("partner_order_id", String.valueOf(order.getId()));
    parameters.put("partner_user_id", String.valueOf(user.getId()));
    parameters.put("item_name", order.getOrderName());
    parameters.put("quantity", String.valueOf(order.getCount()));
    parameters.put("total_amount", String.valueOf(order.getTotalPrice()));
    parameters.put("tax_free_amount", String.valueOf(order.getTotalPrice()));
    parameters.put("approval_url", "http://localhost:8080/payments/success/"+order.getId()+ "/" +user.getId());
    parameters.put("cancel_url", "http://localhost:8080/payments/cancel");
    parameters.put("fail_url", "http://localhost:8080/payments/fail");

    HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(parameters,
        this.getHeaders());

    RestTemplate restTemplate = new RestTemplate();

    ResponseEntity<KakaoPayReadyResponseDto> response = restTemplate.postForEntity(
        "https://open-api.kakaopay.com/online/v1/payment/ready",
        requestEntity,
        KakaoPayReadyResponseDto.class);
    log.info(String.valueOf(response.getBody().getTid()));

    //order
    order.updateTid(response.getBody().getTid());

    log.info(order.getTid());
    ordersRepository.save(order);
    //카카오페이에서 tid를 응답으로 못 받았을 때
    ordersRepository.findByTid(order.getTid()).orElseThrow(
        ()-> new CustomException(ErrorType.NOT_FOUND_TID));

    return response.getBody();
  }

  private HttpHeaders getHeaders() {
    HttpHeaders headers = new HttpHeaders();
    String auth = "SECRET_KEY " + kakaoPayConfig.getSecretKey();
    headers.set("Authorization", auth);
    headers.set("Content-Type", "application/json; charset=utf-8");
    return headers;
  }

  public KakaoPayApproveResponseDto kakaoPayApprove(String pgToken,
      Long userId, Long orderId) {
    User user=userRepository.findById(userId).orElseThrow();
    Order order=ordersRepository.findById(orderId).orElseThrow(
        ()-> new CustomException(ErrorType.NOT_FOUND_ORDER));
    Map<String, String> parameters = new HashMap<>();
    parameters.put("cid", kakaoPayConfig.cid);
    parameters.put("tid", order.getTid());
    parameters.put("pg_token", pgToken);
    parameters.put("partner_order_id", String.valueOf(orderId));
    parameters.put("partner_user_id", String.valueOf(user.getId()));

    HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(parameters,
        this.getHeaders());

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<KakaoPayApproveResponseDto> response = restTemplate.postForEntity(
        "https://open-api.kakaopay.com/online/v1/payment/approve",
        requestEntity,
        KakaoPayApproveResponseDto.class);
    return response.getBody();
  }

  public KakaoPayCancelResponseDto kakaoPayCancel(KakaoPayRefundRequestDto requestDto, User user, Long orderId) {
    //취소하려는 주문이 없음(이미 취소된 주문)
    Order order = ordersRepository.findById(orderId).orElseThrow(
        ()-> new CustomException(ErrorType.NOT_FOUND_ORDER));
    Map<String, String> parameters = new HashMap<>();
    parameters.put("cid", kakaoPayConfig.cid);
    parameters.put("tid", order.getTid());
    parameters.put("cancel_amount", String.valueOf(requestDto.getCancelAmount()));
    parameters.put("cancel_tax_free_amount", String.valueOf(requestDto.getCancelTaxFreeAmount()));

    HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(parameters,
        this.getHeaders());

    RestTemplate restTemplate = new RestTemplate();

    ResponseEntity<KakaoPayCancelResponseDto> refundResponseDto = restTemplate.postForEntity(
        "https://open-api.kakaopay.com/online/v1/payment/cancel",
        requestEntity,
        KakaoPayCancelResponseDto.class);
    //환불 후 주문상태 '환불'로 바꾸기
    order.setOrderStatus(OrderStatus.REFUND);
    return refundResponseDto.getBody();
  }
}