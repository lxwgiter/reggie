package com.lxw.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxw.reggie.common.CustomException;
import com.lxw.reggie.entity.Category;
import com.lxw.reggie.entity.Dish;
import com.lxw.reggie.entity.Setmeal;
import com.lxw.reggie.mapper.CategoryMapper;
import com.lxw.reggie.service.CategoryService;
import com.lxw.reggie.service.DishService;
import com.lxw.reggie.service.SetmealService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Resource
    private DishService dishService;

    @Resource
    private SetmealService setmealService;

    /**
     * 删除分类或者套餐，在分类下或者套餐下关联了菜品时，不能删除
     * @param id
     */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);

        if(count1 > 0){
            throw new CustomException("当前分类下关联了菜品，不能删除！");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if(count2 > 0){
            throw new CustomException("当前分类下关联了套餐，不能删除！");
        }
        //走到这里时，说明可以删除
        super.removeById(id);

    }
}
