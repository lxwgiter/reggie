package com.lxw.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxw.reggie.entity.DishFlavor;
import com.lxw.reggie.mapper.DishFlavorMapper;
import com.lxw.reggie.service.CategoryService;
import com.lxw.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {

}
