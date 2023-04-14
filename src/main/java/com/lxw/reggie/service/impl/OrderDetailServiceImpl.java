package com.lxw.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lxw.reggie.entity.OrderDetail;
import com.lxw.reggie.mapper.OrderDetailMapper;
import com.lxw.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}