package com.lxw.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lxw.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    void remove(Long id);
}
