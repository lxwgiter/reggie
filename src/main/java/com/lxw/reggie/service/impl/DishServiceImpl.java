package com.lxw.reggie.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxw.reggie.common.R;
import com.lxw.reggie.dto.DishDto;
import com.lxw.reggie.entity.Category;
import com.lxw.reggie.entity.Dish;
import com.lxw.reggie.entity.DishFlavor;
import com.lxw.reggie.mapper.DishMapper;
import com.lxw.reggie.service.CategoryService;
import com.lxw.reggie.service.DishFlavorService;
import com.lxw.reggie.service.DishService;
import com.lxw.reggie.utils.SimpleRedisLock;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    DishFlavorService dishFlavorService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    @Lazy
    CategoryService categoryService;

    /**
     * 新增菜品、同时保存对应口味
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存基本信息到菜品表dish,本身接收的参数类型为Dish，但是DishDTO为Dish子类，可选择性保存
        this.save(dishDto);
        //菜品id
        Long dishDtoId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors.stream().map(item ->{
           item.setDishId(dishDtoId);
           return item;
        }).collect(Collectors.toList());

        //保存你菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

        stringRedisTemplate.delete("cache:dish:" + dishDto.getCategoryId());
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {

        //删除图片
        Dish byId = this.getById(dishDto.getId());
        String imageName = byId.getImage();
        String imagePath = "D:/reggiePhoto" + File.separator + imageName;
        File file = new File(imagePath);
        file.delete();

        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

        stringRedisTemplate.delete("cache:dish:" + dishDto.getCategoryId());
    }

    @Override
    @Transactional
    public void deleteWithFlavor(Long id) {
        //删除图片
        Dish byId = this.getById(id);
        String imageName = byId.getImage();
        String imagePath = "D:/reggiePhoto" + File.separator + imageName;
        File file = new File(imagePath);
        file.delete();

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,id);
        dishFlavorService.remove(queryWrapper);

        //查询当前菜品的categoryId并删除当前菜品
        LambdaQueryWrapper<Dish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Dish::getId,id);
        Dish dish = this.getById(id);
        this.remove(queryWrapper1);

        stringRedisTemplate.delete("cache:dish:"+dish.getCategoryId());
    }

    @Override
    public R<List<DishDto>> listWithRedis(Dish dish) {
        //构造key
        String key = "cache:dish:" + dish.getCategoryId();
        //从redis中获取菜品信息
        String dtoListStr = stringRedisTemplate.opsForValue().get(key);
        List<DishDto> toList = JSONUtil.toList(dtoListStr, DishDto.class);

        if(toList.size() != 0){
            //若缓存中已存在数据，直接返回
            return R.success(toList);
        }
        //不存在，进行缓存重建
        SimpleRedisLock redisLock = new SimpleRedisLock("dish", stringRedisTemplate);
        boolean getLock = redisLock.tryLock(5);
        List<DishDto> dishDtos = null;
        try {
            if(!getLock){
                //没有成功拿到锁，线程休眠50毫秒后，继续去redis中查询
                Thread.sleep(50);
                return listWithRedis(dish);
            }else{
                dishDtos = getDishDtos(dish);
                //将查询到的数据写入redis
                stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(dishDtos),60, TimeUnit.MINUTES);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            redisLock.unlock();
        }
        return R.success(dishDtos);
    }

    private List<DishDto> getDishDtos(Dish dish) {
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //有了下面一句代码，可以增强该方法的拓展性，比如可以用来根据名称模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(dish.getName()),Dish::getName, dish.getName());
        queryWrapper.eq(null != dish.getCategoryId(),Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        List<Dish> dishes = list(queryWrapper);
        //将dishes集合转换为dishDto集合
        List<DishDto> dishDtos = dishes.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Category category = categoryService.getById(item.getCategoryId());

            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, item.getId());

            dishDto.setFlavors(dishFlavorService.list(queryWrapper1));
            return dishDto;
        }).collect(Collectors.toList());
        return dishDtos;
    }
}
